/**
 * 
 */
package cnuphys.bCNU.attributes;

import java.util.Collection;

public interface IAttributeDisplayable {

    /**
     * Get an displayable array of attributes. These will get changed if the
     * user makes any modifications--even if cancel is selected, so it is the
     * calling object's responsibility to send a clone if necessary. The
     * attribute editor will only call "setEditableAttributes" if the user
     * selects "OK" or "Apply".
     * 
     * @return a set of Attributes that will be placed in an Attribute Editor.
     */
    public Attributes getDisplayedAttributes();

    /**
     * Notifies an object that a set of attributes has been edited, and the user
     * has selected OK or Apply. However, even if Cancel was selected, the
     * attributes may have changed, so it is the object's responsibility to, if
     * necessary, clone the attributes before sending them in a
     * GetEditableAttributes call.
     * 
     * @param attributes
     *            the modified attributes.
     */
    public void setEditableAttributes(Attributes attributes);

    /**
     * This optional help is needed if non standard attributes are being edited.
     * It will be asked to prove a Component for rendering the attribute in the
     * table and for editing the attribute.
     * 
     * @return the appropriate AttributeTableHelper.
     */
    public IAttributeTableHelper getAttributeTableHelper();

    /**
     * This is used to get uneditable keys (names) so that they can just be
     * displayed in the table.
     * 
     * @return a collection of names (keys) that cannot be edited.
     */
    public Collection<String> getUneditableKeys();
}
