package cnuphys.snr;

import java.util.ArrayList;

/**
 * 
 * @author heddle This is used in the stage 2 analysis, not in the basic SNR one
 *         stage analysis
 */
public class SegmentStartList extends ArrayList<Integer> {

	/**
	 * Holds a list of segment starts for a cluster
	 */
	public SegmentStartList() {
		super();
	}
	
	/**
	 *  A string representation. Note wires are zero-based,
	 *  but we print them out 1-based. Ugh. 
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(128);
		sb.append("<");

		if (!isEmpty()) {
			int len = size();
			for (int i = 0; i < len; i++) {
				int wire = get(i);
				sb.append(wire+1);
				
				if (i < (len-1)) {
					sb.append(" ");
				}
			}
		}

		sb.append(">");
		return sb.toString();
	}
}
