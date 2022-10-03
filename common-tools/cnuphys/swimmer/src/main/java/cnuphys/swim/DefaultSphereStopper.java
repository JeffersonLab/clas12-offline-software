package cnuphys.swim;

import cnuphys.rk4.IStopper;

public class DefaultSphereStopper implements IStopper {
	
    private double _finalPathLength = Double.NaN;

    private double _Rad;
    private int _dir ;

    /**
     * A swim stopper that will stop if the boundary of a plane is crossed
     *
     * @param maxR
     *            the max radial coordinate in meters.
     */
    public DefaultSphereStopper(double Rad, int dir) {
             _Rad = Rad;
             _dir = dir;
    }

    @Override
    public boolean stopIntegration(double t, double[] y) {

        double r = Math.sqrt(y[0] * y[0] + y[1] * y[1] + y[2] * y[2]);
        if(_dir>0) { //starting inside
        return (r > _Rad); //crossed to outside
        } else { //starting outside
            return (r < _Rad); //crossed to inside
        }

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
}
