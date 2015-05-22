/**
 * 
 */
package cnuphys.bCNU.attributes;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * A panel that holds an attribute editor.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class AttributePanelEditor extends JPanel {

    /**
     * The underlying editor
     */
    private AttributeEditor editor;

    /**
     * Constructor for a panel that holds an attribute editor.
     * 
     * @param title
     *            if <code>true</code>, add a title.
     */
    public AttributePanelEditor(boolean title) {
	setLayout(new BorderLayout(2, 2));

	if (title) {
	    setBorder(BorderFactory.createTitledBorder(null, "Attributes",
		    TitledBorder.LEADING, TitledBorder.TOP, null, Color.blue));
	}

	AttributeTableScrollPane asp = new AttributeTableScrollPane();
	editor = new AttributeEditor(asp, true);
	add(asp);
    }

    /**
     * Set the object whose attributes are being edited.
     * 
     * @param displayObject
     */
    public void setDisplayObject(IAttributeDisplayable displayObject) {
	if (editor != null) {
	    editor.setDisplayObject(displayObject);
	}
    }

    /**
     * Clear the table.
     */
    public void clear() {
	if (editor != null) {
	    editor.clear();
	}
    }

    public AttributeEditor getEditor() {
	return editor;
    }

}
