package cnuphys.bCNU.view;

import java.util.Vector;

import cnuphys.bCNU.attributes.AttributeType;
import cnuphys.bCNU.event.EventControl;
import cnuphys.bCNU.event.IPhysicsEventListener;
import cnuphys.bCNU.view.BaseView;
import cnuphys.lund.TrajectoryRowData;
import cnuphys.lund.TrajectoryTable;

@SuppressWarnings("serial")
public abstract class TrajectoryInfoView extends BaseView implements
		IPhysicsEventListener {

	protected TrajectoryTable _trajectoryTable;

	protected TrajectoryInfoView(String title) {
		super(AttributeType.TITLE, title, AttributeType.ICONIFIABLE, true,
				AttributeType.MAXIMIZABLE, true, AttributeType.CLOSABLE, true,
				AttributeType.RESIZABLE, true, AttributeType.WIDTH, 760,
				AttributeType.HEIGHT, 250, AttributeType.LEFT, 700,
				AttributeType.TOP, 100, AttributeType.VISIBLE, true);

		_trajectoryTable = new TrajectoryTable();
		add(_trajectoryTable.getScrollPane());

		// need to listen for events
		EventControl.getInstance().addBeforeViewPhysicsListener(this);
	}

	/**
	 * Get all the row data so the trajectory dialog can be updated.
	 * 
	 * @return a vector of TrajectoryRowData objects.
	 */
	protected abstract Vector<TrajectoryRowData> getRowData();

}
