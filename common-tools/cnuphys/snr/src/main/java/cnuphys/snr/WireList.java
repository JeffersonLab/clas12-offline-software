package cnuphys.snr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * 
 * @author heddle
 * A wire list is a list of wires (0-based) For CLAS12 [0..111]
 */
public class WireList extends ArrayList<Integer> {
	
	//the number of wires
	private int _numWires;
	private byte counts[];
	private double _avgWire = Double.NaN;
	
	/**
	 * Create a wirelist
	 */
	public WireList(int numWires) {
		super();
		_numWires = numWires;
		counts = new byte[numWires];
	}
		
	public void sort() {
		Comparator<Integer> comp = new Comparator<Integer>() {
			

			@Override
			public int compare(Integer o1, Integer o2) {
				
				double del1 = delFromAverage(o1);
				double del2 = delFromAverage(o2);
				return Double.compare(del1, del2);
				
			}
		};
		
		Collections.sort(this, comp);
	}
	
	public double delFromAverage(int wire) {
		if (Double.isNaN(_avgWire)) {
			_avgWire = averageWirePosition();
		}
		
		if (Double.isNaN(_avgWire)) {
			return Double.NaN;
		}
		else {
			return Math.abs(_avgWire-wire);
		}
	}
	
	/**
	 * Add a value, do not allow duplicates
	 * @param wire the 0-based value to add
	 * @return <code>true</code> as required.
	 */
	@Override
	public boolean add(Integer wire) {
		if ((wire < 0) || (wire >= _numWires)) {
			System.err.println("Bad wire index on WireList add: " +  wire);
		}
		remove(wire);
		counts[wire] += 1;
		_avgWire = Double.NaN;
		return super.add(wire);
	}
	
	@Override
	public boolean remove(Object o) {
		_avgWire = Double.NaN;
		return super.remove((Integer)o);
	}
	
	/**
	 * Get the repeat count for this wire
	 * @param wire the 0-based wire index
	 * @return the repeat count
	 */
	public int getCount(int wire) {
		return counts[wire];
	}
	
	@Override
	public void clear() {
		super.clear();
		counts = new byte[_numWires];	
	}
	
	/**
	 *  A string representation.  Note wires are zero-based,
	 *  but we print them out 1-based. Ugh. 
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(128);
		sb.append("[");

		if (!isEmpty()) {
			int len = size();
			for (int i = 0; i < len - 1; i++) {
				sb.append((get(i)+1) + " "); //print 1-based
			}
			sb.append(get(len-1)+1); //print 1-based
		}

		sb.append("]");
		return sb.toString();
	}
	
	/**
	 * Get the average wire position (zero-based)
	 * Duplicate weightings are used via the counts array
	 * @return the average wire position
	 */
	public double averageWirePosition() {
		if (isEmpty()) {
			return Double.NaN;
		}
		
		int totalCount = 0;
		
		double sum = 0;
		for (int wire : this) {
			sum += counts[wire]*wire;
			totalCount += counts[wire];
		}
		
		return sum/totalCount;
	}
	
	/**
	 * Is a given list a subset of this list
	 * @param wl the given list
	 * @return <code>true</code> if it is a subset
	 */
	public boolean hasSubset(WireList wl) {
		if (wl == null) {
			return false;
		}
		
		if (wl.isEmpty()) {
			return true;
		}
		
		if (wl.size() > size()) {
			return false;
		}
		
		for (Integer e : wl) {
			if (!contains(e)) {
				return false;
			}
		}
		
		return true;
	}

}
