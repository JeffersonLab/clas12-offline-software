/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.geant;

/**
 *
 * @author gavalian
 */
public class G4Material {
    private double matAtomicNumber    = 8.0;
    private double matRadiationLength = 0.1;
    private double matDensity         = 37.15; // Density in g cm/2

    public G4Material(){}
    
    public G4Material(double an, double rl){
        this.matAtomicNumber = an;
        this.matRadiationLength = rl;
    }
    
    public double atomicNumber(){ return this.matAtomicNumber;}
    public double radLength(){ return this.matRadiationLength;}
    public double density(){ return this.matDensity;}
    
    public void setAtomicNumber(double an){ this.matAtomicNumber = an;}
    public void setRadLengthr(double rl){ this.matRadiationLength = rl;}
    public void setDensity(double dn){ this.matDensity = dn;}
}
