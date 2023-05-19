// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.projects;

import com.ahli.interfacebuilder.SpringBootApplication;
import com.ahli.interfacebuilder.projects.enums.GameType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@DataJpaTest
@ContextConfiguration(classes = SpringBootApplication.class)
@Execution(ExecutionMode.SAME_THREAD)
final class ProjectServiceTest {
	
	@MockBean
	private ProjectJpaRepository projectRepoMock;
	
	@Autowired
	private ProjectService projectService;
	
	@Test
	void testGetAllProjects() {
		final ProjectEntity project1 =
				ProjectEntity.builder().id(1L).name("name1").projectPath("path1").gameType(GameType.SC2).build();
		final ProjectEntity project2 =
				ProjectEntity.builder().id(2L).name("name2").projectPath("path2").gameType(GameType.HEROES).build();
		when(projectRepoMock.findAll()).thenReturn(Arrays.asList(project1, project2));
		
		final int numberOfProjects = projectService.getAllProjects().size();
		assertEquals(2, numberOfProjects, "not fetching all existing projects");
	}
	
	@Test
	void testSaveProject() {
		final Project project =
				Project.builder().id(1).name("name").projectPath(Path.of("/test/")).gameType(GameType.SC2).build();
		final ProjectEntity projectEntity = ProjectEntity.fromProject(project);
		when(projectRepoMock.save(projectEntity)).thenReturn(projectEntity);
		
		final Project savedProject = projectService.saveProject(project);
		assertEquals(project, savedProject, "saving altered project");
	}
	
}
