package cnuphys.ced.training;

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Vector;

import javax.swing.event.EventListenerList;

import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.evio.clas12.EvioDataEvent;
import org.jlab.geom.DetectorHit;

import cnuphys.ced.clasio.ClasIoEventManager.EventSourceType;
import cnuphys.ced.fastmc.AcceptanceManager;
import cnuphys.ced.fastmc.FastMCManager;
import cnuphys.ced.fastmc.ParticleHits;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.IClasIoEventListener;

/**
 * For training, e.g. neural net or dictionary
 * @author heddle
 *
 */
public class TrainingManager implements IClasIoEventListener {

	//the singleton
	private static TrainingManager _instance;
	static {getInstance();}
	
	//listener list
	private static EventListenerList _listenerList;

	//create the singleton
	private TrainingManager() {
		ClasIoEventManager.getInstance().addClasIoEventListener(this, 2);
	}
	
	/**
	 * Public access for the singleton
	 * @return the singleton
	 */
	public static TrainingManager getInstance() {
		if (_instance == null) {
			_instance = new TrainingManager();
		}
		return _instance;
	}
	
	/**
	 * @param data detector data
	 * @param p the momentum in GeV/c
	 * @param theta the polar angle in degrees
	 * @param phi the azimuthal angle in degrees
	 */
	public static void notifyListeners(TrainingData data, double p, double theta, double phi) {

		if (_listenerList == null) {
			return;
		}

		// Guaranteed to return a non-null array
		Object[] listeners = _listenerList.getListenerList();

		// This weird loop is the bullet proof way of notifying all listeners.
		// for (int i = listeners.length - 2; i >= 0; i -= 2) {
		// order is flipped so it goes in order as added
		for (int i = 0; i < listeners.length; i += 2) {
			if (listeners[i] == ITrainingListener.class) {
				ITrainingListener listener = (ITrainingListener) listeners[i+1];
				listener.train(data, p, theta, phi);
			}

		}
	}

	
	/**
	 * Remove a TrainingListener.
	 * 
	 * @param trainer the TrainingListener to
	 *            remove.
	 */
	public static void removeTrainingListener(
			ITrainingListener training) {

		if ((training == null) || (_listenerList == null)) {
			return;
		}

		_listenerList.remove(ITrainingListener.class,
				training);
	}

	
	/**
	 * Add a training listener
	 * 
	 * @param trainer the listener to add
	 */
	public static void addTrainingListener(
			ITrainingListener trainer) {

		if (_listenerList == null) {
			_listenerList = new EventListenerList();
		}

		// avoid adding duplicates
		_listenerList.remove(ITrainingListener.class,
				trainer);
		_listenerList.add(ITrainingListener.class,
				trainer);
	}

	@Override
	public void newClasIoEvent(EvioDataEvent event) {
	}

	@Override
	public void openedNewEventFile(String path) {
	}

	@Override
	public void changedEventSource(EventSourceType source) {
	}

	@Override
	public void newFastMCGenEvent(PhysicsEvent event) {

		if (event == null) {
			return;
		}
		
		if (!AcceptanceManager.getInstance().currentEventAccepted()) {
//			System.err.println("Current Event not ACCEPTED");
			return;
		}
//		System.err.println("Current Event ACCEPTED");
		
		Vector<ParticleHits> phits = FastMCManager.getInstance().getFastMCHits();
		if ((phits == null) || phits.isEmpty()) {
			return;
		}
		
		Rectangle2D.Double wr = new Rectangle2D.Double(); // used over and over

		for (ParticleHits hits : phits) {
			List<DetectorHit> dchits = hits.getDCHits();
			if (dchits != null) {
				for (DetectorHit hit : dchits) {
//					int sect1 = hit.getSectorId() + 1;
//					int supl1 = hit.getSuperlayerId() + 1;
//					if ((sect1 == _sector) && (supl1 == _superLayer)) {
//						int lay1 = hit.getLayerId() + 1;
//						int wire1 = hit.getComponentId() + 1;
//						drawDCHit(g, container, lay1, wire1, false, hits.getLundId().getId(), wr);
//
//					}
				}
			}
		}
		
	}

	
	
}
