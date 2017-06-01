/**
 * 
 */
package cnuphys.bCNU.component.filetree;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultTreeModel;

import cnuphys.bCNU.util.Fonts;

@SuppressWarnings("serial")
public class FileTreePanel extends JPanel {

	/**
	 * The underlying file tree. Sample use: <code>
	 * 
	 * static {
	 * 	  extensions = new ArrayList<String>(10);
	 * 	  extensions.add("xml");
	 * 	  extensions.add("txt");
	 * 	  extensions.add("ev");
	 *  }
	 * 	
	 * 	ExtensionFileFilter filter = new ExtensionFileFilter(extensions);
	 * 	fileTreePanel = new FileTreePanel(filter);
	 * 	Dimension size = fileTreePanel.getPreferredSize();
	 * 	size.width = FILE_PANEL_WIDTH;
	 * 	fileTreePanel.setPreferredSize(size);
	 * 
	 * fileTreePanel.addFileTreeListener(myFileTreeListener);
	 * 
	 * </code>
	 */
	protected FileTree fileTree = null;
	// protected static FileTreeServer fileTreeServer = null;

	protected DefaultTreeModel m_model;

	/**
	 * A textfield along the top
	 */
	protected JTextField textField;

	/**
	 * Constructor
	 */
	public FileTreePanel() {
		this(null);
	}

	/**
	 * Constructor
	 */
	public FileTreePanel(FileFilter fileFilter) {
		setLayout(new BorderLayout(2, 2));

		fileTree = new FileTree(fileFilter, this);
		fileTree.expandDefaultDir();

		JPanel jp = new JPanel();

		jp.setBackground(Color.white);
		jp.setOpaque(true);

		jp.setLayout(new BorderLayout());
		jp.add(fileTree, BorderLayout.WEST);

		JScrollPane s = new JScrollPane();
		s.getViewport().add(jp);

		// Added 4 Sep 09 by Chris McCann to increase vertical scrolling speed
		s.getVerticalScrollBar().setUnitIncrement(16);

		add(s, BorderLayout.CENTER);

		textField = new JTextField();
		textField.setFont(Fonts.smallFont);
		textField.setEditable(false);
		add(textField, BorderLayout.NORTH);
	}

	/**
	 * Add a file listener.
	 * 
	 * @param fl
	 *            the filetree listener to add.
	 */
	public void addFileTreeListener(IFileTreeListener fl) {
		if (fileTree != null) {
			fileTree.addFileTreeListener(fl);
		}
	}

	/**
	 * Return the last selected file in the file tree
	 * 
	 * @return the last selected file name (full path) in the file tree
	 *         (explorer).
	 */
	public String getLastSelection() {
		if (fileTree == null) {
			return null;
		} else {
			return fileTree.getLastSelection();
		}
	}

	/**
	 * Set the text in the text field.
	 * 
	 * @param s
	 *            the text to set.
	 */
	public void setText(String s) {

		if (textField != null) {
			if (s == null) {
				s = "";
			}
			textField.setText(s);
		}
	}

	/**
	 * Get the underlying file tree.
	 * 
	 * @return the file tree.
	 */
	public FileTree getFileTree() {
		return fileTree;
	}

}
