package org.jlab.rec.cvt.services;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.detector.geant4.v2.CTOFGeant4Factory;
import org.jlab.detector.geant4.v2.FTOFGeant4Factory;
import org.jlab.detector.geant4.v2.SVT.SVTConstants;
import org.jlab.detector.geant4.v2.SVT.SVTStripFactory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.base.Detector;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.banks.RecoBankWriter;
import org.jlab.rec.cvt.bmt.CCDBConstantsLoader;
import org.jlab.rec.cvt.track.StraightTrack;
import org.jlab.rec.cvt.track.Track;
import org.jlab.rec.cvt.track.Track;
import org.jlab.utils.groups.IndexedTable;
//import org.jlab.service.eb.EBHBEngine;
//import org.jlab.service.eb.EBTBEngine;

/**
 * Service to return reconstructed BST track candidates- the output is in Evio
 * format
 *
 * @author ziegler
 *
 */
public class CVTReconstruction extends ReconstructionEngine {

    org.jlab.rec.cvt.svt.Geometry SVTGeom;
    org.jlab.rec.cvt.bmt.Geometry BMTGeom;
    CTOFGeant4Factory CTOFGeom;
    Detector          CNDGeom ;
    SVTStripFactory svtIdealStripFactory;
    
    public CVTReconstruction() {
        super("CVTTracks", "ziegler", "4.0");
        org.jlab.rec.cvt.svt.Constants.Load();
        SVTGeom = new org.jlab.rec.cvt.svt.Geometry();
        BMTGeom = new org.jlab.rec.cvt.bmt.Geometry();

    }

    String FieldsConfig = "";
    int Run = -1;
  
    public void setRunConditionsParameters(DataEvent event, String Fields, int iRun, boolean addMisAlignmts, String misAlgnFile) {
        if (event.hasBank("RUN::config") == false) {
            System.err.println("RUN CONDITIONS NOT READ!");
            return;
        }

        int Run = iRun;

        boolean isMC = false;
        boolean isCosmics = false;
        DataBank bank = event.getBank("RUN::config");
        //System.out.println(bank.getInt("Event")[0]);
        if (bank.getByte("type", 0) == 0) {
            isMC = true;
        }
        if (bank.getByte("mode", 0) == 1) {
            isCosmics = true;
        }

        boolean isSVTonly = false;

        // Load the fields
        //-----------------
        String newConfig = "SOLENOID" + bank.getFloat("solenoid", 0);

        if (Fields.equals(newConfig) == false) {
            // Load the Constants
            
            System.out.println("  CHECK CONFIGS..............................." + FieldsConfig + " = ? " + newConfig);
            Constants.Load(isCosmics, isSVTonly, (double) bank.getFloat("solenoid", 0));
            // Load the Fields
            //System.out.println("************************************************************SETTING FIELD SCALE *****************************************************");
            //TrkSwimmer.setMagneticFieldScale(bank.getFloat("solenoid", 0)); // something changed in the configuration
            //double shift =0;
            //if(bank.getInt("run", 0)>1840)
            //    shift = -1.9;
            //MagneticFields.getInstance().setSolenoidShift(shift);
//            this.setFieldsConfig(newConfig);
            
//CCDBConstantsLoader.Load(new DatabaseConstantProvider(bank.getInt("run", 0), "default"));
        }
        this.setFieldsConfig(newConfig);

        // Load the constants
        //-------------------
        int newRun = bank.getInt("run", 0);

        if (Run != newRun) {
            this.setRun(newRun);
            this.loadConstants( bank.getInt("run",0) );
        }

        Run = newRun;
        this.setRun(Run);
    }

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
		
        CVTRecHandler recHandler = new CVTRecHandler(SVTGeom,BMTGeom,CTOFGeom,CNDGeom);
        setRunConditionsParameters(event, this.getFieldsConfig(), this.getRun(), false, "");            
        double shift = org.jlab.rec.cvt.Constants.getZoffset();
        
        Swim swimmer = new Swim();
        
        RecoBankWriter rbc = new RecoBankWriter();

        if( recHandler.loadClusters( event ) == false ) { return true; };
     
        // skip  high busy events 
        if( recHandler.getSVThits().size() > 700 ) return true; 
 
        recHandler.loadCrosses();

        // skip  high busy events with more than 1k crosses in svt 
        if( recHandler.getCrosses().get(0).size() > 1000 ) { return true; } 

        //System.out.println(" Number of crosses "+crosses.get(0).size()+" + "+crosses.get(1).size());
        if(Constants.isCosmicsData()==true) { 
            List<StraightTrack> cosmics = recHandler.cosmicsTracking();   
        	rbc.appendCVTCosmicsBanks(event, recHandler.getSVThits(), recHandler.getBMThits(), 
        					 recHandler.getSVTclusters(), recHandler.getBMTclusters(), 
        					 recHandler.getCrosses(), cosmics, shift);
        } 
        else {
            List<Track> trks = recHandler.beamTracking(swimmer);   
        	rbc.appendCVTBanks(event, recHandler.getSVThits(), recHandler.getBMThits(), 
        				  recHandler.getSVTclusters(), recHandler.getBMTclusters(), 
        				  recHandler.getCrosses(), trks, shift);
        }
        return true;
    }
    
    public boolean loadConstants( int run ) {
        System.out.println(" ........................................ trying to connect to db ");
//        CCDBConstantsLoader.Load(new DatabaseConstantProvider( "sqlite:///clas12.sqlite", "default"));
        // Load the calibration constants
        String variationName = Optional.ofNullable(this.getEngineConfigString("variation")).orElse("default");
        CCDBConstantsLoader.Load(new DatabaseConstantProvider(run, variationName));
               
        DatabaseConstantProvider cp = new DatabaseConstantProvider(run, variationName);
//        DatabaseConstantProvider cp = new DatabaseConstantProvider( "sqlite:///clas12.sqlite", "default");
        cp = SVTConstants.connect( cp );
        SVTConstants.loadAlignmentShifts( cp );
        cp.disconnect();    
        this.setSVTDB(cp);       
               
        //TrkSwimmer.getMagneticFields();
        return true;
    }

    public boolean init() {
        //System.out.println(" ........................................ trying to connect to db ");
////        CCDBConstantsLoader.Load(new DatabaseConstantProvider( "sqlite:///clas12.sqlite", "default"));
        //CCDBConstantsLoader.Load(new DatabaseConstantProvider(10, "default"));
               
        //DatabaseConstantProvider cp = new DatabaseConstantProvider(11, "default");
////        DatabaseConstantProvider cp = new DatabaseConstantProvider( "sqlite:///clas12.sqlite", "default");
        //cp = SVTConstants.connect( cp );
        //SVTConstants.loadAlignmentShifts( cp );
        //cp.disconnect();    
        //this.setSVTDB(cp);
        
        // Load other geometries
        String variationName = Optional.ofNullable(this.getEngineConfigString("variation")).orElse("default");
        ConstantProvider providerCTOF = GeometryFactory.getConstants(DetectorType.CTOF, 11, variationName);
        CTOFGeom = new CTOFGeant4Factory(providerCTOF);        
        CNDGeom =  GeometryFactory.getDetector(DetectorType.CND, 11, variationName);
        
        //TrkSwimmer.getMagneticFields();
        return true;
    }
    private DatabaseConstantProvider _SVTDB;
    private synchronized void setSVTDB(DatabaseConstantProvider SVTDB) {
        _SVTDB = SVTDB;
    }
    private synchronized DatabaseConstantProvider getSVTDB() {
        return _SVTDB;
    }

    
    public static void main(String[] args)  {
    /*
       String inputFile = "/Users/ziegler/Desktop/Work/Files/Data/ENG/central_2348_uncookedSkim.hipo";
        //String inputFile = "/Users/ziegler/Desktop/Work/Files/Data/skim_clas_002436.evio.90.hipo";
//String inputFile="/Users/ziegler/Desktop/Work/Files/LumiRuns/random/decoded_2341.hipo";
        System.err.println(" \n[PROCESSING FILE] : " + inputFile);

        CVTReconstruction en = new CVTReconstruction();
        en.init();
       //EBHBEngine eb = new EBHBEngine();
       //eb.init();
        int counter = 0;

        HipoDataSource reader = new HipoDataSource();
        reader.open(inputFile);

        HipoDataSync writer = new HipoDataSync();
        //Writer
        //String outputFile = "/Users/ziegler/Desktop/Work/Files/Data/ENG/central_2348_cookedSkim.hipo";
        String outputFile = "/Users/ziegler/Desktop/Work/Files/Data/recook_clas_002436.evio.90.hipo";
        writer.open(outputFile);

        long t1 = 0;
        while (reader.hasEvent()) {
            

            DataEvent event = reader.getNextEvent();
            System.out.println("  EVENT " + event.getBank("RUN::config").getInt("event",0)+" count "+counter);
            
            if (counter > 0) {
                t1 = System.currentTimeMillis();
            }
            //event.show();
            // Processing    
            en.processDataEvent(event);
            //eb.processDataEvent(event);
            
            if(event.hasBank("CVTRec::Tracks")) {
            
                writer.writeEvent(event); 
            }
            counter ++;
            
            if(counter>100000)
                break;
            //if(event.getBank("RUN::config").getInt("event",0)>=2000) break;
            //event.show();
            //if(counter%100==0)
            //System.out.println("run "+counter+" events");

        }
        writer.close();
        double t = System.currentTimeMillis() - t1;
        //System.out.println(t1 + " TOTAL  PROCESSING TIME = " + (t / (float) counter));
        */
        HipoDataSource reader = new HipoDataSource();
        reader.open("/home/fbossu/Data/Tracking/sim/test/gen_cvt1.hipo");
        DataEvent testEvent = reader.gotoEvent(2);
//        DataEvent testEvent = getCVTTestEvent();

        CVTReconstruction CVTengine = new CVTReconstruction();
        CVTengine.init();
        CVTengine.processDataEvent(testEvent);
        testEvent.show();
        if(testEvent.hasBank("CVTRec::Tracks")) {
            testEvent.getBank("CVTRec::Tracks").show();
        }
        
       /*
        EBHBEngine EBHBengine = new EBHBEngine();
        EBHBengine.init();
        EBHBengine.processDataEvent(testEvent);

        EBTBEngine EBTBengine = new EBTBEngine();
        EBTBengine.init();
        EBTBengine.processDataEvent(testEvent);

        System.out.println(isWithinXPercent(10.0, testEvent.getBank("REC::Particle").getFloat("px", 0), -0.375)+" "+
		isWithinXPercent(10.0, testEvent.getBank("REC::Particle").getFloat("py", 0), 0.483)
		+" "+isWithinXPercent(10.0, testEvent.getBank("REC::Particle").getFloat("pz", 0), 0.674)
		+" "+isWithinXPercent(30.0, testEvent.getBank("REC::Particle").getFloat("vz", 0), -13.9));
        */
        
    }
    public static boolean isWithinXPercent(double X, double val, double standard) {
        if(standard >= 0 && val > (1.0 - (X/100.0))*standard && val < (1.0 + (X/100.0))*standard) return true;
        else if(standard < 0 && val < (1.0 - (X/100.0))*standard && val > (1.0 + (X/100.0))*standard) return true;
        return false;
    }
    public static HipoDataEvent getCVTTestEvent() {
		HipoDataSync writer = new HipoDataSync();
		HipoDataEvent testEvent = (HipoDataEvent) writer.createEvent();
		DataBank config = testEvent.createBank("RUN::config", 1);
		DataBank SVTadc = testEvent.createBank("BST::adc", 8);
		DataBank mc = testEvent.createBank("MC::Particle", 1);
		// this event is based on a gemc (4a.1.1 aka 4a.2.0) event with
		// torus = -1.0 , solenoid = 1.0
		//	<option name="BEAM_P"   value="proton, 0.91*GeV, 42.2*deg, 127.8*deg"/>
		// <option name="SPREAD_P" value="0*GeV, 0*deg, 0*deg"/>
		// <option name="BEAM_V" value="(0, 0, -1.39)cm"/>
		// <option name="SPREAD_V" value="(0.0, 0.0)cm"/>

		config.setInt("run", 0, (int) 11);
		config.setInt("event", 0, (int) 1);
		config.setInt("trigger", 0, (int) 0);
		config.setLong("timestamp", 0, (long) 0);
		config.setByte("type", 0, (byte) 0);
		config.setByte("mode", 0, (byte) 0);
		config.setFloat("torus", 0, (float) -1.0);
		config.setFloat("solenoid", 0, (float) 1.0);
//		config.setFloat("rf", 0, (float) 0.0);
//		config.setFloat("startTime", 0, (float) 0.0);
		
		for(int i = 0; i < 8; i++) {
			SVTadc.setByte("order", i, (byte) 0);
			SVTadc.setShort("ped", i, (short) 0);
			SVTadc.setLong("timestamp", i, (long) 0);
		}

		SVTadc.setByte("sector", 0, (byte) 5);
		SVTadc.setByte("sector", 1, (byte) 5);
                SVTadc.setByte("sector", 7, (byte) 5);//
		SVTadc.setByte("sector", 2, (byte) 7);
		SVTadc.setByte("sector", 3, (byte) 7);
		SVTadc.setByte("sector", 4, (byte) 7);
		SVTadc.setByte("sector", 5, (byte) 9);
		SVTadc.setByte("sector", 6, (byte) 9);
		
		SVTadc.setByte("layer", 0, (byte) 1);
		SVTadc.setByte("layer", 1, (byte) 2);
                SVTadc.setByte("layer", 7, (byte) 2);//
		SVTadc.setByte("layer", 2, (byte) 3);
		SVTadc.setByte("layer", 3, (byte) 4);
		SVTadc.setByte("layer", 4, (byte) 4);
		SVTadc.setByte("layer", 5, (byte) 5);
		SVTadc.setByte("layer", 6, (byte) 6);
		
		SVTadc.setShort("component", 0, (short) 109);
		SVTadc.setShort("component", 1, (short) 77);
                SVTadc.setShort("component", 7, (short) 80);//
		SVTadc.setShort("component", 2, (short) 52);
		SVTadc.setShort("component", 3, (short) 137);
		SVTadc.setShort("component", 4, (short) 138);
		SVTadc.setShort("component", 5, (short) 1);
		SVTadc.setShort("component", 6, (short) 190);
		
		SVTadc.setInt("ADC", 0, (int) 7);
		SVTadc.setInt("ADC", 1, (int) 7);
                SVTadc.setInt("ADC", 7, (int) 6); //
		SVTadc.setInt("ADC", 2, (int) 7);
		SVTadc.setInt("ADC", 3, (int) 5);
		SVTadc.setInt("ADC", 4, (int) 5);
		SVTadc.setInt("ADC", 5, (int) 7);
		SVTadc.setInt("ADC", 6, (int) 7);
		
		SVTadc.setFloat("time", 0, (float) 97.0);
		SVTadc.setFloat("time", 1, (float) 201.0);
                SVTadc.setFloat("time", 7, (float) 201.0);//
		SVTadc.setFloat("time", 2, (float) 78.0);
		SVTadc.setFloat("time", 3, (float) 102.0);
		SVTadc.setFloat("time", 4, (float) 81.0);
		SVTadc.setFloat("time", 5, (float) 91.0);
		SVTadc.setFloat("time", 6, (float) 205.0);

		testEvent.appendBank(config);
                testEvent.appendBank(mc);
		testEvent.appendBank(SVTadc);
                
		return testEvent;
                
	}

}
