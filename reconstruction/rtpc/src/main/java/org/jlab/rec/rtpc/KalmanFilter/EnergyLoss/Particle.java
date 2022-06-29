package org.jlab.rec.rtpc.KalmanFilter.EnergyLoss;

public class Particle {
    private final String theParticleName;
    private final double theMass;
    private final double theCharge;
    private final double theSpin;

    public Particle(String aName, double mass, double spin, double charge) {
        theParticleName = aName;
        theMass = mass;
        theCharge = (charge);
        theSpin = (spin);
    }

    public String GetParticleName() {
        return theParticleName;
    }

    public double GetMass() {
        return theMass;
    }

    public double GetCharge() {
        return theCharge;
    }

    public double GetSpin() {
        return theSpin;
    }
}

