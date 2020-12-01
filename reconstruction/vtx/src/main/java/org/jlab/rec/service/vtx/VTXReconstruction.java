package org.jlab.rec.service.vtx;

import java.util.ArrayList;
import java.util.List;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.geom.prim.Point3D;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.vtx.TrackParsHelix;
import org.jlab.rec.vtx.Vertex;
import org.jlab.rec.vtx.banks.Reader;

/**
 * Service to return reconstructed  track candidates- the output is in hipo
 * format
 *
 * @author ziegler
 *
 */
public class VTXReconstruction extends ReconstructionEngine {

    public VTXReconstruction() {
        super("VTX", "ziegler", "1.0");
    }

    String FieldsConfig = "";
    private int Run = -1;
  
 

    public int getRun() {
        return Run;
    }

    public void setRun(int run) {
        Run = run;
    }

    public String getFieldsConfig() {
        return FieldsConfig;
    }

    public void setFieldsConfig(String fieldsConfig) {
        FieldsConfig = fieldsConfig;
    }
    
    @Override
    public boolean processDataEvent(DataEvent event) {
        this.FieldsConfig = this.getFieldsConfig();
        this.Run = this.getRun();
        
        // set parameters for all helixes
    	Reader dcReader = new Reader();  	
    	List<TrackParsHelix> dcTrkList = dcReader.get_DCTrks(event);
    	
        // find helix pairs
    	Vertex vertex = new Vertex();
    	vertex.FindHelixPairs(dcTrkList);
    	List<ArrayList<TrackParsHelix>> hxPairs = vertex.get_HelixPairs();    
    	
    	// find "intersection" between helixes of helix pairs
    	for(int i=0; i<hxPairs.size(); i++) {
    		double[] pInts = null;
    		pInts = vertex.InterpolateBetwHelices(hxPairs.get(i));
                System.out.println(new Point3D(pInts[0],pInts[1],pInts[2]).toString());
    	}
        return true;
   }

    @Override
    public boolean init() {
       
    
       return true;
    }

     
    public static void main(String[] args) {
    }
}
