package cnuphys.swim;

import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.MagneticField;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.Solenoid;
import cnuphys.magfield.Torus;
import cnuphys.rk4.RungeKuttaException;
import cnuphys.magfield.MagneticField.MathLib;
import cnuphys.magfield.MagneticFields.FieldType;

public class VTest {

	
	
	public static void main(String arg[]) {
		MagneticFields.getInstance().initializeMagneticFields();
		Torus torus = (Torus)MagneticFields.getInstance().getIField(FieldType.TORUS);
		Solenoid solenoid = (Solenoid)MagneticFields.getInstance().getIField(FieldType.SOLENOID);
		
		torus.setScaleFactor(-1);
		solenoid.setScaleFactor(1);
			
		
		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITEROTATED);

		System.out.println("Active Field Description: " + MagneticFields.getInstance().getActiveFieldDescription());
		
		float result[] = new float[3];
		MagneticFields.getInstance().getActiveField().field(0.1f, 0.1f, 0.1f, result);
		System.out.println("[" + result[0]+ ", " + result[1] + ", " + result[2] + "]");
		MagneticFields.getInstance().getActiveField().field(-13f, 10f, 350f, result);
		System.out.println("[" + result[0]+ ", " + result[1] + ", " + result[2] + "]");
		
		MagneticField.setMathLib(MagneticField.MathLib.FAST);
		FieldProbe.cache(false);
		
		DCSwimmer swim = new DCSwimmer();
		swim.SetSwimParameters(0, 0, 0, -0.13, -0.62, 2.66, -1);
        double[]swimVal =swim.SwimToPlane(400);
        for(int i = 0; i<swimVal.length; i++)
	            System.out.println("swimVal["+i+"]= "+swimVal[i]);
	 
	}
	

}

class DCSwimmer {
	
	Swimmer swimmer;
	   private double _x0;
	    private double _y0;
	    private double _z0;
	    private double _phi;
	    private double _theta;
	    private double _pTot;
	    private int _charge;
	    public double _maxPathLength = 9;
	    public double _rMax = 5 + 3; //increase to allow swimming to outer detectors

	public DCSwimmer() {	
	     swimmer   = new Swimmer(MagneticFields.getInstance().getActiveField());
	}
	
   
   public void SetSwimParameters(double x0, double y0, double z0, double px, double py, double pz, int charge) {
       _x0 = x0 / 100;
       _y0 = y0 / 100;
       _z0 = z0 / 100;
       _phi = MagneticField.atan2Deg(py, px);
       _pTot = Math.sqrt(px * px + py * py + pz * pz);
       _theta = Math.toDegrees(Math.acos(pz / _pTot));

       _charge = charge;

   }

   public double[] SwimToPlane(double z_cm) {
       double z = z_cm / 100; // the magfield method uses meters
       double[] value = new double[8];
       double accuracy = 20e-6; //20 microns

       double stepSize = Constants.SWIMSTEPSIZE; //  microns

       if (_pTot < Constants.MINTRKMOM) // fiducial cut 
       {
           return null;
       }
       SwimTrajectory traj = null;
       double hdata[] = new double[3];

       try {
           traj = swimmer.swim(_charge, _x0, _y0, _z0, _pTot,
                   _theta, _phi, z, accuracy, _rMax,
                   _maxPathLength, stepSize, Swimmer.CLAS_Tolerance, hdata);

           traj.computeBDL(MagneticFields.getInstance().getActiveField());

           double lastY[] = traj.lastElement();

           value[0] = lastY[0] * 100; // convert back to cm
           value[1] = lastY[1] * 100; // convert back to cm
           value[2] = lastY[2] * 100; // convert back to cm
           value[3] = lastY[3] * _pTot;
           value[4] = lastY[4] * _pTot;
           value[5] = lastY[5] * _pTot;
           value[6] = lastY[6] * 100;
           value[7] = lastY[7] * 10;

       } catch (RungeKuttaException e) {
           e.printStackTrace();
       }
       
       return value;
   }
   
}

class Constants {
	   public static final double SWIMSTEPSIZE = 5.00 * 1.e-4; //n00 microns
	   
	   public static final double MINTRKMOM = 0.050;


}
