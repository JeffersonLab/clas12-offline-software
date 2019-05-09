package cnuphys.splot.toolbar;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

import cnuphys.splot.plot.ImageManager;

public class ToolBarToggleButton extends JToggleButton {

	/**
	 * preferred size. Default, for whatever reason, will be 24x24.
	 */
	protected Dimension preferredSize;

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
	 * The x coordinate of hot spot of a custom cursor, if there is one. A negative
	 * value means it will use the center of the cursor.
	 */
	protected int xhot = -1;

	/**
	 * The y coordinate of hot spot of a custom cursor, if there is one. A negative
	 * value means it will use the center of the cursor.
	 */
	protected int yhot = -1;

	protected static int ICONSIZE = 24;

	/**
	 * Create a toolbar toggle button
	 * 
	 * @param container     the owner container.
	 * @param imageFileName the name if the file holding the icon
	 * @param toolTip       a string for a tool tip
	 * @param actionCommand theaction command.
	 */
	public ToolBarToggleButton(String imageFileName, String toolTip, String actionCommand) {
		this(imageFileName, toolTip, actionCommand, -1, -1, null);
	}

	/**
	 * Create a toolbar toggle button
	 * 
	 * @param container     the owner container.
	 * @param imageFileName the name if the file holding the icon
	 * @param toolTip       a string for a tool tip
	 * @param actionCommand theaction command.
	 */
	public ToolBarToggleButton(String imageFileName, String toolTip, String actionCommand, int xh, int yh,
			String cursorImageFile) {
		this(imageFileName, toolTip, actionCommand, xh, yh, cursorImageFile, ICONSIZE, ICONSIZE);
	}

	/**
	 * Create a toolbar toggle button
	 * 
	 * @param container     the owner container.
	 * @param imageFileName the name if the file holding the icon
	 * @param toolTip       a string for a tool tip
	 * @param actionCommand theaction command.
	 */
	public ToolBarToggleButton(String imageFileName, String toolTip, String actionCommand, int xh, int yh,
			String cursorImageFile, int iw, int ih) {
		super();
		preferredSize = new Dimension(iw, ih);

		xhot = xh;
		yhot = yh;
		customCursorImageFile = cursorImageFile;

		// setFocusPainted(false);

		setActionCommand(actionCommand);
		ImageIcon imageIcon = ImageManager.getInstance().loadImageIcon(imageFileName);

		String bareName = new String(imageFileName);
		int index = bareName.indexOf(".");
		if (index > 1) {
			bareName = bareName.substring(0, index);
		}

		setFocusPainted(false);
		setToolTipText(toolTip);
		setIcon(imageIcon);
		setRolloverEnabled(true);

		// try to get an enabled icon
		if (index > 0) {
			String baseName = imageFileName.substring(0, index);
			String ext = imageFileName.substring(index);
			String enabledFileName = baseName + "enabled" + ext;
			try {
				ImageIcon enabledImageIcon = ImageManager.getInstance().loadImageIcon(enabledFileName);
				setSelectedIcon(enabledImageIcon);
			}
			catch (Exception e) {
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
	public Cursor canvasCursor() {

		if (!triedOnce) {
			if (customCursorImageFile != null) {
				Image image = ImageManager.getInstance().loadImage(customCursorImageFile, this);
				if (image != null) {

					if (xhot < 0) {
						xhot = image.getWidth(this) / 2;
					}
					if (yhot < 0) {
						yhot = image.getHeight(this) / 2;
					}
					customCursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(xhot, yhot),
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
}
