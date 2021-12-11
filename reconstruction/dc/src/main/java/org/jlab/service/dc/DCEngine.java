package org.jlab.service.dc;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.banks.Banks;
import org.jlab.utils.groups.IndexedTable;

public class DCEngine extends ReconstructionEngine {

    // bank names
    private final Banks  bankNames = new Banks();
    
    // options configured from yaml
    private int      selectedSector = 0;
    private boolean  wireDistortion = false;
    private boolean  useStartTime   = true;
    private boolean  useTimeBeta    = false;
    private boolean  useBetaCut     = false;
    private boolean  useDoublets    = false;
    private int      t2d            = 0;
    private int      nSuperLayer    = 5;
    private String   geoVariation   = "default";
    private String   bankType       = "HitBasedTrkg";
    private String   outBankPrefix  = null;
        
    public static final Logger LOGGER = Logger.getLogger(ReconstructionEngine.class.getName());


    public DCEngine(String name) {
        super(name,"ziegler","5.0");
    }

    public void setOptions() {
        
        // set the geometry variation, if dcGeometryVariation is undefined, default to variation
        if(this.getEngineConfigString("dcGeometryVariation")!=null) 
            geoVariation = this.getEngineConfigString("dcGeometryVariation");
        else if(this.getEngineConfigString("variation")!=null) 
            geoVariation = this.getEngineConfigString("variation");
        
        //AI settings for selecting specific sector
        if(this.getEngineConfigString("sectorSelect")!=null) 
            selectedSector=Integer.parseInt(this.getEngineConfigString("sectorSelect"));
            
        // Load config
        if(this.getEngineConfigString("dcUseStartTime")!=null)
            useStartTime = Boolean.valueOf(this.getEngineConfigString("dcUseStartTime"));
      
        // Wire distortions
        if(this.getEngineConfigString("dcWireDistortion")!=null)       
            wireDistortion = Boolean.parseBoolean(this.getEngineConfigString("dcWireDistortion"));
        
        //Use time in tBeta function (true: use time; false: use track doca)
        if(this.getEngineConfigString("dcTimeTBeta")!=null)
            useTimeBeta = (Boolean.valueOf(this.getEngineConfigString("dcTimeTBeta")));
        
        //Use beta cut(true: use time; false: use track doca)
        if(this.getEngineConfigString("dcBetaCut")!=null)
            useBetaCut =Boolean.valueOf(this.getEngineConfigString("dcBetaCut"));
        
        //T2D Function
        if(this.getEngineConfigString("dcT2DFunc")!=null)       
            if(this.getEngineConfigString("dcT2DFunc").equalsIgnoreCase("Polynomial")) {
                t2d=1;
        }
        
        //Recover hit doublets
        if(this.getEngineConfigString("dcDoublets")!=null)       
            useDoublets = Boolean.valueOf(this.getEngineConfigString("dcDoublets"));
        
        //NSUPERLAYERTRACKING
        if(this.getEngineConfigString("dcFOOST")!=null)
            if(!Boolean.valueOf(this.getEngineConfigString("dcFOOST"))) {
                nSuperLayer =6;
            }    
                
        //Set output bank names
        if(this.getEngineConfigString("outputBankPrefix")!=null) {
            outBankPrefix = this.getEngineConfigString("outputBankPrefix");
        }
    }


    public void LoadTables() {

        // Load tables
        Map<String,Integer> dcTables = new HashMap<>();
        dcTables.put(Constants.DOCARES,3);
        dcTables.put(Constants.TIME2DIST,3);
        dcTables.put(Constants.T0CORRECTION,4);
        dcTables.put(Constants.TDCTCUTS,3);
        dcTables.put(Constants.TIMEJITTER,3);
        dcTables.put(Constants.WIRESTAT,3);
        dcTables.put(Constants.BEAMPOS,3);
        
        requireConstants(dcTables);
        this.getConstantsManager().setVariation("default");
    }
        
    
    @Override
    public boolean processDataEvent(DataEvent event) {
        return true;
    }

    @Override
    public boolean init() {
        this.setOptions();
        Constants.getInstance().initialize(this.getName(),
                                           geoVariation, 
                                           wireDistortion, 
                                           useStartTime, 
                                           useTimeBeta, 
                                           useBetaCut, 
                                           t2d,
                                           useDoublets,
                                           nSuperLayer, 
                                           selectedSector);
        this.LoadTables();
        this.initBanks();
        this.setDropBanks();
        return true;
    }

    private void initBanks() {
        if(this.getBankPrefix()!=null) this.getBanks().init(outBankPrefix);
        LOGGER.log(Level.INFO,"["+this.getName()+"] bank names set for " + this.getBanks().toString());       
    }

    public Banks getBanks() {
        return bankNames;
    }
    
    public void setBankPrefix(String prefix) {
        this.outBankPrefix = prefix;
    }

    public String getBankPrefix() {
        return this.outBankPrefix;
    }
    
    public void setDropBanks() {
        
    }
    
    public int getRun(DataEvent event) {
        if (!event.hasBank("RUN::config")) {
            return 0;
        }
        DataBank bank = event.getBank("RUN::config");
        LOGGER.log(Level.FINE,"["+this.getName()+"] EVENT "+bank.getInt("event", 0));       
        
        int run = bank.getInt("run", 0);
        return run;
    }

    public double getTriggerPhase(DataEvent event) {
        DataBank  bank = event.getBank("RUN::config");
        int        run = bank.getInt("run", 0);
        long timeStamp = bank.getLong("timestamp", 0);
        
        double triggerPhase = 0;
        if (run>0 && timeStamp>=0) {
           IndexedTable tabJ = super.getConstantsManager().getConstants(run, Constants.TIMEJITTER);
           double period = tabJ.getDoubleValue("period", 0, 0, 0);
           int    phase  = tabJ.getIntValue("phase", 0, 0, 0);
           int    cycles = tabJ.getIntValue("cycles", 0, 0, 0);

           if (cycles > 0) triggerPhase = period * ((timeStamp + phase) % cycles);
        }
        return triggerPhase;
    }

}
