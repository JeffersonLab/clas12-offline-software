package cnuphys.swimZ;

import java.util.ArrayList;

import Jama.Matrix;
import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.IField;
import cnuphys.rk4.DefaultStopper;
import cnuphys.rk4.IRkListener;
import cnuphys.rk4.IStopper;
import cnuphys.rk4.RungeKutta;
import cnuphys.rk4.RungeKuttaException;

/**
 * This static class holds the parameters and static methods for the swimZ
 * integration. The swimZ integration follows the method described for the
 * HERA-B magnet here: http://arxiv.org/pdf/physics/0511177v1.pdf<br>
 * <p>
 * The state vector has five elements: <br>
 * (x, y, tx, ty, q) <br>
 * Where x and y are the transverse coordinates (cm), tx = px/pz, ty = py/pz,
 * and q = Q/|p| where Q is the integer charge (e.g. -1 for an electron)
 * <p>
 * UNITS
 * <ul>
 * <li>x, y, and z are in cm
 * <li>p is in GeV/c
 * <li>B (mag field) is in kGauss
 * </ul>
 * <p>
 * The global parameters are:
 * <ul>
 * 
 * </ul>
 * 
 * @author heddle
 *
 */
public class SwimZ {

	/** The speed of light in these units: (GeV/c)(1/kG)(1/cm) */
	public static final double V = 0.000299792458;

	/** Argon radiation length in cm */
	public static final double ARGONRADLEN = 14.;

	// Min momentum to swim in GeV/c
	public static final double MINMOMENTUM = 5e-05;

	/** The current magnetic field */
	private IField _field;

	// create a do nothing stopper for now
	private IStopper _stopper = new DefaultStopper();
	
	/**
	 * SwimZ constructor. Here we create a Swimmer that will use the given
	 * magnetic field.
	 * 
	 * @param field
	 *            interface into a magnetic field
	 */
	public SwimZ(IField field) {
		FieldProbe probe = FieldProbe.factory(field);
		_field = (probe != null) ? probe : field;
	}
	

	/**
	 * Swim to a fixed z over short distances using RK adaptive stepsize
	 * 
	 * @param Q
	 *            the integer charge of the particle (-1 for electron)
	 * @param p
	 *            the momentum in Gev/c
	 * @param start
	 *            the starting state vector
	 * @param zf
	 *            the final z value
	 * @param stepSize
	 *            the initial step size
	 * @param relTolerance
	 *            the absolute tolerances on each state variable [x, y, tx, ty]
	 *            (q = const). So it is an array with four entries, like [1.0e-4
	 *            cm, 1.0e-4 cm, 1.0e-5, 1.0e05]
	 * @param hdata
	 *            An array with three elements. Upon return it will have the
	 *            min, average, and max step size (in that order).
	 * @return the swim result
	 * @throws SwimZException
	 */
	public SwimZResult adaptiveRK(int Q,
			double p,
			SwimZStateVector start,
			final double zf,
			double stepSize,
			double absError[],
			double hdata[]) throws SwimZException {
		if (start == null) {
			throw new SwimZException("Null starting state vector.");
		}

		// straight line?
		if ((Q == 0) || (_field == null) || _field.isZeroField()) {
			System.out.println("Z adaptive swimmer detected straight line.");
			return straightLineResult(Q, p, start, zf);
		}

		// need a new derivative
		SwimZDerivative deriv = new SwimZDerivative(Q, p, _field);

		// need a RK4 object
		RungeKutta rk4 = new RungeKutta();

		double yo[] = { start.x, start.y, start.tx, start.ty };

		// create the lists to hold the trajectory
		ArrayList<Double> z = new ArrayList<Double>(100);
		ArrayList<double[]> y = new ArrayList<double[]>(100);

		int nStep = 0;
		try {
			nStep = rk4.adaptiveStepToTf(yo, start.z, zf, stepSize, z, y, deriv, _stopper, absError, hdata);
		}
		catch (RungeKuttaException e) {
			e.printStackTrace();
		}

		if (nStep == 0) {
			return null;
		}

		SwimZResult result = new SwimZResult(Q, p, start.z, zf, nStep);
		result.add(start);
		for (int i = 0; i < z.size(); i++) {
			double v[] = y.get(i);
			SwimZStateVector sv = new SwimZStateVector(z.get(i), v);
			result.add(sv);
		}

		return result;

	}

	/**
	 * Swim to a fixed z over short distances using RK adaptive stepsize
	 * 
	 * @param Q
	 *            the integer charge of the particle (-1 for electron)
	 * @param p
	 *            the momentum in Gev/c
	 * @param start
	 *            the starting state vector
	 * @param stop
	 *            will hold the final state vector
	 * @param zf
	 *            the final z value
	 * @param stepSize
	 *            the initial step size
	 * @param relTolerance
	 *            the absolute tolerances on each state variable [x, y, tx, ty]
	 *            (q = const). So it is an array with four entries, like [1.0e-4
	 *            cm, 1.0e-4 cm, 1.0e-5, 1.0e05]
	 * @param hdata
	 *            An array with three elements. Upon return it will have the
	 *            min, average, and max step size (in that order).
	 * @return the number of steps
	 * @throws SwimZException
	 */
	public int adaptiveRK(int Q,
			double p,
			SwimZStateVector start,
			SwimZStateVector stop,
			final double zf,
			double stepSize,
			double absError[],
			double hdata[]) throws SwimZException {
		if (start == null) {
			throw new SwimZException("Null starting state vector.");
		}

		// straight line?
		if ((Q == 0) || (_field == null) || _field.isZeroField()) {
			System.out.println("Z adaptive swimmer detected straight line.");
			straightLineResult(Q, p, start, stop, zf);
			return 2;
		}

		// need a new derivative
		SwimZDerivative deriv = new SwimZDerivative(Q, p, _field);

		// need a RK4 object
		RungeKutta rk4 = new RungeKutta();

		double yo[] = { start.x, start.y, start.tx, start.ty };

		IRkListener listener = new IRkListener() {

			@Override
			public void nextStep(double newZ, double[] newStateVec, double h) {
				stop.x = newStateVec[0];
				stop.y = newStateVec[1];
				stop.tx = newStateVec[2];
				stop.tx = newStateVec[3];
				stop.z = newZ;
			}

		};

		int nStep = 0;
		try {
			nStep = rk4.adaptiveStepToTf(yo, start.z, zf, stepSize, deriv, _stopper, listener, absError, hdata);
		}
		catch (RungeKuttaException e) {
			e.printStackTrace();
		}

		return nStep;
	}

	/**
	 * Transport to a fixed z over short distances using RK adaptive stepsize
	 * 
	 * @param Q
	 *            the integer charge of the particle (-1 for electron)
	 * @param p
	 *            the momentum in Gev/c
	 * @param start
	 *            the starting state vector
	 * @param covMat
	 *            the initial covariance matrix
	 * @param zf
	 *            the final z value
	 * @param stepSize
	 *            the initial step size
	 * @param relTolerance
	 *            the absolute tolerances on each state variable [x, y, tx, ty]
	 *            (q = const). So it is an array with four entries, like [1.0e-4
	 *            cm, 1.0e-4 cm, 1.0e-5, 1.0e05]
	 * @param hdata
	 *            An array with three elements. Upon return it will have the
	 *            min, average, and max step size (in that order).
	 * @throws SwimZException
	 */

	public int transport(int Q,
			double p,
			final SwimZStateVector start,
			SwimZStateVector stop,
			Matrix covMat,
			final double zf,
			double stepSize,
			double absError[],
			double hdata[]) throws SwimZException {

		if (start == null) {
			throw new SwimZException("Null starting state vector.");
		}

		if (covMat == null) {
			throw new SwimZException("Null starting covariance matrix.");
		}

		// initialize
		stop.copy(start);

		// need a new derivative
		SwimZDerivative deriv = new SwimZDerivative(Q, p, _field);

		// need a RK4 object
		RungeKutta rk4 = new RungeKutta();

		double yo[] = { start.x, start.y, start.tx, start.ty };

		// listens for each step
		IRkListener listener = new IRkListener() {

			@Override
			public void nextStep(double newZ, double[] newStateVec, double s) {

				double x = stop.x;
				double y = stop.y;
				double z = stop.z;
				double tx = stop.tx;
				double ty = stop.ty;

				B bf = new B(z, x, y, tx, ty);

				double[] A = A(tx, ty, bf.Bx, bf.By, bf.Bz);
				double[] dA = delA_delt(tx, ty, bf.Bx, bf.By, bf.Bz);

				// transport covMat
				double delx_deltx0 = s;
				double dely_deltx0 = 0.5 * Q * V * s * s * dA[2];
				double deltx_delty0 = Q * V * s * dA[1];
				double delx_delQ = 0.5 * V * s * s * A[0];
				double deltx_delQ = V * s * A[0];
				double delx_delty0 = 0.5 * Q * V * s * s * dA[1];
				double dely_delty0 = s;
				double delty_deltx0 = Q * V * s * dA[2];
				double dely_delQ = 0.5 * V * s * s * A[1];
				double delty_delQ = V * s * A[1];

				double[][] transpStateJacobian = new double[][] { { 1, 0, delx_deltx0, delx_delty0, delx_delQ },
						{ 0, 1, dely_deltx0, dely_delty0, dely_delQ }, { 0, 0, 1, deltx_delty0, deltx_delQ },
						{ 0, 0, delty_deltx0, 1, delty_delQ }, { 0, 0, 0, 0, 1 } };

				double[][] u = new double[5][5];
				double[][] c = new double[covMat.getRowDimension()][covMat.getColumnDimension()];
				double[][] C = new double[covMat.getRowDimension()][covMat.getColumnDimension()];

				for (int rw = 0; rw < covMat.getRowDimension(); rw++) {
					for (int cl = 0; cl < covMat.getColumnDimension(); cl++) {
						c[rw][cl] = covMat.get(rw, cl);
					}
				}
				
				 //covMat = FCF^T; u = FC;
				for(int j1 = 0; j1 < 5; j1++) {
					u[0][j1] = c[0][j1] + c[2][j1]*transpStateJacobian[0][2] + c[3][j1]*transpStateJacobian[0][3] + c[4][j1]*transpStateJacobian[0][4];
					u[1][j1] = c[1][j1] + c[2][j1]*transpStateJacobian[1][2] + c[3][j1]*transpStateJacobian[1][3] + c[4][j1]*transpStateJacobian[1][4];
					u[2][j1] = c[2][j1] + c[3][j1]*transpStateJacobian[2][3] + c[4][j1]*transpStateJacobian[2][4];
					u[3][j1] = c[2][j1]*transpStateJacobian[3][2] + c[3][j1] + c[4][j1]*transpStateJacobian[3][4];
					u[4][j1] = c[4][j1];		
				}
				
				for(int i1 = 0; i1 < 5; i1++) {
					C[i1][0] = u[i1][0] + u[i1][2]*transpStateJacobian[0][2] + u[i1][3]*transpStateJacobian[0][3] + u[i1][4]*transpStateJacobian[0][4];
					C[i1][1] = u[i1][1] + u[i1][2]*transpStateJacobian[1][2] + u[i1][3]*transpStateJacobian[1][3] + u[i1][4]*transpStateJacobian[1][4];
					C[i1][2] = u[i1][2] + u[i1][3]*transpStateJacobian[2][3] + u[i1][4]*transpStateJacobian[2][4];
					C[i1][3] = u[i1][2]*transpStateJacobian[3][2] + u[i1][3] + u[i1][4]*transpStateJacobian[3][4];
					C[i1][4] = u[i1][4];
				}


				// Q	
				double p = Math.abs(1./Q);
			    double pz = p/Math.sqrt(1 + tx*tx + ty*ty);
			    double px = tx*pz;
			    double py = ty*pz;
			    
			    double t_ov_X0 = Math.signum(zf-start.z)*s/ARGONRADLEN; //path length in radiation length units = t/X0 [true path length/ X0] ; Ar radiation length = 14 cm
			    
			    //double mass = this.MassHypothesis(this.massHypo); // assume given mass hypothesis
			    double mass = MassHypothesis("electron"); // assume given mass hypothesis
			    if(Q>0)
			    	mass = MassHypothesis("proton");
			    
			    double beta = p/Math.sqrt(p*p+mass*mass); // use particle momentum
			    double cosEntranceAngle = Math.abs((x*px+y*py+z*pz)/(Math.sqrt(x*x+y*y+z*z)*p));
			    double pathLength = t_ov_X0/cosEntranceAngle;  
			   
			    double sctRMS = (0.0136/(beta*p))*Math.sqrt(pathLength)*(1+0.038*Math.log(pathLength)); // Highland-Lynch-Dahl formula
			   
			    double cov_txtx = (1+tx*tx)*(1 + tx*tx + ty*ty)*sctRMS*sctRMS;
			    double cov_tyty = (1+ty*ty)*(1 + tx*tx + ty*ty)*sctRMS*sctRMS;
			    double cov_txty = tx*ty*(1 + tx*tx + ty*ty)*sctRMS*sctRMS;
			    
			    if(s>0) { 
			    	C[2][2]+=cov_txtx;
			    	C[2][3]+=cov_txty;
			    	C[3][2]+=cov_txty;
			    	C[3][3]+=cov_tyty;
			    }
			    
			    Matrix Cpropagated = new Matrix(C);	
			    matrixCopy(Cpropagated, covMat);
			    
			    stop.set(newZ, newStateVec);


			}
		};

		int nStep = 0;
		try {
			nStep = rk4.adaptiveStepToTf(yo, start.z, zf, stepSize, deriv, _stopper, listener, absError, hdata);
		}
		catch (RungeKuttaException e) {
			e.printStackTrace();
		}

		return nStep;
	}
	
	private void matrixCopy(Matrix source, Matrix dest) {
		for (int rw = 0; rw < source.getRowDimension(); rw++) {
			for (int cl = 0; cl < source.getColumnDimension(); cl++) {
				dest.set(rw, cl, source.get(rw, cl));
			}
		}
	}

	// /**
	// * Integrator that uses the RungeKutta advance with a Butcher Tableau and
	// * adaptive stepsize and a tolerance vector.
	// *
	// * This version uses an IRk4Listener to notify
	// * the listener that the next step has been advanced.
	// *
	// * A very typical case is a 2nd order ODE converted to a 1st order where
	// the
	// * dependent variables are x, y, z, vx, vy, vz and the independent
	// variable is
	// * time.
	// *
	// * @param yo initial values. Probably something like (xo, yo, zo, vxo,
	// vyo,
	// * vzo).
	// * @param to the initial value of the independent variable, e.g., time.
	// * @param tf the maximum value of the independent variable.
	// * @param h the starting steps size
	// * @param t a list of the values of t at each step
	// * @param y a list of the values of the state vector at each step
	// * @param deriv the derivative computer (interface). This is where the
	// problem specificity resides.
	// * @param stopper if not <code>null</code> will be used to exit the
	// * integration early because some condition has been reached.
	// * @param tableau the Butcher Tableau
	// * @param relTolerance the error tolerance as fractional diffs. Note it is
	// a vector, the same
	// * dimension of the problem, e.g., 6 for [x,y,z,vx,vy,vz].
	// * @param hdata if not null, should be double[3]. Upon return, hdata[0] is
	// the min stepsize
	// * used, hdata[1] is the average stepsize used, and hdata[2] is the max
	// stepsize used
	// * @return the number of steps used.
	// * @throws RungeKuttaException
	// */
	// public int adaptiveStep(double yo[], double to, double tf, double h,
	// final List<Double> t, final List<double[]> y, IDerivative deriv,
	// IStopper stopper, ButcherTableau tableau, double relTolerance[],
	// double hdata[]) throws RungeKuttaException {

	/**
	 * Swim to a fixed z over short distances using uniform stepsize RK4
	 * 
	 * @param Q
	 *            the integer charge of the particle (-1 for electron)
	 * @param p
	 *            the momentum in Gev/c
	 * @param start
	 *            the starting state vector
	 * @param zf
	 *            the final z value
	 * @param stepSize
	 *            the step size
	 * @return the swim result
	 * @throws SwimZException
	 */
	public SwimZResult uniformRK4(int Q, double p, SwimZStateVector start, double zf, double stepSize)
			throws SwimZException {
		if (start == null) {
			throw new SwimZException("Null starting state vector.");
		}

		// straight line?
		if (Q == 0) {
			System.out.println("Z uniform swimmer detected straight line.");
			return straightLineResult(Q, p, start, zf);
		}

		// need a new derivative
		SwimZDerivative deriv = new SwimZDerivative(Q, p, _field);

		// need a RK4 object
		RungeKutta rk4 = new RungeKutta();

		// obtain a range
		SwimZRange swimZrange = new SwimZRange(start.z, zf, stepSize);

		// create a do nothing stopper for now
		IStopper stopper = new DefaultStopper();

		// RK4 storage
		int nDim = 4; // should be able to make 4 since q = const
		int nStep = swimZrange.getNumStep();

		double yo[] = { start.x, start.y, start.tx, start.ty };
		double z[] = new double[nStep];
		double y[][] = new double[nDim][nStep];
		nStep = rk4.uniformStep(yo, start.z, zf, y, z, deriv, stopper);

		SwimZResult result = new SwimZResult(Q, p, start.z, zf, nStep + 1);
		result.add(start);
		for (int i = 0; i < nStep; i++) {

			double v[] = { y[0][i], y[1][i], y[2][i], y[3][i] };
			SwimZStateVector sv = new SwimZStateVector(z[i], v);
			result.add(sv);
		}

		return result;
	}
	
	/**
	 * Swim to a fixed z over short distances using uniform stepsize RK4
	 * 
	 * This version only returns the end points
	 * 
	 * @param Q
	 *            the integer charge of the particle (-1 for electron)
	 * @param p
	 *            the momentum in Gev/c
	 * @param start
	 *            the starting state vector
	 * @param zf
	 *            the final z value
	 * @param stepSize
	 *            the step size
	 * @return the swim result
	 * @throws SwimZException
	 */
	public SwimZResult uniformRK4EPOnly(int Q, double p, SwimZStateVector start, double zf, double stepSize)
			throws SwimZException {
		if (start == null) {
			throw new SwimZException("Null starting state vector.");
		}

		// straight line?
		if (Q == 0) {
			System.out.println("Z uniform swimmer detected straight line.");
			return straightLineResult(Q, p, start, zf);
		}

		// need a new derivative
		SwimZDerivative deriv = new SwimZDerivative(Q, p, _field);

		// need a RK4 object
		RungeKutta rk4 = new RungeKutta();

		// obtain a range
		SwimZRange swimZrange = new SwimZRange(start.z, zf, stepSize);

		// create a do nothing stopper for now
		IStopper stopper = new DefaultStopper();

		// RK4 storage
		int nDim = 4; // should be able to make 4 since q = const
		int nStep = swimZrange.getNumStep();
//System.err.println("NSTEP = " + nStep);
		double yo[] = { start.x, start.y, start.tx, start.ty };
		double z[] = new double[2];
		double y[][] = new double[nDim][2];
		nStep = rk4.uniformStepEPOnly(nStep, yo, start.z, zf, y, z, deriv, stopper);

		SwimZResult result = new SwimZResult(Q, p, start.z, zf, 2);
		result.add(start);
		for (int i = 0; i < nStep; i++) {
//System.err.println("MAKING RESULT: i = " +  i);
			if (i == (nStep - 1)) {
				double v[] = { y[0][1], y[1][1], y[2][1], y[3][1] };
				SwimZStateVector sv = new SwimZStateVector(z[1], v);
				result.add(sv);
			}
		}

		return result;
	}


	/**
	 * Swim to a fixed z over short distances using a parabolic estimate
	 * 
	 * @param Q
	 *            the integer charge of the particle (-1 for electron)
	 * @param p
	 *            the momentum in Gev/c
	 * @param start
	 *            the starting state vector
	 * @param zf
	 *            the final z value
	 * @param stepSize
	 *            the step size
	 * @return the swim result
	 * @throws SwimZException
	 */

	public SwimZResult parabolicEstimate(int Q, double p, SwimZStateVector start, double zf, double stepSize)
			throws SwimZException {

		if (start == null) {
			throw new SwimZException("Null starting state vector.");
		}

		// straight line?
		if (Q == 0) {
			return straightLineResult(Q, p, start, zf);
		}

		double q = Q / p;

		// obtain a range
		SwimZRange swimZrange = new SwimZRange(start.z, zf, stepSize);

		// storage for results
		SwimZResult result = new SwimZResult(Q, p, start.z, zf, swimZrange.getNumStep() + 1);
		result.add(start);
		SwimZStateVector v0 = start;

		for (int i = 0; i < swimZrange.getNumStep(); i++) {
			// get the field
			float B[] = new float[3];
			double x0 = v0.x;
			double y0 = v0.y;
			double z0 = v0.z;
			double tx0 = v0.tx;
			double ty0 = v0.ty;

			_field.field((float) x0, (float) y0, (float) z0, B);

			// some needed factors
			double txsq = tx0 * tx0;
			double tysq = ty0 * ty0;
			double fact = Math.sqrt(1 + txsq + tysq);
			double Ax = fact * (ty0 * (tx0 * B[0] + B[2]) - (1 + txsq) * B[1]);
			double Ay = fact * (-tx0 * (ty0 * B[1] + B[2]) + (1 + tysq) * B[0]);

			double s = stepSize;
			double qvs = q * V * s;
			double qvsq = 0.5 * qvs * s;

			double x1 = x0 + tx0 * s + qvsq * Ax;
			double y1 = y0 + ty0 * s + qvsq * Ay;
			double tx1 = tx0 + qvs * Ax;
			double ty1 = ty0 + qvs * Ay;
			// public SwimZStateVector(double x, double y, double z, double tx,
			// double ty,
			// double q) {

			SwimZStateVector v1 = new SwimZStateVector(x1, y1, swimZrange.z(i + 1), tx1, ty1);

			// add to the resuts
			result.add(v1);
			v0 = v1;
		}

		return result;
	}

	// straight line
	private SwimZResult straightLineResult(int Q, double p, SwimZStateVector start, double zf) {
		SwimZResult result = new SwimZResult(Q, p, start.z, zf, 2);
		result.add(start);
		double s = zf - start.z;
		double x1 = start.x + start.tx * s;
		double y1 = start.y + start.ty * s;
		SwimZStateVector v = new SwimZStateVector(x1, y1, zf, start.tx, start.ty);
		result.add(v);
		return result;
	}

	// straight line
	private void straightLineResult(int Q, double p, SwimZStateVector start, SwimZStateVector stop, double zf) {
		double s = zf - start.z;
		stop.x = start.x + start.tx * s;
		stop.y = start.y + start.ty * s;
		stop.z = zf;
		stop.tx = start.tx;
		stop.ty = start.ty;
	}

	private double[] A(double tx, double ty, double Bx, double By, double Bz) {

		double C = Math.sqrt(1 + tx * tx + ty * ty);
		double Ax = C * (ty * (tx * Bx + Bz) - (1 + tx * tx) * By);
		double Ay = C * (-tx * (ty * By + Bz) + (1 + ty * ty) * Bx);

		return new double[] { Ax, Ay };
	}

	private double[] delA_delt(double tx, double ty, double Bx, double By, double Bz) {

		double C2 = 1 + tx * tx + ty * ty;
		double C = Math.sqrt(C2);
		double Ax = C * (ty * (tx * Bx + Bz) - (1 + tx * tx) * By);
		double Ay = C * (-tx * (ty * By + Bz) + (1 + ty * ty) * Bx);

		double delAx_deltx = tx * Ax / C2 + C * (ty * Bx - 2 * tx * By);
		double delAx_delty = ty * Ax / C2 + C * (tx * Bx + Bz);
		double delAy_deltx = tx * Ay / C2 + C * (-ty * By - Bz);
		double delAy_delty = ty * Ay / C2 + C * (-tx * By + 2 * ty * Bx);

		return new double[] { delAx_deltx, delAx_delty, delAy_deltx, delAy_delty };
	}

	// container for mag field
	private class B {
		final double z;
		double x;
		double y;
		double tx;
		double ty;
		float result[] = new float[3];

		public double Bx;
		public double By;
		public double Bz;

		public B(SwimZStateVector sv) {
			this(sv.z, sv.x, sv.y, sv.tx, sv.ty);
		}

		public B(double z, double x, double y, double tx, double ty) {
			this.z = z;
			this.x = x;
			this.y = y;
			this.tx = tx;
			this.ty = ty;

			if ((_field == null) || _field.isZeroField()) {
				this.Bx = 0;
				this.By = 0;
				this.Bz = 0;
			}
			else {
				_field.field((float) x, (float) y, (float) z, result);
			}

			this.Bx = result[0];
			this.By = result[1];
			this.Bz = result[2];
		}
	}

	public String massHypo = "electron";

	public double MassHypothesis(String H) {
		double piMass = 0.13957018;
		double KMass = 0.493677;
		double muMass = 0.105658369;
		double eMass = 0.000510998;
		double pMass = 0.938272029;
		double value = piMass; // default
		if (H.equals("proton"))
			value = pMass;
		if (H.equals("electron"))
			value = eMass;
		if (H.equals("pion"))
			value = piMass;
		if (H.equals("kaon"))
			value = KMass;
		if (H.equals("muon"))
			value = muMass;
		return value;
	}

}
