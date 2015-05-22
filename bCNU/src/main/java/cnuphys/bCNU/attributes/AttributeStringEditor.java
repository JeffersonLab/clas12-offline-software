/**
 * 
 */
package cnuphys.bCNU.attributes;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;

@SuppressWarnings("serial")
public class AttributeStringEditor extends JTextField implements FocusListener,
	KeyListener {

    /**
     * The attribute name.
     */
    protected String attributeName = null;

    /**
     * The owner table.
     */
    protected AttributeTable attributeTable = null;

    /**
     * cache to monitor changes
     */

    protected String oldText = null;

    /**
     * Create a String editor.
     * 
     * @param attributeTable
     *            the owner table.
     * @param attributeName
     *            the attribute name.
     * @param startValue
     *            the starting value.
     */
    public AttributeStringEditor(AttributeTable attributeTable,
	    String attributeName, String startValue) {

	this.attributeTable = attributeTable;
	this.attributeName = attributeName;
	oldText = new String(startValue);

	if ((attributeTable == null) || (attributeName == null)) {
	    return;
	}
	setBorder(null);
	setText(oldText);

	addFocusListener(this);
	addKeyListener(this);

    }

    /**
     * See if a string has changed. If so, fire a notice.
     * 
     * @param eventComponent
     */

    protected void checkTextChange() {

	try {
	    String newText = getText();

	    if ((oldText == null) || (oldText.compareTo(newText) != 0)) {
		attributeTable.setAttribute(attributeName, newText);
		oldText = new String(newText);
	    }

	} catch (Exception ex) {
	    ex.printStackTrace();
	}

    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
	checkTextChange();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
	checkTextChange();
    }

}
