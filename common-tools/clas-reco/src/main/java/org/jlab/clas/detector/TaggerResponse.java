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
    
    private double    hitTime = 0.0;
    private int         hitID = -1;
    private int     hitCharge = -1;
    private double  hitEnergy = 0.0;
    private int   association = -1;

    private Vector3D         hitMomentum = new Vector3D();

    public TaggerResponse  setTime(double time) { hitTime = time; return this;}
    public void setID(int id){ hitID = id;}
    public void setCharge(int q){hitCharge = q;}
    public void  setEnergy(double energy) { hitEnergy = energy;}
    public void setAssociation(int assoc) {this.association = assoc;}
    
    public int getCharge(){return hitCharge;}
    public int getID(){return hitID;}
    public double getTime(){ return hitTime;}
    public double getEnergy(){ return hitEnergy;}
    public int getAssociation() {return this.association;}
    
    public Vector3D getMomentum(){
        return this.hitMomentum;
    }
    
    public void setMomentum(double px, double py, double pz){
        this.hitMomentum.setXYZ(px, py, pz);
    }
    
    public static List<TaggerResponse>  readHipoEvent(DataEvent event, 
        String bankName){        
        List<TaggerResponse> responseList = new ArrayList<TaggerResponse>();
        if(event.hasBank(bankName)==true){
            DataBank bank = event.getBank(bankName);
            int nrows = bank.rows();
            for(int row = 0; row < nrows; row++){
                int id  = bank.getInt("id", row);
                int charge = bank.getInt("charge", row);
                float cx = bank.getFloat("cx",row);
                float cy = bank.getFloat("cy",row);
                float cz = bank.getFloat("cz",row);
                float time = bank.getFloat("time",row);
                float energy = bank.getFloat("energy",row);
                TaggerResponse ft = new TaggerResponse();
                ft.setCharge(charge);
                ft.setID(id);
                ft.setEnergy(energy);
                ft.setTime(time);
                ft.setMomentum(cx*energy, cy*energy, cz*energy);
                responseList.add(ft);
            }
        }
        return responseList;
    }
    
}
