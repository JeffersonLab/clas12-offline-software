package cnuphys.ced.fastmc;

import org.jlab.clas.physics.PhysicsEvent;

public class AcceptanceManager {
	
	//acceptance dialog
	private AcceptanceDialog _dialog;

	private static AcceptanceManager _instance;
	private AcceptanceManager() {	
	}
	
	/**
	 * Access to the singleton AcceptanceManager
	 * @return the AcceptanceManager
	 */
	public static AcceptanceManager getInstance() {
		if (_instance == null) {
			_instance = new AcceptanceManager();
		}
		return _instance;
	}
	
	/**
	 * Check whether a given event is accepted
	 * @param event the event in question
	 * @return
	 */
	public boolean accepted(PhysicsEvent event) {
		if (event ==  null) {
			return false;
		}
		
		//TODO implement, for now just return true
		return true;
	}
	
	/**
	 * Get a string reason why an event was not accepted
	 * @param event the event in question
	 * @return a string reason
	 */
	public String unacceptedReason(PhysicsEvent event) {
		if (event == null) {
			return "no event";
		}
		
		if (accepted(event)) {
			return "accepted";
		}
		//TODO implement 
		return "not accepted";
	}
	
	/**
	 * Define the acceptance
	 */
	public void defineAcceptance() {
	
		if (_dialog == null) {
			_dialog = new AcceptanceDialog();
		}
		_dialog.setVisible(true);
	}
}
