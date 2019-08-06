// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.projects;

import com.ahli.galaxy.game.GameData;
import interfacebuilder.build.MpqBuilderService;
import interfacebuilder.compress.RuleSet;
import interfacebuilder.projects.enums.Game;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.List;

/**
 * ProjectService manages Observer Interface project related tasks.
 */
public class ProjectService {
	private static final Logger logger = LogManager.getLogger(ProjectService.class);
	
	@Autowired
	private ProjectJpaRepository projectRepo;
	
	@Autowired
	private MpqBuilderService mpqBuilderService;
	
	/**
	 * Returns whether the specified path contains a compilable file for the specified game.
	 *
	 * @param path
	 * @param game
	 * @return
	 */
	public boolean pathContainsCompileableForGame(final String path, final GameData game) {
		// detection via layout file ending as defined in the GameData's GameDef
		final String[] extensions = new String[1];
		extensions[0] = game.getGameDef().getLayoutFileEnding();
		final IOFileFilter filter = new SuffixFileFilter(extensions, IOCase.INSENSITIVE);
		final Iterator<File> iter = FileUtils.iterateFiles(new File(path), filter, TrueFileFilter.INSTANCE);
		
		while (iter.hasNext()) {
			if (iter.next().isFile()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns all Projects.
	 *
	 * @return
	 */
	public List<Project> getAllProjects() {
		return projectRepo.findAll();
	}
	
	/**
	 * Saves the specified project to the database.
	 *
	 * @param project
	 * @return updated instance
	 */
	public Project saveProject(final Project project) {
		try {
			return projectRepo.save(project);
		} catch (final DataAccessException e) {
			logger.error(e);
			return project;
		}
	}
	
	/**
	 * Builds the specified projects.
	 *
	 * @param projects
	 * @param useCmdLineSettings
	 */
	public void build(final Iterable<Project> projects, final boolean useCmdLineSettings) {
		for (final Project project : projects) {
			mpqBuilderService.build(project, useCmdLineSettings);
		}
	}
	
	/**
	 * Removes the specified Project from the database.
	 *
	 * @param project
	 */
	public void deleteProject(final Project project) {
		projectRepo.delete(project);
	}
	
	/**
	 * Returns a list of Projects using the specified path.
	 *
	 * @param path
	 * @return list of Projects with matching path
	 */
	public List<Project> getProjectsOfPath(final String path) {
		return projectRepo.findAll(new Example<>() {
			@Override
			public Project getProbe() {
				return new Project(null, path, null);
			}
			
			@Override
			public ExampleMatcher getMatcher() {
				return ExampleMatcher.matchingAll().withIgnoreNullValues();
			}
		});
	}
	
	
	/**
	 * Initializes the BestCompressionRuleSet field of the specified project and returns it.
	 *
	 * @param project
	 * @return
	 */
	@Transactional
	public RuleSet fetchBestCompressionRuleSet(final Project project) {
		if (!Hibernate.isInitialized(project.getBestCompressionRuleSet())) {
			// grab from DB wire compression rules to old instance
			final Project project2 = projectRepo.getOne(project.getId());
			Hibernate.initialize(project2.getBestCompressionRuleSet());
			project.setBestCompressionRuleSet(project2.getBestCompressionRuleSet());
		}
		return project.getBestCompressionRuleSet();
	}
	
	public void createTemplateProjectFiles(final Project project) throws IOException {
		final String intPath;
		if (project.getGame() == Game.SC2) {
			intPath = "classpath:res/templates/sc2/interface";
		} else {
			intPath = "classpath:res/templates/heroes/interface";
		}
		final Path projPath = Path.of(project.getProjectPath());
		final var resolver = new PathMatchingResourcePatternResolver();
		logger.trace("creating template");
		for (final Resource res : resolver.getResources(intPath)) {
			logger.trace("writing file " + intPath);
			try (final InputStream in = new BufferedInputStream(res.getInputStream(), 1024);
			     final OutputStream out = Files.newOutputStream(projPath)) {
				
				Files.copy(in, Path.of(projPath + File.separator + res.getFilename()),
						StandardCopyOption.REPLACE_EXISTING);
			}
		}
		logger.trace("create template finished");
	}
}
