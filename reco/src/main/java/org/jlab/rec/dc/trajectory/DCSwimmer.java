package org.jlab.rec.dc.trajectory;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.math3.util.FastMath;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

import cnuphys.magfield.CompositeField;
import cnuphys.magfield.MagneticField;
import cnuphys.magfield.RotatedCompositeField;
import cnuphys.magfield.Solenoid;
import cnuphys.magfield.Torus;
import cnuphys.rk4.IStopper;
import cnuphys.rk4.RungeKuttaException;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.Swimmer;

import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.GeometryLoader;
import org.jlab.utils.CLASResources;



/**
 * This class is used to swim a track in the forward tracker
 * @author ziegler
 *
 */
public class DCSwimmer {

	private static RotatedCompositeField rcompositeField;
	private static CompositeField compositeField;
	
	private  Swimmer swimmer;
	public Swimmer getSwimmer() {
		return swimmer;
	}

	public void setSwimmer(Swimmer swimmer) {
		this.swimmer = swimmer;
	}
	private  Swimmer labswimmer;
	public Swimmer getLabswimmer() {
		return labswimmer;
	}

	public void setLabswimmer(Swimmer labswimmer) {
		this.labswimmer = labswimmer;
	}
	
	// get some fit results
	private  double _x0;
	private  double _y0;
	private  double _z0;
	private  double _phi;
	private  double _theta;
	private  double _pTot;
	public  double _rMax = 5;
	public  double _maxPathLength = 9;
	private  int _charge;
		
	//public boolean isRotatedCoordinateSystem = true;
	
	public int nSteps; 
	
	public DCSwimmer() {
		//create a swimmer for our magnetic field
		//swimmer = new Swimmer(rcompositeField);
		// create a swimmer for the magnetic fields
		//if(areFieldsLoaded==false)
		//    getMagneticFields();
		
		swimmer = new Swimmer(rcompositeField);
		
		labswimmer = new Swimmer(compositeField);
	}

	/**
	 * 
	 * @param direction +1 for out -1 for in
	 * @param x0
	 * @param y0
	 * @param thx
	 * @param thy
	 * @param p
	 * @param charge
	 */
	public void SetSwimParameters(int direction, double x0, double y0, double z0, double thx, double thy, double p, int charge) {
		
		// x,y,z in m = swimmer units
		 _x0  = x0/100;
		 _y0  = y0/100;
		 _z0  = z0/100;
		 
		 double pz = direction*p / Math.sqrt(thx*thx + thy*thy + 1);
		 double px = thx*pz;
		 double py = thy*pz;
		 _phi =  Math.toDegrees(FastMath.atan2(py, px));
		 _pTot =  Math.sqrt(px*px+py*py+pz*pz);
		 _theta = Math.toDegrees(Math.acos(pz/_pTot));
		
		 _charge = direction*charge;
		 
		 //System.out.println(" _x0 "+_x0 +" _y0 "+_y0 +" _z0 "+_z0 +" px "+px +" py "+py + "pz "+pz +" tx "+thx+" ty "+thy);
		
	}
	
	/**
	 * Sets the parameters used by swimmer based on the input track state vector parameters swimming outwards
	 * @param superlayerIdx
	 * @param layerIdx
	 * @param x0
	 * @param y0
	 * @param thx
	 * @param thy
	 * @param p
	 * @param charge
	 */
	public void SetSwimParameters(int superlayerIdx, int layerIdx, double x0, double y0, double thx, double thy, double p, int charge) {
		// z at a given DC plane in the tilted coordinate system
		double z0 =0;
		
		if(superlayerIdx!=-1 && layerIdx!=-1) 
			z0 = GeometryLoader.dcDetector.getSector(0).getSuperlayer(superlayerIdx).getLayer(layerIdx).getPlane().point().z();
		
		// x,y,z in m = swimmer units
		 _x0  = x0/100;
		 _y0  = y0/100;
		 _z0  = z0/100;
		 
		 double pz = p / Math.sqrt(thx*thx + thy*thy + 1);
		 double px = thx*pz;
		 double py = thy*pz;
		 _phi =  Math.toDegrees(FastMath.atan2(py, px));
		 _pTot =  Math.sqrt(px*px+py*py+pz*pz);
		 _theta = Math.toDegrees(Math.acos(pz/_pTot));
		
		 _charge = charge;
		
	}
	
	/**
	 * Sets the parameters used by swimmer based on the input track parameters
	 * @param x0
	 * @param y0
	 * @param z0
	 * @param px
	 * @param py
	 * @param pz
	 * @param charge
	 */
		public void SetSwimParameters(double x0, double y0, double z0, double px, double py, double pz, int charge) {
			 _x0  = x0/100;
			 _y0  = y0/100;
			 _z0  = z0/100;
			 _phi =  Math.toDegrees(FastMath.atan2(py, px));
			 _pTot =  Math.sqrt(px*px+py*py+pz*pz);
			 _theta = Math.toDegrees(Math.acos(pz/_pTot));
			
			 _charge = charge;
			
		}

		
	public  double[] SwimToPlane(double z_cm) {
		double z = z_cm/100; // the magfield method uses meters
		double[] value = new double[8];
		double accuracy = 20e-6; //20 microns
		double stepSize = Constants.SWIMSTEPSIZE; //  microns
		
		if(_pTot<Constants.MINTRKMOM  ) // fiducial cut 
			return null;
		SwimTrajectory traj = null;		
		double hdata[] = new double[3];
    
		try {
			traj = swimmer.swim(_charge, _x0, _y0, _z0, _pTot, 
					_theta, _phi, z, accuracy,_rMax, 
					_maxPathLength, stepSize, Swimmer.CLAS_Tolerance, hdata);
			
			traj.computeBDL(rcompositeField);
			
			double lastY[] = traj.lastElement();
			
			value[0] = lastY[0]*100; // convert back to cm
			value[1] = lastY[1]*100; // convert back to cm
			value[2] = lastY[2]*100; // convert back to cm
			value[3] = lastY[3]*_pTot;
			value[4] = lastY[4]*_pTot;
			value[5] = lastY[5]*_pTot;
			value[6] = lastY[6]*100;
			value[7] = lastY[7]*10;
			
			
		} catch (RungeKuttaException e) {
			e.printStackTrace();
		}
		return value;
		
	}
	public  double[] SwimToPlaneLab(double z_cm) {
		double z = z_cm/100; // the magfield method uses meters
		double[] value = new double[8];
		double accuracy = 20e-6; //20 microns
		double stepSize = Constants.SWIMSTEPSIZE; //  microns
		
		if(_pTot<Constants.MINTRKMOM  ) // fiducial cut 
			return null;
		SwimTrajectory traj = null;		
		double hdata[] = new double[3];
      
		
		try {
			traj = labswimmer.swim(_charge, _x0, _y0, _z0, _pTot, 
					_theta, _phi, z, accuracy,_rMax, 
					_maxPathLength, stepSize, Swimmer.CLAS_Tolerance, hdata);
			
			traj.computeBDL(compositeField);
			
			double lastY[] = traj.lastElement();
			
			value[0] = lastY[0]*100; // convert back to cm
			value[1] = lastY[1]*100; // convert back to cm
			value[2] = lastY[2]*100; // convert back to cm
			value[3] = lastY[3]*_pTot;
			value[4] = lastY[4]*_pTot;
			value[5] = lastY[5]*_pTot;
			value[6] = lastY[6]*100;
			value[7] = lastY[7]*10;
			
			
		} catch (RungeKuttaException e) {
			e.printStackTrace();
		}
		return value;
		
	}
	//
	//for matching to HTCC
		private class SphericalBoundarySwimStopper implements IStopper {
			
			private double _finalPathLength = Double.NaN;

			private double _Rad;
			/**
			 * A  swim stopper that will stop if the boundary of a plane is crossed
			 * @param maxR the max radial coordinate in meters. 
			 */
			private SphericalBoundarySwimStopper(double Rad) {
				// DC reconstruction units are cm.  Swimmer units are m.  Hence scale by 100
				_Rad = Rad;
			}
			@Override
			public boolean stopIntegration(double t, double[] y) {
				
				double r = Math.sqrt(y[0]*y[0] + y[1]*y[1] + y[2]*y[2])*100.;

				return (r > _Rad);
				
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
			 * @param finalPathLength the final path length in meters
			 */
			@Override
			public void setFinalT(double finalPathLength) {
				_finalPathLength = finalPathLength;
			}
		}
	
		public  double[] SwimToSphere(double Rad) {
				
				double[] value = new double[8];
				// using adaptive stepsize
				
				SphericalBoundarySwimStopper stopper = new SphericalBoundarySwimStopper(Rad);
		
				// step size in m
				double stepSize = 1e-4; // m
		
				SwimTrajectory st = labswimmer.swim(_charge, _x0, _y0, _z0, _pTot, _theta, _phi, stopper, _maxPathLength, stepSize, 0.0005);
				st.computeBDL(compositeField);
				double[] lastY = st.lastElement();
				
				value[0] = lastY[0]*100; // convert back to cm
				value[1] = lastY[1]*100; // convert back to cm
				value[2] = lastY[2]*100; // convert back to cm
				value[3] = lastY[3]*_pTot; //normalized values
				value[4] = lastY[4]*_pTot;
				value[5] = lastY[5]*_pTot;
				value[6] = lastY[6]*100;
				value[7] = lastY[7]*10; //Conversion from kG.m to T.cm 
		
			return value;
				
		}
	
		private class CylinderBoundarySwimStopper implements IStopper {

			private double _rMaxSq;

			private double _finalPathLength = Double.NaN;

			
			/**
			 * A default swim stopper that will stop if either a max pathlength is
			 * exceeded or if a radial coordinate is exceeded
			 * 
			 * @param maxR
			 *            the max radial coordinate in meters. Give a negative
			 */
			private CylinderBoundarySwimStopper(final double maxR) {

				_rMaxSq =  maxR;
			}

			@Override
			public boolean stopIntegration(double t, double[] y) {
				double xx = y[0];
				double yy = y[1];
				
				// stop if radial coordinate too big
				double rsq = Math.sqrt(xx * xx + yy * yy); 
				
				return (rsq > _rMaxSq);
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
			 * @param finalPathLength the final path length in meters
			 */
			@Override
			public void setFinalT(double finalPathLength) {
				_finalPathLength = finalPathLength;
			}

		}


		public  double[] SwimToCylinder(double cylRad_cm) {
			
			double cylRad = cylRad_cm/100.0;
			double[] value = new double[8];
			// using adaptive stepsize
			double tolerance = 1.e-6;
			double[] hdata = null;
			CylinderBoundarySwimStopper stopper = new CylinderBoundarySwimStopper(cylRad);
			// step size in m
			double stepSize = 1.e-6; // m

			SwimTrajectory st = null;
			st = labswimmer.swim(_charge, _x0, _y0, _z0, _pTot, _theta, _phi, stopper, _maxPathLength, stepSize, 0.000001);
			st.computeBDL(compositeField);
			double[] lastY = st.lastElement();
			
			value[0] = lastY[0]*100; // convert back to cm
			value[1] = lastY[1]*100; // convert back to cm
			value[2] = lastY[2]*100; // convert back to cm
			value[3] = lastY[3]*_pTot; //normalized values
			value[4] = lastY[4]*_pTot;
			value[5] = lastY[5]*_pTot;
			value[6] = lastY[6]*100;
			value[7] = lastY[7]*10; //Conversion from kG.m to T.cm 
	
		return value;
				
		}
		
		//added for swimming to outer detectors
		private class PlaneBoundarySwimStopper implements IStopper {
			private double _finalPathLength = Double.NaN;
			private double 	 _d;
			private Vector3D _n;
			/**
			 * A  swim stopper that will stop if the boundary of a plane is crossed
			 * @param maxR the max radial coordinate in meters. 
			 */
			private PlaneBoundarySwimStopper(double d, Vector3D n) {
				// DC reconstruction units are cm.  Swimmer units are m.  Hence scale by 100
				_d = d;
				_n = n;
			}
			@Override
			public boolean stopIntegration(double t, double[] y) {				
				
				double dtrk = _n.x()*y[0]*100+_n.y()*y[1]*100+_n.z()*y[2]*100;

				return (dtrk > _d);
				
			}
			@Override
			public double getFinalT() {
				
				return _finalPathLength;
			}
			
			/**
			 * Set the final path length in meters
			 * @param finalPathLength the final path length in meters
			 */
			@Override
			public void setFinalT(double finalPathLength) {
				_finalPathLength = finalPathLength;
			}
		}
		
		
		public  double[] SwimToPlane(double d, Vector3D n) {
				
				double[] value = new double[8];
				// using adaptive stepsize
				
				PlaneBoundarySwimStopper stopper = new PlaneBoundarySwimStopper(d, n);
		
				// step size in m
				double stepSize = 1e-4; // m
		
				SwimTrajectory st = swimmer.swim(_charge, _x0, _y0, _z0, _pTot, _theta, _phi, stopper, _maxPathLength, stepSize, 0.0005);
				st.computeBDL(compositeField);
				double[] lastY = st.lastElement();
				
				value[0] = lastY[0]*100; // convert back to cm
				value[1] = lastY[1]*100; // convert back to cm
				value[2] = lastY[2]*100; // convert back to cm
				value[3] = lastY[3]*_pTot; //normalized values
				value[4] = lastY[4]*_pTot;
				value[5] = lastY[5]*_pTot;
				value[6] = lastY[6]*100;
				value[7] = lastY[7]*10; //Conversion from kG.m to T.cm 
		
			return value;
				
		}		
	public static double CLAS_Tolerance[];
	static {
		double xscale = 1.0;  //position scale order of meters
		double pscale = 1.0;  //direct cosine px/P etc scale order of 1
		double eps = 1.0e-12;  //tolerance
		double xTol = eps*xscale;
		double pTol = eps*pscale;
		CLAS_Tolerance = new double[6];
		for (int i = 0; i < 3; i++) {
			CLAS_Tolerance[i] = xTol;
			CLAS_Tolerance[i+3] = pTol;
		}
	}

	
	//get the field at a given point in the tilted sector coordinate system
	/**
	 * 
	 * @param x_cm x in cm
	 * @param y_cm y in cm
	 * @param z_cm z in cm 
	 * @return Field in Tesla at that point in the tilted coordinate system
	 */
	public Point3D Bfield(double x_cm, double y_cm, double z_cm) {
		
		float result[] = new float[3];

		rcompositeField.field((float)x_cm, (float)y_cm, (float)z_cm, result);
		
		return new Point3D(result[0]/10, result[1]/10, result[2]/10);
		
	}
	
	public Point3D BfieldLab(double x_cm, double y_cm, double z_cm) {
		
		float result[] = new float[3];

		compositeField.field((float)x_cm, (float)y_cm, (float)z_cm, result);

		return new Point3D(result[0]/10, result[1]/10, result[2]/10);
		
	}
	public static synchronized void setMagneticFieldsScales(double SolenoidScale, double TorusScale) {
		if (rcompositeField.get(0) != null) {			
			((MagneticField) rcompositeField.get(0)).setScaleFactor(TorusScale);
			System.out.println("FORWARD TRACKING ***** ****** ****** THE TORUS IS BEING SCALED BY "+ (TorusScale*100) +"  %   *******  ****** **** ");			
		}
		if (compositeField.get(0) != null) 		
			((MagneticField) compositeField.get(0)).setScaleFactor(TorusScale);
		
		if (rcompositeField.get(1) != null) {			
			((MagneticField) rcompositeField.get(1)).setScaleFactor(SolenoidScale);
			System.out.println("FORWARD TRACKING ***** ****** ****** THE SOLENOID IS BEING SCALED BY "+ (SolenoidScale*100) +"  %   *******  ****** **** ");			
		}
		if (compositeField.get(1) != null) 		
			((MagneticField) compositeField.get(1)).setScaleFactor(SolenoidScale);
		
		//System.out.println(" Fields at orig = "+compositeField.fieldMagnitude(0, 0, 0)+" Rotated Fields: "+rcompositeField.fieldMagnitude(0, 0, 0));
	
		
	}
	
	static boolean FieldsLoaded = false ;
	//tries to get the magnetic fields 
	public static synchronized void getMagneticFields() {
		 if(FieldsLoaded)
			 return;
		 
		 Torus torus = null;
		 Solenoid solenoid = null;
		//will read mag field assuming we are in a 
		//location relative to clasJLib. This will
		//have to be modified as appropriate.
		
		String clasDictionaryPath = CLASResources.getResourcePath("etc");
		
		String torusFileName = clasDictionaryPath + "/data/magfield/clas12-fieldmap-torus.dat";
		
		File torusFile = new File(torusFileName);
		try {
			torus = Torus.fromBinaryFile(torusFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		//OK, see if we can create a Solenoid
		String solenoidFileName = clasDictionaryPath + "/data/magfield/clas12-fieldmap-solenoid.dat";
			//OK, see if we can create a Torus
			//if(clasDictionaryPath == "../clasJLib")
			//	solenoidFileName = clasDictionaryPath + "/data/solenoid/v1.0/solenoid-srr.dat";
			
		File solenoidFile = new File(solenoidFileName);
		try {
			solenoid = Solenoid.fromBinaryFile(solenoidFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		rcompositeField = new RotatedCompositeField();
		compositeField = new CompositeField();
		//System.out.println("***** ****** CREATED A COMPOSITE ROTATED FIELD ****** **** ");
			
		if (torus != null) {			
			
			rcompositeField.add(torus);
			compositeField.add(torus);
		}
		if (solenoid != null) {
			
			rcompositeField.add(solenoid);
			compositeField.add(solenoid);
			
		}
		FieldsLoaded = true;
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

	
	
	@SuppressWarnings("unused")
	private void printSummary(String message, int nstep, double momentum, double Q[], double hdata[]) {
		System.out.println(message);
		double R = Math.sqrt(Q[0]*Q[0] + Q[1]*Q[1] + Q[2]*Q[2]);
		double norm = Math.sqrt(Q[3]*Q[3] + Q[4]*Q[4] + Q[5]*Q[5]);
		double P = momentum*norm;
				
	    System.out.println("Number of steps: " + nstep);

		if (hdata != null) {
			System.out.println("min stepsize: " + hdata[0]);
			System.out.println("avg stepsize: " + hdata[1]);
			System.out.println("max stepsize: " + hdata[2]);
		}
		System.out.println(String.format("R = [%8.5f, %8.5f, %8.5f] |R| = %7.4f m\nP = [%7.4e, %7.4e, %7.4e] |P| =  %9.6e GeV/c",
				Q[0], Q[1], Q[2], R, P*Q[3], P*Q[4], P*Q[5], P));
		System.out.println("norm (should be 1): " + norm);
		System.out.println("--------------------------------------\n");
	}

	

}