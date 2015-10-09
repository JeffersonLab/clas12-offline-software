/**
 * 
 */
package cnuphys.bCNU.visible;

/**
 * This is used to make tables of things (like logical layers) than can be shown
 * and hidden
 * 
 * @author heddle
 * 
 */
public interface IVisible {

	/**
	 * Get the visibility flag.
	 * 
	 * @return <code>true</code> if the object is visible.
	 */
	public boolean isVisible();

	/**
	 * Set whether the object is visible.
	 * 
	 * @param visible
	 *            the value of the visibility flag.
	 */
	public void setVisible(boolean visible);

	/**
	 * Get the enabled flag.
	 * 
	 * @return <code>true</code> if the object is enabled.
	 */
	public boolean isEnabled();

	/**
	 * Set whether the object is enabled.
	 * 
	 * @param enabled
	 *            the value of the enabled flag.
	 */
	public void setEnabled(boolean enabled);

	/**
	 * Get the name of the object.
	 * 
	 * @return the name of the object, as it will appear in a visibility table.
	 */
	public String getName();
}
