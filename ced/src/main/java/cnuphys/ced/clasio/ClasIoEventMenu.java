package cnuphys.ced.clasio;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.MenuSelectionManager;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.bCNU.util.Environment;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.frame.Ced;

public class ClasIoEventMenu extends JMenu implements ActionListener,
		IClasIoEventListener {

	private ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	// to find recently opened files from the preferences
	private static String _recentFileKey = "RecentEvioFiles";

	// the menu items
	private JMenuItem openEventFile;
	private JMenuItem quitItem;
	private JMenuItem nextItem;
	private JMenuItem prevItem;
	private JMenuItem accumulationItem;

	// recently opened menu
	private JMenu recentMenu;

	// a hash table of menu items used by recent file feature
	private static Hashtable<String, JMenuItem> _menuItems;

	// for goto
	private JTextField evnum;

	// for auto next event
	private JCheckBox _periodEvent;
	private int _period = 2; // sec
	private JTextField _periodTF;
	private Timer _nextEventTimer;

	private boolean _isReady;

	/** Last selected data file */
	private static String dataFilePath;

	public static String extensions[] = { "ev", "ev0", "ev1", "ev2", "ev3",
			"ev4", "ev5", "ev6", "ev7", "ev8", "ev9", "evio" };

	private static FileNameExtensionFilter _evioFileFilter = new FileNameExtensionFilter(
			"EVIO Event Files", extensions);

	/**
	 * The event menu used for the clasio package
	 * 
	 * @param includeAccumulation
	 *            include accumulation option
	 * @param includeQuit
	 *            include quite option
	 */
	public ClasIoEventMenu(boolean includeAccumulation, boolean includeQuit) {
		super("Events");

		_eventManager.addClasIoEventListener(this, 1);

		// open
		openEventFile = getOpenEventFileItem();
		openEventFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				ActionEvent.CTRL_MASK));
		add(openEventFile);

		// recent
		add(getRecentEvioFileMenu());
		addSeparator();

		// next
		nextItem = addMenuItem("Next Event", KeyEvent.VK_N);

		// previous
		prevItem = addMenuItem("Previous Event", KeyEvent.VK_P);

		// goto
		add(createGoToPanel());

		// periodic event
		add(createEventPeriodPanel());

		// accumulate
		if (includeAccumulation) {
			accumulationItem = addMenuItem("Accumulate Events...",
					KeyEvent.VK_A);
		}

		if (includeQuit) {
			addSeparator();
			quitItem = addMenuItem("Quit", 0);
		}

		_isReady = true;
		fixState();
	}

	public static JMenuItem getOpenEventFileItem() {
		final JMenuItem item = new JMenuItem("Open Event File...");

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				openEventFile();
			}

		};
		item.addActionListener(al);
		return item;
	}

	// convenience method to add menu item
	private JMenuItem addMenuItem(String label, int accelKey) {
		JMenuItem item = new JMenuItem(label);
		if (accelKey > 0) {
			item.setAccelerator(KeyStroke.getKeyStroke(accelKey,
					ActionEvent.CTRL_MASK));
		}

		item.addActionListener(this);
		add(item);
		return item;
	}

	/**
	 * Fix the state of the menu items
	 */
	public void fixState() {
		if (!_isReady) {
			return;
		}

		boolean nextOK = _eventManager.isNextOK();

		nextItem.setEnabled(nextOK);
		prevItem.setEnabled(_eventManager.isPrevOK());
		evnum.setEnabled(_eventManager.isGotoOK());
		_periodEvent.setEnabled(nextOK);
		_periodTF.setEnabled(nextOK);

		if (!nextOK) {
			if (_nextEventTimer != null) {
				_nextEventTimer.stop();
			}
		}

		if (accumulationItem != null) {
			accumulationItem.setEnabled(_eventManager.isNextOK());
		}
	}

	/**
	 * Set the default directory in which to look for event files.
	 * 
	 * @param defaultDataDir
	 *            default directory in which to look for event files
	 */
	public static void setDefaultDataDir(String defaultDataDir) {
		dataFilePath = defaultDataDir;
	}

	/**
	 * Select and open an event file.
	 *
	 * @return the opened file reader, or <code>null</code>
	 */
	public static File openEventFile() {

		ClasIoEventManager eventManager = ClasIoEventManager.getInstance();
		File selectedFile = eventManager.getCurrentEventFile();

		JFileChooser chooser = new JFileChooser(dataFilePath);
		chooser.setSelectedFile(null);
		chooser.setFileFilter(_evioFileFilter);
		int returnVal = chooser.showOpenDialog(Ced.getFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			selectedFile = chooser.getSelectedFile();
			try {
				eventManager.openEvioFile(selectedFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return selectedFile;
	}

	/**
	 * Get the menu from which you can choose a recently opened file
	 * 
	 * @return the menu from which you can choose a recently opened file
	 */
	private JMenu getRecentEvioFileMenu() {
		recentMenu = new JMenu("Recent Event Files");

		// get the recent files from the prefs
		Vector<String> recentFiles = Environment.getInstance()
				.getPreferenceList(_recentFileKey);

		if (recentFiles != null) {
			for (String fn : recentFiles) {
				addMenu(fn, false);
			}
		}

		return recentMenu;
	}

	private void addMenu(String path, boolean atTop) {

		// if it is in the hash, remove from has and from menu
		if (_menuItems != null) {
			JMenuItem item = _menuItems.remove(path);
			if (item != null) {
				recentMenu.remove(item);
			}
		} else {
			_menuItems = new Hashtable<String, JMenuItem>(41);
		}

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					String fn = ae.getActionCommand();
					File file = new File(fn);
					if (file.exists()) {
						try {
							_eventManager.openEvioFile(file);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		};
		JMenuItem item = new JMenuItem(path);
		item.addActionListener(al);
		_menuItems.put(path, item);
		if (atTop) {
			recentMenu.add(item, 0);
		} else {
			recentMenu.add(item);
		}

	}

	/**
	 * Update the recent files for the recent file menu.
	 * 
	 * @param path
	 *            the path to the file
	 */
	private void updateRecentFiles(String path) {
		if (path == null) {
			return;
		}
		Vector<String> recentFiles = Environment.getInstance()
				.getPreferenceList(_recentFileKey);
		if (recentFiles == null) {
			recentFiles = new Vector<String>(10);
		}

		// keep no more than 10
		recentFiles.remove(path);
		recentFiles.add(0, path);
		if (recentFiles.size() > 9) {
			recentFiles.removeElementAt(9);
		}
		Environment.getInstance().savePreferenceList(_recentFileKey,
				recentFiles);

		// add to menu
		addMenu(path, true);
	}

	// create the goto event widget
	private JPanel createGoToPanel() {
		JPanel sp = new JPanel();
		sp.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));

		JLabel label = new JLabel("Go To Event: ");

		evnum = new JTextField("1", 10);

		KeyAdapter ka = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent kev) {
				if (kev.getKeyCode() == KeyEvent.VK_ENTER) {
					MenuSelectionManager.defaultManager().clearSelectedPath();
					try {
						int enumber = Integer.parseInt(evnum.getText());
						_eventManager.gotoEvent(enumber);
					} catch (Exception e) {

					}
				}
			}
		};
		evnum.addKeyListener(ka);

		sp.add(label);
		sp.add(evnum);
		evnum.setEnabled(false);
		return sp;
	}

	// create the event every so many seconds widget
	private JPanel createEventPeriodPanel() {
		JPanel sp = new JPanel();
		sp.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));

		_periodEvent = new JCheckBox("Auto Next Event Every ");

		_periodTF = new JTextField("" + _period, 4);

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (_periodEvent.isSelected()) {
					if (_nextEventTimer == null) {

						ActionListener nextAl = new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								_eventManager.getNextEvent();
							}

						};

						int delay = 1000 * _period;
						_nextEventTimer = new Timer(delay, nextAl);

					}
					if (!_nextEventTimer.isRunning()) {
						_nextEventTimer.restart();
					}
				} else {
					if (_nextEventTimer != null) {
						_nextEventTimer.stop();
					}
				}
			}

		};
		_periodEvent.addActionListener(al);

		KeyAdapter ka = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent kev) {
				if (kev.getKeyCode() == KeyEvent.VK_ENTER) {
					MenuSelectionManager.defaultManager().clearSelectedPath();
					try {
						int period = Integer.parseInt(_periodTF.getText());
						_period = Math.max(1, Math.min(60, period));

						if (_nextEventTimer != null) {
							int delay = 1000 * _period;
							_nextEventTimer.setDelay(delay);
						}
					} catch (Exception e) {
					}
					_periodTF.setText("" + _period);
				}
			}
		};
		_periodTF.addKeyListener(ka);

		sp.add(_periodEvent);
		sp.add(_periodTF);
		sp.add(new JLabel("sec"));
		_periodEvent.setEnabled(false);
		_periodTF.setEnabled(false);
		return sp;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if (source == nextItem) {
			_eventManager.getNextEvent();
		} else if (source == prevItem) {
			_eventManager.getPreviousEvent();
		} else if (source == accumulationItem) {
			ClasIoAccumulationDialog dialog = new ClasIoAccumulationDialog(
					AccumulationManager.getInstance());
			dialog.setVisible(true);
		} else if (source == quitItem) {
			System.exit(0);
		}
	}

	/**
	 * Part of the IClasIoEventListener interface
	 * 
	 * @param event
	 *            the new current event
	 */
	@Override
	public void newClasIoEvent(EvioDataEvent event) {
		fixState();
	}

	/**
	 * Part of the IClasIoEventListener interface
	 * 
	 * @param path
	 *            the new path to the event file
	 */
	@Override
	public void openedNewEventFile(String path) {

		// remember which file was chosen
		setDefaultDataDir(path);
		updateRecentFiles(path);
		fixState();
	}
}
