package cnuphys.ced.magfield;

import java.util.Vector;

import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.magneticfield.swim.ISwimAll;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.data.ColumnData;
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

		

		int pid[] = ColumnData.getIntArray("GenPart::true.pid");

		int len = (pid == null) ? 0 : pid.length;
		if (len < 1) {
			return null;
		}
		
		double px[] = ColumnData.getDoubleArray("GenPart::true.px");
		if (px == null) {
			return null;
		}
		double py[] = ColumnData.getDoubleArray("GenPart::true.py");
		double pz[] = ColumnData.getDoubleArray("GenPart::true.pz");
		double vx[] = ColumnData.getDoubleArray("GenPart::true.vx");
		double vy[] = ColumnData.getDoubleArray("GenPart::true.vy");
		double vz[] = ColumnData.getDoubleArray("GenPart::true.vz");
		
		
		Vector<TrajectoryRowData> v = new Vector<TrajectoryRowData>(len);

		try {

			for (int index = 0; index < len; index++) {
				LundId lid = LundSupport.getInstance().get(pid[index]);

				if (lid != null) {
					double pxo = px[index]; // leave in  MeV
					double pyo = py[index]; 
					double pzo = pz[index]; 

					// note conversions from mm to cm
					double x = vx[index] / 10.0;
					double y = vy[index] / 10.0;
					double z = vz[index] / 10.0;

					double p = Math.sqrt(pxo * pxo + pyo * pyo + pzo * pzo);
					double theta = Math.toDegrees(Math.acos(pzo / p));
					double phi = Math.toDegrees(Math.atan2(pyo, pxo));

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
		
		int pid[] = ColumnData.getIntArray("GenPart::true.pid");

		int len = (pid == null) ? 0 : pid.length;
		if (len < 1) {
			return;
		}
		
		double px[] = ColumnData.getDoubleArray("GenPart::true.px");
		if (px == null) {
			return;
		}
		double py[] = ColumnData.getDoubleArray("GenPart::true.py");
		double pz[] = ColumnData.getDoubleArray("GenPart::true.pz");
		double vx[] = ColumnData.getDoubleArray("GenPart::true.vx");
		double vy[] = ColumnData.getDoubleArray("GenPart::true.vy");
		double vz[] = ColumnData.getDoubleArray("GenPart::true.vz");


		try {

			for (int index = 0; index < pid.length; index++) {
				int pdgid = pid[index];
				LundId lid = LundSupport.getInstance().get(pdgid);

				if (lid == null) {
					System.err.println("null LundId object for id: " + pdgid);
				} else {
					// System.err.println("SWIM particle " + lid);

					// covert momenta to GeV/c from MeV/c
					double pxo = px[index] / 1000.0;
					double pyo = py[index] / 1000.0;
					double pzo = pz[index] / 1000.0;
					// note vertices are in mm must convert to meters
					double x = vx[index] / 1000.0;
					double y = vy[index] / 1000.0;
					double z = vz[index] / 1000.0;

					double p = Math.sqrt(pxo * pxo + pyo * pyo + pzo * pzo);
					double theta = Math.toDegrees(Math.acos(pzo / p));
					double phi = Math.toDegrees(Math.atan2(pyo, pxo));

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