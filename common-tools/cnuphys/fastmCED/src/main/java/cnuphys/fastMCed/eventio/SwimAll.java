package cnuphys.fastMCed.eventio;

import java.util.Vector;

import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;

import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.magneticfield.swim.ISwimAll;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.lund.TrajectoryRowData;
import cnuphys.magfield.MagneticFields;
import cnuphys.rk4.RungeKuttaException;
import cnuphys.swim.DefaultSwimStopper;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.Swimmer;
import cnuphys.swim.Swimming;

public class SwimAll implements ISwimAll {
	
	// integration cutoff
	private static final double RMAX = 10.0;
	private static final double PATHMAX = 10.0;


	@Override
	public void swimAll() {
		Swimming.clearMCTrajectories(); // clear all existing trajectories
		PhysicsEvent event = PhysicsEventManager.getInstance().getCurrentEvent();
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

			swim(lid, pxo, pyo, pzo, x, y, z);
		}
	}

	@Override
	public Vector<TrajectoryRowData> getRowData() {
		PhysicsEvent event = PhysicsEventManager.getInstance().getCurrentEvent();
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

			v.add(new TrajectoryRowData(index, lid, x, y, z, p, theta, phi, 0, "FastMC"));
		}

		return v;
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
