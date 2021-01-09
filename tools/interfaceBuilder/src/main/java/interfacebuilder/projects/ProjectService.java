// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.projects;

import com.ahli.galaxy.game.GameData;
import interfacebuilder.build.MpqBuilderService;
import interfacebuilder.compress.RuleSet;
import interfacebuilder.projects.enums.Game;
import interfacebuilder.ui.navigation.NavigationController;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.List;

/**
 * ProjectService manages Observer Interface project related tasks.
 */
public class ProjectService {
	private static final String DIRECTORY_SYMBOL = "/";
	private static final Logger logger = LogManager.getLogger(ProjectService.class);
	private final ProjectJpaRepository projectRepo;
	private final MpqBuilderService mpqBuilderService;
	private final NavigationController navigationController;
	
	public ProjectService(
			final ProjectJpaRepository projectRepo,
			final MpqBuilderService mpqBuilderService,
			final NavigationController navigationController) {
		this.projectRepo = projectRepo;
		this.mpqBuilderService = mpqBuilderService;
		this.navigationController = navigationController;
	}
	
	/**
	 * @param project
	 * @throws IOException
	 */
	public static void createTemplateProjectFiles(final Project project) throws IOException {
		final String jarIntPath;
		if (project.getGame() == Game.SC2) {
			jarIntPath = "templates/sc2/interface/";
		} else {
			jarIntPath = "templates/heroes/interface/";
		}
		
		final Path projPath = Path.of(project.getProjectPath());
		final var resolver = new PathMatchingResourcePatternResolver();
		logger.trace("creating template");
		final int jarIntPathLen = jarIntPath.length();
		for (final Resource res : resolver.getResources("classpath*:" + jarIntPath + "**")) {
			final String uri = res.getURI().toString();
			logger.trace("extracting file {}", uri);
			final int i = uri.indexOf(jarIntPath);
			final String intPath = uri.substring(i + jarIntPathLen);
			final Path path = projPath.resolve(intPath);
			logger.trace("writing file {}", path);
			if (!uri.endsWith(DIRECTORY_SYMBOL)) {
				Files.createDirectories(path.getParent());
				//noinspection ObjectAllocationInLoop
				try (final InputStream in = new BufferedInputStream(res.getInputStream(), 1_024)) {
					Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
				}
			} else {
				Files.createDirectories(path);
			}
		}
		logger.trace("create template finished");
	}
	
	/**
	 * Returns whether the specified path contains a compilable file for the specified game.
	 *
	 * @param path
	 * @param game
	 * @return
	 */
	public static boolean pathContainsCompileableForGame(final String path, final GameData game) {
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
		return ProjectEntity.toProjects(projectRepo.findAll());
	}
	
	/**
	 * Saves the specified project to the database.
	 *
	 * @param project
	 * @return updated instance
	 * @throws DataAccessException
	 */
	public Project saveProject(final Project project) {
		try {
			return ProjectEntity.toProject(projectRepo.save(ProjectEntity.fromProject(project)));
		} catch (final DataAccessException e) {
			logger.error("Saving project failed", e);
			throw e;
		}
	}
	
	/**
	 * Builds the specified projects.
	 *
	 * @param projects
	 * @param useCmdLineSettings
	 */
	public void build(final Iterable<Project> projects, final boolean useCmdLineSettings) {
		boolean building = false;
		for (final Project project : projects) {
			building = true;
			mpqBuilderService.build(project, useCmdLineSettings);
		}
		if (building) {
			// switch to progress
			navigationController.clickProgress();
		}
	}
	
	/**
	 * Removes the specified Project from the database.
	 *
	 * @param project
	 */
	public void deleteProject(final Project project) {
		projectRepo.delete(ProjectEntity.fromProject(project));
	}
	
	/**
	 * Returns a list of Projects using the specified path.
	 *
	 * @param path
	 * @return list of Projects with matching path
	 */
	public List<Project> getProjectsOfPath(final String path) {
		return ProjectEntity.toProjects(projectRepo.findAll(new ProjectsPathMatcher(path)));
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
			final ProjectEntity project2 = projectRepo.getOne(project.getId());
			try {
				Hibernate.initialize(project2.getBestCompressionRuleSet());
			} catch (final HibernateException e) {
				logger.error("Error while fetching compression rule set from DB.", e);
			}
			project.setBestCompressionRuleSet(project2.getBestCompressionRuleSet());
		}
		return project.getBestCompressionRuleSet();
	}
	
	private static final class ProjectsPathMatcher implements Example<ProjectEntity> {
		private final String path;
		
		private ProjectsPathMatcher(final String path) {
			this.path = path;
		}
		
		@Override
		public ProjectEntity getProbe() {
			return new ProjectEntity(null, path, null);
		}
		
		@Override
		public ExampleMatcher getMatcher() {
			return ExampleMatcher.matchingAll().withIgnoreNullValues();
		}
	}
}
