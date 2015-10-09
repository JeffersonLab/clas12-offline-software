package cnuphys.bCNU.et;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.jlab.coda.et.EtConstants;
import org.jlab.coda.jevio.EvioEvent;

import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.bCNU.event.EventMenu;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.shell.IProcessListener;
import cnuphys.bCNU.shell.ProcessRecord;
import cnuphys.bCNU.shell.Shell;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.FileUtilities;
import cnuphys.bCNU.view.MiniShellView;

public class ETSupport {

	private static JMenu _etMenu;

	protected static boolean startInSeparateJVM = false;

	// for launching in separate vm
	private static final int _PROCTYPE = 7729;
	private static ProcessRecord _procRecord;

	// the ET men menu items
	private static JMenuItem _connectItem;
	private static JMenuItem _startLocalItem;
	private static JMenuItem _disconnectItem;
	private static JMenuItem _killLocalItem;

	private static long _runningEventNum;

	// default name for et (a tmp file)
	public static String _defaultName;

	// local host ip address
	public static String _localHost = Environment.getInstance()
			.getHostAddress();

	static {
		if (Environment.getInstance().isWindows()) {
			_defaultName = Environment.getInstance().getTempDirectory()
					+ "et_sys_0000";
		} else {
			_defaultName = "/tmp/et_sys_0000";
		}
	}

	// the CED consumer from which we can get a next event
	private static EvioETConsumer _evioConsumer;

	// the files used for the test producer that loads events
	// from a evio file and puts them into et
	private static File _producerEvioFile;

	// the file producer if we are using the local (mostly for test) ET
	private static EvioFileProducer _fileProducer;

	/**
	 * Get the ET men
	 * 
	 * @return the ET menu
	 */
	public static JMenu getETMenu() {
		if (_etMenu != null) {
			return _etMenu;
		}
		ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleMenu(e);
			}
		};

		_etMenu = new JMenu(" ET ");
		_connectItem = new JMenuItem("Connect to ET...");
		_disconnectItem = new JMenuItem("Disconnect from ET...");
		_startLocalItem = new JMenuItem(
				"Start Local ET With evio File Producer...");
		_killLocalItem = new JMenuItem("Kill Local ET");

		_connectItem.addActionListener(al);
		_disconnectItem.addActionListener(al);
		_startLocalItem.addActionListener(al);
		_killLocalItem.addActionListener(al);

		_etMenu.add(_connectItem);
		_etMenu.add(_disconnectItem);
		_etMenu.add(_startLocalItem);
		_etMenu.add(_killLocalItem);

		fixMenuItems();
		return _etMenu;
	}

	/**
	 * Is ET ready for next event?
	 * 
	 * @return <code>true</code> if it is ready,
	 */
	public static boolean isReady() {
		return (_evioConsumer != null);
	}

	// handle menu item selections
	private static void handleMenu(ActionEvent ae) {
		Object source = ae.getSource();

		if (source == _connectItem) {
			handleConnect();
		} else if (source == _disconnectItem) {
			handleDisconnect();
		} else if (source == _startLocalItem) {
			handleStartLocal();
		} else if (source == _killLocalItem) {
			handleKillLocal();
		}
		fixMenuItems();
	}

	// handle the connection menu item
	private static void handleConnect() {
		ETConnectDialog dialog = new ETConnectDialog();
		dialog.setVisible(true);

		if (dialog.getAnswer() == DialogUtilities.YES_RESPONSE) {
			Log.getInstance().info(
					"Try to connect to ET at " + dialog.getIpAddress()
							+ " on server port: " + dialog.getPort());

			if (_evioConsumer != null) {
				Log.getInstance()
						.warning(
								"Trying to make an ET connection but the shared cedConsumer is not null as expected.");
				_evioConsumer.detach();
			}
			_evioConsumer = new EvioETConsumer(dialog.getETName(),
					dialog.getIpAddress(), dialog.getPort());

		}
		fixMenuItems();
	}

	// handle the disconnect menu item
	private static void handleDisconnect() {
		if (_evioConsumer != null) {
			_evioConsumer.detach();
			_evioConsumer = null;
		}
		fixMenuItems();
	}

	private static void handleStartLocal() {

		final Shell shell = MiniShellView.getInstance().getShell();

		// try to kill any others
		String kcommand = "kill -9 `ps ax | grep StartET | awk ' {print $1;}'`";
		shell.execute(kcommand, 0, 0);

		if (_fileProducer != null) {
			Log.getInstance()
					.warning(
							"Trying to create a local ET but the shared fileProducer is not null as expected.");
			return;
		}
		// get the producer file
		final File file = openProducerFile();
		if (file == null) {
			return;
		}

		if (!startInSeparateJVM) {
			System.err.println("Sarting in same VM");
			new StartET(EvioFileProducer.MAXEVENTSIZE, 100);
			// create the producer
			_fileProducer = new EvioFileProducer(file, ETSupport._defaultName,
					_localHost, EtConstants.serverPort);

			// might as well create the consumer too to save a step
			if (_evioConsumer == null) {
				_evioConsumer = new EvioETConsumer(ETSupport._defaultName,
						_localHost, EtConstants.serverPort);
			}
			fixMenuItems();
			return;
		}

		// launch in separate VM using shell

		String command = StartET.getLaunchCommand();

		IProcessListener ipl = new IProcessListener() {

			@Override
			public void processStart(ProcessRecord processRecord) {
				System.err.println("Process start " + processRecord.getType());

				if (processRecord.getType() == _PROCTYPE) {
					Log.getInstance().info("Launching a local ET system");
					_procRecord = processRecord;

					// create the producer
					_fileProducer = new EvioFileProducer(file,
							ETSupport._defaultName, _localHost,
							EtConstants.serverPort);

					// might as well create the consumer too to save a step
					if (_evioConsumer == null) {
						_evioConsumer = new EvioETConsumer(
								ETSupport._defaultName, _localHost,
								EtConstants.serverPort);
					}
					fixMenuItems();

					// just wanted the start
					shell.removeProcessListener(this);
				}
			}

			@Override
			public void processEnd(ProcessRecord processRecord) {
			}

			@Override
			public void processFail(ProcessRecord processRecord) {
			}

		};

		shell.addProcessListener(ipl);
		shell.execute(command, 0, _PROCTYPE);
	}

	// handle the kill local ET
	private static void handleKillLocal() {
		System.err.println("procRecord: " + _procRecord);
		if (_procRecord != null) {
			// System.err.println("killing ET system process: " +
			// _procRecord.getProcess());
			_procRecord.kill();
			Log.getInstance().info("Killed a local ET system");
			_procRecord = null;
		}

		if (_fileProducer != null) {
			_fileProducer.shutDown();
		}

		if (_evioConsumer != null) {
			_evioConsumer.detach();
		}

		_fileProducer = null;
		_evioConsumer = null;
		fixMenuItems();

	}

	// fix the state of all menu items
	private static void fixMenuItems() {
		_killLocalItem.setEnabled(_fileProducer != null);
		_startLocalItem.setEnabled(_fileProducer == null);
		_disconnectItem.setEnabled(_evioConsumer != null);
		_connectItem.setEnabled(_evioConsumer == null);
		EventMenu.fixItems();
	}

	/**
	 * Obtain next event from ET
	 * 
	 * @return the next event from ET
	 */
	public static EvioEvent nextEvent() {
		if (_evioConsumer == null) {
			Log.getInstance().warning(
					"Called nextEvent in ETSupport with null CedETConsumer");
			return null;
		} else {
			_runningEventNum++;
			return _evioConsumer.nextEvent();
		}
	}

	/**
	 * A count of how many events we took from ET. A quasi-event number.
	 * 
	 * @return a count of how many events we took from ET
	 */
	public static long getETEventNumber() {
		return _runningEventNum;
	}

	/**
	 * Get a file for an ET producer
	 * 
	 * @return a file for an et producer
	 */
	public static File openProducerFile() {
		_producerEvioFile = FileUtilities.openFile(
				FileUtilities.getDefaultDir(), "evio Files",
				EventMenu._extensions);

		return _producerEvioFile;
	}
}
