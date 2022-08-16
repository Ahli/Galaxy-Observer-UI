package com.ahli.interfacebuilder.threads;

import com.ahli.galaxy.game.Game;
import com.ahli.memory.StringInterner;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.BeanCreationNotAllowedException;
import org.springframework.lang.NonNull;

import java.util.concurrent.ForkJoinPool;

@Log4j2
public class TaskCleaner implements CleaningForkJoinTaskCleaner {
	private final Game sc2BaseGame;
	private final Game heroesBaseGame;
	private ForkJoinPool executor;
	
	public TaskCleaner(final Game sc2BaseGame, final Game heroesBaseGame) {
		this.sc2BaseGame = sc2BaseGame;
		this.heroesBaseGame = heroesBaseGame;
	}
	
	@Override
	public void setExecutor(@NonNull final ForkJoinPool executor) {
		this.executor = executor;
	}
	
	/**
	 * After a short delay, the app attempts to clean up its resources,
	 */
	@Override
	@SuppressWarnings("java:S1215")
	public void tryCleanUp() {
		// new delayed thread is required, else the ForkJoinPool is not finished
		new Thread(() -> {
			// free space of baseUI
			if (executor != null && executor.isQuiescent()) {
				log.debug("Freeing up resources");
				// TODO try to get red if lazy beans to avoid this exception
				try {
					sc2BaseGame.setUiCatalog(null);
					heroesBaseGame.setUiCatalog(null);
				} catch (final BeanCreationNotAllowedException e) {
					log.trace("Failed to instantiate lazy beans.", e);
				}
				// GC1 is the default GC and can now release RAM -> actually good to do after a task because we use a
				// lot of RAM for the UIs
				// Weak References survive 3 garbage collections by default
				System.gc();
				System.gc();
				System.gc();
				System.runFinalization();
				// clean up StringInterner's weak references that the GC removed
				log.trace("string interner size before cleaning: {}", StringInterner.size()); // instant calc!
				StringInterner.cleanUpGarbage();
				log.trace("string interner size after cleaning: {}", StringInterner::size);
				// TODO not all Strings are removed for some reason
				//				if (StringInterner.size() < 10) {
				//					log.trace("interner content: \n{}", StringInterner.print());
				//				}
			}
		}).start();
	}
	
}
