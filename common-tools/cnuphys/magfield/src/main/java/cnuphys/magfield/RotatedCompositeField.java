package cnuphys.magfield;

import java.io.PrintStream;

public final class RotatedCompositeField extends CompositeField {

	/**
	 * Print the current configuration
	 * 
	 * @param ps the print stream
	 */
	@Override
	public void printConfiguration(PrintStream ps) {
		ps.println("ROTATED COMPOSITE FIELD");
		for (IMagField field : this) {
			field.printConfiguration(ps);

		}
	}

	@Override
	public String getName() {
		String s = "Rotated Composite contains: ";

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
}
