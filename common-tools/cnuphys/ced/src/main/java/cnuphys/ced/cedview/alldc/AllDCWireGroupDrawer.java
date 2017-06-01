package cnuphys.ced.cedview.alldc;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.BitSet;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.ced.cedview.AWireGroupDrawer;
import cnuphys.ced.item.AllDCSuperLayer;

public abstract class AllDCWireGroupDrawer extends AWireGroupDrawer {

	private static final int LEFTBOTTOM = 0;
	private static final int LEFTTOP = 1;
	private static final int RIGHTTOP = 2;
	private static final int RIGHTBOTTOM = 3;

	// drawing level
	public static enum DrawLevel {
		SUPERLAYER, REGION, SECTOR
	}

	private final int SLOP = 2;

	// the view parent
	protected AllDCView _view;

	public AllDCWireGroupDrawer(AllDCView view) {
		_view = view;
	}

	@Override
	public abstract void draw(Graphics g, IContainer container);

	/**
	 * draw the hull
	 * 
	 * @param g
	 *            the graphics context
	 * @param container
	 *            the rendering container
	 * @param sector
	 *            parallel array of sector ids
	 * @param superlayer
	 *            parallel array of superlayer ids
	 * @param layer
	 *            parallel array of layer ids
	 * @param wire
	 *            parallel array of wire ids
	 * @param id
	 *            parallel ray of object (e.g., segment or track candidate) idsw
	 * @param fillcolor
	 *            the fill color
	 * @param drawLevel
	 *            the drawing level
	 */
	protected void drawHull(Graphics g, IContainer container, int sector[],
			int superlayer[], int layer[], int wire[], int id[],
			Color fillcolor, DrawLevel drawLevel) {

		int len = id.length;

		// how many unique ids?
		BitSet ids = new BitSet(256);
		for (int i = 0; i < len; i++) {
			ids.set(id[i]);
		}

		Graphics2D g2 = (Graphics2D) g;
		Stroke oldStroke = g2.getStroke();

		// loop over object (e.g., segment id)
		for (int i = ids.nextSetBit(0); i >= 0; i = ids.nextSetBit(i + 1)) {
			Polygon poly[][] = getSuperLayerGroupOutlines(container, sector,
					superlayer, layer, wire, id, i);

			switch (drawLevel) {
			case SUPERLAYER:

				// draw all non-null polygons
				for (int sect = 0; sect < 6; sect++) {
					for (int supl = 0; supl < 6; supl++) {
						if ((poly[sect][supl] != null)
								&& (poly[sect][supl].npoints > 3)) {

							Path2D.Double path = new Path2D.Double(
									poly[sect][supl]);
							Path2D hullPath = WorldGraphicsUtilities
									.getConvexHull(path);
							drawShape(g2, fillcolor, hullPath);

							drawShape(g2, fillcolor, poly[sect][supl]);
						}
					}
				} // end sector loop

				break;

			case REGION:
				for (int sect = 0; sect < 6; sect++) {
					for (int region = 0; region < 3; region++) {
						Path2D.Double path = new Path2D.Double();
						int supla = 2 * region;
						int suplb = supla + 1;
						if ((poly[sect][supla] != null)
								&& (poly[sect][supla].npoints > 3)) {
							path.append(poly[sect][supla], false);
						}
						if ((poly[sect][suplb] != null)
								&& (poly[sect][suplb].npoints > 3)) {
							path.append(poly[sect][suplb], false);
						}
						Path2D hullPath = WorldGraphicsUtilities
								.getConvexHull(path);
						drawShape(g2, fillcolor, hullPath);
					}
				}
				break;

			case SECTOR:
				for (int sect = 0; sect < 6; sect++) {
					Path2D.Double path = new Path2D.Double();

					for (int supl = 0; supl < 6; supl++) {
						if ((poly[sect][supl] != null)
								&& (poly[sect][supl].npoints > 3)) {
							path.append(poly[sect][supl], false);
						}
					}

					Path2D hullPath = WorldGraphicsUtilities
							.getConvexHull(path);
					drawShape(g2, fillcolor, hullPath);
				}

				break;
			}

		}

		// restore old stroke
		g2.setStroke(oldStroke);
	}

	// draw a shape
	private void drawShape(Graphics2D g2, Color fillColor, Shape shape) {
		g2.setColor(fillColor);
		g2.fill(shape);
		g2.setStroke(GraphicsUtilities.getStroke(1, LineStyle.SOLID));
		g2.setColor(Color.black);
		g2.draw(shape);
		g2.setStroke(GraphicsUtilities.getStroke(1, LineStyle.DOT));
		g2.setColor(Color.yellow);
		g2.draw(shape);
	}

	/**
	 * Gets the wire groupings for drawing, effectively, a convex hull;
	 * 
	 * @param container
	 *            the rendering container
	 * @param sector
	 *            parallel array of 1-based sector ids
	 * @param superlayer
	 *            parallel array of 1-based superlayer ids
	 * @param layer
	 *            parallel array of 1-based layer ids
	 * @param wire
	 *            parallel array of 1-based wire ids
	 * @param id
	 *            parallel array of object (e.g. segment) ids
	 * @param match
	 *            the index (e.g., segment id) to draw
	 * @return an array of polygons indexed by sector and superlayer, any of
	 *         which might be null.
	 */
	protected Polygon[][] getSuperLayerGroupOutlines(IContainer container,
			int sector[], int superlayer[], int layer[], int wire[], int id[],
			int match) {

		// index for poly corresponds to sector and superlayer
		Polygon[][] poly = new Polygon[6][6];

		// wire index corresponds to sector superlayer layer
		int[][][] maxwire = new int[6][6][6];
		int[][][] minwire = new int[6][6][6];

		for (int sect = 0; sect < 6; sect++) {
			for (int supl = 0; supl < 6; supl++) {
				poly[sect][supl] = null;
				for (int lay = 0; lay < 6; lay++) {
					minwire[sect][supl][lay] = 1000;
					maxwire[sect][supl][lay] = -1000;
				}
			}
		}

		int len = sector.length;
		for (int index = 0; index < len; index++) {

			// does the object (e.g., segment) match?
			if (id[index] == match) {
				int wid = wire[index];

				int sindex = sector[index] - 1;
				int slindex = superlayer[index] - 1;
				int lindex = layer[index] - 1;

				if (maxwire[sindex][slindex][lindex] < wid) {
					maxwire[sindex][slindex][lindex] = wid;
				}
				if (minwire[sindex][slindex][lindex] > wid) {
					minwire[sindex][slindex][lindex] = wid;
				}
			}
		}

		Point pp = new Point();
		Rectangle2D.Double wr = new Rectangle2D.Double();
		Rectangle pr = new Rectangle();

		for (int sect = 0; sect < 6; sect++) {
			for (int supl = 0; supl < 6; supl++) {
				AllDCSuperLayer superLayerItem;

				if (sect < 3) {
					superLayerItem = _view.getAllDCSuperLayer(sect, supl);
					// get the lefts from the max
					for (int lay = 0; lay < 6; lay++) {

						int maxw = maxwire[sect][supl][lay];
						if (maxw > 0) {
							if (poly[sect][supl] == null) {
								poly[sect][supl] = new Polygon();
							}

							superLayerItem.getCell(lay + 1, maxw, wr);
							container.worldToLocal(pr, wr);
							getCorner(pr, LEFTBOTTOM, pp);
							poly[sect][supl].addPoint(pp.x - SLOP, pp.y);
							getCorner(pr, LEFTTOP, pp);
							poly[sect][supl].addPoint(pp.x - SLOP, pp.y);
						}
					}

					// get the rights from the min
					for (int lay = 5; lay >= 0; lay--) {

						int minw = minwire[sect][supl][lay];
						if (minw < 500) {
							superLayerItem.getCell(lay + 1, minw, wr);
							container.worldToLocal(pr, wr);
							getCorner(pr, RIGHTTOP, pp);
							poly[sect][supl].addPoint(pp.x + SLOP + 1, pp.y);
							getCorner(pr, RIGHTBOTTOM, pp);
							poly[sect][supl].addPoint(pp.x + SLOP + 1, pp.y);
						}
					}
				} else {
					// inversion for lower sectors
					superLayerItem = _view.getAllDCSuperLayer(sect, 5 - supl);
					// get the lefts from the max
					for (int lay = 0; lay < 6; lay++) {

						int maxw = maxwire[sect][supl][lay];
						if (maxw > 0) {
							if (poly[sect][supl] == null) {
								poly[sect][supl] = new Polygon();
							}

							superLayerItem.getCell(lay + 1, maxw, wr);
							container.worldToLocal(pr, wr);
							getCorner(pr, LEFTTOP, pp);
							poly[sect][supl].addPoint(pp.x - SLOP, pp.y);
							getCorner(pr, LEFTBOTTOM, pp);
							poly[sect][supl].addPoint(pp.x - SLOP, pp.y);
						}
					}

					// get the rights from the min
					for (int lay = 5; lay >= 0; lay--) {

						int minw = minwire[sect][supl][lay];
						if (minw < 500) {
							superLayerItem.getCell(lay + 1, minw, wr);
							container.worldToLocal(pr, wr);
							getCorner(pr, RIGHTBOTTOM, pp);
							poly[sect][supl].addPoint(pp.x + SLOP + 1, pp.y);
							getCorner(pr, RIGHTTOP, pp);
							poly[sect][supl].addPoint(pp.x + SLOP + 1, pp.y);
						}
					}
				}

			}
		}

		return poly;
	}

	// get the corner of a rectangle
	private void getCorner(Rectangle r, int corner, Point pp) {
		switch (corner) {
		case LEFTBOTTOM:
			pp.setLocation(r.x, r.y + r.height);
			break;
		case LEFTTOP:
			pp.setLocation(r.x, r.y);
			break;
		case RIGHTTOP:
			pp.setLocation(r.x + r.width, r.y);
			break;
		case RIGHTBOTTOM:
			pp.setLocation(r.x + r.width, r.y + r.height);
			break;
		}
	}

}
