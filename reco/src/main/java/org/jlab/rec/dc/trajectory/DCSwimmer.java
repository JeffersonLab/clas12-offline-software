package org.jlab.rec.dc.trajectory;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.math3.util.FastMath;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

import cnuphys.magfield.CompositeField;
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
	// get some fit results
	private  final double hdata[] = new double[3];
	private  double _x0;
	private  double _y0;
	private  double _z0;
	private  double _phi;
	private  double _theta;
	private  double _pTot;
	public  double _rMax = 5;
	public  double _maxPathLength = 9;
	private  int _charge;
		
	public boolean isRotatedCoordinateSystem = true;
	
	public int nSteps; 
	
	public static boolean areFieldsLoaded;
	
	public DCSwimmer() {
		//create a swimmer for our magnetic field
		//swimmer = new Swimmer(rcompositeField);
		// create a swimmer for the magnetic fields
		//if(areFieldsLoaded==false)
		//    getMagneticFields();
		
		if(isRotatedCoordinateSystem == true)
			swimmer = new Swimmer(rcompositeField);
		
		if(isRotatedCoordinateSystem == false)
			swimmer = new Swimmer(compositeField);
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
		
		try {
			SwimTrajectory traj = swimmer.swim(_charge, _x0, _y0, _z0, _pTot, 
					_theta, _phi, z, accuracy,_rMax, 
					_maxPathLength, stepSize, Swimmer.CLAS_Tolerance, hdata);
			
			if(isRotatedCoordinateSystem == true)
				traj.computeBDL(rcompositeField);
			if(isRotatedCoordinateSystem == false)
				traj.computeBDL(compositeField);
			
			double lastY[] = traj.lastElement();
			
			//System.out.println("Swimmer End Params:");
			//System.out.println(lastY[0]+" "+lastY[1]+" "+lastY[2]+" "+lastY[3]+" "+lastY[4]+" "+lastY[5]+" .");
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
	
	//added for raster study
		private class CylinderBoundarySwimStopper implements IStopper {

			private double _cylRad;
			/**
			 * A  swim stopper that will stop if the boundary of a plane is crossed
			 * @param maxR the max radial coordinate in meters. 
			 */
			private CylinderBoundarySwimStopper(double cylRad) {
				// DC reconstruction units are cm.  Swimmer units are m.  Hence scale by 100
				_cylRad = cylRad;
			}
			@Override
			public boolean stopIntegration(double t, double[] y) {
				
				double r = Math.sqrt(y[0]*y[0] +y[1]*y[1])*100.;

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
	
		public  double[] SwimToCylinder(double cylRad) {
				
				double[] value = new double[8];
				// using adaptive stepsize
				
				CylinderBoundarySwimStopper stopper = new CylinderBoundarySwimStopper(cylRad);
		
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
		
		//added for swimming to outer detectors
		private class PlaneBoundarySwimStopper implements IStopper {

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
				// TODO Auto-generated method stub
				return 0;
			}
			@Override
			public void setFinalT(double arg0) {
				// TODO Auto-generated method stub
				
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

		if(isRotatedCoordinateSystem == true)
			rcompositeField.field((float)x_cm, (float)y_cm, (float)z_cm, result);
		if(isRotatedCoordinateSystem == false)
			compositeField.field((float)x_cm, (float)y_cm, (float)z_cm, result);

		return new Point3D(result[0]/10, result[1]/10, result[2]/10);
		
	}
	
	//tries to get the magnetic field assuming it is in clasJLib
	public static synchronized  void getMagneticFields() {

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
			if(clasDictionaryPath == "../clasJLib")
				solenoidFileName = clasDictionaryPath + "/data/solenoid/v1.0/solenoid-srr.dat";
			
		File solenoidFile = new File(solenoidFileName);
		try {
			solenoid = Solenoid.fromBinaryFile(solenoidFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		/*
		if(Constants.FieldConfig=="variable") {
		
			if(Constants.TORSCALE<0) {
				if(torus.isInvertField()==false)
					torus.setInvertField(true);
			}
			if(Constants.SOLSCALE<0) {
				if(solenoid.isInvertField()==false)
					solenoid.setInvertField(true);
			}
			if(Constants.TORSCALE>0) {
				if(torus.isInvertField()==true)
					torus.setInvertField(false);
			}
			if(Constants.SOLSCALE>0) {
				if(solenoid.isInvertField()==true)
					solenoid.setInvertField(false);
			}
		} */
		rcompositeField = new RotatedCompositeField();
		compositeField = new CompositeField();
		//System.out.println("***** ****** CREATED A COMPOSITE ROTATED FIELD ****** **** ");
			
		if (torus != null) {
			/*
			if(Constants.TORSCALE<0) {
				if(torus.isInvertField()==false)
					torus.setInvertField(true);
			}
			
			if(Constants.TORSCALE>0) {
				if(torus.isInvertField()==true)
					torus.setInvertField(false);
			}
			
			torus.setScaleField(true);  */
			torus.setScaleFactor(Constants.TORSCALE);
			System.out.println("***** ****** ****** THE TORUS IS BEING SCALED BY "+ (Constants.TORSCALE*100) +"  %   *******  ****** **** ");
			rcompositeField.add(torus);
			compositeField.add(torus);
		}
		if (solenoid != null) {
			/*
			if(Constants.SOLSCALE<0) {
				if(solenoid.isInvertField()==false)
					solenoid.setInvertField(true);
			}
			
			if(Constants.SOLSCALE>0) {
				if(solenoid.isInvertField()==true)
					solenoid.setInvertField(false);
			}
			solenoid.setScaleField(true); */
			solenoid.setScaleFactor(Constants.SOLSCALE);
			System.out.println(" Sol at orig = "+solenoid.fieldMagnitude(0, 0, 0));
			System.out.println("***** ****** ****** THE SOLENOID IS BEING SCALED BY "+ (Constants.SOLSCALE*100) +"  %   *******  ****** **** ");
					
				rcompositeField.add(solenoid);
				compositeField.add(solenoid);
				System.out.println(" Compos at orig = "+compositeField.fieldMagnitude(0, 0, 0)+" "+rcompositeField.fieldMagnitude(0, 0, 0));
		
		}
		areFieldsLoaded = true;
		//System.out.println("Fields are Loaded! with torus inverted ? "+torus.isInvertField()+
		//		" and solenoid inverted ? "+solenoid.isInvertField());
	}	
		
		
	
	public  Swimmer getSwimmer() {
		return swimmer;
	}

	public  void setSwimmer(Swimmer swimmer) {
		this.swimmer = swimmer;
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