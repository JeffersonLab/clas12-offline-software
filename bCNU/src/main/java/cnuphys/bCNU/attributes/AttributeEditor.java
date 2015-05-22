/**
 * 
 */
package cnuphys.bCNU.attributes;

import java.awt.Component;

public class AttributeEditor implements IAttributeTableHelper {

    // The object that provided the attributes.
    private IAttributeDisplayable _editableObject;

    // The attribute table scroll pane.
    private AttributeTableScrollPane _attributeTableScrollPane;

    // The currently displayed attributes. Starts as a clone of the
    // editableObject's editable attributes.
    private Attributes _currentAttributes;

    public AttributeEditor(AttributeTableScrollPane attributeTableScrollPane,
	    boolean autoApply) {
	_attributeTableScrollPane = attributeTableScrollPane;
	if (autoApply) {
	    attributeTableScrollPane.attributeTable
		    .addAttributeTableHelper(this);
	}
    }

    /**
     * Set the object whose attributes are being displayed.
     * 
     * @param displayObject
     *            the object that will display attributes.
     */
    public void setDisplayObject(IAttributeDisplayable displayObject) {
	// remove old one as a helper
	if (displayObject != null) {
	    IAttributeTableHelper oldHelper = displayObject
		    .getAttributeTableHelper();
	    if (oldHelper != null) {
		_attributeTableScrollPane.getAttributeTable()
			.removeAttributeTableHelper(oldHelper);
	    }
	}

	this._editableObject = displayObject;
	if (displayObject == null) {
	    _attributeTableScrollPane.getAttributeTable().clear();
	    return;
	}

	_currentAttributes = displayObject.getDisplayedAttributes();
	// any uneditables?
	_attributeTableScrollPane.getAttributeTable().setUneditableKeys(
		displayObject.getUneditableKeys());

	_attributeTableScrollPane.getAttributeTable().setAttributes(
		_currentAttributes);

	// see if there is a helper
	IAttributeTableHelper newHelper = displayObject
		.getAttributeTableHelper();
	if (newHelper != null) {
	    _attributeTableScrollPane.getAttributeTable()
		    .addAttributeTableHelper(newHelper);
	}
    }

    /**
     * Applies the current attributes, which reflects edits, to the editable
     * object.
     */
    public void apply() {
	if (_editableObject != null) {
	    _editableObject.setEditableAttributes(_currentAttributes);
	}
    }

    public void clear() {
	_attributeTableScrollPane.getAttributeTable().clear();
    }

    public IAttributeDisplayable getEditableObject() {
	return _editableObject;
    }

    @Override
    public void attributeChanged(AttributeTable attributeTable,
	    String attributeName, Object value) {
	apply();
    }

    @Override
    public Component getTableCellEditor(AttributeTable attributeTable,
	    String attributeName, Object value) {
	return null;
    }

    @Override
    public Component getTableCellRenderer(AttributeTable attributeTable,
	    String attributeName, Object value) {
	return null;
    }

}
