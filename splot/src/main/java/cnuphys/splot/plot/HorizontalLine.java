package cnuphys.splot.plot;

public class HorizontalLine extends PlotLine {

	// the y value of the horizontal line
	private double _y;

	public HorizontalLine(PlotCanvas canvas, double y) {
		super(canvas);
		_y = y;
	}

	@Override
	public double getX0() {
		return _canvas.getWorld().getMinX();
	}

	@Override
	public double getX1() {
		return _canvas.getWorld().getMaxX();
	}

	@Override
	public double getY0() {
		return _y;
	}

	@Override
	public double getY1() {
		return _y;
	}

}
