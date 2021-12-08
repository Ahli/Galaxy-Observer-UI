// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.ui.progress.appenders;

import javafx.beans.property.SimpleBooleanProperty;

public interface Appender {
	/**
	 * Appends the specified line to configured thing.
	 *
	 * @param line
	 * 		line that is added
	 */
	void append(String line);
	
	/**
	 * Signals that the appending is now stopping.
	 */
	void end();
	
	/**
	 * @return
	 */
	SimpleBooleanProperty endedProperty();
}
