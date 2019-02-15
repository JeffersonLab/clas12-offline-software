package cnuphys.magfield;

import java.io.File;
import java.io.PrintStream;

public class DoubleTorus extends CompositeField {

	protected Torus _torus1;
	protected Torus _torus2;
	
	
	public static DoubleTorus getDoubleTorus(String torus1Path, String torus2Path) {
		DoubleTorus doubleTorus = new DoubleTorus();
		doubleTorus._torus1 = readTorus(torus1Path);
		doubleTorus._torus2 = readTorus(torus2Path);
		
		
		return doubleTorus;
	}
	
	// read the torus field
	private static Torus readTorus(String fullPath) {
		
		File file = new File(fullPath);

		Torus torus = null;
		if (file.exists()) {
			try {
				torus = Torus.fromBinaryFile(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		 System.out.println("\nAttempted to read torus from [" + fullPath + "] success: " + (torus != null));
		return torus;
	}
	
	@Override
	public boolean add(IMagField field) {
		System.err.println("This should not have happened. DoubleTorus (A)");
		System.exit(1);
		return false;
	}

	@Override
	public String getName() {
		String s = "Double Torus contains: ";

		int count = 1;
		for (IMagField field : this) {
			if (count == 1) {
				s += field.getName();
			} else {
				s += " + " + field.getName();
			}
			count++;
		}

		return s;
	}
	
	/**
 	 * Check whether we have a torus field
 	 * 
 	 * @return <code>true</code> if we have a torus
 	 */
 	public boolean hasTorus() {
 		return true;
	}

	/**
	 * Check whether we have a solenoid field
	 * 
	 * @return <code>true</code> if we have a solenoid
	 */
	public boolean hasSolenoid() {
		return false;
	}

	@Override
	public float getB1(int index) {
		return _torus2.getB1(index) - _torus1.getB1(index);
	}

	@Override
	public float getB2(int index) {
		return _torus2.getB2(index) - _torus1.getB2(index);
	}

	@Override
	public float getB3(int index) {
		return _torus2.getB3(index) - _torus1.getB3(index);
	}

	@Override
	public float getMaxFieldMagnitude() {
		return Float.NaN;
	}

	@Override
	public double getScaleFactor() {
		return 1;
	}
	
	/**
	 * Print the current configuration
	 * @param ps the print stream
	 */
	@Override
	public void printConfiguration(PrintStream ps) {
		ps.println("DOUBLE TORUS FIELD");
		for (IMagField field : this) {
			field.printConfiguration(ps);
			
		}
	}

	
	public static void main(String[] arg) {
		DoubleTorus doubleTorus = getDoubleTorus("/Users/heddle/magfield/Symm_torus_r2501_phi16_z251_24Apr2018.dat",
				"/Users/heddle/magfield/Full_torus_r251_phi181_z251_08May2018.dat");

		DoubleTorusProbe doubleTorusProbe = new DoubleTorusProbe(doubleTorus);
		doubleTorusProbe.diagnosticField(35f, 0f, 366f);
	}
}
