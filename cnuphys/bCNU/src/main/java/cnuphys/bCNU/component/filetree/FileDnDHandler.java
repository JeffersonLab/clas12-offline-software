/**
 * 
 */
package cnuphys.bCNU.component.filetree;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.util.List;

import cnuphys.bCNU.file.IFileHandler;

public class FileDnDHandler {

	// owner, often but not always a view (might be null)
	private Object _parent;

	// Handler called when file(s) is (are) dropped
	private IFileHandler _handler;

	/**
	 * The constructor for a DnD handler.
	 * 
	 * @param parent
	 *            the view parent
	 * @param target
	 *            the map container
	 * @param handler
	 *            the handler
	 */
	public FileDnDHandler(Object parent, Component target, IFileHandler handler) {
		_parent = parent;
		_handler = handler;
		new DropTarget(target, DnDConstants.ACTION_COPY, new DTListener(), true);
	}

	/**
	 * Handles a list of files that have been dropped.
	 * 
	 * @param fileList
	 *            the list of files.
	 */
	protected void handleFileList(List<?> fileList) {

		if ((fileList == null) || (_handler == null) || (fileList.size() < 1)) {
			return;
		}

		int size = fileList.size();

		if (size == 1) {
			File file = (File) (fileList.get(0));
			if (file != null) {
				_handler.handleFile(_parent, file,
						IFileHandler.HandleAction.DRAGANDDROP);
			}
		} else { // multiple files
			File files[] = new File[size];
			for (int i = 0; i < size; i++) {
				files[i] = (File) (fileList.get(i));
			}
			_handler.handleFiles(_parent, files,
					IFileHandler.HandleAction.DRAGANDDROP);
		}
	}

	class DTListener implements DropTargetListener {

		@Override
		public void dragEnter(DropTargetDragEvent dtde) {
		}

		@Override
		public void dragExit(DropTargetEvent dte) {
		}

		@Override
		public void dragOver(DropTargetDragEvent dtde) {
		}

		@Override
		public void drop(DropTargetDropEvent dtde) {
			Transferable transferable = dtde.getTransferable();

			if (transferable == null) {
				return;
			}
			DataFlavor dataFlavor[] = transferable.getTransferDataFlavors();

			if (dataFlavor == null) {
				return;
			}

			for (DataFlavor df : dataFlavor) {

				if (df != null) {
					if (df.match(DataFlavor.javaFileListFlavor)) {
						try {
							List<?> list;
							list = (List<?>) (transferable.getTransferData(df));
							handleFileList(list);
						} catch (Exception e) {
							// e.printStackTrace();
						}
					}
				}
			}
		}

		@Override
		public void dropActionChanged(DropTargetDragEvent dtde) {
		}

	}

}
