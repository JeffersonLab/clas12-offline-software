package cnuphys.swim;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Random;
import java.util.Vector;

import javax.swing.event.EventListenerList;

import cnuphys.lund.SwimTrajectoryListener;
import cnuphys.magfield.MagneticFieldChangeListener;
import cnuphys.magfield.MagneticFields;
import cnuphys.rk4.RungeKuttaException;

public class Swimming {

    // List of SwimTrajectorylisteners
    private static EventListenerList _listenerList;

    // the mc trajectories
    private static Vector<SwimTrajectory> _mcTrajectories = new Vector<SwimTrajectory>();

    // the recon trajectories
    private static Vector<SwimTrajectory> _reconTrajectories = new Vector<SwimTrajectory>();

    // the current swimmer which will change if the field changes
    private static Swimmer _swimmer;

    // listen to field changes
    static {
	MagneticFieldChangeListener mfl = new MagneticFieldChangeListener() {

	    @Override
	    public void magneticFieldChanged() {
		// clearTrajectories();
		_swimmer = null;
	    }

	};
	MagneticFields.addMagneticFieldChangeListener(mfl);
    }

    /**
     * Clear all the mc trajectories.
     */
    public static void clearMCTrajectories() {
	_mcTrajectories.clear();
	notifyListeners();
    }

    /**
     * Clear all the recon trajectories.
     */
    public static void clearReconTrajectories() {
	_reconTrajectories.clear();
	notifyListeners();
    }

    /**
     * Get all the cached mc trajectories
     * 
     * @return all the cached mc trajectories
     */
    public static Vector<SwimTrajectory> getMCTrajectories() {
	return _mcTrajectories;
    }

    /**
     * Get all the cached recon trajectories
     * 
     * @return all the cached recon trajectories
     */
    public static Vector<SwimTrajectory> getReconTrajectories() {
	return _reconTrajectories;
    }

    /**
     * Add a trajectory to the mc collection
     * 
     * @param traj
     *            the trajectory to add.
     */
    public static void addMCTrajectory(SwimTrajectory traj) {
	_mcTrajectories.remove(traj);
	_mcTrajectories.add(traj);
	notifyListeners();
    }

    /**
     * Add a trajectory to the recon collection
     * 
     * @param traj
     *            the trajectory to add.
     */
    public static void addReconTrajectory(SwimTrajectory traj) {
	_reconTrajectories.remove(traj);
	_reconTrajectories.add(traj);
	notifyListeners();
    }

    /**
     * Remove a trajectory from the mc collection
     * 
     * @param traj
     *            the trajectory to remove.
     */
    public static void removeMCTrajectory(SwimTrajectory traj) {
	_mcTrajectories.remove(traj);
	notifyListeners();
    }

    /**
     * Remove a trajectory from the recon collection
     * 
     * @param traj
     *            the trajectory to remove.
     */
    public static void removeReconTrajectory(SwimTrajectory traj) {
	_reconTrajectories.remove(traj);
	notifyListeners();
    }

    /**
     * Obtain the common swimmer which should be set to swim through the active
     * magnetic field
     * 
     * @return the common swimmer
     */
    public static Swimmer getSwimmer() {
	if (_swimmer == null) {
	    _swimmer = new Swimmer(MagneticFields.getActiveField());
	}
	return _swimmer;
    }

    // notify listeners that the collection of trajectories has changed
    protected static void notifyListeners() {

	if (_listenerList == null) {
	    return;
	}

	// Guaranteed to return a non-null array
	Object[] listeners = _listenerList.getListenerList();

	// This weird loop is the bullet proof way of notifying all listeners.
	// for (int i = listeners.length - 2; i >= 0; i -= 2) {
	// order is flipped so it goes in order as added
	for (int i = 0; i < listeners.length; i += 2) {
	    if (listeners[i] == SwimTrajectoryListener.class) {
		SwimTrajectoryListener listener = (SwimTrajectoryListener) listeners[i + 1];
		listener.trajectoriesChanged();
	    }

	}
    }

    /**
     * Add a magnetic field change listener
     * 
     * @param SwimTrajectoryListener
     *            the listener to add
     */
    public static void addSwimTrajectoryListener(
	    SwimTrajectoryListener SwimTrajectoryListener) {

	if (_listenerList == null) {
	    _listenerList = new EventListenerList();
	}

	// avoid adding duplicates
	_listenerList.remove(SwimTrajectoryListener.class,
		SwimTrajectoryListener);
	_listenerList.add(SwimTrajectoryListener.class, SwimTrajectoryListener);
    }

    /**
     * Remove a SwimTrajectoryListener.
     * 
     * @param SwimTrajectoryListener
     *            the SwimTrajectoryListener to remove.
     */

    public static void removeSwimTrajectoryListener(
	    SwimTrajectoryListener SwimTrajectoryListener) {

	if ((SwimTrajectoryListener == null) || (_listenerList == null)) {
	    return;
	}

	_listenerList.remove(SwimTrajectoryListener.class,
		SwimTrajectoryListener);
    }

    private static void timeTest(int numTraj, long seed) {
	System.out.println("Time Test");

	Random random;
	if (seed < 1) {
	    random = new Random();
	} else {
	    random = new Random(seed);
	}
	double hdata[] = new double[3];
	int charge = -1;
	double maxPathLength = 8.0;
	double stepSizes[] = { 5e-4, 1e-4, 5e-3, 1e-3, 5e-2 }; // m
	double tolerances[] = { 1.0e-10, 1.0e-9, 1.0e-8, 1.0e-7, 1.0e-6, 1.0e-5 };
	double rmax = 6.0;

	double xscale = 1.0; // position scale order of meters
	double pscale = 1.0; // direct cosine px/P etc scale order of 1

	double clas_Tolerance[];

	DefaultSwimStopper stopper = new DefaultSwimStopper(rmax);

	ThreadMXBean bean = ManagementFactory.getThreadMXBean();
	System.out.println(bean.isCurrentThreadCpuTimeSupported());

	for (double tolerance : tolerances) {

	    clas_Tolerance = new double[6];
	    double xTol = tolerance * xscale;
	    double pTol = tolerance * pscale;
	    for (int i = 0; i < 3; i++) {
		clas_Tolerance[i] = xTol;
		clas_Tolerance[i + 3] = pTol;
	    }

	    long start = bean.getCurrentThreadCpuTime();
	    for (int i = 0; i < numTraj; i++) {

		double momentum = 2 + 6 * random.nextDouble();
		double theta = 15 + 10 * random.nextDouble();
		double phi = -20 + 40 * random.nextDouble();

		// System.out.println("P: " + momentum + " theta: " + theta +
		// " phi: " + phi);

		try {
		    SwimTrajectory traj = getSwimmer().swim(charge, 0, 0, 0,
			    momentum, theta, phi, stopper, maxPathLength,
			    stepSizes[0], clas_Tolerance, hdata);

		    double[] lastY = traj.lastElement();
		    // printSummary(
		    // "\nresult from adaptive stepsize method with storage and err vector",
		    // traj.size(), momentum, lastY, hdata);

		} catch (RungeKuttaException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	    long end = bean.getCurrentThreadCpuTime();
	    double timePerTraj = (end - start) / numTraj;

	    System.err.println("tolerance: " + tolerance + "m. Time per traj: "
		    + timePerTraj / 100000 + "mS");
	}
    }

    private static void timeTest3(int numTraj, long seed) {
	System.out.println("Time Test");

	Random random;
	if (seed < 1) {
	    random = new Random();
	} else {
	    random = new Random(seed);
	}
	double hdata[] = new double[3];
	int charge = -1;
	double maxPathLength = 8.0;
	double stepSize = 5e-4; // m
	double tolerances[] = { 1.0e-10, 1.0e-9, 1.0e-8, 1.0e-7, 1.0e-6, 1.0e-5 };
	double zacc = 1.0e-6;
	double rmax = 6.0;
	double ztarget = 2.75; // where integration should stop

	double xscale = 1.0; // position scale order of meters
	double pscale = 1.0; // direct cosine px/P etc scale order of 1

	double clas_Tolerance[];

	ThreadMXBean bean = ManagementFactory.getThreadMXBean();
	System.out.println(bean.isCurrentThreadCpuTimeSupported());

	for (double tolerance : tolerances) {

	    clas_Tolerance = new double[6];
	    double xTol = tolerance * xscale;
	    double pTol = tolerance * pscale;
	    for (int i = 0; i < 3; i++) {
		clas_Tolerance[i] = xTol;
		clas_Tolerance[i + 3] = pTol;
	    }

	    double worstDev = 0;
	    double avgDev = 0;

	    for (int i = 0; i < numTraj; i++) {

		double momentum = 2 + 6 * random.nextDouble();
		double theta = 15 + 10 * random.nextDouble();
		double phi = -20 + 40 * random.nextDouble();

		try {

		    // get reference
		    SwimTrajectory traj = getSwimmer().swim(charge, 0, 0, 0,
			    momentum, theta, phi, ztarget, zacc, rmax,
			    maxPathLength, stepSize, Swimmer.CLAS_Tolerance,
			    hdata);
		    double[] refLastY = traj.lastElement();

		    // System.err.println("REF XYZ: (" + refLastY[0] + ", " +
		    // refLastY[1] + ", " + refLastY[2] + ")");

		    traj = getSwimmer().swim(charge, 0, 0, 0, momentum, theta,
			    phi, ztarget, zacc, rmax, maxPathLength, stepSize,
			    clas_Tolerance, hdata);

		    double[] lastY = traj.lastElement();
		    // System.err.println("XYZ: (" + lastY[0] + ", " + lastY[1]
		    // + ", " + lastY[2] + ")");

		    double dx = lastY[0] - refLastY[0];
		    double dy = lastY[1] - refLastY[1];
		    double dz = lastY[2] - refLastY[2];
		    double dev = Math.sqrt(dx * dx + dy * dy + dz * dz);

		    worstDev = Math.max(worstDev, dev);
		    avgDev += dev;

		} catch (RungeKuttaException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }

	    worstDev *= 1000; // mm
	    avgDev = 1000 * avgDev / numTraj; // mm
	    System.err.println("tolerance: " + tolerance + " worst dev: "
		    + worstDev + " mm  avg dev: " + avgDev + " mm");
	}
    }

    private static void timeTest2(int numTraj, long seed) {
	System.out.println("Time Test");

	Random random;
	if (seed < 1) {
	    random = new Random();
	} else {
	    random = new Random(seed);
	}
	double hdata[] = new double[3];
	int charge = -1;
	double maxPathLength = 8.0;
	double stepSize = 5e-4; // m
	double zaccs[] = { 1.0e-6, 5.0e-6, 1.0e-5, 5.0e-5, 1.0e-4, 5.0e-4 };
	double rmax = 6.0;
	double ztarget = 2.75; // where integration should stop

	ThreadMXBean bean = ManagementFactory.getThreadMXBean();
	System.out.println(bean.isCurrentThreadCpuTimeSupported());

	for (double zacc : zaccs) {

	    long start = bean.getCurrentThreadCpuTime();
	    for (int i = 0; i < numTraj; i++) {

		double momentum = 2 + 6 * random.nextDouble();
		double theta = 15 + 10 * random.nextDouble();
		double phi = -20 + 40 * random.nextDouble();

		// System.out.println("P: " + momentum + " theta: " + theta +
		// " phi: " + phi);

		try {
		    SwimTrajectory traj = getSwimmer().swim(charge, 0, 0, 0,
			    momentum, theta, phi, ztarget, zacc, rmax,
			    maxPathLength, stepSize, Swimmer.CLAS_Tolerance,
			    hdata);

		    // double[] lastY = traj.lastElement();
		    // printSummary(
		    // "\nresult from adaptive stepsize method with storage and err vector",
		    // traj.size(), momentum, lastY, hdata);

		} catch (RungeKuttaException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	    long end = bean.getCurrentThreadCpuTime();
	    double timePerTraj = (end - start) / numTraj;

	    System.err.println("zacc: " + zacc + "m. Time per traj: "
		    + timePerTraj / 100000 + "mS");
	}
    }

    public static void printSummary(String message, int nstep, double momentum,
	    double Q[], double hdata[]) {
	System.out.println(message);
	double R = Math.sqrt(Q[0] * Q[0] + Q[1] * Q[1] + Q[2] * Q[2]);
	double norm = Math.sqrt(Q[3] * Q[3] + Q[4] * Q[4] + Q[5] * Q[5]);
	double P = momentum * norm;

	System.out.println("Number of steps: " + nstep);

	if (hdata != null) {
	    System.out.println("min stepsize: " + hdata[0]);
	    System.out.println("avg stepsize: " + hdata[1]);
	    System.out.println("max stepsize: " + hdata[2]);
	}
	System.out
		.println(String
			.format("R = [%8.5f, %8.5f, %8.5f] |R| = %7.4f m\nP = [%7.4e, %7.4e, %7.4e] |P| =  %9.6e GeV/c",
				Q[0], Q[1], Q[2], R, P * Q[3], P * Q[4], P
					* Q[5], P));
	System.out.println("norm (should be 1): " + norm);
	System.out.println("--------------------------------------\n");
    }

}
