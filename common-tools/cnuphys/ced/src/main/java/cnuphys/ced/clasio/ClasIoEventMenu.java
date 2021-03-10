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
import javax.swing.filechooser.FileFilter;
import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.component.TransparentPanel;
import cnuphys.bCNU.util.Environment;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.frame.Ced;
import cnuphys.splot.plot.ImageManager;

public class ClasIoEventMenu extends JMenu implements ActionListener, IClasIoEventListener {

	private ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	// to find recently opened files from the preferences
	private static String _recentFileKey = "RecentEventFiles";

	// the menu items
	private JMenuItem quitItem;
	private JMenuItem nextItem;
	private JMenuItem prevItem;
	private JMenuItem accumulationItem;

	// recently opened menu
	private static JMenu _recentMenu;

	// a hash table of menu items used by recent file feature
	private static Hashtable<String, JMenuItem> _menuItems;

	// for goto sequential
	private JTextField seqEvNum;
	
	// for goto true event from RUN::config
	private JTextField trueEvNum;


	// for auto next event
	private JCheckBox _periodEvent;
	private float _period = 2f; // sec
	private JTextField _periodTF;
	private Timer _nextEventTimer;

	private boolean _isReady;

	/** Last selected data file */
	private static String dataFilePath;

//	public static String evioExtensions[] = { "evio", "ev", "evio.*"};

//	private static FileNameExtensionFilter _evioEventFileFilter = new FileNameExtensionFilter(
//			"Event Files", evioExtensions);

	private static FileFilter _hipoEventFileFilter;

	private static EvioFileFilter _evioEventFileFilter = new EvioFileFilter();

	// for both evio and hipo
	private static FileFilter _compositeFilter;

	/**
	 * The event menu used for the clasio package
	 * 
	 * @param includeAccumulation include accumulation option
	 * @param includeQuit         include quite option
	 */
	public ClasIoEventMenu(boolean includeAccumulation, boolean includeQuit) {
		super("Events");

		_eventManager.addClasIoEventListener(this, 1);

		if (_hipoEventFileFilter == null) {
			_hipoEventFileFilter = new FileFilter() {

				@Override
				public boolean accept(File f) {
					return f.getPath().endsWith(".hipo") || f.getPath().endsWith(".hippo")
							|| f.getPath().contains(".hipo.");
				}

				@Override
				public String getDescription() {
					return "Hipo Event Files";
				}
			};
		}

		// allows evio or hipo selction
		if (_compositeFilter == null) {
			_compositeFilter = new FileFilter() {

				@Override
				public boolean accept(File f) {
					return _hipoEventFileFilter.accept(f) || _evioEventFileFilter.accept(f);
				}

				@Override
				public String getDescription() {
					return "Hipo and Evio Event Files";
				}

			};
		}

		// accumulate
		if (includeAccumulation) {
			accumulationItem = addMenuItem("Accumulate Events...", KeyEvent.VK_A);
			addSeparator();
		}

		// next
		nextItem = addMenuItem("Next Event", KeyEvent.VK_N);

		// previous
		prevItem = addMenuItem("Previous Event", KeyEvent.VK_P);

		// goto sequential
		add(createGotoSequentialPanel());
		
		// goto true
		add(createGotoTruePanel());


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
	public static JMenuItem getOpenEventFileItem() {
		final JMenuItem item = new JMenuItem("Open Hipo or Evio File...");

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				openEventFile();
			}

		};
		item.addActionListener(al);
		return item;
	}

//	/** 
//	 * Get the menu item to open a HIPO event file
//	 * @return the menu item to open an event file
//	 */
//	public static JMenuItem getOpenHipoEventFileItem() {
//		final JMenuItem item = new JMenuItem("Open Hipo File...");
//
//		ActionListener al = new ActionListener() {
//
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				openHipoEventFile();
//			}
//
//		};
//		item.addActionListener(al);
//		return item;
//	}
//	
//	/** 
//	 * Get the menu item to open an evio event file
//	 * @return the menu item to open an event file
//	 */
//	public static JMenuItem getOpenEvioEventFileItem() {
//		final JMenuItem item = new JMenuItem("Open Evio File...");
//
//		ActionListener al = new ActionListener() {
//
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				openEvioEventFile();
//			}
//
//		};
//		item.addActionListener(al);
//		return item;
//	}

	/**
	 * Get the menu item to connect to any DAQ ring
	 * 
	 * @return the menu item to open any DAQ ring
	 */
//	public static JMenuItem getConnectAnyRingItem() {
//		final JMenuItem item = new JMenuItem("Connect to Hipo Ring...");
//		item.setIcon(ImageManager.getInstance().loadImageIcon("images/hipo2.png"));
//
//		ActionListener al = new ActionListener() {
//
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				ClasIoEventManager.getInstance().ConnectToHipoRing();
//			}
//
//		};
//		item.addActionListener(al);
//		return item;
//	}

	public static JMenuItem getConnectETItem() {
		final JMenuItem item = new JMenuItem("Connect to ET Ring...");
		item.setIcon(ImageManager.getInstance().loadImageIcon("images/et.png"));

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ClasIoEventManager.getInstance().ConnectToETRing();
			}

		};
		item.addActionListener(al);
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
		prevItem.setEnabled(_eventManager.isPrevOK());
		seqEvNum.setEnabled(_eventManager.isGotoOK());
		trueEvNum.setEnabled(_eventManager.isGotoOK() && (_eventManager.getTrueEventNumber() > -1));
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
	private static File openEventFile() {

		JFileChooser chooser = new JFileChooser(dataFilePath);
		chooser.setSelectedFile(null);
		chooser.setFileFilter(_compositeFilter);
		int returnVal = chooser.showOpenDialog(Ced.getFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			try {
				dataFilePath = file.getPath();

				if (_hipoEventFileFilter.accept(file)) {
					ClasIoEventManager.getInstance().openHipoEventFile(file);
				} else if (_evioEventFileFilter.accept(file)) {
					ClasIoEventManager.getInstance().openEvioEventFile(file);
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
							if (_hipoEventFileFilter.accept(file)) {
								ClasIoEventManager.getInstance().openHipoEventFile(file);
							} else if (_evioEventFileFilter.accept(file)) {
								ClasIoEventManager.getInstance().openEvioEventFile(file);
							}
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
	
	// create the goto sequential event widget
	private JPanel createGotoSequentialPanel() {
		JPanel sp = new TransparentPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));

		JLabel label = new JLabel("Goto Sequential Event: ");

		seqEvNum = new JTextField("1", 10);

		KeyAdapter ka = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent kev) {
				if (kev.getKeyCode() == KeyEvent.VK_ENTER) {
					MenuSelectionManager.defaultManager().clearSelectedPath();
					try {
						int enumber = Integer.parseInt(seqEvNum.getText());
						_eventManager.gotoEvent(enumber);
					} catch (Exception e) {

					}
				}
			}
		};
		seqEvNum.addKeyListener(ka);

		sp.add(label);
		sp.add(seqEvNum);
		seqEvNum.setEnabled(false);
		return sp;
	}

	// create the goto true event widget
	private JPanel createGotoTruePanel() {
		JPanel sp = new TransparentPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));

		JLabel label = new JLabel("Goto True Event: ");

		trueEvNum = new JTextField("1", 10);

		KeyAdapter ka = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent kev) {
				if (kev.getKeyCode() == KeyEvent.VK_ENTER) {
					MenuSelectionManager.defaultManager().clearSelectedPath();
					int trueNum = _eventManager.getTrueEventNumber();
					if (trueNum > -1) {
						try {
							int enumber = Integer.parseInt(trueEvNum.getText());
							if (enumber != trueNum) {
								_eventManager.gotoTrueEvent(enumber);
							}
						} catch (Exception e) {
						}
					}
				}
			}
		};
		trueEvNum.addKeyListener(ka);

		sp.add(label);
		sp.add(trueEvNum);
		trueEvNum.setEnabled(false);
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

						ActionListener nextAl = new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								// System.err.println("NEXT EVENT period = " + _period);
								_eventManager.getNextEvent();
							}

						};

						int delay = (int) (1000 * _period);
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
		} else if (source == prevItem) {
			_eventManager.getPreviousEvent();
		} else if (source == accumulationItem) {
			ClasIoAccumulationDialog dialog = new ClasIoAccumulationDialog(AccumulationManager.getInstance());
			dialog.setVisible(true);
		} else if (source == quitItem) {
			System.exit(0);
		}
	}

	/**
	 * Part of the IClasIoEventListener interface
	 * 
	 * @param event the new current event
	 */
	@Override
	public void newClasIoEvent(DataEvent event) {
		fixState();
	}

	/**
	 * Part of the IClasIoEventListener interface
	 * 
	 * @param path the new path to the event file
	 */
	@Override
	public void openedNewEventFile(String path) {

		// remember which file was chosen
		setDefaultDataDir(path);
		updateRecentFiles(path);
		fixState();
	}

	/**
	 * Change the event source type
	 * 
	 * @param source the new source: File, ET
	 */
	@Override
	public void changedEventSource(ClasIoEventManager.EventSourceType source) {
		fixState();
	}

	/**
	 * Tests whether this listener is interested in events while accumulating
	 * 
	 * @return <code>true</code> if this listener is NOT interested in events while
	 *         accumulating
	 */
	@Override
	public boolean ignoreIfAccumulating() {
		return true;
	}

}
