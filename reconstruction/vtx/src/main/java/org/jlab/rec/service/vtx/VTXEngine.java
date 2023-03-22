package org.jlab.rec.service.vtx;

import java.util.ArrayList;
import java.util.List;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.vtx.Constants;
import org.jlab.rec.vtx.Vertex;
import org.jlab.rec.vtx.VertexFinder;
import org.jlab.rec.vtx.banks.Reader;
import org.jlab.rec.vtx.banks.Writer;

/**
 * Service to return reconstructed vertices from EB tracks
 *
 * @author ziegler
 *
 */
public class VTXEngine extends ReconstructionEngine {

    private double docaCut = 3.0; //cut on variable r

    public VTXEngine() {
        super("VTX", "ziegler", "2.0");
    }

    String FieldsConfig = "";
    private int Run = -1;
  
    Writer vtxOut = new Writer();

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
        if (event.hasBank("RUN::config") == false) {
            System.err.println("RUN CONDITIONS NOT READ!");
            return false;
        }

        int newRun = event.getBank("RUN::config").getInt("run", 0); 
        if (Run != newRun)  this.setRun(newRun); 
        
        this.Run = this.getRun();
        
        VertexFinder vtf = new VertexFinder();
        List<Vertex> verteces = new ArrayList<>();
        // set parameters for all helixes
    	Reader trkReader = new Reader();  	
    	trkReader.readDataBanks(event);
        for(int i = 0; i < trkReader.getParticles().size()-1; i++) {
            for (int j = i+1; j < trkReader.getParticles().size(); j++) {
                Vertex vt = vtf.computeVertex(trkReader.getParticles().get(i), 
                        trkReader.getParticles().get(j));
                if(vt!=null)
                    verteces.add(vt);
                
            }
        }
        if(!verteces.isEmpty()) {
            event.appendBank(vtxOut.VtxBank(event, verteces));
        }
        return true;
   }

    @Override
    public boolean init() {
    //    String variation = Optional.ofNullable(this.getEngineConfigString("variation")).orElse("default");
        //beam offset table
    //    DatabaseConstantProvider dbprovider = new DatabaseConstantProvider(11, variation);
    //    dbprovider.loadTable("/geometry/beam/position");
      this.registerBanks();
      this.loadConfiguration();
      Constants.setDOCACUT(this.docaCut);
      return true;
    }
    private void registerBanks() {
        super.registerOutputBank("REC::VertDoca");    
    } 
    
    public void loadConfiguration() {    
        if (this.getEngineConfigString("docaCut")!=null) 
            this.docaCut = (double) Double.valueOf(this.getEngineConfigString("docaCut"));
                   
    }

    
}
