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

	//one row for each reconstructed trajectory
	private static Vector<TrajectoryRowData> _trajData = new Vector<TrajectoryRowData>(25);

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
			addTracks(event, _trajData, "CVTRec::Tracks");

			model.setData(_trajData);
			model.fireTableDataChanged();
			_trajectoryTable.repaint();
			_trajectoryTable.repaint();
		} // !accumulating
	}
	
	//add tracks
	private void addTracks(DataEvent event, Vector<TrajectoryRowData> data, String bankName) {
		try {
			
			//treqt CVT  tracks separately
			if (bankName.contains("CVTRec")) {
				addCVTTracks(event, data, bankName);
				return;
			}
			
			boolean hitBased = bankName.contains("HitBased");
			DataManager dm = DataManager.getInstance();
			float[] vx = dm.getFloatArray(event, bankName + "." + "Vtx0_x"); //vertex x cm
			if ((vx != null) && (vx.length > 0)) {
				float[] vy = dm.getFloatArray(event, bankName + "." + "Vtx0_y"); //vertex y cm
				float[] vz = dm.getFloatArray(event, bankName + "." + "Vtx0_z"); //vertex z cm
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
					TrajectoryRowData row = new TrajectoryRowData(id[i], lid, xo, yo, zo, 1000 * p, Math.toDegrees(theta),
							Math.toDegrees(phi), status[i], bankName);
					data.add(row);
					
				}
			}
		}
		catch (Exception e) {
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
			
			System.err.println("Number of cvt tracks found: " + count);
			if (count > 0) {
				float p[] = dm.getFloatArray(event, bankName + "." + "p");
			}

		}
		catch (Exception e) {
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
	 * @param source
	 *            the new source: File, ET, FastMC
	 */
	@Override
	public void changedEventSource(ClasIoEventManager.EventSourceType source) {
	}

}
