package cnuphys.bCNU.graphics.toolbar;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.graphics.container.IContainer;

@SuppressWarnings("serial")
public abstract class ToolBarButton extends JButton implements ActionListener {

	/**
	 * Preferred size for toolbar buttons.
	 */
	private static Dimension preferredSize = new Dimension(24, 24);

	/**
	 * The owner container.
	 */
	protected IContainer container;

	/**
	 * Constructor
	 * 
	 * @param container
	 *            the owner container.
	 * @param imageFileName
	 *            the name if the file holding the icon
	 * @param toolTip
	 *            a string for a tool tip
	 */
	public ToolBarButton(IContainer container, String imageFileName,
			String toolTip) {
		super();
		this.container = container;
		addActionListener(this);

		ImageIcon imageIcon = ImageManager.getInstance().loadImageIcon(
				imageFileName);
		setFocusPainted(false);
		setBorderPainted(false);

		String bareName = new String(imageFileName);
		int index = bareName.indexOf(".");
		if (index > 1) {
			bareName = bareName.substring(0, index);
		}

		setToolTipText(toolTip);
		setIcon(imageIcon);

	}

	/**
	 * Respond to an action event. That is, the button was selected.
	 * 
	 * @param e
	 *            the event in question.
	 */
	@Override
	public abstract void actionPerformed(ActionEvent e);

	/**
	 * Get the appropriate cursor for this tool.
	 * 
	 * @return the cursor appropriate when the mouse is in the container (and
	 *         this button is active).
	 */
	public Cursor canvasCursor() {
		return Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
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
}
