package org.jlab.rec.cvt.services;

import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.bmt.CCDBConstantsLoader;
import org.jlab.rec.cvt.trajectory.TrkSwimmer;

public class CVTRecConfig {

	public CVTRecConfig() {
		// TODO Auto-generated constructor stub
	}
	private int Run;
	private String FieldsConfig;
	
	public void setRunConditionsParameters(DataEvent event, String FieldsConfig, int iRun, boolean addMisAlignmts, String misAlgnFile) {
		if(event.hasBank("RUN::config")==false) {
			System.err.println("RUN CONDITIONS NOT READ!");
			return;
		}
		
		int Run = iRun;
		
		boolean isMC = false;
		boolean isCosmics = false;
		DataBank bank = event.getBank("RUN::config");
		//System.out.println(bank.getInt("Event")[0]);
		if(bank.getByte("type",0)==0)
			isMC = true;
		if(bank.getByte("mode",0)==1)
			isCosmics = true;
		
		boolean isSVTonly = false;
		
		// Load the fields
		//-----------------
		String newConfig = "SOLENOID"+bank.getFloat("solenoid",0);		
		
		if (FieldsConfig.equals(newConfig)==false) {
			// Load the Constants
			System.out.println("  CHECK CONFIGS..............................."+FieldsConfig+" = ? "+newConfig);
			Constants.Load(isCosmics, isSVTonly, (double)bank.getFloat("solenoid",0)); 
			// Load the Fields
			System.out.println("************************************************************SETTING FIELD SCALE *****************************************************");
			TrkSwimmer.setMagneticFieldScale(bank.getFloat("solenoid",0)); // something changed in the configuration
			this.setFieldsConfig(newConfig);
		}
		FieldsConfig = newConfig;
		
		// Load the constants
		//-------------------
		int newRun = bank.getInt("run", 0);
		
		if(Run!=newRun) {
			System.out.println(" ........................................ trying to connect to db ");
			CCDBConstantsLoader.Load(10);
			DatabaseConstantProvider cp = new DatabaseConstantProvider( 10, "default");
			String ccdbPath = "/geometry/cvt/svt/";
			cp.loadTable( ccdbPath +"svt");
			cp.loadTable( ccdbPath +"region");
			cp.loadTable( ccdbPath +"support");
			cp.loadTable( ccdbPath +"fiducial");
			//cp.loadTable( ccdbPath +"material");
			cp.loadTable( ccdbPath +"alignment");
			cp.disconnect();
			this.setRun(newRun);

			
		}
		Run = newRun;
		this.setRun(Run);
	}

	public  int getRun() {
		return Run;
	}

	public  void setRun(int run) {
		Run = run;
	}

	public String getFieldsConfig() {
		return FieldsConfig;
	}

	public void setFieldsConfig(String fieldsConfig) {
		FieldsConfig = fieldsConfig;
	}

	
}
