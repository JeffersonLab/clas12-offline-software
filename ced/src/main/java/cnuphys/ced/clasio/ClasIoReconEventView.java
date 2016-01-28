package cnuphys.ced.clasio;

import java.util.Vector;

import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.bCNU.log.Log;
import cnuphys.ced.event.data.ColumnData;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.lund.TrajectoryRowData;
import cnuphys.lund.TrajectoryTableModel;

public class ClasIoReconEventView extends ClasIoTrajectoryInfoView {

	// singleton
	private static ClasIoReconEventView instance;

	private static Vector<TrajectoryRowData> data = new Vector<TrajectoryRowData>(
			25);

	private ClasIoReconEventView() {
		super("Reconstructed Tracks");
	}

	public static ClasIoReconEventView getInstance() {
		if (instance == null) {
			instance = new ClasIoReconEventView();
		}
		return instance;
	}

	@Override
	public Vector<TrajectoryRowData> getRowData() {
		return data;
	}

	@Override
	public void newClasIoEvent(EvioDataEvent event) {
		_trajectoryTable.clear(); // remove existing events
		data.clear();

		if (!_eventManager.isAccumulating()) {

			// now fill the table.
			TrajectoryTableModel model = _trajectoryTable.getTrajectoryModel();

			int pid[] = ColumnData.getIntArray("EVENTHB::particle.pid");
			int numTracks = (pid == null) ? 0 : pid.length;

			if (numTracks > 0) {
				for (int i = 0; i < numTracks; i++) {

					double vx[] = ColumnData
							.getDoubleArray("EVENTHB::particle.vx");
					double vy[] = ColumnData
							.getDoubleArray("EVENTHB::particle.vy");
					double vz[] = ColumnData
							.getDoubleArray("EVENTHB::particle.vz");
					double px[] = ColumnData
							.getDoubleArray("EVENTHB::particle.px");
					double py[] = ColumnData
							.getDoubleArray("EVENTHB::particle.py");
					double pz[] = ColumnData
							.getDoubleArray("EVENTHB::particle.pz");
					LundId lid = LundSupport.getInstance().get(pid[i]);

					if (lid != null) {
						double xo = vx[i]; // cm
						double yo = vy[i]; // cm
						double zo = vz[i]; // cm

						double pxo = px[i]; // GeV/c
						double pyo = py[i];
						double pzo = pz[i];

						double p = Math.sqrt(pxo * pxo + pyo * pyo + pzo * pzo); // GeV
						double phi = Math.atan2(pyo, pxo);
						double theta = Math.acos(pzo / p);

						// note conversions to degrees and MeV
						TrajectoryRowData row = new TrajectoryRowData(lid, xo,
								yo, zo, 1000 * p, Math.toDegrees(theta),
								Math.toDegrees(phi));
						data.add(row);
					}
					else {
						Log.getInstance().warning(
								"Bad pid: " + pid[i] + " in ClasIoReconEventView");
					}

					// public TrajectoryRowData(LundId lundId, double xo,
					// double yo, double zo,
					// double p, double theta, double phi) {

				}

				model.setData(data);
				model.fireTableDataChanged();
				_trajectoryTable.repaint();
			} //numTracks > 0
		} // !accumulating
	}

	@Override
	public void openedNewEventFile(String path) {
	}

}
