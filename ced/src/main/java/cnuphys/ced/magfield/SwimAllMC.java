package cnuphys.ced.magfield;

import java.util.Vector;

import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.magneticfield.swim.ISwimAll;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.data.GenPartDataContainer;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.lund.TrajectoryRowData;
import cnuphys.swim.DefaultSwimStopper;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.Swimmer;
import cnuphys.swim.Swimming;

/**
 * Swims all the particles in the MC bank
 * 
 * @author heddle
 * 
 */
public class SwimAllMC implements ISwimAll {

	// convenience reference to event manager
	private static ClasIoEventManager _eventManager = ClasIoEventManager
			.getInstance();

	// integration cutoff
	private static final double RMAX = 10.0;
	private static final double PATHMAX = 10.0;

	/**
	 * Get all the row data so the trajectory dialog can be updated.
	 * 
	 * @param manager
	 *            the swim manager
	 * @return a vector of TrajectoryRowData objects.
	 */
	@Override
	public Vector<TrajectoryRowData> getRowData() {

		GenPartDataContainer mcGemcData = _eventManager.getGenPartData();
		if ((mcGemcData == null) || (mcGemcData.genpart_true_px == null)) {
			return null;
		}

		int pid[] = mcGemcData.genpart_true_pid;

		int len = (pid == null) ? 0 : pid.length;
		if (len < 1) {
			return null;
		}
		Vector<TrajectoryRowData> v = new Vector<TrajectoryRowData>(len);

		try {

			for (int index = 0; index < len; index++) {
				LundId lid = LundSupport.getInstance().get(pid[index]);

				if (lid != null) {
					double px = mcGemcData.genpart_true_px[index]; // leave in
					// MeV
					double py = mcGemcData.genpart_true_py[index]; // leave in
					// MeV
					double pz = mcGemcData.genpart_true_pz[index]; // leave in
					// MeV

					// note conversions from mm to cm
					double x = mcGemcData.genpart_true_vx[index] / 10.0;
					double y = mcGemcData.genpart_true_vy[index] / 10.0;
					double z = mcGemcData.genpart_true_vz[index] / 10.0;

					double p = Math.sqrt(px * px + py * py + pz * pz);
					double theta = Math.toDegrees(Math.acos(pz / p));
					double phi = Math.toDegrees(Math.atan2(py, px));

					v.add(new TrajectoryRowData(lid, x, y, z, p, theta, phi));
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.getInstance().error(
					"Exception while load MC records into table");
			Log.getInstance().exception(e);
			return null;
		}

		return v;
	}

	/**
	 * Swim all Monte Carlo particles
	 * 
	 * @param manager
	 *            the swim manager
	 */
	@Override
	public void swimAll() {

		// System.err.println("SWIM ALL MC");
		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}

		Swimming.clearMCTrajectories(); // clear all existing trajectories

		GenPartDataContainer mcGemcData = _eventManager.getGenPartData();
		if ((mcGemcData == null) || (mcGemcData.genpart_true_px == null)) {
			return;
		}

		int pid[] = mcGemcData.genpart_true_pid;

		int len = (pid == null) ? 0 : pid.length;
		if (len < 1) {
			return;
		}

		try {

			for (int index = 0; index < pid.length; index++) {
				int pdgid = pid[index];
				LundId lid = LundSupport.getInstance().get(pdgid);

				if (lid == null) {
					System.err.println("null LundId object for id: " + pdgid);
				} else {
					// System.err.println("SWIM particle " + lid);

					// covert momenta to GeV/c from MeV/c
					double px = mcGemcData.genpart_true_px[index] / 1000.0;
					double py = mcGemcData.genpart_true_py[index] / 1000.0;
					double pz = mcGemcData.genpart_true_pz[index] / 1000.0;
					// note vertices are in mm must convert to meters
					double x = mcGemcData.genpart_true_vx[index] / 1000.0;
					double y = mcGemcData.genpart_true_vy[index] / 1000.0;
					double z = mcGemcData.genpart_true_vz[index] / 1000.0;

					double p = Math.sqrt(px * px + py * py + pz * pz);
					double theta = Math.toDegrees(Math.acos(pz / p));
					double phi = Math.toDegrees(Math.atan2(py, px));

					// Swimming.swim(lid.getCharge(), x, y, z, p, theta, phi,
					// RMAX, PATHMAX);

					Swimmer swimmer = Swimming.getSwimmer();
					double stepSize = 5e-4; // m
					DefaultSwimStopper stopper = new DefaultSwimStopper(RMAX);

					// System.err.println("swim vertex: (" + x + ", " + y + ", "
					// + z + ")");
					SwimTrajectory traj = swimmer.swim(lid.getCharge(), x, y,
							z, p, theta, phi, stopper, PATHMAX, stepSize,
							Swimmer.CLAS_Tolerance, null);
					traj.setLundId(lid);
					Swimming.addMCTrajectory(traj);
				} // lid != null

			}
		} catch (Exception e) {
			Log.getInstance()
					.error("Exception while swimming all MC particles");
			Log.getInstance().exception(e);
			return;
		}

	}
}