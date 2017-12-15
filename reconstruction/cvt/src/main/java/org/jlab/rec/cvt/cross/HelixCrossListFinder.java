package org.jlab.rec.cvt.cross;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.svt.Constants;

/**
 * A class with methods used to find lists of crosses. This is the Pattern
 * Recognition step used in track seeding, to find the points that are
 * consistent with belonging to the same track. This step precedes the initial
 * estimates of the track parameters.
 *
 * @author ziegler
 *
 */
public class HelixCrossListFinder {

    public HelixCrossListFinder() { 
    }

    /**
     *
     * @param crosses the list of crosses in the event
     * @return the list of crosses determined to be consistent with belonging to
     * a track in the svt
     */
    public CrossList findCandidateCrossLists(List<ArrayList<Cross>> crosses) {  
        // instantiate the crosslist
        CrossList crossList = new CrossList();
        // the list of arraylists of crosses consistent with a track
        ArrayList<ArrayList<Cross>> trkCnds = new ArrayList<ArrayList<Cross>>();
        // the svt crosses
        ArrayList<Cross> svt_crosses = crosses.get(0);
        // the bmt crosses
        ArrayList<Cross> bmt_crosses = crosses.get(1);
        // require crosses to be found in the svt
        if (svt_crosses.size() == 0) {
            return null;
        }
        // sort the crosses by region and phi
        Collections.sort(svt_crosses);
        //create arrays of crosses for each region
        ArrayList<ArrayList<Cross>> theListsByRegion = new ArrayList<ArrayList<Cross>>();
        // add the crosses to their respective region indexes = region -1
        ArrayList<Cross> theRegionList = new ArrayList<Cross>();
        if (svt_crosses.size() > 0) {
            theRegionList.add(svt_crosses.get(0)); // init
        }	  	// fill arrays
        for (int i = 1; i < svt_crosses.size(); i++) {
            Cross c = svt_crosses.get(i);
            if (svt_crosses.get(i - 1).get_Region() != c.get_Region()) {
                theListsByRegion.add(theRegionList);    // end previous list by region
                theRegionList = new ArrayList<Cross>(); // new region list
            }
            theRegionList.add(c);
        }
        theListsByRegion.add(theRegionList);

        int[][] C = new int[][]{
            {1, 2, 3}, {1, 2, 4}, {1, 3, 4}, {2, 3, 4}
        }; // combinatorial array

        // loop over the regions and find cross combinatorial combinations of a minimum of 3 crosses out of 4
        if (theListsByRegion.size() == 4) {
            for (int i1 = 0; i1 < theListsByRegion.get(0).size(); i1++) {
                for (int i2 = 0; i2 < theListsByRegion.get(1).size(); i2++) {
                    for (int i3 = 0; i3 < theListsByRegion.get(2).size(); i3++) {
                        for (int i4 = 0; i4 < theListsByRegion.get(3).size(); i4++) {
                            // look first for a track seed with 4 crosses
                            Seed trkCand
                                    = this.findCandUsingFourCrosses(theListsByRegion.get(0).get(i1),
                                            theListsByRegion.get(1).get(i2), theListsByRegion.get(2).get(i3), theListsByRegion.get(3).get(i4));
                            if (trkCand != null) {
                                // if a seed has been found look for a match with the micromegas crosses
                                if (bmt_crosses.size() > 0) {
                                    ArrayList<Seed> trkCands = this.findCandUsingMicroMegas(trkCand, bmt_crosses);
                                    trkCnds.addAll(trkCands);
                                } else { // no bmt
                                    trkCnds.add(trkCand);
                                }
                            } else {
                                // if the seed-finder fails with 4 crosses, look for 3 out of 4 using possible combinatorial combinations
                                for (int l = 1; l < C.length; l++) {
                                    int l1, l2, l3;
                                    int r1 = C[l][0];
                                    int r2 = C[l][1];
                                    int r3 = C[l][2];
                                    l1 = this.match(r1, i1, i2, i3, i4); // get the correct combinatorial value by matching the region from C for the 1st cross in the sorted array to the value in the loop
                                    l2 = this.match(r2, i1, i2, i3, i4); // get the correct combinatorial value by matching the region from C for the 2nd cross in the sorted array to the value in the loop
                                    l3 = this.match(r3, i1, i2, i3, i4); // get the correct combinatorial value by matching the region from C for the 3rd cross in the sorted array to the value in the loop

                                    trkCand = this.findCandUsingThreeCrosses(theListsByRegion.get(r1 - 1).get(l1),
                                            theListsByRegion.get(r2 - 1).get(l2), theListsByRegion.get(r3 - 1).get(l3));

                                    if (trkCand != null && crossList.ContainsNot(trkCnds, trkCand)) {
                                        if (trkCand.size() < 3) {
                                            continue;
                                        }

                                        if (bmt_crosses.size() > 0) {
                                            // if the seed is found, match to micromegas				  							
                                            ArrayList<Seed> trkCands = this.findCandUsingMicroMegas(trkCand, bmt_crosses);
                                            trkCnds.addAll(trkCands);
                                        } else { // no bmt
                                            trkCnds.add(trkCand);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // repeat above procedure is there is only 3 crosses available
        if (theListsByRegion.size() == 3) {

            for (int i1 = 0; i1 < theListsByRegion.get(0).size(); i1++) {
                for (int i2 = 0; i2 < theListsByRegion.get(1).size(); i2++) {
                    for (int i3 = 0; i3 < theListsByRegion.get(2).size(); i3++) {
                        Seed trkCand
                                = this.findCandUsingThreeCrosses(theListsByRegion.get(0).get(i1), theListsByRegion.get(1).get(i2), theListsByRegion.get(2).get(i3));

                        if (trkCand != null) {
                            if (bmt_crosses.size() > 0) {
                                // if the seed is found, match to micromegas
                                ArrayList<Seed> trkCands = this.findCandUsingMicroMegas(trkCand, bmt_crosses);
                                trkCnds.addAll(trkCands);
                            } else {
                                trkCnds.add(trkCand);
                            }
                        }
                    }
                }
            }
        }

        // add the seed 
        crossList.addAll(trkCnds);
        return crossList;

    }

    private int match(int r1, int i1, int i2, int i3, int i4) {
        int l1 = -1;
        if (r1 == 1) {
            l1 = i1;
        }
        if (r1 == 2) {
            l1 = i2;
        }
        if (r1 == 3) {
            l1 = i3;
        }
        if (r1 == 4) {
            l1 = i4;
        }
        return l1;
    }

    /**
     *
     * @param trkCand the track seed
     * @param bmt_cross BMT cross
     * @return
     */
    private ArrayList<Seed> findCandUsingMicroMegas(Seed trkCand,
            List<Cross> bmt_crosses) {

        ArrayList<Seed> trkCands = new ArrayList<Seed>();
        ArrayList<Cross> BMTCcrosses = new ArrayList<Cross>();
        ArrayList<Cross> BMTZcrosses = new ArrayList<Cross>();
        for (Cross bmt_cross : bmt_crosses) {

            if (!(Double.isNaN(bmt_cross.get_Point().z()))) // C-detector
            {
                BMTCcrosses.add(bmt_cross);
            }
            if (!(Double.isNaN(bmt_cross.get_Point().x()))) // Z-detector
            {
                BMTZcrosses.add(bmt_cross);
            }
        }

        List<Seed> AllSeeds = new ArrayList<Seed>();
        if (BMTCcrosses.size() > 0) {
            for (Cross bmt_Ccross : BMTCcrosses) { // C-detector   		

                if (this.passCcross(trkCand, bmt_Ccross)) {
                    Seed BMTTrkSeed = new Seed(trkCand.avg_tandip, trkCand.delta_phi, trkCand.radius);
                    BMTTrkSeed.addAll(trkCand);
                    BMTTrkSeed.add(bmt_Ccross);
                    AllSeeds.add(BMTTrkSeed);
                }
            }
        }
        if (AllSeeds.size() == 0) { // no C-match
            Seed BMTTrkSeed = new Seed(trkCand.avg_tandip, trkCand.delta_phi, trkCand.radius);
            BMTTrkSeed.addAll(trkCand);
            AllSeeds.add(BMTTrkSeed);
        }
        if (BMTZcrosses.size() > 0) {
            for (int h = 0; h < AllSeeds.size(); h++) {
                for (Cross bmt_Zcross : BMTZcrosses) { // Z-detector   			    		
                    if (this.passZcross(trkCand, bmt_Zcross)) {
                        Seed BMTTrkSeed = new Seed(trkCand.avg_tandip, trkCand.delta_phi, trkCand.radius);
                        BMTTrkSeed.addAll(AllSeeds.get(h));
                        BMTTrkSeed.add(bmt_Zcross);
                        trkCands.add(BMTTrkSeed);
                    }
                }
            }
        }

        return trkCands;
    }

    private boolean passCcross(Seed trkCand, Cross bmt_Ccross) {

        boolean pass = false;

        double dzdrsum = trkCand.avg_tandip * trkCand.size();
        double ave_seed_rad = 0;

        for (int i = 0; i < trkCand.radius.length; i++) {
            ave_seed_rad += trkCand.radius[i];
        }
        ave_seed_rad = ave_seed_rad / (double) trkCand.radius.length;

        double z_bmt = bmt_Ccross.get_Point().z();
        double r_bmt = org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[bmt_Ccross.get_Region() - 1];
        double dzdr_bmt = z_bmt / r_bmt;
        if (Math.abs(1 - (dzdrsum / (double) (trkCand.size())) / ((dzdrsum + dzdr_bmt) / (double) (trkCand.size() + 1))) <= Constants.dzdrcut) // add this to the track
        {
            pass = true;
        }

        return pass;
    }

    private boolean passZcross(Seed trkCand, Cross bmt_Zcross) {

        double ave_seed_rad = 0;

        for (int i = 0; i < trkCand.radius.length; i++) {
            ave_seed_rad += trkCand.radius[i];
        }
        ave_seed_rad = ave_seed_rad / (double) trkCand.radius.length;

        double x_bmt = bmt_Zcross.get_Point().x();
        double y_bmt = bmt_Zcross.get_Point().y();
        boolean pass = true;
        for (int i = 0; i < trkCand.size() - 2; i++) {
            double rad_withBmt = calc_radOfCurv(trkCand.get(i).get_Point().x(), trkCand.get(i + 1).get_Point().x(), x_bmt,
                    trkCand.get(i).get_Point().y(), trkCand.get(i + 1).get_Point().y(), y_bmt);

            if (rad_withBmt < Constants.radcut || Math.abs((rad_withBmt - ave_seed_rad) / ave_seed_rad) > 0.3) // more than 30% different
            {
                pass = false;
            }
        }
        return pass;
    }

    private Seed findCandUsingFourCrosses(Cross c1, Cross c2, Cross c3, Cross c4) {

        // selection for first 3 hits:	
        Seed ct3 = findCandUsingThreeCrosses(c1, c2, c3);

        if (ct3 == null) {
            return null;
        }

        if (ct3.size() > 2) {
            Seed ct = linkToFourthRegion(ct3, c4);
            return ct;
        } else {
            return null;
        }

    }

    /**
     *
     * @param trkCndsR123 the track seed using 3 regions from the SVT
     * @param c4 the cross in the fourth region
     * @return a track seed
     */
    private Seed linkToFourthRegion(Seed trkCndsR123,
            Cross c4) {

        if (trkCndsR123 == null) {
            return null;
        }

        // using the existing cross list with 3 crosses try to see if 4 fourth one can be linked
        ArrayList<Cross> ct = new ArrayList<Cross>();

        Cross c1 = trkCndsR123.get(0);
        Cross c2 = trkCndsR123.get(1);
        Cross c3 = trkCndsR123.get(2);

        //check the fourth cross using the cut-based algorithm
        double phi14 = Math.abs(get_PointInfo(c1, c4, null)[2]);
        double rad234 = get_PointInfo(c2, c3, c4)[3];
        double dzdr4 = get_PointInfo(c4, null, null)[1] / get_PointInfo(c4, null, null)[0];

        double dzdrsum = trkCndsR123.avg_tandip * 3 + dzdr4;

        if (phi14 <= Constants.phi14cut && Math.abs(rad234) >= Constants.radcut
                && Math.abs((dzdr4 - dzdrsum / 4.) / (dzdrsum / 4.)) <= Constants.dzdrcut) { //passes the cuts
            ct.add(c1);
            ct.add(c2);
            ct.add(c3);
            ct.add(c4);

            double seed_avg_tandip = dzdrsum / 4.;
            double[] seed_delta_phi = {trkCndsR123.delta_phi[0], trkCndsR123.delta_phi[1], phi14};
            double[] seed_radius = {trkCndsR123.radius[0], rad234};
            // create a new seed.
            Seed seed = new Seed(seed_avg_tandip, seed_delta_phi, seed_radius);
            seed.addAll(ct);

            return seed;
        } else {
            return trkCndsR123;
        }

    }

    /**
     * for 3 SVT region sorted in increasing order R1 > R2 > R3
     *
     * @param c1 cross in R1
     * @param c2 cross in R2
     * @param c3 cross in R3
     * @return a track seed with 3 crosses
     */
    private Seed findCandUsingThreeCrosses(Cross c1,
            Cross c2, Cross c3) {

        ArrayList<Cross> ct = new ArrayList<Cross>();
        // selection for first 3 hits:	
        //---------------------------	
        double phi12 = Math.abs(get_PointInfo(c1, c2, null)[2]);
        if (phi12 > Constants.phi12cut) {
            return null;
        }
        double phi13 = Math.abs(get_PointInfo(c1, c3, null)[2]);
        if (phi13 > Constants.phi13cut) {
            return null;
        }
        double rad123 = get_PointInfo(c1, c2, c3)[3];
        if (Math.abs(rad123) < Constants.radcut) {
            return null;
        }
        // get the tandip values				 
        double dzdr1 = get_PointInfo(c1, null, null)[1] / get_PointInfo(c1, null, null)[0];
        double dzdr2 = get_PointInfo(c2, null, null)[1] / get_PointInfo(c2, null, null)[0];
        double dzdr3 = get_PointInfo(c3, null, null)[1] / get_PointInfo(c3, null, null)[0];

        double dzdrsum = dzdr1 + dzdr2 + dzdr3;
        // impose selection criteria
        if ((Math.abs((dzdr1 - dzdrsum / 3.) / (dzdrsum / 3.)) > Constants.dzdrcut)
                || (Math.abs((dzdr2 - dzdrsum / 3.) / (dzdrsum / 3.)) > Constants.dzdrcut)
                || (Math.abs((dzdr3 - dzdrsum / 3.) / (dzdrsum / 3.)) > Constants.dzdrcut)) {
            return null;
        }

        ct.add(c1);
        ct.add(c2);
        ct.add(c3);

        double seed_avg_tandip = dzdrsum / 3.;
        double[] seed_delta_phi = {phi12, phi13};
        double[] seed_radius = {rad123};
        // create the seed
        Seed seed = new Seed(seed_avg_tandip, seed_delta_phi, seed_radius);
        seed.addAll(ct);

        return seed;
    }

    /**
     * get the Point selection parameters, rho (sqrt(x^2+y^2), z, radius of
     * curvature using 3 points
     *
     * @param bt Barrel Tracker 1st cross
     * @param bt2 Barrel Tracker 2nd cross
     * @param bt3 Barrel Tracker 3rd cross
     * @return params for selection
     */
    private double[] get_PointInfo(Cross bt, Cross bt2, Cross bt3) {

        double[] arrayInfo = new double[4];
        if (bt == null) {
            return null;
        }

        Point3D btp1 = null;
        Point3D btp2 = null;
        Point3D btp3 = null;

        if (bt != null) {
            btp1 = bt.get_Point();
        }
        if (bt2 != null) {
            btp2 = bt2.get_Point();
        }
        if (bt3 != null) {
            btp3 = bt3.get_Point();
        }

        arrayInfo[1] = btp1.z();
        arrayInfo[0] = Math.sqrt(btp1.x() * btp1.x() + btp1.y() * btp1.y());

        double cos_ZDiff = -1;
        if (bt2 != null) {
            Vector3D btu1 = btp1.toVector3D().asUnit();
            Vector3D btu2 = btp2.toVector3D().asUnit();
            cos_ZDiff = btu1.dot(btu2);
        }
        double radiusOfCurv = 0;
        if (btp3 != null) {
            if (Math.abs(btp2.x() - btp1.x()) > 1.0e-9 && Math.abs(btp3.x() - btp2.x()) > 1.0e-9) {
                // Find the intersection of the lines joining the innermost to middle and middle to outermost point
                double ma = (btp2.y() - btp1.y()) / (btp2.x() - btp1.x());
                double mb = (btp3.y() - btp2.y()) / (btp3.x() - btp2.x());

                if (Math.abs(mb - ma) > 1.0e-9) {
                    double xcen = 0.5 * (ma * mb * (btp1.y() - btp3.y()) + mb * (btp1.x() + btp2.x()) - ma * (btp2.x() + btp3.x())) / (mb - ma);
                    double ycen = (-1. / mb) * (xcen - 0.5 * (btp2.x() + btp3.x())) + 0.5 * (btp2.y() + btp3.y());

                    radiusOfCurv = Math.sqrt(Math.pow((btp1.x() - xcen), 2) + Math.pow((btp1.y() - ycen), 2));
                }
            }
        }

        // the opening angle between crosses 1 and 2 wrt the origin in degrees 
        arrayInfo[2] = Math.toDegrees(Math.acos(cos_ZDiff));
        // the radius of the circle calculated from 3 crosses
        arrayInfo[3] = radiusOfCurv;

        return arrayInfo;

    }

    /**
     *
     * @param x1 cross1 x-coordinate
     * @param x2 cross2 x-coordinate
     * @param x3 cross3 x-coordinate
     * @param y1 cross1 y-coordinate
     * @param y2 cross2 y-coordinate
     * @param y3 cross3 y-coordinate
     * @return radius of circle containing 3 crosses in the (x,y) plane
     */
    private double calc_radOfCurv(double x1, double x2, double x3, double y1, double y2, double y3) {
        double radiusOfCurv = 0;

        if (Math.abs(x2 - x1) > 1.0e-9 && Math.abs(x3 - x2) > 1.0e-9) {
            // Find the intersection of the lines joining the innermost to middle and middle to outermost point
            double ma = (y2 - y1) / (x2 - x1);
            double mb = (y3 - y2) / (x3 - x2);

            if (Math.abs(mb - ma) > 1.0e-9) {
                double xcen = 0.5 * (ma * mb * (y1 - y3) + mb * (x1 + x2) - ma * (x2 + x3)) / (mb - ma);
                double ycen = (-1. / mb) * (xcen - 0.5 * (x2 + x3)) + 0.5 * (y2 + y3);

                radiusOfCurv = Math.sqrt((x1 - xcen) * (x1 - xcen) + (y1 - ycen) * (y1 - ycen));
            }
        }
        return radiusOfCurv;

    }

    /**
     * A class representing the seed object. The seed of a track is the initial
     * guess of the track and contains the crosses that belong to it
     *
     * @author ziegler
     *
     */
    private class Seed extends ArrayList<Cross> {

        private static final long serialVersionUID = 1L;

        final double avg_tandip;		// the dip angle in rho z direction
        final double[] delta_phi;		// opening angle between cross 1 and 2 positions wrt the origin
        final double[] radius;          // the radius of the circle of the seed calculated using 3 crosses belonging to the seed

        /**
         * The constructor of the seed
         *
         * @param avg_tandip
         * @param delta_phi
         * @param radius
         */
        Seed(double avg_tandip, double[] delta_phi, double[] radius) {
            this.avg_tandip = avg_tandip;
            this.delta_phi = delta_phi;
            this.radius = radius;
        }

    }

}
