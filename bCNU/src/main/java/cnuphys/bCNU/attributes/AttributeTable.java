/**
 * 
 */
package cnuphys.bCNU.attributes;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Collection;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.EventListenerList;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * A general table for displaying and editing attributes.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class AttributeTable extends JTable {

    /**
     * An Attribute table can have a helpers that can supply him with renderers
     * and editors for objects it doesn't know about.
     */
    private EventListenerList helpers = null;

    private DefaultTableCellRenderer def_renderer = new DefaultTableCellRenderer();

    private AttributeCellRenderer val_renderer = new AttributeCellRenderer();

    private AttributeCellEditor val_editor;

    /**
     * Used by helpers that complete the edit, maybe through a dialog box,so
     * they do not return an editor--but this allows thim to tell thye table the
     * edit is complete.
     * 
     */
    private boolean editComplete;

    /**
     * The attributes being displayed.
     */
    private Attributes hotAttributes; // attributes being edited

    private TableColumn c1, c2;

    public AttributeTable() {

	val_editor = new AttributeCellEditor(this);

	Dimension dim = getIntercellSpacing();
	dim.width += 2;
	setIntercellSpacing(dim);
	setAutoCreateColumnsFromModel(false);
	setModel(new AttributeTableModel());
	def_renderer.setHorizontalAlignment(SwingConstants.LEFT);

	c1 = new TableColumn(0, 65, def_renderer, null);
	c2 = new TableColumn(1, 140, val_renderer, val_editor);

	addColumn(c1);
	addColumn(c2);
	getTableHeader().setReorderingAllowed(false);

	setRowHeight(24);
    }

    /**
     * Set the attribute for the current hot attributes object.
     * 
     * @param attributeName
     *            the name of the attribute.
     * @param value
     *            the new value
     */
    public void setAttribute(String attributeName, Object value) {

	if (hotAttributes != null) {
	    hotAttributes.put(attributeName, value);
	    notifyHelpers(attributeName, value);
	}
    }

    /**
     * Get the attribute name at a given row and column.
     * 
     * @param row
     *            the zero based row.
     * @return the attribute name.
     */

    public String getAttributeNameAt(int row) {
	try {
	    AttributeTableModel attributeTableModel = (AttributeTableModel) getModel();
	    if (attributeTableModel != null) {
		return attributeTableModel.getAttributeNameAt(row);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return null;
    }

    /**
     * Sets the names (keys) of attributes that are display only.
     * 
     * @param uneditableKeys
     *            the uneditableKeys to set
     */
    public void setUneditableKeys(Collection<String> uneditableKeys) {
	try {
	    AttributeTableModel attributeTableModel = (AttributeTableModel) getModel();
	    if (attributeTableModel != null) {
		attributeTableModel.setUneditableKeys(uneditableKeys);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * Clear the table display.
     */
    public void clear() {
	AttributeTableModel attributeTableModel = (AttributeTableModel) getModel();
	if (attributeTableModel != null) {
	    attributeTableModel.clear();
	}
	removeEditor();
	hotAttributes = null;
	resizeAndRepaint();
    }

    /**
     * Set the attributes that will be displayed,
     * 
     * @param attributes
     *            the Attributes object to display and edit.
     */
    public void setAttributes(Attributes attributes) {

	hotAttributes = attributes;
	removeEditor();

	AttributeTableModel attributeTableModel = (AttributeTableModel) getModel();
	if (attributeTableModel != null) {
	    attributeTableModel.setDisplayedAttributes(hotAttributes);
	}

	resizeAndRepaint();
    }

    /**
     * Add a AttributeTableHelper.
     * 
     * @param helper
     *            the AttributeTableHelper to add.
     */
    public void addAttributeTableHelper(IAttributeTableHelper helper) {

	if (helper == null) {
	    return;
	}

	if (helpers == null) {
	    helpers = new EventListenerList();
	}

	helpers.add(IAttributeTableHelper.class, helper);
    }

    /**
     * Remove a AttributeTableHelper.
     * 
     * @param helper
     *            the AttributeTableHelper to remove.
     */

    public void removeAttributeTableHelper(IAttributeTableHelper helper) {

	if ((helper == null) || (helpers == null)) {
	    return;
	}

	helpers.remove(IAttributeTableHelper.class, helper);
    }

    /**
     * Notify interested parties that an attribute was changed.
     * 
     * @param attributeName
     *            the name of the attribute.
     * @param value
     *            the new value
     */
    public void notifyHelpers(String attributeName, Object value) {

	if (helpers == null) {
	    return;
	}

	// Guaranteed to return a non-null array

	Object[] listeners = helpers.getListenerList();

	// Process the listeners last to first, notifying
	// those that are interested in this event

	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == IAttributeTableHelper.class) {
		((IAttributeTableHelper) listeners[i + 1]).attributeChanged(
			this, attributeName, value);
	    }
	}
    }

    /**
     * Ask the helpers for a renderer for a nonstandard Object type.
     * 
     * @param name
     *            the name of the attribute
     * @param value
     *            the object that needs rendering.
     * @return the first non-null response, or null if no help provided.
     */
    public Component askHelpersForRenderer(String name, Object value) {

	if (helpers == null) {
	    return null;
	}

	// Guaranteed to return a non-null array

	Object[] listeners = helpers.getListenerList();

	// Process the listeners last to first, notifying
	// those that are interested in this event

	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == IAttributeTableHelper.class) {
		Component component = ((IAttributeTableHelper) listeners[i + 1])
			.getTableCellRenderer(this, name, value);
		if (component != null) {
		    return component;
		}
	    }
	}
	return null;
    }

    /**
     * Ask the helpers for a editor for a nonstandard Object type.
     * 
     * @param name
     *            the name of the attribute
     * @param value
     *            the object that needs an editor.
     * @return the first non-null response, or null if no help provided.
     */
    public Component askHelpersForEditor(String name, Object value) {

	if (helpers == null) {
	    return null;
	}

	// Guaranteed to return a non-null array

	Object[] listeners = helpers.getListenerList();

	// Process the listeners last to first, notifying
	// those that are interested in this event

	editComplete = false;
	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == IAttributeTableHelper.class) {
		Component component = ((IAttributeTableHelper) listeners[i + 1])
			.getTableCellEditor(this, name, value);
		if (component != null) {
		    return component;
		}
		if (editComplete) { // helper did just return editor, but did
		    // the edit.
		    return null;
		}
	    }
	}
	return null;
    }

    /**
     * A helper that completes the edit (rather than returning an editor) should
     * call this. The table then knows the editing of that object is complete.
     * This is so if the helper knows how to pop a dialog to edit, he can.
     */
    public void setEditComplete() {
	editComplete = true;
    }
}
