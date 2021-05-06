package cnuphys.splot.plot;

import java.awt.Graphics;
import java.awt.Point;

import cnuphys.splot.style.Styled;

/**
 * Just a straight line to be drawn on the plot, e.g. a y = 0 line
 * 
 * @author heddle
 *
 */
public abstract class PlotLine {

//	public Styled(Color fillColor, Color borderColor, Color fitLineColor, Color auxLineColor, LineStyle fitLineStyle,
//			LineStyle auxLineStyle, float fitLineWidth, float auxLineWidth, SymbolType symbolType, int symbolSize) {

	// default style
	protected static Styled _defaultStyle = new Styled();

	// the plot canvas
	protected PlotCanvas _canvas;

	// init style to default
	protected Styled _style = _defaultStyle;

	// work points
	protected Point p0 = new Point();
	protected Point p1 = new Point();
	protected Point.Double wp = new Point.Double();

	public PlotLine(PlotCanvas canvas) {
		_canvas = canvas;
	}

	/**
	 * Set the line drawing style
	 * 
	 * @param style the new style
	 */
	public void setStyle(Styled style) {
		_style = style;
	}

	/**
	 * Get the line style
	 * 
	 * @return the line style
	 */
	public Styled getStyle() {
		return _style;
	}

	/**
	 * Draw the line
	 * 
	 * @param g the graphis context
	 */
	public void draw(Graphics g) {
		wp.setLocation(getX0(), getY0());
		_canvas.worldToLocal(p0, wp);
		wp.setLocation(getX1(), getY1());
		_canvas.worldToLocal(p1, wp);

		GraphicsUtilities.drawStyleLine(g, _style.getAuxLineColor(), _style.getAuxLineWidth(), _style.getAuxLineStyle(),
				p0.x, p0.y, p1.x, p1.y);
	}

	public abstract double getX0();

	public abstract double getX1();

	public abstract double getY0();

	public abstract double getY1();

}
