package cnuphys.ced.clasio;

import java.util.Vector;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.magneticfield.swim.ISwimAll;
import cnuphys.ced.alldata.DataManager;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.lund.TrajectoryRowData;
import cnuphys.lund.TrajectoryTableModel;

@SuppressWarnings("serial")
public class ClasIoMonteCarloView extends ClasIoTrajectoryInfoView {
	
	// singleton
	private static ClasIoMonteCarloView instance;
	
	// one row for each reconstructed trajectory
	private static Vector<TrajectoryRowData> _trajData = new Vector<TrajectoryRowData>();


	private ClasIoMonteCarloView() {
		super("Monte Carlo Tracks");
	}
	
	/**
	 * Get the monte carlo event view
	 * 
	 * @return the monte carlo event view
	 */
	public static ClasIoMonteCarloView getInstance() {
		if (instance == null) {
			instance = new ClasIoMonteCarloView();
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

		if (!_eventManager.isAccumulating()) {

			// now fill the table.
			TrajectoryTableModel model = _trajectoryTable.getTrajectoryModel();

			addTracks(event, _trajData, "MC::Particle");
			addTracks(event, _trajData, "MC::Lund");

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

			
			DataManager dm = DataManager.getInstance();
			
			
			
			float[] vx = dm.getFloatArray(event, bankName + ".vx"); // vertex x cm
			if ((vx != null) && (vx.length > 0)) {
				float[] vy = dm.getFloatArray(event, bankName + ".vy"); // vertex y cm
				float[] vz = dm.getFloatArray(event, bankName + ".vz"); // vertex z cm
				float px[] = dm.getFloatArray(event, bankName + ".px");
				float py[] = dm.getFloatArray(event, bankName + ".py");
				float pz[] = dm.getFloatArray(event, bankName + ".pz");
				byte q[] = dm.getByteArray(event, bankName + "." + "q");
				int pid[] = dm.getIntArray(event, bankName + ".pid");

				for (int i = 0; i < vx.length; i++) {

					LundId lid = LundSupport.getInstance().get(pid[i]);
					
					if (lid == null) {
						//can't swim if don't know the charge!
						System.err.println("Cannot swim unknown LundID: " + pid[i]);
						continue;
					}

					double xo = vx[i]; // cm
					double yo = vy[i]; // cm
					double zo = vz[i]; // cm

					double pxo = px[i]; // GeV/c
					double pyo = py[i];
					double pzo = pz[i];

					double p = Math.sqrt(pxo * pxo + pyo * pyo + pzo * pzo); // GeV/c
					
					if (p < 1.0e-3) {
						System.err.println(String.format("Skipping extremely low momentum track %-7.3f GeV/c for %s", 
								p, lid.getName()));
						continue;
					}
					double phi = Math.atan2(pyo, pxo);
					double theta = Math.acos(pzo / p);

					// note conversions to degrees and MeV
					TrajectoryRowData row = new TrajectoryRowData(i, lid, xo, yo, zo, 1000 * p,
							Math.toDegrees(theta), Math.toDegrees(phi), 0, bankName);
					data.add(row);

				}
			}
		} catch (Exception e) {
			String warning = "[ClasIoMonteCarloEventView.addTracks] " + e.getMessage();
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
