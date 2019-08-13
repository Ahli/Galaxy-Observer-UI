// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.projects;

import interfacebuilder.InterfaceBuilderApp;
import interfacebuilder.projects.enums.Game;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith (MockitoExtension.class)
@SpringBootTest (classes = InterfaceBuilderApp.class)
public class ProjectServiceTest {
	
	@MockBean
	private ProjectJpaRepository projectRepoMock;
	
	@InjectMocks
	private ProjectService projectService;
	
	@Test
	public void testGetAllProjects() {
		final Project project1 = new Project("name1", "path1", Game.SC2);
		project1.setId(1);
		final Project project2 = new Project("name2", "path2", Game.HEROES);
		project2.setId(2);
		when(projectRepoMock.findAll()).thenReturn(Arrays.asList(project1, project2));
		
		final int numberOfProjects = projectService.getAllProjects().size();
		assertEquals(2, numberOfProjects, "not fetching all existing projects");
	}
	
	@Test
	public void testSaveProject() {
		final Project project = new Project("name", "path", Game.SC2);
		project.setId(1);
		when(projectRepoMock.save(project)).thenReturn(project);
		
		final Project savedProject = projectService.saveProject(project);
		assertEquals(project, savedProject, "saving altered project");
	}
	
}
