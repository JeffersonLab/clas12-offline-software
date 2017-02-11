package org.jlab.service.ft;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.rec.ft.cal.FTCALReconstruction;


public class FTCALRecEngine extends ReconstructionEngine {

	public FTCALRecEngine() {
		super("FTCAL", "devita", "3.0");
	}

	FTCALReconstruction reco;
	int Run = -1;
	FTRecConfig config;
	
	@Override
	public boolean init() {
		config = new FTRecConfig();
		reco = new FTCALReconstruction();
		reco.debugMode=0;
		return true;
	}

	@Override
	public boolean processDataEvent(DataEvent event) {
		Run = config.setRunConditionsParameters(event, "FTCAL", Run);
		reco.processEvent((EvioDataEvent) event);
		return true;
	}

	
	
}
