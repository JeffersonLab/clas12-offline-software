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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;

import cnuphys.bCNU.graphics.ImageManager;

@SuppressWarnings("serial")
public class FileNode implements Transferable, Serializable {

	private static Comparator<FileNode> comparator = new Comparator<FileNode>() {
		@Override
		public int compare(FileNode o1, FileNode o2) {
			return o1.compare(o1, o2);
		}
	};

	static DataFlavor flavors[] = { DataFlavor.javaFileListFlavor };

	/**
	 * The actual file for this node
	 */
	private File theFile;

	/**
	 * An optional filter
	 */
	protected FileFilter fileFilter;

	/**
	 * Constructor.
	 * 
	 * @param file
	 */
	public FileNode(File file, FileFilter ff) {
		theFile = file;
		fileFilter = ff;
	}

	public FileNode(File file) {
		this(file, null);
	}

	/**
	 * Accessor for underlying file.
	 * 
	 * @return The underlying File object.
	 */

	public File getFile() {
		return theFile;
	}

	/**
	 * Return a useful string representation.
	 * 
	 * @return A string representation.
	 */
	@Override
	public String toString() {
		return theFile.getName().length() > 0 ? theFile.getName() : theFile
				.getPath();
	}

	public boolean expand(DefaultMutableTreeNode parent) {
		DefaultMutableTreeNode flag = (DefaultMutableTreeNode) parent
				.getFirstChild();
		if (flag == null) {
			return false;
		}
		Object obj = flag.getUserObject();
		if (!(obj instanceof Boolean)) {
			return false; // Already expanded
		}

		parent.removeAllChildren(); // Remove Flag

		File[] files = listFiles();
		if (files == null) {
			return true;
		}

		Vector<FileNode> v = new Vector<FileNode>();

		for (File f : files) {

			boolean accepted = true;

			if (getFileFilter() != null) {
				accepted = this.getFileFilter().accept(f);
			}

			if (accepted) {

				// use the same file filter
				FileNode newNode = new FileNode(f, this.getFileFilter());
				v.add(newNode);
			}
		}

		// now sort

		Collections.sort(v, comparator);

		for (FileNode nd : v) {
			IconData idata = null;

			// is it a file or a directory?

			if (nd.getFile().isFile()) {
				idata = new IconData(FileTree.ICON_FILE, null, nd);
			} else {
				idata = new IconData(FileTree.ICON_FOLDER,
						FileTree.ICON_EXPANDEDFOLDER, nd);
			}

			DefaultMutableTreeNode node = new DefaultMutableTreeNode(idata);
			parent.add(node);
			// this check determines whether the node is expandable
			if (nd.getFile().isDirectory()) {
				if ((nd.hasSubDirs()) || (nd.hasFiles())) {
					node.add(new DefaultMutableTreeNode(new Boolean(true)));
				}
			}
		}

		return true;
	}

	/**
	 * returns true if underlying file cointains any folders.
	 * 
	 * @return True if this node contains folders (subfirectories)
	 */

	public boolean hasSubDirs() {
		File[] files = listFiles();
		if (files == null) {
			return false;
		}
		for (File f : files) {
			if (f.isDirectory()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * returns true if underlying file cointains any files (that are accepred by
	 * the filter, if there is one.
	 * 
	 * @return True if this node contains files that are accepted by the filter
	 *         (if there is a filter)
	 */

	public boolean hasFiles() {
		File[] files = listFiles();
		if (files == null) {
			return false;
		}

		for (File f : files) {
			if (f.isFile()) {
				if ((fileFilter == null) || (fileFilter.accept(f))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * List all the files in the dir, if it is one!
	 * 
	 * @return An array of all files.
	 */

	protected File[] listFiles() {
		if (!theFile.isDirectory()) {
			return null;
		}
		try {
			return theFile.listFiles();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Error reading directory "
					+ theFile.getAbsolutePath(), "Warning",
					JOptionPane.WARNING_MESSAGE, ImageManager.cnuIcon);
			return null;
		}
	}

	/**
	 * @return Returns the fileFilter.
	 */
	public FileFilter getFileFilter() {
		return fileFilter;
	}

	/**
	 * @param fileFilter
	 *            The fileFilter to set.
	 */
	public void setFileFilter(FileFilter fileFilter) {
		this.fileFilter = fileFilter;
	}

	public int compare(Object o1, Object o2) {

		if ((o1 == null) || (o2 == null)) {
			return 0;
		}

		FileNode fn1, fn2;

		if (o1 instanceof FileNode) {
			fn1 = (FileNode) o1;
		} else {
			return 0;
		}

		if (o2 instanceof FileNode) {
			fn2 = (FileNode) o2;
		} else {
			return 0;
		}

		File f1 = fn1.getFile();
		File f2 = fn2.getFile();

		if ((f1 == null) || (f2 == null)) {
			return 0;
		}

		if ((f1.isDirectory()) && (f2.isFile())) {
			return -1;
		}

		if ((f2.isDirectory()) && (f1.isFile())) {
			return 1;
		}

		String s1 = f1.getName();
		String s2 = f2.getName();
		if ((s1 == null) || (s2 == null)) {
			return 0;
		}

		return s1.compareToIgnoreCase(s2);

	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(DataFlavor.javaFileListFlavor);
	}

	public List<File> getFileList() {
		List<File> list = new Vector<File>(1);
		list.add(theFile);
		return list;
	}

	@Override
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (flavor.equals(DataFlavor.javaFileListFlavor)) {
			return getFileList();
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

}
