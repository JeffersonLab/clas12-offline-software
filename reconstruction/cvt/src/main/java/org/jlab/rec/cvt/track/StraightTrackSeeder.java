package org.jlab.rec.cvt.track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.detector.base.DetectorType;

import org.jlab.geom.prim.Point3D;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.Geometry;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.fit.CircleFitter;
import org.jlab.rec.cvt.fit.CircleFitPars;
import org.jlab.rec.cvt.fit.StraightTrackFitter;
import org.jlab.rec.cvt.svt.SVTParameters;

public class StraightTrackSeeder {
    int NBINS = 36;
    double[] phiShift = new double[]{0, 65, 90}; // move the bin edge to handle bin boundaries
    List<ArrayList<Cross>> scan ;
    Map<Double, ArrayList<Cross>> seedMap ; // init seeds;
    List<ArrayList<ArrayList<Cross>>> sortedCrosses;
    List<Seed> seedScan ;
    List<Double> Xs ;
    List<Double> Ys ;
    List<Double> Ws ;
    double xbeam;
    double ybeam;
    
    public StraightTrackSeeder(double xb, double yb) {
        xbeam = xb;
        ybeam = yb;
        //init lists for scan
        sortedCrosses = new ArrayList<>();
        for(int b =0; b<NBINS; b++) {
            sortedCrosses.add(b, new ArrayList<>() );
            for(int l =0; l<3; l++) {
                sortedCrosses.get(b).add(l,new ArrayList<>() );
            }
        }
        scan = new ArrayList<>();
        seedMap = new HashMap<>(); // init seeds;
        seedScan = new ArrayList<>();
        //for fitting
        Xs = new ArrayList<>();
        Ys = new ArrayList<>();
        Ws = new ArrayList<>();
    }
    private void MatchSeed(List<Cross> othercrs) {
        
        for (Seed seed : seedScan) {
            double d = seed.getDoca();
            double r = seed.getRho();
            double f = seed.getPhi();

            Map<Integer, Cross> matchBMT = new HashMap<>();
            for (Cross c : othercrs ) { 
                if(this.InSamePhiRange(seed, c)== true) {
                    double xi = c.getPoint().x(); 
                    double yi = c.getPoint().y();
                    double ri = Math.sqrt(xi*xi+yi*yi);
                    double fi = Math.atan2(yi,xi) ;

                    double res = this.calcResi(r, ri, d, f, fi);
                    if(Math.abs(res)<20) { 
                        // add to seed   
                        if(matchBMT.containsKey(c.getOrderedRegion())) {
                            Cross cs = (Cross) matchBMT.get(c.getOrderedRegion());
                            double xis = cs.getPoint().x(); 
                            double yis = cs.getPoint().y();
                            double ris = Math.sqrt(xis*xis+yis*yis);
                            double fis = Math.atan2(yis,xis) ;
                            if(Math.abs(this.calcResi(r, ris, d, f, fis))>Math.abs(res)) {
                               matchBMT.put(c.getOrderedRegion(), c); // add only if res better
                            } 
                        } else {
                            matchBMT.put(c.getOrderedRegion(), c);
                        }
                    }
                }
            }
            // add hashmap items to list 
            for (Cross crs : matchBMT.values())  
                seed.getCrosses().add((Cross) crs);
        }
    }
    private void FitSeed(List<Cross> seedcrs) {
        Xs.clear();
        Ys.clear();
        Ws.clear();
        ((ArrayList<Double>) Xs).ensureCapacity(seedcrs.size()+1);
        ((ArrayList<Double>) Ys).ensureCapacity(seedcrs.size()+1);
        ((ArrayList<Double>) Ws).ensureCapacity(seedcrs.size()+1);
        Xs.add(0, this.xbeam); 
        Ys.add(0, this.ybeam);
        Ws.add(0,0.1);
        for (Cross c : seedcrs ) { 
            if(c.getType()==BMTType.C ) System.err.println("WRONG CROSS TYPE");
            Xs.add(c.getPoint().x()); 
            Ys.add(c.getPoint().y());
            Ws.add(1. / (c.getPointErr().x()*c.getPointErr().x()+c.getPointErr().y()*c.getPointErr().y()));
            
        }
        CircleFitter circlefit = new CircleFitter(xbeam, ybeam);
        boolean circlefitstatusOK = circlefit.fitStatus(Xs, Ys, Ws, Xs.size());
        CircleFitPars pars = circlefit.getFit(); 
        if(circlefitstatusOK==false )
            return;
        double d = pars.doca();
        double r = pars.rho(); r= 1.e-09;
        double f = pars.phi();
        
        boolean failed = false;
        for (Cross c : seedcrs ) { 
            double xi = c.getPoint().x(); 
            double yi = c.getPoint().y();
            double ri = Math.sqrt(xi*xi+yi*yi);
            double fi = Math.atan2(yi,xi) ;
            
            double res = this.calcResi(r, ri, d, f, fi);
            if(Math.abs(res)>10) { 
                failed = true; 
                return;
            }
        }
        Seed seed = new Seed(seedcrs, d, r, f);
        
        seedScan.add(seed);
    }
    
    /*
    Finds BMT seeds
    */
    public void findSeedCrossList(List<Cross> crosses) {
        
        seedMap.clear();
        
        for(int si1 = 0; si1<scan.size(); si1++)
            scan.get(si1).clear();
        
        for(int i = 0; i< phiShift.length; i++) {
            findSeedCrossesFixedBin(crosses, phiShift[i]); 
        }
        
        seedMap.forEach((key,value) -> this.FitSeed(value));
    }
   
    
    /*
    Scans overphase space to find groups of BMT crosses 
    */
    private void findSeedCrossesFixedBin(List<Cross> crosses, double phiShift) {
        for(int b =0; b<NBINS; b++) {
            for(int l =0; l<3; l++) {
                sortedCrosses.get(b).get(l).clear();
            }
        }
        int[][] LPhi = new int[NBINS][3];
        for (int i = 0; i < crosses.size(); i++) {
            double phi = Math.toDegrees(crosses.get(i).getPoint().toVector3D().phi());

            phi += phiShift;
            if (phi < 0) {
                phi += 360;
            }

            int binIdx = (int) (phi / (360./NBINS) );
            if(binIdx>35)
                binIdx = 35;
            sortedCrosses.get(binIdx).get(crosses.get(i).getRegion() - 1).add(crosses.get(i));
            LPhi[binIdx][crosses.get(i).getRegion() - 1]++; 
        }
        
        for (int b = 0; b < NBINS; b++) {
            int max_layers =0;
            for (int la = 0; la < 3; la++) { 
                if(LPhi[b][la]>0)
                    max_layers++;
            }
            if (sortedCrosses.get(b) != null && max_layers >= 2) { 
                double SumLyr=0;
                while(LPhi[b][0]+LPhi[b][1]+ LPhi[b][2]>=max_layers) {
                    if(SumLyr!=LPhi[b][0]+LPhi[b][1]+ LPhi[b][2]) {
                        SumLyr = LPhi[b][0]+LPhi[b][1]+ LPhi[b][2];
                    } 
                    ArrayList<Cross> hits = new ArrayList<Cross>(); 
                    for (int la = 0; la < 3; la++) {
                        if (sortedCrosses.get(b).get(la) != null && LPhi[b][la]>0) { 
                            if (sortedCrosses.get(b).get(la).get(LPhi[b][la]-1) != null 
                                    && sortedCrosses.get(b).get(la).size()>0) {
                                hits.add(sortedCrosses.get(b).get(la).get(LPhi[b][la]-1)); 
                                
                                if(LPhi[b][la]>1)
                                   LPhi[b][la]--; 
                                if(SumLyr==max_layers)
                                    LPhi[b][la]=0; 
                            }
                        }
                    }
                   
                    if (hits.size() >= 2) {
                        double seedIdx=0;
                        int s = hits.size();
                        int index = (int) Math.pow(2,s);
                        for(Cross c : hits) {
                            seedIdx +=c.getId()*Math.pow(10, index);
                            index-=4;
                        }
                        seedMap.put(seedIdx, hits);
                    }
                }
            }
        }
    }

    

    List<Seed> BMTmatches = new ArrayList<>();
    public List<Seed> findSeed(List<Cross> svt_crosses, List<Cross> bmt_crosses, boolean isSVTOnly) {
        BMTmatches.clear();
        seedScan.clear() ;
        List<Seed> seedlist = new ArrayList<>();

        List<Cross> crosses = new ArrayList<>();
        List<Cross> bmtC_crosses = new ArrayList<>();
        
        if(isSVTOnly == false ) {
            for(Cross c : bmt_crosses) { 
                if(c.getType()== BMTType.Z)
                    crosses.add(c);
                if(c.getType()==BMTType.C)
                    bmtC_crosses.add(c);
            }
        }
        //this.findSeedCrossList(crosses);
        //this.MatchSeed(svt_crosses);
        this.findSeedCrossList(svt_crosses);
        this.MatchSeed(crosses);
        
        for(Seed mseed : seedScan) { 
            List<Cross> seedcrs = mseed.getCrosses();
            
//            for (Cross c : seedcrs ) { 
//                if(c.getType()==BMTType.C ) continue;
//                c.setAssociatedTrackID(122220);
//            }
          // loop until a good circular fit. removing far crosses each time
          boolean circlefitstatusOK = false;
          while( ! circlefitstatusOK && seedcrs.size()>=3 ){
            
            Xs.clear();
            Ys.clear();
            Ws.clear();
            ((ArrayList<Double>) Xs).ensureCapacity(seedcrs.size()+1);
            ((ArrayList<Double>) Ys).ensureCapacity(seedcrs.size()+1);
            ((ArrayList<Double>) Ws).ensureCapacity(seedcrs.size()+1);
            Xs.add(0, this.xbeam); 
            Ys.add(0, this.ybeam);
            Ws.add(0, 0.1);
            for (Cross c : seedcrs ) { 
                if(c.getType()==BMTType.C ) continue;
//                c.setAssociatedTrackID(122221);
                Xs.add(c.getPoint().x()); 
                Ys.add(c.getPoint().y());
                Ws.add(1. / (c.getPointErr().x()*c.getPointErr().x()
                        +c.getPointErr().y()*c.getPointErr().y()));
            }

            CircleFitter circlefit = new CircleFitter(xbeam, ybeam);
            circlefitstatusOK = circlefit.fitStatus(Xs, Ys, Ws, Xs.size());
            CircleFitPars pars = circlefit.getFit();

            // if not a good fit, check for outliers 
            if (!circlefitstatusOK ||  pars.chisq()/(double)(Xs.size()-3)>10) {
              //System.out.println(" check circular fit" );
              double d = pars.doca();
              double r = pars.rho();
              double f = pars.phi();
              for (Cross c : seedcrs ) { 
                if(c.getType()==BMTType.C ) continue;
//                c.setAssociatedTrackID(122222);
                    double xi = c.getPoint().x(); 
                    double yi = c.getPoint().y();
                    double ri = Math.sqrt(xi*xi+yi*yi);
                    double fi = Math.atan2(yi,xi) ;
                    double res = this.calcResi(r, ri, d, f, fi);
                    if(Math.abs(res)>SVTParameters.RESIMAX) {
                        //System.out.println(" remove detector " + c .getDetector() + " region " + c.getRegion() + " sector " + c.getSector()  );
                        seedcrs.remove(c);
                        break;
                    }
                }
            }
          }
        }


        for(Seed mseed : seedScan) { 
            List<Cross> seedcrs = mseed.getCrosses();
            Track cand = null;
            if(seedcrs.size()>=3)
                cand = fitSeed(seedcrs, 5, false);
            if (cand != null) {
                Seed seed = new Seed();
                seed.setCrosses(seedcrs);
                seed.setHelix(cand.getHelix());
                
                //match to BMT
                if (seed != null ) {
                    List<Cross> sameSectorCrosses = this.findCrossesInSameSectorAsSVTTrk(seed, bmtC_crosses);
                    BMTmatches.clear();
                    if (sameSectorCrosses.size() >= 0) {
                        BMTmatches = this.findCandUsingMicroMegas(seed, sameSectorCrosses);
                    } 
                    
                    for (Seed bseed : BMTmatches) {
                        Collections.sort(bseed.getCrosses());
                        //refit using the BMT
                        Track bcand = fitSeed(bseed.getCrosses(), 5, false);
                        if (bcand != null) {
                            seed = new Seed();
                            seed.setCrosses(bseed.getCrosses());
                            seed.setHelix(bcand.getHelix());
                        }
                    }

                    seedlist.add(seed); 
                } else { 
                //    seedlist.add(seed); System.out.println("no BMT matched"+seed.getHelix().getZ0());
                }
            }
        }
        return seedlist;
    }
    

    private List<Cross> findCrossesInSameSectorAsSVTTrk(Seed seed, List<Cross> bmt_crosses) {
        List<Cross> bmt_crossesInSec = new ArrayList<>();
        //double angle_i = 0; // first angular boundary init
        //double angle_f = 0; // second angular boundary for detector A, B, or C init
        
        double jitter = Math.toRadians(10); // 10 degrees jitter
        for (int i = 0; i < bmt_crosses.size(); i++) { 
            Point3D ref =seed.getCrosses().get(0).getPoint();
            // the hit parameters
            double angle = Math.atan2(ref.y(), ref.x());
            if (angle < 0) {
                angle += 2 * Math.PI;
            }
            //if (bmt_geo.isInDetector(bmt_crosses.get(i).getRegion()*2-1, angle, jitter) 
            //        == bmt_crosses.get(i).getSector() - 1) 
            if (Geometry.getInstance().getBMT().inDetector(bmt_crosses.get(i).getRegion()*2-1, bmt_crosses.get(i).getSector(), ref)==true){
                bmt_crossesInSec.add(bmt_crosses.get(i)); 
            }
            
        }

        return bmt_crossesInSec;
    }

    private final List<Double> X = new ArrayList<>();
    private final List<Double> Y = new ArrayList<>();
    private final List<Double> Z = new ArrayList<>();
    private final List<Double> Rho = new ArrayList<>();
    private final List<Double> ErrZ = new ArrayList<>();
    private final List<Double> ErrRho = new ArrayList<>();
    private final List<Double> ErrRt = new ArrayList<>();
    private final List<Cross> BMTCrossesC = new ArrayList<>();
    private final List<Cross> BMTCrossesZ = new ArrayList<>();
    private final List<Cross> SVTCrosses = new ArrayList<>();
    
    public Track fitSeed(List<Cross> VTCrosses, int fitIter, 
            boolean originConstraint) {
        double chisqMax = Double.POSITIVE_INFINITY;
        
        Track cand = null;
        StraightTrackFitter fitTrk = new StraightTrackFitter();
        for (int i = 0; i < fitIter; i++) {
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
                if (c.getDetector() == DetectorType.BST) {
                    SVTCrosses.add(c);
                } else if (c.getDetector() == DetectorType.BMT && c.getType() == BMTType.C) {
                    BMTCrossesC.add(c);
                } else if (c.getDetector() == DetectorType.BMT && c.getType() == BMTType.Z) {
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

            for (int j = 0; j < SVTCrosses.size(); j++) {
                X.add(j, SVTCrosses.get(j).getPoint().x());
                Y.add(j, SVTCrosses.get(j).getPoint().y());
                if (useSVTdipAngEst == 1) {
                    Z.add(j, SVTCrosses.get(j).getPoint().z());
                    Rho.add(j, Math.sqrt(SVTCrosses.get(j).getPoint().x() * SVTCrosses.get(j).getPoint().x()
                            + SVTCrosses.get(j).getPoint().y() * SVTCrosses.get(j).getPoint().y()));
                    ErrRho.add(j, Math.sqrt(SVTCrosses.get(j).getPointErr().x() * SVTCrosses.get(j).getPointErr().x()
                            + SVTCrosses.get(j).getPointErr().y() * SVTCrosses.get(j).getPointErr().y()));
                    ErrZ.add(j, SVTCrosses.get(j).getPointErr().z());
                }
                ErrRt.add(j, Math.sqrt(SVTCrosses.get(j).getPointErr().x() * SVTCrosses.get(j).getPointErr().x()
                        + SVTCrosses.get(j).getPointErr().y() * SVTCrosses.get(j).getPointErr().y()));
            }

            if (bmtZSz > 0) {
                for (int j = svtSz; j < svtSz + bmtZSz; j++) {
                    X.add(j, BMTCrossesZ.get(j - svtSz).getPoint().x());
                    Y.add(j, BMTCrossesZ.get(j - svtSz).getPoint().y());
                    ErrRt.add(j, Math.sqrt(BMTCrossesZ.get(j - svtSz).getPointErr().x() * BMTCrossesZ.get(j - svtSz).getPointErr().x()
                            + BMTCrossesZ.get(j - svtSz).getPointErr().y() * BMTCrossesZ.get(j - svtSz).getPointErr().y()));
                }
            }
            if (bmtCSz > 0) {
                for (int j = svtSz * useSVTdipAngEst; j < svtSz * useSVTdipAngEst + bmtCSz; j++) {
                    Z.add(j, BMTCrossesC.get(j - svtSz * useSVTdipAngEst).getPoint().z());
                    Rho.add(j, Geometry.getInstance().getBMT().getRadiusMidDrift(BMTCrossesC.get(j - svtSz * useSVTdipAngEst).getCluster1().getLayer()));
                    
                    ErrRho.add(j, Geometry.getInstance().getBMT().getThickness()/2 / Math.sqrt(12.));
                    ErrZ.add(j, BMTCrossesC.get(j - svtSz * useSVTdipAngEst).getPointErr().z());
                }
            }
            //X.add((double) Constants.getXb());
            //Y.add((double) Constants.getYb());
            //ErrRt.add((double) 0.1);
            
            fitTrk.fit(X, Y, Z, Rho, ErrRt, ErrRho, ErrZ);
            
            if (fitTrk.getHelix() == null) { 
                return null;
            }

            fitTrk.getHelix().B = 0;
            cand = new Track(fitTrk.getHelix());
            cand.addAll(SVTCrosses);
            cand.addAll(BMTCrossesZ);
            cand.addAll(BMTCrossesC);
            
            //if(shift==0)
//            if(i==0) System.out.println();
//            System.out.println(fitTrk.getChi2()[0] + " " + chisqMax + " " + Constants.CIRCLEFIT_MAXCHI2);
            cand.update_Crosses(-1);
            if (fitTrk.getChi2()[0] < chisqMax) {
                chisqMax = fitTrk.getChi2()[0];
//                if(chisqMax<Constants.CIRCLEFIT_MAXCHI2)
//                    cand.update_Crosses(svt_geo, bmt_geo);
                //i=fitIter;
            }
        }
        //System.out.println(" Seed fitter "+fitTrk.getChi2()[0]+" "+fitTrk.getChi2()[1]); 
        if(chisqMax>Constants.CIRCLEFIT_MAXCHI2)
            cand=null;
        return cand;
    }



    public List<Seed> findCandUsingMicroMegas(Seed trkCand, List<Cross> bmt_crosses) {
        List<ArrayList<Cross>> BMTCcrosses = new ArrayList<>();
        
        ArrayList<Cross> matches = new ArrayList<>();
        List<Seed> AllSeeds = new ArrayList<>();
        int[] S = new int[3];
       
        for (int r = 0; r < 3; r++) {
            BMTCcrosses.add(new ArrayList<>());
        }
        //for (int r = 0; r < 3; r++) {
        //    BMTCcrosses.get(r).clear();
        //    BMTZcrosses.get(r).clear();
        //}

        for (Cross bmt_cross : bmt_crosses) { 
            if (bmt_cross.getType()==BMTType.C) {// C-detector
                BMTCcrosses.get(bmt_cross.getRegion() - 1).add(bmt_cross); 
            }
        }

        AllSeeds.clear();

        for (int r = 0; r < 3; r++) {
            S[r] = BMTCcrosses.get(r).size();
            if (S[r] == 0) {
                S[r] = 1;
            }
            
        }

        for (int i1 = 0; i1 < S[0]; i1++) {
            for (int i2 = 0; i2 < S[1]; i2++) {
                for (int i3 = 0; i3 < S[2]; i3++) {

                    matches.clear();

                    if (BMTCcrosses.get(0).size() > 0 && i1 < BMTCcrosses.get(0).size()) {
                        if (this.passCcross(trkCand, BMTCcrosses.get(0).get(i1))) {
                            matches.add(BMTCcrosses.get(0).get(i1));
                        }
                    }
                    if (BMTCcrosses.get(1).size() > 0 && i2 < BMTCcrosses.get(1).size()) {
                        if (this.passCcross(trkCand, BMTCcrosses.get(1).get(i2))) {
                            matches.add(BMTCcrosses.get(1).get(i2));
                        }
                    }
                    if (BMTCcrosses.get(2).size() > 0 && i3 < BMTCcrosses.get(2).size()) {
                        if (this.passCcross(trkCand, BMTCcrosses.get(2).get(i3))) {
                            matches.add(BMTCcrosses.get(2).get(i3));
                        }
                    }
                    
                    matches.addAll(trkCand.getCrosses());
                    
                    if (matches.size() > 0) {
                        Seed BMTTrkSeed = new Seed();
                        
                        BMTTrkSeed.setHelix(trkCand.getHelix());
                        BMTTrkSeed.setCrosses(matches);
                        AllSeeds.add(BMTTrkSeed);
                        
                        //if (AllSeeds.size() > 200) {
                        //    AllSeeds.clear();
                        //    return AllSeeds;
                        //}
                        BMTTrkSeed = null;
                                
                    }
                }
            }
        }
        
        
        return AllSeeds;
    }

    private boolean passCcross(Seed trkCand, Cross bmt_Ccross) {
        boolean pass = false;
        double dzdrsum = trkCand.getHelix().getTanDip();

        double z_bmt = bmt_Ccross.getPoint().z();
        double r_bmt = Geometry.getInstance().getBMT().getRadius(bmt_Ccross.getCluster1().getLayer());
        
        Point3D refPoint = trkCand.getCrosses().get(0).getPoint(); 
        //if (bmt_geo.isInSector(bmt_Ccross.getCluster1().getLayer(), Math.atan2(refPoint.y(), refPoint.x()), Math.toRadians(10)) != bmt_Ccross.getSector()) {
        if (Geometry.getInstance().getBMT().getSector(bmt_Ccross.getCluster1().getLayer(), Math.atan2(refPoint.y(), refPoint.x())) != bmt_Ccross.getSector()) {
            return false;
        }
        double dzdr_bmt = z_bmt / r_bmt;
        if (Math.abs(1 - (dzdrsum / (double) (trkCand.getCrosses().size())) / ((dzdrsum + dzdr_bmt) / (double) (trkCand.getCrosses().size() + 1))) <= SVTParameters.DZDRCUT) // add this to the track
        { 
            pass = true;
        } 
        
        return pass;
    }

    private double calcResi(double r, double ri, double d, double f, double fi) {
        double res = 0.5*r*ri*ri - (1+r*d)*ri*Math.sin(f-fi)+0.5*r*d*d+d;
        return res;
    }

    private boolean InSamePhiRange(Seed seed, Cross c) {
        boolean pass = false;
        Cross s = seed.getCrosses().get(0);
        double seedPhi = Math.atan2(s.getPoint().y(), s.getPoint().x());
        double crossPhi = Math.atan2(c.getPoint().y(), c.getPoint().x());
        if(Math.abs(Math.toDegrees(seedPhi-crossPhi))<8)
            pass = true;
        return pass;
    }

}
