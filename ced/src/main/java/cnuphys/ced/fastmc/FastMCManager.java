package cnuphys.ced.fastmc;

import java.awt.Toolkit;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.clasrec.io.LundReader;

import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.frame.Ced;
import cnuphys.splot.plot.Environment;

public class FastMCManager {
	
	//the singleton
	private static FastMCManager _instance;
	
	//the event numnber
	private int _eventNum = 0;
	
	//current lund file
	private File _currentFile;
	
	//Lund reader
	private LundReader _lundReader;
	
	//current generated event
	private PhysicsEvent _genEvent;
	
	/** Last selected data file */
	private static String dataFilePath = Environment.getInstance().getCurrentWorkingDirectory() + "/../../../data";

	public static String extensions[] = { "dat", "DAT"};

	private static FileNameExtensionFilter _lundFileFilter = new FileNameExtensionFilter(
			"Lund Event Files", extensions);

	
	//private constructor for singleton
	private FastMCManager() {
	}

	/**
	 * Get the manager singleton
	 * @return the manager
	 */
	public static FastMCManager getInstance() {
		if (_instance == null) {
			_instance = new FastMCManager();
		}
		return _instance;
	}
	
	/**
	 * Open a Lund File
	 * @return the lund file
	 */
	public File openFile() {
		_currentFile = null;
		_lundReader = new LundReader();
		
		JFileChooser chooser = new JFileChooser(dataFilePath);
		chooser.setSelectedFile(null);
		chooser.setFileFilter(_lundFileFilter);
		int returnVal = chooser.showOpenDialog(Ced.getFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			_currentFile = chooser.getSelectedFile();
			dataFilePath = _currentFile.getParent();
			
			_lundReader.addFile(_currentFile.getPath());
			_lundReader.open();
			_eventNum = 0;
			
//			//test to eof
//			nextEvent();
//			while (_genEvent != null) nextEvent();
		}

		
		Ced.getCed().fixTitle();
		return _currentFile;
	}
	
	public boolean nextEvent() {
		boolean gotOne = _lundReader.next();
		if (gotOne) {
			_genEvent = _lundReader.getEvent();
			_eventNum++;
			System.err.println("fastmc event num: " + _eventNum + "  has: " + _genEvent.count() + " particles");
	//		System.out.println(_genEvent.toLundString());
			parseEvent(_genEvent);
		}
		else {
			_genEvent = null;
			Toolkit.getDefaultToolkit().beep();
		}
		return gotOne;
	}
	
	private void parseEvent(PhysicsEvent event) {
		clear();
		if ((event == null) || (event.count() < 1)) {
			return;
		}
		
//		ISwimAll allSwimmer = ClasIoEventManager.getInstance().getMCSwimmer();
//		if (allSwimmer != null) {
//			TrajectoryTableModel model = _trajectoryTable
//					.getTrajectoryModel();
//			model.setData(allSwimmer.getRowData());
//			model.fireTableDataChanged();
//			_trajectoryTable.repaint();
//		}

		
		for (int index = 0; index < event.count(); index++) {
			Particle particle = event.getParticle(index);
//			System.err.println("[" + (index+1) + "]  " +  particle.toLundString());
		}
		
		ClasIoEventManager.getInstance().notifyListenersFastMCGenEvent(event);
	}
	
	private void clear() {
		
	}
	
	/**
	 * Get the current Lund File
	 * @return the current Lund file.
	 */
	public File getCurrentFile() {
		return _currentFile;
	}
	
	/**
	 * Get the current event number
	 * @return  the current event number
	 */
	public int getEventNumber() {
		return _eventNum;
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
	 * Get the current generated event
	 * @return the current generated event
	 */
	public PhysicsEvent getCurrentGenEvent() {
		return _genEvent;
	}

}
