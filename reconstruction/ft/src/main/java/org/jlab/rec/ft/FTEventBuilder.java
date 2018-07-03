package org.jlab.rec.ft;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.rec.ft.cal.FTCALConstantsLoader;
import org.jlab.utils.groups.IndexedTable;

public class FTEventBuilder {

    public int debugMode = 0;

    private double solenoidField;


    public FTEventBuilder() {
    }

//    public List<FTParticle> getFTparticles() {
//        return FTparticles;
//    }
//
//    public List<FTResponse> getFTresponses() {
//        return FTresponses;
//    }
//
//    public void setFTresponses(List<FTResponse> FTresponses) {
//        this.FTresponses = FTresponses;
//    }
    public double getField() {
        return solenoidField;
    }

    public void setField(double field) {
        this.solenoidField = field;
    }

    public void init(double field) {
        if (debugMode >= 1) {
            System.out.println("New event");
        }
        this.solenoidField = field;
//        this.FTparticles.clear();
//        this.FTresponses.clear();
    }

    public List<FTResponse> addResponses(DataEvent event, ConstantsManager manager, int run) {
        List<FTResponse> responses = new ArrayList<FTResponse>();
        
        IndexedTable cluster = manager.getConstants(run, "/calibration/ft/ftcal/cluster");
                
        if (event instanceof EvioDataEvent) {
            if (event.hasBank("FTCALRec::clusters") == true) {
                EvioDataBank bank = (EvioDataBank) event.getBank("FTCALRec::clusters");
                int nrows = bank.rows();
                for (int i = 0; i < nrows; i++) {
                    FTResponse resp = new FTResponse("FTCAL");
                    resp.setAssociation(-1);
                    resp.setSize(bank.getInt("clusSize", i));
                    resp.setId(bank.getInt("clusID", i));
                    resp.setEnergy(bank.getDouble("clusEnergy", i));
                    resp.setTime(bank.getDouble("clusTime", i));
                    resp.setPosition(bank.getDouble("clusX", i), bank.getDouble("clusY", i), FTCALConstantsLoader.CRYS_ZPOS + cluster.getDoubleValue("depth_z", 1, 1, 0));
                    responses.add(resp);
                }
            }
            if (event.hasBank("FTHODORec::clusters") == true) {
                EvioDataBank bank = (EvioDataBank) event.getBank("FTHODORec::clusters");
                int nrows = bank.rows();
                for (int i = 0; i < nrows; i++) {
                    FTResponse resp = new FTResponse("FTHODO");
                    resp.setSize(bank.getInt("clusterSize", i));
                    resp.setId(bank.getInt("clusterID", i));
                    resp.setEnergy(bank.getDouble("clusterEnergy", i));
                    resp.setTime(bank.getDouble("clusterTime", i));
                    resp.setPosition(bank.getDouble("clusterX", i), bank.getDouble("clusterY", i), bank.getDouble("clusterZ", i));
                    responses.add(resp);
                }
            }
        } else {
            if (event.hasBank("FTCAL::clusters") == true) {
                DataBank bank = event.getBank("FTCAL::clusters");
                int nrows = bank.rows();
                for (int i = 0; i < nrows; i++) {
                    FTResponse resp = new FTResponse("FTCAL");
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
                    FTResponse resp = new FTResponse("FTHODO");
                    resp.setAssociation(-1);
                    resp.setSize(bank.getInt("size", i));
                    resp.setId(bank.getInt("id", i));
                    resp.setEnergy(bank.getFloat("energy", i));
                    resp.setTime(bank.getFloat("time", i));
                    resp.setPosition(bank.getFloat("x", i), bank.getFloat("y", i), bank.getFloat("z", i));
                    responses.add(resp);
                }
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
    
    public List<FTParticle> initFTparticles(List<FTResponse> responses) {
        List<FTParticle> particles = new ArrayList<FTParticle>();
//        this.FTparticles.clear();
        for (int i = 0; i < responses.size(); i++) {
            if (responses.get(i).getType() == "FTCAL") {
                FTParticle track = new FTParticle(i);
                // start assuming the cluster to be associated to a photon
                track.setCharge(0);
                track.setField(this.solenoidField);
                track.setEnergy(responses.get(i).getEnergy());
                track.setPosition(responses.get(i).getPosition());
                track.setDirection();
                track.setTime(responses.get(i).getTime() - responses.get(i).getPosition().mag() / 29.97);
                track.setCalorimeterIndex(responses.get(i).getId());
                track.setHodoscopeIndex(-1);
                track.setTrackerIndex(-1);
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
            int iHodo = track.getDetectorHit(responses, "FTHODO", FTConstants.CAL_HODO_DISTANCE_MATCHING, FTConstants.CAL_HODO_TIME_MATCHING);
            if (iHodo > 0) {
                if (debugMode >= 1) {
                    System.out.println("found signal " + iHodo);
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
        if (particles.size() != 0) {
            if (event instanceof EvioDataEvent) {
                EvioDataBank banktrack = (EvioDataBank) event.getDictionary().createBank("FTRec::tracks", particles.size());
                if (debugMode >= 1) {
                    System.out.println("Creating output track bank with " + particles.size() + " FTparticles");
                }
                for (int i = 0; i < particles.size(); i++) {
                    banktrack.setInt("ID", i, particles.get(i).get_ID());
                    banktrack.setInt("Charge", i, particles.get(i).getCharge());
                    banktrack.setDouble("Energy", i, particles.get(i).getEnergy());
                    banktrack.setDouble("Cx", i, particles.get(i).getDirection().x());
                    banktrack.setDouble("Cy", i, particles.get(i).getDirection().y());
                    banktrack.setDouble("Cz", i, particles.get(i).getDirection().z());
                    banktrack.setDouble("Time", i, particles.get(i).getTime());
                    banktrack.setInt("CalID", i, particles.get(i).getCalorimeterIndex());
                    banktrack.setInt("HodoID", i, particles.get(i).getHodoscopeIndex());
                    banktrack.setInt("TrkID", i, particles.get(i).getTrackerIndex());
                    if (debugMode >= 1) {
                        particles.get(i).show();
                    }
                }
                if (banktrack != null) {
                    event.appendBanks(banktrack);
                }
            } else {
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
                    banktrack.setFloat("time", i, (float) particles.get(i).getTime());
                    banktrack.setShort("calID", i, (short) particles.get(i).getCalorimeterIndex());
                    banktrack.setShort("hodoID", i, (short) particles.get(i).getHodoscopeIndex());
                    banktrack.setShort("trkID", i, (short) particles.get(i).getTrackerIndex());
                    if (debugMode >= 1) {
                        particles.get(i).show();
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

}
