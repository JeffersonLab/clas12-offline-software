package org.jlab.clas.detector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.clas.physics.Particle;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Point3D;
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
        //System.out.println(" SIZE = " + responses.size());
        for(DetectorResponse r : responses){
            int association = r.getAssociation();
            if(association>=0&&association<detectorEvent.getParticles().size()){
                detectorEvent.getParticles().get(association).addResponse(r);
            }
        }
        
        detectorEvent.getPhysicsEvent().clear();
        
        for(DetectorParticle p : particles){
            if(p.getPid()==0){
                Particle part = Particle.createWithMassCharge(
                        p.getMass(),p.getCharge(),
                        p.vector().x(),p.vector().y(),p.vector().z(),
                        p.vertex().x(),p.vertex().y(),p.vertex().z()
                );
                detectorEvent.getPhysicsEvent().addParticle(part);
            } else {
                Particle part = Particle.createWithPid(p.getPid(), 
                        p.vector().x(),p.vector().y(),p.vector().z(),
                        p.vertex().x(),p.vertex().y(),p.vertex().z()
                        );
                detectorEvent.getPhysicsEvent().addParticle(part);
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
                detectorEvent.getPhysicsEvent().addGeneratedParticle(
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
            System.out.println("PID = " + particles.get(row).getPid());
            bank.setByte("charge",row, (byte) particles.get(row).getCharge());
            bank.setFloat("px", row, (float) particles.get(row).vector().x());
            bank.setFloat("py", row, (float) particles.get(row).vector().y());
            bank.setFloat("pz", row, (float) particles.get(row).vector().z());
            
            bank.setFloat("vx", row, (float) particles.get(row).vertex().x());
            bank.setFloat("vy", row, (float) particles.get(row).vertex().y());
            bank.setFloat("vz", row, (float) particles.get(row).vertex().z());
            
//            bank.setFloat("mass", row, (float) particles.get(row).getMass());
            bank.setFloat("beta", row, (float) particles.get(row).getBeta());
            bank.setShort("status", row, (short) particles.get(row).getStatus());
            bank.setFloat("chi2pid", row, (float) particles.get(row).getPidQuality());
        }
        System.out.println("++++++++++++++++++++");
        return bank;
    }
    /**
     * creates a detector response bank
     * @param responses
     * @param event
     * @param bank_name
     * @return 
     */
   public static DataBank getCalorimeterResponseBank(List<DetectorResponse> responses, DataEvent event, String bank_name){
       DataBank bank = event.createBank(bank_name, responses.size());
       for(int row = 0; row < responses.size(); row++){
           DetectorResponse r = responses.get(row);
           bank.setShort("index", row, (short) r.getHitIndex());
           bank.setShort("pindex", row, (short) r.getAssociation());
           bank.setByte("detector", row, (byte) r.getDescriptor().getType().getDetectorId());
           bank.setByte("sector", row, (byte) r.getDescriptor().getSector());
           bank.setByte("layer", row, (byte) r.getDescriptor().getLayer());
           bank.setFloat("x", row, (float) r.getPosition().x());
           bank.setFloat("y", row, (float) r.getPosition().y());
           bank.setFloat("z", row, (float) r.getPosition().z());
           bank.setFloat("hx", row, (float) r.getMatchedPosition().x());
           bank.setFloat("hy", row, (float) r.getMatchedPosition().y());
           bank.setFloat("hz", row, (float) r.getMatchedPosition().z());
           bank.setFloat("lu", row, (float) 0.0);
           bank.setFloat("lv", row, (float) 0.0);
           bank.setFloat("lw", row, (float) 0.0);
           bank.setFloat("du", row, (float) 0.0);
           bank.setFloat("dv", row, (float) 0.0);
           bank.setFloat("dw", row, (float) 0.0);
           bank.setFloat("m2u", row, (float) 0.0);
           bank.setFloat("m2v", row, (float) 0.0);
           bank.setFloat("m2w", row, (float) 0.0);
           bank.setFloat("path", row, (float) r.getPath());
           bank.setFloat("time", row, (float) r.getTime());
           bank.setFloat("energy", row, (float) r.getEnergy());
           bank.setFloat("chi2", row, (float) 0.0);
       }
       return bank;
   }
   
   public static DataBank getScintillatorResponseBank(List<DetectorResponse> responses, DataEvent event, String bank_name){
       DataBank bank = event.createBank(bank_name, responses.size());
       for(int row = 0; row < responses.size(); row++){
           DetectorResponse r = responses.get(row);
           bank.setShort("index",row,(short) r.getHitIndex());
           bank.setShort("pindex", row, (short) r.getAssociation());
           bank.setByte("detector", row, (byte) r.getDescriptor().getType().getDetectorId());
           bank.setByte("sector", row, (byte) r.getDescriptor().getSector());
           bank.setByte("layer", row, (byte) r.getDescriptor().getLayer());
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
           bank.setFloat("chi2", row, (float) 0.0);
       }
       return bank;
   }
   
   public static DataBank getCherenkovResponseBank(List<CherenkovResponse> responses, DataEvent event, String bank_name){
       DataBank bank = event.createBank(bank_name, responses.size());
       for(int row = 0; row < responses.size(); row++){
           CherenkovResponse c = responses.get(row);
           bank.setShort("index", row, (short) c.getHitIndex());
           bank.setShort("pindex", row, (short) c.getAssociation());
           bank.setByte("detector", row, (byte) c.getCherenkovType().getDetectorId());
           bank.setFloat("x", row, (float) c.getHitPosition().x());
           bank.setFloat("y", row, (float) c.getHitPosition().y());
           bank.setFloat("z", row, (float) c.getHitPosition().z());
           bank.setFloat("theta", row, (float) c.getTheta());
           bank.setFloat("phi", row, (float) c.getPhi());
           bank.setFloat("dtheta", row, (float) c.getDeltaTheta());
           bank.setFloat("dphi", row, (float) c.getDeltaPhi());
           bank.setFloat("path", row, (float) 0.0);
           bank.setFloat("time", row, (float) c.getTime());
           bank.setInt("nphe", row, (int) c.getEnergy());
           bank.setFloat("chi2", row, (float) 0.0);
       }
       return bank;
   }
      
      public static DataBank getForwardTaggerBank(List<TaggerResponse> responses, DataEvent event, String bank_name){
       DataBank bank = event.createBank(bank_name, responses.size());
       int row = 0;
       //System.out.println("Forward Tagger Bank Printing");                                                                                    
       for(int i = 0; i < responses.size(); i++){
           TaggerResponse t  = responses.get(i);
           //System.out.println(i + "  " + t.getAssociation());  
           //System.out.println(t.getTime() + " " + t.getEnergy());
           bank.setShort("index", row, (short) t.getHitIndex());
           bank.setShort("pindex", row, (short) t.getAssociation());
           bank.setByte("detector", row, (byte) t.getDescriptor().getType().getDetectorId());
           bank.setFloat("energy", row, (float) t.getEnergy());                                                                               
           bank.setFloat("time", row, (float) t.getTime());                                                                                   
           bank.setFloat("path", row, (float) t.getPath());                                                                                   
           bank.setFloat("x", row, (float) t.getPosition().x());                                                                              
           bank.setFloat("y", row, (float) t.getPosition().y());                                                                              
           bank.setFloat("z", row, (float) t.getPosition().z());                                                                              
           bank.setFloat("dx", row, (float) t.getPositionWidth().x());                                                                        
           bank.setFloat("dy", row, (float) t.getPositionWidth().y());                                                                        
           bank.setFloat("radius", row, (float) t.getRadius());                                                                               
           bank.setShort("size", row, (short) t.getSize());                                                                                   
           bank.setFloat("chi2", row, (float) 0.0);                                                                                           
           row = row + 1;
       }
       return bank;
      } 

   public static DataBank getEventBank(DetectorEvent detectorEvent, DataEvent event, String bank_name){
       DataBank bank = event.createBank(bank_name, 1);
       bank.setInt("NRUN", 0, detectorEvent.getEventHeader().getRun());
       bank.setInt("NEVENT", 0, detectorEvent.getEventHeader().getEvent());
       bank.setLong("TRG", 0, detectorEvent.getEventHeader().getTrigger());
       bank.setFloat("STTime", 0, (float) detectorEvent.getEventHeader().getStartTime());
       bank.setFloat("RFTime", 0, (float) detectorEvent.getEventHeader().getRfTime());     
       return bank;
   }
      
   public static DataBank getTracksBank(List<DetectorParticle> particles, DataEvent event, String bank_name, int rows) {
       DataBank bank = event.createBank(bank_name, rows);
       int row = 0;
       for(int i = 0 ; i < particles.size(); i++) {
           DetectorParticle p = particles.get(i);
           if(p.getTrackDetector()==DetectorType.DC.getDetectorId()) {
              // FIXME:  CD will probably have to be done differently, since it's already matched
              // || p.getTrackDetector()==DetectorType.CVT.getDetectorId() ) {
               bank.setShort("index", row, (short) p.getTrackIndex());
               bank.setShort("pindex", row, (short) i);
               bank.setByte("detector", row, (byte) p.getTrackDetector());
               bank.setByte("q", row, (byte) p.getCharge());
               bank.setFloat("chi2", row, (float) p.getChi2());
               bank.setShort("NDF", row, (short) p.getNDF());
               bank.setFloat("px_nomm", row, (float) p.vector().x());
               bank.setFloat("py_nomm", row, (float) p.vector().y());
               bank.setFloat("pz_nomm", row, (float) p.vector().z());
               bank.setFloat("vx_nomm", row, (float) p.vertex().x());
               bank.setFloat("vy_nomm", row, (float) p.vertex().y());
               bank.setFloat("vz_nomm", row, (float) p.vertex().z());
               row = row + 1;
           }
       }
       return bank;
   }
      
   public static DataBank getCrossBank(List<DetectorParticle> particles, DataEvent event, String bank_name) {
       DataBank bank = event.createBank(bank_name, particles.size());
       for(int row = 0 ; row < particles.size(); row++){
           DetectorParticle p = particles.get(row);
           bank.setShort("pindex", row, (short) row);
           bank.setFloat("c_x", row, (float) p.getCross().x());
           bank.setFloat("c_y", row, (float) p.getCross().y());
           bank.setFloat("c_z", row, (float) p.getCross().z());
           bank.setFloat("c_ux", row, (float) p.getCrossDir().x());
           bank.setFloat("c_uy", row, (float) p.getCrossDir().y());
           bank.setFloat("c_uz", row, (float) p.getCrossDir().z());

       }
       return bank;
   }
      
   public static List<double[]> readTBCovMat(DataEvent event, String bank_name) {
       List<double[]> covMat = new ArrayList<double[]>();
       if(event.hasBank(bank_name)==true){
           DataBank bank = event.getBank(bank_name);
           int nrows = bank.rows();
           for(int row = 0; row < nrows; row++){
               double[] covariance = new double[15];
               covariance[0] = bank.getFloat("C11", row);
               covariance[1] = bank.getFloat("C12", row);
               covariance[2] = bank.getFloat("C13", row);
               covariance[3] = bank.getFloat("C14", row);
               covariance[4] = bank.getFloat("C15", row);
               covariance[5] = bank.getFloat("C22", row);
               covariance[6] = bank.getFloat("C23", row);
               covariance[7] = bank.getFloat("C24", row);
               covariance[8] = bank.getFloat("C25", row);
               covariance[9] = bank.getFloat("C33", row);
               covariance[10] = bank.getFloat("C34", row);
               covariance[11] = bank.getFloat("C35", row);
               covariance[12] = bank.getFloat("C44", row);
               covariance[13] = bank.getFloat("C45", row);
               covariance[14] = bank.getFloat("C55", row);
               covMat.add(covariance);
           }
       }

       return covMat;
   }
   
   public static DataBank getTBCovMatBank(List<DetectorParticle> particles, DataEvent event, String bank_name) {
       DataBank bank = event.createBank(bank_name, particles.size());
       for(int row = 0; row < particles.size(); row++){
           DetectorParticle p = particles.get(row);
           double[] matrix =  p.getTBCovariantMatrix();
           bank.setShort("pindex", row, (short) row);
           bank.setShort("C11", row, (short) matrix[0]);
           bank.setShort("C12", row, (short) matrix[1]);
           bank.setShort("C13", row, (short) matrix[2]);
           bank.setShort("C14", row, (short) matrix[3]);
           bank.setShort("C15", row, (short) matrix[4]);
           bank.setShort("C22", row, (short) matrix[5]);
           bank.setShort("C23", row, (short) matrix[6]);
           bank.setShort("C24", row, (short) matrix[7]);
           bank.setShort("C25", row, (short) matrix[8]);
           bank.setShort("C33", row, (short) matrix[9]);
           bank.setShort("C34", row, (short) matrix[10]);
           bank.setShort("C35", row, (short) matrix[11]);
           bank.setShort("C44", row, (short) matrix[12]);
           bank.setShort("C45", row, (short) matrix[13]);
           bank.setShort("C55", row, (short) matrix[14]);
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

               DetectorTrack  track = new DetectorTrack(charge,pvec.mag(), (row));
               track.setVector(pvec.x(), pvec.y(), pvec.z());
               track.setVertex(vertex.x(), vertex.y(), vertex.z());
               track.setPath(bank.getFloat("pathlength", row));

               // t1 = HTCC, c1 = DCR1, c3 = DCR3
               Vector3D lc_vec = DetectorData.readVector(bank, row, "t1_x", "t1_y", "t1_z");
               Vector3D lc_dir = DetectorData.readVector(bank, row, "t1_px", "t1_py", "t1_pz");

               Vector3D hc_vec = DetectorData.readVector(bank, row, "c3_x", "c3_y", "c3_z");
               Vector3D hc_dir = DetectorData.readVector(bank, row, "c3_ux", "c3_uy", "c3_uz");
               track.addCross(lc_vec.x(), lc_vec.y(), lc_vec.z(), lc_dir.x(), lc_dir.y(), lc_dir.z());
               track.addCross(hc_vec.x(), hc_vec.y(), hc_vec.z(), hc_dir.x(), hc_dir.y(), hc_dir.z());

               track.setNDF(bank.getInt("ndf",row));
               track.setchi2(bank.getFloat("chi2",row));
               track.setStatus(bank.getInt("status",row));

               track.setDetectorID(DetectorType.DC.getDetectorId());

               tracks.add(track);
           }
       }
       return tracks;
   }
   
   
   public static List<DetectorTrack>  readCentralDetectorTracks(DataEvent event, String bank_name){
       List<DetectorTrack>  tracks = new ArrayList<DetectorTrack>();
       if(event.hasBank(bank_name)==true){
           DataBank bank = event.getBank(bank_name);
           int nrows = bank.rows();

           for(int row = 0; row < nrows; row++){
               int charge  = bank.getInt("q", row);               
               double p    = bank.getFloat("p",row);
               double pt   = bank.getFloat("pt",row);
               double phi0 = bank.getFloat("phi0",row);
               double tandip = bank.getFloat("tandip", row);
               double z0 = bank.getFloat("z0", row);
               double d0 = bank.getFloat("d0", row);

               double pz = pt*tandip;
               double py = pt*Math.sin(phi0);
               double px = pt*Math.cos(phi0);

               double vx = d0*Math.cos(phi0);
               double vy = d0*Math.sin(phi0);

               DetectorTrack  track = new DetectorTrack(charge,p,row);
               track.setVector(px, py, pz);
               track.setVertex(vx, vy, z0);
               track.setPath(bank.getFloat("pathlength", row));
               // FIXME:  is this the correct chi2:
               track.setchi2(bank.getFloat("circlefit_chi2_per_ndf",row));

               //track.addCTOFPoint(x,y,z);
               Vector3D hc_vec = DetectorData.readVector(bank, row, "c_x", "c_y", "c_z");
               Vector3D hc_dir = DetectorData.readVector(bank, row, "c_ux", "c_uy", "c_uz");
               track.addCross(hc_vec.x(), hc_vec.y(), hc_vec.z(), hc_dir.x(), hc_dir.y(), hc_dir.z());

               track.setDetectorID(DetectorType.CVT.getDetectorId());

               tracks.add(track);
           }
       }
       return tracks;
   }
   
   public static List<DetectorParticle>  readForwardTaggerParticles(DataEvent event, String bank_name){
        List<DetectorParticle>  particles = new ArrayList<DetectorParticle>();
        if(event.hasBank(bank_name)==true){
            DataBank bank = event.getBank(bank_name);
            int nrows = bank.rows();

            for(int row = 0; row < nrows; row++){
                int charge  = bank.getByte("charge", row);
                double cx   = bank.getFloat("cx",row);
                double cy   = bank.getFloat("cy",row);
                double cz   = bank.getFloat("cz",row);
                double energy = bank.getFloat("energy",row);
                int pid = -1;

                DetectorTrack track = new DetectorTrack(charge, cx*energy ,cy*energy, cz*energy);
                track.setDetectorID(DetectorType.FTCAL.getDetectorId());
                DetectorParticle particle = new DetectorParticle(track);
                
                if(charge==0) pid = 22;
                if(charge<0) pid = 11;
               
                //System.out.println("Particle ID " + pid);
                
                particle.setPid(pid);
                particles.add(particle);
            }
        }
        return particles;
    }
   
   public static List<Map<DetectorType,Integer>>  readForwardTaggerIndex(DataEvent event, String bank_name){
        List<Map<DetectorType, Integer>>  indexmaps = new ArrayList<Map<DetectorType, Integer>>();
        if(event.hasBank(bank_name)==true){
            DataBank bank = event.getBank(bank_name);
            int nrows = bank.rows();

            for(int row = 0; row < nrows; row++){
                Map<DetectorType, Integer> particleFT_indices = new HashMap<DetectorType, Integer>();
                int calID  = bank.getShort("calID", row);
                int hodoID = bank.getShort("hodoID" , row);
                particleFT_indices.put(DetectorType.FTCAL, calID);
                particleFT_indices.put(DetectorType.FTHODO, hodoID);
                indexmaps.add(particleFT_indices);
            }
        }
        return indexmaps;
    }
   
}

