// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.projects;

import interfacebuilder.compress.RuleSet;
import interfacebuilder.projects.enums.Game;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
@ToString
@Builder
@AllArgsConstructor
public class Project {
	private final int id;
	private String name;
	private String projectPath;
	private Game game;
	private LocalDateTime lastBuildDateTime;
	private long lastBuildSize;
	private RuleSet bestCompressionRuleSet;
	
	public Project(final String name, final String projectPath, final Game game) {
		this.name = name;
		this.projectPath = projectPath;
		this.game = game;
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
				Objects.equals(projectPath, project.projectPath) && game == project.game &&
				Objects.equals(lastBuildDateTime, project.lastBuildDateTime) &&
				Objects.equals(bestCompressionRuleSet, project.bestCompressionRuleSet);
	}
	
	@Override
	public final int hashCode() {
		return Objects.hash(id, name, projectPath, game, lastBuildDateTime, lastBuildSize, bestCompressionRuleSet);
	}
}
