package org.jlab.rec.ft;

import java.io.IOException;

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
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.rec.ft.cal.FTCALEngine;
import org.jlab.rec.ft.hodo.FTHODOEngine;

public class FTEBEngine extends ReconstructionEngine {

	public FTEBEngine() {
		super("FTEB", "devita", "3.0");
	}

	FTEventBuilder reco;
	int Run = -1;
	FTConfig config;
	
	@Override
	public boolean init() {
		config = new FTConfig();
		reco = new FTEventBuilder();
		reco.debugMode=0;
		return true;
	}

	@Override
	public boolean processDataEvent(DataEvent event) {
		Run = config.setRunConditionsParameters(event, "FTEB", Run);
                reco.init(config.getSolenoid());
		reco.addResponses(event);
                reco.initFTparticles();
                reco.matchToHODO();
                reco.writeBanks(event);
		return true;
	}
	
        
    public static void main (String arg[]) throws IOException {
		FTCALEngine cal = new FTCALEngine();
		cal.init();
		FTHODOEngine hodo = new FTHODOEngine();
		hodo.init();
		FTEBEngine en = new FTEBEngine();
		en.init();
//		String input = "/Users/devita/Work/clas12/simulations/tests/detectors/clas12/ft/elec_nofield_header.evio";
//		EvioSource  reader = new EvioSource();
		String input = "/Users/devita/Work/clas12/simulations/tests/detectors.ftm/clas12/ft/test.hipo";
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
            hodo.processDataEvent(event);
            en.processDataEvent(event);
            
            if(event instanceof EvioDataEvent) {
                GenericKinematicFitter      fitter = new GenericKinematicFitter(11);
                PhysicsEvent                   gen = fitter.getGeneratedEvent((EvioDataEvent)event);
                if(event.hasBank("FTRec::tracks")) {
                    DataBank bank = event.getBank("FTRec::tracks");
                    int nrows = bank.rows();
                    for(int i=0; i<nrows;i++) {
                        h1.fill(bank.getDouble("Energy",i));
                        h2.fill(bank.getDouble("Energy",i)-gen.getParticle("[11]").vector().p());
                        Vector3D part = new Vector3D(bank.getDouble("Cx",i),bank.getDouble("Cy",i),bank.getDouble("Cz",i));  
                        h3.fill(Math.toDegrees(part.theta()-gen.getParticle("[11]").theta()));
                        h4.fill(Math.toDegrees(part.phi()-gen.getParticle("[11]").phi()));
                        h5.fill(bank.getDouble("Time",i));
                    }
                }
            }
            else{
                DetectorEvent detectorEvent = DetectorData.readDetectorEvent(event);
                PhysicsEvent            gen = detectorEvent.getGeneratedEvent();
                if(event.hasBank("FT::particles")) {
                    DataBank bank = event.getBank("FT::particles");
                    int nrows = bank.rows();
                    for(int i=0; i<nrows;i++) {
                        h1.fill(bank.getFloat("energy",i));
                        h2.fill(bank.getFloat("energy",i)-gen.getGeneratedParticle(0).vector().p());
                        Vector3D part = new Vector3D(bank.getFloat("cx",i),bank.getFloat("cy",i),bank.getFloat("cz",i));  
                        h3.fill(Math.toDegrees(part.theta()-gen.getGeneratedParticle(0).theta()));
                        h4.fill(Math.toDegrees(part.phi()-gen.getGeneratedParticle(0).phi()));
                        h5.fill(bank.getFloat("time",i));
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