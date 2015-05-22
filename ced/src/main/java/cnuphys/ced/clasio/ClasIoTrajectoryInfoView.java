package cnuphys.ced.clasio;

import java.util.Vector;

import cnuphys.bCNU.attributes.AttributeType;
import cnuphys.bCNU.view.BaseView;
import cnuphys.lund.TrajectoryRowData;
import cnuphys.lund.TrajectoryTable;

public abstract class ClasIoTrajectoryInfoView extends BaseView implements
	IClasIoEventListener {

    protected TrajectoryTable _trajectoryTable;

    protected ClasIoEventManager _eventManager = ClasIoEventManager
	    .getInstance();

    protected ClasIoTrajectoryInfoView(String title) {
	super(AttributeType.TITLE, title, AttributeType.ICONIFIABLE, true,
		AttributeType.MAXIMIZABLE, true, AttributeType.CLOSABLE, true,
		AttributeType.RESIZABLE, true, AttributeType.WIDTH, 760,
		AttributeType.HEIGHT, 250, AttributeType.LEFT, 700,
		AttributeType.TOP, 100, AttributeType.VISIBLE, true);

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