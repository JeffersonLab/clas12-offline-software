package cnuphys.swim.util;

import java.io.Serializable;

public class TrajectorySummary implements Serializable {

	public int charge; // integer charge
	public double xo; // m
	public double yo; // m
	public double zo; // m
	public double momentum; // GeV/c
	public double theta; // degrees
	public double phi; // degrees
	public double[] hdata;
	public double[] finalStateVector;

	/**
	 * Create a summary
	 * 
	 * @param charge           the charge: -1 for electron, 1 for proton, etc
	 * @param xo               the x vertex position in meters
	 * @param yo               the y vertex position in meters
	 * @param zo               the z vertex position in meters
	 * @param momentum         initial momentum in GeV/c
	 * @param theta            initial polar angle in degrees
	 * @param phi              initial azimuthal angle in degrees
	 * @param hdata            if not null, should be double[3]. Upon return,
	 *                         hdata[0] is the min stepsize used (m), hdata[1] is
	 *                         the average stepsize used (m), and hdata[2] is the
	 *                         max stepsize (m) used
	 * @param finalStateVector final state vector from swimmer
	 * 
	 */
	public TrajectorySummary(int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
			double hdata[], double[] finalStateVector) {
		super();
		this.charge = charge;
		this.xo = xo;
		this.yo = yo;
		this.zo = zo;
		this.momentum = momentum;
		this.theta = theta;
		this.phi = phi;

		int len = finalStateVector.length;
		this.finalStateVector = new double[len];
		System.arraycopy(finalStateVector, 0, this.finalStateVector, 0, len);
	}

	/**
	 * Create a summary with vertex at origin
	 * 
	 * @param charge           the charge: -1 for electron, 1 for proton, etc the z
	 *                         vertex position in meters
	 * @param momentum         initial momentum in GeV/c
	 * @param theta            initial polar angle in degrees
	 * @param phi              initial azimuthal angle in degrees
	 * @param hdata            if not null, should be double[3]. Upon return,
	 *                         hdata[0] is the min stepsize used (m), hdata[1] is
	 *                         the average stepsize used (m), and hdata[2] is the
	 *                         max stepsize (m) used
	 * @param finalStateVector final state vector from swimmer
	 * 
	 */
	public TrajectorySummary(int charge, double momentum, double theta, double phi, double hdata[],
			double[] finalStateVector) {
		this(charge, 0, 0, 0, momentum, theta, phi, hdata, finalStateVector);
	}

}
