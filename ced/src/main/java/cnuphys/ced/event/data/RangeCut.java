package cnuphys.ced.event.data;

import java.util.Properties;

import javax.xml.stream.XMLStreamException;

import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.xml.XmlPrintStreamWriter;

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
	
	/**
	 * A RangeCut is a simmple cut based on whether
	 * a column or expression value falls in range
	 * @param name the name, either a column or an expression
	 * @param minVal the min value
	 * @param maxVal the max value
	 */
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
	 * Get a unique type name
	 */
	@Override
	public String getCutType() {
		return CUT_TYPE;
	}
	
	@Override
	public void writeXml(XmlPrintStreamWriter writer) {
		Properties props = new Properties();
		props.put(PlotDialog.XmlName, _name);
		props.put(PlotDialog.XmlMin, DoubleFormat.doubleFormat(min, 10));
		props.put(PlotDialog.XmlMax, DoubleFormat.doubleFormat(max, 10));
		props.put(PlotDialog.XmlActive, isActive());
		try {
			writer.writeElementWithProps(PlotDialog.XmlRangeCut, props);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}
}
