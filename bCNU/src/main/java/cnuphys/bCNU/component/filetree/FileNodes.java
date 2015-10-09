/**
 * 
 */
package cnuphys.bCNU.component.filetree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class FileNodes implements Transferable, Serializable {

	static DataFlavor flavors[] = { DataFlavor.javaFileListFlavor };

	private List<File> files;

	public FileNodes(List<File> files) {
		this.files = files;
	}

	@Override
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (flavor.equals(DataFlavor.javaFileListFlavor)) {
			return files;
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(DataFlavor.javaFileListFlavor);
	}

}
