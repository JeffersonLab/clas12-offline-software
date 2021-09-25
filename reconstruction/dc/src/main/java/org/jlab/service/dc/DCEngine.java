package org.jlab.service.dc;

import java.util.HashMap;
import java.util.Map;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
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
        
        // Get the constants for the correct variation
        geoVariation = this.chooseEnvOrYaml("COAT_DC_GEOMETRYVARIATION", "dcGeometryVariation");
        if (geoVariation==null) {
            geoVariation = "default";
            System.out.println("["+this.getName()+"] run with default geometry");
        }
        
        //AI settings for selecting specific sector
        String sectorSelect = this.chooseEnvOrYaml("COAT_DC_SECTORSELECT","sectorSelect");
        if (sectorSelect!=null) {
             selectedSector=Integer.parseInt(sectorSelect);
        }
        else {
            System.out.println("["+this.getName()+"] run with sector config chosen based on default = "+selectedSector);
        }
            
        // Load config
        String useSTTConf = this.chooseEnvOrYaml("COAT_DC_USESTARTTIME","dcUseStartTime");
        if (useSTTConf!=null) {
            useStartTime = Boolean.valueOf(useSTTConf);
        }
        else {
            System.out.println("["+this.getName()+"] run with start time in tracking config chosen based on default = "+useStartTime);
        }
        
        // Wire distortions
        String wireDistortionsFlag = this.chooseEnvOrYaml("COAT_DC_WIREDISTORTION","dcWireDistortion");        
        if (wireDistortionsFlag!=null) {
            if(Boolean.valueOf(wireDistortionsFlag)==true) {
                //Constants.setWIREDIST(1.0);
                wireDistortion = DCGeant4Factory.ENDPLATESBOWON;
            } else {
                //Constants.setWIREDIST(0);
                wireDistortion = DCGeant4Factory.ENDPLATESBOWOFF;
            }
        }
        else {
            System.out.println("["+this.getName()+"] run with default setting for wire distortions in tracking (MC-off/Data-on)");
        }
        
        //Use time in tBeta function (true: use time; false: use track doca)
        String useTIMETBETA = this.chooseEnvOrYaml("COAT_DC_USETIMETBETA","dcTimeTBeta");
        if (useTIMETBETA!=null) {
            useTimeBeta = (Boolean.valueOf(useTIMETBETA));
        }
        else {
            System.out.println("["+this.getName()+"] run with with new tBeta config chosen based on default = "+useTimeBeta);
        }
        //CHECKBETA
        //Use beta cut(true: use time; false: use track doca)
        String useBETACUT = this.chooseEnvOrYaml("COAT_DC_USEBETACUT","dcBetaCut");
        if (useBETACUT!=null) {
            useBetaCut =Boolean.valueOf(useBETACUT);
        }
        else {
            System.out.println("["+this.getName()+"] run with with Beta cut config chosen based on default = "+useBetaCut);
        }
        
        //T2D Function
        String T2Dfcn = this.chooseEnvOrYaml("COAT_DC_T2DFUNC","dcT2DFunc");       
        if (T2Dfcn!=null) {
            if(T2Dfcn.equalsIgnoreCase("Polynomial")) {
                t2d=1;
            } else {
                t2d=0;
            }
        }
        else {
            System.out.println("["+this.getName()+"] run with time to distance exponential function in tracking ");
        }
        
        //NSUPERLAYERTRACKING
        String useFOOST = this.chooseEnvOrYaml("COAT_DC_USEFOOST","dcFOOST");        
        if (useFOOST!=null) {
            if(Boolean.valueOf(useFOOST)==true) {
                nSuperLayer =5;
            } else {
                nSuperLayer =6;
            }    
        }
        else {
            System.out.println("["+this.getName()+"] run with with Five-out-of-six-superlayer-trkg config chosen based on default = "+nSuperLayer);
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
