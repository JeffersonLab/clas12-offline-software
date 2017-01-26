/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.detector;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Vector3D;
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
    public static List<DetectorResponse>  readDetectorResponses(DataEvent event, String bank_name){
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
    /**
     * reads Detector Event, detector particles and detector responses and 
     * then adds all associated responses to each particle.
     * @param event
     * @return 
     */
    public static DetectorEvent  readDetectorEvent(DataEvent event){
        return DetectorData.readDetectorEvent(event, "REC::Particle", "REC::Detector");
    }
    
    public static DetectorEvent  readDetectorEvent(DataEvent event, String particle_bank, String response_bank){
        
        List<DetectorParticle>  particles = DetectorData.readDetectorParticles(event, particle_bank);
        DetectorEvent detectorEvent = new DetectorEvent();
        for(DetectorParticle p : particles){
            detectorEvent.addParticle(p);
        }
        
        List<DetectorResponse> responses = DetectorData.readDetectorResponses(event, response_bank);
        for(DetectorResponse r : responses){
            int association = r.getAssociation();
            if(association>=0&&association<detectorEvent.getParticles().size()){
                detectorEvent.getParticles().get(association).addResponse(r);
            }
        }
        
        if(event.hasBank("MC::Particle")==true){
            DataBank  bank = event.getBank("MC::Particle");
            detectorEvent.getGeneratedEvent().clear();
            int nrows = bank.rows();
            for(int row = 0; row < nrows; row++){
                detectorEvent.getGeneratedEvent().addGeneratedParticle(
                        bank.getInt("pid", row),
                        bank.getFloat("px", row),
                        bank.getFloat("py", row),
                        bank.getFloat("pz", row),
                        bank.getFloat("vx", row),
                        bank.getFloat("vy", row),
                        bank.getFloat("vz", row)
                );
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
    public static DataBank getDetectorParticleBank(List<DetectorParticle> particles, DataEvent event, String bank_name){
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
    /**
     * creates a detector response bank
     * @param responses
     * @param event
     * @param bank_name
     * @return 
     */
   public static DataBank getDetectorResponseBank(List<DetectorResponse> responses, DataEvent event, String bank_name){
       DataBank bank = event.createBank(bank_name, responses.size());
       for(int row = 0; row < responses.size(); row++){
           DetectorResponse r = responses.get(row);
           bank.setShort("pindex", row, (short) r.getAssociation());
           bank.setShort("detector", row, (short) r.getDescriptor().getType().getDetectorId());
           bank.setShort("sector", row, (short) r.getDescriptor().getSector());
           bank.setShort("sector", row, (short) r.getDescriptor().getSector());
           bank.setShort("layer", row, (short) r.getDescriptor().getLayer());
           bank.setShort("component", row, (short) r.getDescriptor().getComponent());
           bank.setFloat("x", row, (float) r.getPosition().x());
           bank.setFloat("y", row, (float) r.getPosition().y());
           bank.setFloat("z", row, (float) r.getPosition().z());
           bank.setFloat("hx", row, (float) r.getMatchedPosition().x());
           bank.setFloat("hy", row, (float) r.getMatchedPosition().y());
           bank.setFloat("hz", row, (float) r.getMatchedPosition().z());
           bank.setFloat("path", row, (float) r.getPath());
           bank.setFloat("time", row, (float) r.getTime());
           bank.setFloat("energy", row, (float) r.getEnergy());
       }
       return bank;
   }
   
   public static Vector3D  readVector(DataBank bank, int row, String xc, String yc, String zc){
       Vector3D vec = new Vector3D();
       vec.setXYZ(bank.getFloat(xc, row), bank.getFloat(yc, row),bank.getFloat(zc, row));
       return vec;
   }
   
   public static List<DetectorTrack>  readDetectorTracks(DataEvent event, String bank_name){
       List<DetectorTrack>  tracks = new ArrayList<DetectorTrack>();
       if(event.hasBank(bank_name)==true){
           DataBank bank = event.getBank(bank_name);
           int nrows = bank.rows();
           
           for(int row = 0; row < nrows; row++){
               int    charge = bank.getByte("q", row);
               Vector3D pvec = DetectorData.readVector(bank, row, "p0_x", "p0_y", "p0_z");
               Vector3D vertex = DetectorData.readVector(bank, row, "Vtx0_x", "Vtx0_y", "Vtx0_z");
               
               DetectorTrack  track = new DetectorTrack(charge,pvec.mag());
               track.setVector(pvec.x(), pvec.y(), pvec.z());
               track.setVertex(vertex.x(), vertex.y(), vertex.z());
               track.setPath(bank.getFloat("pathlength", row));
               
               Vector3D lc_vec = DetectorData.readVector(bank, row, "c1_x", "c1_y", "c1_z");
               Vector3D lc_dir = DetectorData.readVector(bank, row, "c1_ux", "c1_uy", "c1_uz");
               
               Vector3D hc_vec = DetectorData.readVector(bank, row, "c3_x", "c3_y", "c3_z");
               Vector3D hc_dir = DetectorData.readVector(bank, row, "c3_ux", "c3_uy", "c3_uz");
               track.addCross(lc_vec.x(), lc_vec.y(), lc_vec.z(), lc_dir.x(), lc_dir.y(), lc_dir.z());
               track.addCross(hc_vec.x(), hc_vec.y(), hc_vec.z(), hc_dir.x(), hc_dir.y(), hc_dir.z());
               
               tracks.add(track);
           }
       }
       return tracks;
   }
}
