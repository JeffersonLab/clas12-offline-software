package org.jlab.rec.rich;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataBank;

import org.jlab.utils.groups.IndexedTable;
import java.util.Arrays;
import java.util.Optional;

import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Line3D;

import org.jlab.detector.geom.RICH.RICHGeoFactory;

public class RICHEBEngine extends ReconstructionEngine {

    private int Run = -1;
    private int Ncalls = 0;

    private long EBRICH_start_time;
    
    private RICHGeoFactory       richgeo;
    private RICHTime             richtime = new RICHTime();
    private String engineDebug;


    // ----------------
    public RICHEBEngine() {
    // ----------------
        super("RICHEB", "mcontalb", "3.0");

    }


    @Override
    // ----------------
    public boolean init() {
    // ----------------

        int debugMode = 0;
        if(debugMode>=1)System.out.format("I am in RICHEBEngine init \n");


        String[] richTables = new String[]{
                    "/geometry/rich/setup",
                    "/geometry/rich/geo_parameter",
                    "/geometry/rich/module1/aerogel",
                    "/geometry/rich/module2/aerogel",
                    "/geometry/rich/module1/alignment",
                    "/geometry/rich/module2/alignment",
                    "/calibration/rich/reco_flag",
                    "/calibration/rich/reco_parameter",
                    "/calibration/rich/module1/time_walk",
                    "/calibration/rich/module1/time_offset",
                    "/calibration/rich/module1/cherenkov_angle",
                    "/calibration/rich/module1/mapmt_pixel",
                    "/calibration/rich/module1/status_mirror",
                    "/calibration/rich/module1/status_aerogel",
                    "/calibration/rich/module1/status_mapmt",
                    "/calibration/rich/module2/time_walk",
                    "/calibration/rich/module2/time_offset",
                    "/calibration/rich/module2/cherenkov_angle",
                    "/calibration/rich/module2/mapmt_pixel",
                    "/calibration/rich/module2/status_mirror",
                    "/calibration/rich/module2/status_aerogel",
                    "/calibration/rich/module2/status_mapmt"
                 };

        requireConstants(Arrays.asList(richTables));

        // initialize constants manager default variation, will be then modified based on yaml settings
        // Get the constants for the correct variation
        String engineVariation = Optional.ofNullable(this.getEngineConfigString("variation")).orElse("default");         
        this.getConstantsManager().setVariation(engineVariation);
        engineDebug = Optional.ofNullable(this.getEngineConfigString("debug")).orElse("turn_OFF");         

        // Get the constant tables for reconstruction parameters, geometry and optical characterization
        int run = 11;

        richgeo   = new RICHGeoFactory(1, this.getConstantsManager(), run, engineDebug);
        richtime.init_ProcessTime();

        return true;

    }


    @Override
    // ----------------
    public boolean processDataEvent(DataEvent event) {
    // ----------------

        int debugMode = 0;

        // create instances of all event-dependent classes in processDataEvent to avoid interferences between different threads when running in clara
        RICHEvent              richevent = new RICHEvent();
        RICHio                 richio    = new RICHio();
        RICHCalibration        richcal   = new RICHCalibration();
        RICHParameters         richpar   = new RICHParameters();

        RICHPMTReconstruction  rpmt      = new RICHPMTReconstruction(richevent, richgeo, richio);
        RICHEventBuilder       reb       = new RICHEventBuilder(event, richevent, richgeo, richio);
        RICHRayTrace           richtrace = new RICHRayTrace(richgeo, richpar); 
        
        richtime.save_ProcessTime(0, richevent);

        if(debugMode>=1){
            System.out.println("---------------------------------");
            System.out.println("RICH Engine call: "+Ncalls+" New Event Process "+richevent.get_EventID());
            System.out.println("---------------------------------");
        }

	//  Initialize the CCDB information
        if(debugMode>=1)System.out.println("----- Load CCDB data \n");
        int run = richevent.get_RunID(); 
        if(run>0){
            richpar.load_CCDB(this.getConstantsManager(), run, Ncalls, engineDebug);
            richcal.load_CCDB(this.getConstantsManager(), run, Ncalls, richgeo, richpar);
        }else{
            richpar.load_CCDB(this.getConstantsManager(),  11, Ncalls, engineDebug);
            richcal.load_CCDB(this.getConstantsManager(),  11, Ncalls, richgeo, richpar);
        }
        Ncalls++;

        richtime.save_ProcessTime(1, richevent);


	/*
	Process RICH signals to get hits and clusters
	*/
        if(richpar.PROCESS_RAWDATA==1){
            if(debugMode>=1)System.out.println("----- Process raw data \n");
            richio.clear_LowBanks(event); 
            rpmt.process_RawData(event, richpar, richcal);
            richtime.save_ProcessTime(2, richevent);
        }

	/*
	Process RICH-DC event reconstruction
	*/
        if(richpar.PROCESS_DATA==1){
            if(debugMode>=1)System.out.println("----- Process data \n");
            richio.clear_HighBanks(event); 
            if( !reb.process_Data(event, richpar, richcal, richtrace, richtime)) return false;
            richtime.save_ProcessTime(8, richevent);
        }

        if(richpar.DEBUG_PROC_TIME>=1) richtime.dump_ProcessTime();

        return true;

    }

}
