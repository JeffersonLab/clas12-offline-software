package cnuphys.bCNU.item;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class VirtualPanel extends JPanel {

	private BufferedImage image;
	
	private JPanel centerPanel;
	
	private JFrame _frame;
	
	public VirtualPanel() {		
		setLayout(new BorderLayout(0, 0));
		centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout(0, 0));	
		add(centerPanel, BorderLayout.CENTER);
		_frame = new JFrame();
		_frame.setContentPane(this);
		_frame.pack();
	}
	
	/**
	 * Add the main component to this virtual panel.
	 * @param comp the component to add
	 */
	public void addMainComponent(JComponent comp) {
		centerPanel.add(comp, BorderLayout.CENTER);
	}
	
	@Override
	public void paint(Graphics g) {
//		System.err.println("PAINT");
		super.paint(g);
	}
	
	public void getReady() {
		validate();
//		_frame.pack();
		sizeImage();
	}
	
	//size the offscreen buffer
	public void sizeImage() {
		int w = getWidth();
		int h = getHeight();
		
		if ((w < 1) || (h < 1)) {
			return;
		}
		
		if ((image == null) || 
				(image.getWidth() != w) || (image.getHeight() != h)) {
//			System.err.println("New image [" + w + ", " + h + "]");
			image = new BufferedImage(getWidth(), getHeight(),
					BufferedImage.TYPE_INT_ARGB);
		}
		else {
//			System.err.println("Reuse image");
		}
	}
	

	/**
	 * Obtain the ofscreen buffer
	 * @return the offscreen buffer
	 */
	public BufferedImage getImage() {
		return image;
	}
}
