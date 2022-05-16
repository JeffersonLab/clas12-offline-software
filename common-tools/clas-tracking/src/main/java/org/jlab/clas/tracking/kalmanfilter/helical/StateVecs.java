package org.jlab.clas.tracking.kalmanfilter.helical;

import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.tracking.kalmanfilter.AMeasVecs;
import org.jlab.clas.tracking.kalmanfilter.AMeasVecs.MeasVec;
import org.jlab.clas.tracking.kalmanfilter.AStateVecs;
import org.jlab.clas.tracking.kalmanfilter.Surface;
import org.jlab.clas.tracking.kalmanfilter.Units;
import org.jlab.clas.tracking.trackrep.Helix;
import org.jlab.clas.tracking.utilities.MatrixOps;
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

            Point3D st   = new Point3D(sv.x, sv.y, sv.z); 
            Vector3D stu = new Vector3D(sv.px,sv.py,sv.pz).asUnit();
            
            if(mv.surface.plane!=null) {
                Line3D toPln = new Line3D(st, stu);
                Point3D inters = new Point3D();
                int ints = mv.surface.plane.intersection(toPln, inters);
                sv.x = inters.x();
                sv.y = inters.y();
                sv.z = inters.z();  
                sv.path = inters.distance(st);
            }
            else if(mv.surface.cylinder!=null) {
                mv.surface.toLocal().apply(st);
                mv.surface.toLocal().apply(stu);
                double r = mv.surface.cylinder.baseArc().radius();
                double delta = Math.sqrt((st.x()*stu.x()+st.y()*stu.y())*(st.x()*stu.x()+st.y()*stu.y())-(-r*r+st.x()*st.x()+st.y()*st.y())*(stu.x()*stu.x()+stu.y()*stu.y()));
                double l = (-(st.x()*stu.x()+st.y()*stu.y())+delta)/(stu.x()*stu.x()+stu.y()*stu.y());
                if(Math.signum(st.y()+l*stu.y())!=mv.hemisphere) {
                    l = (-(st.x()*stu.x()+st.y()*stu.y())-delta)/(stu.x()*stu.x()+stu.y()*stu.y()); 
                } 
                Point3D inters = new Point3D(st.x()+l*stu.x(),st.y()+l*stu.y(),st.z()+l*stu.z());
                mv.surface.toGlobal().apply(inters);
                // RDV: should switch to use clas-geometry intersection method, not done now to alwys return a value
                sv.x = inters.x();
                sv.y = inters.y();
                sv.z = inters.z();
                sv.path = inters.distance(st);
            }
        } else { 
            if(swim==null) { // applicable only to planes parallel to the z -axis
                Helix helix = sv.getHelix(xref, yref);
                if(mv.surface.plane!=null) {
                    Point3D pos = helix.getHelixPointAtPlane(mv.surface.finitePlaneCorner1.x(), mv.surface.finitePlaneCorner1.y(),
                                                             mv.surface.finitePlaneCorner2.x(), mv.surface.finitePlaneCorner2.y(), 10);
                    Vector3D mom = helix.getMomentumAtPlane(mv.surface.finitePlaneCorner1.x(), mv.surface.finitePlaneCorner1.y(),
                                                            mv.surface.finitePlaneCorner2.x(), mv.surface.finitePlaneCorner2.y(), 10);
                    sv.path = helix.getLAtPlane(mv.surface.finitePlaneCorner1.x(), mv.surface.finitePlaneCorner1.y(),
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
                    Point3D pos = helix.getHelixPointAtR(r);
                    Vector3D mom = helix.getMomentumAtR(r);
                    sv.path = helix.getLAtR(r);
                    sv.x = pos.x();
                    sv.y = pos.y();
                    sv.z = pos.z();
                    sv.px = mom.x();
                    sv.py = mom.y();
                    sv.pz = mom.z();
                }
            }
            else {
                swim.SetSwimParameters(sv.x/units.value(), sv.y/units.value(), sv.z/units.value(), 
                                   dir*sv.px, dir*sv.py, dir*sv.pz, 
                                   KFitter.polarity*(int) Math.signum(sv.kappa)*dir);
                if(mv.surface.plane!=null) {
                    Vector3D norm = mv.surface.plane.normal();
                    Point3D point = new Point3D(mv.surface.plane.point().x()/units.value(),
                                                mv.surface.plane.point().y()/units.value(),
                                                mv.surface.plane.point().z()/units.value());
                    double accuracy = mv.surface.swimAccuracy/units.value();
                    swimPars = swim.SwimPlane(norm,point,accuracy);
 //                   swimPars = swim.AdaptiveSwimPlane(point.x(), point.y(), point.z(), norm.x(), norm.y(), norm.z(), accuracy);
                    if(swimPars==null)
                        return false;

                    sv.x = swimPars[0]*units.value();
                    sv.y = swimPars[1]*units.value();
                    sv.z = swimPars[2]*units.value();
                    sv.px = swimPars[3]*dir;
                    sv.py = swimPars[4]*dir;
                    sv.pz = swimPars[5]*dir;
                    sv.path = swimPars[6]*units.value();
                }
                else   {
                    double r = mv.surface.cylinder.baseArc().radius();
                    Point3D p1 = new Point3D(mv.surface.cylinder.getAxis().origin().x()/units.value(),
                                             mv.surface.cylinder.getAxis().origin().y()/units.value(),
                                             mv.surface.cylinder.getAxis().origin().z()/units.value()) ;
                    Point3D p2 = new Point3D(mv.surface.cylinder.getAxis().end().x()/units.value(),
                                             mv.surface.cylinder.getAxis().end().y()/units.value(),
                                             mv.surface.cylinder.getAxis().end().z()/units.value()) ;
                    double accuracy = mv.surface.swimAccuracy/units.value();
                    swimPars = swim.SwimGenCylinder(p1, p2, r/units.value(), accuracy);
                    if(swimPars==null)
                        return false;
                    sv.x = swimPars[0]*units.value();
                    sv.y = swimPars[1]*units.value();
                    sv.z = swimPars[2]*units.value();
                    sv.px = swimPars[3]*dir;
                    sv.py = swimPars[4]*dir;
                    sv.pz = swimPars[5]*dir;
                    sv.path = swimPars[6]*units.value();
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
    public void corrForEloss(int dir, StateVec vec, AMeasVecs mv) {
        
        if(this.straight || mass<0) return;
                       
        Surface  surf = mv.measurements.get(vec.k).surface;
        double pScale = surf.getEloss(vec.getMomentum(), mass, dir);
        if(pScale>0) {
            vec.kappa = vec.kappa/pScale;
            vec.energyLoss = surf.getEloss(vec.getMomentum().mag(), mass);
            vec.dx = surf.getDx(vec.getMomentum());
            vec.updateFromHelix();
        }
        else {
            vec = null;
        }
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
            double dphi0_prm_del_kappa = (fVec.kappa/iVec.kappa)*(iVec.alpha / (iVec.kappa * iVec.kappa)) / (fVec.d_rho + iVec.alpha / iVec.kappa) * Math.sin(deltaPhi0);
            double dphi0_prm_del_dz = 0;
            double dphi0_prm_del_tanL = 0;

            double drho_prm_del_drho = Math.cos(deltaPhi0);
            double drho_prm_del_phi0 = (iVec.d_rho + iVec.alpha / iVec.kappa) * Math.sin(deltaPhi0);
            double drho_prm_del_kappa = (fVec.kappa/iVec.kappa)*(iVec.alpha / (iVec.kappa * iVec.kappa)) * (1 - Math.cos(deltaPhi0));
            double drho_prm_del_dz = 0;
            double drho_prm_del_tanL = 0;

            double dkappa_prm_del_drho = 0;
            double dkappa_prm_del_phi0 = 0;
            double dkappa_prm_del_dkappa = fVec.kappa/iVec.kappa;
            double dkappa_prm_del_dz = 0;
            double dkappa_prm_del_tanL = 0;

            double dz_prm_del_drho = ((iVec.alpha / iVec.kappa) / (fVec.d_rho + iVec.alpha / iVec.kappa)) * iVec.tanL * Math.sin(deltaPhi0);
            double dz_prm_del_phi0 = (iVec.alpha / iVec.kappa) * iVec.tanL * (1 - Math.cos(deltaPhi0) * (iVec.d_rho + iVec.alpha / iVec.kappa) / (fVec.d_rho + iVec.alpha / iVec.kappa));
            double dz_prm_del_kappa = (fVec.kappa/iVec.kappa)*(iVec.alpha / (iVec.kappa * iVec.kappa)) * iVec.tanL * (deltaPhi0 - Math.sin(deltaPhi0) * (iVec.alpha / iVec.kappa) / (fVec.d_rho + iVec.alpha / iVec.kappa));
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
    public double[][] Q(StateVec vec, AMeasVecs mv) {
        double[][] Q = new double[5][5];
              
        if(this.mass<0) return Q;
                
        Surface surf = mv.measurements.get(vec.k).surface;
        double cosEntranceAngle = this.getLocalDirAtMeasSite(vec, mv.measurements.get(vec.k));

        double p = Math.sqrt(vec.px*vec.px + vec.py*vec.py + vec.pz*vec.pz);
        if(this.straight) p = 1;
        
        // Highland-Lynch-Dahl formula
        double sctRMS = surf.getThetaMS(p, mass, cosEntranceAngle);
        Q = new double[][]{
            {0, 0, 0, 0, 0},
            {0, sctRMS*sctRMS * (1 + vec.tanL * vec.tanL), 0, 0, 0},
            {0, 0, sctRMS*sctRMS * (vec.kappa * vec.kappa * vec.tanL * vec.tanL), 0, sctRMS*sctRMS * (vec.kappa * vec.tanL * (1 + vec.tanL * vec.tanL))},
            {0, 0, 0, 0, 0},
            {0, 0, sctRMS*sctRMS * (vec.kappa * vec.tanL * (1 + vec.tanL * vec.tanL)), 0, sctRMS*sctRMS * (1 + vec.tanL * vec.tanL) * (1 + vec.tanL * vec.tanL)}
        };
        
        return Q;
    }
    
    @Override
    public void init(Helix helix, double[][] cov, double xref, double yref, double zref, double mass, Swim swimmer) {

        this.units     = helix.getUnits();
        this.lightVel  = helix.getLightVelocity();

        this.xref = xref;
        this.yref = yref;
        this.zref = zref;
        
        this.mass = mass;
        
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

        // set bfield according to input helix for consistency
        initSV.alpha = 1/(helix.getB()*helix.getLightVelocity());
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
    public void printlnStateVec(StateVec S) {
        String s = String.format("%d) drho=%.4f phi0=%.4f kappa=%.4f dz=%.4f tanL=%.4f alpha=%.4f\n", S.k, S.d_rho, S.phi0, S.kappa, S.dz, S.tanL, S.alpha);
        s       += String.format("    x0=%.4f y0=%.4f z0=%.4f", S.x0, S.y0, S.z0);
        s       += String.format("    phi=%.4f x=%.4f y=%.4f z=%.4f px=%.4f py=%.4f pz=%.4f", S.phi, S.x, S.y, S.z, S.px, S.py, S.pz);
        System.out.println(s);
    }


    @Override
    public void init(double x0, double z0, double tx, double tz, Units units, double[][] cov) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
