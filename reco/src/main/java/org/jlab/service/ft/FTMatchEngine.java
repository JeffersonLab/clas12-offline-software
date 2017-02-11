package org.jlab.service.ft;

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;

import org.jlab.clas.physics.GenericKinematicFitter;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.groot.data.H1F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioSource;
import org.jlab.rec.ft.FTCALHODOMatching;
import org.jlab.rec.ft.FTTrack;

public class FTMatchEngine extends ReconstructionEngine {

	public FTMatchEngine() {
		super("FTMATCH", "devita", "3.0");
	}

	FTCALHODOMatching reco;
	int Run = -1;
	FTRecConfig config;
	
	@Override
	public boolean init() {
		config = new FTRecConfig();
		reco = new FTCALHODOMatching();
		reco.debugMode=0;
		return true;
	}

	@Override
	public boolean processDataEvent(DataEvent event) {
		Run = config.setRunConditionsParameters(event, "FTMATCH", Run);
		reco.processEvent((EvioDataEvent) event);
		return true;
	}
	
    public ArrayList<FTTrack>  readFTClusters(EvioDataEvent event){
        ArrayList<FTTrack>  ftTracks = new ArrayList<FTTrack>();
        if(event.hasBank("FTRec::tracks")==true){
            EvioDataBank ftBank = (EvioDataBank) event.getBank("FTRec::tracks");
            int nrows = ftBank.rows();
            for(int i = 0; i < nrows; i++) {
            	FTTrack ftTrack = new FTTrack(ftBank.getInt("ID", i));
            	ftTrack.set_trackCharge(ftBank.getInt("Charge", i));
            	ftTrack.set_trackEnergy(ftBank.getDouble("Energy", i));
            	ftTrack.set_trackDir(ftBank.getDouble("Cx", i),ftBank.getDouble("Cy", i),ftBank.getDouble("Cz", i));
            	ftTrack.set_trackTime(ftBank.getDouble("Time", i));
            	ftTrack.set_trackCluster(ftBank.getInt("Cluster", i));
            	ftTrack.set_trackSignal(ftBank.getInt("Signal", i));
            	ftTrack.set_trackCross(ftBank.getInt("Cross", i));
            	ftTracks.add(ftTrack);
            }
        }
        return ftTracks;
    }
    
    
    public static void main (String arg[]) throws IOException {
		FTCALRecEngine cal = new FTCALRecEngine();
		cal.init();
		FTHODORecEngine hodo = new FTHODORecEngine();
		hodo.init();
		FTMatchEngine en = new FTMatchEngine();
		en.init();
		String input = "/Users/devita/Work/clas12/simulations/tests/devel/calcom_pythia/clasdispr.00.e11.000.emn0.75tmn.09.xs65.61nb.dis.1_header.evio";
		EvioSource  reader = new EvioSource();
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
            GenericKinematicFitter      fitter = new GenericKinematicFitter(11);
            PhysicsEvent                   gen = fitter.getGeneratedEvent((EvioDataEvent)event);
            ArrayList<FTTrack>	       ftTracks = en.readFTClusters((EvioDataEvent)event);
            for(int i=0; i<ftTracks.size();i++) {
            	if(ftTracks.get(i).get_trackCharge()==1) {
            		h1.fill(ftTracks.get(i).get_trackEnergy());
            		h2.fill(ftTracks.get(i).get_trackEnergy()-gen.getParticle("[11]").vector().p());
            		h3.fill(ftTracks.get(i).get_trackTheta()-gen.getParticle("[11]").theta()*180/Math.PI);
            		h4.fill(ftTracks.get(i).get_trackPhi()-gen.getParticle("[11]").phi()*180/Math.PI);
            		h5.fill(ftTracks.get(i).get_trackTime()-124.25);
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