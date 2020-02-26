package org.jlab.rec.cvt.track.fit;

import java.util.ArrayList;
import java.util.List;

import org.jlab.geom.prim.Point3D;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.track.Seed;
import org.jlab.rec.cvt.track.Track;
import org.jlab.rec.cvt.track.fit.StateVecs.B;
import org.jlab.rec.cvt.track.fit.StateVecs.StateVec;
import org.jlab.rec.cvt.trajectory.Helix;

import Jama.Matrix;
import org.jlab.clas.swimtools.Swim;
import org.jlab.rec.cvt.Constants;

public class KFitter {

    public boolean setFitFailed = false;

    StateVecs sv = new StateVecs();
    MeasVecs mv = new MeasVecs();

    public StateVec finalStateVec;

    public KFitter(Seed trk, org.jlab.rec.cvt.svt.Geometry geo, Swim swimmer) {
        this.init(trk, geo, null, swimmer);
    }
    
    public KFitter(Seed trk, org.jlab.rec.cvt.svt.Geometry geo, DataEvent event, Swim swimmer) {
        this.init(trk, geo, event, swimmer);
    }

    public void init(Seed trk, org.jlab.rec.cvt.svt.Geometry geo, DataEvent event, Swim swimmer) {
        //Helix helix = trk.get_Helix();
        mv.setMeasVecs(trk, geo);
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
        sv.X0.add((double) Constants.getXb());
        sv.Y0.add((double) Constants.getYb());
        sv.Z0.add((double) 0.0);
        for (int i = 1; i < mv.measurements.size(); i++) {
            sv.Layer.add(mv.measurements.get(i).layer);
            sv.Sector.add(mv.measurements.get(i).sector);
            //Point3D ref = geo.intersectionOfHelixWithPlane(mv.measurements.get(i).layer, mv.measurements.get(i).sector,  helix) ;
            //ref = new Point3D(0,Constants.MODULERADIUS[mv.measurements.get(i).layer-1][0], 0);
            Point3D ref = new Point3D(Constants.getXb(), Constants.getYb(), 0);
            sv.X0.add(ref.x());
            sv.Y0.add(ref.y());
            sv.Z0.add(ref.z());
        }
        sv.init(trk, this, swimmer);
    }

    public int totNumIter = 5;
    double newChisq = Double.POSITIVE_INFINITY;

    public void runFitter(org.jlab.rec.cvt.svt.Geometry sgeo, org.jlab.rec.cvt.bmt.Geometry bgeo, Swim swimmer) {
        double newchisq = Double.POSITIVE_INFINITY;
        this.NDF = sv.X0.size()-5; 
        for (int it = 0; it < totNumIter; it++) {
            this.chi2 = 0;
            TrjPoints.clear();
            for (int k = 0; k < sv.X0.size() - 1; k++) {
                if (sv.trackCov.get(k) == null || mv.measurements.get(k + 1) == null) {
                    return;
                }
                sv.transport(k, k + 1, sv.trackTraj.get(k), sv.trackCov.get(k), sgeo, bgeo, mv.measurements.get(k + 1).type, swimmer);
                this.filter(k + 1, sgeo, bgeo, swimmer);
            }
            
            for (int k =  sv.X0.size() - 1; k>1 ;k--) {
                if (sv.trackCov.get(k) == null || mv.measurements.get(k - 1) == null) {
                    return;
                }
                sv.transport(k, k - 1, sv.trackTraj.get(k), sv.trackCov.get(k), sgeo, bgeo, mv.measurements.get(k - 1).type, swimmer);
                this.filter(k - 1, sgeo, bgeo, swimmer);
            }

            if (it < totNumIter - 1) {
              this.Rinit(swimmer); 
            }
            this.chi2=this.calc_chi2(sgeo);
            if(this.chi2<newchisq) {
                newchisq=this.chi2;
                KFHelix = sv.setTrackPars(1);
                this.setTrajectory();
            } else {
                this.chi2 =newchisq ;
                break;
            }
        }
       
        //KFHelix = sv.setTrackPars(sv.X0.size() - 1);
        //this.setTrajectory();
        
    }
    public List<HitOnTrack> TrjPoints = new ArrayList<HitOnTrack>();

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
            TrjPoints.add(new HitOnTrack(layer, x, y, z, px, py, pz));

//            System.out.println(" Traj layer "+layer+" x "+x+" y "+y+" z "+z);
        }
    }

    public void Rinit(Swim swimmer) {
        Helix helix = sv.setTrackPars(sv.X0.size() - 1);

        sv.trackTraj.get(0).x = -helix.get_dca() * Math.sin(helix.get_phi_at_dca());
        sv.trackTraj.get(0).y = helix.get_dca() * Math.cos(helix.get_phi_at_dca());
        sv.trackTraj.get(0).z = helix.get_Z0();
        double xcen = (1. / helix.get_curvature() - helix.get_dca()) * Math.sin(helix.get_phi_at_dca());
        double ycen = (-1. / helix.get_curvature() + helix.get_dca()) * Math.cos(helix.get_phi_at_dca());
        B Bf = sv.new B(0, 0, 0, 0, swimmer);
        sv.trackTraj.get(0).alpha = Bf.alpha;
        sv.trackTraj.get(0).kappa = Bf.alpha * helix.get_curvature();
        sv.trackTraj.get(0).phi0 = Math.atan2(ycen, xcen);
        if (sv.trackTraj.get(0).kappa < 0) {
            sv.trackTraj.get(0).phi0 = Math.atan2(-ycen, -xcen);
        }
        sv.trackTraj.get(0).dz = helix.get_Z0();
        sv.trackTraj.get(0).tanL = helix.get_tandip();
        sv.trackTraj.get(0).d_rho = helix.get_dca();
        sv.trackTraj.get(0).phi = 0;

        sv.trackCov.get(0).covMat = sv.trackCov.get(sv.X0.size() - 1).covMat;
    }
    public Helix KFHelix;
    
    public Track OutputTrack(Seed trk, org.jlab.rec.cvt.svt.Geometry geo, Swim swimmer) {
   
        Track cand = new Track(KFHelix, swimmer);
        
        if(cand.get_P()<0.05)
            this.setFitFailed = true;
        cand.setNDF(NDF);
        cand.setChi2(chi2);
        
        for (Cross c : trk.get_Crosses()) {
            if (c.get_Detector().equalsIgnoreCase("SVT")) {
                continue;
            }
           // System.out.println("output  track trajectory   "+this.TrjPoints.size());
            for (HitOnTrack h : this.TrjPoints) {
                //System.out.println(" hot : layer "+h.layer+" x "+h.x+" y "+h.y+" z "+h.z);
                if (c.get_Cluster1().get_Layer() == h.layer - 6) {
                    if (Math.sqrt(h.x * h.x + h.y * h.y) < 100) {
                        this.setFitFailed = true;
                    }
                    //System.err.println(c.get_Cluster1().get_Layer()+") error in traj "+this.TrjPoints.size());
//                    if (Double.isNaN(c.get_Point().x())) {
//                        c.set_Point(new Point3D(h.x, h.y, c.get_Point().z()));
//                    }
//                    if (Double.isNaN(c.get_Point().z())) {
//                        c.set_Point(new Point3D(c.get_Point().x(), c.get_Point().y(), h.z));
//                    }

                }
            }
        }
        cand.addAll(trk.get_Crosses());

//        cand.finalUpdate_Crosses(geo);

        return cand;
    }

    public double chi2 = 0;
    public int NDF = 0;

    private double calc_chi2(org.jlab.rec.cvt.svt.Geometry sgeo) {
        //get the measurement
        double m = 0;
        //get the projector state
        double h = 0;
        double chi2 =0;
        m=0;
        h=0;
        for(int k = 1; k< sv.X0.size(); k++) {
            
            if (mv.measurements.get(k).type == 0) {
                m = mv.measurements.get(k).centroid;
                h = mv.h(sv.trackTraj.get(k), sgeo);

            }
            if (mv.measurements.get(k).type == 1) {
                m = Math.atan2(mv.measurements.get(k).y, mv.measurements.get(k).x);
                h = mv.hPhi(sv.trackTraj.get(k));
            }
            if (mv.measurements.get(k).type == 2) {
                m = mv.measurements.get(k).z;
                h = mv.hZ(sv.trackTraj.get(k));
            }

            chi2 += (m - h) * (m - h) / mv.measurements.get(k).error;
        }  
       return chi2;

    }
    private void filter(int k, org.jlab.rec.cvt.svt.Geometry sgeo, org.jlab.rec.cvt.bmt.Geometry bgeo, Swim swimmer) {

        if (sv.trackTraj.get(k) != null && sv.trackCov.get(k).covMat != null) {

            double[] K = new double[5];
            double V = mv.measurements.get(k).error;

            //get the measurement
            double m = 0;
            //get the projector state
            double h = 0;
            if (mv.measurements.get(k).type == 0) {
                m = mv.measurements.get(k).centroid;
                h = mv.h(sv.trackTraj.get(k), sgeo);

            }
            if (mv.measurements.get(k).type == 1) {
                m = Math.atan2(mv.measurements.get(k).y, mv.measurements.get(k).x);
                h = mv.hPhi(sv.trackTraj.get(k));
            }
            if (mv.measurements.get(k).type == 2) {
                m = mv.measurements.get(k).z;
                h = mv.hZ(sv.trackTraj.get(k));
            }
            //get the projector Matrix
            double[] H = new double[5];
            H = mv.H(sv.trackTraj.get(k), sv, sgeo, bgeo, mv.measurements.get(k).type, swimmer);

            double[][] HTGH = new double[][]{
                {H[0] * H[0] / V, H[0] * H[1] / V, H[0] * H[2] / V, H[0] * H[3] / V, H[0] * H[4] / V},
                {H[1] * H[0] / V, H[1] * H[1] / V, H[1] * H[2] / V, H[1] * H[3] / V, H[1] * H[4] / V},
                {H[2] * H[0] / V, H[2] * H[1] / V, H[2] * H[2] / V, H[2] * H[3] / V, H[2] * H[4] / V},
                {H[3] * H[0] / V, H[3] * H[1] / V, H[3] * H[2] / V, H[3] * H[3] / V, H[3] * H[4] / V},
                {H[4] * H[0] / V, H[4] * H[1] / V, H[4] * H[2] / V, H[4] * H[3] / V, H[4] * H[4] / V}
            };

            Matrix Ci = null;
            //this.printMatrix(new Matrix(HTGH));System.err.println("-------------------------------\n");
            if (this.isNonsingular(sv.trackCov.get(k).covMat) == false) {
                //System.err.println("Covariance Matrix is non-invertible - quit filter!");
                //this.printMatrix(sv.trackCov.get(k).covMat);
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
                //System.err.println("Covariance Matrix is non-invertible - quit filter!");
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
            double drho_filt = sv.trackTraj.get(k).d_rho;
            double phi0_filt = sv.trackTraj.get(k).phi0;
            double kappa_filt = sv.trackTraj.get(k).kappa;
            double dz_filt = sv.trackTraj.get(k).dz;
            double tanL_filt = sv.trackTraj.get(k).tanL;
            if (mv.measurements.get(k).type == 0) {
                drho_filt += K[0] * (mv.measurements.get(k).centroid - h);
                phi0_filt += K[1] * (mv.measurements.get(k).centroid - h);
                kappa_filt += K[2] * (mv.measurements.get(k).centroid - h);
                dz_filt += K[3] * (mv.measurements.get(k).centroid - h);
                tanL_filt += K[4] * (mv.measurements.get(k).centroid - h);
            }
            if (mv.measurements.get(k).type == 1) {
                double phiM = Math.atan2(mv.measurements.get(k).y, mv.measurements.get(k).x);
                drho_filt += K[0] * (phiM - h);
                phi0_filt += K[1] * (phiM - h);
                kappa_filt += K[2] * (phiM - h);
                dz_filt += K[3] * (phiM - h);
                tanL_filt += K[4] * (phiM - h);
            }
            if (mv.measurements.get(k).type == 2) {
                drho_filt += K[0] * (mv.measurements.get(k).z - h);
                phi0_filt += K[1] * (mv.measurements.get(k).z - h);
                kappa_filt += K[2] * (mv.measurements.get(k).z - h);
                dz_filt += K[3] * (mv.measurements.get(k).z - h);
                tanL_filt += K[4] * (mv.measurements.get(k).z - h);
            }

            StateVec fVec = sv.new StateVec(sv.trackTraj.get(k).k);
            fVec.d_rho = drho_filt;
            fVec.phi0 = phi0_filt;
            fVec.kappa = kappa_filt;
            fVec.dz = dz_filt;
            fVec.tanL = tanL_filt;
            fVec.alpha = sv.trackTraj.get(k).alpha;

            sv.getStateVecAtModule(k, fVec, sgeo, bgeo, mv.measurements.get(k).type, swimmer);

            double f_h = 0;
            if (mv.measurements.get(k).type == 0) {
                f_h = mv.h(fVec, sgeo);
            }
            if (mv.measurements.get(k).type == 1) {
                f_h = mv.hPhi(fVec);
            }
            if (mv.measurements.get(k).type == 2) {
                f_h = mv.hZ(fVec);
            }

            if(Math.signum(sv.trackTraj.get(k).d_rho)!=Math.signum(drho_filt))
                return;
            if ((m - f_h) * (m - f_h) / V < (m - h) * (m - h) / V) {
                sv.trackTraj.get(k).d_rho = drho_filt;
                sv.trackTraj.get(k).phi0 = phi0_filt;
                sv.trackTraj.get(k).kappa = kappa_filt;
                sv.trackTraj.get(k).dz = dz_filt;
                sv.trackTraj.get(k).tanL = tanL_filt;
                sv.getStateVecAtModule(k, sv.trackTraj.get(k), sgeo, bgeo, mv.measurements.get(k).type, swimmer);
                //chi2 += (m - f_h) * (m - f_h) / V;

                //	sv.trackTraj.put(k, fVec);
            } else {
               // chi2 += (m - h) * (m - h) / V;
            }
            //chi2+=(mv.measurements.get(k).centroid - f_h)*(mv.measurements.get(k).centroid - f_h)/V;

        }
    }

    /**
     * prints the matrix -- used for debugging
     *
     * @param C matrix
     */
    public void printMatrix(Matrix C) {
        for (int k = 0; k < 5; k++) {
            //System.out.println(C.get(k, 0)+"	"+C.get(k, 1)+"	"+C.get(k, 2)+"	"+C.get(k, 3)+"	"+C.get(k, 4));
        }
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

    public class HitOnTrack {

        int layer;
        double x;
        double y;
        double z;
        double px;
        double py;
        double pz;

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
