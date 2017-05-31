package cnuphys.bCNU.drawable;

import java.awt.Graphics;
import java.util.Vector;

import javax.swing.event.EventListenerList;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.item.AItem;
import cnuphys.bCNU.layer.LogicalLayer;

/**
 * A list of <code>IDrawable</code> objects. This is used in several places,
 * including Layers.
 * 
 * @see IDrawable
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class DrawableList extends Vector<IDrawable> implements IDrawable {

	/**
	 * Listener for drawable events
	 */
	private EventListenerList _listenerList = null;

	/**
	 * Visibility flag for this list.
	 */
	protected boolean _visible = true;

	/**
	 * Enabled flag for this list
	 */
	protected boolean _enabled = true;

	/**
	 * Global drawing flag for whether this drawable list is locked.
	 */
	protected boolean _locked = false;

	/**
	 * The name for this drawable list.
	 */
	protected String _name;

	/**
	 * Global "show names" flag-- if <code>false</code>, overrides any drawables
	 * "show name". If <code>true</code>, then each drawable is in control.
	 */
	protected boolean _showNames = true;

	/**
	 * Constructs a <code>DrawableList</code> with the specified name.
	 * 
	 * @param name
	 *            the <code>DrawableList</code>'s name.
	 */
	public DrawableList(String name) {
		super(100, 50);
		this._name = name;
	}

	/**
	 * Add an <code>IDrawableListener</code>.
	 * 
	 * @see IDrawableListener
	 * @param drawableListener
	 *            the <code>IDrawableListener</code> to add.
	 */
	public void addDrawableListener(IDrawableListener drawableListener) {

		if (drawableListener == null) {
			return;
		}

		if (_listenerList == null) {
			_listenerList = new EventListenerList();
		}

		_listenerList.add(IDrawableListener.class, drawableListener);
	}

	/**
	 * Remove an <code>IDrawableListener</code>.
	 * 
	 * @see IDrawableListener
	 * @param drawableListener
	 *            the <code>IDrawableListener</code> to remove.
	 */
	public void removeDrawableListener(IDrawableListener drawableListener) {

		if ((drawableListener == null) || (_listenerList == null)) {
			return;
		}

		_listenerList.remove(IDrawableListener.class, drawableListener);
	}

	/**
	 * Notify interested parties that an <code>IDrawable</code> has changed
	 * 
	 * @param drawable
	 *            the <code>IDrawable</code> in question.
	 * @param type
	 *            the type of change, e.g. one of the enum constants in the
	 *            <code>DrawableChangeType</code> class.
	 */
	public void notifyDrawableChangeListeners(IDrawable drawable,
			DrawableChangeType type) {

		if (_listenerList == null) {
			return;
		}

		// Guaranteed to return a non-null array
		Object[] listeners = _listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IDrawableListener.class) {
				((IDrawableListener) listeners[i + 1]).drawableChanged(this,
						drawable, type);
			}
		}
	}

	/**
	 * Adds an <code>IDrawable</code> to this list.
	 * 
	 * @param drawable
	 *            the <code>IDrawable</code> to add.
	 * @return <code>true</code> (as specified by Collection.add(E))
	 */
	@Override
	public boolean add(IDrawable drawable) {
		super.add(drawable);
		notifyDrawableChangeListeners(drawable, DrawableChangeType.ADDED);
		return true;
	}

	/**
	 * Remove an <code>IDrawable</code> object from this list.
	 * 
	 * @param drawable
	 *            the <code>IDrawable</code> to remove.
	 * @return <code>true</code> if the list contained the specified IDrawable.
	 */
	public boolean remove(IDrawable drawable) {

		drawable.prepareForRemoval();
		boolean result = super.remove(drawable);
		if (result) {
			notifyDrawableChangeListeners(drawable, DrawableChangeType.REMOVED);
		}
		return result;
	}

	/**
	 * Removes all drawables from this layer.
	 */
	@Override
	public void clear() {
		super.clear();
		notifyDrawableChangeListeners(null, DrawableChangeType.LISTCLEARED);
	}

	/**
	 * Draw all the <code>IDrawable</code> objects in this list.
	 * 
	 * @param g
	 *            the graphics context.
	 * @param container
	 *            the graphic container being rendered.
	 */
	@Override
	public void draw(Graphics g, IContainer container) {

		// draw nothing if we are not visible.
		if (!_visible) {
			return;
		}

		synchronized (this) {
			for (IDrawable drawable : this) {
				drawable.draw(g, container);
			}
		}
	}

	/**
	 * Returns the list name.
	 * 
	 * @return the list name.
	 */
	@Override
	public String getName() {
		return _name;
	}

	/**
	 * Move an IDrawable upward in the list which has the effect of sending it
	 * backward when drawn.
	 * 
	 * @param drawable
	 *            the IDrawable to move backward.
	 */
	public void sendBackward(IDrawable drawable) {

		synchronized (this) {
			int src = indexOf(drawable);
			if (src <= 0) {
				return;
			}
			// use super methods so that notifiers are not called
			super.remove(drawable);
			super.add(src - 1, drawable);
		}
	}

	/**
	 * Move an IDrawable downward in the list which has the effect of sending it
	 * forward when drawn.
	 * 
	 * @param drawable
	 *            the IDrawable to move backward.
	 */
	public void sendForward(IDrawable drawable) {

		synchronized (this) {
			int src = indexOf(drawable);
			if (src == -1 || src == (size() - 1)) {
				return;
			}
			// use super methods so that notifiers are not called
			super.remove(drawable);
			super.add(src + 1, drawable);
		}
	}

	/**
	 * Put an <code>IDrawable</code> at the top of the list, which has the
	 * effect of sending to the back when drawn.
	 * 
	 * @param drawable
	 *            the <code>IDrawable</code> to put at the beginning of the
	 *            list, which will result in being drawn on the bottom.
	 */
	public void sendToBack(IDrawable drawable) {

		synchronized (this) {
			// use super methods so that notifiers are not called
			super.remove(drawable);
			super.add(0, drawable);
		}
	}

	/**
	 * Put an <code>IDrawable</code> at the bottom of the list, which has the
	 * effect of sending to the front when drawn.
	 * 
	 * @param drawable
	 *            the <code>IDrawable</code> to put at the end of the list,
	 *            which will result in being drawn on top.
	 */
	public void sendToFront(IDrawable drawable) {

		synchronized (this) {
			// use super methods so that notifiers are not called
			super.remove(drawable);
			super.add(drawable);
		}
	}

	/**
	 * Check whether this list is marked as visible.
	 * 
	 * @return <code>true</code> is this list is marked as visible.
	 */
	@Override
	public boolean isVisible() {
		return _visible;
	}

	/**
	 * Sets the visibility flag.
	 * 
	 * @param visible
	 *            the new value of the flag.
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible == this._visible) {
			return;
		}
		this._visible = visible;

		if (visible) {
			notifyDrawableChangeListeners(null, DrawableChangeType.LISTSHOWN);
		} else {
			notifyDrawableChangeListeners(null, DrawableChangeType.LISTHIDDEN);

		}
	}

	/**
	 * Check whether this list is marked as locked.
	 * 
	 * @return <code>true</code> is this list is marked as locked.
	 */
	public boolean isLocked() {
		return _locked;
	}

	/**
	 * Sets the lock flag.
	 * 
	 * @param locked
	 *            the new value of the flag
	 */
	public void setLocked(boolean locked) {
		this._locked = locked;
	}

	/**
	 * Set the name for this list.
	 * 
	 * @param name
	 *            the name to set.
	 */
	public void setName(String name) {
		this._name = name;
	}

	/**
	 * Get the overall "show names" flag
	 * 
	 * @return the value of the "show names " flag
	 */
	public boolean showNames() {
		return _showNames;
	}

	/**
	 * Set the overall "show names" flag
	 * 
	 * @param showNames
	 *            the value of the "show names" flag
	 */
	public void setShowNames(boolean showNames) {
		this._showNames = showNames;
	}

	/**
	 * Return a descriptive string.
	 * 
	 * @return a descriptive string.
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(5060);
		sb.append("---------------\n");
		sb.append("List: " + getName() + " size: " + size() + "\n");

		for (IDrawable drawable : this) {
			String name = null;
			if (drawable instanceof LogicalLayer) {
				name = "  Layer: " + ((LogicalLayer) drawable).getName() + "["
						+ indexOf(drawable) + "]";
			} else if (drawable instanceof AItem) {
				name = "  Item: " + ((AItem) drawable).getName();
			} else {
				name = "  " + drawable.getClass().getName();
			}
			sb.append(name + "\n");

		}
		return sb.toString();
	}

	/**
	 * Convenience routine to set the dirty property for all items on this
	 * layer. A dirty state is a signal that some cached calculations relevant
	 * for display need to be redone. By careful use of the dirty states,
	 * expensive calculations can be performed only when needed. The danger is
	 * that something that makes the items "dirty" gets missed.
	 * 
	 * @param dirty
	 *            the value to set for the <code>dirty</code> flag.
	 */
	@Override
	public void setDirty(boolean dirty) {

		synchronized (this) {
			for (IDrawable drawable : this) {
				drawable.setDirty(dirty);
			}
		}
	}

	/**
	 * Equality check.
	 * 
	 * @return <code>true</code> if objects are equal.
	 */
	@Override
	public boolean equals(Object o) {

		if ((o != null) && (o instanceof DrawableList)) {
			return (this == o);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cnuphys.bCNU.drawable.IDrawable#prepareForRemoval()
	 */
	@Override
	public void prepareForRemoval() {
	}

	/**
	 * Set whether this logical layer is enabled. If a layer is not enabled,
	 * items on the layer cannot be selected.
	 * 
	 * @param enabled
	 *            the new value of the enabled flag.
	 */
	@Override
	public void setEnabled(boolean enabled) {
		_enabled = enabled;
	}

	/**
	 * Checks whether this logical layer is enabled. If a layer is not enabled,
	 * items on the layer cannot be selected.
	 * 
	 * @return <code>true</code> if the logical layer is enabled.
	 */
	@Override
	public boolean isEnabled() {
		return _enabled;
	}

}
