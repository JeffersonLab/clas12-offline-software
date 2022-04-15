package org.jlab.rec.cvt.splitservices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import org.jlab.clas.pdg.PhysicsConstants;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.detector.geant4.v2.CTOFGeant4Factory;
import org.jlab.detector.geant4.v2.SVT.SVTStripFactory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.bmt.CCDBConstantsLoader;
import org.jlab.rec.cvt.svt.SVTGeometry;

/**
 * Service to return reconstructed TRACKS
 * format
 *
 * @author ziegler
 *
 */
public class CVTEngine extends ReconstructionEngine {

    private int Run = -1;

    private String svtHitBank;
    private String svtClusterBank;
    private String svtCrossBank;
    private String bmtHitBank;
    private String bmtClusterBank;
    private String bmtCrossBank;
    private String cvtSeedBank;
    private String cvtTrackBank;
    private String cvtTrajectoryBank;
    private String cvtKFTrajectoryBank;
    private String cvtCovMatBank;    
    
    // run-time options
    private int     pid = 0;
    private int     kfIterations = 5;
    private boolean kfFilterOn = true;
    private boolean initFromMc = false;    
    
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
    
    
    public CVTEngine(String name) {
        super(name, "ziegler", "5.0");
        
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
        this.printConfiguration();
        return true;    
    }
    
    public final void setOutputBankPrefix(String prefix) {
        this.setBmtHitBank("BMTRec" + prefix + "::Hits");
        this.setBmtClusterBank("BMTRec" + prefix + "::Clusters");
        this.setBmtCrossBank("BMTRec" + prefix + "::Crosses");
        this.setSvtHitBank("BSTRec" + prefix + "::Hits");
        this.setSvtClusterBank("BSTRec" + prefix + "::Clusters");
        this.setSvtCrossBank("BSTRec" + prefix + "::Crosses");
        this.setSeedBank("CVTRec" + prefix + "::Seeds");
        this.setTrackBank("CVTRec" + prefix + "::Tracks");
        this.setCovMatBank("CVTRec" + prefix + "::TrackCovMat");
        this.setTrajectoryBank("CVTRec" + prefix + "::Trajectory");
        this.setKFTrajectoryBank("CVTRec" + prefix + "::KFTrajectory");
    }

    public void registerBanks() {
        super.registerOutputBank(this.bmtHitBank);
        super.registerOutputBank(this.bmtClusterBank);
        super.registerOutputBank(this.bmtCrossBank);
        super.registerOutputBank(this.svtHitBank);
        super.registerOutputBank(this.svtClusterBank);
        super.registerOutputBank(this.svtCrossBank);
        super.registerOutputBank(this.cvtSeedBank);
        super.registerOutputBank(this.cvtTrackBank);
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

    @Override
    public boolean processDataEvent(DataEvent event) {
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
        
        // service dependent configuration settings
        if(this.getEngineConfigString("elossPid")!=null) 
            this.pid = Integer.parseInt(this.getEngineConfigString("elossPid"));       

        if (this.getEngineConfigString("kfFilterOn")!=null)
            this.kfFilterOn = Boolean.valueOf(this.getEngineConfigString("kfFilterOn"));
        
        if (this.getEngineConfigString("initFromMC")!=null)
            this.initFromMc = Boolean.valueOf(this.getEngineConfigString("initFromMC"));
        
        if (this.getEngineConfigString("kfIterations")!=null)
            this.kfIterations = Integer.valueOf(this.getEngineConfigString("kfIterations"));
        
    }


    public void initConstantsTables() {
        String[] tables = new String[]{
            "/calibration/svt/status",
            "/calibration/mvt/bmt_time",
            "/calibration/mvt/bmt_status",
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

    public void setTrackBank(String cvtTrackBank) {
        this.cvtTrackBank = cvtTrackBank;
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

    public String getTrackBank() {
        return cvtTrackBank;
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
    }

}