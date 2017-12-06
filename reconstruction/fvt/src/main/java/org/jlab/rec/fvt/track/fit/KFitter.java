package org.jlab.rec.fvt.track.fit;

import Jama.Matrix; 
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.dc.trajectory.DCSwimmer;
import org.jlab.rec.fvt.track.Track;
import org.jlab.rec.fvt.track.fit.StateVecs.StateVec;

public class KFitter {

    public boolean setFitFailed = false;

    StateVecs sv = new StateVecs();
    MeasVecs mv = new MeasVecs();

    public StateVec finalStateVec;

    public KFitter(Track trk, DataEvent event, DCSwimmer swim) {
        this.init(trk, event,  swim);
    }
    
    
    public void init(Track trk, DataEvent event, DCSwimmer swim) {
        mv.setMeasVecs(trk);
        //mv.setMeasVecs(event, swim);
        sv.Z = new double[mv.measurements.size()];
        sv.Z[0]= trk.getZ();
        for (int i = 1; i < mv.measurements.size(); i++) {
            sv.Z[i] = mv.measurements.get(i).z;
        }
        
       // 
        sv.init(trk, this);
    }

    public int totNumIter = 4;
    double newChisq = Double.POSITIVE_INFINITY;

    public void runFitter(Track trk) {
        DCSwimmer dcSwim = new DCSwimmer();
        dcSwim.SetSwimParameters(trk.getX(), trk.getY(), trk.getZ(), trk.getPx(), trk.getPy(), trk.getPz(), trk.getQ());
	
        double theta_n = ((double)(trk.get_Sector()-1))*Math.toRadians(60.);
        double x_n = Math.cos(theta_n) ; 
        double y_n = Math.sin(theta_n) ;  
        double[] Vt = dcSwim.SwimToPlaneBoundary(0, new Vector3D(x_n, y_n, 0), -1);

        this.chi2 = 0;
        this.NDF = sv.Z.length - 5;
        //if(sv.Z.length<2)
         //   return;
        //System.err.println(" C init");
        //this.printMatrix(sv.trackCov.get(0).covMat);
        for (int i = 1; i <= totNumIter; i++) {
            //if (i > 1) {
            //    sv.transport(sv.Z.length - 1, 0, sv.trackTraj.get(sv.Z.length - 1), sv.trackCov.get(sv.Z.length - 1)); //get new state vec at 1st measurement after propagating back from the last filtered state
           // }
            
            //System.out.println(i+" )INIT "+sv.trackTraj.get(0).x+","+sv.trackTraj.get(0).y+","+sv.trackTraj.get(0).z+" chi2 "+this.chi2 );
            this.chi2 =0;
            for (int k = 0; k < sv.Z.length - 1; k++) {
                if(sv.trackTraj.get(k)==null || sv.trackCov.get(k)==null)
                    return;
                sv.transport(k, k + 1, sv.trackTraj.get(k), sv.trackCov.get(k), false);
               
                //double[] swV = dcSwim.SwimToPlaneLab(sv.trackTraj.get(k+1).z);
                //sv.trackTraj.add(k+1, sv.StateVec); 
                //sv.trackCov.add(k+1, sv.CovMat);
               // this.printMatrix(sv.trackCov.get(k).covMat);
               // System.out.println("-------------------------------------");
                //System.out.println((k+1)+"] trans "+sv.trackTraj.get(k+1).x+","+sv.trackTraj.get(k+1).y+","+
                //		sv.trackTraj.get(k+1).z+","+sv.trackTraj.get(k+1).tx+","+sv.trackTraj.get(k+1).ty+" Q "+sv.trackTraj.get(k+1).Q); 
                this.filter(k + 1);
                //if(sv.trackCov.get(k+1)!=null && sv.trackCov.get(k+1).covMat!=null)
                //    this.printMatrix(sv.trackCov.get(k+1).covMat);System.err.println(" --------------------------");
               // System.out.println((k+1)+"] filt "+sv.trackTraj.get(k+1).x+","+sv.trackTraj.get(k+1).y+","+
                //		sv.trackTraj.get(k+1).z+","+sv.trackTraj.get(k+1).tx+","+sv.trackTraj.get(k+1).ty+" Q "+sv.trackTraj.get(k+1).Q); 
            }
            if(i>1) {
	           // this.calcFinalChisq();
	            if(this.chi2>10000)
	            	i = totNumIter;
	            if (this.chi2 < newChisq) {
	               // this.finalStateVec = sv.trackTraj.get(sv.Z.length - 1);
	                newChisq = this.chi2;
	            } else {
	            	i = totNumIter;
	            }
	        }
            //System.out.println("iteration number "+i+" chi2 "+ newChisq);
            if(i<totNumIter) {
                sv.transport(sv.trackTraj.get(sv.Z.length - 1).k, 0, sv.trackTraj.get(sv.trackTraj.get(sv.Z.length - 1).k), sv.trackCov.get(sv.trackTraj.get(sv.Z.length - 1).k), false);
                //System.err.println(" C 0 at iter "+i);
                //this.printMatrix(sv.trackCov.get(0).covMat);
            }
            //sv.setFittedTrackPars( trk, sv.trackTraj.get(1));
        }
        sv.setFittedTrackPars( trk, sv.trackTraj.get(sv.Z.length - 1));
    }
    public double chi2 = 0;
    public int NDF = 0;

    private void filter(int k) {
        //System.out.println(".............Trying to filter....."+(sv.trackTraj.get(k) == null)+
         //       " "+(sv.trackCov.get(k) == null) +" meas sz "+mv.measurements.get(k).size);
        if (sv.trackTraj.get(k) != null && sv.trackCov.get(k) != null && mv.measurements.get(k).size>0) {
            
            double[] K = new double[5];
            double V = mv.measurements.get(k).error;

            //get the measurement
            double m = 0;
            //get the projector state
            
            m = mv.measurements.get(k).centroid;
            double h = mv.h(sv.trackTraj.get(k));
            if(Math.abs(m-h)>100) {
            //    System.err.println(" exiting KF ... meas out of range!!!");
                return;
            }
            
            //get the projector Matrix
            double[] H = new double[5];
            H = mv.H(sv.trackTraj.get(k), sv);
           /* for (int j = 0; j < 5; j++) {
                // the gain matrix
               System.out.println(" simple K ["+j+" ] = "+(float) (H[0]*sv.trackCov.get(k).covMat.get(j, 0)+H[1]*sv.trackCov.get(k).covMat.get(j, 1))/(V + H[0]*(H[0]*sv.trackCov.get(k).covMat.get(0, 0)+H[1]*sv.trackCov.get(k).covMat.get(1, 0) )
                        + H[1]*(H[0]*sv.trackCov.get(k).covMat.get(1, 0)+H[1]*sv.trackCov.get(k).covMat.get(1, 1))) );
               System.out.println(" simple K NORM ["+j+" ] = "+(float) (V + H[0]*(H[0]*sv.trackCov.get(k).covMat.get(0, 0)+H[1]*sv.trackCov.get(k).covMat.get(1, 0) )
                        + H[1]*(H[0]*sv.trackCov.get(k).covMat.get(1, 0)+H[1]*sv.trackCov.get(k).covMat.get(1, 1))) 
                         +" off diag el j,0 "+sv.trackCov.get(k).covMat.get(j, 0)+" i,1 "+sv.trackCov.get(k).covMat.get(j, 1));
            }
            */
            double[][] HTGH = new double[][]{
                {H[0] * H[0] / V, H[0] * H[1] / V, H[0] * H[2] / V, H[0] * H[3] / V, H[0] * H[4] / V},
                {H[1] * H[0] / V, H[1] * H[1] / V, H[1] * H[2] / V, H[1] * H[3] / V, H[1] * H[4] / V},
                {H[2] * H[0] / V, H[2] * H[1] / V, H[2] * H[2] / V, H[2] * H[3] / V, H[2] * H[4] / V},
                {H[3] * H[0] / V, H[3] * H[1] / V, H[3] * H[2] / V, H[3] * H[3] / V, H[3] * H[4] / V},
                {H[4] * H[0] / V, H[4] * H[1] / V, H[4] * H[2] / V, H[4] * H[3] / V, H[4] * H[4] / V}
            };
            //System.out.println(" --- i---------------");this.printMatrix(sv.trackCov.get(k).covMat);System.out.println(" ------------------");
            Matrix Ci = null;
            //System.err.println("HtGH");this.printMatrix(new Matrix(HTGH));System.err.println("-------------------------------\n");
            if (this.isNonsingular(sv.trackCov.get(k).covMat) == false) {
                System.err.println("Covariance Matrix is non-invertible - quit filter! at "+sv.trackCov.get(k).k);
                this.printMatrix(sv.trackCov.get(k).covMat);
                return;
            }
            //System.err.println("Covariance Matrix");
            //this.printMatrix(sv.trackCov.get(k).covMat);
            try {
                Ci = sv.trackCov.get(k).covMat.inverse();
            } catch (Exception e) {
                return;
            }
            //System.err.println("Ci");
            //this.printMatrix(Ci);
            Matrix Ca = null;
            try {
                Ca = Ci.plus(new Matrix(HTGH));
            } catch (Exception e) {
                return;
            }
            
            //this.printMatrix(Ca);
            if (Ca != null && this.isNonsingular(Ca) == false) {
                System.err.println(sv.trackCov.get(k).k+") Ca Matrix is non-invertible - quit filter!");
                this.printMatrix(Ca);
                System.err.println("C");
                this.printMatrix(sv.trackCov.get(k).covMat);
                System.err.println("HTGH");
                this.printMatrix(new Matrix(HTGH));
                
                return;
            }
            if (Ca != null && this.isNonsingular(Ca) == true) {
                if (Ca.inverse() != null) {
                    Matrix CaInv = Ca.inverse();
                    sv.trackCov.get(k).covMat = CaInv;
                    //System.err.println("new Covariance Matrix");
                    //this.printMatrix(sv.trackCov.get(k).covMat);
                    //System.err.println("Error: e");
                } else {
                    System.err.println("Error: null matrix");
                    return;
                }
            } else {
                System.err.println("Error: non invertable matrix");
                return;
            }
            
            for (int j = 0; j < 5; j++) {
                // the gain matrix
                K[j] = 0;
                for (int i = 0; i < 5; i++) {
                    K[j] += H[i] * sv.trackCov.get(k).covMat.get(j, i) / V ;
                      
                //System.out.println((float)sv.trackTraj.get(k).z+" H["+i+" ] = "+H[i]+" sv.trackCov.get(k).covMat.get("+j+","+i+") = "+(sv.trackCov.get(k).covMat.get(j, i) ) +")-->K ["+j+" ] = "+K[j]);
                }
                //System.out.println(k+")K ["+j+" ] = "+(float)K[j]+ " (m - h) "+(float)(m-h)+" add to stateVec "+(float)(K[j] * (m - h))+" H[0] "+(float)H[0]+" H[1] "+(float)H[1]+" H[2] "+(float)H[2]+" H[3] "+(float)H[3]);
            }
            
            
            
            double x_filt    = sv.trackTraj.get(k).x;
            double y_filt    = sv.trackTraj.get(k).y;
            double tx_filt   = sv.trackTraj.get(k).tx;
            double ty_filt   = sv.trackTraj.get(k).ty;
            double Q_filt    = sv.trackTraj.get(k).Q;
            
            //sv.trackTraj.get(k).x = x_filt;
            //sv.trackTraj.get(k).y = y_filt;
            //sv.trackTraj.get(k).tx = tx_filt;
            //sv.trackTraj.get(k).ty = ty_filt;
            //sv.trackTraj.get(k).Q = Q_filt;
           
            x_filt  += 1.*K[0] * (m - h);
            y_filt  += 1.*K[1] * (m - h);
            tx_filt += 0.*K[2] * (m - h); 
            ty_filt += 0.*K[3] * (m - h);
            Q_filt  += 1.*K[4] * (m - h);
            

            StateVec fVec = sv.new StateVec(sv.trackTraj.get(k).k);
            fVec.x  = x_filt;
            fVec.y  = y_filt;
            fVec.z = sv.trackTraj.get(k).z;
            fVec.tx = tx_filt;
            fVec.ty = ty_filt;
            fVec.Q  = Q_filt;
            
            double f_h = mv.h(fVec);
           // System.out.println("k "+fVec.k+" orig resid "+((m - h) * (m - h) / V)+" filt residual "+((m - f_h) * (m - f_h) / V)+" m "+m+" h "+h+" size"+mv.measurements.get(k).size);
            //System.out.println(m+"   "+h+"   "+mv.measurements.get(k).size+"   "+mv.measurements.get(k).layer);
            
            if ((m - f_h) * (m - f_h) / V <= (m - h) * (m - h) / V ) {
                sv.trackTraj.get(k).x = fVec.x;
                sv.trackTraj.get(k).y = fVec.y;
                sv.trackTraj.get(k).tx = fVec.tx;
                sv.trackTraj.get(k).ty = fVec.ty;
                sv.trackTraj.get(k).Q = fVec.Q;
                
                chi2 += (m - f_h) * (m - f_h) / V;
               
                //	sv.trackTraj.put(k, fVec);
            } else {
                chi2 += (m - h) * (m - h) / V;
                
            }
            //chi2+=(mv.measurements.get(k).centroid - f_h)*(mv.measurements.get(k).centroid - f_h)/V;

        }
    }


    @SuppressWarnings("unused")
	private void smooth(int k) {
        this.chi2 = 0;
        if (sv.trackTraj.get(k) != null && sv.trackCov.get(k).covMat != null) {
            sv.transport(k, 0, sv.trackTraj.get(k), sv.trackCov.get(k), true);
            for (int k1 = 0; k1 < k; k1++) {
                sv.transport(k1, k1 + 1, sv.trackTraj.get(k1), sv.trackCov.get(k1), true);
                this.filter(k1 + 1);
            }
        }
    }

    private void calcFinalChisq() {
        int k = sv.Z.length - 1;
        this.chi2 = 0;
        if (sv.trackTraj.get(k) != null && sv.trackCov.get(k).covMat != null) {
            sv.rinit(0, k);
            for (int k1 = 0; k1 < k; k1++) {
                sv.transport(k1, k1 + 1, sv.trackTraj.get(k1), sv.trackCov.get(k1), true);
                double V = mv.measurements.get(k1 + 1).error; 
                double f_h = mv.h(sv.trackTraj.get(k1 + 1));
                double m = mv.measurements.get(k).centroid;
                 chi2 += (m - f_h) * (m - f_h) / V;
            }
        }
    }

    /**
     * prints the matrix -- used for debugging
     *
     * @param C matrix
     */
    public void printMatrix(Matrix C) {
        for (int k = 0; k < 5; k++) {
            System.out.println(C.get(k, 0) + "	" + C.get(k, 1) + "	" + C.get(k, 2) + "	" + C.get(k, 3) + "	" + C.get(k, 4));
        }
    }

    private boolean isNonsingular(Matrix mat) {
    /*
        for (int j = 0; j < mat.getColumnDimension(); j++) {
            if (mat.get(j, j) < 0.00000000001) {
                return false;
            }
        }
        return true;
    */
        double matDet = mat.det();
        if(Math.abs(matDet)< 1.e-50) {
                return false;
        } else {
                return true;
        }
    }
    
    
}
