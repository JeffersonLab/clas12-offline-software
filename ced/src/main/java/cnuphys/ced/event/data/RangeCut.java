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
	
	public static final String CUT_TYPE = "RANGECUT";
	
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
	
		String minString = "Min: " + DoubleFormat.doubleFormat(min, 3, 2);
		String maxString = "Max: " + DoubleFormat.doubleFormat(max, 3, 2);
		
		return _name + "\n" + minString + "\n" + maxString;
	}
	
	@Override
	public String plotText() {
		String minString = " Min: " + DoubleFormat.doubleFormat(min, 3, 2);
		String maxString = " Max: " + DoubleFormat.doubleFormat(max, 3, 2);
		
		return _name + minString + maxString;
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
	
	/**
	 * Create a range cut from a token string
	 * @param s the definition string from properties
	 * @return a RangeCut greated from a string
	 */
	public static RangeCut fromString(String s) {
		RangeCut rc = null;
		String tokens[] = PlotDialog.getTokens(s);
		if ((tokens != null) && (tokens.length == 5)) {
			// 0th token is type
			String name = tokens[1];
			double minVal = Double.parseDouble(tokens[2]);
			double maxVal = Double.parseDouble(tokens[3]);
			boolean active = Boolean.parseBoolean(tokens[4]);
			rc = new RangeCut(name, minVal, maxVal);
			if (rc != null) {
				rc.setActive(active);
			}
		}
		return rc;
	}
	
	/**
	 * Get the definition used to write in the properties
	 * @return the definition string
	 */
	@Override
	public String getDefinition() {
		String minString = DoubleFormat.doubleFormat(min, 10);
		String maxString = DoubleFormat.doubleFormat(max, 10);
		String activeString = "" + isActive();
		return PlotDialog.makeDelimittedString(CUT_TYPE, _name, minString, maxString, activeString);
	}


	/**
	 * Get a unique type name
	 */
	@Override
	public String getCutType() {
		return CUT_TYPE;
	}
}
