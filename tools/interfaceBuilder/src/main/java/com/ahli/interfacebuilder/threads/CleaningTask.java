// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.threads;

import com.ahli.galaxy.game.Game;
import com.ahli.interfacebuilder.integration.log4j.StylizedTextAreaAppender;
import com.ahli.memory.StringInterner;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
public abstract class CleaningTask implements Runnable {
	private static final AtomicInteger tasksRunning = new AtomicInteger();
	private static final AtomicBoolean shutDown = new AtomicBoolean();
	private final Game sc2Game;
	private final Game heroesGame;
	
	protected CleaningTask(final Game sc2Game, final Game heroesGame) {
		this.sc2Game = sc2Game;
		this.heroesGame = heroesGame;
	}
	
	public static void shutDown() {
		shutDown.set(true);
	}
	
	public static boolean hasShutDown() {
		return shutDown.get();
	}
	
	public static void waitForTasksToComplete(int timeout, final int sleepStep) throws InterruptedException {
		while (timeout > 0 && CleaningTask.hasTasksRunning()) {
			timeout -= sleepStep;
			Thread.sleep(sleepStep);
		}
	}
	
	public static boolean hasTasksRunning() {
		return tasksRunning.get() != 0;
	}
	
	public void start() {
		tasksRunning.getAndIncrement();
	}
	
	public void end() {
		tasksRunning.decrementAndGet();
		tryToCleanUp(sc2Game, heroesGame);
	}
	
	public static void tryToCleanUp(final Game sc2Game, final Game heroesGame) {
		StylizedTextAreaAppender.finishedWork(Thread.currentThread().getName(), true, 50);
		// free space of baseUI
		if (tasksRunning.get() == 0) {
			log.debug("Freeing up resources");
			// TODO try to get rid of lazy beans to avoid this exception
			//			try {
			if (sc2Game != null) {
				sc2Game.setUiCatalog(null);
			}
			if (heroesGame != null) {
				heroesGame.setUiCatalog(null);
			}
			//			} catch (final BeanCreationNotAllowedException e) {
			//				log.trace("Failed to instantiate lazy beans.", e);
			//			}
			// G1 is the default GC and can now release RAM -> actually good to do after a task because we use a
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
	}
	
}
