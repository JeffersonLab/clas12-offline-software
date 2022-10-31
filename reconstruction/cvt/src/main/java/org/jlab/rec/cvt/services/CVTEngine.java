package org.jlab.rec.cvt.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.clas.swimtools.Swim;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.Geometry;
import org.jlab.rec.cvt.banks.RecoBankWriter;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.rec.cvt.track.Seed;
import org.jlab.rec.cvt.track.StraightTrack;
import org.jlab.rec.cvt.track.Track;
import org.jlab.utils.groups.IndexedTable;

/**
 * Service to return reconstructed TRACKS
 * format
 *
 * @author ziegler
 *
 */
public class CVTEngine extends ReconstructionEngine {


    /**
     * @param docacutsum the docacutsum to set
     */
    public void setDocacutsum(double docacutsum) {
        this.docacutsum = docacutsum;
    }

    private int Run = -1;

    private String svtHitBank;
    private String svtClusterBank;
    private String svtCrossBank;
    private String bmtHitBank;
    private String bmtClusterBank;
    private String bmtCrossBank;
    private String cvtSeedBank;
    private String cvtSeedClusBank;
    private String cvtTrackBank;
    private String cvtUTrackBank;
    private String cvtTrajectoryBank;
    private String cvtKFTrajectoryBank;
    private String cvtCovMatBank;    
    private String bankPrefix = "";
    
    // run-time options
    private int     pid = 0;
    private int     kfIterations = 5;
    private boolean kfFilterOn = true;
    private boolean initFromMc = false;    
    
    // yaml setting passed to Constants class
    private boolean isCosmics           = false;
    private boolean svtOnly             = false;
    private String  excludeLayers       = null;
    private String  excludeBMTLayers    = null;
    private int     removeRegion        = 0;
    private int     beamSpotConstraint  = 2;
    private double  beamSpotRadius      = 0.3;
    private String  targetMaterial      = "LH2";
    private boolean elossPrecorrection  = true;
    private boolean svtSeeding          = true;
    private boolean timeCuts            = true;
    public boolean useSVTTimingCuts =  false;
    public boolean removeOverlappingSeeds = true;
    public boolean flagSeeds = true;
    private String  matrixLibrary       = "EJML";
    private boolean useOnlyTruth        = false;
    private boolean useSVTLinkerSeeder  = true;
    private double docacut = 0.75;
    private double docacutsum = 1.15;
    private int svtmaxclussize = 100;
    private int bmtcmaxclussize = 100;
    private int bmtzmaxclussize = 100;
    private double ptcut = 0.250;
    private double z0cut = 10;
    
    public CVTEngine(String name) {
        super(name, "ziegler", "6.0");
    }

    public CVTEngine() {
        super("CVTEngine", "ziegler", "6.0");
    }

    
    @Override
    public boolean init() {        
        this.loadConfiguration();
        Constants.getInstance().initialize(this.getName(),
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
                                           useSVTTimingCuts,
                                           removeOverlappingSeeds,
                                           flagSeeds,
                                           matrixLibrary,
                                           useOnlyTruth,
                                           useSVTLinkerSeeder, 
                                           docacut, 
                                           docacutsum, 
                                           svtmaxclussize, 
                                           bmtcmaxclussize, 
                                           bmtzmaxclussize,
                                           ptcut,
                                           z0cut);

        this.initConstantsTables();
        this.registerBanks();
        this.printConfiguration();
        return true;    
    }
    
    public final void setOutputBankPrefix(String prefix) {
        this.bankPrefix = prefix;
    }

    public void registerBanks() {
        String prefix = bankPrefix;
        if(Constants.getInstance().isCosmics) prefix = "Rec";
        this.setBmtHitBank("BMT" + prefix + "::Hits");
        this.setBmtClusterBank("BMT" + prefix + "::Clusters");
        this.setBmtCrossBank("BMT" + prefix + "::Crosses");
        this.setSvtHitBank("BST" + prefix + "::Hits");
        this.setSvtClusterBank("BST" + prefix + "::Clusters");
        this.setSvtCrossBank("BST" + prefix + "::Crosses");
        this.setSeedBank("CVT" + prefix + "::Seeds");
        this.setSeedClusBank("CVT" + prefix + "::SeedClusters");
        this.setTrackBank("CVT" + prefix + "::Tracks");
        this.setUTrackBank("CVT" + prefix + "::UTracks");
        this.setCovMatBank("CVT" + prefix + "::TrackCovMat");
        this.setTrajectoryBank("CVT" + prefix + "::Trajectory");
        this.setKFTrajectoryBank("CVT" + prefix + "::KFTrajectory");
        super.registerOutputBank(this.bmtHitBank);
        super.registerOutputBank(this.bmtClusterBank);
        super.registerOutputBank(this.bmtCrossBank);
        super.registerOutputBank(this.svtHitBank);
        super.registerOutputBank(this.svtClusterBank);
        super.registerOutputBank(this.svtCrossBank);
        super.registerOutputBank(this.cvtSeedBank);
        super.registerOutputBank(this.cvtSeedClusBank);
        super.registerOutputBank(this.cvtTrackBank);
        super.registerOutputBank(this.cvtUTrackBank);
        super.registerOutputBank(this.cvtCovMatBank);                
        super.registerOutputBank(this.cvtTrajectoryBank); 
        super.registerOutputBank(this.cvtKFTrajectoryBank); 
    }
    
    public int getRun(DataEvent event) {
    
        if (event.hasBank("RUN::config") == false) {
            System.err.println("RUN CONDITIONS NOT READ!");
            return 0;
        }

        DataBank bank = event.getBank("RUN::config");
        int run = bank.getInt("run", 0);  
        if(Constants.getInstance().seedingDebugMode) {
            System.out.println("EVENT "+bank.getInt("event", 0));
        }
        return run;
    }

    public int getPid() {
        return pid;
    }

    public int getKfIterations() {
        return kfIterations;
    }

    public boolean isKfFilterOn() {
        return kfFilterOn;
    }

    public boolean isInitFromMc() {
        return initFromMc;
    }

    public boolean seedBeamSpot() {
        return this.beamSpotConstraint>0;
    }
    
    public boolean kfBeamSpot() {
        return this.beamSpotConstraint==2;
    }
    
    /**
     * @return the docacut
     */
    public double getDocacut() {
        return docacut;
    }

    /**
     * @param docacut the docacut to set
     */
    public void setDocacut(double docacut) {
        this.docacut = docacut;
    }

    /**
     * @return the docacutsum
     */
    public double getDocacutsum() {
        return docacutsum;
    }

    /**
     * @return the svtmaxclussize
     */
    public int getSvtmaxclussize() {
        return svtmaxclussize;
    }

    /**
     * @param svtmaxclussize the svtmaxclussize to set
     */
    public void setSvtmaxclussize(int svtmaxclussize) {
        this.svtmaxclussize = svtmaxclussize;
    }

    /**
     * @return the bmtcmaxclussize
     */
    public int getBmtcmaxclussize() {
        return bmtcmaxclussize;
    }

    /**
     * @param bmtcmaxclussize the bmtcmaxclussize to set
     */
    public void setBmtcmaxclussize(int bmtcmaxclussize) {
        this.bmtcmaxclussize = bmtcmaxclussize;
    }

    /**
     * @return the bmtzmaxclussize
     */
    public int getBmtzmaxclussize() {
        return bmtzmaxclussize;
    }

    /**
     * @param bmtzmaxclussize the bmtzmaxclussize to set
     */
    public void setBmtzmaxclussize(int bmtzmaxclussize) {
        this.bmtzmaxclussize = bmtzmaxclussize;
    }
    
    @Override
    public boolean processDataEvent(DataEvent event) {
        
        Swim swimmer = new Swim();
        
        int run = this.getRun(event); 
        if(run<=0) return true;
        
        IndexedTable svtStatus  = this.getConstantsManager().getConstants(run, "/calibration/svt/status");
        IndexedTable svtLorentz = this.getConstantsManager().getConstants(run, "/calibration/svt/lorentz_angle");
        IndexedTable bmtStatus  = this.getConstantsManager().getConstants(run, "/calibration/mvt/bmt_status");
        IndexedTable bmtTime    = this.getConstantsManager().getConstants(run, "/calibration/mvt/bmt_time");
        IndexedTable bmtVoltage = this.getConstantsManager().getConstants(run, "/calibration/mvt/bmt_voltage");
        IndexedTable beamPos    = this.getConstantsManager().getConstants(run, "/geometry/beam/position");
        
        Geometry.initialize(this.getConstantsManager().getVariation(), 11, svtLorentz, bmtVoltage);
        
        CVTReconstruction reco = new CVTReconstruction(swimmer);
        
        List<ArrayList<Hit>>         hits = reco.readHits(event, svtStatus, bmtStatus, bmtTime);
        List<ArrayList<Cluster>> clusters = reco.findClusters();
        List<ArrayList<Cross>>    crosses = reco.findCrosses();
        
                
        List<DataBank> banks = new ArrayList<>();

        if(crosses != null) {
            if(Constants.getInstance().isCosmics) {
                CosmicTracksRec trackFinder = new CosmicTracksRec();
                List<StraightTrack>  seeds = trackFinder.getSeeds(event, clusters.get(0), clusters.get(1), crosses);
                List<StraightTrack> tracks = trackFinder.getTracks(event, this.isInitFromMc(), 
                                                                          this.isKfFilterOn(), 
                                                                          this.getKfIterations());
                if(seeds!=null) banks.add(RecoBankWriter.fillStraightSeedsBank(event, seeds, "CVTRec::CosmicSeeds"));
                if(tracks!=null) {
                    banks.add(RecoBankWriter.fillStraightTracksBank(event, tracks, "CVTRec::Cosmics"));
                    banks.add(RecoBankWriter.fillStraightTracksTrajectoryBank(event, tracks, "CVTRec::Trajectory"));
                    banks.add(RecoBankWriter.fillStraightTrackKFTrajectoryBank(event, tracks, "CVTRec::KFTrajectory"));
                }            
            } 
            else {
                double[] xyBeam = CVTReconstruction.getBeamSpot(event, beamPos);
                TracksFromTargetRec  trackFinder = new TracksFromTargetRec(swimmer, xyBeam);
                trackFinder.totTruthHits = reco.getTotalNbTruHits();
                List<Seed>   seeds = trackFinder.getSeeds(clusters, crosses);
                
                
                List<Track> tracks = trackFinder.getTracks(event, this.isInitFromMc(), 
                                                                  this.isKfFilterOn(), 
                                                                  this.getKfIterations(), 
                                                                  true, this.getPid());
                
                if(seeds!=null) {
                    banks.add(RecoBankWriter.fillSeedBank(event, seeds, this.getSeedBank()));
                    banks.add(RecoBankWriter.fillSeedClusBank(event, seeds, this.getSeedClusBank()));
                }
                if(tracks!=null) {
                    banks.add(RecoBankWriter.fillTrackBank(event, tracks, this.getTrackBank()));
    //                banks.add(RecoBankWriter.fillTrackCovMatBank(event, tracks, this.getCovMat()));
                    banks.add(RecoBankWriter.fillTrajectoryBank(event, tracks, this.getTrajectoryBank()));
                    banks.add(RecoBankWriter.fillKFTrajectoryBank(event, tracks, this.getKFTrajectoryBank()));
                }
            }
        }
        banks.add(RecoBankWriter.fillSVTHitBank(event, hits.get(0), this.getSvtHitBank()));
        banks.add(RecoBankWriter.fillBMTHitBank(event, hits.get(1), this.getBmtHitBank()));
        banks.add(RecoBankWriter.fillSVTClusterBank(event, clusters.get(0), this.getSvtClusterBank()));
        banks.add(RecoBankWriter.fillBMTClusterBank(event, clusters.get(1), this.getBmtClusterBank()));
        banks.add(RecoBankWriter.fillSVTCrossBank(event, crosses.get(0), this.getSvtCrossBank()));
        banks.add(RecoBankWriter.fillBMTCrossBank(event, crosses.get(1), this.getBmtCrossBank()));

        event.appendBanks(banks.toArray(new DataBank[0]));
            
        
        return true;
    }

         
    public void loadConfiguration() {            
        
        // general (pass-independent) settings
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
        if(this.getEngineConfigString("useSVTTimingCuts")!=null) 
            this.useSVTTimingCuts = Boolean.parseBoolean(this.getEngineConfigString("useSVTTimingCuts"));
        
        if(this.getEngineConfigString("removeOverlappingSeeds")!=null) 
            this.removeOverlappingSeeds = Boolean.parseBoolean(this.getEngineConfigString("removeOverlappingSeeds"));
        
        if(this.getEngineConfigString("flagSeeds")!=null) 
            this.flagSeeds = Boolean.parseBoolean(this.getEngineConfigString("flagSeeds"));
        
        if (this.getEngineConfigString("matLib")!=null)
            this.matrixLibrary = this.getEngineConfigString("matLib");
        
        // service dependent configuration settings
        if(this.getEngineConfigString("elossPid")!=null) 
            this.pid = Integer.parseInt(this.getEngineConfigString("elossPid"));       

        if (this.getEngineConfigString("kfFilterOn")!=null)
            this.kfFilterOn = Boolean.valueOf(this.getEngineConfigString("kfFilterOn"));
        
        if (this.getEngineConfigString("initFromMC")!=null)
            this.initFromMc = Boolean.valueOf(this.getEngineConfigString("initFromMC"));
        
        if (this.getEngineConfigString("useOnlyTruthHits")!=null)
            this.useOnlyTruth = Boolean.valueOf(this.getEngineConfigString("useOnlyTruthHits"));
        
        if (this.getEngineConfigString("useSVTLinkerSeeder")!=null)
            this.useSVTLinkerSeeder = Boolean.valueOf(this.getEngineConfigString("useSVTLinkerSeeder"));
        
        if (this.getEngineConfigString("kfIterations")!=null)
            this.kfIterations = Integer.valueOf(this.getEngineConfigString("kfIterations"));
        
        if (this.getEngineConfigString("docacut")!=null)
            this.setDocacut((double) Double.valueOf(this.getEngineConfigString("docacut")));
        
        if (this.getEngineConfigString("docacutsum")!=null)
            this.setDocacutsum((double) Double.valueOf(this.getEngineConfigString("docacutsum")));
        
        if (this.getEngineConfigString("svtmaxclussize")!=null)
            this.setSvtmaxclussize((int) Integer.valueOf(this.getEngineConfigString("svtmaxclussize")));
        
        if (this.getEngineConfigString("bmtcmaxclussize")!=null)
            this.setBmtcmaxclussize((int) Integer.valueOf(this.getEngineConfigString("bmtcmaxclussize")));
        
        if (this.getEngineConfigString("bmtzmaxclussize")!=null)
            this.setBmtzmaxclussize((int) Integer.valueOf(this.getEngineConfigString("bmtzmaxclussize")));
        
        if (this.getEngineConfigString("ptcut")!=null)
            this.ptcut = Double.valueOf(this.getEngineConfigString("ptcut"));
        
        if (this.getEngineConfigString("z0cut")!=null)
            this.z0cut = Double.valueOf(this.getEngineConfigString("z0cut"));
        
    }


    public void initConstantsTables() {
        String[] tables = new String[]{
            "/calibration/svt/status",
            "/calibration/svt/lorentz_angle",
            "/calibration/mvt/bmt_time",
            "/calibration/mvt/bmt_status",
            "/calibration/mvt/bmt_voltage",
            "/geometry/beam/position"
        };
        requireConstants(Arrays.asList(tables));
        this.getConstantsManager().setVariation("default");
    }
    
    public void setSvtHitBank(String bstHitBank) {
        this.svtHitBank = bstHitBank;
    }

    public void setSvtClusterBank(String bstClusterBank) {
        this.svtClusterBank = bstClusterBank;
    }

    public void setSvtCrossBank(String bstCrossBank) {
        this.svtCrossBank = bstCrossBank;
    }

    public void setBmtHitBank(String bmtHitBank) {
        this.bmtHitBank = bmtHitBank;
    }

    public void setBmtClusterBank(String bmtClusterBank) {
        this.bmtClusterBank = bmtClusterBank;
    }

    public void setBmtCrossBank(String bmtCrossBank) {
        this.bmtCrossBank = bmtCrossBank;
    }

    public void setSeedBank(String cvtSeedBank) {
        this.cvtSeedBank = cvtSeedBank;
    }
    
    public void setSeedClusBank(String cvtSeedClusBank) {
        this.cvtSeedClusBank = cvtSeedClusBank;
    }

    public void setTrackBank(String cvtTrackBank) {
        this.cvtTrackBank = cvtTrackBank;
    }

    public void setUTrackBank(String cvtTrack0Bank) {
        this.cvtUTrackBank = cvtTrack0Bank;
    }

    public void setTrajectoryBank(String cvtTrajectoryBank) {
        this.cvtTrajectoryBank = cvtTrajectoryBank;
    }

    public void setCovMatBank(String cvtTrackCovMat) {
        this.cvtCovMatBank = cvtTrackCovMat;
    }
    
    public void setKFTrajectoryBank(String cvtKFTrajectoryBank) {
        this.cvtKFTrajectoryBank = cvtKFTrajectoryBank;
    }
    
    public String getSvtHitBank() {
        return svtHitBank;
    }

    public String getSvtClusterBank() {
        return svtClusterBank;
    }

    public String getSvtCrossBank() {
        return svtCrossBank;
    }

    public String getBmtHitBank() {
        return bmtHitBank;
    }

    public String getBmtClusterBank() {
        return bmtClusterBank;
    }

    public String getBmtCrossBank() {
        return bmtCrossBank;
    }

    public String getSeedBank() {
        return cvtSeedBank;
    }
    
    public String getSeedClusBank() {
        return cvtSeedClusBank;
    }
    public String getTrackBank() {
        return cvtTrackBank;
    }

    public String getUTrackBank() {
        return cvtUTrackBank;
    }

    public String getTrajectoryBank() {
        return cvtTrajectoryBank;
    }

    public String getKFTrajectoryBank() {
        return cvtKFTrajectoryBank;
    }

    public String getCovMat() {
        return cvtCovMatBank;
    }
    
    
    public void printConfiguration() {            
        
        System.out.println("["+this.getName()+"] run with cosmics setting set to "+Constants.getInstance().isCosmics);        
        System.out.println("["+this.getName()+"] run with SVT only set to "+Constants.getInstance().svtOnly);
        if(this.excludeLayers!=null)
            System.out.println("["+this.getName()+"] run with layers "+this.excludeLayers+" excluded in fit, based on yaml");
        if(this.excludeBMTLayers!=null)
            System.out.println("["+this.getName()+"] run with BMT layers "+this.getEngineConfigString("excludeBMTLayers")+" excluded");
        if(this.removeRegion>0)
            System.out.println("["+this.getName()+"] run with region "+this.getEngineConfigString("removeRegion")+" removed");
        System.out.println("["+this.getName()+"] run with beamSpotConst set to "+Constants.getInstance().beamSpotConstraint+ " (0=no-constraint, 1=seed only, 2=seed and KF)");        
        System.out.println("["+this.getName()+"] run with beam spot size set to "+Constants.getInstance().getBeamRadius());                
        System.out.println("["+this.getName()+"] Target material set to "+ Constants.getInstance().getTargetMaterial().getName());
        System.out.println("["+this.getName()+"] Pre-Eloss correction set to " + Constants.getInstance().preElossCorrection);
        System.out.println("["+this.getName()+"] run SVT-based seeding set to "+ Constants.getInstance().svtSeeding);
        System.out.println("["+this.getName()+"] run BMT timing cuts set to "+ Constants.getInstance().timeCuts);
        System.out.println("["+this.getName()+"] run with matLib "+ Constants.getInstance().KFMatrixLibrary.toString() + " library");
        System.out.println("["+this.getName()+"] ELoss mass set for particle "+ pid);
        System.out.println("["+this.getName()+"] run with Kalman-Filter status set to "+this.kfFilterOn);
        System.out.println("["+this.getName()+"] initialize KF from true MC information "+this.initFromMc);
        System.out.println("["+this.getName()+"] number of KF iterations set to "+this.kfIterations);
        System.out.println("["+this.getName()+"] SLA doca cut "+this.docacut);
        System.out.println("["+this.getName()+"] SLA docasum cut "+this.docacutsum);
        System.out.println("["+this.getName()+"] max svt  cluster size "+this.getSvtmaxclussize());
        System.out.println("["+this.getName()+"] max bmt-c  cluster size "+this.getBmtcmaxclussize());
        System.out.println("["+this.getName()+"] max btm-z  cluster size "+this.getBmtzmaxclussize());
        System.out.println("["+this.getName()+"] pt cut (GeV) "+this.ptcut);
        System.out.println("["+this.getName()+"] z0 cut (mm from target edges) "+this.z0cut); 
        
        
    }

    

}
