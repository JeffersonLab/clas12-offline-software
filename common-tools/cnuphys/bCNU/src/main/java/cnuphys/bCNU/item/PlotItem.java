package cnuphys.bCNU.item;

import java.awt.geom.Point2D;

import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.HistoData;
import cnuphys.splot.plot.PlotCanvas;
import cnuphys.splot.plot.PlotPanel;
import cnuphys.splot.plot.PlotParameters;

public class PlotItem extends PanelItem {

	private PlotCanvas _plotCanvas;
	private PlotPanel _plotPanel;
	
//	  public PlotCanvas(DataSet dataSet, String plotTitle, String xLabel,
//			    String yLabel) {

		  
	/**
	 * Create a panel whose location is based on world coordinates but whose
	 * extent is in pixels. And example might be a plot item or image.
	 * 
	 * @param layer
	 *            the Layer this item is on.
	 * @param location
	 *            the location of the lower-left in world coordinates
	 * @param width
	 *            the width in pixels
	 * @param height
	 *            the height in pixels
	 */
	public PlotItem(LogicalLayer layer, Point2D.Double location,
			int width, int height) {
		this(layer, location, width, height, "Plot Title", "X Axis", "Y Axis", null);
	}
	
	/**
	 * 
	 * @param layer the Layer this item is on.
	 * @param location the location of the lower-left in world coordinates
	 * @param width the width in pixels
	 * @param height the height in pixels
	 * @param title the plot title
	 * @param xLabel the x axis label
	 * @param yLabel the y axis label
	 * @param dataSet the splot DataSet
	 */
	public PlotItem(LogicalLayer layer, Point2D.Double location,
			int width, int height, 
			String title, String xLabel, String yLabel, DataSet dataSet) {
		super(layer, location, width, height);
		_plotCanvas = new PlotCanvas(dataSet, title, xLabel, yLabel);
		_plotPanel = new PlotPanel(_plotCanvas, PlotPanel.VERYBARE);
		_virtualPanel.addMainComponent(_plotPanel);
		
		
//		getContainer().getComponent().addMouseMotionListener(_plotCanvas);
//		getContainer().getComponent().addMouseListener(_plotCanvas);
	}

	/**
	 * Convenience method to create a histogram plot item
	 * @param layer the Layer this item is on.
	 * @param location the location of the lower-left in world coordinates
	 * @param width the width in pixels
	 * @param height the height in pixels
	 * @param title the plot title
	 * @param xLabel the x axis label
	 * @param yLabel the y axis label
	 * @param histoCurveName the curve name, i.e. for a legend
	 * @param minValue the min bin value
	 * @param maxValue the max bin value
	 * @param numBin the number of bins
	 * @return the plot item
	 */
	public static PlotItem createHistogram(LogicalLayer layer,
			Point2D.Double location, int width, int height, String title,
			String xLabel, String yLabel, String histoCurveName,
			double minValue, double maxValue, int numBin) {

		HistoData h1 = new HistoData(histoCurveName, minValue, maxValue,
				numBin);

		DataSet data;
		try {
			data = new DataSet(h1);
		} catch (DataSetException e) {
			e.printStackTrace();
			return null;
		}
		return new PlotItem(layer, location, width, height, title, xLabel,
				yLabel, data);
	}


	/**
	 * Get the underlying plot canvas
	 * @return the underlying plot canvas
	 */
	public PlotCanvas getPlotCanvas() {
		return _plotCanvas;
	}
	
	/**
	 * Get the plot parameters
	 * @return the plot parameters
	 */
	public PlotParameters getPlotParameters() {
		if (_plotCanvas == null) {
			return null;
		}
		return _plotCanvas.getParameters();
	}
}
