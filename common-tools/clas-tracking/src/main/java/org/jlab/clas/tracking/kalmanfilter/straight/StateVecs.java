package org.jlab.clas.tracking.kalmanfilter.straight;

import org.jlab.clas.tracking.kalmanfilter.straight.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jlab.geom.prim.Vector3D;
import org.jlab.geom.prim.Point3D;
import Jama.Matrix;
import org.jlab.clas.pdg.PhysicsConstants;
import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.tracking.kalmanfilter.straight.MeasVecs.MeasVec;
import org.jlab.clas.tracking.trackrep.Helix;
import org.jlab.clas.tracking.trackrep.Helix.Units;
import org.jlab.geom.prim.Line3D;


public class StateVecs {

    private Helix util ;
    public double units;
    public double lightVel;
    
    public List<B> bfieldPoints = new ArrayList<B>();
    public Map<Integer, StateVec> trackTraj = new HashMap<Integer, StateVec>();
    public Map<Integer, CovMat> trackCov = new HashMap<Integer, CovMat>();

    public StateVec StateVec;
    public CovMat CovMat;
    public Matrix F;
    MeasVecs mv = new MeasVecs();

    public List<Double> X0;
    public List<Double> Y0;
    public List<Double> Z0; // reference points
    public double shift; // target shift
    public List<Integer> Layer;
    public List<Integer> Sector;

    double[] value = new double[4]; // x,y,z,phi
    double[] swimPars = new double[7];
    B Bf = new B(0);

    public double[] getStateVecPosAtMeasSite(int k, StateVec iVec, MeasVec mv, Swim swim, 
            boolean useSwimmer) {
        if(swimPars!=null) {
            this.resetArrays(swimPars);
        } else {
            swimPars = new double[7];
        }
        if(value!=null) {
            this.resetArrays(value);
        } else {
            value = new double[4];
        }
        
        Point3D ps = new Point3D(0,0,0) ;

        StateVec kVec = new StateVec(k);
        
        kVec.x0 = iVec.x0;
        kVec.z0 = iVec.z0;
        kVec.tx = iVec.tx;
        kVec.tz = iVec.tz;
        kVec.dl = iVec.dl;

        if(mv.surface!=null) {
            double x = X0.get(0) + kVec.x0 ;
            double y = Y0.get(0) ;
            double z = Z0.get(0) + kVec.z0;
            
            Point3D ref = new Point3D(x,y,z);
            
            double tx = kVec.tx;
            double tz = kVec.tz;
            
            double py = (double) mv.hemisphere/Math.sqrt(1+tx*tx+tz*tz);
            double px = tx*py;
            double pz = tz*py;
            
            Vector3D u = new Vector3D(px, py, pz); 
            
            if(k==0) {
                value[0] = x;
                value[1] = y;
                value[2] = z;
                value[3] = 0.0;
                return value;
            }
            
            if(mv.hemisphere!=0) {
                if(mv.surface.plane!=null) {
                    Line3D toPln = new Line3D(ref,u);
                    Point3D inters = new Point3D();
                    int ints = mv.surface.plane.intersection(toPln, inters);
                    kVec.x = inters.x()  ;
                    kVec.y = inters.y()  ;
                    kVec.z = inters.z()  ;
                    kVec.dl = ref.distance(inters);
                    
                }
                if(mv.surface.cylinder!=null) {
                    Point3D offset = mv.surface.cylShift;
                    Vector3D rotation = mv.surface.cylRotation;
                    ref.translateXYZ(-offset.x(), -offset.y(), -offset.z());
                    ref.rotateZ(-rotation.z());
                    ref.rotateY(-rotation.y());
                    ref.rotateX(-rotation.x());
                    u.rotateZ(-rotation.z());
                    u.rotateY(-rotation.y());
                    u.rotateX(-rotation.x());
                    double r = 0.5*(mv.surface.cylinder.baseArc().radius()+mv.surface.cylinder.highArc().radius());
                    double delta = Math.sqrt((ref.x()*u.x()+ref.y()*u.y())*(ref.x()*u.x()+ref.y()*u.y())
                            -(-r*r+ref.x()*ref.x()+ref.y()*ref.y())*(u.x()*u.x()+u.y()*u.y()));
                    double l = (-(ref.x()*u.x()+ref.y()*u.y())+delta)/(u.x()*u.x()+u.y()*u.y());
                    if(Math.signum(ref.y()+l*u.y())!=mv.hemisphere) {
                        l = (-(ref.x()*u.x()+ref.y()*u.y())-delta)/(u.x()*u.x()+u.y()*u.y()); 
                    } 
                    
                    Point3D cylInt = new Point3D(ref.x()+l*u.x(),ref.y()+l*u.y(),ref.z()+l*u.z());
                    cylInt.translateXYZ(offset.x(), offset.y(), offset.z());
                    cylInt.rotateZ(rotation.z());
                    cylInt.rotateY(rotation.y());
                    cylInt.rotateX(rotation.x());
                    ref.translateXYZ(offset.x(), offset.y(), offset.z());
                    ref.rotateZ(rotation.z());
                    ref.rotateY(rotation.y());
                    ref.rotateX(rotation.x());
                    u.rotateZ(rotation.z());
                    u.rotateY(rotation.y());
                    u.rotateX(rotation.x());
                    kVec.dl = l;
                    kVec.x = cylInt.x();
                    kVec.y = cylInt.y();
                    kVec.z = cylInt.z();
                    
                }
                
                value[0] = kVec.x;
                value[1] = kVec.y;
                value[2] = kVec.z;
                value[3] = kVec.dl ;
                
                return value;
            } 
        }
        return value;
    }

    public void setStateVecPosAtMeasSite(int k, StateVec kVec, MeasVec mv, Swim swimmer) {

        double[] pars = this.getStateVecPosAtMeasSite(k, kVec, mv, swimmer, true);
        if (pars == null) {
            return;
        }
        kVec.x = pars[0];
        kVec.y = pars[1];
        kVec.z = pars[2];
        kVec.dl = pars[3];
    }

    public StateVec newStateVecAtMeasSite(int k, StateVec kVec, MeasVec mv, Swim swimmer, boolean useSwimmer) {

        StateVec newVec = kVec;
        double[] pars = this.getStateVecPosAtMeasSite(k, kVec, mv, swimmer, useSwimmer);
        if (pars == null) {
            return null;
        }

        newVec.x = pars[0];
        newVec.y = pars[1];
        newVec.z = pars[2];
        newVec.dl = pars[3];
        // new state:
        return newVec;
    }
    private void tranState(int f, StateVec iVec, Swim swimmer) {

    }

    public StateVec transported(int i, int f, StateVec iVec, MeasVec mv,
            Swim swimmer) {

        // transport stateVec...
        StateVec fVec = new StateVec(f);

        fVec.x0 = iVec.x0;
        fVec.z0 = iVec.z0;
        fVec.x = iVec.x;
        fVec.y = iVec.y;
        fVec.z = iVec.z;
        fVec.tx = iVec.tx;
        fVec.tz = iVec.tz;
        fVec.dl = iVec.dl;

        this.newStateVecAtMeasSite(f, fVec, mv, swimmer, true);

        return fVec;
    }

    public void transport(int i, int f, StateVec iVec, CovMat icovMat, MeasVec mv,
            Swim swimmer) {
       
        StateVec fVec = this.transported(i, f, iVec, mv, swimmer);
        
        double[][] FMat = new double[][]{
            {1, 0, 0, 0, 0},
            {0, 1, 0, 0, 0},
            {0, 0, 1, 0, 0},
            {0, 0, 0, 1, 0},
            {0, 0, 0, 0, 1}
        };

        //StateVec = fVec;
        this.trackTraj.put(f, fVec);
        F = new Matrix(FMat);
        Matrix FT = F.transpose();
        Matrix Cpropagated = FT.times(icovMat.covMat).times(F);
        if (Cpropagated != null) {
            CovMat fCov = new CovMat(f);
            fCov.covMat = Cpropagated.plus(this.Q(iVec, mv, (int)Math.signum(fVec.y-iVec.y)));
            //CovMat = fCov;
            this.trackCov.put(f, fCov);
        }
    }
    
    private Matrix Q(StateVec iVec, MeasVec mVec, int dir) {

        Matrix Q = new Matrix(new double[][]{
            {0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0}
        });

        // if (iVec.k % 2 == 1 && dir > 0) {
        if (dir >0 ) {
            Vector3D trkDir = this.P(iVec.k).asUnit();
            Vector3D trkPos = this.X(iVec.k);
            double x = trkPos.x();
            double y = trkPos.y();
            double z = trkPos.z();
            double ux = trkDir.x();
            double uy = trkDir.y();
            double uz = trkDir.z();
            double tx = ux/uy;
            double tz = uz/uy;
            double cosEntranceAngle = Math.abs((x * ux + y * uy + z * uz) / Math.sqrt(x * x + y * y + z * z));

            double p = Math.sqrt(ux * ux + uy * uy + uz * uz);
             double sctRMS = 0;
            double t_ov_X0 = mVec.l_over_X0;
            double mass = piMass;   // assume given mass hypothesis 
            double beta = p / Math.sqrt(p * p + mass * mass); // use particle momentum
            t_ov_X0 = t_ov_X0 / cosEntranceAngle;
            if(t_ov_X0!=0) {
            // Highland-Lynch-Dahl formula
                sctRMS = (0.136/(beta*PhysicsConstants.speedOfLight()*p))*Math.sqrt(t_ov_X0)*
                    (1 + 0.038 * Math.log(t_ov_X0));
             //sctRMS = ((0.141)/(beta*PhysicsConstants.speedOfLight()*p))*Math.sqrt(t_ov_X0)*
             //       (1 + Math.log(t_ov_X0)/9.);
            }
            double cov_txtx = (1 + tx * tx) * (1 + tx * tx + tz * tz) * sctRMS * sctRMS;
            double cov_tztz = (1 + tz * tz) * (1 + tx * tx + tz * tz) * sctRMS * sctRMS;
            double cov_txtz = tx * tz * (1 + tx * tx + tz * tz) * sctRMS * sctRMS;

            
            Q = new Matrix(new double[][]{
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 0, cov_txtx, cov_txtz, 0},
                {0, 0, cov_txtz, cov_tztz, 0},
                {0, 0, 0, 0, 0}
            });
            
            
        }

        return Q;
    }

    private StateVec reset(StateVec SV, StateVec stateVec) {
        SV = new StateVec(stateVec.k);
        SV.x = stateVec.x;
        SV.y = stateVec.y;
        SV.z = stateVec.z;
        SV.x0 = stateVec.x0;
        SV.z0 = stateVec.z0;
        SV.tx = stateVec.tx;
        SV.tz = stateVec.tz;
        SV.dl = stateVec.dl;

        return SV;
    }

    private void resetArrays(double[] swimPars) {
        for(int i = 0; i<swimPars.length; i++) {
            swimPars[i] = 0;
        }
    }

    
    
    public class StateVec {

        final int k;

        public double x0;
        public double z0;
        public double x;
        public double y;
        public double z;
        public double tx; //=px/py
        public double tz; //=pz/py
        public double dl;
        public double resi =0;
    
        StateVec(int k) {
            this.k = k;
        }
        private double[] _ELoss = new double[3];

        public double[] get_ELoss() {
            return _ELoss;
        }

        public void set_ELoss(double[] _ELoss) {
            this._ELoss = _ELoss;
        }

    }

    public class CovMat {

        final int k;
        public Matrix covMat;

        CovMat(int k) {
            this.k = k;
        }

    }

    public class B {

        final int k;
        public double x;
        public double y;
        public double z;
        public Swim swimmer;

        public double Bx;
        public double By;
        public double Bz;

        public double alpha;

        float b[] = new float[3];
        B(int k) {
            this.k = k;
        }
        B(int k, double x, double y, double z, Swim swimmer) {
            this.k = k;
            this.x = x;
            this.y = y;
            this.z = z;

            swimmer.BfieldLab(x/units, y/units, z/units + shift/units, b);
            this.Bx = b[0];
            this.By = b[1];
            this.Bz = b[2];
            
            this.alpha = 1. / (lightVel * Math.abs(b[2]));
        }

        public void set() {
            swimmer.BfieldLab(x/units, y/units, z/units + shift/units, b);
            this.Bx = b[0];
            this.By = b[1];
            this.Bz = b[2];

            this.alpha = 1. / (lightVel * Math.abs(b[2]));
        }
    }
    double piMass = 0.13957018;
    double KMass = 0.493677;
    double muMass = 0.105658369;
    double eMass = 0.000510998;
    double pMass = 0.938272029;
    

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
    public Vector3D X(int kf) {
        if (this.trackTraj.get(kf) != null) {
            double tx = this.trackTraj.get(kf).tx;
            double tz = this.trackTraj.get(kf).tz;
            
            double py = 1/Math.sqrt(1+tx*tx+tz*tz);
            double px = tx*py;
            double pz = tz*py;
            
            double x = X0.get(kf) + this.trackTraj.get(kf).x0 + px ;
            double y = Y0.get(kf)+this.trackTraj.get(kf).dl*py ;
            double z = Z0.get(kf) + this.trackTraj.get(kf).z0 + this.trackTraj.get(kf).dl*pz ;

            return new Vector3D(x, y, z);
        } else {
            return new Vector3D(0, 0, 0);
        }

    }
    
    
    public StateVec initSV = new StateVec(0);
    public void init(double x0, double z0, double tx, double tz, double units, Matrix cov, KFitter kf) {
        this.units = units;
        //this.lightVel = 0.0000299792458*units;
        //init stateVec
        //StateVec initSV = new StateVec(0);
        initSV.x0 = x0;
        initSV.z0 = z0;
        initSV.x = x0;
        initSV.y = 0;
        initSV.z = z0;
        initSV.tx = tx;
        initSV.tz = tz;
        initSV.dl = 0;
        this.trackTraj.put(0, initSV);
        CovMat initCM = new CovMat(0);
        Matrix covKF = cov.copy(); 

        initCM.covMat = covKF;
        this.trackCov.put(0, initCM);
    }

    public void printMatrix(Matrix C) {
        for (int k = 0; k < 5; k++) {
            System.out.println(C.get(k, 0) + "	" + C.get(k, 1) + "	" + C.get(k, 2) + "	" + C.get(k, 3) + "	" + C.get(k, 4));
        }
    }

    public void printlnStateVec(StateVec S) {
        System.out.println(S.k + ") x " + S.x + " y " + S.y + " z " + S.z + " tx " + S.tx + " tz " + S.tz + " dl " + S.dl );
    }
}
