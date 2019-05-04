// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui.browse;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TreeItem;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public class FilterableTreeItem <T> extends TreeItem<T> {
	private final ObservableList<TreeItem<T>> sourceList;
	
	private final ObjectProperty<TreeItemPredicate<T>> predicate = new SimpleObjectProperty<>();
	
	public FilterableTreeItem(final T value) {
		super(value);
		sourceList = FXCollections.observableArrayList();
		final FilteredList<TreeItem<T>> filteredList = new FilteredList<>(sourceList);
		
		filteredList.predicateProperty().bind(Bindings.createObjectBinding(() -> child -> {
			// Set the predicate of child items to force filtering
			if (child instanceof FilterableTreeItem) {
				final FilterableTreeItem<T> filterableChild = (FilterableTreeItem<T>) child;
				filterableChild.setPredicate(predicate.get());
			}
			// If there is no predicate, keep this tree item
			if (predicate.get() == null) {
				return true;
			}
			// If there are children, keep this tree item
			if (!child.getChildren().isEmpty()) {
				return true;
			}
			// Otherwise ask the TreeItemPredicate
			return predicate.get().test(this, child.getValue());
		}, predicate));
		
		setHiddenFieldChildren(filteredList);
	}
	
	/**
	 * Set the hidden private field {@link TreeItem} children through reflection and hook the hidden {@link
	 * ListChangeListener} in {@link TreeItem} childrenListener to the list
	 */
	protected void setHiddenFieldChildren(final ObservableList<TreeItem<T>> list) {
		final Field children = ReflectionUtils.findField(getClass(), "children");
		children.setAccessible(true);
		ReflectionUtils.setField(children, this, list);
		
		final Field childrenListener1 = ReflectionUtils.findField(getClass(), "childrenListener");
		childrenListener1.setAccessible(true);
		final Object childrenListener = ReflectionUtils.getField(childrenListener1, this);
		
		list.addListener((ListChangeListener<? super TreeItem<T>>) childrenListener);
	}
	
	/**
	 * Returns the list of children that is backing the filtered list.
	 *
	 * @return underlying list of children
	 */
	public ObservableList<TreeItem<T>> getInternalChildren() {
		return sourceList;
	}
	
	public final ObjectProperty<TreeItemPredicate<T>> predicateProperty() {
		return predicate;
	}
	
	public final TreeItemPredicate<T> getPredicate() {
		return predicate.get();
	}
	
	public final void setPredicate(final TreeItemPredicate<T> predicate) {
		this.predicate.set(predicate);
	}
}