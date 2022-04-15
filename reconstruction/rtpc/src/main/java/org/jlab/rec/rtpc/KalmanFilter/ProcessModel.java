package org.jlab.rec.rtpc.KalmanFilter;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.Material;
import org.jlab.rec.rtpc.KalmanFilter.Integrator.Integrator;

import java.util.Arrays;

import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.PhysicalConstants.c_light;
import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.SystemOfUnits.eplus;

public class ProcessModel {

    /**
     * Magnetic Field
     */
    private final double[] B;

    /**
     * The process noise covariance matrix.
     */
    private final RealMatrix processNoiseCovMatrix;


    /**
     * Create a new {@link org.jlab.rec.rtpc.KalmanFilter.ProcessModel},
     * taking RealMatrix/RealVector as input parameters.
     *
     */
    public ProcessModel(final RealMatrix processNoise, final double[] B) {
        this.B = B;
        this.processNoiseCovMatrix = processNoise;
    }

    public RealMatrix getProcessNoise() {
        return processNoiseCovMatrix;
    }

    public RealVector f(RealVector x, double rMax, double h, double[] sArray, Integrator dormandPrinceRK78, Material material, boolean dir, double[] totEnergyLoss) {
        double TotNbOfStep = 1e8;
        double s = sArray[0], sEnd = 50;
        double[] yIn = x.toArray();

        double[] B = this.B;

        double r = Math.hypot(yIn[0], yIn[1]);
        if (rMax > 30.005 && dir) h = (rMax - r);
        if (rMax > 30.005 && !dir) h = (r - rMax);

        // System.out.println("rMax = " + rMax + " r = " + r);

        for (int nStep = 0; nStep < TotNbOfStep; ++nStep) {

            double[] EnergyLoss = {0};

            double previous_r = Math.hypot(yIn[0], yIn[1]);
            double[] previous_yIn = Arrays.copyOf(yIn, 6);

            if (dir) {
                dormandPrinceRK78.ForwardStepper(yIn, h, B, material, EnergyLoss);
            } else {
                dormandPrinceRK78.BackwardStepper(yIn, h, B, material, EnergyLoss);
            }

            totEnergyLoss[0] += EnergyLoss[0];

            s += h;

            r = Math.hypot(yIn[0], yIn[1]);
            // System.out.println("r = " + r + " h = " + h + " Material = " + material.GetName() + " s = " + s);

            if (dir) {
                if (r >= rMax - h) h = rMax - r;
                if (h < 1e-4) h = 1e-4;
                if (s >= sEnd || r >= rMax) break;

            } else {
                if (rMax == 0) {
                    if (r <= rMax + h) h = h / 10;
                    if (h < 1e-4) h = 1e-4;
                    if ((previous_r - r) < 0) {
                        sArray[0] = s;
                        return new ArrayRealVector(previous_yIn);
                    }
                    ;
                } else {
                    if (r <= rMax + h) h = r - rMax;
                    if (h < 1e-4) h = 1e-4;
                    if (s >= sEnd || r <= rMax) break;
                    if ((previous_r - r) < 0) break;
                }
            }
        }
        sArray[0] = s;
        return new ArrayRealVector(yIn);

    }


    RealMatrix newForwardF(RealVector x, double s) {

        double q = 1;
        double k = eplus * q * c_light;

        double px0 = x.getEntry(3);
        double py0 = x.getEntry(4);
        double pz0 = x.getEntry(5);
        double p = Math.sqrt(px0 * px0 + py0 * py0 + pz0 * pz0);

        double Bz = this.B[2];

        double sin = Math.sin(Bz * k * q * s / p);
        double cos = Math.cos(Bz * k * q * s / p);

        // fx derivative
        double dfxdx = 1.0;
        double dfxdy = 0.0;
        double dfxdz = 0.0;
        double dfxdpx = sin / (Bz * k * q);
        double dfxdpy = (1.0 - cos) / (Bz * k * q);
        double dfxdpz = 0.0;

        // fy derivative
        double dfydx = 0.0;
        double dfydy = 1.0;
        double dfydz = 0.0;
        double dfydpx = (cos - 1) / (Bz * k * q);
        double dfydpy = sin / (Bz * k * q);
        double dfydpz = 0.0;

        // fz derivative
        double dfzdx = 0.0;
        double dfzdy = 0.0;
        double dfzdz = 1.0;
        double dfzdpx = 0.0;
        double dfzdpy = 0.0;
        double dfzdpz = s / p;

        // fpx derivative
        double dfpxdx = 0.0;
        double dfpxdy = 0.0;
        double dfpxdz = 0.0;
        double dfpxdpx = cos;
        double dfpxdpy = sin;
        double dfpxdpz = 0.0;

        // fpy derivative
        double dfpydx = 0.0;
        double dfpydy = 0.0;
        double dfpydz = 0.0;
        double dfpydpx = -sin;
        double dfpydpy = cos;
        double dfpydpz = 0.0;

        // fpy derivative
        double dfpzdx = 0.0;
        double dfpzdy = 0.0;
        double dfpzdz = 0.0;
        double dfpzdpx = 0.0;
        double dfpzdpy = 0.0;
        double dfpzdpz = 1.0;

        return MatrixUtils.createRealMatrix(new double[][]{
                {dfxdx, dfxdy, dfxdz, dfxdpx, dfxdpy, dfxdpz},
                {dfydx, dfydy, dfydz, dfydpx, dfydpy, dfydpz},
                {dfzdx, dfzdy, dfzdz, dfzdpx, dfzdpy, dfzdpz},
                {dfpxdx, dfpxdy, dfpxdz, dfpxdpx, dfpxdpy, dfpxdpz},
                {dfpydx, dfpydy, dfpydz, dfpydpx, dfpydpy, dfpydpz},
                {dfpzdx, dfpzdy, dfpzdz, dfpzdpx, dfpzdpy, dfpzdpz}
        });
    }

    RealMatrix newBackwardF(RealVector x, double s) {

        double q = 1;
        double k = eplus * q * c_light;

        double px0 = x.getEntry(3);
        double py0 = x.getEntry(4);
        double pz0 = x.getEntry(5);
        double p = Math.sqrt(px0 * px0 + py0 * py0 + pz0 * pz0);

        double Bz = this.B[2];

        double sin = Math.sin(Bz * k * q * s / p);
        double cos = Math.cos(Bz * k * q * s / p);

        // fx derivative
        double dfxdx = 1.0;
        double dfxdy = 0.0;
        double dfxdz = 0.0;
        double dfxdpx = -sin / (Bz * k * q);
        double dfxdpy = (1.0 - cos) / (Bz * k * q);
        double dfxdpz = 0.0;

        // fy derivative
        double dfydx = 0.0;
        double dfydy = 1.0;
        double dfydz = 0.0;
        double dfydpx = (cos - 1) / (Bz * k * q);
        double dfydpy = -sin / (Bz * k * q);
        double dfydpz = 0.0;

        // fz derivative
        double dfzdx = 0.0;
        double dfzdy = 0.0;
        double dfzdz = 1.0;
        double dfzdpx = 0.0;
        double dfzdpy = 0.0;
        double dfzdpz = -s / p;

        // fpx derivative
        double dfpxdx = 0.0;
        double dfpxdy = 0.0;
        double dfpxdz = 0.0;
        double dfpxdpx = cos;
        double dfpxdpy = -sin;
        double dfpxdpz = 0.0;

        // fpy derivative
        double dfpydx = 0.0;
        double dfpydy = 0.0;
        double dfpydz = 0.0;
        double dfpydpx = sin;
        double dfpydpy = cos;
        double dfpydpz = 0.0;

        // fpy derivative
        double dfpzdx = 0.0;
        double dfpzdy = 0.0;
        double dfpzdz = 0.0;
        double dfpzdpx = 0.0;
        double dfpzdpy = 0.0;
        double dfpzdpz = 1.0;

        return MatrixUtils.createRealMatrix(new double[][]{
                {dfxdx, dfxdy, dfxdz, dfxdpx, dfxdpy, dfxdpz},
                {dfydx, dfydy, dfydz, dfydpx, dfydpy, dfydpz},
                {dfzdx, dfzdy, dfzdz, dfzdpx, dfzdpy, dfzdpz},
                {dfpxdx, dfpxdy, dfpxdz, dfpxdpx, dfpxdpy, dfpxdpz},
                {dfpydx, dfpydy, dfpydz, dfpydpx, dfpydpy, dfpydpz},
                {dfpzdx, dfpzdy, dfpzdz, dfpzdpx, dfpzdpy, dfpzdpz}
        });
    }

}
