package org.jlab.rec.rtpc.KalmanFilter.Integrator;

import org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.BetheBlochModel;
import org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.Material;
import org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.Particle;

public abstract class Integrator {

    protected int numberOfVariables;

    protected Particle particle;
    protected BetheBlochModel model;

    public abstract void ForwardStepper(double[] yIn, double h, double[] Field, Material material);

    public abstract void BackwardStepper(double[] yIn, double h, double[] Field, Material material);

    protected void ForwardEnergyLoss(double[] yIn, double h, Material material) {
        double mass = particle.GetMass();

        h /= 10; // cm
        double mom = Math.sqrt(yIn[3] * yIn[3] + yIn[4] * yIn[4] + yIn[5] * yIn[5]);
        double kineticEnergy = Math.sqrt(mass * mass + mom * mom) - mass;
        double E = Math.sqrt(mom * mom + mass * mass);

        double dedx = model.ComputeDEDXPerVolume(particle, material, kineticEnergy, true) * 1000; // MeV/cm
        double DeltaE = dedx * h;

        double mom_prim = Math.sqrt((E - DeltaE) * (E - DeltaE) - mass * mass);

        yIn[3] *= mom_prim / mom;
        yIn[4] *= mom_prim / mom;
        yIn[5] *= mom_prim / mom;


    }

    protected void BackwardEnergyLoss(double[] yIn, double h, Material material) {
        double mass = particle.GetMass();

        h /= 10; // cm
        double mom = Math.sqrt(yIn[3] * yIn[3] + yIn[4] * yIn[4] + yIn[5] * yIn[5]);
        double kineticEnergy = Math.sqrt(mass * mass + mom * mom) - mass;
        double E = Math.sqrt(mom * mom + mass * mass);

        double dedx = model.ComputeDEDXPerVolume(particle, material, kineticEnergy, true) * 1000; // MeV/cm
        double DeltaE = dedx * h;

        double mom_prim = Math.sqrt((E + DeltaE) * (E + DeltaE) - mass * mass);

        // double kineticEnergy_prim = Math.sqrt(mass * mass + mom_prim * mom_prim) - mass;
        // double E_prim = Math.sqrt(mom_prim * mom_prim + mass * mass);
        // double dedx_prim = model.ComputeDEDXPerVolume(particle, material, kineticEnergy_prim, true) * 1000; // MeV/cm
        // double DeltaE_prim = dedx_prim * h;
        // double mom_prim_prim = Math.sqrt((E + DeltaE_prim) * (E + DeltaE_prim) - mass * mass);

        yIn[3] *= mom_prim / mom;
        yIn[4] *= mom_prim / mom;
        yIn[5] *= mom_prim / mom;

    }



}
