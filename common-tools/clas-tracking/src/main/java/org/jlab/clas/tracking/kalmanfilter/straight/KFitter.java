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
        this.NDF0 = -4;
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
    }
    
    @Override
    public void runFitter(AStateVecs sv, AMeasVecs mv) {
        for (int it = 0; it < totNumIter; it++) {
            this.runFitterIter(sv, mv);
            
            // chi2
            double newchisq = this.calc_chi2(sv, mv); 
            // if curvature is 0, fit failed
            if(Double.isNaN(newchisq) || sv.trackTraj.get(0)==null) {
                this.setFitFailed = true;
                break;
            }
            else if(newchisq < this.chi2) {
                this.chi2 = newchisq;
                this.setTrajectory(sv, mv);
                setFitFailed = false;
            }
            // stop if chi2 got worse
            else {
                break;
            }
        }
        if(!this.setFitFailed) {
            finalStateVec = sv.new StateVec(sv.trackTraj.get(0));
        }
    }

    @Override
    public void setTrajectory(AStateVecs sv, AMeasVecs mv) {
        TrjPoints = new HashMap<>();
        for (int k = 0; k < sv.trackTraj.size()-1; k++) {
            StateVec stv = sv.trackTraj.get(k+1);
            stv.resi = mv.dh(k+1, stv);
            if(Double.isNaN(stv.resi)) {
                mv.measurements.get(k+1).skip = true;
            }
            else {
                int layer = mv.measurements.get(k+1).layer;
                TrjPoints.put(layer, new HitOnTrack(layer, stv.x, stv.y, stv.z, stv.px, stv.py, stv.pz, stv.resi));
                if(mv.measurements.get(k).skip)
                    TrjPoints.get(layer).isMeasUsed = false;
            }
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
//            System.out.println(k + " " + mv.measurements.get(k).layer  + " " + mv.measurements.get(k).surface.type.name() + " " + H[0] + " " + H[1] + " " + H[2] + " " + H[3]);

            double[][] CaInv =  this.getMatrixOps().filterCovMat(H, sv.trackTraj.get(k).covMat, V);
            if (CaInv != null) {
                    sv.trackTraj.get(k).covMat = CaInv;
                } else {
                    return;
            }
            // the gain matrix
            for (int j = 0; j < 4; j++) {
                K[j] = 0;
                for (int i = 0; i < 4; i++) {
                    K[j] += H[i] * sv.trackTraj.get(k).covMat[j][i] / V;
                } 
            }
//            for (int j = 0; j < 5; j++) {
//                for (int i = 0; i < 5; i++) {
//                    System.out.print(CaInv[j][i] + " ");
//                }
//                System.out.println();
//            }
//            System.out.println(k + " " + mv.measurements.get(k).layer  + " " + mv.measurements.get(k).surface.type.name() + " " + V);
//            System.out.println("\t" + H[0] + " " + H[1] + " " + H[2] + " " + H[3]);
//            System.out.println("\t" + sv.trackTraj.get(k).covMat[0][0] + " " + sv.trackTraj.get(k).covMat[1][1] + " " + sv.trackTraj.get(k).covMat[2][2] + " " + sv.trackTraj.get(k).covMat[3][3]);
//            System.out.println("\t" + K[0] + " " + K[1] + " " + K[2] + " " + K[3]);

            double dh = mv.dh(k, sv.trackTraj.get(k));

            if (!Double.isNaN(dh)) {
                StateVec fVec = sv.new StateVec(sv.trackTraj.get(k));
                fVec.x0 -= K[0] * dh;
                fVec.z0 -= K[1] * dh;
                fVec.tx -= K[2] * dh;
                fVec.tz -= K[3] * dh;
                fVec.updateFromRay();
                sv.setStateVecPosAtMeasSite(fVec, mv.measurements.get(k), null); 
                sv.trackTraj.replace(k, fVec);
                sv.trackTraj.get(k).resi = mv.dh(k, fVec);  
            }
            else {
                this.NDF--;
                mv.measurements.get(k).skip = true;
            }
        }
    }

}
