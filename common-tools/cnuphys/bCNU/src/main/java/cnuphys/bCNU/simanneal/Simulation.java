package cnuphys.bCNU.simanneal;

import java.util.Properties;
import java.util.Random;

import javax.swing.event.EventListenerList;

public class Simulation {
	
	//property keys
	public static final String RANDSEED = "randseed";
	public static final String COOLRATE = "coolrate";
	public static final String MINTEMP  = "mintemp";
	public static final String THERMALCOUNT  = "thermalcount";
	public static final String SUCCESSCOUNT  = "successcount";
	public static final String MAXSTEPS  = "maxsteps";

	//current solution
	private Solution _currentSolution;
		
	//current temperature
	private double _temperature;
	
	//the cooling rate used this way,
	//tNext = (1-coolrate)*tCurrent
	private double _coolRate = 0.1;
		
	//number of successes before lowering temp
	private int _successCount;
	
	//number of tries before lowering temp (unless
	//_successCount reached first
	private int _thermalizationCount = 200;
	
	// Listener list for solution updates.
	private EventListenerList _listenerList;
	
	//random number generator
	private Random _rand;
	
	//simulation properties
	private Properties _props;
	
	//the min or stopping temperature
	private double _minTemp;
	
	//max steps (temp reductions) until stop (unless min temp is reached)
	private int _maxSteps = 100;

		
	/**
	 * Create a Simulation
	 * @param initialSolution the initial solution
	 * @param props key-value properties of the simulation
	 */
	public Simulation(Solution initialSolution, Properties props) {
		
		_props = props;
		
		createRandomGenerator();
		setParameters();
		
		_currentSolution = initialSolution;
		
		setInitialTemperature();
		
	}
	
	/**
	 * Get the current solution
	 * @return the current solution
	 */
	public Solution currentSolution() {
		return _currentSolution;
	}
	
	private void setInitialTemperature() {
		//find a average energy step
		
		int n = 100;
		double e0 = _currentSolution.getEnergy();
		double sum = 0;
		
		for (int i  = 0; i < n; i++) {
			double e1 = _currentSolution.getNeighbor().getEnergy();
			sum += Math.pow(e1-e0, 2);
		}
		
		_temperature = 10*Math.sqrt(sum/n);
		
		System.out.println("Initial temperature: " + _temperature);
	}
	
	//create the random generator using a seed if provided
	private void createRandomGenerator() {
		if (_props.containsKey(RANDSEED)) {
			long seed = Long.parseLong(_props.getProperty(RANDSEED));
			if (seed > 0) {
				_rand = new Random(seed);
			}
			else {
				_rand = new Random();
			}
		}
		else {
			_rand = new Random();
		}
	}
	
	//set parameters from what is in properties
	private void setParameters() {
		//coolrate
		if (_props.containsKey(COOLRATE)) {
			_coolRate = Double.parseDouble(_props.getProperty(COOLRATE));
		}
		
		//min temp
		if (_props.containsKey(MINTEMP)) {
			_minTemp = Double.parseDouble(_props.getProperty(MINTEMP));
		}
		else {
			_minTemp = Math.min(1.0e-08, _coolRate);
		}

		//thermalization count
		if (_props.containsKey(THERMALCOUNT)) {
			_thermalizationCount = Integer.parseInt(_props.getProperty(THERMALCOUNT));
		}
		
		//success count
		if (_props.containsKey(SUCCESSCOUNT)) {
			_successCount = Integer.parseInt(_props.getProperty(SUCCESSCOUNT));
		}
		else {
			_successCount = _thermalizationCount/10;
		}
		
		//max steps
		if (_props.containsKey(MAXSTEPS)) {
			_maxSteps = Integer.parseInt(_props.getProperty(MAXSTEPS));
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
	 * run the simulation
	 */
	public void run() {
		double factor = 1. - _coolRate;
		Solution oldSolution = _currentSolution.copy();
		
		int step = 0;
		
		while ((step < _maxSteps) && (_temperature > _minTemp)) {
			
			int succ = 0;
			
			double eCurrent = _currentSolution.getEnergy();
			
			for (int i = 0; i < getThermalizationCount(); i++) {
				Solution neighbor = _currentSolution.getNeighbor();
				double eTest = neighbor.getEnergy();
				
				if (metrop(eCurrent, eTest)) {
					_currentSolution = neighbor;
					eCurrent = eTest;
					succ++;
					if (succ > _successCount) {
						break;
					}
				}
							
			}
			
			_temperature *= factor;
			step++;
			notifyListeners(_currentSolution, oldSolution);
		}
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
