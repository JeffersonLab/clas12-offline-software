package org.jlab.rec.ft;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.base.DetectorLayer;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.rec.ft.trk.FTTRKConstantsLoader;
import org.jlab.detector.base.DetectorType;

public class FTEventBuilder {

    public static int debugMode = -1; // hardcoded value here PROVISIONAL
    private double solenoidField;


    public FTEventBuilder() {
    }

    public double getField() {
        return solenoidField;
    }

    public void setField(double field) {
        this.solenoidField = field;
    }

    public void setDebugMode(int debug){
        this.debugMode = debug;
    }
    
    public void init(double field) {
        if (debugMode >= 1) {
            System.out.println("New event");
        }
        this.solenoidField = field;
    }

    public List<FTResponse> addResponses(DataEvent event, ConstantsManager manager, int run) {
        List<FTResponse> responses = new ArrayList<>();
                
        if (event.hasBank("FTCAL::clusters") == true) {
            DataBank bank = event.getBank("FTCAL::clusters");
            int nrows = bank.rows();
            for (int i = 0; i < nrows; i++) {
                FTResponse resp = new FTResponse(DetectorType.FTCAL);
                resp.setAssociation(-1);
                resp.setSize(bank.getInt("size", i));
                resp.setId(bank.getInt("id", i));
                resp.setEnergy(bank.getFloat("energy", i));
                resp.setTime(bank.getFloat("time", i));
                resp.setPosition(bank.getFloat("x", i), bank.getFloat("y", i), bank.getFloat("z", i));
                responses.add(resp);
            }
        }
        if (event.hasBank("FTHODO::clusters") == true) {
            DataBank bank = event.getBank("FTHODO::clusters");
            int nrows = bank.rows();
            for (int i = 0; i < nrows; i++) {
                FTResponse resp = new FTResponse(DetectorType.FTHODO);
                resp.setAssociation(-1);
                resp.setSize(bank.getInt("size", i));
                resp.setId(bank.getInt("id", i));
                resp.setEnergy(bank.getFloat("energy", i));
                resp.setTime(bank.getFloat("time", i));
                resp.setPosition(bank.getFloat("x", i), bank.getFloat("y", i), bank.getFloat("z", i));
                responses.add(resp);
            }
        }
        if (event.hasBank("FTTRK::crosses") == true) {
            int TRK1 = DetectorLayer.FTTRK_MODULE1 - 1;   // module1, tracker id=0 (int index for loops)
            int TRK2 = DetectorLayer.FTTRK_MODULE2 - 1;   // module2, tracker id=1
            DataBank bank = event.getBank("FTTRK::crosses");
            int nrows = bank.rows();
            for (int i = 0; i < nrows; i++) {
                FTResponse resp = new FTResponse(DetectorType.FTTRK);
                resp.setAssociation(-1);                    
                resp.setSize(bank.getInt("size", i));
                resp.setId(bank.getInt("id", i));
                resp.setEnergy(bank.getFloat("energy", i));
                resp.setTime(bank.getFloat("time", i));
                resp.setPosition(bank.getFloat("x", i), bank.getFloat("y", i), bank.getFloat("z", i));
                double zCoord = bank.getFloat("z", i);

                if(zCoord >= FTTRKConstantsLoader.Zlayer[TRK1] && zCoord <= FTTRKConstantsLoader.Zlayer[TRK2]){
                    resp.setModule(DetectorLayer.FTTRK_MODULE1);
                }else{
                    resp.setModule(DetectorLayer.FTTRK_MODULE2);
                }

                if(debugMode>=1) System.out.println(" --------- id, cross x, y, z " + bank.getInt("id", i) + " " + bank.getFloat("x", i) + " " + bank.getFloat("y", i) + " " + bank.getFloat("z", i));
                responses.add(resp);
            }
        }

        
        if (debugMode >= 1) {
            this.showResponses(responses);
        }
        return responses;
    }
	
    public void correctDirection(List<FTParticle> particles, ConstantsManager manager, int run) {
        
        IndexedTable thetaCorr = manager.getConstants(run, "/calibration/ft/ftcal/thetacorr");
        IndexedTable phiCorr   = manager.getConstants(run, "/calibration/ft/ftcal/phicorr");
        for (int i = 0; i < particles.size(); i++) {
            FTParticle particle = particles.get(i);
            particle.setDirection(thetaCorr, phiCorr);
        }     
    }
    
    public List<FTParticle> initFTparticles(List<FTResponse> responses, ConstantsManager manager, int run) {
        IndexedTable target = manager.getConstants(run, "/geometry/target");

        List<FTParticle> particles = new ArrayList<>();
        for (int i = 0; i < responses.size(); i++) {
            if (responses.get(i).getType()==DetectorType.FTCAL) {
                // start assuming the cluster to be associated to a photon
                FTParticle track = new FTParticle(i, 0, this.solenoidField, responses.get(i), 0, 0, target.getDoubleValue("position", 0,0,0));
                particles.add(track);
                responses.get(i).setAssociation(particles.size()-1);
            }
        }
        if (debugMode >= 1) {
            for (int i = 0; i < particles.size(); i++) {
                particles.get(i).show();
            }
        }
        return particles;
    }

    public void matchToHODO(List<FTResponse> responses, List<FTParticle> particles) {
        for (int i = 0; i < particles.size(); i++) {
            FTParticle track = particles.get(i);
            if (debugMode >= 1) {
                System.out.println("Searching for matching signal in the hodoscope:");
            }
            int iHodo = track.getDetectorHit(responses, DetectorType.FTHODO, FTConstants.CAL_HODO_DISTANCE_MATCHING, FTConstants.CAL_HODO_TIME_MATCHING);
            if (iHodo > 0) {
                if (debugMode >= 1) {
                    System.out.println("found signal in the hodoscope " + iHodo);
                }
                track.setCharge(-1);
                track.setHodoscopeIndex(responses.get(iHodo).getId());
                responses.get(iHodo).setAssociation(i);
            }
            if (debugMode >= 1) {
                track.show();
            }
        }
    }
   
    public void matchToTRKTwoDetectorsMultiHits(List<FTResponse> responses, List<FTParticle> particles) {
        for (int i = 0; i < particles.size(); i++) {
            FTParticle track = particles.get(i);
            if (debugMode >= 1) {
                System.out.println("Searching for matching signal in the tracker:");
            }
            int[][] iTrk;
            iTrk = track.getTRKOrderedListOfHits(responses, i, FTConstants.CAL_TRK_DISTANCE_MATCHING, FTConstants.CAL_TRK_TIME_MATCHING);
            int nHitsOnTRK = iTrk.length;
            for(int j=0; j<FTTRKConstantsLoader.NSupLayers; j++){ // j: loop on two detectors
                for(int k=0; k<nHitsOnTRK; k++){ // k: loop on hits on TRK
                    if (iTrk[k][j] > 0) {
                        if (debugMode >= 1) System.out.println("found signal in FTTRK" + iTrk[k][j]);
//                        track.setCharge(-999); // provisional, for no field tracking
                        track.setTrackerIndex(responses.get(iTrk[k][j]).getId(), j); 
                        responses.get(iTrk[k][j]).setHitIndex(iTrk[k][j]);
                        responses.get(iTrk[k][j]).setAssociation(i);
                        responses.get(iTrk[k][j]).setMatchPosition(track.getPosition().x(), track.getPosition().y(), track.getPosition().z());
                        if(debugMode>=1) System.out.println("matched cross coordinates " + responses.get(iTrk[k][j]).getPosition().x() + " " + 
                            responses.get(iTrk[k][j]).getPosition().y());
                    }else{  
                        // wrong cross coordinates need to be deleted - consider if any action shuould be taken here TOBEDONE
                    }
                }
            }
            if (debugMode >= 1) track.show();
        }
    }
    
    public void showResponses(List<FTResponse> responses) {
        System.out.println("\nFound " + responses.size() + " clusters in FT detector");
        for (int i = 0; i < responses.size(); i++) {
            responses.get(i).show();
        }
    }

    public void writeBanks(DataEvent event, List<FTParticle> particles) {
        if (debugMode >= 1) {
            System.out.println("Preparing to output track bank with " + particles.size() + " FTparticles");
        }
        if (!particles.isEmpty()) {

            DataBank banktrack = event.createBank("FT::particles", particles.size());
            if (debugMode >= 1) {
                System.out.println("Creating output track bank with " + particles.size() + " FTparticles");
            }
            for (int i = 0; i < particles.size(); i++) {
                banktrack.setShort("id", i, (short) particles.get(i).get_ID());
                banktrack.setByte("charge", i, (byte) particles.get(i).getCharge());
                banktrack.setFloat("energy", i, (float) particles.get(i).getEnergy());
                banktrack.setFloat("cx", i, (float) particles.get(i).getDirection().x());
                banktrack.setFloat("cy", i, (float) particles.get(i).getDirection().y());
                banktrack.setFloat("cz", i, (float) particles.get(i).getDirection().z());
                banktrack.setFloat("vx", i, (float) particles.get(i).getVertex().x());
                banktrack.setFloat("vy", i, (float) particles.get(i).getVertex().y());
                banktrack.setFloat("vz", i, (float) particles.get(i).getVertex().z());
                banktrack.setFloat("time", i, (float) particles.get(i).getTime());
                banktrack.setShort("calID", i, (short) particles.get(i).getCalorimeterIndex());
                banktrack.setShort("hodoID", i, (short) particles.get(i).getHodoscopeIndex());
                banktrack.setShort("trk0ID", i, (short) particles.get(i).getTrackerIndex(DetectorLayer.FTTRK_MODULE1-1));
                banktrack.setShort("trk1ID", i, (short) particles.get(i).getTrackerIndex(DetectorLayer.FTTRK_MODULE2-1));

                if (debugMode >= 1) {
                    particles.get(i).show();
                    System.out.println(particles.get(i).getDirection().x() + " " + particles.get(i).getDirection().y() + " " + particles.get(i).getDirection().z());
                }
            }
            if (banktrack != null) {
                event.appendBanks(banktrack);
            }
            
        }
    }

}
