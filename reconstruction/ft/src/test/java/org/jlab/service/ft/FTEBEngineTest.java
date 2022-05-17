package org.jlab.service.ft;

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
import org.jlab.groot.ui.LatexText;
import org.jlab.groot.math.F1D;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.data.GraphErrors;
import org.jlab.clas.pdg.PhysicsConstants;
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
import org.jlab.rec.ft.FTEventBuilder;
import org.jlab.rec.ft.FTParticle;
import org.jlab.rec.ft.FTResponse;
import org.jlab.rec.ft.FTEBEngine;
import org.jlab.rec.ft.FTConstants;

public class FTEBEngineTest extends ReconstructionEngine {

    FTEventBuilder reco;
    int Run = -1;
    double Solenoid;
    
    public static boolean timeEnergyDiagnosticHistograms = true; 
    
    public static H1F h500 = new H1F("Time Difference FTCAL-response", 100, 0., 200.);
    public static H1F h501 = new H1F("Cross Energy TRK0", 100, 0., 2000.);
    public static H1F h502 = new H1F("Cross Energy TRK1", 100, 0., 2000.);
    public static H1F h503 = new H1F("Cross time TRK0", 100, 0.0, 500.);
    public static H1F h504 = new H1F("Cross time TRK1", 100, 0.0, 500.);
    public static H2F h505 = new H2F("Cross energy vs time TRK0", 100, 0.0, 500., 100, 0., 2000.);
    public static H2F h506 = new H2F("Cross energy vs time TRK1", 100, 0.0, 500., 100, 0., 2000.);
    public static H2F h507 = new H2F("Cross energy vs time TRK0+1", 100, 0.0, 500., 100, 0., 2000.);
    public static H2F h510 = new H2F("Clusters total energies TRK0", 100, 0., 2000., 100, 0., 2000.);
    public static H2F h511 = new H2F("Clusters total energies TRK1", 100, 0., 2000., 100, 0., 2000.);
    public static H1F h512 = new H1F("Clusters total energies TRK0", 100, 0., 2000.);
    public static H1F h513 = new H1F("Clusters total energies TRK1", 100, 0., 2000.);
    // there is no time information yet in banks for clusters
    //public static H1F h520 = new H1F("Time of strips in cluster1 TRK0", 100, 0., 500.);
    //public static H1F h521 = new H1F("Time of strips in cluster2 TRK0", 100, 0., 500.);
    //public static H1F h522 = new H1F("time of strips in cluster1 TRK1", 100, 0., 500.);
    //public static H1F h523 = new H1F("Time of strips in cluster2 TRK1", 100, 0., 500.);
    
    public static H1F h600 = new H1F("TRK response position", 100, 5.93, 6.03);
    public static H2F h601 = new H2F("TRK tof vs time", 100, 0., 500., 100, 5.93, 6.03);
    
    public static H2F hSecDet0 = new H2F("lay 2 vs lay1 sectors fo form a cross", 20, -0.5, 19.5, 20, -0.5, 19.5);
    public static H2F hSecDet1 = new H2F("lay 4 vs lay3 sectors fo form a cross", 20, -0.5, 19.5, 20, -0.5, 19.5);
    public static H2F hSeedDet0 = new H2F("lay 2 vs lay1 cluster seeds fo form a cross", 768/4, -0.5, 767.5, 768/4, -0.5, 767.5);
    public static H2F hSeedDet1 = new H2F("lay 4 vs lay3 cluster seeds fo form a cross", 768/4, -0.5, 767.5, 768/4, -0.5, 767.5);
    
    public static Point3D centerOfTarget = new Point3D(0., 0., -3.);
    
   

    public FTEBEngineTest() {
        super("FTEB", "devita", "3.0");
    }

    @Override
    public boolean init() {
        reco = new FTEventBuilder();
        reco.debugMode = 0;
        String[] tables = new String[]{
            "/calibration/ft/ftcal/cluster",
            "/calibration/ft/ftcal/thetacorr",
            "/calibration/ft/ftcal/phicorr"
        };
        requireConstants(Arrays.asList(tables));
        this.getConstantsManager().setVariation("default");

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
//                reco.correctDirection(FTparticles, this.getConstantsManager(), run);  // correction to be applied only to FTcal and FThodo
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
 
        H1F resTrkXdet0 = new H1F("trk2 x residual wrt line thru trk1", 63, -0.5, 0.5);
        resTrkXdet0.setOptStat(10);
        resTrkXdet0.setTitleX("trk2 x residual (mm) wrt line thru trk1");
        resTrkXdet0.setFillColor(9);
        H1F resTrkYdet0 = new H1F("trk2 y residual wrt line thru trk1", 63, -0.5, 0.5);
        resTrkYdet0.setOptStat(10);
        resTrkYdet0.setFillColor(9);
        resTrkYdet0.setTitleX("trk22 y residual (mm) wrt line thru trk1");
        H1F resTrkXdet1 = new H1F("trk1 x residual wrt line thru trk2", 63, -0.5, 0.5);
        resTrkXdet1.setOptStat(10);
        resTrkXdet1.setFillColor(49);
        resTrkXdet1.setTitleX("trk1 y residual (mm) wrt line thru trk2");
        H1F resTrkYdet1 = new H1F("trk1 y residual wrt line thru trk2", 63, -0.5, 0.5);
        resTrkYdet1.setOptStat(10);
        resTrkYdet1.setTitleX("trk1 y residual (mm) wrt line thru trk2");
        resTrkYdet1.setFillColor(49);

        H1F resTrkThetadet0 = new H1F("trk2 theta residual (rad) wrt line thru trk1", 100, -0.005, 0.005);
        resTrkThetadet0.setOptStat(10);
        resTrkThetadet0.setTitleX("trk2 theta residual (rad) wrt line thru trk1");
        resTrkThetadet0.setFillColor(9);
        H1F resTrkThetadet1 = new H1F("trk1 theta residual (rad) wrt line thru trk2", 100, -0.005, 0.005);
        resTrkThetadet1.setOptStat(10);
        resTrkThetadet1.setTitleX("trk1 theta residual (rad) wrt line thru trk2");
        resTrkThetadet1.setFillColor(49);
        H1F resTrkPhidet0 = new H1F("trk2 phi residual (rad) wrt line thru trk1", 100, -0.1, 0.1);
        resTrkPhidet0.setOptStat(10);
        resTrkPhidet0.setTitleX("trk2 phi residual wrt line thru trk1");
        resTrkPhidet0.setFillColor(9);
        H1F resTrkPhidet1 = new H1F("trk1 phi residual (rad) wrt line thru trk2", 100, -0.1, 0.1);
        resTrkPhidet1.setOptStat(10);
        resTrkPhidet1.setTitleX("trk1 phi residual (rad) wrt line thru trk2");
        resTrkPhidet1.setFillColor(49);
           
        H2F resTrkXVsXdet0 = new H2F("trk2 x residual wrt line thru trk1 vs X", 50, -15., 15., 63, -0.5, 0.5);
        resTrkXVsXdet0.setTitleX("trk2 x (mm)");
        resTrkXVsXdet0.setTitleY("trk2 x residual (mm) wrt line thru trk1");
        H2F resTrkYVsYdet0 = new H2F("trk2 y residual wrt line thru trk1 vs Y", 50, -15., 15., 63, -0.5, 0.5);
        resTrkYVsYdet0.setTitleX("trk2 y (mm)");
        resTrkYVsYdet0.setTitleY("trk2 y residual (mm) wrt line thru trk1");
        H2F resTrkXVsXdet1 = new H2F("trk1 x residual wrt line thru trk2 vs X", 50, -15., 15., 63, -0.5, 0.5);
        resTrkXVsXdet1.setTitleX("trk1 x (mm)");
        resTrkXVsXdet1.setTitleY("trk1 x residual (mm) wrt line thru trk2");
        H2F resTrkYVsYdet1 = new H2F("trk1 y residual wrt line thru trk2 vs Y", 50, -15., 15., 63, -0.5, 0.5);
        resTrkYVsYdet1.setTitleX("trk1 y (mm)");
        resTrkYVsYdet1.setTitleY("trk1 y residual (mm) wrt line thru trk2");
        
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
        
        H1F h71 = new H1F("hOccupancyMatchedSeed1", 768, 0., 769.); h71.setTitleX("Component layer 1"); 
        h71.setLineColor(1); h71.setFillColor(51);
        H1F h72 = new H1F("hOccupancyMatchedSeed2", 768, 0., 769.); h72.setTitleX("Component layer 2"); 
        h72.setLineColor(1); h72.setFillColor(52);
        H1F h73 = new H1F("hOccupancyMatchedSeed3", 768, 0., 769.); h73.setTitleX("Component layer 3"); 
        h73.setLineColor(1); h73.setFillColor(53);
        H1F h74 = new H1F("hOccupancyMatchedSeed4", 768, 0., 769.); h74.setTitleX("Component layer 4"); 
        h74.setLineColor(1); h74.setFillColor(54);

        H1F h81 = new H1F("hOccupancyMatched1", 768, 0., 769.); h81.setTitleX("Component layer 1"); 
        h81.setLineColor(1); h81.setFillColor(31);
        H1F h82 = new H1F("hOccupancyMatched2", 768, 0., 769.); h82.setTitleX("Component layer 2"); 
        h82.setLineColor(1); h82.setFillColor(32);
        H1F h83 = new H1F("hOccupancyMatched3", 768, 0., 769.); h83.setTitleX("Component layer 3"); 
        h83.setLineColor(1); h83.setFillColor(33);
        H1F h84 = new H1F("hOccupancyMatched4", 768, 0., 769.); h84.setTitleX("Component layer 4"); 
        h84.setLineColor(1); h84.setFillColor(34);
        
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
                            int trk1ID = bank.getShort("trk0ID", i);   // it should correspond to the number of the cross in the banks
                            int trk2ID = bank.getShort("trk1ID", i);
                            if(trk1ID >= 0 || trk2ID >= 0){ // at least one cross is present on one TRK det
                                // loop on crosses bank
                                DataBank banktrk = event.getBank("FTTRK::crosses");
                                int ncrosses = banktrk.rows();
                                // how many matched crosses are associated to a given track?
                                int matchedCrosses = 0;
                                for(int nc=0; nc<ncrosses; nc++){
                                    int crossID = banktrk.getInt("id", nc);
                                    int det = banktrk.getInt("detector", nc);
                                    if(trk1ID == crossID && det==TRK1) matchedCrosses++;
                                    if(trk2ID == crossID && det==TRK2) matchedCrosses++;
                                    
                                    // diagnostic histograms for time/energy of crosses - before matching
                                    if(timeEnergyDiagnosticHistograms){
                                        float timeFromTRK = banktrk.getFloat("time", nc);
                                        float energyFromTRK = banktrk.getFloat("energy", nc);
                                        Vector3D ptOnTRK = new Vector3D(banktrk.getFloat("x", nc), banktrk.getFloat("y", nc), banktrk.getFloat("z", nc));
                                        h600.fill(ptOnTRK.mag()/PhysicsConstants.speedOfLight());
                                        h601.fill(timeFromTRK, ptOnTRK.mag()/PhysicsConstants.speedOfLight());
                                        h507.fill(timeFromTRK, energyFromTRK); 
                                        if(debugMode>=1) System.out.println("time from TRK" + timeFromTRK + " to be compared to time from TRK" + 
                                                ptOnTRK.mag()/PhysicsConstants.speedOfLight());
                                        int icl1 = banktrk.getShort("Cluster1ID", nc);   
                                        int icl2 = banktrk.getShort("Cluster2ID", nc);   
                                        DataBank bankcl = event.getBank("FTTRK::clusters");
                                        int sizeCl1 = bankcl.getShort("size", icl1);
                                        int sizeCl2 = bankcl.getShort("size", icl2);
                                        float totEnergyCl1 = bankcl.getFloat("energy", icl1);
                                        float totEnergyCl2 = bankcl.getFloat("energy", icl2);
                                        if(crossID==TRK1){
                                            h503.fill(timeFromTRK);
                                            h501.fill(energyFromTRK);
                                            h505.fill(timeFromTRK, energyFromTRK);    
                                            h510.fill(totEnergyCl1, totEnergyCl2);
                                            h512.fill(totEnergyCl1);
                                            h512.fill(totEnergyCl2);    
                                        }else if(crossID==TRK2){       
                                            h504.fill(timeFromTRK);
                                            h502.fill(energyFromTRK);
                                            h506.fill(timeFromTRK, energyFromTRK);
                                            h511.fill(totEnergyCl1, totEnergyCl2);
                                            h513.fill(totEnergyCl1);
                                            h513.fill(totEnergyCl2);
                                        }
                                    }
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
                                    if(crossID != trk1ID && crossID != trk2ID) continue;
                                    int det =  banktrk.getInt("detector", nc);
                                    if(debugMode>0) System.out.println("trk1ID " + trk1ID + " trk2ID " + trk2ID + 
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
                                                
                                                // extract the mumber of strips forming the cross and store them in an occupancy plot for matched signals
                                                // which strips are forming the id cross?                                       
                                                if(lay1==DetectorLayer.FTTRK_LAYER1){h71.fill(seed1);
                                                }else if(lay1==DetectorLayer.FTTRK_LAYER2){h72.fill(seed1);
                                                }else if(lay1==DetectorLayer.FTTRK_LAYER3){h73.fill(seed1);
                                                }else if(lay1==DetectorLayer.FTTRK_LAYER4){h74.fill(seed1);
                                                }
                                            
                                                if(lay2==DetectorLayer.FTTRK_LAYER1){h71.fill(seed2);
                                                }else if(lay2==DetectorLayer.FTTRK_LAYER2){h72.fill(seed2);
                                                }else if(lay2==DetectorLayer.FTTRK_LAYER3){h73.fill(seed2);
                                                }else if(lay2==DetectorLayer.FTTRK_LAYER4){h74.fill(seed2);
                                                }    
                                                
                                                DataBank bankhit = event.getBank("FTTRK::hits");
                                                if(bankhit.rows()>0){
                                                    for(int k=0; k<bankhit.rows(); k++){
                                                        int clusterNum = bankhit.getInt("clusterID", k);
                                                        if(clusterNum != icl1 && clusterNum != icl2) continue;
                                                        int stripInCluster = bankhit.getInt("component", k);
                                                        int clusterLay = bankhit.getInt("layer", k);
                                                        if(clusterLay==1){h81.fill(stripInCluster);
                                                        }else if(clusterLay==2){h82.fill(stripInCluster);
                                                        }else if(clusterLay==3){h83.fill(stripInCluster);
                                                        }else if(clusterLay==4){h84.fill(stripInCluster);
                                                        }
                                                    }   
                                                }    
                                            }    
                                        }
                                        
                                        if(det==TRK1 && trk1ID==crossID){
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
                                            hSecDet0.fill(sec1, sec2);
                                            hSeedDet0.fill(seed1, seed2);
                                            if(debugMode>0){
                                                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " +  sec1 + "-" + sec2 + 
                                                    " bad seeds d0, event " + nev);
                                            }
                                            canvasCALTRK.cd(0);
                                            segment1.setLineColor(1);
                                            segment2.setLineColor(2);
                                            canvasCALTRK.draw(segment1);
                                            canvasCALTRK.draw(segment2);
                                            
                                        }else if(det==TRK2 && trk2ID==crossID){
                                            h101.fill(xt, yt);
                                            h103.fill(bank.getFloat("cx", i) *zt - xt);
                                            h105.fill(bank.getFloat("cy", i) *zt - yt);
                                            h107.fill((part.theta() - hitOnTrk.theta()));
                                            h109.fill((part.phi() - hitOnTrk.phi()));
                                            int sec1 = FTTRKReconstruction.findSector(seed1);
                                            int sec2 = FTTRKReconstruction.findSector(seed2);
                                            hSecDet1.fill(sec1, sec2);
                                            hSeedDet1.fill(seed1, seed2);
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
                                                                            
                                        // extract residuals of TRK1 wrt TRK0 and viceversa    
                                        // loop on crosses in det0, find track connecting with origin, evaluate residuals of TRK1 hits wrt to this track   
                                        double cx = hitOnTrk.x() - centerOfTarget.x();
                                        double cy = hitOnTrk.y() - centerOfTarget.y();
                                        double cz = hitOnTrk.z() - centerOfTarget.z();
                                        for(int ncj = 0; ncj < ncrosses; ncj++) {
                                            int det1 =  banktrk.getInt("detector", ncj);
                                            if(det1 >=0 && nc != ncj && det != det1){
                                                double x1 = banktrk.getFloat("x", ncj);
                                                double y1 = banktrk.getFloat("y", ncj);
                                                double z1 = banktrk.getFloat("z", ncj);
                                                int secondCrossID = banktrk.getInt("id", ncj);
                                                Vector3D cross = new Vector3D(x1, y1, z1);
                                                // check if within tolerance (previously done in findCross)
                                                double r0 = Math.sqrt(hitOnTrk.x()*hitOnTrk.x() + hitOnTrk.y()*hitOnTrk.y() + hitOnTrk.z()*hitOnTrk.z());
                                                double r1 = Math.sqrt(cross.x()*cross.x() + cross.y()*cross.y() + cross.z()*cross.z());
                                                double r02d = Math.sqrt(hitOnTrk.x()*hitOnTrk.x() + hitOnTrk.y()*hitOnTrk.y());
                                                double r12d = Math.sqrt(cross.x()*cross.x() + cross.y()*cross.y());
                                                double diffRadii =  r02d-r12d;
                                                double diffTheta = Math.acos(hitOnTrk.z()/r0) - Math.acos(cross.z()/r1);
                                                double diffPhi = Math.atan2(hitOnTrk.y(), hitOnTrk.x()) - Math.atan2(cross.y(), cross.x());
                                                if(Math.abs(diffPhi) < diffPhiTolerance && Math.abs(diffRadii)< diffRadTolerance && 
                                                        Math.abs(diffTheta) < diffThetaTolerance){
                                                    double t = (cross.z()-centerOfTarget.z())/hitOnTrk.z();
                                                    Vector3D pointOnTrackAtZ = new Vector3D(cx*t + centerOfTarget.x(), cy*t + centerOfTarget.y(), z1);
                                                    if(det1 == TRK2 && trk2ID == secondCrossID){
                                                        resTrkXdet0.fill(pointOnTrackAtZ.x() - cross.x());
                                                        resTrkYdet0.fill(pointOnTrackAtZ.y() - cross.y());
                                                        resTrkThetadet0.fill(pointOnTrackAtZ.theta() - cross.theta());
                                                        resTrkPhidet0.fill(pointOnTrackAtZ.phi() - cross.phi());  
                                                        resTrkXVsXdet0.fill(cross.x(), pointOnTrackAtZ.x() - cross.x());
                                                        resTrkYVsYdet0.fill(cross.y(), pointOnTrackAtZ.y() - cross.y());
                                                    }else if(det1 == TRK1 && trk1ID == secondCrossID){
                                                        resTrkXdet1.fill(pointOnTrackAtZ.x() - cross.x());
                                                        resTrkYdet1.fill(pointOnTrackAtZ.y() - cross.y());
                                                        resTrkThetadet1.fill(pointOnTrackAtZ.theta() - cross.theta());
                                                        resTrkPhidet1.fill(pointOnTrackAtZ.phi() - cross.phi());
                                                        resTrkXVsXdet1.fill(cross.x(), pointOnTrackAtZ.x() - cross.x());
                                                        resTrkYVsYdet1.fill(cross.y(), pointOnTrackAtZ.y() - cross.y());
                                                    }
                                                }
                                            }
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
                            
                            if(det==TRK1){
                                h2000.fill(hitMCOnTrk.x(), hitMCOnTrk.y()); // Montecarlo hit location on first detector middle plane
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
                                h2001.fill(hitMCOnTrk.x(), hitMCOnTrk.y());
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
        
        if(timeEnergyDiagnosticHistograms){
            JFrame frame = new JFrame("FT Reconstruction");
            frame.setSize(1200, 800);
            EmbeddedCanvas canvas = new EmbeddedCanvas();
            canvas.divide(3, 3);
            canvas.cd(0);
            canvas.draw(h1);
            canvas.cd(1);
            canvas.draw(h2);
            canvas.cd(2);
            canvas.draw(h3);
            canvas.cd(3);
            h501.setFillColor(3); // green
            h502.setFillColor(5); // yellow
            h503.setFillColor(8); // dark green
            h504.setFillColor(7); // orange
            h600.setFillColor(9); // blue violet

        
            canvas.draw(h501);
            canvas.draw(h502,"same");
            canvas.cd(4);
            canvas.draw(h503,"");
            canvas.draw(h504,"same");
        
            for (int i = 0; i < h6.getDataBufferSize(); i++) {
                float meanE = h6.getDataBufferBin(i);
                float nE = h7.getDataBufferBin(i);
                if (nE > 0) {
                    h6.setDataBufferBin(i, meanE / nE);
                }
            }
            canvas.cd(5);
            canvas.draw(h507);
            canvas.cd(6);
            canvas.draw(h600);
            canvas.cd(7);
            canvas.draw(h601);
            canvas.cd(8);
            frame.add(canvas);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }

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
   
        JFrame frametrkrel = new JFrame("FTTRK Resolutions layer1 vs layer2");
        frametrkrel.setSize(800, 800);
        EmbeddedCanvas canvastrkrel = new EmbeddedCanvas();
        canvastrkrel.divide(2, 2);
        canvastrkrel.cd(0);
        canvastrkrel.draw(h202);
        canvastrkrel.draw(h1202,"same");
        canvastrkrel.cd(1);
        canvastrkrel.draw(h203);
        canvastrkrel.draw(h1203,"same");
        canvastrkrel.cd(2);
        canvastrkrel.draw(h204);
        canvastrkrel.draw(h1204,"same");
        canvastrkrel.cd(3);
        canvastrkrel.draw(h205);
        canvastrkrel.draw(h1205,"same");
        
        frametrkrel.add(canvastrkrel);
        frametrkrel.setLocationRelativeTo(null);
        frametrkrel.setVisible(true);
        
        JFrame frameSecradio = new JFrame("20 sectors occupancy");
        frameSecradio.setSize(1000,500);
        EmbeddedCanvas canvasSecradio = new EmbeddedCanvas();
        canvasSecradio.divide(2,1);
        canvasSecradio.cd(0);
        canvasSecradio.draw(hSecDet0);
        // excluded cells
        double excx0h1[] = {0., 1., 2., 3., 4., 5., 10., 11., 12., 13.};
        double excy0h1[] = {10., 11., 12., 13., 14., 15., 16., 17.};
        GraphErrors excludedDet0half1 = new GraphErrors();
        for(int i=0; i<excx0h1.length; i++){
            for(int j=0; j<excy0h1.length; j++){
                excludedDet0half1.addPoint(excx0h1[i], excy0h1[j], 0., 0.);
            }   
        }
        double excx0h2[] = {6., 7., 8., 9., 14., 15., 16., 17., 18., 19.};
        double excy0h2[] = {2., 3., 4., 5., 6., 7., 8., 9.};  
        GraphErrors excludedDet0half2 = new GraphErrors();
        for(int i=0; i<excx0h2.length; i++){
            for(int j=0; j<excy0h2.length; j++){
                excludedDet0half2.addPoint(excx0h2[i], excy0h2[j], 0., 0.);
            }   
        }
        double excx0h3[] = {2., 3., 4., 5., 6., 7., 8., 9.};
        double excy0h3[] = {0., 1.};  // 10 not sure
        GraphErrors excludedDet0half3 = new GraphErrors();
        for(int i=0; i<excx0h3.length; i++){
            for(int j=0; j<excy0h3.length; j++){
                excludedDet0half3.addPoint(excx0h3[i], excy0h3[j], 0., 0.);
            }   
        }
        double excx0h4[] = {10., 11., 12., 13., 14., 15., 16., 17.};
        double excy0h4[] = {18., 19.};  
        GraphErrors excludedDet0half4 = new GraphErrors();
        for(int i=0; i<excx0h4.length; i++){
            for(int j=0; j<excy0h4.length; j++){
                excludedDet0half4.addPoint(excx0h4[i], excy0h4[j], 0., 0.);
            }   
        }
        excludedDet0half1.setMarkerSize(4); excludedDet0half1.setMarkerColor(55);
        excludedDet0half2.setMarkerSize(4); excludedDet0half2.setMarkerColor(55);
        excludedDet0half3.setMarkerSize(4); excludedDet0half3.setMarkerColor(55);
        excludedDet0half4.setMarkerSize(4); excludedDet0half4.setMarkerColor(55);
        
        double excx1h1[] = {0., 1., 2., 3., 4., 5., 10., 11., 12., 13.};
        double excy1h1[] = {2., 3., 4., 5., 6., 7., 8., 9.};
        GraphErrors excludedDet1half1 = new GraphErrors();
        for(int i=0; i<excx1h1.length; i++){
            for(int j=0; j<excy1h1.length; j++){
                excludedDet1half1.addPoint(excx1h1[i], excy1h1[j], 0., 0.);
            }   
        }
        double excx1h2[] = {6., 7., 8., 9., 14., 15., 16., 17., 18., 19.};
        double excy1h2[] = {10., 11., 12., 13., 14., 15., 16., 17.};
        GraphErrors excludedDet1half2 = new GraphErrors();
        for(int i=0; i<excx1h2.length; i++){
            for(int j=0; j<excy1h2.length; j++){
                excludedDet1half2.addPoint(excx1h2[i], excy1h2[j], 0., 0.);
            }   
        }
        double excx1h3[] = {10., 11., 12., 13., 14., 15., 16., 17.};
        double excy1h3[] = {0., 1.};  // 10 not sure
        GraphErrors excludedDet1half3 = new GraphErrors();
        for(int i=0; i<excx1h3.length; i++){
            for(int j=0; j<excy1h3.length; j++){
                excludedDet1half3.addPoint(excx1h3[i], excy1h3[j], 0., 0.);
            }   
        }
        double excx1h4[] = {2., 3., 4., 5., 6., 7., 8., 9.};
        double excy1h4[] = {18., 19.};  
        GraphErrors excludedDet1half4 = new GraphErrors();
        for(int i=0; i<excx1h4.length; i++){
            for(int j=0; j<excy1h4.length; j++){
                excludedDet1half4.addPoint(excx1h4[i], excy1h4[j], 0., 0.);
            }   
        }
        excludedDet1half1.setMarkerSize(4); excludedDet1half1.setMarkerColor(55);
        excludedDet1half2.setMarkerSize(4); excludedDet1half2.setMarkerColor(55);
        excludedDet1half3.setMarkerSize(4); excludedDet1half3.setMarkerColor(55);
        excludedDet1half4.setMarkerSize(4); excludedDet1half4.setMarkerColor(55);
        
        canvasSecradio.draw(excludedDet0half1,"same");
        canvasSecradio.draw(excludedDet0half2,"same");
        canvasSecradio.draw(excludedDet0half3,"same");
        canvasSecradio.draw(excludedDet0half4,"same");
        canvasSecradio.cd(1);
        canvasSecradio.draw(hSecDet1);
        canvasSecradio.draw(excludedDet1half1,"same");
        canvasSecradio.draw(excludedDet1half2,"same");
        canvasSecradio.draw(excludedDet1half3,"same");
        canvasSecradio.draw(excludedDet1half4,"same");
        frameSecradio.add(canvasSecradio);
        frameSecradio.setLocationRelativeTo(null);
        frameSecradio.setVisible(true);
        
        
        JFrame frameSeedradio = new JFrame("Cluster seeds occupancy");
        double limSec[] = 
              {64., 128., 160., 192., 224., 256., 288., 320., 352., 384., 416., 448., 480., 512., 544., 576., 608., 640., 704., 768.};
        double limLab[] =
              {50., 110., 150., 180., 205., 230., 260., 290., 320., 350., 370., 400., 425., 450., 480., 510., 530., 560., 605., 660.};
        double limLab2[] =
              {50., 110., 150., 180., 205., 230., 260., 290., 320., 350., 390., 420., 450., 480., 510., 540., 570., 600., 650., 710.};
        int limLen = limSec.length;
        DataLine[] limitX = new DataLine[limLen];
        DataLine[] limitY = new DataLine[limLen];
        LatexText[] labSecX = new LatexText[limLen];
        LatexText[] labSecY = new LatexText[limLen];
        
        double[] limSecCenter = new double[limLen];
        for(int l=0; l<limLen; l++){
            if(l==0){
                limSecCenter[0] = 32.;
            }else{
                limSecCenter[l] = (limSec[l]+limSec[l-1])/2.;
            }
        }
        GraphErrors excludedDet01 = new GraphErrors();
        for(int i=0; i<excx0h1.length; i++){
            for(int j=0; j<excy0h1.length; j++){
                excludedDet01.addPoint(limSecCenter[(int)excx0h1[i]], limSecCenter[(int)excy0h1[j]], 0., 0.);
            }   
        }
        GraphErrors excludedDet02 = new GraphErrors();
        for(int i=0; i<excx0h2.length; i++){
            for(int j=0; j<excy0h2.length; j++){
                excludedDet02.addPoint(limSecCenter[(int)excx0h2[i]], limSecCenter[(int)excy0h2[j]], 0., 0.);
            }   
        }
        GraphErrors excludedDet03 = new GraphErrors();
        for(int i=0; i<excx0h3.length; i++){
            for(int j=0; j<excy0h3.length; j++){
                excludedDet03.addPoint(limSecCenter[(int)excx0h3[i]], limSecCenter[(int)excy0h3[j]], 0., 0.);
            }   
        }
        GraphErrors excludedDet04 = new GraphErrors();
        for(int i=0; i<excx0h4.length; i++){
            for(int j=0; j<excy0h4.length; j++){
                excludedDet04.addPoint(limSecCenter[(int)excx0h4[i]], limSecCenter[(int)excy0h4[j]], 0., 0.);
            }   
        }
        
        GraphErrors excludedDet11 = new GraphErrors();
        for(int i=0; i<excx1h1.length; i++){
            for(int j=0; j<excy1h1.length; j++){
                excludedDet11.addPoint(limSecCenter[(int)excx1h1[i]], limSecCenter[(int)excy1h1[j]], 0., 0.);
            }   
        }
        GraphErrors excludedDet12 = new GraphErrors();
        for(int i=0; i<excx1h2.length; i++){
            for(int j=0; j<excy1h2.length; j++){
                excludedDet12.addPoint(limSecCenter[(int)excx1h2[i]], limSecCenter[(int)excy1h2[j]], 0., 0.);
            }   
        }
        GraphErrors excludedDet13 = new GraphErrors();
        for(int i=0; i<excx1h3.length; i++){
            for(int j=0; j<excy1h3.length; j++){
                excludedDet13.addPoint(limSecCenter[(int)excx1h3[i]], limSecCenter[(int)excy1h3[j]], 0., 0.);
            }   
        }
        GraphErrors excludedDet14 = new GraphErrors();
        for(int i=0; i<excx1h4.length; i++){
            for(int j=0; j<excy1h4.length; j++){
                excludedDet14.addPoint(limSecCenter[(int)excx1h4[i]], limSecCenter[(int)excy1h4[j]], 0., 0.);
            }   
        }
        excludedDet01.setMarkerSize(6); excludedDet01.setMarkerColor(0);
        excludedDet02.setMarkerSize(6); excludedDet02.setMarkerColor(0);
        excludedDet03.setMarkerSize(6); excludedDet03.setMarkerColor(0);
        excludedDet04.setMarkerSize(6); excludedDet04.setMarkerColor(0);
        excludedDet11.setMarkerSize(6); excludedDet11.setMarkerColor(0);
        excludedDet12.setMarkerSize(6); excludedDet12.setMarkerColor(0);
        excludedDet13.setMarkerSize(6); excludedDet13.setMarkerColor(0);
        excludedDet14.setMarkerSize(6); excludedDet14.setMarkerColor(0);
        
        for(int l=0; l<limLen; l++){
            limitX[l] = new DataLine();
            limitY[l] = new DataLine();
            limitX[l].setOrigin(limSec[l], -0.5); limitX[l].setEnd(limSec[l], 767.5);
            limitY[l].setOrigin(-0.5, limSec[l]); limitY[l].setEnd(767.5, limSec[l]);
            limitX[l].setLineColor(6); //limitX[l].setLineStyle(3);
            limitY[l].setLineColor(6); //limitY[l].setLineStyle(3);
            labSecX[l] = new LatexText(Integer.toString(l), limLab[l], 15.);
            labSecY[l] = new LatexText(Integer.toString(l), 720., limLab2[limLen-1-l]);   // 670
            labSecX[l].setColor(6);
            labSecY[l].setColor(6);
        }
        frameSeedradio.setSize(1500, 800);
        EmbeddedCanvas canvasSeedradio = new EmbeddedCanvas();
        canvasSeedradio.divide(2,1);
        canvasSeedradio.cd(0);
        canvasSeedradio.draw(hSeedDet0);
        for(int l=0; l<limLen; l++){
            canvasSeedradio.draw(limitX[l]);
            canvasSeedradio.draw(limitY[l]);
            canvasSeedradio.draw(labSecX[l]);
            canvasSeedradio.draw(labSecY[l]);
        }
        canvasSeedradio.draw(excludedDet01, "same");
        canvasSeedradio.draw(excludedDet02, "same");
        canvasSeedradio.draw(excludedDet03, "same");
        canvasSeedradio.draw(excludedDet04, "same");
        
        canvasSeedradio.cd(1);
        canvasSeedradio.draw(hSeedDet1);
        for(int l=0; l<limLen; l++){
            canvasSeedradio.draw(limitX[l]);
            canvasSeedradio.draw(limitY[l]);
            canvasSeedradio.draw(labSecX[l]);
            canvasSeedradio.draw(labSecY[l]);
        }
        canvasSeedradio.draw(excludedDet11, "same");
        canvasSeedradio.draw(excludedDet12, "same");
        canvasSeedradio.draw(excludedDet13, "same");
        canvasSeedradio.draw(excludedDet14, "same");
        
        frameSeedradio.add(canvasSeedradio);
        frameSeedradio.setLocationRelativeTo(null);
        frameSeedradio.setVisible(true);
           
        canvasCALTRK.cd(0); canvasCALTRK.draw(h100, "same"); canvasCALTRK.draw(h7, "same");
        canvasCALTRK.cd(1); canvasCALTRK.draw(h101, "same"); canvasCALTRK.draw(h7, "same");
        canvasCALTRK.cd(2); canvasCALTRK.draw(h7, "same");
        frameCALTRK.add(canvasCALTRK);
        frameCALTRK.setLocationRelativeTo(null);
        frameCALTRK.setVisible(true);
        
        if(timeEnergyDiagnosticHistograms){
            JFrame frameStripET = new JFrame("strip energy and time studies");
            frameStripET.setSize(1500, 1000);
            EmbeddedCanvas canvasStripET = new EmbeddedCanvas();
            canvasStripET.divide(4,2);
            canvasStripET.cd(0);
            canvasStripET.draw(h510);
            canvasStripET.cd(1);
            canvasStripET.draw(h511);
            canvasStripET.cd(2);
            h512.setFillColor(53); h513.setFillColor(58);
            h512.setOptStat(Integer.parseInt("11"));
            h513.setOptStat(Integer.parseInt("11"));
            canvasStripET.draw(h512);
            canvasStripET.cd(3);
            canvasStripET.draw(h513);
            canvasStripET.cd(4);
            canvasStripET.draw(h503);
            canvasStripET.cd(5);
            canvasStripET.draw(h505);
            canvasStripET.cd(6);
            canvasStripET.draw(h504);
            canvasStripET.cd(7);
            canvasStripET.draw(h506);
            frameStripET.add(canvasStripET);
            frameStripET.setLocationRelativeTo(null);
            frameStripET.setVisible(true);
            
        } 
        
        
        JFrame frameOccMatchSeed = new JFrame("strip occupancy cluster seed for matched crosses");
        frameOccMatchSeed.setSize(1200,800);
        h71.setOptStat(11); h72.setOptStat(11); h73.setOptStat(11); h74.setOptStat(11);
        EmbeddedCanvas canvasOccMatchSeed = new EmbeddedCanvas();
        canvasOccMatchSeed.divide(2,2);
        canvasOccMatchSeed.cd(0);
        canvasOccMatchSeed.draw(h71);
        canvasOccMatchSeed.cd(1);
        canvasOccMatchSeed.draw(h72);
        canvasOccMatchSeed.cd(2);
        canvasOccMatchSeed.draw(h73);
        canvasOccMatchSeed.cd(3);
        canvasOccMatchSeed.draw(h74);
        frameOccMatchSeed.add(canvasOccMatchSeed);
        frameOccMatchSeed.setLocationRelativeTo(null);
        frameOccMatchSeed.setVisible(true);
        
        JFrame frameOccMatch = new JFrame("strip occupancy for matched crosses");
        frameOccMatch.setSize(1200,800);
        h81.setOptStat(11); h82.setOptStat(11); h83.setOptStat(11); h84.setOptStat(11);
        EmbeddedCanvas canvasOccMatch = new EmbeddedCanvas();
        canvasOccMatch.divide(2,2);
        canvasOccMatch.cd(0);
        canvasOccMatch.draw(h81);
        canvasOccMatch.cd(1);
        canvasOccMatch.draw(h82);
        canvasOccMatch.cd(2);
        canvasOccMatch.draw(h83);
        canvasOccMatch.cd(3);
        canvasOccMatch.draw(h84);
        frameOccMatch.add(canvasOccMatch);
        frameOccMatch.setLocationRelativeTo(null);
        frameOccMatch.setVisible(true);
        
        
        JFrame frametrkonlyres = new JFrame("FTTRK residuals with respect to the second detector");
        frametrkonlyres.setSize(1600, 800);
        EmbeddedCanvas canvastrkonlyres = new EmbeddedCanvas();
        canvastrkonlyres.divide(4, 2);
        int nc=-1;
        canvastrkonlyres.cd(++nc);
        canvastrkonlyres.draw(resTrkXdet0);
        double lowLim = resTrkXdet0.getMean() - resTrkXdet0.getRMS()/1.5;
        double upLim = resTrkXdet0.getMean() + resTrkXdet0.getRMS()/1.5;
        F1D f10x = new F1D("f10x","[amp]*gaus(x,[mean],[sigma])", lowLim, upLim);
        f10x.setParameter(0, resTrkXdet0.getMax());
        f10x.setParameter(1, resTrkXdet0.getMean());
        f10x.setParameter(2, resTrkXdet0.getRMS());
        f10x.setLineColor(6);
        f10x.setLineWidth(3);
        DataFitter.fit(f10x, resTrkXdet0, " "); //No options uses error for sigma
        f10x.setParameter(0, f10x.parameter(0).value());
        f10x.setParameter(1, f10x.parameter(1).value());
        f10x.setParameter(2, f10x.parameter(2).value());
        DataFitter.fit(f10x, resTrkXdet0, " "); //No options uses error for sigma
        f10x.setOptStat(11111);
        canvastrkonlyres.draw(f10x,"same");
        
        canvastrkonlyres.cd(++nc);
        canvastrkonlyres.draw(resTrkYdet0);
        lowLim = resTrkYdet0.getMean() - resTrkYdet0.getRMS()/1.5;
        upLim = resTrkYdet0.getMean() + resTrkYdet0.getRMS()/1.5;
        F1D f10y = new F1D("f10y","[amp]*gaus(x,[mean],[sigma])", lowLim, upLim);
        f10y.setParameter(0, resTrkYdet0.getMax());
        f10y.setParameter(1, resTrkYdet0.getMean());
        f10y.setParameter(2, resTrkYdet0.getRMS());
        f10y.setLineColor(6);
        f10y.setLineWidth(3);
        DataFitter.fit(f10y, resTrkYdet0, "Q"); //No options uses error for sigma
        f10y.setParameter(0, f10y.parameter(0).value());
        f10y.setParameter(1, f10y.parameter(1).value());
        f10y.setParameter(2, f10y.parameter(2).value());
        DataFitter.fit(f10y, resTrkYdet0, "Q"); //No options uses error for sigma
        f10y.setOptStat(11111);
        canvastrkonlyres.draw(f10y,"same");
        
        canvastrkonlyres.cd(++nc);
        canvastrkonlyres.draw(resTrkThetadet0);
        canvastrkonlyres.cd(++nc);
        canvastrkonlyres.draw(resTrkPhidet0);
        
        canvastrkonlyres.cd(++nc);
        canvastrkonlyres.draw(resTrkXdet1);
        lowLim = resTrkXdet1.getMean() - resTrkXdet1.getRMS()/1.5;
        upLim = resTrkXdet1.getMean() + resTrkXdet1.getRMS()/1.5;
        F1D f11x = new F1D("f11x","[amp]*gaus(x,[mean],[sigma])", lowLim, upLim);
        f11x.setParameter(0, resTrkXdet1.getMax());
        f11x.setParameter(1, resTrkXdet1.getMean());
        f11x.setParameter(2, resTrkXdet1.getRMS());
        f11x.setLineColor(6);
        f11x.setLineWidth(3);
        DataFitter.fit(f11x, resTrkXdet1, "Q"); //No options uses error for sigma
        f11x.setParameter(0, f11x.parameter(0).value());
        f11x.setParameter(1, f11x.parameter(1).value());
        f11x.setParameter(2, f11x.parameter(2).value());
        DataFitter.fit(f11x, resTrkXdet1, "Q"); //No options uses error for sigma
        f11x.setOptStat(11111);
        canvastrkonlyres.draw(f11x,"same");
        
        canvastrkonlyres.cd(++nc);
        canvastrkonlyres.draw(resTrkYdet1);
        lowLim = resTrkYdet1.getMean() - resTrkYdet1.getRMS()/1.5;
        upLim = resTrkYdet1.getMean() + resTrkYdet1.getRMS()/1.5;
        F1D f11y = new F1D("f11y","[amp]*gaus(x,[mean],[sigma])", lowLim, upLim);
        f11y.setParameter(0, resTrkYdet1.getMax());
        f11y.setParameter(1, resTrkYdet1.getMean());
        f11y.setParameter(2, resTrkYdet1.getRMS());
        f11y.setLineColor(6);
        f11y.setLineWidth(3);
        DataFitter.fit(f11y, resTrkYdet1, "Q"); //No options uses error for sigma
        f11y.setParameter(0, f11y.parameter(0).value());
        f11y.setParameter(1, f11y.parameter(1).value());
        f11y.setParameter(2, f11y.parameter(2).value());
        DataFitter.fit(f11y, resTrkYdet1, "Q"); //No options uses error for sigma
        f11y.setOptStat(11111);
        canvastrkonlyres.draw(f11y,"same");
        canvastrkonlyres.cd(++nc);
        canvastrkonlyres.draw(resTrkThetadet1);
        canvastrkonlyres.cd(++nc);
        canvastrkonlyres.draw(resTrkPhidet1);
        frametrkonlyres.add(canvastrkonlyres);
        frametrkonlyres.setLocationRelativeTo(null);
        frametrkonlyres.setVisible(true); 
        
        JFrame frametrkonlyresVsCoord = new JFrame("FTTRK residuals wrt 2nd detector vs 2nd det coordinate");
        frametrkonlyresVsCoord.setSize(1000, 800);
        EmbeddedCanvas canvastrkonlyresVsCoord = new EmbeddedCanvas();
        canvastrkonlyresVsCoord.divide(2, 2);
        nc=-1;
        canvastrkonlyresVsCoord.cd(++nc);
        canvastrkonlyresVsCoord.draw(resTrkXVsXdet0);
        canvastrkonlyresVsCoord.cd(++nc);
        canvastrkonlyresVsCoord.draw(resTrkYVsYdet0);
        canvastrkonlyresVsCoord.cd(++nc);
        canvastrkonlyresVsCoord.draw(resTrkXVsXdet1);
        canvastrkonlyresVsCoord.cd(++nc);
        canvastrkonlyresVsCoord.draw(resTrkYVsYdet1);
        frametrkonlyresVsCoord.add(canvastrkonlyresVsCoord);
        frametrkonlyresVsCoord.setLocationRelativeTo(null);
        frametrkonlyresVsCoord.setVisible(true);
    }
}
