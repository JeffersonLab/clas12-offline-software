package cnuphys.ced.event.data;

import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.io.base.DataEvent;

import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.ClasIoEventManager.EventSourceType;
import cnuphys.ced.clasio.IClasIoEventListener;

public class DetectorData implements IClasIoEventListener {
	
	public DetectorData() {
		ClasIoEventManager.getInstance().addClasIoEventListener(this, 0);
	}

	@Override
	public void newClasIoEvent(DataEvent event) {
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
