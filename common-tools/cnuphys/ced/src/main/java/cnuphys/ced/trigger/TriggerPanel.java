package cnuphys.ced.trigger;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.MathUtilities;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.splot.plot.GraphicsUtilities;

public class TriggerPanel extends JPanel implements KeyListener {
	
	//the trigger word id 1,2 or 3
	private int _id;
//	private JLabel _idLabel;
	
	private static Font _smallFont = Fonts.smallFont;
	private static Font _labelFont = Fonts.mediumFont;
	
	//not editable
	private static final Color offColor = new Color(224, 224, 224);
	private static final Color onColor = X11Colors.getX11Color("dark red");
	
	//editable
	private static final Color eOffColor = X11Colors.getX11Color("alice blue");
	private static final Color eOnColor = X11Colors.getX11Color("dark blue");

	
	private static int dlabW = -1;
	
//	private static int HSLOP = 20;
	
	private static final int VSLOP = 1;
	private static final int CELL_SIZE = 15;
//	private static final Dimension COMP_SIZE = new Dimension(CELL_SIZE + 2*HSLOP, CELL_SIZE + 20);
	private static final Dimension COMP_SIZE = new Dimension(CELL_SIZE+1, CELL_SIZE + VSLOP);
	
	//the trigger bits
	private long _trigger;
	private JLabel _decimalLabel;
	
	private JTextField _decimalTF;
	
	private JLabel _trigLabel;
	
	//diplay bites
	private BitDisplay _bitDisplay;
	
	//to size a label
	private static final String sizeString = " 999999999999";
	
	protected int _preferredWidth;
	
	private boolean _extended;
	
	private boolean _editable;
	
	/**
	 * Create a trigger panel
	 */
	public TriggerPanel() {
		this(false, false);
	}
	
	/**
	 * Create a trigger panel
	 * @param extended
	 */
	public TriggerPanel(boolean extended) {
		this(extended, false);
	}
	
	/**
	 * Create a trigger panel
	 * @param extended adds extra components
	 * @param editable can be click edited
	 */
	public TriggerPanel(boolean extended, boolean editable) {
		_extended = extended;
		_editable = editable;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setOpaque(false);
		
		_preferredWidth = 32*CELL_SIZE;

//		_idLabel = new JLabel("   ");
//		_idLabel.setFont(_labelFont);
		
		//if extended add the label for decimal value
		if (extended) {
			_trigLabel = new JLabel("trigger");
			_trigLabel.setForeground(X11Colors.getX11Color("dark red"));
			add(_trigLabel);
			add(Box.createHorizontalStrut(10));			
		}


		if (editable) {
			_decimalTF = new JTextField("                    ") {

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
			
			_decimalTF.setFont(_labelFont);
			_decimalTF.addKeyListener(this);
						
			Border lborder = BorderFactory.createLineBorder(Color.red);
			_decimalTF.setBorder(lborder);

			_preferredWidth += _decimalTF.getPreferredSize().width;


		} else {
			// label for the decimal value
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
		}
		
//		add(_idLabel);
//		add(Box.createHorizontalStrut(10));
		
		
		add(getBitsPanel());
		add(Box.createHorizontalStrut(10));
		
		add(editable ? _decimalTF : _decimalLabel);
		
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

	//create the panel for all 32 bit displays
	private JPanel getBitsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 32, 1, 1));

	//	panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		for (int i = 31; i >= 0; i--) {
			panel.add(new BitDisplay(i));
		}
		return panel;
	}
	
	/**
	 * Set the bits to reflect the trigger value
	 * @param id the id of the trigger
	 * @param trigger the trigger value
	 */
	public void setBits(int id, int trigger) {
		_id = id;
		_trigger = MathUtilities.getUnsignedInt(trigger);
		
		if (_trigger < 0) {
			_trigger += 4294967296L;
		}

		// _idLabel.setText((id < 1) ? " " : "" +id);

		if (_editable) {
			_decimalTF.setText(" " + _trigger);
		} else {
			_decimalLabel.setText(" " + _trigger);
		}
		
		repaint();

	}

	/**
	 * Get the trigger word corresponding to the display.
	 * Note it is an int, so if the 31st but is set it will be
	 * negative
	 * @return the trigger word represented by the bit display
	 */
	public int getBits() {
		Long trigL = new Long(_trigger);
		
		return trigL.intValue();
	}
	
	private static boolean checkBit(long x, int k) {
        return (x & 1L << k) != 0;
    } 
	
	private static long setBit(long x, int k) {
		return x | (1L << k);
	}
	
	private static long toggleBit(long x, int k) {
		return x ^ (1L << k);
	}

	
	//some simple tests
	public static void main(String arg[]) {
		long x = Integer.MAX_VALUE;
		System.out.println("long " + x + "   int " + (new Long(x)).intValue());
		x += 1;
		System.out.println("long " + x + "   int " + (new Long(x)).intValue());

	}


	//used to display (and possibly edit) the word
	class BitDisplay extends JComponent {
		
		//index 0..31
		public final int _index;
		
		public BitDisplay(final int index) {
			_index = index;
			
			if (_editable) {
				MouseAdapter ml = new MouseAdapter() {

					@Override
					public void mouseClicked(MouseEvent e) {
						_trigger = toggleBit(_trigger, _index);
						repaint();
						_decimalTF.setText(" " + _trigger);
						
						int intTrig = (new Long(_trigger)).intValue();
						TriggerManager.getInstance().getTriggerFilter().setBits(intTrig);

					}
					
				};
				
				addMouseListener(ml);
			}
			
		}
		
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			Rectangle bounds = getBounds();
			
			boolean bitOn = checkBit(_trigger, _index);
			drawRect(g, bounds, bitOn);
		}
		
		@Override
		public Dimension getPreferredSize() {
			return COMP_SIZE;
		}
		
		
		private void drawRect(Graphics g, Rectangle bounds, boolean bitIsOn) {
		
			Color fc;
			if (_editable) {
				fc = bitIsOn ? eOnColor : eOffColor;
			}
			else {
				fc = bitIsOn ? onColor : offColor;
			}
			
			g.setColor(fc);
			g.fillRect(0, 0, CELL_SIZE, CELL_SIZE);
			
			
			GraphicsUtilities.drawSimple3DRect(g, 0, 0, CELL_SIZE, CELL_SIZE, false);
			
			
			g.setFont(_smallFont);
			FontMetrics fm = getFontMetrics(_smallFont);
			
			Color tc = bitIsOn ? Color.white : Color.black;
            g.setColor(tc);
		    String s = "" + _index;
		    int xx = 1+ (CELL_SIZE-fm.stringWidth(s))/2;
		    int yy = (CELL_SIZE + fm.getHeight())/2 - 1;
		    g.drawString(s, xx, yy);
			
		}
	}


	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			try {
				Long lval = Long.parseLong(_decimalTF.getText().trim());
				int intTrig = (new Long(lval)).intValue();
				setBits(-1, intTrig);
				TriggerManager.getInstance().getTriggerFilter().setBits(intTrig);
				_decimalTF.getParent().requestFocus();
			}
			catch (Exception ne) {
				ne.printStackTrace();
			}

		}
	}
	
	
}
