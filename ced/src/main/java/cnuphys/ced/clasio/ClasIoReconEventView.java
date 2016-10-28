package cnuphys.ced.clasio;

import java.util.Vector;

import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.io.evio.EvioDataEvent;

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

	/**
	 * New fast mc event
	 * @param event the generated physics event
	 */
	public void newFastMCGenEvent(PhysicsEvent event) {
		_trajectoryTable.clear(); // remove existing events
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
				float vx[] = ColumnData
						.getFloatArray("EVENTHB::particle.vx");
				float vy[] = ColumnData
						.getFloatArray("EVENTHB::particle.vy");
				float vz[] = ColumnData
						.getFloatArray("EVENTHB::particle.vz");
				float px[] = ColumnData
						.getFloatArray("EVENTHB::particle.px");
				float py[] = ColumnData
						.getFloatArray("EVENTHB::particle.py");
				float pz[] = ColumnData
						.getFloatArray("EVENTHB::particle.pz");
				int charge[] = ColumnData
						.getIntArray("EVENTHB::particle.charge");
				int status[] = ColumnData
						.getIntArray("EVENTHB::particle.status");


				for (int i = 0; i < numTracks; i++) {

					// hack for 0 pid
					int thePid = pid[i];
					if (thePid == 0) {
						int q = charge[i];
						if (q == -1) {
							thePid = 11;
						}
						else if (q == 1)  {
							thePid = 0; //Geantino+
						}
						else {
							thePid = 22;  //photon
						}
					}
					LundId lid = LundSupport.getInstance().get(thePid);

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
								Math.toDegrees(phi), status[i], "EVENTHB::particle");
						data.add(row);
					}
					else {
						Log.getInstance().warning(
								"Bad pid: " + pid[i] + " in ClasIoReconEventView");
					}					
				} //loop over num hb tracks

				model.setData(data);
				model.fireTableDataChanged();
				_trajectoryTable.repaint();
			} //numTracks > 0
			
			
			//cvt tracks?
			int q[] = ColumnData.getIntArray("CVTRec::Tracks.q");

			numTracks = (q == null) ? 0 : q.length;

			if (numTracks > 0) {
				double zz0[] = ColumnData
						.getDoubleArray("CVTRec::Tracks.z0");
				double d0[] = ColumnData
						.getDoubleArray("CVTRec::Tracks.d0");
				double phi0[] = ColumnData
						.getDoubleArray("CVTRec::Tracks.phi0");
				double tandip[] = ColumnData
						.getDoubleArray("CVTRec::Tracks.tandip");
				double pt[] = ColumnData
						.getDoubleArray("CVTRec::Tracks.pt");

				
				for (int i = 0; i < numTracks; i++) {
					int thePid = 22;  //photon
					if (q[i] > 0) {
						thePid = 2212;  //assume proton for pos charge
					}
					else if (q[i] < 0) {
						thePid = 11; //assume electron for neg charge
					}
					
					LundId lid = LundSupport.getInstance().get(thePid);

					
					//convert mm to cm
					double x0 = d0[i]*Math.cos(phi0[i])/10;
					double y0 = d0[i]*Math.sin(phi0[i])/10;
					double z0 = zz0[i]/10;
					
					double px = pt[i]*Math.cos(phi0[i]);
					double py = pt[i]*Math.sin(phi0[i]);
					double pz = pt[i]*tandip[i];
					
					double p = Math.sqrt(px * px + py * py + pz * pz); // GeV
					double phi = Math.atan2(py, px);
					double theta = Math.acos(pz / p);

					// note conversions to degrees and MeV
					TrajectoryRowData row = new TrajectoryRowData(lid, x0,
							y0, z0, 1000 * p, Math.toDegrees(theta),
							Math.toDegrees(phi), 0, "CVTRec::Tracks");
					data.add(row);


				}
				
				model.setData(data);
				model.fireTableDataChanged();
				_trajectoryTable.repaint();

			} //num tracks > 0

		} // !accumulating
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
