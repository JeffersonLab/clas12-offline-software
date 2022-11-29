package org.jlab.rec.cvt.track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.Geometry;
import org.jlab.rec.cvt.bmt.BMTType;

import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.fit.HelicalTrackFitter;
import org.jlab.rec.cvt.fit.HelicalTrackFitter.FitStatus;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.rec.cvt.trajectory.Helix;

/**
 *
 * @author ziegler
 *
 */
public class Seed implements Comparable<Seed>{


    private int id;
    private int status;
    private double doca;
    private double rho;
    private double phi;
    private Helix _Helix;
    private List<Cross> _Crosses;
    private List<Cluster> _Clusters;
    private double _circleFitChi2PerNDF;	// the chi2 for the helical track circle fit
    private double _lineFitChi2PerNDF;   	// the linear fit to get the track dip angle
    private int    _NDF;
    private double _chi2;
    private List<Seed> _overlappingSeed;
    private Key _key;
    public double percentTruthMatch;
    public double totpercentTruthMatch;
    public int FirstPassIdx;
    
    public Seed() {
    }

    /**
     * @return the _key
     */
    public Key getKey() {
        return _key;
    }

    /**
     * @param _key the _key to set
     */
    public void setKey(Key _key) {
        this._key = _key;
    }

    public Seed(List<Cross> crosses) {
        this.setCrosses(crosses);
        this.setKey(new Key(this));
    }

    public Seed(List<Cross> crosses, double doca, double rho, double phi) {
        this.setCrosses(crosses);
        this.setDoca(doca);
        this.setRho(rho);
        this.setPhi(phi);
        this.setKey(new Key(this));
    }

    public Seed(List<Cross> crosses, Helix helix) {
        this.setCrosses(crosses);
        this.setHelix(helix);
        this.setKey(new Key(this));
    }

    /**
     * @return the _overlappingSeed
     */
    public List<Seed> getOverlappingSeeds() {
        return _overlappingSeed;
    }

    /**
     * @param _overlappingSeed the _overlappingSeed to set
     */
    public void setOverlappingSeed(List<Seed> _overlappingSeed) {
        this._overlappingSeed = _overlappingSeed;
    }

    public Helix getHelix() {
        return _Helix;
    }

    public final void setHelix(Helix helix) {
        this._Helix = helix;
        this.doca = helix.getDCA();
        this.rho  = helix.getCurvature();
        this.phi  = helix.getPhiAtDCA();
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStatus(int trkStatus) {
        this.status = trkStatus;
    }

    public final void setDoca(double doca) {
        this.doca = doca;
    }

    public final void setRho(double rho) {
        this.rho = rho;
    }

    public final void setPhi(double phi) {
        this.phi = phi;
    }

    public int getId() {
        return id;
    }

    public int getStatus() {
        return status;
    }

    public double getDoca() {
        return this.doca;
    }
    
    public double getRho() {
        return this.rho;
    }
    
    public double getPhi() {
        return this.phi;
    }
        
    private void setClusters() { 
        List<Cluster> clusters = new ArrayList<>(); 
        for(Cross c : this.getCrosses()) { 
	    if(c.getDetector()==DetectorType.BST) {
                clusters.add(c.getCluster1());
                clusters.add(c.getCluster2());
            } else {
                clusters.add(c.getCluster1());
            }
      	}
        Collections.sort(clusters);
        _Clusters = clusters;
    }

    public List<Cluster> getClusters() {
        return _Clusters;
    }

    public void add_Clusters(List<Cluster> clusters) {
        this._Clusters.addAll(clusters);
        Collections.sort(_Clusters);
    }

    public List<Cross> getCrosses() {
        Collections.sort(_Crosses);
        return _Crosses;
    }

    public final void setCrosses(List<Cross> _Crosses) {
        Collections.sort(_Crosses);
        this._Crosses = _Crosses;
        this.setClusters();
    }

    public double getCircleFitChi2PerNDF() {
        return _circleFitChi2PerNDF;
    }

    public void setCircleFitChi2PerNDF(double _circleFitChi2PerNDF) {
        this._circleFitChi2PerNDF = _circleFitChi2PerNDF;
    }

    public double getLineFitChi2PerNDF() {
        return _lineFitChi2PerNDF;
    }

    public void setLineFitChi2PerNDF(double _lineFitChi2PerNDF) {
        this._lineFitChi2PerNDF = _lineFitChi2PerNDF;
    }

    public int getNDF() {
        return _NDF;
    }
    
    public void setNDF(int ndf) {
        this._NDF= ndf;
    }

    public double getChi2() {
        return _chi2;
    }

    public void setChi2(double _chi2) {
        this._chi2 = _chi2;
    }

    public String getIntIdentifier() {
        
        String id = "";
        for(Cluster c: this.getClusters())
            id+=c.getId();
        for(Cross c: this.getCrosses())
            id+=c.getId();
       
        return id;
    }
    public int sortingMethod = 0;
    
    @Override
    public int compareTo(Seed tr) {
        int value;
        if(tr.sortingMethod ==0) {
            //sort by NDF and chi2
            int ProbComp  = this.getChi2() < tr.getChi2() ? -1 : this.getChi2() == tr.getChi2() ? 0 : 1;
            int OtherComp = this.getNDF() > tr.getNDF() ? -1 : this.getNDF() == tr.getNDF() ? 0 : 1;
            value = ((OtherComp == 0) ? ProbComp : OtherComp);
        } 
        else if(tr.sortingMethod ==1) {
            return this.totpercentTruthMatch > tr.totpercentTruthMatch ? -1 : this.totpercentTruthMatch == tr.totpercentTruthMatch ? 0 : 1;
        } else {
            int[] K = new int[9];
            for(int k = 0; k<9; k++) {
                K[k] = this.getKey().crossIds[k] < tr.getKey().crossIds[k] ? -1 : this.getKey().crossIds[k] == tr.getKey().crossIds[k] ? 0 : 1;
            }
            
            int[] R = new int[8];
            R[0] = ((K[0] == 0) ? K[1] : K[0]);
            for(int k = 0; k<7; k++) {
                R[k+1] = ((R[k] == 0) ? K[k+2] : R[k]);
            }
            
            value = R[7];
        }
        
        return value;
    }

    public boolean fit(int fitIter, double xb, double yb, double bfield) {
        
        List<Double> X = new ArrayList<>();
        List<Double> Y = new ArrayList<>();
        List<Double> Z = new ArrayList<>();
        List<Double> Rho = new ArrayList<>();
        List<Double> ErrZ = new ArrayList<>();
        List<Double> ErrRho = new ArrayList<>();
        List<Double> ErrRt = new ArrayList<>();
        List<Cross> BMTCrossesC = new ArrayList<>();
        List<Cross> BMTCrossesZ = new ArrayList<>();
        List<Cross> SVTCrosses = new ArrayList<>();
        
        double chisqMax = Double.POSITIVE_INFINITY;

        int svtSz = 0;
        int bmtZSz = 0;
        int bmtCSz = 0;
        
        for (Cross c : this.getCrosses()) {
            // reset cross to clear previous track settings on direction and Point
            c.reset();
            if (c.getDetector()==DetectorType.BST) {
                SVTCrosses.add(c);
            }
            else if (c.getDetector()==DetectorType.BMT && c.getType()==BMTType.C ) {
                BMTCrossesC.add(c);
            }
            else if (c.getDetector()==DetectorType.BMT && c.getType()==BMTType.Z ) {
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
         
        HelicalTrackFitter fitTrk = new HelicalTrackFitter();
        for (int i = 0; i < fitIter; i++) {
            X.clear();
            Y.clear();
            Z.clear();
            Rho.clear();
            ErrZ.clear();
            ErrRho.clear();
            ErrRt.clear();
            
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
            X.add(xb);
            Y.add(yb);
            ErrRt.add((double) 0.1);
            
            FitStatus fitStatus = fitTrk.fit(X, Y, Z, Rho, ErrRt, ErrRho, ErrZ, xb, yb);
            
            if (fitStatus!=FitStatus.Successful || fitTrk.gethelix() == null) { 
                return false;
            }

            fitTrk.gethelix().B = bfield;
            
            this.setHelix(fitTrk.gethelix());
            if( X.size()>3 ) {
            	this.setCircleFitChi2PerNDF(fitTrk.getchisq()[0]/(X.size()-3));
            }
            else { 
            	this.setCircleFitChi2PerNDF(fitTrk.getchisq()[0]*2); // penalize tracks with only 3 crosses 
            }
            
            if( Z.size() > 2 ) {
            	this.setLineFitChi2PerNDF(fitTrk.getchisq()[1]/(Z.size()-2));
            }
            else {
            	this.setLineFitChi2PerNDF(fitTrk.getchisq()[1]*2);// penalize tracks with only 2 crosses
            }
            this.setChi2(fitTrk.getchisq()[0]+fitTrk.getchisq()[1]);
            this.setNDF(X.size()+Z.size()-5);
            
            if (fitTrk.getchisq()[0] < chisqMax) {
                chisqMax = fitTrk.getchisq()[0];
                if(chisqMax<Constants.CIRCLEFIT_MAXCHI2) {
                    this.update_Crosses();
                }
            }
        }
        return true;
    }

    
    /**
     * Updates the crosses positions based on trajectories or helix
     */
    public void update_Crosses() {
        if (this.getHelix() != null && this.getHelix().getCurvature() != 0) {
            for (int i = 0; i < this.getCrosses().size(); i++) {
                Cross cross = this.getCrosses().get(i);
                double R = Math.sqrt(cross.getPoint().x() * cross.getPoint().x() + cross.getPoint().y() * cross.getPoint().y());
                Point3D  trackPos = this.getHelix().getPointAtRadius(R);
                Vector3D trackDir = this.getHelix().getTrackDirectionAtRadius(R);
                cross.update(trackPos, trackDir);
            }
        }
    }
    
    public void update_Crosses(List<Cross> crosses) {
        if (this.getHelix() != null && this.getHelix().getCurvature() != 0) {
            for (int i = 0; i < crosses.size(); i++) {
                Cross cross = crosses.get(i);
                double R = Math.sqrt(cross.getPoint().x() * cross.getPoint().x() + cross.getPoint().y() * cross.getPoint().y());
                Point3D  trackPos = this.getHelix().getPointAtRadius(R);
                Vector3D trackDir = this.getHelix().getTrackDirectionAtRadius(R);
                cross.update(trackPos, trackDir);
            }
        }
    }
    
    /**
     * Check if track passes basic quality cuts
     * @return 
     */    
    public boolean isGood() {
        //if(Constants.getInstance().svtOnly==false && this.getNDF()<1) 
        //    return false;
//        int nSVT=0;
//        for(Cross c : this.getCrosses()) {
//            if(c.getDetector()==DetectorType.BST)
//                nSVT++;
//        }
//        if(nSVT==0)
//            System.out.println("no SVT? "+this.toString());
        if(Constants.getInstance().seedingDebugMode) {
            System.out.println("Pass SSA , c2 ok "+(this.getChi2() <= Constants.CHI2CUTSSA * (this.getNDF() + 5)) 
                    +" ndf ok "+ (this.getNDF() >= Constants.NDFCUT) 
                    +" r ok "+ (this.getHelix().radius() >= Constants.getInstance().getRCUT()) 
                    +" dz ok "+ (Math.abs(Geometry.getInstance().getZoffset()-this.getHelix().getZ0()) <= Geometry.getInstance().getZlength()+Constants.getInstance().getZRANGE()) 
                    +" ");
        }
        boolean pass = true;
        if(Double.isNaN(this.getChi2())) 
            pass = false;
        if(this.getChi2() > Constants.CHI2CUTSSA * (this.getNDF() + 5)) 
            pass = false;
        if(this.getNDF() < Constants.NDFCUT) 
            pass = false;
        if(this.getHelix().radius() < Constants.getInstance().getRCUT()) 
            pass = false;
        if(Math.abs(Geometry.getInstance().getZoffset()-this.getHelix().getZ0()) > Geometry.getInstance().getZlength()+Constants.getInstance().getZRANGE()+Constants.DZCUTBUFFEESSA) 
            pass = false;
        return pass;
    }
    
    /**
     * Compare this track quality with the given track
     * based on NDF and Chi2
     * @param o the other track
     * @return true if this track quality is better than the given track
     */    
    public boolean betterThan(Seed o) {
        //if(this.getNDF()>o.getNDF()) 
        //    return true;
        if(this.getNDF()==o.getNDF()) {
            if(this.getChi2() < o.getChi2())
                return true;
            else return false;
        }
        else
            return true;
        }
    
    /**
     * Check track overlaps with the given track
     * an overlaps is detected if the tracks share at least two crosses
     * @param o the other track
     * @return true if this track overlaps with the given track, false otherwise
     */
    public boolean overlapWithUsingCrosses(Seed o) {
        int nc = 0;
        for(Cross c : this.getCrosses()) {
            //if(c.getType()==BMTType.C) continue; //skim BMTC
            if(o.getCrosses().contains(c)) nc++;
        }
        if(nc >1) return true;
        else      return false;
    }
    
//    public boolean overlapWithUsingClusters(Seed o) {
//        int nc = 0;
//        for(Cluster c : this.getClusters()) {
//            if(o.getClusters().contains(c)) nc++;
//        }
//        if(nc >1) return true;
//        else      return false;
//    }
    public boolean overlapWithUsingClusters(Seed o) {
        boolean ov = false;
        for(Cluster c : this._Clusters) {
           for(Cluster co : o._Clusters) {
               if(c.getDetector()==co.getDetector() && c.getLayer()==co.getLayer() && c.getId()==co.getId()) 
                   ov = true;
           }
       }
       return ov; 
    }
    
    public static void flagMCSeeds(List<Seed> seeds, int totTruthHits) {
        int hitcnt = 0;
        int mchitcnt = 0; 
        for(Seed s : seeds) {
            hitcnt = 0;
            mchitcnt = 0;
            s.setClusters();
            List<Cluster> cls = s.getClusters();
            for(Cluster cl : cls) {
                for(Hit h : cl) {
                    hitcnt++;
                    if(h.MCstatus==0) 
                        mchitcnt++;
                }
            }
            if(hitcnt!=0)
                s.percentTruthMatch = (double) (mchitcnt*100.0/hitcnt);
            if(totTruthHits!=0)
                s.totpercentTruthMatch = (double) (mchitcnt*100.0/totTruthHits);
        }
    }
    
    public static void removeOverlappingSeeds(List<Seed> seeds) {
        if(seeds==null)
            return;
        for(Seed s : seeds) {
            s.setClusters();
        }
        List<Seed> ovlrem = getOverlapRemovedSeeds(seeds);
        if(ovlrem!=null) {
            seeds.clear();
            seeds.addAll(ovlrem);
        }
    }
    
    public static List<Seed> getOverlapRemovedSeeds(List<Seed> seeds) { 
        if(seeds==null)
            return null;
        List<Seed> ovlrem = new ArrayList<>();
        ovlrem.addAll(seeds);
        while(ovlrem.size()!=getOverlapRemovedSeeds1Pass(ovlrem).size()) {
            ovlrem = getOverlapRemovedSeeds1Pass(ovlrem);
        }
        
        while(ovlrem.size()!=appendSeedList(seeds, ovlrem).size() ){
            ovlrem = appendSeedList(seeds, ovlrem);
        }
        
        return ovlrem;
    }
    
    public static void setOverlaps(List<Seed> seeds) { 
        if(seeds==null)
            return ;
        List<Seed> goodseeds = new ArrayList<>();    
        List<Seed> selectedSeeds =  null;
        for (int i = 0; i < seeds.size(); i++) {
            
            Seed t1 = seeds.get(i);
            selectedSeeds =  new ArrayList<>();
            for(int j=0; j<seeds.size(); j++ ) {
                Seed t2 = seeds.get(j);
                if(i!=j && t1.overlapWithUsingClusters(t2)) {
                    selectedSeeds.add(t2);
                }
            }
            
            t1.setOverlappingSeed(selectedSeeds);
        }
    }
    
    public static List<Seed> getOverlapRemovedSeeds1Pass(List<Seed> seeds) { 
        if(seeds==null)
            return null;
        List<Seed> goodseeds = new ArrayList<>();    
        List<Seed> selectedSeeds =  null;
        for (int i = 0; i < seeds.size(); i++) {
            
            Seed t1 = seeds.get(i);
            selectedSeeds =  new ArrayList<>();
            for(int j=0; j<seeds.size(); j++ ) {
                Seed t2 = seeds.get(j);
                if(i!=j && t1.overlapWithUsingClusters(t2)) {
                    selectedSeeds.add(t2);
                }
            }
            
            t1.setOverlappingSeed(selectedSeeds);
            //System.out.println(t1.getOverlappingSeeds().size()+") Check for seed ovrl "+t1.toString());
            //for(Seed s : t1.getOverlappingSeeds()) {
            //    System.out.println("ovl has seed "+s.toString());
            //}
        }
        //seeds.removeAll(seeds);
        //seeds.addAll(overlappingSeeds);
        selectedSeeds =  new ArrayList<>();
        //Get the best seeds from each list
       
        Map<Double, Seed> seedMap = new HashMap<>();
        for (Seed s : seeds) {
            
            List<Seed> oSeeds  =  new ArrayList<>();
            //System.out.println(" seed "+s.toString());
            //for(int o = 0; o< s.getOverlappingSeeds().size(); o++) { 
            //    System.out.println("has ov seed "+s.getOverlappingSeeds().get(o).toString());
                
            //}
            oSeeds.add(s);
            oSeeds.addAll(s.getOverlappingSeeds());
            //remove complete overlaps
            removeCompleteOverlaps(oSeeds); 
            //get best seed
            setSortingMethod(oSeeds, 0);
            Collections.sort(oSeeds);  
            
            if(seedMap.containsKey(oSeeds.get(0).getChi2())) {
                seedMap.replace(oSeeds.get(0).getChi2(), oSeeds.get(0));
            } else {
                seedMap.put(oSeeds.get(0).getChi2(), oSeeds.get(0));
            }
            
        }
        
        goodseeds.addAll(new ArrayList<>(seedMap.values()));
        return goodseeds;
    }
    
    @Override
    public String toString() {
        String str ="";
        if(this.getHelix()!=null) {
            str = String.format("Track id=%d, q=%d, omega=%.3f mm-1, d0=%.3f mm, phi=%.3f deg, dz=%.3f mm, tanL=%.3f, NDF=%d, chi2=%.3f, seed method=%d\n", 
                         this.getId(), this.getHelix().getCharge(), this.getHelix().getCurvature(), this.getHelix().getDCA(),
                         Math.toDegrees(this.getHelix().getPhiAtDCA()), this.getHelix().getZ0(), this.getHelix().getTanDip(),
                         this.getNDF(), this.getChi2(), this.getStatus());
            for(Cross c: this.getCrosses()) str = str + c.toString() + "\n";
            for(Cluster c: this.getClusters()) str = str + c.toString() + "\n";
        } else {
            
            for(Cross c: this.getCrosses()) str = str + c.toString() + "\n";
            for(Cluster c: this.getClusters()) str = str + c.toString() + "\n";
        }
        return str;
    }

    private static void setSortingMethod(List<Seed> seeds, int m) {
        for(Seed s : seeds) {
            s.sortingMethod = m;
            
        }
    }
    private static void removeCompleteOverlaps(List<Seed> oSeeds) {
        //setSortingMethod(oSeeds, 1);
        Collections.sort(oSeeds);
        List<Seed> oSeedsSubset = new ArrayList<>();
        for(int i =0; i< oSeeds.size(); i++) {
            Seed seed1 = oSeeds.get(i);
            for(int j =i; j< oSeeds.size(); j++) {
                if(j==i) continue;
                Seed seed2 = oSeeds.get(j);
                Seed subseed = subSet(seed1, seed2);
                if(subseed!=null) {
                    Seed seed_1 = null; 
                    Seed seed_2 = null;
                    if(subseed._Crosses.size()==seed2._Crosses.size()) {
                        seed_2 = seed2;
                        seed_1 = seed1;
                    } else {
                        seed_2 = seed1;
                        seed_1 = seed2;
                    }
                    //if the chi2/ndf diff consistent keep the larger seed
                    if((seed_1.getChi2()/seed_1.getNDF())/(seed_2.getChi2()/seed_2.getNDF()) < 1.0*(seed_1.getCrosses().size()-seed_2.getCrosses().size())) {
                        oSeedsSubset.add(seed_2); 
                    } else {
                        oSeedsSubset.add(seed_1); 
                    }
                } 
            }
        }
        oSeeds.removeAll(oSeedsSubset);
    }
    
    private static Seed subSet(Seed seeda, Seed seedb) {
        Seed seed1 = null; 
        Seed seed2 = null;
        if(seeda.getCrosses().size()>seedb.getCrosses().size()) {
            seed1 = seeda;
            seed2 = seedb;
        } else {
            seed1 = seedb;
            seed2 = seeda;
        }
        if(seed1.getKey()==null || seed2.getKey()==null)
            return null;
        int count=0; //the number of 
        for(int k = 0; k<9; k++) {
            if(seed1.getKey().crossIds[k]!=0 && seed2.getKey().crossIds[k]!=0) {
                if(seed1.getKey().crossIds[k]-seed2.getKey().crossIds[k]==0)
                    count++;
            }
        }
        if(count==seed2.getCrosses().size()) {//all the crosses in the smaller seeds are in the larger seed
           
            return seed2;
        } else {
            return null;
        }
    }

    private  static List<Seed> appendSeedList(List<Seed> seeds, List<Seed> ovlrem) {
        
        List<Seed> overlappingSeeds =  new ArrayList<>();
        List<Seed> ovlrem2 = new ArrayList<>();
        ovlrem2.addAll(seeds);
        ovlrem2.removeAll(ovlrem);
        for (Seed s2 : ovlrem2) {
            for(Seed s : ovlrem) {
                if(s2.overlapWithUsingClusters(s)) {
                    s2.setId(-99999);
                }
            }
        }
        for(Seed s2 :  ovlrem2) {
            if(s2.getId()==-99999) 
                overlappingSeeds.add(s2);
        }
        ovlrem2.removeAll(overlappingSeeds);
        //get the best seeds from what remains
        if(!ovlrem2.isEmpty()) {
            while(ovlrem2.size()!=getOverlapRemovedSeeds1Pass(ovlrem2).size()) {
                ovlrem2 = getOverlapRemovedSeeds1Pass(ovlrem2);
            }
        }
        ovlrem2.addAll(ovlrem);
        
        return ovlrem2;
    }
    
    public class Key implements Comparable<Key> {

        public int[] crossIds = new int[9];
       
        public Key(Seed seed) {
            for(Cross c : seed.getCrosses()) {
                int l =0;
                if(c.getDetector()==DetectorType.BST) {
                    l = c.getRegion();
                } else {
                    l = c.getCluster1().getLayer();
                }
                crossIds[l-1]=c.getId();
            }
        }

        
        
        @Override
        public int compareTo(Key o) { //sort by cross ids in layers
            int[] K = new int[9];
            for(int k = 0; k<9; k++) {
                K[k] = this.crossIds[k] < o.crossIds[k] ? -1 : this.crossIds[k] == o.crossIds[k] ? 0 : 1;
            }
            
            int[] R = new int[8];
            R[0] = ((K[0] == 0) ? K[1] : K[0]);
            for(int k = 0; k<7; k++) {
                R[k+1] = ((R[k] == 0) ? K[k+2] : R[k]);
            }
            
            return R[7];
        }
    }
}