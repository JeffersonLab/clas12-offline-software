package org.jlab.service.dc;

import Jama.Matrix;
import java.util.ArrayList;
import java.util.List;
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
import org.jlab.rec.dc.track.fit.KFitter;
import org.jlab.rec.dc.trajectory.DCSwimmer;
import org.jlab.rec.dc.trajectory.StateVec;
import org.jlab.rec.dc.trajectory.Trajectory;
import org.jlab.rec.dc.trajectory.TrajectoryFinder;

public class DCTBEngine extends DCEngine {

//    DCGeant4Factory dcDetector;
//    FTOFGeant4Factory ftofDetector;
//    ECGeant4Factory ecDetector;
//    PCALGeant4Factory pcalDetector; 
//    TrajectorySurfaces tSurf;
    
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
        //if(event.getBank("RECHB::Event").getFloat("STTime", 0)<0)
        //    return true; // require the start time to reconstruct the tracks in the event
        
        DataBank bank = event.getBank("RUN::config");
        // Load the constants
        //-------------------
        int newRun = bank.getInt("run", 0);
        if(newRun==0)
            return true;

        double T_Start = 0;
        if(Constants.isUSETSTART() == true) {
            if(event.hasBank("RECHB::Event")==true) {
                T_Start = event.getBank("RECHB::Event").getFloat("STTime", 0);
                if(T_Start<0) {
                    return true; // quit if start time not found in data
                }
            } else {
                return true; // no REC HB bank
            }
        }
        
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
        hitRead.read_TBHits(event, 
            super.getConstantsManager().getConstants(newRun, "/calibration/dc/signal_generation/doca_resolution"),
            super.getConstantsManager().getConstants(newRun, "/calibration/dc/time_to_distance/time2dist"), tde, Constants.getT0(), Constants.getT0Err());
        List<FittedHit> hits = new ArrayList<FittedHit>();
        //I) get the hits
        if(hitRead.get_TBHits().isEmpty()) {
            hits = hitRead.get_HBHits();

        } else {
            hits = hitRead.get_TBHits();
        }

        //II) process the hits
        //1) exit if hit list is empty
        if(hits.isEmpty() ) {
                return true;
        }

        //2) find the clusters from these hits
        ClusterFinder clusFinder = new ClusterFinder();

        clusters = clusFinder.FindTimeBasedClusters(hits, cf, ct, super.getConstantsManager().getConstants(newRun, "/calibration/dc/time_to_distance/time2dist"), dcDetector, tde);

        if(clusters.isEmpty()) {
            rbc.fillAllTBBanks(event, rbc, hits, null, null, null, null);
            return true;
        }

        //3) find the segments from the fitted clusters
        SegmentFinder segFinder = new SegmentFinder();

        List<FittedCluster> pclusters = segFinder.selectTimeBasedSegments(clusters);

        segments =  segFinder.get_Segments(pclusters, event, dcDetector);

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
            HBtrk.set_Vtx0(new Point3D(trkbank.getFloat("Vtx0_x", i), trkbank.getFloat("Vtx0_y", i), trkbank.getFloat("Vtx0_z", i)));
            HBtrk.set_FitChi2(trkbank.getFloat("chi2", i));
            Matrix initCMatrix = new Matrix(new double[][]{
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
        
        for(Segment seg : segments) {
            TrackArray[seg.get(0).get_AssociatedHBTrackID()-1].get_ListOfHBSegments().add(seg); 
            if(seg.get_Status()==1)
                TrackArray[seg.get(0).get_AssociatedHBTrackID()-1].set_Status(1);
        }
        
        //6) find the list of  track candidates
        TrackCandListFinder trkcandFinder = new TrackCandListFinder("TimeBased");
        TrajectoryFinder trjFind = new TrajectoryFinder();
        for(int i = 0; i < TrackArray.length; i++) {
            if(TrackArray[i].get_ListOfHBSegments()==null || TrackArray[i].get_ListOfHBSegments().size()<4)
                continue;
            TrackArray[i].set_MissingSuperlayer(get_Status(TrackArray[i]));
            TrackArray[i].addAll(crossMake.find_Crosses(TrackArray[i].get_ListOfHBSegments(), dcDetector));
            if(TrackArray[i].size()<1)
                continue;
            crosses.addAll(TrackArray[i]);
            //if(TrackArray[i].get_FitChi2()>200) {
            //    resetTrackParams(TrackArray[i], new DCSwimmer());
            //}
            KFitter kFit = new KFitter(TrackArray[i], dcDetector, true);
            //kFit.totNumIter=30;
            
            StateVec fn = new StateVec();
            kFit.runFitter();
            
            if(kFit.setFitFailed==false && kFit.finalStateVec!=null) {
                // set the state vector at the last measurement site
                fn.set(kFit.finalStateVec.x, kFit.finalStateVec.y, kFit.finalStateVec.tx, kFit.finalStateVec.ty); 
                //set the track parameters if the filter does not fail
                TrackArray[i].set_P(1./Math.abs(kFit.finalStateVec.Q));
                TrackArray[i].set_Q((int)Math.signum(kFit.finalStateVec.Q));
                trkcandFinder.setTrackPars(TrackArray[i], new Trajectory(), trjFind, fn, kFit.finalStateVec.z, dcDetector);
                // candidate parameters are set from the state vector
                TrackArray[i].set_FitChi2(kFit.chi2); 
                TrackArray[i].set_FitNDF(kFit.NDF);
                TrackArray[i].set_Trajectory(kFit.kfStateVecsAlongTrajectory);
                TrackArray[i].set_FitConvergenceStatus(kFit.ConvStatus);
                TrackArray[i].set_Id(TrackArray[i].size()+1);
                TrackArray[i].set_CovMat(kFit.finalCovMat.covMat);
                if(TrackArray[i].get_Vtx0().toVector3D().mag()>500)
                    continue;
                trkcands.add(TrackArray[i]);
            }
        }
        
        
        for(int i = 0; i < crosses.size(); i++) {
            crosses.get(i).set_Id(i+1);
        }
        // track found	
        int trkId = 1;

        if(trkcands.size()>0) {
            //trkcandFinder.removeOverlappingTracks(trkcands);		// remove overlaps

            for(Track trk: trkcands) {
                // reset the id
                trk.set_Id(trkId);
                trkcandFinder.matchHits(trk.get_Trajectory(), trk, dcDetector);
                trk.calcTrajectory(trkId, trkcandFinder.dcSwim, trk.get_Vtx0().x(), trk.get_Vtx0().y(), trk.get_Vtx0().z(), trk.get_pAtOrig().x(), trk.get_pAtOrig().y(), trk.get_pAtOrig().z(), trk.get_Q(), ftofDetector, tSurf);
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
                trkId++;
            }
        }    
       
        if(trkcands.isEmpty()) {

            rbc.fillAllTBBanks(event, rbc, fhits, clusters, segments, crosses, null); // no cand found, stop here and save the hits, the clusters, the segments, the crosses
            return true;
        }
        rbc.fillAllTBBanks(event, rbc, fhits, clusters, segments, crosses, trkcands);

        return true;
    }

    private void resetTrackParams(Track track, DCSwimmer dcSwim) {
        if(track.get_ListOfHBSegments().size()<Constants.NSUPERLAYERTRACKING) {
            //System.err.println(" not enough segments ");
            return;
        }
        double theta3 = track.get_ListOfHBSegments().get(track.get_ListOfHBSegments().size()-1).get_fittedCluster().get_clusterLineFitSlope();
        double theta1 = track.get_ListOfHBSegments().get(0).get_fittedCluster().get_clusterLineFitSlope();
        double deltaTheta = theta3-theta1;
       
        double thX = (track.get(0).get_Dir().x()/track.get(0).get_Dir().z());
        double thY = (track.get(0).get_Dir().y()/track.get(0).get_Dir().z());

        //positive charges bend outward for nominal GEMC field configuration
        int q = (int) Math.signum(deltaTheta); 
        q*= (int)-1*Math.signum(DCSwimmer.getTorScale()); // flip the charge according to the field scale						

        double p = track.get_pAtOrig().mag(); 
        
        double pz = p / Math.sqrt(thX*thX + thY*thY + 1);

        //System.out.println("Setting track params for ");stateVec.printInfo();
        dcSwim.SetSwimParameters(track.get(0).get_Point().x(), track.get(0).get_Point().y(), track.get(0).get_Point().z(),
                    -pz*thX, -pz*thY, -pz,
                     -q);
        
        double[] Vt = dcSwim.SwimToPlane(100);
        if(Vt==null)
            return;
        track.set_pAtOrig(new Vector3D(-Vt[3], -Vt[4], -Vt[5]));
        track.set_Vtx0(new Point3D(Vt[0], Vt[1], Vt[2]));
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
    
   
}
