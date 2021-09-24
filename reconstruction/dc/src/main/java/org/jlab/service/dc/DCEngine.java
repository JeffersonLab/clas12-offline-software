package org.jlab.service.dc;

import java.util.Arrays;
import java.util.Optional;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.detector.geant4.v2.FTOFGeant4Factory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.banks.Banks;
import org.jlab.rec.dc.timetodistance.TableLoader;
import org.jlab.rec.dc.trajectory.TrajectorySurfaces;
import org.jlab.utils.CLASResources;
import org.jlab.utils.groups.IndexedTable;

public class DCEngine extends ReconstructionEngine {

    private Banks  bankNames = new Banks();
    
    String clasDictionaryPath ;
    String variationName;
    
    public DCEngine(String name) {
        super(name,"ziegler","5.0");
    }

    public void setOptions() {
        //AI settings for selecting specific sector
        String sectorSelect = this.getEngineConfigString("sectorSelect");
        
        if (sectorSelect!=null) {
            System.out.println("["+this.getName()+"] run with sector config chosen based on yaml = "+sectorSelect);
            DCHBClustering.sectorSelect=Integer.parseInt(sectorSelect); 
        }
        else {
            sectorSelect = System.getenv("COAT_DC_SECTORSELECT");
            if (sectorSelect!=null) {
                System.out.println("["+this.getName()+"] run with sector config chosen based on env = "+sectorSelect);
                DCHBClustering.sectorSelect=Integer.parseInt(sectorSelect);
            }
        }
        if (sectorSelect==null) {
             System.out.println("["+this.getName()+"] run with sector config chosen based on default = "+sectorSelect);
        }
        // Load config
        String useSTTConf = this.getEngineConfigString("dcUseStartTime");
        
        if (useSTTConf!=null) {
            System.out.println("["+this.getName()+"] run with start time in tracking config chosen based on yaml = "+useSTTConf);
            Constants.setUSETSTART(Boolean.valueOf(useSTTConf));
        }
        else {
            useSTTConf = System.getenv("COAT_DC_USESTARTTIME");
            if (useSTTConf!=null) {
                System.out.println("["+this.getName()+"] run with start time in tracking config chosen based on env = "+useSTTConf);
                Constants.setUSETSTART(Boolean.valueOf(useSTTConf));
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
                Constants.ENDPLATESBOWING = DCGeant4Factory.ENDPLATESBOWON;
            } else {
                //Constants.setWIREDIST(0);
                Constants.ENDPLATESBOWING = DCGeant4Factory.ENDPLATESBOWOFF;
            }
        }
        else {
            wireDistortionsFlag = System.getenv("COAT_DC_WIREDISTORTION"); 
            if (wireDistortionsFlag!=null) {
                System.out.println("["+this.getName()+"] run with wire distortions in tracking config chosen based on env = "+wireDistortionsFlag);
                if(Boolean.valueOf(wireDistortionsFlag)==true) {
                    //Constants.setWIREDIST(1.0);
                    Constants.ENDPLATESBOWING = DCGeant4Factory.ENDPLATESBOWON;
                } else {
                    //Constants.setWIREDIST(0);
                    Constants.ENDPLATESBOWING = DCGeant4Factory.ENDPLATESBOWOFF;
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
            Constants.setUSETIMETBETA(Boolean.valueOf(useTIMETBETA));
        }
        else {
            useTIMETBETA = System.getenv("COAT_DC_USETIMETBETA");
            if (useTIMETBETA!=null) {
                System.out.println("["+this.getName()+"] run with with new tBeta config chosen based on env = "+useTIMETBETA);
                Constants.setUSETIMETBETA(Boolean.valueOf(useTIMETBETA));
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
            Constants.CHECKBETA=Boolean.valueOf(useBETACUT);
        }
        else {
            useBETACUT = System.getenv("COAT_DC_USEBETACUT");
            if (useBETACUT!=null) {
                System.out.println("["+this.getName()+"] run with with with Beta cut config chosen based on env = "+useBETACUT);
                Constants.CHECKBETA=Boolean.valueOf(useBETACUT);
            }
        }
        if (useBETACUT==null) {
             System.out.println("["+this.getName()+"] run with with Beta cut config chosen based on default = "+Constants.CHECKBETA);
        }
        
        //T2D Function
        String T2Dfcn = this.getEngineConfigString("dcT2DFunc");
        
        if (T2Dfcn!=null) {
            System.out.println("["+this.getName()+"] run with time to distance function in tracking config chosen based on yaml = "+T2Dfcn);
            if(T2Dfcn.equalsIgnoreCase("Polynomial")) {
                Constants.setT2D(1);
            } else {
                Constants.setT2D(0);
            }
        }
        else {
            T2Dfcn = System.getenv("COAT_DC_T2DFUNC");
            if (T2Dfcn!=null) {
                System.out.println("["+this.getName()+"] run with time to distance function in config chosen based on env = "+T2Dfcn);
                if(T2Dfcn.equalsIgnoreCase("Polynomial")) {
                Constants.setT2D(1);
            } else {
                Constants.setT2D(0);
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
                Constants.NSUPERLAYERTRACKING =5;
            } else {
                Constants.NSUPERLAYERTRACKING =6;
            }
            System.out.println("["+this.getName()+"] run with Five-out-of-six-superlayer-trkg chosen based on yaml = "+Constants.NSUPERLAYERTRACKING);
                
        }
        else {
            useFOOST = System.getenv("COAT_DC_USEFOOST");
            if (useFOOST!=null) {
                if(Boolean.valueOf(useFOOST)==true) {
                    Constants.NSUPERLAYERTRACKING =5;
                } else {
                    Constants.NSUPERLAYERTRACKING =6;
                }
                System.out.println("["+this.getName()+"] run with with with Five-out-of-six-superlayer-trkg config chosen based on env = "+Constants.NSUPERLAYERTRACKING);               
            }
        }
        if (useFOOST==null) {
             System.out.println("["+this.getName()+"] run with with Five-out-of-six-superlayer-trkg config chosen based on default = "+Constants.NSUPERLAYERTRACKING);
             Constants.NSUPERLAYERTRACKING =5;
        }
    }
    public void LoadTables() {
        
        // Load tables
        clasDictionaryPath= CLASResources.getResourcePath("etc");
        String[]  dcTables = new String[]{
            "/calibration/dc/signal_generation/doca_resolution",
            "/calibration/dc/time_to_distance/time2dist",
            "/calibration/dc/time_corrections/T0Corrections",
            "/calibration/dc/time_corrections/tdctimingcuts",
            "/calibration/dc/time_jitter",
            "/calibration/dc/tracking/wire_status",
            "/geometry/beam/position"
        };

        requireConstants(Arrays.asList(dcTables));
        // Get the constants for the correct variation
        String ccDBVar = this.getEngineConfigString("variation");
        if (ccDBVar!=null) {
            System.out.println("["+this.getName()+"] run with constants variation based on yaml = "+ccDBVar);
        }
        else {
            ccDBVar = System.getenv("COAT_DC_VARIATION");
            if (ccDBVar!=null) {
                System.out.println("["+this.getName()+"] run with constants variation chosen based on env = "+ccDBVar);
            }
        } 
        if (ccDBVar==null) {
            System.out.println("["+this.getName()+"] run with default constants");
        }
        // Load the calibration constants
        String dcvariationName = Optional.ofNullable(ccDBVar).orElse("default");
        variationName = dcvariationName;
        this.getConstantsManager().setVariation(dcvariationName);
    }
    
    public void LoadGeometry() {
        // Get the constants for the correct variation
        String geomDBVar = this.getEngineConfigString("dcGeometryVariation");
        if (geomDBVar!=null) {
            System.out.println("["+this.getName()+"] run with geometry variation based on yaml = "+geomDBVar);
        }
        else {
            geomDBVar = System.getenv("COAT_DC_GEOMETRYVARIATION");
            if (geomDBVar!=null) {
                System.out.println("["+this.getName()+"] run with geometry variation chosen based on env = "+geomDBVar);
            }
        } 
        if (geomDBVar==null) {
            System.out.println("["+this.getName()+"] run with default geometry");
        }
        
        // Load the geometry
        String geoVariation = Optional.ofNullable(geomDBVar).orElse("default");
        ConstantProvider provider = GeometryFactory.getConstants(DetectorType.DC, 11, geoVariation);
        Constants.dcDetector = new DCGeant4Factory(provider, DCGeant4Factory.MINISTAGGERON, Constants.ENDPLATESBOWING);
        for(int l=0; l<6; l++) {
            Constants.wpdist[l] = provider.getDouble("/geometry/dc/superlayer/wpdist", l);
            System.out.println("****************** WPDIST READ *********FROM "+geoVariation+"**** VARIATION ****** "+provider.getDouble("/geometry/dc/superlayer/wpdist", l));
        }
        // Load target
        ConstantProvider providerTG = GeometryFactory.getConstants(DetectorType.TARGET, 11, geoVariation);
        double targetPosition = providerTG.getDouble("/geometry/target/position",0);
        double targetLength   = providerTG.getDouble("/geometry/target/length",0);
        // Load other geometries
        ConstantProvider providerFTOF = GeometryFactory.getConstants(DetectorType.FTOF, 11, geoVariation);
        Constants.ftofDetector = new FTOFGeant4Factory(providerFTOF);        
        ConstantProvider providerEC = GeometryFactory.getConstants(DetectorType.ECAL, 11, geoVariation);
        Constants.ecalDetector =  GeometryFactory.getDetector(DetectorType.ECAL, 11, geoVariation);
        System.out.println(" -- Det Geometry constants are Loaded " );

        // create the surfaces
        Constants.tSurf = new TrajectorySurfaces();
        // for debugging the end plates bowing:
        //====================================
        //try {
        //    tSurf.checkDCGeometry(dcDetector);
        //} catch (FileNotFoundException ex) {
        //    Logger.getLogger(DCEngine.class.getName()).log(Level.SEVERE, null, ex);
        //}
        Constants.tSurf.LoadSurfaces(targetPosition, targetLength,Constants.dcDetector, Constants.ftofDetector, Constants.ecalDetector);
        
    }
    
    @Override
    public boolean processDataEvent(DataEvent event) {
        return true;
    }

    @Override
    public boolean init() {
        return true;
    }

     public void initBankNames() {
        //Initialize bank names
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

    public Banks getBankNames() {
        return bankNames;
    }

    public void setBankNames(Banks bankNames) {
        this.bankNames = bankNames;
    }
}
