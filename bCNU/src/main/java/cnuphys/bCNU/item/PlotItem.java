package cnuphys.bCNU.item;

import java.awt.geom.Point2D;

import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.splot.plot.PlotCanvas;
import cnuphys.splot.plot.PlotPanel;

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
		super(layer, location, width, height);
		_plotCanvas = new PlotCanvas(null, "Plot Title", "X Axis", "Y Axis");
		_plotPanel = new PlotPanel(_plotCanvas);
		_virtualPanel.addMainComponent(_plotPanel);
	}

}
