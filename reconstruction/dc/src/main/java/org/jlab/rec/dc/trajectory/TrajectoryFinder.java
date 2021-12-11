package org.jlab.rec.dc.trajectory;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.cross.Cross;
import trackfitter.fitter.LineFitter;

/**
 * A driver class to find the trajectory of a track candidate.  NOTE THAT THE PATH TO FIELD MAPS IS SET BY THE CLARA_SERVICES ENVIRONMENT VAR.
 *
 * @author ziegler
 *
 */
public class TrajectoryFinder {

	private LineFitter lineFit;

	private double[] tanTheta_x_fitCoeff;
	private double[] tanTheta_y_fitCoeff;
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
        public double TrajChisqProbFitYZ;

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
        traj.set_Sector(candCrossList.get(0).get_Sector());
        fitTrajectory(traj);
        if (this.TrajChisqProbFitXZ<Constants.TCHISQPROBFITXZ) {
            return null;
        }
        traj.set_Trajectory(getStateVecsAlongTrajectory(DcDetector));
        traj.set_IntegralBdl(integralBdl(candCrossList.get(0).get_Sector(), DcDetector, dcSwim));
        traj.set_PathLength(PathLength);
        
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


        while(z<=z3) {
            counter++;
            double x = x_fitCoeff[0]*z*z+x_fitCoeff[1]*z+x_fitCoeff[2];
            double y = y_fitCoeff[0]*z*z+y_fitCoeff[1]*z+y_fitCoeff[2];
 
            float[] result = new float[3];
            dcSwim.Bfield(sector, (x + x0) * 0.5, (y + y0) * 0.5, (z + z0) * 0.5, result);
            Vector3D dl = new Vector3D(x-x0,0,z-z0);
            Vector3D Bf = new Vector3D(result[0], result[1], result[2]);
            intBdl+= dl.cross(Bf).mag();
            pathLen+= dl.mag();
            x0 = x;
            y0 = y;
            z0 = z;

            z+=mmStepSizeForIntBdl/10.;
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
        tanTheta_x_fitCoeff = new double[2];
        tanTheta_y_fitCoeff = new double[2];
        x_fitCoeff = new double[3];
        y_fitCoeff = new double[3];


        double[] theta_x = new double[3];
        double[] theta_x_err = new double[3];
        double[] theta_y = new double[3];
        double[] theta_y_err = new double[3];

        double[] x = new double[3];
        double[] x_err = new double[3];
        double[] y = new double[3];
        double[] y_err = new double[3];
        double[] z = new double[3];

        for (int i =0; i<3; i++) {
            // make sure that the track direction makes sense
            if(candCrossList.get(i).get_Dir().z()==0) {
                return;
            }

            x[i] = candCrossList.get(i).get_Point().x();
            x_err[i] = candCrossList.get(i).get_PointErr().x();
            y[i] = candCrossList.get(i).get_Point().y();
            y_err[i] = candCrossList.get(i).get_PointErr().y();
            z[i] = candCrossList.get(i).get_Point().z();

            theta_x[i] = candCrossList.get(i).get_Dir().x()/candCrossList.get(i).get_Dir().z();
            theta_x_err[i] = calcTanErr(candCrossList.get(i).get_Dir().x(),candCrossList.get(i).get_Dir().z(),candCrossList.get(i).get_DirErr().x(),candCrossList.get(i).get_DirErr().z());
            theta_y[i] = candCrossList.get(i).get_Dir().y()/candCrossList.get(i).get_Dir().z();
            theta_y_err[i] = calcTanErr(candCrossList.get(i).get_Dir().y(),candCrossList.get(i).get_Dir().z(),candCrossList.get(i).get_DirErr().y(),candCrossList.get(i).get_DirErr().z());
        }

        lineFit = new LineFitter();
        boolean linefitstatusOK = lineFit.fitStatus(z, theta_x, new double[3], theta_x_err, 3);

        // tan_thetax = alpha*z + beta;
        // x = a*z^2 +b*z +c
        if (linefitstatusOK) {
            double alpha = lineFit.getFit().slope();
            double beta = lineFit.getFit().intercept();


            double a = alpha/2;
            double b = beta;

            double sum_inv_xerr = 0;
            double sum_X_ov_errX = 0;
            for (int i =0; i<3; i++) {
                x[i]-=a*z[i]*z[i]+b*z[i];
                sum_inv_xerr += 1./x_err[i];
                sum_X_ov_errX += x[i]/x_err[i];
            }
            if(sum_inv_xerr==0) {
                return;
            }
            double c = sum_X_ov_errX/sum_inv_xerr;

            tanTheta_x_fitCoeff[0] = alpha;
            tanTheta_x_fitCoeff[1] = beta;

            x_fitCoeff[0] = a;
            x_fitCoeff[1] = b;
            x_fitCoeff[2] = c;
        }
        TrajChisqProbFitXZ = lineFit.getFit().getProb();

        lineFit = new LineFitter();
        linefitstatusOK = lineFit.fitStatus(z, theta_y, new double[3], theta_y_err, 3);

        // tan_thetay = alpha*z + beta;
        // y = a*z^2 +b*z +c
        if (linefitstatusOK) {
            double alpha = lineFit.getFit().slope();
            double beta = lineFit.getFit().intercept();

            double a = alpha/2;
            double b = beta;

            double sum_inv_yerr = 0;
            double sum_Y_ov_errY = 0;
            for (int i =0; i<3; i++) {
                    y[i]-=a*z[i]*z[i]+b*z[i];
                    sum_inv_yerr += 1./y_err[i];
                    sum_Y_ov_errY += y[i]/y_err[i];
            }
            if(sum_inv_yerr==0) {
                    return;
            }
            double c = sum_Y_ov_errY/sum_inv_yerr;

            tanTheta_y_fitCoeff[0] = alpha;
            tanTheta_y_fitCoeff[1] = beta;

            y_fitCoeff[0] = a;
            y_fitCoeff[1] = b;
            y_fitCoeff[2] = c;
        }
        TrajChisqProbFitYZ = lineFit.getFit().getProb();
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
