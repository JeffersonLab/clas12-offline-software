/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.detector;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author gavalian
 */
public class DetectorData {
    /**
     * Read detector hits from the bank
     * @param event
     * @param bank_name
     * @return 
     */
    public static List<DetectorResponse>  readHipoEvent(DataEvent event, String bank_name){
        List<DetectorResponse>  responses = new ArrayList<DetectorResponse>();
        if(event.hasBank(bank_name)==true){
            DataBank bank = event.getBank(bank_name);

            int nrows = bank.rows();
            for(int row = 0 ; row < nrows; row++){
                DetectorResponse  response = new DetectorResponse();
                response.getDescriptor().setType(DetectorType.getType((int) bank.getShort("detector",row)));
                response.getDescriptor().setSectorLayerComponent(
                        bank.getShort("sector", row),
                        bank.getShort("layer", row),
                        bank.getShort("component", row)
                );
                response.setPath(bank.getFloat("path", row));
                response.setTime(bank.getFloat("time", row));
                response.setEnergy(bank.getFloat("energy", row));
                response.setAssociation(bank.getShort("pindex", row));
                response.getPosition().setXYZ(
                        bank.getFloat("x", row),
                        bank.getFloat("y", row),
                        bank.getFloat("z", row)
                        );
                response.getMatchedPosition().setXYZ(
                        bank.getFloat("hx", row),
                        bank.getFloat("hy", row),
                        bank.getFloat("hz", row)
                );
                responses.add(response);
            }
        }
        return responses;
    }
    /**
     * 
     * @param event
     * @param bank_name
     * @return 
     */
    public static List<DetectorParticle>  readDetectorParticles(DataEvent event, String bank_name){
        List<DetectorParticle>  particles = new ArrayList<DetectorParticle>();
        if(event.hasBank(bank_name)==true){
            DataBank bank = event.getBank(bank_name);
            int nrows = bank.rows();
            for(int row = 0 ; row < nrows; row++){
                DetectorParticle particle = new DetectorParticle();
                particle.setPid(bank.getInt("pid", row));
                particle.vector().setXYZ(
                        bank.getFloat("px", row),
                        bank.getFloat("py", row),
                        bank.getFloat("pz", row));
                particle.vertex().setXYZ(
                        bank.getFloat("vx", row),
                        bank.getFloat("vy", row),
                        bank.getFloat("vz", row));
                particle.setCharge((int) bank.getByte("charge", row));
                particle.setMass(bank.getFloat("mass", row));
                particle.setBeta(bank.getFloat("beta", row));
                particles.add(particle);
            }
        }
        return particles;
    }
    
    public static DetectorEvent  readDetectorEvent(DataEvent event){
        return DetectorData.readDetectorEvent(event, "REC::Particle", "REC::Detector");
    }
    
    public static DetectorEvent  readDetectorEvent(DataEvent event, String particle_bank, String response_bank){
        
        List<DetectorParticle>  particles = DetectorData.readDetectorParticles(event, particle_bank);
        DetectorEvent detectorEvent = new DetectorEvent();
        for(DetectorParticle p : particles){
            detectorEvent.addParticle(p);
        }
        
        List<DetectorResponse> responses = DetectorData.readHipoEvent(event, response_bank);
        for(DetectorResponse r : responses){
            int association = r.getAssociation();
            if(association>=0&&association<detectorEvent.getParticles().size()){
                detectorEvent.getParticles().get(association).addResponse(r);
            }
        }
        return detectorEvent;
    }
    /**
     * creates a bank with particles information.
     * @param particles
     * @param event
     * @param bank_name
     * @return 
     */
    public static DataBank getDetectorParticles(List<DetectorParticle> particles, DataEvent event, String bank_name){
        DataBank bank = event.createBank(bank_name, particles.size());
        for(int row = 0; row < particles.size(); row++){
            bank.setInt("pid",row,particles.get(row).getPid());
            bank.setByte("charge",row, (byte) particles.get(row).getCharge());
            bank.setFloat("px", row, (float) particles.get(row).vector().x());
            bank.setFloat("py", row, (float) particles.get(row).vector().y());
            bank.setFloat("pz", row, (float) particles.get(row).vector().z());
            
            bank.setFloat("vx", row, (float) particles.get(row).vertex().x());
            bank.setFloat("vy", row, (float) particles.get(row).vertex().y());
            bank.setFloat("vz", row, (float) particles.get(row).vertex().z());
            
            bank.setFloat("mass", row, (float) particles.get(row).getMass());
            bank.setFloat("beta", row, (float) particles.get(row).getBeta());
            bank.setByte("status", row, (byte) particles.get(row).getStatus());
        }
        return bank;
    }
    
   
}
