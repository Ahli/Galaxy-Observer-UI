// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder;

import com.ahli.interfacebuilder.integration.CommandLineParams;
import javafx.stage.Stage;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.lang.Nullable;

import java.io.Serial;

@Getter
public class PrimaryStageReadyEvent extends ApplicationEvent {
	
	@Serial
	private static final long serialVersionUID = -4177428667031979303L;
	private final CommandLineParams startingParams;
	
	public PrimaryStageReadyEvent(final Object source, final CommandLineParams startingParams) {
		super(source);
		this.startingParams = startingParams;
	}
	
	@Nullable
	public Stage getStage() {
		return getSource() instanceof final Stage stage ? stage : null;
	}
	
}