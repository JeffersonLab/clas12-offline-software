/**
 * 
 */
package cnuphys.bCNU.attributes;

import java.awt.Component;
import java.util.EventListener;

/**
 * This interface has a two way purpose. It will be notified that an attribute
 * value changed. Typically the only thing it can do is repaint, because
 * ultimate ownership of the changed attribute is difficult to discern. That is,
 * it might be a LineStyle--but whose line style?
 * 
 * The other thing a help can do is provide a rendere and/or an edior for non
 * standard objects. If an AttributeTable has to deal with an unknown Object
 * type, it will ask all its helpers for a renderer or an editor, and use the
 * first one it gets.
 * 
 * @author heddle
 * 
 */
public interface IAttributeTableHelper extends EventListener {

    /**
     * This notifies that some attribute has changed.
     * 
     * @param attributeTable
     *            the table being displayed.
     * @param attributeName
     *            the name of the attribute.
     * @param value
     *            the new value.
     */
    public void attributeChanged(AttributeTable attributeTable,
	    String attributeName, Object value);

    /**
     * Asks the helper for a table cell renderer for the given Object type,
     * which the table doesn't recognize.
     * 
     * @param attributeTable
     *            the table being displayed.
     * @param value
     *            the value being rendered.
     * @return a renderer, or <code>null</code> if the helper doesn't know.
     */
    public Component getTableCellRenderer(AttributeTable attributeTable,
	    String attributeName, Object value);

    /**
     * Asks the helper for a table cell editor for the given Object type, which
     * the table doesn't recognize.
     * 
     * @param attributeTable
     *            the table being displayed.
     * @param value
     *            the value being edited.
     * @return an editor, or <code>null</code> if the helper doesn't know.
     */
    public Component getTableCellEditor(AttributeTable attributeTable,
	    String attributeName, Object value);

}
