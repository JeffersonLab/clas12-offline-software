package org.jlab.service.dc;

import java.util.HashMap;
import java.util.Map;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.banks.Banks;
import org.jlab.utils.groups.IndexedTable;

public class DCEngine extends ReconstructionEngine {

    // bank names
    private Banks  bankNames = new Banks();
    
    // options configured from yaml
    private int     selectedSector = 0;
    private boolean wireDistortion = false;
    private boolean useStartTime   = true;
    private boolean useTimeBeta    = false;
    private boolean useBetaCut     = false;
    private int     t2d            = 0;
    private int     nSuperLayer    = 5;
    private String  geoVariation   = "default";
        
    public DCEngine(String name) {
        super(name,"ziegler","5.0");
    }

    public void setOptions() {
        
        // set the geometry variation, if dcGeometryVariation is undefined, default to variation
        if(this.getEngineConfigString("dcGeometryVariation")!=null) 
            geoVariation = this.getEngineConfigString("dcGeometryVariation");
        else if(this.getEngineConfigString("variation")!=null) 
            geoVariation = this.getEngineConfigString("variation");
        System.out.println("["+this.getName()+"] run with variation = " + geoVariation);
        
        //AI settings for selecting specific sector
        if(this.getEngineConfigString("sectorSelect")!=null) 
            selectedSector=Integer.parseInt(this.getEngineConfigString("sectorSelect"));
        System.out.println("["+this.getName()+"] run with sector selection = " + selectedSector);
            
        // Load config
        if(this.getEngineConfigString("dcUseStartTime")!=null)
            useStartTime = Boolean.valueOf(this.getEngineConfigString("dcUseStartTime"));
        System.out.println("["+this.getName()+"] run with start time option = " + useStartTime);
      
        // Wire distortions
        if(this.getEngineConfigString("dcWireDistortion")!=null)       
            wireDistortion = Boolean.parseBoolean(this.getEngineConfigString("dcWireDistortion"));
        System.out.println("["+this.getName()+"] run with wire distortions = " + wireDistortion);
        
        //Use time in tBeta function (true: use time; false: use track doca)
        if(this.getEngineConfigString("dcTimeTBeta")!=null)
            useTimeBeta = (Boolean.valueOf(this.getEngineConfigString("dcTimeTBeta")));
        System.out.println("["+this.getName()+"] run with with new tBeta configuration = " + useTimeBeta);
        
        //Use beta cut(true: use time; false: use track doca)
        if(this.getEngineConfigString("dcBetaCut")!=null)
            useBetaCut =Boolean.valueOf(this.getEngineConfigString("dcBetaCut"));
        System.out.println("["+this.getName()+"] run with with Beta cut = " + useBetaCut);
        
        //T2D Function
        if(this.getEngineConfigString("dcT2DFunc")!=null)       
            if(this.getEngineConfigString("dcT2DFunc").equalsIgnoreCase("Polynomial")) {
                t2d=1;
            }
        System.out.println("["+this.getName()+"] run with time to distance function set to exponential/polynomial (0/1) = " + t2d);
        
        //NSUPERLAYERTRACKING
        if(this.getEngineConfigString("dcFOOST")!=null)
            if(!Boolean.valueOf(this.getEngineConfigString("dcFOOST"))) {
                nSuperLayer =6;
            }    
        System.out.println("["+this.getName()+"] run with with Five-out-of-six-superlayer-trkg = " + nSuperLayer);
        
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
        Constants.getInstance().initialize(geoVariation, 
                                           wireDistortion, 
                                           useStartTime, 
                                           useTimeBeta, 
                                           useBetaCut, 
                                           t2d, 
                                           nSuperLayer, 
                                           selectedSector);
        this.LoadTables();
        this.initBankNames();
        return true;
    }

    public void initBankNames() {
        //Initialize bank names
    }

    public Banks getBankNames() {
        return bankNames;
    }

    public void setBankNames(Banks bankNames) {
        this.bankNames = bankNames;
    }

    public int getRun(DataEvent event) {
        if (!event.hasBank("RUN::config")) {
            return 0;
        }
        DataBank bank = event.getBank("RUN::config");
        if(Constants.DEBUG)
            System.out.println("EVENT "+bank.getInt("event", 0));
        
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
