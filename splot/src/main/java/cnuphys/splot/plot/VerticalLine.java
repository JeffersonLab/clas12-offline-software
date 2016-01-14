package cnuphys.splot.plot;

public class VerticalLine extends PlotLine {

	// the x value of the vertical line
	private double _x;

	public VerticalLine(PlotCanvas canvas, double x) {
		super(canvas);
		_x = x;
	}

	@Override
	public double getX0() {
		return _x;
	}

	@Override
	public double getX1() {
		return _x;
	}

	@Override
	public double getY0() {
		return _canvas.getWorld().getMinY();
	}

	@Override
	public double getY1() {
		return _canvas.getWorld().getMaxY();
	}

}
