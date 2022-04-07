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
    private String cvtCovMat;    
    private double mass = PhysicsConstants.massPionCharged();
    
    public CVTEngine(String name) {
        super(name, "ziegler", "5.0");
        
    }

    
    @Override
    public boolean init() {        
        this.loadConfiguration();
        this.initConstantsTables();
        this.loadGeometries();
        this.registerBanks();
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
        this.setKFTrajectoryBank("CVTRec" + prefix + "::KFTraj");
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
        super.registerOutputBank(this.cvtCovMat);                
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

    public double getMass() {
        return mass;
    }
   
    @Override
    public boolean processDataEvent(DataEvent event) {
        return true;
    }

         
    public void loadConfiguration() {            
        // Load config
        
        String rmReg = this.getEngineConfigString("removeRegion");        
        if (rmReg!=null) {
            System.out.println("["+this.getName()+"] run with region "+rmReg+"removed config chosen based on yaml");
            Constants.setRmReg(Integer.valueOf(rmReg));
        }
        else {
             System.out.println("["+this.getName()+"] run with all region (default) ");
        }
        
        //svt stand-alone
        String svtStAl = this.getEngineConfigString("svtOnly");        
        if (svtStAl!=null) {
            Constants.SVTONLY = Boolean.valueOf(svtStAl);
            System.out.println("["+this.getName()+"] run with SVT only "+Constants.SVTONLY+" config chosen based on yaml");
        }
        else {
             System.out.println("["+this.getName()+"] run with both CVT systems (default) ");
        }

        if (this.getEngineConfigString("beamSpotConst")!=null) {
            Constants.setBEAMSPOTCONST(Integer.valueOf(this.getEngineConfigString("beamSpotConst")));
        }
        System.out.println("["+this.getName()+"] run with beamSpotConst set to "+Constants.getBEAMSPOTCONST()+ " (0=no-constraint, 1=seed only, 2=seed and KF)");        
         
        if (this.getEngineConfigString("beamSpotRadius")!=null) {
            Constants.setRbErr(Double.valueOf(this.getEngineConfigString("beamSpotRadius")));
        }
        System.out.println("["+this.getName()+"] run with beam spot size set to "+Constants.getRbErr());        
         
        if (this.getEngineConfigString("kfFilterOn")!=null) {
            Constants.KFFILTERON = Boolean.valueOf(this.getEngineConfigString("kfFilterOn"));
        }
        System.out.println("["+this.getName()+"] run with Kalman-Filter status set to "+Constants.KFFILTERON);
        
        if (this.getEngineConfigString("initFromMC")!=null) {
            Constants.INITFROMMC = Boolean.valueOf(this.getEngineConfigString("initFromMC"));
        }
        System.out.println("["+this.getName()+"] initialize KF from true MC information "+Constants.INITFROMMC);
        
        if (this.getEngineConfigString("kfIterations")!=null) {
            Constants.KFITERATIONS = Integer.valueOf(this.getEngineConfigString("kfIterations"));
        }
        System.out.println("["+this.getName()+"] number of KF iterations set to "+Constants.KFITERATIONS);
        
        String svtCosmics = this.getEngineConfigString("cosmics");        
        if (svtCosmics!=null) {
            Constants.ISCOSMICDATA = Boolean.valueOf(svtCosmics);
            System.out.println("["+this.getName()+"] run with cosmics settings "+Constants.ISCOSMICDATA+" config chosen based on yaml");
        }
        else {
            System.out.println("["+this.getName()+"] run with cosmics settings default = false");
        }
        
        //Skip layers
        String exLys = this.getEngineConfigString("excludeLayers");        
        if (exLys!=null)
            System.out.println("["+this.getName()+"] run with layers "+exLys+" excluded in fit config chosen based on yaml");
        else
            System.out.println("["+this.getName()+"] run with all layer in fit (default) ");
        Constants.setUsedLayers(exLys);
        
        //Skip layers
        String exBMTLys = this.getEngineConfigString("excludeBMTLayers");        
        if (exBMTLys!=null) {
            System.out.println("["+this.getName()+"] run with BMT layers "+exBMTLys+"excluded config chosen based on yaml");
            Constants.setBMTExclude(exBMTLys);        
        }
      
//        //new clustering
//        String newClustering = this.getEngineConfigString("newclustering");
//        
//        if (newClustering!=null) {
//            System.out.println("["+this.getName()+"] run with new clustering settings "+newClustering+" config chosen based on yaml");
//            BMTConstants.newClustering= Boolean.valueOf(newClustering);
//        }
//        else {
//            newClustering = System.getenv("COAT_CVT_NEWCLUSTERING");
//            if (newClustering!=null) {
//                System.out.println("["+this.getName()+"] run with new clustering settings "+newClustering+" config chosen based on env");
//                BMTConstants.newClustering= Boolean.valueOf(newClustering);
//            }
//        }
//        if (newClustering==null) {
//             System.out.println("["+this.getName()+"] run with newclustering settings default = false");
//        }

        //
        
        
        String matrixLibrary = "EJML";
        if (this.getEngineConfigString("matLib")!=null) {
            matrixLibrary = this.getEngineConfigString("matLib");
        }
        Constants.setMatLib(matrixLibrary);
        System.out.println("["+this.getName()+"] run with matLib "+ Constants.KFMATLIB.toString() + " library");
        
        if(this.getEngineConfigString("svtSeeding")!=null) {
            Constants.SVTSEEDING = Boolean.parseBoolean(this.getEngineConfigString("svtSeeding"));
            System.out.println("["+this.getName()+"] run SVT-based seeding set to "+ Constants.SVTSEEDING);
        }

        if(this.getEngineConfigString("timeCuts")!=null) {
            Constants.TIMECUTS = Boolean.parseBoolean(this.getEngineConfigString("timeCuts"));
            System.out.println("["+this.getName()+"] run BMT timing cuts set to "+ Constants.TIMECUTS);
        }

        if(this.getEngineConfigString("elossMass")!=null) {
            this.mass = Double.parseDouble(this.getEngineConfigString("elossMass"));
        }
        System.out.println("["+this.getName()+"] ELoss mass set to "+ mass + " GeV");

        if(this.getEngineConfigString("targetMat")!=null) {
            Constants.setTargetMaterial(this.getEngineConfigString("targetMat"));
        }
        System.out.println("["+this.getName()+"] Target material set to "+ Constants.getTargetMaterial());
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
    
    public void loadGeometries() {
        // Load other geometries
        
        String variation = Optional.ofNullable(this.getEngineConfigString("variation")).orElse("default");
        System.out.println(" CVT YAML VARIATION NAME + "+variation);
        ConstantProvider providerCTOF = GeometryFactory.getConstants(DetectorType.CTOF, 11, variation);
        Constants.CTOFGEOMETRY = new CTOFGeant4Factory(providerCTOF);        
        Constants.CNDGEOMETRY  =  GeometryFactory.getDetector(DetectorType.CND, 11, variation);
        
        System.out.println(" LOADING CVT GEOMETRY...............................variation = "+variation);
        CCDBConstantsLoader.Load(new DatabaseConstantProvider(11, variation));
        System.out.println("SVT LOADING WITH VARIATION "+variation);
        DatabaseConstantProvider cp = new DatabaseConstantProvider(11, variation);
        SVTStripFactory svtFac = new SVTStripFactory(cp, true);
        Constants.SVTGEOMETRY  = new SVTGeometry(svtFac);
        Constants.BMTGEOMETRY  = new BMTGeometry();
        
        Constants.CVTSURFACES = new ArrayList<>();
        Constants.CVTSURFACES.addAll(Constants.SVTGEOMETRY.getSurfaces());
        Constants.CVTSURFACES.addAll(Constants.BMTGEOMETRY.getSurfaces());
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
        this.cvtCovMat = cvtTrackCovMat;
    }
    /**
     * @param cvtKFTrajectoryBank the cvtKFTrajectoryBank to set
     */
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

    /**
     * @return the cvtKFTrajectoryBank
     */
    public String getKFTrajectoryBank() {
        return cvtKFTrajectoryBank;
    }

    public String getCovMat() {
        return cvtCovMat;
    }
    
    

}