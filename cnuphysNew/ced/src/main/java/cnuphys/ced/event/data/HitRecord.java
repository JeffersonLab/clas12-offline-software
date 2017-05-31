package cnuphys.ced.event.data;

import org.jlab.geom.prim.Point3D;

public class HitRecord {

	/** the hit Index */
	public int hitIndex;

	public Point3D avgHit;
	/**
	 * The 1-based ints (int order) used to identify the hit. For example, in EC
	 * the ints are sect, stack, view, strip.For DC they are sect, superlayer,
	 * layer, wire.
	 */
	public int[] ids;

	/**
	 * Hold information about a hit
	 * 
	 * @param dContainer
	 *            the data container
	 * @param hIndex
	 *            the 0-based hit index
	 * @param idArray
	 *            The 1-based ints (int order) used to identify the hit. For
	 *            example, in EC the ints are sect, stack, view, strip.For DC
	 *            they are sect, superlayer, layer, wire.
	 */
	public HitRecord(double avgX[], double avgY[],
			double avgZ[], int hIndex, int... idArray) {
		hitIndex = hIndex;
		ids = idArray;

		if ((avgX != null) && (avgX.length > hIndex)) {
			//convert to cm
			avgHit = new Point3D(avgX[hIndex] / 10, avgY[hIndex] / 10,
					avgZ[hIndex] / 10);
		}
	}

	/**
	 * Get a string representing the id array
	 * 
	 * @return a string representing the id array
	 */
	public String getIDString() {
		if ((ids == null) || (ids.length < 1)) {
			return "???";
		}
		StringBuilder sb = new StringBuilder(50);
		sb.append("]");
		for (int i = 0; i < ids.length; i++) {
			sb.append(ids[i]);
			if (i < (ids.length - 1)) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}
}
