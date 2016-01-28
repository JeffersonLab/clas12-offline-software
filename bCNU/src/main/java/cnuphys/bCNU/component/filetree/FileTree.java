/**
 * 
 */
package cnuphys.bCNU.component.filetree;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.FileUtilities;

@SuppressWarnings("serial")
public class FileTree extends JTree implements TreeSelectionListener,
		TreeExpansionListener, DragGestureListener, DragSourceListener {

	public static ImageIcon ICON_COMPUTER;

	/**
	 * Icon used to represent a disk
	 */
	public static ImageIcon ICON_DISK;

	/**
	 * Incon used to represent a file
	 */
	public static ImageIcon ICON_FILE;

	public static ImageIcon ICON_FOLDER;

	public static ImageIcon ICON_EXPANDEDFOLDER;

	private FileTreePanel _fileTreePanel;

	private DragSource dragSource = DragSource.getDefaultDragSource();

	private FileNode selectedNode;

	private FileFilter _fileFilter;

	/**
	 * Listener list for file selections.
	 */
	protected EventListenerList fileListenerList;

	/**
	 * Create a FileTree.
	 * 
	 * @param ff the filter to use.
	 * @param ftp the panel that will hold the file tree.
	 */
	public FileTree(FileFilter ff, FileTreePanel ftp) {
		this(ff, ftp, TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
	}

	/**
	 * Create a FileTree.
	 * 
	 * @param ff the filter to use.
	 */
	public FileTree(FileFilter ff) {
		this(ff, null);
	}

	/**
	 * Create a FileTree.
	 * 
	 * @param ff the filter to use.
	 * @param ftp the panel that will hold the file tree.
	 * @param selectionMode from TreeSelectionModel constants.
	 */
	public FileTree(FileFilter ff, FileTreePanel ftp, int selectionMode) {
		super();
		setOpaque(true);

		_fileTreePanel = ftp;
		_fileFilter = ff;

		// load the icons

		if (ICON_COMPUTER == null) {
			loadImages();
		}

		DefaultMutableTreeNode top = null;
		if (Environment.getInstance().isWindows()) {
			top = new DefaultMutableTreeNode(
					new IconData(ICON_COMPUTER, null, "My Computer"));
		}
		else {
			File rf = new File("/");
			top = new DefaultMutableTreeNode(
					new IconData(ICON_COMPUTER, null, new FileNode(rf)));
		}

		DefaultMutableTreeNode node;

		File roots[];
		if (Environment.getInstance().isWindows()) {
			roots = File.listRoots();
			for (int k = 0; k < roots.length; k++) {
				node = new DefaultMutableTreeNode(new IconData(ICON_DISK, null,
						new FileNode(roots[k], _fileFilter)));
				top.add(node);
				node.add(new DefaultMutableTreeNode(true));
			}
		}
		else {
			File rf = new File("/");
			roots = rf.listFiles();
			FileFilter unixRootFF = unixRootFilter();

			if (roots != null) {
				for (int k = 0; k < roots.length; k++) {
					if (unixRootFF.accept(roots[k])) {
						node = new DefaultMutableTreeNode(
								new IconData(ICON_DISK, null,
										new FileNode(roots[k], _fileFilter)));
						top.add(node);
						node.add(new DefaultMutableTreeNode(true));
					}
				}
			}
		}

		setModel(new DefaultTreeModel(top));

		// putClientProperty("JTree.lineStyle", "Angled");

		putClientProperty("JTree.lineStyle", "None");

		TreeCellRenderer renderer = new IconCellRenderer();
		setCellRenderer(renderer);

		addTreeExpansionListener(this);

		addTreeSelectionListener(this);

		// getSelectionModel().setSelectionMode(
		// TreeSelectionModel.SINGLE_TREE_SELECTION);
		getSelectionModel().setSelectionMode(selectionMode);
		setShowsRootHandles(true);
		setEditable(false);

		// set up the drag n drop
		setupDnD();

		// want to detect double clicks

		MouseListener ml = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int selRow = getRowForLocation(e.getX(), e.getY());
				TreePath selPath = getPathForLocation(e.getX(), e.getY());
				if (selRow != -1) {
					if (e.getClickCount() == 1) {
						if (e.getButton() == MouseEvent.BUTTON3) {
							handleRightClick(e, selRow, selPath);
						}
						else {
							handleSingleClick(selRow, selPath);
						}
					}
					else if (e.getClickCount() == 2) {
						handleDoubleClick(selRow, selPath);
					}
				}
			}
		};
		addMouseListener(ml);
	}

	/**
	 * Obtain a default dir, if there is one, which would have been passed in
	 * via a -p argument. Try to expand the dir in the file tree.
	 */
	public void expandDefaultDir() {

		// do an overall try catch. Don't want an exception here bringing down
		// the app.

		try {

			String defaultDir = FileUtilities.getDefaultDir();

			if (defaultDir == null) {
				return;
			}

			File file = new File(defaultDir);
			if (!file.exists() || !file.isDirectory()) {
				return;
			}

			String path = file.getCanonicalPath();

			// break the path into components
			String tokens[] = FileUtilities.tokenizePath(path);
			if (tokens == null) {
				return;
			}

			DefaultMutableTreeNode root = (DefaultMutableTreeNode) getModel()
					.getRoot();

			if ((root == null) || (root.getChildCount() < 1)) {
				return;
			}

			int tokenIndex = 0;
			while ((root != null) && (root.getChildCount() > 0)
					&& (tokenIndex < tokens.length)) {

				String token = tokens[tokenIndex];
				tokenIndex++;

				DefaultMutableTreeNode newRoot = getChild(root, token);
				if (newRoot != null) {
					TreeNode pathnodes[] = ((DefaultTreeModel) getModel())
							.getPathToRoot(newRoot);
					TreePath cpath = new TreePath(pathnodes);
					scrollPathToVisible(cpath);
					expandPath(cpath);
					newRoot = getChild(root, token);
					pathnodes = ((DefaultTreeModel) getModel())
							.getPathToRoot(newRoot);
				}
				root = newRoot;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private DefaultMutableTreeNode getChild(DefaultMutableTreeNode parent,
			String name) {
		if ((parent == null) || (parent.getChildCount() < 1)) {
			return null;
		}

		try {
			for (Enumeration<?> e = parent.children(); e.hasMoreElements();) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) (e
						.nextElement());
				FileNode fileNode = getFileNode(node);
				if (fileNode != null) {
					File file = fileNode.getFile();
					if ((file != null)
							&& (file.exists() && (file.isDirectory()))) {
						String subTokens[] = FileUtilities
								.tokenizePath(file.getPath());

						if ((subTokens != null) && (subTokens.length > 0)) {

							String fname = subTokens[subTokens.length - 1];
							if (name.equalsIgnoreCase(fname)) {
								return node;
							}
						}
						else {
							Log.getInstance().warning(
									"tokenizer in fileTree getChiild not working properly.");
						}
					}
				} // fileNode != null
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	// handle a right click on a file
	protected void handleRightClick(MouseEvent e, int selRow,
			TreePath selPath) {
		DefaultMutableTreeNode node = getTreeNode(selPath);
		FileNode fnode = getFileNode(node);
		if (fnode != null) {
			// default operates on files only
			final File file = fnode.getFile();
			if ((file != null) && file.isFile()) {
				JPopupMenu popupMenu = new JPopupMenu();
				popupMenu.setLightWeightPopupEnabled(false);

				ActionListener al = new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						notifyFileListeners(file.getPath());
					}

				};

				JMenuItem openItem = new JMenuItem("Open: " + file.getPath());
				openItem.addActionListener(al);
				popupMenu.add(openItem);

				popupMenu.show(this, e.getX(), e.getY());
			}

			// notifyFileListeners(fnode.getFile().getPath());
		}
	}

	// handle a single click on a file
	protected void handleSingleClick(int selRow, TreePath selPath) {
	}

	/**
	 * Handle a double click
	 * 
	 * @param selRow
	 * @param selPath
	 */
	protected void handleDoubleClick(int selRow, TreePath selPath) {
		List<File> files = getSelectedFileList();
		if ((files == null) || (files.size() < 1)) {
			return;
		}

		notifyFileListeners(files);

		// DefaultMutableTreeNode node = getTreeNode(selPath);
		// FileNode fnode = getFileNode(node);
		// if (fnode != null) {
		// final File file = fnode.getFile();
		// if ((file != null) && file.isFile()) {
		// notifyFileListeners(fnode.getFile().getPath());
		// }
		// }
	}

	// get the file corresponding to a node
	protected File getFile(DefaultMutableTreeNode node) {
		if (node == null) {
			return null;
		}

		FileNode fnode = getFileNode(node);
		if (fnode == null) {
			return null;
		}

		return fnode.getFile();
	}

	/**
	 * Notify listeners that a file was clicked.
	 * 
	 * @param fullPath The full path of the file.
	 */
	private void notifyFileListeners(final List<File> files) {

		if ((fileListenerList == null) || (files == null)
				|| (files.size() < 1)) {
			return;
		}

		if (files.size() == 1) {
			notifyFileListeners(files.get(0).getPath());
			return;
		}

		// Guaranteed to return a non-null array
		Object[] listeners = fileListenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IFileTreeListener.class) {
				((IFileTreeListener) listeners[i + 1])
						.filesDoubleClicked(files);
			}
		}
	}

	/**
	 * Notify listeners that a file was clicked.
	 * 
	 * @param fullPath The full path of the file.
	 */
	private void notifyFileListeners(String fullPath) {
		if ((fileListenerList == null) || (fullPath == null)) {
			return;
		}

		// Guaranteed to return a non-null array
		Object[] listeners = fileListenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IFileTreeListener.class) {
				((IFileTreeListener) listeners[i + 1])
						.fileDoubleClicked(fullPath);
			}
		}
	}

	/**
	 * convenience routine for setting up drag and drop
	 * 
	 */
	private void setupDnD() {

		// gesture recognizer establishes what action starts the dnd

		DragGestureRecognizer dgr = dragSource
				.createDefaultDragGestureRecognizer(this, // DragSource
						DnDConstants.ACTION_COPY, // specifies valid actions
						this // DragGestureListener
		);

		/*
		 * Eliminates right mouse clicks as valid actions - useful especially if
		 * you implement a JPopupMenu for the JTree
		 */
		dgr.setSourceActions(dgr.getSourceActions() & ~InputEvent.BUTTON3_MASK);
	}

	/**
	 * Tree selection listener
	 * 
	 * @param event The selection event.
	 */
	@Override
	public void valueChanged(TreeSelectionEvent event) {

		DefaultMutableTreeNode node = getTreeNode(event.getPath());
		FileNode fnode = getFileNode(node);
		if (fnode != null) {
			selectedNode = fnode;
			if (_fileTreePanel != null) {
				_fileTreePanel.setText(fnode.getFile().getAbsolutePath());
			}
		}
		else {
			selectedNode = null;
			if (_fileTreePanel != null) {
				_fileTreePanel.setText("");
			}
		}

	}

	/**
	 * Return the last selected file
	 * 
	 * @return the last selection as a String
	 */

	public String getLastSelection() {
		if (selectedNode == null) {
			return null;
		}
		else {
			try {
				return selectedNode.getFile().getAbsolutePath();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * One of the expansion icons was hit.
	 * 
	 * @param event the causal event.
	 */
	@Override
	public void treeExpanded(TreeExpansionEvent event) {

		final DefaultMutableTreeNode node = getTreeNode(event.getPath());

		final FileNode fnode = getFileNode(node);
		if (fnode != null && fnode.expand(node)) {
			((DefaultTreeModel) getModel()).reload(node);
		}

		// BELOW id if we want to do this in a thread. I see no need.
		// Thread runner = new Thread() {
		// @Override
		// public void run() {
		// if (fnode != null && fnode.expand(node)) {
		// Runnable runnable = new Runnable() {
		// public void run() {
		// ((DefaultTreeModel)getModel()).reload(node);
		// }
		// };
		// SwingUtilities.invokeLater(runnable);
		// }
		// }
		// };
		// runner.start();
	}

	@Override
	public void treeCollapsed(TreeExpansionEvent event) {
		final DefaultMutableTreeNode node = getTreeNode(event.getPath());
		final FileNode fnode = getFileNode(node);

		Thread runner = new Thread() {
			@Override
			public void run() {
				if (fnode != null && fnode.expand(node)) {
					Runnable runnable = new Runnable() {
						@Override
						public void run() {
							((DefaultTreeModel) getModel()).reload(node);
						}
					};
					SwingUtilities.invokeLater(runnable);
				}
			}
		};
		runner.start();
	}

	DefaultMutableTreeNode getTreeNode(TreePath path) {
		return (DefaultMutableTreeNode) (path.getLastPathComponent());
	}

	/**
	 * Get the file node from the tree node.
	 * 
	 * @param node
	 * @return
	 */
	private FileNode getFileNode(DefaultMutableTreeNode node) {
		if (node == null) {
			return null;
		}
		Object obj = node.getUserObject();

		if (obj instanceof IconData) {
			obj = ((IconData) obj).getObject();
		}
		if (obj instanceof FileNode) {
			return (FileNode) obj;
		}
		else {
			return null;
		}
	}

	//
	//
	// private DefaultMutableTreeNode getTreeNode(File file) {
	// if (file == null) {
	// return null;
	// }
	//
	// FileNode fnode = new FileNode(file);
	// return new DefaultMutableTreeNode(fnode);
	// }

	/**
	 * Once only load the images.
	 */
	private static void loadImages() {
		ImageManager im = ImageManager.getInstance();
		ICON_DISK = im.loadImageIcon("images/disk.gif");
		ICON_COMPUTER = im.loadImageIcon("images/compicon.gif");
		ICON_FILE = im.loadImageIcon("images/file.gif");
		ICON_FOLDER = im.loadImageIcon("images/folder.gif");
		ICON_EXPANDEDFOLDER = im.loadImageIcon("images/expandedfolder.gif");
	}

	private List<File> getSelectedFileList() {
		TreePath[] paths = getSelectionPaths();
		if ((paths == null) || (paths.length < 1)) {
			return null;
		}

		Vector<File> list = new Vector<File>(paths.length);

		for (TreePath path : paths) {
			DefaultMutableTreeNode node = getTreeNode(path);
			if (node != null) {
				FileNode fnode = getFileNode(node);
				if (fnode != null) {
					File file = fnode.getFile();
					if (file != null) {
						list.add(file);
					}
				}
			}

		}

		return list;
	}

	/**
	 * Starts the drag process
	 */
	@Override
	public void dragGestureRecognized(DragGestureEvent dge) {
		// Get the selected nodes (even if single select)
		List<File> list = getSelectedFileList();
		if ((list == null) || (list.size() < 1)) {
			return;
		}
		FileNodes fn = new FileNodes(list);

		// Get the Transferable Object
		Transferable transferable = fn;

		// begin the drag
		try {
			dragSource.startDrag(dge, DragSource.DefaultCopyDrop, transferable,
					this);
		} catch (InvalidDnDOperationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void dragEnter(DragSourceDragEvent dsde) {
	}

	@Override
	public void dragOver(DragSourceDragEvent dsde) {
	}

	@Override
	public void dropActionChanged(DragSourceDragEvent dsde) {
	}

	@Override
	public void dragExit(DragSourceEvent dse) {
	}

	@Override
	public void dragDropEnd(DragSourceDropEvent dsde) {
	}

	/**
	 * Remove a filetree listener.
	 * 
	 * @param fl the filetree listener to remove.
	 */
	public void removeFileTreeListener(IFileTreeListener fl) {

		if ((fl == null) || (fileListenerList == null)) {
			return;
		}

		fileListenerList.remove(IFileTreeListener.class, fl);
	}

	/**
	 * Add a file listener.
	 * 
	 * @param fl the filetree listener to add.
	 */
	public void addFileTreeListener(IFileTreeListener fl) {

		if (fl == null) {
			return;
		}

		if (fileListenerList == null) {
			fileListenerList = new EventListenerList();
		}

		fileListenerList.add(IFileTreeListener.class, fl);
	}

	private FileFilter unixRootFilter() {
		FileFilter ff = new FileFilter() {

			@Override
			public boolean accept(File file) {
				if (file == null) {
					return false;
				}

				String name = file.getName();
				if (name == null) {
					return false;
				}

				if (name.startsWith(".")) {
					return false;
				}

				if (name.equals("etc")) {
					return false;
				}
				if (name.equals("dev")) {
					return false;
				}
				if (name.equals("Network")) {
					return false;
				}
				if (name.equals("bin")) {
					return false;
				}
				if (name.equals("cores")) {
					return false;
				}
				if (name.equals("mach_kernel")) {
					return false;
				}
				if (name.equals("net")) {
					return false;
				}
				if (name.equals("sbin")) {
					return false;
				}
				if (name.equals("System")) {
					return false;
				}
				if (name.equals("User Guides And Information")) {
					return false;
				}
				if (name.equals("Library")) {
					return false;
				}
				if (name.equals("System")) {
					return false;
				}

				return true;
			}

			@Override
			public String getDescription() {
				return null;
			}

		};

		return ff;
	}

}
