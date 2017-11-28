package org.jlab.rec.cvt.cross;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.bmt.Constants;
import org.jlab.rec.cvt.fit.LineFitPars;
import org.jlab.rec.cvt.fit.LineFitter;

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
            org.jlab.rec.cvt.svt.Geometry svt_geo,
            org.jlab.rec.cvt.bmt.Geometry bmt_geo, int NbSVTRegions) {
        // start finding svt crosses
        ArrayList<Cross> svt_crosses = crosses.get(0);
        // if there are no svt crosses then return - there is no track
        if (svt_crosses.size() == 0) {
            return null;
        }

        // first look for seeds		
        CrossList crossLists = this.findTrackSeeds(svt_crosses);

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

                        if (c.get_Sector() != p.get_Sector() || c.get_Region() != p.get_Region()) {
                            continue;
                        }
                        double d = Math.sqrt((c.get_Point0().x() - p.get_Point0().x()) * (c.get_Point0().x() - p.get_Point0().x())
                                + (c.get_Point0().y() - p.get_Point0().y()) * (c.get_Point0().y() - p.get_Point0().y()));
                        if (d < doca) {
                            doca = d;
                            closestCross = c; // the closest cross to the trajectory
                        }
                    }
                    if (closestCross != null) // if there is a closest point it has to be within the cuts
                    {
                        if (Math.abs(closestCross.get_Point0().x() - p.get_Point0().x()) < org.jlab.rec.cvt.svt.Constants.MAXDISTTOTRAJXY && Math.abs(closestCross.get_Point0().y() - p.get_Point0().y()) < org.jlab.rec.cvt.svt.Constants.MAXDISTTOTRAJXY) {
                            crossList.add(closestCross);

                        }
                    }
                    /*
					doca = 1000;					
					closestCross = null;					
					// find the crosses which are closest to the trajectory obtained from the seed
					for(Cross c : bmt_crosses) {				
								
						if(c.get_Sector()!= p.get_Sector() || c.get_Region()!= p.get_Region() || !c.get_DetectorType().equalsIgnoreCase(p.get_DetectorType()))
							continue;
						double d = Math.sqrt((c.get_Point().x()-p.get_Point0().x())*(c.get_Point().x()-p.get_Point0().x())+
								(c.get_Point().y()-p.get_Point0().y())*(c.get_Point().y()-p.get_Point0().y()));
						if(d<doca) { 
							doca = d;
							closestCross=c; // the closest cross to the trajectory
						}
					}
					if(closestCross!=null) // if there is a closest point it has to be within the cuts
						if(Math.abs(closestCross.get_Point().x()-p.get_Point0().x())<org.jlab.rec.cvt.svt.Constants.MAXDISTTOTRAJXY && Math.abs(closestCross.get_Point().y()-p.get_Point0().y())<org.jlab.rec.cvt.svt.Constants.MAXDISTTOTRAJXY) { 
							crossList.add(closestCross);
						}
                     */
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

    private ArrayList<Cross> get_XYTrajectory(List<Cross> crosses, org.jlab.rec.cvt.svt.Geometry svt_geo,
            org.jlab.rec.cvt.bmt.Geometry bmt_geo, int NbSVTRegions) {

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
            double yxinterc, org.jlab.rec.cvt.svt.Geometry svt_geo,
            org.jlab.rec.cvt.bmt.Geometry bmt_geo, int NbSVTRegions) {

        ArrayList<Cross> projectedCrosses = new ArrayList<Cross>();
        //Layer 1-8:
        for (int l = 0; l < 2 * NbSVTRegions; l++) {
            if (l % 2 != 0) {
                continue;
            }

            for (int s = 0; s < org.jlab.rec.cvt.svt.Constants.NSECT[l]; s++) {

                double epsilon = 1e-6;
                Vector3D n = svt_geo.findBSTPlaneNormal(s + 1, l + 1);

                double dot = (n.x() * yxslope + n.y());

                if (Math.abs(dot) < epsilon) {
                    continue;
                }
                double R = (org.jlab.rec.cvt.svt.Constants.MODULERADIUS[l][0] + org.jlab.rec.cvt.svt.Constants.MODULERADIUS[l + 1][0]) / 2.;

                Vector3D w = new Vector3D(yxinterc - R * n.x(), -R * n.y(), 0);

                double fac = -(n.x() * w.x() + n.y() * w.y() + n.z() * w.z()) / dot;

                Vector3D Delt = new Vector3D(fac * yxslope + yxinterc - R * n.x(), fac - R * n.y(), 0);

                if (Delt.mag() < org.jlab.rec.cvt.svt.Constants.ACTIVESENWIDTH / 2) {
                    double tX = fac * yxslope + yxinterc;
                    double tY = fac;
                    Cross cross2D = new Cross("SVT", "", s + 1, (int) (l + 2) / 2, -1); // 2-dimentional cross object corresponding to a point on the trajectory line in the xy plane
                    cross2D.set_Point0(new Point3D(tX, tY, 0));
                    projectedCrosses.add(cross2D);
                }
            }
        }
        //BMTs

        double[] t = new double[4];
        for (int r = 0; r < 3; r++) {

            this.calcBMT2DPoint(yxslope,
                    yxinterc, org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[r] + org.jlab.rec.cvt.bmt.Constants.hDrift, t);

            Cross cross2D1 = new Cross("BMT", "C", bmt_geo.isInSector((r + 1) * 2, Math.atan2(t[1], t[0]), Math.toRadians(Constants.isInSectorJitter)), r + 1, -1);
            cross2D1.set_Point0(new Point3D(t[0], t[1], 0));
            projectedCrosses.add(cross2D1);
            if (t[3] != t[1] && t[2] != t[0]) {

                Cross cross2D2 = new Cross("BMT", "C", bmt_geo.isInSector((r + 1) * 2, Math.atan2(t[3], t[2]), Math.toRadians(Constants.isInSectorJitter)), r + 1, -1);
                cross2D2.set_Point0(new Point3D(t[2], t[3], 0));
                projectedCrosses.add(cross2D2);
            }
            this.calcBMT2DPoint(yxslope,
                    yxinterc, org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[r] + org.jlab.rec.cvt.bmt.Constants.hDrift, t);

            Cross cross2D3 = new Cross("BMT", "Z", bmt_geo.isInSector((r + 1) * 2, Math.atan2(t[1], t[0]), Math.toRadians(Constants.isInSectorJitter)), r + 1, -1);
            cross2D3.set_Point0(new Point3D(t[0], t[1], 0));
            projectedCrosses.add(cross2D3);
            if (t[3] != t[1] && t[2] != t[0]) {

                Cross cross2D4 = new Cross("BMT", "Z", bmt_geo.isInSector((r + 1) * 2, Math.atan2(t[3], t[2]), Math.toRadians(Constants.isInSectorJitter)), r + 1, -1);
                cross2D4.set_Point0(new Point3D(t[2], t[3], 0));
                projectedCrosses.add(cross2D4);
            }
        }

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
