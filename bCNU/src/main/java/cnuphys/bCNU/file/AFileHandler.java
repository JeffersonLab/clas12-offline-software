/**
 * 
 */
package cnuphys.bCNU.file;

import java.io.File;

import cnuphys.bCNU.log.Log;

/**
 * @author heddle
 * 
 */
public abstract class AFileHandler implements IFileHandler {

	/**
	 * Handle a file, e.g., because of a drag and drop.
	 * 
	 * @param parent
	 *            the view being affected.
	 * @param file
	 *            the file to handle
	 * @param action
	 *            the action that initiated the need to handle this file
	 */
	@Override
	public void handleFile(Object parent, File file, HandleAction action) {
		switch (action) {
		case DRAGANDDROP:
			handleDragAndDrop(parent, file);
			break;

		case OPEN:
			handleOpen(parent, file);
			break;

		case DOUBLECLICK:
			handleDoubleClick(parent, file);
			break;

		case DELETE:
			handleDelete(parent, file);
			break;
		}
	}

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
	@Override
	public void handleFiles(Object parent, File files[], HandleAction action) {
		System.err.println("Got " + files.length + " files.");
		System.err.println("For now just peeling off the top file");

		handleFile(parent, files[0], action);
	}

	/**
	 * Handle an open. This method is abstract and must be supplied by the
	 * superclass.
	 * 
	 * @param parent
	 *            the object (often a view) being affected.
	 * @param file
	 *            the file in question.
	 */
	public abstract void handleOpen(Object parent, File file);

	/**
	 * Handle an "drag and drop". The default handling is to call the open
	 * handler-- i.e., the default behavior is to treat a drag and drop as an
	 * open.
	 * 
	 * @param parent
	 *            the object (often a view) being affected.
	 * @param file
	 *            the file in question.
	 */
	public void handleDragAndDrop(Object parent, File file) {
		if (file == null) {
			return;
		}
		// System.err.println("CALL OPEN FOR DnD");
		handleOpen(parent, file);
	}

	/**
	 * Handle a "double click". The default handling is to call the open
	 * handler-- i.e., the default behavior is to treat a double click as an
	 * open.
	 * 
	 * @param parent
	 *            the object (often a view) being affected.
	 * @param file
	 *            the file in question.
	 */
	public void handleDoubleClick(Object parent, File file) {
		if (file == null) {
			return;
		}
		handleOpen(parent, file);
	}

	/**
	 * The default implementation simply tries to delete the file.
	 * 
	 * @param parent
	 *            the object (often a view) being affected.
	 * @param file
	 *            the file in question.
	 */
	public void handleDelete(Object parent, File file) {

		if (file == null) {
			return;
		}

		try {
			file.delete();
		} catch (Exception e) {
			Log.getInstance().error(
					"File could not be deleted: " + file.getPath());
			e.printStackTrace();
		}
	}

}
