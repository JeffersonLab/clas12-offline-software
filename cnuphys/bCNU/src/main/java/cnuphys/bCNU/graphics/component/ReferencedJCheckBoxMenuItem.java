package cnuphys.bCNU.graphics.component;

import java.awt.event.ItemListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

/**
 * This class associates an object with a JCheckBox menu item. This is often
 * useful, because the state of the check box is tied to the state of an object,
 * so this gives quick access to that object.
 * 
 * @author heddle
 */
@SuppressWarnings("serial")
public class ReferencedJCheckBoxMenuItem extends JCheckBoxMenuItem {

	// The associated object. E.g., a view if this is a view menu checkbox.
	private Object _object;

	/**
	 * Constructor. Uses the default state (unchecked.)
	 * 
	 * @param label
	 *            the checkbox menu item label.
	 * @param object
	 *            the object that will be associated with this checkbox menu
	 *            item.
	 */
	public ReferencedJCheckBoxMenuItem(String label, Object object) {
		this(label, object, false, null, null);
	}

	/**
	 * Constructor
	 * 
	 * @param label
	 *            the checkbox menu item label.
	 * @param object
	 *            the object that will be associated with this checkbox menu
	 *            item.
	 * @param state
	 *            the default state, checked <code>true</code> or unchecked.
	 */
	public ReferencedJCheckBoxMenuItem(String label, Object object,
			boolean state) {
		this(label, object, state, null, null);
	}

	/**
	 * Constructor
	 * 
	 * @param label
	 *            the checkbox menu item label.
	 * @param object
	 *            the object that will be associated with this checkbox menu
	 *            item.
	 * @param state
	 *            the default state, checked <code>true</code> or unchecked.
	 * @param itemListener
	 *            an optional item listener.
	 */
	public ReferencedJCheckBoxMenuItem(String label, Object object,
			boolean state, ItemListener itemListener) {
		this(label, object, state, itemListener, null);
	}

	/**
	 * Constructor
	 * 
	 * @param label
	 *            the checkbox menu item label.
	 * @param object
	 *            the object that will be associated with this checkbox menu
	 *            item.
	 * @param state
	 *            the default state, checked <code>true</code> or unchecked.
	 * @param itemListener
	 *            an optional item listener.
	 * @param menu
	 *            and optional menu to add this checkbox menu item to.
	 */
	public ReferencedJCheckBoxMenuItem(String label, Object object,
			boolean state, ItemListener itemListener, JMenu menu) {
		super(label, state);
		if (menu != null) {
			menu.add(this);
		}

		if (itemListener != null) {
			addItemListener(itemListener);
		}

		_object = object;
	}

	/**
	 * Get the associated object.
	 * 
	 * @return the associated object.
	 */
	public Object getObject() {
		return _object;
	}

	/**
	 * Set the associated object.
	 * 
	 * @param object
	 *            the associated object.
	 */
	public void setObject(Object object) {
		this._object = object;
	}

}
