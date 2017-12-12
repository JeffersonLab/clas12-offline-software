package org.jlab.rec.cvt.track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jlab.geom.prim.Point3D;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.cross.CrossMaker;
import org.jlab.rec.cvt.fit.CircleFitter;
import org.jlab.rec.cvt.fit.HelicalTrackFitter;
import org.jlab.rec.cvt.svt.Constants;

public class TrackSeeder {

    public TrackSeeder() {
        
        sortedClusters = new ArrayList<ArrayList<ArrayList<Cluster>>>();
        
        for(int b =0; b<36; b++) {
            sortedClusters.add(b, new ArrayList<ArrayList<Cluster>>() );
            for(int l =0; l<6; l++) {
                sortedClusters.get(b).add(l,new ArrayList<Cluster>() );
            }
        }
    }

    private List<ArrayList<Cluster>> seedClusters = new ArrayList<ArrayList<Cluster>>();

    double[] phiShift = new double[]{0, 90}; // move the bin edge to handle bin boundaries

    public void FindSeedClusters(List<Cluster> SVTclusters) {
        
        seedClusters.removeAll(seedClusters); 
        //for(int si1 = 0; si1<seedClusters.size(); si1++)
        //    seedClusters.get(si1).clear();

        List<ArrayList<Cluster>> phi0 = FindSeedClustersFixedBin(SVTclusters, phiShift[0]);
        
        List<ArrayList<Cluster>> phi90 = FindSeedClustersFixedBin(SVTclusters, phiShift[1]);
        if (phi0.size() > phi90.size()) {
            seedClusters = phi0; 
        }
        if (phi90.size() > phi0.size()) {
            seedClusters = phi90;
        }
        if (phi90.size() == phi0.size()) {
            for (int i = 0; i < phi0.size(); i++) {
                if (phi0.get(i).size() >= phi90.get(i).size()) {
                    seedClusters.add(phi0.get(i));
                } else {
                    seedClusters.add(phi90.get(i));
                }
            }
        }
    }
   
    List<ArrayList<ArrayList<Cluster>>> sortedClusters;
    
    public List<ArrayList<Cluster>> FindSeedClustersFixedBin(List<Cluster> SVTclusters, double phiShift) {
            for(int b =0; b<36; b++) {
                for(int l =0; l<6; l++) {
                sortedClusters.get(b).get(l).clear();
            }
        }
        List<ArrayList<Cluster>> inseedClusters = new ArrayList<ArrayList<Cluster>>();
        int[][] LPhi = new int[36][6];
        for (int i = 0; i < SVTclusters.size(); i++) {
            double phi = Math.toDegrees(SVTclusters.get(i).get(0).get_Strip().get_ImplantPoint().toVector3D().phi());

            phi += phiShift;
            if (phi < 0) {
                phi += 360;
            }

            int binIdx = (int) (phi / 36);
            sortedClusters.get(binIdx).get(SVTclusters.get(i).get_Layer() - 1).add(SVTclusters.get(i));
            LPhi[binIdx][SVTclusters.get(i).get_Layer() - 1]++; 
        }
        
        
        for (int b = 0; b < 36; b++) {
            int max_layers =0;
            for (int la = 0; la < 6; la++) {
                if(LPhi[b][la]>0)
                    max_layers++;
            }
            if (sortedClusters.get(b) != null && max_layers>3) { 
                double SumLyr=0;
                while(LPhi[b][0]+LPhi[b][1]+ LPhi[b][2]+LPhi[b][3]+ LPhi[b][4]+ LPhi[b][5]>=max_layers) {
                    if(SumLyr!=LPhi[b][0]+LPhi[b][1]+ LPhi[b][2]+LPhi[b][3]+ LPhi[b][4]+ LPhi[b][5]) {
                        SumLyr = LPhi[b][0]+LPhi[b][1]+ LPhi[b][2]+LPhi[b][3]+ LPhi[b][4]+ LPhi[b][5];
                    } 
                    ArrayList<Cluster> hits = new ArrayList<Cluster>(); 
                    for (int la = 0; la < 6; la++) {

                        if (sortedClusters.get(b).get(la) != null && LPhi[b][la]>0) { 
                            if (sortedClusters.get(b).get(la).get(LPhi[b][la]-1) != null && sortedClusters.get(b).get(la).get(LPhi[b][la]-1).size()>0) {
                                hits.add(sortedClusters.get(b).get(la).get(LPhi[b][la]-1)); 
                               
                                 if(LPhi[b][la]>1)
                                    LPhi[b][la]--; 
                                 if(SumLyr==max_layers)
                                     LPhi[b][la]=0; 
                            }
                        }

                    }
                   
                    if (hits.size() > 3) {
                        inseedClusters.add(hits);
                    }
                    
                }
                
            }
        }
       
        return inseedClusters;
    }

    

    
    private List<Double> Xs = new ArrayList<Double>();
    private List<Double> Ys = new ArrayList<Double>();
    private List<Double> Ws = new ArrayList<Double>();

    private List<Seed> BMTmatches = new ArrayList<Seed>();

    public List<Seed> findSeed(List<Cluster> SVTclusters, org.jlab.rec.cvt.svt.Geometry svt_geo, List<Cross> bmt_crosses, org.jlab.rec.cvt.bmt.Geometry bmt_geo) {
       
        List<Seed> seedlist = new ArrayList<Seed>();

        this.FindSeedClusters(SVTclusters);

        for (int s = 0; s < seedClusters.size(); s++) {
            //	if(seeds.get(s).size()<4)
            //	continue;

            Xs.clear();
            Ys.clear();
            Ws.clear();
            ((ArrayList<Double>) Xs).ensureCapacity(seedClusters.get(s).size());
            ((ArrayList<Double>) Ys).ensureCapacity(seedClusters.get(s).size());
            ((ArrayList<Double>) Ws).ensureCapacity(seedClusters.get(s).size());

            int loopIdx = 0;
            for (Cluster c : seedClusters.get(s)) {
                Xs.add(loopIdx, c.get(0).get_Strip().get_MidPoint().x()); 
                Ys.add(loopIdx, c.get(0).get_Strip().get_MidPoint().y());
                double err = svt_geo.getSingleStripResolution(c.get(0).get_Layer(), c.get(0).get_Strip().get_Strip(), Constants.ACTIVESENLEN / 2);
                Ws.add(loopIdx, 1. / (err * err));
                loopIdx++;
            }

            CircleFitter circlefit = new CircleFitter();
            boolean circlefitstatusOK = circlefit.fitStatus(Xs, Ys, Ws, Xs.size());

            if (!circlefitstatusOK) {
                continue;
            }
            CrossMaker cm = new CrossMaker();
            // instantiate array of clusters that are sorted by detector (SVT, BMT [C, Z]) and inner/outer layers
            ArrayList<ArrayList<Cluster>> sortedClusters = new ArrayList<ArrayList<Cluster>>();
            // fill the sorted list
            sortedClusters = cm.sortClusterByDetectorAndIO(seedClusters.get(s));
            // array indexes: array index 0 (1) = svt inner (outer) layer clusters, 2 (3) = bmt inner (outer) layers
            ArrayList<Cluster> svt_innerlayrclus = sortedClusters.get(0);
            ArrayList<Cluster> svt_outerlayrclus = sortedClusters.get(1);
            // arrays of BMT and SVT crosses
            ArrayList<Cross> SVTCrosses = cm.findSVTCrosses(svt_innerlayrclus, svt_outerlayrclus, svt_geo);
            
            Track cand = fitSeed(SVTCrosses, svt_geo, 5, false);
            if (cand != null) {
                Seed seed = new Seed();
                seed.set_Clusters(seedClusters.get(s));
                seed.set_Crosses(SVTCrosses);
                seed.set_Helix(cand.get_helix());
                
                //match to BMT
                if (bmt_crosses != null ) {
                    List<Cross> sameSectorCrosses = this.FindCrossesInSameSectorAsSVTTrk(seed, bmt_crosses, bmt_geo);
                    BMTmatches.clear();
                    if (sameSectorCrosses.size() < 100 && sameSectorCrosses.size() >= 0) {
                        BMTmatches = this.findCandUsingMicroMegas(seed, sameSectorCrosses, bmt_geo);
                    } 
                    
                    for (Seed bseed : BMTmatches) {
                        //System.out.println(" All crosses on track:");
                        //for(Cross c : bseed.get_Crosses())
                        //	System.out.println(c.printInfo());
                        //refit using the BMT
                        Track bcand = fitSeed(bseed.get_Crosses(), svt_geo, 5, false);
                        if (bcand != null) {
                            seed = new Seed();
                            seed.set_Clusters(seedClusters.get(s));
                            seed.set_Crosses(bseed.get_Crosses());
                            seed.set_Helix(bcand.get_helix());
                        }
                    }

                    seedlist.add(seed);
                } else { // no bmt
                    seedlist.add(seed);
                }
            }
        }
        
        return seedlist;
    }
    

    private List<Cross> FindCrossesInSameSectorAsSVTTrk(Seed seed, List<Cross> bmt_crosses, org.jlab.rec.cvt.bmt.Geometry bmt_geo) {
        List<Cross> bmt_crossesInSec = new ArrayList<Cross>();
        //double angle_i = 0; // first angular boundary init
        //double angle_f = 0; // second angular boundary for detector A, B, or C init
        
        // the hit parameters
        double angle = Math.atan2(seed.get_Crosses().get(seed.get_Crosses().size() - 1).get_Point().y(),
                seed.get_Crosses().get(seed.get_Crosses().size() - 1).get_Point().x());
        if (angle < 0) {
            angle += 2 * Math.PI;
        }

        double jitter = Math.toRadians(10); // 10 degrees jitter
        for (int i = 0; i < bmt_crosses.size(); i++) { 
            if (bmt_crosses.get(i).get_DetectorType().equalsIgnoreCase("Z") && bmt_geo.isInDetector(1, angle, jitter) == bmt_crosses.get(i).get_Sector() - 1) {
                bmt_crossesInSec.add(bmt_crosses.get(i)); 
            }
            if (bmt_crosses.get(i).get_DetectorType().equalsIgnoreCase("C")) {
                bmt_crossesInSec.add(bmt_crosses.get(i)); 
            }
        }

        return bmt_crossesInSec;
    }

    private List<Double> X = new ArrayList<Double>();
    private List<Double> Y = new ArrayList<Double>();
    private List<Double> Z = new ArrayList<Double>();
    private List<Double> Rho = new ArrayList<Double>();
    private List<Double> ErrZ = new ArrayList<Double>();
    private List<Double> ErrRho = new ArrayList<Double>();
    private List<Double> ErrRt = new ArrayList<Double>();
    List<Cross> BMTCrossesC = new ArrayList<Cross>();
    List<Cross> BMTCrossesZ = new ArrayList<Cross>();
    List<Cross> SVTCrosses = new ArrayList<Cross>();

    public Track fitSeed(List<Cross> VTCrosses, org.jlab.rec.cvt.svt.Geometry svt_geo, int fitIter, boolean originConstraint) {
        double chisqMax = Double.POSITIVE_INFINITY;
        
        Track cand = null;
        HelicalTrackFitter fitTrk = new HelicalTrackFitter();
        for (int i = 0; i < fitIter; i++) {
            //	if(originConstraint==true) {
            //		X.add(0, (double) 0);
            //		Y.add(0, (double) 0);
            //		Z.add(0, (double) 0);
            //		Rho.add(0, (double) 0);
            //		ErrRt.add(0, (double) org.jlab.rec.cvt.svt.Constants.RHOVTXCONSTRAINT);
            //		ErrZ.add(0, (double) org.jlab.rec.cvt.svt.Constants.ZVTXCONSTRAINT);		
            //		ErrRho.add(0, (double) org.jlab.rec.cvt.svt.Constants.RHOVTXCONSTRAINT);										
            //	}
            X.clear();
            Y.clear();
            Z.clear();
            Rho.clear();
            ErrZ.clear();
            ErrRho.clear();
            ErrRt.clear();

            int svtSz = 0;
            int bmtZSz = 0;
            int bmtCSz = 0;

            BMTCrossesC.clear();
            BMTCrossesZ.clear();
            SVTCrosses.clear();

            for (Cross c : VTCrosses) {
                if (!(Double.isNaN(c.get_Point().z()) || Double.isNaN(c.get_Point().x()))) {
                    SVTCrosses.add(c);
                }

                if (Double.isNaN(c.get_Point().x())) {
                    BMTCrossesC.add(c);
                }
                if (Double.isNaN(c.get_Point().z())) {
                    BMTCrossesZ.add(c);
                }
            }
            svtSz = SVTCrosses.size();
            if (BMTCrossesZ != null) {
                bmtZSz = BMTCrossesZ.size();
            }
            if (BMTCrossesC != null) {
                bmtCSz = BMTCrossesC.size();
            }

            int useSVTdipAngEst = 1;
            if (bmtCSz >= 2) {
                useSVTdipAngEst = 0;
            }

            ((ArrayList<Double>) X).ensureCapacity(svtSz + bmtZSz);
            ((ArrayList<Double>) Y).ensureCapacity(svtSz + bmtZSz);
            ((ArrayList<Double>) Z).ensureCapacity(svtSz * useSVTdipAngEst + bmtCSz);
            ((ArrayList<Double>) Rho).ensureCapacity(svtSz * useSVTdipAngEst + bmtCSz);
            ((ArrayList<Double>) ErrZ).ensureCapacity(svtSz * useSVTdipAngEst + bmtCSz);
            ((ArrayList<Double>) ErrRho).ensureCapacity(svtSz * useSVTdipAngEst + bmtCSz); // Try: don't use svt in dipdangle fit determination
            ((ArrayList<Double>) ErrRt).ensureCapacity(svtSz + bmtZSz);

            cand = new Track(null);
            cand.addAll(SVTCrosses);
            for (int j = 0; j < SVTCrosses.size(); j++) {
                X.add(j, SVTCrosses.get(j).get_Point().x());
                Y.add(j, SVTCrosses.get(j).get_Point().y());
                if (useSVTdipAngEst == 1) {
                    Z.add(j, SVTCrosses.get(j).get_Point().z());
                    Rho.add(j, Math.sqrt(SVTCrosses.get(j).get_Point().x() * SVTCrosses.get(j).get_Point().x()
                            + SVTCrosses.get(j).get_Point().y() * SVTCrosses.get(j).get_Point().y()));
                    ErrRho.add(j, Math.sqrt(SVTCrosses.get(j).get_PointErr().x() * SVTCrosses.get(j).get_PointErr().x()
                            + SVTCrosses.get(j).get_PointErr().y() * SVTCrosses.get(j).get_PointErr().y()));
                    ErrZ.add(j, SVTCrosses.get(j).get_PointErr().z());
                }
                ErrRt.add(j, Math.sqrt(SVTCrosses.get(j).get_PointErr().x() * SVTCrosses.get(j).get_PointErr().x()
                        + SVTCrosses.get(j).get_PointErr().y() * SVTCrosses.get(j).get_PointErr().y()));
            }

            if (bmtZSz > 0) {
                for (int j = svtSz; j < svtSz + bmtZSz; j++) {
                    X.add(j, BMTCrossesZ.get(j - svtSz).get_Point().x());
                    Y.add(j, BMTCrossesZ.get(j - svtSz).get_Point().y());
                    ErrRt.add(j, Math.sqrt(BMTCrossesZ.get(j - svtSz).get_PointErr().x() * BMTCrossesZ.get(j - svtSz).get_PointErr().x()
                            + BMTCrossesZ.get(j - svtSz).get_PointErr().y() * BMTCrossesZ.get(j - svtSz).get_PointErr().y()));
                }
            }
            if (bmtCSz > 0) {
                for (int j = svtSz * useSVTdipAngEst; j < svtSz * useSVTdipAngEst + bmtCSz; j++) {
                    Z.add(j, BMTCrossesC.get(j - svtSz * useSVTdipAngEst).get_Point().z());
                    Rho.add(j, org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[BMTCrossesC.get(j - svtSz * useSVTdipAngEst).get_Region() - 1]
                            + org.jlab.rec.cvt.bmt.Constants.hStrip2Det);
                    
                    ErrRho.add(j, org.jlab.rec.cvt.bmt.Constants.hStrip2Det / Math.sqrt(12.));
                    ErrZ.add(j, BMTCrossesC.get(j - svtSz * useSVTdipAngEst).get_PointErr().z());
                }
            }
            X.add((double) 0);
            Y.add((double) 0);

            ErrRt.add((double) 0.1);
            
            fitTrk.fit(X, Y, Z, Rho, ErrRt, ErrRho, ErrZ);

            if (fitTrk.get_helix() == null) {
                return null;
            }

            cand = new Track(fitTrk.get_helix());
            //cand.addAll(SVTCrosses);
            cand.addAll(SVTCrosses);
            cand.set_HelicalTrack(fitTrk.get_helix());
            //if(shift==0)
            if (fitTrk.get_chisq()[0] < chisqMax) {
                chisqMax = fitTrk.get_chisq()[0];
                cand.update_Crosses(svt_geo);
                //i=fitIter;
            }
        }
       
        return cand;
    }



    public List<Seed> findCandUsingMicroMegas(Seed trkCand,
        List<Cross> bmt_crosses, org.jlab.rec.cvt.bmt.Geometry bmt_geo) {
        List<ArrayList<Cross>> BMTCcrosses = new ArrayList<ArrayList<Cross>>();
        List<ArrayList<Cross>> BMTZcrosses = new ArrayList<ArrayList<Cross>>();
        ArrayList<Cross> matches = new ArrayList<Cross>();
        List<Seed> AllSeeds = new ArrayList<Seed>();
        int[] S = new int[6];
       
        for (int r = 0; r < 3; r++) {
            BMTCcrosses.add(new ArrayList<Cross>());
            BMTZcrosses.add(new ArrayList<Cross>());
        }
        //for (int r = 0; r < 3; r++) {
        //    BMTCcrosses.get(r).clear();
        //    BMTZcrosses.get(r).clear();
        //}

        for (Cross bmt_cross : bmt_crosses) { 
            if (bmt_cross.get_DetectorType().equalsIgnoreCase("C")) // C-detector
            {
                BMTCcrosses.get(bmt_cross.get_Region() - 1).add(bmt_cross); 
            }
            if (bmt_cross.get_DetectorType().equalsIgnoreCase("Z")) // Z-detector
            {
                BMTZcrosses.get(bmt_cross.get_Region() - 1).add(bmt_cross); 
            }
        }

        AllSeeds.clear();

        for (int r = 0; r < 3; r++) {
            S[r] = BMTCcrosses.get(r).size();
            if (S[r] == 0) {
                S[r] = 1;
            }
            S[r + 3] = BMTZcrosses.get(r).size();
            if (S[r + 3] == 0) {
                S[r + 3] = 1;
            }
        }

        for (int i1 = 0; i1 < S[0]; i1++) {
            for (int i2 = 0; i2 < S[1]; i2++) {
                for (int i3 = 0; i3 < S[2]; i3++) {
                    for (int j1 = 0; j1 < S[3]; j1++) {
                        for (int j2 = 0; j2 < S[4]; j2++) {
                            for (int j3 = 0; j3 < S[5]; j3++) {

                                matches.clear();

                                if (BMTCcrosses.get(0).size() > 0 && i1 < BMTCcrosses.get(0).size()) {
                                    if (this.passCcross(trkCand, BMTCcrosses.get(0).get(i1), bmt_geo)) {
                                        matches.add(BMTCcrosses.get(0).get(i1));
                                    }
                                }
                                if (BMTCcrosses.get(1).size() > 0 && i2 < BMTCcrosses.get(1).size()) {
                                    if (this.passCcross(trkCand, BMTCcrosses.get(1).get(i2), bmt_geo)) {
                                        matches.add(BMTCcrosses.get(1).get(i2));
                                    }
                                }
                                if (BMTCcrosses.get(2).size() > 0 && i3 < BMTCcrosses.get(2).size()) {
                                    if (this.passCcross(trkCand, BMTCcrosses.get(2).get(i3), bmt_geo)) {
                                        matches.add(BMTCcrosses.get(2).get(i3));
                                    }
                                }
                                if (BMTZcrosses.get(0).size() > 0 && j1 < BMTZcrosses.get(0).size()) {
                                    if (this.passZcross(trkCand, BMTZcrosses.get(0).get(j1))) {
                                        matches.add(BMTZcrosses.get(0).get(j1));
                                    }
                                }
                                if (BMTZcrosses.get(1).size() > 0 && j2 < BMTZcrosses.get(1).size()) {
                                    if (this.passZcross(trkCand, BMTZcrosses.get(1).get(j2))) {
                                        matches.add(BMTZcrosses.get(1).get(j2));
                                    }
                                }
                                if (BMTZcrosses.get(2).size() > 0 && j3 < BMTZcrosses.get(2).size()) {
                                    if (this.passZcross(trkCand, BMTZcrosses.get(2).get(j3))) {
                                        matches.add(BMTZcrosses.get(2).get(j3));
                                    }
                                }
                                
                                matches.addAll(trkCand.get_Crosses());
                                if (matches.size() > 0) {
                                    Seed BMTTrkSeed = new Seed();

                                    BMTTrkSeed.set_Clusters(trkCand.get_Clusters());

                                    BMTTrkSeed.set_Helix(trkCand.get_Helix());
                                    BMTTrkSeed.set_Crosses(matches);
                                    
                                    AllSeeds.add(BMTTrkSeed);
                                    if (AllSeeds.size() > 200) {
                                        AllSeeds.clear();
                                        return AllSeeds;
                                    }
                                    BMTTrkSeed = null;
                                }
                                                                
                            }
                        }
                    }
                }
            }
        }
        this.removeDuplicates(AllSeeds);
        
        return AllSeeds;
    }

    private boolean passCcross(Seed trkCand, Cross bmt_Ccross, org.jlab.rec.cvt.bmt.Geometry bmt_geo) {
        boolean pass = false;

        double dzdrsum = trkCand.get_Helix().get_tandip();

        double z_bmt = bmt_Ccross.get_Point().z();
        double r_bmt = org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[bmt_Ccross.get_Region() - 1];
        Point3D phiHelixAtSurf = trkCand.get_Helix().getPointAtRadius(r_bmt); 
        if (bmt_geo.isInSector(bmt_Ccross.get_Cluster1().get_Layer(), Math.atan2(phiHelixAtSurf.y(), phiHelixAtSurf.x()), Math.toRadians(10)) != bmt_Ccross.get_Sector()) {
            return false;
        }
        double dzdr_bmt = z_bmt / r_bmt;
        if (Math.abs(1 - (dzdrsum / (double) (trkCand.get_Crosses().size())) / ((dzdrsum + dzdr_bmt) / (double) (trkCand.get_Crosses().size() + 1))) <= Constants.dzdrcut) // add this to the track
        {
            pass = true;
        } 
        
        return pass;
    }

    private boolean passZcross(Seed trkCand, Cross bmt_Zcross) {
        double ave_seed_rad = trkCand.get_Helix().radius();

        double x_bmt = bmt_Zcross.get_Point().x();
        double y_bmt = bmt_Zcross.get_Point().y();
        boolean pass = true;
        for (int i = 0; i < trkCand.get_Crosses().size() - 2; i++) {
            if (trkCand.get_Crosses().get(i).get_Point().x() != x_bmt) {
                double rad_withBmt = calc_radOfCurv(trkCand.get_Crosses().get(i).get_Point().x(), trkCand.get_Crosses().get(i + 1).get_Point().x(), x_bmt,
                        trkCand.get_Crosses().get(i).get_Point().y(), trkCand.get_Crosses().get(i + 1).get_Point().y(), y_bmt);
                if (rad_withBmt == 0) {
                    continue;
                }

                if (rad_withBmt < Constants.radcut || Math.abs((rad_withBmt - ave_seed_rad) / ave_seed_rad) > 0.75)           
                {
                    pass = false;
                }

            }
        }

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

    private void removeDuplicates(List<Seed> AllSeeds) {
        Collections.sort(AllSeeds);
        List<Seed> Dupl = new ArrayList<Seed>();
        for(int i = 1; i < AllSeeds.size(); i++) { 
            if(AllSeeds.get(i-1).get_IntIdentifier().equalsIgnoreCase(AllSeeds.get(i).get_IntIdentifier())) { 
                Dupl.add(AllSeeds.get(i));
            }
        }
        AllSeeds.removeAll(Dupl);
        
    }

}