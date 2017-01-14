package cnuphys.ced.clasio;

import java.util.Vector;

import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.magneticfield.swim.ISwimAll;
import cnuphys.lund.TrajectoryRowData;
import cnuphys.lund.TrajectoryTableModel;

public class ClasIoMonteCarloView extends ClasIoTrajectoryInfoView {

	public ClasIoMonteCarloView() {
		super("Monte Carlo Events");
	}

	@Override
	protected Vector<TrajectoryRowData> getRowData() {
		return null;
	}
	
	/**
	 * New fast mc event
	 * @param event the generated physics event
	 */
	public void newFastMCGenEvent(PhysicsEvent event) {
		_trajectoryTable.clear(); // remove existing events

		// now fill the table.
		if (!_eventManager.isAccumulating()) {
			ISwimAll allSwimmer = _eventManager.getMCSwimmer();
			if (allSwimmer != null) {
				TrajectoryTableModel model = _trajectoryTable
						.getTrajectoryModel();
				model.setData(allSwimmer.getRowData());
				model.fireTableDataChanged();
				_trajectoryTable.repaint();
			}
		}
	}


	@Override
	public void newClasIoEvent(DataEvent event) {
		_trajectoryTable.clear(); // remove existing events

		// now fill the table.
		if (!_eventManager.isAccumulating()) {
			ISwimAll allSwimmer = _eventManager.getMCSwimmer();
			if (allSwimmer != null) {
				TrajectoryTableModel model = _trajectoryTable
						.getTrajectoryModel();
				model.setData(allSwimmer.getRowData());
				model.fireTableDataChanged();
				_trajectoryTable.repaint();
			}
		}
	}

	@Override
	public void openedNewEventFile(String path) {
	}

	/**
	 * Change the event source type
	 * @param source the new source: File, ET, FastMC
	 */
	@Override
	public void changedEventSource(ClasIoEventManager.EventSourceType source) {
	}

}
