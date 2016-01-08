package cnuphys.ced.event.data;

import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.log.Log;

public class RangeCut implements ICut {
	
	private static final double TOLERANCE = 1.0e-8;
	
	/** minimum value */
	public double min;
	
	/** maximum value */
	public double max;
	
	protected boolean _active = true;
	
	protected String _name;
	
	protected ColumnData _cd;
	
	public RangeCut(String name, double minVal, double maxVal) {
		_name = name;
		_cd = ColumnData.getColumnData(name);
		if (_cd == null) {
			Log.getInstance().warning("null ColumnData in RangeCut for [" + name + "]");
		}
		min = minVal;
		max = maxVal;
	}
	
	@Override
	public String toString() {
	
		String minString = "min: " + DoubleFormat.doubleFormat(min, 4);
		String maxString = "max: " + DoubleFormat.doubleFormat(max, 4);
		
		return _name + "\n" + minString + "\n" + maxString;
	}

	@Override
	public boolean pass(double val) {
		if (!_active) {
			return true;
		}
		boolean passMin = (val - min) > -TOLERANCE;
		boolean passMax = (max - val) > -TOLERANCE;
		return passMax && passMin;
	}

	@Override
	public boolean pass(int index) {
				
		if (!_active) {
			return true;
		}
		
		if (_cd == null) {
			return true;
		}
		
		double vals[] = _cd.getAsDoubleArray();

		if ((vals == null) || (index < 0)) {
			return false;
		}
		
		if (index >= vals.length) {
			return false;
		}
		
		return pass(vals[index]);
	}

	@Override
	public void setActive(boolean active) {
		_active = active;
	}
	
	@Override
	public boolean isActive() {
		return _active;
	}

	@Override
	public String getName() {
		return _name;
	}

}
