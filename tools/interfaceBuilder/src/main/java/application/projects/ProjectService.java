package application.projects;

import application.build.MpqBuilderService;
import com.ahli.galaxy.game.GameData;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * ProjectService manages Observer Interface project related tasks.
 */
public class ProjectService {
	private static final Logger logger = LogManager.getLogger();
	
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
	
	public Project addProject(final Project project) {
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
	public void build(final List<Project> projects, final boolean useCmdLineSettings) {
		for (final Project project : projects) {
			mpqBuilderService.build(project, useCmdLineSettings);
		}
	}
	
	public void deleteProject(final Project project) {
		projectRepo.delete(project);
	}
}
