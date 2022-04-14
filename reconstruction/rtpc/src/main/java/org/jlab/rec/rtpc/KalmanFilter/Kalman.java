package org.jlab.rec.rtpc.KalmanFilter;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.util.MathUtils;
import org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.Material;
import org.jlab.rec.rtpc.KalmanFilter.Integrator.Integrator;

public class Kalman {


    /**
     * The process model used by this filter instance.
     */
    private final ProcessModel processModel;
    /**
     * Integrator for the ordinary differential equation
     */
    private final Integrator integrator;
    /**
     * The internal state estimation vector, equivalent to x hat.
     */
    private RealVector stateEstimation;
    /**
     * The error covariance matrix, equivalent to P.
     */
    private RealMatrix errorCovariance;


    /**
     * Creates a new Kalman filter with the given process and measurement models.
     *
     * @param process the model defining the underlying process dynamics
     */
    public Kalman(final ProcessModel process, final Integrator integrator, final RealVector initialStateEstimate, final RealMatrix initialErrorCovariance) {

        MathUtils.checkNotNull(process);

        this.processModel = process;
        this.integrator = integrator;
        stateEstimation = initialStateEstimate;
        errorCovariance = initialErrorCovariance;


    }


    /**
     * Predict the internal state estimation one time step ahead.
     */
    public void predict(double rMax, double h, Material material, boolean dir) {

        RealMatrix transitionMatrix = processModel.F(stateEstimation, rMax, h, integrator, material, dir);
        RealMatrix transitionMatrixT = transitionMatrix.transpose();

        // project the state estimation ahead (a priori state)
        // xHat(k)- = f(xHat(k-1))
        stateEstimation = processModel.f(stateEstimation, rMax, h, integrator, material, dir);

        // project the error covariance ahead
        // P(k)- = F * P(k-1) * F' + Q
        errorCovariance = transitionMatrix.multiply(errorCovariance)
                .multiply(transitionMatrixT)
                .add(processModel.getProcessNoise());
    }

    /**
     * Predict the internal state estimation one time step ahead.
     */
    public void newpredict(double rMax, double h, Material material, boolean dir, boolean targetNoise) {


        double[] s = {0};
        // project the state estimation ahead (a priori state)
        // xHat(k)- = f(xHat(k-1))
        stateEstimation = processModel.newf(stateEstimation, rMax, h, s, integrator, material, dir);


        RealMatrix transitionMatrix;
        if (dir) transitionMatrix = processModel.newForwardF(stateEstimation, s[0]);
        else transitionMatrix = processModel.newBackwardF(stateEstimation, s[0]);

        RealMatrix transitionMatrixT = transitionMatrix.transpose();

        RealMatrix processNoise;
        if (targetNoise) processNoise = processModel.getProcessNoiseTarget();
        else processNoise = processModel.getProcessNoise();

        // project the error covariance ahead
        // P(k)- = F * P(k-1) * F' + Q
        errorCovariance = transitionMatrix.multiply(errorCovariance)
                .multiply(transitionMatrixT)
                .add(processNoise);

        // System.out.println("stateEstimation predict = " + stateEstimation + " r = " + Math.hypot(
        //         stateEstimation.getEntry(0),stateEstimation.getEntry(1)));

    }


    /**
     * Correct the current state estimate with an actual measurement.
     *
     * @param z the measurement vector
     * @throws NullArgumentException      if the measurement vector is {@code null}
     * @throws DimensionMismatchException if the dimension of the measurement vector does not fit
     * @throws SingularMatrixException    if the covariance matrix could not be inverted
     */
    public void correct(final RealVector z, final RealMatrix measurementNoise)
            throws NullArgumentException, DimensionMismatchException, SingularMatrixException {

        // sanity checks
        MathUtils.checkNotNull(z);


        RealMatrix measurementMatrix = MeasurementModel.H(stateEstimation);
        RealMatrix measurementMatrixT = measurementMatrix.transpose();

        // S = H * P(k) * H' + R
        RealMatrix S = measurementMatrix.multiply(errorCovariance)
                .multiply(measurementMatrixT)
                .add(measurementNoise);

        // Inn = z(k) - h(xHat(k)-)
        RealVector innovation = z.subtract(MeasurementModel.h(stateEstimation));
        RealMatrix kalmanGain = errorCovariance.multiply(measurementMatrixT)
                .multiply(MatrixUtils.inverse(S));


        // update estimate with measurement z(k)
        // xHat(k) = xHat(k)- + K * Inn
        stateEstimation = stateEstimation.add(kalmanGain.operate(innovation));
        // update covariance of prediction error
        // P(k) = (I - K * H) * P(k)-
        RealMatrix identity = MatrixUtils.createRealIdentityMatrix(kalmanGain.getRowDimension());
        errorCovariance = identity.subtract(kalmanGain.multiply(measurementMatrix)).multiply(errorCovariance);



        // System.out.println("stateEstimation correct = " + stateEstimation + " r = " + Math.hypot(
        //         stateEstimation.getEntry(0),stateEstimation.getEntry(1)));

    }


    /**
     * Returns the current state estimation vector.
     *
     * @return the state estimation vector
     */
    public double[] getStateEstimation() {
        return stateEstimation.toArray();
    }

    /**
     * Returns a copy of the current state estimation vector.
     *
     * @return the state estimation vector
     */
    public RealVector getStateEstimationVector() {
        return stateEstimation.copy();
    }

}
