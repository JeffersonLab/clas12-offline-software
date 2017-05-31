package cnuphys.bCNU.layer;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Vector;

import cnuphys.bCNU.drawable.DrawableChangeType;
import cnuphys.bCNU.drawable.DrawableList;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.item.AItem;

@SuppressWarnings("serial")
public class LogicalLayer extends DrawableList {

	// the owner container
	protected IContainer _container;

	/**
	 * Create a layer for holding items.
	 * 
	 * @param name
	 *            the name of the layer.
	 */
	public LogicalLayer(IContainer container, String name) {
		super(name);
		_container = container;
	}

	/**
	 * Add all the items on this layer that enclose a given point to a vector of
	 * items. This will be used to collect all such items across all layers.
	 * 
	 * @param items
	 *            the collection we are adding to.
	 * @param container
	 *            the graphical container rendering the item.
	 * @param screenPoint
	 *            the point in question.
	 */
	public void addItemsAtPoint(Vector<AItem> items, IContainer container,
			Point screenPoint) {
		Vector<AItem> itemsAtPoint = getItemsAtPoint(container, screenPoint);
		if (itemsAtPoint != null) {
			items.addAll(itemsAtPoint);
		}
	}

	/**
	 * Add all the selected items on this layer to an Items collection. This is
	 * used to find all selected items across all layers.
	 * 
	 * @param items
	 *            the collection to which we will add all selected items on this
	 *            layer.
	 */
	public void addSelectedItems(Vector<AItem> items) {

		Vector<AItem> selectedItems = getSelectedItems();
		if (selectedItems != null) {
			addAll(selectedItems);
		}
	}

	/**
	 * Clears all the items. Not as simple as it appears. The main gotcha is
	 * that items were probably added to the container's feedback control as
	 * feedback providers. If they are not removed they will continue to produce
	 * feedback (and will not be garbage collected) even if they are removed
	 * from the layer--which itself only ensures that they will not be drawn.
	 * 
	 * @param container
	 */
	public void clearAllItems(IContainer container) {

		synchronized (this) {

			Vector<AItem> allItems = getAllItems();
			if (allItems == null) {
				return;
			}

			for (AItem item : allItems) {
				if (item.isDeletable() && (container != null)) {

					container.getFeedbackControl().removeFeedbackProvider(item);
					remove(item);

					// did I delete the reference item?
					if (item == container.getYouAreHereItem()) {
						container.setYouAreHereItem(null);
					}
				}
			}
		}

		clear();
	}

	/**
	 * Deletes all selected (visible) items on this layer. Deleting simply means
	 * removing them from the list. They will no longer be drawn. Items that are
	 * not deletable are not removed.
	 * 
	 * @param container
	 *            the container they lived on.
	 */
	public void deleteSelectedItems(IContainer container) {

		// if the layer is not visible, do nothing.
		if (!isVisible()) {
			return;
		}

		synchronized (this) {
			Vector<AItem> selitems = getSelectedItems();
			if (selitems != null) {
				for (AItem item : selitems) {
					if (item.isDeletable()) {

						if (container != null) {
							container.getFeedbackControl()
									.removeFeedbackProvider(item);
						}
						remove(item);

						// did I delete the reference item?
						if ((container != null)
								&& (item == container.getYouAreHereItem())) {
							container.setYouAreHereItem(null);
						}
					}
				}
			}
		}
	}

	/**
	 * Delete a single item, making sure to remove it from the feedback listener
	 * list.
	 * 
	 * @param item
	 *            the item to remove
	 */
	public void deleteItem(AItem item) {
		item.getContainer().getFeedbackControl().removeFeedbackProvider(item);
		remove(item);
	}

	/**
	 * Find the topmost item, if any, at the point, probably a mouse location.
	 * 
	 * @param container
	 *            the graphical container rendering the item.
	 * @param screenPoint
	 *            the point in question.
	 * @return the topmost item at that location, or <code>null</code>.
	 */
	public AItem getItemAtPoint(IContainer container, Point screenPoint) {

		synchronized (this) {

			for (int i = size() - 1; i >= 0; i--) {
				AItem item = (AItem) get(i);
				if (item.isVisible() && item.contains(container, screenPoint)) {
					return item;
				}
			}

		}
		return null;
	}

	/**
	 * Returns all items that contain the given point. The items are returned in
	 * reverse order, from top to bottom.
	 * 
	 * @param container
	 *            the graphical container rendering the item.
	 * @param lp
	 *            the point in question.
	 * @return all items that contain the given point. If any, the topmost will
	 *         be the first entry.
	 */
	public Vector<AItem> getItemsAtPoint(IContainer container, Point lp) {

		Vector<AItem> locitems = null;

		synchronized (this) {

			for (int i = size() - 1; i >= 0; i--) {
				AItem item = (AItem) get(i);
				if (item.isVisible() && item.contains(container, lp)) {
					if (locitems == null) {
						locitems = new Vector<AItem>(25, 10);
					}
					locitems.add(item);
				}
			}

		}
		return locitems;
	}

	/**
	 * Count how many items are selected on this layer.
	 * 
	 * @return the number of selected items on this layer..
	 */
	public int getSelectedCount() {

		int count = 0;
		synchronized (this) {
			for (IDrawable drawable : this) {
				if (((AItem) drawable).isSelected()) {
					++count;
				}
			}
		}
		return count;
	}

	/**
	 * Check whether at least one item is selected.
	 * 
	 * @return <code>true</code> if at least one item is selected.
	 */
	public boolean anySelected() {
		synchronized (this) {
			for (IDrawable drawable : this) {
				if (((AItem) drawable).isSelected()) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Obtain a collection of selected items on this layer.
	 * 
	 * @return all selected items on this layer.
	 */
	public Vector<AItem> getSelectedItems() {

		Vector<AItem> selitems = null;

		synchronized (this) {
			for (IDrawable drawable : this) {
				if (((AItem) drawable).isSelected()
						&& ((AItem) drawable).isVisible()) {
					if (selitems == null) {
						selitems = new Vector<AItem>(25, 10);
					}
					selitems.add((AItem) drawable);
				}
			}
		}
		return selitems;
	}

	/**
	 * Get all the items into a vector All the items on this layer
	 */
	public Vector<AItem> getAllItems() {

		Vector<AItem> allitems = null;

		synchronized (this) {
			for (IDrawable drawable : this) {
				if (allitems == null) {
					allitems = new Vector<AItem>(25, 10);
				}
				allitems.add((AItem) drawable);

			}
		}
		return allitems;
	}

	/**
	 * Select (or deselect) all items.
	 * 
	 * @param select
	 *            if <code>true</code> select, otherwise deselect.
	 */
	public void selectAllItems(boolean select) {
		selectAllItems(select, null);
	}

	/**
	 * Select (or deselect) all item excepting a single specified item.
	 * 
	 * @param select
	 *            if <code>true</code> select, otherwise deselect.
	 * @param excludedItem
	 *            optional Item to be excluded from the operation, may be null.
	 */
	public void selectAllItems(boolean select, AItem excludedItem) {
		for (IDrawable drawable : this) {
			if (drawable instanceof AItem) {
				AItem item = (AItem) drawable;
				if ((!item.isLocked()) && (item != excludedItem)) {

					if (select && !item.isSelected()) {
						item.setSelected(true);
						notifyDrawableChangeListeners(drawable,
								DrawableChangeType.SELECTED);
					} else if (!select && item.isSelected()) {
						item.setSelected(false);
						notifyDrawableChangeListeners(drawable,
								DrawableChangeType.DESELECTED);
					}
				}
			}
		}
	}

	/**
	 * Select or deselect a single item and send the notification.
	 * 
	 * @param item
	 *            the item in question.
	 * @param select
	 *            the new select state.
	 */
	public void selectItem(AItem item, boolean select) {
		if ((item != null) && (!item.isLocked())) {
			if (select && !item.isSelected()) {
				item.setSelected(true);
				notifyDrawableChangeListeners(item, DrawableChangeType.SELECTED);
			} else if (!select && item.isSelected()) {
				item.setSelected(false);
				notifyDrawableChangeListeners(item,
						DrawableChangeType.DESELECTED);
			}
		}
	}

	/**
	 * Add all the enclosed items to a collection
	 * 
	 * @param container
	 *            the container being rendered.
	 * @param items
	 *            the vector we are adding to.
	 * @param rect
	 *            the enclosing rectangle.
	 */
	public void addEnclosedItems(IContainer container, Vector<AItem> items,
			Rectangle rect) {

		synchronized (this) {
			if (size() > 0) {
				items.addAll(getEnclosedItems(container, rect));
			}
		}
	}

	/**
	 * Get all the items enclosed by a rectangle.
	 * 
	 * @param container
	 *            the container being rendered.
	 * @param rect
	 *            the rectangle in question.
	 */
	public Vector<AItem> getEnclosedItems(IContainer container, Rectangle rect) {

		synchronized (this) {
			if (size() > 0) {
				Vector<AItem> encitems = new Vector<AItem>(25);
				for (IDrawable drawable : this) {
					if (drawable instanceof AItem) {
						AItem item = (AItem) drawable;
						if (item.isVisible() && item.enclosed(container, rect)) {
							encitems.add(item);
						}
					}
				}
				return encitems;
			}
			return null;
		}
	}

	/**
	 * Equality check.
	 * 
	 * @return <code>true</code> if objects are equal.
	 */
	public boolean equals(LogicalLayer o) {

		if (o != null) {
			return (this == o);
		}
		return false;
	}

	/**
	 * Get the container for this layer.
	 * 
	 * @return the container for this layer.
	 */
	public IContainer getContainer() {
		return _container;
	}
}
