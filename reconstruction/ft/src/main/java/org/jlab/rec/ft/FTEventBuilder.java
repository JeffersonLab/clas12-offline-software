package org.jlab.rec.ft;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.pdg.PhysicsConstants;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.rec.ft.cal.FTCALConstantsLoader;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.geom.prim.Point3D;

public class FTEventBuilder {

    public int debugMode = -1; 

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

    public void setDebugMode(int debug){
        this.debugMode = debug;
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

            if (event.hasBank("FTTRK::crosses") == true) {
                DataBank bank = event.getBank("FTTRK::crosses");
                int nrows = bank.rows();
                for (int i = 0; i < nrows; i++) {
                    FTResponse resp = new FTResponse("FTTRK");
                    resp.setAssociation(-1);
                    resp.setSize(bank.getInt("size", i));
//                    resp.setSize(nrows);
                    resp.setId(bank.getInt("id", i));
                    resp.setEnergy(bank.getFloat("energy", i));
                    resp.setTime(bank.getFloat("time", i));
                    resp.setPosition(bank.getFloat("x", i), bank.getFloat("y", i), bank.getFloat("z", i));
                    if(debugMode>=1) System.out.println(" --------- id, cross x, y, z " + bank.getInt("id", i) + " " + bank.getFloat("x", i) + " " + bank.getFloat("y", i) + " " + bank.getFloat("z", i));
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
            if (event.hasBank("FTTRK::crosses") == true) {
                DataBank bank = event.getBank("FTTRK::crosses");
                //DataBank bankClust = event.getBank("FTTRK::clusters");
                //DataBank bankHits = event.getBank("FTTRK::hits");
                int nrows = bank.rows();
                for (int i = 0; i < nrows; i++) {
                    FTResponse resp = new FTResponse("FTTRK");
                    resp.setAssociation(-1);                    
                    resp.setSize(bank.getInt("size", i));
                    resp.setId(bank.getInt("id", i));
//                    resp.setEnergy(-9999.);
//                    resp.setTime(9999.);

/*
                    // time and energy correspond to the energies of the clusters associated to the cross
                    int det = bank.getInt("detector", i);
                    int idCl1 = bank.getInt("Cluster1ID", i);
                    int idCl2 = bank.getInt("Cluster2ID", i);
                    double meanEnergy = -9999;
                    double meanTime = -9999;
                    double enCl1 = 0., enCl2 = 0., timeCross1 = 0., timeCross2 = 0.;
                    int layer = -1;
                    if(det==0){
                        // find clusters on layers 1 and 2
                        for(int j=0; j<bankClust.rows(); j++){
                            layer = bankClust.getInt("layer", j);
                            if(layer==1) enCl1 = bankClust.getFloat("energy", idCl1);
                            if(layer==2) enCl2 = bankClust.getFloat("energy", idCl2);
                        }
                        meanEnergy = Math.sqrt(enCl1*enCl2);
                        // find hits on layer 1 and 2 related to the cluster
                        int nHits1=0, nHits2=0;
                        for(int k=0; k<bankHits.rows(); k++){
                            int clId  = bankHits.getInt("clusterID", k);
                            int strip = bankHits.getInt("component", k);
                            double t = bankHits.getFloat("time", k);
                            layer = bankHits.getInt("layer",k);
                            if(layer==1){
                                if(clId==idCl1){
                                    timeCross1 += bankHits.getFloat("time", k);
                                    nHits1++; 
                                }
                            }else if(layer==2){
                                if(clId==idCl2){
                                    timeCross2 += bankHits.getFloat("time", k);
                                    nHits2++;
                                }
                            }
                        }
                        if(nHits1 != 0){timeCross1 /= nHits1;}else{timeCross1 = 9999.;};
                        if(nHits2 != 0){timeCross2 /= nHits2;}else{timeCross2 = 9999.;};
                        meanTime = (timeCross1+timeCross2)/2.;                               
                    }else if(det==1){
                        for(int j=0; j<bankClust.rows(); j++){  // loop on clusters in a given layer
                            layer = bankClust.getInt("layer", j);
                            if(layer==3) enCl1 = bankClust.getFloat("energy", idCl1);
                            if(layer==4) enCl2 = bankClust.getFloat("energy", idCl2);
                        }
                        meanEnergy = Math.sqrt(enCl1*enCl2);
                        // find hits on layer 3 and 4 related to the cluster
                        int nHits1=0, nHits2=0;
                        for(int k=0; k<bankHits.rows(); k++){    // loop on hits of a given layer and cluster
                            layer = bankHits.getInt("layer",k);
                            int cl = bankHits.getInt("clusterID", k);
                            if(layer==3){
                                if(bankHits.getInt("clusterID",k)==idCl1){
                                    timeCross1 += bankHits.getFloat("time", k);
                                    nHits1++;
                                }
                            }else if(layer==4){
                                if(bankHits.getInt("clusterID",k)==idCl2){
                                    timeCross2 += bankHits.getFloat("time", k);
                                    nHits2++;
                                }
                            }
                        }
                        if(nHits1 != 0){timeCross1 /= nHits1;}else{timeCross1 = 9999.;};
                        if(nHits2 != 0){timeCross2 /= nHits2;}else{timeCross2 = 9999.;};
                        meanTime = (timeCross1+timeCross2)/2.;
                    } 
*/

                    resp.setEnergy(bank.getFloat("energy", i));
                    resp.setTime(bank.getFloat("time", i));
                    resp.setPosition(bank.getFloat("x", i), bank.getFloat("y", i), bank.getFloat("z", i));
                    if(debugMode>=1) System.out.println(" --------- id, cross x, y, z " + bank.getInt("id", i) + " " + bank.getFloat("x", i) + " " + bank.getFloat("y", i) + " " + bank.getFloat("z", i));
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
            if(particle.getCalorimeterIndex()>-1) particle.setDirection(thetaCorr, phiCorr);
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
                track.setTime(responses.get(i).getTime() - responses.get(i).getPosition().mag() / PhysicsConstants.speedOfLight());
                track.setCalorimeterIndex(responses.get(i).getId());
                track.setHodoscopeIndex(-1);
                track.setTrackerIndex(-1);
                particles.add(track);
                responses.get(i).setAssociation(particles.size()-1);
            }
            
            if (responses.get(i).getType() == "FTTRK") {
                FTParticle track = new FTParticle(i);
                track.setCharge(-999);  // provisional, one should take care of the nofield case
                track.setField(this.solenoidField);
                track.setEnergy(responses.get(i).getEnergy());
                track.setPosition(responses.get(i).getPosition());
                track.setDirection();
                double trkTime = responses.get(i).getTime() - responses.get(i).getPosition().mag()/PhysicsConstants.speedOfLight();
                if(trkTime>=0){track.setTime(trkTime);}else{track.setTime(0.);};  
                track.setCalorimeterIndex(-1);
                track.setHodoscopeIndex(-1);
                track.setTrackerIndex(responses.get(i).getId());
                particles.add(track);
                responses.get(i).setAssociation(particles.size()-responses.size());
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

   public void matchToTRK(List<FTResponse> responses, List<FTParticle> particles) {
        for (int i = 0; i < particles.size(); i++) {
            FTParticle track = particles.get(i);
            if (debugMode >= 1) {
                System.out.println("Searching for matching signal in the tracker:");
            }
            int iTrk = track.getDetectorHit(responses, "FTTRK", FTConstants.CAL_TRK_DISTANCE_MATCHING, FTConstants.CAL_TRK_TIME_MATCHING);
            if (iTrk > 0) {
                if (debugMode >= 1) {
                    System.out.println("found signal in FTTRK" + iTrk);
                }
                track.setCharge(-999); // provisional, for no field tracking
                track.setTrackerIndex(responses.get(iTrk).getId());
                responses.get(iTrk).setAssociation(i);
            }
            if (debugMode >= 1) track.show();
        }
    }
    
    public void matchTRKHits(List<FTResponse> responses, List<FTParticle> particles) {
        for (int i = 0; i < particles.size(); i++) {
            FTParticle track = particles.get(i);
            if (debugMode >= 1) {
                System.out.println("Searching for matching signal in the tracker:");
            }
            // consider FTParticles only through FTTRK detector
            if(track.getCalorimeterIndex()<0){
                int iTrk = track.getDetectorHit(responses, "FTTRK", FTConstants.CAL_TRK_DISTANCE_MATCHING, FTConstants.CAL_TRK_TIME_MATCHING);
                if (iTrk > 0) {
                    if (debugMode >= 1) {
                        System.out.println("found signal in FTTRK" + iTrk);
                    }
                    track.setCharge(-999); // provisional, for no field tracking
                    track.setTrackerIndex(responses.get(iTrk).getId());
                    responses.get(iTrk).setAssociation(i);
                }
                if (debugMode >= 1) track.show();
            }   
        }
    }
   
    /*
    // alternative method, keep it provisionally 
    public void matchTRKCrosses(List<FTResponse> responses){
       // check whether the responses on opposite FTTRK detectors can belong to the same track
        Point3D origin = new Point3D(0.,0.,0.);    
        if(this.solenoidField == 0){   
              for(int i=0; i<responses.size(); i++){
           // is the response belonging to FTTRK?
                FTResponse resp1 = responses.get(i);
                if(resp1.getType() == "FTTRK" && resp1.getSector()==0){ // hit on the first cross
                   // if no field draw the line between the origin and the hit on the first FTTRK detector
                   Line3D line1 = new Line3D(origin, resp1.getPosition().toPoint3D());
                   // look for another different hit on a different detector
                   for(int j=responses.size(); j>i; j--){
                       FTResponse resp2 = responses.get(j);
                       if(resp2.getType() == "FTTRK" && resp2.getSector()==1){
                           Line3D line2 = new Line3D(origin, resp2.getPosition().toPoint3D());                
                           // check the distance of the lines at the z coordinate of the second hit
                           double x2 = resp2.getPosition().toPoint3D().x();
                           double y2 = resp2.getPosition().toPoint3D().y();
                           double z2 = resp2.getPosition().toPoint3D().z();
                           double t = line1.direction().z()/z2;
                           double x1 = line1.direction().x() * t;
                           double y1 = line1.direction().y() * t;
                           Line3D diff = new Line3D(x1, y1, z2, x2, y2, z2);
                           double distance = diff.length();
                           // if distance is small than a given tolerance, validate both the responses
                           double FTTRK_DISTANCE_TOLERANCE = 0.1; // cm
                           if(distance < FTTRK_DISTANCE_TOLERANCE){
                               responses.get(i).setAssociation(2);
                               responses.get(j).setAssociation(2);
                           }
                        }
                   }
               }
               
           }
       }
    }
    */

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
