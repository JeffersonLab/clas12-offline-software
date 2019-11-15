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

public class RICHEBEngine extends ReconstructionEngine {

    private int Run = -1;
    private int Ncalls = 0;

    private long EBRICH_start_time;
    
    private RICHTool  tool = null;
    private boolean LOAD_TABLES = true;

    // ----------------
    public RICHEBEngine() {
    // ----------------
        super("RICHEB", "mcontalb-kenjo", "3.0");

    }


    @Override
    // ----------------
    public boolean init() {
    // ----------------


        int debugMode = 0;

        tool = new RICHTool();

        boolean ccdb = init_CCDB();

        return true;

    }

 
    // ----------------
    public boolean init_CCDB() {
    // ----------------

        int debugMode = 0;

        String[] richTables = new String[]{
                    "/calibration/rich/aerogel",
                    "/calibration/rich/time_walk",
                    "/calibration/rich/time_offset",
                    "/calibration/rich/misalignments",
                    "/calibration/rich/parameter"
                 };

        requireConstants(Arrays.asList(richTables));

        // initialize constants manager default variation, will be then modified based on yaml settings
            // Get the constants for the correct variation
        String engineVariation = Optional.ofNullable(this.getEngineConfigString("variation")).orElse("default");         
        getConstantsManager().setVariation(engineVariation);

        if(debugMode==1)System.out.format("RICHEBEngine: Load geometry constants from CCDB \n");

        // Get the constant tables for reconstruction parameters, geometry and optical characterization
        int run = 11;

        IndexedTable test = getConstantsManager().getConstants(run, "/calibration/rich/parameter");


        tool.init_GeoConstants(
                  getConstantsManager().getConstants(run, "/calibration/rich/parameter"),
                  getConstantsManager().getConstants(run, "/calibration/rich/aerogel"),
                  getConstantsManager().getConstants(run, "/calibration/rich/misalignments") );

        return true;

    }


    @Override
    // ----------------
    public boolean processDataEvent(DataEvent event) {
    // ----------------

        int debugMode = 0;

	Ncalls++;
        
        // create instances of all event-dependent classes in processDataEvent to avoid interferences between different threads when running in clara
        RICHEvent        richevent = new RICHEvent();
        RICHio              richio = new RICHio();
        RICHPMTReconstruction rpmt = new RICHPMTReconstruction(richevent, tool, richio);
        RICHEventBuilder       reb = new RICHEventBuilder(richevent, tool, richio);

	/*
	Initialize the RICH event
	*/
        reb.init_Event(event);
        init_Event(event,richevent,tool);

        if(debugMode>=1){
            System.out.println("---------------------------------");
            System.out.println("RICH Engine call: "+Ncalls+" New Event Process "+reb.getEventID()+"\n");
            System.out.println("---------------------------------");
        }

        // clear RICH output banks
        if(tool.get_Constants().REDO_RICH_RECO==1)richio.clear_Banks(event); // would be better to move all io operation here rather than passing richio around

	/*
	Process RICH signals to get hits and clusters
	*/
        rpmt.processRawData(event);

        tool.save_ProcessTime(0);

	/*
	Process RICH-DC event reconstruction
	*/
        if( !reb.process_Data(event)) return false;

        tool.save_ProcessTime(6);
        //tool.dump_ProcessTime();

        return true;

    }

    // ----------------
    public void init_Event(DataEvent event, RICHEvent richevent, RICHTool tool) {
    // ----------------

        int debugMode = 0;
        int run = richevent.get_RunID(); 
        if(run>0 && LOAD_TABLES) {

            LOAD_TABLES = false;

            if(debugMode>=1)System.out.format("LOAD constants from CCDB for run %5d \n",run);
    
            // Get the run-dependent tables for time calibration
            tool.init_TimeConstants( getConstantsManager().getConstants(run, "/calibration/rich/time_walk"),
                      getConstantsManager().getConstants(run, "/calibration/rich/time_offset") );

        }

    }

}
