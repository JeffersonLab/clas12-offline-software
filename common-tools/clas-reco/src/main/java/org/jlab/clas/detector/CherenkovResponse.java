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

    private double  hitTime       = 0.0;
    private double  hitTheta      = 0.0;
    private double  hitPhi        = 0.0;
    private double  hitNphe       = 0;
    private double  hitDeltaPhi   = 0.0;
    private double  hitDeltaTheta = 0.0;
    private int     hitIndex      = -1;
    private int     association   = -1;

    private DetectorType  cherenkovType = DetectorType.HTCC;
    private Point3D         hitPosition = new Point3D();
    private DetectorDescriptor desc = new DetectorDescriptor();

    public CherenkovResponse(double theta, double phi, double dtheta, double dphi){
        hitTheta = theta;
        hitPhi   = phi;
        hitDeltaTheta  = dtheta;
        hitDeltaPhi    = dphi;
    }

    public CherenkovResponse  setTime(double time) { hitTime = time; return this;}
    public CherenkovResponse  setEnergy(double energy) { hitNphe = energy; return this;}
    public void setAssociation(int assoc) {this.association = assoc;}
    public void setHitIndex(int index){this.hitIndex = index;}

    public double getTime(){ return hitTime;}
    public int getHitIndex(){return hitIndex;}
    public double getEnergy(){ return hitNphe;}
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
        // FIXME:
        // 1. remove hardcoded constant (10 degrees)
        // 2. either use both dphi and dtheta from detector bank (only
        //    exists for HTCC), or get both from CCDB
        return (Math.abs(vecHit.theta()-vecRec.theta())<10.0/57.2958
        && Math.abs(vecHit.phi()-vecRec.phi())<this.hitDeltaPhi);
    }
   
    public boolean matchToPoint(Line3D trackPoint) {
        if (trackPoint==null) return false;
        Vector3D vecTrk = trackPoint.origin().toVector3D();
        Vector3D vecHit = this.hitPosition.toVector3D();
        return (Math.abs(vecHit.theta()-vecTrk.theta())<10.0/57.2958
                && Math.abs(vecHit.phi()-vecTrk.phi())<this.hitDeltaPhi);
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

                double x = bank.getFloat("x",row);
                double y = bank.getFloat("y",row);
                double z = bank.getFloat("z",row);
                double time = bank.getFloat("time",row);
                double nphe = bank.getFloat("nphe", row);

                // FIXME: unify LTCC/HTCC detector banks
                // Here we have to treat them differently:
                // 2.  either add dtheta/dphi to LTCC, or ignore HTCC's and use CCDB for both
                // 3.  HTCC provides both x/y/z and theta/phi, while LTCC provides only x/y/z.
                //     The current convention in EB is to use only x/y/z, while theta/phi is
                //     just propogated.

                double theta=0,phi=0,dtheta=0,dphi=0;

                if (type==DetectorType.HTCC) {
                    dtheta = bank.getFloat("dtheta",row);
                    dphi = bank.getFloat("dphi",row);
                    theta = bank.getFloat("theta", row);
                    phi = bank.getFloat("phi",row);
                }

                else if (type==DetectorType.LTCC) {
                    // Hardcode some dtheta/dphi values for now (for matching):
                    dtheta = 10*3.14159/180;
                    dphi   = 10*3.14159/180;
                    // Fill theta/phi:
                    // Not yet.
                }

                else {
                    throw new RuntimeException(
                            "CherenkovResponse::readHipoEvent:  invalid DetectorType: "+type);
                }

                CherenkovResponse che = new CherenkovResponse(theta,phi,dtheta,dphi);
                che.setHitPosition(x, y, z);
                che.setHitIndex(row);
                che.setEnergy(nphe);
                che.setTime(time);
                che.setCherenkovType(type);

                responseList.add(che);
            }
        }
        return responseList;
    }
    
}

