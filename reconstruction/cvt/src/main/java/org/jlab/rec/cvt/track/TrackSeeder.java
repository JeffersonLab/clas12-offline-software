package org.jlab.rec.cvt.track;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Point3D;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.Geometry;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.fit.CircleFitter;
import org.jlab.rec.cvt.fit.CircleFitPars;
import org.jlab.rec.cvt.svt.SVTParameters;

public class TrackSeeder {
    
    private final BMTGeometry bgeo = Geometry.getInstance().getBMT();
    private  double bfield;
    
    private final int NBINS = 36;
    private final double[] phiShift = new double[]{0, 65, 90}; // move the bin edge to handle bin boundaries
    private List<ArrayList<Cross>> scan ;
    private Map<Double, ArrayList<Cross>> seedMap ; // init seeds;
    private Map<Integer, Map<Integer, ArrayList<Cross>>> sortedCrosses ;
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
        sortedCrosses = new HashMap<>();
        
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
        Map<Integer, List<Cross>> crsMap = new HashMap<>();
        for (Seed seed : getSeedScan()) {
            crsMap.clear();
            double d = seed.getDoca();
            double r = seed.getRho();
            double f = seed.getPhi();

            for (Cross c : othercrs ) { 
                if(this.inSamePhiRange(seed, c)== true) {
                    int region = c.getRegion();
                    if(c.getDetector()!=DetectorType.BMT)
                        continue;
                    if(!crsMap.containsKey(region)) {
                        crsMap.put(region, new ArrayList<>());
                        crsMap.get(region).add(c);
                    } else {
                        crsMap.get(region).add(c);
                    }
                }
            }
            
            for(int rix = 0; rix<3; rix++) {
            
                if(crsMap.containsKey(rix+1)) {
                    Cross bestCross = null;
                    double bestRes = 999999;
                    for (Cross c : crsMap.get(rix+1) ) { 

                        double xi = c.getPoint().x(); 
                        double yi = c.getPoint().y();
                        double ri = Math.sqrt(xi*xi+yi*yi);
                        double fi = Math.atan2(yi,xi) ;

                        double res = this.calcResi(r, ri, d, f, fi);

                        if(Math.abs(res)<SVTParameters.RESIMAX && Math.abs(res)<bestRes) { 
                            bestCross = c;  
                            bestRes = Math.abs(res);
                        }
                    }
                    if(bestCross!=null)
                        seed.getCrosses().add(bestCross);
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
        getSeedScan().add(seed);
    }
    
    /*
    Finds SVT seeds
    */
    public void findSeedCrossList(List<Cross> crosses) {
        
        seedMap.clear();
        
        for(int si1 = 0; si1<scan.size(); si1++)
            scan.get(si1).clear();
        
        for(int i = 0; i< phiShift.length; i++) {
            findSeedCrossesFixedBin(crosses, phiShift[i]); 
        }
        if(Constants.getInstance().seedingDebugMode) {
            seedMap.forEach((key,value) -> this.printInfo(value));
        }
        seedMap.forEach((key,value) -> this.fitSeed(value));
    }
   
    
    private void findSeedCrossesFixedBin(List<Cross> crosses, double phiShift) {
        sortedCrosses.clear();
        int[][] LPhi = new int[NBINS][3];
        for (int i = 0; i < crosses.size(); i++) {
            crosses.get(i).reset();
            double phi = Math.toDegrees(crosses.get(i).getPoint().toVector3D().phi());

            phi += phiShift;
            if (phi < 0) {
                phi += 360;
            }

            int binIdx = (int) (phi / (360./NBINS) );
            if(binIdx>NBINS-1)
                binIdx = NBINS-1;
            if(!sortedCrosses.containsKey(binIdx)) {
                sortedCrosses.put(binIdx, new HashMap<Integer, ArrayList<Cross>>());
                sortedCrosses.get(binIdx).put(crosses.get(i).getRegion() - 1, new ArrayList<Cross>());
                sortedCrosses.get(binIdx).get(crosses.get(i).getRegion() - 1).add(crosses.get(i));
                LPhi[binIdx][crosses.get(i).getRegion() - 1]++; 
                
            } else {
                if(!sortedCrosses.get(binIdx).containsKey(crosses.get(i).getRegion() - 1)) {
                    sortedCrosses.get(binIdx).put(crosses.get(i).getRegion() - 1, new ArrayList<Cross>());
                    sortedCrosses.get(binIdx).get(crosses.get(i).getRegion() - 1).add(crosses.get(i));
                    LPhi[binIdx][crosses.get(i).getRegion() - 1]++; 
                } else {
                    sortedCrosses.get(binIdx).get(crosses.get(i).getRegion() - 1).add(crosses.get(i));
                    LPhi[binIdx][crosses.get(i).getRegion() - 1]++; 
                }
            }
        }
        
        
        for (int b = 0; b < NBINS; b++) {
            for (int la = 0; la < 3; la++) { 
                if(LPhi[b][la]==0) {
                    LPhi[b][la]=1;
                } 
            }
        }
        List<Cross> hits = new ArrayList<>(); 
        for (int b = 0; b < NBINS; b++) {
            if(!sortedCrosses.containsKey(b)) continue;
            
            for(int i1 = 0; i1< LPhi[b][0]; i1++) {
                for(int i2 = 0; i2< LPhi[b][1]; i2++) {
                    for(int i3 = 0; i3< LPhi[b][2]; i3++) {
                        hits.clear();
                        if(sortedCrosses.get(b).containsKey(0))
                            hits.add(sortedCrosses.get(b).get(0).get(i1));
                        if(sortedCrosses.get(b).containsKey(1))
                            hits.add(sortedCrosses.get(b).get(1).get(i2));
                        if(sortedCrosses.get(b).containsKey(2))
                            hits.add(sortedCrosses.get(b).get(2).get(i3));
                        if(hits.size()==3) {
                            if(this.checkZ((ArrayList<Cross>) hits)) {
                                this.addToSeedMap((ArrayList<Cross>) hits);
                            }
                        } else if(hits.size()==2) {
                            this.addToSeedMap((ArrayList<Cross>) hits);
                        }
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
        
        for(Seed mseed : getSeedScan()) { 
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
                    mseed.setDoca(d);
                    mseed.setRho(r);
                    mseed.setPhi(f);
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


        for(Seed mseed : getSeedScan()) { 
            boolean fitStatus = false;
            if(mseed.getCrosses().size()>2) {
                fitStatus = mseed.fit(Constants.SEEDFITITERATIONS, xbeam, ybeam, bfield);
            }
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

        
        if(!seedlist.isEmpty()) {
            // remove overlapping seeds
            if(Constants.getInstance().removeOverlappingSeeds)
                Seed.removeOverlappingSeeds(seedlist);
            //if(Constants.getInstance().flagSeeds)
            //        Seed.flagMCSeeds(seedlist);
            for (Seed bseed : seedlist) { 
                for(Cross c : bseed.getCrosses()) {
                    c.isInSeed = true;
                }
                bseed.setStatus(2);
            }
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

    List<ArrayList<Cross>> BMTCcrosses = new ArrayList<>();
    Map<String, Seed> AllSeeds = new HashMap<>();
    public List<Seed> findCandUsingMicroMegas(Seed trkCand, List<Cross> bmt_crosses) {
        
        BMTCcrosses.clear();
        AllSeeds.clear();
        
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
        
        ArrayList<Cross> matches = new ArrayList<>();

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
        boolean value = false;
        double angle =Math.toDegrees(seed.getCrosses().get(seed.getCrosses().size()-1).getPoint().toVector3D().angle(c.getPoint().toVector3D()));
        
        if(Math.abs(angle)<45) 
            value = true;
        return value;
    }

    private void printInfo(ArrayList<Cross> value) {
        System.out.println("SVTSTANDALONE SEED:"); 
        for(Cross c : value) {
            System.out.println(c.printInfo());
        }
        
    }

    private void addToSeedMap(ArrayList<Cross> hitlist) {
        ArrayList<Cross> hits = new ArrayList<>();
        double seedIdx=0;
        int s = hitlist.size();
        int index = (int) Math.pow(2,s);
        for(Cross c : hitlist) {
            hits.add(c);
            seedIdx +=c.getId()*Math.pow(10, index);
            index-=4;
        }
        
        seedMap.put(seedIdx, hits);    
    }
    private boolean checkZ(ArrayList<Cross> hits) {
        boolean value = true;
        if(hits.get(0).getDetector()==DetectorType.BST && hits.get(1).getDetector()==DetectorType.BST && hits.get(2).getDetector()==DetectorType.BST ) {
             value = false;
        } else {
            return value;
        }
        Cross c1 = hits.get(0);
        Cross c2 = hits.get(1);
        Cross c3 = hits.get(2);
        double sl = (c1.getPoint().z() - c3.getPoint().z())/(c1.getPoint().toVector3D().rho() - c3.getPoint().toVector3D().rho());
        double in = -sl*c1.getPoint().toVector3D().rho()+c1.getPoint().z();
        
        double Rm = c2.getPoint().toVector3D().rho();
        double Zm = c2.getPoint().z();
        double Zc = sl*Rm +in;
        double Zerr = c2.getPointErr().z(); 
        if(Math.abs(Zc-Zm)<Zerr*20) {
            value = true;  
        }
        return value;
    }

    /**
     * @return the seedScan
     */
    public List<Seed> getSeedScan() {
        return seedScan;
    }

    /**
     * @param seedScan the seedScan to set
     */
    public void setSeedScan(List<Seed> seedScan) {
        this.seedScan = seedScan;
    }
}
