package org.jlab.clas.tracking.kalmanfilter.straight;

import org.jlab.clas.pdg.PhysicsConstants;
import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.tracking.kalmanfilter.AKFitter;
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
    public double[] getStateVecPosAtMeasSite(int k, StateVec iVec, AMeasVecs.MeasVec mv, Swim swim, boolean useSwimmer) {
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
        kVec.dl = 0;

        if(mv.surface!=null) {
            double x = kVec.x0 ;
            double y = 0 ;
            double z = kVec.z0;
            
            Point3D ref = new Point3D(x,y,z);
            
            double tx = kVec.tx;
            double tz = kVec.tz;
            
            double py = (double) mv.hemisphere/Math.sqrt(1+tx*tx+tz*tz);
            double px = tx*py;
            double pz = tz*py;
            
            Vector3D u = new Vector3D(px, py, pz).asUnit(); 
            
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
//                    mv.surface.toGlobal().apply(ref);
//                    mv.surface.toGlobal().apply(u);

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
    @Override
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

    @Override
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

    @Override
    public void tranState(int f, StateVec iVec, Swim swimmer) {
        
    }

    @Override
    public StateVec transported(int i, int f, StateVec iVec, AMeasVecs.MeasVec mv, Swim swimmer) {
        // transport stateVec...
        StateVec fVec = new StateVec(f);

        fVec.x0 = iVec.x0;
        fVec.z0 = iVec.z0;
        fVec.x = iVec.x0;
        fVec.y = 0;
        fVec.z = iVec.z0;
        fVec.tx = iVec.tx;
        fVec.tz = iVec.tz;
        fVec.dl = 0;
        if(f>0)
            this.newStateVecAtMeasSite(f, fVec, mv, swimmer, true);

        return fVec;
    }

    @Override
    public void transport(int i, int f, StateVec iVec, CovMat icovMat, AMeasVecs.MeasVec mv, Swim swimmer) {
        AStateVecs.StateVec fVec = this.transported(i, f, iVec, mv, swimmer);
        
        double[][] FMat = new double[][]{
            {1, 0, 0, 0, 0},
            {0, 1, 0, 0, 0},
            {0, 0, 1, 0, 0},
            {0, 0, 0, 1, 0},
            {0, 0, 0, 0, 1}
        };

        
        this.trackTraj.put(f, fVec);
        //F = new Matrix();
        //F.set(FMat);
        
        double[][] Cpropagated = propagatedMatrix(FMat, icovMat.covMat, FMat);
       
        if (Cpropagated != null) {
            CovMat fCov = new CovMat(f);
            double[][] CPN = addProcessNoise(Cpropagated, this.Q(iVec, mv, f - i));
            
            fCov.covMat = CPN;
            //CovMat = fCov;
            this.trackCov.put(f, fCov);
        }
    }

    @Override
    public double[][] Q(StateVec iVec, AMeasVecs.MeasVec mVec, int dir) {
        double[][] Q = new double[5][5];
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

            double p = 1;
             double sctRMS = 0;
            double t_ov_X0 = mVec.l_over_X0;
            double mass = piMass;   // assume given mass hypothesis 
            double beta = 1; // use particle momentum
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
            
            Q = new double[][]{
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 0, cov_txtx, cov_txtz, 0},
                {0, 0, cov_txtz, cov_tztz, 0},
                {0, 0, 0, 0, 0}
            };
        }
        return Q;
    }

    @Override
    public StateVec reset(StateVec SV, StateVec stateVec) {
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
        System.out.println(S.k + ") x " + S.x + " y " + S.y + " z " + S.z + " tx " + S.tx + " tz " + S.tz + " dl " + S.dl );
    }

    @Override
    public void init(Helix trk, double[][] cov, AKFitter kf, Swim swimmer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    public StateVec initSV = new StateVec(0);
    @Override
    public void init(double x0, double z0, double tx, double tz, double units, double[][] cov, AKFitter kf) {
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
        double[][] covKF = new double[5][5]; 
    
        for(int ic = 0; ic<5; ic++) {
            for(int ir = 0; ir<5; ir++) {
                covKF[ic][ir]=cov[ic][ir];
            }
        }

        initCM.covMat = covKF;
        this.trackCov.put(0, initCM);
    }

    
    @Override
    public void setTrackPars(StateVec sv, Swim swim) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Helix setTrackPars() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
}
