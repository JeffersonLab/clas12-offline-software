package cnuphys.bCNU.view;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JSplitPane;

import cnuphys.bCNU.component.filetree.ExtensionFileFilter;
import cnuphys.bCNU.component.filetree.FileDnDHandler;
import cnuphys.bCNU.component.filetree.FileTreePanel;
import cnuphys.bCNU.component.filetree.IFileTreeListener;
import cnuphys.bCNU.file.AFileHandler;
import cnuphys.bCNU.file.FileHandlerFactory;
import cnuphys.bCNU.file.IFileHandler;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.xml.tree.SAXJTree;

@SuppressWarnings("serial")
public class XMLView extends BaseView implements IFileHandler,
		IFileTreeListener {

	// constant used for file tree width
	private static final int FILE_PANEL_WIDTH = 200;

	/**
	 * List of extension that appear in the file tree
	 */
	private static ArrayList<String> extensions;

	/**
	 * These are the extension the file filter will show
	 */
	static {
		extensions = new ArrayList<String>(5);
		extensions.add("xml");
	}

	// the tree widget
	private SAXJTree _saxTree;

	// for file dragdrop
	private FileTreePanel _fileTreePanel;

	/**
	 * Constructor for XML view.
	 */
	public XMLView() {
		super(PropertySupport.TITLE, "XML Tree", PropertySupport.ICONIFIABLE, true,
			PropertySupport.MAXIMIZABLE, true, PropertySupport.CLOSABLE, true,
			PropertySupport.RESIZABLE, true, PropertySupport.WIDTH, 700,
			PropertySupport.HEIGHT, 700, PropertySupport.VISIBLE, false);

		_saxTree = new SAXJTree();
		_fileTreePanel = createFileTreePanel();

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				false, _fileTreePanel, _saxTree.getScrollPane());
		splitPane.setResizeWeight(0.2);

		add(splitPane);
		new FileDnDHandler(this, this, this);

	}

	/**
	 * Creates the file tree panel.
	 */
	private FileTreePanel createFileTreePanel() {
		// the file tree
		ExtensionFileFilter filter = new ExtensionFileFilter(extensions);
		FileTreePanel fileTree = new FileTreePanel(filter);
		Dimension size = fileTree.getPreferredSize();
		size.width = FILE_PANEL_WIDTH;
		fileTree.setPreferredSize(size);
		fileTree.addFileTreeListener(this);
		return fileTree;
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
		System.err.println("must handle file: " + file.getPath() + "  reason: "
				+ action);

		// use the factory to create the correct handler.
		AFileHandler fileHandler = FileHandlerFactory.createFileHandler(parent,
				file);
		if (fileHandler != null) {
			fileHandler.handleFile(parent, file, action);
		}
	}

	/**
	 * Handle multiple files, e.g., because of a drag and drop.
	 * 
	 * @param parent
	 *            the object being affected (might be null).
	 * @param files
	 *            the files to handle
	 * @param action
	 *            the action that initiated the need to handle this file
	 */
	@Override
	public void handleFiles(Object parent, File files[], HandleAction action) {
		System.err.println("XMLView got " + files.length + " files.");
		System.err.println("For now just peeling off the top file");

		handleFile(parent, files[0], action);
	}

	@Override
	public void fileDoubleClicked(String fullPath) {
		System.err.println("double clicked file: " + fullPath);
		File file = new File(fullPath);
		if (file.exists()) {
			// use the factory to create the correct handler.
			AFileHandler fileHandler = FileHandlerFactory.createFileHandler(
					this, file);
			if (fileHandler != null) {
				fileHandler.handleFile(this, file, HandleAction.OPEN);
			}
		}
	}

	@Override
	public void filesDoubleClicked(List<File> files) {
		for (File file : files) {
			fileDoubleClicked(file.getPath());
		}
	}

	/**
	 * Get the tree object--for example to display an xml file.
	 * 
	 * @return the tree object.
	 */
	public SAXJTree getSaxTree() {
		return _saxTree;
	}

}
