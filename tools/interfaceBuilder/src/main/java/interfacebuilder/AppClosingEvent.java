// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder;

import org.springframework.context.ApplicationEvent;

public class AppClosingEvent extends ApplicationEvent {
	public AppClosingEvent(final Object source) {
		super(source);
	}
}
