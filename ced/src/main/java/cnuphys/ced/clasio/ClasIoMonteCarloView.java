package cnuphys.ced.clasio;

import java.util.Vector;

import org.jlab.evio.clas12.EvioDataEvent;

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

	@Override
	public void newClasIoEvent(EvioDataEvent event) {
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

}
