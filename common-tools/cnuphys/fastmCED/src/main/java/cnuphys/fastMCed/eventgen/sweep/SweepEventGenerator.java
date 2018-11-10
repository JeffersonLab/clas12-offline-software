package cnuphys.fastMCed.eventgen.sweep;

import org.jlab.clas.physics.PhysicsEvent;

import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.fastMCed.eventgen.AEventGenerator;

public class SweepEventGenerator extends AEventGenerator {
	
	//dialog used to generate random event event after it is closed
	private SweepEvGenDialog _dialog;
	
	//event number
	private int _eventNumber = 0;
	
	//most recent event
	private PhysicsEvent _currentEvent;
	
	private SweepEventGenerator(SweepEvGenDialog source) {
		_dialog = source;
	}
	
	public static SweepEventGenerator createSweepGenerator() {
		SweepEvGenDialog dialog = new SweepEvGenDialog(null);
		dialog.setVisible(true);
		
		if (dialog.getReason() == DialogUtilities.OK_RESPONSE) {
			return new SweepEventGenerator(dialog);
		}
		else {
			return null;
		}
	}

	@Override
	public String generatorDescription() {
		return "Sweep Generator";
	}

	@Override
	public PhysicsEvent nextEvent() {
		_currentEvent = _dialog.getEvent();
		_eventNumber++;
		return _currentEvent;
	}

	@Override
	public int eventNumber() {
		return _eventNumber;
	}

	@Override
	public int eventCount() {
		return (int)_dialog.totalSteps();
	}

	@Override
	public PhysicsEvent getCurrentEvent() {
		return _currentEvent;
	}

}