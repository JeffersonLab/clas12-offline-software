package org.jlab.service.dc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
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
import org.jlab.rec.dc.timetodistance.TableLoader;
import org.jlab.rec.dc.timetodistance.TimeToDistanceEstimator;
import org.jlab.rec.dc.track.Track;
import org.jlab.rec.dc.track.TrackCandListFinder;
import org.jlab.rec.dc.track.fit.KFitterDoca;
import org.jlab.rec.dc.trajectory.StateVec;
import org.jlab.rec.dc.trajectory.Trajectory;
import org.jlab.rec.dc.trajectory.TrajectoryFinder;
import org.jlab.utils.groups.IndexedTable;

public class DCTBEngine extends DCEngine {
    
    private TimeToDistanceEstimator tde = null;

    public DCTBEngine(String trking) {
        super(trking);
        tde = new TimeToDistanceEstimator();
    }
    public DCTBEngine() {
        super("DCTB");
        this.getBanks().init("TimeBasedTrkg", "HB", "TB");
        tde = new TimeToDistanceEstimator();
    }
    
    @Override
    public void setDropBanks() {
        super.registerOutputBank(this.getBanks().getHitsBank());
        super.registerOutputBank(this.getBanks().getClustersBank());
        super.registerOutputBank(this.getBanks().getSegmentsBank());
        super.registerOutputBank(this.getBanks().getCrossesBank());
        super.registerOutputBank(this.getBanks().getTracksBank());
        super.registerOutputBank(this.getBanks().getCovmatBank());
        super.registerOutputBank(this.getBanks().getTrajBank());
    }
    
    @Override
    public boolean processDataEvent(DataEvent event) {

        int run = this.getRun(event);
        if(run==0) return true;
        
        double T_Start = 0;
        if(Constants.getInstance().isUSETSTART() == true) {
            String recBankName = this.getBanks().getRecEventBank();
            if(event.hasBank(recBankName)==true) {
                T_Start = event.getBank(recBankName).getFloat("startTime", 0);
                if(T_Start<0) { 
                    return true; // quit if start time not found in data
                }
            } else { 
                return true; // no REC HB bank
            }
        }
        // get Field
        Swim dcSwim = new Swim();        
       
        // fill T2D table
        TableLoader.Fill(this.getConstantsManager().getConstants(run, Constants.TIME2DIST));

        ClusterFitter cf = new ClusterFitter();
        ClusterCleanerUtilities ct = new ClusterCleanerUtilities();

        List<FittedHit> fhits = new ArrayList<>();	
        List<FittedCluster> clusters = new ArrayList<>();
        List<Segment> segments = new ArrayList<>();
        List<Cross> crosses = new ArrayList<>();
        List<Track> trkcands = new ArrayList<>();
        
        LOGGER.log(Level.FINE, "TB AI "+ this.getName());
        //instantiate bank writer
        RecoBankWriter rbc = new RecoBankWriter(this.getBanks());

        HitReader hitRead = new HitReader(this.getBanks()); //vz; modified reader to read regular or ai hits
        hitRead.read_HBHits(event, 
            this.getConstantsManager().getConstants(run, Constants.DOCARES),
            this.getConstantsManager().getConstants(run, Constants.TIME2DIST),
            this.getConstantsManager().getConstants(run, Constants.T0CORRECTION),
            Constants.getInstance().dcDetector, tde);
        //I) get the hits
        List<FittedHit> hits = hitRead.get_HBHits();
        //II) process the hits
        //1) exit if hit list is empty
        if(hits.isEmpty() ) {
            return true;
        }

        //2) find the clusters from these hits
        ClusterFinder clusFinder = new ClusterFinder();

        clusters = clusFinder.FindTimeBasedClusters(event, hits, cf, ct, 
                this.getConstantsManager().getConstants(run, Constants.TIME2DIST), Constants.getInstance().dcDetector, tde);
        for(FittedCluster c : clusters) {
            c.set_Id(c.get(0).get_AssociatedClusterID());
        }
        if(clusters.isEmpty()) {
            rbc.fillAllTBBanks(event, hits, null, null, null, null);
            return true;
        }

        //3) find the segments from the fitted clusters
        SegmentFinder segFinder = new SegmentFinder();

        List<FittedCluster> pclusters = segFinder.selectTimeBasedSegments(clusters);

        segments =  segFinder.get_Segments(pclusters, event, Constants.getInstance().dcDetector, false);

        if(segments.isEmpty()) { // need 6 segments to make a trajectory
            for(FittedCluster c : clusters) {					
                for(FittedHit hit : c) {		
                    hit.set_AssociatedClusterID(c.get_Id());
                    hit.set_AssociatedHBTrackID(c.get(0).get_AssociatedHBTrackID());
                    fhits.add(hit);						
                }
            }
            rbc.fillAllTBBanks( event, fhits, clusters, null, null, null);
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
        if (event.hasBank(this.getBanks().getInputTracksBank()) == false) { 
            return true;
        }
        
        DataBank trkbank = event.getBank(this.getBanks().getInputTracksBank());
        //DataBank trkcovbank = event.getBank("TimeBasedTrkg::TBCovMat");
        int trkrows = trkbank.rows();
        //if(trkbank.rows()!=trkcovbank.rows()) {
        //    return true; // HB tracks not saved correctly
        //}
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
//            Matrix initCMatrix = new Matrix();
//            initCMatrix.set(new double[][]{
//            {trkcovbank.getFloat("C11", i), trkcovbank.getFloat("C12", i), trkcovbank.getFloat("C13", i), trkcovbank.getFloat("C14", i), trkcovbank.getFloat("C15", i)},
//            {trkcovbank.getFloat("C21", i), trkcovbank.getFloat("C22", i), trkcovbank.getFloat("C23", i), trkcovbank.getFloat("C24", i), trkcovbank.getFloat("C25", i)},
//            {trkcovbank.getFloat("C31", i), trkcovbank.getFloat("C32", i), trkcovbank.getFloat("C33", i), trkcovbank.getFloat("C34", i), trkcovbank.getFloat("C35", i)},
//            {trkcovbank.getFloat("C41", i), trkcovbank.getFloat("C42", i), trkcovbank.getFloat("C43", i), trkcovbank.getFloat("C44", i), trkcovbank.getFloat("C45", i)},
//            {trkcovbank.getFloat("C51", i), trkcovbank.getFloat("C52", i), trkcovbank.getFloat("C53", i), trkcovbank.getFloat("C54", i), trkcovbank.getFloat("C55", i)}
//            });
//            HBtrk.set_CovMat(initCMatrix);
            TrackArray[HBtrk.get_Id()-1] = HBtrk; 
            TrackArray[HBtrk.get_Id()-1].set_Status(0);
        }
        if(TrackArray==null) {
            return true; // HB tracks not saved correctly
        }
        for(Segment seg : segments) {
            if(seg.get(0).get_AssociatedHBTrackID()>0) {
                    TrackArray[seg.get(0).get_AssociatedHBTrackID()-1].get_ListOfHBSegments().add(seg); 
                    if(seg.get_Status()==1)
                        TrackArray[seg.get(0).get_AssociatedHBTrackID()-1].set_Status(1);
            }
        }
        
        //6) find the list of  track candidates
        // read beam offsets from database
        IndexedTable beamOffset = this.getConstantsManager().getConstants(run, Constants.BEAMPOS);
        double beamXoffset = beamOffset.getDoubleValue("x_offset", 0,0,0);
        double beamYoffset = beamOffset.getDoubleValue("y_offset", 0,0,0);
        TrackCandListFinder trkcandFinder = new TrackCandListFinder("TimeBased");
        TrajectoryFinder trjFind = new TrajectoryFinder();
        for (Track TrackArray1 : TrackArray) {
            if (TrackArray1 == null || TrackArray1.get_ListOfHBSegments() == null || TrackArray1.get_ListOfHBSegments().size() < 5) {
                continue;
            }
            TrackArray1.set_MissingSuperlayer(get_Status(TrackArray1));
            TrackArray1.addAll(crossMake.find_Crosses(TrackArray1.get_ListOfHBSegments(), Constants.getInstance().dcDetector));
            if (TrackArray1.size() < 1) {
                continue;
            }
            crosses.addAll(TrackArray1);
            //if(TrackArray[i].get_FitChi2()>200) {
            //    resetTrackParams(TrackArray[i], new DCSwimmer());
            //}
            KFitterDoca kFit = new KFitterDoca(TrackArray1, Constants.getInstance().dcDetector, true, dcSwim, 0);
            StateVec fn = new StateVec();
            kFit.runFitter(TrackArray1.get(0).get_Sector());
            if (kFit.setFitFailed==false && kFit.finalStateVec!=null) { 
                // set the state vector at the last measurement site
                fn.set(kFit.finalStateVec.x, kFit.finalStateVec.y, kFit.finalStateVec.tx, kFit.finalStateVec.ty); 
                //set the track parameters if the filter does not fail
                TrackArray1.set_P(1./Math.abs(kFit.finalStateVec.Q));
                TrackArray1.set_Q((int)Math.signum(kFit.finalStateVec.Q));
                trkcandFinder.setTrackPars(TrackArray1, new Trajectory(), trjFind, fn, kFit.finalStateVec.z, Constants.getInstance().dcDetector, dcSwim, beamXoffset, beamYoffset);
                // candidate parameters are set from the state vector
                if (TrackArray1.fit_Successful == false) {
                    continue;
                }
                TrackArray1.set_FitChi2(kFit.chi2);
                TrackArray1.set_FitNDF(kFit.NDF);
                TrackArray1.set_Trajectory(kFit.kfStateVecsAlongTrajectory);
                TrackArray1.set_FitConvergenceStatus(kFit.ConvStatus);
                //TrackArray[i].set_Id(TrackArray[i].size()+1);
                //TrackArray[i].set_CovMat(kFit.finalCovMat.covMat);
                if (TrackArray1.get_Vtx0().toVector3D().mag() > 500) {
                    continue;
                }
                // get CovMat at vertex
                Point3D VTCS = crosses.get(0).getCoordsInSector(TrackArray1.get_Vtx0().x(), TrackArray1.get_Vtx0().y(), TrackArray1.get_Vtx0().z());
                TrackArray1.set_CovMat(kFit.propagateToVtx(crosses.get(0).get_Sector(), VTCS.z()));
                if (TrackArray1.isGood()) {
                    trkcands.add(TrackArray1);
                }
            }
        }
        
        
        //for(int i = 0; i < crosses.size(); i++) {
        //    crosses.get(i).set_Id(i+1);
        //}
        // track found	
        //int trkId = 1;

        if(trkcands.size()>0) {
            //trkcandFinder.removeOverlappingTracks(trkcands);		// remove overlaps
            for(Track trk: trkcands) {
                int trkId = trk.get_Id();
                // reset the id
                //trk.set_Id(trkId);
                trkcandFinder.matchHits(trk.get_Trajectory(), trk, Constants.getInstance().dcDetector, dcSwim);
                trk.calcTrajectory(trkId, dcSwim, trk.get_Vtx0().x(), trk.get_Vtx0().y(), trk.get_Vtx0().z(), 
                        trk.get_pAtOrig().x(), trk.get_pAtOrig().y(), trk.get_pAtOrig().z(), trk.get_Q(), 
                        Constants.getInstance().tSurf);
//                for(int j = 0; j< trk.trajectory.size(); j++) {
//                LOGGER.log(Level.FINE, trk.get_Id()+" "+trk.trajectory.size()+" ("+trk.trajectory.get(j).getDetId()+") ["+
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
                    c.set_CrossDirIntersSegWires();
                    c.get_Segment1().isOnTrack=true;
                    c.get_Segment2().isOnTrack=true;
                    for (FittedHit h1 : c.get_Segment1()) {
                        h1.set_AssociatedTBTrackID(trk.get_Id());
                    }
                    for (FittedHit h2 : c.get_Segment2()) {
                        h2.set_AssociatedTBTrackID(trk.get_Id());
                    }
                                        
                    if (c.get_Segment1().get_Id() == -1) {
                        trk.set_MissingSuperlayer(c.get_Segment1().get_Superlayer());
                        trk.setSingleSuperlayer(c.get_Segment2());
                    }
                    if (c.get_Segment2().get_Id() == -1) {
                        trk.set_MissingSuperlayer(c.get_Segment2().get_Superlayer());
                        trk.setSingleSuperlayer(c.get_Segment1());
                    }
                }
            }
            this.ensureTrackUnique(trkcands);
        }    
       
        this.ensureUniqueness(fhits, clusters, segments, crosses);
        if(trkcands.isEmpty()) {

            rbc.fillAllTBBanks(event, fhits, clusters, segments, crosses, null); // no cand found, stop here and save the hits, the clusters, the segments, the crosses
            return true;
        }
        
        rbc.fillAllTBBanks(event, fhits, clusters, segments, crosses, trkcands);

        return true;
    }

    

    private int get_Status(Track track) {
        int miss = 0;    
        
        int L[] = new int[6];
        Map<Integer, Segment> SegMap = new HashMap<>();
        for(int l = 0; l<track.get_ListOfHBSegments().size(); l++) {
            L[track.get_ListOfHBSegments().get(l).get_Superlayer()-1]++;
            SegMap.put(track.get_ListOfHBSegments().get(l).get_Superlayer(), track.get_ListOfHBSegments().get(l));
        }
        for(int l = 0; l<6; l++) {
            if(L[l]==0){
                miss=l+1;
                if(miss%2==0 && SegMap.containsKey(l)) {       //missing sl in 2,4,6
                    track.setSingleSuperlayer(SegMap.get(l));  //isolated sl in 1,3,5
                    LOGGER.log(Level.FINE, "Missing superlayer "+miss+" seg "+SegMap.get(l).printInfo());
                } 
                else if(miss%2==1 && SegMap.containsKey(l+2)) { //missing sl in 1,3,5
                    track.setSingleSuperlayer(SegMap.get(l+2)); //isolated sl in 2,4,6
                    LOGGER.log(Level.FINE, "Missing superlayer "+miss+" seg "+track.getSingleSuperlayer().printInfo());
                }
            }
        } 
        return miss;
    }

    private void ensureTrackUnique(List<Track> trkcands) {
        List<Track> rmFrmList = new ArrayList<>();
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
    private void ensureUniqueness(List<FittedHit> fhits, List<FittedCluster> clusters, List<Segment> segments, List<Cross> crosses) {
        
        Map<Integer, FittedHit> hitsMap = new HashMap<>();
        for(FittedHit h : fhits) {
            if(hitsMap.get(h.get_Id())==null) {
                hitsMap.put(h.get_Id(), h);
            } else {
                if(h.get_AssociatedHBTrackID()!=-1)
                    hitsMap.replace(h.get_Id(), h);
            }
             
        }
        
        fhits.removeAll(fhits);
        for (Map.Entry<Integer, FittedHit> entry : hitsMap.entrySet()) {
            fhits.add(entry.getValue());
        }
        
        Map<Integer, FittedCluster> clsMap = new HashMap<>();
        for(FittedCluster clus : clusters) {
            if(clsMap.get(clus.get_Id())==null) {
                clsMap.put(clus.get_Id(), clus);
            } else {
                if(clus.get(0).get_AssociatedHBTrackID()!=-1)
                    clsMap.replace(clus.get_Id(), clus);
            }
             
        }
        
        clusters.removeAll(clusters);
        for (Map.Entry<Integer, FittedCluster> entry : clsMap.entrySet()) {
            clusters.add(entry.getValue());
        }
        
        Map<Integer, Segment> segMap = new HashMap<>();
        for(Segment seg : segments) {
            if(segMap.get(seg.get_Id())==null) {
                segMap.put(seg.get_Id(), seg);
            } else {
                if(seg.get(0).get_AssociatedHBTrackID()!=-1)
                    segMap.replace(seg.get_Id(), seg);
            }
             
        }
        
        segments.removeAll(segments);
        for (Map.Entry<Integer, Segment> entry : segMap.entrySet()) {
            segments.add(entry.getValue());
        }
        
        Map<Integer, Cross> crsMap = new HashMap<>();
        for(Cross cr : crosses) {
            if(crsMap.get(cr.get_Id())==null) {
                crsMap.put(cr.get_Id(), cr);
            } else {
                if(cr.get_Segment1().get(0).get_AssociatedHBTrackID()!=-1 
                        && cr.get_Segment1().get(0).get_AssociatedHBTrackID()==cr.get_Segment2().get(0).get_AssociatedHBTrackID())
                    crsMap.replace(cr.get_Id(), cr);
            }
             
        }
        
        crosses.removeAll(crosses);
        for (Map.Entry<Integer, Cross> entry : crsMap.entrySet()) {
            crosses.add(entry.getValue());
        }
        
    }
}
