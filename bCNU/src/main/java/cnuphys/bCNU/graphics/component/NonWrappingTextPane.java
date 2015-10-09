package cnuphys.bCNU.graphics.component;

import java.awt.Component;

import javax.swing.JTextPane;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.StyledDocument;

/**
 * NonWrappingTextPane Class from "Core Swing: Advanced Programming" page 21
 * 
 * @author Kim Topley
 */

@SuppressWarnings("serial")
public class NonWrappingTextPane extends JTextPane {

	public NonWrappingTextPane(StyledDocument d) {
		super(d);
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		Component parent = getParent();
		ComponentUI ui = getUI();
		return parent != null ? ui.getPreferredSize(this).width <= parent
				.getSize().getWidth() : true;
	}
}
