package org.jlab.rec.rtpc.KalmanFilter.Integrator;

import org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.BetheBlochModel;
import org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.Material;
import org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.Particle;

public class EulerMethod extends Integrator {

    public EulerMethod(Particle particle, BetheBlochModel betheBlochModel, int nvar) {
        this.numberOfVariables = nvar;
        this.particle = particle;
        this.model = betheBlochModel;
    }

    @Override
    public void ForwardStepper(double[] yIn, double h, double[] Field, Material material) {
        double[] dydt = new double[numberOfVariables];
        double[] yInTemp = new double[numberOfVariables];
        System.arraycopy(yIn, 0, yInTemp, 0, numberOfVariables);

        EquationOfMotion.ForwardRightHandSide(yInTemp, dydt, Field);

        for (int i = 0; i < numberOfVariables; ++i) {
            yIn[i] = yInTemp[i] + h * dydt[i];
        }

        ForwardEnergyLoss(yIn, h, material);

    }

    @Override
    public void BackwardStepper(double[] yIn, double h, double[] Field, Material material) {
        double[] dydt = new double[numberOfVariables];
        double[] yInTemp = new double[numberOfVariables];
        System.arraycopy(yIn, 0, yInTemp, 0, numberOfVariables);

        EquationOfMotion.BackwardRightHandSide(yInTemp, dydt, Field);

        for (int i = 0; i < numberOfVariables; ++i) {
            yIn[i] = yInTemp[i] + h * dydt[i];
        }

        BackwardEnergyLoss(yIn, h, material);

    }

}
