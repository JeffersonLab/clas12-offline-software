/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.ft.trk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFrame;
import org.jlab.clas.detector.DetectorData;
import org.jlab.clas.detector.DetectorEvent;
import org.jlab.clas.physics.GenericKinematicFitter;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;

/**
 *
 * @author devita
 */
public class FTTRKEngine extends ReconstructionEngine {

	public FTTRKEngine() {
		super("FTTRK", "devita", "1.0");
	}

	FTTRKReconstruction reco;
	
	@Override
	public boolean init() {
            
            FTTRKConstantsLoader.Load();
            
		reco = new FTTRKReconstruction();
		reco.debugMode=0;

            String[]  tables = new String[]{ 
                "/calibration/ft/fthodo/charge_to_energy",
                "/calibration/ft/fthodo/time_offsets",
                "/calibration/ft/fthodo/status",
                "/geometry/ft/fthodo"
            };
            requireConstants(Arrays.asList(tables));
            this.getConstantsManager().setVariation("default");

            return true;
	}

	@Override
	public boolean processDataEvent(DataEvent event) {
            List<FTTRKHit> allHits      = new ArrayList();
            List<FTTRKHit> selectedHits = new ArrayList();
            List<FTTRKCluster> clusters = new ArrayList();
            List<FTTRKCross>   crosses  = new ArrayList();
            
            // update calibration constants based on run number if changed
            int run = setRunConditionsParameters(event);
            
            if(run>=0) {
                // get hits fron banks
                allHits = reco.initFTTRK(event,this.getConstantsManager(), run);
//                // select good hits and order them by energy
//                selectedHits = reco.selectHits(allHits); 
                // create clusters
                clusters = reco.findClusters(allHits);
                // create crosses
                crosses = reco.findCrosses(clusters);
                // write output banks
                reco.writeBanks(event, allHits, clusters, crosses);
            }
            return true;
	}

    public int setRunConditionsParameters(DataEvent event) {
        int run = -1;
        if(event.hasBank("RUN::config")==false) {
                System.out.println("RUN CONDITIONS NOT READ!");
        }       
    
        DataBank bank = event.getBank("RUN::config");
            run = bank.getInt("run")[0];
        
	return run;	
    }

    
    public static void main (String arg[]) {
		FTTRKEngine trk = new FTTRKEngine();
		trk.init();
		String input = "/Users/devita/work/clas12/simulations/clas12Tags/4.3.1/out.hipo";
		HipoDataSource  reader = new HipoDataSource();
		reader.open(input);
		
		// initialize histos
        H2F h1 = new H2F("h1", "Layer vs. Component",100, 0.,1000,5, 0.,5.);         
        h1.setTitleX("Component");
        h1.setTitleX("Layer");
        H1F h2 = new H1F("Energy",100, 0, 100);         
        h2.setOptStat(Integer.parseInt("1111")); h2.setTitleX("Energy"); h2.setTitleY("Counts");
        H1F h3 = new H1F("Time",100, -2, 2);         
        h3.setOptStat(Integer.parseInt("1111")); h3.setTitleX("Time"); h3.setTitleY("Counts");

        while(reader.hasEvent()){
            DataEvent event = (DataEvent) reader.getNextEvent();
            trk.processDataEvent(event);

            DetectorEvent detectorEvent = DetectorData.readDetectorEvent(event);
            PhysicsEvent            gen = detectorEvent.getGeneratedEvent();
            if(event.hasBank("FTTRK::hits")) {
                DataBank bank = event.getBank("FTTRK::hits");
                int nrows = bank.rows();
                for(int i=0; i<nrows;i++) {
                    int layer  = bank.getByte("layer",i);
                    int comp   = bank.getShort("component",i);
                    float energy = bank.getFloat("energy",i);
                    float time   = bank.getFloat("time",i);
                    
                    h1.fill(comp,layer);
                    h2.fill(energy);
                    h3.fill(time);
            	}
            }
        }
        JFrame frame = new JFrame("FT Reconstruction");
        frame.setSize(800,800);
        EmbeddedCanvas canvas = new EmbeddedCanvas();
        canvas.divide(1,3);
        canvas.cd(0); canvas.draw(h1);
        canvas.cd(1); canvas.draw(h2);
        canvas.cd(2); canvas.draw(h3);
        frame.add(canvas);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);     

	}		
}
