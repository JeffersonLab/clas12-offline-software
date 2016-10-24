/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.detector;

import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.geom.prim.Line3D;

/**
 *
 * @author gavalian
 */
public class CherenkovResponse {
    
    private double  hitTime = 0.0;
    private double hitTheta = 0.0;
    private double hitPhi   = 0.0;
    private double hitNphe  = 0.0;
    private double hitDeltaPhi   = 0.0;
    private double hitDeltaTheta = 0.0;
    //private Sphere3D 
    private DetectorDescriptor desc = new DetectorDescriptor();
    
    public CherenkovResponse(double theta, double phi, double dtheta, double dphi){
        hitTheta = theta;
        hitPhi   = phi;
        hitDeltaTheta  = dtheta;
        hitDeltaPhi    = dphi;
    }
    
    public CherenkovResponse  setTime(double time) { hitTime = time; return this;}
    public CherenkovResponse  setEnergy(double energy) { hitNphe = energy; return this;}
    
    public double getTime(){ return hitTime;}
    
    public double getEnergy(){ return hitNphe;}
    
    public double getDistance(Line3D line){
        
        return -1000.0;
    }
}
