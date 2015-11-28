package cnuphys.ced.clasio.table;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.MenuSelectionManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jlab.coda.jevio.EvioNode;
import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.bCNU.component.filetree.FileDnDHandler;
import cnuphys.bCNU.component.filetree.FileTreePanel;
import cnuphys.bCNU.component.filetree.IFileTreeListener;
import cnuphys.bCNU.event.graphics.EventInfoPanel;
import cnuphys.bCNU.file.IFileHandler;
import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.util.FileUtilities;
import cnuphys.bCNU.util.Fonts;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.ClasIoEventMenu;
import cnuphys.ced.clasio.ClasIoPresentBankPanel;
import cnuphys.ced.clasio.EvioNodeSupport;
import cnuphys.ced.clasio.IClasIoEventListener;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.IAccumulationListener;

public class NodePanel extends JPanel implements ActionListener,
		ListSelectionListener, IClasIoEventListener, IFileTreeListener,
		IFileHandler, IAccumulationListener {

	// file tree width
	private static final int FILE_PANEL_WIDTH = 220;

	// Text area shows data values for selected nodes.
	private JTextArea _dataTextArea;

	// the event info panel
	private EventInfoPanel _eventInfoPanel;

	/** A button for selecting "next" event. */
	protected JButton nextButton;

	/** A button for selecting "previous" event. */
	protected JButton prevButton;

	/** show ints as hex */
	protected JCheckBox intsInHexButton;

	/** Used for "goto" event */
	protected JTextField eventNumberInput;

	// the table
	protected NodeTable _nodeTable;

	// summary table
	protected NodeSummaryPanel _nodeSummaryPanel;

	// the event manager
	ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	// set true when constructor finished
	private boolean _isReady;

	// current selected node
	private EvioNode _currentNode;

	// present banks
	private ClasIoPresentBankPanel _presentPanel;

	// file tree
	private FileTreePanel _filePanel;

	/**
	 * Create a node panel for displaying events
	 */
	public NodePanel() {
		_eventManager.addClasIoEventListener(this, 1);

		new FileDnDHandler(null, this, this);

		setLayout(new BorderLayout());
		addCenter();
		addEast();
		// addWest();

		_isReady = true;
		fixButtons();

		AccumulationManager.getInstance().addAccumulationListener(this);
	}

	// add the east components
	private void addEast() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		// shows which banks are present
		_presentPanel = new ClasIoPresentBankPanel(_nodeTable);

		panel.add(_presentPanel);
		panel.add(Box.createVerticalGlue());

		add(panel, BorderLayout.EAST);
	}

	/**
	 * Create the text area that will display structure data. What is actually
	 * returned is the scroll pane that contains the text area.
	 *
	 * @return the scroll pane holding the text area.
	 */
	private JScrollPane createDataTextArea() {

		_dataTextArea = new JTextArea();
		_dataTextArea.setFont(Fonts.mediumFont);
		// _dataTextArea.setBorder(BorderFactory.createTitledBorder(null,
		// "Data",
		// TitledBorder.LEADING, TitledBorder.TOP, null, Color.blue));
		_dataTextArea.setEditable(false);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().setView(_dataTextArea);
		// Borderlayout respects preferred width in east/west,
		// but ignores height -- so use this to set width only.
		// Don't use "setPreferredSize" on textArea or it messes up the
		// scrolling.
		scrollPane.setPreferredSize(new Dimension(180, 600));

		return scrollPane;
	}

	// add the center components
	private void addCenter() {
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout(0, 0));

		// event info
		_eventInfoPanel = new EventInfoPanel();
		addEventControls();

		JPanel npanel = new JPanel();
		npanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4));
		npanel.add(_eventInfoPanel);
		centerPanel.add(npanel, BorderLayout.NORTH);

		// node summary
		_nodeSummaryPanel = new NodeSummaryPanel();
		centerPanel.add(_nodeSummaryPanel, BorderLayout.SOUTH);

		_nodeTable = new NodeTable();
		_nodeTable.getSelectionModel().addListSelectionListener(this);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				false, createDataTextArea(), _nodeTable.getScrollPane());
		splitPane.setResizeWeight(0.0);
		centerPanel.add(splitPane, BorderLayout.CENTER);

		add(centerPanel, BorderLayout.CENTER);
	}

	// add the west components
	private void addWest() {
		_filePanel = createFileTreePanel();
		add(_filePanel, BorderLayout.WEST);
	}

	/**
	 * Creates the file tree panel.
	 */
	private FileTreePanel createFileTreePanel() {
		// the file tree
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"EVIO Event Files", ClasIoEventMenu.extensions);
		FileTreePanel fileTree = new FileTreePanel(filter);
		Dimension size = fileTree.getPreferredSize();
		size.width = FILE_PANEL_WIDTH;
		fileTree.setPreferredSize(size);

		// fileTree.setMaximumSize(new Dimension(10000, 10000));
		fileTree.addFileTreeListener(this);
		return fileTree;
	}

	/**
	 * Set the model data based on a clasIO EvioDataEvent
	 * 
	 * @param event
	 *            the event
	 */
	public void setData(EvioDataEvent event) {
		_nodeTable.setData(event);
	}

	// list of ignored tags
	/**
	 * Create a panel to change events in viewer.
	 */
	private void addEventControls() {

		JPanel sourcePanel = _eventInfoPanel.getSourcePanel();
		JPanel numPanel = _eventInfoPanel.getNumberPanel();

		nextButton = new JButton("next");
		nextButton.setFont(Fonts.smallFont);
		nextButton.addActionListener(this);

		prevButton = new JButton("prev");
		prevButton.setFont(Fonts.smallFont);
		prevButton.addActionListener(this);

		JLabel label = new JLabel("Go to # ");
		GraphicsUtilities.setSizeSmall(label);

		eventNumberInput = new JTextField(6);

		KeyAdapter ka = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent kev) {
				if (kev.getKeyCode() == KeyEvent.VK_ENTER) {
					MenuSelectionManager.defaultManager().clearSelectedPath();
					try {
						int enumber = Integer.parseInt(eventNumberInput
								.getText());
						_eventManager.gotoEvent(enumber);
					} catch (Exception e) {
						eventNumberInput.setText("");
					}
				}
			}
		};
		eventNumberInput.addKeyListener(ka);

		intsInHexButton = new JCheckBox("Show ints in hex", false);
		GraphicsUtilities.setSizeSmall(intsInHexButton);

		ItemListener il = new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				updateDataArea(_currentNode);
			}

		};
		intsInHexButton.addItemListener(il);

		sourcePanel.add(Box.createHorizontalStrut(4));
		sourcePanel.add(intsInHexButton);

		numPanel.add(Box.createHorizontalStrut(2));
		numPanel.add(prevButton);
		numPanel.add(Box.createHorizontalStrut(2));
		numPanel.add(nextButton);
		numPanel.add(Box.createHorizontalStrut(2));
		numPanel.add(label);
		numPanel.add(eventNumberInput);
	}

	/**
	 * Set the selectability of the buttons
	 */
	public void fixButtons() {
		if (!_isReady) {
			return;
		}
		nextButton.setEnabled(_eventManager.isNextOK());
		prevButton.setEnabled(_eventManager.isPrevOK());
		eventNumberInput.setEnabled(_eventManager.isGotoOK());
		setSummary(_eventManager.getCurrentEvent());
	}

	/**
	 * /** Set the displayed event source value.
	 * 
	 * @param source
	 *            event source.
	 */
	public void setSource(String source) {
		_eventInfoPanel.setSource(source);
	}

	/**
	 * Get the displayed event source value.
	 * 
	 * @return the displayed event source value.
	 */
	public String getSource() {
		return _eventInfoPanel.getSource();
	}

	/**
	 * Set the displayed event number value.
	 * 
	 * @param eventNumber
	 *            event number.
	 */
	public void setEventNumber(int eventNumber) {
		_eventInfoPanel.setEventNumber(eventNumber);
	}

	/**
	 * Get the displayed event number value.
	 * 
	 * @return the displayed event number value.
	 */
	public int getEventNumber() {
		return _eventInfoPanel.getEventNumber();
	}

	/**
	 * Set the displayed number-of-events value.
	 * 
	 * @param numberOfEvents
	 *            number of events.
	 */
	public void setNumberOfEvents(int numberOfEvents) {
		_eventInfoPanel.setNumberOfEvents(numberOfEvents);
	}

	/**
	 * Get the displayed number-of-events value.
	 * 
	 * @return the displayed number-of-events value.
	 */
	public int getNumberOfEvents() {
		return _eventInfoPanel.getNumberOfEvents();
	}

	/**
	 * Set the fields in the panel based on the data in the root node.
	 * 
	 * @param event
	 *            the event whose root node will be used
	 */
	public void setSummary(EvioDataEvent event) {
		_nodeSummaryPanel.setSummary(EvioNodeSupport.getRootNode(event));
	}

	/**
	 * Set the fields in the panel based on the data in the node.
	 * 
	 * @param node
	 *            the node to use
	 */
	public void setSummary(EvioNode node) {
		_nodeSummaryPanel.setSummary(node);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if (source == nextButton) {
			_eventManager.getNextEvent();
		} else if (source == prevButton) {
			_eventManager.getPreviousEvent();
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			return;
		}
		int row = _nodeTable.getSelectedRow();

		_currentNode = _nodeTable.getNode(row);
		updateDataArea(_currentNode);
	}

	/**
	 * Update the data text area
	 *
	 * @param treeSelectionEvent
	 *            the causal event.
	 */
	protected void updateDataArea(EvioNode node) {

		_nodeSummaryPanel.setSummary(node);

		_dataTextArea.setText("");
		int blankLineEveryNth = 5; // put in a blank line after every Nth
		// element listed

		if (node == null) {
			return;
		}

		if (EvioNodeSupport.isLeaf(node)) {

			int lineCounter = 1, index = 1;

			int tag = node.getTag();
			int num = node.getNum();

			switch (node.getDataTypeObj()) {
			case DOUBLE64:
				double doubledata[] = _eventManager.getCurrentEvent()
						.getDouble(tag, num);
				if (doubledata != null) {
					for (double d : doubledata) {
						String doubStr = DoubleFormat.doubleFormat(d, 6, 4);
						String s = String
								.format("[%02d]  %s", index++, doubStr);
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
				float floatdata[] = _eventManager.getCurrentEvent().getFloat(
						tag, num);
				if (floatdata != null) {
					for (float d : floatdata) {
						String doubStr = DoubleFormat.doubleFormat(d, 6, 4);
						String s = String
								.format("[%02d]  %s", index++, doubStr);
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
				_dataTextArea.append("Not Supported");
				break;

			case INT32:
			case UINT32:
				int intdata[] = _eventManager.getCurrentEvent()
						.getInt(tag, num);
				if (intdata != null) {
					for (int i : intdata) {
						String s;
						if (intsInHexButton.isSelected()) {
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
				short shortdata[] = _eventManager.getCurrentEvent().getShort(
						tag, num);
				if (shortdata != null) {
					for (short i : shortdata) {
						String s;
						if (intsInHexButton.isSelected()) {
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
				byte bytedata[] = _eventManager.getCurrentEvent().getByte(tag,
						num);
				if (bytedata != null) {
					for (byte i : bytedata) {

						String s;
						if (intsInHexButton.isSelected()) {
							s = String.format("[%02d]  %#06X", index++, i);
						} else {
							s = String.format("[%02d]  %d", index++, i);
						}

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
				// if ((tag == 5) && (num == 1)) {
				// EvioDataEvent event = _eventManager.getCurrentEvent();
				byte bytes[] = node.getStructureBuffer(true).array();

				if (bytes != null) {
					String ss = new String(bytes);

					if (ss != null) {
						String tokens[] = FileUtilities.tokens(ss, "\0");
						if (tokens != null) {
							for (String tok : tokens) {
								_dataTextArea.append(tok + "\n");
							}
						}
					}
					// }
				} else {
					_dataTextArea.append("Not Supported");
				}
				break;

			case COMPOSITE:
				break;

			default:
			} // switch
		} // isLeaf
	}

	/**
	 * Part of the IClasIoEventListener interface
	 * 
	 * @param event
	 *            the new current event
	 */
	@Override
	public void newClasIoEvent(EvioDataEvent event) {

		if (!_eventManager.isAccumulating()) {
			setData(event);
			setEventNumber(_eventManager.getEventNumber());
			fixButtons();
		}
	}

	/**
	 * Part of the IClasIoEventListener interface
	 * 
	 * @param path
	 *            the new path to the event file
	 */
	@Override
	public void openedNewEventFile(String path) {
		setEventNumber(0);
		_nodeSummaryPanel.setSummary(null);

		// set the text field
		setSource(path);
		setNumberOfEvents(_eventManager.getEventCount());
		fixButtons();
	}

	@Override
	public void fileDoubleClicked(String fullPath) {
		File file = new File(fullPath);
		handleFile(null, file, null);
	}

	@Override
	public void filesDoubleClicked(List<File> files) {
	}

	@Override
	public void handleFile(Object parent, File file, HandleAction action) {
		if (file.exists()) {
			try {
				ClasIoEventManager.getInstance().openEvioFile(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void handleFiles(Object parent, File[] files, HandleAction action) {
	}

	@Override
	public void accumulationEvent(int reason) {
		switch (reason) {
		case AccumulationManager.ACCUMULATION_STARTED:
			break;

		case AccumulationManager.ACCUMULATION_CANCELLED:
			break;

		case AccumulationManager.ACCUMULATION_FINISHED:
			System.err.println("ACCUM FINISHED");
			setData(_eventManager.getCurrentEvent());
			setEventNumber(_eventManager.getEventNumber());
			fixButtons();
			break;
		}
	}

}
