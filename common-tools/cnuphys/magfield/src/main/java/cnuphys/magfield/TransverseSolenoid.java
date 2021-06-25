package cnuphys.magfield;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class TransverseSolenoid extends Solenoid {
	
	/**
	 * private constructor to instantiate a TransverseSolenoid
	 */
	private TransverseSolenoid() {
		setCoordinateNames("x", "y", "z");
	}


	@Override
	public String getName() {
		return "Transverse Solenoid";
	}
	
	/**
	 * Get some data as a string.
	 * 
	 * @return a string representation.
	 */
	@Override
	public final String toString() {
		String s = "Transverse Solenoid\n";
		s += super.toString();
		return s;
	}

	/**
	 * Print the current configuration
	 * 
	 * @param ps the print stream
	 */
	@Override
	public void printConfiguration(PrintStream ps) {
		ps.println(String.format("TRANSVERSE SOLENOID scale: %6.3f file: %s", _scaleFactor,
				MagneticFields.getInstance().getSolenoidBaseName()));
	}


	/**
	 * Checks this field active.
	 * 
	 * @return <code>true</code> if this field is active;
	 */
	@Override
	public boolean isActive() {
		return MagneticFields.getInstance().hasActiveSolenoid();
	}
	
	/**
	 * Obtain a transverse solenoid object from a binary file.
	 *
	 * @param file the file to read
	 * @return the transverse solenoid object
	 * @throws FileNotFoundException the file not found exception
	 */
	public static TransverseSolenoid fromBinaryFile(File file) throws FileNotFoundException {
		TransverseSolenoid transverseSolenoid = new TransverseSolenoid();
		transverseSolenoid.readBinaryMagneticField(file);
		// is the field ready to use?
		System.out.println(transverseSolenoid.toString());
		return transverseSolenoid;
	}

	/**
	 * Checks whether the field boundary contain the given point.
	 * 
	 * @param x the x coordinate in cm
	 * @param y the y coordinate in cm
	 * @param z the z coordinate in cm
	 * @return <code>true</code> if the field contains the given point
	 */
	@Override
	public boolean contains(double x, double y, double z) {

		if (!isActive()) {
			return false;
		}

		// apply the shifts
		x -= _shiftX;
		if ((x < q1Coordinate.getMin()) || (x > q1Coordinate.getMax())) {
			return false;
		}
		
		
		y -= _shiftY;
		if ((y < q2Coordinate.getMin()) || (y > q2Coordinate.getMax())) {
			return false;
		}

		
		z -= _shiftZ;
		if ((z < q3Coordinate.getMin()) || (z > q3Coordinate.getMax())) {
			return false;
		}


		
		return true;
	}

}
