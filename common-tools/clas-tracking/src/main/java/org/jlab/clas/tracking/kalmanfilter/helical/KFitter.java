package org.jlab.clas.tracking.kalmanfilter.helical;

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
import org.jlab.clas.tracking.utilities.MatrixOps.Libr;

/**
 *
 * @author ziegler
 */
public class KFitter extends AKFitter {
    public Map<Integer, HitOnTrack> TrjPoints = new HashMap<>();
    private StateVecs sv = new StateVecs();
    private MeasVecs  mv = new MeasVecs();
    public StateVecs.StateVec finalStateVec = null;    
    public Helix KFHelix;

    
    public KFitter(boolean filter, int iterations, boolean beamspot, Swim swim, Libr mo) {
        super(filter, iterations, beamspot, swim, mo);
    }
        
    public final void init(Helix helix, double[][] cov, 
                           double Xb, double Yb, double Zref, 
                           List<Surface> measSurfaces) {
        finalStateVec = null;
        KFHelix = null;
        this.NDF0 = -5;
        this.NDF  = -5;
        this.chi2 = Double.POSITIVE_INFINITY;
        this.numIter = 0;
        this.setFitFailed = false;
        mv.setMeasVecs(measSurfaces);
        for (int i = 1; i < mv.measurements.size(); i++) {
            if(mv.measurements.get(i).skip==false) {
                this.NDF++;
            }
        } 
        this.setXb(Xb);
        this.setYb(Yb);
        sv.init( helix, cov, Xb, Yb, Zref, this.getSwimmer());
    }
        
    public void runFitter() {
        this.runFitter(sv, mv);
    }
    
    @Override
    public void runFitter(AStateVecs sv, AMeasVecs mv) {
        
        StateVecs.StateVec finalSVonPivot = null;
        for (int it = 0; it < totNumIter; it++) {
            this.runFitterIter(sv, mv);

            // chi2
            double newchisq = this.calc_chi2(sv, mv); 
            // if curvature is 0, fit failed
            if(Double.isNaN(newchisq) ||
               sv.trackTraj.get(0)==null ||
               sv.trackTraj.get(0).kappa==0 || 
               Double.isNaN(sv.trackTraj.get(0).kappa)) {
                this.setFitFailed = true;
                break;
            }
            // if chi2 improved and curvature is non-zero, save fit results but continue iterating
            else if(newchisq < this.chi2) {
                this.chi2 = newchisq;
                finalSVonPivot = sv.new StateVec(sv.trackTraj.get(0));
                this.setTrajectory(sv, mv);
            }
            // stop if chi2 got worse
            else {
                break;
            }
        }
        if(!this.setFitFailed) {
            finalStateVec = sv.new StateVec(finalSVonPivot);
            finalStateVec.setPivot(this.getXb(), this.getYb(), 0);
            finalStateVec.covMat = this.sv.propagateCovMat(finalSVonPivot, finalStateVec);
            KFHelix = finalStateVec.getOldHelix();
        }
    }

    @Override
    public void setTrajectory(AStateVecs s, AMeasVecs mv) {
        TrjPoints = new HashMap<>();
        for (int k = 1; k < s.trackTraj.size(); k++) {
            int layer   = mv.measurements.get(k).layer;
            double resi = mv.dh(k, s.trackTraj.get(k));
            TrjPoints.put(layer, new HitOnTrack(layer, s.trackTraj.get(k), resi));
            if(mv.measurements.get(k).skip)
                TrjPoints.get(layer).isMeasUsed = false;
            //System.out.println(" Traj layer "+layer+" x "+TrjPoints.get(layer).x+" y "+TrjPoints.get(layer).y+" z "+TrjPoints.get(layer).z);
        }
    }

    @Override
    public void filter(int k, AStateVecs sv, AMeasVecs mv) {
        if (sv.trackTraj.get(k) != null && sv.trackTraj.get(k).covMat != null 
                && mv.measurements.get(k).skip == false && filterOn) {

            double[] K = new double[5];
            double V = mv.measurements.get(k).error*mv.measurements.get(k).error;

            double dh = mv.dh(k, sv.trackTraj.get(k));
//            System.out.println(dh);
            //get the projector Matrix
            double[] H = mv.H(sv.trackTraj.get(k), sv,  mv.measurements.get(k), this.getSwimmer());
//            System.out.println(k + " " + mv.measurements.get(k).layer + " " + H[0] + " " + H[1] + " " + H[2] + " " + H[3] + " " + H[4] + " " +dh );
            
            double[][] CaInv =  this.getMatrixOps().filterCovMat(H, sv.trackTraj.get(k).covMat, V);
            if (CaInv != null) {
                    sv.trackTraj.get(k).covMat = CaInv;
                } else {
                    return;
            }
            
            for (int j = 0; j < 5; j++) {
                // the gain matrix
                K[j] = 0;
                for (int i = 0; i < 5; i++) {
                    K[j] += H[i] * sv.trackTraj.get(k).covMat[j][i] / V;
                } 
            }
            if(sv.straight) {
                    K[2] = 0;
            }

            StateVec fVec = sv.new StateVec(sv.trackTraj.get(k));
            
            if (!Double.isNaN(dh)) {
//                for (int j = 0; j < 5; j++) {
//                    for (int i = 0; i < 5; i++) {
//                        System.out.print(CaInv[j][i] + " ");
//                    }
//                    System.out.println();
//                }
//                System.out.println(k + " " + CaInv[0][0] + " " + CaInv[1][1] + " " + CaInv[2][2] + " " + CaInv[3][3] + " " + CaInv[4][4] + " " + V );
//                System.out.println(k + " " + H[0] + " " + H[1] + " " + H[2] + " " + H[3] + " " + H[4] + " " +dh );
//                System.out.println(k + " " + K[0] + " " + K[1] + " " + K[2] + " " + K[3] + " " + K[4] + " " +dh );
                fVec.d_rho -= K[0] * dh;
                fVec.phi0  -= K[1] * dh;
                fVec.kappa -= K[2] * dh;
                fVec.dz    -= K[3] * dh;
                fVec.tanL  -= K[4] * dh;
            }

            if(this.getSwimmer()!=null && !sv.straight) fVec.rollBack(mv.rollBackAngle);
            fVec.updateFromHelix();
//            sv.printlnStateVec(fVec);
            sv.setStateVecPosAtMeasSite(fVec, mv.measurements.get(k), this.getSwimmer()); 
//            sv.printlnStateVec(fVec);

            double dh_filt = mv.dh(k, fVec); 
//            System.out.println(dh_filt + " " + dh);
            if (!Double.isNaN(dh_filt) 
             && Math.abs(dh_filt) < 10*Math.abs(dh) 
             && Math.abs(dh_filt)/Math.sqrt(V)<this.getResiCut()) { 
                sv.trackTraj.get(k).copy(fVec);
            } else {
                this.NDF--;
                mv.measurements.get(k).skip = true;
            }
        }
    }

}
