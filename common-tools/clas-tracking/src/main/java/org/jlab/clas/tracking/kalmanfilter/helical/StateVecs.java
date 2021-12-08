package org.jlab.clas.tracking.kalmanfilter.helical;

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

     
    double[] swimPars = new double[7];
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

        AStateVecs.StateVec kVec = new AStateVecs.StateVec(k);
        kVec.phi0 = iVec.phi0;
        kVec.d_rho = iVec.d_rho;
        kVec.kappa = iVec.kappa;
        kVec.dz = iVec.dz;
        kVec.tanL = iVec.tanL;
        kVec.alpha = iVec.alpha;

        if(mv.surface!=null) {
            double x = X0.get(0) + kVec.d_rho * Math.cos(kVec.phi0);
            double y = Y0.get(0) + kVec.d_rho * Math.sin(kVec.phi0);
            double z = Z0.get(0) + kVec.dz;
            
            Bf.swimmer = swim;
            Bf.x = x;
            Bf.y = y;
            Bf.z = z;
            Bf.set();
            kVec.alpha = Bf.alpha;
            
            if(k==0) {
                //value[0] = x;
                //value[1] = y;
                //value[2] = z;
                //value[3] = 0.0;
                double invKappa = 1. / Math.abs(kVec.kappa);
                double px = -invKappa * Math.sin(kVec.phi0 );
                double py = invKappa * Math.cos(kVec.phi0 );
                double pz = invKappa * kVec.tanL;
                int ch = (int) KFitter.polarity*(int) Math.signum(kVec.kappa);
                double accuracy = mv.surface.swimAccuracy/units;
                swim.SetSwimParameters(x/units, y/units, z/units, px, py, pz, ch);
                swimPars = swim.SwimRho(50./units, accuracy);
                if(swimPars==null)
                    return null;
                swim.SetSwimParameters(swimPars[0], swimPars[1], swimPars[2], -swimPars[3], -swimPars[4], -swimPars[5], -ch);
                Vector3D norm = new Vector3D(px,py,pz).asUnit();
                Point3D point = new Point3D(mv.surface.plane.point().x()/units,
                                            mv.surface.plane.point().y()/units,
                                            mv.surface.plane.point().z()/units);
                swimPars = swim.SwimPlane(norm,point,accuracy);
                if(swimPars==null)
                    return null;
                for(int j =0; j < 3; j++) {
                    swimPars[j]*=units;
                }
                kVec.x = swimPars[0];
                kVec.y = swimPars[1];
                kVec.z = swimPars[2]; 
            }
            
            if(this.straight) {
                Vector3D u = new Vector3D((Math.signum(kVec.kappa)) * Math.sin(kVec.phi0),
                                         -(Math.signum(kVec.kappa)) * Math.cos(kVec.phi0),
                                         -(Math.signum(kVec.kappa)) * kVec.tanL).asUnit();
                
                if(mv.surface.plane!=null) {
                    Line3D toPln = new Line3D(new Point3D(x,y,z),u);
                    Point3D inters = new Point3D();
                    int ints = mv.surface.plane.intersection(toPln, inters);
                    kVec.x = inters.x()  ;
                    kVec.y = inters.y()  ;
                    kVec.z = inters.z()  ;  
                    
                }
                else if(mv.surface.cylinder!=null) {
                    Point3D st   = new Point3D(x,y,z); 
                    Vector3D stu = new Vector3D(u.x(),u.y(),u.z());
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
                    kVec.x = cylInt.x();
                    kVec.y = cylInt.y();
                    kVec.z = cylInt.z();
                }
                value[0] = kVec.x;
                value[1] = kVec.y;
                value[2] = kVec.z;
                value[3] = this.calcPhi(kVec);
                
                return value;
            } else { 
                if(mv.surface.plane!=null) {
                   if(useSwimmer=false) { // applicable only to planes parallel to the z -axis
                        double r0 = mv.surface.finitePlaneCorner1.toVector3D().dot(mv.surface.plane.normal());
                        double stepSize = 5; //mm
                        int nSteps = (int) (r0/stepSize);

                        double dist = 0;

                        for(int i = 1; i<nSteps; i++) {
                            dist = (double) (i*stepSize);
                            this.setHelixPars(kVec, swim);
                            ps = util.getHelixPointAtR(dist);
                            kVec.x = ps.x();
                            kVec.y = ps.y();
                            kVec.z = ps.z();
                            this.tranState(k, kVec, swim);
                        }
                        this.setHelixPars(kVec, swim);
                        ps = util.getHelixPointAtPlane(mv.surface.finitePlaneCorner1.x(), mv.surface.finitePlaneCorner1.y(),
                                mv.surface.finitePlaneCorner2.x(), mv.surface.finitePlaneCorner2.y(), 10);
                        this.tranState(k, kVec, swim);
                        kVec.x = ps.x();
                        kVec.y = ps.y();
                        kVec.z = ps.z();
                        if(swimPars==null)
                            return null;
                        swimPars[0] = ps.x();
                        swimPars[1] = ps.y();
                        swimPars[2] = ps.z();
                    }
                    else {
                        this.setTrackPars(kVec, swim);
                        Vector3D norm = mv.surface.plane.normal();
                        Point3D point = new Point3D(mv.surface.plane.point().x()/units,
                                                    mv.surface.plane.point().y()/units,
                                                    mv.surface.plane.point().z()/units);
                        double accuracy = mv.surface.swimAccuracy/units;
                        swimPars = swim.SwimPlane(norm,point,accuracy);
     //                   swimPars = swim.AdaptiveSwimPlane(point.x(), point.y(), point.z(), norm.x(), norm.y(), norm.z(), accuracy);
                        if(swimPars==null)
                            return null;
                        for(int j =0; j < 3; j++) {
                            swimPars[j]*=units;
                        }
                        kVec.x = swimPars[0];
                        kVec.y = swimPars[1];
                        kVec.z = swimPars[2]; 
                    }

                }
                if(mv.surface.cylinder!=null) {
                    double r = mv.surface.cylinder.baseArc().radius();
                    if(useSwimmer==false) {
                        double stepSize = 5; //mm
                        int nSteps = (int) (r/stepSize);

                        double dist = 0;
                        for(int i = 1; i<nSteps; i++) {
                            dist = (double) (i*stepSize);
                            this.setHelixPars(kVec, swim);
                            ps = util.getHelixPointAtR(dist);
                            kVec.x = ps.x();
                            kVec.y = ps.y();
                            kVec.z = ps.z();
                            this.tranState(k, kVec, swim);
                        }
                        this.setHelixPars(kVec, swim);
                        ps = new Point3D(kVec.x, kVec.y, kVec.z);
                        swimPars[0] = ps.x();
                        swimPars[1] = ps.y();
                        swimPars[2] = ps.z();
                        if(swimPars==null)
                            return null;
                    } else {
                        this.setTrackPars(kVec, swim);
                        Point3D p1 = new Point3D(mv.surface.cylinder.getAxis().origin().x()/units,
                                                 mv.surface.cylinder.getAxis().origin().y()/units,
                                                 mv.surface.cylinder.getAxis().origin().z()/units) ;
                        Point3D p2 = new Point3D(mv.surface.cylinder.getAxis().end().x()/units,
                                                 mv.surface.cylinder.getAxis().end().y()/units,
                                                 mv.surface.cylinder.getAxis().end().z()/units) ;
                        double accuracy = mv.surface.swimAccuracy/units;
                        swimPars = swim.SwimGenCylinder(p1, p2, r/units, accuracy);
                        if(swimPars==null)
                            return null;
                        for(int j =0; j < 3; j++) {
                            swimPars[j]*=units;
                        }
                        kVec.x = swimPars[0];
                        kVec.y = swimPars[1];
                        kVec.z = swimPars[2];
                   }
                }

                value[0] = swimPars[0];
                value[1] = swimPars[1];
                value[2] = swimPars[2];
                value[3] = this.calcPhi(kVec);

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
        //System.out.println(" k "+k+" "+pars[0]+", "+pars[1]);
        kVec.x = pars[0];
        kVec.y = pars[1];
        kVec.z = pars[2];

        kVec.alpha = new B(k, kVec.x, kVec.y, kVec.z, swimmer).alpha;
        kVec.phi = pars[3];
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

        newVec.alpha = new B(k, newVec.x, newVec.y, newVec.z, swimmer).alpha;
        newVec.phi = pars[3];

        // new state:
        return newVec;
    }
    @Override
    public void tranState(int f, StateVec iVec, Swim swimmer) {
        Bf.swimmer = swimmer;
        Bf.x = iVec.x;
        Bf.y = iVec.y;
        Bf.z = iVec.z;
        Bf.set();

        double Xc = X0.get(iVec.k) + (iVec.d_rho + Bf.alpha / iVec.kappa) * Math.cos(iVec.phi0);
        double Yc = Y0.get(iVec.k) + (iVec.d_rho + Bf.alpha / iVec.kappa) * Math.sin(iVec.phi0);

        double phi_f = Math.atan2(Yc - Y0.get(f), Xc - X0.get(f));
        if (iVec.kappa < 0) {
            phi_f = Math.atan2(-Yc + Y0.get(f), -Xc + X0.get(f));
        }

        if (phi_f < 0) {
            phi_f += 2 * Math.PI;
        }
        double fphi0 = phi_f;
        double fd_rho = (Xc - X0.get(f)) * Math.cos(phi_f) + (Yc - Y0.get(f)) * Math.sin(phi_f) - Bf.alpha / iVec.kappa;
        //fkappa = iVec.kappa;
        double fdz = Z0.get(iVec.k) - Z0.get(f) + iVec.dz - (Bf.alpha / iVec.kappa) * (phi_f - iVec.phi0) * iVec.tanL;
        //ftanL = iVec.tanL;
        double falpha = Bf.alpha;

        if (fphi0 < 0) {
            fphi0 += 2. * Math.PI;
        }

        iVec.phi0 = fphi0;
        iVec.d_rho = fd_rho;
        iVec.dz = fdz;
        iVec.alpha = falpha;
    }

    @Override
    public StateVec transported(int i, int f, StateVec iVec, AMeasVecs.MeasVec mv, Swim swimmer) {
        // transport stateVec...
        StateVec fVec = new StateVec(f);

        if (iVec.phi0 < 0) {
            iVec.phi0 += 2. * Math.PI;
        }
        double x = X0.get(0) + iVec.d_rho * Math.cos(iVec.phi0);
        double y = Y0.get(0) + iVec.d_rho * Math.sin(iVec.phi0);
        double z = Z0.get(0) + iVec.dz;
        B Bf = new B(i, x, y, z, swimmer);

        fVec.phi0 = iVec.phi0;
        fVec.d_rho = iVec.d_rho;
        fVec.kappa = iVec.kappa;
        fVec.dz = iVec.dz;
        fVec.tanL = iVec.tanL;
        fVec.alpha = Bf.alpha;

        this.newStateVecAtMeasSite(f, fVec, mv, swimmer, true);

        return fVec;
    }

    @Override
    public void transport(int i, int f, StateVec iVec, CovMat icovMat, AMeasVecs.MeasVec mv, Swim swimmer) {
        StateVec fVec = this.transported(i, f, iVec, mv, swimmer);

        // now transport covMat...
        double dphi0_prm_del_drho = -1. / (fVec.d_rho + iVec.alpha / iVec.kappa) * Math.sin(fVec.phi0 - iVec.phi0);
        double dphi0_prm_del_phi0 = (iVec.d_rho + iVec.alpha / iVec.kappa) / (fVec.d_rho + iVec.alpha / iVec.kappa) * Math.cos(fVec.phi0 - iVec.phi0);
        double dphi0_prm_del_kappa = (iVec.alpha / (iVec.kappa * iVec.kappa)) / (fVec.d_rho + iVec.alpha / iVec.kappa) * Math.sin(fVec.phi0 - iVec.phi0);
        double dphi0_prm_del_dz = 0;
        double dphi0_prm_del_tanL = 0;

        double drho_prm_del_drho = Math.cos(fVec.phi0 - iVec.phi0);
        double drho_prm_del_phi0 = (iVec.d_rho + iVec.alpha / iVec.kappa) * Math.sin(fVec.phi0 - iVec.phi0);
        double drho_prm_del_kappa = (iVec.alpha / (iVec.kappa * iVec.kappa)) * (1 - Math.cos(fVec.phi0 - iVec.phi0));
        double drho_prm_del_dz = 0;
        double drho_prm_del_tanL = 0;

        double dkappa_prm_del_drho = 0;
        double dkappa_prm_del_phi0 = 0;
        double dkappa_prm_del_dkappa = 1;
        double dkappa_prm_del_dz = 0;
        double dkappa_prm_del_tanL = 0;

        double dz_prm_del_drho = ((iVec.alpha / iVec.kappa) / (fVec.d_rho + iVec.alpha / iVec.kappa)) * iVec.tanL * Math.sin(fVec.phi0 - iVec.phi0);
        double dz_prm_del_phi0 = (iVec.alpha / iVec.kappa) * iVec.tanL * (1 - Math.cos(fVec.phi0 - iVec.phi0) * (iVec.d_rho + iVec.alpha / iVec.kappa) / (fVec.d_rho + iVec.alpha / iVec.kappa));
        double dz_prm_del_kappa = (iVec.alpha / (iVec.kappa * iVec.kappa)) * iVec.tanL * (fVec.phi0 - iVec.phi0 - Math.sin(fVec.phi0 - iVec.phi0) * (iVec.alpha / iVec.kappa) / (fVec.d_rho + iVec.alpha / iVec.kappa));
        double dz_prm_del_dz = 1;
        double dz_prm_del_tanL = -iVec.alpha * (fVec.phi0 - iVec.phi0) / iVec.kappa;

        double dtanL_prm_del_drho = 0;
        double dtanL_prm_del_phi0 = 0;
        double dtanL_prm_del_dkappa = 0;
        double dtanL_prm_del_dz = 0;
        double dtanL_prm_del_tanL = 1;

        double[][] FMat = new double[][]{
            {drho_prm_del_drho, drho_prm_del_phi0, drho_prm_del_kappa, drho_prm_del_dz, drho_prm_del_tanL},
            {dphi0_prm_del_drho, dphi0_prm_del_phi0, dphi0_prm_del_kappa, dphi0_prm_del_dz, dphi0_prm_del_tanL},
            {dkappa_prm_del_drho, dkappa_prm_del_phi0, dkappa_prm_del_dkappa, dkappa_prm_del_dz, dkappa_prm_del_tanL},
            {dz_prm_del_drho, dz_prm_del_phi0, dz_prm_del_kappa, dz_prm_del_dz, dz_prm_del_tanL},
            {dtanL_prm_del_drho, dtanL_prm_del_phi0, dtanL_prm_del_dkappa, dtanL_prm_del_dz, dtanL_prm_del_tanL}
        };
        double[][] FMatT = new double[][]{
            {drho_prm_del_drho, dphi0_prm_del_drho, dkappa_prm_del_drho,  dz_prm_del_drho, dtanL_prm_del_drho},
            {drho_prm_del_phi0, dphi0_prm_del_phi0, dkappa_prm_del_phi0,  dz_prm_del_phi0, dtanL_prm_del_phi0},
            {drho_prm_del_kappa,dphi0_prm_del_kappa,dkappa_prm_del_dkappa,dz_prm_del_kappa,dtanL_prm_del_dkappa},
            {drho_prm_del_dz,   dphi0_prm_del_dz,   dkappa_prm_del_dz,    dz_prm_del_dz,   dtanL_prm_del_dz},
            {drho_prm_del_tanL, dphi0_prm_del_tanL, dkappa_prm_del_tanL,  dz_prm_del_tanL, dtanL_prm_del_tanL}
        };
        //StateVec = fVec;
        this.trackTraj.put(f, fVec);
        //F = new Matrix();
        //F.set(FMat);
        
        double[][] Cpropagated = propagatedMatrix(FMatT, icovMat.covMat, FMat);
       
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

            double cosEntranceAngle = Math.abs((x * ux + y * uy + z * uz) / Math.sqrt(x * x + y * y + z * z));

            double pt = Math.abs(1. / iVec.kappa);
            double pz = pt * iVec.tanL;
            double p = Math.sqrt(pt * pt + pz * pz);
             double sctRMS = 0;
            double t_ov_X0 = mVec.l_over_X0;
            double mass = piMass;   // assume given mass hypothesis 
            double beta = p / Math.sqrt(p * p + mass * mass); // use particle momentum
            t_ov_X0 = t_ov_X0 / cosEntranceAngle;
            if(t_ov_X0>0) {
            // Highland-Lynch-Dahl formula
                sctRMS = (0.136/(beta*PhysicsConstants.speedOfLight()*p))*Math.sqrt(t_ov_X0)*
                    (1 + 0.038 * Math.log(t_ov_X0));
             //sctRMS = ((0.141)/(beta*PhysicsConstants.speedOfLight()*p))*Math.sqrt(t_ov_X0)*
             //       (1 + Math.log(t_ov_X0)/9.);
            }
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
    private void setHelix(Helix util, double x0, double y0, double z0, double px0, double py0, double pz0,
            int q, double B) {
        util.setTurningSign(q);
        util.setB(B);
        double pt = Math.sqrt(px0*px0 + py0*py0);
        util.setR(pt/(B*util.getLIGHTVEL())) ;
        util.setPhi0(Math.atan2(py0, px0));
        util.setTanL(pz0/pt);
        util.setZ0(z0);
        util.setOmega((double) -q/util.getR());

        double S = Math.sin(util.getPhi0());
        double C = Math.cos(util.getPhi0());
        
        util.setCosphi0(C);
        util.setSinphi0(S);
        
        if(Math.abs(S)>=Math.abs(C)) {
            util.setD0(-(x0-X0.get(0))/S) ;
        } else {
            util.setD0((y0-Y0.get(0))/C) ;
        }

        util.Update();
    }
    private void setHelixPars(StateVec kVec, Swim swim) {
        double x0 = X0.get(kVec.k) + kVec.d_rho * Math.cos(kVec.phi0) ;
        double y0 = Y0.get(kVec.k) + kVec.d_rho * Math.sin(kVec.phi0) ;
        double z0 = Z0.get(kVec.k) + kVec.dz ;
        double invKappa = 1. / Math.abs(kVec.kappa);
        double px0 = -invKappa * Math.sin(kVec.phi0 );
        double py0 = invKappa * Math.cos(kVec.phi0 );
        double pz0 = invKappa * kVec.tanL;

        int ch = (int) KFitter.polarity*(int) Math.signum(kVec.kappa);

        double B = 1. / (lightVel * kVec.alpha);
        this.setHelix(util, x0,y0,z0,px0,py0,pz0,ch, B);
    }
    
    @Override
    public void setTrackPars(StateVec kVec, Swim swim) {

        double x0 = X0.get(kVec.k) + kVec.d_rho * Math.cos(kVec.phi0) ;
        double y0 = Y0.get(kVec.k) + kVec.d_rho * Math.sin(kVec.phi0) ;
        double z0 = Z0.get(kVec.k) + kVec.dz ;
        double invKappa = 1. / Math.abs(kVec.kappa);
        double px0 = -invKappa * Math.sin(kVec.phi0 );
        double py0 = invKappa * Math.cos(kVec.phi0 );
        double pz0 = invKappa * kVec.tanL;
        int ch = (int) KFitter.polarity*(int) Math.signum(kVec.kappa);

        swim.SetSwimParameters(
                        x0/units,
                        y0/units,
                        z0/units,
                        px0, py0, pz0, ch);
    }

    private double calcPhi(StateVec kVec) {
        double xc = X0.get(kVec.k) + (kVec.d_rho + kVec.alpha / kVec.kappa) * Math.cos(kVec.phi0);
        double yc = Y0.get(kVec.k) + (kVec.d_rho + kVec.alpha / kVec.kappa) * Math.sin(kVec.phi0);
        double r = Math.abs(kVec.alpha / kVec.kappa);
        Vector3D ToPoint = new Vector3D();
        Vector3D ToRef = new Vector3D(X0.get(kVec.k) - xc, Y0.get(kVec.k) - yc, 0);

        ToPoint = new Vector3D(kVec.x - xc, kVec.y - yc, 0);
        double phi = ToRef.angle(ToPoint);
        phi *= -Math.signum(kVec.kappa);

        return phi;
    }
    private void iterateHelixAtR(int it, int k, StateVec kVec, Swim swim,
            double r, B Bf, Point3D ps) {
        for(int i = 0; i < it; i++) {
            this.setHelixPars(kVec, swim);
            ps = util.getHelixPointAtR(r);
            kVec.x = ps.x();
            kVec.y = ps.y();
            kVec.z = ps.z();
            this.tranState(k, kVec, swim);
            Bf = new B(kVec.k, kVec.x, kVec.y, kVec.z, swim);
            kVec.alpha = Bf.alpha;
            this.tranState(k, kVec, swim);
            this.setHelixPars(kVec, swim);
            kVec.x = ps.x();
            kVec.y = ps.y();
            kVec.z = ps.z();
            this.tranState(k, kVec, swim);
        }
    }
    @Override
    public StateVec reset(StateVec SV, StateVec stateVec) {
        SV = new StateVec(stateVec.k);
        SV.x = stateVec.x;
        SV.y = stateVec.y;
        SV.z = stateVec.z;
        SV.d_rho = stateVec.d_rho;
        SV.dz = stateVec.dz;
        SV.phi0 = stateVec.phi0;
        SV.phi = stateVec.phi;
        SV.tanL = stateVec.tanL;
        SV.alpha = stateVec.alpha;
        SV.kappa = stateVec.kappa;

        return SV;
    }

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
            double x = X0.get(kf) + this.trackTraj.get(kf).d_rho * Math.cos(this.trackTraj.get(kf).phi0) + this.trackTraj.get(kf).alpha / this.trackTraj.get(kf).kappa * (Math.cos(this.trackTraj.get(kf).phi0) - Math.cos(this.trackTraj.get(kf).phi0 + this.trackTraj.get(kf).phi));
            double y = Y0.get(kf) + this.trackTraj.get(kf).d_rho * Math.sin(this.trackTraj.get(kf).phi0) + this.trackTraj.get(kf).alpha / this.trackTraj.get(kf).kappa * (Math.sin(this.trackTraj.get(kf).phi0) - Math.sin(this.trackTraj.get(kf).phi0 + this.trackTraj.get(kf).phi));
            double z = Z0.get(kf) + this.trackTraj.get(kf).dz - this.trackTraj.get(kf).alpha / this.trackTraj.get(kf).kappa * this.trackTraj.get(kf).tanL * this.trackTraj.get(kf).phi;

            return new Vector3D(x, y, z);
        } else {
            return new Vector3D(0, 0, 0);
        }
    }

    @Override
    public Vector3D X(StateVec kVec, double phi) {
    if (kVec != null) {
            double x = X0.get(kVec.k) + kVec.d_rho * Math.cos(kVec.phi0) + kVec.alpha / kVec.kappa * (Math.cos(kVec.phi0) - Math.cos(kVec.phi0 + phi));
            double y = Y0.get(kVec.k) + kVec.d_rho * Math.sin(kVec.phi0) + kVec.alpha / kVec.kappa * (Math.sin(kVec.phi0) - Math.sin(kVec.phi0 + phi));
            double z = Z0.get(kVec.k) + kVec.dz - kVec.alpha / kVec.kappa * kVec.tanL * phi;

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
            double x = X0.get(kf) + this.trackTraj.get(kf).d_rho * Math.cos(this.trackTraj.get(kf).phi0);
            double y = Y0.get(kf) + this.trackTraj.get(kf).d_rho * Math.sin(this.trackTraj.get(kf).phi0);
            double z = Z0.get(kf) + this.trackTraj.get(kf).dz;

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

        return new Helix(X.x(), X.y(), X.z(), P.x(), P.y(), P.z(), q, B, X0.get(0), Y0.get(0), util.units);
    }
    public StateVec initSV = new StateVec(0);
   
    @Override
    public void init(Helix trk, double[][] cov, AKFitter kf,
            Swim swimmer) {
        this.units = trk.getUnitScale();
        this.lightVel = trk.getLIGHTVEL();
        this.util = trk;
        //init stateVec
        //StateVec initSV = new StateVec(0);
        initSV.x = - trk.getD0() * Math.sin(trk.getPhi0());
        initSV.y = trk.getD0() * Math.cos(trk.getPhi0());
        initSV.z = trk.getZ0();

        double xcen = (1. / trk.getOmega() - trk.getD0()) * Math.sin(trk.getPhi0());
        double ycen = (-1. / trk.getOmega() + trk.getD0()) * Math.cos(trk.getPhi0());

        B Bf = new B(0, (float)initSV.x, (float)initSV.x, (float)initSV.z, swimmer);
        
        if(Math.abs(Bf.Bz)<0.001)
           this.straight = true;
        
        initSV.alpha = Bf.alpha;
        initSV.kappa = Bf.alpha * trk.getOmega();
        initSV.phi0 = Math.atan2(ycen, xcen);
        if (initSV.kappa < 0) {
            initSV.phi0 = Math.atan2(-ycen, -xcen);
        }

        initSV.dz    = trk.getZ();
        initSV.tanL  = trk.getTanL();
        initSV.d_rho = trk.getD0()*(Math.cos(trk.getPhi0())*Math.sin(initSV.phi0) -Math.sin(trk.getPhi0())*Math.cos(initSV.phi0));

        double x0 = X0.get(0) + initSV.d_rho * Math.cos(initSV.phi0) ;
        double y0 = Y0.get(0) + initSV.d_rho * Math.sin(initSV.phi0) ;
        double z0 = Z0.get(0) + initSV.dz ;
        double invKappa = 1. / Math.abs(initSV.kappa);
        double px0 = -invKappa * Math.sin(initSV.phi0 );
        double py0 = invKappa * Math.cos(initSV.phi0 );
        double pz0 = invKappa * initSV.tanL;

        initSV.phi = 0;

        this.trackTraj.put(0, initSV);
        CovMat initCM = new CovMat(0);
        double[][] covKF = new double[5][5]; 
    
        for(int ic = 0; ic<5; ic++) {
            for(int ir = 0; ir<5; ir++) {
                covKF[ic][ir]=cov[ic][ir];
            }
        }
    
        covKF[2][2] = cov[2][2]*600 ;

        initCM.covMat = covKF;
        this.trackCov.put(0, initCM);
    }

    @Override
    public void printlnStateVec(StateVec S) {
        System.out.println(S.k + ") drho " + S.d_rho + " phi0 " + S.phi0 + " kappa " + S.kappa + " dz " + S.dz + " tanL " + S.tanL + " phi " + S.phi + " x " + S.x + " y " + S.y + " z " + S.z + " alpha " + S.alpha);
    }


    @Override
    public void init(double x0, double z0, double tx, double tz, double units, double[][] cov, AKFitter kf) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
