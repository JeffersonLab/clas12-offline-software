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
public class TaggerResponse {
    

    
    
    private Double    hitTime = 0.0;
    private Integer         hitID = -1;
    private Integer     hitSize = -1;
    private Double  hitRadius = 0.0;
    private Double  hitEnergy = 0.0;
    private Integer   association = -1;
    private Integer   hitIndex = -1;
    private Double hitPath = 0.0;
    private DetectorDescriptor  descriptor  = new DetectorDescriptor();

    private Vector3D         hitMomentum = new Vector3D();
    private Point3D         hitPosition = new Point3D();
    private Point3D         hitWidth = new Point3D();
    
    public void setID(int id){ hitID = id;}
    public void setTime(double time) {hitTime = time;}
    public void setSize(int q){hitSize = q;}
    public void  setEnergy(double energy) { hitEnergy = energy;}
    public void setAssociation(int assoc) {this.association = assoc;}
    public void setHitIndex(int index) {this.hitIndex = index;}
    public void setRadius(double r) {hitRadius = r;}
    public void setPath(double path) {this.hitPath = path;}
    
    public DetectorDescriptor getDescriptor(){ return this.descriptor;}
    public int getSize(){return hitSize;}
    public int getID(){return hitID;}
    public double getTime(){ return hitTime;}
    public double getEnergy(){ return hitEnergy;}
    public int getAssociation() {return this.association;}
    public int getHitIndex() {return this.hitIndex;}
    public double getRadius() {return this.hitRadius;}
    public double getPath() {return this.hitPath;}
    
    public Vector3D getMomentum(){
        return this.hitMomentum;
    }
    
    public void setMomentum(double px, double py, double pz){
        this.hitMomentum.setXYZ(px, py, pz);
    }
    
    public Point3D getPosition(){
        return this.hitPosition;
    }
    
    public void setPosition(double x, double y, double z){
        this.hitPosition.set(x, y, z);
    }
 
    public Point3D getPositionWidth(){
        return this.hitPosition;
    }
    
    public void setPositionWidth(double x, double y, double z){
        this.hitWidth.set(x, y, z);
    }

    public static List<TaggerResponse>  readHipoEvent(DataEvent event, 
        String bankName, DetectorType type){        
        List<TaggerResponse> responseList = new ArrayList<TaggerResponse>();
        if(event.hasBank(bankName)==true){
            DataBank bank = event.getBank(bankName);
            int nrows = bank.rows();
            for(int row = 0; row < nrows; row++){
                int id  = bank.getShort("id", row);
                int size = bank.getShort("size", row);
                double x = bank.getFloat("x",row);
                double y = bank.getFloat("y",row);
                double z = bank.getFloat("z",row);
                double dx = bank.getFloat("widthX",row);
                double dy = bank.getFloat("widthY",row);
                double radius = bank.getFloat("radius", row);
                double time = bank.getFloat("time",row);
                double energy = bank.getFloat("energy",row);
                TaggerResponse ft = new TaggerResponse();

                ft.getDescriptor().setType(type);
               

               
                double z0 = 0; // FIXME vertex
                double path = Math.sqrt(x*x+y*y+(z-z0)*(z-z0)); 
                double cx = x / path;
                double cy = y / path;
                double cz = (z-z0) / path;
                ft.setMomentum(energy*cx,energy*cy,energy*cz);


                ft.setSize(size);
                ft.setID(id);
                ft.setEnergy(energy);
                ft.setRadius(radius);
                ft.setTime(time);
                ft.setHitIndex(row);
                ft.setPosition(x, y, z);
                ft.setPath(path);

                ft.getDescriptor().setType(type);

                responseList.add(ft);
            }
        }
        return responseList;
    }
    
}