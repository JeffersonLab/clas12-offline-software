package org.jlab.rec.rtpc.KalmanFilter.Integrator;

import org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.BetheBlochModel;
import org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.Material;
import org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.Particle;

public class DormandPrince45 extends Integrator {

    public DormandPrince45(Particle particle, BetheBlochModel betheBlochModel, int nvar) {

        this.numberOfVariables = nvar;
        this.particle = particle;
        this.model = betheBlochModel;
    }

    @Override
    public void ForwardStepper(double[] yIn, double h, double[] Field, Material material, double[] EnergyLoss) {
        double
                b21 = 0.2,
                b31 = 3.0 / 40.0, b32 = 9.0 / 40.0,
                b41 = 44.0 / 45.0, b42 = -56.0 / 15.0, b43 = 32.0 / 9.0,

                b51 = 19372.0 / 6561.0, b52 = -25360.0 / 2187.0, b53 = 64448.0 / 6561.0,
                b54 = -212.0 / 729.0,

                b61 = 9017.0 / 3168.0, b62 = -355.0 / 33.0,
                b63 = 46732.0 / 5247.0, b64 = 49.0 / 176.0,
                b65 = -5103.0 / 18656.0,

                b71 = 35.0 / 384.0, b72 = 0.,
                b73 = 500.0 / 1113.0, b74 = 125.0 / 192.0,
                b75 = -2187.0 / 6784.0, b76 = 11.0 / 84.0;


        double[] dydt = new double[numberOfVariables];
        double[] k1 = new double[numberOfVariables];
        double[] k2 = new double[numberOfVariables];
        double[] k3 = new double[numberOfVariables];
        double[] k4 = new double[numberOfVariables];
        double[] k5 = new double[numberOfVariables];
        double[] k6 = new double[numberOfVariables];

        double[] yInTemp = new double[numberOfVariables];
        double[] yTemp = new double[numberOfVariables];


        for (int i = 0; i < numberOfVariables; ++i) {
            yInTemp[i] = yIn[i];
        }

        EquationOfMotion.ForwardRightHandSide(yInTemp, dydt, Field);
        for (int i = 0; i < numberOfVariables; ++i) {
            k1[i] = h * dydt[i];
        }

        for (int i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yInTemp[i] + b21 * k1[i];
        }
        EquationOfMotion.ForwardRightHandSide(yTemp, dydt, Field);
        for (int i = 0; i < numberOfVariables; ++i) {
            k2[i] = h * dydt[i];
        }

        for (int i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yInTemp[i] + b31 * k1[i] + b32 * k2[i];
        }
        EquationOfMotion.ForwardRightHandSide(yTemp, dydt, Field);
        for (int i = 0; i < numberOfVariables; ++i) {
            k3[i] = h * dydt[i];
        }

        for (int i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yInTemp[i] + b41 * k1[i] + b42 * k2[i] + b43 * k3[i];
        }
        EquationOfMotion.ForwardRightHandSide(yTemp, dydt, Field);
        for (int i = 0; i < numberOfVariables; ++i) {
            k4[i] = h * dydt[i];
        }

        for (int i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yInTemp[i] + b51 * k1[i] + b52 * k2[i] + b53 * k3[i] +
                    b54 * k4[i];
        }
        EquationOfMotion.ForwardRightHandSide(yTemp, dydt, Field);
        for (int i = 0; i < numberOfVariables; ++i) {
            k5[i] = h * dydt[i];
        }

        for (int i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yInTemp[i] + b61 * k1[i] + b62 * k2[i] + b63 * k3[i] +
                    b64 * k4[i] + b65 * k5[i];
        }
        EquationOfMotion.ForwardRightHandSide(yTemp, dydt, Field);
        for (int i = 0; i < numberOfVariables; ++i) {
            k6[i] = h * dydt[i];
        }

        for (int i = 0; i < numberOfVariables; ++i) {
            yIn[i] = yInTemp[i] + b71 * k1[i] + b72 * k2[i] + b73 * k3[i] + b74 * k4[i] + b75 * k5[i] + b76 * k6[i];
        }

        ForwardEnergyLoss(yIn, h, material, EnergyLoss);
    }

    @Override
    public void BackwardStepper(double[] yIn, double h, double[] Field, Material material, double[] EnergyLoss) {
        double
                b21 = 0.2,
                b31 = 3.0 / 40.0, b32 = 9.0 / 40.0,
                b41 = 44.0 / 45.0, b42 = -56.0 / 15.0, b43 = 32.0 / 9.0,

                b51 = 19372.0 / 6561.0, b52 = -25360.0 / 2187.0, b53 = 64448.0 / 6561.0,
                b54 = -212.0 / 729.0,

                b61 = 9017.0 / 3168.0, b62 = -355.0 / 33.0,
                b63 = 46732.0 / 5247.0, b64 = 49.0 / 176.0,
                b65 = -5103.0 / 18656.0,

                b71 = 35.0 / 384.0, b72 = 0.,
                b73 = 500.0 / 1113.0, b74 = 125.0 / 192.0,
                b75 = -2187.0 / 6784.0, b76 = 11.0 / 84.0;


        double[] dydt = new double[numberOfVariables];
        double[] k1 = new double[numberOfVariables];
        double[] k2 = new double[numberOfVariables];
        double[] k3 = new double[numberOfVariables];
        double[] k4 = new double[numberOfVariables];
        double[] k5 = new double[numberOfVariables];
        double[] k6 = new double[numberOfVariables];

        double[] yInTemp = new double[numberOfVariables];
        double[] yTemp = new double[numberOfVariables];


        for (int i = 0; i < numberOfVariables; ++i) {
            yInTemp[i] = yIn[i];
        }

        EquationOfMotion.BackwardRightHandSide(yInTemp, dydt, Field);
        for (int i = 0; i < numberOfVariables; ++i) {
            k1[i] = h * dydt[i];
        }

        for (int i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yInTemp[i] + b21 * k1[i];
        }
        EquationOfMotion.BackwardRightHandSide(yTemp, dydt, Field);
        for (int i = 0; i < numberOfVariables; ++i) {
            k2[i] = h * dydt[i];
        }

        for (int i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yInTemp[i] + b31 * k1[i] + b32 * k2[i];
        }
        EquationOfMotion.BackwardRightHandSide(yTemp, dydt, Field);
        for (int i = 0; i < numberOfVariables; ++i) {
            k3[i] = h * dydt[i];
        }

        for (int i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yInTemp[i] + b41 * k1[i] + b42 * k2[i] + b43 * k3[i];
        }
        EquationOfMotion.BackwardRightHandSide(yTemp, dydt, Field);
        for (int i = 0; i < numberOfVariables; ++i) {
            k4[i] = h * dydt[i];
        }

        for (int i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yInTemp[i] + b51 * k1[i] + b52 * k2[i] + b53 * k3[i] +
                    b54 * k4[i];
        }
        EquationOfMotion.BackwardRightHandSide(yTemp, dydt, Field);
        for (int i = 0; i < numberOfVariables; ++i) {
            k5[i] = h * dydt[i];
        }

        for (int i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yInTemp[i] + b61 * k1[i] + b62 * k2[i] + b63 * k3[i] +
                    b64 * k4[i] + b65 * k5[i];
        }
        EquationOfMotion.BackwardRightHandSide(yTemp, dydt, Field);
        for (int i = 0; i < numberOfVariables; ++i) {
            k6[i] = h * dydt[i];
        }

        for (int i = 0; i < numberOfVariables; ++i) {
            yIn[i] = yInTemp[i] + b71 * k1[i] + b72 * k2[i] + b73 * k3[i] + b74 * k4[i] + b75 * k5[i] + b76 * k6[i];
        }

        BackwardEnergyLoss(yIn, h, material, EnergyLoss);
    }


}
