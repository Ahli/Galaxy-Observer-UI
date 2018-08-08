package interfacebuilder.projects;

import interfacebuilder.projects.enums.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith (MockitoExtension.class)
@SpringBootTest
public class ProjectServiceTest {
	@Mock
	ProjectJpaRepository projectRepoMock;
	
	@InjectMocks
	private ProjectService projectService;
	
	@BeforeEach
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testGetAllProjects() {
		final Project project1 = new Project("name1", "path1", Game.SC2);
		project1.setId(1);
		final Project project2 = new Project("name2", "path2", Game.HEROES);
		project2.setId(2);
		when(projectRepoMock.findAll()).thenReturn(Arrays.asList(project1, project2));
		
		final int numberOfProjects = projectService.getAllProjects().size();
		assertEquals(2, numberOfProjects);
	}
	
	@Test
	public void testSaveProject() {
		final Project project = new Project("name", "path", Game.SC2);
		project.setId(1);
		when(projectRepoMock.save(project)).thenReturn(project);
		
		final Project savedProject = projectService.saveProject(project);
		assertEquals(project, savedProject);
	}
	
}
