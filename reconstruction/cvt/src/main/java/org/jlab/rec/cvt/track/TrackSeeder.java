package org.jlab.rec.cvt.track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jlab.clas.swimtools.Swim;

import org.jlab.geom.prim.Point3D;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.fit.CircleFitter;
import org.jlab.rec.cvt.fit.CircleFitPars;
import org.jlab.rec.cvt.fit.HelicalTrackFitter;
import org.jlab.rec.cvt.svt.Constants;

public class TrackSeeder {
    public int NBINS = 36;

    public TrackSeeder() {
        
        sortedCrosses = new ArrayList<ArrayList<ArrayList<Cross>>>();
        
        for(int b =0; b<NBINS; b++) {
            sortedCrosses.add(b, new ArrayList<ArrayList<Cross>>() );
            for(int l =0; l<6; l++) {
                sortedCrosses.get(b).add(l,new ArrayList<Cross>() );
            }
        }
    }

    private List<ArrayList<Cross>> seedCrosses = new ArrayList<ArrayList<Cross>>();

    double[] phiShift = new double[]{0, 65}; // move the bin edge to handle bin boundaries

    public void FindSeedCrossList(List<Cross> crosses) {
        
        seedCrosses.removeAll(seedCrosses); 
        //for(int si1 = 0; si1<seedClusters.size(); si1++)
        //    seedCrosses.get(si1).clear();

        List<ArrayList<Cross>> phi0 = FindSeedCrossesFixedBin(crosses, phiShift[0]);       
        List<ArrayList<Cross>> phi90 = FindSeedCrossesFixedBin(crosses, phiShift[1]);
        
        if (phi0.size() > phi90.size()) {
            seedCrosses = phi0; 
        }
        if (phi90.size() > phi0.size()) {
            seedCrosses = phi90;
        }
        if (phi90.size() == phi0.size()) {
            for (int i = 0; i < phi0.size(); i++) {
                if (phi0.get(i).size() >= phi90.get(i).size()) {
                    seedCrosses.add(phi0.get(i));
                } else {
                    seedCrosses.add(phi90.get(i));
                }
            }
        }
    }
   
    List<ArrayList<ArrayList<Cross>>> sortedCrosses;
    
    public List<ArrayList<Cross>> FindSeedCrossesFixedBin(List<Cross> crosses, double phiShift) {
        for(int b =0; b<NBINS; b++) {
            for(int l =0; l<6; l++) {
                sortedCrosses.get(b).get(l).clear();
            }
        }
        List<ArrayList<Cross>> inseedCrosses = new ArrayList<ArrayList<Cross>>();
        int[][] LPhi = new int[NBINS][6];
        for (int i = 0; i < crosses.size(); i++) {
            double phi = Math.toDegrees(crosses.get(i).get_Point().toVector3D().phi());

            phi += phiShift;
            if (phi < 0) {
                phi += 360;
            }

            int binIdx = (int) (phi / (360./NBINS) );
            if(crosses.get(i).get_Detector().equalsIgnoreCase("BMT") ) { 
                sortedCrosses.get(binIdx).get(crosses.get(i).get_Region() - 1 + 3).add(crosses.get(i));
                LPhi[binIdx][crosses.get(i).get_Region() - 1 + 3]++; 
            }
            if(crosses.get(i).get_Detector().equalsIgnoreCase("SVT") ) {
                sortedCrosses.get(binIdx).get(crosses.get(i).get_Region() - 1).add(crosses.get(i));
                LPhi[binIdx][crosses.get(i).get_Region() - 1]++; 
            }
        }
        
        
        for (int b = 0; b < NBINS; b++) {
            int max_layers =0;
            int max_layersSVT =0;
            int max_layersBMT =0;
            for (int la = 0; la < 6; la++) { 
                if(LPhi[b][la]>0)
                    max_layers++;
            }
            // count for svt
            for (int la = 0; la < 3; la++) { 
                if(LPhi[b][la]>0){
                    max_layersSVT++;
                }
            }
            // count for bmt
            for (int la = 3; la < 6; la++) { 
                if(LPhi[b][la]>0){
                    max_layersBMT++;
                }
            }
            
            if (sortedCrosses.get(b) != null && max_layers>=3 && max_layersBMT >= 1) { 
                double SumLyr=0;
                while(LPhi[b][0]+LPhi[b][1]+ LPhi[b][2]+LPhi[b][3]+ LPhi[b][4]+ LPhi[b][5]>=max_layers) {
                    if(SumLyr!=LPhi[b][0]+LPhi[b][1]+ LPhi[b][2]+LPhi[b][3]+ LPhi[b][4]+ LPhi[b][5]) {
                        SumLyr = LPhi[b][0]+LPhi[b][1]+ LPhi[b][2]+LPhi[b][3]+ LPhi[b][4]+ LPhi[b][5];
                    } 
                    ArrayList<Cross> hits = new ArrayList<Cross>(); 
                    for (int la = 0; la < 6; la++) {
                        if (sortedCrosses.get(b).get(la) != null && LPhi[b][la]>0) { 
                            if (sortedCrosses.get(b).get(la).get(LPhi[b][la]-1) != null && sortedCrosses.get(b).get(la).size()>0) {
                                hits.add(sortedCrosses.get(b).get(la).get(LPhi[b][la]-1)); 
                                
                                if(LPhi[b][la]>1)
                                   LPhi[b][la]--; 
                                if(SumLyr==max_layers)
                                    LPhi[b][la]=0; 
                            }
                        }

                    }
                   
                    if (hits.size() >= 3) {
                        inseedCrosses.add(hits);
                    }
                    
                }
                
            }
        }
       
        return inseedCrosses;
    }

    

    
    private List<Double> Xs = new ArrayList<Double>();
    private List<Double> Ys = new ArrayList<Double>();
    private List<Double> Ws = new ArrayList<Double>();

    private List<Seed> BMTmatches = new ArrayList<Seed>();

    public List<Seed> findSeed(List<Cross> svt_crosses, List<Cross> bmt_crosses, 
            org.jlab.rec.cvt.svt.Geometry svt_geo, org.jlab.rec.cvt.bmt.Geometry bmt_geo,
            Swim swimmer) {
       
        List<Seed> seedlist = new ArrayList<Seed>();

        List<Cross> crosses = new ArrayList<Cross>();
        List<Cross> bmtC_crosses = new ArrayList<Cross>();
        
        crosses.addAll(svt_crosses);
        
        for(Cross c : bmt_crosses) { 
            if(c.get_DetectorType().equalsIgnoreCase("Z"))
                crosses.add(c);
            if(c.get_DetectorType().equalsIgnoreCase("C"))
                bmtC_crosses.add(c);
        }
        
        this.FindSeedCrossList(crosses);


        for ( List<Cross> seedcrs : seedCrosses ) {

          // loop until a good circular fit. removing far crosses each time
          boolean circlefitstatusOK = false;
          while( ! circlefitstatusOK && seedcrs.size()>=4 ){
            //System.out.println( " While loop on circle fit " );
            Xs.clear();
            Ys.clear();
            Ws.clear();
            ((ArrayList<Double>) Xs).ensureCapacity(seedcrs.size()+1);
            ((ArrayList<Double>) Ys).ensureCapacity(seedcrs.size()+1);
            ((ArrayList<Double>) Ws).ensureCapacity(seedcrs.size()+1);
            Xs.add(0, 0.0); 
            Ys.add(0, 0.0);
            Ws.add(0,0.1);
            int loopIdx = 1;
            for (Cross c : seedcrs ) { 
                if(c.get_DetectorType().equalsIgnoreCase("C") ) continue;
                Xs.add(loopIdx, c.get_Point().x()); 
                Ys.add(loopIdx, c.get_Point().y());
                Ws.add(loopIdx, 1. / (c.get_PointErr().x()*c.get_PointErr().x()+c.get_PointErr().y()*c.get_PointErr().y()));
                loopIdx++;
            }

            CircleFitter circlefit = new CircleFitter();
            circlefitstatusOK = circlefit.fitStatus(Xs, Ys, Ws, Xs.size());
            CircleFitPars pars = circlefit.getFit();

            // if not a good fit, check for outliers 
            if (!circlefitstatusOK ||  pars.chisq()/(double)(Xs.size()-3)>10) {
              //System.out.println(" check circular fit" );
              double d = pars.doca();
              double r = pars.rho();
              double f = pars.phi();
              // find the maximum residual
              double maxresiduals = 0.;
              for (Cross c : seedcrs ) { 
                if(c.get_DetectorType().equalsIgnoreCase("C") ) continue;
                  double xi = c.get_Point().x(); 
                  double yi = c.get_Point().y();
                  double unci = Math.sqrt(c.get_PointErr().x()*c.get_PointErr().x()+c.get_PointErr().y()*c.get_PointErr().y());
                  double ri = Math.sqrt(xi*xi+yi*yi);
                  double fi = Math.atan2(yi,xi) ;
                  double res = 0.5*r*ri*ri - (1+r*d)*ri*Math.sin(f-fi)+0.5*r*d*d+d;
                  res = res*res;
                  if( res > maxresiduals ) maxresiduals = res;
              }
              
              // remove the outlier
              for (Cross c : seedcrs ) { 
                if(c.get_DetectorType().equalsIgnoreCase("C") ) continue;
                  double xi = c.get_Point().x(); 
                  double yi = c.get_Point().y();
                  double unci = Math.sqrt(c.get_PointErr().x()*c.get_PointErr().x()+c.get_PointErr().y()*c.get_PointErr().y());
                  double ri = Math.sqrt(xi*xi+yi*yi);
                  double fi = Math.atan2(yi,xi) ;
                  double res = 0.5*r*ri*ri - (1+r*d)*ri*Math.sin(f-fi)+0.5*r*d*d+d;
                  res = res*res;
                  if( Math.abs(res - maxresiduals) < 1.e-3 ) {
                    //System.out.println(" remove detector " + c .get_Detector() + " region " + c.get_Region() + " sector " + c.get_Sector()  );
                    seedcrs.remove(c);
                    break;
                  }
              }
      
            }

          }
        }

        for (int s = 0; s < seedCrosses.size(); s++) {
          if( seedCrosses.get(s).size()<4 ){
      
            if( seedCrosses.get(s).size()<3 ){
              seedCrosses.remove(seedCrosses.get(s));
              continue;
            }
            boolean hasBMT = false;
            for( Cross c : seedCrosses.get(s) ){
              if( c.get_Detector().equalsIgnoreCase("BMT") ) hasBMT = true;
            }
            if( hasBMT == false){
              seedCrosses.remove(seedCrosses.get(s));
              continue;
            }
        
          }
        }

        for (int s = 0; s < seedCrosses.size(); s++) {
            //	if(seeds.get(s).size()<4)
            //	continue;
            
            Track cand = fitSeed(seedCrosses.get(s), svt_geo, 5, false, swimmer);
            if (cand != null) {
                Seed seed = new Seed();
                seed.set_Crosses(seedCrosses.get(s));
                seed.set_Helix(cand.get_helix());
                List<Cluster> clusters = new ArrayList<Cluster>();
                for(Cross c : seed.get_Crosses()) { 
                    if(c.get_Detector().equalsIgnoreCase("SVT")) {
                        clusters.add(c.get_Cluster1());
                        clusters.add(c.get_Cluster2());
                    } else {
                        clusters.add(c.get_Cluster1());
                    }
                }
                seed.set_Clusters(clusters);
                //match to BMT
                if (seed != null ) {
                    List<Cross> sameSectorCrosses = this.FindCrossesInSameSectorAsSVTTrk(seed, bmtC_crosses, bmt_geo);
                    BMTmatches.clear();
                    if (sameSectorCrosses.size() >= 0) {
                        BMTmatches = this.findCandUsingMicroMegas(seed, sameSectorCrosses, bmt_geo);
                    } 
                    
                    for (Seed bseed : BMTmatches) {
                        //refit using the BMT
                        Track bcand = fitSeed(bseed.get_Crosses(), svt_geo, 5, false, swimmer);
                        if (bcand != null) {
                            seed = new Seed();
                            seed.set_Crosses(bseed.get_Crosses());
                            seed.set_Clusters(bseed.get_Clusters());
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
        
        double jitter = Math.toRadians(10); // 10 degrees jitter
        for (int i = 0; i < bmt_crosses.size(); i++) { 
            Point3D pAtBMTSurf =seed.get_Helix().getPointAtRadius(org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[bmt_crosses.get(i).get_Region()-1]);
            // the hit parameters
            double angle = Math.atan2(pAtBMTSurf.y(), pAtBMTSurf.x());
            if (angle < 0) {
                angle += 2 * Math.PI;
            }
            if (bmt_geo.isInDetector(bmt_crosses.get(i).get_Region()*2-1, angle, jitter) == bmt_crosses.get(i).get_Sector() - 1) {
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
    float b[] = new float[3];
    
    public Track fitSeed(List<Cross> VTCrosses, org.jlab.rec.cvt.svt.Geometry svt_geo, int fitIter, 
            boolean originConstraint, Swim swimmer) {
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

            cand = new Track(null, swimmer);
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

            cand = new Track(fitTrk.get_helix(), swimmer);
            //cand.addAll(SVTCrosses);
            cand.addAll(SVTCrosses);
            
            cand.set_HelicalTrack(fitTrk.get_helix(), swimmer, b);
            //if(shift==0)
            if (fitTrk.get_chisq()[0] < chisqMax) {
                chisqMax = fitTrk.get_chisq()[0];
                if(chisqMax<Constants.CIRCLEFIT_MAXCHI2)
                    cand.update_Crosses(svt_geo);
                //i=fitIter;
            }
        }
        //System.out.println(" Seed fitter "+fitTrk.get_chisq()[0]+" "+fitTrk.get_chisq()[1]); 
        if(chisqMax>Constants.CIRCLEFIT_MAXCHI2)
            cand=null;
        return cand;
    }



    public List<Seed> findCandUsingMicroMegas(Seed trkCand,
        List<Cross> bmt_crosses, org.jlab.rec.cvt.bmt.Geometry bmt_geo) {
        List<ArrayList<Cross>> BMTCcrosses = new ArrayList<ArrayList<Cross>>();
        
        ArrayList<Cross> matches = new ArrayList<Cross>();
        List<Seed> AllSeeds = new ArrayList<Seed>();
        int[] S = new int[3];
       
        for (int r = 0; r < 3; r++) {
            BMTCcrosses.add(new ArrayList<Cross>());
        }
        //for (int r = 0; r < 3; r++) {
        //    BMTCcrosses.get(r).clear();
        //    BMTZcrosses.get(r).clear();
        //}

        for (Cross bmt_cross : bmt_crosses) { 
            if (bmt_cross.get_DetectorType().equalsIgnoreCase("C")) // C-detector
                BMTCcrosses.get(bmt_cross.get_Region() - 1).add(bmt_cross); 
            
           
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
                    
                    matches.addAll(trkCand.get_Crosses());
                    
                    if (matches.size() > 0) {
                        Seed BMTTrkSeed = new Seed();
                        
                        BMTTrkSeed.set_Helix(trkCand.get_Helix());
                        BMTTrkSeed.set_Crosses(matches);
                        BMTTrkSeed.set_Clusters(trkCand.get_Clusters());
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
