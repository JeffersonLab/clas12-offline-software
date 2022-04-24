package org.jlab.rec.cvt.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.banks.HitReader;
import org.jlab.rec.cvt.banks.RecoBankWriter;
import org.jlab.rec.cvt.bmt.BMTConstants;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cluster.ClusterFinder;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.cross.CrossMaker;
import org.jlab.rec.cvt.hit.ADCConvertor;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.rec.cvt.svt.SVTParameters;
import org.jlab.utils.groups.IndexedTable;

/**
 * Service to return reconstructed TRACKS
 * format
 *
 * @author ziegler
 *
 */
public class CVTEngine extends ReconstructionEngine {

    private CosmicTracksRec   strgtTrksRec = null;
    private TracksFromTargetRec trksFromTargetRec = null;
    private final String svtHitBankName        = "BSTRec::Hits";
    private final String svtClusterBankName    = "BSTRec::Clusters";
    private final String svtCrossBankName      = "BSTRec::Crosses";
    private final String bmtHitBankName        = "BMTRec::Hits";
    private final String bmtClusterBankName    = "BMTRec::Clusters";
    private final String bmtCrossBankName      = "BMTRec::Crosses";
    private final String cvtSeedBankName       = "CVTRec::Seeds";
    private final String cvtTrackBankName      = "CVTRec::Tracks";
    private final String cvtTrajectoryBankName = "CVTRec::Trajectory";
    private final String cvtTrackCovMatName    = "CVTRec::TrackCovMats";  
    private int Run = -1;
    // run-time options
    private int     pid = Constants.DEFAULTPID;   
    
    // yaml setting passed to Constants class
    private String  variation           = "default";
    private boolean isCosmics           = false;
    private boolean svtOnly             = false;
    private String  excludeLayers       = null;
    private String  excludeBMTLayers    = null;
    private int     removeRegion        = 0;
    private int     beamSpotConstraint  = 1;
    private double  beamSpotRadius      = 0.3;
    private String  targetMaterial      = "LH2";
    private boolean elossPrecorrection  = true;
    private boolean svtSeeding          = true;
    private boolean timeCuts            = false;
    private String  matrixLibrary       = "EJML";
     
    public CVTEngine() {
        super("CVTTracks", "ziegler", "4.0");
        
        strgtTrksRec      = new CosmicTracksRec();
        trksFromTargetRec = new TracksFromTargetRec();
        
    }

    
    @Override
    public boolean init() {
        
        this.loadConfiguration();
        Constants.getInstance().initialize(this.getName(),
                                           this.variation, 
                                           isCosmics,
                                           svtOnly,
                                           excludeLayers,
                                           excludeBMTLayers,
                                           removeRegion,
                                           beamSpotConstraint,
                                           beamSpotRadius,
                                           targetMaterial,
                                           elossPrecorrection,
                                           svtSeeding,
                                           timeCuts,
                                           matrixLibrary);

        this.initConstantsTables();
        this.registerBanks();
        return true;
    }
    
    public void setRunConditionsParameters(DataEvent event, int iRun, boolean addMisAlignmts, String misAlgnFile) {
        if (event.hasBank("RUN::config") == false) {
            System.err.println("RUN CONDITIONS NOT READ!");
            return;
        }

        boolean isMC = false;
        boolean isCosmics = false;
        DataBank bank = event.getBank("RUN::config");
        if (bank.getByte("type", 0) == 0) {
            isMC = true;
        }
        if (bank.getByte("mode", 0) == 1) {
            isCosmics = true;
        }

        // Load the constants
        //-------------------
        int newRun = bank.getInt("run", 0); 
        if (Run != newRun) {

            this.setRun(newRun); 
           
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

   
    @Override
    public boolean processDataEvent(DataEvent event) {
        this.setRunConditionsParameters(event, Run, false, "");
        
        Swim swimmer = new Swim();

        RecoBankWriter rbc = new RecoBankWriter();

        IndexedTable svtStatus = this.getConstantsManager().getConstants(this.getRun(), "/calibration/svt/status");
        IndexedTable bmtStatus = this.getConstantsManager().getConstants(this.getRun(), "/calibration/mvt/bmt_status");
        IndexedTable bmtTime   = this.getConstantsManager().getConstants(this.getRun(), "/calibration/mvt/bmt_time");
        IndexedTable beamPos   = this.getConstantsManager().getConstants(this.getRun(), "/geometry/beam/position");

        HitReader hitRead = new HitReader();
        hitRead.fetch_SVTHits(event, -1, -1, svtStatus);
        if(Constants.getInstance().svtOnly==false)
          hitRead.fetch_BMTHits(event, swimmer, bmtStatus, bmtTime);

        List<Hit> hits = new ArrayList<>();
        //I) get the hits
        List<Hit> SVThits = hitRead.getSVTHits();
        if(SVThits.size()>SVTParameters.MAXSVTHITS)
            return true;
        if (SVThits != null && !SVThits.isEmpty()) {
            hits.addAll(SVThits);
        }

        List<Hit> BMThits = hitRead.getBMTHits();
        if (BMThits != null && BMThits.size() > 0) {
            hits.addAll(BMThits);

            if(BMThits.size()>BMTConstants.MAXBMTHITS)
                 return true;
        }

        //II) process the hits		
        //1) exit if hit list is empty
        if (hits.isEmpty()) {
            return true;
        }
       
        List<Cluster> clusters = new ArrayList<>();
        List<Cluster> SVTclusters = new ArrayList<>();
        List<Cluster> BMTclusters = new ArrayList<>();

        //2) find the clusters from these hits
        ClusterFinder clusFinder = new ClusterFinder();
        clusters.addAll(clusFinder.findClusters(SVThits));     
        if(BMThits != null && BMThits.size() > 0) {
            clusters.addAll(clusFinder.findClusters(BMThits)); 
        }
        if (clusters.isEmpty()) {
            DataBank svtHitBank = RecoBankWriter.fillSVTHitBank(event, SVThits, this.svtHitBankName);
            DataBank bmtHitBank = RecoBankWriter.fillBMTHitBank(event, SVThits, this.bmtHitBankName);
            event.appendBanks(svtHitBank, bmtHitBank);
            return true;
        }
        
        if (!clusters.isEmpty()) {
            for (int i = 0; i < clusters.size(); i++) {
                if (clusters.get(i).getDetector() == DetectorType.BST) {
                    SVTclusters.add(clusters.get(i));
                }
                if (clusters.get(i).getDetector() == DetectorType.BMT) {
                    BMTclusters.add(clusters.get(i));
                }
            }
        }

        CrossMaker crossMake = new CrossMaker();
        List<ArrayList<Cross>> crosses = crossMake.findCrosses(clusters);
        if(crosses.get(0).size() > SVTParameters.MAXSVTCROSSES ) {
            DataBank svtHitBank = RecoBankWriter.fillSVTHitBank(event, SVThits, this.svtHitBankName);
            DataBank bmtHitBank = RecoBankWriter.fillBMTHitBank(event, SVThits, this.bmtHitBankName);
            DataBank svtClusterBank = RecoBankWriter.fillSVTClusterBank(event, SVTclusters, this.svtClusterBankName);
            DataBank bmtClusterBank = RecoBankWriter.fillBMTClusterBank(event, BMTclusters, this.bmtClusterBankName);
            event.appendBanks(svtHitBank, bmtHitBank, svtClusterBank, bmtClusterBank);
            return true; 
        }
        
        if(Constants.getInstance().isCosmics) {
            strgtTrksRec.initKF(Constants.INITFROMMC, Constants.KFFILTERON, Constants.KFITERATIONS);
            strgtTrksRec.processEvent(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses);
        } else {
            double xb = beamPos.getDoubleValue("x_offset", 0, 0, 0)*10;
            double yb = beamPos.getDoubleValue("y_offset", 0, 0, 0)*10;
            trksFromTargetRec.processEvent(event, SVThits, BMThits, SVTclusters, BMTclusters, 
                crosses, xb , yb, pid, swimmer);
        }
        return true;
    }
     
    
    public void loadConfiguration() {            
        
        // general (pass-independent) settings
        if (this.getEngineConfigString("variation")!=null) 
            this.variation = this.getEngineConfigString("variation");
               
        if (this.getEngineConfigString("cosmics")!=null) 
            this.isCosmics = Boolean.valueOf(this.getEngineConfigString("cosmics"));
               
        if (this.getEngineConfigString("svtOnly")!=null)
            this.svtOnly = Boolean.valueOf(this.getEngineConfigString("svtOnly"));
        
        if (this.getEngineConfigString("excludeLayers")!=null) 
            this.excludeLayers = this.getEngineConfigString("excludeLayers");
        
        if (this.getEngineConfigString("excludeBMTLayers")!=null) 
            this.excludeBMTLayers = this.getEngineConfigString("excludeBMTLayers");                

        if (this.getEngineConfigString("removeRegion")!=null) 
            this.removeRegion = Integer.valueOf(this.getEngineConfigString("removeRegion"));
        
        if (this.getEngineConfigString("beamSpotConst")!=null)
            this.beamSpotConstraint = Integer.valueOf(this.getEngineConfigString("beamSpotConst"));
        
        if (this.getEngineConfigString("beamSpotRadius")!=null)
            this.beamSpotRadius = Double.valueOf(this.getEngineConfigString("beamSpotRadius"));
            
        if(this.getEngineConfigString("targetMat")!=null)
            this.targetMaterial = this.getEngineConfigString("targetMat");

        if(this.getEngineConfigString("elossPreCorrection")!=null)
            this.elossPrecorrection = Boolean.parseBoolean(this.getEngineConfigString("elossPreCorrection"));
        
        if(this.getEngineConfigString("svtSeeding")!=null)
            this.svtSeeding = Boolean.parseBoolean(this.getEngineConfigString("svtSeeding"));
        
        if(this.getEngineConfigString("timeCuts")!=null) 
            this.timeCuts = Boolean.parseBoolean(this.getEngineConfigString("timeCuts"));
        
        if (this.getEngineConfigString("matLib")!=null)
            this. matrixLibrary = this.getEngineConfigString("matLib");
    }

    private void initConstantsTables() {
        String[] tables = new String[]{
            "/calibration/svt/status",
            "/calibration/mvt/bmt_time",
            "/calibration/mvt/bmt_status",
            "/geometry/beam/position"
        };
        requireConstants(Arrays.asList(tables));
        this.getConstantsManager().setVariation("default");
    }
       
    private void registerBanks() {
        super.registerOutputBank("BMTRec::Hits");
        super.registerOutputBank("BMTRec::Clusters");
        super.registerOutputBank("BSTRec::Crosses");
        super.registerOutputBank("BSTRec::Hits");
        super.registerOutputBank("BSTRec::Clusters");
        super.registerOutputBank("BSTRec::Crosses");
        super.registerOutputBank("CVTRec::Seeds");
        super.registerOutputBank("CVTRec::Tracks");
        super.registerOutputBank("CVTRec::Trajectory");        
    }
    
    public void printConfiguration() {            
        
        System.out.println("["+this.getName()+"] run with cosmics setting set to "+Constants.getInstance().isCosmics);        
        System.out.println("["+this.getName()+"] run with SVT only set to "+Constants.getInstance().svtOnly);
        if(this.excludeLayers!=null)
            System.out.println("["+this.getName()+"] run with layers "+this.excludeLayers+" excluded in fit, based on yaml");
        if(this.excludeBMTLayers!=null)
            System.out.println("["+this.getName()+"] run with BMT layers "+this.excludeBMTLayers+" excluded");
        if(this.removeRegion>0)
            System.out.println("["+this.getName()+"] run with region "+this.getEngineConfigString("removeRegion")+" removed");
        System.out.println("["+this.getName()+"] run with beamSpotConst set to "+Constants.getInstance().beamSpotConstraint+ " (0=no-constraint, 1=seed only, 2=seed and KF)");        
        System.out.println("["+this.getName()+"] run with beam spot size set to "+Constants.getInstance().getBeamRadius());                
        System.out.println("["+this.getName()+"] Target material set to "+ Constants.getInstance().getTargetMaterial().getName());
        System.out.println("["+this.getName()+"] Pre-Eloss correction set to " + Constants.getInstance().preElossCorrection);
        System.out.println("["+this.getName()+"] run SVT-based seeding set to "+ Constants.getInstance().svtSeeding);
        System.out.println("["+this.getName()+"] run BMT timing cuts set to "+ Constants.getInstance().timeCuts);
        System.out.println("["+this.getName()+"] run with matLib "+ Constants.getInstance().KFMatrixLibrary.toString() + " library");
        System.out.println("["+this.getName()+"] ELoss mass set for particle "+ Constants.DEFAULTPID);
        System.out.println("["+this.getName()+"] run with Kalman-Filter status set to "+Constants.KFFILTERON);
        System.out.println("["+this.getName()+"] initialize KF from true MC information "+Constants.INITFROMMC);
        System.out.println("["+this.getName()+"] number of KF iterations set to "+Constants.KFITERATIONS);
    }
}