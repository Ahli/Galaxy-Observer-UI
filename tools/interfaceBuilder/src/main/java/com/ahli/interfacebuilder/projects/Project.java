// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.projects;

import com.ahli.interfacebuilder.compress.RuleSet;
import com.ahli.interfacebuilder.projects.enums.GameType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
public class Project {
	private final long id;
	private String name;
	private Path projectPath;
	private GameType gameType;
	private LocalDateTime lastBuildDateTime;
	private long lastBuildSize;
	private RuleSet bestCompressionRuleSet;
	
	public Project(final String name, final Path projectPath, final GameType gameType) {
		this.name = name;
		this.projectPath = projectPath;
		this.gameType = gameType;
		id = 0;
	}
	
	@Override
	public final boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof final Project project)) {
			return false;
		}
		return id == project.id && lastBuildSize == project.lastBuildSize && Objects.equals(name, project.name) &&
				Objects.equals(projectPath, project.projectPath) && gameType == project.gameType &&
				Objects.equals(lastBuildDateTime, project.lastBuildDateTime) &&
				Objects.equals(bestCompressionRuleSet, project.bestCompressionRuleSet);
	}
	
	@Override
	public final int hashCode() {
		return Objects.hash(id, name, projectPath, gameType, lastBuildDateTime, lastBuildSize, bestCompressionRuleSet);
	}
}
