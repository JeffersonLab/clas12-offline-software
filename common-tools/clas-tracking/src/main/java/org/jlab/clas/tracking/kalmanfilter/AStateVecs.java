package org.jlab.clas.tracking.kalmanfilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jlab.geom.prim.Vector3D;

import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.tracking.kalmanfilter.AMeasVecs.MeasVec;
import org.jlab.clas.tracking.trackrep.Helix;

public abstract class AStateVecs {

    public List<Double> X0;
    public List<Double> Y0;
    public List<Double> Z0; // reference points
   
    public Helix util ;
    public double units;
    public double lightVel;
    
    public List<B> bfieldPoints = new ArrayList<B>();
    public Map<Integer, StateVec> trackTraj = new HashMap<Integer, StateVec>();
    public Map<Integer, CovMat> trackCov = new HashMap<Integer, CovMat>();

    public boolean straight;
    public StateVec StateVec;
    public CovMat CovMat;
    
    AMeasVecs mv = new AMeasVecs() {
        @Override
        public double[] H(StateVec stateVec, AStateVecs sv, MeasVec mv, Swim swimmer, int dir) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public StateVec reset(StateVec SVplus, StateVec stateVec, AStateVecs sv) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    } ;

    public double shift; // target shift
    public List<Integer> Layer;
    public List<Integer> Sector;

    public double[] value = new double[4]; // x,y,z,phi
    public double[] swimPars = new double[7];
    public B Bf = new B(0);

    public abstract void init(Helix trk, double[][] cov, AKFitter kf, Swim swimmer);
    public abstract void init(double x0, double z0, double tx, double tz, double units, double[][] cov, AKFitter kf) ;
    public abstract void setStateVecPosAtMeasSite(int k, StateVec kVec, MeasVec mv, Swim swimmer) ;
    public abstract StateVec newStateVecAtMeasSite(int k, StateVec kVec, MeasVec mv, Swim swimmer, 
            boolean useSwimmer);
    public abstract double[] getStateVecPosAtMeasSite(int k, StateVec iVec, MeasVec mv, Swim swim, 
            boolean useSwimmer) ;
    public abstract void tranState(int f, StateVec iVec, Swim swimmer) ;

    public abstract StateVec transported(int i, int f, StateVec iVec, MeasVec mv,
            Swim swimmer) ;
    
    public abstract void transport(int i, int f, StateVec iVec, CovMat icovMat, MeasVec mv,
            Swim swimmer) ;

    public abstract void setTrackPars(StateVec kVec, Swim swim);
    
    public abstract Helix setTrackPars();
    
    private double[][] multiplyMatrices(double[][] firstMatrix, double[][] secondMatrix) {
        int r1 =firstMatrix.length; int c1=firstMatrix[0].length; int c2=secondMatrix[0].length;
        double[][] product = new double[r1][c2];
        for(int i = 0; i < r1; i++) {
            for (int j = 0; j < c2; j++) {
                for (int k = 0; k < c1; k++) {
                    product[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
                }
            }
        }

        return product;
    }
    
    public double[][] propagatedMatrix(double[][] firstMatrix, double[][] sMatrix, double[][] secondMatrix){
        double[][] m1 = multiplyMatrices(sMatrix, secondMatrix);
        double[][] m2 = multiplyMatrices(firstMatrix,m1);
        
        return m2;
    }
    
    public double[][] addProcessNoise(double[][] C, double[][] Q){
        double[][] result = new double[5][5];
        for(int i = 0; i<5; i++) {
            for(int j = 0; j<5; j++) {
                result[i][j] = C[i][j];
                if(Q[i][j]!=0) {
                    result[i][j] +=Q[i][j];
                }
            }
        }
          
        return result;
    }
    public abstract double[][] Q(StateVec iVec, MeasVec mVec, int dir) ;
    
    public abstract StateVec reset(StateVec SV, StateVec stateVec) ;

    public void resetArrays(double[] swimPars) {
        for(int i = 0; i<swimPars.length; i++) {
            swimPars[i] = 0;
        }
    }

    
    
    public class StateVec {

        public final int k;

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
        
        //Cosmics
        public double x0;
        public double z0;
        //public double x;
        //public double y;
        //public double z;
        public double tx; //=px/py
        public double tz; //=pz/py
        public double dl;
        public double resi =0;
        
        public StateVec(int k) {
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

        public final int k;
        public double[][] covMat;

        public CovMat(int k) {
            this.k = k;
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
    public double piMass = 0.13957018;
    public double KMass = 0.493677;
    public double muMass = 0.105658369;
    public double eMass = 0.000510998;
    public double pMass = 0.938272029;
    

    public abstract Vector3D P(int kf) ;
    public abstract Vector3D X(int kf) ;
    public abstract Vector3D X(StateVec kVec, double phi) ;
    public abstract Vector3D P0(int kf) ;
    public abstract Vector3D X0(int kf) ;

    
    public abstract void printlnStateVec(StateVec S) ;
}