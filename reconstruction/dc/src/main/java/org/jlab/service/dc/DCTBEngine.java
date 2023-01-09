package org.jlab.service.dc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.jnp.matrix.Matrix;
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
import org.jlab.rec.dc.trajectory.StateVec;
import org.jlab.rec.dc.trajectory.Trajectory;
import org.jlab.rec.dc.trajectory.TrajectoryFinder;
import org.jlab.utils.groups.IndexedTable;

import org.jlab.clas.tracking.kalmanfilter.Surface;
import org.jlab.clas.tracking.kalmanfilter.zReference.KFitter;
import org.jlab.clas.tracking.kalmanfilter.zReference.StateVecs;
import org.jlab.clas.tracking.utilities.MatrixOps.Libr;
import org.jlab.clas.tracking.utilities.RungeKuttaDoca;

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
        if(Constants.getInstance().getT2D()==0) {
            TableLoader.Fill(this.getConstantsManager().getConstants(run, Constants.TIME2DIST));
        } else {
        TableLoader.Fill(this.getConstantsManager().getConstants(run, Constants.T2DPRESSURE),
                this.getConstantsManager().getConstants(run, Constants.T2DPRESSUREREF),
                this.getConstantsManager().getConstants(run, Constants.PRESSURE));
        }
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

        HitReader hitRead = new HitReader(this.getBanks(), super.getConstantsManager(), Constants.getInstance().dcDetector); //vz; modified reader to read regular or ai hits
        hitRead.read_HBHits(event, tde);
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
        Track[] TrackArray = new Track[trkrows];
        for (int i = 0; i < trkrows; i++) {
            Track HBtrk = new Track();
            HBtrk.set_Id(trkbank.getShort("id", i));
            HBtrk.setSector(trkbank.getByte("sector", i));
            HBtrk.set_Q(trkbank.getByte("q", i));
            HBtrk.set_pAtOrig(new Vector3D(trkbank.getFloat("p0_x", i), trkbank.getFloat("p0_y", i), trkbank.getFloat("p0_z", i)));
            HBtrk.set_P(HBtrk.get_pAtOrig().mag());
            HBtrk.set_Vtx0(new Point3D(trkbank.getFloat("Vtx0_x", i), trkbank.getFloat("Vtx0_y", i), trkbank.getFloat("Vtx0_z", i)));
            HBtrk.set_FitChi2(trkbank.getFloat("chi2", i));
            StateVec HBFinalSV = new StateVec(trkbank.getFloat("x", i), trkbank.getFloat("y", i), 
                    trkbank.getFloat("tx", i), trkbank.getFloat("ty", i));
            HBFinalSV.setZ(trkbank.getFloat("z", i));
            HBtrk.setFinalStateVec(HBFinalSV);
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
        double beamXoffset, beamYoffset;
	IndexedTable beamOffset = this.getConstantsManager().getConstants(run, Constants.BEAMPOS);
        beamXoffset = beamOffset.getDoubleValue("x_offset", 0, 0, 0);
        beamYoffset = beamOffset.getDoubleValue("y_offset", 0, 0, 0);
        if(event.hasBank("RASTER::position")){
            DataBank raster_bank = event.getBank("RASTER::position");
            beamXoffset += raster_bank.getFloat("x", 0);
            beamYoffset += raster_bank.getFloat("y", 0);
        }
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
            
			KFitter kFZRef = new KFitter(true, 30, 1, dcSwim, Constants.getInstance().Z, Libr.JNP);
			List<Surface> measSurfaces = getMeasSurfaces(TrackArray1, Constants.getInstance().dcDetector);
			StateVecs svs = new StateVecs();
			org.jlab.clas.tracking.kalmanfilter.AStateVecs.StateVec initSV = svs.new StateVec(0);
			getInitState(TrackArray1, measSurfaces.get(0).z, initSV, kFZRef, dcSwim, new float[3]);
			kFZRef.initFromHB(measSurfaces, initSV, TrackArray1.get(0).get(0).get(0).get_Beta());			
			kFZRef.runFitter();
			List<org.jlab.rec.dc.trajectory.StateVec> kfStateVecsAlongTrajectory = setKFStateVecsAlongTrajectory(kFZRef);			
			
            StateVec fn = new StateVec(); 
            if (kFZRef.setFitFailed==false && kFZRef.finalStateVec!=null) { 
                // set the state vector at the last measurement site
                fn.set(kFZRef.finalStateVec.x, kFZRef.finalStateVec.y, kFZRef.finalStateVec.tx, kFZRef.finalStateVec.ty); 
                //set the track parameters if the filter does not fail
                TrackArray1.set_P(1./Math.abs(kFZRef.finalStateVec.Q));
                TrackArray1.set_Q((int)Math.signum(kFZRef.finalStateVec.Q));                
                
                trkcandFinder.setTrackPars(TrackArray1, new Trajectory(), trjFind, fn, kFZRef.finalStateVec.z, Constants.getInstance().dcDetector, dcSwim, beamXoffset, beamYoffset);
                // candidate parameters are set from the state vector
                if (TrackArray1.fit_Successful == false) {
                    continue;
                }                
                
                TrackArray1.set_FitChi2(kFZRef.chi2);
                TrackArray1.set_FitNDF(kFZRef.NDF);
                TrackArray1.setStateVecs(kfStateVecsAlongTrajectory);
                TrackArray1.set_FitConvergenceStatus(kFZRef.ConvStatus);
                if (TrackArray1.get_Vtx0().toVector3D().mag() > 500) {
                    continue;
                }
                
                // get CovMat at vertex
                Point3D VTCS = crosses.get(0).getCoordsInSector(TrackArray1.get_Vtx0().x(), TrackArray1.get_Vtx0().y(), TrackArray1.get_Vtx0().z());
                TrackArray1.set_CovMat(kFZRef.propagateToVtx(crosses.get(0).get_Sector(), VTCS.z()));
                
                if (TrackArray1.isGood()) {
                    trkcands.add(TrackArray1);
                }

            }
              		
        }        
    	
        if(!trkcands.isEmpty()) {
            //trkcandFinder.removeOverlappingTracks(trkcands);		// remove overlaps        	        	
            for(Track trk: trkcands) {
                int trkId = trk.get_Id();
                // reset the id
                //trk.set_Id(trkId);
                trkcandFinder.matchHits(trk.getStateVecs(), trk, Constants.getInstance().dcDetector, dcSwim);
                trk.calcTrajectory(trkId, dcSwim, trk.get_Vtx0(), trk.get_pAtOrig(), trk.get_Q());
                LOGGER.log(Level.FINE, trk.toString());               

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

    public List<org.jlab.rec.dc.trajectory.StateVec> setKFStateVecsAlongTrajectory(KFitter kFZRef) {
    	List<org.jlab.rec.dc.trajectory.StateVec> kfStateVecsAlongTrajectory = new ArrayList<>();
    	
    	for(int i = 0; i < kFZRef.kfStateVecsAlongTrajectory.size(); i++) {
    		org.jlab.clas.tracking.kalmanfilter.AStateVecs.StateVec svc = kFZRef.kfStateVecsAlongTrajectory.get(i);
    		
    		org.jlab.rec.dc.trajectory.StateVec sv = new org.jlab.rec.dc.trajectory.StateVec(svc.x, svc.y, svc.tx, svc.ty);
            sv.setZ(svc.z);
            sv.setB(svc.B);
            sv.setPathLength(svc.getPathLength());   
            kfStateVecsAlongTrajectory.add(sv);
    	}
    	
    	return kfStateVecsAlongTrajectory;
    }
    
    public void getInitState(Track trkcand, double z0, org.jlab.clas.tracking.kalmanfilter.AStateVecs.StateVec initStateVec, KFitter kf, Swim dcSwim, float[] bf) {
        if (trkcand != null && trkcand.getFinalStateVec()!=null ) {
            initStateVec.x = trkcand.getFinalStateVec().x();
            initStateVec.y = trkcand.getFinalStateVec().y();
            initStateVec.z = trkcand.getFinalStateVec().getZ();
            initStateVec.tx = trkcand.getFinalStateVec().tanThetaX();
            initStateVec.ty = trkcand.getFinalStateVec().tanThetaY();
            initStateVec.Q = ((double) trkcand.get_Q())/trkcand.get_P();

            RungeKuttaDoca rk = new RungeKuttaDoca();
            rk.SwimToZ(trkcand.get(0).get_Sector(), initStateVec, dcSwim, z0, bf);

            //initCM.covMat = trkcand.get_CovMat();
            //test
            StateVecs svs = new StateVecs();
            org.jlab.clas.tracking.kalmanfilter.AStateVecs.StateVec rinitSV = svs.new StateVec(0);
            rinitSV.x = trkcand.getFinalStateVec().x();
            rinitSV.y = trkcand.getFinalStateVec().y();
            rinitSV.z = trkcand.getFinalStateVec().getZ();
            rinitSV.tx = trkcand.getFinalStateVec().tanThetaX();
            rinitSV.ty = trkcand.getFinalStateVec().tanThetaY();
            rinitSV.Q = ((double) trkcand.get_Q())/trkcand.get_P();
            double[] FTF = new double[25];
            double[] F = this.F(trkcand.get(0).get_Sector(), z0, rinitSV, rk, dcSwim, bf);
            for(int i = 0; i<5; i++) {
                FTF[i*5+i]=F[i]*F[i];

            }

            //Matrix initCMatrix = new Matrix(FTF);
            Matrix initCMatrix = new Matrix();
            initCMatrix.set(FTF);
            initStateVec.CM = initCMatrix;
            
        } else {
            kf.setFitFailed = true;
        }

    }
    
    private org.jlab.clas.tracking.kalmanfilter.AStateVecs.StateVec reset(org.jlab.clas.tracking.kalmanfilter.AStateVecs.StateVec sv, org.jlab.clas.tracking.kalmanfilter.AStateVecs.StateVec stateVec) {
    	StateVecs svs = new StateVecs();
        sv = svs.new StateVec(stateVec.k);
        sv.x = stateVec.x;
        sv.y = stateVec.y;
        sv.tx = stateVec.tx;
        sv.ty = stateVec.ty;
        sv.z = stateVec.z;
        sv.Q = stateVec.Q;

        return sv;
    }
    private void swimToSite(int sector, double z0,
    		org.jlab.clas.tracking.kalmanfilter.AStateVecs.StateVec SVplus, org.jlab.clas.tracking.kalmanfilter.AStateVecs.StateVec SVminus, RungeKuttaDoca rk, Swim dcSwim, float[] bf) {

        rk.SwimToZ(sector, SVplus, dcSwim, z0, bf);
        rk.SwimToZ(sector, SVminus, dcSwim, z0, bf);
    }
    
    double[] F(int sector, double z0, org.jlab.clas.tracking.kalmanfilter.AStateVecs.StateVec stateVec, RungeKuttaDoca rk, Swim dcSwim, float[] bf) {
        double[] _F = new double[5];
        org.jlab.clas.tracking.kalmanfilter.AStateVecs.StateVec SVplus = null;
        org.jlab.clas.tracking.kalmanfilter.AStateVecs.StateVec SVminus = null;

        SVplus = this.reset(SVplus, stateVec);
        SVminus = this.reset(SVminus, stateVec);

        double delt_x = 0.05;
        SVplus.x += delt_x/2.;
        SVminus.x-= delt_x/2.;

        this.swimToSite(sector, z0, SVplus, SVminus, rk, dcSwim, bf);

        _F[0] = (SVplus.x - SVminus.x)/delt_x;

        SVplus = this.reset(SVplus, stateVec);
        SVminus = this.reset(SVminus, stateVec);

        double delt_y = 0.05;
        SVplus.y += delt_y/2.;
        SVminus.y-= delt_y/2.;

        this.swimToSite(sector, z0, SVplus, SVminus, rk, dcSwim, bf);

        _F[1] = (SVplus.y - SVminus.y)/delt_y;

        SVplus = this.reset(SVplus, stateVec);
        SVminus = this.reset(SVminus, stateVec);

        double delt_tx = 0.001;
        SVplus.tx += delt_tx/2.;
        SVminus.tx-= delt_tx/2.;

        this.swimToSite(sector, z0, SVplus, SVminus, rk, dcSwim, bf);

        _F[2] = (SVplus.tx - SVminus.tx)/delt_tx;

        SVplus = this.reset(SVplus, stateVec);
        SVminus = this.reset(SVminus, stateVec);

        double delt_ty = 0.001;
        SVplus.ty += delt_ty/2.;
        SVminus.ty-= delt_ty/2.;

        this.swimToSite(sector, z0, SVplus, SVminus, rk, dcSwim, bf);

        _F[3] = (SVplus.ty - SVminus.ty)/delt_ty;

        SVplus = this.reset(SVplus, stateVec);
        SVminus = this.reset(SVminus, stateVec);


        _F[4] = 0.01/Math.abs(SVplus.Q);

        return _F;

    }
        
    public List<Surface> getMeasSurfaces(Track trk, DCGeant4Factory DcDetector) { 
        List<HitOnTrack> hOTS = new ArrayList<>(); // the list of hits on track		
        FittedHit hitOnTrk;
        for(int s = 0; s < trk.get_ListOfHBSegments().size(); s++) {
            for(int h = 0; h < trk.get_ListOfHBSegments().get(s).size(); h++) { 
                trk.get_ListOfHBSegments().get(s).get(h).calc_CellSize(DcDetector);
                hitOnTrk = trk.get_ListOfHBSegments().get(s).get(h); 
                int slayr = trk.get_ListOfHBSegments().get(s).get(h).get_Superlayer();

                double sl1 = trk.get_ListOfHBSegments().get(s).get_fittedCluster().get_clusterLineFitSlope();
                double it1 = trk.get_ListOfHBSegments().get(s).get_fittedCluster().get_clusterLineFitIntercept();

                double Z = hitOnTrk.get_Z();
                double X = sl1 * Z + it1;
                HitOnTrack hot = new HitOnTrack(slayr, X, Z, 
                        hitOnTrk.get_WireMaxSag(),
                        hitOnTrk.get_WireLine());
                
                hot._doca[0] = trk.get_ListOfHBSegments().get(s).get(h).get_Doca();
                
                double LR = Math.signum(trk.get_ListOfHBSegments().get(s).get(h).get_XWire()-trk.get_ListOfHBSegments().get(s).get(h).get_X());
                hot._doca[0]*=LR;
                hot._hitError = trk.get_ListOfHBSegments().get(s).get(h).get_DocaErr()*trk.get_ListOfHBSegments().get(s).get(h).get_DocaErr();
                //LOGGER.log(Level.FINE, " Z "+Z+" ferr "+(float)(hot._Unc /(hot._hitError/4.)));
                hot._Unc[0] = hot._hitError;
                hot.region = trk.get_ListOfHBSegments().get(s).get(h).get_Region();
				hot.sector = trk.get_ListOfHBSegments().get(s).get(h).get_Sector();
				hot.superlayer = trk.get_ListOfHBSegments().get(s).get(h).get_Superlayer();
				hot.layer = trk.get_ListOfHBSegments().get(s).get(h).get_Layer();   
                hOTS.add(hot);                
            }
        }
                                       
        Collections.sort(hOTS); // sort the collection in order of increasing Z value (i.e. going downstream from the target)
        // identify double hits and take the average position		
        for (int i = 0; i < hOTS.size(); i++) {
            if (i > 0) {
                if (Math.abs(hOTS.get(i - 1)._Z - hOTS.get(i)._Z)<0.01) {
                    hOTS.get(i - 1)._doca[1] = hOTS.get(i)._doca[0];
                    hOTS.get(i - 1)._Unc[1] = hOTS.get(i)._Unc[0];
                    hOTS.get(i - 1)._wireLine[1] = hOTS.get(i)._wireLine[0];
                    hOTS.remove(i);
                    hOTS.get(i - 1).nMeas = 2;
                }
            }
        }                

        List<Surface> surfaces = new ArrayList<>(hOTS.size());
        
    	for (int i = 0; i < hOTS.size(); i++) {
    		Surface surf = new Surface(hOTS.get(i).region, hOTS.get(i)._Z, hOTS.get(i)._X, hOTS.get(i)._tilt, hOTS.get(i)._wireMaxSag, 
    				hOTS.get(i)._doca, hOTS.get(i)._Unc, hOTS.get(i)._hitError, hOTS.get(i)._wireLine);
    		surf.setSector(hOTS.get(i).sector);
    		surf.setSuperLayer(hOTS.get(i).superlayer);
    		surf.setLayer(hOTS.get(i).layer);
    		surf.setNMeas(hOTS.get(i).nMeas);
    		surfaces.add(i, surf);
    	}

    	return surfaces;
    }
    
    public class HitOnTrack implements Comparable<HitOnTrack> {

    	public double _hitError;
    	public double _X;
    	public double _Z;
    	public double[] _Unc = new double[2];
    	public double _tilt;
    	public double[] _doca = new double[2];
    	public double _wireMaxSag;
    	public Line3D[] _wireLine = new Line3D[2];
    	public int region;
    	public int superlayer;
    	public int sector;
    	public int layer;
    	public int nMeas = 1;


    	public HitOnTrack(int superlayer, double X, double Z, double wiremaxsag, Line3D wireLine) {
    		_X = X;
    		_Z = Z;
    		_wireMaxSag = wiremaxsag;
    		_wireLine[0] = wireLine;
    		_doca[0] = -99;
    		_doca[1] = -99;
    		_Unc[0] = 1;
    		_Unc[1] = 1;

    		//use stereo angle in fit based on wire direction
    		_tilt = 90-Math.toDegrees(wireLine.direction().asUnit().angle(new Vector3D(1,0,0)));
    	}

    	@Override
    	public int compareTo(HitOnTrack o) {
    		if (this._Z == o._Z) {
    			return 0;
    		}
    		if (this._Z > o._Z) {
    			return 1;
    		} else {
    			return -1;
    		}
    	}
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
