package cnuphys.swimZ;

import java.util.ArrayList;
import java.util.List;

import cnuphys.lund.GeneratedParticleRecord;
import cnuphys.magfield.FastMath;
import cnuphys.magfield.FieldProbe;
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

	// the initial z value in cm
	private double _zo;

	// the final z value in cm
	private double _zf;

	// the sign of pz
	private int _pzSign;

	// the |dl x B| integral in kG cm
	private double _bdl = Double.NaN;

	// the pathlength in cm
	private double _pathLength = Double.NaN;;

	/**
	 * Constructor Create a SwimZResult with the trajectory initialized but empty.
	 * 
	 * @param Q        the integer charge (-1 for electron)
	 * @param p        the momentum in Gev/c
	 * @param zo       the initial z value in cm;
	 * @param zf       the final z value in cm;
	 * @param capacity the initial capacity of the trajectory
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
	 * @param sv the given state vector
	 * @return the three momentum in x, y, z order in GeV/c
	 */
	public double[] getThreeMomentum(SwimZStateVector sv) {
		double p3[] = new double[3];
		getThreeMomentum(sv, p3);
		return p3;
	}

	/**
	 * Get the approximate path length in cm
	 * 
	 * @return the approximate path length in cm
	 */
	public double getPathLength() {

		// only compute if necessary
		if (Double.isNaN(_pathLength)) {
			_pathLength = 0;
			int size = size();

			SwimZStateVector prev = null;
			if (size > 1) {

				double dr[] = new double[3];

				for (SwimZStateVector next : _trajectory) {
					if (prev != null) {
						prev.dR(next, dr);
						_pathLength += vecmag(dr);

					}
					prev = next;
				}
			}
		}

		return _pathLength;
	}

	/**
	 * Get the approximate integral |B x dL|
	 * 
	 * @param probe the probe use to compute this result trajectory
	 * @return the approximate integral |B x dL| in kG*cm
	 */
	public double getBDL(FieldProbe probe) {

		// only compute if necessary
		if (Double.isNaN(_bdl)) {
			_bdl = 0;
			_pathLength = 0;
			int size = size();

			SwimZStateVector prev = null;
			if (size > 1) {

				double dr[] = new double[3];

				float b[] = new float[3];
				double bxdl[] = new double[3];

				for (SwimZStateVector next : _trajectory) {
					if (prev != null) {
						prev.dR(next, dr);
						_pathLength += vecmag(dr);

						// get the field at the midpoint
						float xmid = (float) ((prev.x + next.x) / 2);
						float ymid = (float) ((prev.y + next.y) / 2);
						float zmid = (float) ((prev.z + next.z) / 2);
						probe.field(xmid, ymid, zmid, b);

						cross(b, dr, bxdl);
						_bdl += vecmag(bxdl);

					}
					prev = next;
				}
			}
		}

		return _bdl;
	}

	/**
	 * Get the approximate integral |B x dL|
	 * 
	 * @param sector sector 1..6
	 * @param probe  the probe use to compute this result trajectory
	 * @return the approximate integral |B x dL| in kG*cm
	 */
	public double sectorGetBDL(int sector, FieldProbe probe) {

		// only compute if necessary
		if (Double.isNaN(_bdl)) {
			_bdl = 0;
			_pathLength = 0;

			int size = size();

			SwimZStateVector prev = null;
			if (size > 1) {

				double dr[] = new double[3];

				float b[] = new float[3];
				double bxdl[] = new double[3];

				for (SwimZStateVector next : _trajectory) {
					if (prev != null) {
						prev.dR(next, dr);
						_pathLength += vecmag(dr);

						// get the field at the midpoint
						float xmid = (float) ((prev.x + next.x) / 2);
						float ymid = (float) ((prev.y + next.y) / 2);
						float zmid = (float) ((prev.z + next.z) / 2);
						probe.field(sector, xmid, ymid, zmid, b);

						cross(b, dr, bxdl);
						_bdl += vecmag(bxdl);

					}
					prev = next;
				}
			}
		}

		return _bdl;
	}

	// usual cross product c = a x b
	private static void cross(float a[], double b[], double c[]) {
		c[0] = a[1] * b[2] - a[2] * b[1];
		c[1] = a[2] * b[0] - a[0] * b[2];
		c[2] = a[0] * b[1] - a[1] * b[0];
	}

	// usual vec mag
	private static double vecmag(double a[]) {
		double asq = a[0] * a[0] + a[1] * a[1] + a[2] * a[2];
		return Math.sqrt(asq);
	}

	/**
	 * Get the momentum three-vector for a given statevector, which should be on
	 * this result's trajectory.
	 * 
	 * @param sv the given state vector
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
	 * @param vector the vector to add
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
	 * @param sv the statevector, presumably on this trajectory
	 * @return theta and phi in an array, in that order, in degrees.
	 */
	public double[] getThetaAndPhi(SwimZStateVector sv) {
		double thetaPhi[] = { Double.NaN, Double.NaN };

		if (sv != null) {
			double p3[] = getThreeMomentum(sv);
			if (Math.abs(_p) > 1.0e-20) {
				thetaPhi[0] = FastMath.acos2Deg(p3[2] / _p); // theta
				thetaPhi[1] = FastMath.atan2Deg(p3[1], p3[0]);
			}
		}
		return thetaPhi;
	}

	/**
	 * Get the values of theta and phi from the momentum and the final state vector.
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
