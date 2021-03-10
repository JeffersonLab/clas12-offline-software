package cnuphys.ced.magfield;

import java.util.ArrayList;
import java.util.Vector;

import org.jlab.io.base.DataEvent;

import cnuphys.adaptiveSwim.AdaptiveSwimException;
import cnuphys.adaptiveSwim.AdaptiveSwimResult;
import cnuphys.adaptiveSwim.AdaptiveSwimmer;
import cnuphys.adaptiveSwim.test.InitialValues;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.magneticfield.swim.ISwimAll;
import cnuphys.ced.alldata.DataManager;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.ClasIoMonteCarloView;
import cnuphys.lund.GeneratedParticleRecord;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.lund.TrajectoryRowData;
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
	private static final double PATHMAX = 11.0;

	/**
	 * Get all the row data so the trajectory dialog can be updated.
	 * 
	 * @param manager the swim manager
	 * @return a vector of TrajectoryRowData objects.
	 */
	@Override
	public Vector<TrajectoryRowData> getRowData() {	
		return ClasIoMonteCarloView.getInstance().getRowData();
	}

	/**
	 * Swim all Monte Carlo particles
	 * 
	 * @param manager the swim manager
	 */
	@Override
	public void swimAll() {

		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}

		Swimming.clearMCTrajectories(); // clear all existing trajectories

		Vector<TrajectoryRowData> data = getRowData();
		if (data == null) {
			return;
		}
		
		AdaptiveSwimmer swimmer = new AdaptiveSwimmer();
		double stepSize = 1.0e-3;
		double eps = 1.0e-6;
		
		//used to avoid swimming duplicates
		ArrayList<String> swam = new ArrayList<>();
		
		for (TrajectoryRowData trd : data) {
			LundId lid = LundSupport.getInstance().get(trd.getId());
			
			double sf = PATHMAX;
						
			if (lid != null) {
				try {
					AdaptiveSwimResult result = new AdaptiveSwimResult(true);
					
					String summaryStr = String.format("%s %10.6f  %10.6f  %10.6f  %10.6f  %10.6f  %10.6f", 
							lid.getName(), trd.getXo(), trd.getYo(), trd.getZo(),
							trd.getMomentum(), trd.getTheta(), trd.getPhi());
					
					if (swam.contains(summaryStr)) {
			//			System.err.println("Skipping duplicate swim, probably MC::Particle and MC::Lund [" + lid.getName() + "]");
						continue;
					}
					
					swam.add(summaryStr);
					
					swimmer.swim(lid.getCharge(), trd.getXo() / 100, trd.getYo() / 100, trd.getZo() / 100,
							trd.getMomentum() / 1000, trd.getTheta(), trd.getPhi(), sf, stepSize, eps, result);
					result.getTrajectory().setLundId(lid);
					result.getTrajectory().setSource(trd.getSource());
					
					if (result.getTrajectory().getGeneratedParticleRecord() == null) {
						InitialValues iv = result.getInitialValues();
						GeneratedParticleRecord genPart =  new GeneratedParticleRecord(iv.charge,
								iv.xo, iv.yo, iv.zo, iv.p, iv.theta, iv.phi);
						result.getTrajectory().setGeneratedParticleRecord(genPart);
					}
					
					Swimming.addMCTrajectory(result.getTrajectory());
				} catch (AdaptiveSwimException e) {
					e.printStackTrace();
				}
				
			}
		} //for trd
		


	}
}