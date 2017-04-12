package randomChoice;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

public class Student extends JComponent {
	
	private static int maxNameLen = 235;
	
	private Font _baseFont = Fonts.hugeFont;
	
	private static Dimension size = new Dimension(250, 300);

	private static Color fc = new Color(220, 220, 220);

	public String name;
	public ImageIcon icon;
	
	public Student(File imageFile) {
		name = imageFile.getName();
		name = name.replace(".png", "");
		name = name.replace('_', ' ');
//		System.err.println("Student name: [" + name + "]");
		
		icon = ImageManager.getInstance().loadImageIcon("images/" + imageFile.getName());
//		System.err.println("Icon = " + icon + "  [W,H] = [" + icon.getIconWidth() + ", " + icon.getIconHeight() + "]");
	}
	
	@Override
	public void paintComponent(Graphics g) {
		g.setColor(fc);
		g.fillRect(0, 1, size.width, size.height-1);
		g.setColor(Color.black);
		g.drawRect(0, 1, size.width, size.height-1);
		
		g.setColor(Color.white);
		g.drawLine(0, 1, 0, size.height-1);
		g.drawLine(0, 1, size.width-1, 1);
		
		g.drawImage(icon.getImage(), 32, 20, 186, 200, this);
		
		paintName(g);
	}
	
	private void paintName(Graphics g) {
		Font font = _baseFont;
		FontMetrics fm = getFontMetrics(font);
		int sw = fm.stringWidth(name);
//		System.err.println("SW for " + name + ": " + sw);
		
		if (sw > maxNameLen) {
//			System.err.println("Scaling name: " + name);
			float scalefactor = ((float)maxNameLen/(float)sw);
			font = Fonts.scaleFont(_baseFont, scalefactor);
			fm = getFontMetrics(font);
			sw = fm.stringWidth(name);
		}
		
		g.setFont(font);
		g.setColor(Color.white);		
		g.drawString(name, (size.width - sw)/2-1, size.height - 21);
		g.setColor(Color.black);		
		g.drawString(name, (size.width - sw)/2, size.height - 20);
	}
	
	@Override
	public Dimension getMinimumSize() {
		return size;
	}
	
	@Override
	public Dimension getMaximumSize() {
		return size;
	}


	@Override
	public Dimension getPreferredSize() {
		return size;
	}

}
