package cnuphys.ced.training;

import javax.swing.event.EventListenerList;

/**
 * For training, e.g. neural net or dictionary
 * @author heddle
 *
 */
public class TrainingManager {

	//the singleton
	private static TrainingManager _instance;
	
	//listener list
	private static EventListenerList _listenerList;

	
	private TrainingManager() {
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
	public static void notifyListeners(long data[], double p, double theta, double phi) {

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
	public static void addMagneticFieldChangeListener(
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

	
	
}
