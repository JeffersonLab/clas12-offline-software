package cnuphys.cnf.plot;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.view.PlotView;
import cnuphys.bCNU.view.ViewManager;
import cnuphys.cnf.alldata.ColumnData;
import cnuphys.cnf.alldata.DataManager;
import cnuphys.cnf.event.EventManager;
import cnuphys.cnf.event.IEventListener;
import cnuphys.splot.pdata.DataColumn;
import cnuphys.splot.pdata.DataColumnType;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.DataSetType;
import cnuphys.splot.plot.PlotCanvas;
import cnuphys.splot.plot.PlotParameters;

/**
 * A wrapper class for plots
 * @author heddle
 *
 */
public class PlotWrapper implements IEventListener {
	
	//all the wrappers
	private static Hashtable<String, PlotWrapper> _wrappers = new Hashtable<>();
	
	//number of plots created
	private static int _plotCount = 0;

	//the view
	private PlotView _view;
	
	//this plot's id based on _plotCount;
	private int _id;
	
	//holds the column data
	private ArrayList<ColumnData> _columnDataArray;
	
	private String _key;
	
	
	private double _xMin =  Double.POSITIVE_INFINITY;
	private double _xMax =  Double.NEGATIVE_INFINITY;
	private double _yMin =  Double.POSITIVE_INFINITY;
	private double _yMax =  Double.NEGATIVE_INFINITY;
	
	private PlotWrapper(String key, String title) {
		_key = key;
		_id = _plotCount++;
		_view = new PlotView(title);
		_view.setVisible(true);
		_columnDataArray = new ArrayList<ColumnData>();
		EventManager.getInstance().addEventListener(this, 0);
		_wrappers.put(_key, this);
	}
	
	
	/**
	 * Create a simple 2D scatter olot
	 * @param xdata the x axis data
	 * @param ydata the y axis data
	 * @return
	 */
	public static PlotWrapper create2DScatterPlot(ColumnData xdata, ColumnData ydata) {
		
		String key = getKey(xdata, ydata);
		if (_wrappers.get(key) != null) {
			System.err.println("Already have a plot with key = [" + key + "]");
			return null;
		}
		
		String xs = xdata.getColumnName();
		String ys = ydata.getColumnName();
		String title = xs + " v " + ys; 

		PlotWrapper wrapper = new PlotWrapper(key, title);
		
		DataSet dataSet = null;
		try {
			dataSet = new DataSet(DataSetType.XYXY, xdata.getFullName(), ydata.getFullName());
			wrapper.getPlotCanvas().setDataSet(dataSet);
		} catch (DataSetException e) {
			e.printStackTrace();
		}
		
		wrapper._columnDataArray.add(xdata);
		wrapper._columnDataArray.add(ydata);
				
		wrapper.setXLabel(xs);
		wrapper.setYLabel(ys);
		wrapper.setTitle(title);
		
//		wrapper.getPlotParameters().setXRange(-0.4, 0.4);
//		wrapper.getPlotParameters().setYRange(0, 360);
		
		return wrapper;
	}
	
	private static String getKey(ColumnData... data) {
		StringBuffer sb = new StringBuffer(256);
		
		for (ColumnData cd : data) {
			sb.append(cd.getFullName() + "$");
		}
		String key =  sb.toString();
		return key;
	}
	
	public void setXLabel(String s) {
		PlotParameters params = getPlotParameters();
		params.setXLabel(s);
	}
	
	public void setYLabel(String s) {
		PlotParameters params = getPlotParameters();
		params.setYLabel(s);
	}
	
	public void setTitle(String s) {
		PlotParameters params = getPlotParameters();
		params.setPlotTitle(s);
	}

	/**
	 * An id simply based on the plot count
	 * @return the id
	 */
	public int getId() {
		return _id;
	}
	
	public PlotView getView() {
		return _view;
	}
	
	/**
	 * Get the plot canvas
	 * 
	 * @return the plot canvas
	 */
	public PlotCanvas getPlotCanvas() {
		return _view.getPlotCanvas();
	}
	
	/**
	 * Get the plot data set
	 * @return the plot data set
	 */
	public DataSet getDataSet() {
		return getPlotCanvas().getDataSet();
	}
	
	/**
	 * Get the plot parameters
	 * 
	 * @return the plot canvas
	 */
	public PlotParameters getPlotParameters() {
		return getPlotCanvas().getParameters();
	}

	
	/**
	 * Get all the plot wrappers
	 * @return all the plot wrappers
	 */
	public static Collection<PlotWrapper> getAllWrappers() {
		return _wrappers.values();
	}

	@Override
	public void newEvent(DataEvent event, boolean isStreaming) {
		DataSet ds = getDataSet();
		
		int count = ds.getColumnCount();
		
		double vals[] = new double[count];
		
		for (int i = 0; i < count; i++) {
			DataColumn dc = ds.getColumn(i);
//			System.err.println("DC NAME: [" + dc.getName() + "]");

			ColumnData cd = DataManager.getInstance().getColumnData(dc.getName());
			float fv[] = cd.getFloatArray(EventManager.getInstance().getCurrentEvent());

			if ((fv != null) && (fv.length > 0)) {
			//	System.err.println(fv[0]);
				vals[i] = fv[0];
				
				if (dc.getType() == DataColumnType.X) {
					_xMin = Math.min(_xMin, fv[0]);
					_xMax = Math.max(_xMax, fv[0]);
					getPlotParameters().setXRange(_xMin, _xMax);
//					System.err.println("XRange (" + _xMin + ", " + _xMax + ")");
				}
				if (dc.getType() == DataColumnType.Y) {
					_yMin = Math.min(_yMin, fv[0]);
					_yMax = Math.max(_yMax, fv[0]);
					getPlotParameters().setYRange(_yMin, _yMax);
//					System.err.println("YRange (" + _yMin + ", " + _yMax + ")");
				}
				
			}
			else {
				vals[i] = Double.NaN;
			}
		}
		
		if (goodVals(vals)) {
			try {
				ds.add(vals);
			} catch (DataSetException e) {
				e.printStackTrace();
			}
		}
		
		
		if (!isStreaming) {
			refresh();
		}
		
	}
	
	/**
	 * Call if done with a plot
	 */
	public void dispose() {
		_view.setVisible(false);
		ViewManager.getInstance().remove(_view);
		getPlotCanvas().clearPlot();
		EventManager.getInstance().removeEventListener(this);
	}
	
	/**
	 * Refresh the plot canvas
	 */
	public void refresh() {
		getPlotCanvas().needsRedraw(true);
	}
	
	private boolean goodVals(double vals[]) {
		if (vals == null) {
			return false;
		}
		
		if (vals.length < 1) {
			return false;
		}
		
		for (double val : vals) {
			if (Double.isNaN(val)) {
				return false;
			}
		}
		
		return true;
	}

	@Override
	public void openedNewEventFile(File file) {
		
	}

	@Override
	public void rewoundFile(File file) {
	}
	

	/**
	 * Streaming start message
	 * @param file file being streamed
	 * @param numToStream number that will be streamed
	 */
	@Override
	public void streamingStarted(File file, int numToStream) {
	}
	
	/**
	 * Streaming ended message
	 * @param file the file that was streamed
	 * @param int the reason the streaming ended
	 */
	@Override
	public void streamingEnded(File file, int reason) {
	}
}
