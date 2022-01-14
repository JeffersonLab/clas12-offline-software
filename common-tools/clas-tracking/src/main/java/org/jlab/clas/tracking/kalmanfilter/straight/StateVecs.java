package org.jlab.clas.tracking.kalmanfilter.straight;

import java.util.HashMap;
import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.tracking.kalmanfilter.AMeasVecs;
import org.jlab.clas.tracking.kalmanfilter.AMeasVecs.MeasVec;
import org.jlab.clas.tracking.kalmanfilter.AStateVecs;
import org.jlab.clas.tracking.trackrep.Helix;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

/**
 *
 * @author ziegler
 */
public class StateVecs extends AStateVecs {

    
    @Override
    public boolean getStateVecPosAtMeasSite(StateVec vec, AMeasVecs.MeasVec mv, Swim swim) {
        double[] value = new double[4];
        
        if(mv.surface==null) return false;
        
        Point3D ref = new Point3D(vec.x0,vec.y0,vec.z0);
        Vector3D u = new Vector3D(vec.px, vec.py, vec.pz).asUnit(); 

        if(mv.k==0) {
            vec.x = vec.x0;
            vec.y = vec.y0;
            vec.z = vec.z0;
            return true;
        }            
        else if(mv.hemisphere!=0) {
            if(mv.surface.plane!=null) {
                Line3D toPln = new Line3D(ref,u);
                Point3D inters = new Point3D();
                int ints = mv.surface.plane.intersection(toPln, inters);
                vec.x = inters.x()  ;
                vec.y = inters.y()  ;
                vec.z = inters.z()  ;
            }
            if(mv.surface.cylinder!=null) {
                mv.surface.toLocal().apply(ref);
                mv.surface.toLocal().apply(u);
                double r = 0.5*(mv.surface.cylinder.baseArc().radius()+mv.surface.cylinder.highArc().radius());
                double delta = Math.sqrt((ref.x()*u.x()+ref.y()*u.y())*(ref.x()*u.x()+ref.y()*u.y())
                        -(-r*r+ref.x()*ref.x()+ref.y()*ref.y())*(u.x()*u.x()+u.y()*u.y()));
                double l = (-(ref.x()*u.x()+ref.y()*u.y())+delta)/(u.x()*u.x()+u.y()*u.y());
                if(Math.signum(ref.y()+l*u.y())!=mv.hemisphere) {
                    l = (-(ref.x()*u.x()+ref.y()*u.y())-delta)/(u.x()*u.x()+u.y()*u.y()); 
                    }

                Point3D cylInt = new Point3D(ref.x()+l*u.x(),ref.y()+l*u.y(),ref.z()+l*u.z());
                mv.surface.toGlobal().apply(cylInt);
                vec.x = cylInt.x();
                vec.y = cylInt.y();
                vec.z = cylInt.z();
            } 
            return true;
        }
        else {
            return false;
        }
    }
    
    @Override
    public boolean setStateVecPosAtMeasSite(StateVec sv, MeasVec mv, Swim swimmer) {

        boolean status = this.getStateVecPosAtMeasSite(sv, mv, swimmer);
        if (!status) {
            return false;
        }
        sv.k = mv.k;
        sv.updateRay();
        return true;
    }

    @Override
    public double[][] F(StateVec iVec, StateVec fVec) {
        double[][] FMat = new double[][]{
            {1, 0, 0, 0, 0},
            {0, 1, 0, 0, 0},
            {0, 0, 1, 0, 0},
            {0, 0, 0, 1, 0},
            {0, 0, 0, 0, 1}
        };
        return FMat;
    }

    @Override
    public double[][] Q(int i, int f, StateVec iVec, AMeasVecs mv) {
        double[][] Q = new double[5][5];

        int dir = (int) Math.signum(f-i);
        if(dir<0) return Q;
        
        double t_ov_X0 = 0;
        // depending on dir, k increases or decreases
        for (int k = i; (k-f)*dir <= 0; k += dir) {
            int hemisphere = (int) mv.measurements.get(k).surface.hemisphere;
            if(dir*hemisphere>0 && k==f) continue;
            if(dir*hemisphere<0 && k==i) continue;
            double cosEntranceAngle = 1;//this.getLocalDirAtMeasSite(iVec, mv.measurements.get(k));
            t_ov_X0 += mv.measurements.get(k).l_over_X0 / cosEntranceAngle;
        }

        if (t_ov_X0>0) {
            double p    = 1;
            double mass = piMass;   // assume given mass hypothesis 
            double beta = p / Math.sqrt(p * p + mass * mass);
            // Highland-Lynch-Dahl formula
            double sctRMS = (0.0136/(beta*p))*Math.sqrt(t_ov_X0)*(1 + 0.038 * Math.log(t_ov_X0));        
            double cov_txtx = (1 + iVec.tx * iVec.tx) * (1 + iVec.tx * iVec.tx + iVec.tz * iVec.tz) * sctRMS * sctRMS;
            double cov_tztz = (1 + iVec.tz * iVec.tz) * (1 + iVec.tx * iVec.tx + iVec.tz * iVec.tz) * sctRMS * sctRMS;
            double cov_txtz = iVec.tx * iVec.tz * (1 + iVec.tx * iVec.tx + iVec.tz * iVec.tz) * sctRMS * sctRMS;
            Q = new double[][]{
                {0, 0, 0,        0,        0},
                {0, 0, 0,        0,        0},
                {0, 0, cov_txtx, cov_txtz, 0},
                {0, 0, cov_txtz, cov_tztz, 0},
                {0, 0, 0,        0,        0}
            };
        }
        return Q;
    }

    @Override
    public Vector3D P(int kf) {
        if (this.trackTraj.get(kf) != null) {
            double tx = this.trackTraj.get(kf).tx;
            double tz = this.trackTraj.get(kf).tz;
            
            double py = 1/Math.sqrt(1+tx*tx+tz*tz);
            double px = tx*py;
            double pz = tz*py;

            return new Vector3D(px, py, pz);
        } else {
            return new Vector3D(0, 0, 0);
        }
    }

    @Override
    public Vector3D X(int kf) {
        if (this.trackTraj.get(kf) != null) {
            double tx = this.trackTraj.get(kf).tx;
            double tz = this.trackTraj.get(kf).tz;
            
            double py = 1/Math.sqrt(1+tx*tx+tz*tz);
            double px = tx*py;
            double pz = tz*py;
            
            double x = this.trackTraj.get(kf).x0 + px ;
            double y = this.trackTraj.get(kf).dl*py ;
            double z = this.trackTraj.get(kf).z0 + this.trackTraj.get(kf).dl*pz ;

            return new Vector3D(x, y, z);
        } else {
            return new Vector3D(0, 0, 0);
        }
    }

    @Override
    public Vector3D X(StateVec kVec, double phi) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Vector3D P0(int kf) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Vector3D X0(int kf) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
public void printlnStateVec(StateVec S) {
        String s = String.format("%d) x0=%.4f y0=%.4f z0=%.4f tx=%.4f tz=%.4f dl=%.4f", S.k, S.x0, S.y0, S.z0, S.tx, S.tz, S.dl);
        s       += String.format("    x=%.4f y=%.4f z=%.4f px=%.4f py=%.4f pz=%.4f", S.x, S.y, S.z, S.px, S.py, S.pz);
        System.out.println(s);
    }   

    @Override
    public void init(Helix trk, double[][] cov, double xref, double yref, double zref, Swim swimmer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void init(double x0, double z0, double tx, double tz, double units, double[][] cov) {
        this.trackTraj = new HashMap<>();
        this.units = units;
        //this.lightVel = 0.0000299792458*units;
        //init stateVec
        //StateVec initSV = new StateVec(0);
        initSV = new StateVec(0);
        initSV.x0 = x0;
        initSV.y0 = 0;
        initSV.z0 = z0;
        initSV.tx = tx;
        initSV.tz = tz;
        initSV.dl = 0;
        initSV.updateFromRay();
        double[][] covKF = new double[5][5];    
        for(int ic = 0; ic<5; ic++) {
            for(int ir = 0; ir<5; ir++) {
                covKF[ic][ir]=cov[ic][ir];
            }
        }
        initSV.covMat = covKF;
        this.trackTraj.put(0, new StateVec(initSV));
    }

    @Override
    public Helix setTrackPars() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
