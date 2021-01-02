// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.projects;

import interfacebuilder.compress.RuleSet;
import interfacebuilder.projects.enums.Game;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//import jakarta.persistence.CascadeType;
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.EnumType;
//import jakarta.persistence.Enumerated;
//import jakarta.persistence.FetchType;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.OneToOne;
//import jakarta.persistence.Table;

@Entity
@Table(name = "project")
@Data
@Builder
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class ProjectEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	@Column(/*unique = false,*/ nullable = false, length = 30)
	private String name;
	
	@Column(unique = true, nullable = false/*, length = 255*/)
	private String projectPath;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 8)
	private Game game;
	
	@Column(name = "lastBuildDate")
	private LocalDateTime lastBuildDateTime;
	
	@Column
	private Long lastBuildSize;
	
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	private RuleSet bestCompressionRuleSet;
	
	public ProjectEntity() {
		// required
	}
	
	public ProjectEntity(final String name, final String projectPath, final Game game) {
		this.name = name;
		this.projectPath = projectPath;
		this.game = game;
	}
	
	public static List<Project> toProjects(final Collection<ProjectEntity> projectEntities) {
		final List<Project> list = new ArrayList<>(projectEntities.size());
		for (final ProjectEntity entity : projectEntities) {
			list.add(ProjectEntity.toProject(entity));
		}
		return list;
	}
	
	public static Project toProject(final ProjectEntity entity) {
		return new Project.ProjectBuilder().id(entity.getId())
				.name(entity.getName())
				.projectPath(entity.getProjectPath())
				.game(entity.getGame())
				.lastBuildDateTime(entity.getLastBuildDateTime())
				.lastBuildSize(entity.getLastBuildSize() != null ? entity.getLastBuildSize() : 0)
				.bestCompressionRuleSet(entity.getBestCompressionRuleSet())
				.build();
	}
	
	public static ProjectEntity fromProject(final Project project) {
		return ProjectEntity.builder()
				.id(project.getId())
				.name(project.getName())
				.projectPath(project.getProjectPath())
				.game(project.getGame())
				.lastBuildDateTime(project.getLastBuildDateTime())
				.lastBuildSize(project.getLastBuildSize())
				.bestCompressionRuleSet(project.getBestCompressionRuleSet())
				.build();
	}
	
}
