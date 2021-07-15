package cnuphys.splot.plot;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.Vector;

import cnuphys.splot.pdata.DataSet;

public class DataDrawer {

	// the owner canvas
	private PlotCanvas _plotCanvas;

	/**
	 * Create a DataDrawer
	 * 
	 * @param plotCanvas the owner canvas
	 */
	public DataDrawer(PlotCanvas plotCanvas) {
		_plotCanvas = plotCanvas;
	}

	/**
	 * Draw a data set on the canvas
	 * 
	 * @param g  the graphics context
	 * @param ds the DataSet to draw.
	 */
	public void draw(Graphics g, DataSet ds) {

		if ((ds == null) || ds.getSize() < 1) {
			return;
		}

		if (!(g.getClip().intersects(_plotCanvas.getActiveBounds()))) {
//			System.err.println("CLIP SKIP");
			return;
		}

		Rectangle clipRect = GraphicsUtilities.minClip(g.getClip(), _plotCanvas.getActiveBounds());
		if ((clipRect == null) || (clipRect.width == 0) || (clipRect.height == 0)) {
			return;
		}

		// save the clip, set clip to active area
		Shape oldClip = g.getClip();

		g.setClip(clipRect);

		// any fixed lines?
		Vector<PlotLine> lines = _plotCanvas.getParameters().getPlotLines();
		if (!lines.isEmpty()) {
			for (PlotLine line : lines) {
				line.draw(g);
			}
		}

		switch (ds.getType()) {
		case XYEXYE:
			for (int i = 0; i < ds.getColumnCount() / 3; i++) {
				int j = 3 * i;
				CurveDrawer.drawCurve(g, _plotCanvas, ds.getColumn(j), ds.getColumn(j + 1), null, ds.getColumn(j + 2));
			}
			break;

		case XYEEXYEE:
			for (int i = 0; i < ds.getColumnCount() / 4; i++) {
				int j = 4 * i;
				CurveDrawer.drawCurve(g, _plotCanvas, ds.getColumn(j), ds.getColumn(j + 1), ds.getColumn(j + 2),
						ds.getColumn(j + 3));
			}
			break;

		case H1D:
			for (int i = 0; i < ds.getColumnCount(); i++) {
				CurveDrawer.drawHisto1D(g, _plotCanvas, ds.getColumn(i));
			}
			break;

		case H2D:
			CurveDrawer.drawHisto2D(g, _plotCanvas, ds.getColumn(0));
			break;

		case XYY: // share an x column
			for (int i = 1; i < ds.getColumnCount(); i++) {
				CurveDrawer.drawCurve(g, _plotCanvas, ds.getColumn(0), ds.getColumn(i));
			}
			break;

		case XYXY:
			for (int i = 0; i < ds.getColumnCount() / 2; i++) {
				int j = 2 * i;
				CurveDrawer.drawCurve(g, _plotCanvas, ds.getColumn(j), ds.getColumn(j + 1));
			}
			break;

		case STRIP:
			CurveDrawer.drawCurve(g, _plotCanvas, ds.getColumn(0), ds.getColumn(1));
			break;

		case UNKNOWN:
			System.err.println("Cannot draw UNKNOWN data set type");
			break;
		}

		// restore the old clip
		g.setClip(oldClip);
	}

}
