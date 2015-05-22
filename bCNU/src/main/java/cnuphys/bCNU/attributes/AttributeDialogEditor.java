/**
 * 
 */
package cnuphys.bCNU.attributes;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Vector;

import javax.swing.JDialog;

import cnuphys.bCNU.dialog.ButtonPanel;
import cnuphys.bCNU.dialog.DialogUtilities;

/**
 * A general purpose Attribute editor. Will get an editable array of attributes.
 * These will get changed if the user makes any modifications--even if cancel is
 * selected, so it is the calling object's responsibility to send a clone if
 * necessary. The attribute editor will only call "setEditableAttributes" if the
 * user selects "OK" or "Apply".
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class AttributeDialogEditor extends JDialog implements ActionListener {

    // The underlying editor
    private AttributeEditor _editor;

    /**
     * Create an Attribute editor. After created, it can be cached and then just
     * reused by calling setEditableObject, setTitle, and setVisible.
     * 
     * @param title
     *            the title of the dialog.
     * @param editableObject
     *            the object whose attributes are being edited.
     */
    public AttributeDialogEditor(String title,
	    IAttributeDisplayable editableObject) {
	super();
	setModal(true);
	setTitle(title);
	setup();
	setDisplayObject(editableObject);
	pack();
	DialogUtilities.centerDialog(this);
    }

    /**
     * Add the content to the dialog.
     */
    private void setup() {
	Container contentPane = getContentPane();

	AttributeTableScrollPane attributeTableScrollPane = new AttributeTableScrollPane();
	_editor = new AttributeEditor(attributeTableScrollPane, false);
	contentPane.add(attributeTableScrollPane, BorderLayout.CENTER);
	contentPane.add(ButtonPanel.closeOutPanel(
		ButtonPanel.USE_OKCANCELAPPLY, this, 10), BorderLayout.SOUTH);
    }

    /**
     * Set the object whose attributes are being edited.
     * 
     * @param displayObject
     */
    public void setDisplayObject(IAttributeDisplayable displayObject) {
	if (_editor != null) {
	    _editor.setDisplayObject(displayObject);
	}
    }

    /**
     * One of the closeout buttons was hit.
     * 
     * @param e
     *            the causal event.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
	String command = e.getActionCommand();

	if (ButtonPanel.OK_LABEL.equals(command)) {
	    if (_editor.getEditableObject() != null) {
		_editor.apply();
	    }
	    setVisible(false);
	}

	else if (ButtonPanel.CANCEL_LABEL.equals(command)) {
	    setVisible(false);
	}

	else if (ButtonPanel.APPLY_LABEL.equals(command)) {
	    if (_editor.getEditableObject() != null) {
		_editor.apply();
	    }
	}
    }

    /**
     * main program for testing.
     * 
     * @param args
     */
    public static void main(String[] args) {

	final IAttributeTableHelper helper = new IAttributeTableHelper() {

	    @Override
	    public void attributeChanged(AttributeTable table,
		    String attributeName, Object value) {
		System.err.println("Attribute [" + attributeName
			+ "] changed to: " + value);
	    }

	    @Override
	    public Component getTableCellRenderer(AttributeTable table,
		    String AttributeName, Object value) {
		System.err.println("Asked for a component table renderer for "
			+ value.getClass().getName());
		return null;
	    }

	    @Override
	    public Component getTableCellEditor(AttributeTable table,
		    String AttributeName, Object value) {
		System.err.println("Asked for a component table editor for "
			+ value.getClass().getName());
		return null;
	    }

	};

	IAttributeDisplayable editable = new IAttributeDisplayable() {

	    @Override
	    public IAttributeTableHelper getAttributeTableHelper() {
		return helper;
	    }

	    @Override
	    public Attributes getDisplayedAttributes() {
		return new Attributes(true);
	    }

	    @Override
	    public void setEditableAttributes(Attributes attributes) {
	    }

	    // demonstrate how some can be made uneditable
	    @Override
	    public Collection<String> getUneditableKeys() {
		Vector<String> uneditable = new Vector<String>();
		uneditable.add(AttributeType.HEIGHT.toString());
		return uneditable;
	    }

	};

	AttributeDialogEditor editor = new AttributeDialogEditor(
		"Attribute Editor", editable);
	editor.setVisible(true);

    }
}
