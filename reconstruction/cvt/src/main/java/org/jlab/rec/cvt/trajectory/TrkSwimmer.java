package org.jlab.rec.cvt.trajectory;

import java.io.File;
import java.io.FileNotFoundException;

import org.jlab.geom.prim.Point3D;

import cnuphys.magfield.Solenoid;
import cnuphys.rk4.IStopper;
import cnuphys.rk4.RungeKuttaException;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.Swimmer;

import org.jlab.utils.CLASResources;

/**
 * This class is used to swim a track in the forward tracker
 *
 * @author ziegler
 *
 */
public class TrkSwimmer {

    private static Solenoid sField;
    private static Swimmer swimmer;
    // get some fit results

    private double _x0;
    private double _y0;
    private double _z0;
    private double _phi;
    private double _theta;
    private double _pTot;
    private double _maxPathLength;
    private int _charge;

    public double swamPathLength;
    public double swamIBdl;

    public int nSteps;

    public TrkSwimmer() {
        //create a swimmer for our magnetic field
        //swimmer = new Swimmer(rcompositeField);
        // create a swimmer for the torus field
        //if(areFieldsLoaded==false)
        //   getMagneticFields();
        swimmer = new Swimmer(sField);
    }

    /**
     * Sets the parameters used by swimmer based on the input track parameters
     *
     * @param x0
     * @param y0
     * @param z0
     * @param px
     * @param py
     * @param pz
     * @param maxPathLength
     * @param charge
     */
    public void SetSwimParameters(double x0, double y0, double z0, double px, double py, double pz, double maxPathLength, int charge) {
        _x0 = x0 / 1000;
        _y0 = y0 / 1000;
        _z0 = z0 / 1000;
        _phi = Math.toDegrees(Math.atan2(py, px));
        _pTot = Math.sqrt(px * px + py * py + pz * pz);
        _theta = Math.toDegrees(Math.acos(pz / _pTot));

        _maxPathLength = maxPathLength;
        _charge = charge;
    }

    public void SetSwimParameters(Helix helix,
            double maxPathLength, int charge, double p) {

        _maxPathLength = maxPathLength;
        _charge = charge;
        _phi = Math.toDegrees(helix.get_phi_at_dca());
        _theta = Math.toDegrees(Math.acos(helix.costheta())); 
        _pTot = p;
        _x0 = helix.xdca() / 1000;
        _y0 = helix.ydca() / 1000;
        _z0 = helix.get_Z0() / 1000;

    }

    private class CylinderBoundarySwimStopper implements IStopper {

        private double _cylRad;

        /**
         * A swim stopper that will stop if the boundary of a plane is crossed
         *
         * @param maxR the max radial coordinate in meters.
         */
        private CylinderBoundarySwimStopper(double cylRad) {
            // BST reconstruction units are mm.  Swimmer units are m.  Hence scale by 1000
            _cylRad = cylRad;
        }

        @Override
        public boolean stopIntegration(double t, double[] y) {

            double r = Math.sqrt(y[0] * y[0] + y[1] * y[1]) * 1000.;

            return (r > _cylRad);

        }

        @Override
        public double getFinalT() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setFinalT(double arg0) {
            // TODO Auto-generated method stub

        }
    }

    public double[] SwimToPlane(double z_mm) {
        double hdata[] = new double[3];
        double z = z_mm / 1000; // the magfield method uses meters
        double[] value = new double[8];
        double accuracy = 2.0e-6; //2.0 microns
        double stepSize = 1e-6; // m
        if (_pTot < 0.05) // fiducial cut 50 MeV
        {
            return null;
        }

        try {
            SwimTrajectory traj = swimmer.swim(_charge, _x0, _y0, _z0, _pTot,
                    _theta, _phi, z, accuracy, _maxPathLength,
                    _maxPathLength, stepSize, Swimmer.CLAS_Tolerance, hdata);

            traj.computeBDL(sField);
            double[] lastY = traj.lastElement();

            value[0] = lastY[0] * 1000.; // convert back to mm
            value[1] = lastY[1] * 1000.; // convert back to mm
            value[2] = lastY[2] * 1000.; // convert back to mm
            value[3] = lastY[3] * _pTot; //normalized values * p
            value[4] = lastY[4] * _pTot;
            value[5] = lastY[5] * _pTot;

            swamPathLength = lastY[6] * 1000;
            swamIBdl = lastY[7] * 100; //Conversion from kG.m to T.mm :*100
        } catch (RungeKuttaException e) {
            e.printStackTrace();
        }
        return value;

    }

    public double[] SwimToCylinder(double cylRad) {

        double[] value = new double[6];
        // using adaptive stepsize

        CylinderBoundarySwimStopper stopper = new CylinderBoundarySwimStopper(cylRad);

        // step size in m
        double stepSize = 1e-4; // m

        SwimTrajectory st = swimmer.swim(_charge, _x0, _y0, _z0, _pTot, _theta, _phi, stopper, _maxPathLength, stepSize, 0.0005);
        st.computeBDL(sField);
        double[] lastY = st.lastElement();

        value[0] = lastY[0] * 1000; // convert back to mm
        value[1] = lastY[1] * 1000; // convert back to mm
        value[2] = lastY[2] * 1000; // convert back to mm
        value[3] = lastY[3] * _pTot; //normalized values
        value[4] = lastY[4] * _pTot;
        value[5] = lastY[5] * _pTot;

        swamPathLength = lastY[6] * 1000;
        swamIBdl = lastY[7] * 100; //Conversion from kG.m to T.mm :*100

        return value;

    }

    /**
     *
     * @param x_cm x in cm
     * @param y_cm y in cm
     * @param z_cm z in cm
     * @return Field in Tesla at that point in the lab coordinate system
     */
    public Point3D Bfield(double x_cm, double y_cm, double z_cm) {

        float result[] = new float[3];

        sField.field((float) x_cm, (float) y_cm, (float) z_cm, result);

        return new Point3D(result[0] / 10., result[1] / 10., result[2] / 10.);

    }

    static boolean FieldsLoaded = false;
    //tries to get the magnetic fields 

    public static synchronized void getMagneticFields() {
        if (FieldsLoaded) {
            return;
        }

        Solenoid solenoid = null;
        //will read mag field assuming we are in a 
        //location relative to clasJLib. This will
        //have to be modified as appropriate.

        String clasDictionaryPath = CLASResources.getResourcePath("etc");

        //OK, see if we can create a Solenoid
        String solenoidFileName = clasDictionaryPath + "/data/magfield/clas12-fieldmap-solenoid.dat";

        File solenoidFile = new File(solenoidFileName);
        try {
            solenoid = Solenoid.fromBinaryFile(solenoidFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (solenoid != null) {
            System.out.println("                     SOLENOID MAP LOADED                    !!! ");
            sField = solenoid;
        }
        FieldsLoaded = true;
    }

    public static synchronized void setMagneticFieldScale(double SolenoidScale) {

        if (sField != null) {
            sField.setScaleFactor(SolenoidScale);
            System.out.println("CENTRAL TRACKING ***** ****** ****** THE SOLENOID IS BEING SCALED BY " + (SolenoidScale * 100) + "  %   *******  ****** **** ");
        }

    }

    public static Solenoid getField() {
        return sField;
    }

    public static void setField(Solenoid sField) {
        TrkSwimmer.sField = sField;
    }

    public static Swimmer getSwimmer() {
        return swimmer;
    }

    public static void setSwimmer(Swimmer swimmer) {
        TrkSwimmer.swimmer = swimmer;
    }

    public double get_x0() {
        return _x0;
    }

    public void set_x0(double _x0) {
        this._x0 = _x0;
    }

    public double get_y0() {
        return _y0;
    }

    public void set_y0(double _y0) {
        this._y0 = _y0;
    }

    public double get_z0() {
        return _z0;
    }

    public void set_z0(double _z0) {
        this._z0 = _z0;
    }

    public double get_phi() {
        return _phi;
    }

    public void set_phi(double _phi) {
        this._phi = _phi;
    }

    public double get_theta() {
        return _theta;
    }

    public void set_theta(double _theta) {
        this._theta = _theta;
    }

    public double get_pTot() {
        return _pTot;
    }

    public void set_pTot(double _pTot) {
        this._pTot = _pTot;
    }

    public double get_lundId() {
        return _charge;
    }

    public void set_lundId(int q) {
        this._charge = q;
    }

    /*
	 public static  void main(String arg[]) {
		if (BSTSwimmer.areFieldsLoaded == false) {
			BSTSwimmer.getMagneticFields();
		}	
		BSTSwimmer sw = new BSTSwimmer();
		//Point3D Bfield(double x_cm, double y_cm, double z_cm)
		Point3D p = sw.Bfield(39.7883, 26.2086, 28.5785)  ;
		System.out.println("B = "+p.x()*10000+", "
				+p.y()*10000+", "
				+p.z()*10000
				);
	 }
     */
    private static void printSummary(String message, int nstep, double momentum, double Q[], double hdata[]) {
        System.out.println(message);
        double R = Math.sqrt(Q[0] * Q[0] + Q[1] * Q[1] + Q[2] * Q[2]);
        double norm = Math.sqrt(Q[3] * Q[3] + Q[4] * Q[4] + Q[5] * Q[5]);
        double P = momentum * norm;

        System.out.println("Number of steps: " + nstep);

        if (hdata != null) {
            System.out.println("min stepsize: " + hdata[0]);
            System.out.println("avg stepsize: " + hdata[1]);
            System.out.println("max stepsize: " + hdata[2]);
        }
        System.out.println(String.format("R = [%8.5f, %8.5f, %8.5f] |R| = %7.4f m\nP = [%7.4e, %7.4e, %7.4e] |P| =  %9.6e GeV/c",
                Q[0], Q[1], Q[2], R, P * Q[3], P * Q[4], P * Q[5], P));
        System.out.println("norm (should be 1): " + norm);
        System.out.println("--------------------------------------\n");
    }

}
