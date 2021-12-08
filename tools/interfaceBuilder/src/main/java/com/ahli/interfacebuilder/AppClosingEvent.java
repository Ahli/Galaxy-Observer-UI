// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder;

import org.springframework.context.ApplicationEvent;

import java.io.Serial;

public class AppClosingEvent extends ApplicationEvent {
	
	@Serial
	private static final long serialVersionUID = -7023381733913412190L;
	
	public AppClosingEvent(final Object source) {
		super(source);
	}
}
