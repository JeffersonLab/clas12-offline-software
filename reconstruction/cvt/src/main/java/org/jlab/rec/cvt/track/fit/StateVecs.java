package org.jlab.rec.cvt.track.fit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.track.Seed;
import org.jlab.rec.cvt.trajectory.Helix;

import Jama.Matrix;
import org.jlab.clas.swimtools.Swim;
import org.jlab.rec.cvt.svt.Constants;

public class StateVecs {

    final static double speedLight = Constants.LIGHTVEL;

    public List<B> bfieldPoints = new ArrayList<B>();
    public Map<Integer, StateVec> trackTraj = new HashMap<Integer, StateVec>();
    public Map<Integer, CovMat> trackCov = new HashMap<Integer, CovMat>();

    public StateVec StateVec;
    public CovMat CovMat;
    public Matrix F;

    public List<Double> X0;
    public List<Double> Y0;
    public List<Double> Z0; // reference points

    public List<Integer> Layer;
    public List<Integer> Sector;

    public double[] getStateVecPosAtModule(int k, StateVec kVec, org.jlab.rec.cvt.svt.Geometry svt_geo, org.jlab.rec.cvt.bmt.Geometry bmt_geo, int type) {
      
        double xc = X0.get(k) + (kVec.d_rho + kVec.alpha / kVec.kappa) * Math.cos(kVec.phi0);
        double yc = Y0.get(k) + (kVec.d_rho + kVec.alpha / kVec.kappa) * Math.sin(kVec.phi0);
        double r = Math.abs(kVec.alpha / kVec.kappa);
        Vector3D ToPoint = new Vector3D();
        Vector3D ToRef = new Vector3D(X0.get(k) - xc, Y0.get(k) - yc, 0);
        double[] value = new double[4]; // x,y,z,phi
        double X = 0;
        double Y = 0;

        if (type != 0) { //BMT
            double rm = 0;
            if (type == 1) {
                rm = org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[(Layer.get(k) - 5) / 2 - 1] + org.jlab.rec.cvt.bmt.Constants.hStrip2Det;
            }
            if (type == 2) {
                rm = org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[(Layer.get(k) - 5) / 2 - 1] + org.jlab.rec.cvt.bmt.Constants.hStrip2Det;
            }
            
            double a = 0.5 * (rm * rm - r * r + xc * xc + yc * yc) / yc;
            double b = -xc / yc;

            double delta = a * a * b * b - (1 + b * b) * (a * a - rm * rm);

            double xp = (-a * b + Math.sqrt(delta)) / (1 + b * b);
            double xm = (-a * b - Math.sqrt(delta)) / (1 + b * b);

            double yp = a + b * xp;
            double ym = a + b * xm;

            if (bmt_geo.isInSector(Layer.get(k) - 6, Math.atan2(ym, xm), Math.toRadians(org.jlab.rec.cvt.bmt.Constants.isInSectorJitter)) == Sector.get(k)) {
                X = xm;
                Y = ym;
            }
            if (bmt_geo.isInSector(Layer.get(k) - 6, Math.atan2(yp, xp), Math.toRadians(org.jlab.rec.cvt.bmt.Constants.isInSectorJitter)) == Sector.get(k)) {
                X = xp;
                Y = yp;
            }
            //System.out.println("R="+rm+" sector "+Sector.get(k)+" [xm, ym]= ["+xm+","+ym+"]; "
            //        + "[xp,yp]= ["+xp+","+yp+"]; [x,y]= ["+X+","+Y+"]"+
            //"+sec "+bmt_geo.isInSector(Layer.get(k)-6, Math.atan2(yp, xp), 
            //        Math.toRadians(org.jlab.rec.cvt.bmt.Constants.isInSectorJitter))
            //        +"-sec "+bmt_geo.isInSector(Layer.get(k)-6, Math.atan2(ym, xm), 
            //                Math.toRadians(org.jlab.rec.cvt.bmt.Constants.isInSectorJitter)));
        } else {

            // Find the intersection of the helix circle with the module plane projection in XY which is a line
            // Plane representative line equation y = mx +d
            Point3D Or = svt_geo.getPlaneModuleOrigin(Sector.get(k), Layer.get(k));
            Point3D En = svt_geo.getPlaneModuleEnd(Sector.get(k), Layer.get(k));

            if (En.x() - Or.x() == 0) {
                X = Or.x();
                double y1 = yc + Math.sqrt(r * r - (X - xc) * (X - xc));
                double y2 = yc - Math.sqrt(r * r - (X - xc) * (X - xc));

                if (Math.abs(y1 - Or.y()) < Math.abs(En.y() - Or.y())+10) {
                    Y = y1;
                } else {
                    if (Math.abs(y2 - Or.y()) < Math.abs(En.y() - Or.y())+10) {
                        Y = y2;
                    }
                }
            }
            if (En.y() - Or.y() == 0) {
                Y = Or.y();
                double x1 = xc + Math.sqrt(r * r - (Y - yc) * (Y - yc));
                double x2 = xc - Math.sqrt(r * r - (Y - yc) * (Y - yc));

                if (Math.abs(x1 - Or.x()) < Math.abs(En.x() - Or.x())+10) {
                    X = x1;
                } else {
                    if (Math.abs(x2 - Or.x()) < Math.abs(En.x() - Or.x())+10) {
                        X = x2;
                    }
                }
            }

            if (En.x() - Or.x() != 0 && En.y() - Or.y() != 0) {
                double m = (En.y() - Or.y()) / (En.x() - Or.x());
                double d = Or.y() - Or.x() * m;

                //double del = r*r*(1+m*m) - (yc-m*xc-d)*(yc-m*xc-d);
                double del = (xc + (-d + yc) * m) * (xc + (-d + yc) * m) - (1 + m * m) * (xc * xc + (d - yc) * (d - yc) - r * r);
                if (del < 0) {
                    return null;
                }
                double x1 = (xc + (-d + yc) * m + Math.sqrt(del)) / (1 + m * m);
                double x2 = (xc + (-d + yc) * m - Math.sqrt(del)) / (1 + m * m);

                if (Math.abs(x1 - Or.x()) < Math.abs(En.x() - Or.x())+10) {
                    X = x1;
                } else {
                    if (Math.abs(x2 - Or.x()) < Math.abs(En.x() - Or.x())+10) {
                        X = x2;
                    }
                }
                double y1 = yc + Math.sqrt(r * r - (X - xc) * (X - xc));
                double y2 = yc - Math.sqrt(r * r - (X - xc) * (X - xc));

                if (Math.abs(y1 - Or.y()) < Math.abs(En.y() - Or.y())+10) {
                    Y = y1;
                } else {
                    if (Math.abs(y2 - Or.y()) < Math.abs(En.y() - Or.y())+10) {
                        Y = y2;
                    }
                }
            }
            //System.out.println(" sector "+Sector.get(k)+" layer "+Layer.get(k)+" [x,y]= ["+X+","+Y+"]");
            
        }

        ToPoint = new Vector3D(X - xc, Y - yc, 0);
        double phi = ToRef.angle(ToPoint);
        phi *= -Math.signum(kVec.kappa);
        double x = X0.get(k) + kVec.d_rho * Math.cos(kVec.phi0) + kVec.alpha / kVec.kappa * (Math.cos(kVec.phi0) - Math.cos(kVec.phi0 + phi));
        double y = Y0.get(k) + kVec.d_rho * Math.sin(kVec.phi0) + kVec.alpha / kVec.kappa * (Math.sin(kVec.phi0) - Math.sin(kVec.phi0 + phi));
        double z = Z0.get(k) + kVec.dz - kVec.alpha / kVec.kappa * kVec.tanL * phi;

        value[0] = x;
        value[1] = y;
        value[2] = z;
        value[3] = phi;

        return value;
    }

    public void getStateVecAtModule(int k, StateVec kVec, org.jlab.rec.cvt.svt.Geometry sgeo, 
            org.jlab.rec.cvt.bmt.Geometry bgeo, int type, Swim swimmer) {

        StateVec newVec = kVec;
        double[] pars = this.getStateVecPosAtModule(k, kVec, sgeo, bgeo, type);
        if (pars == null) {
            return;
        }
        //System.out.println(" k "+k+" "+pars[0]+", "+pars[1]);
        newVec.x = pars[0];
        newVec.y = pars[1];
        newVec.z = pars[2];

        newVec.alpha = new B(k, newVec.x, newVec.y, newVec.z, swimmer).alpha;
        newVec.phi = pars[3];

        // new state: 
        kVec = newVec;
    }

    public StateVec newStateVecAtModule(int k, StateVec kVec, org.jlab.rec.cvt.svt.Geometry sgeo, 
            org.jlab.rec.cvt.bmt.Geometry bgeo, int type, Swim swimmer) {

        StateVec newVec = kVec;
        double[] pars = this.getStateVecPosAtModule(k, kVec, sgeo, bgeo, type);
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

    public void transport(int i, int f, StateVec iVec, CovMat icovMat, 
            org.jlab.rec.cvt.svt.Geometry sgeo, org.jlab.rec.cvt.bmt.Geometry bgeo, int type, 
            Swim swimmer) { // s = signed step-size
        if (iVec.phi0 < 0) {
            iVec.phi0 += 2. * Math.PI;
        }

        B Bf = new B(i, iVec.x, iVec.y, iVec.z, swimmer);

        double Xc = X0.get(i) + (iVec.d_rho + iVec.alpha / iVec.kappa) * Math.cos(iVec.phi0);
        double Yc = Y0.get(i) + (iVec.d_rho + iVec.alpha / iVec.kappa) * Math.sin(iVec.phi0);

        // transport stateVec...
        StateVec fVec = new StateVec(f);

        double phi_f = Math.atan2(Yc - Y0.get(f), Xc - X0.get(f));
        if (iVec.kappa < 0) {
            phi_f = Math.atan2(-Yc + Y0.get(f), -Xc + X0.get(f));
        }

        if (phi_f < 0) {
            phi_f += 2 * Math.PI;
        }
        fVec.phi0 = phi_f;

        fVec.d_rho = (Xc - X0.get(f)) * Math.cos(phi_f) + (Yc - Y0.get(f)) * Math.sin(phi_f) - Bf.alpha / iVec.kappa;

        fVec.kappa = iVec.kappa;

        double[] ElossTot = ELoss_hypo(iVec, f - i);
        for (int e = 0; e < 3; e++) {
            ElossTot[e] = iVec.get_ELoss()[e] + ElossTot[e];
        }
        fVec.set_ELoss(ElossTot);

        fVec.dz = Z0.get(i) - Z0.get(f) + iVec.dz - (Bf.alpha / iVec.kappa) * (phi_f - iVec.phi0) * iVec.tanL;

        fVec.tanL = iVec.tanL;

        //Bf = new B(f, X0.get(f), Y0.get(f), Z0.get(f));
        fVec.alpha = Bf.alpha;

        ////System.out.println("... B "+Bf.Bz+"Z0.get(i)"+ Z0.get(i) +" Z0.get(f) "+Z0.get(f));
        this.getStateVecAtModule(f, fVec, sgeo, bgeo, type, swimmer);

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

        double dz_prm_del_drho = ((iVec.alpha / iVec.kappa) / (fVec.dz + iVec.alpha / iVec.kappa)) * iVec.tanL * Math.sin(fVec.phi0 - iVec.phi0);
        double dz_prm_del_phi0 = (iVec.alpha / iVec.kappa) * iVec.tanL * (1 - Math.cos(fVec.phi0 - iVec.phi0) * (iVec.dz + iVec.alpha / iVec.kappa) / (fVec.dz + iVec.alpha / iVec.kappa));
        double dz_prm_del_kappa = (iVec.alpha / (iVec.kappa * iVec.kappa)) * iVec.tanL * (fVec.phi0 - iVec.phi0 - Math.sin(fVec.phi0 - iVec.phi0) * (iVec.alpha / iVec.kappa) / (fVec.dz + iVec.alpha / iVec.kappa));
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

        //StateVec = fVec;
        this.trackTraj.put(f, fVec);
        F = new Matrix(FMat);
        Matrix FT = F.transpose();
        Matrix Cpropagated = FT.times(icovMat.covMat).times(F);
        //if(Z0.get(i)!=Z0.get(f))
        //	Cpropagated = icovMat.covMat;
        if (Cpropagated != null) {
            CovMat fCov = new CovMat(f);
            fCov.covMat = Cpropagated.plus(this.Q(iVec, f - i));
            //CovMat = fCov;
            this.trackCov.put(f, fCov);
        }
    }

    private double get_t_ov_X0(double radius) {
        double value = Constants.SILICONTHICK / Constants.SILICONRADLEN;
        if(radius>=org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[0]) 
            value = org.jlab.rec.cvt.bmt.Constants.get_T_OVER_X0()[this.getBMTLayer(radius)-1];
        return value;
    }
    
    private double detMat_Z_ov_A_timesThickn(double radius) {    
        double value = 0;
        if(radius>=Constants.MODULERADIUS[0][0]&& radius<org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[0])
            value = org.jlab.rec.cvt.svt.Constants.detMatZ_ov_A_timesThickn;
        if(radius>=org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[0] && this.getBMTLayer(radius)>0)
            value = org.jlab.rec.cvt.bmt.Constants.getEFF_Z_OVER_A()[this.getBMTLayer(radius)-1];
        return value;
    }
    private int getBMTLayer(double radius) {
        int layer = 0;
        if(radius>=org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[0] && radius<org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[0])
            layer=1;
        if(radius>=org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[0] && radius<org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[1])
            layer=2;
        if(radius>=org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[1] && radius<org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[1])
            layer=3;
        if(radius>=org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[1] && radius<org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[2])
            layer=4;
        if(radius>=org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[2] && radius<org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[2])
           layer=5;
        if(radius>=org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[2])
           layer=6;
       
        return layer;
    }
    private double[] ELoss_hypo(StateVec iVec, int dir) {
        double[] Eloss = new double[3]; //Eloss for pion, kaon, proton hypotheses

        if (dir < 0  || Math.sqrt(iVec.x*iVec.x+iVec.y*iVec.y)<Constants.MODULERADIUS[0][0]) {
            return Eloss;
        }

        Vector3D trkDir = this.P(iVec.k);
        trkDir.unit();
        double cosEntranceAngle = trkDir.z();
       // System.out.println(" cosTrk "+Math.toDegrees(Math.acos(trkDir.z()))+" at state "+iVec.k+" dir "+dir);
        double pt = Math.abs(1. / iVec.kappa);
        double pz = pt * iVec.tanL;
        double p = Math.sqrt(pt * pt + pz * pz);

        for (int hyp = 2; hyp < 5; hyp++) {

            double mass = MassHypothesis(hyp); // assume given mass hypothesis
            double beta = p / Math.sqrt(p * p + mass * mass); // use particle momentum
            double gamma = 1. / Math.sqrt(1 - beta * beta);

            double s = MassHypothesis(1) / mass;

            double Wmax = 2. * mass * beta * beta * gamma * gamma / (1. + 2. * s * gamma + s * s);
            double I = 0.000000172;

            double logterm = 2. * mass * beta * beta * gamma * gamma * Wmax / (I * I);

            double delta = 0.;
            double dEdx = 0.00001535 * this.detMat_Z_ov_A_timesThickn(Math.sqrt(iVec.x*iVec.x+iVec.y*iVec.y)) * (Math.log(logterm) - 2 * beta * beta - delta) / (beta * beta); //in GeV/mm
            //System.out.println(" mass hy "+hyp+" Mat at "+Math.sqrt(iVec.x*iVec.x+iVec.y*iVec.y)+"Z/A*t "+this.detMat_Z_ov_A_timesThickn(Math.sqrt(iVec.x*iVec.x+iVec.y*iVec.y))+" dEdx "+dEdx);
            Eloss[hyp - 2] = dir * Math.abs(dEdx / cosEntranceAngle);
        }
        return Eloss;
    }

    private Matrix Q(StateVec iVec, int dir) {

        Matrix Q = new Matrix(new double[][]{
            {0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0}
        });

        // if (iVec.k % 2 == 1 && dir > 0) {
        if (dir >0 && Math.sqrt(iVec.x*iVec.x+iVec.y*iVec.y)>=Constants.MODULERADIUS[0][0]) {
            Vector3D trkDir = this.P(iVec.k);
            trkDir.unit();
            double cosEntranceAngle = Math.abs(this.P(iVec.k).z());

            double pt = Math.abs(1. / iVec.kappa);
            double pz = pt * iVec.tanL;
            double p = Math.sqrt(pt * pt + pz * pz);

            //double t_ov_X0 = 2. * 0.32 / Constants.SILICONRADLEN; //path length in radiation length units = t/X0 [true path length/ X0] ; Si radiation length = 9.36 cm
            double t_ov_X0 = this.get_t_ov_X0(Math.sqrt(iVec.x*iVec.x+iVec.y*iVec.y)); //System.out.println(Math.log(t_ov_X0)/9.+" rad "+Math.sqrt(iVec.x*iVec.x+iVec.y*iVec.y)+" t/x0 "+t_ov_X0);
            double mass = MassHypothesis(2);   // assume given mass hypothesis (2=pion)
            double beta = p / Math.sqrt(p * p + mass * mass); // use particle momentum
            double pathLength = t_ov_X0 / cosEntranceAngle;
//0.0136?

            double sctRMS = (0.00141 / (beta * p)) * Math.sqrt(pathLength) * (1 + Math.log10(pathLength)/9.); // Highland-Lynch-Dahl formula
            
            Q = new Matrix(new double[][]{
                {0, 0, 0, 0, 0},
                {0, sctRMS*sctRMS * (1 + iVec.tanL * iVec.tanL), 0, 0, 0},
                {0, 0, sctRMS*sctRMS * (iVec.kappa * iVec.kappa * iVec.tanL * iVec.tanL), 0, sctRMS*sctRMS * (iVec.kappa * iVec.tanL * (1 + iVec.tanL * iVec.tanL))},
                {0, 0, 0, 0, 0},
                {0, 0, sctRMS*sctRMS * (iVec.kappa * iVec.tanL * (1 + iVec.tanL * iVec.tanL)), 0, sctRMS*sctRMS * (1 + iVec.tanL * iVec.tanL) * (1 + iVec.tanL * iVec.tanL)}
            });
        }

        return Q;

    }

    public class StateVec {

        final int k;

        public double x;
        public double y;
        public double z;
        public double kappa;
        public double d_rho;
        public double phi0;
        public double phi;
        public double tanL;
        public double dz;
        public double alpha;

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

    private double shift = org.jlab.rec.cvt.Constants.getZoffset();
    public class B {

        final int k;
        double x;
        double y;
        double z;
        Swim swimmer;
        
        public double Bx;
        public double By;
        public double Bz;

        public double alpha;

        float b[] = new float[3];
        B(int k, double x, double y, double z, Swim swimmer) {
            this.k = k;
            this.x = x;
            this.y = y;
            this.z = z;

            swimmer.BfieldLab(x / 10, y / 10, z / 10 + shift, b);
            this.Bx = b[0];
            this.By = b[1];
            this.Bz = b[2];

            this.alpha = 1. / (StateVecs.speedLight * Math.sqrt(b[0]*b[0]+b[1]*b[1]+b[2]*b[2]));
            //this.alpha = 1. / (5.);
        }
    }

    //public String massHypo = "pion";
    public double MassHypothesis(int H) {
        double piMass = 0.13957018;
        double KMass = 0.493677;
        double muMass = 0.105658369;
        double eMass = 0.000510998;
        double pMass = 0.938272029;
        double value = piMass; //default
        if (H == 4) {
            value = pMass;
        }
        if (H == 1) {
            value = eMass;
        }
        if (H == 2) {
            value = piMass;
        }
        if (H == 3) {
            value = KMass;
        }
        if (H == 0) {
            value = muMass;
        }
        return value;
    }

    public Vector3D P(int kf) {
        if (this.trackTraj.get(kf) != null) {
            //double x = this.trackTraj.get(kf).x;
            //double y = this.trackTraj.get(kf).y;
            //double z = this.trackTraj.get(kf).z; 
            //B Bf = new B(kf, x, y, z);
            double px = -Math.signum(1 / this.trackTraj.get(kf).kappa) * Math.sin(this.trackTraj.get(kf).phi0 + this.trackTraj.get(kf).phi);
            double py = Math.signum(1 / this.trackTraj.get(kf).kappa) * Math.cos(this.trackTraj.get(kf).phi0 + this.trackTraj.get(kf).phi);
            double pz = Math.signum(1 / this.trackTraj.get(kf).kappa) * this.trackTraj.get(kf).tanL;
            //int q = (int) Math.signum(this.trackTraj.get(kf).kappa);

            return new Vector3D(px, py, pz);
        } else {
            return new Vector3D(0, 0, 0);
        }

    }

    public Helix setTrackPars(int kf) {

        double x = this.trackTraj.get(kf).d_rho * Math.cos(this.trackTraj.get(kf).phi0);
        double y = this.trackTraj.get(kf).d_rho * Math.sin(this.trackTraj.get(kf).phi0);
        double z = this.trackTraj.get(kf).dz;
        double px = -Math.abs(1. / this.trackTraj.get(kf).kappa) * Math.sin(this.trackTraj.get(kf).phi0);
        double py = Math.abs(1. / this.trackTraj.get(kf).kappa) * Math.cos(this.trackTraj.get(kf).phi0);
        double pz = Math.abs(1. / this.trackTraj.get(kf).kappa) * this.trackTraj.get(kf).tanL;
        int q = (int) Math.signum(this.trackTraj.get(kf).kappa);
        double p_unc = Math.sqrt(px * px + py * py + pz * pz);

        double E_loss = this.trackTraj.get(kf).get_ELoss()[2];

        double h_dca = Math.sqrt(x * x + y * y);
        double h_phi0 = Math.atan2(py, px);
        if(Math.abs(Math.sin(h_phi0))>0.1) {
            h_dca = -x/Math.sin(h_phi0);
        } else {
            h_dca = y/Math.cos(h_phi0);
        }
            
        double kappa = Math.signum(this.trackTraj.get(kf).kappa) / Math.sqrt(px * px + py * py);
        double h_omega = kappa / this.trackTraj.get(kf).alpha; h_omega = kappa/this.trackTraj.get(0).alpha;
        double h_dz = z;
        double h_tandip = pz / Math.sqrt(px * px + py * py);
        
        Helix trkHelix = new Helix(h_dca, h_phi0, h_omega, h_dz, h_tandip, this.trackCov.get(kf).covMat);
       // System.out.println("x "+x+" y "+y+" x' "+(-h_dca*Math.sin(h_phi0))+" y' "+y*Math.cos(h_phi0) +" theta "+Math.toDegrees(Math.acos(pz/Math.sqrt(px*px+py*py+pz*pz)))+" phi "+Math.toDegrees(Math.atan2(py, px))+" q "+q);

        return trkHelix;
    }

    public void init(Seed trk, KFitter kf, Swim swimmer) {
        //init stateVec
        StateVec initSV = new StateVec(0);
        initSV.x = -trk.get_Helix().get_dca() * Math.sin(trk.get_Helix().get_phi_at_dca());
        initSV.y = trk.get_Helix().get_dca() * Math.cos(trk.get_Helix().get_phi_at_dca());
        initSV.z = trk.get_Helix().get_Z0();
        double xcen = (1. / trk.get_Helix().get_curvature() - trk.get_Helix().get_dca()) * Math.sin(trk.get_Helix().get_phi_at_dca());
        double ycen = (-1. / trk.get_Helix().get_curvature() + trk.get_Helix().get_dca()) * Math.cos(trk.get_Helix().get_phi_at_dca());
        B Bf = new B(0, (float)org.jlab.rec.cvt.Constants.getXb(), (float)org.jlab.rec.cvt.Constants.getYb(), initSV.z, swimmer);
        initSV.alpha = Bf.alpha;
        initSV.kappa = Bf.alpha * trk.get_Helix().get_curvature();
        initSV.phi0 = Math.atan2(ycen, xcen);
        if (initSV.kappa < 0) {
            initSV.phi0 = Math.atan2(-ycen, -xcen);
        }
        initSV.dz = trk.get_Helix().get_Z0();
        initSV.tanL = trk.get_Helix().get_tandip();
        initSV.d_rho = trk.get_Helix().get_dca();
        initSV.phi = 0;
        //
        
        this.trackTraj.put(0, initSV);
        //init covMat
        Matrix fitCovMat = trk.get_Helix().get_covmatrix();
        double cov_d02 = fitCovMat.get(0, 0);
        double cov_d0phi0 = fitCovMat.get(0, 1);
        double cov_d0rho = Bf.alpha * fitCovMat.get(0, 2);
        double cov_phi02 = fitCovMat.get(1, 1);
        double cov_phi0rho = Bf.alpha * fitCovMat.get(1, 2);
        double cov_rho2 = Bf.alpha * Bf.alpha * fitCovMat.get(2, 2);
        double cov_z02 = fitCovMat.get(3, 3);
        double cov_z0tandip = fitCovMat.get(3, 4);
        double cov_tandip2 = fitCovMat.get(4, 4);

        double components[][] = new double[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                components[i][j] = 0;
            }
        }

        components[0][0] = cov_d02;
        components[0][1] = cov_d0phi0;
        components[1][0] = cov_d0phi0;
        components[1][1] = cov_phi02;
        components[2][0] = cov_d0rho;
        components[0][2] = cov_d0rho;
        components[2][1] = cov_phi0rho;
        components[1][2] = cov_phi0rho;
        components[2][2] = cov_rho2;
        components[3][3] = cov_z02;
        components[3][4] = cov_z0tandip;
        components[4][3] = cov_z0tandip;
        components[4][4] = cov_tandip2;

        Matrix initCMatrix = new Matrix(components);

        CovMat initCM = new CovMat(0);
        initCM.covMat = initCMatrix;

        this.trackCov.put(0, initCM);
    }

    public void printMatrix(Matrix C) {
        for (int k = 0; k < 5; k++) {
            System.out.println(C.get(k, 0) + "	" + C.get(k, 1) + "	" + C.get(k, 2) + "	" + C.get(k, 3) + "	" + C.get(k, 4));
        }
    }

    public void printlnStateVec(StateVec S) {
        System.out.println(S.k + ") drho " + S.d_rho + " phi0 " + S.phi0 + " kappa " + S.kappa + " dz " + S.dz + " tanL " + S.tanL + " phi " + S.phi + " x " + S.x + " y " + S.y + " z " + S.z + " alpha " + S.alpha);
    }
}
