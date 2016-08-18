package cnuphys.swimZ;

import java.util.ArrayList;
import java.util.List;

import cnuphys.lund.GeneratedParticleRecord;
import cnuphys.magfield.MagneticField;
import cnuphys.swim.SwimTrajectory;

/**
 * Holds the result of a swimZ integration
 * 
 * @author heddle
 *
 */

public class SwimZResult {

	// the trajectory of state vectors
	private ArrayList<SwimZStateVector> _trajectory;

	// the integer charge (-1 for electron)
	private int _Q;

	// the momentum in GeV/c
	private double _p;

	// the initial z value
	private double _zo;

	// the final z value
	private double _zf;

	// the sign of pz
	private int _pzSign;

	// /**
	// * Constructor
	// * Create a SwimZResult with the trajectory initialized but empty.
	// * @param Q the integer charge (-1 for electron)
	// * @param p the momentum in Gev/c
	// * @param zo the final z value;
	// * @param zf the final z value;
	// */
	// public SwimZResult(int Q, double p, double zo, double zf) {
	// this(Q, p, zo, zf, 100,100);
	// }

	/**
	 * Constructor Create a SwimZResult with the trajectory initialized but
	 * empty.
	 * 
	 * @param Q
	 *            the integer charge (-1 for electron)
	 * @param p
	 *            the momentum in Gev/c
	 * @param zo
	 *            the final z value;
	 * @param zf
	 *            the final z value;
	 * @param capacity
	 *            the initial capacity of the trajectory
	 * @param increment
	 *            the increment when more space is needed
	 */
	public SwimZResult(int Q, double p, double zo, double zf, int capacity) {
		_Q = Q;
		_p = p;
		_zo = zo;
		_zf = zf;
		_pzSign = (zf < zo) ? -1 : 1;
		_trajectory = new ArrayList<SwimZStateVector>(capacity);
	}

	/**
	 * Get the magnitude of the three momentum, which is constant.
	 * 
	 * @return the magnitude of the three momentum in GeV/c
	 */
	public double getMomentum() {
		return _p;
	}

	/**
	 * Get the momentum three-vector for a given statevector, which should be on
	 * this result's trajectory.
	 * 
	 * @param sv
	 *            the given state vector
	 * @return the three momentum in x, y, z order in GeV/c
	 */
	public double[] getThreeMomentum(SwimZStateVector sv) {
		double p3[] = new double[3];
		getThreeMomentum(sv, p3);
		return p3;
	}

	/**
	 * Get the momentum three-vector for a given statevector, which should be on
	 * this result's trajectory.
	 * 
	 * @param sv
	 *            the given state vector
	 * @p3 on return holds the momentum in x, y, z order in GeV/c
	 */
	public void getThreeMomentum(SwimZStateVector sv, double p3[]) {
		double px = Double.NaN;
		double py = Double.NaN;
		double pz = Double.NaN;

		if (sv != null) {
			double txsq = sv.tx * sv.tx;
			double tysq = sv.ty * sv.ty;
			pz = _pzSign * _p / Math.sqrt(txsq + tysq + 1);
			px = pz * sv.tx;
			py = pz * sv.ty;
		}

		p3[0] = px;
		p3[1] = py;
		p3[2] = pz;
	}

	/**
	 * Get the initial three momentum
	 * 
	 * @return the initial three momentum
	 */
	public double[] getInitialThreeMomentum() {
		return getThreeMomentum(first());
	}

	/**
	 * Get the final three momentum
	 * 
	 * @return the final three momentum
	 */
	public double[] getFinalThreeMomentum() {
		return getThreeMomentum(last());
	}

	/**
	 * Get the integer charge. This is not an element of the state vector but is
	 * stored here for convenience.
	 * 
	 * @return the integer charge (e.g., -1 for electron)
	 */
	public int getQ() {
		return _Q;
	}

	/**
	 * Get the starting z value
	 * 
	 * @return the starting z value
	 */
	public double getZo() {
		return _zo;
	}

	/**
	 * Get the final z value
	 * 
	 * @return the final z value
	 */
	public double getZf() {
		return _zf;
	}

	/**
	 * Add a state vector into the trajectory
	 * 
	 * @param vector
	 *            the vector to add
	 */
	protected void add(SwimZStateVector vector) {
		_trajectory.add(vector);
	}

	/**
	 * Get the trajectory of state vectors
	 * 
	 * @return the trajectory
	 */
	public List<SwimZStateVector> getTrajectory() {
		return _trajectory;
	}

	/**
	 * Get the first state vector
	 * 
	 * @return the first state vector
	 */
	public SwimZStateVector first() {
		if ((_trajectory == null) || _trajectory.isEmpty()) {
			return null;
		}
		return _trajectory.get(0);
	}

	/**
	 * Get the number of state vectors in the trajectory
	 * 
	 * @return the number of state vectors in the trajectory
	 */
	public int size() {
		if ((_trajectory == null) || _trajectory.isEmpty()) {
			return 0;
		}
		return _trajectory.size();
	}

	/**
	 * Get the final state vector
	 * 
	 * @return the final state vector
	 */
	public SwimZStateVector last() {
		if ((_trajectory == null) || _trajectory.isEmpty()) {
			return null;
		}
		return _trajectory.get(_trajectory.size() - 1);
	}

	/**
	 * Get the values of theta and phi from the momentum and a state vector.
	 * 
	 * @param sv
	 *            the statevector, presumably on this trajectory
	 * @return theta and phi in an array, in that order, in degrees.
	 */
	public double[] getThetaAndPhi(SwimZStateVector sv) {
		double thetaPhi[] = { Double.NaN, Double.NaN };

		if (sv != null) {
			double p3[] = getThreeMomentum(sv);
			if (Math.abs(_p) > 1.0e-20) {
				thetaPhi[0] = Math.toDegrees(Math.acos(p3[2] / _p)); // theta
				thetaPhi[1] = MagneticField.atan2Deg(p3[1], p3[0]);
			}
		}
		return thetaPhi;
	}

	/**
	 * Get the values of theta and phi from the momentum and the final state
	 * vector.
	 * 
	 * @return theta and phi in an array, in that order, in degrees.
	 */
	public double[] getFinalThetaAndPhi() {
		return getThetaAndPhi(last());
	}

	/**
	 * Get the values of theta and phi from the momentum and the initial state
	 * vector.
	 * 
	 * @return theta and phi in an array, in that order, in degrees.
	 */
	public double[] getInitialThetaAndPhi() {
		return getThetaAndPhi(first());
	}

	/**
	 * Obtain a GeneratedParticleRecord for this result
	 * 
	 * @return a GeneratedParticleRecord for this result
	 */
	public GeneratedParticleRecord getGeneratedParticleRecord() {
		SwimZStateVector sv = first();
		if (sv == null) {
			return null;
		}

		double xo = sv.x / 100.0; // cm to m
		double yo = sv.y / 100.0; // cm to m
		double zo = _zo / 100.0; // cm to m
		double thetaPhi[] = getThetaAndPhi(sv);
		return new GeneratedParticleRecord(_Q, xo, yo, zo, _p, thetaPhi[0], thetaPhi[1]);
	}

	// /**
	// * Create a one point trajectory. Used hrn the initial momentum is lower
	// than some minimum value.
	// * @param charge the charge of the particle (-1 for electron, +1 for
	// proton, etc.)
	// * @param xo the x vertex position in m
	// * @param yo the y vertex position in m
	// * @param zo the z vertex position in m
	// * @param momentum initial momentum in GeV/c
	// * @param theta initial polar angle in degrees
	// * @param phi initial azimuthal angle in degrees
	// */
	// public SwimTrajectory(int charge, double xo, double yo, double zo,
	// double momentum, double theta, double phi) {

	/**
	 * Create a SwimTrajectory for this result object
	 * 
	 * @return a SwimTrajectory corresponding to this result.
	 */
	public SwimTrajectory toSwimTrajectory() {
		SwimZStateVector sv = first();
		if (sv == null) {
			return null;
		}

		double xo = sv.x / 100.0; // cm to m
		double yo = sv.y / 100.0; // cm to m
		double zo = _zo / 100.0; // cm to m
		double thetaPhi[] = getThetaAndPhi(sv);

		SwimTrajectory traj = new SwimTrajectory(_Q, xo, yo, zo, _p, thetaPhi[0], thetaPhi[1]);

		double p3[] = new double[3];
		for (SwimZStateVector v : _trajectory) {
			double x = v.x / 100; // cm to m
			double y = v.y / 100; // cm to m
			double z = v.z / 100; // cm to m
			getThreeMomentum(v, p3);
			double u[] = { x, y, z, p3[0] / _p, p3[1] / _p, p3[2] / _p };
			traj.add(u);
		}
		return traj;
	}
}
