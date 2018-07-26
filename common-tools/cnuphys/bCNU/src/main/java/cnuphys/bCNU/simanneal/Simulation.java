package cnuphys.bCNU.simanneal;

import java.util.Random;

import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.swing.event.EventListenerList;

import cnuphys.bCNU.attributes.Attributes;

public abstract class Simulation implements Runnable {
	
	//current state of the simulation
	private SimulationState _simState = SimulationState.STOPPED;
	
	//common attribute keys
	public static final String RANDSEED = "randseed";
	public static final String COOLRATE = "coolrate";
	public static final String MINTEMP  = "mintemp";
	public static final String THERMALCOUNT  = "thermalcount";
	public static final String SUCCESSCOUNT  = "successcount";
	public static final String MAXSTEPS  = "maxsteps";
	public static final String PLOTTITLE  = "plottitle";
	public static final String XAXISLABEL  = "xaxislabel";
	public static final String YAXISLABEL  = "yaxislabel";
	public static final String USELOGTEMP  = "uselogtemp";

	//current solution
	protected Solution _currentSolution;
		
	//current temperature
	protected double _temperature;
	
	//the cooling rate used this way,
	//tNext = (1-coolrate)*tCurrent
	protected double _coolRate = 0.1;
		
	//number of successes before lowering temp
	protected int _successCount;
	
	//number of tries before lowering temp (unless
	//_successCount reached first
	protected int _thermalizationCount = 200;
	
	// Listener list for solution updates.
	protected EventListenerList _listenerList;
	
	//random number generator
	protected Random _rand;
	
	//simulation attributes
	protected Attributes _attributes;
	
	//the min or stopping temperature
	protected double _minTemp;
	
	//max steps (temp reductions) until stop (unless min temp is reached)
	protected int _maxSteps = 100;
	
	//the initial solution. Saved to be available for reset.
	protected Solution _initialSolution;
	
	//the thread that runs the simulation
	protected Thread _thread;
		
	/**
	 * Create a Simulation
	 * @param props key-value properties of the simulation. Used for initialization.
	 */
	public Simulation() {

		//call the subclass to set up attributes and create the initial solution
		
		_attributes = defaultAttributes();
		setInitialAttributes(_attributes);
		
		//create the random number generator
		createRandomGenerator();

		//cache the initial solution and make a copy
		_initialSolution = setInitialSolution();
		_currentSolution = _initialSolution.copy();
	}
	
	/**
	 * Get the simulation state
	 * @return the simulation state
	 */
	public SimulationState getSimulationState() {
		return _simState;
	}
	
	/**
	 * Set the simulation state
	 * @param simState the new simulation state
	 */
	public void setSimulationState(SimulationState simState) {
		if (_simState == simState) {
			return;
		}
		SimulationState oldState = _simState; 
		_simState = simState;
//		System.err.println("STATE IS NOW " + _simState);
		notifyListeners(oldState, _simState);
	}
	
	/**
	 * Retrieve the initial solution
	 * @return the initial solution
	 */
	public Solution getInitialSolution() {
		return _initialSolution;
	}
	
	private Attributes defaultAttributes() {
		Attributes attributes = new Attributes();
		attributes.add(Simulation.COOLRATE, 0.03);
		attributes.add(Simulation.RANDSEED, -1L);
		attributes.add(Simulation.THERMALCOUNT, 200);
		attributes.add(Simulation.MAXSTEPS, 1000);

		
		attributes.add(Simulation.USELOGTEMP, false, false, false);
		attributes.add(Simulation.PLOTTITLE, "Simulated Annealing", false, false);
		attributes.add(Simulation.XAXISLABEL, "Temperature", false, false);
		attributes.add(Simulation.YAXISLABEL, "Energy", false, false);

		return attributes;
	}
	
	/**
	 * Get the initial attributes
	 * @return the initial attributes
	 */
	protected abstract void setInitialAttributes(Attributes attributes);
	
	/**
	 * Create the initial solution
	 * @return the initial solution
	 */
	protected abstract Solution setInitialSolution();
	
	/**
	 * Reset the simulation
	 */
	protected void reset() {
		_initialSolution = setInitialSolution();
		_currentSolution = _initialSolution.copy();
		notifyListeners();
//		notifyListeners(_initialSolution, _initialSolution);
	}
	
	/**
	 * Accessor for the attributes
	 * @return the attributes
	 */
	public Attributes getAttributes() {
		return _attributes;
	}
	
	/**
	 * Get the current solution
	 * @return the current solution
	 */
	public Solution currentSolution() {
		return _currentSolution;
	}
	
	//make a guess for an initial temperature
	private void setInitialTemperature() {
		//find a average energy step
		
		int n = 100;
		double e0 = _currentSolution.getEnergy();
		double sum = 0;
		
		for (int i  = 0; i < n; i++) {
			double e1 = _currentSolution.getRearrangement().getEnergy();
			sum += Math.pow(e1-e0, 2);
		}
		
//		_temperature = 10*Math.sqrt(sum/n);
		_temperature = 1.2*Math.sqrt(sum/n);
		
//		System.out.println("Initial temperature: " + _temperature);
	}
	
	// create the random generator using a seed if provided
	private void createRandomGenerator() {

		if (_attributes.contains(RANDSEED)) {
			try {
				long seed = _attributes.getAttribute(RANDSEED).getLong();
				if (seed > 0) {
					_rand = new Random(seed);
				} else {
					_rand = new Random();
				}
			} catch (InvalidTargetObjectTypeException e) {
				e.printStackTrace();
			}
		} else {
			_rand = new Random();
		}
	}

	// set parameters from what is in attributes
	private void setParametersFromAttributes() {
		// coolrate
		try {
			if (_attributes.contains(COOLRATE)) {
				_coolRate = _attributes.getAttribute(COOLRATE).getDouble();
			}

			// min temp
			if (_attributes.contains(MINTEMP)) {
				_minTemp = _attributes.getAttribute(MINTEMP).getDouble();
			} else {
				_minTemp = Math.min(1.0e-08, _coolRate);
			}

			// thermalization count
			if (_attributes.contains(THERMALCOUNT)) {
				_thermalizationCount = _attributes.getAttribute(THERMALCOUNT).getInt();
			}

			// success count
			if (_attributes.contains(SUCCESSCOUNT)) {
				_successCount = _attributes.getAttribute(SUCCESSCOUNT).getInt();
			} else {
				_successCount = _thermalizationCount / 10;
			}

			// max steps
			if (_attributes.contains(MAXSTEPS)) {
				_maxSteps = _attributes.getAttribute(MAXSTEPS).getInt();
			}
		} catch (InvalidTargetObjectTypeException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Get the cool rate. It is used this way:
	 * tNext = (1-coolrate)*tCurrent
	 * @return the cool rate.
	 */
	public double getCoolRate() {
		return _coolRate;
	}
	
	/**
	 * Set the cool rate. It is used this way:
	 * tNext = (1-coolrate)*tCurrent
	 * @param coolRate the new cool rate
	 */
	public void setCoolRate(double coolRate) {
		_coolRate = coolRate;
	}
	
	/**
	 * Get the temperature
	 * @return the temperature
	 */
	public double getTemperature() {
		return _temperature;
	}
	
	/**
	 * Get the number of reconfigure attempts at a given temperature
	 * @return the number of reconfigure attempts at a given temperature
	 */
	public int getThermalizationCount() {
		return _thermalizationCount;
	}
	
	/**
	 * Set the number of reconfigure attempts at a given temperature
	 * @param thermalCount the number of reconfigure attempts at a given temperature
	 */
	public void setThermalizationCount(int thermalCount) {
		_thermalizationCount = thermalCount;
	}

	/**
	 * Get the number of successful reconfigure attempts that will
	 * short circuit the thermalization process
	 * @return the number of successful reconfigure attempts
	 */
	public int getSuccessCount() {
		return _successCount;
	}
	
	/**
	 * Set the number of successful reconfigure attempts that will
	 * @param successCount the number of successful reconfigure attempts
	 */
	public void setSuccessCount(int successCount) {
		_successCount = successCount;
	}

	
	/**
	 * Get the minimum or stopping temperature
	 * @return the minimum or stopping temperature
	 */
	public double getMinTemperature() {
		return _minTemp;
	}
	
	/**
	 * Set the minimum or stopping temperature
	 * @param minTemp the minimum or stopping temperature
	 */
	public void setMinTemperature(double minTemp) {
		_minTemp = minTemp;
	}
	
	/**
	 * Start the simulation
	 */
	public void startSimulation() {
		
		if ((_thread != null) && _thread.isAlive()) {
			_simState = SimulationState.STOPPED;
			try {
				System.out.print("Waiting for current thread to die");
				_thread.join();
				System.out.println("died.");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		setParametersFromAttributes();
		setInitialTemperature();

		_thread = new Thread(this);
		_simState = SimulationState.RUNNING;
		_thread.start();
	}
	
	/**
	 * run the simulation
	 */
	@Override
	public void run() {

		double factor = 1. - _coolRate;
		Solution oldSolution = _currentSolution.copy();

		int step = 0;

		while ((_simState != SimulationState.STOPPED) && (step < _maxSteps) && (_temperature > _minTemp)) {

			if (_simState == SimulationState.PAUSED) {
				//sleep for a second
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			} else if (_simState == SimulationState.RUNNING) {  //running
				int succ = 0;

				double eCurrent = _currentSolution.getEnergy();

				for (int i = 0; i < getThermalizationCount(); i++) {
					Solution rearrangement = _currentSolution.getRearrangement();
					double eTest = rearrangement.getEnergy();

					if (metrop(eCurrent, eTest)) {
						_currentSolution = rearrangement;
						eCurrent = eTest;
						succ++;
						if (succ > _successCount) {
							break;
						}
					}

				}

				// reduce the temperature
				_temperature *= factor;
				
	//			System.err.println("Current temp: " + _temperature);
				step++;
				notifyListeners(_currentSolution, oldSolution);
			} //running
		} // while
		
		setSimulationState(SimulationState.STOPPED);
	}
	
		
	//the Metropolis test
	private boolean metrop(double ebest, double etest) {
		if (etest < ebest) {
			return true;
		}
		double delE = etest - ebest; //> 0
		double prob = Math.exp(-delE/_temperature);
		return (_rand.nextDouble() < prob);
	}
	
	/**
	 * Notify listeners that the solution was updated
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
						.updateSolution(this, newSolution, oldSolution);
			}
		}
		
	}
	
	/**
	 * Notify listeners that the simulation was reset
	 */
	protected void notifyListeners() {
		if (_listenerList == null) {
			return;
		}

		// Guaranteed to return a non-null array
		Object[] listeners = _listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IUpdateListener.class) {
				((IUpdateListener) listeners[i + 1]).reset(this);
			}
		}
		
	}
	
	/**
	 * Notify listeners that the state changed
	 */
	protected void notifyListeners(SimulationState oldState, SimulationState simState) {
		if (_listenerList == null) {
			return;
		}

		// Guaranteed to return a non-null array
		Object[] listeners = _listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IUpdateListener.class) {
				((IUpdateListener) listeners[i + 1]).stateChange(this, oldState, _simState);
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
