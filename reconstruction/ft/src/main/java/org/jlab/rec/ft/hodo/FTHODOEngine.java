package org.jlab.rec.ft.hodo;

import java.io.IOException;
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
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioSource;
import org.jlab.io.hipo.HipoDataSource;


public class FTHODOEngine extends ReconstructionEngine {

	public FTHODOEngine() {
		super("FTHODO", "devita", "3.0");
	}

	FTHODOReconstruction reco;
	
	@Override
	public boolean init() {
		reco = new FTHODOReconstruction();
		reco.debugMode=0;

                String[]  tables = new String[]{ 
                    "/calibration/ft/fthodo/charge_to_energy",
                    "/calibration/ft/fthodo/time_offsets",
                    "/calibration/ft/fthodo/status",
                    "/geometry/ft/fthodo"
                };
                requireConstants(Arrays.asList(tables));
                this.getConstantsManager().setVariation("default");

                this.registerOutputBank("FTHODO::hits","FTHODO::clusters");
                
                return true;
	}

	@Override
	public boolean processDataEvent(DataEvent event) {
            List<FTHODOHit> allHits      = new ArrayList();
            List<FTHODOHit> selectedHits = new ArrayList();
            List<FTHODOCluster> clusters = new ArrayList();
            
            // update calibration constants based on run number if changed
            int run = setRunConditionsParameters(event);
            
            if(run>=0) {
                // get hits fron banks
                allHits = reco.initFTHODO(event,this.getConstantsManager(), run);
                // select good hits and order them by energy
                selectedHits = reco.selectHits(allHits); 
                // create clusters
                clusters = reco.findClusters(selectedHits);
                // write output banks
                reco.writeBanks(event, selectedHits, clusters);
            }
            return true;
	}

    public int setRunConditionsParameters(DataEvent event) {
        int run = -1;
        if(event.hasBank("RUN::config")==false) {
                System.out.println("RUN CONDITIONS NOT READ!");
        }       
    
        if(event instanceof EvioDataEvent) {
            EvioDataBank bank = (EvioDataBank) event.getBank("RUN::config");
            run = bank.getInt("Run",0);
        }
        else {
            DataBank bank = event.getBank("RUN::config");
            run = bank.getInt("run",0);
        }
	return run;	
    }

    
    public static void main (String arg[]) {
		FTHODOEngine cal = new FTHODOEngine();
		cal.init();
//		String input = "/Users/devita/data/out_clasdispr.00.e11.000.emn0.75tmn.09.xs65.61nb.dis.1.V5.hipo";
//		HipoDataSource  reader = new HipoDataSource();
		String input = "/Users/devita/test4.hipo";
		HipoDataSource  reader = new HipoDataSource();
		reader.open(input);
		
		// initialize histos
        H1F h1 = new H1F("Cluster Energy",100, 0.,5.);         
        h1.setOptStat(Integer.parseInt("1111")); h1.setTitleX("Cluster Energy (GeV)");
        H1F h2 = new H1F("Energy Resolution",100, -1, 1);         
        h2.setOptStat(Integer.parseInt("1111")); h2.setTitleX("Energy Resolution(GeV)");
        H1F h3 = new H1F("Theta Resolution",100, -2, 2);         
        h3.setOptStat(Integer.parseInt("1111")); h3.setTitleX("Theta Resolution(deg)");
        H1F h4 = new H1F("Phi Resolution",100, -10, 10);         
        h4.setOptStat(Integer.parseInt("1111")); h4.setTitleX("Phi Resolution(deg)");
        H1F h5 = new H1F("Time Resolution",100, -10, 10);         
        h5.setOptStat(Integer.parseInt("1111")); h5.setTitleX("Time Resolution(ns)");

        while(reader.hasEvent()){
            DataEvent event = (DataEvent) reader.getNextEvent();
            cal.processDataEvent(event);

            DetectorEvent detectorEvent = DetectorData.readDetectorEvent(event);
            PhysicsEvent            gen = detectorEvent.getGeneratedEvent();
            if(event.hasBank("FTHODO::clusters")) {
                DataBank bank = event.getBank("FTHODO::clusters");
                int nrows = bank.rows();
                for(int i=0; i<nrows;i++) {
                    h1.fill(bank.getFloat("energy",i));
                    h2.fill(bank.getFloat("energy",i)-gen.getParticle("[11]").vector().p());
//                    h3.fill(bank.getFloat("clusterTheta",i)-gen.getParticle("[11]").theta()*180/Math.PI);
//                    h4.fill(bank.getFloat("clusterPhi",i)-gen.getParticle("[11]").phi()*180/Math.PI);
                    h5.fill(bank.getFloat("time",i));
            	}
            }
        }
        JFrame frame = new JFrame("FT Reconstruction");
        frame.setSize(800,800);
        EmbeddedCanvas canvas = new EmbeddedCanvas();
        canvas.divide(2,3);
        canvas.cd(0); canvas.draw(h1);
        canvas.cd(1); canvas.draw(h2);
        canvas.cd(2); canvas.draw(h3);
        canvas.cd(3); canvas.draw(h4);
        canvas.cd(5); canvas.draw(h5);
        frame.add(canvas);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);     

	}		
}
