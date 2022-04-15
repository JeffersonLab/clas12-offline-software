package org.jlab.rec.cvt.track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.clas.swimtools.Swim;

import org.jlab.geom.prim.Point3D;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.fit.CircleFitter;
import org.jlab.rec.cvt.fit.CircleFitPars;
import org.jlab.rec.cvt.svt.SVTGeometry;
import org.jlab.rec.cvt.svt.SVTParameters;

public class TrackSeeder {
    
    private final SVTGeometry sgeo = Constants.getInstance().SVTGEOMETRY;
    private final BMTGeometry bgeo = Constants.getInstance().BMTGEOMETRY;
    private  double bfield;
    
    private final int NBINS = 36;
    private final double[] phiShift = new double[]{0, 65, 90}; // move the bin edge to handle bin boundaries
    private List<ArrayList<Cross>> scan ;
    private Map<Double, ArrayList<Cross>> seedMap ; // init seeds;
    private List<ArrayList<ArrayList<Cross>>> sortedCrosses;
    private List<Seed> seedScan ;
    private List<Double> Xs ;
    private List<Double> Ys ;
    private List<Double> Ws ;
    private double xbeam;
    private double ybeam;
    public boolean unUsedHitsOnly = false;
    
    public TrackSeeder(Swim swimmer, double xb, double yb) {
        float[] b = new float[3];
        swimmer.BfieldLab(0, 0, 0, b);
        this.bfield = Math.abs(b[2]);

        //init lists for scan
        sortedCrosses = new ArrayList<>();
        for(int i =0; i<NBINS; i++) {
            sortedCrosses.add(i, new ArrayList<>() );
            for(int l =0; l<3; l++) {
                sortedCrosses.get(i).add(l,new ArrayList<>() );
            }
        }
        scan = new ArrayList<>();
        seedMap = new HashMap<>(); // init seeds;
        seedScan = new ArrayList<>();
        //for fitting
        Xs = new ArrayList<>();
        Ys = new ArrayList<>();
        Ws = new ArrayList<>();
        xbeam = xb;
        ybeam = yb;
    }
    
    private void matchSeed(List<Cross> othercrs) {
        if(othercrs==null || othercrs.isEmpty())
            return;
        
        for (Seed seed : seedScan) {
            double d = seed.getDoca();
            double r = seed.getRho();
            double f = seed.getPhi();

            for (Cross c : othercrs ) { 
                if(this.inSamePhiRange(seed, c)== true) {
                    double xi = c.getPoint().x(); 
                    double yi = c.getPoint().y();
                    double ri = Math.sqrt(xi*xi+yi*yi);
                    double fi = Math.atan2(yi,xi) ;

                    double res = this.calcResi(r, ri, d, f, fi);
                    if(Math.abs(res)<SVTParameters.RESIMAX) { 
                        // add to seed    
                        seed.getCrosses().add(c);
                    }
                }
            }
        }
    }
    
    public void fitSeed(List<Cross> seedcrs) {
        Xs.clear();
        Ys.clear();
        Ws.clear();
        ((ArrayList<Double>) Xs).ensureCapacity(seedcrs.size()+1);
        ((ArrayList<Double>) Ys).ensureCapacity(seedcrs.size()+1);
        ((ArrayList<Double>) Ws).ensureCapacity(seedcrs.size()+1);
        Xs.add(0, xbeam); 
        Ys.add(0, ybeam);
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
        double r = pars.rho();
        double f = pars.phi();
        
        boolean failed = false;
        for (Cross c : seedcrs ) { 
            double xi = c.getPoint().x(); 
            double yi = c.getPoint().y();
            double ri = Math.sqrt(xi*xi+yi*yi);
            double fi = Math.atan2(yi,xi) ;
            
            double res = this.calcResi(r, ri, d, f, fi);
            if(Math.abs(res)>SVTParameters.RESIMAX) { 
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
        
        seedMap.forEach((key,value) -> this.fitSeed(value));
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
                    ArrayList<Cross> hits = new ArrayList<>(); 
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
    public List<Seed> findSeed(List<Cross> bst_crosses, List<Cross> bmt_crosses) {
        
        List<Seed> seedlist = new ArrayList<>();

        List<Cross> crosses = new ArrayList<>();
        List<Cross> svt_crosses = new ArrayList<>();
        List<Cross> bmtC_crosses = new ArrayList<>();
        
        if(bmt_crosses!=null && bmt_crosses.size()>0) {
            for(Cross c : bmt_crosses) { 
                if(c.getType()==BMTType.Z) { 
                    if(this.unUsedHitsOnly == false) {
                        crosses.add(c);
                    } else {
                        if(this.unUsedHitsOnly == true && c.isInSeed == false) {
                            crosses.add(c);
                        }
                    }
                }
                if(c.getType()==BMTType.C) {
                    if(this.unUsedHitsOnly == false) {
                        bmtC_crosses.add(c);
                    } else {
                        if(this.unUsedHitsOnly == true && c.isInSeed == false) {
                            bmtC_crosses.add(c);
                        }
                    }
                }
            }
        }
        if(bst_crosses!=null && bst_crosses.size()>0) {
            for(Cross c : bst_crosses) { 
                if(this.unUsedHitsOnly == false) {
                        svt_crosses.add(c);
                } else {
                    if(this.unUsedHitsOnly == true && c.isInSeed == false) {
                        svt_crosses.add(c);
                    }
                }
            }
        }
        this.findSeedCrossList(svt_crosses);
        this.matchSeed(crosses);
        
        for(Seed mseed : seedScan) { 
            List<Cross> seedcrs = mseed.getCrosses();
   
            // loop until a good circular fit. removing far crosses each time
            boolean circlefitstatusOK = false;
            while( ! circlefitstatusOK && seedcrs.size()>=3 ){
            
                Xs.clear();
                Ys.clear();
                Ws.clear();
                ((ArrayList<Double>) Xs).ensureCapacity(seedcrs.size()+1);
                ((ArrayList<Double>) Ys).ensureCapacity(seedcrs.size()+1);
                ((ArrayList<Double>) Ws).ensureCapacity(seedcrs.size()+1);
                Xs.add(0, xbeam); 
                Ys.add(0, ybeam);
                Ws.add(0, 0.1);
                for (Cross c : seedcrs ) { 
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
                    for (Cross c : seedcrs) {
                        double xi = c.getPoint().x();
                        double yi = c.getPoint().y();
                        double ri = Math.sqrt(xi * xi + yi * yi);
                        double fi = Math.atan2(yi, xi);
                        double res = this.calcResi(r, ri, d, f, fi);
                        if (Math.abs(res) > SVTParameters.RESIMAX) {
                            //System.out.println(" remove detector " + c .getDetector() + " region " + c.getRegion() + " sector " + c.getSector()  );
                            seedcrs.remove(c);
                            break;
                        }
                    }
                }
            }
        }


        for(Seed mseed : seedScan) { 
            boolean fitStatus = false;
            if(mseed.getCrosses().size()>=3)
                fitStatus = mseed.fit(Constants.SEEDFITITERATIONS, xbeam, ybeam, bfield);
            if (fitStatus) { 
                List<Cross> sameSectorCrosses = this.findCrossesInSameSectorAsSVTTrk(mseed, bmtC_crosses);
                BMTmatches.clear();
                if (sameSectorCrosses.size() >= 0) {
                    BMTmatches = this.findCandUsingMicroMegas(mseed, sameSectorCrosses);
                } 
                
                Seed bestSeed = null;
                double chi2_Circ = Double.POSITIVE_INFINITY;
                double chi2_Line = Double.POSITIVE_INFINITY;
                for (Seed bseed : BMTmatches) {
                    //refit using the BMT
                    fitStatus = bseed.fit(Constants.SEEDFITITERATIONS, xbeam, ybeam, bfield);

                    if (fitStatus && bseed.getCircleFitChi2PerNDF()<chi2_Circ
                                  && bseed.getLineFitChi2PerNDF()<chi2_Line
                                  && bseed.isGood()) {
                        bestSeed  = bseed;
                        chi2_Circ = bseed.getCircleFitChi2PerNDF();
                        chi2_Line = bseed.getLineFitChi2PerNDF();
                    }
                }
                if(bestSeed!= null) seedlist.add(bestSeed);
            }
        }
       
        // remove overlapping seeds
        Seed.removeOverlappingSeeds(seedlist);
        
        for (Seed bseed : seedlist) { 
            for(Cross c : bseed.getCrosses()) {
                c.isInSeed = true;
            }
            bseed.setStatus(2);
        }
        return seedlist;
    }
    

    private List<Cross> findCrossesInSameSectorAsSVTTrk(Seed seed, List<Cross> bmt_crosses) {
        List<Cross> bmt_crossesInSec = new ArrayList<Cross>();
        //double angle_i = 0; // first angular boundary init
        //double angle_f = 0; // second angular boundary for detector A, B, or C init
        
        double jitter = Math.toRadians(10); // 10 degrees jitter
        for (int i = 0; i < bmt_crosses.size(); i++) { 
            Point3D pAtBMTSurf =seed.getHelix().getPointAtRadius(bgeo.getRadius(bmt_crosses.get(i).getCluster1().getLayer()));
            // the hit parameters
            double angle = Math.atan2(pAtBMTSurf.y(), pAtBMTSurf.x());
            if (angle < 0) {
                angle += 2 * Math.PI;
            }
            //if (bmt_geo.isInDetector(bmt_crosses.get(i).getRegion()*2-1, angle, jitter) 
            //        == bmt_crosses.get(i).getSector() - 1) 
            //inDetector(int layer, int sector, Point3D traj) 
            if (bgeo.inDetector(bmt_crosses.get(i).getRegion()*2-1, bmt_crosses.get(i).getSector(), pAtBMTSurf)==true){
                bmt_crossesInSec.add(bmt_crosses.get(i)); 
            }
            
        }

        return bmt_crossesInSec;
    }

    public List<Seed> findCandUsingMicroMegas(Seed trkCand, List<Cross> bmt_crosses) {
        List<ArrayList<Cross>> BMTCcrosses = new ArrayList<>();
        
        Map<String, Seed> AllSeeds = new HashMap<>();
        int[] S = new int[3];
       
        for (int r = 0; r < 3; r++) {
            BMTCcrosses.add(new ArrayList<>());
        }
        //for (int r = 0; r < 3; r++) {
        //    BMTCcrosses.get(r).clear();
        //    BMTZcrosses.get(r).clear();
        //}

        for (Cross bmt_cross : bmt_crosses) { 
            if (bmt_cross.getType()==BMTType.C) // C-detector
                BMTCcrosses.get(bmt_cross.getRegion() - 1).add(bmt_cross); 
        }

        for (int r = 0; r < 3; r++) {
            S[r] = BMTCcrosses.get(r).size();
            if (S[r] == 0) {
                S[r] = 1;
            }
            
        }

        for (int i1 = 0; i1 < S[0]; i1++) {
            for (int i2 = 0; i2 < S[1]; i2++) {
                for (int i3 = 0; i3 < S[2]; i3++) {

                    ArrayList<Cross> matches = new ArrayList<>();

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
                        String st = "";
                        for(Cross c : matches)
                            st+=c.getId();
                        BMTTrkSeed.setHelix(trkCand.getHelix());
                        BMTTrkSeed.setCrosses(matches);
                        AllSeeds.put(st,BMTTrkSeed);
         
                        //if (AllSeeds.size() > 200) {
                        //    AllSeeds.clear();
                        //    return AllSeeds;
                        //}
                    }
                }
            }
        }
        
        List<Seed> outputSeeds = new ArrayList<>();
        for(Seed s : AllSeeds.values())
            outputSeeds.add(s);
        
        return outputSeeds;
    }

    private boolean passCcross(Seed trkCand, Cross bmt_Ccross) {
        boolean pass = false;

        double dzdrsum = trkCand.getHelix().getTanDip();

        double z_bmt = bmt_Ccross.getPoint().z();
        double r_bmt = bgeo.getRadius(bmt_Ccross.getCluster1().getLayer());
        Point3D phiHelixAtSurf = trkCand.getHelix().getPointAtRadius(r_bmt); 
        //if (bmt_geo.isInSector(bmt_Ccross.getCluster1().getLayer(), Math.atan2(phiHelixAtSurf.y(), phiHelixAtSurf.x()), Math.toRadians(10)) 
        //        != bmt_Ccross.getSector()) 
        int sector = bgeo.getSector(bmt_Ccross.getCluster1().getLayer(), Math.atan2(phiHelixAtSurf.y(), phiHelixAtSurf.x()));
        if(sector!= bmt_Ccross.getSector() || sector ==0){
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

    private boolean inSamePhiRange(Seed seed, Cross c) {
        return true;
    }

}
