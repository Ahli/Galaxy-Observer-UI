// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.projects;

import interfacebuilder.compress.RuleSet;
import interfacebuilder.projects.enums.Game;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

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
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "project")
@Data
@Builder
@AllArgsConstructor
public class ProjectEntity implements Serializable {
	
	@Serial
	private static final long serialVersionUID = -7732465886025160494L;
	
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
				.projectPath(Path.of(entity.getProjectPath()))
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
				.projectPath(project.getProjectPath().toString())
				.game(project.getGame())
				.lastBuildDateTime(project.getLastBuildDateTime())
				.lastBuildSize(project.getLastBuildSize())
				.bestCompressionRuleSet(project.getBestCompressionRuleSet())
				.build();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof final ProjectEntity that)) {
			return false;
		}
		return Objects.equals(name, that.name) && Objects.equals(projectPath, that.projectPath) && game == that.game &&
				Objects.equals(lastBuildDateTime, that.lastBuildDateTime) &&
				Objects.equals(lastBuildSize, that.lastBuildSize) &&
				Objects.equals(bestCompressionRuleSet, that.bestCompressionRuleSet);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(name, projectPath, game, lastBuildDateTime, lastBuildSize, bestCompressionRuleSet);
	}
}
