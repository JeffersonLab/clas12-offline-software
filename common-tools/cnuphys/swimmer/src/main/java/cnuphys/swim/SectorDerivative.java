package cnuphys.swim;

import cnuphys.magfield.RotatedCompositeProbe;
import cnuphys.rk4.IDerivative;

/**
 * Used for swimming in the sector system with a rotated composite field
 * 
 * @author heddle
 *
 */
public class SectorDerivative implements IDerivative {

	private RotatedCompositeProbe _rcProbe;

	// the sector [1..6]
	private int _sector;

	private double _momentum; // GeV/c

	// alpha is qe/p where q is the integer charge,
	// e is the electron charge = (10^-9) in GeV/(T*m)
	// p is in GeV/c
	private double _alpha;

	// for mag field result
	float b[] = new float[3];

	/**
	 * The derivative for swimming through a magnetic field
	 * 
	 * @param sector   the sector [1..6]
	 * @param charge   -1 for electron, +1 for proton, etc.
	 * @param momentum the magnitude of the momentum.
	 * @param field    the magnetic field
	 */
	public SectorDerivative(int sector, int charge, double momentum, RotatedCompositeProbe rcProbe) {
		_sector = sector;
		_rcProbe = rcProbe;
		_momentum = momentum;
		// units of this alpha are 1/(T*m)
		_alpha = 1.0e-9 * charge * Swimmer.C / _momentum;
	}

	public void set(int sector, int charge, double momentum, RotatedCompositeProbe rcProbe) {
		_rcProbe = rcProbe;
		_sector = sector;
		_momentum = momentum;
		// units of this alpha are 1/(T*m)
		_alpha = 1.0e-9 * charge * Swimmer.C / _momentum;
	}

	/**
	 * Compute the derivatives given the value of s (path length) and the values of
	 * the state vector.
	 * 
	 * @param s    the value of the independent variable path length (input).
	 * @param Q    the values of the state vector ([x,y,z, px/p, py/p, pz/p]) at s
	 *             (input).
	 * @param dydt will be filled with the values of the derivatives at t (output).
	 */
	@Override
	public void derivative(double s, double[] Q, double[] dQds) {
		double Bx = 0.0;
		double By = 0.0;
		double Bz = 0.0;

		if (_rcProbe != null) {

			float b[] = new float[3];

			// convert to cm
			double xx = Q[0] * 100;
			double yy = Q[1] * 100;
			double zz = Q[2] * 100;

			_rcProbe.field(_sector, (float) xx, (float) yy, (float) zz, b);

			// convert to tesla
			Bx = b[0] / 10.0;
			By = b[1] / 10.0;
			Bz = b[2] / 10.0;
		}

		dQds[3] = _alpha * (Q[4] * Bz - Q[5] * By); // vyBz-vzBy
		dQds[4] = _alpha * (Q[5] * Bx - Q[3] * Bz); // vzBx-vxBz
		dQds[5] = _alpha * (Q[3] * By - Q[4] * Bx); // vxBy-vyBx
		dQds[0] = Q[3];
		dQds[1] = Q[4];
		dQds[2] = Q[5];
	}

}
