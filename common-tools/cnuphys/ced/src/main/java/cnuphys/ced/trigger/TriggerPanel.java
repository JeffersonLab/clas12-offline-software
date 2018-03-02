package cnuphys.ced.trigger;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.MathUtilities;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.splot.plot.GraphicsUtilities;

public class TriggerPanel extends JPanel {
	
	//the trigger word id 1,2 or 3
	private int _id;
//	private JLabel _idLabel;
	
	private static Font _smallFont = Fonts.smallFont;
	private static Font _labelFont = Fonts.mediumFont;
	
	private static final Color offColor = new Color(224, 224, 224);
	private static final Color onColor = X11Colors.getX11Color("dark red");
	
	private static int dlabW = -1;
	
//	private static int HSLOP = 20;
	
	private static final int VSLOP = 1;
	private static final int CELL_SIZE = 15;
//	private static final Dimension COMP_SIZE = new Dimension(CELL_SIZE + 2*HSLOP, CELL_SIZE + 20);
	private static final Dimension COMP_SIZE = new Dimension(CELL_SIZE+1, CELL_SIZE + VSLOP);
	
	//the trigger bits
	private long _trigger;
	private JLabel _decimalLabel;
	
	private JLabel _trigLabel;
	
	//diplay bites
	private BitDisplay _bitDisplay;
	
	//to size a label
	private static final String sizeString = " 999999999999";
	
	protected int _preferredWidth;
	
	private boolean _extended;
	
	public TriggerPanel() {
		this(false);
	}
	
	public TriggerPanel(boolean extended) {
		_extended = extended;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setOpaque(false);
		
		_preferredWidth = 32*CELL_SIZE;

		
//		_idLabel = new JLabel("   ");
//		_idLabel.setFont(_labelFont);
		
		if (extended) {
			_trigLabel = new JLabel("trigger");
			_trigLabel.setForeground(X11Colors.getX11Color("dark red"));
			add(_trigLabel);
			add(Box.createHorizontalStrut(10));			
		}


		_decimalLabel = new JLabel("                    ") {

			@Override
			public Dimension getPreferredSize() {
				if (dlabW < 0) {
					FontMetrics fm = this.getFontMetrics(_labelFont);
					dlabW = fm.stringWidth(sizeString);
				}

				Dimension s = super.getPreferredSize();
				s.width = dlabW;
				return s;
			}
			
		};
		_decimalLabel.setFont(_labelFont);
		_decimalLabel.setOpaque(true);
		_decimalLabel.setBackground(Color.black);
		_decimalLabel.setForeground(Color.yellow);
		Border lborder = BorderFactory.createLineBorder(Color.cyan);
		_decimalLabel.setBorder(lborder);

		_preferredWidth += _decimalLabel.getPreferredSize().width;
		
//		add(_idLabel);
//		add(Box.createHorizontalStrut(10));
		
		
		add(getBitsPanel());
		add(Box.createHorizontalStrut(10));
		add(_decimalLabel);
		
		//the struts
		_preferredWidth += 10;
		
		//extended?
		if (extended) {
			_preferredWidth += _trigLabel.getPreferredSize().width;
			_preferredWidth += 10;

		}
		
		Border emptyBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
		setBorder(emptyBorder);
	}

	@Override
	public void paintComponent(Graphics g) {

		if (!_extended) {
			Rectangle b = getBounds();
			g.setColor(Color.gray);
			g.fillRect(0, 0, b.width, b.height);
		}
		super.paintComponent(g);
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
		_trigger = MathUtilities.getUnsignedInt(trigger);
		
		if (_trigger < 0) {
			_trigger += 4294967296L;
		}
		
//		_idLabel.setText((id < 1) ? " " : "" +id);
		_decimalLabel.setText(" " + _trigger);
		
		repaint();
	}
	
	private static boolean checkBit(long x, int k) {
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
		
								
			Color fc = bitIsOn ? onColor : offColor;
			g.setColor(fc);
			g.fillRect(0, 0, CELL_SIZE, CELL_SIZE);
			
			
			GraphicsUtilities.drawSimple3DRect(g, 0, 0, CELL_SIZE, CELL_SIZE, false);
			
			
			
//			g.setColor(Color.black);
//			g.drawRect(0, 0, CELL_SIZE, CELL_SIZE);
			
			g.setFont(_smallFont);
			FontMetrics fm = getFontMetrics(_smallFont);
			
			Color tc = bitIsOn ? Color.white : Color.black;
            g.setColor(tc);
		    String s = "" + index;
		    int xx = 1+ (CELL_SIZE-fm.stringWidth(s))/2;
		    int yy = (CELL_SIZE + fm.getHeight())/2 - 1;
		    g.drawString(s, xx, yy);

			
//			int xc = bounds.width/2;
//			int sw = fm.stringWidth(s);
//			int y = CELL_SIZE + fm.getHeight();
//			
//			g.setColor(Color.black);
//			g.drawString(s, xc-sw/2, y);
			
			
			
		}
	}
}
