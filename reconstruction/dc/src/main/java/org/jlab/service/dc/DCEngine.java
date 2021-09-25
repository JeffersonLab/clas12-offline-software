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
        geoVariation = this.getEngineConfigString("dcGeometryVariation");
        if (geoVariation!=null) {
            System.out.println("["+this.getName()+"] run with geometry variation based on yaml = "+geoVariation);
        }
        else {
            geoVariation = System.getenv("COAT_DC_GEOMETRYVARIATION");
            if (geoVariation!=null) {
                System.out.println("["+this.getName()+"] run with geometry variation chosen based on env = "+geoVariation);
            }
        } 
        if (geoVariation==null) {
            geoVariation = "default";
            System.out.println("["+this.getName()+"] run with default geometry");
        }
        
    //AI settings for selecting specific sector
        String sectorSelect = this.getEngineConfigString("sectorSelect");
        if (sectorSelect!=null) {
            System.out.println("["+this.getName()+"] run with sector config chosen based on yaml = "+sectorSelect);
            selectedSector=Integer.parseInt(sectorSelect); 
        }
        else {
            sectorSelect = System.getenv("COAT_DC_SECTORSELECT");
            if (sectorSelect!=null) {
                System.out.println("["+this.getName()+"] run with sector config chosen based on env = "+sectorSelect);
                selectedSector=Integer.parseInt(sectorSelect);
            }
        }
        if (sectorSelect==null) {
             System.out.println("["+this.getName()+"] run with sector config chosen based on default = "+sectorSelect);
        }
        
        // Load config
        String useSTTConf = this.getEngineConfigString("dcUseStartTime");
        if (useSTTConf!=null) {
            System.out.println("["+this.getName()+"] run with start time in tracking config chosen based on yaml = "+useSTTConf);
            useStartTime = Boolean.valueOf(useSTTConf);
        }
        else {
            useSTTConf = System.getenv("COAT_DC_USESTARTTIME");
            if (useSTTConf!=null) {
                System.out.println("["+this.getName()+"] run with start time in tracking config chosen based on env = "+useSTTConf);
                useStartTime = Boolean.valueOf(useSTTConf);
            }
        }
        if (useSTTConf==null) {
             System.out.println("["+this.getName()+"] run with start time in tracking config chosen based on default = "+Constants.isUSETSTART());
        }
        
        // Wire distortions
        String wireDistortionsFlag = this.getEngineConfigString("dcWireDistortion");        
        if (wireDistortionsFlag!=null) {
            System.out.println("["+this.getName()+"] run with wire distortions in tracking config chosen based on yaml = "+wireDistortionsFlag);
            if(Boolean.valueOf(wireDistortionsFlag)==true) {
                //Constants.setWIREDIST(1.0);
                wireDistortion = DCGeant4Factory.ENDPLATESBOWON;
            } else {
                //Constants.setWIREDIST(0);
                wireDistortion = DCGeant4Factory.ENDPLATESBOWOFF;
            }
        }
        else {
            wireDistortionsFlag = System.getenv("COAT_DC_WIREDISTORTION"); 
            if (wireDistortionsFlag!=null) {
                System.out.println("["+this.getName()+"] run with wire distortions in tracking config chosen based on env = "+wireDistortionsFlag);
                if(Boolean.valueOf(wireDistortionsFlag)==true) {
                    //Constants.setWIREDIST(1.0);
                    wireDistortion = DCGeant4Factory.ENDPLATESBOWON;
                } else {
                    //Constants.setWIREDIST(0);
                    wireDistortion = DCGeant4Factory.ENDPLATESBOWOFF;
                }
            }
        }
        if (wireDistortionsFlag==null) {
             System.out.println("["+this.getName()+"] run with default setting for wire distortions in tracking (MC-off/Data-on)");
        }
        //Use time in tBeta function (true: use time; false: use track doca)
        String useTIMETBETA = this.getEngineConfigString("dcTimeTBeta");
        
        if (useTIMETBETA!=null) {
            System.out.println("["+this.getName()+"] run with new tBeta chosen based on yaml = "+useTIMETBETA);
            useTimeBeta = (Boolean.valueOf(useTIMETBETA));
        }
        else {
            useTIMETBETA = System.getenv("COAT_DC_USETIMETBETA");
            if (useTIMETBETA!=null) {
                System.out.println("["+this.getName()+"] run with with new tBeta config chosen based on env = "+useTIMETBETA);
                useTimeBeta = (Boolean.valueOf(useTIMETBETA));
            }
        }
        if (useTIMETBETA==null) {
             System.out.println("["+this.getName()+"] run with with new tBeta config chosen based on default = "+Constants.useUSETIMETBETA());
        }
        //CHECKBETA
        //Use beta cut(true: use time; false: use track doca)
        String useBETACUT = this.getEngineConfigString("dcBetaCut");
        
        if (useBETACUT!=null) {
            System.out.println("["+this.getName()+"] run with Beta cut chosen based on yaml = "+useBETACUT);
            useBetaCut =Boolean.valueOf(useBETACUT);
        }
        else {
            useBETACUT = System.getenv("COAT_DC_USEBETACUT");
            if (useBETACUT!=null) {
                System.out.println("["+this.getName()+"] run with with with Beta cut config chosen based on env = "+useBETACUT);
                useBetaCut = Boolean.valueOf(useBETACUT);
            }
        }
        if (useBETACUT==null) {
             System.out.println("["+this.getName()+"] run with with Beta cut config chosen based on default = "+useBetaCut);
        }
        
        //T2D Function
        String T2Dfcn = this.getEngineConfigString("dcT2DFunc");
        
        if (T2Dfcn!=null) {
            System.out.println("["+this.getName()+"] run with time to distance function in tracking config chosen based on yaml = "+T2Dfcn);
            if(T2Dfcn.equalsIgnoreCase("Polynomial")) {
                t2d=1;
            } else {
                t2d=0;
            }
        }
        else {
            T2Dfcn = System.getenv("COAT_DC_T2DFUNC");
            if (T2Dfcn!=null) {
                System.out.println("["+this.getName()+"] run with time to distance function in config chosen based on env = "+T2Dfcn);
                if(T2Dfcn.equalsIgnoreCase("Polynomial")) {
                t2d=1;
            } else {
                t2d=0;
            }
            }
        }
        if (T2Dfcn==null) {
             System.out.println("["+this.getName()+"] run with time to distance exponential function in tracking ");
        }
        //NSUPERLAYERTRACKING
        String useFOOST = this.getEngineConfigString("dcFOOST");        
        if (useFOOST!=null) {
            if(Boolean.valueOf(useFOOST)==true) {
                nSuperLayer =5;
            } else {
                nSuperLayer =6;
            }
            System.out.println("["+this.getName()+"] run with Five-out-of-six-superlayer-trkg chosen based on yaml = "+nSuperLayer);
                
        }
        else {
            useFOOST = System.getenv("COAT_DC_USEFOOST");
            if (useFOOST!=null) {
                if(Boolean.valueOf(useFOOST)==true) {
                    nSuperLayer =5;
                } else {
                    nSuperLayer =6;
                }
                System.out.println("["+this.getName()+"] run with with with Five-out-of-six-superlayer-trkg config chosen based on env = "+nSuperLayer);               
            }
        }
        if (useFOOST==null) {
             System.out.println("["+this.getName()+"] run with with Five-out-of-six-superlayer-trkg config chosen based on default = "+nSuperLayer);
             nSuperLayer =5;
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
