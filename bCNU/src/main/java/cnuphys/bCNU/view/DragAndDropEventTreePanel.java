package cnuphys.bCNU.view;

import java.io.File;
import java.util.List;

import cnuphys.bCNU.component.filetree.FileDnDHandler;
import cnuphys.bCNU.component.filetree.IFileTreeListener;
import cnuphys.bCNU.event.graphics.EventTreePanel;
import cnuphys.bCNU.file.AFileHandler;
import cnuphys.bCNU.file.FileHandlerFactory;
import cnuphys.bCNU.file.IFileHandler;

@SuppressWarnings("serial")
public class DragAndDropEventTreePanel extends EventTreePanel implements
		IFileHandler, IFileTreeListener {

	// the parent is often a view
	private Object _parent;

	public DragAndDropEventTreePanel(Object parent) {
		super();
		_parent = parent;
		// this will allow me to be notified of drag and dropped (dnd) files
		// from the file tree,
		new FileDnDHandler(_parent, this, this);

	}

	/**
	 * Handle a file--probably resulting from a drop. This is essentially a call
	 * to open the file.
	 * 
	 * @param parent
	 *            the object being affected, usually but not always a view
	 * @param file
	 *            the file in question.
	 */
	@Override
	public void handleFile(Object parent, File file,
			IFileHandler.HandleAction action) {

		// use the factory to create the correct handler.
		AFileHandler fileHandler = FileHandlerFactory.createFileHandler(parent,
				file);
		if (fileHandler != null) {
			fileHandler.handleFile(parent, file, action);
		}
	}

	@Override
	public void fileDoubleClicked(String fullPath) {
		File file = new File(fullPath);
		if (file.exists()) {
			// use the factory to create the correct handler.
			AFileHandler fileHandler = FileHandlerFactory.createFileHandler(
					_parent, file);
			if (fileHandler != null) {
				fileHandler.handleFile(_parent, file, HandleAction.OPEN);
			}
		}
	}

	@Override
	public void filesDoubleClicked(List<File> files) {
	}

	@Override
	public void handleFiles(Object parent, File[] files, HandleAction action) {
	}

}
