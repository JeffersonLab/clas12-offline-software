package cnuphys.ced.fastmc;

import java.awt.Graphics;
import java.awt.Toolkit;
import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.clasrec.io.LundReader;
import org.jlab.geom.DetectorHit;
import org.jlab.geom.prim.Path3D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.frame.Ced;
import cnuphys.ced.geometry.DCGeometry;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.splot.plot.Environment;
import cnuphys.swim.SwimMenu;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.Swimming;

public class FastMCManager {
	
	//the singleton
	private static FastMCManager _instance;
	
	//are we training?
	private boolean _training = false;
	
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

	private Vector<ParticleHits> _particleHits = new Vector<ParticleHits>();
	
	//private constructor for singleton
	private FastMCManager() {
	}
	
	/**
	 * Check whether we are training
	 * @return the training flag
	 */
	public boolean isTraining() {
		return _training;
	}
	
	/**
	 * Set whether we are training
	 * @param training the new training flag
	 */
	public void setTraining(boolean training) {
		_training = training;
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
	//		System.err.println("fastmc event num: " + _eventNum + "  has: " + _genEvent.count() + " particles");
	//		System.out.println(_genEvent.toLundString());
			parseEvent(_genEvent);
		}
		else {
			_genEvent = null;
			Toolkit.getDefaultToolkit().beep();
		}
		return gotOne;
	}
	
	//parse the event
	private void parseEvent(PhysicsEvent event) {
		clear();
		if ((event == null) || (event.count() < 1)) {
			return;
		}
		
		//always swim fastMC MC tracks, that is needed to get the hits
		SwimMenu.getInstance().firePropertyChange(SwimMenu.SWIM_ALL_MC_PROP, 0,
				1);
		
		//how many trajectories?
		Vector<SwimTrajectory> trajectories = Swimming.getMCTrajectories();
		int trajCount = (trajectories == null) ? 0 : trajectories.size();
//		System.err.println("NUMBER of FASTMC TRAJ: " + trajCount);

		// get DC hits for charged particles

		if (trajectories != null) {
			for (SwimTrajectory traj : trajectories) {
				if (traj.getLundId() != null) {
					Path3D path3D = GeometryManager.fromSwimTrajectory(traj);
					_particleHits.add(new ParticleHits(traj.getLundId(), path3D));
				}
				
//				int charge = traj.getLundId().getCharge();
//				System.err.println("CHARGE: " + charge);
//				if (charge != 0) {
//					List<DetectorHit> hits = DCGeometry.getHits(path3D);
//					
//					for (DetectorHit hit : hits) {
//						// indices from gethits are 0-based
//						System.err.println("sector: " + (hit.getSectorId()+1) 
//						+ " superlayer: " + (hit.getSuperlayerId()+1)
//						+ " layer: " + (hit.getLayerId()+1) 
//						+ "  wire: " + (hit.getComponentId()+1));
//					}
//				}
			}
		}
		
		
//		ISwimAll allSwimmer = ClasIoEventManager.getInstance().getMCSwimmer();
//		if (allSwimmer != null) {
//			TrajectoryTableModel model = _trajectoryTable
//					.getTrajectoryModel();
//			model.setData(allSwimmer.getRowData());
//			model.fireTableDataChanged();
//			_trajectoryTable.repaint();
//		}

		
//		for (int index = 0; index < event.count(); index++) {
//			Particle particle = event.getParticle(index);
//			System.err.println("[" + (index+1) + "]  " +  particle.toLundString());
//		}
		
		//notify all listeners of the event
		ClasIoEventManager.getInstance().notifyListenersFastMCGenEvent(event);
	}
	
	private void clear() {
		_particleHits.clear();
		Swimming.clearMCTrajectories();
		Swimming.clearReconTrajectories();
	}
	
	/**
	 * Get the hits for all particles
	 * @return the detector hits
	 */
	public Vector<ParticleHits> getFastMCHits() {
		return _particleHits;
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
