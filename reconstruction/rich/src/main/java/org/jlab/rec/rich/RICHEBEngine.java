package org.jlab.rec.rich;


import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;
import java.util.Arrays;

public class RICHEBEngine extends ReconstructionEngine {

    private RICHPMTReconstruction    rpmt;
    private RICHEventBuilder         reb;
    private RICHTool                 tool;

    private int Run = -1;
    private int Ncalls = 0;

    private long EBRICH_start_time;
    

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

        tool = new RICHTool();
        tool.init(getConstantsManager().getConstants(0, "/calibration/rich/aerogel") );
   
        rpmt = new RICHPMTReconstruction(tool);

        reb = new RICHEventBuilder(tool);

        return true;

    }

 
    // ----------------
    public boolean init_CCDB() {
    // ----------------

        String[] aeroTables = new String[]{
                    "/calibration/rich/aerogel"
                 };

        requireConstants(Arrays.asList(aeroTables));

        // Get the constants for the correct variation
        getConstantsManager().setVariation("default");

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
        reb.init_Event(event);

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
        tool.dump_ProcessTime();

        return true;

    }

}
