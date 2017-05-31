/**
 * 
 */
package cnuphys.bCNU.file;

import java.io.File;

public interface IFileHandler {

	public enum HandleAction {
		DRAGANDDROP, OPEN, DELETE, DOUBLECLICK
	};

	/**
	 * Handle a file, e.g., because of a drag and drop.
	 * 
	 * @param parent
	 *            the object (often a view) being affected (might be null).
	 * @param file
	 *            the file to handle
	 * @param action
	 *            the action that initiated the need to handle this file
	 */
	public void handleFile(Object parent, File file, HandleAction action);

	/**
	 * Handle multiple files, e.g., because of a drag and drop.
	 * 
	 * @param parent
	 *            the object (often a view) being affected (might be null).
	 * @param files
	 *            the files to handle
	 * @param action
	 *            the action that initiated the need to handle this file
	 */
	public void handleFiles(Object parent, File files[], HandleAction action);

}
