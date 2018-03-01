package cnuphys.ced.trigger;

import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.io.base.DataEvent;

import cnuphys.ced.alldata.ColumnData;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.IClasIoEventListener;
import cnuphys.ced.clasio.ClasIoEventManager.EventSourceType;

public class TriggerManager implements IClasIoEventListener{

	//singleton
	private static TriggerManager _instance;
	
	//the dialog for display
	private TriggerDialog _dialog;
	
	//the bank name
	private static String _bankName = "RUN::trigger";
	
	
	//the data columns in the Run::trigger bank
	private int _id[];
	private int _trigger[];
	
	
	//private constructor for singleton
	private TriggerManager() {
		ClasIoEventManager.getInstance().addClasIoEventListener(this, 2);
		_dialog = TriggerDialog.getInstance();
	}
	
	/**
	 * Public access to the TriggerManager
	 * @return the TriggerManager singleton
	 */
	public static TriggerManager getInstance() {
		if (_instance == null) {
			_instance = new TriggerManager();
		}
		
		return _instance;
	}
	

	@Override
	public void newClasIoEvent(DataEvent event) {
		if (ClasIoEventManager.getInstance().isAccumulating()) {
		}
		else {  //single event
			System.err.println("Trigger Manager got a single event");
			
			
			_id = null;
			_trigger = null;
			
			_id = ColumnData.getIntArray(_bankName + ".id");
			if (_id != null) {
				_trigger = ColumnData.getIntArray(_bankName + ".trigger");
				System.err.println("Trigger words: " + ((_trigger == null) ? 0 : _trigger.length));
			}
			
			_dialog.setCurrentEvent(_id, _trigger);
		}
	}

	@Override
	public void openedNewEventFile(String path) {
	}

	@Override
	public void changedEventSource(EventSourceType source) {
	}

	@Override
	public void newFastMCGenEvent(PhysicsEvent event) {
	}

}
