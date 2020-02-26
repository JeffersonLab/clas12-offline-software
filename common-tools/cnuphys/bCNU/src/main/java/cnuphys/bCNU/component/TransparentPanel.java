package cnuphys.bCNU.component;

import java.awt.LayoutManager;

import javax.swing.JPanel;

/**
 * A transparent panel
 * @author heddle
 *
 */

@SuppressWarnings("serial")
public class TransparentPanel extends JPanel {

	/**
	 * XCeate a transparent panel
	 * @param layoutMgr the layout manager
	 */
	public TransparentPanel(LayoutManager layoutMgr) {
		setOpaque(false);
		setBackground(null);
		setLayout(layoutMgr);
	}
}
