package cnuphys.bCNU.view;

import java.util.Vector;

import cnuphys.bCNU.event.EventControl;
import cnuphys.bCNU.magneticfield.swim.ISwimAll;
import cnuphys.lund.TrajectoryRowData;
import cnuphys.lund.TrajectoryTableModel;

import org.jlab.coda.jevio.EvioEvent;

@SuppressWarnings("serial")
public class MonteCarloView extends TrajectoryInfoView {

	public MonteCarloView() {
		super("Monte Carlo Events");
	}

	@Override
	public void newPhysicsEvent(EvioEvent event) {
		_trajectoryTable.clear(); // remove existing events

		// now fill the table.
		if (!EventControl.getInstance().isAccumulating()) {
			ISwimAll allSwimmer = EventControl.getInstance().getAllSwimmer();
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
	protected Vector<TrajectoryRowData> getRowData() {
		return null;
	}

}
