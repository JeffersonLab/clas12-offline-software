package cnuphys.ced.magfield;

import java.util.Vector;

import cnuphys.bCNU.magneticfield.swim.ISwimAll;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.ClasIoReconEventView;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.lund.TrajectoryRowData;
import cnuphys.rk4.RungeKuttaException;
import cnuphys.swim.DefaultSwimStopper;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.Swimmer;
import cnuphys.swim.Swimming;

/**
 * Swims all the particles in the Recon bank
 * 
 * @author heddle
 * 
 */
public class SwimAllRecon implements ISwimAll {

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
		return ClasIoReconEventView.getInstance().getRowData();
	}

	/**
	 * Swim all Monte Carlo particles
	 * 
	 * @param manager
	 *            the swim manager
	 */
	@Override
	public void swimAll() {
		// System.err.println("SWIM ALL RECON");
		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}
		
		Swimming.clearReconTrajectories();

		Vector<TrajectoryRowData> data = getRowData();
		if (data == null) {
			return;
		}
		// System.err.println("SWIM " + data.size() + "  recon trax");

		Swimmer swimmer = new Swimmer();
		double stepSize = 5e-4; // m
		DefaultSwimStopper stopper = new DefaultSwimStopper(RMAX);

		for (TrajectoryRowData trd : data) {
			LundId lid = LundSupport.getInstance().get(trd.getId());
		

			if (lid != null) {
				SwimTrajectory traj;
				try {
					traj = swimmer.swim(lid.getCharge(), trd.getXo() / 100,
							trd.getYo() / 100, trd.getZo() / 100,
							trd.getMomentum() / 1000, trd.getTheta(),
							trd.getPhi(), stopper, 0, PATHMAX, stepSize,
							Swimmer.CLAS_Tolerance, null);
					traj.setLundId(lid);
					traj.setSource(trd.getSource());
					Swimming.addReconTrajectory(traj);
				} catch (RungeKuttaException e) {
					e.printStackTrace();
				}
			}

		}

	}
}
