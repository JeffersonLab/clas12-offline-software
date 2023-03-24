package org.jlab.rec.dc.trajectory;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.cross.Cross;
import trackfitter.fitter.LineFitter;
import Jama.Matrix;

/**
 * A driver class to find the trajectory of a track candidate.  NOTE THAT THE PATH TO FIELD MAPS IS SET BY THE CLARA_SERVICES ENVIRONMENT VAR.
 *
 * @author ziegler
 *
 */
public class TrajectoryFinder {

	private LineFitter lineFit;

	private double[] x_fitCoeff;
	private double[] y_fitCoeff;

	/**
	 *  Field instantiated using the torus and the solenoid
	*/
	
	/**
	 * Step size used in integral Bdl Riemann integration
	 */
	public double mmStepSizeForIntBdl = 10;

	private double PathLength;
                
	/**
	 *
	 * @return the value of the integral of the magnetic field over the path traveled by the particle as estimated from the fits to the crosses.
	 */
	int counter =0;
        
        public double TrajChisqProbFitXZ;
    /**
     *
     * @param candCrossList the input list of crosses used in determining a trajectory
     * @param DcDetector
     * @param dcSwim
     * @return a trajectory object
     */
    public Trajectory findTrajectory(List<Cross> candCrossList, DCGeant4Factory DcDetector, Swim dcSwim) {
        Trajectory traj = new Trajectory();
        if (candCrossList.isEmpty()) {
            return traj;
        }
        traj.addAll(candCrossList);
        traj.setSector(candCrossList.get(0).get_Sector());
        fitTrajectory(traj);
        if (this.TrajChisqProbFitXZ<Constants.TCHISQPROBFITXZ) {            
            return null;
        }
        traj.setStateVecs(getStateVecsAlongTrajectory(DcDetector));
        traj.setIntegralBdl(integralBdl(candCrossList.get(0).get_Sector(), DcDetector, dcSwim));
        traj.setPathLength(PathLength);
        traj.setA(x_fitCoeff[0]);
        
        return traj;
    }
    
    /**
     * 
     * @param sector
     * @param DcDetector DC detector utility
     * @param dcSwim
     * @return integral Bdl
     */
    public double integralBdl(int sector, DCGeant4Factory DcDetector, Swim dcSwim) {

        double z1 = DcDetector.getRegionMidpoint(0).z;
        double z3 = DcDetector.getRegionMidpoint(2).z;

        double z = z1;

        double intBdl = 0;
        double pathLen =0;
        double x0 = x_fitCoeff[0]*z1*z1+x_fitCoeff[1]*z1+x_fitCoeff[2];
        double y0 = y_fitCoeff[0]*z1*z1+y_fitCoeff[1]*z1+y_fitCoeff[2];
        double z0 = z1;


        while(z<z3) {
            counter++;
            
            if(z3 - z < mmStepSizeForIntBdl/10)
                z+=mmStepSizeForIntBdl/10.;
            else 
                z = z3;
            
            double x = x_fitCoeff[0]*z*z+x_fitCoeff[1]*z+x_fitCoeff[2];
            double y = y_fitCoeff[0]*z*z+y_fitCoeff[1]*z+y_fitCoeff[2];
 
            float[] result = new float[3];
            dcSwim.Bfield(sector, (x + x0) * 0.5, (y + y0) * 0.5, (z + z0) * 0.5, result);
            Vector3D dl = new Vector3D(x-x0,y - y0,z-z0);
            Vector3D Bf = new Vector3D(result[0], result[1], result[2]);
            Vector3D Bdl = dl.cross(Bf);
            intBdl+= (new Vector3D(Bdl.x(), 0, Bdl.z())).mag();
            pathLen+= dl.mag();
            x0 = x;
            y0 = y;
            z0 = z;
        }
        PathLength = pathLen;

        return intBdl;
    }
 
    /**
     *
     * @param DcDetector
     * @return the list of state vectors along the trajectory
     */
    public List<StateVec> getStateVecsAlongTrajectory(DCGeant4Factory DcDetector) {
        List<StateVec> stateVecAtPlanesList = new ArrayList<>(36);
        for(int superlayerIdx =0; superlayerIdx<6; superlayerIdx++) {
            for(int layerIdx =0; layerIdx<6; layerIdx++) {
                double z = DcDetector.getLayerMidpoint(superlayerIdx, layerIdx).z;
                double x = x_fitCoeff[0]*z*z+x_fitCoeff[1]*z+x_fitCoeff[2];
                double y = y_fitCoeff[0]*z*z+y_fitCoeff[1]*z+y_fitCoeff[2];
                double tanTheta_x = x_fitCoeff[0]*z+x_fitCoeff[1];
                double tanTheta_y = y_fitCoeff[0]*z+y_fitCoeff[1];

                StateVec stateVec = new StateVec(x,y,tanTheta_x, tanTheta_y);
                stateVecAtPlanesList.add(stateVec);
            }
        }
        return stateVecAtPlanesList;
    }

	
    /**
     * The parametric form of the trajectory determined from fitting the tangent values of the state vecs linearly,
     * and constraining the quadratic parameters of the function describing the position values of the state vecs.
     * @param candCrossList list of crosses used in the fit
     */
    public void fitTrajectory(List<Cross> candCrossList) {             
        x_fitCoeff = new double[3];
        y_fitCoeff = new double[3];
        
        double[] theta_x = new double[3];
        double[] theta_x_err = new double[3];

        double[] x = new double[3];
        double[] y = new double[3];
        double[] z = new double[3];

        double[] x_err = new double[3];
        double[] y_err = new double[3];

        for (int i = 0; i < 3; i++) {
            // make sure that the track direction makes sense
            if (candCrossList.get(i).get_Dir().z() == 0) {
                return;
            }

            x[i] = candCrossList.get(i).get_Point().x();
            y[i] = candCrossList.get(i).get_Point().y();
            z[i] = candCrossList.get(i).get_Point().z();

            x_err[i] = candCrossList.get(i).get_PointErr().x();
            y_err[i] = candCrossList.get(i).get_PointErr().x();

            theta_x[i] = candCrossList.get(i).get_Dir().x() / candCrossList.get(i).get_Dir().z();
            theta_x_err[i] = calcTanErr(candCrossList.get(i).get_Dir().x(), candCrossList.get(i).get_Dir().z(), candCrossList.get(i).get_DirErr().x(), candCrossList.get(i).get_DirErr().z());
        }

        lineFit = new LineFitter();
        boolean linefitstatusOK = lineFit.fitStatus(z, theta_x, new double[3], theta_x_err, 3);
        TrajChisqProbFitXZ = lineFit.getFit().getProb();

        x_fitCoeff = quadraticLRFit(z, x, x_err);
        y_fitCoeff = quadraticLRFit(z, y, y_err);                  
    }
    
    private double[] quadraticLRFit(double[] x, double[] y, double[] err) {
        double[] ret = {0., 0., 0.};

        Matrix A = new Matrix(3, 3);
        Matrix V = new Matrix(3, 1);
        double sum1 = 0.0;
        double sum2 = 0.0;
        double sum3 = 0.0;
        double sum4 = 0.0;
        double sum5 = 0.0;
        double sum6 = 0.0;
        double sum7 = 0.0;
        double sum8 = 0.0;
        for (int i = 0; i < x.length; ++i) {
            double y1 = y[i];
            double x1 = x[i];
            double x2 = x1 * x1;
            double x3 = x2 * x1;
            double x4 = x2 * x2;
            double e2 = err[i] * err[i];
            sum1 += x4 / e2;
            sum2 += x3 / e2;
            sum3 += x2 / e2;
            sum4 += x1 / e2;
            sum5 += 1.0 / e2;
            sum6 += y1 * x2 / e2;
            sum7 += y1 * x1 / e2;
            sum8 += y1 / e2;
        }
        A.set(0, 0, sum1);
        A.set(0, 1, sum2);
        A.set(0, 2, sum3);
        A.set(1, 0, sum2);
        A.set(1, 1, sum3);
        A.set(1, 2, sum4);
        A.set(2, 0, sum3);
        A.set(2, 1, sum4);
        A.set(2, 2, sum5);
        V.set(0, 0, sum6);
        V.set(1, 0, sum7);
        V.set(2, 0, sum8);
        Matrix Ainv = A.inverse();
        Matrix X;
        try {
            X = Ainv.times(V);
            for (int i = 0; i < 3; ++i) {
                ret[i] = X.get(i, 0);
            }

        } catch (ArithmeticException e) {
            // TODO Auto-generated catch block
        }
        return (ret);
    }
    
    /**
     *
     * @param num
     * @param denom
     * @param numEr
     * @param denomEr
     * @return the error on the tangent num/denom.
     */
    private double calcTanErr(double num, double denom, double numEr, double denomEr) {
        double d1 = num/(denom*denom);
        double e1 = denomEr;
        double d2 = 1/denom;
        double e2 = numEr;

        return Math.sqrt(d1*d1+e1*e1 + d2*d2*e2*e2);
    }

}
