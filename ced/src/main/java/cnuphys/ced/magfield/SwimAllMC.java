package cnuphys.ced.magfield;

import java.util.Vector;

import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.magneticfield.swim.ISwimAll;
import cnuphys.ced.alldata.DataManager;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.fastmc.FastMCManager;
import cnuphys.ced.fastmc.StreamTimer;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.lund.TrajectoryRowData;
import cnuphys.magfield.MagneticFields;
import cnuphys.rk4.RungeKuttaException;
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

	//get row data from FastMC event 
	private Vector<TrajectoryRowData> getRowDataFastMC() {
		PhysicsEvent event = FastMCManager.getInstance().getCurrentGenEvent();
		if ((event == null) || (event.count() < 1)) {
			return null;
		}
		
		Vector<TrajectoryRowData> v = new Vector<TrajectoryRowData>(event.count());
		
		for (int index = 0; index < event.count();  index++) {
			Particle particle = event.getParticle(index);
			LundId lid = LundSupport.getInstance().get(particle.pid());
			double pxo = particle.px()*1000.; //convert to MeV
			double pyo = particle.py()*1000.; //convert to MeV
			double pzo = particle.pz()*1000.; //convert to MeV

			// note conversions from mm to cm
			double x = particle.vertex().x(); //leave in cm
			double y = particle.vertex().y(); //leave in cm
			double z = particle.vertex().z(); //leave in cm

			double p = Math.sqrt(pxo * pxo + pyo * pyo + pzo * pzo);
			double theta = Math.toDegrees(Math.acos(pzo / p));
			double phi = Math.toDegrees(Math.atan2(pyo, pxo));

			v.add(new TrajectoryRowData(index, lid, x, y, z, p, theta, phi, 0, "LUND File"));
		}

		return v;
	}
	
	/**
	 * Get all the row data so the trajectory dialog can be updated.
	 * 
	 * @param manager
	 *            the swim manager
	 * @return a vector of TrajectoryRowData objects.
	 */
	@Override
	public Vector<TrajectoryRowData> getRowData() {
		
		DataEvent event = ClasIoEventManager.getInstance().getCurrentEvent();
		if (event == null) {
			return null;
		}
		DataManager dm = DataManager.getInstance();

		if (ClasIoEventManager.getInstance().isSourceFastMC()) {
			return getRowDataFastMC();
		}

		int pid[] = dm.getIntArray(event, "MC::Particle.pid");

		int len = (pid == null) ? 0 : pid.length;
		if (len < 1) {
			return null;
		}
		
		float px[] = dm.getFloatArray(event, "MC::Particle.px");
		if (px == null) {
			return null;
		}
		float py[] = dm.getFloatArray(event, "MC::Particle.py");
		float pz[] = dm.getFloatArray(event, "MC::Particle.pz");
		float vx[] = dm.getFloatArray(event, "MC::Particle.vx");
		float vy[] = dm.getFloatArray(event, "MC::Particle.vy");
		float vz[] = dm.getFloatArray(event, "MC::Particle.vz");
				
		Vector<TrajectoryRowData> v = new Vector<TrajectoryRowData>(len);

		try {

			for (int index = 0; index < len; index++) {
				LundId lid = LundSupport.getInstance().get(pid[index]);

				if (lid != null) {
					double pxo = 1000*px[index]; // Convert to MeV
					double pyo = 1000*py[index];
					double pzo = 1000*pz[index];
					double p = Math.sqrt(pxo * pxo + pyo * pyo + pzo * pzo);
					double theta = Math.toDegrees(Math.acos(pzo / p));
					// filter out 0 theta
					if (theta > 1.) {

						// note conversions from mm to cm
						double x = vx[index] / 10.0;
						double y = vy[index] / 10.0;
						double z = vz[index] / 10.0;

						double phi = Math.toDegrees(Math.atan2(pyo, pxo));

						v.add(new TrajectoryRowData(index, lid, x, y, z, p, theta, phi, 0, "MC::Particle"));
					}
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

	private void SwimAllFastMC() {
		Swimming.clearMCTrajectories(); // clear all existing trajectories
		PhysicsEvent event = FastMCManager.getInstance().getCurrentGenEvent();
		if ((event == null) || (event.count() < 1)) {
			return;
		}

		for (int index = 0; index < event.count();  index++) {
			Particle particle = event.getParticle(index);
			LundId lid = LundSupport.getInstance().get(particle.pid());
			double pxo = particle.px(); //leave in GeV
			double pyo = particle.py(); //leave in GeV
			double pzo = particle.pz(); //leave in GeV

			// note conversions from mm to cm
			double x = particle.vertex().x()/100.; //cm to meters
			double y = particle.vertex().y()/100.; //cm to meters
			double z = particle.vertex().z()/100.; //cm to meters

			StreamTimer st = FastMCManager.getInstance().getTimer();
			swim(lid, pxo, pyo, pzo, x, y, z);
		}
	}
	
	/**
	 * Swim all Monte Carlo particles
	 * 
	 * @param manager
	 *            the swim manager
	 */
	@Override
	public void swimAll() {
		
		if (ClasIoEventManager.getInstance().isSourceFastMC()) {
			SwimAllFastMC();
			return;
		}


		// System.err.println("SWIM ALL MC");
		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}
		


		Swimming.clearMCTrajectories(); // clear all existing trajectories
		
		DataEvent event = ClasIoEventManager.getInstance().getCurrentEvent();
		if (event == null) {
			return;
		}
		
		
//		if (ClasIoEventManager.getInstance().isSourceEvioFile()) {
//			System.err.println("not swimming for evio file");
//			return;
//		}

		DataManager dm = DataManager.getInstance();

		int pid[] = dm.getIntArray(event, "MC::Particle.pid");

		int len = (pid == null) ? 0 : pid.length;
		if (len < 1) {
			return;
		}
		
		float px[] = dm.getFloatArray(event, "MC::Particle.px");
		if (px == null) {
			return;
		}
		float py[] = dm.getFloatArray(event, "MC::Particle.py");
		float pz[] = dm.getFloatArray(event, "MC::Particle.pz");
		float vx[] = dm.getFloatArray(event, "MC::Particle.vx");
		float vy[] = dm.getFloatArray(event, "MC::Particle.vy");
		float vz[] = dm.getFloatArray(event, "MC::Particle.vz");

		try {

			for (int index = 0; index < pid.length; index++) {
				int pdgid = pid[index];
				LundId lid = LundSupport.getInstance().get(pdgid);

				if (lid == null) {
					System.err.println("null LundId object for id: " + pdgid);
				} else {
					if (lid != null) {
						double pxo = px[index]; // in Gev??
						double pyo = py[index];
						double pzo = pz[index];
						double p = Math.sqrt(pxo * pxo + pyo * pyo + pzo * pzo);
						double theta = Math.toDegrees(Math.acos(pzo / p));
						// filter out 0 theta
						if (theta > 1.) {

							// covert momenta to GeV/c from MeV/c
//							pxo /= 1000.0;
//							pyo /= 1000.0;
//							pzo /= 1000.0;
							// note vertices are in mm must convert to meters
							double x = vx[index] / 1000.0;
							double y = vy[index] / 1000.0;
							double z = vz[index] / 1000.0;

							swim(lid, pxo, pyo, pzo, x, y, z);
						}
					} // lid != null
				} //else

			}
		} catch (Exception e) {
			Log.getInstance()
					.error("Exception while swimming all MC particles");
			Log.getInstance().exception(e);
			return;
		}

	}
	
	//units GeV/c and meters
	private void swim(LundId lid, double px, double py, double pz, double x, double y, double z) {
		double p = Math.sqrt(px * px + py * py + pz * pz);
		double theta = Math.toDegrees(Math.acos(pz / p));
		double phi = Math.toDegrees(Math.atan2(py, px));

		Swimmer swimmer = new Swimmer(MagneticFields.getInstance().getActiveField());
		double stepSize = 5e-4; // m
		DefaultSwimStopper stopper = new DefaultSwimStopper(RMAX);

		// System.err.println("swim vertex: (" + x + ", " + y + ", "
		// + z + ")");
		SwimTrajectory traj;
		try {
			traj = swimmer.swim(lid.getCharge(), x, y,
					z, p, theta, phi, stopper, PATHMAX, stepSize,
					Swimmer.CLAS_Tolerance, null);
			traj.setLundId(lid);
			Swimming.addMCTrajectory(traj);
		} catch (RungeKuttaException e) {
			Log.getInstance().error("Exception while swimming all MC particles");
			Log.getInstance().exception(e);
		}
	}
}