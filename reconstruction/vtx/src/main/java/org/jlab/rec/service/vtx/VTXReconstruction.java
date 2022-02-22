package org.jlab.rec.service.vtx;

import java.util.ArrayList;
import java.util.List;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.clas.swimtools.Swim;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.vtx.TrackParsHelix;
import org.jlab.rec.vtx.Vertex;
import org.jlab.rec.vtx.VertexFinder;
import org.jlab.rec.vtx.banks.Reader;
import org.jlab.rec.vtx.banks.Writer;

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
        Swim swimmer = new Swim();
        // set parameters for all helixes
    	Reader dcReader = new Reader();  	
    	List<TrackParsHelix> dcTrkList = dcReader.get_Trks(event, swimmer);
    	List<Vertex> verteces = new ArrayList<Vertex>();
        // find helix pairs
    	VertexFinder vertexFinder = new VertexFinder();
    	vertexFinder.FindHelixPairs(dcTrkList);
    	List<ArrayList<TrackParsHelix>> hxPairs = vertexFinder.get_HelixPairs();    
    	
    	// find "intersection" between helixes of helix pairs
    	for(int i=0; i<hxPairs.size(); i++) {
    		Vertex v = vertexFinder.FindVertex(hxPairs.get(i));
                //put cuts....
                if(v!=null)
                    verteces.add(v);
    	}
        Writer vtxOut = new Writer();
        event.appendBank(vtxOut.VtxBank(event, verteces));
        return true;
   }

    @Override
    public boolean init() {
       
    
       return true;
    }

     
    public static void main(String[] args) {
    }
}
