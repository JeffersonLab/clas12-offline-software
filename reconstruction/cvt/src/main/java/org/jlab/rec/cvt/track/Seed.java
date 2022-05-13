package org.jlab.rec.cvt.track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.bmt.BMTType;

import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.fit.HelicalTrackFitter;
import org.jlab.rec.cvt.fit.HelicalTrackFitter.FitStatus;
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
    
    
    public Seed() {
    }

    public Seed(List<Cross> crosses) {
        this.setCrosses(crosses);
    }

    public Seed(List<Cross> crosses, double doca, double rho, double phi) {
        this.setCrosses(crosses);
        this.setDoca(doca);
        this.setRho(rho);
        this.setPhi(phi);
    }

    public Seed(List<Cross> crosses, Helix helix) {
        this.setCrosses(crosses);
        this.setHelix(helix);
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

    @Override
    public int compareTo(Seed arg) {
    	return ( this._Crosses.size() > arg.getCrosses().size() ) ? -1 : ( this._Crosses.size() == arg.getCrosses().size() ) ? 0 : 1;        
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
                    Rho.add(j, Constants.getInstance().BMTGEOMETRY.getRadiusMidDrift(BMTCrossesC.get(j - svtSz * useSVTdipAngEst).getCluster1().getLayer()));
                    
                    ErrRho.add(j, Constants.getInstance().BMTGEOMETRY.getThickness()/2 / Math.sqrt(12.));
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
    
    /**
     * Check if track passes basic quality cuts
     * @return 
     */    
    public boolean isGood() {
        if(Double.isNaN(this.getChi2())) 
            return false;
        else if(this.getChi2() > Constants.CHI2CUT * (this.getNDF() + 5)) 
            return false;
        else if(this.getNDF() < Constants.NDFCUT) 
            return false;
        else if(this.getHelix().getPt(this.getHelix().B) < Constants.PTCUT) 
            return false;
//        else if(Math.abs(this.getHelix().getTanDip()) > Constants.TANDIP) 
//            return false;
        else if(Math.abs(this.getHelix().getZ0()) > Constants.ZRANGE) 
            return false;
        else 
            return true;
    }
    
    /**
     * Compare this track quality with the given track
     * based on NDF and Chi2
     * @param o the other track
     * @return true if this track quality is better than the given track
     */    
    public boolean betterThan(Seed o) {
        if(this.getNDF()>o.getNDF()) 
            return true;
        else if(this.getNDF()==o.getNDF()) {
            if(this.getChi2()/this.getNDF() < o.getChi2()/o.getNDF())
                return true;
            else return false;
        }
        else
            return false;
    }
    
    /**
     * Check track overlaps with the given track
     * an overlaps is detected if the tracks share at least two crosses
     * @param o the other track
     * @return true if this track overlaps with the given track, false otherwise
     */
    public boolean overlapWith(Seed o) {
        int nc = 0;
        for(Cross c : this.getCrosses()) {
            if(c.getType()==BMTType.C) continue; //skim BMTC
            if(o.getCrosses().contains(c)) nc++;
        }
        if(nc >1) return true;
        else      return false;
    }
    
    public static void removeOverlappingSeeds(List<Seed> seeds) {
            if(seeds==null)
                return;
            
        List<Seed> selectedSeeds =  new ArrayList<>();
        for (int i = 0; i < seeds.size(); i++) {
            boolean overlap = false;
            Seed t1 = seeds.get(i);
            for(int j=0; j<seeds.size(); j++ ) {
                Seed t2 = seeds.get(j);
                if(i!=j && t1.overlapWith(t2) && !t1.betterThan(t2)) {
                    overlap=true;
                }
            }
            if(!overlap) selectedSeeds.add(t1);
        }
        seeds.removeAll(seeds);
        seeds.addAll(selectedSeeds);
    }
    
    @Override
    public String toString() {
        String str = String.format("Track id=%d, q=%d, omega=%.3f mm-1, d0=%.3f mm, phi=%.3f deg, dz=%.3f mm, tanL=%.3f, NDF=%d, chi2=%.3f, seed method=%d\n", 
                     this.getId(), this.getHelix().getCharge(), this.getHelix().getCurvature(), this.getHelix().getDCA(),
                     Math.toDegrees(this.getHelix().getPhiAtDCA()), this.getHelix().getZ0(), this.getHelix().getTanDip(),
                     this.getNDF(), this.getChi2(), this.getStatus());
        for(Cross c: this.getCrosses()) str = str + c.toString() + "\n";
        for(Cluster c: this.getClusters()) str = str + c.toString() + "\n";
        return str;
    }
}
