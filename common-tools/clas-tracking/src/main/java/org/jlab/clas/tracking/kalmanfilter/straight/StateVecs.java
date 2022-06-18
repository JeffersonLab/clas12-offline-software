package org.jlab.clas.tracking.kalmanfilter.straight;

import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.tracking.kalmanfilter.AMeasVecs;
import org.jlab.clas.tracking.kalmanfilter.AMeasVecs.MeasVec;
import org.jlab.clas.tracking.kalmanfilter.AStateVecs;
import org.jlab.clas.tracking.kalmanfilter.Surface;
import org.jlab.clas.tracking.kalmanfilter.Units;
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
    public boolean getStateVecPosAtMeasSite(StateVec vec, MeasVec mv, Swim swim) {
        
        if(mv.surface==null) return false;
        
        Point3D ref = new Point3D(vec.x0,vec.y0,vec.z0);
        Vector3D u = new Vector3D(vec.tx, 1, vec.tz).asUnit(); 

        if(mv.k==0) {
            vec.x = vec.x0-vec.y0*vec.tx;
            vec.y = 0;
            vec.z = vec.z0-vec.y0*vec.tz;
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
                vec.path = inters.distance(ref);
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
                vec.path = cylInt.distance(ref);
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
        /* Assumes the state vector is (x, z, tx, ty)
         * with (x0, 0, z0) being the track intercept with the y=0 plane;
         * x = x0 + dl*tx;      xi = x0 + yi*tx;                                  xf = xi + (yf-yi)*tx;
         * y = y0 + dl;     =>  yi = dl;            for iVec->fVec, i.e. yi->yf   zf = zi + (yf-yi)*tz;
         * z = z0 + dl*tz;      zi = z0 + yi*tz;                                  txf = tx; tzf = tz;           
        */                                                                      
        double[][] FMat = new double[][]{
            { 1, 0, (fVec.y-iVec.y),               0,   0},
            { 0, 1,               0, (fVec.y-iVec.y),   0},
            { 0, 0,               1,               0,   0},
            { 0, 0,               0,               1,   0},
            { 0, 0,               0,               0,   1}
        };
        return FMat;
    }

    @Override
    public double[][] Q(StateVec vec, AMeasVecs mv) {

        double[][] Q = new double[5][5];

//        int dir = (int) Math.signum(f-i);
//        if(dir<0) return Q;
                
        Surface surf = mv.measurements.get(vec.k).surface;
        double cosEntranceAngle = this.getLocalDirAtMeasSite(vec, mv.measurements.get(vec.k));

        double p = 1;
        
        // Highland-Lynch-Dahl formula
        double sctRMS = surf.getThetaMS(p, mass, cosEntranceAngle);
        double cov_txtx = (1 + vec.tx * vec.tx) * (1 + vec.tx * vec.tx + vec.tz * vec.tz) * sctRMS * sctRMS;
        double cov_tztz = (1 + vec.tz * vec.tz) * (1 + vec.tx * vec.tx + vec.tz * vec.tz) * sctRMS * sctRMS;
        double cov_txtz = vec.tx * vec.tz * (1 + vec.tx * vec.tx + vec.tz * vec.tz) * sctRMS * sctRMS;
        Q = new double[][]{
            {0, 0, 0,        0,        0},
            {0, 0, 0,        0,        0},
            {0, 0, cov_txtx, cov_txtz, 0},
            {0, 0, cov_txtz, cov_tztz, 0},
            {0, 0, 0,        0,        0}
        };
        
        return Q;
    }
    
    @Override
    public void printlnStateVec(StateVec S) {
        String s = String.format("%d) x0=%.4f y0=%.4f z0=%.4f tx=%.4f tz=%.4f dl=%.4f", S.k, S.x0, S.y0, S.z0, S.tx, S.tz, S.dl);
        s       += String.format("    x=%.4f y=%.4f z=%.4f px=%.4f py=%.4f pz=%.4f", S.x, S.y, S.z, S.px, S.py, S.pz);
        System.out.println(s);
    }   

    @Override
    public void init(Helix trk, double[][] cov, double xref, double yref, double zref, double mass, Swim swimmer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void init(double x0, double z0, double tx, double tz, Units units, double[][] cov) {

        this.units = units;

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
        double[][] FMat = new double[][]{
            {1, 0, 0, 0, 0},
            {0, 1, 0, 0, 0},
            {0, 0, 1, 0, 0},
            {0, 0, 0, 1, 0},
            {0, 0, 0, 0, 1}
        };
        initSV.F = FMat;
        this.trackTrajT.clear();
        this.trackTrajF.clear();
        this.trackTrajP.clear();
        this.trackTrajB.clear();
        this.trackTrajS.clear();
        this.trackTrajT.put(0, new StateVec(initSV));
        
    }

    @Override
    public void corrForEloss(int dir, StateVec iVec, AMeasVecs mv) {

    }

}
