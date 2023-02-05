package org.jlab.rec.cvt.track;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.detector.base.DetectorType;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.fit.CircleFitter;
import org.jlab.rec.cvt.fit.CircleFitPars;

public class TrackSeederXY {
    
    
    private final int NBINS = 36;
    private final double[] phiShift = new double[]{0,65,90}; // move the bin edge to handle bin boundaries
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
    
    public TrackSeederXY(double xb, double yb) {
        
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
        
        Seed seed = new Seed(seedcrs, d, r, f);
        seedScan.add(seed);
    }
    
    /*
    Finds xy seeds
    */
    public void findSeedCrossList(List<Cross> crosses) {
        scan.clear();
        seedMap.clear();
        seedScan.clear();
        
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
           
            if(binIdx>NBINS-1)
                binIdx = NBINS-1;
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

    public List<Seed> findSeed(List<Cross> cvt_crosses) {
        
        List<Seed> seedlist = new ArrayList<>();

        List<Cross> crosses = new ArrayList<>();
        
        if(cvt_crosses!=null && cvt_crosses.size()>0) {
            for(Cross c : cvt_crosses) { 
                if(c.getType()!=BMTType.C) { 
                    crosses.add(c);
                }
            }
        }
        
        this.findSeedCrossList(crosses);
        for(Seed mseed : seedScan) { 
            this.CircleFitSeed(mseed); 
        }
        
        for(Seed mseed : seedScan) { 
            CircleFitPars fpars = null;
            
            if(mseed.getCrosses().size()>=2) {
                fpars =
                        this.CircleFit(mseed.getCrosses());
               
                if (fpars!=null) { 
                    this.addMissingCrosses(mseed.getCrosses(),crosses, fpars);
                    fpars = this.CircleFit(mseed.getCrosses());
                    double d = fpars.doca();
                    double r = fpars.rho();
                    double f = fpars.phi();
                    //this.removeOutliers(mseed, r, d, f); System.out.println("rmoutliers fit "); for(Cross c : mseed.getCrosses()) System.out.println(c.printInfo());
                    fpars = this.CircleFit(mseed.getCrosses());
                }
            }
            if (fpars!=null && this.countType(mseed.getCrosses(), DetectorType.BST)>0 && mseed.getCrosses().size()>2) {
                seedlist.add(mseed);
            }
        }
        
        //update the cluster list
        for (Seed bseed : seedlist) { 
            if(Constants.getInstance().seedingDebugMode) {
                System.out.println("XY seed");
                System.out.println(bseed.toString());
            }
            bseed.getClusters().clear();
            for(Cross c : bseed.getCrosses()) {
                //c.isInSeed = true;
                bseed.getClusters().add(c.getCluster1());
                if(c.getCluster2()!=null)
                    bseed.getClusters().add(c.getCluster2());
            }
            //bseed.setStatus(3);
        }
       
        return seedlist;
    }
    
    private int countType(List<Cross> cand, DetectorType dt) {
        int countsvt=0;
        for(Cross c : cand) 
            if(c.getDetector()==dt)
                countsvt++;
        
        return countsvt;
    }
    private double calcResi(double rho, double d0, double phi0, double xc, double yc) {
        double r = Math.sqrt(xc*xc+yc*yc);
        double par = 1. - ((r * r - d0 * d0) * rho * rho) / (2. * (1. + d0 * Math.abs(rho)));
        double newPathLength = Math.abs(Math.acos(par) / rho);
        int charge = (int) Math.signum(rho);
        double alpha = -newPathLength * rho;

        double x = d0 * charge * Math.sin(phi0) + (charge / Math.abs(rho)) 
                * (Math.sin(phi0) - Math.cos(alpha) * Math.sin(phi0) - Math.sin(alpha) * Math.cos(phi0));
        double y = -d0 * charge * Math.cos(phi0) - (charge / Math.abs(rho)) 
                * (Math.cos(phi0) + Math.sin(alpha) * Math.sin(phi0) - Math.cos(alpha) * Math.cos(phi0));
       
        double res2 = ((x-xc)*(x-xc)+(y-yc)*(y-yc));
        return Math.sqrt(res2);
    }


    private CircleFitPars CircleFit(List<Cross> seedcrs) {

        // loop until a good circular fit. removing far crosses each time
        boolean circlefitstatusOK = false;
       
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
        if (!circlefitstatusOK )
            return null;
        return pars;        
    }
    
    private void CircleFitSeed(Seed mseed) {
        List<Cross> seedcrs = mseed.getCrosses();
        CircleFitPars pars = null;
        int NbIter =3;
        for(int i =0; i<NbIter; i++) {
            pars = this.CircleFit(seedcrs);
            if(pars==null) {
                seedcrs.clear();
                return;
            }
            
            //if (pars.chisq()/(double)(Xs.size())>10 && seedcrs.size()>3) {
                //System.out.println(" check circular fit " +circlefitstatusOK+" c2 "+pars.chisq());
                double d = pars.doca();
                double r = pars.rho();
                double f = pars.phi();
                if(seedcrs.size()>3)
                    this.removeOutliers(mseed, r, d, f);
                
            //}
        }
    }
    private void removeOutliers(Seed seed, double r, double d, double f) {
        Cross w =null;
        double worseResi =-999;
        List<Cross> seedcrsrm = new ArrayList<>();
        List<Cross> seedcrs = seed.getCrosses();
        for (Cross c : seedcrs) {
            double xi = c.getPoint().x();
            double yi = c.getPoint().y();
            double res = this.calcResi(r, d, f, xi, yi);
            if(res>worseResi) {
               worseResi = res;
               w=c;
            }
        }
        
        if (Math.abs(worseResi) > Constants.RESICUT ) {
           
            seedcrsrm.add(w);
        }
        seedcrs.removeAll(seedcrsrm);
        
    }

    private void addMissingCrosses(List<Cross> seedcrs, List<Cross> crosses, CircleFitPars pars) {
        double d = pars.doca();
        double r = pars.rho();
        double f = pars.phi();
        double ave_resi =0;
        int[] L = new int[6];
        for (Cross c : seedcrs) {
            double xi = c.getPoint().x();
            double yi = c.getPoint().y();
            double res = this.calcResi(r, d, f, xi, yi); 
            ave_resi+=res;
            int rg = c.getRegion();
            if(c.getDetector() == DetectorType.BMT)
                rg+=3;
            L[rg-1] = 1;
        }
        ave_resi/=seedcrs.size(); 
        double[] Rs = new double[]{9999,9999,9999,9999,9999,9999};
        Cross[] Cs = new Cross[6];
    
        for (Cross c : crosses) {
            int rg = c.getRegion();
            if(c.getDetector() == DetectorType.BMT)
                rg+=3;
            
            if(L[rg-1] ==0) { //missing layer --> search for cross
                
                double xi = c.getPoint().x();
                double yi = c.getPoint().y();
                double res = this.calcResi(r, d, f, xi, yi); 
                if(res<Rs[rg-1] && (res<Constants.RESICUT || res<ave_resi) ){
                    Rs[rg-1] = res;
                    Cs[rg-1] = c; 
                }
            }
        }
        for(int i = 0; i<6; i++) {
            if(Rs[i]<9999) {
                if(Cs[i]!=null) {
                    seedcrs.add(Cs[i]);
                    
                }
            }
        }
    }
}
