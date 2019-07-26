package org.jlab.service.eb;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioFactory;
import org.jlab.clas.detector.*;

import org.jlab.rec.eb.EBScalers;
import org.jlab.rec.eb.EBCCDBEnum;
import org.jlab.rec.eb.EBCCDBConstants;

/**
 *
 * @author gavalian
 */
public class EBio {
    
    public static int  TRACKS_HB = 1;
    public static int  TRACKS_TB = 2;
    
    // read header bank information 
    public static DetectorHeader readHeader(DataEvent event, EBScalers ebs, EBCCDBConstants ccdb) {
        
        DetectorHeader dHeader = new DetectorHeader();
       
        if(event.hasBank("RUN::config")==true){
            DataBank bank = event.getBank("RUN::config");
            dHeader.setRun(bank.getInt("run", 0));
            dHeader.setEvent(bank.getInt("event", 0));
            dHeader.setTrigger(bank.getLong("trigger", 0));
        }

        // helicity:
        if(ccdb.getInteger(EBCCDBEnum.HELICITY_delay)==0 && event.hasBank("HEL::adc")) {
            final int helComponent=1;
            final int helHalf=2000;
            DataBank bank = event.getBank("HEL::adc");
            for (int ii=0; ii<bank.rows(); ii++) {
                if (bank.getInt("component",ii)==helComponent) {
                    byte helicity=-1;
                    if (bank.getInt("ped",ii)>helHalf) helicity=1;
                    dHeader.setHelicityRaw(helicity);
                    dHeader.setHelicity((byte)(helicity*ccdb.getInteger(EBCCDBEnum.HWP_position)));
                    break;
                }
            }
        }

        // scaler data for beam charge and livetime:
        //EBScalers.Reading ebsr = ebs.readScalers(event,ccdb);
        //dHeader.setBeamChargeGated((float)ebsr.getBeamCharge());
        //dHeader.setLiveTime((float)ebsr.getLiveTime());

        return dHeader;
    }
    
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
        List<DetectorParticle> dpList = new ArrayList<>();
    
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
                
                p.setCharge(bank.getInt("q", i));
                dpList.add(p);
            }
        }
        return dpList;
    }
    
    
    public static List<DetectorParticle>  readCentralTracks(DataEvent event){
        List<DetectorParticle> dpList = new ArrayList<>();
        if(event.hasBank("CVTRec::Tracks")==true){
            EvioDataBank bank = (EvioDataBank) event.getBank("CVTRec::Tracks");
            int nrows = bank.rows();
            for(int i = 0; i < nrows; i++){
                double pt = bank.getDouble("pt", i);
                double phi0 = bank.getDouble("phi0", i);
                double tandip = bank.getDouble("tandip", i);
                double z0 = bank.getDouble("z0", i);
                double d0 = bank.getDouble("d0", i);
                
                DetectorParticle part = new DetectorParticle();
                double pz = pt*tandip;
                double py = pt*Math.sin(phi0);
                double px = pt*Math.cos(phi0);
                
                double vx = d0*Math.cos(phi0);
                double vy = d0*Math.sin(phi0);
                
                part.vector().setXYZ(px, py, pz);
                part.vertex().setXYZ(vx, vy, z0);
                part.setCharge(bank.getInt("q", i));
                dpList.add(part);
            }
        }
        return dpList;
    }
    
    public static boolean isTimeBased(DataEvent de){
        boolean tb = false;
        if(de.hasBank("TimeBasedTrkg::TBHits")==true){
            return true;
        }
        return tb;
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
            
            bank.setInt("charge", i, p.getCharge());
            bank.setInt("pid", i, p.getPid());
            
            bank.setFloat("beta",i, (float) p.getBeta());
            
            bank.setFloat("px", i, (float) p.vector().x());
            bank.setFloat("py", i, (float) p.vector().y());
            bank.setFloat("pz", i, (float) p.vector().z());
            
            bank.setFloat("vx", i, (float) p.vertex().x());
            bank.setFloat("vy", i, (float) p.vertex().y());
            bank.setFloat("vz", i, (float) p.vertex().z());
        }
        
        return bank;
    }
    
    //write cherenkov responses
    
    public static DataBank writeResponses(List<DetectorResponse> responses, int type ){
        String bankName = "EVENTHB::particle";
        
        switch (type){
            case 1 : bankName = "EVENTHB::detector"; break;
            case 2 : bankName = "EVENTTB::detector"; break;
            default: break;
        }
        
        EvioDataBank  bank = EvioFactory.createBank(bankName, responses.size());
        
        for(int i = 0; i < responses.size();i++){
            bank.setInt("pindex", i,responses.get(i).getAssociation());
            bank.setInt("index", i,i);
            bank.setInt("detector", i, responses.get(i).getDescriptor().getType().getDetectorId());
            bank.setInt("sector", i, responses.get(i).getDescriptor().getSector());
            bank.setInt("layer", i, responses.get(i).getDescriptor().getLayer());
            
            bank.setFloat("X", i, (float) responses.get(i).getPosition().x());
            bank.setFloat("Y", i, (float) responses.get(i).getPosition().y());
            bank.setFloat("Z", i, (float) responses.get(i).getPosition().z());
            
            bank.setFloat("hX", i, (float) responses.get(i).getMatchedPosition().x());
            bank.setFloat("hY", i, (float) responses.get(i).getMatchedPosition().y());
            bank.setFloat("hZ", i, (float) responses.get(i).getMatchedPosition().z());
            
            bank.setFloat("path", i, (float) responses.get(i).getPath());
            bank.setFloat("time", i, (float) responses.get(i).getTime());
            bank.setFloat("energy", i, (float) responses.get(i).getEnergy());
            
        }
        return bank;
    }

    public static DataBank writeCherenkovResponses(List<CherenkovResponse> responses, int type ) {
        String bankName = "EVENTHB::particle";

        switch (type){
            case 1 : bankName = "EVENTHB::cherenkov"; break;
            case 2 : bankName = "EVENTTB::cherenkov"; break;
            default: break;
        }
        EvioDataBank  bank = EvioFactory.createBank(bankName, responses.size());
        for(int i = 0; i < responses.size();i++){
            bank.setInt("pindex", i,responses.get(i).getAssociation());
            bank.setInt("index", i,i);
            bank.setFloat("X", i, (float) responses.get(i).getHitPosition().x());
            bank.setFloat("Y", i, (float) responses.get(i).getHitPosition().y());
            bank.setFloat("Z", i, (float) responses.get(i).getHitPosition().z());
            bank.setFloat("time", i, (float) responses.get(i).getTime());
            bank.setFloat("nphe", i, (float) responses.get(i).getEnergy());
        }
        return bank;
    }

    public static DataBank writeTrigger(DetectorEvent event){
        String bankName = "Trigger::info";
        EvioDataBank  bank = EvioFactory.createBank(bankName, 1);
        //bank.setDouble("starttime", 0, event.getEventTrigger().getStartTime());
        //bank.setDouble("vertextime",0, event.getEventTrigger().getVertexTime());
        //bank.setDouble("rftime", 0, event.getEventTrigger().getRFTime());
        //bank.setInt("id",0,event.getEventTrigger().getTriggerID());
        return bank;
    }
    
    public static List<DetectorResponse> readECAL(DataEvent event){
        List<DetectorResponse> ecal = new ArrayList<>();
        if(event.hasBank("ECDetector::clusters")==true){
            EvioDataBank bank = (EvioDataBank) event.getBank("ECDetector::clusters");
            int nrows = bank.rows();
            for(int i = 0; i < nrows; i++){
                int sector  = bank.getInt("sector", i);
                int layer   = bank.getInt("layer", i);
                DetectorResponse resp = new DetectorResponse();
                resp.getDescriptor().setType(DetectorType.ECAL);
                resp.getDescriptor().setSectorLayerComponent(sector, layer, 0);
                resp.setPosition(
                        bank.getDouble("X", i),bank.getDouble("Y", i),
                        bank.getDouble("Z", i)
                        );
                resp.setTime(bank.getDouble("time", i));
                resp.setEnergy(bank.getDouble("energy", i));
                ecal.add(resp);
            }
        }
        return ecal;
    }
    
    public static List<DetectorResponse>  readFTOF(DataEvent event){
        List<DetectorResponse> ftof = new ArrayList<>();
        if(event.hasBank("FTOFRec::ftofhits")==true){
            EvioDataBank bank = (EvioDataBank) event.getBank("FTOFRec::ftofhits");
            int nrows = bank.rows();
            for(int i = 0; i < nrows; i++){
                int sector  = bank.getInt("sector", i);
                int layer   = bank.getInt("panel_id", i);
                int paddle  = bank.getInt("paddle_id", i);
                if(layer==2||layer==3){
                    DetectorResponse resp = new DetectorResponse();
                    resp.getDescriptor().setType(DetectorType.FTOF);
                    resp.getDescriptor().setSectorLayerComponent(sector, layer, paddle);
                    resp.setPosition(
                            bank.getFloat("x", i),bank.getFloat("y", i),
                            bank.getFloat("z", i)
                    );
                    resp.setTime(bank.getFloat("time", i));
                    resp.setEnergy(bank.getFloat("energy", i));
                    ftof.add(resp);
                }
            }
        }
        return ftof;
    }
    
    public static List<CherenkovResponse> readHTCC(DataEvent event) {
        List<CherenkovResponse> htcc = new ArrayList<>();
        if(event.hasBank("HTCCRec::clusters")==true){
            
            EvioDataBank bank = (EvioDataBank) event.getBank("HTCCRec::clusters");
            int nrows = bank.rows();
            for(int i = 0; i < nrows; i++){
                int nphe  = bank.getInt("nphe", i);
                double dtheta = bank.getDouble("dtheta",i);
                double dphi = bank.getDouble("dphi",i);
                double x = bank.getDouble("x",i);
                double y = bank.getDouble("y",i);
                double z = bank.getDouble("z",i);
                double time = bank.getFloat("time",i);
                CherenkovResponse che = new CherenkovResponse(dtheta,dphi);
                che.setHitPosition(x, y, z);
                che.setEnergy(nphe);
                che.setTime(time);
                che.getDescriptor().setType(DetectorType.HTCC);
                htcc.add(che);

            }
        }
        return htcc;
    }
}

