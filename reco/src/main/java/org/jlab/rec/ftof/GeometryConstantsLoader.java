package org.jlab.rec.ftof;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.detector.geant4.v2.FTOFGeant4Factory;
import org.jlab.detector.hits.DetHit;
import org.jlab.detector.hits.FTOFDetHit;
import org.jlab.geometry.prim.Line3d;

import eu.mihosoft.vrl.v3d.Vector3d;

/**
 * 
 * @author ziegler
 *
 */
public class GeometryConstantsLoader {

	public GeometryConstantsLoader() {
		// TODO Auto-generated constructor stub
	}
	public static boolean CSTLOADED = false;
	
	
	 //Get constants from DB    
    public static final DatabaseConstantProvider dbprovider = new DatabaseConstantProvider(10,"default");
    //private Detector ftofDetector;
    public static boolean areCalibConstantsLoaded = false;
    
    public static synchronized void Load() {
		if (CSTLOADED) return;
	    // load table reads entire table and makes an array of variables for each column in the table.
		
		dbprovider.loadTable("/geometry/ftof/panel1a/paddles");
		dbprovider.loadTable("/geometry/ftof/panel1a/panel");
		dbprovider.loadTable("/geometry/ftof/panel1b/paddles");
		dbprovider.loadTable("/geometry/ftof/panel1b/panel");
		dbprovider.loadTable("/geometry/ftof/panel2/paddles");
		dbprovider.loadTable("/geometry/ftof/panel2/panel");
		dbprovider.disconnect();

	    //disconncect from database. Important to do this after loading tables.
	    dbprovider.disconnect(); 

	   // dbprovider.show();

	    CSTLOADED = true;
    }
   
    
    
    public static void main (String arg[]) throws IOException {
    	GeometryConstantsLoader.Load();
    	FTOFGeant4Factory factory = new FTOFGeant4Factory(dbprovider);
    	
    	Random rnd = new Random();

    	for(int itrack=0; itrack<1000; itrack++){
    	        Line3d line = new Line3d(new Vector3d(rnd.nextDouble() * 10000 - 5000, rnd.nextDouble() * 10000 - 5000,  3000),
    	                                                new Vector3d(rnd.nextDouble() * 10000 - 5000, rnd.nextDouble() * 10000 - 5000,  9000));

    	        List<DetHit> hits = factory.getIntersections(line);

    	        for(DetHit hit: hits){
    	                FTOFDetHit fhit = new FTOFDetHit(hit);
    	               
    	        }
    	}


    }
}
