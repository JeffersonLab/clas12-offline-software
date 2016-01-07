package cnuphys.ced.cedview.bst;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.graphics.container.BaseContainer;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.util.Fonts;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.data.ColumnData;
import cnuphys.ced.event.data.DataSupport;
import cnuphys.ced.geometry.BSTGeometry;
import cnuphys.ced.geometry.BSTxyPanel;

public class BSTxyMagDrawer extends DrawableAdapter {

	// the owner view
	private final BSTxyView _view;

	private static Font _font = Fonts.smallFont;

	private double _xmin, _xmax, _zmin, _zmax, _width, _height;

	private Rectangle2D.Double _layerRect = new Rectangle2D.Double();

	public BSTxyMagDrawer(BSTxyView view) {
		_view = view;
	}

	@Override
	public void draw(Graphics g, IContainer container) {

		if (!_view.isSingleEventMode()) {
			return;
		}

		BSTxyPanel closestPanel = _view.closestPanel();

		if (closestPanel == null) {
			return;
		}

		int sector = closestPanel.getSector(); // 1..6
		int layer = closestPanel.getLayer(); // 1..8
		int supl = (layer - 1) / 2; // 0..3

		float coords[] = new float[6];

		setWorld(container, closestPanel);

		Rectangle b = container.getComponent().getBounds();
		Rectangle r = new Rectangle();

		container.worldToLocal(r, _layerRect);

		// System.err.println("r: " + r );
		// System.err.println("b: " + b+ "\n");

		g.setColor(Color.black);
		g.fillRect(b.x, b.y, b.width, b.height);
		g.setColor(Color.red);
		g.fillRect(r.x - 1, r.y - 1, r.width + 2, r.height + 2);
		g.setColor(Color.white);
		g.fillRect(r.x, r.y, r.width, r.height);

		// draw the upper layer strips
		// Point2D.Double wp0 = new Point2D.Double();
		// Point2D.Double wp1 = new Point2D.Double();
		// Point pp0 = new Point();
		// Point pp1 = new Point();
		// g.setColor(Color.blue);
		// for (int strip = 0; strip < 256; strip = strip + 16) {
		// //always get coords for sector 1
		// BSTGeometry.getStrip(1, layer, strip, coords);
		// wp0.setLocation(10*coords[2], 10*coords[0]);
		// wp1.setLocation(10*coords[5], 10*coords[3]);
		// container.worldToLocal(pp0, wp0);
		// container.worldToLocal(pp1, wp1);
		//
		// g.drawLine(pp0.x, pp0.y, pp1.x, pp1.y);
		// }

		g.setFont(_font);
		g.setColor(Color.cyan);
		FontMetrics fm = container.getComponent().getFontMetrics(_font);
		int y = fm.getHeight() + 2;
		int laylow = supl * 2 + 1;
		int layhi = laylow + 1;
		g.drawString(
				"Sector: " + sector + "  Layers: " + laylow + " and " + layhi,
				4, y);
		y += fm.getHeight();
		g.drawString("hits (layer, strip): ", 4, y);
		y += fm.getHeight();
		drawBSTHitsSingleMode(g, container, sector, laylow, layhi, y);

	}

	private void setWorld(IContainer container, BSTxyPanel closestPanel) {
		// get the limits to create an appropriate world system
		// use sector 1 (which has constant y) limits
		double vals[] = new double[10];
		int supl = (closestPanel.getLayer() - 1) / 2; // [0,1,2,3]
		int layer = (closestPanel.getLayer() - 1) % 2; // [0, 1]
		BSTGeometry.getLimitValues(0, supl, layer, vals);

		// values are in mm
		_xmax = Math.max(Math.abs(vals[0]), Math.abs(vals[2]));
		_xmin = -_xmax;
		_zmin = Double.POSITIVE_INFINITY;
		_zmax = Double.NEGATIVE_INFINITY;
		for (int i = 4; i < 10; i++) {
			_zmin = Math.min(_zmin, vals[i]);
			_zmax = Math.max(_zmax, vals[i]);
		}
		// System.err.println("Supl, Lay: " + supl + ", " + layer);
		// System.err.println("xmin, xmax = " + _xmin + ", " + _xmax + " mm");
		// System.err.println("zmin, zmax = " + _zmin + ", " + _zmax + " mm");

		// x will be the vertical, z the horizontal
		double xc = (_xmin + _xmax) / 2;
		double zc = (_zmin + _zmax) / 2;

		_width = _zmax - _zmin;

		// distort in x
		_height = 2 * (_xmax - _xmin);
		double size = 1.05 * _width;
		double s2 = size / 2;

		Rectangle.Double wr = new Rectangle.Double(zc - s2, xc - s2, size,
				size);
		_layerRect.setFrame(zc - _width / 2, xc - _height / 2, _width, _height);

		// System.err.println("wr: " + wr);
		// System.err.println("lr: " + _layerRect);
		// System.err.println("view: " + container.getView());

		((BaseContainer) container).setWorldSystem(wr);
		container.setDirty(true);
	}

	private void drawBSTHitsSingleMode(Graphics g, IContainer container,
			int sector, int laylow, int layhi, int y) {
		ClasIoEventManager eventManager = ClasIoEventManager.getInstance();

		String hitString = "";

		int hitCount = DataSupport.bstGetHitCount();
		if (hitCount > 0) {
			int bstsector[] = ColumnData.getIntArray("BST::dgtz.sector");
			int bstlayer[] = ColumnData.getIntArray("BST::dgtz.layer");
			int bststrip[] = ColumnData.getIntArray("BST::dgtz.strip");

			float coords[] = new float[6];

			Point2D.Double wp0 = new Point2D.Double();
			Point2D.Double wp1 = new Point2D.Double();
			Point pp0 = new Point();
			Point pp1 = new Point();
			g.setColor(Color.blue);

			for (int hitIndex = 0; hitIndex < hitCount; hitIndex++) {
				if (bstsector[hitIndex] == sector) {
					int layer = bstlayer[hitIndex];
					if ((layer == laylow) || (layer == layhi)) {
						int strip = bststrip[hitIndex];
						hitString += "(" + layer + "," + strip + ") ";

						// //always get coords for sector 1
						BSTGeometry.getStrip(1, layer, strip, coords);
						wp0.setLocation(10 * coords[2], 10 * coords[0]);
						wp1.setLocation(10 * coords[5], 10 * coords[3]);
						container.worldToLocal(pp0, wp0);
						container.worldToLocal(pp1, wp1);

						if (layer == laylow) {
							g.setColor(Color.red);
						}
						else {
							g.setColor(Color.blue);
						}

						g.drawLine(pp0.x, pp0.y, pp1.x, pp1.y);
					}

				}
			}

		}

		g.setColor(Color.cyan);
		g.drawString(hitString, 4, y);

		// crosses?
		double labx[] = ColumnData.getDoubleArray("BSTRec::Crosses.x");
		if (labx != null) {
			double laby[] = ColumnData.getDoubleArray("BSTRec::Crosses.y");
			double labz[] = ColumnData.getDoubleArray("BSTRec::Crosses.z");
			// bstData.bstrec
			// for (int i = 0; i < labx.length; i++) {
			// Point3D p3d = new Point3D(labx[i], laby[i], labz[i]);
			// Point3D rotPnt = BSTGeometry.inverseRotate(sector, superlayer,
			// layer, p3d);
			// }

		}
	}
}
