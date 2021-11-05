package org.jlab.clas.tracking.kalmanfilter.straight;

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
import org.jlab.geom.prim.Point3D;

/**
 *
 * @author ziegler
 */
public class KFitter extends AKFitter {

    public double yx_slope;
    public double yz_slope;
    public double yx_interc;
    public double yz_interc;
    private double resiCut = 100;//residual cut for the measurements
    public Map<Integer, HitOnTrack> TrjPoints = new HashMap<Integer, HitOnTrack>();
    StateVecs sv = new StateVecs();
    MeasVecs mv = new MeasVecs();
    public StateVecs.StateVec finalStateVec;
    
     public KFitter(double x0, double z0,double tx,double tz, double units, double[][] cov, KFitter kf,
            List<Surface> measSurfaces) {
        this.init(x0, z0, tx, tz, units, cov, kf, measSurfaces);
    }
    public void runFitter(Swim swimmer) {
        this.runFitter(swimmer, sv, mv);
    }
    
    @Override
    public void runFitter(Swim swimmer, AStateVecs sv, AMeasVecs mv) {
        double newchisq = Double.POSITIVE_INFINITY;
        this.NDF = sv.Layer.size()-5; 
        
        for (int it = 0; it < totNumIter; it++) {
            this.chi2 = 0;
            this.runFitterIter(swimmer, it, sv, mv);
            if(sv.trackTraj.get(1)!=null) { 
                finalStateVec = sv.trackTraj.get(1);
                finalStateVec.x = finalStateVec.x0;
                finalStateVec.z = finalStateVec.z0;
                
                yx_slope = finalStateVec.tx;
                yz_slope = finalStateVec.tz;
                yx_interc = finalStateVec.x0;
                yz_interc = finalStateVec.z0;
               
                
                this.setTrajectory(sv, mv);
                setFitFailed = false;
            } else {
                //this.chi2 =newchisq ;
                //break;
            }
        }
    
    }

    @Override
    public void setTrajectory(AStateVecs sv, AMeasVecs mv) {
        TrjPoints.clear();
        double c2 = 0;
        for (int k = 0; k < sv.trackTraj.size()-1; k++) {
            StateVec stv = sv.transported(k, k+1, sv.trackTraj.get(k), mv.measurements.get(k+1), null); 
            sv.setStateVecPosAtMeasSite(k+1, stv, mv.measurements.get(k+1), null); 
            double resi = mv.dh(k+1, stv);
            c2 += resi*resi / mv.measurements.get(k+1).error;
            int layer = mv.measurements.get(k+1).layer;
            double x = stv.x;
            double y = stv.y;
            double z = stv.z;
            double tx = stv.tx;
            double tz = stv.tz;
            double py = 1/Math.sqrt(1+tx*tx+tz*tz);
            double px = tx*py;
            double pz = tz*py;
            //double resi = stv.resi;
            TrjPoints.put(layer, new HitOnTrack(layer, x, y, z, px, py, pz,resi));
            //System.out.println("interc "+new Point3D(stv.x0,0,stv.z0).toString()+" P "+new Point3D(px,py,pz).toString());
            //System.out.println("...reso "+resi+" dl "+stv.dl+" Traj layer "+layer+" x "+TrjPoints.get(layer).x+" y "+TrjPoints.get(layer).y+" z "+TrjPoints.get(layer).z);
        } 
        this.chi2 = c2;
    }

    @Override
    public void filter(int k, Swim swimmer, int dir, AStateVecs sv, AMeasVecs mv) {
        if (sv.trackTraj.get(k) != null && sv.trackCov.get(k).covMat != null 
                && mv.measurements.get(k).skip == false ) {
            double[] K = new double[5];
            double V = mv.measurements.get(k).error;

            //get the projector Matrix
            double[] H = new double[5];
            H = mv.H(sv.trackTraj.get(k), sv,  mv.measurements.get(k), swimmer, dir);

            double[][] CaInv =  mo.filterCovMat(H, sv.trackCov.get(k).covMat, V);
            if (CaInv != null) {
                    sv.trackCov.get(k).covMat = CaInv;
                } else {
                    return;
            }
            for (int j = 0; j < 4; j++) {
                    // the gain matrix
                    K[j] = 0;
                    if(filterOn) {
                        for (int i = 0; i < 4; i++) {
                            K[j] += H[i] * sv.trackCov.get(k).covMat[j][i] / V;
                        } 
                    }
                }
                double x0_filt = sv.trackTraj.get(k).x0;
                double z0_filt = sv.trackTraj.get(k).z0;
                double tx_filt = sv.trackTraj.get(k).tx;
                double tz_filt = sv.trackTraj.get(k).tz;

                double dh =0;
                for(int i = 1; i<k+1; i++) {
                    dh += mv.dh(k, sv.trackTraj.get(i));
                }
                if (!Double.isNaN(dh)) {
                    x0_filt -= K[0] * dh/k;
                    z0_filt -= K[1] * dh/k;
                    tx_filt -= K[2] * dh/k;
                    tz_filt -= K[3] * dh/k;
                }
                StateVec fVec = sv.new StateVec(sv.trackTraj.get(k).k);
                fVec.x0 = x0_filt;
                fVec.z0 = z0_filt;
                fVec.tx = tx_filt;
                fVec.tz = tz_filt;
                sv.setStateVecPosAtMeasSite(k, fVec, mv.measurements.get(k), swimmer); 
                sv.trackTraj.get(k).resi = mv.dh(k, fVec);
        }
    }
    
    public void init(double x0, double z0,double tx,double tz, double units, double[][] cov, AKFitter kf,
            List<Surface> measSurfaces) {
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
        for(int j =0; j<5; j++) {
            mv.delta_d_a[j]=cov[j][j];
        }
        //take first plane along beam line with n = y-dir;
        sv.Layer.add(0);
        sv.Sector.add(0);
        this.NDF = -4;
        for (int i = 1; i < mv.measurements.size(); i++) {
            sv.Layer.add(mv.measurements.get(i).layer);
            sv.Sector.add(mv.measurements.get(i).sector);
            if(mv.measurements.get(i).skip==false) {
                this.NDF++;
            }
            Point3D ref = new Point3D(0.0, 0.0, 0.0);
        } 
        sv.init(x0, z0, tx, tz, units, cov, kf);
        //double x0, double z0,double tx,double tz, double units, Matrix cov, KFitter kf
    }

}
