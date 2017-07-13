/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.detector;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;


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
    private int     hitIndex = -1;
    
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
    public void setHitIndex(int index){this.hitIndex = index;}
    
    public double getTime(){ return hitTime;}
    public int getHitIndex(){return hitIndex;}
    public int getEnergy(){ return hitNphe;}
    public double getTheta() {return this.hitTheta;}
    public double getPhi() {return this.hitPhi;}
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
    
    public boolean match(Line3D particletrack){
        Point3D intersection = this.getIntersection(particletrack);
        Vector3D vecRec = intersection.toVector3D();
        Vector3D vecHit = this.hitPosition.toVector3D();
        //System.out.println(particletrack);
        //System.out.println(this.hitPosition);
//        System.out.println("Calculated Theta Difference (Degrees)" + Math.abs(vecHit.theta()-vecRec.theta())*57.2958);
//        System.out.println("Expected Theta Difference (Degrees)" + this.hitDeltaTheta*57.2958);
//        System.out.println(" ");
//        System.out.println("Calculated Phi Difference (Degrees)" + Math.abs(vecHit.phi()-vecRec.phi())*57.2958);
//        System.out.println("Expected Phi Difference (Degrees)" + this.hitDeltaPhi*57.2958);
//        System.out.println(" ");

//System.out.println(Math.abs(vecHit.theta()-vecRec.theta())*57.2958 + "  " + 
//        Math.abs(vecHit.phi()-vecRec.phi())*57.2958);
        
        return (Math.abs(vecHit.theta()-vecRec.theta())<10.0/57.2958
        && Math.abs(vecHit.phi()-vecRec.phi())<this.hitDeltaPhi);
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
    
    public static List<CherenkovResponse>  readHipoEvent(DataEvent event, 
        String bankName, DetectorType type){        
        List<CherenkovResponse> responseList = new ArrayList<CherenkovResponse>();
        if(event.hasBank(bankName)==true){
            DataBank bank = event.getBank(bankName);
            int nrows = bank.rows();
            for(int row = 0; row < nrows; row++){
                int nphe  = bank.getInt("nphe", row);
                double theta   = bank.getFloat("theta", row);
                double dtheta = bank.getFloat("dtheta",row);
                double phi = bank.getFloat("phi",row);
                double dphi = bank.getFloat("dphi",row);
                double x = bank.getFloat("x",row);
                double y = bank.getFloat("y",row);
                double z = bank.getFloat("z",row);
                double time = bank.getFloat("time",row);
                    CherenkovResponse che = new CherenkovResponse(theta,phi,dtheta,dphi);
                    che.setHitPosition(x, y, z);
                   //System.out.println(che.getHitPosition());
                   //System.out.println("hello there is cherenkov");
                    che.setHitIndex(row);
                    che.setEnergy(nphe);
                    che.setTime(time);
                    che.setCherenkovType(DetectorType.HTCC);

                responseList.add(che);
            }
        }
        return responseList;
    }
    
}

