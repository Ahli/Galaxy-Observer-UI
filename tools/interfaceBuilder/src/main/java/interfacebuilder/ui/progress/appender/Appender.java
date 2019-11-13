// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui.progress.appender;

public interface Appender {
	/**
	 * Appends the specified line to configured thing.
	 *
	 * @param line
	 * 		line that is added
	 */
	void append(String line);
}
