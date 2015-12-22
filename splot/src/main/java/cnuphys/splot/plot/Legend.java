package cnuphys.splot.plot;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.Properties;

import javax.xml.stream.XMLStreamException;

import cnuphys.splot.fit.Fit;
import cnuphys.splot.fit.FitType;
import cnuphys.splot.pdata.DataColumn;
import cnuphys.splot.pdata.DataColumnType;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.style.Styled;
import cnuphys.splot.style.SymbolDraw;
import cnuphys.splot.style.SymbolType;
import cnuphys.splot.xml.XmlPrintStreamWritable;
import cnuphys.splot.xml.XmlPrintStreamWriter;
import cnuphys.splot.xml.XmlSupport;

public class Legend extends Rectangle
		implements XmlPrintStreamWritable, Draggable {

	/** The XML root element name */
	public static final String XmlRootElementName = "Legend";

	public static final String XmlLegendParamsElementName = "LegendParameters";
	public static final String XmlHgapAttName = "hgap";
	public static final String XmlVgapAttName = "vgap";
	public static final String XmlLegendBoundsElementName = "LegendBounds";

	// the owner plot panel
	private PlotCanvas _canvas;

	// the plot parameters
	private PlotParameters _params;

	// gap before text
	private final int HGAP = 10;

	// extra v gap
	private final int VGAP = 8;

	private int _maxStringWidth;
	private int _extra;

	private int _numVisCurves;

	public Legend(PlotCanvas canvas) {
		_canvas = canvas;
		_params = canvas.getParameters();
	}

	/**
	 * Draw the legend
	 * 
	 * @param g the graphics context
	 */
	public void draw(Graphics g) {
		DataSet ds = _canvas.getDataSet();

		if (ds == null) {
			return;
		}

		if (ds.getAllVisibleCurves().isEmpty()) {
			return;
		}

		_numVisCurves = ds.getAllVisibleCurves().size();

		if (_numVisCurves == 1) {
			DataColumn dc = ds.getCurve(0);
			if (!dc.isHistogram()) {
				return;
			}
		}

		width = getLegendWidth();
		height = getLegendHeight();

		g.setColor(_params.getLegendBackground());
		g.fillRect(x, y, width, height);

		if (_params.isLegendBorder()) {
			g.setColor(_params.getLegendBorderColor());
			g.drawRect(x, y, width, height);
		}

		int yi = y + VGAP;

		Collection<DataColumn> ycols = ds.getAllColumnsByType(DataColumnType.Y);
		for (DataColumn curve : ycols) {
			if (curve.isVisible()) {
				yi = drawColumnLegendInfo(g, yi, curve);
			}
		}
	}

	// draw the info for the given y column
	private int drawColumnLegendInfo(Graphics g, int y, DataColumn curve) {
		FontMetrics fm = _canvas.getFontMetrics(_params.getLegendFont());
		g.setFont(_params.getLegendFont());
		g.setColor(_params.getLegendForeground());

		Styled style = curve.getStyle();
		Fit fit = curve.getFit();
		int space = spaceNeeded(curve);
		int yc = y + space / 2;

		String legStr = curve.getName();
		if (legStr == null) {
			legStr = "";
		}

		if (curve.isHistogram()) {
			legStr += (" " + curve.getHistoData().statStr());
		}

		g.drawString(legStr, x + width - _maxStringWidth - HGAP,
				yc + fm.getHeight() / 2);

		if ((_numVisCurves > 1) && fit.getFit() != FitType.NOLINE) {
			GraphicsUtilities.drawStyleLine(g, style, x + HGAP, yc,
					x + HGAP + _params.getLegendLineLength(), yc);
		}

		SymbolDraw.drawSymbol(g, x + HGAP + _extra / 2, yc, curve.getStyle());

		return y + space + VGAP;
	}

	// get required width of the legend
	private int getLegendWidth() {
		FontMetrics fm = _canvas.getFontMetrics(_params.getLegendFont());
		DataSet ds = _canvas.getDataSet();

		_maxStringWidth = 0;
		_extra = 0;

		Collection<DataColumn> ycols = ds.getAllColumnsByType(DataColumnType.Y);
		for (DataColumn dc : ycols) {
			if (dc.isVisible()) {

				Fit fit = dc.getFit();
				if ((_numVisCurves > 1) && fit.getFitType() != FitType.NOLINE) {
					_extra = _params.getLegendLineLength();
				}
				else {
					Styled style = dc.getStyle();
					if (style.getSymbolType() != SymbolType.NOSYMBOL) {
						_extra = Math.max(_extra, style.getSymbolSize());
					}
				}

				String legStr = dc.getName();
				if (dc.isHistogram()) {
					legStr += (" " + dc.getHistoData().statStr());
				}

				int sw = fm.stringWidth(legStr);

				_maxStringWidth = Math.max(_maxStringWidth, sw);
			}

		}

		return _extra + _maxStringWidth + 3 * HGAP;
	}

	// get required height of the legend
	private int getLegendHeight() {
		DataSet ds = _canvas.getDataSet();

		int height = VGAP;
		Collection<DataColumn> ycols = ds.getAllColumnsByType(DataColumnType.Y);
		for (DataColumn curve : ycols) {
			if (curve.isVisible()) {
				height += (VGAP + spaceNeeded(curve));
			}
		}

		return height;
	}

	/**
	 * Set whether or not the border should be drawn.
	 * 
	 * @param drawBorder the new flag
	 */
	public void setDrawBorder(boolean drawBorder) {
		if (_params.isLegendBorder() != drawBorder) {
			_params.setLegendBorder(drawBorder);
			_canvas.repaint();
		}
	}

	// get the vertical space needed for a curve
	private int spaceNeeded(DataColumn curve) {
		FontMetrics fm = _canvas.getFontMetrics(_params.getLegendFont());
		return Math.max(fm.getHeight(), curve.getStyle().getSymbolSize());
	}

	/**
	 * This is called as a result of a save. The Legend needs to write itself
	 * out in xml.
	 * 
	 * @param write the xml writer
	 */
	@Override
	public void writeXml(XmlPrintStreamWriter writer) {
		try {
			writer.writeStartElement(XmlRootElementName);
			writeLegendProperties(writer);
			writeLegendBounds(writer);
			writer.writeEndElement();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	// write some basic legend parameters
	private void writeLegendProperties(XmlPrintStreamWriter writer)
			throws XMLStreamException {
		Properties props = new Properties();
		props.put(XmlHgapAttName, HGAP);
		props.put(XmlVgapAttName, VGAP);
		writer.writeElementWithProps(XmlLegendParamsElementName, props);
	}

	// write bounds info
	private void writeLegendBounds(XmlPrintStreamWriter writer)
			throws XMLStreamException {
		Properties props = new Properties();
		XmlSupport.addRectangleAttribute(props, this);
		writer.writeElementWithProps(XmlLegendBoundsElementName, props);
	}

	// are we being dragged
	private boolean _dragging;

	// is dragging primed
	private boolean _draggingPrimed;

	// current point
	private Point _currentPoint;

	@Override
	public boolean isDraggingPrimed() {
		return _draggingPrimed;
	}

	@Override
	public boolean isDragging() {
		return _dragging;
	}

	@Override
	public void setDraggingPrimed(boolean primed) {
		_draggingPrimed = primed;
	}

	@Override
	public void setDragging(boolean dragging) {
		_dragging = dragging;
	}

	@Override
	public void setCurrentPoint(Point p) {
		if (p == null) {
			_currentPoint = null;
		}
		else {
			_currentPoint = new Point(p);
		}
	}

	@Override
	public Point getCurrentPoint() {
		return _currentPoint;
	}

}
