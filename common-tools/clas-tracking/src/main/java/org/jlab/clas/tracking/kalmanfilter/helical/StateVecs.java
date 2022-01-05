package org.jlab.clas.tracking.kalmanfilter.helical;

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
    public boolean getStateVecPosAtMeasSite(StateVec sv, AMeasVecs.MeasVec mv, Swim swim) {
        double[] swimPars = new double[7];
        Point3D  pos = new Point3D(0,0,0);
        Vector3D mom = new Vector3D(0,0,0);

        if(mv.surface==null) return false;
        
        int dir = (int) Math.signum(mv.k-sv.k);
        if(dir==0) dir=1;

        if(mv.k==0) {
            // Transport from measurement surface to DOCA to reference line
            // assumes uniform field
            sv.setPivot(this.xref, this.yref, this.zref);
            sv.toDoca();                
            return true;
        }

        if(mv.surface.plane==null && mv.surface.cylinder==null) return false;
        
        if(this.straight) {

            if(mv.surface.plane!=null) {
                Line3D toPln = new Line3D(new Point3D(sv.x,sv.y,sv.z),new Vector3D(sv.px,sv.py,sv.pz).asUnit());
                Point3D inters = new Point3D();
                int ints = mv.surface.plane.intersection(toPln, inters);
                sv.x = inters.x();
                sv.y = inters.y();
                sv.z = inters.z();                   
            }
            else if(mv.surface.cylinder!=null) {
                Point3D st   = new Point3D(sv.x, sv.y, sv.z); 
                Vector3D stu = new Vector3D(sv.px,sv.py,sv.pz).asUnit();
                mv.surface.toLocal().apply(st);
                mv.surface.toLocal().apply(stu);

                double r = mv.surface.cylinder.baseArc().radius();
                double delta = Math.sqrt((st.x()*stu.x()+st.y()*stu.y())*(st.x()*stu.x()+st.y()*stu.y())-(-r*r+st.x()*st.x()+st.y()*st.y())*(stu.x()*stu.x()+stu.y()*stu.y()));
                double l = (-(st.x()*stu.x()+st.y()*stu.y())+delta)/(stu.x()*stu.x()+stu.y()*stu.y());
                if(Math.signum(st.y()+l*stu.y())!=mv.hemisphere) {
                    l = (-(st.x()*stu.x()+st.y()*stu.y())-delta)/(stu.x()*stu.x()+stu.y()*stu.y()); 
                } 
                Point3D cylInt = new Point3D(st.x()+l*stu.x(),st.y()+l*stu.y(),st.z()+l*stu.z());
                mv.surface.toGlobal().apply(cylInt);
                // RDV: should switch to use clas-geometry intersection method, not done now to alwys return a value
                sv.x = cylInt.x();
                sv.y = cylInt.y();
                sv.z = cylInt.z();
            }
        } else { 
            if(swim==null) { // applicable only to planes parallel to the z -axis
                sv.toOldHelix(util);
                if(mv.surface.plane!=null) {
                    pos = util.getHelixPointAtPlane(mv.surface.finitePlaneCorner1.x(), mv.surface.finitePlaneCorner1.y(),
                            mv.surface.finitePlaneCorner2.x(), mv.surface.finitePlaneCorner2.y(), 10);
                    mom = util.getMomentumAtPlane(mv.surface.finitePlaneCorner1.x(), mv.surface.finitePlaneCorner1.y(),
                            mv.surface.finitePlaneCorner2.x(), mv.surface.finitePlaneCorner2.y(), 10);
                    sv.x = pos.x();
                    sv.y = pos.y();
                    sv.z = pos.z();
                    sv.px = mom.x();
                    sv.py = mom.y();
                    sv.pz = mom.z();
                }
                else {
                    double r = mv.surface.cylinder.baseArc().radius();
                    pos = util.getHelixPointAtR(r);
                    mom = util.getMomentumAtR(r);
                    sv.x = pos.x();
                    sv.y = pos.y();
                    sv.z = pos.z();
                    sv.px = mom.x();
                    sv.py = mom.y();
                    sv.pz = mom.z();
                }
            }
            else {
                swim.SetSwimParameters(sv.x/units, sv.y/units, sv.z/units, 
                                   dir*sv.px, dir*sv.py, dir*sv.pz, 
                                   KFitter.polarity*(int) Math.signum(sv.kappa)*dir);
                if(mv.surface.plane!=null) {
                    Vector3D norm = mv.surface.plane.normal();
                    Point3D point = new Point3D(mv.surface.plane.point().x()/units,
                                                mv.surface.plane.point().y()/units,
                                                mv.surface.plane.point().z()/units);
                    double accuracy = mv.surface.swimAccuracy/units;
                    swimPars = swim.SwimPlane(norm,point,accuracy);
 //                   swimPars = swim.AdaptiveSwimPlane(point.x(), point.y(), point.z(), norm.x(), norm.y(), norm.z(), accuracy);
                    if(swimPars==null)
                        return false;

                    sv.x = swimPars[0]*units;
                    sv.y = swimPars[1]*units;
                    sv.z = swimPars[2]*units;
                    sv.px = swimPars[3]*dir;
                    sv.py = swimPars[4]*dir;
                    sv.pz = swimPars[5]*dir;
                }
                else   {
                    double r = mv.surface.cylinder.baseArc().radius();
                    Point3D p1 = new Point3D(mv.surface.cylinder.getAxis().origin().x()/units,
                                             mv.surface.cylinder.getAxis().origin().y()/units,
                                             mv.surface.cylinder.getAxis().origin().z()/units) ;
                    Point3D p2 = new Point3D(mv.surface.cylinder.getAxis().end().x()/units,
                                             mv.surface.cylinder.getAxis().end().y()/units,
                                             mv.surface.cylinder.getAxis().end().z()/units) ;
                    double accuracy = mv.surface.swimAccuracy/units;
                    swimPars = swim.SwimGenCylinder(p1, p2, r/units, accuracy);
                    if(swimPars==null)
                        return false;
                    sv.x = swimPars[0]*units;
                    sv.y = swimPars[1]*units;
                    sv.z = swimPars[2]*units;
                    sv.px = swimPars[3]*dir;
                    sv.py = swimPars[4]*dir;
                    sv.pz = swimPars[5]*dir;
               }
            }
        }
        return true;
    }
    
    @Override
    public boolean setStateVecPosAtMeasSite(StateVec sv, MeasVec mv, Swim swimmer) {

        boolean status = this.getStateVecPosAtMeasSite(sv, mv, swimmer);
        if (!status) {
            return false;
        }
        sv.k = mv.k;
        if(swimmer!=null && !this.straight) sv.alpha = new B(sv.k, sv.x, sv.y, sv.z, swimmer).alpha;
        if(!this.straight) sv.pivotTransform(); //for straight tracks, keep the same pivot since F matrix is fixed anyway
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
        if(!this.straight) {
            double deltaPhi0 = fVec.phi0 - iVec.phi0;
            if(Math.abs(deltaPhi0)>Math.PI) deltaPhi0 -= Math.signum(deltaPhi0)*2*Math.PI;
            double dphi0_prm_del_drho = -1. / (fVec.d_rho + iVec.alpha / iVec.kappa) * Math.sin(deltaPhi0);
            double dphi0_prm_del_phi0 = (iVec.d_rho + iVec.alpha / iVec.kappa) / (fVec.d_rho + iVec.alpha / iVec.kappa) * Math.cos(deltaPhi0);
            double dphi0_prm_del_kappa = (iVec.alpha / (iVec.kappa * iVec.kappa)) / (fVec.d_rho + iVec.alpha / iVec.kappa) * Math.sin(deltaPhi0);
            double dphi0_prm_del_dz = 0;
            double dphi0_prm_del_tanL = 0;

            double drho_prm_del_drho = Math.cos(deltaPhi0);
            double drho_prm_del_phi0 = (iVec.d_rho + iVec.alpha / iVec.kappa) * Math.sin(deltaPhi0);
            double drho_prm_del_kappa = (iVec.alpha / (iVec.kappa * iVec.kappa)) * (1 - Math.cos(deltaPhi0));
            double drho_prm_del_dz = 0;
            double drho_prm_del_tanL = 0;

            double dkappa_prm_del_drho = 0;
            double dkappa_prm_del_phi0 = 0;
            double dkappa_prm_del_dkappa = 1;
            double dkappa_prm_del_dz = 0;
            double dkappa_prm_del_tanL = 0;

            double dz_prm_del_drho = ((iVec.alpha / iVec.kappa) / (fVec.d_rho + iVec.alpha / iVec.kappa)) * iVec.tanL * Math.sin(deltaPhi0);
            double dz_prm_del_phi0 = (iVec.alpha / iVec.kappa) * iVec.tanL * (1 - Math.cos(deltaPhi0) * (iVec.d_rho + iVec.alpha / iVec.kappa) / (fVec.d_rho + iVec.alpha / iVec.kappa));
            double dz_prm_del_kappa = (iVec.alpha / (iVec.kappa * iVec.kappa)) * iVec.tanL * (deltaPhi0 - Math.sin(deltaPhi0) * (iVec.alpha / iVec.kappa) / (fVec.d_rho + iVec.alpha / iVec.kappa));
            double dz_prm_del_dz = 1;
            double dz_prm_del_tanL = -iVec.alpha * (deltaPhi0) / iVec.kappa;

            double dtanL_prm_del_drho = 0;
            double dtanL_prm_del_phi0 = 0;
            double dtanL_prm_del_dkappa = 0;
            double dtanL_prm_del_dz = 0;
            double dtanL_prm_del_tanL = 1;

            FMat = new double[][]{
                {drho_prm_del_drho, drho_prm_del_phi0, drho_prm_del_kappa, drho_prm_del_dz, drho_prm_del_tanL},
                {dphi0_prm_del_drho, dphi0_prm_del_phi0, dphi0_prm_del_kappa, dphi0_prm_del_dz, dphi0_prm_del_tanL},
                {dkappa_prm_del_drho, dkappa_prm_del_phi0, dkappa_prm_del_dkappa, dkappa_prm_del_dz, dkappa_prm_del_tanL},
                {dz_prm_del_drho, dz_prm_del_phi0, dz_prm_del_kappa, dz_prm_del_dz, dz_prm_del_tanL},
                {dtanL_prm_del_drho, dtanL_prm_del_phi0, dtanL_prm_del_dkappa, dtanL_prm_del_dz, dtanL_prm_del_tanL}
            };
        }
        return FMat;
    }

    @Override
    public double[][] Q(int i, int f, StateVec iVec, AMeasVecs mv) {
        double[][] Q = new double[5][5];
        // if (iVec.k % 2 == 1 && dir > 0) {
        int dir = f-i;
        double t_ov_X0 = 0;
        if(dir>0) {
            for(int k=i+1; k<=f; k++) t_ov_X0 += mv.measurements.get(k).l_over_X0;
        }
        else {
            for(int k=i; k>f; k--)    t_ov_X0 += mv.measurements.get(k).l_over_X0;
        }

        if (t_ov_X0>0) {
            Vector3D trkDir = this.P(iVec.k).asUnit();
            Vector3D trkPos = this.X(iVec.k);
            double x = trkPos.x();
            double y = trkPos.y();
            double z = trkPos.z();
            double ux = trkDir.x();
            double uy = trkDir.y();
            double uz = trkDir.z();

            double cosEntranceAngle = Math.abs((x * ux + y * uy + z * uz) / Math.sqrt(x * x + y * y + z * z));

            double pt = Math.abs(1. / iVec.kappa);
            double pz = pt * iVec.tanL;
            double p  = Math.sqrt(pt * pt + pz * pz);
            double mass = piMass;   // assume given mass hypothesis 
            double beta = p / Math.sqrt(p * p + mass * mass); // use particle momentum
            t_ov_X0 = t_ov_X0 / cosEntranceAngle;
            // Highland-Lynch-Dahl formula
            double sctRMS = (0.0136/(beta*p))*Math.sqrt(t_ov_X0)*
                            (1 + 0.038 * Math.log(t_ov_X0));
             //sctRMS = ((0.141)/(beta*PhysicsConstants.speedOfLight()*p))*Math.sqrt(t_ov_X0)*
             //       (1 + Math.log(t_ov_X0)/9.);
            Q = new double[][]{
                {0, 0, 0, 0, 0},
                {0, sctRMS*sctRMS * (1 + iVec.tanL * iVec.tanL), 0, 0, 0},
                {0, 0, sctRMS*sctRMS * (iVec.kappa * iVec.kappa * iVec.tanL * iVec.tanL), 0, sctRMS*sctRMS * (iVec.kappa * iVec.tanL * (1 + iVec.tanL * iVec.tanL))},
                {0, 0, 0, 0, 0},
                {0, 0, sctRMS*sctRMS * (iVec.kappa * iVec.tanL * (1 + iVec.tanL * iVec.tanL)), 0, sctRMS*sctRMS * (1 + iVec.tanL * iVec.tanL) * (1 + iVec.tanL * iVec.tanL)}
            };
        }

        return Q;
    }
//    private void setHelix(Helix util, double x0, double y0, double z0, double px0, double py0, double pz0,
//            int q, double B) {
//        util.setTurningSign(q);
//        util.setB(B);
//        double pt = Math.sqrt(px0*px0 + py0*py0);
//        util.setR(pt/(B*util.getLIGHTVEL())) ;
//        util.setPhi0(Math.atan2(py0, px0));
//        util.setTanL(pz0/pt);
//        util.setZ0(z0);
//        util.setOmega((double) -q/util.getR());
//
//        double S = Math.sin(util.getPhi0());
//        double C = Math.cos(util.getPhi0());
//        
//        util.setCosphi0(C);
//        util.setSinphi0(S);
//        
//        if(Math.abs(S)>=Math.abs(C)) {
//            util.setD0(-(x0-X0.get(0))/S) ;
//        } else {
//            util.setD0((y0-Y0.get(0))/C) ;
//        }
//
//        util.Update();
//    }
//    private void setHelixPars(StateVec kVec, Swim swim) {
//        StateVec vec = new StateVec(0, kVec);
//        vec.updateHelix();
//        vec.phi = 0;
//        vec.updateFromHelix();
//
//        double x0 = X0.get(vec.k) + vec.d_rho * Math.cos(vec.phi0) ;
//        double y0 = Y0.get(vec.k) + vec.d_rho * Math.sin(vec.phi0) ;
//        double z0 = Z0.get(vec.k) + vec.dz ;
//        double invKappa = 1. / Math.abs(vec.kappa);
//        double px0 = -invKappa * Math.sin(vec.phi0 );
//        double py0 = invKappa * Math.cos(vec.phi0 );
//        double pz0 = invKappa * vec.tanL;
//
//        int ch = (int) KFitter.polarity*(int) Math.signum(vec.kappa);
//
//        double B = 1. / (lightVel * vec.alpha);
////        System.out.println(x0 + " " + y0 + " " + z0 + " " + px0  + " " + py0 + " " + pz0);
//        this.setHelix(util, x0,y0,z0,px0,py0,pz0,ch, B);
//    }
    
//    @Override
//    public void setTrackPars(StateVec kVec, Swim swim) {
//
//        double x0 = kVec.x0 + kVec.d_rho * Math.cos(kVec.phi0) ;
//        double y0 = kVec.y0 + kVec.d_rho * Math.sin(kVec.phi0) ;
//        double z0 = kVec.z0 + kVec.dz ;
//        double invKappa = 1. / Math.abs(kVec.kappa);
//        double px0 = -invKappa * Math.sin(kVec.phi0 );
//        double py0 = invKappa * Math.cos(kVec.phi0 );
//        double pz0 = invKappa * kVec.tanL;
//        int ch = (int) KFitter.polarity*(int) Math.signum(kVec.kappa);
////        System.out.println("old pivot " + new Point3D(x0,y0,z0).toString() + new Vector3D(px0,py0,pz0).toString());
//        swim.SetSwimParameters(
//                        x0/units,
//                        y0/units,
//                        z0/units,
//                        px0, py0, pz0, ch);
//    }


//    private void iterateHelixAtR(int it, int k, StateVec kVec, Swim swim,
//            double r, B Bf, Point3D ps) {
//        for(int i = 0; i < it; i++) {
//            this.setHelixPars(kVec, swim);
//            ps = util.getHelixPointAtR(r);
//            kVec.x = ps.x();
//            kVec.y = ps.y();
//            kVec.z = ps.z();
//            this.tranState(k, kVec, swim);
//            Bf = new B(kVec.k, kVec.x, kVec.y, kVec.z, swim);
//            kVec.alpha = Bf.alpha;
//            this.tranState(k, kVec, swim);
//            this.setHelixPars(kVec, swim);
//            kVec.x = ps.x();
//            kVec.y = ps.y();
//            kVec.z = ps.z();
//            this.tranState(k, kVec, swim);
//        }
//    }

    @Override
    public Vector3D P(int kf) {
    if (this.trackTraj.get(kf) != null) {
            double px = -(Math.signum(this.trackTraj.get(kf).kappa) / this.trackTraj.get(kf).kappa) * Math.sin(this.trackTraj.get(kf).phi0 + this.trackTraj.get(kf).phi);
            double py = (Math.signum(this.trackTraj.get(kf).kappa) / this.trackTraj.get(kf).kappa) * Math.cos(this.trackTraj.get(kf).phi0 + this.trackTraj.get(kf).phi);
            double pz = (Math.signum(this.trackTraj.get(kf).kappa) / this.trackTraj.get(kf).kappa) * this.trackTraj.get(kf).tanL;

            return new Vector3D(px, py, pz);
        } else {
            return new Vector3D(0, 0, 0);
        }
    }

    @Override
    public Vector3D X(int kf) {
    if (this.trackTraj.get(kf) != null) {
            double x = this.trackTraj.get(kf).x0 + this.trackTraj.get(kf).d_rho * Math.cos(this.trackTraj.get(kf).phi0) + this.trackTraj.get(kf).alpha / this.trackTraj.get(kf).kappa * (Math.cos(this.trackTraj.get(kf).phi0) - Math.cos(this.trackTraj.get(kf).phi0 + this.trackTraj.get(kf).phi));
            double y = this.trackTraj.get(kf).y0 + this.trackTraj.get(kf).d_rho * Math.sin(this.trackTraj.get(kf).phi0) + this.trackTraj.get(kf).alpha / this.trackTraj.get(kf).kappa * (Math.sin(this.trackTraj.get(kf).phi0) - Math.sin(this.trackTraj.get(kf).phi0 + this.trackTraj.get(kf).phi));
            double z = this.trackTraj.get(kf).z0 + this.trackTraj.get(kf).dz - this.trackTraj.get(kf).alpha / this.trackTraj.get(kf).kappa * this.trackTraj.get(kf).tanL * this.trackTraj.get(kf).phi;

            return new Vector3D(x, y, z);
        } else {
            return new Vector3D(0, 0, 0);
        }
    }

    @Override
    public Vector3D X(StateVec kVec, double phi) {
    if (kVec != null) {
            double x = kVec.x0 + kVec.d_rho * Math.cos(kVec.phi0) + kVec.alpha / kVec.kappa * (Math.cos(kVec.phi0) - Math.cos(kVec.phi0 + phi));
            double y = kVec.y0 + kVec.d_rho * Math.sin(kVec.phi0) + kVec.alpha / kVec.kappa * (Math.sin(kVec.phi0) - Math.sin(kVec.phi0 + phi));
            double z = kVec.z0 + kVec.dz - kVec.alpha / kVec.kappa * kVec.tanL * phi;

            return new Vector3D(x, y, z);
        } else {
            return new Vector3D(0, 0, 0);
        }
    }

    @Override
    public Vector3D P0(int kf) {
        if (this.trackTraj.get(kf) != null) {
            double px = -(Math.signum(this.trackTraj.get(kf).kappa) / this.trackTraj.get(kf).kappa) * Math.sin(this.trackTraj.get(kf).phi0);
            double py = (Math.signum(this.trackTraj.get(kf).kappa) / this.trackTraj.get(kf).kappa) * Math.cos(this.trackTraj.get(kf).phi0);
            double pz = (Math.signum(this.trackTraj.get(kf).kappa) / this.trackTraj.get(kf).kappa) * this.trackTraj.get(kf).tanL;

            return new Vector3D(px, py, pz);
        } else {
            return new Vector3D(0, 0, 0);
        }

    }

    @Override
    public Vector3D X0(int kf) {
        if (this.trackTraj.get(kf) != null) {
            double x = this.trackTraj.get(kf).x0 + this.trackTraj.get(kf).d_rho * Math.cos(this.trackTraj.get(kf).phi0);
            double y = this.trackTraj.get(kf).y0 + this.trackTraj.get(kf).d_rho * Math.sin(this.trackTraj.get(kf).phi0);
            double z = this.trackTraj.get(kf).z0 + this.trackTraj.get(kf).dz;

            return new Vector3D(x, y, z);
        } else {
            return new Vector3D(0, 0, 0);
        }
    }

    @Override
    public Helix setTrackPars() {
        Vector3D X = this.X0(0);
        Vector3D P = this.P0(0);

        int q = KFitter.polarity*(int) Math.signum(this.trackTraj.get(0).kappa);
        double B = 1./Math.abs(this.trackTraj.get(0).alpha)/lightVel ;

        return new Helix(X.x(), X.y(), X.z(), P.x(), P.y(), P.z(), q, B, this.xref, this.yref, util.units);
    }

    
    @Override
    public void init(Helix helix, double[][] cov, double xref, double yref, double zref, Swim swimmer) {
        this.trackTraj = new HashMap<>();
        this.units = helix.getUnitScale();
        this.lightVel = helix.getLIGHTVEL();
        this.util = helix;

        this.xref = xref;
        this.yref = yref;
        this.zref = zref;
        
        if(Math.abs(helix.getB())<0.001)
           this.straight = true;
        //        System.out.println(this.straight);

        //init stateVec, pivot set to current vertex for field-on and to the reference for straight tracks
        initSV = new StateVec(0);
        if(this.straight) {
            initSV.x0 = xref;
            initSV.y0 = yref;
            initSV.z0 = zref;
        }
        else {
            initSV.x0 = helix.getX();
            initSV.y0 = helix.getY();
            initSV.z0 = helix.getZ();           
        }
        initSV.x  = helix.getX();
        initSV.y  = helix.getY();
        initSV.z  = helix.getZ();
        initSV.px = helix.getPx();
        initSV.py = helix.getPy();
        initSV.pz = helix.getPz();
//        this.printlnStateVec(initSV);

        // set bfield according to input helix for consistency
        initSV.alpha = 1/(helix.getB()*helix.getLIGHTVEL());
        // set kappa to define the charge
        initSV.kappa = initSV.alpha * helix.getOmega();
        // convert from the helix class representation to KF
        // kappa = alpha/omega = - q / pt
        // drho  = - d0
        // phi0  = (phi0 - 90)
//        initSV.kappa = initSV.alpha * trk.getOmega();
//        initSV.phi0  = trk.getPhi0()-Math.PI/2;
//        initSV.dz    = trk.getZ();
//        initSV.tanL  = trk.getTanL();
//        initSV.d_rho = -trk.getD0();

        // recalculate helix parameters from vertex and momentum using chosen pivot
        initSV.updateHelix();
        
        double[][] covKF = new double[5][5]; 
    
       //convert from helix to KF representation
        for(int ic = 0; ic<5; ic++) {
            for(int ir = 0; ir<5; ir++) {
                covKF[ic][ir]=cov[ic][ir];
                if(ic==2)
                    covKF[ic][ir]*=initSV.alpha;
                if(ir==2)
                    covKF[ic][ir]*=initSV.alpha;
            }
        }
        initSV.covMat = covKF;
        this.trackTraj.put(0, new StateVec(initSV));
    }

    @Override
    public void printlnStateVec(StateVec S) {
        String s = String.format("%d) drho=%.4f phi0=%.4f kappa=%.4f dz=%.4f tanL=%.4f alpha=%.4f\n", S.k, S.d_rho, S.phi0, S.kappa, S.dz, S.tanL, S.alpha);
        s       += String.format("    x0=%.4f y0=%.4f z0=%.4f", S.x0, S.y0, S.z0);
        s       += String.format("    phi=%.4f x=%.4f y=%.4f z=%.4f px=%.4f py=%.4f pz=%.4f", S.phi, S.x, S.y, S.z, S.px, S.py, S.pz);
        System.out.println(s);
    }


    @Override
    public void init(double x0, double z0, double tx, double tz, double units, double[][] cov) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
