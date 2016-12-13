/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.detector;

import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

/**
 *
 * @author gavalian
 */
public class CherenkovResponse {
    
    private double        hitTime = 0.0;
    private double       hitTheta = 0.0;
    private double         hitPhi = 0.0;
    private int        hitNphe = 0;
    private double    hitDeltaPhi = 0.0;
    private double  hitDeltaTheta = 0.0;
    private int     association = -1;
    
    private DetectorType  cherenkovType = DetectorType.HTCC;
    private Point3D         hitPosition = new Point3D();
    //private Sphere3D 
    private DetectorDescriptor desc = new DetectorDescriptor();
    
    public CherenkovResponse(double theta, double phi, double dtheta, double dphi){
        hitTheta = theta;
        hitPhi   = phi;
        hitDeltaTheta  = dtheta;
        hitDeltaPhi    = dphi;
    }
    
    public CherenkovResponse  setTime(double time) { hitTime = time; return this;}
    public CherenkovResponse  setEnergy(int energy) { hitNphe = energy; return this;}
    public void setAssociation(int assoc) {this.association = assoc;}
    
    public double getTime(){ return hitTime;}
    
    public int getEnergy(){ return hitNphe;}
    public double getDeltaTheta(){ return this.hitDeltaTheta;}
    public double getDeltaPhi() {return this.hitDeltaPhi;}
    public int getAssociation() {return this.association;}
    
    public Point3D getHitPosition(){
        return this.hitPosition;
    }
    
    public void setHitPosition(double x, double y, double z){
        this.hitPosition.set(x, y, z);
    }
    
    public Point3D getIntersection(Line3D line){
        Vector3D vec = new Vector3D(this.hitPosition.x(),this.hitPosition.y(),this.hitPosition.z());
        vec.unit();        
        Plane3D plane = new Plane3D(this.hitPosition,vec);
        Point3D intersect = new Point3D();
        plane.intersection(line, intersect);
        return intersect;
    }
    
    public boolean match(DetectorParticle particle){
        Point3D intersection = this.getIntersection(particle.getLowerCross());
        Vector3D vecRec = intersection.toVector3D();
        Vector3D vecHit = this.hitPosition.toVector3D();
        return (Math.abs(vecHit.theta()-vecRec.theta())<this.hitDeltaTheta
                &&Math.abs(vecHit.phi()-vecRec.phi())<this.hitDeltaPhi);
    }
    
    public double getDistance(Line3D line){
        
        return -1000.0;
    }
    
    public DetectorType getCherenkovType(){
        return this.cherenkovType;
    }
    
    public void setCherenkovType(DetectorType htcc){
        this.cherenkovType = htcc;
    }
    
}
