package cnuphys.ced.clasio;

import java.util.Vector;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.log.Log;
import cnuphys.ced.alldata.DataManager;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.lund.TrajectoryRowData;
import cnuphys.lund.TrajectoryTableModel;

public class ClasIoReconEventView extends ClasIoTrajectoryInfoView {

	// singleton
	private static ClasIoReconEventView instance;

	// one row for each reconstructed trajectory
	private static Vector<TrajectoryRowData> _trajData = new Vector<TrajectoryRowData>();

	private ClasIoReconEventView() {
		super("Reconstructed Tracks");
	}

	/**
	 * Get the reconstructed event view
	 * 
	 * @return the reconstructed event view
	 */
	public static ClasIoReconEventView getInstance() {
		if (instance == null) {
			instance = new ClasIoReconEventView();
		}
		return instance;
	}

	@Override
	public Vector<TrajectoryRowData> getRowData() {
		return _trajData;
	}

	@Override
	public void newClasIoEvent(DataEvent event) {
		_trajectoryTable.clear(); // remove existing events
		_trajData.clear();

		if (!_eventManager.isAccumulating()) {

			// now fill the table.
			TrajectoryTableModel model = _trajectoryTable.getTrajectoryModel();

			addTracks(event, _trajData, "HitBasedTrkg::HBTracks");
			addTracks(event, _trajData, "TimeBasedTrkg::TBTracks");
			addTracks(event, _trajData, "REC::Particle");
			
			addTracks(event, _trajData, "HitBasedTrkg::AITracks");
			addTracks(event, _trajData, "TimeBasedTrkg::AITracks");
			
			// look for cvt tyracks
			addTracks(event, _trajData, "CVTRec::Tracks");
			addTracks(event, _trajData, "CVTRec::TracksCA");

			model.setData(_trajData);
			model.fireTableDataChanged();
			_trajectoryTable.repaint();
			_trajectoryTable.repaint();
		} // !accumulating
	}

	// add tracks
	private void addTracks(DataEvent event, Vector<TrajectoryRowData> data, String bankName) {
		try {

			// do we have any data?
			boolean hasBank = event.hasBank(bankName);
			if (!hasBank) {
				return;
			}

			if (bankName.contains("CVTRec::TracksCA")) {
				addCVTTracks(event, data, bankName);
				return;
			}
			if (bankName.contains("CVTRec::Tracks")) {
				addCVTTracks(event, data, bankName);
				return;
			}

			if (bankName.contains("REC::Particle")) {
				addRECParticleTracks(event, data, bankName);
				return;
			}

			boolean hitBased = bankName.contains("HitBased");
			
			
			DataManager dm = DataManager.getInstance();
			float[] vx = dm.getFloatArray(event, bankName + "." + "Vtx0_x"); // vertex x cm
			if ((vx != null) && (vx.length > 0)) {
				float[] vy = dm.getFloatArray(event, bankName + "." + "Vtx0_y"); // vertex y cm
				float[] vz = dm.getFloatArray(event, bankName + "." + "Vtx0_z"); // vertex z cm
				float px[] = dm.getFloatArray(event, bankName + "." + "p0_x");
				float py[] = dm.getFloatArray(event, bankName + "." + "p0_y");
				float pz[] = dm.getFloatArray(event, bankName + "." + "p0_z");
				byte q[] = dm.getByteArray(event, bankName + "." + "q");
				short status[] = dm.getShortArray(event, bankName + "." + "status");
				short id[] = dm.getShortArray(event, bankName + "." + "id");

				for (int i = 0; i < vx.length; i++) {

					LundId lid = (hitBased ? LundSupport.getHitbased(q[i]) : LundSupport.getTrackbased(q[i]));

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
					TrajectoryRowData row = new TrajectoryRowData(id[i], lid, xo, yo, zo, 1000 * p,
							Math.toDegrees(theta), Math.toDegrees(phi), status[i], bankName);
					data.add(row);

				}
			}
		} catch (Exception e) {
			String warning = "[ClasIoReconEventView.addTracks] " + e.getMessage();
			Log.getInstance().warning(warning);
		}
	}
	
	// add CVT reconstructed tracks
	private void addRECParticleTracks(DataEvent event, Vector<TrajectoryRowData> data, String bankName) {
		
		DataManager dm = DataManager.getInstance();
		
		try {
		float[] vx = dm.getFloatArray(event, bankName + "." + "vx"); // vertex x cm
		if ((vx != null) && (vx.length > 0)) {
			float[] vy = dm.getFloatArray(event, bankName + "." + "vy"); // vertex y cm
			float[] vz = dm.getFloatArray(event, bankName + "." + "vz"); // vertex z cm
			float px[] = dm.getFloatArray(event, bankName + "." + "px");
			float py[] = dm.getFloatArray(event, bankName + "." + "py");
			float pz[] = dm.getFloatArray(event, bankName + "." + "pz");
			byte charge[] = dm.getByteArray(event, bankName + "." + "charge");
				short status[] = dm.getShortArray(event, bankName + "." + "status");
				int pid[] = dm.getIntArray(event, bankName + "." + "pid");

				LundId lid;
				
				for (int i = 0; i < vx.length; i++) {

					if (pid[i] == 0) {
						if (charge[i] == -1) {
							lid = LundSupport.unknownMinus;
						}
						if (charge[i] == 1) {
							lid = LundSupport.unknownPlus;
						}
						else {
							lid = LundSupport.unknownNeutral;

						}
					} else {
						lid = LundSupport.getInstance().get(pid[i], charge[i]);
					}

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
				TrajectoryRowData row = new TrajectoryRowData(0, lid, xo, yo, zo, 1000 * p,
						Math.toDegrees(theta), Math.toDegrees(phi), status[i], bankName);
				data.add(row);

			}
		}
	} catch (Exception e) {
		String warning = "[ClasIoReconEventView.addTracks] " + e.getMessage();
		Log.getInstance().warning(warning);
	}
	}


	// add CVT reconstructed tracks
	private void addCVTTracks(DataEvent event, Vector<TrajectoryRowData> data, String bankName) {
		try {
			DataManager dm = DataManager.getInstance();
			byte q[] = dm.getByteArray(event, bankName + "." + "q");
			int count = (q == null) ? 0 : q.length;

			// System.err.println("Number of cvt tracks found: " + count);
			if (count > 0) {
				float pt[] = dm.getFloatArray(event, bankName + "." + "pt");
				float phi0[] = dm.getFloatArray(event, bankName + "." + "phi0");
				float d0[] = dm.getFloatArray(event, bankName + "." + "d0");
				float z0[] = dm.getFloatArray(event, bankName + "." + "z0");
				float tandip[] = dm.getFloatArray(event, bankName + "." + "tandip");
				short id[] = dm.getShortArray(event, bankName + "." + "ID");

				for (int i = 0; i < count; i++) {

					LundId lid = LundSupport.getCVTbased(q[i]);

					double xo = -d0[i] * Math.sin(phi0[i]);
					double yo = d0[i] * Math.cos(phi0[i]);
					double zo = z0[i];
					double pxo = pt[i] * Math.cos(phi0[i]);
					double pyo = pt[i] * Math.sin(phi0[i]);
					double pzo = pt[i] * tandip[i];

					double p = Math.sqrt(pxo * pxo + pyo * pyo + pzo * pzo);
					double theta = Math.acos(pzo / p);
					TrajectoryRowData row = new TrajectoryRowData(id[i], lid, xo, yo, zo, 1000 * p,
							Math.toDegrees(theta), Math.toDegrees(phi0[i]), 0, bankName);
					data.add(row);
				}
			}

//			X_vtx = -d0*sin(phi0)
//			Y_vtx = d0*cos(phi0)
//			Z_vtx = z0
//			Px_vtx = pt*cos(phi0)
//			Py_vtx = pt*sin(phi0)
//			Pz_vtx = pt*tandip			

		} catch (Exception e) {
			String warning = "[ClasIoReconEventView.addCVTTracks] " + e.getMessage();
			Log.getInstance().warning(warning);
		}
	}

	@Override
	public void openedNewEventFile(String path) {
	}

	/**
	 * Change the event source type
	 * 
	 * @param source the new source: File, ET
	 */
	@Override
	public void changedEventSource(ClasIoEventManager.EventSourceType source) {
	}

}
