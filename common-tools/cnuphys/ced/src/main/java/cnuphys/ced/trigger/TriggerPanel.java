package cnuphys.ced.trigger;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import cnuphys.bCNU.util.Fonts;

public class TriggerPanel extends JPanel {
	
	//the trigger word id 1,2 or 3
	private int _id;
//	private JLabel _idLabel;
	
	private static Font _smallFont = Fonts.smallFont;
	private static Font _labelFont = Fonts.largeFont;
	
	private static int dlabW = -1;
	
//	private static int HSLOP = 20;
	
	private static final int VSLOP = 16;
	private static final int CELL_SIZE = 16;
//	private static final Dimension COMP_SIZE = new Dimension(CELL_SIZE + 2*HSLOP, CELL_SIZE + 20);
	private static final Dimension COMP_SIZE = new Dimension(CELL_SIZE+1, CELL_SIZE + VSLOP);
	
	//the trigger bits
	private int _trigger;
	private JLabel _decimalLabel;
	
	//diplay bites
	private BitDisplay _bitDisplay;
	
	public TriggerPanel() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

//		_idLabel = new JLabel("   ");
//		_idLabel.setFont(_labelFont);

		_decimalLabel = new JLabel("                    ") {

			@Override
			public Dimension getPreferredSize() {
				if (dlabW < 0) {
					FontMetrics fm = this.getFontMetrics(_labelFont);
					dlabW = fm.stringWidth("999999999999");
				}

				Dimension s = super.getPreferredSize();
				s.width = dlabW;
				return s;
			}
			
		};
		_decimalLabel.setFont(_labelFont);
		
//		add(_idLabel);
		add(Box.createHorizontalStrut(10));
		add(getBitsPanel());
		add(Box.createHorizontalStrut(10));
		add(_decimalLabel);
		
		Border emptyBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
		setBorder(emptyBorder);
	}
	
	private JPanel getBitsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 32, 1, 1));

	//	panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		for (int i = 31; i >= 0; i--) {
			panel.add(new BitDisplay(i));
		}
		return panel;
	}
	
	
	public void set(int id, int trigger) {
		_id = id;
		_trigger = trigger;
		
//		_idLabel.setText((id < 1) ? " " : "" +id);
		_decimalLabel.setText("" +trigger);
		
		repaint();
	}
	
	private static boolean checkBit(int x, int k) {
        return (x & 1 << k) != 0;
    } 

	
	class BitDisplay extends JComponent {
		
		public int index;
		
		public BitDisplay(int index) {
			this.index = index;
		}
		
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			Rectangle bounds = getBounds();
			
			boolean bitOn = checkBit(_trigger, index);
			drawRect(g, bounds, bitOn);
		}
		
		@Override
		public Dimension getPreferredSize() {
			return COMP_SIZE;
		}
		
//		@Override
//		public Dimension getMinimumSize() {
//			return COMP_SIZE;
//		}
		

		
		private void drawRect(Graphics g, Rectangle bounds, boolean bitIsOn) {
		
								
			Color fc = bitIsOn ? Color.darkGray : Color.white;
			g.setColor(fc);
			g.fillRect(0, 0, CELL_SIZE, CELL_SIZE);
			
			g.setColor(Color.black);
			g.drawRect(0, 0, CELL_SIZE, CELL_SIZE);
			
			g.setFont(_smallFont);
			FontMetrics fm = getFontMetrics(_smallFont);
			
			int xc = bounds.width/2;
		    String s = "" + index;
			int sw = fm.stringWidth(s);
			int y = CELL_SIZE + fm.getHeight();
			
			g.drawString(s, xc-sw/2, y);
			
	//		System.err.println("Draw [" + s + "] at (" + (xc-sw/2) + ", " + y + ")" + "    bounds: " + bounds);
			
			
		}
	}
}
