package cnuphys.cnf.alldata.graphics;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.log.Log;
import cnuphys.cnf.alldata.ColumnData;
import cnuphys.cnf.alldata.DataManager;
import cnuphys.cnf.event.EventManager;

public class RangeCut implements ICut {

	private static final double TOLERANCE = 1.0e-8;

	/** minimum value */
	public double min;

	/** maximum value */
	public double max;

	protected boolean _active = true;

	protected String _name;

	// Column Data (if the name is a column name)
	protected ColumnData _columnData;

	// The expression (if expression used instead of columnData)
	private String _namedExpressionName;
	private NamedExpression _namedExpression;

	public static final String CUT_TYPE = "RANGECUT";

	/**
	 * A RangeCut is a simple cut based on whether a column or expression value
	 * falls in range
	 * 
	 * @param name   the name, either a column or an expression
	 * @param minVal the min value
	 * @param maxVal the max value
	 */
	public RangeCut(String name, double minVal, double maxVal) {
		_name = name;

		boolean isColumn = DataManager.getInstance().validColumnName(name);
		if (isColumn) {
			_columnData = DataManager.getInstance().getColumnData(name);
			if (_columnData == null) {
				Log.getInstance().warning("null ColumnData in RangeCut for [" + name + "]");
			}
		} else {
			_namedExpressionName = name;
		}

		min = minVal;
		max = maxVal;
	}

	/**
	 * Get the NamedExpression which might be null
	 * 
	 * @return the named expression
	 */
	public NamedExpression getNamedExpression() {
		if (_namedExpression != null) {
			return _namedExpression;
		}

		_namedExpression = DefinitionManager.getInstance().getNamedExpression(_namedExpressionName);

		return _namedExpression;
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

		DataEvent event = EventManager.getInstance().getCurrentEvent();
		if (event == null) {
			return true;
		}

		if (!_active) {
			return true;
		}

		NamedExpression namedExpression = getNamedExpression();

		if ((_columnData == null) && (namedExpression == null)) {
			return true;
		}

		double val;
		if (_columnData != null) {
			double vals[] = _columnData.getAsDoubleArray(event);

			if ((vals == null) || (index < 0)) {
				return false;
			}

			if (index >= vals.length) {
				return false;
			}

			val = vals[index];
		} else {
			val = namedExpression.value(event, index);
		}
		return pass(val);
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

}
