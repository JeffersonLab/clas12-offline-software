package cnuphys.ced.fastmc;

import java.util.Vector;

import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.ced.clasio.ClasIoEventManager.EventSourceType;
import cnuphys.ced.clasio.IClasIoEventListener;

public class AcceptanceManager implements IClasIoEventListener {
	
	public enum AcceptanceStatus {UNCHECKED, ACCEPTED, NOTACCEPTED}
	
	private AcceptanceStatus _currentStatus = AcceptanceStatus.UNCHECKED;

	// list of conditions that are added together
	private static Vector<ACondition> _conditions = new Vector<ACondition>();
	
	//acceptance result
	private String acceptanceResult = "Unchecked";
	
	private static ElectronCondition _e36Condition;
	private static ProtonCondition _p36Condition;

	private static AcceptanceManager _instance;

	private AcceptanceManager() {
	}

	public ACondition getElectronCondition() {
		return _e36Condition;
	}
	
	public ACondition getProtonCondition() {
		return _p36Condition;
	}
	
	/**
	 * Access to the singleton AcceptanceManager
	 * 
	 * @return the AcceptanceManager
	 */
	public static AcceptanceManager getInstance() {
		if (_instance == null) {
			_instance = new AcceptanceManager();

			_e36Condition = new ElectronCondition(36, true);
			_p36Condition = new ProtonCondition(36, false);
			
			// default condition is 36 hits for electrons
			_conditions.add(_e36Condition);
			_conditions.add(_p36Condition);
		}
		return _instance;
	}

	/**
	 * Is the current event accepted
	 * @return <code>true</code> if the current event is accepted
	 */
	public boolean currentEventAccepted() {
		if (_currentStatus == AcceptanceStatus.UNCHECKED) {
			testAccepted(FastMCManager.getInstance().getCurrentGenEvent());
		}
		return (_currentStatus == AcceptanceStatus.ACCEPTED);
	}

	/**
	 * Retest the current event
	 */
	public void retestCurrentEvent() {
		_currentStatus = AcceptanceStatus.NOTACCEPTED;
		testAccepted(FastMCManager.getInstance().getCurrentGenEvent());
	}

	/**
	 * Check whether the current event is accepted
	 * 
	 */
	public void testAccepted(PhysicsEvent event) {
		if (event == null) {
			acceptanceResult = "";
			_currentStatus = AcceptanceStatus.UNCHECKED;
			return;
		}

		if ((_conditions == null) || _conditions.isEmpty()) {
			acceptanceResult = "Accepted";
			_currentStatus = AcceptanceStatus.ACCEPTED;
			return;
		}

		for (ACondition condition : _conditions) {
			if (condition.isActive() && !condition.pass()) {
				acceptanceResult = "Failed: " + condition.getDescription();
				_currentStatus = AcceptanceStatus.NOTACCEPTED;
				return;
			}
		}

		acceptanceResult = "Accepted";
		_currentStatus = AcceptanceStatus.ACCEPTED;
	}

	/**
	 * Get a string reason why an event was not accepted
	 * 
	 * @param event
	 *            the event in question
	 * @return a string reason
	 */
	public String acceptanceResult() {
		if (_currentStatus == AcceptanceStatus.UNCHECKED) {
			testAccepted(FastMCManager.getInstance().getCurrentGenEvent());
		}
		return acceptanceResult;
	}

	/**
	 * Define the acceptance
	 */
	public void defineAcceptance() {
	}

	@Override
	public void newClasIoEvent(EvioDataEvent event) {
		_currentStatus = AcceptanceStatus.UNCHECKED;
		acceptanceResult = "";
	}

	@Override
	public void openedNewEventFile(String path) {
		_currentStatus = AcceptanceStatus.UNCHECKED;
		acceptanceResult = "";
	}

	@Override
	public void changedEventSource(EventSourceType source) {
		_currentStatus = AcceptanceStatus.UNCHECKED;
		acceptanceResult = "";
	}

	@Override
	public void newFastMCGenEvent(PhysicsEvent event) {
		_currentStatus = AcceptanceStatus.UNCHECKED;
		acceptanceResult = "";

	}

}
