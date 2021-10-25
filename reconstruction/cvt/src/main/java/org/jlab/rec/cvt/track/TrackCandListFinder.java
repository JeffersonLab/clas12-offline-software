package org.jlab.rec.cvt.track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.geant4.v2.CTOFGeant4Factory;
import org.jlab.geom.base.Detector;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.bmt.BMTConstants;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.cross.CrossList;
import org.jlab.rec.cvt.fit.HelicalTrackFitter;
import org.jlab.rec.cvt.fit.LineFitter;
import org.jlab.rec.cvt.fit.CosmicFitter;
import org.jlab.rec.cvt.fit.LineFitPars;
import org.jlab.rec.cvt.hit.FittedHit;
import org.jlab.rec.cvt.svt.SVTGeometry;
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
public class TrackCandListFinder {

    public TrackCandListFinder() {
        X = new ArrayList<Double>();
        Y = new ArrayList<Double>();
        Z = new ArrayList<Double>();
        Rho = new ArrayList<Double>();
        ErrZ = new ArrayList<Double>();
        ErrRho = new ArrayList<Double>();
        ErrRt = new ArrayList<Double>();
        Y_prime = new ArrayList<Double>();
        ErrY_prime = new ArrayList<Double>();
    }

    private List<Double> X;			// x coordinate array
    private List<Double> Y;			// y coordinate array (same size as X array)
    private List<Double> Z;			// z coordinate array
    private List<Double> Rho;			// rho (= sqrt(x^2 + y^2) for SVT; detector radius for BMT) coordinate array (same size as Z array)
    private List<Double> ErrZ;		// z uncertainty (same size as Z array)
    private List<Double> ErrRho;		// rho uncertainty (same size as Z array)
    private List<Double> ErrRt;		// sqrt(x^2 + y^2)  uncertainty array (same size as X & Y arrays)
    private List<Double> Y_prime;			// y coordinate array 
    private List<Double> ErrY_prime;

    /**
     * A class representing the measurement variables used to fit a helical
     * track The track fitting algorithm employs the Karimaki algorithm to
     * determine the circle fit paramters: the doca to the z axis, the phi at
     * the doca, the curvature; and a linear regression line fitting algorithm
     * to determine z at the doca and the helix dip angle
     *
     * @author ziegler
     *
     */
    private class HelixMeasurements {

        List<Double> _X;			// x coordinate array
        List<Double> _Y;			// y coordinate array (same size as X array)
        List<Double> _Z;			// z coordinate array
        List<Double> _Rho;		// rho (= sqrt(x^2 + y^2) for SVT; detector radius for BMT) coordinate array (same size as Z array)
        List<Double> _ErrZ;		// z uncertainty (same size as Z array)
        List<Double> _ErrRho;		// rho uncertainty (same size as Z array)
        List<Double> _ErrRt;		// sqrt(x^2 + y^2)  uncertainty array (same size as X & Y arrays)

        HelixMeasurements(List<Double> X,
                List<Double> Y,
                List<Double> Z,
                List<Double> Rho,
                List<Double> ErrZ,
                List<Double> ErrRho,
                List<Double> ErrRt) {
            this._X = X;
            this._Y = Y;
            this._Z = Z;
            this._Rho = Rho;
            this._ErrZ = ErrZ;
            this._ErrRho = ErrRho;
            this._ErrRt = ErrRt;
        }
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
     * NOT Used
     * @param list the input list of crosses
     * @return an array list of track candidates in the SVT
     */
    public void getHelicalTrack(Seed cand, SVTGeometry svt_geo, BMTGeometry bmt_geo) {
        X.clear();
        Y.clear();
        Z.clear();
        Rho.clear();
        ErrZ.clear();
        ErrRho.clear();
        ErrRt.clear();

        List<Cross> list = cand.get_Crosses();

        if (list.size() == 0) {
            //System.err.print("Error in estimating track candidate trajectory: less than 3 crosses found");
            return;
        }
        // instantiating the Helical Track fitter
        HelicalTrackFitter fitTrk = new HelicalTrackFitter();
        // sets the index according to assumption that the track comes from the origin or not
        int shift = 0;
        //	if(org.jlab.rec.cvt.BMTConstants.trk_comesfrmOrig)
        //		shift =1;

        // interate the fit a number of times set in the constants file
        int Max_Number_Of_Iterations = SVTParameters.BSTTRKINGNUMBERITERATIONS;

        // loop over the cross list and do the fits to the crosses
        if (list.size() >= 3) {
            // for debugging purposes only sets all errors to 1
            boolean ignoreErr = SVTParameters.ignoreErr;

            int Number_Of_Iterations = 0;
            // do till the number of iterations is reached			
            while (Number_Of_Iterations <= Max_Number_Of_Iterations) {
                Number_Of_Iterations++;
                fitTrk = new HelicalTrackFitter();
                // get the measuerement arrays for the helical track fit
                HelixMeasurements MeasArrays = this.get_HelixMeasurementsArrays(list, shift, ignoreErr, false);

                X = MeasArrays._X;
                Y = MeasArrays._Y;
                Z = MeasArrays._Z;
                Rho = MeasArrays._Rho;
                ErrZ = MeasArrays._ErrZ;
                ErrRho = MeasArrays._ErrRho;
                ErrRt = MeasArrays._ErrRt;

                // do the fit to X, Y taking ErrRt uncertainties into account to get the circle fit params, 
                // and do the fit to Rho, Z taking into account the uncertainties in Rho and Z into account to get the linefit params
                fitTrk.fit(X, Y, Z, Rho, ErrRt, ErrRho, ErrZ);

                // if the fit failed then use the uncorrected SVT points since the z-correction in resetting the SVT cross points sometimes fails 
                if (fitTrk.get_helix() == null) {
                    //System.err.println("Error in Helical Track fitting -- helix not found -- trying to refit using the uncorrected crosses...");
                    MeasArrays = this.get_HelixMeasurementsArrays(list, shift, ignoreErr, true);

                    X = MeasArrays._X;
                    Y = MeasArrays._Y;
                    Z = MeasArrays._Z;
                    Rho = MeasArrays._Rho;
                    ErrZ = MeasArrays._ErrZ;
                    ErrRho = MeasArrays._ErrRho;
                    ErrRt = MeasArrays._ErrRt;

                    fitTrk.fit(X, Y, Z, Rho, ErrRt, ErrRho, ErrZ);
                    Number_Of_Iterations = Max_Number_Of_Iterations + 1;
                    //if(fitTrk.get_helix()==null) 
                    //System.err.println("Error in Helical Track fitting -- helix not found -- refit FAILED");
                }

                // if the fit is successful
                if (fitTrk.get_helix() != null && fitTrk.getFit() != null) {
                    cand.set_Helix(fitTrk.get_helix());
                }
            }
        }
        // remove clones

    }

    /**
     *
     * @param crossList the input list of crosses
     * @return an array list of track candidates in the SVT
     */
    public ArrayList<Track> getHelicalTracks(CrossList crossList, 
            SVTGeometry svt_geo, BMTGeometry bmt_geo,
            CTOFGeant4Factory ctof_geo, Detector cnd_geo,
            Swim swimmer) {

        X.clear();
        Y.clear();
        Z.clear();
        Rho.clear();
        ErrZ.clear();
        ErrRho.clear();
        ErrRt.clear();

        ArrayList<Track> cands = new ArrayList<Track>();

        if (crossList.size() == 0) {
            System.err.print("Error in estimating track candidate trajectory: less than 3 crosses found");
            return cands;
        }
        // instantiating the Helical Track fitter
        HelicalTrackFitter fitTrk = new HelicalTrackFitter();
        // sets the index according to assumption that the track comes from the origin or not
        int shift = 0;
        if (Constants.trk_comesfrmOrig) {
            shift = 1;
        }

        // interate the fit a number of times set in the constants file
        int Max_Number_Of_Iterations = SVTParameters.BSTTRKINGNUMBERITERATIONS;

        // loop over the cross list and do the fits to the crosses
        for (int i = 0; i < crossList.size(); i++) {
            // for debugging purposes only sets all errors to 1
            boolean ignoreErr = SVTParameters.ignoreErr;

            int Number_Of_Iterations = 0;
            // do till the number of iterations is reached			
            while (Number_Of_Iterations <= Max_Number_Of_Iterations) {
                Number_Of_Iterations++;
                fitTrk = new HelicalTrackFitter();
                // get the measuerement arrays for the helical track fit
                HelixMeasurements MeasArrays = this.get_HelixMeasurementsArrays(crossList.get(i), shift, ignoreErr, false);

                X = MeasArrays._X;
                Y = MeasArrays._Y;
                Z = MeasArrays._Z;
                Rho = MeasArrays._Rho;
                ErrZ = MeasArrays._ErrZ;
                ErrRho = MeasArrays._ErrRho;
                ErrRt = MeasArrays._ErrRt;

                // do the fit to X, Y taking ErrRt uncertainties into account to get the circle fit params, 
                // and do the fit to Rho, Z taking into account the uncertainties in Rho and Z into account to get the linefit params
                fitTrk.fit(X, Y, Z, Rho, ErrRt, ErrRho, ErrZ);

                // if the fit failed then use the uncorrected SVT points since the z-correction in resetting the SVT cross points sometimes fails 
                if (fitTrk.get_helix() == null) {
                    //System.err.println("Error in Helical Track fitting -- helix not found -- trying to refit using the uncorrected crosses...");
                    MeasArrays = this.get_HelixMeasurementsArrays(crossList.get(i), shift, ignoreErr, true);

                    X = MeasArrays._X;
                    Y = MeasArrays._Y;
                    Z = MeasArrays._Z;
                    Rho = MeasArrays._Rho;
                    ErrZ = MeasArrays._ErrZ;
                    ErrRho = MeasArrays._ErrRho;
                    ErrRt = MeasArrays._ErrRt;

                    fitTrk.fit(X, Y, Z, Rho, ErrRt, ErrRho, ErrZ);
                    Number_Of_Iterations = Max_Number_Of_Iterations + 1;
                    //if(fitTrk.get_helix()==null) 
                    //System.err.println("Error in Helical Track fitting -- helix not found -- refit FAILED");
                }

                // if the fit is successful
                if (fitTrk.get_helix() != null && fitTrk.getFit() != null) {
                    Track cand = new Track(fitTrk.get_helix());
                    cand.addAll(crossList.get(i));
                    //cand.set_HelicalTrack(fitTrk.get_helix());			done in Track constructor			
                    //cand.update_Crosses(svt_geo);

                    cand.set_circleFitChi2PerNDF(fitTrk.get_chisq()[0] / (int) (X.size() - 3)); // 3 fit params					
                    cand.set_lineFitChi2PerNDF(fitTrk.get_chisq()[1] / (int) (Z.size() - 2)); // 2 fit params

                    if (Number_Of_Iterations == Max_Number_Of_Iterations) {
                        cands.add(cand); // dump the cand							
                    }
                }
            }
        }
        // remove clones
        ArrayList<Track> passedcands = this.rmHelicalTrkClones(SVTParameters.removeClones, cands);
        // loop over candidates and set the trajectories
        
//        for (int ic = 0; ic < passedcands.size(); ic++) {
//            Helix trkHelix = passedcands.get(ic).get_helix();
//            if(trkHelix!=null) {
//                TrajectoryFinder trjFind = new TrajectoryFinder();
//
//                //Trajectory traj = trjFind.findTrajectory(passedcands.get(ic).get_Id(), trkHelix, passedcands.get(ic), svt_geo, bmt_geo, "final");
//                Trajectory traj = trjFind.findTrajectory(ic+1, passedcands.get(ic), svt_geo, bmt_geo, ctof_geo, cnd_geo, swimmer, "final");
//
//                passedcands.get(ic).set_Trajectory(traj.get_Trajectory());
//
//                passedcands.get(ic).set_Id(ic+1);
//            }
//
//        }

        return passedcands;

    }

    /**
     *
     * @param SVTCrossList the input list of crosses
     * @return an array list of track candidates in the SVT
     */
    public ArrayList<StraightTrack> getStraightTracks(CrossList SVTCrosses, List<Cross> BMTCrosses, SVTGeometry svt_geo, BMTGeometry bmt_geo) {

        ArrayList<StraightTrack> cands = new ArrayList<StraightTrack>();
        Map<String, StraightTrack> candMap= new HashMap<String, StraightTrack>();
        
        if (SVTCrosses.size() == 0) {
            System.err.print("Error in estimating track candidate trajectory: less than 3 crosses found");
            return cands;
        }

        CosmicFitter fitTrk = new CosmicFitter();
        for (int i = 0; i < SVTCrosses.size(); i++) {
            ArrayList<Cross> crossesToFit = new ArrayList<Cross>();
            // remove SVT regions
            // remove the crosses from the exluded region to fit the track
            for (Cross crossInTrackToFit : SVTCrosses.get(i)) { 
                if (crossInTrackToFit.get_Region() != SVTParameters.BSTEXCLUDEDFITREGION) {
                    crossesToFit.add(crossInTrackToFit);
                }
            }

            if (crossesToFit.size() < 3) {
                continue;
            }

            //fitTrk = new CosmicFitter();
            RayMeasurements MeasArrays = this.get_RayMeasurementsArrays(crossesToFit, false, false, false);

            LineFitter linefitYX = new LineFitter();
            boolean linefitresultYX = linefitYX.fitStatus(MeasArrays._Y, MeasArrays._X, MeasArrays._ErrRt, null, MeasArrays._Y.size());

            // prelim cross corrections
            LineFitPars linefitparsYX = linefitYX.getFit();
            StraightTrack cand = new StraightTrack(null);
            cand.addAll(crossesToFit);
            if (linefitresultYX && linefitparsYX != null) {
                cand.update_Crosses(linefitparsYX.getYXRay(), svt_geo); 
            }
            // update measurements
            MeasArrays = this.get_RayMeasurementsArrays(crossesToFit, false, false, false);

            // fit SVt crosses
            fitTrk.fit(MeasArrays._X, MeasArrays._Y, MeasArrays._Z, MeasArrays._Y_prime, MeasArrays._ErrRt, MeasArrays._ErrY_prime, MeasArrays._ErrZ);
            //create the cand
            if (fitTrk.get_ray() != null) {
                //refit 
                MeasArrays = this.get_RayMeasurementsArrays(crossesToFit, false, false, false);
                fitTrk.fit(MeasArrays._X, MeasArrays._Y, MeasArrays._Z, MeasArrays._Y_prime, MeasArrays._ErrRt, MeasArrays._ErrY_prime, MeasArrays._ErrZ);
   
                cand = new StraightTrack(fitTrk.get_ray());
                cand.addAll(crossesToFit);
            }
            if (fitTrk.get_ray() == null) {
                //System.err.println("Error in  Track fitting -- ray not found -- trying to refit using the uncorrected crosses...");
                MeasArrays = this.get_RayMeasurementsArrays(crossesToFit, false, true, false);

                fitTrk.fit(MeasArrays._X, MeasArrays._Y, MeasArrays._Z, MeasArrays._Y_prime, MeasArrays._ErrRt, MeasArrays._ErrY_prime, MeasArrays._ErrZ);
                //create the cand
                //refit 
                MeasArrays = this.get_RayMeasurementsArrays(crossesToFit, false, false, false);
                fitTrk.fit(MeasArrays._X, MeasArrays._Y, MeasArrays._Z, MeasArrays._Y_prime, MeasArrays._ErrRt, MeasArrays._ErrY_prime, MeasArrays._ErrZ);
            
                cand = new StraightTrack(fitTrk.get_ray());
                cand.addAll(crossesToFit);
                
                if (fitTrk.get_ray() == null) {
                    continue;
                }
                //System.err.println("Error in  Track fitting -- track not found -- refit FAILED");
            }
            cand.update_Crosses(cand.get_ray(), svt_geo);
//            System.out.println(cand.get_ray().toLine().origin().toString() + " " 
//                    + Math.toDegrees(cand.get_ray().toLine().direction().theta()) + " " 
//                    + Math.toDegrees(cand.get_ray().toLine().direction().phi()) + " ");
            // eliminate bad residuals
            this.EliminateStraightTrackOutliers(crossesToFit, fitTrk, svt_geo);
            if (crossesToFit.size() < 3) {
                continue;
            }

            fitTrk.fit(MeasArrays._X, MeasArrays._Y, MeasArrays._Z, MeasArrays._Y_prime, MeasArrays._ErrRt, MeasArrays._ErrY_prime, MeasArrays._ErrZ);
            //create the cand
            if (fitTrk.get_ray() != null) { 
                cand = new StraightTrack(fitTrk.get_ray());
                cand.addAll(crossesToFit);
                cand.update_Crosses(cand.get_ray(), svt_geo);
           
            }

            // match to Micromegas
            ArrayList<Cross> crossesToFitWithBMT = new ArrayList<Cross>();
            //crossesToFitWithBMT.addAll(crossesToFit);

            //ArrayList<Cross> BMTmatches = this.matchTrackToBMT(BMTCrosses, cand, bmt_geo);
            //crossesToFitWithBMT.addAll(BMTmatches);
            ArrayList<Cross> BMTmatches = new ArrayList<Cross>();
            ArrayList<Cross> SVTmatches = new ArrayList<Cross>();
            // reset the arrays
            RayMeasurements NewMeasArrays = new RayMeasurements(null, null, null, null, null, null, null);

            //for (int iter = 0; iter < 11; iter++) {
                // refit with Micromegas
                crossesToFitWithBMT.clear();
                SVTmatches.clear();
                for (Cross c : cand) { 
                    if (c.get_Detector()==DetectorType.BST && svt_geo.isInFiducial(c.get_Region()*2-1, c.get_Sector(), c.get_Point())) {
                        SVTmatches.add(c);
                    }
                }
                BMTmatches.clear();
                BMTmatches = this.matchTrackToBMT(cand, BMTCrosses, bmt_geo);
                
                //crossesToFitWithBMT.addAll(SVTmatches);
                crossesToFitWithBMT.addAll(BMTmatches);

                NewMeasArrays = this.get_RayMeasurementsArrays(BMTmatches, false, false, true);
                fitTrk.fit(NewMeasArrays._X, NewMeasArrays._Y, NewMeasArrays._Z, NewMeasArrays._Y_prime, NewMeasArrays._ErrRt, NewMeasArrays._ErrY_prime, NewMeasArrays._ErrZ);
                cand.addAll(crossesToFitWithBMT);
//                cand.reset_Crosses();
                cand.update_Crosses(cand.get_ray(), svt_geo);
                NewMeasArrays = this.get_RayMeasurementsArrays(cand, false, false, true);
                fitTrk.fit(NewMeasArrays._X, NewMeasArrays._Y, NewMeasArrays._Z, NewMeasArrays._Y_prime, NewMeasArrays._ErrRt, NewMeasArrays._ErrY_prime, NewMeasArrays._ErrZ);
                crossesToFitWithBMT.clear();
                crossesToFitWithBMT.addAll(cand);
                //create the cand
               
                if (fitTrk.get_ray() != null) {
//                    System.out.println(fitTrk.get_ray().toLine().origin().toString() + " " 
//                        + Math.toDegrees(fitTrk.get_ray().toLine().direction().theta()) + " " 
//                        + Math.toDegrees(fitTrk.get_ray().toLine().direction().phi()) + " ");
                    cand = new StraightTrack(fitTrk.get_ray()); 
                    cand.addAll(crossesToFitWithBMT); 
                    cand.update_Crosses(cand.get_ray(), svt_geo);
                    //refit not using only BMT to fit the z profile
                    NewMeasArrays = this.get_RayMeasurementsArrays(crossesToFitWithBMT, false, false, false);
                    fitTrk.fit(NewMeasArrays._X, NewMeasArrays._Y, NewMeasArrays._Z, NewMeasArrays._Y_prime, NewMeasArrays._ErrRt, NewMeasArrays._ErrY_prime, NewMeasArrays._ErrZ);
                    cand = new StraightTrack(fitTrk.get_ray()); 
                    cand.addAll(crossesToFitWithBMT);
                    cand.update_Crosses(cand.get_ray(), svt_geo);
                    cand.set_ndf(NewMeasArrays._Y.size() + NewMeasArrays._Y_prime.size() - 4);
                    double chi2 = cand.calc_straightTrkChi2(); 
                    cand.set_chi2(chi2);
                    cand.set_Id(cands.size() + 1);
                    String crossNbs = "";
                    for(int ic = 0; ic < cand.size(); ic++)
                        crossNbs+=cand.get(ic).get_Id()+".";
                    candMap.put(crossNbs, cand);
                }
            //}
            candMap.forEach((key,value) -> cands.add(value));
        }

        //ArrayList<StraightTrack> passedcands = this.rmStraightTrkClones(org.jlab.rec.cvt.svt.BMTConstants.removeClones, cands);
        ArrayList<StraightTrack> passedcands = this.rmStraightTrkClones(true, cands);

//        for (int ic = 0; ic < passedcands.size(); ic++) {
//            
//            Ray trkRay = passedcands.get(ic).get_ray();
//            if(trkRay!=null) {
//                TrajectoryFinder trjFind = new TrajectoryFinder();
//
//                //Trajectory traj = trjFind.findTrajectory(passedcands.get(ic).get_Id(), trkRay, passedcands.get(ic), svt_geo, bmt_geo);
//                Trajectory traj = trjFind.findTrajectory(ic+1, trkRay, passedcands.get(ic), svt_geo, bmt_geo);
//
//                passedcands.get(ic).set_Trajectory(traj.get_Trajectory());
//
//                passedcands.get(ic).set_Id(ic+1);
//
//                this.upDateCrossesFromTraj(passedcands.get(ic), traj, svt_geo);
//            }
//
//        }

        return passedcands;
    }

    public void upDateCrossesFromTraj(StraightTrack cand, Trajectory trj, SVTGeometry geo) {

        double[][][] trajPlaneInters = trj.get_SVTIntersections();
        ArrayList<Cross> hitsOnTrack = new ArrayList<Cross>();
        for (Cross c : cand) {
            if (c.get_Detector()==DetectorType.BST) {
                hitsOnTrack.add(c);
            }
        }

        for (int j = 0; j < hitsOnTrack.size(); j++) {

            int l1 = hitsOnTrack.get(j).get_Cluster1().get_Layer();
            int s = hitsOnTrack.get(j).get_Cluster1().get_Sector();
            double s1 = hitsOnTrack.get(j).get_Cluster1().get_Centroid();
            int l2 = hitsOnTrack.get(j).get_Cluster2().get_Layer();
            double s2 = hitsOnTrack.get(j).get_Cluster2().get_Centroid();

            double trajX1 = trajPlaneInters[l1 - 1][s - 1][0];
            double trajY1 = trajPlaneInters[l1 - 1][s - 1][1];
            double trajZ1 = trajPlaneInters[l1 - 1][s - 1][2];
            double trajX2 = trajPlaneInters[l2 - 1][s - 1][0];
            double trajY2 = trajPlaneInters[l2 - 1][s - 1][1];
            double trajZ2 = trajPlaneInters[l2 - 1][s - 1][2];

            if (trajX1 == -999 || trajX2 == -999) {
                continue;
            }

            //Point3D Point = geo.recalcCrossFromTrajectoryIntersWithModulePlanes(s, s1, s2, l1, l2, trajX1, trajY1, trajZ1, trajX2, trajY2, trajZ2);

            //set the cross to that point
            //System.out.println(" trajX1 "+trajX1+" trajY1 "+trajY1+" trajZ1 "+trajZ1+" trajX2 "+trajX2+" trajY2 "+trajY2+" trajZ2 "+trajZ2);
            //hitsOnTrack.get(j).set_Point(Point);

            hitsOnTrack.get(j).set_Dir(trj.get_ray().get_dirVec());

        }
    }

    private HelixMeasurements get_HelixMeasurementsArrays(List<Cross> list,
            int shift, boolean ignoreErr, boolean resetSVTMeas) {
        X.clear();
        Y.clear();
        Z.clear();
        Rho.clear();
        ErrZ.clear();
        ErrRho.clear();
        ErrRt.clear();
        SVTcrossesInTrk.clear();
        BMTCdetcrossesInTrk.clear();
        BMTZdetcrossesInTrk.clear();

        //make lists
        for (Cross c : list) {

            if (c.get_Detector()==DetectorType.BST) {
                SVTcrossesInTrk.add(c);
            }
            if (c.get_Detector()==DetectorType.BMT) { // Micromegas
                if (c.get_Type() == BMTType.C) {//C-detector --> only Z defined
                    BMTCdetcrossesInTrk.add(c);
                }
                if (c.get_Type() == BMTType.Z) {//Z-detector --> only phi defined
                    BMTZdetcrossesInTrk.add(c);
                }
            }
        }

        ((ArrayList<Double>) X).ensureCapacity(SVTcrossesInTrk.size() + BMTZdetcrossesInTrk.size());
        ((ArrayList<Double>) Y).ensureCapacity(SVTcrossesInTrk.size() + BMTZdetcrossesInTrk.size());
        ((ArrayList<Double>) Z).ensureCapacity(SVTcrossesInTrk.size() + BMTCdetcrossesInTrk.size());
        ((ArrayList<Double>) ErrZ).ensureCapacity(SVTcrossesInTrk.size() + BMTCdetcrossesInTrk.size());
        ((ArrayList<Double>) ErrRt).ensureCapacity(SVTcrossesInTrk.size() + BMTZdetcrossesInTrk.size());
        ((ArrayList<Double>) Rho).ensureCapacity(SVTcrossesInTrk.size() + BMTCdetcrossesInTrk.size());
        ((ArrayList<Double>) ErrRho).ensureCapacity(SVTcrossesInTrk.size() + BMTCdetcrossesInTrk.size());

        ((ArrayList<Double>) X).ensureCapacity(SVTcrossesInTrk.size() + BMTZdetcrossesInTrk.size() + shift);
        ((ArrayList<Double>) Y).ensureCapacity(SVTcrossesInTrk.size() + BMTZdetcrossesInTrk.size() + shift);
        ((ArrayList<Double>) Z).ensureCapacity(SVTcrossesInTrk.size() + BMTCdetcrossesInTrk.size() );
        ((ArrayList<Double>) Rho).ensureCapacity(SVTcrossesInTrk.size() + BMTCdetcrossesInTrk.size() + shift);
        ((ArrayList<Double>) ErrZ).ensureCapacity(SVTcrossesInTrk.size() + BMTCdetcrossesInTrk.size() );
        ((ArrayList<Double>) ErrRho).ensureCapacity(SVTcrossesInTrk.size() + BMTCdetcrossesInTrk.size() + shift);
        ((ArrayList<Double>) ErrRt).ensureCapacity(SVTcrossesInTrk.size() + BMTZdetcrossesInTrk.size() + shift);

        if (shift == 1) {
            //X.add(0, (double) 0);
            //Y.add(0, (double) 0);
            double xb = Constants.getXb();
            double yb = Constants.getYb();
            double rb = Math.sqrt(xb*xb+yb*yb);
            X.add(0, (double) Constants.getXb());
            Y.add(0, (double) Constants.getYb());
            //Z.add(0, (double) 0);
            //Rho.add(0, (double) 0);
            Rho.add(0, (double) rb);
            //ErrRt.add(0, org.jlab.rec.cvt.svt.BMTConstants.RHOVTXCONSTRAINT);
            ErrRt.add(0, Constants.getRbErr());
            //ErrZ.add(0, org.jlab.rec.cvt.svt.BMTConstants.ZVTXCONSTRAINT);
            //ErrRho.add(0, org.jlab.rec.cvt.svt.BMTConstants.RHOVTXCONSTRAINT);
            ErrRho.add(0, Constants.getRbErr());
        }

        for (int j = shift; j < shift + SVTcrossesInTrk.size(); j++) {

            X.add(j, SVTcrossesInTrk.get(j - shift).get_Point().x());
            Y.add(j, SVTcrossesInTrk.get(j - shift).get_Point().y());
            Z.add(j, SVTcrossesInTrk.get(j - shift-1).get_Point().z());
            Rho.add(j, Math.sqrt(SVTcrossesInTrk.get(j - shift).get_Point().x() * SVTcrossesInTrk.get(j - shift).get_Point().x()
                    + SVTcrossesInTrk.get(j - shift).get_Point().y() * SVTcrossesInTrk.get(j - shift).get_Point().y()));
            ErrRho.add(j, Math.sqrt(SVTcrossesInTrk.get(j - shift).get_PointErr().x() * SVTcrossesInTrk.get(j - shift).get_PointErr().x()
                    + SVTcrossesInTrk.get(j - shift).get_PointErr().y() * SVTcrossesInTrk.get(j - shift).get_PointErr().y()));
            ErrRt.add(j, Math.sqrt(SVTcrossesInTrk.get(j - shift).get_PointErr().x() * SVTcrossesInTrk.get(j - shift).get_PointErr().x()
                    + SVTcrossesInTrk.get(j - shift).get_PointErr().y() * SVTcrossesInTrk.get(j - shift).get_PointErr().y()));
            ErrZ.add(j, SVTcrossesInTrk.get(j - shift-1).get_PointErr().z());

        }

        if (ignoreErr == true) {
            for (int j = 0; j < shift + SVTcrossesInTrk.size(); j++) {
                ErrRt.add(j, (double) 1);
                ErrRho.add(j, (double) 1);
                ErrZ.add(j, (double) 1);
            }
        }
        int j0 = SVTcrossesInTrk.size();
        for (int j = shift + j0; j < shift + j0 + BMTZdetcrossesInTrk.size(); j++) {
            X.add(j, BMTZdetcrossesInTrk.get(j - shift - j0).get_Point().x());
            Y.add(j, BMTZdetcrossesInTrk.get(j - shift - j0).get_Point().y());
            ErrRt.add(j, Math.sqrt(BMTZdetcrossesInTrk.get(j - shift - j0).get_PointErr().x() * BMTZdetcrossesInTrk.get(j - shift - j0).get_PointErr().x()
                    + BMTZdetcrossesInTrk.get(j - shift - j0).get_PointErr().y() * BMTZdetcrossesInTrk.get(j - shift - j0).get_PointErr().y()));
        }

        if (ignoreErr == true) {
            for (int j = shift + SVTcrossesInTrk.size(); j < shift + SVTcrossesInTrk.size() + BMTZdetcrossesInTrk.size(); j++) {
                X.add(j, (double) 1);
                Y.add(j, (double) 1);
                ErrRt.add(j, (double) 1);
            }
        }

        for (int j = shift + j0; j < shift + j0 + BMTCdetcrossesInTrk.size(); j++) {
            Z.add(j, BMTCdetcrossesInTrk.get(j - shift - j0).get_Point().z());
            Rho.add(j, BMTConstants.getCRCRADIUS()[BMTCdetcrossesInTrk.get(j - shift - j0).get_Region() - 1] + BMTConstants.hDrift/2);
            System.out.println(BMTCdetcrossesInTrk.get(j - shift - j0).get_Point().toString() + " " + BMTConstants.getCRCRADIUS()[BMTCdetcrossesInTrk.get(j - shift - j0).get_Region() - 1]);
            ErrRho.add(j, 0.01); // check this error on thickness measurement					
            ErrZ.add(j, BMTCdetcrossesInTrk.get(j - shift - j0).get_PointErr().z());

        }
        if (resetSVTMeas) {
            //System.err.println("Error in Helical Track fitting -- helix not found -- trying to refit using the uncorrected crosses...");
            for (int j = shift; j < shift + j0; j++) {
                X.add(j, SVTcrossesInTrk.get(j - shift).get_Point0().x());
                Y.add(j, SVTcrossesInTrk.get(j - shift).get_Point0().y());
                Z.add(j, SVTcrossesInTrk.get(j - shift-1).get_Point0().z());
                ErrRho.add(j, Math.sqrt(SVTcrossesInTrk.get(j - shift).get_PointErr().x() * SVTcrossesInTrk.get(j - shift).get_PointErr().x()
                        + SVTcrossesInTrk.get(j - shift).get_PointErr().y() * SVTcrossesInTrk.get(j - shift).get_PointErr().y()));
                ErrZ.add(j, SVTcrossesInTrk.get(j - shift-1).get_PointErr0().z());
            }
        }
        //	if(BMTConstants.DEBUGMODE) {
        //		System.out.println(" FIT ARRAYS ");
        //		for(int i =0; i<X.length; i++)
        //			System.out.println("X["+i+"] = "+X[i]+ "  Y["+i+"] = "+Y[i]);
        //		for(int i =0; i<Z.length; i++)
        //			System.out.println("Rho["+i+"] = "+Rho[i]+ "  Z["+i+"] = "+Z[i]);
        //	}
        HelixMeasurements MeasArray = new HelixMeasurements(X, Y, Z, Rho, ErrZ, ErrRho, ErrRt);

        return MeasArray;
    }

    List<Cross> SVTcrossesInTrk = new ArrayList<Cross>();
    List<Cross> BMTCdetcrossesInTrk = new ArrayList<Cross>();
    List<Cross> BMTZdetcrossesInTrk = new ArrayList<Cross>();

    public RayMeasurements get_RayMeasurementsArrays(ArrayList<Cross> arrayList, 
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
            if (c.get_Detector()==DetectorType.BST) {
                SVTcrossesInTrk.add(c);
            }
            if (c.get_Detector()==DetectorType.BMT) { // Micromegas
                if (c.get_Type() == BMTType.C) {//C-detector --> only Z defined
                    BMTCdetcrossesInTrk.add(c);
                }
                if (c.get_Type()==BMTType.Z) {//Z-detector --> only phi defined
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

            X.add(j, SVTcrossesInTrk.get(j).get_Point().x());
            Y.add(j, SVTcrossesInTrk.get(j).get_Point().y());
            Z.add(j, SVTcrossesInTrk.get(j).get_Point().z());
            Y_prime.add(j, SVTcrossesInTrk.get(j).get_Point().y());
            ErrY_prime.add(j, SVTcrossesInTrk.get(j).get_PointErr().y());
            ErrRt.add(j, Math.sqrt(SVTcrossesInTrk.get(j).get_PointErr().x() * SVTcrossesInTrk.get(j).get_PointErr().x()
                    + SVTcrossesInTrk.get(j).get_PointErr().y() * SVTcrossesInTrk.get(j).get_PointErr().y()));
            ErrZ.add(j, SVTcrossesInTrk.get(j).get_PointErr().z());

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
            X.add(j, BMTZdetcrossesInTrk.get(j - j0).get_Point().x());
            Y.add(j, BMTZdetcrossesInTrk.get(j - j0).get_Point().y());
            ErrRt.add(j, Math.sqrt(BMTZdetcrossesInTrk.get(j - j0).get_PointErr().x() * BMTZdetcrossesInTrk.get(j - j0).get_PointErr().x()
                    + BMTZdetcrossesInTrk.get(j - j0).get_PointErr().y() * BMTZdetcrossesInTrk.get(j - j0).get_PointErr().y()));
        }

        if (ignoreErr == true) {
            for (int j = j0; j < j0 + BMTZdetcrossesInTrk.size(); j++) {
                X.add(j, (double) 1);
                Y.add(j, (double) 1);
                ErrRt.add(j, (double) 1);
            }
        }
        
        for (int j = j0; j < j0 + BMTCdetcrossesInTrk.size(); j++) {
            Z.add(j, BMTCdetcrossesInTrk.get(j - j0).get_Point().z());
            Y_prime.add(j, BMTCdetcrossesInTrk.get(j - j0).get_Point().y());
            ErrY_prime.add(j, 0.);
            ErrZ.add(j, BMTCdetcrossesInTrk.get(j - j0).get_PointErr().z());
        }
        if (resetSVTMeas) {
            
            //System.err.println("Error in Helical Track fitting -- helix not found -- trying to refit using the uncorrected crosses...");
//            for (int j = 0; j < SVTcrossesInTrk.size(); j++) {
//                X.add(j, SVTcrossesInTrk.get(j).get_Point0().x());
//                Y.add(j, SVTcrossesInTrk.get(j).get_Point0().y());
//                Z.add(j, SVTcrossesInTrk.get(j).get_Point0().z());
//                ErrY_prime.add(j, SVTcrossesInTrk.get(j).get_Point0().y());
//                ErrZ.add(j, SVTcrossesInTrk.get(j).get_PointErr0().z());
//            }
        }

        if(BMTCdetcrossesInTrk.size()>1 && useBMTCforZonly==true) {
            
            Z.clear();
            Y_prime.clear();
            ErrZ.clear();
            ErrY_prime.clear();
            for (int j = 0; j < BMTCdetcrossesInTrk.size(); j++) {
                Z.add(j, BMTCdetcrossesInTrk.get(j).get_Point().z());
                Y_prime.add(j, BMTCdetcrossesInTrk.get(j).get_Point().y());
                ErrY_prime.add(j, 0.);
                ErrZ.add(j, BMTCdetcrossesInTrk.get(j).get_PointErr().z());

            }
        }
        RayMeasurements MeasArray = new RayMeasurements(X, Y, Z, Y_prime, ErrZ, ErrY_prime, ErrRt);

        return MeasArray;
    }

    /**
     *
     * @param bmtCrosses the BMT crosses
     * @param thecand the straight track candidate
     * @param geo the BMT geometry
     * @return an arraylist of BMT crosses matched to the track
     */
    public ArrayList<Cross> matchTrackToBMT(StraightTrack thecand, List<Cross> bmtCrosses, BMTGeometry geo) {

        ArrayList<Cross> BMTCrossList = new ArrayList<Cross>();
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
            List<Cross> zCross = this.matchTrackToBMT(geo.getLayer(region, BMTType.Z), thecand.get_ray(), bmtCrosses, 
                                                Constants.COSMICSMINRESIDUALX/geo.getRadiusMidDrift(geo.getLayer(region, BMTType.Z)), geo);
            if(zCross!=null) 
                BMTCrossList.addAll(zCross);
            
            // match Z crosses
            List<Cross> cCross = this.matchTrackToBMT(geo.getLayer(region, BMTType.C), thecand.get_ray(), bmtCrosses, Constants.COSMICSMINRESIDUALZ, geo);
            if(cCross!=null) 
                BMTCrossList.addAll(cCross);
        
//            // match Z crosses
//            ArrayList<Cross> zCrosses = this.MatchMMCrossZ(iregion + 1, thecand.get_ray(), bmtCrosses, Math.toRadians(2), geo); // 2 degrees opening angle
//            if (zCrosses.size() > 0) {
//                BMTCrossList.addAll(zCrosses);
//            }
//
//            ArrayList<Cross> cCrosses = this.MatchMMCrossC(iregion + 1, thecand.get_ray(), bmtCrosses, SVTParameters.COSMICSMINRESIDUAL, geo);
//            if (cCrosses.size() > 0) {
//                BMTCrossList.addAll(cCrosses);
//            }
//
//            for (Cross Ccross : cCrosses) {
//                for (Cross Zcross : zCrosses) {
//                    if ((Ccross.get_Cluster1().get_Layer() - Zcross.get_Cluster1().get_Layer()) == 1 && (Ccross.get_Cluster1().get_Sector() == Zcross.get_Cluster1().get_Sector())) {
//                        Ccross.set_MatchedZCross(Zcross);
//                        Zcross.set_MatchedCCross(Ccross);
//                    }
//                }
//            }
        } 
        return BMTCrossList;
    }

    private List<Cross> matchTrackToBMT(int layer, Ray ray, List<Cross> crosses, double matchCut, BMTGeometry geo) {
        
        List<Cross> matched = new ArrayList<>();
        
        for(int is=0; is<geo.getNSectors(); is++) {
            // get ray intersection with cross tile
            int sector = is+1;
            List<Point3D> trajs = new ArrayList<>();
            int ntrajs = geo.getTileSurface(layer, sector).intersection(ray.toLine(), trajs);
            if(ntrajs==0) continue;
            
            // find the closests cross-trajectory match
            for(Point3D traj : trajs) {
                Cross closestCross = null;
                double doca = Double.MAX_VALUE;
        
                // loop over all BMT crosses
                for(Cross c : crosses) {
                    Cluster cluster = c.get_Cluster1();
                    // consider only the ones in the selected layer
                    if(cluster.get_Layer()!=layer || cluster.get_Sector()!=sector) continue;

                    double residual = cluster.residual(traj);
                    if(Math.abs(residual)<doca) {
                        doca = Math.abs(residual);
                        closestCross = c;
                    }
                }
                if(doca<matchCut) {
                    closestCross.setBMTCrossPosition(traj);
                    matched.add(closestCross);
                }
            }
        }
        return matched;
    }

    private ArrayList<Cross> MatchMMCrossC(int Region, Ray ray, List<Cross> MMCrosses, double matchCutOff, BMTGeometry geo) {

        ArrayList<Cross> matchedMMCrosses = new ArrayList<Cross>();

        double R = geo.getRadiusMidDrift(geo.getLayer(Region,BMTType.C));		     // R for C detector

        double sx = ray.get_yxslope();
        double ix = ray.get_yxinterc();
        double sz = ray.get_yzslope();
        double iz = ray.get_yzinterc();

        double D = sx * sx * ix * ix - (1 + sx * sx) * (ix * ix - R * R);

        double[] y = null;
        // calculate the y position of the trajectory ray at the radius of the C detector
        if (D == 0) {
            y = new double[1];
            y[0] = -sx * ix / (1 + sx * sx);
        }

        if (D > 0) {
            y = new double[2];
            y[0] = (-sx * ix + Math.sqrt(D)) / (1 + sx * sx);
            y[1] = (-sx * ix - Math.sqrt(D)) / (1 + sx * sx);
        }
        
        for (int j = 0; j < y.length; j++) {
            double x = sx * y[j] + ix;
            double phi = Math.atan2(y[j], x);
            // calculate the z position of the trajectory at the radius of the C detector
            double z = sz * y[j] + iz;

            double doca = 1000;
            Cross closestCross = null;
            // match the cross z measurement closest to the calculated z
            for (int i = 0; i < MMCrosses.size(); i++) {
                if (MMCrosses.get(i).get_Region() != Region) {
                    continue;
                }
                //if(Double.isNaN(MMCrosses.get(i).get_Point0().z()))
                if (MMCrosses.get(i).get_Type()==BMTType.Z) {
                    continue;
                }
                double m_z = MMCrosses.get(i).get_Point().z();  
                //int sector = geo.isInSector(MMCrosses.get(i).get_Region() * 2, phi, Math.toRadians(BMTConstants.isInSectorJitter));
                int sector = geo.getSector(MMCrosses.get(i).get_Region() * 2, phi);
                if (sector != MMCrosses.get(i).get_Sector()) {
                    continue;
                }
                double d = Math.abs(z - m_z);
                if (d < doca) {
                    doca = d;
                    closestCross = (Cross) MMCrosses.get(i).clone();

                    closestCross.set_Point(new Point3D(x, y[j], m_z));
                    closestCross.set_PointErr(new Point3D(10, 10, MMCrosses.get(i).get_PointErr().z())); // to do : put in the correct error
                }
            }

            if (closestCross != null) {
                if (doca < matchCutOff * 100) {
                    //System.out.println(doca+" -->match C "+closestCross.toString());
                    matchedMMCrosses.add(closestCross);
                }
            }

        }

        return matchedMMCrosses;
    }

    private ArrayList<Cross> MatchMMCrossZ(int Region, Ray ray, List<Cross> MMCrosses, double matchCutOff, BMTGeometry geo) {

        ArrayList<Cross> matchedMMCrosses = new ArrayList<Cross>();

        double R = geo.getRadiusMidDrift(geo.getLayer(Region,BMTType.Z));		     // R for Z detector
        double sx = ray.get_yxslope();
        double ix = ray.get_yxinterc();
        double sz = ray.get_yzslope();
        double iz = ray.get_yzinterc();

        double D = sx * sx * ix * ix - (1 + sx * sx) * (ix * ix - R * R);

        double[] y = null;

        if (D == 0) {
            y = new double[1];
            y[0] = -sx * ix / (1 + sx * sx);
        }

        if (D > 0) {
            y = new double[2];
            y[0] = (-sx * ix + Math.sqrt(D)) / (1 + sx * sx);
            y[1] = (-sx * ix - Math.sqrt(D)) / (1 + sx * sx);
        }
        for (int j = 0; j < y.length; j++) {
            double x = sx * y[j] + ix;
            double z = sz * y[j] + iz;

            double minCosAngMeasToTrk = -1;
            Cross closestCross = null;

            for (int i = 0; i < MMCrosses.size(); i++) {
                if (MMCrosses.get(i).get_Region() != Region) {
                    continue;
                }
                // measuring phi
                //if(Double.isNaN(MMCrosses.get(i).get_Point0().x()))
                if (MMCrosses.get(i).get_Type()==BMTType.C) {
                    continue;
                }
                double m_x = MMCrosses.get(i).get_Point().x();
                double m_y = MMCrosses.get(i).get_Point().y();
                //System.out.println(" lookin to match "+MMCrosses.get(i).printInfo());
                double cosAngMeasToTrk = (x * m_x + y[j] * m_y) / Math.sqrt((x * x + y[j] * y[j]) * (m_x * m_x + m_y * m_y)); // the cosine between the measured (x,y) cross coords and the (x,y) trajectory should be close to 1
                // opening angle must be within about 11 degrees to start the search for nearest point
                //System.out.println(" cosAngMeasToTrk "+cosAngMeasToTrk);
                if (cosAngMeasToTrk < 0.98) {
                    continue;
                }

                if (Math.acos(cosAngMeasToTrk) < Math.acos(minCosAngMeasToTrk)) {
                    minCosAngMeasToTrk = cosAngMeasToTrk;
                    closestCross = (Cross) MMCrosses.get(i).clone();
                    closestCross.set_Point(new Point3D(m_x, m_y, z));
                    closestCross.set_PointErr(new Point3D(MMCrosses.get(i).get_PointErr().x(), MMCrosses.get(i).get_PointErr().y(), 10.)); // to do : put in the correct error
                }
            }
            if (closestCross != null) {
                //if(minCosAngMeasToTrk<matchCutOff) 
                //	System.out.println("matched Z-det"+closestCross.printInfo());
                matchedMMCrosses.add(closestCross);

            }
        }

        return matchedMMCrosses;
    }

    private ArrayList<Track> rmHelicalTrkClones(boolean removeClones, ArrayList<Track> cands) {
        ArrayList<Track> passedcands = new ArrayList<Track>();
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
                            if ((cands.get(k2).get_circleFitChi2PerNDF() + cands.get(k2).get_lineFitChi2PerNDF()) > (cands.get(k).get_circleFitChi2PerNDF() + cands.get(k).get_lineFitChi2PerNDF())) {

                                cands.get(k2).set_Id(-999);

                            }
                        }
                    }
                }
                for (int k = 0; k < cands.size(); k++) {
                    if (cands.get(k).get_Id() != -999) {
                        passedcands.add(cands.get(k));

                    }
                }
            }
        }

        return passedcands;
    }

    private ArrayList<StraightTrack> rmStraightTrkClones(boolean removeClones, ArrayList<StraightTrack> cands) {
        ArrayList<StraightTrack> passedcands = new ArrayList<StraightTrack>();
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
                            if ((cands.get(k2).get_chi2()) > (cands.get(k).get_chi2())) {

                                cands.get(k2).set_Id(-999);

                            }
                        }
                    }
                }
                for (int k = 0; k < cands.size(); k++) {
                    if (cands.get(k).get_Id() != -999) {
                        passedcands.add(cands.get(k));

                    }
                }
            }
        }

        return passedcands;
    }

    private void EliminateStraightTrackOutliers(ArrayList<Cross> crosses, CosmicFitter track, SVTGeometry svt_geo) {
        ArrayList<Cross> toKeep = new ArrayList<>();
        
        for(int j = 0; j < crosses.size(); j++) {
            Cross c = crosses.get(j);
            int layer  = c.get_Cluster1().get_Layer();
            int sector = c.get_Sector();
            
            Point3D traj = new Point3D();
            int ntraj = svt_geo.getPlane(layer, sector).intersection(track.get_ray().toLine(), traj);
            
            if(ntraj!=1) 
                c.resetCross(svt_geo);
            else if(!svt_geo.isInFiducial(layer, sector, traj)) 
                c.resetCross(svt_geo);
            else {
                Vector3D distance = svt_geo.toLocal(layer, sector, c.get_Point().vectorTo(traj));
                if(Math.abs(distance.x())>Constants.COSMICSMINRESIDUALX ||
                   Math.abs(distance.z())>Constants.COSMICSMINRESIDUALZ) c.resetCross(svt_geo);
                else
                    toKeep.add(c);
            }
        }
        crosses = toKeep;
    }

    public void matchClusters(List<Cluster> sVTclusters, TrajectoryFinder tf, SVTGeometry svt_geo, BMTGeometry bmt_geo, boolean trajFinal,
            ArrayList<StateVec> trajectory, int k) {
        if(trajectory == null)
            return;
        Collections.sort(sVTclusters);
        for (StateVec st : trajectory) { 
            for (Cluster cls : sVTclusters) {
                if (cls.get_AssociatedTrackID() != -1) {
                    continue;
                }
                if (st.get_SurfaceSector() != cls.get_Sector()) {
                    continue;
                }
                if (st.get_SurfaceLayer() != cls.get_Layer()) {
                    continue;
                }
                if (Math.abs(st.get_CalcCentroidStrip() - cls.get_Centroid()) < 4) {
                    tf.setHitResolParams("SVT", cls.get_Sector(), cls.get_Layer(), cls,
                            st, svt_geo, bmt_geo, trajFinal);
                    //System.out.println("trying to associate a cluster ");cls.printInfo(); System.out.println(" to "+st.get_CalcCentroidStrip()+" dStp = "+(st.get_CalcCentroidStrip()-cls.get_Centroid()));
                    cls.set_AssociatedTrackID(k);
                    for (FittedHit h : cls) {
                        h.set_AssociatedTrackID(k);
                    }
                }
            }
        }
    }

}
