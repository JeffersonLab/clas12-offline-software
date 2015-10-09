package cnuphys.bCNU.event.graphics;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.EventListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jlab.coda.jevio.EventParser;
import org.jlab.coda.jevio.EvioEvent;
import org.jlab.coda.jevio.EvioException;
import org.jlab.coda.jevio.EvioReader;
import org.jlab.coda.jevio.IEvioListener;

public class EventTreeMenu {

	// ----------------------
	// gui stuff
	// ----------------------

	/** A button for selecting "next" event. */
	protected JButton nextButton;

	/** A button for selecting "previous" event. */
	protected JButton prevButton;

	/** Menu item for opening event file. */
	protected JMenuItem openEventFile;

	/** Menu item setting the number of the event (from a file) to be displayed. */
	protected JTextField eventNumberInput;

	/** The panel that holds the tree and all associated widgets. */
	protected EventTreePanel eventTreePanel;

	/** Number of event currently being displayed. */
	protected int eventIndex;

	/** Last selected data file. */
	protected String dataFilePath;

	/** Filter so only files with specified extensions are seen in file viewer. */
	protected FileNameExtensionFilter evioFileFilter;

	/** The reader object for the currently viewed evio file. */
	protected EvioReader evioFileReader;

	// ----------------------------
	// General function
	// ----------------------------
	/**
	 * Listener list for structures (banks, segments, tagsegments) encountered
	 * while processing an event.
	 */
	private EventListenerList evioListenerList;

	/**
	 * Constructor. Holds the menus for a frame or internal frame that wants to
	 * manage a tree panel.
	 * 
	 * @param eventTreePanel
	 *            holds the tree and all associated the widgets.
	 */
	public EventTreeMenu(final EventTreePanel eventTreePanel) {
		this.eventTreePanel = eventTreePanel;
	}

	/**
	 * Get the main event display panel.
	 * 
	 * @return main event display panel.
	 */
	public EventTreePanel getEventTreePanel() {
		return eventTreePanel;
	}

	/**
	 * Create a panel to change events in viewer.
	 */
	void addEventControlPanel() {

		nextButton = new JButton("next >");

		// next event menu item
		ActionListener al_next = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// If we're looking at a file, there are multiple events
				// contained in it
				if (evioFileReader != null) {
					try {
						EvioEvent event = evioFileReader
								.parseEvent(++eventIndex);
						if (event != null) {
							eventTreePanel.setEvent(event);
						}
						if (eventIndex > 1)
							prevButton.setEnabled(true);
					} catch (IOException e1) {
						eventIndex--;
						e1.printStackTrace();
					} catch (EvioException e1) {
						eventIndex--;
						e1.printStackTrace();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}

			}
		};
		nextButton.addActionListener(al_next);

		prevButton = new JButton("< prev");
		prevButton.setEnabled(false);
		// previous event menu item
		ActionListener al_prev = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// If we're looking at a file, there are multiple events
				// contained in it
				if (evioFileReader != null) {
					try {
						if (eventIndex > 1) {
							EvioEvent event = evioFileReader
									.parseEvent(--eventIndex);
							if (event != null) {
								eventTreePanel.setEvent(event);
							}
							if (eventIndex < 2) {
								prevButton.setEnabled(false);
							}
						}
					} catch (IOException e1) {
						eventIndex++;
						e1.printStackTrace();
					} catch (EvioException e1) {
						eventIndex++;
						e1.printStackTrace();
					}
				}

			}
		};
		prevButton.addActionListener(al_prev);

		// goto event menu item
		ActionListener al_eventNumIn = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					String num = eventNumberInput.getText();
					if (num != null) {
						int eventNum = Integer.parseInt(num);

						// Go to the specified event number
						if ((eventNum > 0)
								&& (eventNum <= evioFileReader.getEventCount())) {
							eventIndex = eventNum;
							EvioEvent event = evioFileReader
									.gotoEventNumber(eventIndex);
							if (event != null) {
								eventTreePanel.setEvent(event);
							}
						} else {
							eventNumberInput.setText("");
						}
					}
				} catch (NumberFormatException e1) {
					// bad entry in widget
					eventNumberInput.setText("");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		};

		JLabel label = new JLabel("Go to event # ");
		eventNumberInput = new JTextField();
		eventNumberInput.addActionListener(al_eventNumIn);

		JPanel cPanel = eventTreePanel.getEventInfoPanel().controlPanel;
		cPanel.setBorder(new EmptyBorder(5, 8, 5, 8));
		cPanel.setLayout(new GridLayout(2, 2, 5, 5)); // rows, cols, hgap, vgap
		cPanel.add(prevButton);
		cPanel.add(nextButton);
		cPanel.add(label);
		cPanel.add(eventNumberInput);
	}

	/**
	 * Create the view menu.
	 *
	 * @return the view menu.
	 */
	public JMenu createViewMenu() {
		final JMenu menu = new JMenu(" View ");

		// ints-viewed-as-hex menu item
		ActionListener al_hex = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JMenuItem item = (JMenuItem) e.getSource();
				String txt = item.getText();
				if (txt.equals("Hexidecimal")) {
					eventTreePanel.setIntsInHex(true);
					item.setText("Decimal");
				} else {
					eventTreePanel.setIntsInHex(false);
					item.setText("Hexidecimal");
				}
				eventTreePanel.refreshDisplay();
			}
		};

		JMenuItem hexItem = new JMenuItem("Hexidecimal");
		hexItem.addActionListener(al_hex);
		hexItem.setEnabled(true);
		menu.add(hexItem);

		return menu;
	}

	/**
	 * Create the file menu.
	 *
	 * @return the file menu.
	 */
	public JMenu createFileMenu() {
		JMenu menu = new JMenu(" File ");

		// open event file menu item
		ActionListener al_oef = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doOpenEventFile();
			}
		};
		openEventFile = new JMenuItem("Open Event File...");
		openEventFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				ActionEvent.CTRL_MASK));
		openEventFile.addActionListener(al_oef);
		menu.add(openEventFile);

		// Quit menu item
		ActionListener al_exit = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		};
		JMenuItem exit_item = new JMenuItem("Quit");
		exit_item.addActionListener(al_exit);
		menu.add(exit_item);

		return menu;
	}

	/**
	 * Select and open an event file.
	 */
	private void doOpenEventFile() {
		EvioReader eFile = evioFileReader;
		EvioReader evioFile = openEventFile();
		// handle cancel button properly
		if (eFile == evioFile) {
			return;
		}
		nextButton.setEnabled(evioFile != null);
		// prevButton.setEnabled(evioFile != null);
		prevButton.setEnabled(false);
		eventTreePanel.setEvent(null);
		// automatically go to the first event
		nextButton.doClick();
	}

	/**
	 * Convenience method to open a file programmatically.
	 * 
	 * @param file
	 *            the file to open
	 */
	public void manualOpenEventFile(File file) {
		EvioReader eFile = evioFileReader;
		EvioReader evioFile = openEventFile(file);
		// handle cancel button properly
		if (eFile == evioFile) {
			return;
		}
		nextButton.setEnabled(evioFile != null);
		// prevButton.setEnabled(evioFile != null);
		prevButton.setEnabled(false);
		eventTreePanel.setEvent(null);
	}

	/**
	 * Add a file extension for viewing evio format files in file chooser.
	 * 
	 * @param extension
	 *            file extension to add
	 */
	public void addEventFileExtension(String extension) {
		if (evioFileFilter == null) {
			evioFileFilter = new FileNameExtensionFilter("EVIO Event Files",
					"ev", "evt", "evio", extension);
		} else {
			String[] exts = evioFileFilter.getExtensions();
			String[] newExts = Arrays.copyOf(exts, exts.length + 1);
			newExts[exts.length] = extension;
			evioFileFilter = new FileNameExtensionFilter("EVIO Event Files",
					newExts);
		}
	}

	/**
	 * Set all file extensions for viewing evio format files in file chooser.
	 * 
	 * @param extensions
	 *            all file extensions
	 */
	public void setEventFileExtensions(String[] extensions) {
		evioFileFilter = new FileNameExtensionFilter("EVIO Event Files",
				extensions);
	}

	/**
	 * Select and open an event file.
	 *
	 * @return the opened file reader, or <code>null</code>
	 */
	public EvioReader openEventFile() {
		eventTreePanel.getEventInfoPanel().setEventNumber(0);

		JFileChooser chooser = new JFileChooser(dataFilePath);
		chooser.setSelectedFile(null);
		if (evioFileFilter == null) {
			evioFileFilter = new FileNameExtensionFilter("EVIO Event Files",
					"ev", "evt", "evio");
		}
		chooser.setFileFilter(evioFileFilter);
		int returnVal = chooser.showOpenDialog(eventTreePanel);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			eventTreePanel.getHeaderPanel().setHeader(null);

			// remember which file was chosen
			File selectedFile = chooser.getSelectedFile();
			dataFilePath = selectedFile.getAbsolutePath();

			// set the text field
			eventTreePanel.getEventInfoPanel().setSource(dataFilePath);

			try {
				if (evioFileReader != null) {
					evioFileReader.close();
					eventTreePanel.getEventInfoPanel().setNumberOfEvents(0);
				}

				evioFileReader = new EvioReader(selectedFile);
				eventTreePanel.getEventInfoPanel().setNumberOfEvents(
						evioFileReader.getEventCount());
				eventIndex = 0;
			} catch (EvioException e) {
				evioFileReader = null;
				e.printStackTrace();
			} catch (IOException e) {
				evioFileReader = null;
				e.printStackTrace();
			}
		}
		connectEvioListeners(); // Connect Listeners to the parser.

		return evioFileReader;
	}

	/**
	 * Open an event file using a given file.
	 *
	 * @param file
	 *            the file to use, i.e., an event file
	 * @return the opened file reader, or <code>null</code>
	 */
	public EvioReader openEventFile(File file) {
		eventTreePanel.getEventInfoPanel().setEventNumber(0);

		eventTreePanel.getHeaderPanel().setHeader(null);

		// remember which file was chosen
		dataFilePath = file.getAbsolutePath();

		// set the text field
		eventTreePanel.getEventInfoPanel().setSource(dataFilePath);

		try {
			if (evioFileReader != null) {
				evioFileReader.close();
				eventTreePanel.getEventInfoPanel().setNumberOfEvents(0);
			}

			evioFileReader = new EvioReader(file);
			eventTreePanel.getEventInfoPanel().setNumberOfEvents(
					evioFileReader.getEventCount());
			eventIndex = 0;
		} catch (EvioException e) {
			evioFileReader = null;
			e.printStackTrace();
		} catch (IOException e) {
			evioFileReader = null;
			e.printStackTrace();
		}
		connectEvioListeners(); // Connect Listeners to the parser.
		return evioFileReader;
	}

	/**
	 * Get the EvioReader object so the file/buffer can be read.
	 * 
	 * @return EvioReader object
	 */
	public EvioReader getEvioFileReader() {
		return evioFileReader;
	}

	/**
	 * Set the default directory in which to look for event files.
	 * 
	 * @param defaultDataDir
	 *            default directory in which to look for event files
	 */
	public void setDefaultDataDir(String defaultDataDir) {
		dataFilePath = defaultDataDir;
	}

	/**
	 * Add an Evio listener. Evio listeners listen for structures encountered
	 * when an event is being parsed. The listeners are passed to the
	 * EventParser once a file is opened.
	 * 
	 * @param listener
	 *            The Evio listener to add.
	 */
	public void addEvioListener(IEvioListener listener) {

		if (listener == null) {
			return;
		}

		if (evioListenerList == null) {
			evioListenerList = new EventListenerList();
		}

		evioListenerList.add(IEvioListener.class, listener);
	}

	/**
	 * Connect the listeners in the evioListenerList to the EventParser
	 */
	private void connectEvioListeners() {

		if (evioListenerList == null) {
			return;
		}

		EventParser parser = getEvioFileReader().getParser();

		EventListener listeners[] = evioListenerList
				.getListeners(IEvioListener.class);

		for (int i = 0; i < listeners.length; i++) {
			parser.addEvioListener((IEvioListener) listeners[i]);
		}
	}

}