package org.jlab.rec.ahdc.KalmanFilter;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.jlab.clas.pdg.PhysicsConstants;
import org.jlab.geom.prim.Point3D;

public class KFitter {

	private       RealVector stateEstimation;
	private       RealMatrix errorCovariance;
	public final  Stepper    stepper;
	private final Propagator propagator;
	public        double     chi2             = 0;
	private final double     electron_mass_c2 = PhysicsConstants.massElectron() * 1000;
	private final double     proton_mass_c2   = PhysicsConstants.massProton() * 1000;


	public KFitter(final RealVector initialStateEstimate, final RealMatrix initialErrorCovariance, final Stepper stepper, final Propagator propagator) {
		this.stateEstimation = initialStateEstimate;
		this.errorCovariance = initialErrorCovariance;
		this.stepper         = stepper;
		this.propagator      = propagator;
	}

	public void predict(Indicator indicator) throws Exception {
		// Initialization
		stepper.initialize(indicator);
		Stepper stepper1 = new Stepper(stepper.y);

		// project the state estimation ahead (a priori state) : xHat(k)- = f(xHat(k-1))
		stateEstimation = propagator.f(stepper, indicator);

		// project the covariance matrix ahead
		RealMatrix transitionMatrix  = F(indicator, stepper1);
		RealMatrix transitionMatrixT = transitionMatrix.transpose();

		double px            = Math.abs(stepper.y[3]);
		double py            = Math.abs(stepper.y[4]);
		double pz            = Math.abs(stepper.y[5]);
		double p             = Math.sqrt(px * px + py * py + pz * pz);
		double mass          = proton_mass_c2;
		double kineticEnergy = Math.sqrt(mass * mass + p * p) - mass;

		double ratio = electron_mass_c2 / mass;
		double tau   = kineticEnergy / mass;
		double tmax  = 2.0 * electron_mass_c2 * tau * (tau + 2.) / (1. + 2.0 * (tau + 1.) * ratio + ratio * ratio);

		double gam   = tau + 1.0;
		double bg2   = tau * (tau + 2.0);
		double beta2 = bg2 / (gam * gam);

		double s  = stepper.s;
		double E  = Math.sqrt(p * p + mass * mass);
		double dE = Math.abs(stepper.dEdx);

		double K           = 0.000307075;
		double sigma2_dE   = indicator.material.getDensity() * K * indicator.material.getZoverA() / beta2 * tmax * s / 10 * (1.0 - beta2 / 2) * 1000 * 1000;
		double dp_prim_ddE = (E + dE) / Math.sqrt((E + dE) * (E + dE) - mass * mass);
		double sigma2_px   = Math.pow(px / p, 2) * Math.pow(dp_prim_ddE, 2) * sigma2_dE;
		double sigma2_py   = Math.pow(py / p, 2) * Math.pow(dp_prim_ddE, 2) * sigma2_dE;
		double sigma2_pz   = Math.pow(pz / p, 2) * Math.pow(dp_prim_ddE, 2) * sigma2_dE;

		double std = 1;
		RealMatrix processNoise = MatrixUtils.createRealMatrix(new double[][]{
				{0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, {0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, {0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, {0.0, 0.0, 0.0, std * sigma2_px, 0.0, 0.0}, {0.0, 0.0, 0.0, 0.0, std * sigma2_py, 0.0}, {0.0, 0.0, 0.0, 0.0, 0.0, std * sigma2_pz}});

		// project the error covariance ahead P(k)- = F * P(k-1) * F' + Q
		errorCovariance = (transitionMatrix.multiply(errorCovariance.multiply(transitionMatrixT))).add(processNoise);
	}

	public void correct(Indicator indicator) {
		RealVector z;
		RealMatrix measurementNoise;
		RealMatrix measurementMatrix;
		RealVector h;
		if (indicator.R == 0.0 && !indicator.direction) {
			measurementNoise =
					new Array2DRowRealMatrix(
							new double[][]{
									{9.00, 0.0000, 0.0000},
									{0.00, 1e10, 0.0000},
									{0.00, 0.0000, 1e10}
							});
			measurementMatrix  = H_beam(stateEstimation);
			h = h_beam(stateEstimation);
			z = indicator.hit.get_Vector_beam();
		} else {
			measurementNoise = indicator.hit.get_MeasurementNoise();
			measurementMatrix  = H(stateEstimation, indicator);
			h = h(stateEstimation, indicator);
			z = indicator.hit.get_Vector();

		}

		RealMatrix measurementMatrixT = measurementMatrix.transpose();

		// S = H * P(k) * H' + R
		RealMatrix S = measurementMatrix.multiply(errorCovariance).multiply(measurementMatrixT).add(measurementNoise);

		// Inn = z(k) - h(xHat(k)-)
		RealVector innovation = z.subtract(h);

		double chi2inc = innovation.dotProduct(MatrixUtils.inverse(S).operate(innovation));
		chi2 += chi2inc;

		RealMatrix kalmanGain = errorCovariance.multiply(measurementMatrixT).multiply(MatrixUtils.inverse(S));

		// update estimate with measurement z(k) xHat(k) = xHat(k)- + K * Inn
		stateEstimation = stateEstimation.add(kalmanGain.operate(innovation));
		// update covariance of prediction error P(k) = (I - K * H) * P(k)-
		RealMatrix identity = MatrixUtils.createRealIdentityMatrix(kalmanGain.getRowDimension());
		// Numerically more stable !!
		RealMatrix tmpMatrix = identity.subtract(kalmanGain.multiply(measurementMatrix));
		errorCovariance = tmpMatrix.multiply(errorCovariance.multiply(tmpMatrix.transpose())).add(kalmanGain.multiply(measurementNoise.multiply(kalmanGain.transpose())));

		// Give back to the stepper the new stateEstimation
		stepper.y = stateEstimation.toArray();
	}

	private RealMatrix F(Indicator indicator, Stepper stepper1) throws Exception {

		double[] dfdx  = subfunctionF(indicator, stepper1, 0);
		double[] dfdy  = subfunctionF(indicator, stepper1, 1);
		double[] dfdz  = subfunctionF(indicator, stepper1, 2);
		double[] dfdpx = subfunctionF(indicator, stepper1, 3);
		double[] dfdpy = subfunctionF(indicator, stepper1, 4);
		double[] dfdpz = subfunctionF(indicator, stepper1, 5);

		return MatrixUtils.createRealMatrix(new double[][]{
				{dfdx[0], dfdy[0], dfdz[0], dfdpx[0], dfdpy[0], dfdpz[0]}, {dfdx[1], dfdy[1], dfdz[1], dfdpx[1], dfdpy[1], dfdpz[1]}, {dfdx[2], dfdy[2], dfdz[2], dfdpx[2], dfdpy[2], dfdpz[2]}, {dfdx[3], dfdy[3], dfdz[3], dfdpx[3], dfdpy[3], dfdpz[3]}, {dfdx[4], dfdy[4], dfdz[4], dfdpx[4], dfdpy[4], dfdpz[4]}, {dfdx[5], dfdy[5], dfdz[5], dfdpx[5], dfdpy[5], dfdpz[5]}});
	}

	double[] subfunctionF(Indicator indicator, Stepper stepper1, int i) throws Exception {
		double  h             = 1e-8;
		Stepper stepper_plus  = new Stepper(stepper1.y);
		Stepper stepper_minus = new Stepper(stepper1.y);

		stepper_plus.initialize(indicator);
		stepper_minus.initialize(indicator);

		stepper_plus.y[i]  = stepper_plus.y[i] + h;
		stepper_minus.y[i] = stepper_minus.y[i] - h;

		propagator.f(stepper_plus, indicator);
		propagator.f(stepper_minus, indicator);

		double dxdi  = (stepper_plus.y[0] - stepper_minus.y[0]) / (2 * h);
		double dydi  = (stepper_plus.y[1] - stepper_minus.y[1]) / (2 * h);
		double dzdi  = (stepper_plus.y[2] - stepper_minus.y[2]) / (2 * h);
		double dpxdi = (stepper_plus.y[3] - stepper_minus.y[3]) / (2 * h);
		double dpydi = (stepper_plus.y[4] - stepper_minus.y[4]) / (2 * h);
		double dpzdi = (stepper_plus.y[5] - stepper_minus.y[5]) / (2 * h);

		return new double[]{dxdi, dydi, dzdi, dpxdi, dpydi, dpzdi};
	}

	private RealVector h(RealVector x, Indicator indicator) {

		double d = indicator.hit.distance(new Point3D(x.getEntry(0), x.getEntry(1), x.getEntry(2)));

		return MatrixUtils.createRealVector(new double[]{d});
	}

	private RealMatrix H(RealVector x, Indicator indicator) {

		double ddocadx  = subfunctionH(x, indicator, 0);
		double ddocady  = subfunctionH(x, indicator, 1);
		double ddocadz  = subfunctionH(x, indicator, 2);
		double ddocadpx = subfunctionH(x, indicator, 3);
		double ddocadpy = subfunctionH(x, indicator, 4);
		double ddocadpz = subfunctionH(x, indicator, 5);


		return MatrixUtils.createRealMatrix(new double[][]{
				{ddocadx, ddocady, ddocadz, ddocadpx, ddocadpy, ddocadpz}});
	}

	double subfunctionH(RealVector x, Indicator indicator, int i) {
		double     h       = 1e-8;
		RealVector x_plus  = x.copy();
		RealVector x_minus = x.copy();

		x_plus.setEntry(i, x_plus.getEntry(i) + h);
		x_minus.setEntry(i, x_minus.getEntry(i) - h);

		double doca_plus  = h(x_plus, indicator).getEntry(0);
		double doca_minus = h(x_minus, indicator).getEntry(0);

		return (doca_plus - doca_minus) / (2 * h);
	}

	private RealVector h_beam(RealVector x) {

		double xx = x.getEntry(0);
		double yy = x.getEntry(1);
		double zz = x.getEntry(2);

		double r = Math.hypot(xx, yy);
		double phi = Math.atan2(yy, xx);

		return MatrixUtils.createRealVector(new double[]{r, phi, zz});
	}

	private RealMatrix H_beam(RealVector x) {

		double xx = x.getEntry(0);
		double yy = x.getEntry(1);

		double drdx = (xx) / (Math.hypot(xx, yy));
		double drdy = (yy) / (Math.hypot(xx, yy));
		double drdz = 0.0;
		double drdpx = 0.0;
		double drdpy = 0.0;
		double drdpz = 0.0;

		double dphidx = -(yy) / (xx * xx + yy * yy);
		double dphidy = (xx) / (xx * xx + yy * yy);
		double dphidz = 0.0;
		double dphidpx = 0.0;
		double dphidpy = 0.0;
		double dphidpz = 0.0;

		double dzdx = 0.0;
		double dzdy = 0.0;
		double dzdz = 1.0;
		double dzdpx = 0.0;
		double dzdpy = 0.0;
		double dzdpz = 0.0;

		return MatrixUtils.createRealMatrix(
				new double[][]{
						{drdx, drdy, drdz, drdpx, drdpy, drdpz},
						{dphidx, dphidy, dphidz, dphidpx, dphidpy, dphidpz},
						{dzdx, dzdy, dzdz, dzdpx, dzdpy, dzdpz}
				});
	}


	private RealVector innovation(RealVector z, Indicator indicator) {

		return z.subtract(h(stateEstimation, indicator));
	}

	/**
	 * Returns a copy of the current state estimation vector.
	 *
	 * @return the state estimation vector
	 */
	public RealVector getStateEstimationVector() {
		return stateEstimation.copy();
	}

	public double getMomentum() {
		return Math.sqrt(stateEstimation.getEntry(3) * stateEstimation.getEntry(3) + stateEstimation.getEntry(4) * stateEstimation.getEntry(4) + stateEstimation.getEntry(5) * stateEstimation.getEntry(5));
	}

}
