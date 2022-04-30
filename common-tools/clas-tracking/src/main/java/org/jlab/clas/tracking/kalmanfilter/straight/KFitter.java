package org.jlab.clas.tracking.kalmanfilter.straight;

import java.util.List;
import org.jlab.clas.tracking.kalmanfilter.AKFitter;
import org.jlab.clas.tracking.kalmanfilter.AMeasVecs;
import org.jlab.clas.tracking.kalmanfilter.AStateVecs;
import org.jlab.clas.tracking.kalmanfilter.AStateVecs.StateVec;
import org.jlab.clas.tracking.kalmanfilter.Surface;
import org.jlab.clas.tracking.kalmanfilter.Units;
import org.jlab.clas.tracking.utilities.MatrixOps.Libr;

/**
 *
 * @author ziegler
 */
public class KFitter extends AKFitter {

    public final StateVecs sv = new StateVecs();
    public final MeasVecs  mv = new MeasVecs();
    public StateVec finalStateVec;

    public KFitter(boolean filter, int iterations, int dir, Libr m) {
        super(filter, iterations, dir, null, m);
    }
    
    public void runFitter() {
        this.runFitter(sv, mv);
    }
    
    public void init(double x0, double z0,double tx,double tz, Units units, double[][] cov, List<Surface> measSurfaces) {
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
            if(Double.isNaN(newchisq) || sv.smoothed().get(0)==null) {
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
            finalStateVec = sv.new StateVec(sv.smoothed().get(0));
        }
    }

    @Override
    public StateVec filter(int k, StateVec vec, AMeasVecs mv) {
        if (vec != null && vec.covMat != null) {
        
            StateVec fVec = sv.new StateVec(vec);
            
            if(mv.measurements.get(k).skip == false && filterOn) {
    
                double[] K = new double[5];
                double V = mv.measurements.get(k).error*mv.measurements.get(k).error;

                //get the projector Matrix
                double[] H = new double[5];
                H = mv.H(fVec, sv,  mv.measurements.get(k), null);
//                System.out.println(k + " " + mv.measurements.get(k).layer  + " " + mv.measurements.get(k).surface.type.name() + " " + H[0] + " " + H[1] + " " + H[2] + " " + H[3]);

                double[][] CaInv =  this.getMatrixOps().filterCovMat(H, fVec.covMat, V);
                if (CaInv != null) {
                        fVec.covMat = CaInv;
                    } else {
                        return null;
                }
                // the gain matrix
                for (int j = 0; j < 4; j++) {
                    K[j] = 0;
                    for (int i = 0; i < 4; i++) {
                        K[j] += H[i] * fVec.covMat[j][i] / V;
                    } 
                }
    //            for (int j = 0; j < 5; j++) {
    //                for (int i = 0; i < 5; i++) {
    //                    System.out.print(CaInv[j][i] + " ");
    //                }
    //                System.out.println();
    //            }
//                System.out.println(k + " " + mv.measurements.get(k).layer  + " " + mv.measurements.get(k).surface.type.name() + " " + V);
//                System.out.println("\t" + H[0] + " " + H[1] + " " + H[2] + " " + H[3]);
//                System.out.println("\t" + fVec.covMat[0][0] + " " + fVec.covMat[1][1] + " " + fVec.covMat[2][2] + " " + fVec.covMat[3][3]);
//                System.out.println("\t" + K[0] + " " + K[1] + " " + K[2] + " " + K[3]);

                double dh = mv.dh(k, fVec);

                if (!Double.isNaN(dh)) {
                    fVec.x0 -= K[0] * dh;
                    fVec.z0 -= K[1] * dh;
                    fVec.tx -= K[2] * dh;
                    fVec.tz -= K[3] * dh;
                    fVec.updateFromRay();
                    sv.setStateVecPosAtMeasSite(fVec, mv.measurements.get(k), null); 
                    fVec.residual = mv.dh(k, fVec);
                }
    //            System.out.println(dh_filt + " " + dh);
                if (Double.isNaN(fVec.residual) 
                 || Math.abs(fVec.residual) > Math.max(V, 10*Math.abs(dh))
                 || Math.abs(fVec.residual)/Math.sqrt(V)>this.getResidualsCut()) { 
                    this.NDF--;
                    mv.measurements.get(k).skip = true;
                }
            }
            return fVec;
        }
        return null;
    }

    @Override
    public StateVec smooth(int k, AStateVecs sv, AMeasVecs mv) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public StateVec smooth(StateVec v1, StateVec v2) {
        
        if(v1==null || v2==null) return null;
//        // move pivot of first state vector to match second
//        v1.setPivot(v2.x0, v2.y0, v2.z0);
        // get covariance matrices and arrays
        double[][] c1 = v1.covMat;
        double[][] c2 = v2.covMat;
        double[]   a1 = v1.getRayArray();
        double[]   a2 = v2.getRayArray();
        // smooth covariance matrices
        double[][] c1i = this.getMatrixOps().inverse(c1);
        double[][] c2i = this.getMatrixOps().inverse(c2);
        if(c1i == null || c2i == null) return null;
        double[][]  ci = this.getMatrixOps().mo.MatrixAddition(c1i, c2i);
        double[][]   c = this.getMatrixOps().inverse(ci);
        if(c == null) return null;
        // smooth state vectors
        double[] a = new double[a1.length];
        for(int i=0; i<a1.length; i++) {
            for(int j=0; j<a1.length; j++) {
                for(int k=0; k<a1.length; k++) {
                    a[i] += c[i][j]*(c1i[j][k]*a1[k]+c2i[j][k]*a2[k]);
    }
            }
        }
        // create averaged state vector
        StateVec vave = sv.new StateVec(v2);
        vave.x0 = a[0];
        vave.z0 = a[1];
        vave.tx = a[2];
        vave.tz = a[3];
        vave.covMat = c;
        vave.updateFromRay();
        sv.setStateVecPosAtMeasSite(vave, mv.measurements.get(vave.k), null); 
        
        return vave;
    }



}
