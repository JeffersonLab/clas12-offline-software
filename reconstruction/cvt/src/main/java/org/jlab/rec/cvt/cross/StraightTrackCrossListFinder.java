package org.jlab.rec.cvt.cross;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Plane3D;

import org.jlab.geom.prim.Point3D;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.fit.LineFitPars;
import org.jlab.rec.cvt.fit.LineFitter;
import org.jlab.rec.cvt.svt.SVTGeometry;
import org.jlab.rec.cvt.svt.SVTParameters;
import org.jlab.rec.cvt.trajectory.Ray;

/**
 * A class with methods used to find lists of crosses. This is the Pattern
 * Recognition step used in track seeding, to find the points that are
 * consistent with belonging to the same track. This step precedes the initial
 * estimates of the track parameters.
 *
 * @author ziegler
 *
 */
public class StraightTrackCrossListFinder {

    public StraightTrackCrossListFinder() {
    }

    public CrossList findTrackSeeds(List<Cross> crosses) {

        /// The principle of Hough Transform in pattern recognition is as follows.
        /// For every point (rho, z) on a line there exists an infinite number of
        /// lines that go through this point.  Each such line can be parametrized
        /// with parameters (r, theta) such that r = rho * cos(theta) + z * sin(theta),
        /// where theta is the polar angle of the line which is perpendicular to it
        /// and intersects the origin, and r is the distance between that line and
        /// the origin, for a given theta.
        /// Hence a point in (rho, z) parameter space corresponds to a sinusoidal
        /// curve in (r, theta) parameter space, which is the so-called Hough-
        /// transform space.
        /// Points that fall on a line in (rho, z) space correspond to sinusoidal
        /// curves that intersect at a common point in Hough-transform space.
        /// Remapping this point in (rho, z) space yields the line that Contains
        /// the points in the original line.
        /// This method is a pattern recognition tool used to select groups of points
        /// belonging to the same shape, such as a line or a circle.
        /// To find the ensemble of points belonging to a line in the original space
        /// the Hough transform algorithm makes use of an array, called an accumulator.
        /// The principle of the accumulator is a counting method.
        /// The dimensions of the array is equal to the number of parameters in
        /// Hough transform space, which is 2 corresponding to the (r, theta) pair
        /// in our particular case.
        /// The bin size in the array are finite intervals in r and theta, which are
        /// called accumulator cells.
        /// The bin content of cells along the curve of discretized (r, theta) values
        /// get incremented.  The cell with the highest count corresponds to the
        /// intersection of the curves.  This is a numerical method to find
        /// the intersection of any number of curves.
        /// Once the accumulator array has been filled with all (r, theta) points
        /// peaks and their associated (rho, z) points are determined.
        /// From these, sets of points belonging to common lines can be
        /// determined.
        /// This is a preliminary pattern recognition method used to identify
        /// reconstructed hits belonging to the same track-segment.
        CrossList crossListFinal = new CrossList();

        if (crosses.size() < 3) {
            crossListFinal.add(0, (ArrayList<Cross>) crosses);
            return crossListFinal;
        }

        // From this calculate the bin size in the theta accumulator array
        double ThetaMin = 0.;
        double ThetaMax = 360.;

        // Define the dimension of the r accumulator array
        //int N_t = 180;
        //int N_r = 180;
        int N_t = 180;
        int N_r = 45;
        // From this calculate the bin size in the theta accumulator array
        double RMin = -180;
        double RMax = 180;

        double SizeThetaBin = (ThetaMax - ThetaMin) / ((double) N_t);

        int[][] R_Z_Accumul;
        R_Z_Accumul = new int[N_r][N_t];

        // cache the cos and sin theta values [for performance improvement]
        double[] cosTheta_Rz_array;
        double[] sinTheta_Rz_array;

        // the values corresponding to the peaks in the array
        double[] binrMaxR_Z;
        double[] bintMaxR_Z;
        binrMaxR_Z = new double[N_r * N_t];
        bintMaxR_Z = new double[N_r * N_t];

        cosTheta_Rz_array = new double[N_t];
        sinTheta_Rz_array = new double[N_t];

        for (int j_t = 0; j_t < N_t; j_t++) {
            // theta_j in the middle of the bin :
            //System.out.println(" bin "+j_t);
            double theta_j = ThetaMin + (0.5 + j_t) * SizeThetaBin;
            cosTheta_Rz_array[j_t] = Math.cos(Math.toRadians(theta_j));
            sinTheta_Rz_array[j_t] = Math.sin(Math.toRadians(theta_j));
            //System.out.println("             theta "+theta_j+"  cos "+Math.cos(Math.toRadians(theta_j))+" sin "+Math.cos(Math.toRadians(theta_j)));
        }

        // loop over points to fill the accumulator arrays
        for (int i = 0; i < crosses.size(); i++) {

            double rho = crosses.get(i).get_Point().y();
            double z = crosses.get(i).get_Point().x();

            // fill the accumulator arrays
            for (int j_t = 0; j_t < N_t; j_t++) {
                // cashed theta_j in the middle of the bin :
                //double theta_j   = ThetaMin + (0.5 + j_t)*SizeThetaBin;
                // r_j corresponding to that theta_j:
                double r_j = rho * cosTheta_Rz_array[j_t] + z * sinTheta_Rz_array[j_t];
                // this value of r_j falls into the following bin in the r array:
                int j_r = (int) Math.floor(N_r * (r_j - RMin) / (float) (RMax - RMin));
                //System.out.println("check range "+RMin+" [ "+r_j +" --> "+j_r+" ] "+RMax);
                // increase this accumulator cell:
                R_Z_Accumul[j_r][j_t]++;
                //if(R_Z_Accumul[j_r][j_t]>=1)
                //System.out.println(" accumulator value at (x, y ) = ("+r_j+", "+(ThetaMin + (0.5 + j_t)*SizeThetaBin) +") falls in bin ["+j_r+" ] ["+j_t+" ] = "+R_Z_Accumul[j_r][j_t]);
            }

        }

        // loop over accumulator array to find peaks (allows for more than one peak for multiple tracks)
        // The accumulator cell count must be at least half the total number of hits
        // Make binrMax, bintMax arrays to allow for more than one peak
        int thresholdMin = 3;
        int threshold = thresholdMin; // minimum number of crosses requirement
        int nbPeaksR_Z = 0;

        // 1st find the peaks in the R_Z accumulator array
        for (int ibinr1 = 0; ibinr1 < N_r; ibinr1++) {
            for (int ibint1 = 0; ibint1 < N_t; ibint1++) {
                //find the peak

                if (R_Z_Accumul[ibinr1][ibint1] >= thresholdMin) {

                    if (R_Z_Accumul[ibinr1][ibint1] > threshold) {
                        threshold = R_Z_Accumul[ibinr1][ibint1];
                    }

                    binrMaxR_Z[nbPeaksR_Z] = ibinr1;
                    bintMaxR_Z[nbPeaksR_Z] = ibint1;
                    nbPeaksR_Z++;

                }
            }
        }

        // For a given Maximum value of the accumulator, find the set of points associated with it;
        //  for this, begin again loop over all the points
        ArrayList<ArrayList<Cross>> crossLists = new ArrayList<ArrayList<Cross>>();
        int index = 0;

        for (int p = nbPeaksR_Z - 1; p > -1; p--) {
            // Make a new list
            ArrayList<Cross> crossList = new ArrayList<Cross>();

            for (int i = 0; i < crosses.size(); i++) {

                double rho = crosses.get(i).get_Point().y();
                double z = crosses.get(i).get_Point().x();

                for (int j_t = 0; j_t < N_t; j_t++) {
                    // theta_j in the middle of the bin :
                    //double theta_j   = ThetaMin + (0.5 + j_t)*SizeThetaBin;
                    // r_j corresponding to that theta_j:
                    double r_j = rho * cosTheta_Rz_array[j_t] + z * sinTheta_Rz_array[j_t];
                    // this value of r_j falls into the following bin in the r array:
                    int j_r = (int) Math.floor(N_r * (r_j - RMin) / (float) (RMax - RMin));

                    // match bins:
                    if (j_r == binrMaxR_Z[p] && j_t == bintMaxR_Z[p]) {
                        crossList.add(crosses.get(i));  // add this hit

                    }
                }
            }
            // if the region uniqueness is satisfied pass the crosslist
            boolean passList = this.regionUniquenessFlag(crossList);
            if (passList) {
                if (!crossLists.contains(crossList)) {
                    crossLists.add(index, crossList);

                    index++;
                }
            }
        }

        //remove duplicate lists
        for (int i = crossLists.size() - 1; i > -1; i--) {
            for (int j = crossLists.size() - 1; j > -1; j--) {
                if (i == j) {
                    continue;
                }
                if (crossLists.get(i) == null || crossLists.get(j) == null) {
                    continue;
                }
                if (crossLists.get(i).size() < crossLists.get(j).size()) {
                    continue;
                }
                if (crossLists.get(i).size() > 0 && crossLists.get(j).size() > 0) {
                    crossListFinal.Contains(crossLists.get(i), crossLists.get(j));

                }
            }
        }
        // create the indexed arraylists
        ArrayList<ArrayList<Cross>> newcrossLists = new ArrayList<ArrayList<Cross>>();
        int newListIndex = 0;
        for (int i = 0; i < crossLists.size(); i++) {
            if (crossLists.get(i).size() > 0) {
                newcrossLists.add(newListIndex, crossLists.get(i));
                newListIndex++;
            }
        }

        crossListFinal.addAll(newcrossLists);

        return crossListFinal;

    }

    /**
     *
     * @param crossList the crosslist
     * @return a flag to indicate that there is a unique cross per region in a
     * crosslist
     */
    private boolean regionUniquenessFlag(ArrayList<Cross> crossList) {
        int[] theRegionsCount = new int[6];
        boolean passList = true;

        for (Cross thecross : crossList) {

            theRegionsCount[thecross.get_SVTCosmicsRegion() - 1]++;

            if (crossList.size() == 3 && theRegionsCount[thecross.get_SVTCosmicsRegion() - 1] > 1) {
                passList = false;
            }
            if (crossList.size() > 3 && theRegionsCount[thecross.get_SVTCosmicsRegion() - 1] > 2) {
                passList = false;
            }
        }
        return passList;
    }

    /**
     *
     * @param crosses the list of crosses
     * @param geo
     * @return find track guess with svt only
     */
    public CrossList findCosmicsCandidateCrossLists(List<ArrayList<Cross>> crosses,
            SVTGeometry svt_geo, BMTGeometry bmt_geo, int NbSVTRegions) {
        CrossList crossLists = new CrossList();
        
        
        // start finding SVT crosses
        ArrayList<Cross> svt_crosses = crosses.get(0);
        // if there are no svt crosses then return - there is no track
        if (svt_crosses.size() == 0) {
            return null;
        }
        
        Map svtMap = new HashMap<Integer, ArrayList<Cross>>();
        for(Cross c : svt_crosses) {
            if(svtMap.containsKey(c.get_Region())==false) {
                ArrayList<Cross> list = new ArrayList<Cross>();
                list.add(c);
                svtMap.put(c.get_Region(), list);
            } else {
                ArrayList<Cross> list =  (ArrayList<Cross>) svtMap.get(c.get_Region());
                list.add(c);
            }
        }
        
        int L[] = new int[3];
        for(int i = 0; i<3; i++) {
            if(svtMap.containsKey(i+1)==true) {
                L[i]++;
            }
        }
        for(int i = 0; i<3; i++) {
            if(L[i]==0)
                L[i]=1;
        }
        for(int i1 = 0; i1<L[0]; i1++) {
            for(int i2 = 0; i2<L[1]; i2++) {
                for(int i3 = 0; i3<L[2]; i3++) {
                     ArrayList<Cross> list = new ArrayList<Cross>();
                    if(svtMap.containsKey(1)==true) {
                        ArrayList<Cross> list1 = (ArrayList<Cross>) svtMap.get(1);
                        list.add(list1.get(i1));
                    }
                    if(svtMap.containsKey(2)==true) {
                        ArrayList<Cross> list1 = (ArrayList<Cross>) svtMap.get(2);
                        list.add(list1.get(i2));
                    }
                    if(svtMap.containsKey(3)==true) {
                        ArrayList<Cross> list1 = (ArrayList<Cross>) svtMap.get(3);
                        list.add(list1.get(i3));
                    }
                    crossLists.addAll(this.findTrackSeeds(list));
                }
            }
        }

        // instantiate the resulting crosslist
        CrossList crossListFinal = new CrossList();
        ArrayList<ArrayList<Cross>> newCrossLists = new ArrayList<ArrayList<Cross>>();

        // loop over the list of crosslists
        for (int i = 0; i < crossLists.size(); i++) {
            if (crossLists.get(i).size() > 0) {
                ArrayList<Cross> crossList = new ArrayList<Cross>();
                // find the trajectory for each crosslist
                ArrayList<Cross> TrajPoints = get_XYTrajectory(crossLists.get(i), svt_geo, bmt_geo, NbSVTRegions);

                Collections.sort(svt_crosses);
                for (Cross p : TrajPoints) {	 // loop over the trajectory points obtained from the trajectory function
                    // find the closest measuremnt point for each region
                    double doca = 1000;
                    Cross closestCross = null;
                    // find the crosses which are closest to the trajectory obtained from the seed
                    for (Cross c : svt_crosses) {

                        if (c.get_Sector() != p.get_Sector() || c.get_Region() != p.get_Region() || p.get_Detector()!=DetectorType.BST) {
                            continue;
                        }
                        double d = c.get_Point0().distance(p.get_Point0());
                        if (d < doca) {
                            doca = d;
                            closestCross = c; // the closest cross to the trajectory
                        }
                    }
                    if (closestCross != null) // if there is a closest point it has to be within the cuts
                    {
                        if (Math.abs(closestCross.get_Point0().x() - p.get_Point0().x()) < SVTParameters.MAXDISTTOTRAJXY && 
                            Math.abs(closestCross.get_Point0().y() - p.get_Point0().y()) < SVTParameters.MAXDISTTOTRAJXY) {
                            crossList.add(closestCross);

                        }
                    }
                }
                // add the crosslist to index i in the list of crosslists.				
                newCrossLists.add(crossList);
            }
        }

        //crossListFinal.addAll(newCrossLists);
        for (int k = 0; k < newCrossLists.size(); k++) {
            if (newCrossLists.get(k).size() != 0) {
                crossListFinal.add(newCrossLists.get(k));
            }
        }
        return crossListFinal;
    }
    
    
    private List<Double> X = new ArrayList<Double>();
    private List<Double> Y = new ArrayList<Double>();
    private List<Double> errX = new ArrayList<Double>();
    private List<Double> errY = new ArrayList<Double>();

    private ArrayList<Cross> get_XYTrajectory(List<Cross> crosses, SVTGeometry svt_geo,
            BMTGeometry bmt_geo, int NbSVTRegions) {

        ArrayList<Cross> projectedCrosses = new ArrayList<Cross>();

        List<Cross> _hitsOnTrack = crosses;

        LineFitter linefitYX = new LineFitter();
        if (_hitsOnTrack == null) {
            return projectedCrosses;
        }

        X.clear();
        ((ArrayList<Double>) X).ensureCapacity(_hitsOnTrack.size());
        errX.clear();
        ((ArrayList<Double>) errX).ensureCapacity(_hitsOnTrack.size());
        Y.clear();
        ((ArrayList<Double>) Y).ensureCapacity(_hitsOnTrack.size());
        errY.clear();
        ((ArrayList<Double>) errY).ensureCapacity(_hitsOnTrack.size());

        for (int j = 0; j < _hitsOnTrack.size(); j++) {

            X.add(j, _hitsOnTrack.get(j).get_Point().x());
            errX.add(j, _hitsOnTrack.get(j).get_PointErr().x());
            Y.add(j, _hitsOnTrack.get(j).get_Point().y());
            errY.add(j, _hitsOnTrack.get(j).get_PointErr().y());

        }
        // Fit XY Profile first to correct the cross
        boolean linefitresultYX = linefitYX.fitStatus(Y, X, errY, errX, _hitsOnTrack.size());

        //  Get the results of the fits
        if (!linefitresultYX) {
            return projectedCrosses;
        }

        LineFitPars linefitparsYX = linefitYX.getFit();

        double yxslope = linefitparsYX.slope();
        double yxinterc = linefitparsYX.intercept();

        projectedCrosses = this.get_CalcHitsOnTrackXY(yxslope, yxinterc, svt_geo,
                bmt_geo, NbSVTRegions);

        return projectedCrosses;
    }
        
    /**
     *
     * @param yxslope the slope for the line equation x = slope*y + intercept
     * @param yxinterc the intercept for the line equation x = slope*y +
     * intercept
     * @param geo the SVT geometry
     * @return calculated crosses on the trajectory in the xy plane
     */
    private ArrayList<Cross> get_CalcHitsOnTrackXY(double yxslope,
            double yxinterc, SVTGeometry svt_geo, BMTGeometry bmt_geo, int NbSVTRegions) {

        Ray xyTrack = new Ray(yxslope, 0, yxinterc, 0, 0, 0, 0, 0);
        
        ArrayList<Cross> projectedCrosses = new ArrayList<Cross>();
        
        
        // SVT: Layer 1-8:
        for (int ir = 0; ir < NbSVTRegions; ir++) {
            int region = ir+1;
            int layer  = 2*ir+1;
            
            for (int is = 0; is < SVTGeometry.NSECTORS[ir*2]; is++) {
                int sector = is+1;
                
                Plane3D  plane = svt_geo.getPlane(layer, sector);
                Point3D traj = new Point3D();
                if(plane.intersection(xyTrack.toLine(), traj)==1 && svt_geo.isInFiducial(layer, sector, traj)) {
                    Cross cross2D = new Cross(DetectorType.BST, BMTType.UNDEFINED, sector, region, -1); // 2-dimentional cross object corresponding to a point on the trajectory line in the xy plane
                    cross2D.set_Point0(new Point3D(traj.x(), traj.y(), 0));
                    projectedCrosses.add(cross2D);
                }
            }
        }
        //BMTs
        for (int il = 0; il < bmt_geo.getNLayers(); il++) {
            int layer = il+1;
            for(int is=0; is<bmt_geo.getNSectors(); is++) {
                int sector = is+1;
                
                List<Point3D> trajs = new ArrayList<>();
                int nTraj = bmt_geo.getTileSurface(layer, sector).intersection(xyTrack.toLine(), trajs);
                if(nTraj==0) continue;
                
                for(Point3D traj : trajs) {
                    Cross cross2D = new Cross(DetectorType.BMT, BMTGeometry.getDetectorType(layer), sector, bmt_geo.getRegion(layer), -1);
                    cross2D.set_Point0(new Point3D(traj.x(), traj.y(), 0)); 
                    projectedCrosses.add(cross2D); 
                }
            }   
        }
        
//        double[] t = new double[4];
//        for (int ir = 0; ir < 3; ir++) {
//            
//            
//            this.calcBMT2DPoint(yxslope, yxinterc, bmt_geo.getRadiusMidDrift(bmt_geo.getLayer(ir+1, BMTType.C)), t);
//
//            //Cross cross2D1 = new Cross("BMT", BMTType.C, bmt_geo.isInSector((ir + 1) * 2, Math.atan2(t[1], t[0]), Math.toRadians(Constants.isInSectorJitter)), ir + 1, -1);
//            Cross cross2D1 = new Cross(DetectorType.BMT, BMTType.C, bmt_geo.getSector((ir + 1) * 2, Math.atan2(t[1], t[0])), ir + 1, -1);
//            cross2D1.set_Point0(new Point3D(t[0], t[1], 0)); 
//            projectedCrosses.add(cross2D1);
//            System.out.println("C" + ir + " " + cross2D1.get_Point0().toString());
//            if (t[3] != t[1] && t[2] != t[0]) {
//
//                //Cross cross2D2 = new Cross("BMT", BMTType.C, bmt_geo.isInSector((ir + 1) * 2, Math.atan2(t[3], t[2]), Math.toRadians(Constants.isInSectorJitter)), ir + 1, -1);
//                Cross cross2D2 = new Cross(DetectorType.BMT, BMTType.C, bmt_geo.getSector((ir + 1) * 2, Math.atan2(t[3], t[2])), ir + 1, -1);
//                cross2D2.set_Point0(new Point3D(t[2], t[3], 0)); 
//            System.out.println("C" + ir + " " + cross2D2.get_Point0().toString());
//                projectedCrosses.add(cross2D2);
//            }
//            this.calcBMT2DPoint(yxslope,
//                    yxinterc, bmt_geo.getRadiusMidDrift(bmt_geo.getLayer(ir+1, BMTType.Z)), t);
//
//            //Cross cross2D3 = new Cross("BMT", BMTType.Z, bmt_geo.isInSector((ir + 1) * 2, Math.atan2(t[1], t[0]), Math.toRadians(Constants.isInSectorJitter)), ir + 1, -1);
//            Cross cross2D3 = new Cross(DetectorType.BMT, BMTType.Z, bmt_geo.getSector((ir + 1) * 2, Math.atan2(t[1], t[0])), ir + 1, -1);
//            cross2D3.set_Point0(new Point3D(t[0], t[1], 0)); 
//            System.out.println("Z" + ir + " " + cross2D3.get_Point0().toString());
//            projectedCrosses.add(cross2D3);
//            if (t[3] != t[1] && t[2] != t[0]) {
//
//                //Cross cross2D4 = new Cross("BMT", BMTType.Z, bmt_geo.isInSector((ir + 1) * 2, Math.atan2(t[3], t[2]), Math.toRadians(Constants.isInSectorJitter)), ir + 1, -1);
//                Cross cross2D4 = new Cross(DetectorType.BMT, BMTType.Z, bmt_geo.getSector((ir + 1) * 2, Math.atan2(t[3], t[2])), ir + 1, -1);
//                cross2D4.set_Point0(new Point3D(t[2], t[3], 0)); 
//                projectedCrosses.add(cross2D4);
//                            System.out.println("Z" + ir + " " + cross2D4.get_Point0().toString());
//            }
//        }

        return projectedCrosses;
    }

    private void calcBMT2DPoint(double yxslope,
            double yxinterc, double rb, double[] t) {
        t[0] = 0; //first point x
        t[1] = 0; //first point y
        t[2] = 0; //second point x
        t[3] = 0; //second point y

        double Delta = yxslope * yxslope * yxinterc * yxinterc - (yxslope * yxslope + 1) * (yxinterc * yxinterc - rb * rb);
        if (Delta < 0) {
            return;
        }

        t[1] = (-yxslope * yxinterc + Math.sqrt(Delta)) / (yxslope * yxslope + 1);
        t[3] = (-yxslope * yxinterc - Math.sqrt(Delta)) / (yxslope * yxslope + 1);
        t[0] = yxslope * t[1] + yxinterc;
        t[2] = yxslope * t[3] + yxinterc;
    }

}
