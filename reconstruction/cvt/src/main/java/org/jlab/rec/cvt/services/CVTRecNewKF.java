package org.jlab.rec.cvt.services;

import java.util.ArrayList;
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
import org.jlab.geom.base.Detector;
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
import org.jlab.rec.cvt.hit.FittedHit;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.rec.cvt.svt.SVTGeometry;
import org.jlab.rec.cvt.svt.SVTParameters;

/**
 * Service to return reconstructed TRACKS
 * format
 *
 * @author ziegler
 *
 */
public class CVTRecNewKF extends ReconstructionEngine {

    SVTGeometry       SVTGeom;
    BMTGeometry       BMTGeom;
    CTOFGeant4Factory CTOFGeom;
    Detector          CNDGeom ;
    SVTStripFactory svtIdealStripFactory;
    CosmicTracksRec strgtTrksRec;
    TracksFromTargetRec trksFromTargetRec;
    
    public CVTRecNewKF() {
        super("CVTTracks", "ziegler", "4.0");
        
        BMTGeom = new BMTGeometry();
        strgtTrksRec = new CosmicTracksRec();
        trksFromTargetRec = new TracksFromTargetRec();
    }

    private int Run = -1;
    public boolean isSVTonly = false;
    public boolean isCosmic = false;
    public boolean exclLayrs = false;
    
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

            Constants.Load(isCosmics, isSVTonly);
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

        HitReader hitRead = new HitReader();
        hitRead.fetch_SVTHits(event, adcConv, -1, -1, SVTGeom);
        if(isSVTonly==false)
          hitRead.fetch_BMTHits(event, adcConv, BMTGeom, swimmer);

        List<Hit> hits = new ArrayList<>();
        //I) get the hits
        List<Hit> svt_hits = hitRead.get_SVTHits();
        if(svt_hits.size()>SVTParameters.MAXSVTHITS)
            return true;
        if (svt_hits != null && !svt_hits.isEmpty()) {
            hits.addAll(svt_hits);
        }

        List<Hit> bmt_hits = hitRead.get_BMTHits();
        if (bmt_hits != null && bmt_hits.size() > 0) {
            hits.addAll(bmt_hits);

            if(bmt_hits.size()>BMTConstants.MAXBMTHITS)
                 return true;
        }

        //II) process the hits		
        List<FittedHit> SVThits = new ArrayList<>();
        List<FittedHit> BMThits = new ArrayList<>();
        //1) exit if hit list is empty
        if (hits.isEmpty()) {
            return true;
        }
       
        List<Cluster> clusters = new ArrayList<>();
        List<Cluster> SVTclusters = new ArrayList<>();
        List<Cluster> BMTclusters = new ArrayList<>();

        //2) find the clusters from these hits
        ClusterFinder clusFinder = new ClusterFinder();
        clusters.addAll(clusFinder.findClusters(svt_hits));     
        if(bmt_hits != null && bmt_hits.size() > 0) {
            clusters.addAll(clusFinder.findClusters(bmt_hits)); 
        }
        if (clusters.isEmpty()) {
            rbc.appendCVTBanks(event, SVThits, BMThits, null, null, null, null, null);
            return true;
        }
        
        // fill the fitted hits list.
        if (!clusters.isEmpty()) {
            for (int i = 0; i < clusters.size(); i++) {
                if (clusters.get(i).get_Detector() == DetectorType.BST) {
                    SVTclusters.add(clusters.get(i));
                    SVThits.addAll(clusters.get(i));
                }
                if (clusters.get(i).get_Detector() == DetectorType.BMT) {
                    BMTclusters.add(clusters.get(i));
                    BMThits.addAll(clusters.get(i));
                }
            }
        }

        CrossMaker crossMake = new CrossMaker();
        List<ArrayList<Cross>> crosses = crossMake.findCrosses(clusters, SVTGeom, BMTGeom);
        if(crosses.get(0).size() > SVTParameters.MAXSVTCROSSES ) {
            rbc.appendCVTBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, null, null, null);
            return true; 
        }
        
        if(this.isCosmic) {
//            if(this.isSVTonly) {
//                List<ArrayList<Cross>> crosses_svtOnly = new ArrayList<>();
//                crosses_svtOnly.add(0, crosses.get(0));
//                crosses_svtOnly.add(1, new ArrayList<>());
//            } 
            strgtTrksRec.processEvent(event, SVThits, BMThits, SVTclusters, BMTclusters, 
                    crosses, SVTGeom, BMTGeom, CTOFGeom, CNDGeom, rbc, this.exclLayrs, swimmer);
        } else {
            trksFromTargetRec.processEvent(event, SVThits, BMThits, SVTclusters, BMTclusters, 
                crosses, SVTGeom, BMTGeom, CTOFGeom, CNDGeom, rbc, swimmer, 
                this.isSVTonly, this.exclLayrs);
        }
        return true;
    }
     
    @Override
    public boolean init() {
        // Load config
        String rmReg = this.getEngineConfigString("removeRegion");
        
        if (rmReg!=null) {
            System.out.println("["+this.getName()+"] run with region "+rmReg+"removed config chosen based on yaml");
            Constants.setRmReg(Integer.valueOf(rmReg));
        }
        else {
            rmReg = System.getenv("COAT_CVT_REMOVEREGION");
            if (rmReg!=null) {
                System.out.println("["+this.getName()+"] run with region "+rmReg+"removed config chosen based on env");
                Constants.setRmReg(Integer.valueOf(rmReg));
            }
        }
        if (rmReg==null) {
             System.out.println("["+this.getName()+"] run with all region (default) ");
        }
        //svt stand-alone
        String svtStAl = this.getEngineConfigString("svtOnly");
        
        if (svtStAl!=null) {
            System.out.println("["+this.getName()+"] run with SVT only "+svtStAl+" config chosen based on yaml");
            this.isSVTonly= Boolean.valueOf(svtStAl);
        }
        else {
            svtStAl = System.getenv("COAT_SVT_ONLY");
            if (svtStAl!=null) {
                System.out.println("["+this.getName()+"] run with SVT only "+svtStAl+" config chosen based on env");
                this.isSVTonly= Boolean.valueOf(svtStAl);
            }
        }
        if (svtStAl==null) {
             System.out.println("["+this.getName()+"] run with both CVT systems (default) ");
        }
        //svt stand-alone
        String beamSpotConst = this.getEngineConfigString("BeamSpotConst");
        
        if (beamSpotConst!=null) {
            System.out.println("["+this.getName()+"] run with beamSpotConst settings "+beamSpotConst+" config chosen based on yaml");
            Constants.beamSpotConstraint = Boolean.valueOf(beamSpotConst);
        }
        else {
            beamSpotConst = System.getenv("COAT_CVT_BEAMSPOTCONST");
            if (beamSpotConst!=null) {
                System.out.println("["+this.getName()+"] run with beamSpotConst settings "+beamSpotConst+" config chosen based on env");
                this.isCosmic= Boolean.valueOf(beamSpotConst);
                Constants.beamSpotConstraint = Boolean.valueOf(beamSpotConst);
            }
        }
        if (beamSpotConst==null) {
             System.out.println("["+this.getName()+"] run with beamSpotConst settings default = false");
        }
        String svtCosmics = this.getEngineConfigString("cosmics");
        
        if (svtCosmics!=null) {
            System.out.println("["+this.getName()+"] run with cosmics settings "+svtCosmics+" config chosen based on yaml");
            this.isCosmic= Boolean.valueOf(svtCosmics);
            Constants.setCosmicsData(isCosmic);
        }
        else {
            svtCosmics = System.getenv("COAT_CVT_COSMICS");
            if (svtCosmics!=null) {
                System.out.println("["+this.getName()+"] run with cosmics settings "+svtCosmics+" config chosen based on env");
                this.isCosmic= Boolean.valueOf(svtCosmics);
                Constants.setCosmicsData(isCosmic);
            }
        }
        if (svtCosmics==null) {
             System.out.println("["+this.getName()+"] run with cosmics settings default = false");
        }
        //all layers used --> 1
        for(int i = 0; i < 12; i++)
            Constants.getLayersUsed().put(i+1, 1);
        
        //Skip layers
         String exLys = this.getEngineConfigString("excludeLayers");
        
        if (exLys!=null) {
            System.out.println("["+this.getName()+"] run with layers "+exLys+"excluded in fit config chosen based on yaml");
            String exlys = String.valueOf(exLys);
            String[] values = exlys.split(",");
            for (String value : values) {
                Constants.getLayersUsed().put(Integer.valueOf(value), 0);
            }
        }
        else {
            exLys = System.getenv("COAT_CVT_EXCLUDELAYERS");
            if (exLys!=null) {
                System.out.println("["+this.getName()+"] run with region "+rmReg+"excluded in fit  config chosen based on env");
                String exlys = String.valueOf(exLys);
                String[] values = exlys.split(",");
                for (String value : values) {
                    Constants.getLayersUsed().put(Integer.valueOf(value), 0); // layer excluded --->0
                }
            }
        }
        if (exLys==null) {
             System.out.println("["+this.getName()+"] run with all layer in fit (default) ");
        }
        
        int exlyrsnb = 0;
        for(int ilayrs = 0; ilayrs<12; ilayrs++) {
            if((int)Constants.getLayersUsed().get(ilayrs+1)<1) {
                System.out.println("EXCLUDE CVT LAYER "+(ilayrs+1));
                exlyrsnb++;
            }
        }
        if(exlyrsnb>0)
            exclLayrs = true;
        
        //Skip layers
         String exBMTLys = this.getEngineConfigString("excludeBMTLayers");
        
        if (exBMTLys!=null) {
            System.out.println("["+this.getName()+"] run with BMT layers "+exBMTLys+"excluded config chosen based on yaml");
            String exbmtlys = String.valueOf(exBMTLys);
            String[] values = exbmtlys.split(",");
            int layer = Integer.valueOf(values[0]);
            double phi_min = (double) Float.valueOf(values[1]);
            double phi_max = (double) Float.valueOf(values[2]);
            double z_min = (double) Float.valueOf(values[3]);
            double z_max = (double) Float.valueOf(values[4]);
            org.jlab.rec.cvt.Constants.setBMTLayerExcld(layer);
            double[][]BMTPhiZRangeExcld= new double[2][2];
            BMTPhiZRangeExcld[0][0] = phi_min;
            BMTPhiZRangeExcld[0][1] = phi_max;
            BMTPhiZRangeExcld[1][0] = z_min;
            BMTPhiZRangeExcld[1][1] = z_max;
            org.jlab.rec.cvt.Constants.setBMTPhiZRangeExcld(BMTPhiZRangeExcld);
            
        }
        else {
            exBMTLys = System.getenv("COAT_CVT_EXCLUDEBMTLAYERS");
            if (exBMTLys!=null) {
                System.out.println("["+this.getName()+"] run with region "+exBMTLys+"excluded in fit  config chosen based on env");
                String exbmtlys = String.valueOf(exBMTLys);
                String[] values = exbmtlys.split(",");
                int layer = Integer.valueOf(values[0]);
                double phi_min = (double) Float.valueOf(values[1]);
                double phi_max = (double) Float.valueOf(values[2]);
                double z_min = (double) Float.valueOf(values[3]);
                double z_max = (double) Float.valueOf(values[4]);
                org.jlab.rec.cvt.Constants.setBMTLayerExcld(layer);
                double[][]BMTPhiZRangeExcld= new double[2][2];
                BMTPhiZRangeExcld[0][0] = phi_min;
                BMTPhiZRangeExcld[0][1] = phi_max;
                BMTPhiZRangeExcld[1][0] = z_min;
                BMTPhiZRangeExcld[1][1] = z_max;
                org.jlab.rec.cvt.Constants.setBMTPhiZRangeExcld(BMTPhiZRangeExcld);
            }
        }
       
        //double[][]bmtx = new double[2][2];
        //bmtx[0][0]= Math.toRadians(90);
        //bmtx[0][1]= Math.toRadians(115);
        //bmtx[1][0] =100;
        //bmtx[1][1] =250;
        //Constants.setBMTPhiZRangeExcld(bmtx);
        //Constants.setBMTLayerExcld(1);
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
        System.out.println("["+this.getName()+"] run with matLib "+ Constants.kfMatLib.toString() + " library");
        

        // Load other geometries
        
        variationName = Optional.ofNullable(this.getEngineConfigString("variation")).orElse("default");
        System.out.println(" CVT YAML VARIATION NAME + "+variationName);
        ConstantProvider providerCTOF = GeometryFactory.getConstants(DetectorType.CTOF, 11, variationName);
        CTOFGeom = new CTOFGeant4Factory(providerCTOF);        
        CNDGeom =  GeometryFactory.getDetector(DetectorType.CND, 11, variationName);
        //
          
        
        System.out.println(" LOADING CVT GEOMETRY...............................variation = "+variationName);
        CCDBConstantsLoader.Load(new DatabaseConstantProvider(11, variationName));
        System.out.println("SVT LOADING WITH VARIATION "+variationName);
        DatabaseConstantProvider cp = new DatabaseConstantProvider(11, variationName);
        cp = SVTConstants.connect( cp );
        cp.disconnect();  
        SVTStripFactory svtFac = new SVTStripFactory(cp, true);
        SVTGeom = new SVTGeometry(svtFac);

        return true;
    }
  
    private String variationName;
    

}