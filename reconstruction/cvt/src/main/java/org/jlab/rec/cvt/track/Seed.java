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
        this.set_Crosses(crosses);
    }

    public Seed(List<Cross> crosses, double doca, double rho, double phi) {
        this.set_Crosses(crosses);
        this.set_Doca(doca);
        this.set_Rho(rho);
        this.set_Phi(phi);
    }

    public Seed(List<Cross> crosses, Helix helix) {
        this.set_Crosses(crosses);
        this.set_Helix(helix);
    }

    public Helix get_Helix() {
        return _Helix;
    }

    public final void set_Helix(Helix helix) {
        this._Helix = helix;
        this.doca = helix.get_dca();
        this.rho  = helix.get_curvature();
        this.phi  = helix.get_phi_at_dca();
    }

    public void setId(int id) {
        this.id = id;
    }

    public void set_Status(int trkStatus) {
        this.status = trkStatus;
    }

    public final void set_Doca(double doca) {
        this.doca = doca;
    }

    public final void set_Rho(double rho) {
        this.rho = rho;
    }

    public final void set_Phi(double phi) {
        this.phi = phi;
    }

    public int getId() {
        return id;
    }

    public int get_Status() {
        return status;
    }

    public double get_Doca() {
        return this.doca;
    }
    
    public double get_Rho() {
        return this.rho;
    }
    
    public double get_Phi() {
        return this.phi;
    }
        
    private void set_Clusters() {
        List<Cluster> clusters = new ArrayList<Cluster>(); 
        for(Cross c : this.get_Crosses()) { 
	    if(c.get_Detector()==DetectorType.BST) {
                clusters.add(c.get_Cluster1());
                clusters.add(c.get_Cluster2());
            } else {
                clusters.add(c.get_Cluster1());
            }
      	}
        Collections.sort(clusters);
        _Clusters = clusters;
    }

    public List<Cluster> get_Clusters() {
        return _Clusters;
    }

    public void add_Clusters(List<Cluster> clusters) {
        this._Clusters.addAll(clusters);
        Collections.sort(_Clusters);
    }

    public List<Cross> get_Crosses() {
        Collections.sort(_Crosses);
        return _Crosses;
    }

    public final void set_Crosses(List<Cross> _Crosses) {
        Collections.sort(_Crosses);
        this._Crosses = _Crosses;
        this.set_Clusters();
    }

    public double get_circleFitChi2PerNDF() {
        return _circleFitChi2PerNDF;
    }

    public void set_circleFitChi2PerNDF(double _circleFitChi2PerNDF) {
        this._circleFitChi2PerNDF = _circleFitChi2PerNDF;
    }

    public double get_lineFitChi2PerNDF() {
        return _lineFitChi2PerNDF;
    }

    public void set_lineFitChi2PerNDF(double _lineFitChi2PerNDF) {
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

    public String get_IntIdentifier() {
        
        String id = "";
        for(Cluster c: this.get_Clusters())
            id+=c.get_Id();
        for(Cross c: this.get_Crosses())
            id+=c.get_Id();
       
        return id;
    }

    @Override
    public int compareTo(Seed arg) {

    	return ( this._Crosses.size() > arg.get_Crosses().size() ) ? -1 : ( this._Crosses.size() == arg.get_Crosses().size() ) ? 0 : 1;
//        return Double.parseDouble(this.get_IntIdentifier()) < Double.parseDouble(arg.get_IntIdentifier()) ? -1 : Double.parseDouble(this.get_IntIdentifier()) == Double.parseDouble(arg.get_IntIdentifier()) ? 0 : 1;
        
    }
    

    public boolean fit(int fitIter, boolean originConstraint,
            double bfield) {
        
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
        
        for (Cross c : this.get_Crosses()) {
            // reset cross to clear previous track settings on direction and Point
            c.reset();
            if (c.get_Detector()==DetectorType.BST) {
                SVTCrosses.add(c);
            }
            else if (c.get_Detector()==DetectorType.BMT && c.get_Type()==BMTType.C ) {
                BMTCrossesC.add(c);
            }
            else if (c.get_Detector()==DetectorType.BMT && c.get_Type()==BMTType.Z ) {
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
                    Rho.add(j, Constants.BMTGEOMETRY.getRadiusMidDrift(BMTCrossesC.get(j - svtSz * useSVTdipAngEst).get_Cluster1().get_Layer()));
                    
                    ErrRho.add(j, Constants.BMTGEOMETRY.getThickness()/2 / Math.sqrt(12.));
                    ErrZ.add(j, BMTCrossesC.get(j - svtSz * useSVTdipAngEst).get_PointErr().z());
                }
            }
            X.add((double) Constants.getXb());
            Y.add((double) Constants.getYb());

            ErrRt.add((double) 0.1);
            
            FitStatus fitStatus = fitTrk.fit(X, Y, Z, Rho, ErrRt, ErrRho, ErrZ);
            
            if (fitStatus!=FitStatus.Successful || fitTrk.get_helix() == null) { 
                return false;
            }

            fitTrk.get_helix().B = bfield;
            
            this.set_Helix(fitTrk.get_helix());
            if( X.size()>3 ) {
            	this.set_circleFitChi2PerNDF(fitTrk.get_chisq()[0]/(X.size()-3));
            }
            else { 
            	this.set_circleFitChi2PerNDF(fitTrk.get_chisq()[0]*2); // penalize tracks with only 3 crosses 
            }
            
            if( Z.size() > 2 ) {
            	this.set_lineFitChi2PerNDF(fitTrk.get_chisq()[1]/(Z.size()-2));
            }
            else {
            	this.set_lineFitChi2PerNDF(fitTrk.get_chisq()[1]*2);// penalize tracks with only 2 crosses
            }
            this.setChi2(fitTrk.get_chisq()[0]+fitTrk.get_chisq()[1]);
            this.setNDF(X.size()+Z.size()-5);
            
            if (fitTrk.get_chisq()[0] < chisqMax) {
                chisqMax = fitTrk.get_chisq()[0];
                if(chisqMax<Constants.CIRCLEFIT_MAXCHI2) {
                    this.update_Crosses();
                }
            }
        }
        return true;
    }

    /**
     * Updates the crosses positions based on trajectories or helix
     * @param sgeo
     * @param bgeo
     */
    public void update_Crosses() {
        if (this.get_Helix() != null && this.get_Helix().get_curvature() != 0) {
            for (int i = 0; i < this.get_Crosses().size(); i++) {
                Cross cross = this.get_Crosses().get(i);
                double R = Math.sqrt(cross.get_Point().x() * cross.get_Point().x() + cross.get_Point().y() * cross.get_Point().y());
                Point3D  trackPos = this.get_Helix().getPointAtRadius(R);
                Vector3D trackDir = this.get_Helix().getTrackDirectionAtRadius(R);
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
        else if(this.get_Helix().getPt(this.get_Helix().B) < Constants.PTCUT) 
            return false;
//        else if(Math.abs(this.get_Helix().get_tandip()) > Constants.TANDIP) 
//            return false;
        else if(Math.abs(this.get_Helix().get_Z0()) > Constants.ZRANGE) 
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
        for(Cross c : this.get_Crosses()) {
            if(c.get_Type()==BMTType.C) continue; //skim BMTC
            if(o.get_Crosses().contains(c)) nc++;
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
                     this.getId(), this.get_Helix().get_charge(), this.get_Helix().get_curvature(), this.get_Helix().get_dca(),
                     Math.toDegrees(this.get_Helix().get_phi_at_dca()), this.get_Helix().get_Z0(), this.get_Helix().get_tandip(),
                     this.getNDF(), this.getChi2(), this.get_Status());
        for(Cross c: this.get_Crosses()) str = str + c.toString() + "\n";
        return str;
    }
}
