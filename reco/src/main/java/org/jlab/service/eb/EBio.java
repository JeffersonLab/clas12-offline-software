/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.eb;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.detector.DetectorParticle;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioFactory;

/**
 *
 * @author gavalian
 */
public class EBio {
    
    public static int  TRACKS_HB = 1;
    public static int  TRACKS_TB = 2;
    
    /**
     * Read tracks from tracking.
     * @param event
     * @param type
     * @return 
     */
    public static List<DetectorParticle>  readTracks(DataEvent event, int type){
        String bankName = "HitBasedTrkg::HBTracks";
        switch (type){
            case 1 : bankName =  "HitBasedTrkg::HBTracks"; break;
            case 2 : bankName = "TimeBasedTrkg::TBTracks"; break;
            default: break;
        }
        List<DetectorParticle> dpList = new ArrayList<DetectorParticle>();
    
        if(event.hasBank(bankName)==true){
            EvioDataBank bank = (EvioDataBank) event.getBank(bankName);
            
            int nrows = bank.rows();
            
            for(int i = 0; i < nrows; i++){
                DetectorParticle p = new DetectorParticle();
                
                p.vector().setXYZ(
                        bank.getDouble("p0_x",i),
                        bank.getDouble("p0_y",i),
                        bank.getDouble("p0_z",i));
                
                p.vertex().setXYZ(
                        bank.getDouble("Vtx0_x",i),
                        bank.getDouble("Vtx0_y",i),
                        bank.getDouble("Vtx0_z",i));
                
                p.setCross( 
                        bank.getDouble("c3_x", i),
                        bank.getDouble("c3_y", i),
                        bank.getDouble("c3_z", i),
                        bank.getDouble("c3_ux", i),
                        bank.getDouble("c3_uy", i),
                        bank.getDouble("c3_uz", i)
                );
                
                p.setCharge(bank.getInt("q", i));
                dpList.add(p);
            }
        }
        return dpList;
    }
    
    public static boolean isTimeBased(DataEvent de){
        return de.hasBank("TimeBasedTrkg::TBTracks");
    }
    
    public static List<DetectorParticle>  readTracks(DataEvent event){
        return readTracks(event,EBio.TRACKS_TB);
    }
    
    
    /**
     * Creates a DataBank from list of particles. type will
     * indicate which bank to create type=TRACKS_HB will create
     * a EVENTHB bank, and type=TRACKS_TB will create a bank EVENTTB.
     * @param particles list of detector particles
     * @param type type of the bank to create (Time based or Hit based)
     * @return 
     */
    public static DataBank  writeTraks(List<DetectorParticle> particles, int type){
        
        String bankName = "EVENTHB::particle";
        
        switch (type){
            case 1 : bankName = "EVENTHB::particle"; break;
            case 2 : bankName = "EVENTTB::particle"; break;
            default: break;
        }
        
        EvioDataBank  bank = EvioFactory.createBank(bankName, particles.size());
        
        for(int i = 0; i < particles.size(); i++){
            
            DetectorParticle p = particles.get(i);
            
            bank.setInt("status", i, p.getStatus());
            bank.setInt("charge", i, p.getCharge());
            bank.setInt("pid", i, p.getPid());
            
            bank.setFloat("px", i, (float) p.vector().x());
            bank.setFloat("py", i, (float) p.vector().y());
            bank.setFloat("pz", i, (float) p.vector().z());
            
            bank.setFloat("vx", i, (float) p.vertex().x());
            bank.setFloat("vy", i, (float) p.vertex().y());
            bank.setFloat("vz", i, (float) p.vertex().z());
        }
        
        return bank;
    }
}
