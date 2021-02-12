package interfacebuilder;

import org.springframework.context.ApplicationEvent;

public class AppClosingEvent extends ApplicationEvent {
	public AppClosingEvent(final Object source) {
		super(source);
	}
}
