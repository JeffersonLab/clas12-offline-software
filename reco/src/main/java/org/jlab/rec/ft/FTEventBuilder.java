package org.jlab.rec.ft;

import java.util.ArrayList;
import java.util.List;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.rec.ft.cal.FTCALConstantsLoader;

public class FTEventBuilder{

    public int debugMode = 0;

    private List<FTParticle> FTparticles = new ArrayList<FTParticle>();
    private List<FTResponse> FTresponses = new ArrayList<FTResponse>();

    private double solenoidField;
    
    
    public FTEventBuilder() {
    }
 
    public List<FTParticle> getFTparticles() {
        return FTparticles;
    }

    public List<FTResponse> getFTresponses() {
        return FTresponses;
    }

    public void setFTresponses(List<FTResponse> FTresponses) {
        this.FTresponses = FTresponses;
    }

    public double getField() {
        return solenoidField;
    }

    public void setField(double field) {
        this.solenoidField = field;
    }

    public void init(double field) {
        this.solenoidField = field;
        this.FTparticles.clear();
        this.FTresponses.clear();
    }
    
    public void addResponses(DataEvent event) {
        if(event instanceof EvioDataEvent) {
            if(event.hasBank("FTCALRec::clusters")==true) {
                EvioDataBank bank = (EvioDataBank) event.getBank("FTCALRec::clusters");
                int nrows = bank.rows();
                for(int i=0; i<nrows; i++) {
                    FTResponse resp = new FTResponse("FTCAL");
                    resp.setAssociation(-1);
                    resp.setSize(bank.getInt("clusSize", i));
                    resp.setId(bank.getInt("clusID", i));
                    resp.setEnergy(bank.getDouble("clusEnergy", i));
                    resp.setTime(bank.getDouble("clusTime", i));
                    resp.setPosition(bank.getDouble("clusX", i),bank.getDouble("clusY", i),FTCALConstantsLoader.CRYS_ZPOS+FTCALConstantsLoader.depth_z );
                    this.FTresponses.add(resp); 
                }
            }
            if(event.hasBank("FTHODORec::clusters")==true) {
                EvioDataBank bank = (EvioDataBank) event.getBank("FTHODORec::clusters");
                int nrows = bank.rows();
                for(int i=0; i<nrows; i++) {
                    FTResponse resp = new FTResponse("FTHODO");
                    resp.setSize(bank.getInt("clusterSize", i));
                    resp.setId(bank.getInt("clusterID", i));
                    resp.setEnergy(bank.getDouble("clusterEnergy", i));
                    resp.setTime(bank.getDouble("clusterTime", i));
                    resp.setPosition(bank.getDouble("clusterX", i),bank.getDouble("clusterY", i),FTCALConstantsLoader.CRYS_ZPOS+FTCALConstantsLoader.depth_z );                    
                    this.FTresponses.add(resp);  
                }
            }
        }
        else {
            if(event.hasBank("FTCAL::clusters")==true) {
                DataBank bank = event.getBank("FTCAL::clusters");
                int nrows = bank.rows();
                for(int i=0; i<nrows; i++) {
                    FTResponse resp = new FTResponse("FTCAL");
                    resp.setAssociation(-1);
                    resp.setSize(bank.getInt("size", i));
                    resp.setId(bank.getInt("id", i));
                    resp.setEnergy(bank.getFloat("energy", i));
                    resp.setTime(bank.getFloat("time", i));
                    resp.setPosition(bank.getFloat("x", i),bank.getFloat("y", i),bank.getFloat("z", i));
                    this.FTresponses.add(resp); 
                }
            }
            if(event.hasBank("FTHODO::clusters")==true) {
                DataBank bank = event.getBank("FTHODO::clusters");
                int nrows = bank.rows();
                for(int i=0; i<nrows; i++) {
                    FTResponse resp = new FTResponse("FTHODO");
                    resp.setAssociation(-1);
                    resp.setSize(bank.getInt("size", i));
                    resp.setId(bank.getInt("id", i));
                    resp.setEnergy(bank.getFloat("energy", i));
                    resp.setTime(bank.getFloat("time", i));
                    resp.setPosition(bank.getFloat("x", i),bank.getFloat("y", i),bank.getFloat("z", i));
                    this.FTresponses.add(resp); 
                }
            }
        }
        if(debugMode>=1) this.showResponses();
    }
    
    public void initFTparticles() {
        this.FTparticles.clear();
        for(int i=0; i<this.FTresponses.size(); i++) {
            if(this.FTresponses.get(i).getType() == "FTCAL") {
                FTParticle track = new FTParticle(i);        
                // start assuming the cluster to be associated to a photon
                track.setCharge(0);
                track.setField(this.solenoidField);
                track.setEnergy(this.FTresponses.get(i).getEnergy());
                track.setPosition(this.FTresponses.get(i).getPosition());
                track.setTime(this.FTresponses.get(i).getTime()-this.FTresponses.get(i).getPosition().mag()/297.);
                track.setCalorimeterIndex(i);
                track.setHodoscopeIndex(-1);
                track.setTrackerIndex(-1);    
                this.FTparticles.add(track);
                this.FTresponses.get(i).setAssociation(i);
            }
        }
        if(debugMode >= 1) for(int i=0; i<this.FTparticles.size(); i++) this.FTparticles.get(i).show();
    }
    
    public void matchToHODO() {
        for(int i=0; i<this.FTparticles.size(); i++) {
            FTParticle track = this.FTparticles.get(i);
            if(debugMode>=1) System.out.println("Searching for matching signal in the hodoscope:");
            int iHodo = track.getDetectorHit(this.FTresponses,"FTHODO",FTConstants.CAL_HODO_DISTANCE_MATCHING,FTConstants.CAL_HODO_TIME_MATCHING);
            if(iHodo>0) {
                if(debugMode>=1) System.out.println("found signal " + iHodo);
                track.setCharge(-1);
                track.setHodoscopeIndex(this.FTresponses.get(iHodo).getId());
                this.FTresponses.get(iHodo).setAssociation(i);
            }
            if(debugMode>= 1) track.show();
        }
    }
    
    public void showResponses() {
        System.out.println("\nFound " + this.FTresponses.size() + " clusters in FT detector");
        for(int i=0; i<this.FTresponses.size(); i++) this.FTresponses.get(i).show();
    }
    
    public void writeBanks(DataEvent event) {
		
	if(FTparticles.size()!=0){
            if(event instanceof EvioDataEvent) {
                EvioDataBank banktrack = (EvioDataBank) event.getDictionary().createBank("FTRec::tracks",FTparticles.size());
                if(debugMode>=1) System.out.println("Creating output track bank with " + FTparticles.size() + " FTparticles");
		for(int i =0; i< FTparticles.size(); i++) {
                    banktrack.setInt("ID", i,FTparticles.get(i).get_ID());
                    banktrack.setInt("Charge", i,FTparticles.get(i).getCharge());
                    banktrack.setDouble("Energy", i,FTparticles.get(i).getEnergy());
                    banktrack.setDouble("Cx", i,FTparticles.get(i).getDirection().x());
                    banktrack.setDouble("Cy", i,FTparticles.get(i).getDirection().y());
                    banktrack.setDouble("Cz", i,FTparticles.get(i).getDirection().z());
                    banktrack.setDouble("Time", i,FTparticles.get(i).getTime());
                    banktrack.setInt("CalID",i,FTparticles.get(i).getCalorimeterIndex());
                    banktrack.setInt("HodoID",i,FTparticles.get(i).getHodoscopeIndex());
                    banktrack.setInt("TrkID", i,FTparticles.get(i).getTrackerIndex());
                    if(debugMode>=1) {
                            FTparticles.get(i).show();
                    }
                }
        	if(banktrack!=null) event.appendBanks(banktrack);
            }
            else {
                DataBank banktrack = event.createBank("FT::particles",FTparticles.size());
                if(debugMode>=1) System.out.println("Creating output track bank with " + FTparticles.size() + " FTparticles");
		for(int i =0; i< FTparticles.size(); i++) {
                    banktrack.setShort("id",     i, (short) FTparticles.get(i).get_ID());
                    banktrack.setByte("charge",  i, (byte)  FTparticles.get(i).getCharge());
                    banktrack.setFloat("energy", i, (float) FTparticles.get(i).getEnergy());
                    banktrack.setFloat("cx",     i, (float) FTparticles.get(i).getDirection().x());
                    banktrack.setFloat("cy",     i, (float) FTparticles.get(i).getDirection().y());
                    banktrack.setFloat("cx",     i, (float) FTparticles.get(i).getDirection().z());
                    banktrack.setFloat("time",   i, (float) FTparticles.get(i).getTime());
                    banktrack.setShort("calID",  i, (short) FTparticles.get(i).getCalorimeterIndex());
                    banktrack.setShort("hodoID", i, (short) FTparticles.get(i).getHodoscopeIndex());
                    banktrack.setShort("trkID",  i, (short) FTparticles.get(i).getTrackerIndex());
                    if(debugMode>=1) {
                            FTparticles.get(i).show();
                   }
                }
        	if(banktrack!=null) event.appendBanks(banktrack);                
            }
	}
    }

}
