package org.jlab.rec.rtpc.KalmanFilter;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.util.MathUtils;
import org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.Material;
import org.jlab.rec.rtpc.KalmanFilter.Integrator.Integrator;

/*************************************************************************************************************
 *  Class for Discrete Extended Kalman Filter
 *  The system to be estimated is defined as a discrete nonlinear dynamic dystem:
 *              x(k) = f[x(k-1), u(k-1)] + v(k)     ; x = Nx1,    u = Mx1
 *              y(k) = h[x(k)] + n(k)               ; y = Zx1
 *
 *        Where:
 *          x(k) : State Variable at time-k                          : Nx1
 *          y(k) : Measured output at time-k                         : Zx1
 *          u(k) : System input at time-k                            : Mx1
 *          v(k) : Process noise, AWGN assumed, w/ covariance Qn     : Nx1
 *          n(k) : Measurement noise, AWGN assumed, w/ covariance Rn : Nx1
 *
 *          f(..), h(..) is a nonlinear transformation of the system to be estimated.
 *
 ***************************************************************************************************
 *      Extended Kalman Filter algorithm:
 *          Initialization:
 *              x(k=0|k=0) = Expected value of x at time-0 (i.e. x(k=0)), typically set to zero.
 *              P(k=0|k=0) = Identity matrix * covariant(P(k=0)), typically initialized with some
 *                            big number.
 *              Q, R       = Covariance matrices of process & measurement. As this implementation
 *                            the noise as AWGN (and same value for every variable), this is set
 *                            to Q=diag(QInit,...,QInit) and R=diag(RInit,...,RInit).
 *
 *
 *          EKF Calculation (every sampling time):
 *              Calculate the Jacobian matrix of f (i.e. F):
 *                  F = d(f(..))/dx |x(k-1|k-1),u(k-1)                               ...{EKF_1}
 *
 *              Predict x(k) through nonlinear function f:
 *                  x(k|k-1) = f[x(k-1|k-1), u(k-1)]                                 ...{EKF_2}
 *
 *              Predict P(k) using linearized f (i.e. F):
 *                  P(k|k-1)  = F*P(k-1|k-1)*F' + Q                                  ...{EKF_3}
 *
 *              Calculate the Jacobian matrix of h (i.e. C):
 *                  C = d(h(..))/dx |x(k|k-1)                                        ...{EKF_4}
 *
 *              Predict residual covariance S using linearized h (i.e. H):
 *                  S       = C*P(k|k-1)*C' + R                                      ...{EKF_5}
 *
 *              Calculate the kalman gain:
 *                  K       = P(k|k-1)*C'*(S^-1)                                     ...{EKF_6}
 *
 *              Correct x(k) using kalman gain:
 *                  x(k|k) = x(k|k-1) + K*[y(k) - h(x(k|k-1))]                       ...{EKF_7}
 *
 *              Correct P(k) using kalman gain:
 *                  P(k|k)  = (I - K*C)*P(k|k-1)                                     ...{EKF_8}
 *
 *
 *
 *
 ************************************************************************************************************/

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
     * @param process                the model defining the underlying process dynamics.
     * @param integrator             integrator use for the prediction
     * @param initialStateEstimate   Expected value of x at time-0 (i.e. x(k=0))
     * @param initialErrorCovariance covariant(P(k=0))
     */
    public Kalman(final ProcessModel process, final Integrator integrator, final RealVector initialStateEstimate, final RealMatrix initialErrorCovariance) {

        MathUtils.checkNotNull(process);

        this.processModel = process;
        this.integrator = integrator;
        this.stateEstimation = initialStateEstimate;
        this.errorCovariance = initialErrorCovariance;


    }


    /**
     * Predict the internal state estimation one time step ahead.
     */
    public void predict(double rMax, double h, Material material, boolean dir) {


        double[] s = {0};
        double[] totalEnergyLoss = {0};

        // project the state estimation ahead (a priori state)
        // xHat(k)- = f(xHat(k-1))
        stateEstimation = processModel.f(stateEstimation, rMax, h, s, integrator, material, dir, totalEnergyLoss);


        RealMatrix transitionMatrix;
        if (dir) transitionMatrix = processModel.newForwardF(stateEstimation, s[0]);
        else transitionMatrix = processModel.newBackwardF(stateEstimation, s[0]);

        RealMatrix transitionMatrixT = transitionMatrix.transpose();


        RealMatrix processNoise = processModel.getProcessNoise().scalarMultiply(totalEnergyLoss[0]);

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
        // errorCovariance = identity.subtract(kalmanGain.multiply(measurementMatrix))
        //         .multiply(errorCovariance);

        // Numerically more stable !!
        RealMatrix temp1 = identity.subtract(kalmanGain.multiply(measurementMatrix));
        errorCovariance = temp1.multiply(errorCovariance.multiply(temp1.transpose()))
                .add(kalmanGain.multiply(measurementNoise.multiply(kalmanGain.transpose())));


        // System.out.println("stateEstimation correct = " + stateEstimation + " r = " + Math.hypot(
        //         stateEstimation.getEntry(0), stateEstimation.getEntry(1)) + " p = " +
        //         Math.sqrt(stateEstimation.getEntry(3)*stateEstimation.getEntry(3) +
        //                 stateEstimation.getEntry(4)*stateEstimation.getEntry(4) +
        //                 stateEstimation.getEntry(5)*stateEstimation.getEntry(5)));

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
