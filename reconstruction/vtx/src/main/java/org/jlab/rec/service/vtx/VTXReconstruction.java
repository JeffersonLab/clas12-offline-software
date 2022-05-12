package org.jlab.rec.service.vtx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.vtx.TrackParsHelix;
import org.jlab.rec.vtx.Vertex;
import org.jlab.rec.vtx.VertexFinder;
import org.jlab.rec.vtx.banks.Reader;
import org.jlab.rec.vtx.banks.Writer;
import org.jlab.utils.groups.IndexedTable;

/**
 * Service to return reconstructed vertices from EB tracks
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
    private void initConstantsTables() {
        String[] tables = new String[]{
            "/geometry/beam/position"
        };
        requireConstants(Arrays.asList(tables));
        this.getConstantsManager().setVariation("default");
    }
    @Override
    public boolean processDataEvent(DataEvent event) {
        this.FieldsConfig = this.getFieldsConfig();
        if (event.hasBank("RUN::config") == false) {
            System.err.println("RUN CONDITIONS NOT READ!");
            return false;
        }

        int newRun = event.getBank("RUN::config").getInt("run", 0); 
        if (Run != newRun)  this.setRun(newRun); 
        
        this.Run = this.getRun();
        Swim swimmer = new Swim();
        IndexedTable beamPos   = this.getConstantsManager().getConstants(this.getRun(), "/geometry/beam/position");
        double xb = beamPos.getDoubleValue("x_offset", 0, 0, 0);
        double yb = beamPos.getDoubleValue("y_offset", 0, 0, 0);
        
        // set parameters for all helixes
    	Reader trkReader = new Reader();  	
    	List<TrackParsHelix> trkList = trkReader.get_Trks(event, swimmer, xb, yb);
    	List<Vertex> verteces = new ArrayList<Vertex>();
        // find helix pairs
    	VertexFinder vertexFinder = new VertexFinder();
    	vertexFinder.FindHelixPairs(trkList);
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
        this.initConstantsTables();
        String variation = Optional.ofNullable(this.getEngineConfigString("variation")).orElse("default");
        //beam offset table
        DatabaseConstantProvider dbprovider = new DatabaseConstantProvider(11, variation);
        dbprovider.loadTable("/geometry/beam/position");
        return true;
    }
    private void registerBanks() {
        super.registerOutputBank("REC::VertDoca");    
    } 
    public static void main(String[] args) {
    }
}
