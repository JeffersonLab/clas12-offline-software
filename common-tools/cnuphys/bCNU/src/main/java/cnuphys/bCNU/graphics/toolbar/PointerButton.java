package cnuphys.bCNU.graphics.toolbar;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.SwingUtilities;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.rubberband.IRubberbanded;
import cnuphys.bCNU.graphics.rubberband.Rubberband;
import cnuphys.bCNU.item.AItem;
import cnuphys.bCNU.item.ItemModification;
import cnuphys.bCNU.util.Environment;

/**
 * The is the default "pointer" button.
 * 
 * @author heddle
 */
@SuppressWarnings("serial")
public class PointerButton extends ToolBarToggleButton
		implements IRubberbanded {

	/**
	 * Item being modified (dragged, rotated, resized)
	 */
	private AItem _modifiedItem;

	/**
	 * Must move at least this far to be a drag.
	 */
	private static final int MINDRAGSTEP = 2;

	/**
	 * Starting mouse position
	 */
	private Point _startPoint = new Point();

	/**
	 * previous mouse position.
	 */
	private Point _prevPoint = new Point();

	/**
	 * Current mouse position.
	 */
	private Point _currentPoint = new Point();

	/**
	 * Used to prevent accidental drags
	 */
	private long lastClick = 0;

	// used to prevent accidental drags
	private long dragDelay = 0;

	/**
	 * True if we are dragging or rotating or resizing.
	 */
	private boolean _modifying = false;

	/**
	 * Creates the default pointer button used for selecting objects,
	 * 
	 * @param container the owner container.
	 */
	public PointerButton(final IContainer container) {
		super(container, "images/pointer.gif", "Make selections");

		// use a custom cursor
		xhot = 3;
		yhot = 1;
		// customCursorImageFile = "images/pointercursor.gif";

	}

	/**
	 * The mouse was clicked. Note that the order the events will come is
	 * PRESSED, RELEASED, CLICKED. And a CLICKED will happen only if the mouse
	 * was not moved between press and release.
	 * 
	 * @param mouseEvent the causal event.
	 */
	@Override
	public void mouseClicked(MouseEvent mouseEvent) {
		reset();
	}

	/**
	 * The mouse was clicked. Note that the order the events will come is
	 * PRESSED, RELEASED, CLICKED. And a CLICKED will happen only if the mouse
	 * was not moved between press and release.
	 * 
	 * @param mouseEvent the causal event.
	 */
	@Override
	public void mousePressed(MouseEvent mouseEvent) {

		if (mouseEvent.isPopupTrigger() || SwingUtilities.isRightMouseButton(mouseEvent)) {
			popupTrigger(mouseEvent);
			return;
		}
		
		// get the topmost item
		_modifiedItem = container.getItemAtPoint(mouseEvent.getPoint());
		if (_modifiedItem != null) {
			if (!_modifiedItem.isEnabled()) {
				_modifiedItem = null;
			}
		}

		// no selection, start rubber-banding
		if (_modifiedItem == null) {
			if (rubberband == null) {
				rubberband = new Rubberband(container, this,
						Rubberband.Policy.RECTANGLE);
				rubberband.setActive(true);
				rubberband.startRubberbanding(mouseEvent.getPoint());
			}

			return;
		}

		selectItemsFromClick(_modifiedItem, mouseEvent);

		// ignore untrackable items
		if ((_modifiedItem != null) && !_modifiedItem.isTrackable()) {
			_modifiedItem = null;
		}

		// set the current point and the start point and the previous point
		_currentPoint.setLocation(mouseEvent.getPoint());
		_startPoint.setLocation(mouseEvent.getPoint());
		_prevPoint.setLocation(mouseEvent.getPoint());
		_modifying = false;
		lastClick = System.currentTimeMillis();
	}

	/**
	 * Mouse has been dragged with pointer button active.
	 * 
	 * @param mouseEvent the causal event.
	 */
	@Override
	public void mouseDragged(MouseEvent mouseEvent) {

		Environment.getInstance().setDragging(true);

		_currentPoint.setLocation(mouseEvent.getPoint());

		if (_modifiedItem != null) {
			if ((Math.abs(_currentPoint.x - _prevPoint.x) >= MINDRAGSTEP)
					|| (Math.abs(
							_currentPoint.y - _prevPoint.y) >= MINDRAGSTEP)) {
				if (_modifying) {
					ItemModification itemModification = _modifiedItem
							.getItemModification();
					itemModification.setCurrentMousePoint(_currentPoint);
					_modifiedItem.modify();

					// if this is a drag and I have children, then I have to
					// trick them into dragging too.
					if (itemModification
							.getType() == ItemModification.ModificationType.DRAG) {
						Vector<AItem> descendants = _modifiedItem
								.getAllDescendants();
						if (descendants != null) {
							for (AItem descendant : descendants) {
								ItemModification dModification = descendant
										.getItemModification();
								dModification
										.setCurrentMousePoint(_currentPoint);
								descendant.modify();
							}
						}
					}

				}
				else {
					_modifying = ((System.currentTimeMillis()
							- lastClick) > dragDelay);
					if (_modifying) {
						ItemModification itemModification = new ItemModification(
								_modifiedItem, container, _startPoint,
								_currentPoint, mouseEvent.isShiftDown(),
								mouseEvent.isControlDown());
						_modifiedItem.setModificationItem(itemModification);
						_modifiedItem.startModification();

						// if this is a drag and I have children, then I have to
						// trick them into dragging too.

						if (itemModification
								.getType() == ItemModification.ModificationType.DRAG) {
							Vector<AItem> descendants = _modifiedItem
									.getAllDescendants();
							if (descendants != null) {
								for (AItem descendant : descendants) {
									ItemModification dModification = new ItemModification(
											descendant, container, _startPoint,
											_currentPoint,
											mouseEvent.isShiftDown(),
											mouseEvent.isControlDown());
									dModification.setType(
											ItemModification.ModificationType.DRAG);
									descendant
											.setModificationItem(dModification);
								}
							}
						}

					}
				}
			}
		}
	}

	/**
	 * The mouse was clicked. Note that the order the events will come is
	 * PRESSED, RELEASED, CLICKED. And a CLICKED will happen only if the mouse
	 * was not moved between press and release.
	 * 
	 * @param mouseEvent the causal event.
	 */
	@Override
	public void mouseReleased(MouseEvent mouseEvent) {

		if (_modifying) {
			ItemModification itemModification = _modifiedItem
					.getItemModification();
			itemModification.setCurrentMousePoint(_currentPoint);
			_modifiedItem.stopModification();
			reset();
		}
		Environment.getInstance().setDragging(false);
	}

	/**
	 * Reset all the modification parameters.
	 */
	private void reset() {
		_modifying = false;
		_modifiedItem = null;
	}

	/**
	 * Handle a mouse double event.
	 * 
	 * @param mouseEvent the causal event
	 */
	@Override
	public void mouseDoubleClicked(MouseEvent mouseEvent) {
		AItem item = container.getItemAtPoint(mouseEvent.getPoint());
		if (item != null) {
			item.doubleClicked(mouseEvent);
		}
	}

	/**
	 * Handle a mouse button 3 event.
	 * 
	 * @param mouseEvent the causal event
	 */
	@Override
	public void mouseButton3Click(MouseEvent mouseEvent) {
		System.err.println("MB3 CLICK POINTERBUTTON");
	}

	/**
	 * Select items based on a click.
	 * 
	 * @param item if not null, we clicked on this item. If null, we clicked on
	 *            the container but not on an item.
	 * @param mouseEvent the causal event
	 */
	private void selectItemsFromClick(AItem item, MouseEvent mouseEvent) {

		if (!SwingUtilities.isLeftMouseButton(mouseEvent)) {
			return;
		}

		// if we click on a selected item do nothing
		if ((item != null) && ((item.isLocked()) || item.isSelected())) {
			return;
		}

		// if control not pressed, deselect all items
		if (!mouseEvent.isControlDown()) {
			container.selectAllItems(false);
		}

		// call the layer to set the select state because it calls the item
		// listeners
		if ((item != null) && (!item.isLocked())) {
			item.getLayer().selectItem(item, true);
		}
		container.getToolBar().checkButtonState();
		container.setDirty(true);
		container.refresh();
	}

	/**
	 * The rubberbanding is complete.
	 */
	@Override
	public void doneRubberbanding() {
		Rectangle b = rubberband.getRubberbandBounds();
		rubberband = null;
		container.selectAllItems(false);

		Vector<AItem> enclosedItems = container.getEnclosedItems(b);
		if (enclosedItems != null) {
			for (AItem item : enclosedItems) {
				// call the layer to set the select state because it calls the
				// item listeners
				if ((item != null) && (!item.isLocked())) {
					item.getLayer().selectItem(item, true);
				}
			}
		}
		container.getToolBar().checkButtonState();
		container.refresh();
	}
}
