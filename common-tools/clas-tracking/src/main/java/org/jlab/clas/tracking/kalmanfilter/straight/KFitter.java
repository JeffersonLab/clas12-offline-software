package org.jlab.clas.tracking.kalmanfilter.straight;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.clas.tracking.kalmanfilter.AKFitter;
import org.jlab.clas.tracking.kalmanfilter.AMeasVecs;
import org.jlab.clas.tracking.kalmanfilter.AStateVecs;
import org.jlab.clas.tracking.kalmanfilter.AStateVecs.StateVec;
import org.jlab.clas.tracking.kalmanfilter.Surface;
import org.jlab.clas.tracking.utilities.MatrixOps.Libr;

/**
 *
 * @author ziegler
 */
public class KFitter extends AKFitter {

    public double yx_slope;
    public double yz_slope;
    public double yx_interc;
    public double yz_interc;
    private final double resiCut = 100;//residual cut for the measurements
    public Map<Integer, HitOnTrack> TrjPoints = new HashMap<>();
    public final StateVecs sv = new StateVecs();
    public final MeasVecs  mv = new MeasVecs();
    public StateVecs.StateVec finalStateVec;

    public KFitter(boolean filter, int iterations, boolean beamspot, Libr m) {
        super(filter, iterations, beamspot, null, m);
    }
    
    public void runFitter() {
        this.runFitter(sv, mv);
    }
    
    public void init(double x0, double z0,double tx,double tz, double units, double[][] cov, List<Surface> measSurfaces) {
        finalStateVec = null;
        this.NDF0 = -45;
        this.NDF  = -4;
        this.chi2 = Double.POSITIVE_INFINITY;
        this.numIter = 0;
        this.setFitFailed = false;
        mv.setMeasVecs(measSurfaces);
        for(int j =0; j<5; j++) {
            mv.delta_d_a[j]=cov[j][j];
        }
        for (int i = 1; i < mv.measurements.size(); i++) {
            if(mv.measurements.get(i).skip==false) {
                this.NDF++;
            }
        } 
        sv.init(x0, z0, tx, tz, units, cov);
        //double x0, double z0,double tx,double tz, double units, Matrix cov, KFitter kf
    }
    
    @Override
    public void runFitter(AStateVecs sv, AMeasVecs mv) {
        for (int it = 0; it < totNumIter; it++) {
            this.runFitterIter(sv, mv);
            
            // chi2
            double newchisq = this.calc_chi2(sv, mv); 
            // if curvature is 0, fit failed
            if(sv.trackTraj.get(1)==null) {
                this.setFitFailed = true;
                break;
            }
            else if(newchisq < this.chi2) {
                this.chi2 = newchisq;
                finalStateVec = sv.trackTraj.get(1);
                finalStateVec.x = finalStateVec.x0;
                finalStateVec.z = finalStateVec.z0;
                
                yx_slope = finalStateVec.tx;
                yz_slope = finalStateVec.tz;
                yx_interc = finalStateVec.x0;
                yz_interc = finalStateVec.z0;
                               
                this.setTrajectory(sv, mv);
                setFitFailed = false;
            }
            // stop if chi2 got worse
            else {
                break;
            }
        }
    }

    @Override
    public void setTrajectory(AStateVecs sv, AMeasVecs mv) {
        TrjPoints = new HashMap<>();
        for (int k = 0; k < sv.trackTraj.size()-1; k++) {
            StateVec stv = sv.trackTraj.get(k+1);
            double resi = mv.dh(k+1, stv);
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
    }

    @Override
    public void filter(int k, AStateVecs sv, AMeasVecs mv) {
        if (sv.trackTraj.get(k) != null && sv.trackTraj.get(k).covMat != null 
                && mv.measurements.get(k).skip == false && this.filterOn) {
            double[] K = new double[5];
            double V = mv.measurements.get(k).error*mv.measurements.get(k).error;

            //get the projector Matrix
            double[] H = new double[5];
            H = mv.H(sv.trackTraj.get(k), sv,  mv.measurements.get(k), null);

            double[][] CaInv =  this.getMatrixOps().filterCovMat(H, sv.trackTraj.get(k).covMat, V);
            if (CaInv != null) {
                    sv.trackTraj.get(k).covMat = CaInv;
                } else {
                    return;
            }
            for (int j = 0; j < 4; j++) {
                    // the gain matrix
                    K[j] = 0;
                    if(filterOn) {
                        for (int i = 0; i < 4; i++) {
                            K[j] += H[i] * sv.trackTraj.get(k).covMat[j][i] / V;
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
                sv.setStateVecPosAtMeasSite(fVec, mv.measurements.get(k), null); 
                sv.trackTraj.get(k).resi = mv.dh(k, fVec);
        }
    }

}
