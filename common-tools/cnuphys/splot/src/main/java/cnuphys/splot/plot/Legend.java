package cnuphys.splot.plot;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.Collection;
import cnuphys.splot.fit.Fit;
import cnuphys.splot.fit.FitType;
import cnuphys.splot.pdata.DataColumn;
import cnuphys.splot.pdata.DataColumnType;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.style.Styled;
import cnuphys.splot.style.SymbolDraw;
import cnuphys.splot.style.SymbolType;

public class Legend extends DraggableRectangle {

	// the owner plot panel
	private PlotCanvas _canvas;

	// the plot parameters
	private PlotParameters _params;

	// gap before text
	private final int HGAP = 8;

	// extra v gap
	private final int VGAP = 6;

	private int _maxStringWidth;
	private int _extra;

	private int _numVisCurves;

	/**
	 * Create a Legend rectangle
	 * 
	 * @param canvas the parent plot canvas
	 */
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
		// System.err.println(toString());
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
			if (!dc.isHistogram1D()) {
				return;
			}
		}

		width = getLegendWidth();
		height = getLegendHeight();

		g.setColor(_params.getTextBackground());
		g.fillRect(x, y, width, height);

		if (_params.isLegendBorder()) {
			g.setColor(_params.getTextBorderColor());
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
		FontMetrics fm = _canvas.getFontMetrics(_params.getTextFont());
		g.setFont(_params.getTextFont());
		g.setColor(_params.getTextForeground());

		Styled style = curve.getStyle();
		Fit fit = curve.getFit();
		int space = spaceNeeded(curve);
		int yc = y + space / 2;

		String legStr = curve.getName();
		if (legStr == null) {
			legStr = "";
		}

		if (curve.isHistogram1D()) {
			legStr += (" " + curve.getHistoData().statStr());
		}

		g.drawString(legStr, x + width - _maxStringWidth - HGAP, yc + fm.getHeight() / 2);

		if ((_numVisCurves > 1) && fit.getFit() != FitType.NOLINE) {
			GraphicsUtilities.drawStyleLine(g, style.getFitLineColor(), style.getFitLineWidth(),
					style.getFitLineStyle(), x + HGAP, yc, x + HGAP + _params.getLegendLineLength(), yc);
		}

		SymbolDraw.drawSymbol(g, x + HGAP + _extra / 2, yc, curve.getStyle());

		return y + space + VGAP;
	}

	// get required width of the legend
	private int getLegendWidth() {
		FontMetrics fm = _canvas.getFontMetrics(_params.getTextFont());
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
				if (dc.isHistogram1D()) {
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

	// get the vertical space needed for a curve
	private int spaceNeeded(DataColumn curve) {
		FontMetrics fm = _canvas.getFontMetrics(_params.getTextFont());
		return Math.max(fm.getHeight(), curve.getStyle().getSymbolSize());
	}

}
