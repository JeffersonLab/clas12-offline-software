package org.jlab.rec.ft.cal;

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
import org.jlab.geom.prim.Vector3D;
import org.jlab.groot.data.H1F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataSource;


public class FTCALEngine extends ReconstructionEngine {

	public FTCALEngine() {
		super("FTCAL", "devita", "3.0");
	}

	FTCALReconstruction reco;
	
	@Override
	public boolean init() {
		reco = new FTCALReconstruction();
		reco.debugMode=0;

                String[]  tables = new String[]{ 
                    "/calibration/ft/ftcal/charge_to_energy",
                    "/calibration/ft/ftcal/time_offsets",
                    "/calibration/ft/ftcal/time_walk",
                    "/calibration/ft/ftcal/status",
                    "/calibration/ft/ftcal/thresholds",
                    "/calibration/ft/ftcal/cluster",
                    "/calibration/ft/ftcal/energycorr"
                };
                requireConstants(Arrays.asList(tables));
                this.getConstantsManager().setVariation("default");

                this.registerOutputBank("FTCAL::hits","FTCAL::clusters");
                
                return true;
	}

	@Override
	public boolean processDataEvent(DataEvent event) {
            List<FTCALHit>     allHits           = new ArrayList();
            List<FTCALHit>     selectedHits      = new ArrayList();
            List<FTCALCluster> clusters          = new ArrayList();
            
            // update calibration constants based on run number if changed
            int run = setRunConditionsParameters(event);

            if(run>=0) {
                // get hits fron banks
                allHits = reco.initFTCAL(event,this.getConstantsManager(), run);
                // select good hits and order them by energy
                selectedHits = reco.selectHits(allHits,this.getConstantsManager(), run);
                // create clusters
                clusters = reco.findClusters(selectedHits, this.getConstantsManager(), run);
                // set cluster status
                reco.selectClusters(clusters, this.getConstantsManager(), run);
                // write output banks
                reco.writeBanks(event, selectedHits, clusters, this.getConstantsManager(), run);
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

    
    public static void main (String arg[])  {
		FTCALEngine cal = new FTCALEngine();
		cal.init();
		String input = "/Users/devita/Work/clas12/simulations/ft/out.hipo";
		HipoDataSource  reader = new HipoDataSource();
//		String input = "/Users/devita/Work/clas12/simulations/tests/detectors/clas12/ft/out_header.ev";
//		EvioSource  reader = new EvioSource();
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

            if(event instanceof EvioDataEvent) {
                GenericKinematicFitter      fitter = new GenericKinematicFitter(11);
                PhysicsEvent                   gen = fitter.getGeneratedEvent((EvioDataEvent)event);
                if(event.hasBank("FTCALRec::clusters")) {
                    DataBank bank = event.getBank("FTCALRec::clusters");
                    int nrows = bank.rows();
                    for(int i=0; i<nrows;i++) {
                        h1.fill(bank.getDouble("clusEnergy",i));
                        h2.fill(bank.getDouble("clusEnergy",i)-gen.getParticle("[11]").vector().p());
                        h3.fill(bank.getDouble("clusTheta",i)-gen.getParticle("[11]").theta()*180/Math.PI);
                        h4.fill(bank.getDouble("clusPhi",i)-gen.getParticle("[11]").phi()*180/Math.PI);
                        h5.fill(bank.getDouble("clusTime",i));
                    }
                }
            }
            else{
                DetectorEvent detectorEvent = DetectorData.readDetectorEvent(event);
                PhysicsEvent            gen = detectorEvent.getGeneratedEvent();
                if(event.hasBank("FTCAL::clusters")) {
                    DataBank bank = event.getBank("FTCAL::clusters");
                    int nrows = bank.rows();
                    for(int i=0; i<nrows;i++) {
                        h1.fill(bank.getFloat("energy",i));
                        h2.fill(bank.getFloat("energy",i)-gen.getGeneratedParticle(0).vector().p());
                        Vector3D cluster = new Vector3D(bank.getFloat("x",i),bank.getFloat("y",i),bank.getFloat("z",i));  
                        h3.fill(Math.toDegrees(cluster.theta()-gen.getGeneratedParticle(0).theta()));
//                        System.out.println(cluster.theta() + " " + gen.getGeneratedParticle(0).theta());
//                        System.out.println(cluster.x() + " " + cluster.y() + " " + cluster.z() + " ");
                        h4.fill(Math.toDegrees(cluster.phi()-gen.getGeneratedParticle(0).phi()));
                        h5.fill(bank.getFloat("time",i)-124.25);
                    }
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
