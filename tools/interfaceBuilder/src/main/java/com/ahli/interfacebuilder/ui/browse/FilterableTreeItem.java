// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.ui.browse;

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
import java.util.concurrent.Callable;
import java.util.function.Predicate;

public class FilterableTreeItem<T> extends TreeItem<T> {
	private final ObjectProperty<TreeItemPredicate<T>> predicate;
	private final ObservableList<TreeItem<T>> sourceList;
	
	public FilterableTreeItem(final T value) {
		super(value);
		predicate = new SimpleObjectProperty<>();
		sourceList = FXCollections.observableArrayList();
		final FilteredList<TreeItem<T>> filteredList = new FilteredList<>(sourceList);
		
		filteredList.predicateProperty().bind(Bindings.createObjectBinding(new BindingUpdateHandler(), predicate));
		
		setHiddenFieldChildren(filteredList);
	}
	
	/**
	 * Set the hidden private field {@link TreeItem} children through reflection and hook the hidden
	 * {@link ListChangeListener} in {@link TreeItem} childrenListener to the list
	 */
	@SuppressWarnings("unchecked")
	private void setHiddenFieldChildren(final ObservableList<TreeItem<T>> list) {
		final Field children = ReflectionUtils.findField(getClass(), "children");
		if (children != null) {
			/* TODO requires "--add-opens=javafx.controls/javafx.scene.control=interfacex.builder" in VM options in
			    IntelliJ to start/debug in IntelliJ. The JAR created by Maven does not need this. */
			children.setAccessible(true);
			ReflectionUtils.setField(children, this, list);
		}
		
		final Field childrenListener1 = ReflectionUtils.findField(getClass(), "childrenListener");
		if (childrenListener1 != null) {
			childrenListener1.setAccessible(true);
			final Object childrenListener = ReflectionUtils.getField(childrenListener1, this);
			list.addListener((ListChangeListener<? super TreeItem<T>>) childrenListener);
		}
	}
	
	public void setPredicate(final TreeItemPredicate<T> predicate) {
		this.predicate.set(predicate);
	}
	
	/**
	 * Returns the list of children that is backing the filtered list.
	 *
	 * @return underlying list of children
	 */
	public ObservableList<TreeItem<T>> getInternalChildren() {
		return sourceList;
	}
	
	public ObjectProperty<TreeItemPredicate<T>> predicateProperty() {
		return predicate;
	}
	
	private class BindingUpdateHandler implements Callable<Predicate<? super TreeItem<T>>> {
		@Override
		public Predicate<? super TreeItem<T>> call() {
			return this::test;
		}
		
		private boolean test(final TreeItem<T> child) {
			// Set the predicate of child items to force filtering
			if (child instanceof final FilterableTreeItem<T> filterableChild) {
				filterableChild.setPredicate(predicate.get());
			}
			// If there is no predicate or if there are children, keep this tree item
			if (predicate.get() == null || !child.getChildren().isEmpty()) {
				return true;
			}
			// Otherwise ask the TreeItemPredicate
			return predicate.get().test(child.getValue());
		}
	}
}
