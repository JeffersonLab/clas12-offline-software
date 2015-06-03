package cnuphys.splot.plot;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.stream.XMLStreamException;

import cnuphys.splot.edit.DataEditor;
import cnuphys.splot.edit.PlotPreferencesDialog;
import cnuphys.splot.pdata.DataColumn;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.DataSetType;
import cnuphys.splot.pdata.HistoData;
import cnuphys.splot.pdata.NiceScale;
import cnuphys.splot.rubberband.IRubberbanded;
import cnuphys.splot.rubberband.Rubberband;
import cnuphys.splot.toolbar.CommonToolBar;
import cnuphys.splot.toolbar.IToolBarListener;
import cnuphys.splot.toolbar.ToolBarButton;
import cnuphys.splot.toolbar.ToolBarToggleButton;
import cnuphys.splot.xml.XmlPrintStreamWritable;
import cnuphys.splot.xml.XmlPrintStreamWriter;

public class PlotCanvas extends JComponent implements MouseListener,
	MouseMotionListener, IRubberbanded, IToolBarListener,
	TableModelListener, XmlPrintStreamWritable {

    /** The XML root element name */
    public static final String XmlRootElementName = "sPlot";

    public static final String DONEDRAWINGPROP = "Done Drawing";
    public static final String TITLECHANGEPROP = "Plot Title Change";
    public static final String XLABELCHANGEPROP = "X Label Change";
    public static final String YLABELCHANGEPROP = "Y Label Change";
    public static final String DATACLEAREDPROP = "Data Cleared";

    // used to fire property changes. Transient.
    private long drawCount = 0;

    // default values for margins
    private int _topMargin = 10;
    private int _leftMargin = 30;
    private int _bottomMargin = 30;
    private int _rightMargin = 10;

    // for saving files
    private String _dataFilePath = Environment.getInstance().getHomeDirectory();

    // the bounds of the plot
    private Rectangle _activeBounds;

    // redraw check for dynamic data adding
    private boolean _needsRedraw;
    private boolean _needsRescale;

    // the world system of the active area
    private Rectangle2D.Double _worldSystem = new Rectangle2D.Double(0, 0, 1, 1);

    // convert from screen to data
    protected AffineTransform _localToWorld;
    protected AffineTransform _worldToLocal;

    // dataset being plotted
    protected DataSet _dataSet;

    // plot parameters
    protected PlotParameters _parameters;

    // work point
    private Point2D.Double _workPoint = new Point2D.Double();

    // plot ticks
    private PlotTicks _plotTicks;

    // if this has a parent (optional), print applies to the
    // parent. For example, the parent might be a PlotPanel
    private Component _parent;

    // legend and floating label dragging
    private Legend _legend;

    // data drawer
    private DataDrawer _dataDrawer;

    // for rubberbanding
    private Rubberband _rubberband;

    // gives xy of mouse in plot coordinates
    private String _locationString = " ";

    // toolbar controlling plot
    private CommonToolBar _toolbar;

    // popup menu
    private PlotPopupMenu _plotPopup;

    /**
     * Create a plot canvas for plotting a dataset
     * 
     * @param dataSet
     *            the dataset to plot. It might contain many curves
     * @param plotTitle
     *            the plot title
     * @param xLabel
     *            the x axis label
     * @param yLabel
     *            the y axis label
     */
    public PlotCanvas(DataSet dataSet, String plotTitle, String xLabel,
	    String yLabel) {

	_parameters = new PlotParameters(this);
	_parameters.setPlotTitle(plotTitle);
	_parameters.setXLabel(xLabel);
	_parameters.setYLabel(yLabel);
	_plotTicks = new PlotTicks(this);

	// default to xy plot no errors
	if (dataSet == null) {
	    try {
		dataSet = new DataSet(DataSetType.XYY, "X", "Y");
	    } catch (DataSetException e) {
		e.printStackTrace();
	    }
	}

	setDataSet(dataSet);

	ComponentAdapter componentAdapter = new ComponentAdapter() {
	    @Override
	    public void componentResized(ComponentEvent ce) {
		setAffineTransforms();
		repaint();
	    }
	};

	_legend = new Legend(this);
	_dataDrawer = new DataDrawer(this);
	_plotPopup = new PlotPopupMenu(this);
	setComponentPopupMenu(_plotPopup);

	dataSet.addTableModelListener(this);

	addComponentListener(componentAdapter);
	addMouseListener(this);
	addMouseMotionListener(this);

	// every canvas has a swing timer
	int delay = 1000; // milliseconds
	ActionListener taskPerformer = new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent evt) {
		if (_needsRedraw) {
		    if (_needsRescale) {
			setWorldSystem();
		    }
		    repaint();
		}
		_needsRescale = false;
		_needsRedraw = false;
	    }
	};
	new Timer(delay, taskPerformer).start();
    }

    /**
     * Get the plot title
     * 
     * @return the plot title
     */
    public String getTitle() {
	return _parameters.getPlotTitle();
    }

    /**
     * Get the plot parameters
     * 
     * @return the plot parameters
     */
    public PlotParameters getParameters() {
	return _parameters;
    }

    /**
     * Set the parent component, probably a PlotPanel
     * 
     * @param parent
     *            the optional parent component
     */
    public void setParent(Component parent) {
	_parent = parent;
    }

    /**
     * Set a new data set for the canvas
     * 
     * @param ds
     *            the new dataset
     */
    public void setDataSet(DataSet ds) {
	_dataSet = ds;
	if (_dataSet != null) {
	    _dataSet.notifyListeners();
	}
	setWorldSystem();
	repaint();
    }

    /**
     * Clear the plot of all data. It will have a null dataset.
     */
    public void clearPlot() {
	_dataSet = null;
	setWorldSystem();
	_parameters.setPlotTitle("No Plot");

	firePropertyChange(DATACLEAREDPROP, 0, 1);
	repaint();
    }

    /**
     * Get the underlying dataset
     * 
     * @return the underlying dataset
     */
    public DataSet getDataSet() {
	return _dataSet;
    }

    /**
     * Get the world boundary
     * 
     * @return the world boundary
     */
    public Rectangle.Double getWorld() {
	return _worldSystem;
    }

    /**
     * Set the world system based on the dataset
     */
    public void setWorldSystem() {

	if (_worldSystem == null) {
	    _worldSystem = new Rectangle2D.Double();
	}

	// watch for no dataset
	if (_dataSet == null) {
	    _worldSystem.setFrame(0, 0, 1, 1);
	    return;
	}

	double xmin = _dataSet.getXmin();
	if (Double.isNaN(xmin)) {
	    return;
	}

	double xmax = _dataSet.getXmax();
	double ymin = _dataSet.getYmin();
	double ymax = _dataSet.getYmax();

	PlotParameters params = getParameters();

	if (params.manualRangeX()) {
	    xmin = params.getManualXMin();
	    xmax = params.getManualXMax();
	} else if (_parameters.useXDataLimits()) {
	    // do nothing
	} else {
	    NiceScale ns = new NiceScale(xmin, xmax,
		    _plotTicks.getNumMajorTickX() + 2,
		    _parameters.includeXZero());
	    xmin = ns.getNiceMin();
	    xmax = ns.getNiceMax();
	}

	if (params.manualRangeY()) {
	    ymin = params.getManualYMin();
	    ymax = params.getManualYMax();
	} else if (_parameters.useYDataLimits()) {
	    // do nothing
	} else {
	    NiceScale ns = new NiceScale(ymin, ymax,
		    _plotTicks.getNumMajorTickY() + 2,
		    _parameters.includeYZero());
	    ymin = ns.getNiceMin();
	    ymax = ns.getNiceMax();
	}

	// try nice values

	_worldSystem.setFrame(xmin, ymin, xmax - xmin, ymax - ymin);
    }

    /**
     * Paint the canvas
     * 
     * @param g
     *            the graphics context
     */
    @Override
    public void paintComponent(Graphics g) {

	Rectangle b = getBounds();
	g.setColor(Color.white);
	g.fillRect(0, 0, b.width, b.height);

	// super.paintComponent(g);
	setAffineTransforms();

	// draw the data
	_dataDrawer.draw(g, _dataSet);

	// frame the active area
	g.setColor(Color.black);
	g.drawRect(_activeBounds.x, _activeBounds.y, _activeBounds.width,
		_activeBounds.height);

	// draw the ticks and legend
	_plotTicks.draw(g);

	if (_parameters.legendDrawing()) {
	    _legend.draw(g);
	}

	firePropertyChange(DONEDRAWINGPROP, drawCount, ++drawCount);

    }

    /**
     * Data is being added, possibly very quickly, so lets schedule a redraw
     * 
     * @param rescale
     *            if <code>true</code> the world system will also be rescaled
     */
    public void needsRedraw(boolean rescale) {
	_needsRedraw = true;
	_needsRescale = _needsRescale || rescale;
    }

    /**
     * Get the active plot area
     * 
     * @return the active plot area
     */
    public Rectangle getActiveBounds() {
	return _activeBounds;
    }

    // set the active bounds from the component bounds and the margins
    private void setActiveBounds() {
	Rectangle bounds = getBounds();
	if (bounds == null) {
	    _activeBounds = null;
	} else {
	    int left = 0;
	    int top = 0;
	    int right = left + bounds.width;
	    int bottom = top + bounds.height;

	    left += _leftMargin;
	    top += _topMargin;
	    right -= _rightMargin;
	    bottom -= _bottomMargin;

	    if (_activeBounds == null) {
		_activeBounds = new Rectangle();
	    }
	    _activeBounds.setBounds(left, top, right - left, bottom - top);
	}

    }

    // Get the transforms for world to local and vice versa
    protected void setAffineTransforms() {
	Rectangle bounds = getBounds();

	if ((bounds == null) || (bounds.width < 1) || (bounds.height < 1)) {
	    _localToWorld = null;
	    _worldToLocal = null;
	    _activeBounds = null;
	    return;
	}

	setActiveBounds();

	if (_worldSystem == null) {
	    return;
	}

	double scaleX = _worldSystem.width / _activeBounds.width;
	double scaleY = _worldSystem.height / _activeBounds.height;

	_localToWorld = AffineTransform.getTranslateInstance(_worldSystem.x,
		_worldSystem.getMaxY());
	_localToWorld.concatenate(AffineTransform.getScaleInstance(scaleX,
		-scaleY));
	_localToWorld.concatenate(AffineTransform.getTranslateInstance(
		-_activeBounds.x, -_activeBounds.y));

	try {
	    _worldToLocal = _localToWorld.createInverse();
	} catch (NoninvertibleTransformException e) {
	    // e.printStackTrace();
	}
    }

    /**
     * The mouse has been dragged over the plot canvas
     * 
     * @param e
     *            the mouseEvent
     */
    @Override
    public void mouseDragged(MouseEvent e) {
	if (_legend.isDraggingPrimed()) {
	    _legend.setDragging(true);
	}

	if (_legend.isDragging()) {
	    int dx = e.getX() - _legend.getCurrentPoint().x;
	    int dy = e.getY() - _legend.getCurrentPoint().y;
	    _legend.x += dx;
	    _legend.y += dy;
	    _legend.setCurrentPoint(e.getPoint());
	    repaint();
	}
    }

    /**
     * The mouse has moved over the plot canvas
     * 
     * @param e
     *            the mouseEvent
     */
    @Override
    public void mouseMoved(MouseEvent e) {
	if (_dataSet == null) {
	    return;
	}

	Point pp = e.getPoint();

	if ((_activeBounds == null) || (_worldSystem == null)) {
	    _locationString = " ";
	}

	else if (!activeBoundsContains(pp.x, pp.y)) {
	    _locationString = " ";
	} else {
	    // pp.x -= _activeBounds.x;
	    // pp.y -= _activeBounds.y;
	    localToWorld(pp, _workPoint);
	    _locationString = String.format("<html>(x, y) = (%7.2f, %-7.2f)",
		    _workPoint.x, _workPoint.y);

	    if (_dataSet.is1DHistoSet()) {
		Vector<DataColumn> ycols = (Vector<DataColumn>) _dataSet
			.getAllVisibleCurves();
		int size = ycols.size();

		for (int i = 0; i < size; i++) {
		    HistoData hd = ycols.get(i).getHistoData();
		    String s = HistoData.statusString(this, hd, pp, _workPoint);
		    if (s != null) {
			Color lc = ycols.get(i).getStyle().getLineColor();
			_locationString += "&nbsp&nbsp"
				+ colorStr(s, GraphicsUtilities.colorToHex(lc));
			// break;
		    }
		}
	    }

	}
    }

    private String colorStr(String s, String color) {
	return "<font color=" + color + ">" + s + "</font>";
    }

    // this method includes the border
    private boolean activeBoundsContains(int x, int y) {
	if (_activeBounds == null) {
	    return false;
	}
	int l = _activeBounds.x;
	if (x < l) {
	    return false;
	}

	int t = _activeBounds.y;
	if (y < t) {
	    return false;
	}

	int r = l + _activeBounds.width;
	if (x > r) {
	    return false;
	}

	int b = t + _activeBounds.height;
	if (y > b) {
	    return false;
	}

	return true;
    }

    /**
     * Get the last updated location string
     * 
     * @return
     */
    public String getLocationString() {
	return _locationString;
    }

    /**
     * The mouse has been clicked on the plot canvas
     * 
     * @param e
     *            the mouseEvent
     */
    @Override
    public void mouseClicked(MouseEvent e) {

	if (_rubberband != null) {
	    return;
	}
	if (_parameters.legendDrawing() && _legend.contains(e.getPoint())) {
	    return;
	}

	String command = toolbarCommand();

	if (CommonToolBar.CENTER.equals(command)) {
	    recenterAtClick(e.getPoint());
	} else if ((SwingUtilities.isLeftMouseButton(e) && e.isControlDown())) {
	    if (CommonToolBar.POINTER.equals(command)) {
		_plotPopup.show(e.getComponent(), e.getX(), e.getY());
	    }

	}
    }

    private boolean isPointer() {
	return (_toolbar == null) || _toolbar.isDefaultActivated();
    }

    /**
     * The mouse has been pressed on plot canvas
     * 
     * @param e
     *            the mouseEvent
     */
    @Override
    public void mousePressed(MouseEvent e) {

	String command = toolbarCommand();

	if (isPointer() && _parameters.legendDrawing()
		&& _legend.contains(e.getPoint())) {
	    _legend.setDraggingPrimed(true);
	    _legend.setCurrentPoint(e.getPoint());
	} else {

	    if (CommonToolBar.BOXZOOM.equals(command) && (_rubberband == null)) {
		if (getDataSet().is1DHistoSet()) {
		    _rubberband = new Rubberband(this, this,
			    Rubberband.Policy.XONLY);

		} else {
		    _rubberband = new Rubberband(this, this,
			    Rubberband.Policy.RECTANGLE);
		}
		_rubberband.setActive(true);
		_rubberband.startRubberbanding(e.getPoint());
	    }

	}

    }

    // get the active toolbar toggle butto command
    private String toolbarCommand() {
	return (_toolbar != null) ? _toolbar.getActiveCommand()
		: CommonToolBar.POINTER;
    }

    /**
     * The mouse has been released on plot canvas. A release comes before the
     * click
     * 
     * @param e
     *            the mouseEvent
     */
    @Override
    public void mouseReleased(MouseEvent e) {
	_legend.setDragging(false);
	_legend.setDraggingPrimed(false);
	_legend.setCurrentPoint(null);
    }

    /**
     * The mouse has entered the area of the plot canvas
     * 
     * @param e
     *            the mouseEvent
     */
    @Override
    public void mouseEntered(MouseEvent e) {

    }

    /**
     * The mouse has exited the area of the plot canvas
     * 
     * @param e
     *            the mouseEvent
     */
    @Override
    public void mouseExited(MouseEvent e) {
    }

    /**
     * This converts a screen or pixel point to a world point.
     * 
     * @param pp
     *            contains the local (screen-pixel) point.
     * @param wp
     *            will hold the resultant world point.
     */
    public void localToWorld(Point pp, Point.Double wp) {
	if (_localToWorld != null) {
	    _localToWorld.transform(pp, wp);
	}
    }

    /**
     * This converts a screen or pixel rectangle to a world rectangle.
     * 
     * @param pr
     *            contains the local (screen-pixel) rectangle.
     * @param wr
     *            will hold the resultant world rectangle.
     */
    public void localToWorld(Rectangle pr, Rectangle2D.Double wr) {
	if (_localToWorld != null) {
	    int l = pr.x;
	    int t = pr.y;
	    int r = l + pr.width;
	    int b = t + pr.height;
	    Point pplt = new Point(l, t);
	    Point pprb = new Point(r, b);
	    Point.Double wplt = new Point.Double();
	    Point.Double wprb = new Point.Double();
	    _localToWorld.transform(pplt, wplt);
	    _localToWorld.transform(pprb, wprb);
	    wr.x = Math.min(wplt.x, wprb.x);
	    wr.y = Math.min(wplt.y, wprb.y);
	    wr.width = Math.abs(wplt.x - wprb.x);
	    wr.height = Math.abs(wplt.y - wprb.y);
	}
    }

    /**
     * This converts a world point to a screen or pixel point.
     * 
     * @param pp
     *            will hold the resultant local (screen-pixel) point.
     * @param wp
     *            contains world point.
     */
    public void worldToLocal(Point pp, Point.Double wp) {
	if (_worldToLocal != null) {
	    _worldToLocal.transform(wp, pp);
	}
    }

    // /**
    // * The data set has changed, so we must redraw.
    // *
    // * @param dataset the dataset that changed.
    // */
    // @Override
    // public void dataSetChanged(DataSet dataSet) {
    // setWorldSystem();
    // repaint();
    // }

    @Override
    public void doneRubberbanding() {
	_toolbar.resetDefaultSelection();
	Rectangle rbrect = _rubberband.getRubberbandBounds();
	if ((rbrect.width < 15) || (rbrect.height < 15)) {
	    return;
	}

	if (getDataSet().is1DHistoSet()) {
	    // preserve full y
	    double y = _worldSystem.y;
	    double h = _worldSystem.height;
	    localToWorld(rbrect, _worldSystem);
	    _worldSystem.y = y;
	    _worldSystem.height = h;
	} else {
	    localToWorld(rbrect, _worldSystem);
	}
	_rubberband = null;
	repaint();
    }

    /**
     * Center the plot world at the click.
     * 
     * @param pp
     */
    public void recenterAtClick(Point pp) {
	Point.Double wp = new Point.Double();
	localToWorld(pp, wp);

	_worldSystem.x = wp.x - _worldSystem.width / 2;
	_worldSystem.y = wp.y - _worldSystem.height / 2;
	repaint();
    }

    /**
     * Scale the canvas by a given amount
     * 
     * @param amount
     *            the factor to scale by
     */
    public void scale(double amount) {
	double xc = _worldSystem.getCenterX();
	double w = _worldSystem.width * amount;
	double x = xc - w / 2;

	double h;
	double y;

	// if histo scale x only
	if ((getDataSet() != null) && (getDataSet().is1DHistoSet())) {
	    h = _worldSystem.height;
	    y = _worldSystem.y;
	} else {
	    h = _worldSystem.height * amount;
	    double yc = _worldSystem.getCenterY();
	    y = yc - h / 2;
	}
	_worldSystem.setFrame(x, y, w, h);
	repaint();
    }

    @Override
    public void buttonPressed(CommonToolBar toolbar, ToolBarButton button) {
	_toolbar = toolbar;
	String command = button.getActionCommand();

	if (CommonToolBar.ZOOMIN.equals(command)) {
	    scale(0.85);
	}
	if (CommonToolBar.ZOOMOUT.equals(command)) {
	    scale(1. / 0.85);
	}
	if (CommonToolBar.WORLD.equals(command)) {
	    setWorldSystem();
	    repaint();
	}
	if (CommonToolBar.PRINT.equals(command)) {
	    // PrintUtilities.printComponent(this);
	    PrintUtilities.printComponent((_parent != null) ? _parent : this);
	}

	if (CommonToolBar.PNG.equals(command)) {
	    takePicture();
	}
    }

    public File getSavePngFile() {
	FileNameExtensionFilter filter = new FileNameExtensionFilter(
		"PNG File", "png", "PNG");

	File selectedFile = null;
	JFileChooser chooser = new JFileChooser(_dataFilePath);
	chooser.setSelectedFile(null);
	chooser.setFileFilter(filter);
	int returnVal = chooser.showSaveDialog(this);
	if (returnVal == JFileChooser.APPROVE_OPTION) {
	    selectedFile = chooser.getSelectedFile();
	    if (selectedFile != null) {

		if (selectedFile.exists()) {
		    int answer = JOptionPane
			    .showConfirmDialog(
				    null,
				    selectedFile.getAbsolutePath()
					    + "  already exists. Do you want to overwrite it?",
				    "Overwite Existing File?",
				    JOptionPane.YES_NO_OPTION);

		    if (answer != JFileChooser.APPROVE_OPTION) {
			selectedFile = null;
		    }
		} // end file exists check
	    }
	}

	return selectedFile;
    }

    // take a picture, save as png
    private void takePicture() {
	try {

	    // try making a png
	    if (Environment.getInstance().getPngWriter() != null) {

		File file = getSavePngFile();
		if (file == null) {
		    return;
		}

		// Buffered image object to be written to depending on the view
		// type
		BufferedImage bi;

		ImageOutputStream ios = ImageIO.createImageOutputStream(file);
		Environment.getInstance().getPngWriter().setOutput(ios);

		bi = GraphicsUtilities
			.getComponentImage((_parent != null) ? _parent : this);

		Environment.getInstance().getPngWriter().write(bi);
		ios.close();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    @Override
    public void toggleButtonActivated(CommonToolBar toolbar,
	    ToolBarToggleButton button) {
	_toolbar = toolbar;
    }

    /**
     * Show the plot preferences dialog
     */
    public void showPreferencesEditor() {
	PlotPreferencesDialog prefEditor = new PlotPreferencesDialog(this);
	prefEditor.setVisible(true);
	prefEditor.toFront();
    }

    /**
     * Show the data editor
     */
    public void showDataEditor() {
	DataEditor dataEditor = new DataEditor(this);
	dataEditor.setVisible(true);
	dataEditor.toFront();
    }

    /**
     * Used so another object can tell the plot canvas to fire a propert change
     * event
     * 
     * @param propName
     * @param oldValue
     * @param newValue
     */
    public void remoteFirePropertyChange(String propName, Object oldValue,
	    Object newValue) {
	firePropertyChange(propName, oldValue, newValue);
    }

    @Override
    public void tableChanged(TableModelEvent e) {
	setWorldSystem();
	needsRedraw(false);
    }

    /**
     * This is called as a result of a save. The canvas needs to write itself
     * out in xml.
     * 
     * @param write
     *            the xml writer
     */
    @Override
    public void writeXml(XmlPrintStreamWriter writer) {
	try {
	    writer.writeStartElement(XmlRootElementName);
	    writeBasicData(writer);
	    _parameters.writeXml(writer);
	    _legend.writeXml(writer);
	    _plotTicks.writeXml(writer);
	    _dataSet.writeXml(writer);
	    writer.writeEndElement();
	} catch (XMLStreamException e) {
	    e.printStackTrace();
	}
    }

    // write a little basic data
    private void writeBasicData(XmlPrintStreamWriter writer)
	    throws XMLStreamException {
    }

}
