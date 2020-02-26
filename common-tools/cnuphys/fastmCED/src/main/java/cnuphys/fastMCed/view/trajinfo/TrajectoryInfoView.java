package cnuphys.fastMCed.view.trajinfo;

import java.util.List;
import java.util.Vector;

import org.jlab.clas.physics.PhysicsEvent;

import cnuphys.bCNU.magneticfield.swim.ISwimAll;
import cnuphys.fastMCed.eventgen.AEventGenerator;
import cnuphys.fastMCed.fastmc.ParticleHits;
import cnuphys.fastMCed.streaming.StreamManager;
import cnuphys.lund.TrajectoryRowData;
import cnuphys.lund.TrajectoryTableModel;

@SuppressWarnings("serial")
public class TrajectoryInfoView extends ATrajectoryInfoView {

	public TrajectoryInfoView() {
		super("Lund Tracks");
	}

	@Override
	protected Vector<TrajectoryRowData> getRowData() {
		return null;
	}
	

	@Override
	public void newPhysicsEvent(PhysicsEvent event, List<ParticleHits> particleHits) {
		_trajectoryTable.clear(); // remove existing events

		if (StreamManager.getInstance().isStarted()) {
			return;
		}
		
		ISwimAll allSwimmer = _eventManager.getAllSwimmer();
		if (allSwimmer != null) {
			TrajectoryTableModel model = _trajectoryTable
					.getTrajectoryModel();
			model.setData(allSwimmer.getRowData());
			model.fireTableDataChanged();
			_trajectoryTable.repaint();
		}
	}


	/**
	 * A new event generator is active
	 * @param generator the now active generator
	 */
	@Override
	public void newEventGenerator(final AEventGenerator generator) {
	}

}