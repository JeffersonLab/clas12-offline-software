package org.jlab.rec.rtpc.KalmanFilter;

import org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.*;

public class RungeKutta4 {
  private final int numberOfVariables;
  private final Particle particle;
  private final BetheBlochModel model;
  private final double[] B;

  private Stepper stepper;

  private final double[] k1;
  private final double[] k2;
  private final double[] k3;
  private final double[] k4;

  private final double[] yInTemp;
  private final double[] yTemp;

  public RungeKutta4(
      Particle particle, BetheBlochModel betheBlochModel, int numberOfVariables, double[] B) {

    this.numberOfVariables = numberOfVariables;
    this.particle = particle;
    this.model = betheBlochModel;
    this.B = B;

    this.k1 = new double[numberOfVariables];
    this.k2 = new double[numberOfVariables];
    this.k3 = new double[numberOfVariables];
    this.k4 = new double[numberOfVariables];

    this.yInTemp = new double[numberOfVariables];
    this.yTemp = new double[numberOfVariables];
  }

  public void doOneStep(Stepper stepper) {

    this.stepper = stepper;

    double[] yIn = stepper.y;
    double h = stepper.h;
    Material material = stepper.material;

    stepper.s += h;
    if (stepper.is_in_drift) stepper.s_drift += h;

    if (stepper.direction) stepper.sTot += h;
    else stepper.sTot -= h;

    System.arraycopy(yIn, 0, yInTemp, 0, numberOfVariables);

    double[] dydt = f(yInTemp);
    for (int i = 0; i < numberOfVariables; ++i) {
      k1[i] = h * dydt[i];
    }

    for (int i = 0; i < numberOfVariables; ++i) {
      yTemp[i] = yInTemp[i] + 0.5 * k1[i];
    }
    dydt = f(yTemp);
    for (int i = 0; i < numberOfVariables; ++i) {
      k2[i] = h * dydt[i];
    }

    for (int i = 0; i < numberOfVariables; ++i) {
      yTemp[i] = yInTemp[i] + 0.5 * k2[i];
    }
    dydt = f(yTemp);
    for (int i = 0; i < numberOfVariables; ++i) {
      k3[i] = h * dydt[i];
    }

    for (int i = 0; i < numberOfVariables; ++i) {
      yTemp[i] = yInTemp[i] + k3[i];
    }
    dydt = f(yTemp);
    for (int i = 0; i < numberOfVariables; ++i) {
      k4[i] = h * dydt[i];
    }

    for (int i = 0; i < numberOfVariables; ++i) {
      yIn[i] =
          yInTemp[i]
              + 1.0 / 6.0 * k1[i]
              + 1.0 / 3.0 * k2[i]
              + 1.0 / 3.0 * k3[i]
              + 1.0 / 6.0 * k4[i];
    }

    energyLoss(yIn, h, material);
  }

  private double[] f(double[] y) {
    double charge = 1.0;
    double pModuleInverse = 1.0 / Math.sqrt(y[3] * y[3] + y[4] * y[4] + y[5] * y[5]);
    double k = SystemOfUnits.eplus * charge * PhysicalConstants.c_light * pModuleInverse;

    if (this.stepper.direction) {
      return new double[] {
        y[3] * pModuleInverse,
        y[4] * pModuleInverse,
        y[5] * pModuleInverse,
        k * (y[4] * B[2] - y[5] * B[1]),
        k * (y[5] * B[0] - y[3] * B[2]),
        k * (y[3] * B[1] - y[4] * B[0])
      };
    } else {
      return new double[] {
        -y[3] * pModuleInverse,
        -y[4] * pModuleInverse,
        -y[5] * pModuleInverse,
        -k * (y[4] * B[2] - y[5] * B[1]),
        -k * (y[5] * B[0] - y[3] * B[2]),
        -k * (y[3] * B[1] - y[4] * B[0])
      };
    }
  }

  private void energyLoss(double[] yIn, double h, Material material) {
    double mass = particle.GetMass();

    h /= 10; // cm
    double mom = Math.sqrt(yIn[3] * yIn[3] + yIn[4] * yIn[4] + yIn[5] * yIn[5]);
    double kineticEnergy = Math.sqrt(mass * mass + mom * mom) - mass;
    double E = Math.sqrt(mom * mom + mass * mass);

    double dedx =
        model.ComputeDEDXPerVolume(particle, material, kineticEnergy, false) * 1000; // MeV/cm
    double DeltaE = dedx * h;

    stepper.dEdx += DeltaE;

    double mom_prim;
    if (this.stepper.direction) mom_prim = Math.sqrt((E - DeltaE) * (E - DeltaE) - mass * mass);
    else mom_prim = Math.sqrt((E + DeltaE) * (E + DeltaE) - mass * mass);

    yIn[3] *= mom_prim / mom;
    yIn[4] *= mom_prim / mom;
    yIn[5] *= mom_prim / mom;
  }

  public double Bz() {
    return this.B[2];
  }
}
