// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui.browse;

import javafx.scene.control.TreeItem;

import java.util.function.Predicate;

@FunctionalInterface
public interface TreeItemPredicate <T> {
	/**
	 * Utility method to create a TreeItemPredicate from a given {@link Predicate}
	 */
	static <T> TreeItemPredicate<T> create(final Predicate<T> predicate) {
		return (parent, value) -> predicate.test(value);
	}
	
	/**
	 * Evaluates this predicate on the given argument.
	 *
	 * @param parent
	 * 		the parent tree item of the element or null if there is no parent
	 * @param value
	 * 		the value to be tested
	 * @return {@code true} if the input argument matches the predicate,otherwise {@code false}
	 */
	boolean test(TreeItem<T> parent, T value);
}
