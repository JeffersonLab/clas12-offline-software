package cnuphys.bCNU.item;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class VirtualPanel extends JPanel {

	private BufferedImage image;
	
	private JPanel centerPanel;
	
	public VirtualPanel() {		
		setLayout(new BorderLayout(0, 0));
		centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout(0, 0));	
		add(centerPanel, BorderLayout.CENTER);
	}
	
	public void addMainComponent(JComponent comp) {
		centerPanel.add(comp, BorderLayout.CENTER);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		if (g == null) {
			sizeImage();
			Graphics2D gph = (Graphics2D) image.getGraphics();

			// paint to gph here
			super.paintComponent(gph);
			gph.dispose();
			return;
		}
		
	}
		
	private void sizeImage() {
		int w = getWidth();
		int h = getHeight();
		
		if ((image == null) || 
				(image.getWidth() != w) || (image.getHeight() != h)) {
//			System.err.println("New image");
			image = new BufferedImage(getWidth(), getHeight(),
					BufferedImage.TYPE_INT_ARGB);
		}
		else {
//			System.err.println("Reuse image");
		}
	}
	

	public BufferedImage getImage() {
		return image;
	}
}
