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
     * @param x0 track x parameter
     * @param y0 track y parameter
     * @param tanTheta_x track ux/uz (u: unit direction vector along the track trajectory at x,y, z[fixed]) 
     * @param tanTheta_y track uy/uz
     * @param p track momentum
     * @param q track change
     * @param DcDetector DC detector utility
     * @return list of state vecs along track trajectory ... used for tFlight computation
     */
//    public List<StateVec> getStateVecsAlongTrajectory(double x0, double y0, double z0, double tanTheta_x, double tanTheta_y, double p, int q, DCGeant4Factory DcDetector) {              
//        //initialize at target
//        dcSwim.SetSwimParameters(x0, y0, z0, tanTheta_x,  tanTheta_y,  p,  q);
//        //position array 
//        double[] X = new double[36];
//        double[] Y = new double[36];
//        double[] Z = new double[36];
//        double[] thX = new double[36];
//        double[] thY = new double[36];
//
//        //Z[0] = GeometryLoader.dcDetector.getSector(0).getSuperlayer(0).getLayer(0).getPlane().point().z();
//        Z[0] = DcDetector.getLayerMidpoint(0, 0).z; 
//        double[] swamPars = dcSwim.SwimToPlane(Z[0]) ;
//        X[0] = swamPars[0];
//        Y[0] = swamPars[1];
//        thX[0] = swamPars[3]/swamPars[5];
//        thY[0] = swamPars[4]/swamPars[5];
//        double pathLen = swamPars[6];
//        int planeIdx = 0;
//        int lastSupLyrIdx = 0;
//        int lastLyrIdx = 0;
//        List<StateVec> stateVecAtPlanesList = new ArrayList<StateVec>(36);
//
//        stateVecAtPlanesList.add(new StateVec(X[0],Y[0],thX[0], thY[0]));
//        stateVecAtPlanesList.get(stateVecAtPlanesList.size()-1).setPathLength(pathLen);
//        for(int superlayerIdx =0; superlayerIdx<6; superlayerIdx++) {
//            for(int layerIdx =0; layerIdx<6; layerIdx++) {
//                if(superlayerIdx ==0 && layerIdx==0) {    
//                    continue;
//                } else {
//                    // move to the next plane and determine the swam track parameters at that plane
//                    planeIdx++;
//                    dcSwim.SetSwimParameters(X[planeIdx-1],  Y[planeIdx-1], Z[planeIdx-1], thX[planeIdx-1], thY[planeIdx-1],  p,  q);
//                    //Z[layerIdx] = GeometryLoader.dcDetector.getSector(0).getSuperlayer(superlayerIdx).getLayer(layerIdx).getPlane().point().z();
//                    Z[planeIdx] = DcDetector.getLayerMidpoint(superlayerIdx, layerIdx).z; 
//
//                    swamPars = dcSwim.SwimToPlane(Z[planeIdx]) ;
//                    X[planeIdx] = swamPars[0];
//                    Y[planeIdx] = swamPars[1];
//                    thX[planeIdx] = swamPars[3]/swamPars[5];
//                    thY[planeIdx] = swamPars[4]/swamPars[5];
//                    pathLen+=swamPars[6];
//                    StateVec stVec = new StateVec(X[planeIdx],Y[planeIdx],thX[planeIdx], thY[planeIdx]);
//                    stVec.set_planeIdx(planeIdx);
//                    stVec.setPathLength(pathLen);
//                    stateVecAtPlanesList.add(stVec);
//                }
//                lastSupLyrIdx = superlayerIdx;
//                lastLyrIdx = layerIdx;
//            }
//        }
//        // return the list of state vectors at the list of measurement planes
//        return stateVecAtPlanesList;
//    }
//        
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

        for (int i =0; i<3; i++) {
            // make sure that the track direction makes sense
            if(candCrossList.get(i).get_Dir().z()==0) {
                return;
            }

            x[i] = candCrossList.get(i).get_Point().x();
            y[i] = candCrossList.get(i).get_Point().y();
            z[i] = candCrossList.get(i).get_Point().z();

            theta_x[i] = candCrossList.get(i).get_Dir().x()/candCrossList.get(i).get_Dir().z();
            theta_x_err[i] = calcTanErr(candCrossList.get(i).get_Dir().x(),candCrossList.get(i).get_Dir().z(),candCrossList.get(i).get_DirErr().x(),candCrossList.get(i).get_DirErr().z());            
        }
        
        lineFit = new LineFitter();
        boolean linefitstatusOK = lineFit.fitStatus(z, theta_x, new double[3], theta_x_err, 3);
        TrajChisqProbFitXZ = lineFit.getFit().getProb(); 
        
        
        double[][] array = {{z[0]*z[0], z[0], 1}, {z[1]*z[1], z[1], 1}, {z[2]*z[2], z[2], 1}};
        double[][] x_array0 = {{x[0], z[0], 1}, {x[1], z[1], 1}, {x[2], z[2], 1}};
        double[][] x_array1 = {{z[0]*z[0], x[0], 1}, {z[1]*z[1], x[1], 1}, {z[2]*z[2], x[2], 1}};
        double[][] x_array2 = {{z[0]*z[0], z[0], x[0]}, {z[1]*z[1], z[1], x[1]}, {z[2]*z[2], z[2], x[2]}};        
        double[][] y_array0 = {{y[0], z[0], 1}, {y[1], z[1], 1}, {y[2], z[2], 1}};
        double[][] y_array1 = {{z[0]*z[0], y[0], 1}, {z[1]*z[1], y[1], 1}, {z[2]*z[2], y[2], 1}};
        double[][] y_array2 = {{z[0]*z[0], z[0], y[0]}, {z[1]*z[1], z[1], y[1]}, {z[2]*z[2], z[2], y[2]}};
        
        
        Matrix D = new Matrix(array);
        Matrix x_D0 = new Matrix(x_array0);
        Matrix x_D1 = new Matrix(x_array1);     
        Matrix x_D2 = new Matrix(x_array2);
        Matrix y_D0 = new Matrix(y_array0);
        Matrix y_D1 = new Matrix(y_array1);     
        Matrix y_D2 = new Matrix(y_array2);
        
        x_fitCoeff[0] = x_D0.det()/D.det();
        x_fitCoeff[1] = x_D1.det()/D.det();
        x_fitCoeff[2] = x_D2.det()/D.det();
        
        y_fitCoeff[0] = y_D0.det()/D.det();
        y_fitCoeff[1] = y_D1.det()/D.det();
        y_fitCoeff[2] = y_D2.det()/D.det();                       
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
