package org.jlab.rec.cvt.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.detector.geant4.v2.CTOFGeant4Factory;
import org.jlab.detector.geant4.v2.SVT.SVTConstants;
import org.jlab.detector.geant4.v2.SVT.SVTStripFactory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.banks.HitReader;
import org.jlab.rec.cvt.banks.RecoBankWriter;
import org.jlab.rec.cvt.bmt.BMTConstants;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.bmt.CCDBConstantsLoader;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cluster.ClusterFinder;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.cross.CrossMaker;
import org.jlab.rec.cvt.hit.ADCConvertor;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.rec.cvt.svt.SVTGeometry;
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
    private int Run = -1;
    
    public CVTEngine() {
        super("CVTTracks", "ziegler", "4.0");
        
        strgtTrksRec      = new CosmicTracksRec();
        trksFromTargetRec = new TracksFromTargetRec();
    }

    
    @Override
    public boolean init() {
        
        this.loadConfiguration();
        this.initConstantsTables();
        this.loadGeometries();
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
           
            Constants.isMC = newRun<100;
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
        ADCConvertor adcConv = new ADCConvertor();

        RecoBankWriter rbc = new RecoBankWriter();

        IndexedTable svtStatus = this.getConstantsManager().getConstants(this.getRun(), "/calibration/svt/status");
        IndexedTable bmtStatus = this.getConstantsManager().getConstants(this.getRun(), "/calibration/mvt/bmt_status");
        IndexedTable bmtTime   = this.getConstantsManager().getConstants(this.getRun(), "/calibration/mvt/bmt_time");
        IndexedTable beamPos   = this.getConstantsManager().getConstants(this.getRun(), "/geometry/beam/position");

        HitReader hitRead = new HitReader();
        hitRead.fetch_SVTHits(event, adcConv, -1, -1, svtStatus);
        if(Constants.SVTONLY==false)
          hitRead.fetch_BMTHits(event, adcConv, swimmer, bmtStatus, bmtTime);

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
            rbc.appendCVTBanks(event, SVThits, BMThits, null, null, null, null, null);
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
            rbc.appendCVTBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, null, null, null);
            return true; 
        }
        
        if(Constants.ISCOSMICDATA) {
            strgtTrksRec.processEvent(event, SVThits, BMThits, SVTclusters, BMTclusters, 
                    crosses, rbc, swimmer);
        } else {
            double xb = beamPos.getDoubleValue("x_offset", 0, 0, 0)*10.0;
            double yb = beamPos.getDoubleValue("y_offset", 0, 0, 0)*10.0;
            trksFromTargetRec.processEvent(event, SVThits, BMThits, SVTclusters, BMTclusters, 
                crosses, xb , yb, rbc, swimmer);
        }
        return true;
    }
     
    
    private void loadConfiguration() {            
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
    
    private void loadGeometries() {
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
        cp = SVTConstants.connect( cp );
        cp.disconnect();  
        SVTStripFactory svtFac = new SVTStripFactory(cp, true);
        Constants.SVTGEOMETRY  = new SVTGeometry(svtFac);
        Constants.BMTGEOMETRY  = new BMTGeometry();
        
        Constants.CVTSURFACES = new ArrayList<>();
        Constants.CVTSURFACES.addAll(Constants.SVTGEOMETRY.getSurfaces());
        Constants.CVTSURFACES.addAll(Constants.BMTGEOMETRY.getSurfaces());
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
    

}