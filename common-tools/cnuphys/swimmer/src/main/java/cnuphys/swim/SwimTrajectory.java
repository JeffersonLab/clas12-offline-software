package cnuphys.swim;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;

import cnuphys.adaptiveSwim.AdaptiveSwimUtilities;
import cnuphys.lund.GeneratedParticleRecord;
import cnuphys.lund.LundId;
import cnuphys.magfield.FastMath;
import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.RotatedCompositeProbe;

/**
 * Combines a generated particle record with a path (trajectory). A trajectory
 * is a collection of state vectors. A state vector is the six component vector:
 * <BR>
 * Q = [x, y, z, px/p, py/p, pz/p] <BR>
 * 
 * @author heddle
 * 
 */

public class SwimTrajectory extends ArrayList<double[]>  implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3850772573951127304L;

	// the particle that we swam
	private GeneratedParticleRecord _genPartRec;

	// the lund id, if it is known (i.e. from montecarlo truth)
	private LundId _lundId;

	// flag indicating whether bdl was computed
	private boolean _computedBDL;

	/** index for the x component (m) */
	public static final int X_IDX = 0;

	/** index for the y component (m) */
	public static final int Y_IDX = 1;

	/** index for the z component (m) */
	public static final int Z_IDX = 2;

	/** index for the px/p direction cosine */
	public static final int DIRCOSX_IDX = 3;

	/** index for the py/p direction cosine */
	public static final int DIRCOSY_IDX = 4;

	/** index for the pz/p direction cosine */
	public static final int DIRCOSZ_IDX = 5;

	/** index for the accumulated path length (m) */
	public static final int PATHLEN_IDX = 6;

	/** index for the accumulated integral |B x dL| component (kG-m) */
	public static final int BXDL_IDX = 7;

	/** user object */
	public Object userObject;

	/** The source of the trajectory e.g. hbtracking */
	private String _source = "???";

	/**
	 * Create a swim trajectory with no initial content
	 */
	public SwimTrajectory() {
		super();
	}
	
	/**
	 * Clear the trajectory
	 */
	@Override
	public void clear() {
		super.clear();
		_computedBDL = false;
	}
	
	/**
	 * Create a one point trajectory. Used hrn the initial momentum is lower than
	 * some minimum value.
	 * 
	 * @param charge   the charge of the particle (-1 for electron, +1 for proton,
	 *                 etc.)
	 * @param xo       the x vertex position in m
	 * @param yo       the y vertex position in m
	 * @param zo       the z vertex position in m
	 * @param momentum initial momentum in GeV/c
	 * @param theta    initial polar angle in degrees
	 * @param phi      initial azimuthal angle in degrees
	 */
	public SwimTrajectory(int charge, double xo, double yo, double zo, double momentum, double theta, double phi) {
		this(charge, xo, yo, zo, momentum, theta, phi, 1);

		double thetRad = Math.toRadians(theta);
		double phiRad = Math.toRadians(phi);

		double pz = Math.cos(thetRad);
		double rho = Math.sin(thetRad);
		double px = rho * Math.cos(phiRad);
		double py = rho * Math.sin(phiRad);
		double v[] = new double[6];
		v[0] = xo;
		v[1] = yo;
		v[2] = zo;
		v[3] = px;
		v[4] = py;
		v[5] = pz;
		add(0, v);
	}

	/**
	 * @param charge          the charge of the particle (-1 for electron, +1 for
	 *                        proton, etc.)
	 * @param xo              the x vertex position in m
	 * @param yo              the y vertex position in m
	 * @param zo              the z vertex position in m
	 * @param momentum        initial momentum in GeV/c
	 * @param theta           initial polar angle in degrees
	 * @param phi             initial azimuthal angle in degrees
	 * @param initialCapacity the initial capacity of the trajectory list
	 */
	public SwimTrajectory(int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
			int initialCapacity) {
		this(new GeneratedParticleRecord(charge, xo, yo, zo, momentum, theta, phi), initialCapacity);
	}

	/**
	 * @param genPartRec      the generated particle record
	 * @param initialCapacity the initial capacity of the trajectory list
	 */
	public SwimTrajectory(GeneratedParticleRecord genPartRec, int initialCapacity) {
		super(initialCapacity);
		
		if (genPartRec == null) {
			System.err.println("NULL GEN PART REC (A)");
		}
		
		_genPartRec = genPartRec;
	}

	/**
	 * Set the generated particle record
	 * @param genPart the generated particle record
	 */
	public void setGeneratedParticleRecord(GeneratedParticleRecord genPart) {
		
		if (genPart == null) {
			System.err.println("NULL GEN PART REC (A)");
			(new Throwable()).printStackTrace();
		}

		
		_genPartRec = genPart;
	}
	/**
	 * Set the lund id. This is not needed for swimming, but is useful for ced or
	 * when MonteCarlo truth is known.
	 * 
	 * @param lundId the Lund Id.
	 */
	public void setLundId(LundId lundId) {
		_lundId = lundId;
	}

	/**
	 * Get the lund id. This is not needed for swimming, and may be
	 * <code>null</code>. It is useful for ced or when MonteCarlo truth is known.
	 * return the Lund Id.
	 */
	public LundId getLundId() {
		return _lundId;
	}

	/**
	 * Get the underlying generated particle record
	 * 
	 * @return the underlying generated particle record
	 */
	public GeneratedParticleRecord getGeneratedParticleRecord() {
		return _genPartRec;
	}

	/**
	 * Get the original theta for this trajectory
	 * 
	 * @return the original theta for this trajectory in degrees
	 */
	public double getOriginalTheta() {
		return _genPartRec.getTheta();
	}

	/**
	 * Get the original phi for this trajectory
	 * 
	 * @return the original phi for this trajectory in degrees
	 */
	public double getOriginalPhi() {
		return _genPartRec.getPhi();
	}

	/**
	 * Get the r coordinate in cm for the given index
	 * 
	 * @param index the index
	 * @return the r coordinate
	 */
	public double getR(int index) {
		if ((index < 0) || (index > size())) {
			return Double.NaN;
		}

		double v[] = get(index);
		if (v == null) {
			return Double.NaN;
		}

		double x = v[0];
		double y = v[1];
		double z = v[2];

		// convert to cm
		return Math.sqrt(x * x + y * y + z * z) * 100.;
	}

	@Override
	public boolean add(double u[]) {
		if (u == null) {
			return false;
		}
		
		int dim = u.length;
		double ucopy[] = new double[dim];
		System.arraycopy(u, 0, ucopy, 0, dim);
		return super.add(ucopy);
	}
	
	
	public boolean add(double u[], double s) {
		if (u == null) {
			return false;
		}
		
		int dim = u.length;
		double ucopy[] = new double[dim+1];
		System.arraycopy(u, 0, ucopy, 0, dim);
		ucopy[dim] = s;
		return super.add(ucopy);
	}

	
	/**
	 * @param xo       the x vertex position in m
	 * @param yo       the y vertex position in m
	 * @param zo       the z vertex position in m
	 * @param momentum initial momentum in GeV/c
	 * @param theta    initial polar angle in degrees
	 * @param phi      initial azimuthal angle in degrees
	 */
	public void add(double xo, double yo, double zo, double momentum, double theta, double phi) {
		double thetRad = Math.toRadians(theta);
		double phiRad = Math.toRadians(phi);

		double pz = Math.cos(thetRad);
		double rho = Math.sin(thetRad);
		double px = rho * Math.cos(phiRad);
		double py = rho * Math.sin(phiRad);
		double v[] = new double[6];
		v[0] = xo;
		v[1] = yo;
		v[2] = zo;
		v[3] = px;
		v[4] = py;
		v[5] = pz;
		add(v);
	}

	/**
	 * Get the average phi for this trajectory based on positions, not directions
	 * 
	 * @return the average phi value in degrees
	 */
	public double getAveragePhi() {

		double phi = getOriginalPhi();
		if (size() < 6) {
			return phi;
		}

		double count = 1;

		int step = 1;
		for (int i = step; i < size(); i += step) {
			double pos[] = get(i);
			double x = pos[X_IDX];
			double y = pos[Y_IDX];
			double tp = FastMath.atan2Deg(y, x);

			phi += tp;
			count++;
		}

		return phi / count;
	}

	/**
	 * Get the last element
	 * 
	 * @return the last element
	 */
	public double[] lastElement() {
		if (isEmpty()) {
			return null;
		}
		return get(size() - 1);
	}

	/**
	 * Get the final radial coordinate
	 * 
	 * @return final radial coordinate in meters
	 */
	public double getFinalR() {
		if (isEmpty()) {
			return Double.NaN;
		}

		double pos[] = getFinalPosition();
		double x = pos[X_IDX];
		double y = pos[Y_IDX];
		double z = pos[Z_IDX];
		return Math.sqrt(x * x + y * y + z * z);
	}

	/**
	 * Get the final position
	 * 
	 * @return the final position in x, y, z
	 */
	final double[] getFinalPosition() {
		if (isEmpty()) {
			return null;
		}
		double[] pos = new double[3];
		double lastQ[] = get(this.size() - 1);
//		double lastQ[] = lastElement();

		for (int i = 0; i < 3; i++) {
			pos[i] = lastQ[i];
		}
		return pos;
	}

	/**
	 * Get the total BDL integral if computed
	 * 
	 * @return the total BDL integral in kG-m
	 */
	public double getComputedBDL() {
		if (!_computedBDL) {
			return Double.NaN;
		}

		if (this.size() < 1) {
			return 0;
		}

		return this.lastElement()[BXDL_IDX];
	}

	/**
	 * Compute the integral B cross dl. This will cause the state vector arrays to
	 * expand by two, becoming [x, y, z, px/p, py/p, pz/p, l, bdl] where the 7th
	 * entry l is cumulative pathlength in m and the eighth entry bdl is the
	 * cumulative integral bdl in kG-m.
	 * 
	 * @param probe the field getter
	 */
	public void computeBDL(FieldProbe probe) {
		if (_computedBDL) {
			return;
		}

		if (probe instanceof RotatedCompositeProbe) {
			System.err.println(
					"SHOULD NOT HAPPEN. In rotated composite field probe, should not call computeBDL without the sector argument.");

			(new Throwable()).printStackTrace();
			System.exit(1);

		}

		Bxdl previous = new Bxdl();
		Bxdl current = new Bxdl();
		double[] p0 = this.get(0);
		augment(p0, 0, 0, 0);

		for (int i = 1; i < size(); i++) {
			double[] p1 = get(i);
			Bxdl.accumulate(previous, current, p0, p1, probe);

			augment(p1, current.getPathlength(), current.getIntegralBxdl(), i);
			previous.set(current);
			p0 = p1;
		}

		_computedBDL = true;
	}

	/**
	 * Compute the integral B cross dl. This will cause the state vector arrays to
	 * expand by two, becoming [x, y, z, px/p, py/p, pz/p, l, bdl] where the 7th
	 * entry l is cumulative pathlength in m and the eighth entry bdl is the
	 * cumulative integral bdl in kG-m.
	 * 
	 * @param sector sector 1..6
	 * @param probe  the field getter
	 */
	public void sectorComputeBDL(int sector, RotatedCompositeProbe probe) {
		if (_computedBDL) {
			return;
		}

		Bxdl previous = new Bxdl();
		Bxdl current = new Bxdl();
		double[] p0 = get(0);
		augment(p0, 0, 0, 0);

		for (int i = 1; i < size(); i++) {
			double[] p1 = get(i);
			Bxdl.sectorAccumulate(sector, previous, current, p0, p1, probe);

			augment(p1, current.getPathlength(), current.getIntegralBxdl(), i);

			previous.set(current);
			p0 = p1;
		}
		_computedBDL = true;
	}

	// replace the 6D state vector <at the given index with
	// and 8D vector that appends pathelength (m) and integral
	// b dot dl (kg-m)
	private void augment(double p[], double pl, double bdl, int index) {
		double newp[] = new double[8];
		System.arraycopy(p, 0, newp, 0, 6);
		newp[PATHLEN_IDX] = pl;
		newp[BXDL_IDX] = bdl;

		set(index, newp);
	}

	/**
	 * Check whether the accumulated integral bdl has been computed
	 * 
	 * @return <code>true</code> if the accumulated integral bdl has been computed
	 */
	public boolean isBDLComputed() {
		return _computedBDL;
	}

	/**
	 * Get the source of the trajectory e.g. hbtracking
	 * 
	 * @return the source of the trajectory
	 */
	public String getSource() {
		return _source;
	}

	/**
	 * Set the source of the trajectory e.g. hbtracking
	 * 
	 * @param source the source of the trajectory
	 */
	public void setSource(String source) {
		_source = new String((source == null) ? "???" : source);
	}

	/**
	 * Get an array of elements of the state vector
	 * 
	 * @param index the desired element index
	 * @return the array
	 */
	public double[] getArray(int index) {
		int size = size();
		if (size < 1) {
			return null;
		}

		double array[] = new double[size];
		int i = 0;
		for (double s[] : this) {
			array[i] = s[index];
			i++;
		}
		return array;
	}

	public double[] getX() {
		double x[] = getArray(X_IDX);
		if (x != null) {
			for (int i = 0; i < x.length; i++) {
				x[i] *= 100.; // convert to cm
			}
		}
		return x;
	}

	public double[] getY() {
		double y[] = getArray(Y_IDX);
		if (y != null) {
			for (int i = 0; i < y.length; i++) {
				y[i] *= 100.; // convert to cm
			}
		}
		return y;
	}

	public double[] getZ() {
		double z[] = getArray(Z_IDX);
		if (z != null) {
			for (int i = 0; i < z.length; i++) {
				z[i] *= 100.; // convert to cm
			}
		}
		return z;
	}
	
	public void print(PrintStream ps) {
		ps.println("Number of trajectory points: " + size());
		
		int i = 0;
		for (double[] u : this) {
			++i;
			double x = u[0];
			double y = u[1];
			double z = u[2];
			double rho = FastMath.hypot(x, y);
			double phi = FastMath.atan2Deg(y, x);
			String str = String.format("[%d]    x (m): %-8.4f  y (m): %-8.4f  z (m): %-8.4f  phi (deg):  %-7.3f,  rho (m): %-8.4f,   sector: %d", i,  x, y, z, phi, rho,  AdaptiveSwimUtilities.getSector(phi));
			ps.println(str);
		}
	}

}