package cnuphys.ced.magfield;

import java.util.Vector;

import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;

import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.magneticfield.swim.ISwimAll;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.data.ColumnData;
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

	//get row data from FastMC even instead of evio
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

			v.add(new TrajectoryRowData(lid, x, y, z, p, theta, phi, 0, "LUND File"));

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

		if (ClasIoEventManager.getInstance().isSourceFastMC()) {
			return getRowDataFastMC();
		}

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

					v.add(new TrajectoryRowData(lid, x, y, z, p, theta, phi, 0, "GEMC GenPart::true"));
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

					swim(lid, pxo, pyo, pzo, x, y, z);
				} // lid != null

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

		Swimmer swimmer = new Swimmer(MagneticFields.getActiveField());
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