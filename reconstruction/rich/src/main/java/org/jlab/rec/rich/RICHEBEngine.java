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

public class RICHEBEngine extends ReconstructionEngine {

    private RICHPMTReconstruction    rpmt;
    private RICHEventBuilder         reb;
    private RICHTool                 tool;
    private RICHEvent                richevent;

    private int Run = -1;
    private int Ncalls = 0;

    private long EBRICH_start_time;
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

        boolean ccdb = init_CCDB();

        richevent = new RICHEvent();
        tool = new RICHTool();

        rpmt = new RICHPMTReconstruction(richevent, tool);

        reb = new RICHEventBuilder(richevent, tool);

        return true;

    }

 
    // ----------------
    public boolean init_CCDB() {
    // ----------------

        String[] richTables = new String[]{
                    "/calibration/rich/aerogel",
                    "/calibration/rich/time_walk",
                    "/calibration/rich/time_offset",
                    "/calibration/rich/misalignments",
                    "/calibration/rich/parameter"
                 };

        requireConstants(Arrays.asList(richTables));


        /*int newRun = 2467;
        IndexedTable prova = getConstantsManager().getConstants(newRun, "/calibration/rich/aerogel");

        for (int i=1; i<=16; i++){
          System.out.format(" PROVA %10.5f %5.2f \n",prova.getDoubleValue("n400", 4,201,i),prova.getDoubleValue("planarity", 4,201,i));
        }*/

        return true;

    }


    @Override
    // ----------------
    public boolean processDataEvent(DataEvent event) {
    // ----------------

        int debugMode = 0;

	Ncalls++;

        if(debugMode>=1){
            System.out.println("---------------------------------");
            System.out.println("RICH Engine call: "+Ncalls+" New Event Process "+reb.getEventID()+"\n");
            System.out.println("---------------------------------");
        }

	/*
	Initialize the RICH event
	*/
        init_Event(event);

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
    public void init_Event(DataEvent event) {
    // ----------------

        int debugMode = 0;

        reb.init_Event(event);

        int run = richevent.get_RunID(); 
        if(run>0 && LOAD_TABLES){

            LOAD_TABLES = true;

            if(debugMode>=1)System.out.format("LOAD constants from CCDB for run %5d \n",run);
    
            // Get the constants for the correct variation
            getConstantsManager().setVariation("default");

            // Get the tables
            tool.init(getConstantsManager().getConstants(run, "/calibration/rich/aerogel"),
                      getConstantsManager().getConstants(run, "/calibration/rich/time_walk"),
                      getConstantsManager().getConstants(run, "/calibration/rich/time_offset"), 
                      getConstantsManager().getConstants(run, "/calibration/rich/misalignments"),
                      getConstantsManager().getConstants(run, "/calibration/rich/parameter") );

        }

    }

}
