package org.jlab.clas.tracking.kalmanfilter;

import java.util.HashMap;
import java.util.Map;

import org.jlab.geom.prim.Vector3D;

import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.tracking.kalmanfilter.AMeasVecs.MeasVec;
import org.jlab.clas.tracking.kalmanfilter.helical.KFitter;
import org.jlab.clas.tracking.trackrep.Helix;
import org.jlab.geom.prim.Point3D;

public abstract class AStateVecs {

    public Units units;
    public double lightVel;

    public double xref;
    public double yref;
    public double zref;

    public double mass;

    public StateVec initSV; 
    public StateVec lastSV; 
         
    public Map<Integer, StateVec> trackTrajS  = new HashMap<>(); // smoothed    
    public Map<Integer, StateVec> trackTrajB  = new HashMap<>(); // backward filtered    
    public Map<Integer, StateVec> trackTrajP  = new HashMap<>(); // backward transport    
    public Map<Integer, StateVec> trackTrajF  = new HashMap<>(); // filtered    
    public Map<Integer, StateVec> trackTrajT  = new HashMap<>(); // transport

    public boolean straight;

    public abstract void init(Helix trk, double[][] cov, double xref, double yref, double zref, double mass, Swim swimmer);

    public abstract void init(double x0, double z0, double tx, double tz, Units units, double[][] cov);

    public Map<Integer, StateVec> smoothed() {
        return this.trackTrajS;
    }
    
    public Map<Integer, StateVec> transported() {
        return this.transported(true);
    }
    
    public Map<Integer, StateVec> transported(boolean forward) {
        if(forward) return this.trackTrajT;
        else        return this.trackTrajP;
    }
    
    public Map<Integer, StateVec> filtered(boolean forward) {
        if(forward) return this.trackTrajF;
        else        return this.trackTrajB;
    }
        
    public abstract boolean setStateVecPosAtMeasSite(StateVec vec, MeasVec mv, Swim swimmer);

    public StateVec newStateVecAtMeasSite(StateVec vec, MeasVec mv, Swim swimmer) {
        StateVec newVec = new StateVec(vec);
        if(!this.setStateVecPosAtMeasSite(newVec, mv, swimmer))
            return null;
        else
            return newVec;
    }

    public abstract boolean getStateVecPosAtMeasSite(StateVec iVec, MeasVec mv, Swim swim);

    public StateVec transport(StateVec iVec, int f, AMeasVecs mv, Swim swimmer) {
        // transport state vector
        int dir = (int) Math.signum(f-iVec.k);
 
        StateVec fVec = new StateVec(iVec);
        
        if(dir>0) {
            this.corrForEloss(dir, fVec, mv);
            if(fVec == null) return fVec;
        }

        if(!this.setStateVecPosAtMeasSite(fVec, mv.measurements.get(f), swimmer)) return null;

        if(dir<0) this.corrForEloss(dir, fVec, mv);

        //transport covariance matrix
        double[][] fCov = this.propagateCovMat(iVec, fVec);
        double[][] fQ = null;
        
        if(dir>0) {
            double[][] iQ = this.Q(iVec, mv);
            fQ = this.propagateMatrix(iVec, fVec, iQ);
//            System.out.println("From " + i + " to " + f + " including material from surface " + i + " with X0 = " +  mv.measurements.get(i).surface.getToverX0());
//            System.out.println(mv.measurements.get(i).surface.toString());
                        }
        else {
            fQ =this.Q(fVec, mv);
//            System.out.println("From " + i + " to " + f + " including material from surface " + f + " with X0 = " +  mv.measurements.get(f).surface.getToverX0());
//            System.out.println(mv.measurements.get(f).surface.toString());
        }
        if (fCov != null) {
            fVec.covMat = addProcessNoise(fCov, fQ);            
        }
        
        return fVec;
    }
    
    public final void transport(int i, int f, AMeasVecs mv, Swim swimmer) {
        // transport state vector
        StateVec iVec = this.trackTrajT.get(i);
        StateVec fVec = this.transport(iVec, f, mv, swimmer);
        this.trackTrajT.put(f, fVec);
    }

    public abstract void corrForEloss(int dir, StateVec iVec, AMeasVecs mv);
    
    public final double[][] propagateCovMat(StateVec ivec, StateVec fvec) {
        return this.propagateMatrix(ivec, fvec, ivec.covMat);
    }
    
    private double[][] propagateMatrix(StateVec ivec, StateVec fvec, double[][] matrix) {
        double[][] FMat  = this.F(ivec, fvec);
        double[][] FMatT = this.transposeMatrix(FMat);
        ivec.F = FMat;
        return multiplyMatrices(FMat, matrix, FMatT);
    }
    
    private double[][] multiplyMatrices(double[][] firstMatrix, double[][] secondMatrix) {
        int r1 = firstMatrix.length;
        int c1 = firstMatrix[0].length;
        int c2 = secondMatrix[0].length;
        double[][] product = new double[r1][c2];
        for (int i = 0; i < r1; i++) {
            for (int j = 0; j < c2; j++) {
                for (int k = 0; k < c1; k++) {
                    product[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
                }
            }
        }

        return product;
    }

    public double[][] transposeMatrix(double[][] matrix) {
        double[][] result = new double[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                result[j][i] = matrix[i][j];
            }
        }
        return result;

    }

    public double[][] multiplyMatrices(double[][] firstMatrix, double[][] sMatrix, double[][] secondMatrix) {
        double[][] m1 = this.multiplyMatrices(sMatrix, secondMatrix);
        double[][] m2 = this.multiplyMatrices(firstMatrix, m1);

        return m2;
    }

    public double[][] addProcessNoise(double[][] C, double[][] Q) {
        double[][] result = new double[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                result[i][j] = C[i][j];
                if (Q[i][j] != 0) {
                    result[i][j] += Q[i][j];
                }
            }
        }

        return result;
    }

    public double getLocalDirAtMeasSite(StateVec vec, MeasVec mv) {
        if(mv.surface==null) 
            return 1;
        else if(mv.surface.type!=Type.PLANEWITHSTRIP && 
                mv.surface.type!=Type.CYLINDERWITHSTRIP && 
                mv.surface.type!=Type.LINE) 
            return 1;
        else {
            Point3D  pos = new Point3D(vec.x, vec.y, vec.z);
            Vector3D dir = new Vector3D(vec.px, vec.py, vec.pz).asUnit();
            if(mv.surface.type==Type.PLANEWITHSTRIP) {
                Vector3D norm = mv.surface.plane.normal();
                return Math.abs(norm.dot(dir));
            }
            else if(mv.surface.type==Type.CYLINDERWITHSTRIP) {
                mv.surface.toLocal().apply(pos);
                mv.surface.toLocal().apply(dir);
                Vector3D norm = pos.toVector3D().asUnit();
                return Math.abs(norm.dot(dir));
            }
            else if(mv.surface.type==Type.LINE) {
                Vector3D norm = mv.surface.lineEndPoint1.vectorTo(mv.surface.lineEndPoint2).asUnit();
                double cosdir = Math.abs(norm.dot(dir));
                return Math.sqrt(1-cosdir*cosdir);
            }
            return 0;
        }
    }
        
    public abstract double[][] Q(StateVec vec, AMeasVecs mv);

    public abstract double[][] F(StateVec ivec, StateVec fvec);

    public class StateVec {

        public int k;

        // regular coordinates
        public double x;
        public double y;
        public double z;
        public double px;
        public double py;
        public double pz;

        // helix variables
        public double kappa;
        public double d_rho;
        public double phi0;
        public double phi;
        public double tanL;
        public double dz;
        public double alpha;

        // helix pivot or cosmic rays intercept (y0=0) 
        public double x0;
        public double y0;
        public double z0;

        // cosmics rays
        public double tx; //=px/py
        public double tz; //=pz/py
        public double dl;

        public double residual;

        public double[][] covMat;
        
        public double[][] F;

        public double energyLoss;
        public double dx;
        public double path;

        public StateVec(int k) {
            this.k = k;
        }

        public StateVec(StateVec s) {
            this(s.k);
            this.copy(s);
        }

        public StateVec(int k, StateVec s) {
            this(k);
            this.copy(s);
        }

        public StateVec(int k, double xpivot, double ypivot, double zpivot, StateVec s) {
            this(k);
            this.copy(s);
            this.setPivot(xpivot, ypivot, zpivot);
        }

        public StateVec clone() {
            return new StateVec(this);
        }
        
        public final void copy(StateVec s) {
            this.d_rho = s.d_rho;
            this.phi0 = s.phi0;
            this.kappa = s.kappa;
            this.tanL = s.tanL;
            this.dz = s.dz;
            this.phi = s.phi;
            this.alpha = s.alpha;
            this.x0 = s.x0;
            this.y0 = s.y0;
            this.z0 = s.z0;
            this.tx = s.tx;
            this.tz = s.tz;
            this.dl = s.dl;
            this.x = s.x;
            this.y = s.y;
            this.z = s.z;
            this.px = s.px;
            this.py = s.py;
            this.pz = s.pz;
            this.residual = s.residual;
            this.energyLoss = s.energyLoss;
            this.dx = s.dx;
            this.path = s.path;
            this.covMat = this.copyMatrix(s.covMat);
            this.F = this.copyMatrix(s.F);
        }

        public void copyCovMat(double[][] c) {
            this.covMat = this.copyMatrix(c);
        }
        
        public void copyFMat(double[][] c) {
            this.F = this.copyMatrix(c);
        }
        
        private double[][] copyMatrix(double[][] c) {
            int rows = c.length;
            int cols = c[0].length;
            double[][] cCopy = new double[rows][cols];
            for (int ir = 0; ir < rows; ir++) {
                for (int ic = 0; ic < cols; ic++) {
                    cCopy[ir][ic] = c[ir][ic];
                }
            }
            return cCopy;
        }
        
        public void scaleCovMat(double scale) {
            int rows = this.covMat.length;
            int cols = this.covMat[0].length;
            for (int ir = 0; ir < rows; ir++) {
                for (int ic = 0; ic < cols; ic++) {
                    this.covMat[ir][ic] *= scale;
                }
            }        
        }
        
        public void updateFromHelix() {
            this.x = x0 + this.d_rho * Math.cos(this.phi0) + this.alpha / this.kappa * (Math.cos(this.phi0) - Math.cos(this.phi0 + this.phi));
            this.y = y0 + this.d_rho * Math.sin(this.phi0) + this.alpha / this.kappa * (Math.sin(this.phi0) - Math.sin(this.phi0 + this.phi));
            this.z = z0 + this.dz - this.alpha / this.kappa * this.tanL * this.phi;
            this.px = -Math.sin(this.phi0 + this.phi) / Math.abs(this.kappa);
            this.py = Math.cos(this.phi0 + this.phi) / Math.abs(this.kappa);
            this.pz = this.tanL / Math.abs(this.kappa);
        }

        public final void updateHelix() {
            double kappa = Math.signum(this.kappa) / Math.sqrt(this.px * this.px + this.py * this.py);
            double tanL = Math.abs(kappa) * this.pz;
            double phit = Math.atan2(-this.px, this.py);
            double xcen = this.x + Math.signum(kappa) * this.alpha * this.py;
            double ycen = this.y - Math.signum(kappa) * this.alpha * this.px;
            double phi0 = Math.atan2(ycen - y0, xcen - x0);
            if (Math.signum(kappa) < 0) {
                phi0 = Math.atan2(-(ycen - y0), -(xcen - x0));
            }
            double phi = phit - phi0;
            if (Math.abs(phi) > Math.PI) {
                phi -= 2 * Math.signum(phi) * Math.PI;
            }
            double drho = (xcen - x0) * Math.cos(phi0) + (ycen - y0) * Math.sin(phi0) - this.alpha / kappa;
            double dz = this.z - z0 + this.alpha / kappa * tanL * phi;
            this.d_rho = drho;
            this.phi0 = phi0;
            this.kappa = kappa;
            this.dz = dz;
            this.tanL = tanL;
            this.phi = phi;
        }

        public void rollBack(double angle) {
            this.phi += Math.signum(this.kappa) * angle;
            this.updateFromHelix();
        }

        public void toDoca() {
            this.phi = 0;
            this.updateFromHelix();
        }

        public void pivotTransform() {
            this.setPivot(this.x, this.y, this.z);
        }

        public final void setPivot(double xPivot, double yPivot, double zPivot) {
            x0 = xPivot;
            y0 = yPivot;
            z0 = zPivot;
            this.updateHelix();
        }

        public double[] subtractHelix(StateVec vec) {
            double[] result = new double[5];
            vec.setPivot(this.x0, this.y0, this.z0);
            result[0] = this.d_rho-vec.d_rho;
            result[1] = this.phi0-vec.phi0;
            result[2] = this.kappa-vec.kappa;
            result[3] = this.dz-vec.dz;
            result[4] = this.tanL-vec.tanL;
            return result;
        }
        
        public void updateHelix(double x, double y, double z, double px, double py, double pz, double alpha) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.px = px;
            this.py = py;
            this.pz = pz;
            this.alpha = alpha;
            this.updateHelix();
        }

        public Helix getHelix(double xref, double yref) {
            StateVec vec = new StateVec(0, xref, yref, 0, this);

            int turningSign = (int) Math.signum(vec.kappa) * KFitter.polarity;
            double bfield   = 1 / vec.alpha / lightVel;
            double R        = vec.alpha / Math.abs(vec.kappa);
            double phi0     = vec.phi0 + Math.PI / 2;
            if (Math.abs(phi0) > Math.PI) phi0 -= Math.signum(phi0) * 2 * Math.PI;
            double tanDip   = vec.tanL;
            double z0       = vec.z0 + vec.dz;
            double omega    = -turningSign / R;
            double d0       = -vec.d_rho;
            
            Helix helix = new Helix(d0, phi0, omega, z0, tanDip, turningSign, bfield, xref, yref, units);
            
            return helix;
        }
        
        public void updateRay() {
            this.dl = 0;
            this.tx = this.px/this.py;
            this.tz = this.pz/this.py;
            this.x0 = this.x -this.dl*this.tx;
            this.y0 = this.y -this.dl;
            this.z0 = this.z -this.dl*this.tz;
        }
        
        public void updateFromRay() {
            this.py = 1/Math.sqrt(1+tx*tx+tz*tz);
            this.px = tx*py;
            this.pz = tz*py;
            this.x = this.x0 + this.dl*this.tx;
            this.y = this.y0 + this.dl;
            this.z = this.z0 + this.dl*this.tz;
        }

        public double[] getHelixArray() {
            double[] a = new double[5];
            for(int i=0; i<5; i++)
                a[i] = this.getHelixComponent(i);
            return a;
        }
        
        public double[] getRayArray() {
            double[] a = new double[5];
            for(int i=0; i<5; i++)
                a[i] = this.getRayComponent(i);
            return a;
        }
        
        public double getHelixComponent(int i) {
            switch (i) {
                case 0:
                    return d_rho;
                case 1:
                    return phi0;
                case 2:
                    return kappa;
                case 3:
                    return dz;
                case 4:
                    return tanL;
                default:
                    return 0;
            }
        }

        public double getRayComponent(int i) {
            switch (i) {
                case 0:
                    return x0;
                case 1:
                    return z0;
                case 2:
                    return tx;
                case 3:
                    return tz;
                default:
                    return 0;
            }
        }

        public Point3D getPosition() {
            return new Point3D(this.x, this.y, this.z);
        }

        public Vector3D getMomentum() {
            return new Vector3D(this.px, this.py, this.pz);
        }
        
        @Override
        public String toString() {
            String s = String.format("%d) drho=%.4f phi0=%.4f kappa=%.4f dz=%.4f tanL=%.4f alpha=%.4f\n", this.k, this.d_rho, this.phi0, this.kappa, this.dz, this.tanL, this.alpha);
            s += String.format("    phi=%.4f x=%.4f y=%.4f z=%.4f px=%.4f py=%.4f pz=%.4f", this.phi, this.x, this.y, this.z, this.px, this.py, this.pz);
            return s;
        }

    }

    public class B {

        public final int k;
        public double x;
        public double y;
        public double z;
        public Swim swimmer;

        public double Bx;
        public double By;
        public double Bz;

        public double alpha;

        float b[] = new float[3];

        public B(int k) {
            this.k = k;
        }

        public B(int k, double x, double y, double z, Swim swimmer) {
            this.k = k;
            this.x = x;
            this.y = y;
            this.z = z;

            swimmer.BfieldLab(x / units.value(), y / units.value(), z / units.value(), b);
            this.Bx = b[0];
            this.By = b[1];
            this.Bz = b[2];

            this.alpha = 1. / (lightVel * Math.abs(b[2]));
        }

        public void set() {
            swimmer.BfieldLab(x / units.value(), y / units.value(), z / units.value(), b);
            this.Bx = b[0];
            this.By = b[1];
            this.Bz = b[2];

            this.alpha = 1. / (lightVel * Math.abs(b[2]));
        }
    }

    public abstract void printlnStateVec(StateVec S);
}
