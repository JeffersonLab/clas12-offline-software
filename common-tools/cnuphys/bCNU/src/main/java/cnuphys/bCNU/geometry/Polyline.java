package cnuphys.bCNU.geometry;

import java.util.ArrayList;

/**
 * A PolyLine is a collection of Points. It is considered "open". i.e. the last
 * point is not the same as the first.
 * 
 * @author heddle
 *
 */
@SuppressWarnings("serial")
public class Polyline extends ArrayList<Point> {

	/**
	 * Get a String representation
	 * 
	 * @return a String representation of the PolyLine
	 */
	@Override
	public String toString() {
		if (size() == 0) {
			return "Empty PolyLine";
		}
		StringBuffer sb = new StringBuffer(60 * size());

		sb.append("PolyLine has " + size() + " points.\n");
		for (int i = 0; i < size(); i++) {
			Point p = get(i);
			sb.append("[" + (i + 1) + "] " + p + "\n");
		}

		return sb.toString();
	}
}
