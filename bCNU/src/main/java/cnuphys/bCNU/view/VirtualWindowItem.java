package cnuphys.bCNU.view;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.item.RectangleItem;

//getAnnotationLayer()
public class VirtualWindowItem extends RectangleItem {

	// lives on the virtual view
	private VirtualView _vview;

	private BaseView _baseView;

	public VirtualWindowItem(VirtualView vview, BaseView baseView) {
		super(vview.getContainer().getAnnotationLayer(), getWorldRect(baseView));
		_vview = vview;
		_baseView = baseView;
		_baseView.setVirtualItem(this);

		setRightClickable(true);
		setDraggable(true);
		setRotatable(false);
		setResizable(false);
		setDeletable(false);
		setLocked(false);
		setEnabled(true);
		setVisible(false);
	}

	/**
	 * Called when the item was double clicked. The default implementation is to
	 * edit the item's properties.
	 * 
	 * @param mouseEvent
	 *            the causal event.
	 */
	@Override
	public void doubleClicked(MouseEvent mouseEvent) {
	}

	/**
	 * Sets whether this item is marked as selected.
	 * 
	 * @param selected
	 *            the new value of the flag.
	 */
	@Override
	public void setSelected(boolean selected) {
	}

	/**
	 * A modification such as a drag, resize or rotate has ended.
	 */
	@Override
	public void stopModification() {
		switch (_modification.getType()) {
		case DRAG:
			Point2D.Double wp0 = _modification.getStartWorldPoint();
			Point2D.Double wp1 = _modification.getCurrentWorldPoint();
			int dh = (int) (wp1.x - wp0.x);
			int dv = (int) (wp0.y - wp1.y);

			_baseView.offset(dh, dv);
			setLocation();
			_vview.getContainer().refresh();
			break;

		default:
			break;
		}
		super.stopModification();
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
		if (!_baseView.isVisible() || _baseView.isClosed()
				|| _baseView.isIcon()) {
			setEnabled(false);
		} else {
			super.drawItem(g, container);
			setEnabled(true);
		}
	}

	private static Rectangle2D.Double getWorldRect(BaseView bv) {
		Rectangle b = bv.getBounds();
		Rectangle2D.Double wr = new Rectangle2D.Double();
		wr.x = b.x;
		wr.y = b.y + b.height;
		wr.width = b.width;
		wr.height = b.height;
		return wr;
	}

	public void setLocation() {
		Rectangle2D.Double world = _vview.getContainer().getWorldSystem();
		Rectangle bvBounds = _baseView.getBounds();
		Rectangle2D.Double wr = new Rectangle2D.Double();

		Point2D.Double offset = _vview.totalOffset();

		wr.x = offset.x + bvBounds.x;
		wr.y = offset.y + world.y + world.height
				- (bvBounds.y + bvBounds.height);
		wr.width = bvBounds.width;
		wr.height = bvBounds.height;

		setPath(WorldGraphicsUtilities.getPoints(wr));
		_vview.getContainer().refresh();
	}

	/**
	 * Get the base view that this item represents
	 * 
	 * @return the base view
	 */
	public BaseView getBaseView() {
		return _baseView;
	}
}
