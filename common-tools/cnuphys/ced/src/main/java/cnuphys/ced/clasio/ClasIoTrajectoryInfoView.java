package cnuphys.ced.clasio;

import java.util.Vector;

import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.view.BaseView;
import cnuphys.lund.TrajectoryRowData;
import cnuphys.lund.TrajectoryTable;

public abstract class ClasIoTrajectoryInfoView extends BaseView implements
		IClasIoEventListener {

	protected TrajectoryTable _trajectoryTable;

	protected ClasIoEventManager _eventManager = ClasIoEventManager
			.getInstance();

	protected ClasIoTrajectoryInfoView(String title) {
		super(PropertySupport.TITLE, title, PropertySupport.ICONIFIABLE, true,
				PropertySupport.MAXIMIZABLE, true, PropertySupport.CLOSABLE, true,
				PropertySupport.RESIZABLE, true, PropertySupport.WIDTH, 1050,
				PropertySupport.HEIGHT, 350, PropertySupport.LEFT, 700,
				PropertySupport.TOP, 100, PropertySupport.VISIBLE, true);

		_trajectoryTable = new TrajectoryTable();
		add(_trajectoryTable.getScrollPane());

		// need to listen for events
		_eventManager.addClasIoEventListener(this, 1);
	}

	/**
	 * Get all the row data so the trajectory dialog can be updated.
	 * 
	 * @return a vector of TrajectoryRowData objects.
	 */
	protected abstract Vector<TrajectoryRowData> getRowData();
	
	/**
	 * Tests whether this listener is interested in events while accumulating
	 * @return <code>true</code> if this listener is NOT interested in  events while accumulating
	 */
	@Override
	public boolean ignoreIfAccumulating() {
		return true;
	}


}