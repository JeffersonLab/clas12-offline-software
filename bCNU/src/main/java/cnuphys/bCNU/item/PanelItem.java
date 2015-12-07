package cnuphys.bCNU.item;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.layer.LogicalLayer;

public class PanelItem extends PixelRectangleItem {

	protected VirtualPanel _virtualPanel = new VirtualPanel();

	
	/**
	 * Create a panel whose location is based on world coordinates but whose
	 * extent is in pixels. And example might be a plot item or image.
	 * 
	 * @param layer
	 *            the Layer this item is on.
	 * @param location
	 *            the location of the lower-left in world coordinates
	 * @param width
	 *            the width in pixels
	 * @param height
	 *            the height in pixels
	 */
	public PanelItem(LogicalLayer layer, Point2D.Double location,
			int width, int height) {
		super(layer, location, width, height);
		_virtualPanel = new VirtualPanel();
		_virtualPanel.setVisible(true);
//		_virtualPanel.addMainComponent(new JLabel("Main Component"));
	}
	
	@Override
	public void drawItem(Graphics g, IContainer container) {
		Rectangle r = getBounds(container);
		Rectangle b = container.getComponent().getBounds();

		_virtualPanel.setBounds(r.x + b.x, r.y + b.y, r.width, r.height);
		_virtualPanel.getReady();
		Image image = _virtualPanel.getImage();
		if (image != null) {
//			System.err.println("DRAW on IMAGE");
			Graphics imageG = image.getGraphics();
			_virtualPanel.paint(imageG);
			imageG.dispose();
			
			g.drawImage(_virtualPanel.getImage(), r.x, r.y, r.width, r.height,
					_virtualPanel);
//			 g.drawImage(_virtualPanel.getImage(), r.x, r.y, r.x+r.width,
//			 r.y+r.height, 0, 0, r.width, r.height, _virtualPanel);
		}

	}

}
