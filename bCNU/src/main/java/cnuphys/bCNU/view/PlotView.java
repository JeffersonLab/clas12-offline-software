package cnuphys.bCNU.view;

import javax.swing.JMenuBar;

import cnuphys.bCNU.util.PropertySupport;
import cnuphys.splot.plot.PlotCanvas;
import cnuphys.splot.plot.PlotPanel;
import cnuphys.splot.plot.SplotMenus;

/**
 * This is a predefined view used to display a plot from splot
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class PlotView extends BaseView {

	// reserved view type for log view
	public static final int PLOTVIEWTYPE = -340555;

	// the owner canvas
	protected PlotCanvas _plotCanvas;

	// panel that holds the canvas
	protected PlotPanel _plotPanel;

	// all the menus and items
	protected SplotMenus _menus;

	public PlotView() {
		super(PropertySupport.TITLE, "sPlot", PropertySupport.ICONIFIABLE, true,
				PropertySupport.MAXIMIZABLE, true, PropertySupport.CLOSABLE, true,
				PropertySupport.RESIZABLE, true, PropertySupport.WIDTH, 800,
				PropertySupport.HEIGHT, 800, PropertySupport.VISIBLE, false,
				PropertySupport.VIEWTYPE, PLOTVIEWTYPE);
		add(createPlotPanel());
		addMenus();
	}

	// add the plot edit menus
	private void addMenus() {
		JMenuBar mbar = new JMenuBar();
		_menus = new SplotMenus(_plotCanvas, mbar, false);
		setJMenuBar(mbar);
	}

	// create the plot panel
	private PlotPanel createPlotPanel() {
		_plotCanvas = new PlotCanvas(null, "Empty Plot", "X Axis", "Y axis");
		_plotPanel = new PlotPanel(_plotCanvas);
		return _plotPanel;
	}

	/**
	 * Get the plot canvas
	 * 
	 * @return th plot canvas
	 */
	public PlotCanvas getPlotCanvas() {
		return _plotCanvas;
	}
}