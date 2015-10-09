package cnuphys.bCNU.xml.tree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.util.FileUtilities;

/**
 * @author heddle A simple tree for displaying a xml document parsed via SAX
 */
@SuppressWarnings("serial")
public class SAXJTree extends JTree {

	// private static Color background = new Color(212, 208, 200);
	private static Color background = Color.white;

	/**
	 * An optional scroll pane.
	 */
	private JScrollPane scrollPane = null;

	/**
	 * constructor
	 * 
	 */
	public SAXJTree() {
		setModel(null);

		setBackground(background);

		DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) getCellRenderer();
		renderer.setBackgroundSelectionColor(Color.darkGray);
		renderer.setBackgroundNonSelectionColor(background);
		// renderer.setBorderSelectionColor(background);

		BasicTreeUI basicUi = (BasicTreeUI) getUI();

		// try using plus and minus icons
		try {
			basicUi.setCollapsedIcon(ImageManager.getInstance().loadImageIcon(
					"images/treePlusBox.gif"));
			basicUi.setExpandedIcon(ImageManager.getInstance().loadImageIcon(
					"images/treeMinusBox.gif"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		setBorder(BorderFactory.createTitledBorder(null, "XML elements",
				TitledBorder.LEADING, TitledBorder.TOP, null, Color.blue));

		putClientProperty("JTree.lineStyle", "Angled");
		setShowsRootHandles(true);
		setEditable(false);
	}

	/**
	 * Put the tree in a scroll pane
	 * 
	 * @return a scroll pane holding the tree
	 */
	public JScrollPane getScrollPane() {

		if (scrollPane == null) {
			scrollPane = new JScrollPane();
			scrollPane.getViewport().setView(this);
		}

		return scrollPane;
	}

	/**
	 * Build the tree from an xml string
	 * 
	 * @param xmlstr
	 *            the xml string
	 * @throws IOException
	 * @throws SAXException
	 */
	public void buildTree(String xmlstr) throws IOException, SAXException {

		if (xmlstr == null) {
			throw (new IOException("null XMLString"));
		}

		// Create instances needed for parsing
		XMLReader reader = XMLReaderFactory.createXMLReader();

		ContentHandler jTreeHandler = new JTreeHandler(this);

		// Register content handler
		reader.setContentHandler(jTreeHandler);

		StringReader stringReader = new StringReader(xmlstr);
		InputSource inputSource = new InputSource(stringReader);
		reader.parse(inputSource);
	}

	/**
	 * Build the tree from an xml file
	 * 
	 * @param file
	 *            the xml file
	 * @throws IOException
	 * @throws SAXException
	 */
	public void buildTree(File file) throws IOException, SAXException {

		if (file == null) {
			throw (new IOException("null File"));
		}

		// Create instances needed for parsing
		XMLReader reader = XMLReaderFactory.createXMLReader();

		ContentHandler jTreeHandler = new JTreeHandler(this);

		// Register content handler
		reader.setContentHandler(jTreeHandler);

		FileReader fileReader = null;
		try {
			fileReader = new FileReader(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		InputSource inputSource = new InputSource(fileReader);
		reader.parse(inputSource);

		// expand the tree
		for (int i = 0; i < getRowCount(); i++) {
			expandRow(i);
		}
	}

	// Traverse all nodes in tree
	public void visitAllNodes(NodeProcessor nodeProcessor) {
		TreeNode root = (TreeNode) getModel().getRoot();
		visitAllNodes(nodeProcessor, root);
	}

	/**
	 * Traverse the tree, visit all nodes exactly once
	 * 
	 * @param nodeProcessor
	 * @param node
	 */
	private void visitAllNodes(NodeProcessor nodeProcessor, TreeNode node) {

		if (nodeProcessor != null) {
			try {
				nodeProcessor.processNode(this, node);
				Thread.yield();
			} catch (BadNodeException e) {
				e.printStackTrace();
			}
		}

		if (node.getChildCount() >= 0) {
			for (Enumeration<?> e = node.children(); e.hasMoreElements();) {
				TreeNode n = (TreeNode) e.nextElement();
				visitAllNodes(nodeProcessor, n);
			}
		}
	}

	public String[] nodeTokens(TreeNode node) {
		if (node == null) {
			return null;
		}

		String tokens[] = FileUtilities.tokens(node.toString(), ":");

		if (tokens != null) {

			if ((tokens.length == 1) && (tokens[0].startsWith("Attribute"))) {
				return parseAttribute(tokens[0]);
			}

			if ((tokens.length == 2)
					&& (tokens[0].startsWith("Character Data"))) {
				return parseCharacterData(tokens[0], tokens[1]);
			}

			for (int i = 0; i < tokens.length; i++) {
				tokens[i] = tokens[i].replace('\'', ' ');
				tokens[i] = tokens[i].trim();
			}
		}

		return tokens;
	}

	private String[] parseCharacterData(String s0, String s1) {
		if ((s0 == null) || (s1 == null)) {
			return null;
		}

		String tokens[] = FileUtilities.tokens(s1, " ");

		String stokens[] = null;

		if (tokens == null) {
			stokens = new String[2];
			stokens[0] = s0;
			stokens[1] = s1;
		} else {
			stokens = new String[tokens.length + 1];
			stokens[0] = s0;
			for (int i = 0; i < tokens.length; i++) {
				tokens[i] = tokens[i].replace('\'', ' ');
				tokens[i] = tokens[i].trim();
				stokens[i + 1] = tokens[i];
			}
		}
		return stokens;
	}

	private String[] parseAttribute(String attStr) {
		if (attStr == null) {
			return null;
		}

		String tokens[] = FileUtilities.tokens(attStr, "'");

		if ((tokens == null) || (tokens.length < 4)) {
			return tokens;
		}

		String stokens[] = new String[3];
		stokens[0] = "Attribute";
		stokens[1] = tokens[1]; // name
		stokens[2] = tokens[3]; // value
		return stokens;
	}

	/**
	 * Main program for testing.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		final JFrame testFrame = new JFrame();

		// set up what to do if the window is closed
		WindowAdapter windowAdapter = new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent event) {
				testFrame.setVisible(false);
			}
		};

		testFrame.addWindowListener(windowAdapter);
		testFrame.setSize(600, 600);

		final SAXJTree tree = new SAXJTree();

		testFrame.getContentPane().add(tree.getScrollPane(),
				BorderLayout.CENTER);

		File file = new File("C:\\temp\\run2.xml");
		try {
			tree.buildTree(file);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}

		testFrame.setVisible(true);

		NodeProcessor np = new NodeProcessor() {

			@Override
			public void processNode(SAXJTree tree, TreeNode node) {
				String tokens[] = tree.nodeTokens(node);
				if (tokens != null) {
					System.out.println("");
					for (String s : tokens) {
						System.out.print("[" + s + "] ");
					}
				}
			}

		};

		tree.visitAllNodes(np);
	}
}

/**
 * This class does the actual parsing, and adds nodes to the tree.
 */

class JTreeHandler extends DefaultHandler {

	/** Current node to add sub-nodes to */
	private DefaultMutableTreeNode current = null;

	private JTree tree;

	private DefaultTreeModel model = null;

	/**
	 * Constructor
	 * 
	 * @param base
	 *            the base (root) tree node
	 */
	public JTreeHandler(JTree tree) {
		this.tree = tree;
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String s = new String(ch, start, length);
		DefaultMutableTreeNode data = new DefaultMutableTreeNode(
				"Character Data: '" + s + "'");
		current.add(data);
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// Walk back up the tree
		current = (DefaultMutableTreeNode) current.getParent();

	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		DefaultMutableTreeNode ws = new DefaultMutableTreeNode(
				"Whitespace (length = " + length + ")");
		current.add(ws);

	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		DefaultMutableTreeNode pi = new DefaultMutableTreeNode("PI (target = '"
				+ target + "', data = '" + data + "')");
		current.add(pi);

	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		DefaultMutableTreeNode skipped = new DefaultMutableTreeNode(
				"Skipped Entity: '" + name + "'");
		current.add(skipped);

	}

	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		DefaultMutableTreeNode element = new DefaultMutableTreeNode("Element: "
				+ localName);

		// is this the root?

		if (current == null) {
			model = new DefaultTreeModel(element);
			tree.setModel(model);
		} else {
			current.add(element);
		}

		current = element;

		// Process attributes
		for (int i = 0; i < atts.getLength(); i++) {
			DefaultMutableTreeNode attribute = new DefaultMutableTreeNode(
					"Attribute (name = '" + atts.getLocalName(i)
							+ "', value = '" + atts.getValue(i) + "')");
			current.add(attribute);
		}

	}

}
