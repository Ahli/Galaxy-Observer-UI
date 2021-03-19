package interfacebuilder;

import interfacebuilder.integration.CommandLineParams;
import javafx.stage.Stage;
import org.springframework.context.ApplicationEvent;

public class PrimaryStageReadyEvent extends ApplicationEvent {
	
	private final CommandLineParams startingParams;
	
	public PrimaryStageReadyEvent(final Object source, final CommandLineParams startingParams) {
		super(source);
		this.startingParams = startingParams;
	}
	
	public Stage getStage() {
		return getSource() instanceof final Stage stage ? stage : null;
	}
	
	public CommandLineParams getStartingParams() {
		return startingParams;
	}
}