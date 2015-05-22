package cnuphys.ced.clasio;

import java.util.Vector;

import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.ced.event.data.RecEventDataContainer;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.lund.TrajectoryRowData;
import cnuphys.lund.TrajectoryTableModel;

public class ClasIoReconEventView extends ClasIoTrajectoryInfoView {

    // singleton
    private static ClasIoReconEventView instance;

    private static Vector<TrajectoryRowData> data = new Vector<TrajectoryRowData>(
	    25);

    // convenient access to the event manager
    protected ClasIoEventManager _eventManager = ClasIoEventManager
	    .getInstance();

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
	    if (!_eventManager.isAccumulating()) {
		RecEventDataContainer recData = _eventManager
			.getReconEventData();
		TrajectoryTableModel model = _trajectoryTable
			.getTrajectoryModel();
		int numTracks = recData.getHitCount(0);

		if (numTracks > 0) {
		    for (int i = 0; i < numTracks; i++) {
			int pid = recData.eventhb_particle_pid[i];
			LundId lid = LundSupport.getInstance().get(pid);

			double xo = recData.eventhb_particle_vx[i]; // cm
			double yo = recData.eventhb_particle_vy[i]; // cm
			double zo = recData.eventhb_particle_vz[i]; // cm

			double px = recData.eventhb_particle_px[i]; // GeV/c
			double py = recData.eventhb_particle_py[i];
			double pz = recData.eventhb_particle_pz[i];

			double p = Math.sqrt(px * px + py * py + pz * pz); // GeV
			double phi = Math.atan2(py, px);
			double theta = Math.acos(pz / p);

			// note conversions to degrees and MeV
			TrajectoryRowData row = new TrajectoryRowData(lid, xo,
				yo, zo, 1000 * p, Math.toDegrees(theta),
				Math.toDegrees(phi));
			data.add(row);

			// public TrajectoryRowData(LundId lundId, double xo,
			// double yo, double zo,
			// double p, double theta, double phi) {

		    }

		}
		model.setData(data);
		model.fireTableDataChanged();
		_trajectoryTable.repaint();
	    }
	}
    }

    @Override
    public void openedNewEventFile(String path) {
    }

}
