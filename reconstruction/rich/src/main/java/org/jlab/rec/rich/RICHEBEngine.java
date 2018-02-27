package org.jlab.rec.rich;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import org.jlab.clas.detector.DetectorData;
import org.jlab.clas.detector.DetectorEvent;

import org.jlab.clas.physics.GenericKinematicFitter;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.geom.prim.Vector3D;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataSource;

public class RICHEBEngine extends ReconstructionEngine {

    RICHEventBuilder reco;
    int Run = -1;
    int debugMode=0;
    
    // ----------------
    public RICHEBEngine() {
    // ----------------
        super("RICHEB", "mcontalb-kenjo", "3.0");

    }

    @Override
    // ----------------
    public boolean init() {
    // ----------------
        //config = new FTConfig();

	if(debugMode>=1){
	    System.out.print("RICH Engine Initialization");
	}
        reco = new RICHEventBuilder();
	reco.init();
        reco.debugMode=0;

        return true;

    }


    @Override
    // ----------------
    public boolean processDataEvent(DataEvent event) {
    // ----------------

	if(debugMode>=1){
	    System.out.print("RICH Engine: Event Process");
	}

	reco.ProcessRawPMTData(event);

	/*
        List<FTParticle> FTparticles = new ArrayList<FTParticle>();
        List<FTResponse> FTresponses = new ArrayList<FTResponse>();

        //Run = config.setRunConditionsParameters(event, "FTEB", Run);
        reco.init(config.getSolenoid());
        FTresponses = reco.addResponses(event);
        FTparticles = reco.initFTparticles(FTresponses);
        reco.matchToHODO(FTresponses, FTparticles);
        reco.writeBanks(event, FTparticles);
	*/
        return true;

    }
    
}
