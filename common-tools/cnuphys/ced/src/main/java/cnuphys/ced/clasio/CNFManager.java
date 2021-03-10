package cnuphys.ced.clasio;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.jlab.io.base.DataEvent;

import cnuphys.adaptiveSwim.AdaptiveSwimException;
import cnuphys.adaptiveSwim.AdaptiveSwimResult;
import cnuphys.adaptiveSwim.AdaptiveSwimmer;
import cnuphys.ced.alldata.DataManager;
import cnuphys.ced.clasio.ClasIoEventManager.EventSourceType;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.magfield.MagneticFields;
import cnuphys.splot.plot.Environment;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.Swimming;

/**
 * Used only for the CNF project
 * 
 * @author heddle
 *
 */
public class CNFManager implements IClasIoEventListener {
	
	// singleton
	private static CNFManager _instance;

	private CNFManager() {
		ClasIoEventManager.getInstance().addClasIoEventListener(this, 2);
	}

	/**
	 * public access to the singleton
	 * 
	 * @return
	 */
	public static CNFManager getInstance() {
		if (_instance == null) {
			_instance = new CNFManager();
		}
		return _instance;
	}

	@Override
	public void newClasIoEvent(DataEvent event) {
		if (event != null) {
			if (event.hasBank("CNF::Particles")) {
				processParticlesBank(event);
			}
		}
	}

	// process the particle bank
	private void processParticlesBank(DataEvent event) {
		
		List<SwimTrajectory> trajectories = Swimming.getAuxTrajectories();
		
		DataManager dm = DataManager.getInstance();
		short flag[] = dm.getShortArray(event, "CNF::Particles.flag");
		short pid[] = dm.getShortArray(event, "CNF::Particles.pid");
		float momentum[] = dm.getFloatArray(event, "CNF::Particles.mom"); // GeV
		float theta[] = dm.getFloatArray(event, "CNF::Particles.the"); // radians
		float phi[] = dm.getFloatArray(event, "CNF::Particles.phi"); // radians
		float xo[] = dm.getFloatArray(event, "CNF::Particles.vx"); // cm
		float yo[] = dm.getFloatArray(event, "CNF::Particles.vy"); // cm
		float zo[] = dm.getFloatArray(event, "CNF::Particles.vz"); // cm
		
		int len = (theta == null) ? 0 : theta.length;

		double eps = 1.0e-6;
		double sf = 8;
		double h0 = 0.01;

		
		// convert angles to degrees and distances to meters
		if (len > 0) {
			AdaptiveSwimmer swim = new AdaptiveSwimmer();
			
			File baseDir = new File(Environment.getInstance().getHomeDirectory(), "CNFTraj");
			if (!baseDir.exists()) {
				baseDir.mkdir();
			}
			
			//subdir based on nano time
			long nanotime = System.nanoTime();
			File subDir = new File(baseDir, "" + nanotime);
			subDir.mkdir();
			
			MagneticFields mf = MagneticFields.getInstance();
			double torusScale = (mf.hasActiveTorus()? mf.getTorus().getScaleFactor() : 0);
			double solenoidScale = (mf.hasActiveSolenoid()? mf.getSolenoid().getScaleFactor() : 0);

			
			for (int i = 0; i < len; i++) {
				AdaptiveSwimResult result = new AdaptiveSwimResult(true);
				double thetaF = Math.toDegrees(theta[i]);
				double phiF = Math.toDegrees(phi[i]);
				double xoF = xo[i]/100.;
				double yoF = yo[i]/100.;
				double zoF = zo[i]/100.;
				LundId lid = LundSupport.getInstance().get(pid[i]);
				int id = lid.getId();
				double p = momentum[i];

				if (lid != null) {
					System.err.println("Got a[n] " + lid.getName() + "  charge: " + lid.getCharge());
					try {
						swim.swim(lid.getCharge(), xoF, yoF, zoF, p, thetaF, phiF, sf, h0, eps, result);
						System.err.println("num points in trajectory: " + result.getTrajectory().size());
						
						SwimTrajectory traj = result.getTrajectory();
						traj.setLundId(lid);
						trajectories.add(traj);
						
						File file =  new File(subDir,"" + lid.getId()+".csv");
						String path =  file.getPath();
						
						//write the csv file
						try {
							DataOutputStream dos = new DataOutputStream(new FileOutputStream(path));
							
							String header = String.format("# %d,%-3.1f,%-3.1f,%-3.1f,%-3.1f,%-3.1f,%-3.1f,%-3.1f,%-3.1f", 
									id, torusScale, solenoidScale, xoF, yoF, zoF, p, thetaF, phiF);
							stringLn(dos, header);
							
							for (double u[] : traj) {
								String s = String.format("%f,%f,%f,%f,%f,%f", u[0], u[1], u[2], p*u[3], p*u[4], p*u[5]);
								stringLn(dos, s);
							}
							dos.close();
						}
						catch (Exception e) {
							e.printStackTrace();
						}
						
						
					} catch (AdaptiveSwimException e) {
						e.printStackTrace();
					}
				}
			}
		}
		System.err.println("  has particles bank");
	}
	
	
	//for csv output
	private static void stringLn(DataOutputStream dos, String s) {
		
		s = s.replace("  ", "");		
		s = s.replace(" ", "");		
		s = s.replace(", ", ",");		
		s = s.replace(", ", ",");		
		s = s.replace(" ,", ",");		

		
		try {
			dos.writeBytes(s);
			dos.writeBytes("\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	public void openedNewEventFile(String path) {
		// TODO Auto-generated method stub

	}

	@Override
	public void changedEventSource(EventSourceType source) {
	}

	@Override
	public boolean ignoreIfAccumulating() {
		return false;
	}
}
