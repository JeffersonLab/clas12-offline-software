package cnuphys.cnf.event;

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
import javax.swing.filechooser.FileFilter;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.component.TransparentPanel;
import cnuphys.bCNU.util.Environment;
import cnuphys.cnf.frame.Def;

@SuppressWarnings("serial")
public class EventMenu extends JMenu implements ActionListener, IEventListener {

	private EventManager _eventManager = EventManager.getInstance();

	// to find recently opened files from the preferences
	private static String _recentFileKey = "RecentEventFiles";

	// the menu items
	private JMenuItem quitItem;
	private JMenuItem nextItem;

	// recently opened menu
	private static JMenu _recentMenu;

	// a hash table of menu items used by recent file feature
	private static Hashtable<String, JMenuItem> _menuItems;

	// for goto
	private JTextField evnum;

	// for auto next event
	private JCheckBox _periodEvent;
	private float _period = 2f; // sec
	private JTextField _periodTF;
	private Timer _nextEventTimer;

	private boolean _isReady;

	/** Last selected data file */
	private static String dataFilePath;

	private static FileFilter _hipoEventFileFilter;

	/**
	 * The event menu
	 * 
	 * @param includeQuit include quite option
	 */
	public EventMenu(boolean includeQuit) {
		super("Events");

		_eventManager.addEventListener(this, 1);

		if (_hipoEventFileFilter == null) {
			_hipoEventFileFilter = new FileFilter() {

				@Override
				public boolean accept(File f) {
					return f.isDirectory() || f.getPath().endsWith(".hipo") || f.getPath().endsWith(".hippo")
							|| f.getPath().contains(".hipo.");
				}

				@Override
				public String getDescription() {
					return "Hipo Event Files";
				}
			};
		}

		// next
		nextItem = addMenuItem("Next Event", KeyEvent.VK_N);

		// goto
		add(createGoToPanel());

		// periodic event
		add(createEventPeriodPanel());

		if (includeQuit) {
			addSeparator();
			quitItem = addMenuItem("Quit", 0);
		}

		_isReady = true;
		fixState();
	}

	/**
	 * Get the menu item to open a HIPO event file
	 * 
	 * @return the menu item to open an event file
	 */
	public static JMenuItem getOpenHipoEventFileItem() {
		final JMenuItem item = new JMenuItem("Open Hipo File...");

		item.addActionListener(event -> openHipoEventFile());
		return item;
	}

	// convenience method to add menu item
	private JMenuItem addMenuItem(String label, int accelKey) {
		JMenuItem item = new JMenuItem(label);
		if (accelKey > 0) {
			item.setAccelerator(KeyStroke.getKeyStroke(accelKey, ActionEvent.CTRL_MASK));
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

		// System.err.println("FIX STATE: nextOK: " + nextOK);

		nextItem.setEnabled(nextOK);
		evnum.setEnabled(_eventManager.isGotoOK());
		_periodEvent.setEnabled(nextOK);
		_periodTF.setEnabled(nextOK);

		if (!nextOK) {
			if (_nextEventTimer != null) {
				_nextEventTimer.stop();
			}
		}
	}

	/**
	 * Set the default directory in which to look for event files.
	 * 
	 * @param defaultDataDir default directory in which to look for event files
	 */
	public static void setDefaultDataDir(String defaultDataDir) {
		dataFilePath = defaultDataDir;
	}

	/**
	 * Select and open an event file.
	 *
	 * @return the opened file reader, or <code>null</code>
	 */
	private static File openHipoEventFile() {

		JFileChooser chooser = new JFileChooser(dataFilePath);
		chooser.setSelectedFile(null);
		chooser.setFileFilter(_hipoEventFileFilter);
		int returnVal = chooser.showOpenDialog(Def.getFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			try {
				dataFilePath = file.getPath();

				if (_hipoEventFileFilter.accept(file)) {
					EventManager.getInstance().openHipoEventFile(file);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * Get the menu from which you can choose a recently opened file
	 * 
	 * @return the menu from which you can choose a recently opened file
	 */
	public static JMenu getRecentEventFileMenu() {
		if (_recentMenu != null) {
			return _recentMenu;
		}
		_recentMenu = new JMenu("Recent Event Files");

		// get the recent files from the prefs
		Vector<String> recentFiles = Environment.getInstance().getPreferenceList(_recentFileKey);

		if (recentFiles != null) {
			for (String fn : recentFiles) {

				// make sure the file still exists
				File file = new File(fn);
				if (file.exists()) {
					addMenu(fn, false);
				}
			}
		}

		return _recentMenu;
	}

	// use to open recent files
	private static void addMenu(String path, boolean atTop) {

		// if it is in the hash, remove from has and from menu
		if (_menuItems != null) {
			JMenuItem item = _menuItems.remove(path);
			if (item != null) {
				_recentMenu.remove(item);
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
							EventManager.getInstance().openHipoEventFile(file);
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
			_recentMenu.add(item, 0);
		} else {
			_recentMenu.add(item);
		}

	}

	/**
	 * Update the recent files for the recent file menu.
	 * 
	 * @param path the path to the file
	 */
	private void updateRecentFiles(String path) {
		if (path == null) {
			return;
		}
		Vector<String> recentFiles = Environment.getInstance().getPreferenceList(_recentFileKey);
		if (recentFiles == null) {
			recentFiles = new Vector<String>(10);
		}

		// keep no more than 10
		recentFiles.remove(path);
		recentFiles.add(0, path);
		if (recentFiles.size() > 9) {
			recentFiles.removeElementAt(9);
		}
		Environment.getInstance().savePreferenceList(_recentFileKey, recentFiles);

		// add to menu
		addMenu(path, true);
	}

	// create the goto event widget
	private JPanel createGoToPanel() {
		JPanel sp = new TransparentPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));

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
		JPanel sp = new TransparentPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));

		_periodEvent = new JCheckBox("Auto Next-Event Every ");

		_periodTF = new JTextField("" + _period, 4);

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (_periodEvent.isSelected()) {
					if (_nextEventTimer == null) {

						float period = Float.parseFloat(_periodTF.getText());
						_period = Math.max(0.001f, Math.min(60f, period));

						int delay = (int) (1000 * _period);
						_nextEventTimer = new Timer(delay, event->_eventManager.getNextEvent());

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
						float period = Float.parseFloat(_periodTF.getText());
						_period = Math.max(0.001f, Math.min(60f, period));

						if (_nextEventTimer != null) {
							int delay = (int) (1000 * _period);
//							System.err.println("Set next event delay to " + delay + " ms");
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
		} else if (source == quitItem) {
			System.exit(0);
		}
	}

	/**
	 * Part of the IEventListener interface
	 * 
	 * @param event       the new current event
	 * @param isStreaming <code>true</code> if this is during file streaming
	 */
	@Override
	public void newEvent(DataEvent event, boolean isStreaming) {
		if (!isStreaming) {
			fixState();
		}
	}

	/**
	 * Part of the IEventListener interface
	 * 
	 * @param file the new file
	 */
	@Override
	public void openedNewEventFile(File file) {

		String path = file.getAbsolutePath();

		// remember which file was chosen
		setDefaultDataDir(path);
		updateRecentFiles(path);
		fixState();
	}

	/**
	 * Rewound the current file
	 * 
	 * @param file the file
	 */
	@Override
	public void rewoundFile(File file) {
	}

	/**
	 * Streaming start message
	 * 
	 * @param file        file being streamed
	 * @param numToStream number that will be streamed
	 */
	@Override
	public void streamingStarted(File file, int numToStream) {
	}

	/**
	 * Streaming ended message
	 * 
	 * @param file the file that was streamed
	 * @param int  the reason the streaming ended
	 */
	@Override
	public void streamingEnded(File file, int reason) {
	}

}