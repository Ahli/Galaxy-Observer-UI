// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.projects;

import com.ahli.interfacebuilder.compress.RuleSet;
import com.ahli.interfacebuilder.projects.enums.GameType;
import jakarta.persistence.PersistenceException;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Hibernate;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * ProjectService manages Observer Interface project related tasks.
 */
@Log4j2
public class ProjectService {
	private static final String DIRECTORY_SYMBOL = "/";
	private final ProjectJpaRepository projectRepo;
	
	public ProjectService(final ProjectJpaRepository projectRepo) {
		this.projectRepo = projectRepo;
	}
	
	/**
	 * @param project
	 * @throws IOException
	 */
	public void createTemplateProjectFiles(final Project project) throws IOException {
		final String jarIntPath;
		if (project.getGameType() == GameType.SC2) {
			jarIntPath = "templates/sc2/interface/";
		} else {
			jarIntPath = "templates/heroes/interface/";
		}
		
		final Path projPath = project.getProjectPath();
		final var resolver = new PathMatchingResourcePatternResolver();
		log.trace("creating template");
		final int jarIntPathLen = jarIntPath.length();
		for (final Resource res : resolver.getResources("classpath*:" + jarIntPath + "**")) {
			final String uri = res.getURI().toString();
			log.trace("extracting file {}", uri);
			final int i = uri.indexOf(jarIntPath);
			final String intPath = uri.substring(i + jarIntPathLen);
			final Path path = projPath.resolve(intPath);
			log.trace("writing file {}", path);
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
		log.trace("create template finished");
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
			log.error("Saving project failed", e);
			throw e;
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
	public List<Project> getProjectsOfPath(final Path path) {
		return ProjectEntity.toProjects(projectRepo.findAllByProjectPath(path.toString()));
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
			final ProjectEntity project2 = projectRepo.getReferenceById(project.getId());
			try {
				final RuleSet ruleSet = Hibernate.unproxy(project2.getBestCompressionRuleSet(), RuleSet.class);
				project.setBestCompressionRuleSet(ruleSet);
			} catch (final PersistenceException e) {
				log.error("Error while fetching compression rule set from DB.", e);
			}
		}
		return project.getBestCompressionRuleSet();
	}
	
}
