package cnuphys.splot.pdata;

import java.util.Timer;
import java.util.TimerTask;

import cnuphys.splot.fit.IValueGetter;

public class StripData {

	// this is the "curve" name
	private String _name;

	// the capacity before data starts falling off the (left) edge
	private int _capacity;

	// retrieves the next value
	private IValueGetter _valueGetter;

	// update timer
	private Timer _timer;

	// Data set
	private DataSet _dataSet;

	// time when data collection started
	private long _startTime;

	// update interval ms
	private long _interval;

	// x and y columns
	private DataColumn _xColumn;
	private DataColumn _yColumn;

	public StripData(String name, int capacity, IValueGetter getter, long interval) {
		_name = name;
		_capacity = capacity;
		_valueGetter = getter;
		_interval = interval;
	}

	public void setDataSet(final DataSet ds) {
		if (_dataSet != null) {
			return;
		}

		_dataSet = ds;

		_xColumn = _dataSet.getXColumn(0);
		_yColumn = _dataSet.getCurve(0);

		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				double t = (System.currentTimeMillis() - _startTime) / 1000;
				double val = _valueGetter.value(t);
				
				// do we have to bump left?
				if (_xColumn.size() == _capacity) {
					_xColumn.removeFirst();
					_yColumn.removeFirst();
				}

				try {
					_dataSet.add(t, val);
				}
				catch (DataSetException e) {
					e.printStackTrace();
				}
			}

		};

		_startTime = System.currentTimeMillis();

		_timer = new Timer();
		_timer.scheduleAtFixedRate(task, _interval, _interval);
	}

	/**
	 * Stop the accumulation
	 */
	public void stop() {
		if (_timer != null) {
			_timer.cancel();
		}
	}
}
