package org.jlab.rec.cvt.track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jlab.detector.base.DetectorType;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.bmt.BMTType;

import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.fit.HelicalTrackFitter;
import org.jlab.rec.cvt.svt.SVTGeometry;
import org.jlab.rec.cvt.trajectory.Helix;

/**
 *
 * @author ziegler
 *
 */
public class Seed implements Comparable<Seed>{

    private int status;
    private double doca;
    private double rho;
    private double phi;
    private Helix _Helix;
    private List<Cross> _Crosses;
    
    
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
        
    public List<Cluster> get_Clusters() {
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
        return clusters;
    }

    public List<Cross> get_Crosses() {
        Collections.sort(_Crosses);
        return _Crosses;
    }

    public final void set_Crosses(List<Cross> _Crosses) {
        Collections.sort(_Crosses);
        this._Crosses = _Crosses;
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
    

    public Track fit(SVTGeometry svt_geo, BMTGeometry bmt_geo, int fitIter, boolean originConstraint,
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
            c.reset(svt_geo);
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
                    Rho.add(j, bmt_geo.getRadiusMidDrift(BMTCrossesC.get(j - svtSz * useSVTdipAngEst).get_Cluster1().get_Layer()));
                    
                    ErrRho.add(j, bmt_geo.getThickness()/2 / Math.sqrt(12.));
                    ErrZ.add(j, BMTCrossesC.get(j - svtSz * useSVTdipAngEst).get_PointErr().z());
                }
            }
            X.add((double) Constants.getXb());
            Y.add((double) Constants.getYb());

            ErrRt.add((double) 0.1);
            
            fitTrk.fit(X, Y, Z, Rho, ErrRt, ErrRho, ErrZ);
            
            if (fitTrk.get_helix() == null) { 
                return null;
            }

            fitTrk.get_helix().B = bfield;
            cand = new Track(fitTrk.get_helix());
            cand.addAll(SVTCrosses);
            cand.addAll(BMTCrossesC);
            cand.addAll(BMTCrossesZ);
            
            if( X.size()>3 ) {
            	cand.set_circleFitChi2PerNDF(fitTrk.get_chisq()[0]/(X.size()-3));
            }
            else { 
            	cand.set_circleFitChi2PerNDF(fitTrk.get_chisq()[0]*2); // penalize tracks with only 3 crosses 
            }
            
            if( Z.size() > 2 ) {
            	cand.set_lineFitChi2PerNDF(fitTrk.get_chisq()[1]/(Z.size()-2));
            }
            else {
            	cand.set_lineFitChi2PerNDF(fitTrk.get_chisq()[1]*2);// penalize tracks with only 2 crosses
            }
            cand.setChi2(fitTrk.get_chisq()[0]+fitTrk.get_chisq()[1]);
            cand.setNDF(X.size()+Z.size());

            if (fitTrk.get_chisq()[0] < chisqMax) {
                chisqMax = fitTrk.get_chisq()[0];
                if(chisqMax<Constants.CIRCLEFIT_MAXCHI2)
                    cand.update_Crosses(svt_geo, bmt_geo);
            }
        }
        return cand;
    }

    public Track toTrack(int id) {
        Track track = new Track(this.get_Helix());
        track.set_Id(id);
        track.set_TrackingStatus(this.get_Status());
        track.addAll(this.get_Crosses());
        for(Cross c : track) {
            c.set_AssociatedTrackID(id);
        }
        return track;
    }

}
