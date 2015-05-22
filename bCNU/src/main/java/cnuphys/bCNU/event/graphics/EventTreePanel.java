package cnuphys.bCNU.event.graphics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jlab.coda.jevio.BaseStructure;
import org.jlab.coda.jevio.BaseStructureHeader;
import org.jlab.coda.jevio.CompositeData;
import org.jlab.coda.jevio.EvioEvent;
import org.jlab.coda.jevio.EvioException;

import cnuphys.bCNU.et.ETSupport;
import cnuphys.bCNU.event.EventControl;

public class EventTreePanel extends JPanel implements TreeSelectionListener {

    /**
     * Panel for displaying header information.
     */
    private HeaderPanel headerPanel = new HeaderPanel();

    /**
     * Panel for displaying event information.
     */
    private EventInfoPanel eventInfoPanel = new EventInfoPanel();

    /**
     * The actual graphical tree object.
     */
    private JTree tree;

    /**
     * The current event.
     */
    private EvioEvent event;

    /**
     * Text area shows data values for selected nodes.
     */
    private JTextArea _dataTextArea;

    /**
     * View ints in hexadecimal or decimal?
     */
    private boolean intsInHex;

    /**
     * Class helping to contain info about the currently selected evio
     * structure.
     */
    private class SelectionInfo {
	/** Evio header tag. */
	int tag;
	/**
	 * When multiple children have the same tag, this indicates the position
	 * of the selected child (starting from 0).
	 */
	int pos;

	SelectionInfo(int tag, int pos) {
	    this.tag = tag;
	    this.pos = pos;
	}
    }

    /**
     * Info about the currently selected evio structure being viewed. Empty if
     * none or if selection contains evio structures and not actual data. It's a
     * linked list since structure is part of hierarchy.
     */
    LinkedList<SelectionInfo> structureSelection = new LinkedList<SelectionInfo>();

    /**
     * Constructor for a simple tree viewer for evio files.
     */
    public EventTreePanel() {
	setLayout(new BorderLayout());
	// add all the components
	addComponents();
    }

    /**
     * Get the selection path information list.
     * 
     * @return the selection path information list.
     */
    public LinkedList<SelectionInfo> getStructureSelection() {
	return structureSelection;
    }

    /**
     * Set wether integer data is displayed in hexidecimal or decimal.
     * 
     * @param intsInHex
     *            if <code>true</code> then display as hex, else deciaml
     */
    public void setIntsInHex(boolean intsInHex) {
	this.intsInHex = intsInHex;
    }

    /**
     * Get the panel displaying the event information.
     * 
     * @return
     */
    public EventInfoPanel getEventInfoPanel() {
	return eventInfoPanel;
    }

    /**
     * Get the panel displaying header information.
     * 
     * @return
     */
    public HeaderPanel getHeaderPanel() {
	return headerPanel;
    }

    /**
     * Refresh textArea display.
     */
    public void refreshDisplay() {
	valueChanged(null);
    }

    /**
     * Refresh description (dictionary) display.
     */
    public void refreshDescription() {
	headerPanel.setDescription(event);
    }

    /**
     * Add the components to this panel.
     */
    protected void addComponents() {
	add(eventInfoPanel, BorderLayout.NORTH);

	JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
		false, createDataTextArea(), createTree());
	splitPane.setResizeWeight(0.0);
	add(splitPane, BorderLayout.CENTER);

	// define south

	JPanel panel = new JPanel();
	panel.setLayout(new BorderLayout());
	panel.add(headerPanel, BorderLayout.CENTER);

	add(panel, BorderLayout.SOUTH);
    }

    /**
     * Create the tree that will display the event. What is actually returned is
     * the scroll pane that contains the tree.
     *
     * @return the scroll pane holding the event tree.
     */
    private JScrollPane createTree() {
	tree = new JTree();
	tree.setModel(null);

	tree.setBorder(BorderFactory.createTitledBorder(null,
		"EVIO event tree", TitledBorder.LEADING, TitledBorder.TOP,
		null, Color.blue));

	tree.putClientProperty("JTree.lineStyle", "Angled");
	tree.setShowsRootHandles(true);
	tree.setEditable(false);
	tree.getSelectionModel().setSelectionMode(
		TreeSelectionModel.SINGLE_TREE_SELECTION);
	tree.addTreeSelectionListener(this);

	JScrollPane scrollPane = new JScrollPane();
	scrollPane.getViewport().setView(tree);
	return scrollPane;
    }

    /**
     * Create the text area that will display structure data. What is actually
     * returned is the scroll pane that contains the text area.
     *
     * @return the scroll pane holding the text area.
     */
    private JScrollPane createDataTextArea() {

	_dataTextArea = new JTextArea();
	_dataTextArea.setBorder(BorderFactory.createTitledBorder(null, "Data",
		TitledBorder.LEADING, TitledBorder.TOP, null, Color.blue));
	_dataTextArea.setEditable(false);

	JScrollPane scrollPane = new JScrollPane();
	scrollPane.getViewport().setView(_dataTextArea);
	// Borderlayout respects preferred width in east/west,
	// but ignores height -- so use this to set width only.
	// Don't use "setPreferredSize" on textArea or it messes up the
	// scrolling.
	scrollPane.setPreferredSize(new Dimension(200, 1000));

	return scrollPane;
    }

    /**
     * Selection event on our tree.
     *
     * @param treeSelectionEvent
     *            the causal event.
     */
    @Override
    public void valueChanged(TreeSelectionEvent treeSelectionEvent) {

	BaseStructure structure = (BaseStructure) tree
		.getLastSelectedPathComponent();
	_dataTextArea.setText("");
	int blankLineEveryNth = 5; // put in a blank line after every Nth
				   // element listed

	if (structure == null) {
	    return;
	}
	headerPanel.setHeader(structure);

	// Old selection is not remembered
	structureSelection.clear();

	if (structure.isLeaf()) {
	    int pos;

	    // Store information about the current selection, if any
	    TreePath selectionPath = tree.getSelectionPath();

	    if (selectionPath != null) {
		// Root is the first element, etc.
		Object[] pathItems = selectionPath.getPath();

		// Pull info out of tree
		if (pathItems != null && pathItems.length > 0) {
		    for (int i = 0; i < pathItems.length; i++) {
			BaseStructure bs = (BaseStructure) pathItems[i];
			// Find what # child we are by looking at all kids of
			// our parent
			pos = 0;
			BaseStructure parent = bs.getParent();
			// If null parent, pos = 0
			if (parent != null) {
			    Iterator<BaseStructure> iter = parent.getChildren()
				    .iterator();
			    for (int j = 0; iter.hasNext(); j++) {
				BaseStructure bsKid = iter.next();
				if (bsKid == bs) {
				    pos = j;
				    break;
				}
			    }
			}

			structureSelection.add(new SelectionInfo(bs.getHeader()
				.getTag(), pos));
		    }
		}
	    }

	    int lineCounter = 1, index = 1;
	    BaseStructureHeader header = structure.getHeader();

	    switch (header.getDataType()) {
	    case DOUBLE64:
		double doubledata[] = structure.getDoubleData();
		if (doubledata != null) {
		    for (double d : doubledata) {
			String s = String.format("[%02d]  %15.11e", index++, d);
			_dataTextArea.append(s);
			if (lineCounter < doubledata.length) {
			    if (lineCounter % blankLineEveryNth == 0) {
				_dataTextArea.append("\n\n");
			    } else {
				_dataTextArea.append("\n");
			    }
			    lineCounter++;
			}
		    }
		} else {
		    _dataTextArea.append("null data\n");
		}
		break;

	    case FLOAT32:
		float floatdata[] = structure.getFloatData();
		if (floatdata != null) {
		    for (float d : floatdata) {
			String s = String.format("[%02d]  %10.6e", index++, d);
			_dataTextArea.append(s);
			if (lineCounter < floatdata.length) {
			    if (lineCounter % blankLineEveryNth == 0) {
				_dataTextArea.append("\n\n");
			    } else {
				_dataTextArea.append("\n");
			    }
			    lineCounter++;
			}
		    }
		} else {
		    _dataTextArea.append("null data\n");
		}
		break;

	    case LONG64:
	    case ULONG64:
		long longdata[] = structure.getLongData();
		if (longdata != null) {
		    for (long i : longdata) {
			String s;
			if (intsInHex) {
			    s = String.format("[%02d]  %#018X", index++, i);
			} else {
			    s = String.format("[%02d]  %d", index++, i);
			}
			_dataTextArea.append(s);
			if (lineCounter < longdata.length) {
			    if (lineCounter % blankLineEveryNth == 0) {
				_dataTextArea.append("\n\n");
			    } else {
				_dataTextArea.append("\n");
			    }
			    lineCounter++;
			}
		    }
		} else {
		    _dataTextArea.append("null data\n");
		}
		break;

	    case INT32:
	    case UINT32:
		int intdata[] = structure.getIntData();
		if (intdata != null) {
		    for (int i : intdata) {
			String s;
			if (intsInHex) {
			    s = String.format("[%02d]  %#010X", index++, i);
			} else {
			    s = String.format("[%02d]  %d", index++, i);
			}
			_dataTextArea.append(s);
			if (lineCounter < intdata.length) {
			    if (lineCounter % blankLineEveryNth == 0) {
				_dataTextArea.append("\n\n");
			    } else {
				_dataTextArea.append("\n");
			    }
			    lineCounter++;
			}
		    }
		} else {
		    _dataTextArea.append("null data\n");
		}
		break;

	    case SHORT16:
	    case USHORT16:
		short shortdata[] = structure.getShortData();
		if (shortdata != null) {
		    for (short i : shortdata) {
			String s;
			if (intsInHex) {
			    s = String.format("[%02d]  %#06X", index++, i);
			} else {
			    s = String.format("[%02d]  %d", index++, i);
			}
			_dataTextArea.append(s);
			if (lineCounter < shortdata.length) {
			    if (lineCounter % blankLineEveryNth == 0) {
				_dataTextArea.append("\n\n");
			    } else {
				_dataTextArea.append("\n");
			    }
			    lineCounter++;
			}
		    }
		} else {
		    _dataTextArea.append("null data\n");
		}
		break;

	    case CHAR8:
	    case UCHAR8:
		byte bytedata[] = structure.getByteData();
		if (bytedata != null) {
		    for (byte i : bytedata) {
			String s = String.format("[%02d]  %d", index++, i);
			_dataTextArea.append(s);
			if (lineCounter < bytedata.length) {
			    if (lineCounter % blankLineEveryNth == 0) {
				_dataTextArea.append("\n\n");
			    } else {
				_dataTextArea.append("\n");
			    }
			    lineCounter++;
			}
		    }
		} else {
		    _dataTextArea.append("null data\n");
		}
		break;

	    case CHARSTAR8:
		String stringdata[] = structure.getStringData();
		for (String str : stringdata) {
		    String s = String.format("[%02d]  %s\n", index++, str);
		    _dataTextArea.append(s != null ? s : "null data\n");
		}
		break;

	    case COMPOSITE:
		try {
		    CompositeData[] cData = structure.getCompositeData();
		    if (cData != null) {
			for (int i = 0; i < cData.length; i++) {
			    _dataTextArea.append("composite data object ");
			    _dataTextArea.append(i + ":\n");
			    _dataTextArea.append(cData[i].toString(intsInHex));
			    _dataTextArea.append("\n\n");
			}
		    } else {
			_dataTextArea.append("null data\n");
		    }
		} catch (EvioException e) {
		    // internal format error
		}
		break;

	    default:
	    }

	}
	tree.repaint();
    }

    /**
     * Get the currently displayed event.
     *
     * @return the currently displayed event.
     */
    public EvioEvent getEvent() {
	return event;
    }

    /**
     * Set the currently displayed event.
     *
     * @param event
     *            the currently displayed event.
     */
    public void setEvent(EvioEvent event) {
	this.event = event;

	if (event != null) {
	    tree.setModel(event.getTreeModel());
	    headerPanel.setHeader(event);

	    if (EventControl.isSourceET()) {
		eventInfoPanel.setEventNumber((int) ETSupport
			.getETEventNumber());
	    } else {
		eventInfoPanel.setEventNumber(event.getEventNumber());
	    }
	    expandAll();

	    // Duplicate the previous selection if any
	    if (structureSelection.size() > 0) {
		// New event's root
		BaseStructure kid, parent = (BaseStructure) event
			.getTreeModel().getRoot();

		// Compare this root event's tag with previous root event's tag
		SelectionInfo info = structureSelection.get(0);
		if (info.tag != parent.getHeader().getTag()) {
		    return;
		}

		Object[] objs = new Object[structureSelection.size()];
		objs[0] = parent;

		for (int i = 1; i < structureSelection.size(); i++) {
		    info = structureSelection.get(i);

		    // skip to info.pos #th kid
		    if (info.pos + 1 > parent.getChildCount()) {
			return;
		    }

		    kid = parent.getChildren().get(info.pos);

		    if (info.tag != kid.getHeader().getTag()) {
			return;
		    }

		    objs[i] = parent = kid;
		}

		tree.setSelectionPath(new TreePath(objs));
	    }
	} else {
	    tree.setModel(null);
	    headerPanel.setHeader(null);
	    eventInfoPanel.setEventNumber(-1);
	}
    }

    /**
     * Expand all nodes.
     */
    public void expandAll() {
	if (tree != null) {
	    for (int i = 0; i < tree.getRowCount(); i++) {
		tree.expandRow(i);
	    }
	}
    }

}