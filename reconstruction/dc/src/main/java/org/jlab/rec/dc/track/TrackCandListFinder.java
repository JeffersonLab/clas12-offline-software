package org.jlab.rec.dc.track;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.clas.clas.math.FastMath;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.cross.Cross;
import org.jlab.rec.dc.cross.CrossList;
import org.jlab.rec.dc.hit.FittedHit;
import org.jlab.rec.dc.segment.Segment;
import org.jlab.rec.dc.track.fit.KFitterDoca;
import org.jlab.rec.dc.trajectory.StateVec;
import org.jlab.rec.dc.trajectory.Trajectory;
import org.jlab.rec.dc.trajectory.TrajectoryFinder;

import trackfitter.fitter.LineFitPars;
import trackfitter.fitter.LineFitter;

/**
 * A class with a method implementing an algorithm that finds lists of track
 * candidates in the DC
 *
 * @author ziegler
 */

public class TrackCandListFinder {

    private static final Logger LOGGER = Logger.getLogger(TrackCandListFinder.class.getName());

    long startTime, startTime2 = 0;

    /**
     * the tracking status = HitBased or TimeBased
     */
    private String trking;

    /**
     * @param stat the tracking status Hit-based or Time-based
     */
    public TrackCandListFinder(String stat) {
        trking = stat;
    }

    /**
     * @param crossesInTrk the list of crosses on track
     * @return the number of superlayers used in the fit
     */
    private boolean PassNSuperlayerTracking(List<Cross> crossesInTrk, Track cand) {
        boolean pass = true;
        int NbMissingSl = 0; // find the missing superlayers from the pseudo-crosses
        for (Cross c : crossesInTrk) {
            if (c.isPseudoCross) {
                if ((c.get_Segment1().get_Id() == -1) || (c.get_Segment2().get_Id() == -1)) {
                    NbMissingSl++;
                }
                if (c.get_Segment1().get_Id() == -1) {
                    cand.set_MissingSuperlayer(c.get_Segment1().get_Superlayer());
                    cand.setSingleSuperlayer(c.get_Segment2());
                }
                if (c.get_Segment2().get_Id() == -1) {
                    cand.set_MissingSuperlayer(c.get_Segment2().get_Superlayer());
                    cand.setSingleSuperlayer(c.get_Segment1());
                }
            } else {
                if ((c.get_Segment1().get_Status() == 1) || (c.get_Segment2().get_Status() == 1)) {
                    cand.set_Status(1);
                }
            }
        }
        // if more superlayers are missing than the required number in the analysis - skip the track
        if (NbMissingSl > 6 - Constants.getInstance().NSUPERLAYERTRACKING) {
            pass = false;
        }
        return pass;
    }

    private double getHitBasedFitChi2ToCrosses(int sector, double x1, double y1, double z1,
            double x2, double y2, double z2, double x3,
            double y3, double z3, double p, int q, double x,
            double y, double z, double tanThX, double tanThY, Swim dcSwim) {
        double pz = p / Math.sqrt(tanThX * tanThX + tanThY * tanThY + 1);

        dcSwim.SetSwimParameters(x, y, z,
                -pz * tanThX, -pz * tanThY, -pz,
                -q);
        double chi2 = 0; // assume err =1 on points 
        double[] R = dcSwim.SwimToPlaneTiltSecSys(sector, z3);
        if (R == null) {
            return Double.POSITIVE_INFINITY;
        }

        //chi2 += (R[0] - x3) * (R[0] - x3) + (R[1] - y3) * (R[1] - y3);
        chi2 += (Math.sqrt(R[0] * R[0] + R[1] * R[1]) - Math.sqrt(x3 * x3 + y3 * y3)) * (Math.sqrt(R[0] * R[0] + R[1] * R[1]) - Math.sqrt(x3 * x3 + y3 * y3));
        dcSwim.SetSwimParameters(R[0], R[1], R[2],
                R[3], R[4], R[5],
                -q);
        R = dcSwim.SwimToPlaneTiltSecSys(sector, z2);
        if (R == null) {
            return Double.POSITIVE_INFINITY;
        }

        dcSwim.SetSwimParameters(R[0], R[1], R[2],
                R[3], R[4], R[5],
                -q);
        chi2 += (R[0] - x2) * (R[0] - x2) + (R[1] - y2) * (R[1] - y2);
        dcSwim.SetSwimParameters(R[0], R[1], R[2],
                R[3], R[4], R[5],
                -q);
        R = dcSwim.SwimToPlaneTiltSecSys(sector, z1);
        if (R == null) {
            return Double.POSITIVE_INFINITY;
        }

        chi2 += (R[0] - x1) * (R[0] - x1) + (R[1] - y1) * (R[1] - y1);

        return chi2;
    }

    private double[] getTrackInitFit(int sector, double x1, double y1, double z1,
            double x2, double y2, double z2, double x3, double y3, double z3,
            double ux, double uy, double uz, double thX, double thY,
            double theta1, double theta3,
            double iBdl, double TORSCALE, Swim dcSwim) {
        if (theta1 < -998 || theta3 < -998) {
            return new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
        }
        double[] pars = new double[2];

        double chi2 = 0; // assume err =1 on points 
        double intBdl = 0;

        double p = calcInitTrkP(ux, uy, uz, thX, thY,
                theta1, theta3,
                iBdl, TORSCALE);
        double p_x = ux * p;
        double p_y = uy * p;
        double p_z = uz * p;

        int q = calcInitTrkQ(theta1, theta3, TORSCALE);

        dcSwim.SetSwimParameters(x1, y1, z1, p_x, p_y, p_z, q);
        double[] R = dcSwim.SwimToPlaneTiltSecSys(sector, z2);
        if (R == null) {
            return new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
        }

        chi2 += (R[0] - x2) * (R[0] - x2) + (R[1] - y2) * (R[1] - y2);
        intBdl += R[7];
        dcSwim.SetSwimParameters(R[0], R[1], R[2],
                R[3], R[4], R[5],
                q);
        R = dcSwim.SwimToPlaneTiltSecSys(sector, z3);
        if (R == null) {
            return new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
        }

        chi2 += (R[0] - x3) * (R[0] - x3) + (R[1] - y3) * (R[1] - y3);
        intBdl += R[7];

        pars[0] = chi2;
        pars[1] = intBdl;

        return pars;
    }

    private double calcInitTrkP(double ux, double uy, double uz, double thX, double thY,
            double theta1, double theta3,
            double iBdl, double TORSCALE) {
        double deltaTheta = theta3 - theta1;
        if (deltaTheta == 0) {
            return Double.POSITIVE_INFINITY;
        }

        // momentum estimate if Bdl is non zero and the track has curvature    
        double pxz = Math.abs(Constants.LIGHTVEL * iBdl / deltaTheta);
        double py = Math.sqrt((thX * thX + thY * thY + 1) / (thX * thX + 1) - 1) * pxz;

        double p = Math.sqrt(pxz * pxz + py * py);
        return p;
    }

    private int calcInitTrkQ(double theta1, double theta3,
            double TORSCALE) {
        double deltaTheta = theta3 - theta1;

        //positive charges bend outward for nominal GEMC field configuration
        int q = (int) Math.signum(deltaTheta);
        q *= (int) -1 * Math.signum(TORSCALE); // flip the charge according to the field scale

        return q;
    }

    /**
     * @param crossList the input list of crosses
     * @param DcDetector
     * @param TORSCALE
     * @param dcSwim
     * @param donotapplyCuts
     * @return a list of track candidates in the DC
     */
    public List<Track> getTrackCands(CrossList crossList, DCGeant4Factory DcDetector, double TORSCALE, Swim dcSwim,
            boolean donotapplyCuts) {
        List<Track> cands = null;
        if (Math.abs(TORSCALE) < 0.001) {
            cands = this.findStraightTracks(crossList, DcDetector, TORSCALE, dcSwim);
        } else {
            cands = this.findCurvedTracks(crossList, DcDetector, TORSCALE, dcSwim, donotapplyCuts);
        }
        return cands;
    }

    /**
     * @param cand straight track candidate
     */
    private void getStraightTrack(Track cand) {

        double[] x = new double[3];
        double[] y1 = new double[3];
        double[] y2 = new double[3];
        double[] ex = new double[3];
        double[] ey1 = new double[3];
        double[] ey2 = new double[3];

        for (int i = 0; i < 3; i++) {

            Point3D X = cand.get(i).getCoordsInLab(cand.get(i).get_Point().x(),
                    cand.get(i).get_Point().y(), cand.get(i).get_Point().z());
            Point3D eX = cand.get(i).getCoordsInLab(cand.get(i).get_PointErr().x(),
                    cand.get(i).get_PointErr().y(), cand.get(i).get_PointErr().z());

            x[i] = X.z();
            ex[i] = eX.z();

            y1[i] = X.x();
            ey1[i] = eX.x();

            y2[i] = X.y();
            ey2[i] = eX.y();
        }

        if (x != null) {

            LineFitter linefit = new LineFitter();
            boolean linefitstatusOK1 = linefit.fitStatus(x, y1, ex, ey1, 3);

            LineFitPars FitPars1 = null;
            LineFitPars FitPars2 = null;
            if (linefitstatusOK1) //  Get the results of the fits
            {
                FitPars1 = linefit.getFit();
            }

            boolean linefitstatusOK2 = linefit.fitStatus(x, y2, ex, ey2, 3);
            if (linefitstatusOK2) //  Get the results of the fits
            {
                FitPars2 = linefit.getFit();
            }

            double X0 = -99999;
            double Y0 = -99999;

            if (FitPars1 != null && FitPars2 != null) {

                X0 = FitPars1.intercept();
                Y0 = FitPars2.intercept();
                Point3D trkR1X = new Point3D(FitPars1.slope() * x[0] + FitPars1.intercept(),
                        FitPars2.slope() * x[0] + FitPars2.intercept(), x[0]);
                Point3D trkR3X = new Point3D(FitPars1.slope() * x[2] + FitPars1.intercept(),
                        FitPars2.slope() * x[2] + FitPars2.intercept(), x[2]);

                Vector3D trkDir = new Vector3D(trkR3X.x() - trkR1X.x(),
                        trkR3X.y() - trkR1X.y(), trkR3X.z() - trkR1X.z()).asUnit();
                trkDir.scale(10);

                Point3D trkVtx = new Point3D(X0, Y0, 0);

                cand.set_P(10);
                cand.set_Q(-1); // assume it's a muon
                cand.set_pAtOrig(trkDir);
                cand.set_Vtx0(trkVtx);
                cand.set_PreRegion1CrossPoint(new Point3D(trkR1X.x() - trkDir.x(),
                        trkR1X.y() - trkDir.y(), trkR1X.z() - trkDir.z()));
                cand.set_PostRegion3CrossPoint(new Point3D(trkR3X.x() + trkDir.x(),
                        trkR3X.y() + trkDir.y(), trkR3X.z() + trkDir.z()));
                cand.set_PreRegion1CrossDir(new Point3D(trkDir.x(), trkDir.y(), trkDir.z()));
                cand.set_PostRegion3CrossDir(new Point3D(trkDir.x(), trkDir.y(), trkDir.z()));
                cand.set_Region1TrackX(trkR1X);
                cand.set_Region1TrackP(new Point3D(trkDir.x(), trkDir.y(), trkDir.z()));

                cand.set_PathLength(trkR3X.distance(trkVtx));
            }
        }
    }

    /**
     * @param x x coordinate in the lab frame
     * @param y y coordinate in the lab frame
     * @return the sector in the DC lab frame system corresponding to the (x,y)
     * coordinates
     */
    private int getSector(double x, double y) {
        double phi = Math.toDegrees(FastMath.atan2(y, x));
        double ang = phi + 30;
        while (ang < 0) {
            ang += 360;
        }
        int sector = 1 + (int) (ang / 60.);

        if (sector == 7) {
            sector = 6;
        }

        if ((sector < 1) || (sector > 6)) {
            System.err.println("Track sector not found....");
        }
        return sector;
    }

    /**
     * @param cand the track candidate
     * @param traj the track trajectory
     * @param trjFind the track trajectory utility
     * @param stateVec the track state vector at the last measurement site used
     * by the Kalman Filter
     * @param z the z position in the tilted sector coordinate system at the
     * last measurement site
     * @param getDcDetector the detector geometry
     * @param dcSwim
     */
    public void setTrackPars(Track cand,
            Trajectory traj,
            TrajectoryFinder trjFind,
            StateVec stateVec, double z,
            DCGeant4Factory getDcDetector,
            Swim dcSwim) {
        double pz = cand.get_P() / Math.sqrt(stateVec.tanThetaX() * stateVec.tanThetaX()
                + stateVec.tanThetaY() * stateVec.tanThetaY() + 1);

        //LOGGER.log(Level.FINE, "Setting track params for ");stateVec.printInfo();
        dcSwim.SetSwimParameters(stateVec.x(), stateVec.y(), z,
                pz * stateVec.tanThetaX(), pz * stateVec.tanThetaY(), pz,
                cand.get_Q());

        // swimming to a ref points outside of the last DC region
        double[] VecAtTarOut = dcSwim.SwimToPlaneTiltSecSys(cand.get(0).get_Sector(), 592);
        if (VecAtTarOut == null) {
            return;
        }

        double xOuter = VecAtTarOut[0];
        double yOuter = VecAtTarOut[1];
        double zOuter = VecAtTarOut[2];
        double uxOuter = VecAtTarOut[3] / cand.get_P();
        double uyOuter = VecAtTarOut[4] / cand.get_P();
        double uzOuter = VecAtTarOut[5] / cand.get_P();
        //Cross crossR = new Cross(cand.get(2).get_Sector(), cand.get(2).get_Region(), -1);
        Cross crossR = new Cross(cand.get(cand.size() - 1).get_Sector(),
                cand.get(cand.size() - 1).get_Region(), -1);
        Point3D xOuterExtp = crossR.getCoordsInLab(xOuter, yOuter, zOuter);
        Point3D uOuterExtp = crossR.getCoordsInLab(uxOuter, uyOuter, uzOuter);

        //set the pseudocross at extrapolated position
        cand.set_PostRegion3CrossPoint(xOuterExtp);
        cand.set_PostRegion3CrossDir(uOuterExtp);

        dcSwim.SetSwimParameters(stateVec.x(), stateVec.y(), z,
                -pz * stateVec.tanThetaX(), -pz * stateVec.tanThetaY(), -pz,
                -cand.get_Q());

        //swimming to a ref point upstream of the first DC region
        double[] VecAtTarIn = dcSwim.SwimToPlaneTiltSecSys(cand.get(0).get_Sector(), 180);
        if (VecAtTarIn == null) {
            cand.fit_Successful = false;
            return;
        }

        if (VecAtTarIn[6] + VecAtTarOut[6] < 200) {
            cand.fit_Successful = false;
            return;
        }

        double xOr = VecAtTarIn[0];
        double yOr = VecAtTarIn[1];
        double zOr = VecAtTarIn[2];
        double pxOr = -VecAtTarIn[3];
        double pyOr = -VecAtTarIn[4];
        double pzOr = -VecAtTarIn[5];

        Cross C = new Cross(cand.get(cand.size() - 1).get_Sector(), cand.get(cand.size() - 1).get_Region(), -1);

        Point3D trkR1X = C.getCoordsInLab(xOr, yOr, zOr);
        Point3D trkR1P = C.getCoordsInLab(pxOr, pyOr, pzOr);
        cand.set_Region1TrackX(new Point3D(trkR1X.x(), trkR1X.y(), trkR1X.z()));
        cand.set_Region1TrackP(new Point3D(trkR1P.x(), trkR1P.y(), trkR1P.z()));

        Point3D R3TrkPoint = C.getCoordsInLab(stateVec.x(), stateVec.y(), z);
        Point3D R3TrkMomentum = C.getCoordsInLab(pz * stateVec.tanThetaX(),
                pz * stateVec.tanThetaY(), pz);
        dcSwim.SetSwimParameters(R3TrkPoint.x(), R3TrkPoint.y(),
                R3TrkPoint.z(),
                -R3TrkMomentum.x(),
                -R3TrkMomentum.y(),
                -R3TrkMomentum.z(),
                -cand.get_Q());
        // recalc new vertex using plane stopper
        //int sector = cand.get(2).get_Sector();
        int sector = cand.get(cand.size() - 1).get_Sector();
        double theta_n = ((double) (sector - 1)) * Math.toRadians(60.);
        double x_n = Math.cos(theta_n);
        double y_n = Math.sin(theta_n);
        double[] Vt = dcSwim.SwimToPlaneBoundary(0, new Vector3D(x_n, y_n, 0), -1);

        if (Vt == null) {
            return;
        }

        int status = 99999;

        int LR = 0;
        for (Cross crs : cand) {
            Segment s1 = crs.get_Segment1();
            Segment s2 = crs.get_Segment2();

            for (FittedHit h : s1) {
                LR += h._lr;
            }
            for (FittedHit h : s2) {
                LR += h._lr;
            }

        }

        status = LR;

        double xOrFix = Vt[0];
        double yOrFix = Vt[1];
        double zOrFix = Vt[2];
        double pxOrFix = -Vt[3];
        double pyOrFix = -Vt[4];
        double pzOrFix = -Vt[5];
        double PathInFromR3 = Vt[6];

        //double totPathLen = VecAtTarlab0[6] + VecAtTarOut[6] + arclen;
        double totPathLen = PathInFromR3 + VecAtTarOut[6];
        cand.set_TotPathLen(totPathLen);

        cand.set_Vtx0(new Point3D(xOrFix, yOrFix, zOrFix));
        cand.set_pAtOrig(new Vector3D(pxOrFix, pyOrFix, pzOrFix));

        double[] VecAtHtccSurf = dcSwim.SwimToSphere(175);
        double xInner = VecAtHtccSurf[0];
        double yInner = VecAtHtccSurf[1];
        double zInner = VecAtHtccSurf[2];
        double uxInner = VecAtHtccSurf[3] / cand.get_P();
        double uyInner = VecAtHtccSurf[4] / cand.get_P();
        double uzInner = VecAtHtccSurf[5] / cand.get_P();

        //set the pseudocross at extrapolated position
        cand.set_PreRegion1CrossPoint(new Point3D(xInner, yInner, zInner));
        cand.set_PreRegion1CrossDir(new Point3D(uxInner, uyInner, uzInner));

        cand.fit_Successful = true;
        cand.set_TrackingInfoString(trking);
    }

    public void setTrackPars(Track cand,
            Trajectory traj,
            TrajectoryFinder trjFind,
            StateVec stateVec, double z,
            DCGeant4Factory getDcDetector,
            Swim dcSwim, double xB, double yB) {

        double pz = cand.get_P() / Math.sqrt(stateVec.tanThetaX() * stateVec.tanThetaX()
                + stateVec.tanThetaY() * stateVec.tanThetaY() + 1);

        //LOGGER.log(Level.FINE, "Setting track params for ");stateVec.printInfo();
        dcSwim.SetSwimParameters(stateVec.x(), stateVec.y(), z,
                pz * stateVec.tanThetaX(), pz * stateVec.tanThetaY(), pz,
                cand.get_Q());

        // swimming to a ref points outside of the last DC region
        double[] VecAtTarOut = dcSwim.SwimToPlaneTiltSecSys(cand.get(0).get_Sector(), 592);
        if(VecAtTarOut==null) {
            cand.fit_Successful = false;
            return;
        }
        double xOuter = VecAtTarOut[0];
        double yOuter = VecAtTarOut[1];
        double zOuter = VecAtTarOut[2];
        double uxOuter = VecAtTarOut[3] / cand.get_P();
        double uyOuter = VecAtTarOut[4] / cand.get_P();
        double uzOuter = VecAtTarOut[5] / cand.get_P();
        //Cross crossR = new Cross(cand.get(2).get_Sector(), cand.get(2).get_Region(), -1);
        Cross crossR = new Cross(cand.get(cand.size() - 1).get_Sector(),
                cand.get(cand.size() - 1).get_Region(), -1);
        Point3D xOuterExtp = crossR.getCoordsInLab(xOuter, yOuter, zOuter);
        Point3D uOuterExtp = crossR.getCoordsInLab(uxOuter, uyOuter, uzOuter);

        //set the pseudocross at extrapolated position
        cand.set_PostRegion3CrossPoint(xOuterExtp);
        cand.set_PostRegion3CrossDir(uOuterExtp);

        dcSwim.SetSwimParameters(stateVec.x(), stateVec.y(), z,
                -pz * stateVec.tanThetaX(), -pz * stateVec.tanThetaY(), -pz,
                -cand.get_Q());

        //swimming to a ref point upstream of the first DC region
        double[] VecAtTarIn = dcSwim.SwimToPlaneTiltSecSys(cand.get(0).get_Sector(), 180);
        if (VecAtTarIn == null) {
            cand.fit_Successful = false;
            return;
        }

        if (VecAtTarIn[6] + VecAtTarOut[6] < Constants.MINPATH) {
            cand.fit_Successful = false;
            return;
        }

        double xOr = VecAtTarIn[0];
        double yOr = VecAtTarIn[1];
        double zOr = VecAtTarIn[2];
        double pxOr = -VecAtTarIn[3];
        double pyOr = -VecAtTarIn[4];
        double pzOr = -VecAtTarIn[5];

        Cross C = new Cross(cand.get(cand.size() - 1).get_Sector(), cand.get(cand.size() - 1).get_Region(), -1);

        Point3D trkR1X = C.getCoordsInLab(xOr, yOr, zOr);
        Point3D trkR1P = C.getCoordsInLab(pxOr, pyOr, pzOr);
        cand.set_Region1TrackX(new Point3D(trkR1X.x(), trkR1X.y(), trkR1X.z()));
        cand.set_Region1TrackP(new Point3D(trkR1P.x(), trkR1P.y(), trkR1P.z()));

        Point3D R3TrkPoint = C.getCoordsInLab(stateVec.x(), stateVec.y(), z);
        Point3D R3TrkMomentum = C.getCoordsInLab(pz * stateVec.tanThetaX(),
                pz * stateVec.tanThetaY(), pz);
        dcSwim.SetSwimParameters(R3TrkPoint.x(), R3TrkPoint.y(),
                R3TrkPoint.z(),
                -R3TrkMomentum.x(),
                -R3TrkMomentum.y(),
                -R3TrkMomentum.z(),
                -cand.get_Q());

        double[] Vt = dcSwim.SwimToBeamLine(xB, yB);
        if(Vt==null) {
            cand.fit_Successful = false;
            return;
        }
        
       // recalc new vertex using plane stopper
        //int sector = cand.get(2).get_Sector();
        //double[] Vt = null;
        //int sector = cand.get(cand.size() - 1).get_Sector();
        //double theta_n = ((double) (sector - 1)) * Math.toRadians(60.);
        //double x_n = Math.cos(theta_n);
        //double y_n = Math.sin(theta_n);
        //double d = x_n*xB + y_n*yB; 
        //Vt = dcSwim.SwimToPlaneBoundary(d, new Vector3D(x_n, y_n, 0), -1); 
        //if(Vt==null)
        //    return;
        double xOrFix = Vt[0];
        double yOrFix = Vt[1];
        double zOrFix = Vt[2];
        double pxOrFix = -Vt[3];
        double pyOrFix = -Vt[4];
        double pzOrFix = -Vt[5];

        double PathInFromR3 = Vt[6];

        double totPathLen = PathInFromR3 + VecAtTarOut[6];
        cand.set_TotPathLen(totPathLen);

        cand.set_Vtx0(new Point3D(xOrFix, yOrFix, zOrFix));

        cand.set_pAtOrig(new Vector3D(pxOrFix, pyOrFix, pzOrFix));

        cand.fit_Successful = true;
        cand.set_TrackingInfoString(trking);

        dcSwim.SetSwimParameters(xOrFix, yOrFix, zOrFix,
                pxOrFix, pyOrFix, pzOrFix,
                cand.get_Q());
        double[] VecAtHtccSurf = dcSwim.SwimToSphere(175);
        if(VecAtHtccSurf==null) {
            cand.fit_Successful = false;
            return;
        }
        double xInner = VecAtHtccSurf[0];
        double yInner = VecAtHtccSurf[1];
        double zInner = VecAtHtccSurf[2];
        double uxInner = VecAtHtccSurf[3] / cand.get_P();
        double uyInner = VecAtHtccSurf[4] / cand.get_P();
        double uzInner = VecAtHtccSurf[5] / cand.get_P();

        //set the pseudocross at extrapolated position
        cand.set_PreRegion1CrossPoint(new Point3D(xInner, yInner, zInner));
        cand.set_PreRegion1CrossDir(new Point3D(uxInner, uyInner, uzInner));
    }

    private Integer getKey(Track trk) {
        return trk.get(0).get_Id() * 1000000
                + trk.get(1).get_Id() * 1000
                + trk.get(2).get_Id();
    }

    public void removeOverlappingTracksOld(List<Track> trkcands) {
        if(Constants.DEBUG) {
            LOGGER.log(Level.FINE, "Found "+trkcands.size()+" HB seeds ");
            for(int i = 0; i< trkcands.size(); i++) {
                LOGGER.log(Level.FINE, "cand "+i);
                for(Cross c : trkcands.get(i)) {
                    LOGGER.log(Level.FINE, c.printInfo());
                }
                LOGGER.log(Level.FINE, "------------------------------------------------------------------ ");
            }
        }
        Map<Integer, Track> selectedTracksMap = new HashMap<>();
        List<Track> list = new ArrayList<>();
        int size = trkcands.size();
        for (int i = 0; i < size; i++) {
            list.clear();
            this.getOverlapLists(trkcands.get(i), trkcands, list);
            Track selectedTrk = this.FindBestTrack(list);

            if (selectedTrk == null) {
                continue;
            }
            selectedTracksMap.put(this.getKey(selectedTrk), selectedTrk);
        }

        trkcands.removeAll(trkcands);
        selectedTracksMap.entrySet().forEach(entry -> {
            trkcands.add(entry.getValue());
        });
        if(Constants.DEBUG) {
            LOGGER.log(Level.FINE, "After Overlap Remvr "+trkcands.size()+" HB seeds ");
            for(int i = 0; i< trkcands.size(); i++) {
                LOGGER.log(Level.FINE, "cand "+i);
                for(Cross c : trkcands.get(i)) {
                    LOGGER.log(Level.FINE, c.printInfo());
                }
                LOGGER.log(Level.FINE, "------------------------------------------------------------------ ");
            }
        }
    }
    
    public void removeOverlappingTracks(List<Track> trkcands) {
        if(Constants.DEBUG) {
            LOGGER.log(Level.FINE, "Found "+trkcands.size()+" HB seeds ");
            for(int i = 0; i< trkcands.size(); i++) {
                LOGGER.log(Level.FINE, "cand "+i);
                for(Cross c : trkcands.get(i)) {
                    LOGGER.log(Level.FINE, c.printInfo());
                }
                LOGGER.log(Level.FINE, "------------------------------------------------------------------ ");
            }
        }
        
        List<Track> selectedTracks =  new ArrayList<>();
        for (int i = 0; i < trkcands.size(); i++) {
            boolean overlap = false;
            Track t1 = trkcands.get(i);
            for(int j=0; j<trkcands.size(); j++ ) {
                Track t2 = trkcands.get(j);
//                LOGGER.log(Level.FINE, "Checking overlaps for tracks ");
//                t1.printInfo();t2.printInfo();
                if(i!=j && t1.overlaps(t2)) {
                    if(t1.get_FitChi2()>t2.get_FitChi2())
                        overlap=true;
                    else if(t1.get_FitChi2()==t2.get_FitChi2() && i>j)
                        overlap=true;
                }
//               LOGGER.log(Level.FINE, overlap);
            }
            if(!overlap) selectedTracks.add(t1);
        }
        trkcands.removeAll(trkcands);
        trkcands.addAll(selectedTracks);
    }
    
//    public void removeOverlappingTracks(List<Track> trkcands) { 
//        List<Track> selectedTracks = new ArrayList<Track>();
//        List<Track> list = new ArrayList<Track>();
//        int size = trkcands.size();
//        for (int i = 0; i < size; i++) {
//            list.clear();
//            this.getOverlapLists(trkcands.get(i), trkcands, list);
//            trkcands.removeAll(list);
//            size -= list.size();
//            Track selectedTrk = this.FindBestTrack(list);
//            if (selectedTrk == null)
//                continue;
//            //if(this.ListContainsTrack(selectedTracks, selectedTrk)==false)
//            selectedTracks.add(selectedTrk);
//        }
//        //trkcands.removeAll(trkcands);
//        trkcands.addAll(selectedTracks);
//    }
    /**
     * @param selectedTracks the list of selected tracks
     * @param selectedTrk the selected track
     * @return a boolean indicating if the track is in the list
     */
    private boolean ListContainsTrack(List<Track> selectedTracks, Track selectedTrk) {
        boolean isInList = false;
        for (Track trk : selectedTracks) {
            if (trk == null) {
                continue;
            }
            if (trk.get_Id() == selectedTrk.get_Id()) {
                isInList = true;
            }
        }
        return isInList;
    }

    /**
     * @param track the track
     * @param trkcands the list of candidates
     * @param list the list of selected tracks
     */
    private void getOverlapLists(Track track, List<Track> trkcands, List<Track> list) {
        for (int i = 0; i < trkcands.size(); i++) {
            if ((track.get(0).get_Id() != -1 && track.get(0).get_Id() == trkcands.get(i).get(0).get_Id())
                    || (track.get(1).get_Id() != -1 && track.get(1).get_Id() == trkcands.get(i).get(1).get_Id())
                    || (track.get(2).get_Id() != -1 && track.get(2).get_Id() == trkcands.get(i).get(2).get_Id())) {
                list.add(trkcands.get(i));

            }
        }
    }

    /**
     * @param trkList the list of tracks
     * @return the track with the best chi2 from the list
     */
    private Track FindBestTrack(List<Track> trkList) {
        double bestChi2 = 9999999;
        Track bestTrk = null;

        for (int i = 0; i < trkList.size(); i++) {
            if (trkList.get(i).get_FitChi2() < bestChi2) {
                bestChi2 = trkList.get(i).get_FitChi2();
                bestTrk = trkList.get(i);
            }
        }
        return bestTrk;
    }

    public void matchHits(List<StateVec> stateVecAtPlanesList, Track trk,
            DCGeant4Factory DcDetector, Swim dcSwim) {

        if (stateVecAtPlanesList == null || trk.get_Vtx0()==null) {
            return;
        }

        List<FittedHit> fhits = new ArrayList<>();

        dcSwim.SetSwimParameters(trk.get_Vtx0().x(),
                trk.get_Vtx0().y(), trk.get_Vtx0().z(), trk.get_pAtOrig().x(),
                trk.get_pAtOrig().y(), trk.get_pAtOrig().z(), trk.get_Q());
        double[] ToFirstMeas = dcSwim.SwimToPlaneTiltSecSys(trk.get(0).get_Sector(),
                stateVecAtPlanesList.get(0).getZ());
        if (ToFirstMeas == null) {
            return;
        }

        for (StateVec st : stateVecAtPlanesList) {
            if (st == null) {
                return;
            }

            for (Cross c : trk) {
                c.get_Segment1().isOnTrack=true;
                c.get_Segment2().isOnTrack=true;
                for (FittedHit h1 : c.get_Segment1()) {
                    if (Math.abs(st.getZ() - h1.get_Z()) < 0.1 && c.get_Segment1().get_Id() > -1
                            && (h1.get_XWire() - st.getProjector()) < 0.1) {

                        h1.set_AssociatedHBTrackID(trk.get_Id());
                        h1.updateHitfromSV(st, DcDetector);
                        fhits.add(h1);
                    }
                }
                for (FittedHit h1 : c.get_Segment2()) {
                    if (Math.abs(st.getZ() - h1.get_Z()) < 0.1 && c.get_Segment2().get_Id() > -1
                            && (h1.get_XWire() - st.getProjector()) < 0.1) {

                        h1.set_AssociatedHBTrackID(trk.get_Id());
                        h1.updateHitfromSV(st, DcDetector);
                        fhits.add(h1);
                    }
                }
            }
        }
        trk.setHitsOnTrack(fhits);
    }

    public void setHitDoubletsInfo(Segment seg) {
        if(seg.isOnTrack) {
            for(FittedHit h : seg) {
                // for hits with no timing information, check if there is a neighbor hit 
                // in the same layer and copy time info from there
                // currently disabled to avoid fake doublets
                if((h.get_AssociatedHBTrackID()==-1 || h.getTFlight()==0) && Constants.getInstance().USEDOUBLETS()) {
                    for(FittedHit o :seg) {
                        if(h.get_Id()!=o.get_Id() && 
                           o.get_AssociatedHBTrackID()>0 && 
                           o.getTFlight()>0 && 
                           h.get_Layer()==o.get_Layer() && 
                           Math.abs(h.get_Wire()-o.get_Wire())==1) {
                            h.set_AssociatedHBTrackID(o.get_AssociatedHBTrackID());
                            h.setB(o.getB());
                            h.setTProp(o.getTProp());
                            h.setTFlight(o.getTFlight());
                            h.set_QualityFac(1);
                        }
                    }
                }
                // if time info is still not available, reset the hit trkID
                if(h.get_AssociatedHBTrackID()>0 && h.getTFlight()==0)
                    h.set_AssociatedHBTrackID(-1);
            }          
        }
    }

    private double calcCurvSign(Track cand) {
        double P0x = cand.get(0).get_Point().z();
        double P1x = cand.get(1).get_Point().z();
        double P2x = cand.get(2).get_Point().z();
        double P0y = cand.get(0).get_Point().x();
        double P1y = cand.get(1).get_Point().x();
        double P2y = cand.get(2).get_Point().x();

        if (Math.abs(P1x - P0x) < 1.0e-18 || Math.abs(P2x - P1x) < 1.0e-18) {
            return 0.0;
        }

        // Find the intersection of the lines joining the innermost to middle and middle to outermost point
        double ma = (P1y - P0y) / (P1x - P0x);
        double mb = (P2y - P1y) / (P2x - P1x);

        if (Math.abs(mb - ma) < 1.0e-18) {
            return 0.0;
        }

        double xcen = 0.5 * (ma * mb * (P0y - P2y) + mb * (P0x + P1x) - ma * (P1x + P2x)) / (mb - ma);
        double ycen = (-1. / mb) * (xcen - 0.5 * (P1x + P2x)) + 0.5 * (P1y + P2y);

        return Math.signum(ycen);
    }

    private List<Track> findStraightTracks(CrossList crossList, DCGeant4Factory DcDetector, double TORSCALE, Swim dcSwim) {

        if(LOGGER.getLevel()==Level.FINE) {
            startTime2 = System.currentTimeMillis();
        }

        List<Track> cands = new ArrayList<>();
        if (crossList.isEmpty()) {
            return cands;
        }

        for (List<Cross> aCrossList : crossList) {
            //initialize
            Track cand = new Track();
            TrajectoryFinder trjFind = new TrajectoryFinder();

            if(LOGGER.getLevel()==Level.FINE) {
                startTime = System.currentTimeMillis();
            }
            Trajectory traj = trjFind.findTrajectory(aCrossList, DcDetector, dcSwim);
            LOGGER.log(Level.FINE, "Trajectory finding = " + (System.currentTimeMillis() - startTime));
            

            if (traj == null) {
                continue;
            }

            //look for straight tracks    
            if (aCrossList.size() == 3 && Math.abs(TORSCALE) < 0.001) {
                cand.addAll(aCrossList);
                cand.set_Sector(aCrossList.get(0).get_Sector());
                //no field --> fit straight track
                this.getStraightTrack(cand);
                if (cand.get_pAtOrig() != null) {
                    cand.set_Id(cands.size() + 1);
                    // the state vector at the region 1 cross
                    StateVec VecAtReg1MiddlePlane = new StateVec(cand.get(0).get_Point().x(),
                            cand.get(0).get_Point().y(),
                            cand.get(0).get_Dir().x() / cand.get(0).get_Dir().z(),
                            cand.get(0).get_Dir().y() / cand.get(0).get_Dir().z());
                    cand.set_StateVecAtReg1MiddlePlane(VecAtReg1MiddlePlane);
                    // initialize the fitter with the candidate track
                    KFitterDoca kFit = new KFitterDoca(cand, DcDetector, false, dcSwim, 0);
                    kFit.totNumIter = 1;

                    if(LOGGER.getLevel()==Level.FINE) {
                        startTime = System.currentTimeMillis();
                    }
                    kFit.runFitter(cand.get(0).get_Sector());
                    LOGGER.log(Level.FINE, "Kalman fitter = " + (System.currentTimeMillis() - startTime));
                    

                    if (kFit.finalStateVec == null) {
                        continue;
                    }

                    // initialize the state vector corresponding to the last measurement site
                    StateVec fn = new StateVec();

                    //LOGGER.log(Level.FINE, " fit failed due to chi2 "+kFit.setFitFailed+" p "+1./Math.abs(kFit.finalStateVec.Q));
                    if (!kFit.setFitFailed && kFit.finalStateVec != null) {
                        // set the state vector at the last measurement site
                        fn.set(kFit.finalStateVec.x, kFit.finalStateVec.y, kFit.finalStateVec.tx, kFit.finalStateVec.ty);
                        //set the track parameters if the filter does not fail
                        fn.setZ(kFit.finalStateVec.z);
                        cand.setFinalStateVec(fn);
                        cand.set_P(1. / Math.abs(kFit.finalStateVec.Q));
                        cand.set_Q((int) Math.signum(kFit.finalStateVec.Q));
                        this.setTrackPars(cand, traj, trjFind, fn, kFit.finalStateVec.z, DcDetector, dcSwim);
                        if(cand.fit_Successful==true) {
                            // candidate parameters are set from the state vector
                            cand.set_FitChi2(kFit.chi2);
                            cand.set_FitNDF(kFit.NDF);
                            cand.set_FitConvergenceStatus(kFit.ConvStatus);
                            cand.set_Id(cands.size() + 1);
                            cand.set_CovMat(kFit.finalCovMat.covMat);
                            cand.set_Trajectory(kFit.kfStateVecsAlongTrajectory);
                            // add candidate to list of tracks
                            cands.add(cand);
                        }
                    }
                    //this.matchHits(traj.get_Trajectory(), cand, DcDetector);
                    cands.add(cand);
                }
            }
        }
        return cands;
    }

    private List<Track> findCurvedTracks(CrossList crossList, DCGeant4Factory DcDetector, double TORSCALE, Swim dcSwim,
            boolean donotapplyCuts) {
        if(LOGGER.getLevel()==Level.FINE) {
            startTime2 = System.currentTimeMillis();
        }

        List<Track> cands = new ArrayList<>();
        if (crossList.isEmpty()) {
            return cands;
        }
        for (List<Cross> aCrossList : crossList) {
            aCrossList.sort(Comparator.comparing(Cross::get_Sector).thenComparing(Cross::get_Region));
            if (aCrossList.size() < 3) {
               // return cands;
               continue;
            }
            //initialize
            Track cand = new Track();
            TrajectoryFinder trjFind = new TrajectoryFinder();

            if(LOGGER.getLevel()==Level.FINE) {
                startTime = System.currentTimeMillis();
            }
            Trajectory traj = trjFind.findTrajectory(aCrossList, DcDetector, dcSwim);
            
            LOGGER.log(Level.FINE, "Trajectory finding = " + (System.currentTimeMillis() - startTime));
            

            if (traj == null) {
                continue;
            }

            if (aCrossList.size() == 3 && this.PassNSuperlayerTracking(aCrossList, cand)) {
                cand.addAll(aCrossList);
                cand.set_Sector(aCrossList.get(0).get_Sector());

                // set the candidate trajectory using the parametrization of the track trajectory
                // and estimate intefral Bdl along that path
                cand.set_Trajectory(traj.get_Trajectory());
                cand.set_IntegralBdl(traj.get_IntegralBdl());

                //require 3 crosses to make a track (allows for 1 pseudo-cross)
                if (cand.size() == 3) {
                //    LOGGER.log(Level.FINE, "---- cand in sector " + aCrossList.get(0).get_Sector());
                //    LOGGER.log(Level.FINE, aCrossList.get(0).printInfo());
                //    LOGGER.log(Level.FINE, aCrossList.get(1).printInfo());
                //    LOGGER.log(Level.FINE, aCrossList.get(2).printInfo());
                //    LOGGER.log(Level.FINE, "---------------");
                    double x1 = aCrossList.get(0).get_Point().x();
                    double y1 = aCrossList.get(0).get_Point().y();
                    double z1 = aCrossList.get(0).get_Point().z();
                    double x2 = aCrossList.get(1).get_Point().x();
                    double y2 = aCrossList.get(1).get_Point().y();
                    double z2 = aCrossList.get(1).get_Point().z();
                    double x3 = aCrossList.get(2).get_Point().x();
                    double y3 = aCrossList.get(2).get_Point().y();
                    double z3 = aCrossList.get(2).get_Point().z();
                    double ux = aCrossList.get(0).get_Dir().x();
                    double uy = aCrossList.get(0).get_Dir().y();
                    double uz = aCrossList.get(0).get_Dir().z();
                    double thX = ux / uz;
                    double thY = uy / uz;
                    double theta3s2 = Math.atan(cand.get(2).get_Segment2().get_fittedCluster().get_clusterLineFitSlope());
                    double theta1s2 = Math.atan(cand.get(0).get_Segment2().get_fittedCluster().get_clusterLineFitSlope());
                    double theta3s1 = Math.atan(cand.get(2).get_Segment1().get_fittedCluster().get_clusterLineFitSlope());
                    double theta1s1 = Math.atan(cand.get(0).get_Segment1().get_fittedCluster().get_clusterLineFitSlope());

                    if (cand.get(0).get_Segment2().get_Id() == -1) {
                        theta1s2 = theta1s1; //do not use
                    }                    //theta1s2=-999; //do not use
                    if (cand.get(0).get_Segment1().get_Id() == -1) {
                        theta1s1 = theta1s2;
                    }
                    //theta1s1=-999;
                    if (cand.get(2).get_Segment2().get_Id() == -1) {
                        theta3s2 = theta3s1;
                    }
                    //theta3s2=-999;
                    if (cand.get(2).get_Segment1().get_Id() == -1) {
                        theta3s1 = theta3s2;
                    }
                    //theta3s1=-999;
                    double theta3 = 0;
                    double theta1 = 0;

                    double chisq = Double.POSITIVE_INFINITY;
                    double chi2;
                    double iBdl = traj.get_IntegralBdl();
                    double[] pars;

                    if(LOGGER.getLevel()==Level.FINE) {
                        startTime = System.currentTimeMillis();
                    }
                    pars = getTrackInitFit(cand.get(0).get_Sector(), x1, y1, z1, x2, y2, z2, x3, y3, z3,
                            ux, uy, uz, thX, thY,
                            theta1s1, theta3s1,
                            traj.get_IntegralBdl(), TORSCALE, dcSwim);
                    chi2 = pars[0];
                    if (chi2 < chisq) {
                        chisq = chi2;
                        theta1 = theta1s1;
                        theta3 = theta3s1;
                        iBdl = pars[1];
                    }
                    
                    LOGGER.log(Level.FINE, "TrackInitFit-1 = " + (System.currentTimeMillis() - startTime));

                    if(LOGGER.getLevel()==Level.FINE) {
                        startTime = System.currentTimeMillis();
                    }
                    pars = getTrackInitFit(cand.get(0).get_Sector(), x1, y1, z1, x2, y2, z2, x3, y3, z3,
                            ux, uy, uz, thX, thY,
                            theta1s1, theta3s2,
                            traj.get_IntegralBdl(), TORSCALE, dcSwim);
                    chi2 = pars[0];
                    if (chi2 < chisq) {
                        chisq = chi2;
                        theta1 = theta1s1;
                        theta3 = theta3s2;
                        iBdl = pars[1];
                    }
                    
                    LOGGER.log(Level.FINE, "TrackInitFit-2 = " + (System.currentTimeMillis() - startTime));

                    if(LOGGER.getLevel()==Level.FINE) {
                        startTime = System.currentTimeMillis();
                    }
                    pars = getTrackInitFit(cand.get(0).get_Sector(), x1, y1, z1, x2, y2, z2, x3, y3, z3,
                            ux, uy, uz, thX, thY,
                            theta1s2, theta3s1,
                            traj.get_IntegralBdl(), TORSCALE, dcSwim);
                    chi2 = pars[0];
                    if (chi2 < chisq) {
                        chisq = chi2;
                        theta1 = theta1s2;
                        theta3 = theta3s1;
                        iBdl = pars[1];
                    }
                    
                    LOGGER.log(Level.FINE, "TrackInitFit-3 = " + (System.currentTimeMillis() - startTime));

                    if(LOGGER.getLevel()==Level.FINE) {
                        startTime = System.currentTimeMillis();
                    }
                    pars = getTrackInitFit(cand.get(0).get_Sector(), x1, y1, z1, x2, y2, z2, x3, y3, z3,
                            ux, uy, uz, thX, thY,
                            theta1s2, theta3s2,
                            traj.get_IntegralBdl(), TORSCALE, dcSwim);
                    chi2 = pars[0];
                    if (chi2 < chisq) {
                        theta1 = theta1s2;
                        theta3 = theta3s2;
                        iBdl = pars[1];
                    }
                    
                    LOGGER.log(Level.FINE, "TrackInitFit-4 = " + (System.currentTimeMillis() - startTime));

                    if (chi2 > Constants.SEEDCUT && donotapplyCuts == false) {
                        continue;
                    }
                    // compute delta theta using the non-pseudo segments in region 1 and 3

                    // get integral Bdl from the swimmer trajectory
                    //double iBdl = traj.get_IntegralBdl(); 
                    if (iBdl != 0) {
                        // momentum estimate if Bdl is non zero and the track has curvature  
                        double p = calcInitTrkP(ux, uy, uz, thX, thY,
                                theta1, theta3,
                                iBdl, TORSCALE);
                        if(LOGGER.getLevel()==Level.FINE) {
                            startTime = System.currentTimeMillis();
                        }
                        int q = this.calcInitTrkQ(theta1, theta3, TORSCALE);
                        
                        LOGGER.log(Level.FINE, "calcInitTrkQ = " + (System.currentTimeMillis() - startTime));

                        if (p > 11) {
                            p = 11;
                        }
                        //if(p>Constants.MAXTRKMOM || p< Constants.MINTRKMOM)
                        //  continue;

                        cand.set_Q(q);
                        // momentum correction using the swam trajectory iBdl
                        cand.set_P(p);

                        // the state vector at the region 1 cross
                        StateVec VecAtReg1MiddlePlane = new StateVec(cand.get(0).get_Point().x(),
                                cand.get(0).get_Point().y(),
                                cand.get(0).get_Dir().x() / cand.get(0).get_Dir().z(),
                                cand.get(0).get_Dir().y() / cand.get(0).get_Dir().z());
                        cand.set_StateVecAtReg1MiddlePlane(VecAtReg1MiddlePlane);
                        // initialize the fitter with the candidate track
                        KFitterDoca kFit = null;
                        StateVec fitStateVec = null;
                        
                        LOGGER.log(Level.FINE, "Kalman fitter - 2 = " + (System.currentTimeMillis() - startTime));
                        
                        // prefer to initialize the seed with region 2 cross due to higher background in region 1
                        int crossIdxinList = 1;
                        if (cand.get(1).isPseudoCross) {
                            crossIdxinList = 0;
                        }
                        kFit = new KFitterDoca(cand, DcDetector, false, dcSwim, crossIdxinList);
                        kFit.totNumIter = 10;
                        kFit.runFitter(cand.get(0).get_Sector());
                        if (kFit.finalStateVec == null) {
                            continue;
                        } else {
                            if (kFit.chi2 < Constants.MAXCHI2) {
                                
                                fitStateVec = new StateVec(kFit.finalStateVec.x,
                                        kFit.finalStateVec.y, kFit.finalStateVec.tx, kFit.finalStateVec.ty);
                                q = (int) Math.signum(kFit.finalStateVec.Q);
                                p = 1. / Math.abs(kFit.finalStateVec.Q);
                                fitStateVec.setZ(kFit.finalStateVec.z);

                                //set the track parameters 
                                cand.set_P(p);
                                cand.set_Q(q);

                                // candidate parameters 
                                cand.set_FitChi2(kFit.chi2);
                                cand.set_FitNDF(kFit.NDF);
                                cand.set_FitConvergenceStatus(kFit.ConvStatus);

                                cand.set_CovMat(kFit.finalCovMat.covMat);
                                cand.set_Trajectory(kFit.kfStateVecsAlongTrajectory);

                                cand.setFinalStateVec(fitStateVec);
                                cand.set_Id(cands.size() + 1);
                                this.setTrackPars(cand, traj,
                                        trjFind, fitStateVec,
                                        fitStateVec.getZ(),
                                        DcDetector, dcSwim);
                                // add candidate to list of tracks
                                if (cand.fit_Successful = true) {
                                    cands.add(cand);
                                }
                            }
                        }
                    }
                }
            }
        }
        return cands;
    }

}
