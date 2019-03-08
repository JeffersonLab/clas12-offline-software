package cnuphys.swimZ;

import cnuphys.magfield.FastMath;
import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.RotatedCompositeProbe;

public class SectorSwimZDerivative extends SwimZDerivative {

	private int _sector = 0;

	/**
	 * Set the sector [1..6]
	 * 
	 * @param sector the sector [1..6]
	 */
	public void setSector(int sector) {
		_sector = sector;
	}

	/**
	 * Set the parameters
	 * 
	 * @param sect  the sector [1..6]
	 * @param Q     -1 for electron, +1 for proton, etc.
	 * @param p     the magnitude of the momentum in GeV/c.
	 * @param probe the magnetic field getter
	 */
	public void set(int sector, int Q, double p, FieldProbe probe) {
		_sector = sector;
		_probe = probe;
		set(Q, p, probe);
	}

	/**
	 * Compute the derivatives given the value of the independent variable and the
	 * values of the function. Think of the Differential Equation as being dydt =
	 * f[y,t].
	 * 
	 * @param z    the value of the independent variable (the z coordinate) (input).
	 * @param x    the values of the state vector (x, y, tx, ty, q) at z (input).
	 * @param dxdz will be filled with the values of the derivatives at z (output).
	 */
	@Override
	public void derivative(double z, double[] x, double[] dxdz) {

		// get the field
		((RotatedCompositeProbe) _probe).field(_sector, (float) x[0], (float) x[1], (float) z, B);

		// some needed factors
		double tx = x[2];
		double ty = x[3];
		double txsq = tx * tx;
		double tysq = ty * ty;
		double fact = FastMath.sqrt(1 + txsq + tysq);
//		double txty = tx*ty;
		double Ax = fact * (ty * (tx * B[0] + B[2]) - (1 + txsq) * B[1]);
		double Ay = fact * (-tx * (ty * B[1] + B[2]) + (1 + tysq) * B[0]);
//		double Ax = fact * (txty * B[0] + ty*B[2] - (1 + txsq) * B[1]);
//		double Ay = fact * (-txty * B[1] - tx*B[2] + (1 + tysq) * B[0]);

		dxdz[0] = tx;
		dxdz[1] = ty;
		dxdz[2] = _qv * Ax;
		dxdz[3] = _qv * Ay;
	}

}
