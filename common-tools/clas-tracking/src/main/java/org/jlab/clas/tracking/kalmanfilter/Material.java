package org.jlab.clas.tracking.kalmanfilter;

import org.jlab.clas.pdg.PhysicsConstants;

/**
 *
 * @author devita
 */
public class Material {

    private String name;
    private double thickness;
    private double density;
    private double ZoverA;
    private double X0;
    private double IeV;
    private Units units; // 1 - cm, 10 - mm

    public Material(String name, double thickness, double density, double ZoverA, double X0, double IeV, Units units) {
        this.name = name;
        this.thickness = thickness;
        this.density = density;
        this.ZoverA = ZoverA;
        this.X0 = X0;
        this.IeV = IeV;
        this.units = units;
    }

    public String getName() {
        return name;
    }

    public double getThickness() {
        return thickness;
    }

    public double getDensity() {
        return density;
    }

    public double getZoverA() {
        return ZoverA;
    }

    public double getX0() {
        return X0;
    }

    public double getIeV() {
        return IeV;
    }

    @Override
    public Material clone() {
        return new Material(this.name, this.thickness, this.density, this.ZoverA, this.X0, this.IeV, this.units);
    }
    
    public Material clone(double newThickness) {
        return new Material(this.name, newThickness, this.density, this.ZoverA, this.X0, this.IeV, this.units);
    }
    // RDV make units a property of the material
    public double getEloss(double p, double mass) {
        if(mass==0) return 0;
        double beta = p / Math.sqrt(p * p + mass * mass);
        double s = PhysicsConstants.massElectron() / mass;
        double gamma = 1. / Math.sqrt(1 - beta * beta);
        double Wmax = 2. * PhysicsConstants.massElectron() * beta * beta * gamma * gamma
                / (1. + 2. * s * gamma + s * s);
        double K = 0.000307075 * units.value() * units.value(); //  GeV mol-1 cm2
        double I = this.IeV * 1E-9;
        double logterm = 2. * PhysicsConstants.massElectron() * beta * beta * gamma * gamma * Wmax / (I * I);
        double dE = this.thickness * this.density * K * this.ZoverA
                * (0.5 * Math.log(logterm) - beta * beta) / beta / beta; //in GeV
        return dE;
    }

        
}
