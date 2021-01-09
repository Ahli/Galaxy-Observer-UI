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

import java.time.LocalDateTime;

@Data
@ToString
@EqualsAndHashCode
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
		this.id = 0;
	}
	
}
