package cnuphys.bCNU.simanneal;

import java.util.Random;

import javax.swing.event.EventListenerList;

public class Simulation {
	
	private Solution _bestSolution;
	
	private double _temperature;
	
	private double _coolRate;
		
	//number of successes
	private int _successCount;
	
	// Listener list for solution updates.
	private EventListenerList _listenerList;
	
	private Random _rand;
	
	private double _minTemp;

	public Simulation(Solution initialSolution, double coolRate) {
		this(initialSolution, coolRate,-1L);
	}
		
	public Simulation(Solution initialSolution, double coolRate, long seed) {
		
		if (seed > 0) {
			_rand = new Random(seed);
		}
		else {
			_rand = new Random();
		}
		
		_temperature = 1.0;
		_bestSolution = initialSolution;
		_coolRate = coolRate;
		
		//base the min temp loosly on the coorate
		_minTemp = Math.min(0.003, _coolRate);
	}
	
	public double getTemperature() {
		return _temperature;
	}
	
	public void run() {
		double factor = 1. - _coolRate;
		Solution oldBest = _bestSolution.copy();
		
		while (_temperature > _minTemp) {
			for (int i = 0; i < _bestSolution.getThermalizationCount(); i++) {
				Solution neighbor = _bestSolution.getNeighbor();
				double ebest = _bestSolution.getEnergy();
				double etest = neighbor.getEnergy();
				
				if (etest < ebest) {
					_bestSolution = neighbor;
				}
				else {
					double delE = etest - ebest; //> 0
					double prob = Math.exp(-delE/_temperature);
					if (_rand.nextDouble() < prob) {
						_bestSolution = neighbor;
					}
				}
			}
			
			_temperature *= factor;
			notifyListeners(_bestSolution, oldBest);
		}
	}
	
	/**
	 * Notify listeners that a range slider (namely "this") was updated.
	 */
	protected void notifyListeners(Solution newSolution, Solution oldSolution) {
		if (_listenerList == null) {
			return;
		}

		// Guaranteed to return a non-null array
		Object[] listeners = _listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IUpdateListener.class) {
				((IUpdateListener) listeners[i + 1])
						.updateSolution(newSolution, oldSolution, _temperature);
			}
		}
	}
	

	/**
	 * Remove a solution update listener.
	 * 
	 * @param listener
	 *            the update listener to remove.
	 */
	public void removeUpdateListener(IUpdateListener listener) {

		if ((listener == null) || (_listenerList == null)) {
			return;
		}

		_listenerList.remove(IUpdateListener.class, listener);
	}

	/**
	 * Add a solution update listener.
	 * 
	 * @param listener
	 *            the update listener to add.
	 */
	public void addUpdateListener(IUpdateListener listener) {

		if (listener == null) {
			return;
		}

		if (_listenerList == null) {
			_listenerList = new EventListenerList();
		}

		_listenerList.add(IUpdateListener.class, listener);
	}


}
