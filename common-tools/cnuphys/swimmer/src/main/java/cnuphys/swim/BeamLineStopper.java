package cnuphys.swim;

import cnuphys.rk4.IStopper;

public class BeamLineStopper implements IStopper {

        private double _finalPathLength = Double.NaN;

        private double _xB;
        private double _yB;
        
        
        double min = Double.POSITIVE_INFINITY;
        public BeamLineStopper(double xB, double yB) {
                // DC reconstruction units are cm. Swim units are m. Hence scale by
                // 100
                _xB = xB;
                _yB = yB;
        }

        @Override
        public boolean stopIntegration(double t, double[] y) {

                double r = Math.sqrt((_xB-y[0]* 100.) * (_xB-y[0]* 100.) + (_yB-y[1]* 100.) * (_yB-y[1]* 100.));
                if(r<min && y[2]<2.0) //start at about 2 meters before target.  Avoid inbending stopping when P dir changes
                    min = r;
                return (r > min );

        }

        /**
         * Get the final path length in meters
         *
         * @return the final path length in meters
         */
        @Override
        public double getFinalT() {
                return _finalPathLength;
        }

        /**
         * Set the final path length in meters
         *
         * @param finalPathLength
         *            the final path length in meters
         */
        @Override
        public void setFinalT(double finalPathLength) {
                _finalPathLength = finalPathLength;
        }
        
        
        //test vals
        // distBetweenSaves = .0005 m

	/**
	 * 
	 * @param charge
	 * @param charge           the charge: -1 for electron, 1 for proton, etc
	 * @param xo               the x vertex position in meters
	 * @param yo               the y vertex position in meters
	 * @param zo               the z vertex position in meters
	 * @param pTot             momentum in GeV/c
	 * @param theta            initial polar angle in degrees
	 * @param phi              initial azimuthal angle in degrees
	 * @param maxS             max path length in meters. This determines the max
	 *                         number of steps based on the step size. If a stopper
	 *                         is used, the integration might terminate before all
	 *                         the steps are taken. A reasonable value for CLAS is
	 *                         8. meters
	 * @param stepSize         the uniform step size in meters.
	 * @param distBetweenSaves this distance is in meters. It should be bigger than
	 *                         stepSize. It is approximately the distance between
	 *                         "saves" where the point is saved in a trajectory for
	 *                         later drawing.
	 * @param xB               max x distance to be considered on beam line (m)
	 * @param yB               max y distance to be considered on beam line (m)
	 * @return                 final state vector
	 */
	public static double[] SwimToBeamLine(int charge, double xo, double yo, double zo, double pTot, double theta,
			double phi, double maxS, double stepSize, double distBetweenSaves, double xB, double yB) {

		double[] value = new double[8];

		// use the current active probe
            Swimmer swimmer = new Swimmer();
    
            BeamLineStopper stopper = new BeamLineStopper(xB, yB);

            SwimTrajectory st = swimmer.swim(charge, xo, yo, zo, pTot, theta, phi, stopper, maxS, stepSize,
                            0.0005);
            if(st==null) {
                return null;
            }
            
            st.computeBDL(swimmer.getProbe());
            // st.computeBDL(compositeField);

            double[] lastY = st.lastElement();

            return lastY;

        }
}
