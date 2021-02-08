package org.jlab.service.dc;

//import Jama.Matrix;
import org.jlab.jnp.matrix.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.jlab.clas.swimtools.Swim;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.banks.HitReader;
import org.jlab.rec.dc.banks.RecoBankWriter;
import org.jlab.rec.dc.cluster.ClusterCleanerUtilities;
import org.jlab.rec.dc.cluster.ClusterFinder;
import org.jlab.rec.dc.cluster.ClusterFitter;
import org.jlab.rec.dc.cluster.FittedCluster;
import org.jlab.rec.dc.cross.Cross;
import org.jlab.rec.dc.cross.CrossMaker;
import org.jlab.rec.dc.hit.FittedHit;
import org.jlab.rec.dc.segment.Segment;
import org.jlab.rec.dc.segment.SegmentFinder;
import org.jlab.rec.dc.timetodistance.TimeToDistanceEstimator;
import org.jlab.rec.dc.track.Track;
import org.jlab.rec.dc.track.TrackCandListFinder;
import org.jlab.rec.dc.track.fit.KFitterDoca;
import org.jlab.rec.dc.trajectory.StateVec;
import org.jlab.rec.dc.trajectory.Trajectory;
import org.jlab.rec.dc.trajectory.TrajectoryFinder;
import org.jlab.utils.groups.IndexedTable;

public class DCTBEngine extends DCEngine {

//    DCGeant4Factory dcDetector;
//    FTOFGeant4Factory ftofDetector;
//    ECGeant4Factory ecDetector;
//    PCALGeant4Factory pcalDetector; 
//    TrajectorySurfaces tSurf;
    private AtomicInteger Run = new AtomicInteger(0);
    
    private int newRun = 0;
    private TimeToDistanceEstimator tde;

    public DCTBEngine() {
        super("DCTB");
        tde = new TimeToDistanceEstimator();
    }
    @Override
    public boolean init() {
        super.LoadTables();
        return true;
    }
    @Override
    public boolean processDataEvent(DataEvent event) {
        //setRunConditionsParameters( event) ;
        if(event.hasBank("RUN::config")==false) {
            System.err.println("RUN CONDITIONS NOT READ AT TIMEBASED LEVEL!");
            return true;
        }
        //if(event.getBank("RECHB::Event").getFloat("startTime", 0)<0)
        //    return true; // require the start time to reconstruct the tracks in the event
        
        DataBank bank = event.getBank("RUN::config");
        // Load the constants
        //-------------------
        newRun = bank.getInt("run", 0);
        if(newRun==0)
            return true;
        if (Run.get() == 0 || (Run.get() != 0 && Run.get() != newRun)) {
           Run.set(newRun);
        }
        double T_Start = 0;
        if(Constants.isUSETSTART() == true) {
            if(event.hasBank("RECHB::Event")==true) {
                T_Start = event.getBank("RECHB::Event").getFloat("startTime", 0);
                if(T_Start<0) {
                    return true; // quit if start time not found in data
                }
            } else {
                return true; // no REC HB bank
            }
        }
        // get Field
        Swim dcSwim = new Swim();        
        //System.out.println(" RUNNING TIME BASED....................................");
        ClusterFitter cf = new ClusterFitter();
        ClusterCleanerUtilities ct = new ClusterCleanerUtilities();

        List<FittedHit> fhits = new ArrayList<FittedHit>();	
        List<FittedCluster> clusters = new ArrayList<FittedCluster>();
        List<Segment> segments = new ArrayList<Segment>();
        List<Cross> crosses = new ArrayList<Cross>();
        List<Track> trkcands = new ArrayList<Track>();

        //instantiate bank writer
        RecoBankWriter rbc = new RecoBankWriter();

        HitReader hitRead = new HitReader();
        hitRead.read_HBHits(event, 
            super.getConstantsManager().getConstants(newRun, "/calibration/dc/signal_generation/doca_resolution"),
            super.getConstantsManager().getConstants(newRun, "/calibration/dc/time_to_distance/time2dist"),
            Constants.getT0(), Constants.getT0Err(), dcDetector, tde);
        List<FittedHit> hits = new ArrayList<FittedHit>();
        //I) get the hits
        hits = hitRead.get_HBHits();

        //II) process the hits
        //1) exit if hit list is empty
        if(hits.isEmpty() ) {
                return true;
        }

        //2) find the clusters from these hits
        ClusterFinder clusFinder = new ClusterFinder();

        clusters = clusFinder.FindTimeBasedClusters(event, hits, cf, ct, 
                super.getConstantsManager().getConstants(newRun, "/calibration/dc/time_to_distance/time2dist"), dcDetector, tde);

        if(clusters.isEmpty()) {
            rbc.fillAllTBBanks(event, rbc, hits, null, null, null, null);
            return true;
        }

        //3) find the segments from the fitted clusters
        SegmentFinder segFinder = new SegmentFinder();

        List<FittedCluster> pclusters = segFinder.selectTimeBasedSegments(clusters);

        segments =  segFinder.get_Segments(pclusters, event, dcDetector, false);

        if(segments.isEmpty()) { // need 6 segments to make a trajectory
            for(FittedCluster c : clusters) {					
                for(FittedHit hit : c) {		
                    hit.set_AssociatedClusterID(c.get_Id());
                    hit.set_AssociatedHBTrackID(c.get(0).get_AssociatedHBTrackID());
                    fhits.add(hit);						
                }
            }
            rbc.fillAllTBBanks( event, rbc, fhits, clusters, null, null, null);
            return true;
        }

        for(Segment seg : segments) {					
            for(FittedHit hit : seg.get_fittedCluster()) {
                fhits.add(hit);						
            }
        }

        CrossMaker crossMake = new CrossMaker();
        //crosses = crossMake.find_Crosses(segments, dcDetector);

        //if(crosses.isEmpty() ) {			
        //    rbc.fillAllTBBanks(event, rbc, fhits, clusters, segments, null, null);
        //    return true;
        //}
        
        //
        // also need Track bank
        if (event.hasBank("HitBasedTrkg::HBTracks") == false) {
            return true;
        }
        
        DataBank trkbank = event.getBank("HitBasedTrkg::HBTracks");
        DataBank trkcovbank = event.getBank("TimeBasedTrkg::TBCovMat");
        int trkrows = trkbank.rows();
        if(trkbank.rows()!=trkcovbank.rows()) {
            return true; // HB tracks not saved correctly
        }
        Track[] TrackArray = new Track[trkrows];
        for (int i = 0; i < trkrows; i++) {
            Track HBtrk = new Track();
            HBtrk.set_Id(trkbank.getShort("id", i));
            HBtrk.set_Sector(trkbank.getByte("sector", i));
            HBtrk.set_Q(trkbank.getByte("q", i));
            HBtrk.set_pAtOrig(new Vector3D(trkbank.getFloat("p0_x", i), trkbank.getFloat("p0_y", i), trkbank.getFloat("p0_z", i)));
            HBtrk.set_P(HBtrk.get_pAtOrig().mag());
            HBtrk.set_Vtx0(new Point3D(trkbank.getFloat("Vtx0_x", i), trkbank.getFloat("Vtx0_y", i), trkbank.getFloat("Vtx0_z", i)));
            HBtrk.set_FitChi2(trkbank.getFloat("chi2", i));
            StateVec HBFinalSV = new StateVec(trkbank.getFloat("x", i), trkbank.getFloat("y", i), 
                    trkbank.getFloat("tx", i), trkbank.getFloat("ty", i));
            HBFinalSV.setZ(trkbank.getFloat("z", i));
            HBtrk.setFinalStateVec(HBFinalSV);
            Matrix initCMatrix = new Matrix();
            initCMatrix.set(new double[][]{
            {trkcovbank.getFloat("C11", i), trkcovbank.getFloat("C12", i), trkcovbank.getFloat("C13", i), trkcovbank.getFloat("C14", i), trkcovbank.getFloat("C15", i)},
            {trkcovbank.getFloat("C21", i), trkcovbank.getFloat("C22", i), trkcovbank.getFloat("C23", i), trkcovbank.getFloat("C24", i), trkcovbank.getFloat("C25", i)},
            {trkcovbank.getFloat("C31", i), trkcovbank.getFloat("C32", i), trkcovbank.getFloat("C33", i), trkcovbank.getFloat("C34", i), trkcovbank.getFloat("C35", i)},
            {trkcovbank.getFloat("C41", i), trkcovbank.getFloat("C42", i), trkcovbank.getFloat("C43", i), trkcovbank.getFloat("C44", i), trkcovbank.getFloat("C45", i)},
            {trkcovbank.getFloat("C51", i), trkcovbank.getFloat("C52", i), trkcovbank.getFloat("C53", i), trkcovbank.getFloat("C54", i), trkcovbank.getFloat("C55", i)}
            });
            HBtrk.set_CovMat(initCMatrix);
            TrackArray[HBtrk.get_Id()-1] = HBtrk; 
            TrackArray[HBtrk.get_Id()-1].set_Status(0);
        }
        if(TrackArray==null) {
            return true; // HB tracks not saved correctly
        }
        for(Segment seg : segments) {
            TrackArray[seg.get(0).get_AssociatedHBTrackID()-1].get_ListOfHBSegments().add(seg); 
            if(seg.get_Status()==1)
                TrackArray[seg.get(0).get_AssociatedHBTrackID()-1].set_Status(1);
        }
        
        //6) find the list of  track candidates
        // read beam offsets from database
        IndexedTable beamOffset = this.getConstantsManager().getConstants(newRun, "/geometry/beam/position");
        double beamXoffset = beamOffset.getDoubleValue("x_offset", 0,0,0);
        double beamYoffset = beamOffset.getDoubleValue("y_offset", 0,0,0);
        TrackCandListFinder trkcandFinder = new TrackCandListFinder("TimeBased");
        TrajectoryFinder trjFind = new TrajectoryFinder();
        for(int i = 0; i < TrackArray.length; i++) {
            if(TrackArray[i]==null || TrackArray[i].get_ListOfHBSegments()==null || TrackArray[i].get_ListOfHBSegments().size()<4)
                continue;
            TrackArray[i].set_MissingSuperlayer(get_Status(TrackArray[i]));
            TrackArray[i].addAll(crossMake.find_Crosses(TrackArray[i].get_ListOfHBSegments(), dcDetector));
            if(TrackArray[i].size()<1)
                continue;
            crosses.addAll(TrackArray[i]);
            //if(TrackArray[i].get_FitChi2()>200) {
            //    resetTrackParams(TrackArray[i], new DCSwimmer());
            //}
            KFitterDoca kFit = new KFitterDoca(TrackArray[i], dcDetector, true, dcSwim, 0);
             
            StateVec fn = new StateVec();
            kFit.runFitter(TrackArray[i].get(0).get_Sector());
            
            if(kFit.setFitFailed==false && kFit.finalStateVec!=null) { 
                // set the state vector at the last measurement site
                fn.set(kFit.finalStateVec.x, kFit.finalStateVec.y, kFit.finalStateVec.tx, kFit.finalStateVec.ty); 
                //set the track parameters if the filter does not fail
                TrackArray[i].set_P(1./Math.abs(kFit.finalStateVec.Q));
                TrackArray[i].set_Q((int)Math.signum(kFit.finalStateVec.Q));
                trkcandFinder.setTrackPars(TrackArray[i], new Trajectory(), trjFind, fn, 
                        kFit.finalStateVec.z, dcDetector, dcSwim, beamXoffset, beamYoffset);
                // candidate parameters are set from the state vector
                if(TrackArray[i].fit_Successful==false)
                    continue;
                TrackArray[i].set_FitChi2(kFit.chi2); 
                TrackArray[i].set_FitNDF(kFit.NDF);
                TrackArray[i].set_Trajectory(kFit.kfStateVecsAlongTrajectory);
                TrackArray[i].set_FitConvergenceStatus(kFit.ConvStatus);
                //TrackArray[i].set_Id(TrackArray[i].size()+1);
                //TrackArray[i].set_CovMat(kFit.finalCovMat.covMat); 
                if(TrackArray[i].get_Vtx0().toVector3D().mag()>500)
                    continue;
                // get CovMat at vertex
                Point3D VTCS = crosses.get(0).getCoordsInSector(
                        TrackArray[i].get_Vtx0().x(), TrackArray[i].get_Vtx0().y(), TrackArray[i].get_Vtx0().z());
                TrackArray[i].set_CovMat(kFit.propagateToVtx(crosses.get(0).get_Sector(), VTCS.z()));
                if(TrackArray[i].isGood()) trkcands.add(TrackArray[i]);
            }
        }
        
        
        for(int i = 0; i < crosses.size(); i++) {
            crosses.get(i).set_Id(i+1);
        }
        // track found	
        //int trkId = 1;

        if(trkcands.size()>0) {
            //trkcandFinder.removeOverlappingTracks(trkcands);		// remove overlaps

            for(Track trk: trkcands) {
                int trkId = trk.get_Id();
                // reset the id
                //trk.set_Id(trkId);
                trkcandFinder.matchHits(trk.get_Trajectory(), trk, dcDetector, dcSwim);
                trk.calcTrajectory(trkId, dcSwim, trk.get_Vtx0().x(), trk.get_Vtx0().y(), trk.get_Vtx0().z(), 
                        trk.get_pAtOrig().x(), trk.get_pAtOrig().y(), trk.get_pAtOrig().z(), trk.get_Q(), 
                        tSurf);
//                for(int j = 0; j< trk.trajectory.size(); j++) {
//                System.out.println(trk.get_Id()+" "+trk.trajectory.size()+" ("+trk.trajectory.get(j).getDetId()+") ["+
//                            trk.trajectory.get(j).getDetName()+"] "+
//                            (float)trk.trajectory.get(j).getX()/trk.get_P()+", "+
//                            (float)trk.trajectory.get(j).getY()/trk.get_P()+", "+
//                            (float)trk.trajectory.get(j).getZ()/trk.get_P()+", "+
//                            (float)trk.trajectory.get(j).getpX()/trk.get_P()+", "+
//                            (float)trk.trajectory.get(j).getpY()/trk.get_P()+", "+
//                            (float)trk.trajectory.get(j).getpZ()/trk.get_P()+", "+
//                            (float)trk.trajectory.get(j).getPathLen()+" "
//                            );               
//                }
 
                for(Cross c : trk) { 
                    c.get_Segment1().isOnTrack=true;
                    c.get_Segment2().isOnTrack=true;
                    
                    for(FittedHit h1 : c.get_Segment1()) { 
                        h1.set_AssociatedTBTrackID(trk.get_Id());

                    }
                    for(FittedHit h2 : c.get_Segment2()) {
                        h2.set_AssociatedTBTrackID(trk.get_Id());                              
                    }
                }
                //trkId++;
            }
        }    
       
        if(trkcands.isEmpty()) {

            rbc.fillAllTBBanks(event, rbc, fhits, clusters, segments, crosses, null); // no cand found, stop here and save the hits, the clusters, the segments, the crosses
            return true;
        }
        this.ensureTrackUnique(trkcands);
        rbc.fillAllTBBanks(event, rbc, fhits, clusters, segments, crosses, trkcands);

        return true;
    }

    

    private int get_Status(Track track) {
        int miss = 0;    
        
        int L[] = new int[6];
        for(int l = 0; l<track.get_ListOfHBSegments().size(); l++) {
            L[track.get_ListOfHBSegments().get(l).get_Superlayer()-1]++;
        }
        for(int l = 0; l<6; l++) {
            if(L[l]==0)
                miss=l+1;
        }
        return miss;
    }

    private void ensureTrackUnique(List<Track> trkcands) {
        List<Track> rmFrmList = new ArrayList<Track>();
        trkcands.sort(Comparator.comparing(Track::get_Id).thenComparing(Track::get_FitChi2));
        for(int i = 0; i < trkcands.size()-1; i++) {
            if(trkcands.get(i).get_Id()==trkcands.get(i+1).get_Id()) {
                double chi2_ov_ndf_i=trkcands.get(i).get_FitChi2()/(double)trkcands.get(i).get_FitNDF();
                double chi2_ov_ndf_ip1=trkcands.get(i+1).get_FitChi2()/(double)trkcands.get(i+1).get_FitNDF();
                if(chi2_ov_ndf_i<chi2_ov_ndf_ip1) {
                    rmFrmList.add(trkcands.get(i+1));
                } else {
                    rmFrmList.add(trkcands.get(i));
                }
            }
        }
        trkcands.removeAll(rmFrmList);
    }
}
