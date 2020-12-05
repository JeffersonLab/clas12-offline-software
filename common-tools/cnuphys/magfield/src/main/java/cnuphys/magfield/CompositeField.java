package cnuphys.magfield;

import java.io.PrintStream;
import java.util.ArrayList;

/**
 * A composition of multiple magnetic field maps. The resulting magnetic field
 * at a given point is the sum of the constituent fields at that point.
 *
 */
@SuppressWarnings("serial")
public class CompositeField extends ArrayList<IMagField> implements IMagField {

	/**
	 * Checks whether the field has been set to always return zero.
	 * 
	 * @return <code>true</code> if the field is set to return zero.
	 */
	@Override
	public final boolean isZeroField() {

		for (IMagField ifield : this) {
			if (!(ifield.isZeroField())) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean add(IMagField field) {

		// remove(field); //prevent duplicates

		// further check, only one solenoid or one torus
		// (might have different instances for some reason)

		for (IMagField ifield : this) {
			if (ifield.getClass().equals(field.getClass())) {
				remove(ifield);
				break;
			}
		}

		return super.add(field);
	}

	@Override
	public String getName() {
		String s = "Composite contains: ";

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
		for (IMagField field : this) {
			if (field instanceof Torus) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Check whether we have a solenoid field
	 * 
	 * @return <code>true</code> if we have a solenoid
	 */
	public boolean hasSolenoid() {
		for (IMagField field : this) {
			if (field instanceof Solenoid) {
				return true;
			}
		}

		return false;
	}

	@Override
	public float getB1(int index) {
		float b = 0f;
		for (IMagField field : this) {
			b += field.getB1(index);
		}
		return b;
	}

	@Override
	public float getB2(int index) {
		float b = 0f;
		for (IMagField field : this) {
			b += field.getB2(index);
		}
		return b;
	}

	@Override
	public float getB3(int index) {
		float b = 0f;
		for (IMagField field : this) {
			b += field.getB3(index);
		}
		return b;
	}

	@Override
	public float getMaxFieldMagnitude() {
		float max = 0;
		for (IMagField field : this) {
			max = Math.max(max, field.getMaxFieldMagnitude());
		}
		return max;
	}

	@Override
	public double getScaleFactor() {
		return 1;
	}

	/**
	 * Print the current configuration
	 * 
	 * @param ps the print stream
	 */
	@Override
	public void printConfiguration(PrintStream ps) {
		ps.println("COMPOSITE FIELD");
		for (IMagField field : this) {
			field.printConfiguration(ps);

		}
	}

	@Override
	public boolean contains(double x, double y, double z) {
		for (IMagField field : this) {
			if (field.contains(x, y, z)) {
				return true;
			}
		}
		return false;
	}

}
