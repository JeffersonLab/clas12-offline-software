package org.jlab.clas.detector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.clas.physics.Particle;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author gavalian
 * @author baltzell
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
           CalorimeterResponse r = (CalorimeterResponse)responses.get(row);
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
           bank.setFloat("lu", row, (float) r.getCoordUVW().x()); 
           bank.setFloat("lv", row, (float) r.getCoordUVW().y()); 
           bank.setFloat("lw", row, (float) r.getCoordUVW().z()); 
           bank.setFloat("du", row, (float) r.getWidthUVW().x()); 
           bank.setFloat("dv", row, (float) r.getWidthUVW().y()); 
           bank.setFloat("dw", row, (float) r.getWidthUVW().z()); 
           bank.setFloat("m2u", row, (float) r.getSecondMomentUVW().x()); 
           bank.setFloat("m2v", row, (float) r.getSecondMomentUVW().y()); 
           bank.setFloat("m2w", row, (float) r.getSecondMomentUVW().z()); 
           bank.setFloat("m3u", row, (float) r.getThirdMomentUVW().x()); 
           bank.setFloat("m3v", row, (float) r.getThirdMomentUVW().y()); 
           bank.setFloat("m3w", row, (float) r.getThirdMomentUVW().z()); 
           bank.setFloat("path", row, (float) r.getPath());
           bank.setFloat("time", row, (float) r.getTime());
           bank.setFloat("energy", row, (float) r.getEnergy());
           bank.setFloat("chi2", row, (float) 0.0);
           bank.setShort("status",row,(short) r.getStatus());
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
           bank.setShort("status",row,(short) r.getStatus());
       }
       return bank;
   }
   
   public static DataBank getCherenkovResponseBank(List<DetectorResponse> responses, DataEvent event, String bank_name){
       DataBank bank = event.createBank(bank_name, responses.size());
       for(int row = 0; row < responses.size(); row++){
           CherenkovResponse c = (CherenkovResponse)responses.get(row);
           bank.setShort("index", row, (short) c.getHitIndex());
           bank.setShort("pindex", row, (short) c.getAssociation());
           bank.setByte("detector", row, (byte) c.getDescriptor().getType().getDetectorId());
           bank.setFloat("x", row, (float) c.getHitPosition().x());
           bank.setFloat("y", row, (float) c.getHitPosition().y());
           bank.setFloat("z", row, (float) c.getHitPosition().z());
           bank.setFloat("theta", row, (float) c.getTheta());
           bank.setFloat("phi", row, (float) c.getPhi());
           bank.setFloat("dtheta", row, (float) c.getDeltaTheta());
           bank.setFloat("dphi", row, (float) c.getDeltaPhi());
           bank.setFloat("path", row, (float) c.getPath());
           bank.setFloat("time", row, (float) c.getTime());
           bank.setFloat("nphe", row, (float) c.getEnergy());
           bank.setFloat("chi2", row, (float) 0.0);
           bank.setShort("status",row,(short) c.getStatus());
       }
       return bank;
   }
      
      public static DataBank getForwardTaggerBank(List<DetectorResponse> responses, DataEvent event, String bank_name){
       DataBank bank = event.createBank(bank_name, responses.size());
       int row = 0;
       for(int i = 0; i < responses.size(); i++){
           TaggerResponse t  = (TaggerResponse)responses.get(i);
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
       bank.setByte("Helic", 0, detectorEvent.getEventHeader().getHelicity());
       bank.setFloat("BCG", 0, detectorEvent.getEventHeader().getBeamChargeGated());
       bank.setDouble("LT", 0, detectorEvent.getEventHeader().getLivetime());
       bank.setShort("EvCAT", 0, detectorEvent.getEventHeader().getEventCategory());
       return bank;
   }
      
   public static DataBank getTracksBank(List<DetectorParticle> particles, DataEvent event, String bank_name, int rows) {
       DataBank bank = event.createBank(bank_name, rows);
       int row = 0;
       for(int i = 0 ; i < particles.size(); i++) {
           DetectorParticle p = particles.get(i);
           if(p.getTrackDetector()==DetectorType.DC.getDetectorId() ||
              p.getTrackDetector()==DetectorType.CVT.getDetectorId() ) {
               bank.setShort("index",   row, (short) p.getTrackIndex());
               bank.setShort("pindex",  row, (short) i);
               bank.setByte( "sector",  row, (byte)  p.getTrackSector());
               bank.setByte( "detector",row, (byte)  p.getTrackDetector());
               bank.setByte( "q",       row, (byte)  p.getCharge());
               bank.setFloat("chi2",    row, (float) p.getTrackChi2());
               bank.setShort("NDF",     row, (short) p.getNDF());
               bank.setFloat("px_nomm", row, (float) p.vector().x());
               bank.setFloat("py_nomm", row, (float) p.vector().y());
               bank.setFloat("pz_nomm", row, (float) p.vector().z());
               bank.setFloat("vx_nomm", row, (float) p.vertex().x());
               bank.setFloat("vy_nomm", row, (float) p.vertex().y());
               bank.setFloat("vz_nomm", row, (float) p.vertex().z());
               bank.setInt(  "status",  row, (int)   p.getTrackStatus());
               row = row + 1;
           }
       }
       return bank;
   }
     
   public static DataBank getTrajectoriesBank(List<DetectorParticle> particles, DataEvent event, String bank_name) {

       DataBank bank=null;
       if (bank_name!=null) {
           int nrows = 0;
           for(int i = 0 ; i < particles.size(); i++) {
               if(particles.get(i).getTrackDetector()==DetectorType.DC.getDetectorId() ||
                  particles.get(i).getTrackDetector()==DetectorType.CVT.getDetectorId() ) {
                   nrows += particles.get(i).getTrackTrajectory().size();
               }
           }

           bank = event.createBank(bank_name, nrows);
           int row = 0;
           for(int i = 0 ; i < particles.size(); i++) {
               DetectorParticle p = particles.get(i);
               if(p.getTrackDetector()==DetectorType.DC.getDetectorId() ||
                  p.getTrackDetector()==DetectorType.CVT.getDetectorId() ) {
                   for (int detId : p.getTrackTrajectory().keySet()) {
                       bank.setShort("index", row, (short) p.getTrackIndex());
                       bank.setShort("pindex", row, (short) i);
                       bank.setShort("detId", row, (byte) detId);
                       DetectorTrack.TrajectoryPoint tp = p.getTrackTrajectory().get(detId);
                       bank.setFloat("pathlength",row, tp.getPathLength());
                       bank.setFloat("x",row, (float)tp.getCross().origin().x());
                       bank.setFloat("y",row, (float)tp.getCross().origin().y());
                       bank.setFloat("z",row, (float)tp.getCross().origin().z());
                       bank.setFloat("cx",row, (float)tp.getCross().direction().x());
                       bank.setFloat("cy",row, (float)tp.getCross().direction().y());
                       bank.setFloat("cz",row, (float)tp.getCross().direction().z());
                       row = row + 1;
                   }
               }
           }
       }
       return bank;
   }
   
   public static DataBank getCovMatrixBank(List<DetectorParticle> particles, DataEvent event, String bank_name) {

       DataBank bank=null;
       if (bank_name!=null) {
           int nrows = 0;
           for(int i = 0 ; i < particles.size(); i++) {
               if(particles.get(i).getTrackDetector()==DetectorType.DC.getDetectorId() ||
                  particles.get(i).getTrackDetector()==DetectorType.CVT.getDetectorId() ) {
                   nrows += 1;
               }
           }

           bank = event.createBank(bank_name, nrows);
           int row = 0;
           for(int i = 0 ; i < particles.size(); i++) {
               DetectorParticle p = particles.get(i);
               if (p.getTrackDetector()!=DetectorType.DC.getDetectorId() &&
                   p.getTrackDetector()!=DetectorType.CVT.getDetectorId() ) continue;
               bank.setShort("index",row,(short)p.getTrackIndex());
               bank.setShort("pindex",row,(short)i);
               for (int ii=0; ii<5; ii++) {
                   for (int jj=0; jj<5; jj++) {
                       String varName = String.format("C%d%d",ii+1,jj+1);
                       if (bank.getDescriptor().hasEntry(varName)!=true) continue;
                       bank.setFloat(varName,row,p.getCovMatrix(ii,jj));
                   }
               }
               row++;
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
      
   public static Vector3D  readVector(DataBank bank, int row, String xc, String yc, String zc){
       Vector3D vec = new Vector3D();
       vec.setXYZ(bank.getFloat(xc, row), bank.getFloat(yc, row),bank.getFloat(zc, row));
       return vec;
   }
   
   public static List<DetectorTrack>  readDetectorTracks(DataEvent event, String bank_name, String traj_bank_name, String cov_bank_name){
       
       List<DetectorTrack>  tracks = new ArrayList<DetectorTrack>();
       if(event.hasBank(bank_name)==true){
           DataBank bank = event.getBank(bank_name);
           int nrows = bank.rows();

           DataBank trajBank = null;
           if (traj_bank_name!=null && event.hasBank(traj_bank_name)) {
               trajBank=event.getBank(traj_bank_name);
           }
           DataBank covBank = null;
           if (cov_bank_name!=null && event.hasBank(cov_bank_name)) {
               covBank=event.getBank(cov_bank_name);
           }

           for(int row = 0; row < nrows; row++){
               int    charge = bank.getByte("q", row);
               Vector3D pvec = DetectorData.readVector(bank, row, "p0_x", "p0_y", "p0_z");
               Vector3D vertex = DetectorData.readVector(bank, row, "Vtx0_x", "Vtx0_y", "Vtx0_z");

               DetectorTrack  track = new DetectorTrack(charge,pvec.mag(), (row));
               track.setVector(pvec.x(), pvec.y(), pvec.z());
               track.setVertex(vertex.x(), vertex.y(), vertex.z());
               track.setPath(bank.getFloat("pathlength", row));
               track.setSector(bank.getByte("sector", row));

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

               final int trkId=bank.getInt("id",row);

               // this could be optimized:
               if (trajBank!=null) {
                   for (int ii=0; ii<trajBank.rows(); ii++) {
                       if (trajBank.getInt("tid",ii) !=  trkId) continue;
                       int detId=trajBank.getInt("did",ii);
                       float bField=trajBank.getFloat("B",ii);
                       float pathLength=trajBank.getFloat("L",ii);
                       float xx=trajBank.getFloat("x",ii);
                       float yy=trajBank.getFloat("y",ii);
                       float zz=trajBank.getFloat("z",ii);
                       Line3D traj=new Line3D(xx,yy,zz,
                               xx+track.getMaxLineLength()*trajBank.getFloat("tx",ii),
                               yy+track.getMaxLineLength()*trajBank.getFloat("ty",ii),
                               zz+track.getMaxLineLength()*trajBank.getFloat("tz",ii));
                       track.addTrajectoryPoint(detId,traj,bField,pathLength);
                   }
               }
               if (covBank!=null) {
                   final int dimCovMat=5;
                   for (int ii=0; ii<covBank.rows(); ii++) {
                       if (covBank.getInt("id",ii) !=  trkId) continue;
                       for (int jj=1; jj<=dimCovMat; jj++) {
                           for (int kk=1; kk<=dimCovMat; kk++) {
                               float ele=covBank.getFloat(String.format("C%d%d",jj,kk),ii);
                               track.setCovMatrix(jj-1,kk-1,ele);
                           }
                       }
                   }
               }

               tracks.add(track);
           }
       }
       return tracks;
   }
   
   
   public static List<DetectorTrack>  readCentralDetectorTracks(DataEvent event, String bank_name, String traj_bank_name){
      
       // these are ordered by index (1,2,3,4,5):
       final String[] covVarNames={"d0","phi0","rho","z0","tandip"};
       
       List<DetectorTrack>  tracks = new ArrayList<DetectorTrack>();
       if(event.hasBank(bank_name)==true){
           DataBank bank = event.getBank(bank_name);
           int nrows = bank.rows();

           DataBank trajBank = null;
           if (traj_bank_name!=null && event.hasBank(traj_bank_name)) {
               trajBank=event.getBank(traj_bank_name);
           }

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
               track.setNDF(bank.getInt("ndf",row));
               track.setchi2(bank.getFloat("chi2",row));

               //track.addCTOFPoint(x,y,z);
               Vector3D hc_vec = DetectorData.readVector(bank, row, "c_x", "c_y", "c_z");
               Vector3D hc_dir = DetectorData.readVector(bank, row, "c_ux", "c_uy", "c_uz");
               track.addCross(hc_vec.x(), hc_vec.y(), hc_vec.z(), hc_dir.x(), hc_dir.y(), hc_dir.z());

               for (int ii=0; ii<5; ii++) {
                   for (int jj=0; jj<5; jj++) {
                       String varName = String.format("cov_%s%s",covVarNames[ii],ii==jj?"2":covVarNames[jj]);
                       if (bank.getDescriptor().hasEntry(varName)==false) continue;
                       track.setCovMatrix(ii,jj,bank.getFloat(varName,row));
                   }
               }


               track.setDetectorID(DetectorType.CVT.getDetectorId());

               final int trkId=bank.getInt("ID",row);

               // this could be optimized:
               if (trajBank!=null) {
                   for (int ii=0; ii<trajBank.rows(); ii++) {
                       if (trajBank.getInt("ID",ii) !=  trkId) continue;
                       int detId=trajBank.getInt("LayerTrackIntersPlane",ii);
                       float xx=trajBank.getFloat("XtrackIntersPlane",ii);
                       float yy=trajBank.getFloat("YtrackIntersPlane",ii);
                       float zz=trajBank.getFloat("ZtrackIntersPlane",ii);

                       float theta=trajBank.getFloat("ThetaTrackIntersPlane",ii);
                       float phi  =trajBank.getFloat("PhiTrackIntersPlane",ii);
                       
                       float cz = (float)(Math.cos(theta));
                       float cx = (float)(Math.sin(theta)*Math.cos(phi));
                       float cy = (float)(Math.sin(theta)*Math.sin(phi));
                       
                       Line3D traj=new Line3D(xx,yy,zz,
                               xx+track.getMaxLineLength()*cx,
                               yy+track.getMaxLineLength()*cy,
                               zz+track.getMaxLineLength()*cz);
                       track.addTrajectoryPoint(detId,traj);
                   }
               }

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

