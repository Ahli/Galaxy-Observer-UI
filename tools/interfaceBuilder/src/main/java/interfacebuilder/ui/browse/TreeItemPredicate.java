// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui.browse;

import javafx.scene.control.TreeItem;

import java.util.function.Predicate;

@FunctionalInterface
public interface TreeItemPredicate<T> {
	/**
	 * Utility method to create a TreeItemPredicate from a given {@link Predicate}
	 */
	static <T> TreeItemPredicate<T> create(final Predicate<T> predicate) {
		return new TestingTreeItemPredicate<>(predicate);
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
	
	/**
	 * TreeItemPredicate implementation for Utility method
	 *
	 * @param <T>
	 */
	final class TestingTreeItemPredicate<T> implements TreeItemPredicate<T> {
		private final Predicate<T> predicate;
		
		public TestingTreeItemPredicate(final Predicate<T> predicate) {
			this.predicate = predicate;
		}
		
		@Override
		public boolean test(final TreeItem<T> parent, final T value) {
			return predicate.test(value);
		}
	}
}