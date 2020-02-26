package cnuphys.bCNU.graphics.toolbar;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;

import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.rubberband.Rubberband;
import cnuphys.bCNU.item.AItem;
import cnuphys.bCNU.item.ItemPopupManager;
import cnuphys.bCNU.menu.ViewPopupMenu;
import cnuphys.bCNU.view.BaseView;

@SuppressWarnings("serial")
public class ToolBarToggleButton extends CommonToolBarToggleButton {

	/**
	 * preferred size. Default, for whatever reason, will be 24x24.
	 */
	protected Dimension preferredSize;

	/**
	 * A rubber band for selecting
	 */
	protected Rubberband rubberband;

	/**
	 * The owner container.
	 */
	protected IContainer container;

	/**
	 * Custom cursor.
	 */
	protected Cursor customCursor;

	/**
	 * Optional file with a custom cursor.
	 */
	protected String customCursorImageFile;

	/**
	 * Used in conjunction with custom cursor.
	 */
	protected boolean triedOnce = false;

	/**
	 * The x coordinate of hot spot of a custom cursor, if there is one. A
	 * negative value means it will use the center of the cursor.
	 */
	protected int xhot = -1;

	/**
	 * The y coordinate of hot spot of a custom cursor, if there is one. A
	 * negative value means it will use the center of the cursor.
	 */
	protected int yhot = -1;

	/**
	 * Create a toolbar toggle button with default preferred size of 22x22
	 * 
	 * @param container the owner container.
	 * @param imageFileName the name if the file holding the icon
	 * @param toolTip a string for a tool tip
	 */
	public ToolBarToggleButton(IContainer container, String imageFileName,
			String toolTip) {
		this(container, imageFileName, toolTip, 24, 24);
	}

	/**
	 * Create a toolbar toggle button
	 * 
	 * @param container the owner container.
	 * @param imageFileName the name if the file holding the icon
	 * @param toolTip a string for a tool tip
	 * @param preferredWidth the preferred width in pixels.
	 * @param preferredHeight the preferred height in pixels.
	 */
	public ToolBarToggleButton(IContainer container, String imageFileName,
			String toolTip, int preferredWidth, int preferredHeight) {
		super();
		preferredSize = new Dimension(preferredWidth, preferredHeight);

		this.container = container;
		setFocusPainted(false);

		ImageIcon imageIcon = ImageManager.getInstance()
				.loadImageIcon(imageFileName);

		String bareName = new String(imageFileName);
		int index = bareName.indexOf(".");
		if (index > 1) {
			bareName = bareName.substring(0, index);
		}

		setFocusPainted(false);
		setToolTipText(toolTip);
		setIcon(imageIcon);

		// try to get an enabled icon
		if (index > 0) {
			String baseName = imageFileName.substring(0, index);
			String ext = imageFileName.substring(index);
			String enabledFileName = baseName + "enabled" + ext;
			try {
				ImageIcon enabledImageIcon = ImageManager.getInstance()
						.loadImageIcon(enabledFileName);
				setSelectedIcon(enabledImageIcon);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Get the preferred size.
	 * 
	 * @return the preferred size for layout.
	 */
	@Override
	public Dimension getPreferredSize() {
		return preferredSize;
	}

	/**
	 * Get the appropriate cursor for this tool.
	 * 
	 * @return The cursor appropriate when the mouse is in the container. The
	 *         default will be a cross hair.
	 */
	@Override
	public Cursor canvasCursor() {

		if (!triedOnce) {
			if (customCursorImageFile != null) {
				Image image = ImageManager.getInstance()
						.loadImage(customCursorImageFile, this);
				if (image != null) {

					if (xhot < 0) {
						xhot = image.getWidth(container.getComponent()) / 2;
					}
					if (yhot < 0) {
						yhot = image.getHeight(container.getComponent()) / 2;
					}
					customCursor = Toolkit.getDefaultToolkit()
							.createCustomCursor(image, new Point(xhot, yhot),
									"pointer");
				}
			}
			triedOnce = true;
		}

		if (customCursor != null) {
			return customCursor;
		}
		else {
			return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
		}
	}

	/**
	 * Handle a mouse button 3 event.
	 * 
	 * @param mouseEvent the causal event
	 */
	@Override
	public void mouseButton3Click(MouseEvent mouseEvent) {
	}

	// mouse press platform's popup trigger
	@Override
	public void popupTrigger(MouseEvent mouseEvent) {

		BaseView view = getView();
		if (view == null) {
			return;
		}

		AItem item = null;

		// on item?
		if (view.getContainer() != null) {
			item = view.getContainer().getItemAtPoint(mouseEvent.getPoint());
		}

		if (item != null) {
			if (item.isRightClickable()) {
				ItemPopupManager.prepareForPopup(item, container,
						mouseEvent.getPoint());
				return;
			}

		}
		
		//handled by subclass?
		if (view.rightClicked(mouseEvent)) {
			return;
		}

		ViewPopupMenu vmenu = view.getViewPopupMenu();
		if ((vmenu == null) || !vmenu.isEnabled()) {
			return;
		}

		vmenu.show(this, mouseEvent.getX(), mouseEvent.getY());
	}

	/**
	 * Get the view this toll bar button resides upon
	 * 
	 * @return the parent view
	 */
	public BaseView getView() {
		return (container == null) ? null : container.getView();
	}
}
