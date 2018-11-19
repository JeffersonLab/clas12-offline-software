package cnuphys.fastMCed.view.trajinfo;

import java.util.Vector;

import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.view.BaseView;
import cnuphys.fastMCed.eventio.IPhysicsEventListener;
import cnuphys.fastMCed.eventio.PhysicsEventManager;
import cnuphys.lund.TrajectoryRowData;
import cnuphys.lund.TrajectoryTable;

public abstract class ATrajectoryInfoView extends BaseView implements
		IPhysicsEventListener {

	protected TrajectoryTable _trajectoryTable;

	protected PhysicsEventManager _eventManager = PhysicsEventManager
			.getInstance();

	protected ATrajectoryInfoView(String title) {
		super(PropertySupport.TITLE, title, PropertySupport.ICONIFIABLE, true,
				PropertySupport.TOOLBAR, false,
				PropertySupport.MAXIMIZABLE, true, PropertySupport.CLOSABLE, true,
				PropertySupport.RESIZABLE, true, PropertySupport.WIDTH, 1050,
				PropertySupport.HEIGHT, 350, PropertySupport.LEFT, 700,
				PropertySupport.TOP, 100, PropertySupport.VISIBLE, true);

		_trajectoryTable = new TrajectoryTable();
		add(_trajectoryTable.getScrollPane());

		// need to listen for events
		_eventManager.addPhysicsListener(this, 1);
	}

	/**
	 * Get all the row data so the trajectory dialog can be updated.
	 * 
	 * @return a vector of TrajectoryRowData objects.
	 */
	protected abstract Vector<TrajectoryRowData> getRowData();
	
}