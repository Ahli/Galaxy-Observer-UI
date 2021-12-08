// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.projects;

import com.ahli.interfacebuilder.compress.RuleSet;
import com.ahli.interfacebuilder.projects.enums.GameType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public final class ProjectEntity implements Serializable {
	
	@Serial
	private static final long serialVersionUID = -7732465886025160494L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false)
	private Long id;
	
	@Column(name = "name", nullable = false, length = 30)
	private String name;
	
	@Column(name = "project_path", unique = true, nullable = false, length = 255)
	private String projectPath;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "game", nullable = false, length = 8)
	private GameType gameType;
	
	@Column(name = "last_build_date")
	private LocalDateTime lastBuildDateTime;
	
	@Column(name = "last_build_size")
	private Long lastBuildSize;
	
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	@JoinColumn(name = "best_compression_rule_set_id", nullable = true)
	@ToString.Exclude
	private RuleSet bestCompressionRuleSet;
	
	private ProjectEntity() {
		// required for hibernate
	}
	
	public ProjectEntity(final String name, final String projectPath, final GameType gameType) {
		this.name = name;
		this.projectPath = projectPath;
		this.gameType = gameType;
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
				.gameType(entity.getGameType())
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
				.gameType(project.getGameType())
				.lastBuildDateTime(project.getLastBuildDateTime())
				.lastBuildSize(project.getLastBuildSize())
				.bestCompressionRuleSet(project.getBestCompressionRuleSet())
				.build();
	}
	
	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
			return false;
		}
		final ProjectEntity that = (ProjectEntity) o;
		// only compare primary keys
		return Objects.equals(id, that.id);
	}
	
	@Override
	public int hashCode() {
		// for generated primary keys, the hashcode must be constant before and after
		return 0;
	}
	
}
