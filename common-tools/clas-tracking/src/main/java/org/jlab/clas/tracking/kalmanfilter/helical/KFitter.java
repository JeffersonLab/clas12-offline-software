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
    public Map<Integer, AKFitter.HitOnTrack> TrjPoints = new HashMap<>();
    private StateVecs sv = new StateVecs();
    private MeasVecs  mv = new MeasVecs();
    public StateVecs.StateVec finalStateVec = null;    
    public Helix KFHelix;

    
    public KFitter(boolean filter, int iterations, int dir, boolean beamspot, Swim swim, Libr mo) {
        super(filter, iterations, dir, beamspot, swim, mo);
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
        // System.out.println("******************************ITER "+totNumIter);
        StateVecs.StateVec finalSVonPivot = null;
        for (int it = 0; it < totNumIter; it++) {
            this.runFitterIter(sv, mv, dir);
            // chi2
            double newchisq = this.calc_chi2(sv, mv); 
            //System.out.println("******************************ITER "+(it+1)+" "+ newchisq+" <? "+this.chi2);
            // if curvature is 0, fit failed
            if(Double.isNaN(newchisq) ||
               sv.trackTrajS.get(0)==null ||
               sv.trackTrajS.get(0).kappa==0 || 
               Double.isNaN(sv.trackTrajS.get(0).kappa)) {
                this.setFitFailed = true;
                break;
            }
            
            // if chi2 improved and curvature is non-zero, save fit results but continue iterating
            else if(newchisq < this.chi2) {
                this.chi2 = newchisq;
                finalSVonPivot = sv.new StateVec(sv.trackTrajS.get(0));
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
            KFHelix = finalStateVec.getHelix(this.getXb(), this.getYb());
        }
    }

    @Override
    public void setTrajectory(AStateVecs s, AMeasVecs mv) {
        TrjPoints = new HashMap<>();
        for (int k = 0; k < s.trackTrajT.size(); k++) {
            int layer   = mv.measurements.get(k).layer;
            double resi = mv.dh(k, s.trackTrajT.get(k));
            if(!mv.measurements.get(k).surface.passive) {
                TrjPoints.put(layer, new HitOnTrack(layer, s.trackTrajT.get(k), resi));
                if(mv.measurements.get(k).skip)
                    TrjPoints.get(layer).isMeasUsed = false;
            }
            //System.out.println(" Traj layer "+layer+" x "+TrjPoints.get(layer).x+" y "+TrjPoints.get(layer).y+" z "+TrjPoints.get(layer).z);
        }
    }

    @Override
    public void filter(int k, AStateVecs sv, AMeasVecs mv) {
        if (sv.trackTrajT.get(k) != null && sv.trackTrajT.get(k).covMat != null) {
        
            sv.trackTrajF.put(k, sv.new StateVec(sv.trackTrajT.get(k)));
            
            if(mv.measurements.get(k).skip == false && filterOn) {

                double[] K = new double[5];
                double V = mv.measurements.get(k).error*mv.measurements.get(k).error;

                double dh = mv.dh(k, sv.trackTrajF.get(k));
    //            System.out.println(dh);
                //get the projector Matrix
                double[] H = mv.H(sv.trackTrajF.get(k), sv,  mv.measurements.get(k), this.getSwimmer());
    //            System.out.println(k + " " + mv.measurements.get(k).layer + " " + H[0] + " " + H[1] + " " + H[2] + " " + H[3] + " " + H[4] + " " +dh );

                double[][] CaInv =  this.getMatrixOps().filterCovMat(H, sv.trackTrajF.get(k).covMat, V);
                if (CaInv != null) {
                        sv.trackTrajF.get(k).covMat = CaInv;
                    } else {
                        return;
                }

                for (int j = 0; j < 5; j++) {
                    // the gain matrix
                    K[j] = 0;
                    for (int i = 0; i < 5; i++) {
                        K[j] += H[i] * sv.trackTrajF.get(k).covMat[j][i] / V;
                    } 
                }
                if(sv.straight) {
                        K[2] = 0;
                }

                StateVec fVec = sv.new StateVec(sv.trackTrajF.get(k));
                //sv.printlnStateVec(fVec);
                if (!Double.isNaN(dh) ) {
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
    // sv.printlnStateVec(fVec);
                sv.setStateVecPosAtMeasSite(fVec, mv.measurements.get(k), this.getSwimmer()); 
    // sv.printlnStateVec(fVec);
                double dh_filt = mv.dh(k, fVec); 
    //            System.out.println(dh_filt + " " + dh);
                if (!Double.isNaN(dh_filt) 
                 && Math.abs(dh_filt) < 10*Math.abs(dh) 
                 && Math.abs(dh_filt)/Math.sqrt(V)<this.getResiCut()) { 
                    sv.trackTrajF.get(k).copy(fVec);
                } else {
                    this.NDF--;
                    mv.measurements.get(k).skip = true;
                }
            }
        }
    }

    @Override
    public void smooth(int k, AStateVecs sv, AMeasVecs mv) {
        //State vector recursion formula :
        //-------------------------------
        //a_n_k = a_k + A_k(a_n_kp1 - a_k_kp1);
        //where:
        //a_n_k: smoothed at k, using n measurements
        //a_k: filtered at k
        //a_n_kp1: smoothed at k+1, using n measurements
        //a_k_kp1: transported from k to k+1 using observations from 1 to k;
        //and
        //Ak = C_k FT_k (C_k_kp1)^{-1}
        //where:
        //C_k_kp1: transported covariance matrix from k to k+1 using observations from 1 to k;
        //C_k: filtered covariance matrix at k
        //FT_k: transpose of del_f/del_a_k, the propagator matrix at k
        //====================================================================================
        //Covariance matrix recursion formula:
        //C_n_k = C_k + A_k(C_n_kp1 - C_k_kp1)AT_k;
        //where:
        //C_n_k: smoothed at k, using n measurements
        //C_k: filtered at k
        //C_n_kp1: smoothed at k+1, using n measurements
        //C_k_kp1: transported from k to k+1 using observations from 1 to k.

        StateVec a_k     = sv.trackTrajF.get(k);
        StateVec a_n_k   = sv.new StateVec(a_k);
        StateVec a_n_kp1 = sv.new StateVec(sv.trackTrajS.get(k+dir));
        StateVec a_k_kp1 = sv.new StateVec(sv.trackTrajT.get(k+dir));
        double[][] FMat  = a_n_k.F;
        double[][] FMatT = this.getMatrixOps().mo.MatrixTranspose(FMat);
        double[][] C_k     = a_k.covMat;
        double[][] C_n_kp1 = a_n_kp1.covMat;
        double[][] C_k_kp1 = a_k_kp1.covMat;
        //smoothed cov matrix 
        double[][] Ak = this.getMatrixOps().smoothingCorr(C_k, FMatT, C_k_kp1);
        if(Ak==null)
            return;
        double [][] C_n_k = this.getMatrixOps().smoothCovMat(C_n_kp1, C_k, Ak, C_k_kp1);

        double [] diffSvComps = a_k_kp1.subtractHelix(a_n_kp1);
        double[] r = new double[5];
        for(int i =0; i<5; i++) {
            for(int j =0; j<5; j++) { 
                r[i]+=Ak[i][j]*(diffSvComps[j]);
            }
            if(Double.isNaN(r[i]))
                return;
        }
        a_n_k.d_rho   -=r[0];
        a_n_k.phi0    -=r[1];
        a_n_k.kappa   -=r[2];
        a_n_k.dz      -=r[3];
        a_n_k.tanL    -=r[4];

        if(this.getSwimmer()!=null && !sv.straight) a_n_k.rollBack(mv.rollBackAngle);
        a_n_k.updateFromHelix();

        boolean stateOK = sv.setStateVecPosAtMeasSite(a_n_k, mv.measurements.get(k), this.getSwimmer()); 
        if(!stateOK) {
            return;
        } 

        a_n_k.covMat = C_n_k;
        sv.trackTrajS.put(k, a_n_k);
    }
}
