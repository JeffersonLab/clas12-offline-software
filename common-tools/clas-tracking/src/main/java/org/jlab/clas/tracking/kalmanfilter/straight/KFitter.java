package org.jlab.clas.tracking.kalmanfilter.straight;

import org.jlab.clas.tracking.kalmanfilter.straight.*;
import java.util.ArrayList;
import java.util.List;

import java.util.HashMap;
import java.util.Map;

import org.jlab.geom.prim.Point3D;
import org.jlab.io.base.DataEvent;

import Jama.Matrix;
import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.tracking.kalmanfilter.Surface;
import org.jlab.clas.tracking.kalmanfilter.straight.StateVecs.StateVec;

public class KFitter {
    
    public static int polarity = -1;
    
    public boolean setFitFailed = true;

    StateVecs sv = new StateVecs();
    
    MeasVecs mv = new MeasVecs();
    public boolean filterOn = true;
    public StateVec finalStateVec;
    public double yx_slope;
    public double yz_slope;
    public double yx_interc;
    public double yz_interc;
    private double resiCut = 100;//residual cut for the measurements
    
    public void setMeasurements(List<Surface> measSurfaces) {
        mv.setMeasVecs(measSurfaces);
    }
    //double x0, double z0,double px,double py,double pz, double units, Matrix cov, KFitter kf
    public KFitter(double x0, double z0,double tx,double tz, double units, Matrix cov, KFitter kf,
            List<Surface> measSurfaces) {
        this.init(x0, z0, tx, tz, units, cov, kf, measSurfaces);
    }
    //private Matrix iCov;
    public void init(double x0, double z0,double tx,double tz, double units, Matrix cov, KFitter kf,
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
            mv.delta_d_a[j]=cov.get(j, j);
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

    /**
     * @return the resiCut
     */
    public double getResiCut() {
        return resiCut;
    }

    /**
     * @param resiCut the resiCut to set
     */
    public void setResiCut(double resiCut) {
        this.resiCut = resiCut;
    }

    public int totNumIter = 5;
    double newChisq = Double.POSITIVE_INFINITY;
                            
    public void runFitter(Swim swimmer) {
        double newchisq = Double.POSITIVE_INFINITY;
        this.NDF = sv.Layer.size()-5; 
        
        for (int it = 0; it < totNumIter; it++) {
            this.chi2 = 0;
            for (int k = 0; k < sv.Layer.size() - 1; k++) {
                if (sv.trackCov.get(k) == null || mv.measurements.get(k + 1) == null) {
                    return;
                } 
                sv.transport(k, k + 1, sv.trackTraj.get(k), sv.trackCov.get(k), mv.measurements.get(k+1), 
                        swimmer); 
                this.filter(k + 1, swimmer, 1); 
                
            }

            
            // chi2
            //this.chi2=this.calc_chi2(swimmer); 
            //int k1 = this.getk(swimmer); 
            if(sv.trackTraj.get(1)!=null) { 
                finalStateVec = sv.trackTraj.get(1);
                finalStateVec.x = finalStateVec.x0;
                finalStateVec.z = finalStateVec.z0;
                
                yx_slope = finalStateVec.tx;
                yz_slope = finalStateVec.tz;
                yx_interc = finalStateVec.x0;
                yz_interc = finalStateVec.z0;
               
                
                this.setTrajectory(swimmer);
                setFitFailed = false;
                //if(newchisq-this.chi2<0.0001)
                //    break;
                //newchisq = this.chi2;
            } else {
                //this.chi2 =newchisq ;
                //break;
            }
        }
       
    }
    
    private int getk(Swim swimmer) {
        int k = -1;
        double chi2 = Double.POSITIVE_INFINITY;
        for (int k1 = 1; k1 < sv.trackTraj.size(); k1++) {
            double newchi2 = this.calc_chi2(swimmer, k1);
            if(newchi2<chi2) {
                k = k1;
                chi2 = newchi2;
            }
        }
        this.chi2 = chi2;
        return k;
    }
    private double calc_chi2(Swim swimmer, int j) {
        //get the measurement
        double m = 0;
        //get the projector state
        double h = 0;
        double chi2 =0;
        m=0;
        h=0;
        int ndf = -5;
        StateVec iStateVec = sv.trackTraj.get(0);
        iStateVec.x0 = sv.trackTraj.get(j).x0;
        iStateVec.z0 = sv.trackTraj.get(j).z0;
        iStateVec.tx = sv.trackTraj.get(j).tx;
        iStateVec.tz = sv.trackTraj.get(j).tz;
        StateVec stv = sv.transported(0, 1, iStateVec, mv.measurements.get(1), swimmer);
        double dh = mv.dh(1, stv);
        //sv.trackTraj.get(1).resi = dh; 
        if(mv.measurements.get(1).skip==false) { 
            chi2 = dh*dh / mv.measurements.get(1).error;
            ndf++;
        }
        for(int k = 1; k< sv.Layer.size()-1; k++) { 
            if(mv.measurements.get(k+1).skip==false) {
                stv = sv.transported(k, k+1, stv, mv.measurements.get(k+1), swimmer);
                dh = mv.dh(k+1, stv);   
                //sv.trackTraj.get(k+1).resi = dh;
                chi2 += dh*dh / mv.measurements.get(k+1).error;
                ndf++;
            }
        }  
        return chi2;
    }
    
    public Map<Integer, HitOnTrack> TrjPoints = new HashMap<Integer, HitOnTrack>();

    public void setTrajectory(Swim swimmer) {
        TrjPoints.clear();
        double c2 = 0;
        for (int k = 0; k < sv.trackTraj.size()-1; k++) {
            StateVec stv = sv.transported(k, k+1, sv.trackTraj.get(k), mv.measurements.get(k+1), swimmer); 
            sv.setStateVecPosAtMeasSite(k+1, stv, mv.measurements.get(k+1), swimmer); 
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

    public double chi2 = 0;
    public int NDF = 0;
    
    private double calc_chi2(Swim swimmer) {
        //get the measurement
        double m = 0;
        //get the projector state
        double h = 0;
        double chi2 =0;
        m=0;
        h=0;
        int ndf = -5;
        StateVec stv = sv.trackTraj.get(1);
        double dh = sv.trackTraj.get(1).resi;
        if(mv.measurements.get(1).skip==false) { 
            chi2 = dh*dh / mv.measurements.get(1).error; 
            ndf++;
        }
        for(int k = 2; k< sv.Layer.size(); k++) { 
            if(mv.measurements.get(k).skip==false) {
                stv = sv.trackTraj.get(k);
                dh = sv.trackTraj.get(k).resi; 
                //sv.trackTraj.get(k+1).resi = dh; 
                chi2 += dh*dh / mv.measurements.get(k).error; 
                this.printStateVec(sv.trackTraj.get(k)); 
                ndf++;
            }
        }  
        return chi2;
    }
    
    private void filter(int k, Swim swimmer, int dir) {

        if (sv.trackTraj.get(k) != null && sv.trackCov.get(k).covMat != null 
                && mv.measurements.get(k).skip == false ) {

            double[] K = new double[5];
            double V = mv.measurements.get(k).error;
            
            //get the projector Matrix
            double[] H = new double[5];
            H = mv.H(sv.trackTraj.get(k), sv,  mv.measurements.get(k), swimmer, dir);
            double[][] HTGH = new double[][]{
                {H[0] * H[0] / V, H[0] * H[1] / V, H[0] * H[2] / V, H[0] * H[3] / V, H[0] * H[4] / V},
                {H[1] * H[0] / V, H[1] * H[1] / V, H[1] * H[2] / V, H[1] * H[3] / V, H[1] * H[4] / V},
                {H[2] * H[0] / V, H[2] * H[1] / V, H[2] * H[2] / V, H[2] * H[3] / V, H[2] * H[4] / V},
                {H[3] * H[0] / V, H[3] * H[1] / V, H[3] * H[2] / V, H[3] * H[3] / V, H[3] * H[4] / V},
                {H[4] * H[0] / V, H[4] * H[1] / V, H[4] * H[2] / V, H[4] * H[3] / V, H[4] * H[4] / V}
            };

            Matrix Ci = null;
            if (this.isNonsingular(sv.trackCov.get(k).covMat) == false) {
                //System.err.println("Covariance Matrix is non-invertible - quit filter!");
                return;
            }
            try {
                Ci = sv.trackCov.get(k).covMat.inverse();
            } catch (Exception e) {
                return;
            }
            Matrix Ca = null;
            try {
                Ca = Ci.plus(new Matrix(HTGH)); 
            } catch (Exception e) {
                return;
            }
            if (Ca != null && this.isNonsingular(Ca) == false) {
                //System.err.println("Covariance Matrix Ca is non-invertible - quit filter!");
                return;
            }

            if (Ca != null && this.isNonsingular(Ca) == true) {
                if (Ca.inverse() != null) {
                    Matrix CaInv = Ca.inverse();
                    sv.trackCov.get(k).covMat = CaInv;
                } else {
                    return;
                }
            } else {
                return;
            }

            for (int j = 0; j < 4; j++) {
                // the gain matrix
                K[j] = 0;
                if(filterOn) {
                    for (int i = 0; i < 4; i++) {
                        K[j] += H[i] * sv.trackCov.get(k).covMat.get(j, i) / V;
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
            
            /*
            double dh_filt = mv.dh(k, fVec);  
            
            //this.printStateVec(sv.trackTraj.get(k)); this.printStateVec(fVec);
            //System.out.println(" dh "+dh+" dh' "+dh_filt);
            sv.trackTraj.get(k).resi = dh;
            if (Math.abs(dh_filt) < Math.abs(dh) 
                    && Math.abs(dh_filt)/Math.sqrt(V)<this.getResiCut()) { 
                sv.trackTraj.get(k).resi = dh_filt;
                sv.trackTraj.get(k).x0 = x0_filt;
                sv.trackTraj.get(k).z0 = z0_filt;
                sv.trackTraj.get(k).tx = tx_filt;
                sv.trackTraj.get(k).tz = tz_filt;
                sv.trackTraj.get(k).dl = fVec.dl;
                sv.trackTraj.get(k).x = fVec.x;
                sv.trackTraj.get(k).y = fVec.y;
                sv.trackTraj.get(k).z = fVec.z;  
            } else {
                //this.NDF--;
                //mv.measurements.get(k).skip = true;
            } */
        }
    }

   

    /**
     * prints the matrix -- used for debugging
     *
     * @param C matrix
     */
    public void printMatrix(Matrix C) {
        System.out.println("    ");
        for (int k = 0; k < 5; k++) {
            System.out.println(C.get(k, 0)+"	"+C.get(k, 1)+"	"+C.get(k, 2)+"	"+C.get(k, 3)+"	"+C.get(k, 4));
        }
        System.out.println("    ");
    }

    private boolean isNonsingular(Matrix mat) {
        double matDet = mat.det(); 
        if (Math.abs(matDet) < 1.e-30) {
            return false;
        } else {
            return true;
        }
        /*
	      for (int j = 0; j < mat.getColumnDimension(); j++) {
	        // if (Math.abs(mat.get(j, j)) < 0.00000000001) {
	         if (Math.abs(mat.get(j, j)) < 0.0000000001) {
	            return false;
	         }
	      }
         */

    }

    private void printStateVec(StateVec sv) {
        System.out.println(sv.k+"]  ");
        System.out.println((float)sv.x0+", "+
            (float)sv.z0+", "+
            (float)sv.tx+", "+
            (float)sv.tz+", "+
            " xyz "+new Point3D(sv.x,sv.y,sv.z).toString());
        System.out.println("  ");
    }

    public class HitOnTrack {

        public int layer;
        public double x;
        public double y;
        public double z;
        public double px;
        public double py;
        public double pz;
        public double resi;
        public boolean isMeasUsed = true;
        
        HitOnTrack(int layer, double x, double y, double z, double px, double py, double pz, double resi) {
            this.layer = layer;
            this.x = x;
            this.y = y;
            this.z = z;
            this.px = px;
            this.py = py;
            this.pz = pz;
            this.resi = resi;
        }
    }

}
