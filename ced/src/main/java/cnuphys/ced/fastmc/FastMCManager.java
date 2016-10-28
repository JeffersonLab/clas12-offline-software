package cnuphys.ced.fastmc;

import java.awt.Toolkit;
import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.geom.DetectorHit;
import org.jlab.geom.prim.Path3D;
import org.jlab.physics.io.LundReader;

import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.frame.Ced;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.splot.plot.Environment;
import cnuphys.swim.SwimMenu;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.Swimming;

public class FastMCManager {

	// the singleton
	private static FastMCManager _instance;

	// are we training?
	private boolean _training = false;

	// the event numnber
	private int _eventNum = 0;

	// current lund file
	private File _currentFile;

	// Lund reader
	private LundReader _lundReader;

	// current generated event
	private PhysicsEvent _genEvent;

	// are we streaming
	private boolean _isStreaming;

	/** Last selected data file */
	private static String dataFilePath = Environment.getInstance().getCurrentWorkingDirectory() + "/../../../data";

	public static String extensions[] = { "dat", "DAT" };

	private static FileNameExtensionFilter _lundFileFilter = new FileNameExtensionFilter("Lund Event Files",
			extensions);

	private Vector<ParticleHits> _particleHits = new Vector<ParticleHits>();

	private StreamTimer _streamTimer = new StreamTimer();

	// private constructor for singleton
	private FastMCManager() {
	}

	public StreamTimer getTimer() {
		return _streamTimer;
	}

	/**
	 * Check whether we are training
	 * 
	 * @return the training flag
	 */
	public boolean isTraining() {
		return _training;
	}

	/**
	 * Set whether we are training
	 * 
	 * @param training
	 *            the new training flag
	 */
	public void setTraining(boolean training) {
		_training = training;
	}

	/**
	 * Get the manager singleton
	 * 
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
	 * 
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

			// //test to eof
			// nextEvent();
			// while (_genEvent != null) nextEvent();
		}

		Ced.getCed().fixTitle();
		return _currentFile;
	}

	public void reloadCurrentEvent() {
		if (_genEvent != null) {
			parseEvent(_genEvent);
		}
	}
	
	public boolean nextEvent() {
		boolean gotOne = _lundReader.next();

		if (gotOne) {
			_genEvent = _lundReader.getEvent();
			_eventNum++;
			// System.err.println("fastmc event num: " + _eventNum + " has: " +
			// _genEvent.count() + " particles");
			// System.out.println(_genEvent.toLundString());

			parseEvent(_genEvent);
		} else {
			_genEvent = null;
			Toolkit.getDefaultToolkit().beep();
		}
		return gotOne;
	}

	/**
	 * Get the DC hit count for a given particle
	 * 
	 * @param lundid
	 *            the integer lund id
	 * @return the number of dc hits for tht particle
	 */
	public int particleDCHitCount(int lundid) {
		for (ParticleHits phits : _particleHits) {
			if (phits.lundId() == lundid) {
				return phits.DCHitCount();
			}
		}
		return 0;
	}

	// parse the event
	private void parseEvent(PhysicsEvent event) {
		_particleHits.clear();
		if ((event == null) || (event.count() < 1)) {
			return;
		}

		// always swim fastMC MC tracks, that is needed to get the hits
		SwimMenu.getInstance().firePropertyChange(SwimMenu.SWIM_ALL_MC_PROP, 0, 1);

		// how many trajectories?
		List<SwimTrajectory> trajectories = Swimming.getMCTrajectories();
		
		
//		int trajCount = (trajectories == null) ? 0 : trajectories.size();
		// System.err.println("NUMBER of FASTMC TRAJ: " + trajCount);

		// get DC hits for charged particles

		if (trajectories != null) {
			for (SwimTrajectory traj : trajectories) {
				if (traj.getLundId() != null) {
					Path3D path3D = GeometryManager.fromSwimTrajectory(traj);					
					_particleHits.add(new ParticleHits(traj.getLundId(), path3D));
				}
			}
		}

		// notify all listeners of the event
		AcceptanceManager.getInstance().testAccepted(event);
		ClasIoEventManager.getInstance().notifyListenersFastMCGenEvent(event);
	}

	/**
	 * Get the hits for all particles
	 * 
	 * @return the detector hits
	 */
	public Vector<ParticleHits> getFastMCHits() {
		return _particleHits;
	}

	/**
	 * Get the current Lund File
	 * 
	 * @return the current Lund file.
	 */
	public File getCurrentFile() {
		return _currentFile;
	}

	/**
	 * Get the current event number
	 * 
	 * @return the current event number
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
	 * 
	 * @return the current generated event
	 */
	public PhysicsEvent getCurrentGenEvent() {
		return _genEvent;
	}

	/**
	 * Get the data needed to run the SNR analysis
	 * 
	 * @return the data for the snr analysis
	 */
	public NoiseData getNoiseData() {

		if ((_particleHits == null) || (_particleHits.isEmpty())) {
			return null;
		}

		// count DC hits
		int count = 0;
		for (ParticleHits phits : _particleHits) {
			count += phits.DCHitCount();
		}

		if (count == 0) {
			return null;
		}

		NoiseData nd = new NoiseData();
		nd.count = count;
		nd.sector = new int[count];
		nd.superlayer = new int[count];
		nd.layer = new int[count];
		nd.wire = new int[count];
		int index = 0;
		for (ParticleHits phits : _particleHits) {
			List<DetectorHit> ldh = phits.getDCHits();
			if (ldh != null) {
				for (DetectorHit hit : ldh) {
					nd.sector[index] = hit.getSectorId() + 1;
					nd.superlayer[index] = hit.getSuperlayerId() + 1;
					nd.layer[index] = hit.getLayerId() + 1;
					nd.wire[index] = hit.getComponentId() + 1;
					index++;
				}
			}
		}

		return nd;
	}

	/**
	 * Set whether we are streaming
	 * 
	 * @param streaming
	 *            the new value of the streaming flag
	 */
	public void setStreaming(boolean streaming) {
		_isStreaming = streaming;
	}

	/**
	 * Check whether we are streaming
	 * 
	 * @return <code>true</code> id we are streaming
	 */
	public boolean isStreaming() {
		return _isStreaming;
	}

}
