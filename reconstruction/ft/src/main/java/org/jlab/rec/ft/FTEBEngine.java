package org.jlab.rec.ft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import org.jlab.clas.detector.DetectorData;
import org.jlab.clas.detector.DetectorEvent;

import org.jlab.clas.physics.GenericKinematicFitter;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.detector.base.DetectorLayer;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.data.DataLine;
import org.jlab.groot.math.F1D;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.rec.ft.cal.FTCALConstantsLoader;
import org.jlab.rec.ft.cal.FTCALEngine;
import org.jlab.rec.ft.hodo.FTHODOEngine;
import org.jlab.rec.ft.trk.FTTRKEngine;
import org.jlab.rec.ft.trk.FTTRKConstantsLoader;
import org.jlab.rec.ft.trk.FTTRKReconstruction;

public class FTEBEngine extends ReconstructionEngine {

    FTEventBuilder reco;
    int Run = -1;
    double Solenoid;
    
    public FTEBEngine() {
        super("FTEB", "devita", "3.0");
    }

    @Override
    public boolean init() {
        reco = new FTEventBuilder();
        reco.debugMode = 0;
        String[] tables = new String[]{
            "/calibration/ft/ftcal/cluster",
            "/calibration/ft/ftcal/thetacorr",
            "/calibration/ft/ftcal/phicorr",
            "/geometry/target"
        };
        requireConstants(Arrays.asList(tables));
        this.getConstantsManager().setVariation("default");

        this.registerOutputBank("FT::particles");

        return true;
    }

    @Override
    public boolean processDataEvent(DataEvent event) {
        List<FTParticle> FTparticles = new ArrayList<FTParticle>();
        List<FTResponse> FTresponses = new ArrayList<FTResponse>();

        int run = this.setRunConditionsParameters(event);
        if (run>=0) {
            reco.init(this.getSolenoid());
            FTresponses = reco.addResponses(event, this.getConstantsManager(), run);

            FTparticles = reco.initFTparticles(FTresponses, this.getConstantsManager(), run);
            if(FTparticles.size()>0){
                reco.matchToTRKTwoDetectorsMultiHits(FTresponses, FTparticles);
                reco.matchToHODO(FTresponses, FTparticles);
                reco.correctDirection(FTparticles, this.getConstantsManager(), run);
                reco.writeBanks(event, FTparticles);
            }
        }
        return true;
    }

    public int setRunConditionsParameters(DataEvent event) {
        int run = -1;
        if (event.hasBank("RUN::config") == false) {
            System.err.println("RUN CONDITIONS NOT READ!");
        }
        else {
            double fieldScale = 0;

            boolean isMC = false;
            boolean isCosmics = false;

            if (event instanceof EvioDataEvent) {
                EvioDataBank bank = (EvioDataBank) event.getBank("RUN::config");
                if (bank.getByte("Type",0) == 0) {
                    isMC = true;
                }
                if (bank.getByte("Mode",0)== 1) {
                    isCosmics = true;
                }
                run = bank.getInt("Run",0);
                fieldScale = bank.getFloat("Solenoid")[0];
            } else {
                DataBank bank = event.getBank("RUN::config");
                if (bank.getByte("type",0) == 0) {
                    isMC = true;
                }
                if (bank.getByte("mode",0)== 1) {
                    isCosmics = true;
                }
                run = bank.getInt("run",0);
                fieldScale = bank.getFloat("solenoid",0);
            }
            this.setSolenoid(fieldScale);
        }
        return run;
    }

    public int getRun() {
        return Run;
    }

    public void setRun(int run) {
        Run = run;
    }

    public double getSolenoid() {
        return Solenoid;
    }

    public void setSolenoid(double Solenoid) {
        this.Solenoid = Solenoid;
    }
    
    public int getDebugMode() {
        return this.reco.debugMode;
    }
    
    
    public static void main(String arg[]){
       
        FTCALEngine cal = new FTCALEngine();
        cal.init();
        FTHODOEngine hodo = new FTHODOEngine();
        hodo.init();
	FTTRKEngine trk = new FTTRKEngine();
	trk.init();
        FTEBEngine en = new FTEBEngine();
        en.init();
        int debugMode = en.getDebugMode();
        String input = "/home/filippi/clas12/fttrkDev/clas12-offline-software-6.5.13-fttrkDev/filter_005418_newbanks.hipo";
        HipoDataSource reader = new HipoDataSource();
        reader.open(input);

        // initialize histos
        H1F h1 = new H1F("Cluster Energy", 100, 0., 8.);
        h1.setOptStat(Integer.parseInt("11111"));
        h1.setTitleX("Cluster Energy (GeV)");
        H1F h2 = new H1F("Energy Resolution", 100, -1, 1);
        h2.setOptStat(Integer.parseInt("11111"));
        h2.setTitleX("Energy Resolution(GeV)");
        H1F h3 = new H1F("Theta Resolution", 100, -1, 1);
        h3.setOptStat(Integer.parseInt("11111"));
        h3.setTitleX("Theta Resolution(deg)");
        H1F h4 = new H1F("Phi Resolution", 100, -10, 10);
        h4.setOptStat(Integer.parseInt("11111"));
        h4.setTitleX("Phi Resolution(deg)");

        H1F h5 = new H1F("Time Resolution", 100, -2, 2);
        h5.setOptStat(Integer.parseInt("1111"));
        h5.setTitleX("Time Resolution(ns)");
        H2F h6 = new H2F("Cluster Energy", 24, -180., 180., 24, -180., 180.);
        h6.setTitleX("x (cm)");
        h6.setTitleY("y (cm)");
        H2F h7 = new H2F("N. Clusters", 24, -18., 18., 24, -18., 18.);
        h7.setTitleX("x (mm)");
        h7.setTitleY("y (mm)");
        H2F h8 = new H2F("Cluster Energy", 100, 0., 9., 100, -0.5, 0.5);
        h8.setTitleX("E (GeV)");
        h8.setTitleY("Energy Resolution(GeV)");
        H2F h9 = new H2F("Cluster Energy", 100, 2., 5., 100, -0.5, 0.5);
        h9.setTitleX("#theta");
        h9.setTitleY("Energy Resolution(GeV)");
        H2F h10 = new H2F("Cluster Energy", 100, -180., 180., 100, -0.5, 0.5);
        h10.setTitleX("#phi");
        h10.setTitleY("Energy Resolution(GeV)");
      
        // residual plots (of TRK wrt tracking on ECAL - black histograms)
        H2F h100 = new H2F("Cross on trk layer1", 100, -18., 18., 100, -18., 18.);
        h100.setTitleX("x (mm) trk1");
        h100.setTitleY("y (mm) trk1");
        H2F h101 = new H2F("Cross on trk layer2", 100, -18., 18., 100, -18., 18.);
        h101.setTitleX("x (mm) trk2");
        h101.setTitleY("y (mm) trk2");
        H2F h700 = new H2F("Strips on trk layer1", 100, -18., 18., 100, -18., 18.);
        h100.setTitleX("x (mm) trk1");
        h100.setTitleY("y (mm) trk1");
        H2F h701 = new H2F("Strips on trk layer2", 100, -18., 18., 100, -18., 18.);
        h101.setTitleX("x (mm) trk2");
        h101.setTitleY("y (mm) trk2");
        
        double limTC = 4.;      // for MC 0.05 is ok
        H1F h102 = new H1F("trk1 x residual", 63, -limTC, limTC);
        h102.setOptStat(10);
        h102.setTitleX("trk1 x residual (mm)");
        h102.setFillColor(1);
        H1F h103 = new H1F("trk2 x residual", 63, -limTC, limTC);
        h103.setOptStat(10);
        h103.setFillColor(51);
        h103.setTitleX("trk2 x residual (mm)");
        H1F h104 = new H1F("trk1 y residual", 63, -limTC, limTC);
        h104.setOptStat(Integer.parseInt("0"));
        h104.setFillColor(1);
        h104.setTitleX("trk1 y residual (mm)");
        H1F h105 = new H1F("trk2 y residual;", 63, -limTC, limTC);
        h105.setOptStat(10);
        h105.setTitleX("trk2 y residual (mm)");
        h105.setFillColor(51);
        double limTCTheta = 0.05;
        double limTCPhi = 1.;
        H1F h106 = new H1F("trk1 theta residual (rad)", 100, -limTCTheta, limTCTheta);
        h106.setOptStat(10);
        h106.setTitleX("trk1 theta residual (rad)");
        h106.setFillColor(1);
        H1F h107 = new H1F("trk2 theta residual (rad)", 100, -limTCTheta, limTCTheta);
        h107.setOptStat(Integer.parseInt("0"));
        h107.setTitleX("trk2 theta residual (rad)");
        h107.setFillColor(51);
        H1F h108 = new H1F("trk1 phi residual (rad)", 100, -limTCPhi, limTCPhi);
        h108.setOptStat(0);
        h108.setTitleX("trk1 phi residual");
        h108.setFillColor(1);
        H1F h109 = new H1F("trk2 phi residual (rad)", 100, -limTCPhi, limTCPhi);
        h109.setOptStat(10);
        h109.setTitleX("trk2 phi residual (rad)");
        h109.setFillColor(51);
        
        H1F h202 = new H1F("trk1 x", 25, 8.2, 9.0);
        H1F h1202 = new H1F("trk1 x MC", 25, 8.2, 9.0);      
        h202.setOptStat(0);
        h202.setTitleX("trk1 x position (mm)");
        h202.setTitleY("counts/strip width/sqrt(12) (16 um)"); // 25 bins
        h202.setLineColor(3);
        h202.setFillColor(3);
        h1202.setLineColor(9);
        h1202.setFillColor(49);
        H1F h203 = new H1F("trk2 x", 25, 8.2, 9.0);
        H1F h1203 = new H1F("trk2 MC", 25, 8.2, 9.0);  
        h203.setOptStat(0);
        h203.setTitleX("trk2 x position (mm)");
        h203.setTitleY("counts/strip width/sqrt(12) (16 um)"); // 25 bins
        h203.setLineColor(3);
        h203.setFillColor(3);
        h1203.setLineColor(9);
        h1203.setFillColor(49);
        H1F h204 = new H1F("trk1 y", 25, 2.4, 4.0);
        H1F h1204 = new H1F("trk1 y MC", 25, 2.4, 4.0);  
        h204.setOptStat(0);
        h204.setTitleX("trk1 y position (mm)");
        h204.setTitleY("counts/strip width/sqrt(12) (16 um)"); // 25 bins
        h204.setLineColor(3);
        h204.setFillColor(3);
        h1204.setLineColor(9);
        h1204.setFillColor(49);
        H1F h205 = new H1F("trk2 y", 25, 2.4, 4.0);
        H1F h1205 = new H1F("trk2 y MC", 25, 2.4, 4.0);
        h205.setOptStat(0);
        h205.setTitleX("trk2 y position (mm)");
        h205.setTitleY("counts/strip width/sqrt(12) (16 um)"); // 25 bins
        h205.setLineColor(3);
        h205.setFillColor(3);
        h1205.setLineColor(9);
        h1205.setFillColor(49);
        
        // resolution plots (of TRK wrt MC truth - red histograms)
        H2F h1100 = new H2F("Cross on trk layer1 MC truth", 100, -18., 18., 100, -18., 18.);
        h1100.setTitleX("x (mm) trk1");
        h1100.setTitleY("y (mm) trk1");
        H2F h1101 = new H2F("Cross on trk layer2 MC truth", 100, -18., 18., 100, -18., 18.);
        h1101.setTitleX("x (mm) trk2");
        h1101.setTitleY("y (mm) trk2");
        int binres = 35;
        double reslim = 5.*FTTRKConstantsLoader.Pitch;
        H1F h1102 = new H1F("trk1 x resolution", binres, -reslim, reslim);
        h1102.setOptStat(0);
        h1102.setTitleX("trk1 x resolutionl (mm)");
        h1102.setLineColor(2);
        h1102.setFillColor(2);
        H1F h1103 = new H1F("trk2 x resolution", binres, -reslim, reslim);
        h1103.setOptStat(0);
        h1103.setTitleX("trk2 x resolution (mm)");
        h1103.setLineColor(2);
        h1103.setFillColor(32);
        H1F h1104 = new H1F("trk1 y resolution", binres, -reslim, reslim);
        h1104.setOptStat(0);
        h1104.setTitleX("trk1 y resolution (mm)");
        h1104.setLineColor(2);
        h1104.setFillColor(2);
        H1F h1105 = new H1F("trk2 y resolution", binres, -reslim, reslim);
        h1105.setOptStat(0);
        h1105.setTitleX("trk2 y resolution (mm)");
        h1105.setLineColor(2);
        h1105.setFillColor(32);
        H1F h1106 = new H1F("trk1 theta resolution", 50, -0.001, 0.001);
        h1106.setOptStat(0);
        h1106.setTitleX("trk1 theta resolution (rad)");
        h1106.setLineColor(2);
        h1106.setFillColor(27);
        H1F h1107 = new H1F("trk2 theta resolution", 50, -0.001, 0.001);
        h1107.setOptStat(0);
        h1107.setTitleX("trk2 theta resolution (rad)");
        h1107.setLineColor(2);
        h1107.setFillColor(37);
        H1F h1108 = new H1F("trk1 phi resolution", 50, -0.02, 0.02);
        h1108.setOptStat(0);
        h1108.setTitleX("trk1 phi resolution (rad)");
        h1108.setLineColor(2);
        h1108.setFillColor(2);
        H1F h1109 = new H1F("trk2 phi resolution", 50, -0.02, 0.02);
        h1109.setOptStat(0);
        h1109.setTitleX("trk2 phi resolution (rad)");
        h1109.setLineColor(2);
        h1109.setFillColor(32);
        
        H1F h1112 = new H1F("trk1 Delta x", 50, -0.05, 0.05);
        h1112.setOptStat(0);
        h1112.setTitleX("trk1 Delta x");
        h1112.setLineColor(6);
        h1112.setFillColor(6);
        H1F h1113 = new H1F("trk2 Delta x", 50, -0.05, 0.05);
        h1113.setOptStat(0);
        h1113.setTitleX("trk2 Delta x");
        h1113.setLineColor(6);
        h1113.setFillColor(36);
        H1F h1114 = new H1F("trk1 Delta y", 50, -0.1, 0.1);
        h1114.setOptStat(0);
        h1114.setTitleX("trk1 Delta y");
        h1114.setLineColor(6);
        h1114.setFillColor(6);
        H1F h1115 = new H1F("trk2 Delta y", 50, -0.1, 0.1);
        h1115.setOptStat(0);
        h1115.setTitleX("trk2 Delta y");
        h1115.setLineColor(6);
        h1115.setFillColor(36); 
        
        // Montecarlo radiography
        H2F h2000 = new H2F("Montecarlo radiography at first FTTRK det", 100, -15., 15., 100, -15, 15.);
        h2000.setTitleX("x (mm)");
        h2000.setTitleY("y (mm)");
        H2F h2001 = new H2F("Montecarlo radiography at second FTTRK det", 100, -15., 15., 100, -15, 15.);
        h2001.setTitleX("x (mm)");
        h2001.setTitleY("y (mm)");
        
        JFrame frameCALTRK = new JFrame("radiography FTCAL hits and FTTRK Crosses");
        frameCALTRK.setSize(1500,500);
        EmbeddedCanvas canvasCALTRK = new EmbeddedCanvas();
        canvasCALTRK.divide(3,1);
        
        double diffRadTolerance = FTConstants.TRK1_TRK2_RADTOL;
        double diffPhiTolerance = FTConstants.TRK1_TRK2_PHITOL; 
        double diffThetaTolerance = FTConstants.TRK1_TRK2_THETATOL;
        
        int nev = 0;
        int nevWithCrosses = 0, ncrosses2 = 0, nOfFTParticles = 0;
        int TRK1 = DetectorLayer.FTTRK_MODULE1 - 1;
        int TRK2 = DetectorLayer.FTTRK_MODULE2 - 1;
        while (reader.hasEvent()) {  // run over all events
//        int nev1 = 0; int nev2 = 10000; for(nev=nev1; nev<nev2; nev++){   // run on one event only
            DataEvent event = (DataEvent) reader.getNextEvent();
            DataBank bk;
            int bankEvt = -1;
            if(event.hasBank("RUN::config")) {
                nev++;
                bk = event.getBank("RUN::config");
                bankEvt = bk.getInt("event", 0);
            }else{
                continue;
            } 
            // bankEvt is the number used to extract the event with groovy
            // always print to keep track of running
            if(debugMode>-1) System.out.println("////////////// event read " + bankEvt + " - sequential number " + nev);
            //if(nev > 10239) System.exit(0); if(nev != 10239) continue; // stop at a given evt number
            cal.processDataEvent(event);
            hodo.processDataEvent(event);
	    trk.processDataEventAndGetClusters(event);
            en.processDataEvent(event);
            if(!event.hasBank("FTCAL::hits")) continue; 
            if (event instanceof EvioDataEvent) {
                GenericKinematicFitter fitter = new GenericKinematicFitter(11);
                PhysicsEvent gen = fitter.getGeneratedEvent((EvioDataEvent) event);
                if (event.hasBank("FTRec::tracks")) {
                    DataBank bank = event.getBank("FTRec::tracks");
                    int nrows = bank.rows();
                    for (int i = 0; i < nrows; i++) {
                        h1.fill(bank.getDouble("Energy", i));    
                        Vector3D part = new Vector3D(bank.getDouble("Cx", i), bank.getDouble("Cy", i), bank.getDouble("Cz", i));
                        h5.fill(bank.getDouble("Time", i));
                        if(gen.countGenerated() != 0){
                            h2.fill(bank.getDouble("Energy", i) - gen.getParticle("[11]").vector().p());
                            h3.fill(Math.toDegrees(part.theta() - gen.getParticle("[11]").theta()));
                            h4.fill(Math.toDegrees(part.phi() - gen.getParticle("[11]").phi()));
                            h6.fill(bank.getDouble("Energy", i), bank.getDouble("Energy", i) - gen.getParticle("[11]").vector().p());
                        }
                    }
                }
            } else {
                DetectorEvent detectorEvent = DetectorData.readDetectorEvent(event);
                PhysicsEvent gen = detectorEvent.getGeneratedEvent();
                     if (event.hasBank("FT::particles")) {
                    DataBank bank = event.getBank("FT::particles");
                    int nrows = bank.rows();
                    if(nrows>0) nOfFTParticles++;
                    for (int i = 0; i < nrows; i++) {
                        int calId = bank.getShort("calID",i);
                        if(bank.getByte("charge", i)==-1 && bank.getShort("calID",i)>0) { 
                            // track candidate through calorimeter
                            h1.fill(bank.getFloat("energy", i));
                            Vector3D part = new Vector3D(bank.getFloat("cx", i), bank.getFloat("cy", i), bank.getFloat("cz", i));
                            h5.fill(bank.getFloat("time", i) - 124.25);  // simulation  
                            h7.fill((bank.getFloat("cx", i) * FTCALConstantsLoader.CRYS_ZPOS)/10., (bank.getFloat("cy", i) * FTCALConstantsLoader.CRYS_ZPOS)/10.);
                            if(gen.countGenerated() != 0){
                                h2.fill(bank.getFloat("energy", i) - gen.getGeneratedParticle(0).vector().p()); 
                                h3.fill(gen.getGeneratedParticle(0).vector().p(),Math.toDegrees(part.theta() - gen.getGeneratedParticle(0).theta()));
                                h4.fill(gen.getGeneratedParticle(0).vector().p(),Math.toDegrees(part.phi() - gen.getGeneratedParticle(0).phi()));  
                                h6.fill(bank.getFloat("cx", i) * FTCALConstantsLoader.CRYS_ZPOS, bank.getFloat("cy", i) * FTCALConstantsLoader.CRYS_ZPOS, bank.getFloat("energy", i) - gen.getGeneratedParticle(0).vector().p());
                                h8.fill(gen.getGeneratedParticle(0).vector().p(), bank.getFloat("energy", i) - gen.getGeneratedParticle(0).vector().p());
                                h9.fill(Math.toDegrees(gen.getGeneratedParticle(0).theta()), bank.getFloat("energy", i) - gen.getGeneratedParticle(0).vector().p());
                                h10.fill(Math.toDegrees(gen.getGeneratedParticle(0).phi()), bank.getFloat("energy", i) - gen.getGeneratedParticle(0).vector().p());
                            }
                            
                            // check match with trk bank (entry trkID)
//                            int trkID = bank.getShort("trkID", i); 
                            int trk0ID = bank.getShort("trk0ID", i);   // it should correspond to the number of the cross in the banks
                            int trk1ID = bank.getShort("trk1ID", i);
                            if(trk0ID >= 0 || trk1ID >= 0){ // at least one cross is present on one TRK det
                                // loop on crosses bank
                                DataBank banktrk = event.getBank("FTTRK::crosses");
                                int ncrosses = banktrk.rows();
                                // how many matched crosses are associated to a given track?
                                int matchedCrosses = 0;
                                for(int nc=0; nc<ncrosses; nc++){
                                    int crossID = banktrk.getInt("id", nc);
                                    int det = banktrk.getInt("detector", nc);
                                    if(trk0ID == crossID && det==TRK1) matchedCrosses++;
                                    if(trk1ID == crossID && det==TRK2) matchedCrosses++;
                                }

//                              if(matchedCrosses != 1){
//                              if(matchedCrosses != 2){
                                if(matchedCrosses < FTConstants.TRK_MIN_CROSS_NUMBER){  // at least TRK_MIN_CROSS_NUMBER per event
                                    continue;
                                }else{
                                    ncrosses2++;
                                    if(ncrosses<100 && debugMode>0) System.out.println("++++++++++++++++++++++++++ Sequential number " + nev);
                                }
                                
                                for(int nc = 0; nc < ncrosses; nc++){
                                    int crossID = banktrk.getInt("id", nc);
                                    if(crossID != trk0ID && crossID != trk1ID) continue;
                                    int det =  banktrk.getInt("detector", nc);
                                    if(debugMode>0) System.out.println("trk0ID " + trk0ID + " trk1ID " + trk1ID + 
                                        " crossID " + crossID);
                                         
                                    if(det>=0){
                                        float xt = banktrk.getFloat("x", nc);
                                        float yt = banktrk.getFloat("y", nc);
                                        float zt = banktrk.getFloat("z", nc);
                                        Vector3D hitOnTrk = new Vector3D(xt, yt, zt);
                                        // extract information on the crossed strips 
                                        float icl1 = banktrk.getShort("Cluster1ID", nc);   
                                        float icl2 = banktrk.getShort("Cluster2ID", nc);   
                                        DataBank bankcl = event.getBank("FTTRK::clusters");
                                        // which is the correct location in the cluster bank for the cluster with the given id?
                                        int icl1ok = -1;
                                        int icl2ok = -1;
                                        for(int k=0; k<bankcl.rows(); k++){
                                            int cid = bankcl.getInt("id", (int) k);
                                            if((int)icl1 == cid) icl1ok = k;
                                            if((int)icl2 == cid) icl2ok = k;
                                        }  
                                        DataLine segment1 = new DataLine();
                                        DataLine segment2 = new DataLine();
                                        int seed1 = -1;
                                        int seed2 = -1;
                                        if(bankcl.rows()>0){
                                            segment1.setOrigin(0.,0.); segment1.setEnd(0.,0.);
                                            segment2.setOrigin(0.,0.); segment2.setEnd(0.,0.);
                                            seed1 = bankcl.getInt("seed", (int)icl1ok);
                                            seed2 = bankcl.getInt("seed", (int)icl2ok);
                                            int cent1 = (int)bankcl.getFloat("centroid", icl1ok);
                                            int cent2 = (int)bankcl.getFloat("centroid", icl2ok);
                                            // if the cluster is formed by >= 3 strips take the centroid
                                            int clustsize1 =  bankcl.getShort("size", icl1ok);
                                            int clustsize2 =  bankcl.getShort("size", icl2ok);
                                            if(clustsize1>=FTConstants.TRK_MIN_ClusterSizeForCentroid){
                                                int sector = FTTRKReconstruction.findSector(seed1);
                                                if(!(sector == 0 || sector == 1 || sector == 18 || sector == 19))  seed1 = cent1;
                                            } 
                                            if(clustsize2>=FTConstants.TRK_MIN_ClusterSizeForCentroid){
                                                int sector = FTTRKReconstruction.findSector(seed2);
                                                if(!(sector == 0 || sector == 1 || sector == 18 || sector == 19))  seed2 = cent2;
                                            }
                                            int lay1 = bankcl.getInt("layer", (int)icl1ok);
                                            int lay2 = bankcl.getInt("layer", (int)icl2ok);
                                            if(debugMode>0){
                                                System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ seed1, seed2 " + 
                                                        seed1 + " " + seed2 + " layer1 layer2 " + lay1 + " " + lay2);
                                                System.out.println("~~~~~~~~~~ hit strip 1: " + seed1 + " layer " + lay1 + " sector " + 
                                                        FTTRKReconstruction.findSector(seed1));
                                                System.out.println("~~~~~~~~~~ hit strip 2: " + seed2 + " layer " + lay2 + " sector " + 
                                                        FTTRKReconstruction.findSector(seed2));
                                            }
                                            if(seed1!=0 && seed2!=0){
                                                Line3D seg1 = (Line3D) FTTRKConstantsLoader.getStripSegmentLab(lay1, seed1);
                                                Line3D seg2 = (Line3D) FTTRKConstantsLoader.getStripSegmentLab(lay2, seed2);
                                                segment1.setOrigin(seg1.origin().x(), seg1.origin().y());
                                                segment2.setOrigin(seg2.origin().x(), seg2.origin().y());
                                                segment1.setEnd(seg1.end().x(), seg1.end().y());
                                                segment2.setEnd(seg2.end().x(), seg2.end().y());  
                                                  
                                            }    
                                        }
                                        
                                        if(det==TRK1 && trk0ID==crossID){
                                            h100.fill(xt, yt);
                                            if(debugMode>0){ 
                                                System.out.println("coordinates of track on fttrk " + bank.getFloat("cx", i)*zt  + " " +
                                                        bank.getFloat("cy", i)*zt  + " " + bank.getFloat("cz", i)*zt);
                                                System.out.println("director cosines hit on calorimeter " + part.x() + " " + part.y() + " " + part.z());
                                                System.out.println("director cosines hit of fttrk " + xt + " " + yt + " " + zt);
                                            }
                                            h102.fill(bank.getFloat("cx", i) *zt - xt);
                                            h104.fill(bank.getFloat("cy", i) *zt - yt);
                                            h106.fill((part.theta() - hitOnTrk.theta()));
                                            h108.fill((part.phi() - hitOnTrk.phi()));
                                            int sec1 = FTTRKReconstruction.findSector(seed1);
                                            int sec2 = FTTRKReconstruction.findSector(seed2);
                                            if(debugMode>0){
                                                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " +  sec1 + "-" + sec2 + 
                                                    " bad seeds d0, event " + nev);
                                            }
                                            canvasCALTRK.cd(0);
                                            segment1.setLineColor(1);
                                            segment2.setLineColor(2);
                                            canvasCALTRK.draw(segment1);
                                            canvasCALTRK.draw(segment2);
                                            
                                        }else if(det==TRK2 && trk1ID==crossID){
                                            h101.fill(xt, yt);
                                            h103.fill(bank.getFloat("cx", i) *zt - xt);
                                            h105.fill(bank.getFloat("cy", i) *zt - yt);
                                            h107.fill((part.theta() - hitOnTrk.theta()));
                                            h109.fill((part.phi() - hitOnTrk.phi()));
                                            int sec1 = FTTRKReconstruction.findSector(seed1);
                                            int sec2 = FTTRKReconstruction.findSector(seed2);
                                            if(debugMode>0){
                                                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " +  sec1 + "-" + sec2 + 
                                                    " bad seeds d1, event " + nev);
                                            }
                                            canvasCALTRK.cd(1);
                                            segment1.setLineColor(3);
                                            segment2.setLineColor(4);
                                            canvasCALTRK.draw(segment1);
                                            canvasCALTRK.draw(segment2);    
                                        }
                                                                                
                                    }
                                } 
                                       
                                if(ncrosses==2){
                                   float x0 = banktrk.getFloat("x", 0);
                                   float y0 = banktrk.getFloat("y", 0);
                                   float x1 = banktrk.getFloat("x", 1);
                                   float y1 = banktrk.getFloat("y", 1);
                                   
                                   Point3D c0 = new Point3D(x0, y0, banktrk.getFloat("z",0));
                                   Point3D c1 = new Point3D(x1, y1, banktrk.getFloat("z",1));
                                   Line3D lineBwCrosses = new Line3D(c1, c0);
                                   if(debugMode>0){
                                        System.out.println("x coordinates on 2 layers " + x0 + " " + x1);
                                        System.out.println("director cosines straight line bw crosses, cx " + lineBwCrosses.originDir().x() + " cy " + 
                                            lineBwCrosses.originDir().y() + " cz " + lineBwCrosses.originDir().z());
                                    }   
                                }
                            }
                        }
                    }
                }

                if(event.hasBank("FTTRK::crosses")){
                    nevWithCrosses++;
                    DataBank banktrk = event.getBank("FTTRK::crosses");
                    int nrows = banktrk.rows();
                    
                     // comparison with MC data
                    // how many particles have been generated?
                    int ipart = gen.countGenerated();                    
                    for(int ip=0; ip<ipart; ip++){
                    // no magnetic field: the track is a straight line
                        double mass = gen.getGeneratedParticle(ip).mass();
                        if(debugMode>0) System.out.println("------- generated particle n " + ip + " type: " + gen.getGeneratedParticle(ip).pid() + " mass " + mass); 
                        double P = gen.getGeneratedParticle(ip).p();   
                        double cx = gen.getGeneratedParticle(ip).px()/P;    
                        double cy = gen.getGeneratedParticle(ip).py()/P;
                        double cz = gen.getGeneratedParticle(ip).pz()/P;
                        double Pz = P*cz;
                        double Pperp = P*Math.sqrt(cx*cx+cy*cy);
                        double x0 = gen.getGeneratedParticle(ip).vx();
                        double y0 = gen.getGeneratedParticle(ip).vy();
                        double z0 = gen.getGeneratedParticle(ip).vz();
                        double q = Math.abs(gen.getGeneratedParticle(ip).charge());  // negative particles bend upwards
                        for (int i = 0; i < nrows; i++) {
                            int det =  banktrk.getInt("detector", i);
                            double xt = banktrk.getFloat("x", i);
                            double yt = banktrk.getFloat("y", i);
                            double zt = banktrk.getFloat("z", i);
                        
                            if(debugMode>0){
                                System.out.println("MC: x0 = " + x0 + " y0 " + y0 + " z0 " + z0);
                                System.out.println("MC cosines: cx = " + cx + " cy " + cy + " cz " + cz);
                                System.out.println("trk: xt = " + xt + " yt " + yt + " zt " + zt);
                            }
                            double t = (zt - z0)/cz;
                            Vector3D hitOnTrk = new Vector3D(xt, yt, zt);
                            Vector3D hitMCOnTrk = new Vector3D(x0+cx*t, y0+cy*t, zt);    // straight line  
                        
                            if(debugMode>0) System.out.println("MC on the straight line x = " + hitMCOnTrk.x() + " y = " + hitMCOnTrk.y() + 
                                    " z = " + hitMCOnTrk.z());
                            // if the magnetic field is on, the coordinates must be swum along a helix
                            double B = en.getSolenoid();
                            if(B!=0.){
                                // find the crossing point of the helix and a xy plane at given z, where the magneti field stops
                                double zStop = 100.0;       // PROVISIONAL fixed to 1 meter
                                double phi0 = Math.atan2(cy,cx);
                                double R = Pperp/0.3/B/q*1.e2;  // R in cm
                                double dAlpha = (zStop-z0)*(B*q*0.3)/Pz*1.e-2; // deltaZ in meters
                                int nturns = (int) Math.floor(dAlpha/2./Math.PI);       // number of full turns
                                if(dAlpha>0){
                                    dAlpha -= nturns*2.*Math.PI;
                                }else{
                                    dAlpha += nturns*2.*Math.PI;
                                }
                                
                                double xc =  R*Math.sin(phi0);
                                double yc =  R*Math.cos(phi0);
                                Vector3D hitOnPlane = new Vector3D();
                                Vector3D tangentAtZstop = new Vector3D();
                                if(R*dAlpha>0){
                                    hitOnPlane.setX(-xc + R*(Math.sin(phi0 + dAlpha)));
                                    hitOnPlane.setY( yc - R*(Math.cos(phi0 + dAlpha)));
                                    hitOnPlane.setZ(zStop);
                                    double gamma = Math.atan2(hitOnPlane.y(),hitOnPlane.x());
                                    double vx1 = Pperp/P*Math.sin(gamma);
                                    double vy1 = Pperp/P*Math.cos(gamma);
                                    double vz1 = Math.sqrt(1. - vx1*vx1 + vy1*vy1);
                                    double t1 = (zt - hitOnPlane.z())/vz1;
                                    hitMCOnTrk.setXYZ(hitOnPlane.x()+vx1*t1, hitOnPlane.y()+vy1*t1, zt);
                                }else{
                                    if(debugMode>0) System.out.println("check particle/curvature signs");
                                }                                   
                            }
                            if(debugMode>0) System.out.println("MC after swimming in mag field x = " + hitMCOnTrk.x() + " y = " + hitMCOnTrk.y() + 
                                    " z = " + hitMCOnTrk.z());
                            
                            // Montecarlo hit location on first detector middle plane
                            if(det==TRK1) h2000.fill(hitMCOnTrk.x(), hitMCOnTrk.y());
                            if(det==TRK2) h2001.fill(hitMCOnTrk.x(), hitMCOnTrk.y());
                            if(det==TRK1){
                                h1100.fill(hitOnTrk.x(), hitOnTrk.y());
                                h1102.fill(hitMCOnTrk.x() - hitOnTrk.x());
                                h1104.fill(hitMCOnTrk.y() - hitOnTrk.y());
                                h1106.fill((hitMCOnTrk.theta() - hitOnTrk.theta()));
                                h1108.fill((hitMCOnTrk.phi() - hitOnTrk.phi()));
                                h1112.fill((hitMCOnTrk.x() - hitOnTrk.x())/hitMCOnTrk.x());
                                h1114.fill((hitMCOnTrk.y() - hitOnTrk.y())/hitMCOnTrk.y());                       
                                h202.fill(hitOnTrk.x());
                                h1202.fill(hitMCOnTrk.x());
                                h204.fill(hitOnTrk.y());
                                h1204.fill(hitMCOnTrk.y());
                            }else if(det==TRK2){
                                h1101.fill(hitOnTrk.x(), hitOnTrk.y());
                                h1103.fill(hitMCOnTrk.x() - hitOnTrk.x());
                                h1105.fill(hitMCOnTrk.y() - hitOnTrk.y());
                                h1107.fill((hitMCOnTrk.theta() - hitOnTrk.theta()));
                                h1109.fill((hitMCOnTrk.phi() - hitOnTrk.phi()));
                           
                                h1113.fill((hitMCOnTrk.x() - hitOnTrk.x())/hitMCOnTrk.x());
                                h1115.fill((hitMCOnTrk.y() - hitOnTrk.y())/hitMCOnTrk.y());
                           
                                h203.fill(hitOnTrk.x());
                                h1203.fill(hitMCOnTrk.x());
                                h205.fill(hitOnTrk.y());
                                h1205.fill(hitMCOnTrk.y());
                            }   
                            if(debugMode>0){
                                System.out.println("MC hit coordinates " + hitMCOnTrk.x() + " " + hitMCOnTrk.y() + " " + hitMCOnTrk.z());
                                System.out.println("MC theta " + hitMCOnTrk.theta() + " hit on trk " + hitOnTrk.theta());
                                System.out.println("MC phi " + hitMCOnTrk.phi() + " hit on trk " + hitOnTrk.phi());
                                System.out.println("trk hit coordinates " + hitOnTrk.x() + " " + hitOnTrk.y() + " " + hitOnTrk.z());
                            }
                        }   
                    } // end MC comparison
                } // end loop on crosses
            }
        }
        
        if(debugMode>=-1) // print always
            System.out.println("@@@@@@@@@@@@@ total number of events read " + nev + " @@@@@ total number of events with rec cross in FTTRK " 
                    + nevWithCrosses + " @@@@ number of reconstructed FTParticles " + nOfFTParticles);
       
        double narrowFactor = 7.5;  // was 4.5
        JFrame frametrk = new JFrame("FTTRK Reconstruction with respect to FTCAL tracking");
        frametrk.setSize(1600, 800);
        EmbeddedCanvas canvastrk = new EmbeddedCanvas();
        canvastrk.divide(5, 2);
        canvastrk.cd(0);
        canvastrk.draw(h100);
        canvastrk.cd(1);
        canvastrk.draw(h102);
        F1D f02 = new F1D("f02","[amp]*gaus(x,[mean],[sigma])", -limTC/narrowFactor, limTC/narrowFactor);
        f02.setParameter(0, 10.0);
        f02.setParameter(1, 0.0);
        f02.setParameter(2, 1.0);
        f02.setLineColor(6);
        f02.setLineWidth(3);
        DataFitter.fit(f02, h102, "Q"); //No options uses error for sigma
        f02.setParameter(0, f02.parameter(0).value());
        f02.setParameter(1, f02.parameter(1).value());
        f02.setParameter(2, f02.parameter(2).value());
        DataFitter.fit(f02, h102, "Q"); //No options uses error for sigma
        f02.setOptStat(11111);
        canvastrk.draw(f02,"same");
        canvastrk.cd(2);
        canvastrk.draw(h104);
        F1D f04 = new F1D("f04","[amp]*gaus(x,[mean],[sigma])", -limTC/narrowFactor, limTC/(narrowFactor));
        f04.setParameter(0, h104.getMax());
        f04.setParameter(1, h104.getMean());
        f04.setParameter(2, h104.getRMS()/2.);
        f04.setLineColor(6);
        f04.setLineWidth(3);
        DataFitter.fit(f04, h104, "Q"); //No options uses error for sigma
        f04.setParameter(0, f04.parameter(0).value());
        f04.setParameter(1, f04.parameter(1).value());
        f04.setParameter(2, f04.parameter(2).value());
        DataFitter.fit(f04, h104, "Q"); //No options uses error for sigma
        f04.setOptStat(11111);
        canvastrk.draw(f04,"same");
        canvastrk.cd(3);
        canvastrk.draw(h106);
        F1D f06 = new F1D("f06","[amp]*gaus(x,[mean],[sigma])", -limTCTheta/narrowFactor, limTCTheta/narrowFactor);
        f06.setParameter(0, 10.0);
        f06.setParameter(1, h106.getMean());
        f06.setParameter(2, h106.getRMS());
        f06.setLineColor(6);
        f06.setLineWidth(3);
        DataFitter.fit(f06, h106, "Q"); //No options uses error for sigma
        f06.setParameter(0, f06.parameter(0).value());
        f06.setParameter(1, f06.parameter(1).value());
        f06.setParameter(2, f06.parameter(2).value());
        DataFitter.fit(f06, h106, "Q"); //No options uses error for sigma
        f06.setOptStat(11111);
        canvastrk.draw(f06,"same");  
        canvastrk.cd(4);
        canvastrk.draw(h108);
        F1D f08 = new F1D("f08","[amp]*gaus(x,[mean],[sigma])", -limTCPhi/narrowFactor, limTCPhi/narrowFactor);
        f08.setParameter(0, h108.getMax());
        f08.setParameter(1, h108.getMean());
        f08.setParameter(2, h108.getRMS());
        f08.setLineColor(6);
        f08.setLineWidth(3);
        DataFitter.fit(f08, h108, "Q"); //No options uses error for sigma
        f08.setOptStat(11111);
        canvastrk.draw(f08,"same");
        
        canvastrk.cd(5);
        canvastrk.draw(h101);
        
        canvastrk.cd(6);
        canvastrk.draw(h103);
        F1D f03 = new F1D("f03","[amp]*gaus(x,[mean],[sigma])", -limTC/narrowFactor, limTC/narrowFactor);
        f03.setParameter(0, 10.0);
        f03.setParameter(1, 0.0);
        f03.setParameter(2, 1.0);
        f03.setLineColor(6);
        f03.setLineWidth(3);
        DataFitter.fit(f03, h103, "Q"); //No options uses error for sigma
        f03.setParameter(0, f03.parameter(0).value());
        f03.setParameter(1, f03.parameter(1).value());
        f03.setParameter(2, f03.parameter(2).value());
        DataFitter.fit(f03, h103, "Q"); //No options uses error for sigma
        f03.setOptStat(1111);
        canvastrk.draw(f03,"same");
        
        
        canvastrk.cd(7);
        canvastrk.draw(h105);
        F1D f05 = new F1D("f05","[amp]*gaus(x,[mean],[sigma])", -limTC/narrowFactor, limTC/narrowFactor);
        f05.setParameter(0, 10.0);
        f05.setParameter(1, 0.0);
        f05.setParameter(2, 1.0);
        f05.setLineColor(6);
        f05.setLineWidth(3);
        DataFitter.fit(f05, h105, "Q"); //No options uses error for sigma
        f05.setParameter(0, f05.parameter(0).value());
        f05.setParameter(1, f05.parameter(1).value());
        f05.setParameter(2, f05.parameter(2).value());
        DataFitter.fit(f05, h105, "Q"); //No options uses error for sigma
        f05.setOptStat(1111);
        canvastrk.draw(f05,"same");
        
        canvastrk.cd(8);
        canvastrk.draw(h107);
        F1D f07 = new F1D("f07","[amp]*gaus(x,[mean],[sigma])", -limTCTheta/narrowFactor, limTCTheta/narrowFactor);
        f07.setParameter(0, h107.getMax());
        f07.setParameter(1, h107.getMean());
        f07.setParameter(2, h107.getRMS());
        f07.setLineColor(6);
        f07.setLineWidth(3);
        DataFitter.fit(f07, h107, "Q"); //No options uses error for sigma
        f07.setOptStat(1111);
        canvastrk.draw(f07,"same");
        
        canvastrk.cd(9);
        canvastrk.draw(h109);
        F1D f09 = new F1D("f09","[amp]*gaus(x,[mean],[sigma])", -limTCPhi/narrowFactor, limTCPhi/narrowFactor);
        f09.setParameter(0, h109.getMax());
        f09.setParameter(1, h109.getMean());
        f09.setParameter(2, h109.getRMS());
        f09.setLineColor(6);
        f09.setLineWidth(3);
        DataFitter.fit(f09, h109, "Q"); //No options uses error for sigma
        f09.setParameter(0, f09.parameter(0).value());
        f09.setParameter(1, f09.parameter(1).value());
        f09.setParameter(2, f09.parameter(2).value());
        DataFitter.fit(f09, h109, "Q"); //No options uses error for sigma
        f09.setOptStat(1111);
        canvastrk.draw(f09,"same");
        frametrk.add(canvastrk);
        frametrk.setLocationRelativeTo(null);
        frametrk.setVisible(true);
        
        JFrame frametrkres = new JFrame("FTTRK Resolutions wrt MC generated events");
        frametrkres.setSize(1800, 800);
        EmbeddedCanvas canvastrkres = new EmbeddedCanvas();
        canvastrkres.divide(7, 2);
        int ii = 0;
        canvastrkres.cd(ii);
        canvastrkres.draw(h1100);
        canvastrkres.cd(++ii);
        canvastrkres.draw(h1102);
        F1D f1 = new F1D("f1","[amp]*gaus(x,[mean],[sigma])", -0.25, 0.25);
        f1.setParameter(0, h1102.getMax());
        f1.setParameter(1, h1102.getMean());
        f1.setParameter(2, h1102.getRMS());
        f1.setLineColor(4);
        f1.setLineWidth(3);
        DataFitter.fit(f1, h1102, "Q"); //No options uses error for sigma
        f1.setParameter(0, f1.parameter(0).value());
        f1.setParameter(1, f1.parameter(1).value());
        f1.setParameter(2, f1.parameter(2).value());
        DataFitter.fit(f1, h1102, "Q"); //No options uses error for sigma
        f1.setOptStat(1111);
        canvastrkres.draw(f1,"same");
        if(debugMode>0) System.out.println(" mean = " + f1.parameter(1).value() 
              + " sigma = " + f1.parameter(2).value());
        canvastrkres.cd(++ii);
        canvastrkres.draw(h1104);
        F1D f2 = new F1D("f2","[amp]*gaus(x,[mean],[sigma])", -0.25, 0.25);
        f2.setParameter(0, f1.parameter(0).value());
        f2.setParameter(1, f1.parameter(1).value());
        f2.setParameter(2, f1.parameter(2).value());
        f2.setLineColor(24);
        f2.setLineWidth(3);
        DataFitter.fit(f2, h1104, "Q"); //No options uses error for sigma
        f2.setOptStat(1111);
        canvastrkres.draw(f2,"same");
        if(debugMode>0) System.out.println(" mean = " + f2.parameter(1).value() 
              + " sigma = " + f2.parameter(2).error());
        canvastrkres.cd(++ii);
        canvastrkres.draw(h1106);
        F1D f6 = new F1D("f6","[amp]*gaus(x,[mean],[sigma])", -0.001, 0.001);
        f6.setParameter(0, f1.parameter(0).value());
        f6.setParameter(1, h1106.getMean());
        f6.setParameter(2, h1106.getRMS());
        f6.setLineColor(24);
        f6.setLineWidth(3);
        DataFitter.fit(f6, h1106, "Q"); //No options uses error for sigma
        f6.setOptStat(1111);
        canvastrkres.draw(f6,"same");
        
        canvastrkres.cd(++ii);
        canvastrkres.draw(h1108);
        F1D f8 = new F1D("f8","[amp]*gaus(x,[mean],[sigma])", -0.005, 0.005);
        f8.setParameter(0, h1108.getMax());
        f8.setParameter(1, h1108.getMean());
        f8.setParameter(2, h1108.getRMS()/2);
        f8.setLineColor(24);
        f8.setLineWidth(3);
        DataFitter.fit(f8, h1108, "Q"); //No options uses error for sigma
        f8.setOptStat(1111);
        canvastrkres.draw(f8,"same");
        canvastrkres.cd(++ii);
        canvastrkres.draw(h1112);
        F1D f12 = new F1D("f12","[amp]*gaus(x,[mean],[sigma])", -0.05, 0.05);
        f12.setParameter(0, f1.parameter(0).value());
        f12.setParameter(1, h1112.getMean());
        f12.setParameter(2, h1112.getRMS());
        f12.setLineColor(24);
        f12.setLineWidth(3);
        DataFitter.fit(f12, h1112, "Q"); //No options uses error for sigma
        f12.setOptStat(1111);
        canvastrkres.draw(f12,"same");
        
        canvastrkres.cd(++ii);
        canvastrkres.draw(h1114);
        F1D f14 = new F1D("f14","[amp]*gaus(x,[mean],[sigma])", -0.1, 0.1);
        f14.setParameter(0, f1.parameter(0).value());
        f14.setParameter(1, h1114.getMean());
        f14.setParameter(2, h1114.getRMS());
        f14.setLineColor(24);
        f14.setLineWidth(3);
        DataFitter.fit(f14, h1114, "Q"); //No options uses error for sigma
        f14.setOptStat(1111);
        canvastrkres.draw(f14,"same");
        
        canvastrkres.cd(++ii);
        canvastrkres.draw(h1101);
        canvastrkres.cd(++ii);
        canvastrkres.draw(h1103);
        F1D f3 = new F1D("f3","[amp]*gaus(x,[mean],[sigma])", -0.25, 0.25);
        f3.setParameter(0, f1.parameter(0).value());
        f3.setParameter(1, f1.parameter(1).value());
        f3.setParameter(2, f1.parameter(2).value());
        f3.setLineColor(34);
        f3.setLineWidth(3);
        DataFitter.fit(f3, h1103, "Q"); //No options uses error for sigma
        f3.setOptStat(1111);
        canvastrkres.draw(f3,"same");
        if(debugMode>0) System.out.println(" mean = " + f3.parameter(1).value() 
              + " sigma = " + f3.parameter(2).error());
        canvastrkres.cd(++ii);
        canvastrkres.draw(h1105);
        F1D f4 = new F1D("f4","[amp]*gaus(x,[mean],[sigma])", -0.25, 0.25);
        f4.setParameter(0, f1.parameter(0).value());
        f4.setParameter(1, f1.parameter(1).value());
        f4.setParameter(2, f1.parameter(2).value());
        f4.setLineColor(44);
        f4.setLineWidth(3);
        DataFitter.fit(f4, h1105, "Q"); //No options uses error for sigma
        f4.setOptStat(1111);
        canvastrkres.draw(f4,"same");
        if(debugMode>0) System.out.println(" mean = " + f4.parameter(1).value() 
              + " sigma = " + f4.parameter(2).error());
        canvastrkres.cd(++ii);
        canvastrkres.draw(h1107);
        F1D f17 = new F1D("f17","[amp]*gaus(x,[mean],[sigma])", -0.001, 0.001);
        f17.setParameter(0, h1107.getMax());
        f17.setParameter(1, h1107.getMean());
        f17.setParameter(2, h1107.getRMS());
        f17.setLineColor(24);
        f17.setLineWidth(3);
        DataFitter.fit(f17, h1107, "Q"); //No options uses error for sigma
        f17.setOptStat(1111);
        canvastrkres.draw(f17,"same");
        canvastrkres.cd(++ii);
        canvastrkres.draw(h1109);
        F1D f19 = new F1D("f19","[amp]*gaus(x,[mean],[sigma])", -0.002, 0.002);
        f19.setParameter(0, h1109.getMax());
        f19.setParameter(1, h1109.getMean());
        f19.setParameter(2, h1109.getRMS());
        f19.setLineColor(24);
        f19.setLineWidth(3);
        DataFitter.fit(f19, h1109, "Q"); //No options uses error for sigma
        f19.setOptStat(1111);
        canvastrkres.draw(f19,"same");
        
        canvastrkres.cd(++ii);
        canvastrkres.draw(h1113);
        F1D f13 = new F1D("f13","[amp]*gaus(x,[mean],[sigma])", -0.05, 0.05);
        f13.setParameter(0, f1.parameter(0).value());
        f13.setParameter(1, h1113.getMean());
        f13.setParameter(2, h1113.getRMS());
        f13.setLineColor(24);
        f13.setLineWidth(3);
        DataFitter.fit(f13, h1113, "Q"); //No options uses error for sigma
        f13.setOptStat(1111);
        canvastrkres.draw(f13,"same");
        
        canvastrkres.cd(++ii);
        canvastrkres.draw(h1115);
        F1D f15 = new F1D("f15","[amp]*gaus(x,[mean],[sigma])", -0.1, 0.1);
        f15.setParameter(0, f1.parameter(0).value());
        f15.setParameter(1, h1115.getMean());
        f15.setParameter(2, h1115.getRMS());
        f15.setLineColor(24);
        f15.setLineWidth(3);
        DataFitter.fit(f15, h1115, "Q"); //No options uses error for sigma
        f15.setOptStat(1111);
        canvastrkres.draw(f15,"same");
        
        frametrkres.add(canvastrkres);
        frametrkres.setLocationRelativeTo(null);
        frametrkres.setVisible(true);
   
        canvasCALTRK.cd(0); canvasCALTRK.draw(h100, "same"); canvasCALTRK.draw(h7, "same");
        canvasCALTRK.cd(1); canvasCALTRK.draw(h101, "same"); canvasCALTRK.draw(h7, "same");
        canvasCALTRK.cd(2); canvasCALTRK.draw(h7, "same");
        frameCALTRK.add(canvasCALTRK);
        frameCALTRK.setLocationRelativeTo(null);
        frameCALTRK.setVisible(true);
            
    }
}
