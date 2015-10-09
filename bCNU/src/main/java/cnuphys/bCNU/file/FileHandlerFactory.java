/**
 * 
 */
package cnuphys.bCNU.file;

import java.io.File;

import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.FileUtilities;
import cnuphys.bCNU.view.EventView;

/**
 * @author heddle
 * 
 */
public class FileHandlerFactory {

	public static AFileHandler createFileHandler(Object parent, File file) {
		if (file == null) {
			return null;
		}

		String extension = FileUtilities.getExtension(file);

		// keep adding here when new file handlers are developed
		// note "getExtension" always returns lower case.
		if (extension != null) {
			if (extension.equals("xml")) {
				return new XMLFileHandler();
			} else if (EventView.goodExtension(extension)) {
				// System.err.println("Factory provided event file handler");
				return new EventFileHandler();
			}
		}

		Log.getInstance().warning(
				"Could not create a file handler for file: " + file.getPath()
						+ "\nIt had no recognized extension.");
		return null;
	}

}
