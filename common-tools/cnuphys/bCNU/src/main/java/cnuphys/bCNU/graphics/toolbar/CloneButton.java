package cnuphys.bCNU.graphics.toolbar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import cnuphys.bCNU.graphics.container.IContainer;

public class CloneButton extends ToolBarButton {
	
	private static Dimension preferredSize = new Dimension(38, 24);


	/**
	 * Create the zoom-in button, which zooms in by a fixed amount.
	 * 
	 * @param container
	 *            the container this button acts upon.
	 */
	public CloneButton(IContainer container) {
		super(container, "images/dolly.png", "Clone the view");
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Rectangle bb = getBounds();
		
		int l = 0;
		int r = l + bb.width-1;
		int t = 0;
		int b = t + bb.height-1;
		
		g.setColor (Color.white);
		g.drawLine(l, b, l, t);
		g.drawLine(l, t, r, t);
		g.setColor (Color.black);
		g.drawLine(r, t, r, b);
		g.drawLine(r, b, l, b);
	}

	/**
	 * This is what I do if I am pressed
	 * 
	 * @param e
	 *            The causal event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		container.getView().cloneView();
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
