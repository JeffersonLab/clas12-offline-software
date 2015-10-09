package cnuphys.bCNU.file;

import java.io.File;

import cnuphys.bCNU.view.EventPanel;
import cnuphys.bCNU.view.EventView;

public class EventFileHandler extends AFileHandler {

	/**
	 * An event file needs to be opened. This could be the result of a drag and
	 * drop, or a open file dialog.
	 * 
	 * @param parent
	 *            the view being affected.
	 * @param file
	 *            the file to handle.
	 */
	@Override
	public void handleOpen(Object parent, File file) {
		if (parent == null) {
			System.err.println("null handler in handle open");
			return;
		} else if (parent instanceof EventView) {
			((EventView) parent).openEventFile(file);
		} else if (parent instanceof EventPanel) {
			((EventPanel) parent).openEventFile(file);
		}
	}

}
