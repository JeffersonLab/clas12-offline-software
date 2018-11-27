package org.jlab.service.dc;

import java.util.Arrays;
import java.util.Optional;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.detector.geant4.v2.ECGeant4Factory;
import org.jlab.detector.geant4.v2.FTOFGeant4Factory;
import org.jlab.detector.geant4.v2.PCALGeant4Factory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.trajectory.TrajectorySurfaces;
import org.jlab.utils.CLASResources;

public class DCEngine extends ReconstructionEngine {

    //String FieldsConfig="";
    //AtomicInteger Run = new AtomicInteger(0);
    DCGeant4Factory dcDetector;
    FTOFGeant4Factory ftofDetector;
    ECGeant4Factory ecDetector;
    PCALGeant4Factory pcalDetector; 
    TrajectorySurfaces tSurf;
    String clasDictionaryPath ;
    String variationName;
    public DCEngine(String name) {
        super(name,"ziegler","5.0");
    }

    public void setStartTimeOption() {
        // Load config
        String useSTTConf = this.getEngineConfigString("useStartTime");
        
        if (useSTTConf!=null) {
            System.out.println("["+this.getName()+"] run with start time in tracking config chosen based on yaml ="+useSTTConf);
            Constants.setUSETSTART(Boolean.valueOf(useSTTConf));
        }
        else {
            useSTTConf = System.getenv("USESTT");
            if (useSTTConf!=null) {
                System.out.println("["+this.getName()+"] run with start time in tracking config chosen based on env ="+useSTTConf);
                Constants.setUSETSTART(Boolean.valueOf(useSTTConf));
            }
        }
        if (useSTTConf==null) {
             System.out.println("["+this.getName()+"] run with start time in tracking config chosen based on default ="+Constants.isUSETSTART());
        }
        
        // Wire distortions
        String wireDistortionsFlag = this.getEngineConfigString("wireDistort");
        
        if (wireDistortionsFlag!=null) {
            System.out.println("["+this.getName()+"] run with wire distortions in tracking config chosen based on yaml ="+wireDistortionsFlag);
            if(Boolean.valueOf(wireDistortionsFlag)==true) {
                Constants.setWIREDIST(1.0);
            } else {
                Constants.setWIREDIST(0);
            }
        }
        else {
            wireDistortionsFlag = System.getenv("USEWIREDIST");
            if (wireDistortionsFlag!=null) {
                System.out.println("["+this.getName()+"] run with wire distortions in tracking config chosen based on env ="+wireDistortionsFlag);
                if(Boolean.valueOf(wireDistortionsFlag)==true) {
                    Constants.setWIREDIST(1.0);
                } else {
                    Constants.setWIREDIST(0);
                }
            }
        }
        if (wireDistortionsFlag==null) {
             System.out.println("["+this.getName()+"] run with default setting for wire distortions in tracking (off in MC, on in data)");
        }
    }
    public void LoadTables() {
        
        // Load tables
        clasDictionaryPath= CLASResources.getResourcePath("etc");
        String[]  dcTables = new String[]{
            "/calibration/dc/signal_generation/doca_resolution",
          //  "/calibration/dc/time_to_distance/t2d",
            "/calibration/dc/time_to_distance/time2dist",
         //   "/calibration/dc/time_corrections/T0_correction",
            "/calibration/dc/time_corrections/tdctimingcuts",
            "/calibration/dc/time_jitter",
            "/calibration/dc/tracking/wire_status",
        };

        requireConstants(Arrays.asList(dcTables));
        // Get the constants for the correct variation
        String geomDBVar = this.getEngineConfigString("geomDBVariation");
        if (geomDBVar!=null) {
            System.out.println("["+this.getName()+"] run with geometry variation based on yaml ="+geomDBVar);
        }
        else {
            geomDBVar = System.getenv("GEOMDBVAR");
            if (geomDBVar!=null) {
                System.out.println("["+this.getName()+"] run with geometry variation chosen based on env ="+geomDBVar);
            }
        } 
        if (geomDBVar==null) {
            System.out.println("["+this.getName()+"] run with default geometry");
        }
        
        // Load the geometry
        ConstantProvider provider = GeometryFactory.getConstants(DetectorType.DC, 11, Optional.ofNullable(geomDBVar).orElse("default"));
        dcDetector = new DCGeant4Factory(provider, DCGeant4Factory.MINISTAGGERON);
        for(int l=0; l<6; l++) {
            Constants.wpdist[l] = provider.getDouble("/geometry/dc/superlayer/wpdist", l);
            System.out.println("****************** WPDIST READ *********FROM "+geomDBVar+"**** VARIATION ****** "+provider.getDouble("/geometry/dc/superlayer/wpdist", l));
        }
        // Load other geometries
        ConstantProvider providerFTOF = GeometryFactory.getConstants(DetectorType.FTOF, 11, "default");
        ftofDetector = new FTOFGeant4Factory(providerFTOF);
        
        ConstantProvider providerEC = GeometryFactory.getConstants(DetectorType.ECAL, 11, "default");
        ecDetector = new ECGeant4Factory(providerEC);
        pcalDetector = new PCALGeant4Factory(providerEC);
        
        
        System.out.println(" -- Det Geometry constants are Loaded " );
        // create the surfaces
        tSurf = new TrajectorySurfaces();
        tSurf.LoadSurfaces(dcDetector, ftofDetector, ecDetector, pcalDetector);
        
        // Get the constants for the correct variation
        String ccDBVar = this.getEngineConfigString("constantsDBVariation");
        if (ccDBVar!=null) {
            System.out.println("["+this.getName()+"] run with constants variation based on yaml ="+ccDBVar);
        }
        else {
            ccDBVar = System.getenv("CCDBVAR");
            if (ccDBVar!=null) {
                System.out.println("["+this.getName()+"] run with constants variation chosen based on env ="+ccDBVar);
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
    
    
    @Override
    public boolean processDataEvent(DataEvent event) {
        return true;
    }

    @Override
    public boolean init() {
        return true;
    }

}
