package org.jlab.rec.cvt.cross;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jlab.clas.swimtools.Swim;
import org.jlab.geom.prim.Point3D;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.svt.Constants;
import org.jlab.rec.cvt.svt.Geometry;
import org.jlab.rec.cvt.track.Track;
import org.jlab.rec.cvt.track.TrackSeeder;

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

    // combinatorials
    int[][] C5 = new int[][]{
            {2,3,4,5,6}
    };
    int[][] C4 = new int[][]{
        {3,4,5,6}, 
        {2,4,5,6},
        {2,3,5,6},
        {2,3,4,6},
        {2,3,4,5}
    };

    int[][] C3 = new int[][]{
        {4,5,6}, {3,5,6}, {3,4,6}, {3,4,5},
        {4,5,6}, {2,5,6}, {2,4,6}, {2,4,5},
        {3,5,6}, {2,5,6}, {2,3,6}, {2,3,5},
        {3,4,6}, {2,4,6}, {2,3,6}, {2,3,4},
        {3,4,5}, {2,4,5}, {2,3,5}, {2,3,4}
    };
    
    /**
     *
     * @param crosses the list of crosses in the event
     * @return the list of crosses determined to be consistent with belonging to
     * a track in the cvt
     */
    public List<org.jlab.rec.cvt.track.Seed> findCandidateCrossLists(List<ArrayList<Cross>> cvt_crosses, 
            org.jlab.rec.cvt.svt.Geometry svt_geo, Swim swimmer) {  
        // instantiate the crosslist
        //List<Seed> seedList = new ArrayList<Seed>();
        
        // require crosses to be found in the svt
        if (cvt_crosses.size() == 0) {
            return null;
        }
        
        List<org.jlab.rec.cvt.track.Seed> seedList = new ArrayList<org.jlab.rec.cvt.track.Seed>();
        
        //create arrays of crosses for each region
        ArrayList<ArrayList<Cross>> theListsByRegion = new ArrayList<ArrayList<Cross>>();
        ArrayList<ArrayList<Cross>> theListsByRegionBMTC = new ArrayList<ArrayList<Cross>>();
        /////
        for(int r = 0; r<6; r++) {
            theListsByRegion.add(new ArrayList<Cross>());
            if(r<3)
                theListsByRegionBMTC.add(new ArrayList<Cross>());
        }
        ArrayList<Cross> allCrossList = new ArrayList<Cross>();
        if (cvt_crosses.size() > 0) {
            // sort the crosses by region and phi
            if(cvt_crosses.get(0).size() > 0 ) {
                Collections.sort(cvt_crosses.get(0));
                allCrossList.addAll(cvt_crosses.get(0)); // init
            }
            if(cvt_crosses.get(1).size() > 0 ) {
                //Collections.sort(cvt_crosses.get(1));
                allCrossList.addAll(cvt_crosses.get(1)); // init
            }
        }
        for(Cross c : allCrossList){
            //System.out.println(" CROSSLISTER "+c.printInfo());
            if(c.get_Detector().equalsIgnoreCase("SVT"))
                theListsByRegion.get(c.get_Region()-1).add(c);
            if(c.get_Detector().equalsIgnoreCase("BMT") && c.get_DetectorType().equalsIgnoreCase("Z"))
                theListsByRegion.get(c.get_Region()+2).add(c);
            if(c.get_Detector().equalsIgnoreCase("BMT") && c.get_DetectorType().equalsIgnoreCase("C"))
                theListsByRegionBMTC.get(c.get_Region()-1).add(c);
        }
        // 
        for(int r =0; r<6; r++) {
            if(theListsByRegion.get(r).size()==0)
                theListsByRegion.get(r).add(null);
        }
        for(int r =0; r<3; r++) {
            if(theListsByRegionBMTC.get(r).size()==0)
                theListsByRegionBMTC.get(r).add(null);
        }
        List<Seed> CirTrks = new ArrayList<Seed>();
        for (int i2 = 0; i2 < theListsByRegion.get(1).size(); i2++) {
            for (int i3 = 0; i3 < theListsByRegion.get(2).size(); i3++) {
                for (int i4 = 0; i4 < theListsByRegion.get(3).size(); i4++) {
                    for (int i5 = 0; i5 < theListsByRegion.get(4).size(); i5++) {
                        for (int i6 = 0; i6 < theListsByRegion.get(5).size(); i6++) {
                            Seed cand ; 
                            //System.out.println("-----");
                            //if(theListsByRegion.get(1).get(i2)!=null) System.out.println(" 5-l "+theListsByRegion.get(1).get(i2).printInfo());
                            //if(theListsByRegion.get(2).get(i3)!=null) System.out.println(" 5-l "+theListsByRegion.get(2).get(i3).printInfo());
                            //if(theListsByRegion.get(3).get(i4)!=null) System.out.println(" 5-l "+theListsByRegion.get(3).get(i4).printInfo());
                            //if(theListsByRegion.get(4).get(i5)!=null) System.out.println(" 5-l "+theListsByRegion.get(4).get(i5).printInfo());
                            //if(theListsByRegion.get(5).get(i6)!=null) System.out.println(" 5-l "+theListsByRegion.get(5).get(i6).printInfo());
                            cand = this.isTrack5(
                                    theListsByRegion.get(1).get(i2), 
                                    theListsByRegion.get(2).get(i3), 
                                    theListsByRegion.get(3).get(i4), 
                                    theListsByRegion.get(4).get(i5),
                                    theListsByRegion.get(5).get(i6));
                            if(cand!=null && this.ContainsSeed(CirTrks, cand)==false) {
                                CirTrks.add(cand);
                                continue;
                            } else {
                                for (int l = 0; l < C4.length; l++) { 
                              //      System.out.println("0) TRAC 4 regions "+C4[l][0]+" cnt "+this.match(C4[l][0], i2, i3, i4, i5, i6));
                              //      System.out.println("1) TRAC 4 regions "+C4[l][1]+" cnt "+this.match(C4[l][1], i2, i3, i4, i5, i6));
                              //      System.out.println("2) TRAC 4 regions "+C4[l][2]+" cnt "+this.match(C4[l][2], i2, i3, i4, i5, i6));
                              //      System.out.println("3) TRAC 4 regions "+C4[l][3]+" cnt "+this.match(C4[l][3], i2, i3, i4, i5, i6));
                                    cand = this.isTrack4(
                                        theListsByRegion.get(C4[l][0] - 1).get(this.match(C4[l][0], i2, i3, i4, i5, i6)),
                                        theListsByRegion.get(C4[l][1] - 1).get(this.match(C4[l][1], i2, i3, i4, i5, i6)), 
                                        theListsByRegion.get(C4[l][2] - 1).get(this.match(C4[l][2], i2, i3, i4, i5, i6)),
                                        theListsByRegion.get(C4[l][3] - 1).get(this.match(C4[l][3], i2, i3, i4, i5, i6)));

                                    if(cand!=null && this.ContainsSeed(CirTrks, cand)==false) {
                                        CirTrks.add(cand); 
                                        continue;
                                    } else {
                                        for(int ll = l*4; ll<((l+1)*4-1); ll++) {
                                            cand = this.isTrack3(
                                                theListsByRegion.get(C3[ll][0] - 1).get(this.match(C3[ll][0], i2, i3, i4, i5, i6)),
                                                theListsByRegion.get(C3[ll][1] - 1).get(this.match(C3[ll][1], i2, i3, i4, i5, i6)), 
                                                theListsByRegion.get(C3[ll][2] - 1).get(this.match(C3[ll][2], i2, i3, i4, i5, i6)));
                                            if(cand!=null && this.ContainsSeed(CirTrks, cand)==false) {
                                                CirTrks.add(cand); 
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
         
        for(Seed s : CirTrks) { 
            if(s==null)
                continue;
            
            
            //this.MatchBMTC(s, theListsByRegionBMTC.get(0), svt_geo); // match the seed to each BMT region
            //this.MatchBMTC(s, theListsByRegionBMTC.get(1), svt_geo); // match the seed to each BMT region
            //this.MatchBMTC(s, theListsByRegionBMTC.get(2), svt_geo); // match the seed to each BMT region
           
            Track trk = s.seedFit.fitSeed(s, svt_geo, 3, true, swimmer);
            if(trk==null)
                continue;
            //match to r1
            MatchToRegion1( s, theListsByRegion.get(0), svt_geo, swimmer); 
            org.jlab.rec.cvt.track.Seed trkSeed = new org.jlab.rec.cvt.track.Seed();
            
            trkSeed.set_Crosses(s);
            List<Cluster> clusters = new ArrayList<Cluster>();
            System.out.println(" seed crosses ");
            for(Cross c : s ) { System.out.println(" --> "+c.printInfo());
                if(c.get_Detector().equalsIgnoreCase("SVT")) {
                    c.get_Cluster1().set_CentroidError(this.calcCentErr(c, c.get_Cluster1(), svt_geo));
                    c.get_Cluster2().set_CentroidError(this.calcCentErr(c, c.get_Cluster2(), svt_geo));
                    
                    clusters.add(c.get_Cluster1());
                    clusters.add(c.get_Cluster2());
                }
                if(c.get_Detector().equalsIgnoreCase("BMT")) {
                    clusters.add(c.get_Cluster1());
                    
                }
            }
            
            trkSeed.set_Clusters(clusters);
           
            trkSeed.set_Helix(trk.get_helix());
            seedList.add(trkSeed);
        }
       
        return seedList;

    }

    private int match(int r, int i2, int i3, int i4, int i5, int i6) {
        int hitIdx = -1;
        
        if (r == 2) {
            hitIdx = i2;
        }
        if (r == 3) {
            hitIdx = i3;
        }
        if (r == 4) {
            hitIdx = i4;
        }
        if (r == 5) {
            hitIdx = i5;
        }
        if (r == 6) {
            hitIdx = i6;
        }
        return hitIdx;
    }

    

    private boolean passCcross(Seed trkCand, Cross bmt_Ccross) {
        if(trkCand==null)
            return false;
        boolean pass = false;
        if(bmt_Ccross==null)
            return false;
        double avg_tandip =0;
        int countCrosses =0;
        for(Cross c : trkCand) {
            if(c.get_Detector().equalsIgnoreCase("SVT")) {
                countCrosses++;
                avg_tandip+=c.get_Point().z()/Math.sqrt(c.get_Point().x()*c.get_Point().x()+c.get_Point().y()*c.get_Point().y());
            }
            if(countCrosses>0)
                avg_tandip/=countCrosses;
        }
        double dzdrsum = avg_tandip*countCrosses;
        double z_bmt = bmt_Ccross.get_Point().z();
        double r_bmt = org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[bmt_Ccross.get_Region() - 1];
        double dzdr_bmt = z_bmt / r_bmt;
        if (Math.abs(1 - (dzdrsum / (double) (countCrosses)) / ((dzdrsum + dzdr_bmt) / (double) (countCrosses + 1))) <= Constants.dzdrcut) // add this to the track
            pass = true;
        
        return pass;
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

    

    private Seed isTrack5(Cross c1, Cross c2, Cross c3, Cross c4, Cross c5) {
        if(c1==null || c2==null || c3==null || c4==null || c5==null)
            return null;
        double phi12 = Math.abs(relPhi(c1, c2));
        if (phi12 > Constants.phi12cut) {
            return null;
        }
        double phi13 = Math.abs(relPhi(c1, c3));
        if (phi13 > Constants.phi13cut) {
            return null;
        }
        double phi14 = Math.abs(relPhi(c1, c4));
        if (phi14 > Constants.phi14cut) {
            return null;
        }
        double phi15 = Math.abs(relPhi(c1, c5));
        if (phi15 > Constants.phi14cut) {
            return null;
        }
        double rad123 = radCurvature(c1, c2, c3);
        if (Math.abs(rad123) < Constants.radcut) {
            return null;
        }
        double rad124 = radCurvature(c1, c2, c4);
        if (Math.abs(rad124) < Constants.radcut) {
            return null;
        }
        double rad134 = radCurvature(c1, c3, c4);
        if (Math.abs(rad134) < Constants.radcut) {
            return null;
        }
        double rad234 = radCurvature(c2, c3, c4);
        if (Math.abs(rad234) < Constants.radcut) {
            return null;
        }
        double rad135 = radCurvature(c1, c3, c5);
        if (Math.abs(rad135) < Constants.radcut) {
            return null;
        }
        double rad235 = radCurvature(c2, c3, c5);
        if (Math.abs(rad235) < Constants.radcut) {
            return null;
        }
        double rad245 = radCurvature(c2, c4, c5);
        if (Math.abs(rad245) < Constants.radcut) {
            return null;
        }
        double[] seed_delta_phi = {phi12, phi13, phi14, phi15};
        double[] seed_radius = {rad123, rad124, rad134, rad234, rad135, rad235, rad245};
        // create the seed
        Seed seed = new Seed(seed_delta_phi, seed_radius);
        seed.add(c1);
        seed.add(c2);
        seed.add(c3);
        seed.add(c4);
        
        return seed;
    }

    private Seed isTrack4(Cross c1, Cross c2, Cross c3, Cross c4) {
        if(c1==null || c2==null || c3==null || c4==null)
            return null;
        
        double phi12 = Math.abs(relPhi(c1, c2));
        if (phi12 > Constants.phi12cut) {
            return null;
        }
        double phi13 = Math.abs(relPhi(c1, c3));
        if (phi13 > Constants.phi13cut) {
            return null;
        }
        double phi14 = Math.abs(relPhi(c1, c4));
        if (phi14 > Constants.phi14cut) {
            return null;
        }
        double rad123 = radCurvature(c1, c2, c3);
        if (Math.abs(rad123) < Constants.radcut) {
            return null;
        }
        double rad124 = radCurvature(c1, c2, c4);
        if (Math.abs(rad124) < Constants.radcut) {
            return null;
        }
        double rad134 = radCurvature(c1, c3, c4);
        if (Math.abs(rad134) < Constants.radcut) {
            return null;
        }
        double rad234 = radCurvature(c2, c3, c4);
        if (Math.abs(rad234) < Constants.radcut) {
            return null;
        }
        double[] seed_delta_phi = {phi12, phi13, phi14};
        double[] seed_radius = {rad123, rad124, rad134, rad234};
        // create the seed
        Seed seed = new Seed(seed_delta_phi, seed_radius);
        seed.add(c1);
        seed.add(c2);
        seed.add(c3);
        seed.add(c4);
        
        return seed;
    }

    private Seed isTrack3(Cross c1, Cross c2, Cross c3) {
        if(c1==null || c2==null || c3==null)
            return null;
        double phi12 = Math.abs(relPhi(c1,c2));
        if (phi12 > Constants.phi12cut) {
            return null;
        }
        double phi13 = Math.abs(relPhi(c1, c3));
        if (phi13 > Constants.phi13cut) {
            return null;
        }
        double rad123 = radCurvature(c1, c2, c3);
        if (Math.abs(rad123) < Constants.radcut) {
            return null;
        }
        
        double[] seed_delta_phi = {phi12, phi13};
        double[] seed_radius = {rad123};
        // create the seed
        Seed seed = new Seed(seed_delta_phi, seed_radius);
        seed.add(c1);
        seed.add(c2);
        seed.add(c3);
        
        return seed;
    }

    private double relPhi(Cross c1, Cross c2) {
        //double cos_ZDiff = -1;
        double x1 = c1.get_Point().x();
        double y1 = c1.get_Point().y();
        double x2 = c2.get_Point().x();
        double y2 = c2.get_Point().y();
        double n1 = Math.sqrt(x1*x1+y1*y1);
        double n2 = Math.sqrt(x2*x2+y2*y2);
        //Vector3D bt1 = new Vector3D(c1.get_Point().x(), c1.get_Point().y(),0);
        //Vector3D bt2 = new Vector3D(c2.get_Point().x(), c2.get_Point().y(),0);
        //cos_ZDiff = bt1.asUnit().dot(bt2.asUnit());
        return Math.toDegrees(Math.acos((x1*x2+y1*y2)/(n1*n2)));
    }
    
    private double radCurvature(Cross c1, Cross c2, Cross c3) {
        double radiusOfCurv = 0;
        if (Math.abs(c2.get_Point().x() - c1.get_Point().x()) > 1.0e-9 && Math.abs(c3.get_Point().x() - c2.get_Point().x()) > 1.0e-9) {
            // Find the intersection of the lines joining the innermost to middle and middle to outermost point
            double ma = (c2.get_Point().y() - c1.get_Point().y()) / (c2.get_Point().x() - c1.get_Point().x());
            double mb = (c3.get_Point().y() - c2.get_Point().y()) / (c3.get_Point().x() - c2.get_Point().x());

            if (Math.abs(mb - ma) > 1.0e-9) {
                double xcen = 0.5 * (ma * mb * (c1.get_Point().y() - c3.get_Point().y()) + mb * (c1.get_Point().x() + c2.get_Point().x()) - ma * (c2.get_Point().x() + c3.get_Point().x())) / (mb - ma);
                double ycen = (-1. / mb) * (xcen - 0.5 * (c2.get_Point().x() + c3.get_Point().x())) + 0.5 * (c2.get_Point().y() + c3.get_Point().y());

                radiusOfCurv = Math.sqrt(Math.pow((c1.get_Point().x() - xcen), 2) + Math.pow((c1.get_Point().y() - ycen), 2));
            }
        
        }
        return radiusOfCurv;

    }

    private void MatchToRegion1(Seed s, ArrayList<Cross> R1Crosses, org.jlab.rec.cvt.svt.Geometry svt_geo, Swim swimmer) {
        
        if(s==null)
            return;
        Track cand = s.seedFit.fitSeed(s, svt_geo, 3, true, swimmer);
        if(cand==null)
            return;
         
        Point3D trkAtR1 =cand.get_helix().getPointAtRadius(org.jlab.rec.cvt.svt.Constants.MODULERADIUS[0][0]+org.jlab.rec.cvt.svt.Constants.LAYRGAP/2);
        List<Cross> candMatches = new ArrayList<Cross>();
        for (int i = 0; i < R1Crosses.size(); i++) {
            if(R1Crosses.get(i)==null)
                continue;
            
            if(Math.abs(Math.sqrt(trkAtR1.x()*trkAtR1.x()+trkAtR1.y()*trkAtR1.y()) - 
                    Math.sqrt(R1Crosses.get(i).get_Point().x()*R1Crosses.get(i).get_Point().x()+R1Crosses.get(i).get_Point().y()*R1Crosses.get(i).get_Point().y()))<2)
                candMatches.add(R1Crosses.get(i));
        }
        Point3D trkAtL1 =cand.get_helix().getPointAtRadius(org.jlab.rec.cvt.svt.Constants.MODULERADIUS[0][0]);
        Point3D trkAtL2 =cand.get_helix().getPointAtRadius(org.jlab.rec.cvt.svt.Constants.MODULERADIUS[0][0]+org.jlab.rec.cvt.svt.Constants.LAYRGAP);
        
        double dMin = Double.POSITIVE_INFINITY;
        Cross cMatch = null;
        for (int i = 0; i < candMatches.size(); i++) {//find cross for which the distance of the track to the 2 clusters in the double layers is minimal
            double d = svt_geo.getDOCAToStrip(candMatches.get(i).get_Sector(), candMatches.get(i).get_Cluster1().get_Layer(), candMatches.get(i).get_Cluster1().get_Centroid(), trkAtL1)
                    +svt_geo.getDOCAToStrip(candMatches.get(i).get_Sector(), candMatches.get(i).get_Cluster2().get_Layer(), candMatches.get(i).get_Cluster2().get_Centroid(), trkAtL2);
            if(d<dMin) {
                dMin =d;
                cMatch = (Cross) candMatches.get(i).clone();
            }
        }
        if(cMatch != null) 
            s.add(cMatch);
  
    }

    private void MatchBMTC(Seed s, ArrayList<Cross> BMTCrosses, org.jlab.rec.cvt.svt.Geometry svt_geo, Swim swimmer) {
        
        Track cand = s.seedFit.fitSeed(s, svt_geo, 3, true, swimmer);
        if(s==null)
            return;
        double maxChi2 = Double.POSITIVE_INFINITY;
        Cross BestMatch = null;
        for (int i = 0; i < BMTCrosses.size(); i++) {
        if(passCcross(s, BMTCrosses.get(i)) == false) {
            continue; 
        } else {
            s.add(BMTCrosses.get(i));
            cand = s.seedFit.fitSeed(s, svt_geo, 3, true, swimmer);
            if(cand==null)
                continue;
            double linechi2perndf = cand.get_lineFitChi2PerNDF();
            if(linechi2perndf<maxChi2) {
                maxChi2 = linechi2perndf;
                BestMatch = (Cross) BMTCrosses.get(i).clone();
            }
            s.remove(s.size()-1);
        }
        if(BestMatch!=null)
            s.add(BestMatch);
           
        }
    }

    private boolean ContainsSeed(List<Seed> CirTrks, Seed cand) {
        boolean inSeed = false;
       
        for(int i = 0; i<CirTrks.size(); i++) {
            int NbOverlaps =0;
            for(Cross ci: CirTrks.get(i)) {
                for(Cross c: cand)
                    if(c.get_Id()==ci.get_Id())
                        NbOverlaps++;
            }
            if(CirTrks.get(i).size()==NbOverlaps)
                CirTrks.remove(i);
            if(cand.size()==NbOverlaps)
                inSeed=true;
        }
        return inSeed;
    }

    private double calcCentErr(Cross c, Cluster Cluster1, Geometry svt_geo) {
        double Z = svt_geo.transformToFrame(Cluster1.get_Sector(), Cluster1.get_Layer(), c.get_Point().x(), c.get_Point().y(), c.get_Point().z(), "local", "").z();
        if(Z<0)
            Z=0;
        if(Z>Constants.ACTIVESENLEN)
            Z=Constants.ACTIVESENLEN;
        return Cluster1.get_ResolutionAlongZ(Z, svt_geo) / (Constants.PITCH / Math.sqrt(12.));
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
        final double[] delta_phi;		// opening angle between cross 1 and 2 positions wrt the origin
        final double[] radius;          // the radius of the circle of the seed calculated using 3 crosses belonging to the seed
        public TrackSeeder seedFit;
        /**
         * The constructor of the seed
         *
         * @param delta_phi
         * @param radius
         */
        Seed(double[] delta_phi, double[] radius) {
            seedFit = new TrackSeeder();
            this.delta_phi = delta_phi;
            this.radius = radius;
        }
    }

}
