package org.jlab.clas.tracking.kalmanfilter.helical;

import java.util.ArrayList;
import java.util.List;

import java.util.HashMap;
import java.util.Map;

import org.jlab.geom.prim.Point3D;
import org.jlab.io.base.DataEvent;

import Jama.Matrix;
import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.tracking.kalmanfilter.Surface;
import org.jlab.clas.tracking.kalmanfilter.helical.StateVecs.StateVec;
import org.jlab.clas.tracking.trackrep.Helix;

public class KFitter {
    
    public static int polarity = -1;
    
    public boolean setFitFailed = true;

    StateVecs sv = new StateVecs();
    
    MeasVecs mv = new MeasVecs();
    
    public StateVec finalStateVec;
    
    private double _Xb;
    private double _Yb;
    private double resiCut = 100;//residual cut for the measurements
    
    public void setMeasurements(List<Surface> measSurfaces) {
        mv.setMeasVecs(measSurfaces);
    }
    
    public KFitter(Helix helix, Matrix cov, DataEvent event, Swim swimmer, double Xb, double Yb, 
            double Zref, List<Surface> measSurfaces) {
        _Xb = Xb;
        _Yb = Yb;
        this.init(helix, cov, event, swimmer, Xb, Yb, 
             Zref, mv, measSurfaces);
    }
    //private Matrix iCov;
    public void init(Helix helix, Matrix cov, DataEvent event, Swim swimmer, double Xb, double Yb, 
            double Zref, MeasVecs mv, List<Surface> measSurfaces) {
        
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
        sv.Z0.add(Zref); 
        for (int i = 1; i < mv.measurements.size(); i++) {
            sv.Layer.add(mv.measurements.get(i).layer);
            sv.Sector.add(mv.measurements.get(i).sector);
            Point3D ref = new Point3D(Xb, Yb, Zref);
            sv.X0.add(ref.x());
            sv.Y0.add(ref.y());
            sv.Z0.add(ref.z());
        }
        sv.init( helix, cov, this, swimmer);
        this.NDF = mv.measurements.size()-6;
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
        this.NDF = sv.X0.size()-5; 
        
        for (int it = 0; it < totNumIter; it++) {
            this.chi2 = 0;
            for (int k = 0; k < sv.X0.size() - 1; k++) {
                if (sv.trackCov.get(k) == null || mv.measurements.get(k + 1) == null) {
                    return;
                } 
                sv.transport(k, k + 1, sv.trackTraj.get(k), sv.trackCov.get(k), mv.measurements.get(k+1), 
                        swimmer); 
                this.filter(k + 1, swimmer, 1); 
            }
            
            for (int k =  sv.X0.size() - 1; k>0 ;k--) {
                if (sv.trackCov.get(k) == null || mv.measurements.get(k - 1) == null) {
                    return;
                }
                sv.transport(k, k - 1, sv.trackTraj.get(k), sv.trackCov.get(k), mv.measurements.get(k-1), 
                        swimmer);
                 if(k>1)
                    this.filter(k - 1, swimmer, -1);
            }

            // chi2
            this.chi2=this.calc_chi2(swimmer); 
            if(this.chi2<newchisq) { 
                newchisq=this.chi2;
                KFHelix = sv.setTrackPars();
                finalStateVec = sv.trackTraj.get(0);
                //KFHelix = sv.getHelixAtBeamLine(1, swimmer); 
                this.setTrajectory();
                setFitFailed = false;
            } else {
                this.chi2 =newchisq ;
                break;
            }
        }
       
    }
    public Map<Integer, HitOnTrack> TrjPoints = new HashMap<Integer, HitOnTrack>();

    public void setTrajectory() {
        TrjPoints.clear();
        for (int k = 1; k < sv.trackTraj.size(); k++) {
            int layer = mv.measurements.get(k).layer;
            double x = sv.trackTraj.get(k).x;
            double y = sv.trackTraj.get(k).y;
            double z = sv.trackTraj.get(k).z;
            double azi = sv.trackTraj.get(k).phi0 + sv.trackTraj.get(k).phi;
            //System.out.println("Trj "+x+","+y+","+z);
            double invKappa = 1. / Math.abs(sv.trackTraj.get(k).kappa);
            double px = -invKappa * Math.sin(azi);
            double py = invKappa * Math.cos(azi);
            double pz = invKappa * sv.trackTraj.get(k).tanL;
            TrjPoints.put(layer, new HitOnTrack(layer, x, y, z, px, py, pz));
            if(mv.measurements.get(k).skip)
                TrjPoints.get(layer).isMeasUsed = false;
            //System.out.println(" Traj layer "+layer+" x "+TrjPoints.get(layer).x+" y "+TrjPoints.get(layer).y+" z "+TrjPoints.get(layer).z);
        }
    }

    
    public Helix KFHelix;
    
    
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
        StateVec stv = sv.transported(0, 1, sv.trackTraj.get(0), mv.measurements.get(1), swimmer);
        double dh = mv.dh(1, stv);
        if(mv.measurements.get(1).skip==false) {
            chi2 = dh*dh / mv.measurements.get(1).error;
            ndf++;
        }
        for(int k = 1; k< sv.X0.size()-1; k++) {
            if(mv.measurements.get(k+1).skip==false) {
                stv = sv.transported(k, k+1, stv, mv.measurements.get(k+1), swimmer);
                dh = mv.dh(k+1, stv);
                chi2 += dh*dh / mv.measurements.get(k+1).error;
                ndf++;
            }
        }  
        return chi2;
    }
    
    private void filter(int k, Swim swimmer, int dir) {

        if (sv.trackTraj.get(k) != null && sv.trackCov.get(k).covMat != null 
                && mv.measurements.get(k).skip == false) {

            double[] K = new double[5];
            double V = mv.measurements.get(k).error;

            double dh = mv.dh(k, sv.trackTraj.get(k));
            
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
                    //System.err.println("Error: e");
                } else {
                    return;
                }
            } else {
                return;
            }
            
            for (int j = 0; j < 5; j++) {
                // the gain matrix
                K[j] = 0;
                for (int i = 0; i < 5; i++) {
                    K[j] += H[i] * sv.trackCov.get(k).covMat.get(j, i) / V;
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
        System.out.println((float)sv.d_rho+", "+
            (float)sv.phi0+", "+
            (float)sv.kappa+", "+
            (float)sv.dz+", "+
            (float)sv.tanL+" xyz "+new Point3D(sv.x,sv.y,sv.z)+" phi "+Math.toDegrees(Math.atan2(sv.y,sv.x))+" theta "+Math.toDegrees(Math.atan2(sv.y,sv.z-sv.dz)));
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
        public boolean isMeasUsed = true;
        
        HitOnTrack(int layer, double x, double y, double z, double px, double py, double pz) {
            this.layer = layer;
            this.x = x;
            this.y = y;
            this.z = z;
            this.px = px;
            this.py = py;
            this.pz = pz;

        }
    }

}
