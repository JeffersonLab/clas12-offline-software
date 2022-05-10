package org.jlab.rec.cvt.track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.detector.base.DetectorType;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.cross.CrossList;
import org.jlab.rec.cvt.fit.LineFitter;
import org.jlab.rec.cvt.fit.CosmicFitter;
import org.jlab.rec.cvt.fit.LineFitPars;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.rec.cvt.svt.SVTParameters;
import org.jlab.rec.cvt.trajectory.Ray;
import org.jlab.rec.cvt.trajectory.StateVec;
import org.jlab.rec.cvt.trajectory.Trajectory;
import org.jlab.rec.cvt.trajectory.TrajectoryFinder;

/**
 * A class with a method implementing an algorithm that finds lists of track
 * candidates in the BST
 *
 * @author ziegler
 *
 */
public class StraightTrackCandListFinder {

    private List<Double> X;			// x coordinate array
    private List<Double> Y;			// y coordinate array (same size as X array)
    private List<Double> Z;			// z coordinate array
    private List<Double> Rho;			// rho (= sqrt(x^2 + y^2) for SVT; detector radius for BMT) coordinate array (same size as Z array)
    private List<Double> ErrZ;		// z uncertainty (same size as Z array)
    private List<Double> ErrRho;		// rho uncertainty (same size as Z array)
    private List<Double> ErrRt;		// sqrt(x^2 + y^2)  uncertainty array (same size as X & Y arrays)
    private List<Double> Y_prime;			// y coordinate array 
    private List<Double> ErrY_prime;

    public StraightTrackCandListFinder() {
        X = new ArrayList<>();
        Y = new ArrayList<>();
        Z = new ArrayList<>();
        Rho = new ArrayList<>();
        ErrZ = new ArrayList<>();
        ErrRho = new ArrayList<>();
        ErrRt = new ArrayList<>();
        Y_prime = new ArrayList<>();
        ErrY_prime = new ArrayList<>();
    }

    /**
     * A class representing the measurement variables used to fit a straight
     * track
     *
     * @author ziegler
     *
     */
    public class RayMeasurements {

        public List<Double> _X;			// x coordinate array
        public List<Double> _Y;			// y coordinate array (same size as X array)
        public List<Double> _Z;			// z coordinate array
        public List<Double> _Y_prime;		// y coordinate array (same size as Z array)
        public List<Double> _ErrZ;		// z uncertainty (same size as Z array)
        public List<Double> _ErrY_prime;	// y uncertainty (same size as Z array)
        public List<Double> _ErrRt;		// sqrt(x^2 + y^2)  uncertainty array (same size as X & Y arrays)

        RayMeasurements(List<Double> X,
                List<Double> Y,
                List<Double> Z,
                List<Double> Y_prime,
                List<Double> ErrZ,
                List<Double> ErrY_prime,
                List<Double> ErrRt) {
            this._X = X;
            this._Y = Y;
            this._Z = Z;
            this._Y_prime = Y_prime;
            this._ErrZ = ErrZ;
            this._ErrY_prime = ErrY_prime;
            this._ErrRt = ErrRt;
        }
    }

 
    /**
     *
     * @param SVTCrosses
     * @param BMTCrosses
     * @return an array list of track candidates in the SVT
     */
    public ArrayList<StraightTrack> getStraightTracks(CrossList SVTCrosses, List<Cross> BMTCrosses) {

        ArrayList<StraightTrack> cands = new ArrayList<>();
        Map<String, StraightTrack> candMap= new HashMap<>();
        
        if (SVTCrosses.isEmpty()) {
            System.err.print("Error in estimating track candidate trajectory: less than 3 crosses found");
            return cands;
        }

        CosmicFitter fitTrk = new CosmicFitter();
        for (int i = 0; i < SVTCrosses.size(); i++) {
            ArrayList<Cross> crossesToFit = new ArrayList<>();
            // remove SVT regions
            // remove the crosses from the exluded region to fit the track
            for (Cross crossInTrackToFit : SVTCrosses.get(i)) { 
                if (crossInTrackToFit.getRegion() != SVTParameters.BSTEXCLUDEDFITREGION) {
                    crossesToFit.add(crossInTrackToFit);
                }
            }

            if (crossesToFit.size() < org.jlab.rec.cvt.Constants.MINSVTCRSFORCOSMIC) {
                continue;
            }
            
            //fitTrk = new CosmicFitter();
            RayMeasurements MeasArrays = this.getRayMeasurementsArrays(crossesToFit, false, false, false);

            LineFitter linefitYX = new LineFitter();
            boolean linefitresultYX = linefitYX.fitStatus(MeasArrays._Y, MeasArrays._X, MeasArrays._ErrRt, null, MeasArrays._Y.size());

            // prelim cross corrections
            LineFitPars linefitparsYX = linefitYX.getFit();
            StraightTrack cand = new StraightTrack(null);
            cand.addAll(crossesToFit);
            if (linefitresultYX && linefitparsYX != null) {
                cand.updateCrosses(linefitparsYX.getYXRay()); 
            }
            // update measurements
            MeasArrays = this.getRayMeasurementsArrays(crossesToFit, false, false, false);

            // fit SVt crosses
            fitTrk.fit(MeasArrays._X, MeasArrays._Y, MeasArrays._Z, MeasArrays._Y_prime, MeasArrays._ErrRt, MeasArrays._ErrY_prime, MeasArrays._ErrZ);
            //create the cand
            if (fitTrk.getray() != null) {
                //refit 
                MeasArrays = this.getRayMeasurementsArrays(crossesToFit, false, false, false);
                fitTrk.fit(MeasArrays._X, MeasArrays._Y, MeasArrays._Z, MeasArrays._Y_prime, MeasArrays._ErrRt, MeasArrays._ErrY_prime, MeasArrays._ErrZ);
   
                cand = new StraightTrack(fitTrk.getray());
                cand.addAll(crossesToFit);
            }
            if (fitTrk.getray() == null) {
                //System.err.println("Error in  Track fitting -- ray not found -- trying to refit using the uncorrected crosses...");
                MeasArrays = this.getRayMeasurementsArrays(crossesToFit, false, true, false);

                fitTrk.fit(MeasArrays._X, MeasArrays._Y, MeasArrays._Z, MeasArrays._Y_prime, MeasArrays._ErrRt, MeasArrays._ErrY_prime, MeasArrays._ErrZ);
                //create the cand
                //refit 
                MeasArrays = this.getRayMeasurementsArrays(crossesToFit, false, false, false);
                fitTrk.fit(MeasArrays._X, MeasArrays._Y, MeasArrays._Z, MeasArrays._Y_prime, MeasArrays._ErrRt, MeasArrays._ErrY_prime, MeasArrays._ErrZ);
            
                cand = new StraightTrack(fitTrk.getray());
                cand.addAll(crossesToFit);
                
                if (fitTrk.getray() == null) {
                    continue;
                }
                //System.err.println("Error in  Track fitting -- track not found -- refit FAILED");
            }
            cand.updateCrosses();
//            System.out.println(cand.getRay().toLine().origin().toString() + " " 
//                    + Math.toDegrees(cand.getRay().toLine().direction().theta()) + " " 
//                    + Math.toDegrees(cand.getRay().toLine().direction().phi()) + " ");
            // eliminate bad residuals
            this.eliminateStraightTrackOutliers(crossesToFit, fitTrk);
            if (crossesToFit.size() < org.jlab.rec.cvt.Constants.MINSVTCRSFORCOSMIC) {
                continue;
            }

            fitTrk.fit(MeasArrays._X, MeasArrays._Y, MeasArrays._Z, MeasArrays._Y_prime, MeasArrays._ErrRt, MeasArrays._ErrY_prime, MeasArrays._ErrZ);
            //create the cand
            if (fitTrk.getray() != null) { 
                cand = new StraightTrack(fitTrk.getray());
                cand.addAll(crossesToFit);
                cand.updateCrosses();
           
            }

            // match to Micromegas
            ArrayList<Cross> crossesToFitWithBMT = new ArrayList<>();
            //crossesToFitWithBMT.addAll(crossesToFit);

            //ArrayList<Cross> BMTmatches = this.matchTrackToBMT(BMTCrosses, cand, bmt_geo);
            //crossesToFitWithBMT.addAll(BMTmatches);
            ArrayList<Cross> BMTmatches = new ArrayList<>();
            ArrayList<Cross> SVTmatches = new ArrayList<>();
            // reset the arrays
            RayMeasurements NewMeasArrays = new RayMeasurements(null, null, null, null, null, null, null);

            //for (int iter = 0; iter < 11; iter++) {
                // refit with Micromegas
                crossesToFitWithBMT.clear();
                SVTmatches.clear();
                for (Cross c : cand) { 
                    if (c.getDetector()==DetectorType.BST && Constants.getInstance().SVTGEOMETRY.isInFiducial(c.getRegion()*2-1, c.getSector(), c.getPoint())) {
                        SVTmatches.add(c);
                    }
                }
                BMTmatches.clear();
                BMTmatches = this.matchTrackToBMT(cand, BMTCrosses);
                
                //crossesToFitWithBMT.addAll(SVTmatches);
                crossesToFitWithBMT.addAll(BMTmatches);

                NewMeasArrays = this.getRayMeasurementsArrays(BMTmatches, false, false, true);
                fitTrk.fit(NewMeasArrays._X, NewMeasArrays._Y, NewMeasArrays._Z, NewMeasArrays._Y_prime, NewMeasArrays._ErrRt, NewMeasArrays._ErrY_prime, NewMeasArrays._ErrZ);
                cand.addAll(crossesToFitWithBMT);
//                cand.resetCrosses();
                cand.updateCrosses();
                NewMeasArrays = this.getRayMeasurementsArrays(cand, false, false, true);
                fitTrk.fit(NewMeasArrays._X, NewMeasArrays._Y, NewMeasArrays._Z, NewMeasArrays._Y_prime, NewMeasArrays._ErrRt, NewMeasArrays._ErrY_prime, NewMeasArrays._ErrZ);
                crossesToFitWithBMT.clear();
                crossesToFitWithBMT.addAll(cand);
                //create the cand
               
                if (fitTrk.getray() != null) {
//                    System.out.println(fitTrk.getRay().toLine().origin().toString() + " " 
//                        + Math.toDegrees(fitTrk.getRay().toLine().direction().theta()) + " " 
//                        + Math.toDegrees(fitTrk.getRay().toLine().direction().phi()) + " ");
                    cand = new StraightTrack(fitTrk.getray()); 
                    cand.addAll(crossesToFitWithBMT); 
                    cand.updateCrosses();
                    //refit not using only BMT to fit the z profile
                    NewMeasArrays = this.getRayMeasurementsArrays(crossesToFitWithBMT, false, false, false);
                    fitTrk.fit(NewMeasArrays._X, NewMeasArrays._Y, NewMeasArrays._Z, NewMeasArrays._Y_prime, NewMeasArrays._ErrRt, NewMeasArrays._ErrY_prime, NewMeasArrays._ErrZ);
                    cand = new StraightTrack(fitTrk.getray()); 
                    cand.addAll(crossesToFitWithBMT);
                    cand.updateCrosses();
                    cand.setNDF(NewMeasArrays._Y.size() + NewMeasArrays._Y_prime.size() - 4);
                    cand.setCovMat(fitTrk.getray().getCovMat());
                    double chi2 = cand.calcStraightTrkChi2(); 
                    cand.setChi2(chi2);
                    cand.setId(cands.size() + 1);
                    String crossNbs = "";
                    for(int ic = 0; ic < cand.size(); ic++)
                        crossNbs+=cand.get(ic).getId()+".";
                    candMap.put(crossNbs, cand);
                }
            //}
            candMap.forEach((key,value) -> cands.add(value));
        }
        ArrayList<StraightTrack> passedcands = this.rmStraightTrkClones(true, cands);
        return passedcands;
    }

    public void updateCrossesFromTraj(StraightTrack cand, Trajectory trj) {

        double[][][] trajPlaneInters = trj.getSVTIntersections();
        ArrayList<Cross> hitsOnTrack = new ArrayList<>();
        for (Cross c : cand) {
            if (c.getDetector()==DetectorType.BST) {
                hitsOnTrack.add(c);
            }
        }

        for (int j = 0; j < hitsOnTrack.size(); j++) {

            int l1 = hitsOnTrack.get(j).getCluster1().getLayer();
            int s = hitsOnTrack.get(j).getCluster1().getSector();
            double s1 = hitsOnTrack.get(j).getCluster1().getCentroid();
            int l2 = hitsOnTrack.get(j).getCluster2().getLayer();
            double s2 = hitsOnTrack.get(j).getCluster2().getCentroid();

            double trajX1 = trajPlaneInters[l1 - 1][s - 1][0];
            double trajY1 = trajPlaneInters[l1 - 1][s - 1][1];
            double trajZ1 = trajPlaneInters[l1 - 1][s - 1][2];
            double trajX2 = trajPlaneInters[l2 - 1][s - 1][0];
            double trajY2 = trajPlaneInters[l2 - 1][s - 1][1];
            double trajZ2 = trajPlaneInters[l2 - 1][s - 1][2];

            if (trajX1 == -999 || trajX2 == -999) {
                continue;
            }

            hitsOnTrack.get(j).setDir(trj.getRay().getDirVec());

        }
    }

    List<Cross> SVTcrossesInTrk = new ArrayList<>();
    List<Cross> BMTCdetcrossesInTrk = new ArrayList<>();
    List<Cross> BMTZdetcrossesInTrk = new ArrayList<>();

    public RayMeasurements getRayMeasurementsArrays(ArrayList<Cross> arrayList, 
            boolean ignoreErr, boolean resetSVTMeas, boolean useBMTCforZonly) {

        X.clear();
        Y.clear();
        Z.clear();
        Y_prime.clear();
        ErrZ.clear();
        ErrY_prime.clear();
        ErrRt.clear();
        SVTcrossesInTrk.clear();
        BMTCdetcrossesInTrk.clear();
        BMTZdetcrossesInTrk.clear();

        //make lists
        for (Cross c : arrayList) { 
            if (c.getDetector()==DetectorType.BST) {
                SVTcrossesInTrk.add(c);
            }
            if (c.getDetector()==DetectorType.BMT) { // Micromegas
                if (c.getType() == BMTType.C) {//C-detector --> only Z defined
                    BMTCdetcrossesInTrk.add(c);
                }
                if (c.getType()==BMTType.Z) {//Z-detector --> only phi defined
                    BMTZdetcrossesInTrk.add(c);
                }
            }
        }
        
        ((ArrayList<Double>) X).ensureCapacity(SVTcrossesInTrk.size() + BMTZdetcrossesInTrk.size());
        ((ArrayList<Double>) Y).ensureCapacity(SVTcrossesInTrk.size() + BMTZdetcrossesInTrk.size());
        ((ArrayList<Double>) Z).ensureCapacity(SVTcrossesInTrk.size() + BMTCdetcrossesInTrk.size());
        ((ArrayList<Double>) ErrZ).ensureCapacity(SVTcrossesInTrk.size() + BMTCdetcrossesInTrk.size());
        ((ArrayList<Double>) ErrRt).ensureCapacity(SVTcrossesInTrk.size() + BMTZdetcrossesInTrk.size());
        ((ArrayList<Double>) Y_prime).ensureCapacity(SVTcrossesInTrk.size() + BMTCdetcrossesInTrk.size());
        ((ArrayList<Double>) ErrY_prime).ensureCapacity(SVTcrossesInTrk.size() + BMTCdetcrossesInTrk.size());

        for (int j = 0; j < SVTcrossesInTrk.size(); j++) {

            X.add(j, SVTcrossesInTrk.get(j).getPoint().x());
            Y.add(j, SVTcrossesInTrk.get(j).getPoint().y());
            Z.add(j, SVTcrossesInTrk.get(j).getPoint().z());
            Y_prime.add(j, SVTcrossesInTrk.get(j).getPoint().y());
            ErrY_prime.add(j, SVTcrossesInTrk.get(j).getPointErr().y());
            ErrRt.add(j, Math.sqrt(SVTcrossesInTrk.get(j).getPointErr().x() * SVTcrossesInTrk.get(j).getPointErr().x()
                    + SVTcrossesInTrk.get(j).getPointErr().y() * SVTcrossesInTrk.get(j).getPointErr().y()));
            ErrZ.add(j, SVTcrossesInTrk.get(j).getPointErr().z());

        }

        if (ignoreErr == true) {
            for (int j = 0; j < SVTcrossesInTrk.size(); j++) {
                ErrRt.add(j, (double) 1);
                ErrY_prime.add(j, (double) 1);
                ErrZ.add(j, (double) 1);
            }
        }

        int j0 = SVTcrossesInTrk.size();
        for (int j = j0; j < j0 + BMTZdetcrossesInTrk.size(); j++) {
            X.add(j, BMTZdetcrossesInTrk.get(j - j0).getPoint().x());
            Y.add(j, BMTZdetcrossesInTrk.get(j - j0).getPoint().y());
            ErrRt.add(j, Math.sqrt(BMTZdetcrossesInTrk.get(j - j0).getPointErr().x() * BMTZdetcrossesInTrk.get(j - j0).getPointErr().x()
                    + BMTZdetcrossesInTrk.get(j - j0).getPointErr().y() * BMTZdetcrossesInTrk.get(j - j0).getPointErr().y()));
        }

        if (ignoreErr == true) {
            for (int j = j0; j < j0 + BMTZdetcrossesInTrk.size(); j++) {
                X.add(j, (double) 1);
                Y.add(j, (double) 1);
                ErrRt.add(j, (double) 1);
            }
        }
        
        for (int j = j0; j < j0 + BMTCdetcrossesInTrk.size(); j++) {
            Z.add(j, BMTCdetcrossesInTrk.get(j - j0).getPoint().z());
            Y_prime.add(j, BMTCdetcrossesInTrk.get(j - j0).getPoint().y());
            ErrY_prime.add(j, 0.);
            ErrZ.add(j, BMTCdetcrossesInTrk.get(j - j0).getPointErr().z());
        }

        if(BMTCdetcrossesInTrk.size()>1 && useBMTCforZonly==true) {
            
            Z.clear();
            Y_prime.clear();
            ErrZ.clear();
            ErrY_prime.clear();
            for (int j = 0; j < BMTCdetcrossesInTrk.size(); j++) {
                Z.add(j, BMTCdetcrossesInTrk.get(j).getPoint().z());
                Y_prime.add(j, BMTCdetcrossesInTrk.get(j).getPoint().y());
                ErrY_prime.add(j, 0.);
                ErrZ.add(j, BMTCdetcrossesInTrk.get(j).getPointErr().z());

            }
        }
        RayMeasurements MeasArray = new RayMeasurements(X, Y, Z, Y_prime, ErrZ, ErrY_prime, ErrRt);

        return MeasArray;
    }

    /**
     *
     * @param bmtCrosses the BMT crosses
     * @param thecand the straight track candidate
     * @return an arraylist of BMT crosses matched to the track
     */
    public ArrayList<Cross> matchTrackToBMT(StraightTrack thecand, List<Cross> bmtCrosses) {

        ArrayList<Cross> BMTCrossList = new ArrayList<>();
        if (thecand == null) {
            return BMTCrossList;
        }

        if (bmtCrosses == null || bmtCrosses.isEmpty()) {
            return BMTCrossList;
        }

        // for configuration in 3 double SVT layers + 3 double BMT layers
        //------------------------------------------------------------------------------------------------------
        for(int iregion = 0; iregion < 3; iregion++) {
            int region = iregion + 1;
            
            // match Z crosses
            List<Cross> zCross = this.matchTrackToBMT(Constants.getInstance().BMTGEOMETRY.getLayer(region, BMTType.Z), thecand.getRay(), bmtCrosses, 
                                 Constants.COSMICSMINRESIDUALX/Constants.getInstance().BMTGEOMETRY.getRadiusMidDrift(Constants.getInstance().BMTGEOMETRY.getLayer(region, BMTType.Z)));
            if(zCross!=null) 
                BMTCrossList.addAll(zCross);
            
            // match Z crosses
            List<Cross> cCross = this.matchTrackToBMT(Constants.getInstance().BMTGEOMETRY.getLayer(region, BMTType.C), thecand.getRay(), bmtCrosses, Constants.getInstance().COSMICSMINRESIDUALZ);
            if(cCross!=null) 
                BMTCrossList.addAll(cCross);
        } 
        return BMTCrossList;
    }

    private List<Cross> matchTrackToBMT(int layer, Ray ray, List<Cross> crosses, double matchCut) {
        
        List<Cross> matched = new ArrayList<>();
        
        for(int is=0; is<Constants.getInstance().BMTGEOMETRY.getNSectors(); is++) {
            // get ray intersection with cross tile
            int sector = is+1;
            List<Point3D> trajs = new ArrayList<>();
            int ntrajs = Constants.getInstance().BMTGEOMETRY.getTileSurface(layer, sector).intersection(ray.toLine(), trajs);
            if(ntrajs==0) continue;
            
            // find the closests cross-trajectory match
            for(Point3D traj : trajs) {
                Cross closestCross = null;
                double doca = Double.MAX_VALUE;
        
                // loop over all BMT crosses
                for(Cross c : crosses) {
                    Cluster cluster = c.getCluster1();
                    // consider only the ones in the selected layer
                    if(cluster.getLayer()!=layer || cluster.getSector()!=sector) continue;

                    double residual = cluster.residual(traj);
                    if(Math.abs(residual)<doca) {
                        doca = Math.abs(residual);
                        closestCross = c;
                    }
                }
                if(doca<matchCut) {
                    closestCross.updateBMTCross(traj, ray.getDirVec());
                    matched.add(closestCross);
                }
            }
        }
        return matched;
    }

    private ArrayList<StraightTrack> rmStraightTrkClones(boolean removeClones, ArrayList<StraightTrack> cands) {
        ArrayList<StraightTrack> passedcands = new ArrayList<>();
        if (removeClones == false) {
            passedcands = cands;
        }

        if (removeClones == true) {
            if (cands.size() > 0) {
                // clean up duplicates:
                for (int k = 0; k < cands.size(); k++) {
                    for (int k2 = 0; k2 < cands.size(); k2++) {
                        if (k2 == k) {
                            continue;
                        }
                        int overlaps = 0; 
                        for (int k3 = 0; k3 < cands.get(k).size(); k3++) {
                            if (cands.get(k2).containsCross(cands.get(k).get(k3))) {
                                overlaps++;

                            }
                        }
                        if (overlaps > 1) {
                            if ((cands.get(k2).getChi2()) > (cands.get(k).getChi2())) {

                                cands.get(k2).setId(-999);

                            }
                        }
                    }
                }
                for (int k = 0; k < cands.size(); k++) {
                    if (cands.get(k).getId() != -999) {
                        passedcands.add(cands.get(k));

                    }
                }
            }
        }

        return passedcands;
    }

    private void eliminateStraightTrackOutliers(ArrayList<Cross> crosses, CosmicFitter track) {
        ArrayList<Cross> toKeep = new ArrayList<>();
        
        for(int j = 0; j < crosses.size(); j++) {
            Cross c = crosses.get(j);
            int layer  = c.getCluster1().getLayer();
            int sector = c.getSector();
            
            Point3D traj = new Point3D();
            int ntraj = Constants.getInstance().SVTGEOMETRY.getPlane(layer, sector).intersection(track.getray().toLine(), traj);
            
            if(ntraj!=1) 
                c.reset();
            else if(!Constants.getInstance().SVTGEOMETRY.isInFiducial(layer, sector, traj)) 
                c.reset();
            else {
                Vector3D distance = Constants.getInstance().SVTGEOMETRY.toLocal(layer, sector, c.getPoint().vectorTo(traj));
                if(Math.abs(distance.x())>Constants.getInstance().COSMICSMINRESIDUALX ||
                   Math.abs(distance.z())>Constants.COSMICSMINRESIDUALZ) c.reset();
                else
                    toKeep.add(c);
            }
        }
        crosses = toKeep;
    }

    public void matchClusters(List<Cluster> sVTclusters, TrajectoryFinder tf, boolean trajFinal,
            ArrayList<StateVec> trajectory, int k) {
        if(trajectory == null)
            return;
        Collections.sort(sVTclusters);
        for (StateVec st : trajectory) { 
            for (Cluster cls : sVTclusters) {
                if (cls.getAssociatedTrackID() != -1) {
                    continue;
                }
                if (st.getSurfaceSector() != cls.getSector()) {
                    continue;
                }
                if (st.getSurfaceLayer() != cls.getLayer()) {
                    continue;
                }
                if (Math.abs(st.getCalcCentroidStrip() - cls.getCentroid()) < 4) {
                    tf.setHitResolParams("SVT", cls.getSector(), cls.getLayer(), cls, st, trajFinal);
                    //System.out.println("trying to associate a cluster ");cls.printInfo(); System.out.println(" to "+st.getCalcCentroidStrip()+" dStp = "+(st.getCalcCentroidStrip()-cls.getCentroid()));
                    cls.setAssociatedTrackID(k);
                    for (Hit h : cls) {
                        h.setAssociatedTrackID(k);
                    }
                }
            }
        }
    }

}
