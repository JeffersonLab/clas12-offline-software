package org.jlab.service.ft;

import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.rec.ft.cal.FTCALConstantsLoader;
import org.jlab.rec.ft.hodo.FTHODOConstantsLoader;

public class FTRecConfig {

	public FTRecConfig() {
		// TODO Auto-generated constructor stub
	}
	private int Run;
	
	public int setRunConditionsParameters(DataEvent event, String Detector, int iRun) {
		if(event.hasBank("RUN::config")==false) {
			System.err.println("RUN CONDITIONS NOT READ!");
			return -1;
		}
		
		int Run = iRun;
		
		boolean isMC = false;
		boolean isCosmics = false;
		EvioDataBank bank = (EvioDataBank) event.getBank("RUN::config");
        
		if(bank.getByte("Type")[0]==0)
			isMC = true;
		if(bank.getByte("Mode")[0]==1)
			isCosmics = true;
		
		
		// Load the constants
		//-------------------
		int newRun = bank.getInt("Run")[0];
		
		if(Run!=newRun) {
			if(Detector.equals("FTCAL") ) 
				FTCALConstantsLoader.Load(newRun); 
			if(Detector.equals("FTHODO") ) 
				FTHODOConstantsLoader.Load(newRun); 
		}
		Run = newRun;
		this.setRun(Run);
		return Run;
	}

	public  int getRun() {
		return Run;
	}

	public  void setRun(int run) {
		Run = run;
	}

	
}
