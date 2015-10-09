package cnuphys.bCNU.item;

import java.awt.Graphics;
import java.awt.geom.Point2D;

import cnuphys.bCNU.attributes.AttributeType;
import cnuphys.bCNU.attributes.Attributes;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.layer.LogicalLayer;

public class PolylineItem extends PathBasedItem {

	/**
	 * Create a world polyline item
	 * 
	 * @param layer
	 *            the Layer this item is on.
	 * @param points
	 *            the points of the polygon
	 */
	public PolylineItem(LogicalLayer layer, Point2D.Double points[]) {
		super(layer);

		// get the path
		_path = WorldGraphicsUtilities.worldPolygonToPath(points);
		_focus = WorldGraphicsUtilities.getCentroid(_path);

		_style.setFillColor(null);
	}

	/**
	 * Custom drawer for the item.
	 * 
	 * @param g
	 *            the graphics context.
	 * @param container
	 *            the graphical container being rendered.
	 */
	@Override
	public void drawItem(Graphics g, IContainer container) {
		// TODO use dirty

		_lastDrawnPolygon = WorldGraphicsUtilities.drawPath2D(g, container,
				_path, _style, false);

	}

	/**
	 * Reshape the polygon based on the modification. Not much we can do to a
	 * polygon except move the selected point. Keep in mind that if control or
	 * shift was pressed, the polygon will scale rather than coming here.
	 */
	@Override
	protected void reshape() {
		int index = _modification.getSelectIndex();
		Point2D.Double[] wpoly = WorldGraphicsUtilities
				.pathToWorldPolygon(_path);
		Point2D.Double wp = _modification.getCurrentWorldPoint();
		wpoly[index] = wp;
		_path = WorldGraphicsUtilities.worldPolygonToPath(wpoly);
		updateFocus();
	}

	/**
	 * Get an displayable array of attributes. These will get changed if the
	 * user makes any modifications--even if cancel is selected, so it is the
	 * calling object's responsibility to send a clone if necessary. The
	 * attribute editor will only call "setEditableAttributes" if the user
	 * selects "OK" or "Apply".
	 * 
	 * @return a set of Attributes that will be placed in an Attribute Editor.
	 */
	@Override
	public Attributes getDisplayedAttributes() {
		// base implementation. The idea is to clone, if possible. The clone
		// will be changed,
		// even if the user hits cancel. But only if the user OK or cancel will
		// the method
		// setEditableAttributes be called.

		Attributes attributes = super.getDisplayedAttributes();
		attributes.remove(AttributeType.FILLCOLOR);
		attributes.remove(AttributeType.UUID);
		return attributes;
	}

}
