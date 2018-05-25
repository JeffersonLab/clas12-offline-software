package org.jlab.rec.dc.track;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.cross.Cross;
import org.jlab.rec.dc.cross.CrossList;
import org.jlab.rec.dc.hit.FittedHit;
import org.jlab.rec.dc.segment.Segment;
import org.jlab.rec.dc.track.fit.KFitter;
import org.jlab.rec.dc.trajectory.DCSwimmer;
import org.jlab.rec.dc.trajectory.StateVec;
import org.jlab.rec.dc.trajectory.Trajectory;
import org.jlab.rec.dc.trajectory.TrajectoryFinder;
//import org.jlab.rec.dc.trajectory.Vertex;

import trackfitter.fitter.LineFitPars;
import trackfitter.fitter.LineFitter;

/**
 * A class with a method implementing an algorithm that finds lists of track candidates in the DC
 * @author ziegler
 *
 */

public class TrackCandListFinder {

    /**
     * the tracking status = HitBased or TimeBased
     */
    private String trking;
    /**
     * 
     * @param stat the tracking status Hit-based or Time-based
     */
    public TrackCandListFinder(String stat) {
            trking = stat;
    }
    public DCSwimmer dcSwim = new DCSwimmer();
    
    /**
     * 
     * @param crossesInTrk the list of crosses on track
     * @return the number of superlayers used in the fit
     */
    public boolean PassNSuperlayerTracking(List<Cross> crossesInTrk, Track cand) {
        boolean pass = true;
        int NbMissingSl=0; // find the missing superlayers from the pseudo-crosses
        for(Cross c: crossesInTrk) {
            if(c.isPseudoCross) {
                if((c.get_Segment1().get_Id()==-1) || (c.get_Segment2().get_Id()==-1) )
                    NbMissingSl++;
                if(c.get_Segment1().get_Id()==-1)
                    cand.set_MissingSuperlayer(c.get_Segment1().get_Superlayer());
                if(c.get_Segment2().get_Id()==-1)
                    cand.set_MissingSuperlayer(c.get_Segment2().get_Superlayer());
            } else {
                if((c.get_Segment1().get_Status()==1) || (c.get_Segment2().get_Status()==1) )
                    cand.set_Status(1);
            }
        }
        // if more superlayers are missing than the required number in the analysis - skip the track
        if(NbMissingSl>6-Constants.NSUPERLAYERTRACKING) {
            pass = false; 
        }
        return pass;
    }
    
    public double getHitBasedFitChi2ToCrosses(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, 
        double p, int q, double x, double y, double z, double tanThX, double tanThY) {
        double pz = p / Math.sqrt(tanThX*tanThX + tanThY*tanThY + 1);

        dcSwim.SetSwimParameters(x,y,z,
                        -pz*tanThX,-pz*tanThY,-pz,
                         -q);
        double chi2 = 0; // assume err =1 on points 
        double[] R = dcSwim.SwimToPlane(z3);
        chi2+= (R[0] - x3)*(R[0] - x3) + (R[1] - y3)*(R[1] - y3);
        dcSwim.SetSwimParameters(R[0],R[1],R[2],
                        R[3],R[4],R[5],
                         -q);
        R = dcSwim.SwimToPlane(z2);
        dcSwim.SetSwimParameters(R[0],R[1],R[2],
                        R[3],R[4],R[5],
                         -q);
        chi2+= (R[0] - x2)*(R[0] - x2) + (R[1] - y2)*(R[1] - y2);
        dcSwim.SetSwimParameters(R[0],R[1],R[2],
                        R[3],R[4],R[5],
                         -q);
        R = dcSwim.SwimToPlane(z1);
        chi2+= (R[0] - x1)*(R[0] - x1) + (R[1] - y1)*(R[1] - y1);
        
        return chi2;
    }
    
    private double[] getTrackInitFit(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3,
            double ux, double uy, double uz, double thX, double thY, 
            double theta1, double theta3, 
            double iBdl, double TORSCALE) {
        if(theta1<-998 || theta3<-998) {
            return new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
        }
        double[] pars = new double[2];
        
        double chi2 = 0; // assume err =1 on points 
        double intBdl =0;
        
        double p = calcInitTrkP(ux, uy, uz, thX, thY, 
             theta1, theta3, 
             iBdl, TORSCALE);
        double p_x = ux*p;
        double p_y = uy*p;
        double p_z = uz*p;
        
        int q= calcInitTrkQ(theta1, theta3,TORSCALE);
        
        dcSwim.SetSwimParameters(x1,y1,z1,p_x, p_y, p_z, q);
        double[] R = dcSwim.SwimToPlane(z2);
        chi2+= (R[0] - x2)*(R[0] - x2) + (R[1] - y2)*(R[1] - y2);
        intBdl+=R[7];
        dcSwim.SetSwimParameters(R[0],R[1],R[2],
                        R[3],R[4],R[5],
                         q);
        R = dcSwim.SwimToPlane(z3);
        chi2+= (R[0] - x3)*(R[0] - x3) + (R[1] - y3)*(R[1] - y3);
        intBdl+=R[7];
        
        pars[0]= chi2;
        pars[1]= intBdl;
        
        return pars;
    }
    
    private double calcInitTrkP(double ux, double uy, double uz, double thX, double thY, 
            double theta1, double theta3, 
            double iBdl, double TORSCALE) {
        double deltaTheta = theta3-theta1; 
        if(deltaTheta==0)
            return Double.POSITIVE_INFINITY;
        
        // momentum estimate if Bdl is non zero and the track has curvature    
        double pxz = Math.abs(Constants.LIGHTVEL*iBdl/deltaTheta);
        double py = Math.sqrt( (thX*thX+thY*thY+1)/(thX*thX+1) - 1 )*pxz;

        double p = Math.sqrt(pxz*pxz+py*py);
        return p;
    }
    private int calcInitTrkQ(double theta1, double theta3, 
            double TORSCALE) {
        double deltaTheta = theta3-theta1; 
       
        //positive charges bend outward for nominal GEMC field configuration
        int q = (int) Math.signum(deltaTheta); 
        q*= (int)-1*Math.signum(TORSCALE); // flip the charge according to the field scale						
        
        return q;
    }
    /**
     * 
     * @param crossList the input list of crosses
     * @return a list of track candidates in the DC
     */
    public List<Track> getTrackCands(CrossList crossList, DCGeant4Factory DcDetector, double TORSCALE) {
        List<Track> cands = new ArrayList<Track>();
        if(crossList.size()==0) {
            //System.err.print("Error no tracks found");
            return cands;
        }

        for(int i = 0; i<crossList.size(); i++) {
            Track cand = new Track();
            List<Cross> crossesInTrk = crossList.get(i);
            TrajectoryFinder trjFind = new TrajectoryFinder();

            Trajectory traj = trjFind.findTrajectory(crossesInTrk, DcDetector);
            if(traj == null) 
                continue;
            
            if(crossesInTrk.size()==3 && this.PassNSuperlayerTracking(crossesInTrk, cand)==true) {
                cand.addAll(crossesInTrk);
                cand.set_Sector(crossesInTrk.get(0).get_Sector());

                if(Math.abs(TORSCALE)<0.001) {
                //no field --> fit straight track
                    this.getStraightTrack(cand);
                    if(cand.get_pAtOrig()!=null) {
                        cand.set_Id(cands.size()+1);						
                        cands.add(cand); 
                    }
                }	
                // set the candidate trajectory using the parametrization of the track trajectory
                // and estimate intefral Bdl along that path
                cand.set_Trajectory(traj.get_Trajectory());
                cand.set_IntegralBdl(traj.get_IntegralBdl());

                //require 3 crosses to make a track (allows for 1 pseudo-cross)
                if(cand.size()==3) { 
                    //System.out.println("---- cand in sector "+crossesInTrk.get(0).get_Sector());
                    //System.out.println(crossesInTrk.get(0).printInfo());
                    //System.out.println(crossesInTrk.get(1).printInfo());
                    //System.out.println(crossesInTrk.get(2).printInfo());
                    //System.out.println("---------------");
                    double x1 = crossesInTrk.get(0).get_Point().x();
                    double y1 = crossesInTrk.get(0).get_Point().y();
                    double z1 = crossesInTrk.get(0).get_Point().z();
                    double x2 = crossesInTrk.get(1).get_Point().x();
                    double y2 = crossesInTrk.get(1).get_Point().y();
                    double z2 = crossesInTrk.get(1).get_Point().z();
                    double x3 = crossesInTrk.get(2).get_Point().x();
                    double y3 = crossesInTrk.get(2).get_Point().y();
                    double z3 = crossesInTrk.get(2).get_Point().z();
                    double ux = crossesInTrk.get(0).get_Dir().x();
                    double uy = crossesInTrk.get(0).get_Dir().y();
                    double uz = crossesInTrk.get(0).get_Dir().z();
                    double thX = ux/uz;
                    double thY = uy/uz;
                    double theta3s2 = Math.atan(cand.get(2).get_Segment2().get_fittedCluster().get_clusterLineFitSlope());
                    double theta1s2 = Math.atan(cand.get(0).get_Segment2().get_fittedCluster().get_clusterLineFitSlope());
                    double theta3s1 = Math.atan(cand.get(2).get_Segment1().get_fittedCluster().get_clusterLineFitSlope());
                    double theta1s1 = Math.atan(cand.get(0).get_Segment1().get_fittedCluster().get_clusterLineFitSlope());
                    
                    if(cand.get(0).get_Segment2().get_Id()==-1 ) 
                        theta1s2=theta1s1; //do not use
                        //theta1s2=-999; //do not use
                    if(cand.get(0).get_Segment1().get_Id()==-1 ) 
                        theta1s1=theta1s2;
                        //theta1s1=-999;
                    if(cand.get(2).get_Segment2().get_Id()==-1 ) 
                        theta3s2=theta3s1;
                        //theta3s2=-999;
                    if(cand.get(2).get_Segment1().get_Id()==-1 ) 
                        theta3s1=theta3s2;
                        //theta3s1=-999;
                    double theta3=0;
                    double theta1=0;
                    
                    double chisq = Double.POSITIVE_INFINITY;
                    double chi2 = 0;
                    double iBdl = traj.get_IntegralBdl();
                    double[] pars;
                    
                    pars = getTrackInitFit(x1,  y1,  z1,  x2,  y2,  z2,  x3,  y3,  z3,
                     ux,  uy,  uz,  thX,  thY, 
                     theta1s1,  theta3s1, 
                     traj.get_IntegralBdl(),  TORSCALE);
                    chi2 = pars[0];
                    if(chi2<chisq) {
                        chisq = chi2;
                        theta1 = theta1s1;
                        theta3 = theta3s1;
                        iBdl = pars[1];
                    }
                    
                    pars = getTrackInitFit(x1,  y1,  z1,  x2,  y2,  z2,  x3,  y3,  z3,
                     ux,  uy,  uz,  thX,  thY, 
                     theta1s1,  theta3s2, 
                     traj.get_IntegralBdl(),  TORSCALE);
                    chi2 = pars[0];
                    if(chi2<chisq) {
                        chisq = chi2;
                        theta1 = theta1s1;
                        theta3 = theta3s2;
                        iBdl = pars[1];
                    }
                    
                    pars = getTrackInitFit(x1,  y1,  z1,  x2,  y2,  z2,  x3,  y3,  z3,
                     ux,  uy,  uz,  thX,  thY, 
                     theta1s2,  theta3s1, 
                     traj.get_IntegralBdl(),  TORSCALE);
                    chi2 = pars[0];
                    if(chi2<chisq) {
                        chisq = chi2;
                        theta1 = theta1s2;
                        theta3 = theta3s1;
                        iBdl = pars[1];
                    }
                    
                    pars = getTrackInitFit(x1,  y1,  z1,  x2,  y2,  z2,  x3,  y3,  z3,
                     ux,  uy,  uz,  thX,  thY, 
                     theta1s2,  theta3s2, 
                     traj.get_IntegralBdl(),  TORSCALE);
                    chi2 = pars[0];
                    if(chi2<chisq) {
                        chisq = chi2;
                        theta1 = theta1s2;
                        theta3 = theta3s2;
                        iBdl = pars[1];
                    }
                    
                    if(chi2>2500)
                        continue;
                    
                    // compute delta theta using the non-pseudo segments in region 1 and 3
                    
                    // get integral Bdl from the swimmer trajectory
                    //double iBdl = traj.get_IntegralBdl(); 
                    
                    if(iBdl != 0) {
                        // momentum estimate if Bdl is non zero and the track has curvature  
                        double p = calcInitTrkP(ux, uy, uz, thX, thY, 
                                             theta1, theta3, 
                                            iBdl, TORSCALE);
                        int q = calcInitTrkQ(theta1, theta3, TORSCALE);
                       
                        if(p>11)
                            p=11;
                        //if(p>Constants.MAXTRKMOM || p< Constants.MINTRKMOM)
                          //  continue;

                        cand.set_Q(q);
                        // momentum correction using the swam trajectory iBdl
                        cand.set_P(p); 
                        // the state vector at the region 1 cross
                        StateVec VecAtReg1MiddlePlane = new StateVec(cand.get(0).get_Point().x(),cand.get(0).get_Point().y(),
                                        cand.get(0).get_Dir().x()/cand.get(0).get_Dir().z(), cand.get(0).get_Dir().y()/cand.get(0).get_Dir().z());
                        cand.set_StateVecAtReg1MiddlePlane(VecAtReg1MiddlePlane); 	
                        // initialize the fitter with the candidate track
                        KFitter kFit = new KFitter(cand, DcDetector, false);
                        if(this.trking.equalsIgnoreCase("TimeBased"))
                            kFit.totNumIter=30;
                       
                        // initialize the state vector corresponding to the last measurement site
                        StateVec fn = new StateVec();
                        kFit.runFitter();
                        if(kFit.finalStateVec==null)
                            continue;
                        if(this.trking.equalsIgnoreCase("HitBased")) { 
                        /*    System.out.println("x1 "+x1);
                            System.out.println("y1 "+y1);
                            System.out.println("z1 "+z1);
                            System.out.println("x2 "+x2);
                            System.out.println("y2 "+y2);
                            System.out.println("z2 "+z2);
                            System.out.println("x3 "+x3);
                            System.out.println("y3 "+y3);
                            System.out.println("z3 "+z3);
                            System.out.println("x "+ kFit.finalStateVec.x);
                            System.out.println("y "+ kFit.finalStateVec.y);
                            System.out.println("z "+ kFit.finalStateVec.z); */
                            double HBc2 = getHitBasedFitChi2ToCrosses( x1,  y1,  z1,  x2,  y2,  z2,  x3,  y3,  z3, 
                            1./Math.abs(kFit.finalStateVec.Q), (int)Math.signum(kFit.finalStateVec.Q),
                                     kFit.finalStateVec.x, kFit.finalStateVec.y, kFit.finalStateVec.z, kFit.finalStateVec.tx, kFit.finalStateVec.ty);
                            //System.out.println(cand.get(0).get_Sector()+" HB fit to crosses c2 "+HBc2);
                            if(HBc2>1000) {
                                kFit.setFitFailed=true;
                            }
                        }
                        //System.out.println(" fit failed due to chi2 "+kFit.setFitFailed+" p "+1./Math.abs(kFit.finalStateVec.Q));
                        if(kFit.setFitFailed==false && kFit.finalStateVec!=null) {
                            // set the state vector at the last measurement site
                            fn.set(kFit.finalStateVec.x, kFit.finalStateVec.y, kFit.finalStateVec.tx, kFit.finalStateVec.ty); 
                            //set the track parameters if the filter does not fail
                            cand.set_P(1./Math.abs(kFit.finalStateVec.Q));
                            cand.set_Q((int)Math.signum(kFit.finalStateVec.Q));
                            this.setTrackPars(cand, traj, trjFind, fn, kFit.finalStateVec.z, DcDetector);
                            // candidate parameters are set from the state vector
                            cand.set_FitChi2(kFit.chi2);
                            cand.set_FitNDF(kFit.NDF);
                            cand.set_FitConvergenceStatus(kFit.ConvStatus);
                            cand.set_Id(cands.size()+1);
                            cand.set_CovMat(kFit.finalCovMat.covMat);
                            // add candidate to list of tracks
                            cands.add(cand); 
                            
                        }
                    }
                }
            }
        }
        //this.setAssociatedIDs(cands);
        return cands;
    }

    /**
     * 
     * @param cand straight track candidate
     */
    private void getStraightTrack(Track cand) {

        double[] x = new double[3];
        double[] y1 = new double[3];
        double[] y2 = new double[3];
        double[] ex = new double[3];
        double[] ey1 = new double[3];
        double[] ey2 = new double[3];

        for(int i = 0; i < 3; i++) {

            Point3D X = cand.get(i).getCoordsInLab(cand.get(i).get_Point().x(), cand.get(i).get_Point().y(), cand.get(i).get_Point().z());
            Point3D eX = cand.get(i).getCoordsInLab(cand.get(i).get_PointErr().x(), cand.get(i).get_PointErr().y(), cand.get(i).get_PointErr().z());

            x[i] 	= X.z();
            ex[i] 	= eX.z();

            y1[i] 	= X.x();
            ey1[i] 	= eX.x();

            y2[i] 	= X.y();
            ey2[i] 	= eX.y();
        }


        if(x!=null) {

            LineFitter linefit = new LineFitter();
            boolean linefitstatusOK1 = linefit.fitStatus(x, y1, ex, ey1, 3);

            LineFitPars FitPars1 = null;
            LineFitPars FitPars2 = null;
            if(linefitstatusOK1)		
                //  Get the results of the fits
                FitPars1 = linefit.getFit();

            boolean linefitstatusOK2 = linefit.fitStatus(x, y2, ex, ey2, 3);
            if(linefitstatusOK2)		
                //  Get the results of the fits
                FitPars2 = linefit.getFit();

            double X0 = -99999;
            double Y0 = -99999;

            if(FitPars1 != null && FitPars2!=null) {

                X0 = FitPars1.intercept();
                Y0 = FitPars2.intercept();

                Point3D trkR1X = new Point3D(FitPars1.slope()*x[0]+FitPars1.intercept(), FitPars2.slope()*x[0]+FitPars2.intercept(), x[0]);
                Point3D trkR3X = new Point3D(FitPars1.slope()*x[2]+FitPars1.intercept(), FitPars2.slope()*x[2]+FitPars2.intercept(), x[2]);

                Vector3D trkDir = new Vector3D(trkR3X.x()-trkR1X.x(), trkR3X.y()-trkR1X.y(), trkR3X.z()-trkR1X.z()).asUnit();
                trkDir.scale(10);

                Point3D trkVtx = new Point3D(X0, Y0, 0);

                cand.set_P(10);
                cand.set_Q(-1); // assume it's a muon
                cand.set_pAtOrig(trkDir);
                cand.set_Vtx0(trkVtx);
                cand.set_PreRegion1CrossPoint(new Point3D(trkR1X.x()-trkDir.x(), trkR1X.y()-trkDir.y(), trkR1X.z()-trkDir.z()) );
                cand.set_PostRegion3CrossPoint(new Point3D(trkR3X.x()+trkDir.x(), trkR3X.y()+trkDir.y(), trkR3X.z()+trkDir.z()));
                cand.set_PreRegion1CrossDir(new Point3D(trkDir.x(), trkDir.y(), trkDir.z()));
                cand.set_PostRegion3CrossDir(new Point3D(trkDir.x(), trkDir.y(), trkDir.z()));
                cand.set_Region1TrackX(trkR1X);
                cand.set_Region1TrackP(new Point3D(trkDir.x(), trkDir.y(), trkDir.z()));
                
                cand.set_PathLength(trkR3X.distance(trkVtx));
            }
        }
    }
    
    /**
     * 
     * @param x x coordinate in the lab frame
     * @param y y coordinate in the lab frame
     * @return the sector in the DC lab frame system corresponding to the (x,y) coordinates
     */
    private int getSector(double x, double y) {
        double phi = Math.toDegrees(FastMath.atan2(y, x));
        double ang = phi + 30;
        while (ang < 0) {
                ang += 360;
        }
        int sector = 1 + (int)(ang/60.);

        if(sector ==7 )
            sector =6;

        if ((sector < 1) || (sector > 6)) {
            System.err.println("Track sector not found....");
        }
        return sector;
    }

    /**
     * 
     * @param cand the track candidate
     * @param traj the track trajectory
     * @param trjFind the track trajectory utility
     * @param stateVec the track state vector at the last measurement site used by the Kalman Filter
     * @param z the z position in the tilted sector coordinate system at the last measurement site 
     * @param getDcDetector the detector geometry
     */
    public void setTrackPars(Track cand, Trajectory traj, TrajectoryFinder trjFind, StateVec stateVec, double z, DCGeant4Factory getDcDetector) {
        double pz = cand.get_P() / Math.sqrt(stateVec.tanThetaX()*stateVec.tanThetaX() + stateVec.tanThetaY()*stateVec.tanThetaY() + 1);

        //System.out.println("Setting track params for ");stateVec.printInfo();
        dcSwim.SetSwimParameters(stateVec.x(),stateVec.y(),z,
                        pz*stateVec.tanThetaX(),pz*stateVec.tanThetaY(),pz,
                         cand.get_Q());

        // swimming to a ref points outside of the last DC region
        double[] VecAtTarOut = dcSwim.SwimToPlane(592);
        double xOuter  = VecAtTarOut[0];
        double yOuter  = VecAtTarOut[1];
        double zOuter  = VecAtTarOut[2];
        double uxOuter = VecAtTarOut[3]/cand.get_P();
        double uyOuter = VecAtTarOut[4]/cand.get_P();
        double uzOuter = VecAtTarOut[5]/cand.get_P();
        //Cross crossR = new Cross(cand.get(2).get_Sector(), cand.get(2).get_Region(), -1);
        Cross crossR = new Cross(cand.get(cand.size()-1).get_Sector(), cand.get(cand.size()-1).get_Region(), -1);
        Point3D xOuterExtp = crossR.getCoordsInLab(xOuter, yOuter, zOuter);
        Point3D uOuterExtp = crossR.getCoordsInLab(uxOuter, uyOuter, uzOuter);

        //set the pseudocross at extrapolated position
        cand.set_PostRegion3CrossPoint(xOuterExtp);
        cand.set_PostRegion3CrossDir(uOuterExtp);

        dcSwim.SetSwimParameters(stateVec.x(),stateVec.y(),z,
                        -pz*stateVec.tanThetaX(),-pz*stateVec.tanThetaY(),-pz,
                         -cand.get_Q());

        //swimming to a ref point upstream of the first DC region
        double[] VecAtTarIn = dcSwim.SwimToPlane(180);

        if(VecAtTarIn==null) {
                cand.fit_Successful=false;
                return;
        }

        if(VecAtTarIn[6]+VecAtTarOut[6]<200) {
                cand.fit_Successful=false;
                return;
        }

        double xOr = VecAtTarIn[0];
        double yOr = VecAtTarIn[1];
        double zOr = VecAtTarIn[2];
        double pxOr = -VecAtTarIn[3];
        double pyOr = -VecAtTarIn[4];
        double pzOr = -VecAtTarIn[5];

        if(traj!=null && trjFind!=null) {
                traj.set_Trajectory(trjFind.getStateVecsAlongTrajectory(xOr, yOr, pxOr/pzOr, pyOr/pzOr, cand.get_P(),cand.get_Q(), getDcDetector));
                cand.set_Trajectory(traj.get_Trajectory());
        }
        //cand.set_Vtx0_TiltedCS(trakOrigTiltSec);
        //cand.set_pAtOrig_TiltedCS(pAtOrigTiltSec.toVector3D());

        //Cross C = new Cross(cand.get(2).get_Sector(), cand.get(2).get_Region(), -1);
        Cross C = new Cross(cand.get(cand.size()-1).get_Sector(), cand.get(cand.size()-1).get_Region(), -1);
        
        Point3D trkR1X = C.getCoordsInLab(xOr,yOr,zOr);
        Point3D trkR1P = C.getCoordsInLab(pxOr,pyOr,pzOr);
        cand.set_Region1TrackX(new Point3D(trkR1X.x(), trkR1X.y(), trkR1X.z()));
        cand.set_Region1TrackP(new Point3D(trkR1P.x(), trkR1P.y(), trkR1P.z()));

        Point3D R3TrkPoint = C.getCoordsInLab(stateVec.x(),stateVec.y(),z);
        Point3D R3TrkMomentum = C.getCoordsInLab(pz*stateVec.tanThetaX(),pz*stateVec.tanThetaY(),pz);
        dcSwim.SetSwimParameters(R3TrkPoint.x(), R3TrkPoint.y(), R3TrkPoint.z(), -R3TrkMomentum.x(), -R3TrkMomentum.y(), -R3TrkMomentum.z(), -cand.get_Q());

        // recalc new vertex using plane stopper
        //int sector = cand.get(2).get_Sector();
        int sector = cand.get(cand.size()-1).get_Sector();
        double theta_n = ((double)(sector-1))*Math.toRadians(60.);
        double x_n = Math.cos(theta_n) ; 
        double y_n = Math.sin(theta_n) ; 
        double[] Vt = dcSwim.SwimToPlaneBoundary(0, new Vector3D(x_n, y_n, 0), -1);
     
        int status = 99999;

        int LR = 0;
        for(Cross crs : cand) {		
            Segment s1 = crs.get_Segment1();
            Segment s2 = crs.get_Segment2();

            for(FittedHit h : s1) 
                LR+=h._lr;
            for(FittedHit h : s2) 
                LR+=h._lr;

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
        double totPathLen =  PathInFromR3+VecAtTarOut[6];
        cand.set_TotPathLen(totPathLen);

        cand.set_Vtx0(new Point3D(xOrFix,yOrFix, zOrFix));
        cand.set_pAtOrig(new Vector3D(pxOrFix, pyOrFix, pzOrFix));

        double[] VecAtHtccSurf = dcSwim.SwimToSphere(175);
        double xInner  = VecAtHtccSurf[0];
        double yInner  = VecAtHtccSurf[1];
        double zInner  = VecAtHtccSurf[2];
        double uxInner = VecAtHtccSurf[3]/cand.get_P();
        double uyInner = VecAtHtccSurf[4]/cand.get_P();
        double uzInner = VecAtHtccSurf[5]/cand.get_P();

        //set the pseudocross at extrapolated position
        cand.set_PreRegion1CrossPoint(new Point3D(xInner,yInner,zInner));
        cand.set_PreRegion1CrossDir(new Point3D(uxInner,uyInner,uzInner));

        cand.fit_Successful=true;
        cand.set_TrackingInfoString(trking);
    }

    /**
     * 
     * @param trkcands the list of track candidates
     * Removes the list of tracks that overlap with the track selected based on best chi2
     */
    public void removeOverlappingTracks(List<Track> trkcands) {
        List<Track> selectedTracks =new ArrayList<Track>();
        List<Track> list = new  ArrayList<Track>();
        int size = trkcands.size();
        for(int i =0; i<size; i++) { 
            list.clear();
            this.getOverlapLists(trkcands.get(i), trkcands, list);
            trkcands.removeAll(list);
            size-=list.size();
            Track selectedTrk = this.FindBestTrack(list);
            if(selectedTrk==null)
                continue;
            //if(this.ListContainsTrack(selectedTracks, selectedTrk)==false)
            selectedTracks.add(selectedTrk);
        }
        //trkcands.removeAll(trkcands);
        trkcands.addAll(selectedTracks);
    }
    /**
     * 
     * @param selectedTracks  the list of selected tracks
     * @param selectedTrk the selected track
     * @return a boolean indicating if the track is in the list
     */
    private boolean ListContainsTrack(List<Track> selectedTracks, Track selectedTrk) {
        boolean isInList = false;
        for(Track trk : selectedTracks) {
            if(trk==null)
                continue;
                if(trk.get_Id()==selectedTrk.get_Id())
                    isInList=true;
        }
        return isInList;
    }
    /**
     * 
     * @param track the track
     * @param trkcands the list of candidates
     * @param list the list of selected tracks
     */
    private void getOverlapLists(Track track, List<Track> trkcands, List<Track> list) { 
        for(int i =0; i<trkcands.size(); i++) { 
            if( (track.get(0).get_Id()!=-1 && track.get(0).get_Id()==trkcands.get(i).get(0).get_Id()) || 
                        (track.get(1).get_Id()!=-1 && track.get(1).get_Id()==trkcands.get(i).get(1).get_Id()) || 
                        (track.get(2).get_Id()!=-1 && track.get(2).get_Id()==trkcands.get(i).get(2).get_Id()) ) {
                list.add(trkcands.get(i));

            }
        }
    }
    /**
     * 
     * @param trkList the list of tracks
     * @return the track with the best chi2 from the list
     */
    private Track FindBestTrack(List<Track> trkList) {
        double bestChi2 = 9999999;
        Track bestTrk = null;

        for(int i =0; i<trkList.size(); i++) {
            if(trkList.get(i).get_FitChi2()<bestChi2) {
                bestChi2 = trkList.get(i).get_FitChi2();
                bestTrk = trkList.get(i);
            }
        }
        return bestTrk;
    }



public void matchHits(List<StateVec> stateVecAtPlanesList, Track trk, DCGeant4Factory DcDetector) {
    int planeIdNum=0;
    if(stateVecAtPlanesList==null)
        return;
    for(StateVec st : stateVecAtPlanesList) {
        if(st==null)
            return;
        planeIdNum++;
        for(Cross c : trk) { 
                for(FittedHit h1 : c.get_Segment1()) { 
                        if(planeIdNum== (h1.get_Superlayer()-1)*6+h1.get_Layer() ) {
                            h1.setAssociatedStateVec(st);   
                            h1.setSignalPropagTimeAlongWire(DcDetector);
                            h1.setSignalTimeOfFlight(); 
                        }
                }
                for(FittedHit h2 : c.get_Segment2()) {
                        if(planeIdNum== (h2.get_Superlayer()-1)*6+h2.get_Layer() ) {
                            h2.setAssociatedStateVec(st);
                            h2.setSignalPropagTimeAlongWire(DcDetector);
                            h2.setSignalTimeOfFlight();
                        }
                }
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

}
