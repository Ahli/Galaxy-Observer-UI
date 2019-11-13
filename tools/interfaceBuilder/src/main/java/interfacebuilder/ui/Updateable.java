// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui;

/**
 * Interface for Updateable classes providing a method to update.
 */
public interface Updateable {
	
	/**
	 * Notifies this class that it should update.
	 */
	void update();
}
