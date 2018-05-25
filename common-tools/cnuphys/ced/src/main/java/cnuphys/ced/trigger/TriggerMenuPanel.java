package cnuphys.ced.trigger;

import java.awt.Dimension;

import org.jlab.io.base.DataEvent;

import cnuphys.ced.alldata.ColumnData;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.IClasIoEventListener;
import cnuphys.ced.clasio.ClasIoEventManager.EventSourceType;

public class TriggerMenuPanel extends TriggerPanel implements IClasIoEventListener {
	
	//the bank name
	private static String _bankName = "RUN::trigger";

	
	public TriggerMenuPanel() {
		super(true);
		ClasIoEventManager.getInstance().addClasIoEventListener(this, 2);
	}
	
	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		d.width = _preferredWidth;
		return d;
	}
	
	@Override
	public Dimension getMaximumSize() {
		return getPreferredSize() ;
	}



	@Override
	public void newClasIoEvent(DataEvent event) {
		if (ClasIoEventManager.getInstance().isAccumulating()) {
		}
		else {  //single event
			setBits(0,  0);

			
			int idData[] = null;
			int triggerData[] = null;
			
			idData = ColumnData.getIntArray(_bankName + ".id");
			if (idData != null) {
				triggerData = ColumnData.getIntArray(_bankName + ".trigger");
				setBits(idData[0], triggerData[0]);

			}
			
			
		}
	}

	@Override
	public void openedNewEventFile(String path) {
	}

	@Override
	public void changedEventSource(EventSourceType source) {
	}
	
	/**
	 * Tests whether this listener is interested in events while accumulating
	 * @return <code>true</code> if this listener is NOT interested in  events while accumulating
	 */
	@Override
	public boolean ignoreIfAccumulating() {
		return true;
	}


}
