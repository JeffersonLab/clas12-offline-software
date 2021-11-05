package org.jlab.clas.tracking.kalmanfilter.helical;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.tracking.kalmanfilter.AKFitter;
import org.jlab.clas.tracking.kalmanfilter.AMeasVecs;
import org.jlab.clas.tracking.kalmanfilter.AStateVecs;
import org.jlab.clas.tracking.kalmanfilter.AStateVecs.StateVec;
import org.jlab.clas.tracking.kalmanfilter.Surface;
import org.jlab.clas.tracking.trackrep.Helix;
import org.jlab.geom.prim.Point3D;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author ziegler
 */
public class KFitter extends AKFitter {
    public Map<Integer, HitOnTrack> TrjPoints = new HashMap<Integer, HitOnTrack>();
    StateVecs sv = new StateVecs();
    MeasVecs mv = new MeasVecs();
    public StateVecs.StateVec finalStateVec;
    
    public Helix KFHelix;
    public KFitter(Helix helix, double[][] cov, DataEvent event, Swim swimmer, double Xb, double Yb, 
            double Zref, List<Surface> measSurfaces) {
        this.setXb(Xb);
        this.setYb(Yb);
        this.setTarShift(Zref);
        this.init(helix, cov, event, swimmer, Xb, Yb, 
             Zref, sv, mv, measSurfaces);
    }
    public void runFitter(Swim swimmer) {
        this.runFitter(swimmer, sv, mv);
    }
    @Override
    public void runFitter(Swim swimmer, AStateVecs sv, AMeasVecs mv) {
     double newchisq = Double.POSITIVE_INFINITY;
     
        for (int it = 0; it < totNumIter; it++) {
            this.chi2 = 0;
            this.runFitterIter(swimmer, it, sv, mv);

            // chi2
            this.chi2=this.calc_chi2(swimmer, sv, mv); 
            if(this.chi2<newchisq) { 
                KFHelix = sv.setTrackPars();
                finalStateVec = sv.trackTraj.get(0);
                this.setTrajectory(sv, mv);
                setFitFailed = false;
                if(newchisq-this.chi2<0.1)
                    break;
                    
                newchisq=this.chi2;
            } else {
                this.chi2 =newchisq ; 
                break;
            }
        }
    }

    @Override
    public void setTrajectory(AStateVecs sv, AMeasVecs mv) {
        TrjPoints.clear();
        for (int k = 1; k < sv.trackTraj.size(); k++) {
            int layer = mv.measurements.get(k).layer;
            double x = sv.trackTraj.get(k).x;
            double y = sv.trackTraj.get(k).y;
            double z = sv.trackTraj.get(k).z;
            double azi = sv.trackTraj.get(k).phi0 + sv.trackTraj.get(k).phi;
            double invKappa = 1. / Math.abs(sv.trackTraj.get(k).kappa);
            double px = -invKappa * Math.sin(azi);
            double py = invKappa * Math.cos(azi);
            double pz = invKappa * sv.trackTraj.get(k).tanL;
            double resi = mv.dh(k, sv.trackTraj.get(k));
            TrjPoints.put(layer, new HitOnTrack(layer, x, y, z, px, py, pz, resi));
            if(mv.measurements.get(k).skip)
                TrjPoints.get(layer).isMeasUsed = false;
            //System.out.println(" Traj layer "+layer+" x "+TrjPoints.get(layer).x+" y "+TrjPoints.get(layer).y+" z "+TrjPoints.get(layer).z);
        }
    }

    @Override
    public void filter(int k, Swim swimmer, int dir, AStateVecs sv, AMeasVecs mv) {
        if (sv.trackTraj.get(k) != null && sv.trackCov.get(k).covMat != null 
                && mv.measurements.get(k).skip == false && filterOn) {

            double[] K = new double[5];
            double V = mv.measurements.get(k).error;

            double dh = mv.dh(k, sv.trackTraj.get(k));
            
            //get the projector Matrix
            double[] H = new double[5];
            H = mv.H(sv.trackTraj.get(k), sv,  mv.measurements.get(k), swimmer, dir);
            
            double[][] CaInv =  mo.filterCovMat(H, sv.trackCov.get(k).covMat, V);
            if (CaInv != null) {
                    sv.trackCov.get(k).covMat = CaInv;
                } else {
                    return;
            }
            
            for (int j = 0; j < 5; j++) {
                // the gain matrix
                K[j] = 0;
                for (int i = 0; i < 5; i++) {
                    K[j] += H[i] * sv.trackCov.get(k).covMat[j][i] / V;
                } 
            }
            if(sv.straight == true) {
                //for (int i = 0; i < 5; i++) {
                    K[2] = 0;
                //}
            }
            double drho_filt = sv.trackTraj.get(k).d_rho;
            double phi0_filt = sv.trackTraj.get(k).phi0;
            double kappa_filt = sv.trackTraj.get(k).kappa;
            double dz_filt = sv.trackTraj.get(k).dz;
            double tanL_filt = sv.trackTraj.get(k).tanL;
            
            if (!Double.isNaN(dh)) {
                drho_filt -= K[0] * dh;
                phi0_filt -= K[1] * dh;
                kappa_filt -= K[2] * dh;
                dz_filt -= K[3] * dh;
                tanL_filt -= K[4] * dh;
            }
            
            StateVec fVec = sv.new StateVec(sv.trackTraj.get(k).k);
            fVec.d_rho = drho_filt;
            fVec.phi0 = phi0_filt;
            fVec.kappa = kappa_filt;
            fVec.dz = dz_filt;
            fVec.tanL = tanL_filt;
            fVec.alpha = sv.trackTraj.get(k).alpha;
            sv.setStateVecPosAtMeasSite(k, fVec, mv.measurements.get(k), swimmer); 

            double dh_filt = mv.dh(k, fVec);  
            if (Math.abs(dh_filt) < Math.abs(dh) 
                    && Math.abs(dh_filt)/Math.sqrt(V)<this.getResiCut()) { 
                sv.trackTraj.get(k).d_rho = drho_filt;
                sv.trackTraj.get(k).phi0 = phi0_filt;
                sv.trackTraj.get(k).kappa = kappa_filt;
                sv.trackTraj.get(k).dz = dz_filt;
                sv.trackTraj.get(k).tanL = tanL_filt;
                sv.trackTraj.get(k).phi = fVec.phi;
                sv.trackTraj.get(k).x = fVec.x;
                sv.trackTraj.get(k).y = fVec.y;
                sv.trackTraj.get(k).z = fVec.z;  
            } else {
                this.NDF--;
                mv.measurements.get(k).skip = true;
            }
        }
    }
    
    public void init(Helix helix, double[][] cov, DataEvent event, Swim swimmer, double Xb, double Yb, 
            double Zref, StateVecs sv, MeasVecs mv, List<Surface> measSurfaces) {
        sv.shift = Zref;
        //iCov = cov;
        mv.setMeasVecs(measSurfaces);
        if (sv.Layer != null) {
            sv.Layer.clear();
        } else {
            sv.Layer = new ArrayList<Integer>();
        }
        if (sv.Sector != null) {
            sv.Sector.clear();
        } else {
            sv.Sector = new ArrayList<Integer>();
        }
        if (sv.X0 != null) {
            sv.X0.clear();
        } else {
            sv.X0 = new ArrayList<Double>();
        }
        if (sv.Y0 != null) {
            sv.Y0.clear();
        } else {
            sv.Y0 = new ArrayList<Double>();
        }
        if (sv.Z0 != null) {
            sv.Z0.clear();
        } else {
            sv.Z0 = new ArrayList<Double>();
        }
        //take first plane along beam line with n = y-dir;
        sv.Layer.add(0);
        sv.Sector.add(0);
        sv.X0.add(Xb);
        sv.Y0.add(Yb);
        sv.Z0.add(0.0); 
        this.NDF = -5;
        for (int i = 1; i < mv.measurements.size(); i++) {
            sv.Layer.add(mv.measurements.get(i).layer);
            sv.Sector.add(mv.measurements.get(i).sector);
            if(mv.measurements.get(i).skip==false) {
                this.NDF++;
            }
            Point3D ref = new Point3D(Xb, Yb, 0.0);
            sv.X0.add(ref.x());
            sv.Y0.add(ref.y());
            sv.Z0.add(ref.z());
        } 
        sv.init( helix, cov, this, swimmer);

    }


}
