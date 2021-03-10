package cnuphys.ced.cedview.alldc;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.frame.CedColors;

public class AllDCDisplayPanel extends JPanel implements ActionListener {

	// the parent view
	private AllDCView _view;

	// the toggle buttons
	private JCheckBox _rawHitsButton;
	private JCheckBox _hbHitsButton;
	private JCheckBox _tbHitsButton;
	private JCheckBox _aihbHitsButton;
	private JCheckBox _aitbHitsButton;

	private JCheckBox _nnHitsButton;

	public AllDCDisplayPanel(CedView view) {
		_view = (AllDCView) view;
		setup();
	}

	// create and lawout the components
	private void setup() {
		setLayout(new GridLayout(3, 2, 2, 2));
		setBorder(new CommonBorder("Hit Display Control"));

		JPanel[] panels = new JPanel[6];
		for (int i = 0; i < panels.length; i++) {
			panels[i] = new JPanel();
			panels[i].setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
		}

		_rawHitsButton = createButton(panels[0], 0);
		_nnHitsButton = createButton(panels[1], 1);
		_hbHitsButton = createButton(panels[2], 2);
		_tbHitsButton = createButton(panels[3], 3);
		_aihbHitsButton = createButton(panels[4], 4);
		_aitbHitsButton = createButton(panels[5], 5);

		
		for (JPanel panel : panels) {
			add(panel);
		}
	}

	private JCheckBox createButton(JPanel panel, int opt) {
		JCheckBox button = null;
		
		JComponent component = null;
	
		int w = 8;
		int h = 12;
		
		switch (opt) {
		case 0:
			
			component =  new JComponent() {
				public void paintComponent(Graphics g) {
					g.setColor(Color.red);
					g.fillRect(0, 0, w, h);
				}
				
			};
			
			
			button = new JCheckBox("Raw ", true);
			break;
			
		case 1:
			component =  new JComponent() {
				public void paintComponent(Graphics g) {
					Rectangle b = getBounds();
					g.setColor(CedColors.NN_COLOR);
					g.fillOval(0, 0, w, h);
				}
				
			};
	
			button = new JCheckBox("NN ", false);
			break;

		case 2:
			component =  new JComponent() {
				public void paintComponent(Graphics g) {
					g.setColor(CedColors.HB_COLOR);
					g.fillRect(0, 0, w, h);
				}
				
			};
	
			button = new JCheckBox("Reg HB ", false);
			break;

		case 3:
			component =  new JComponent() {
				public void paintComponent(Graphics g) {
					Rectangle b = getBounds();
					g.setColor(CedColors.TB_COLOR);
					g.fillRect(0, 0, w, h);				
				}
				
			};
	
			button = new JCheckBox("Reg TB ", false);
			break;

		case 4:
			component =  new JComponent() {
				public void paintComponent(Graphics g) {
					g.setColor(CedColors.AIHB_COLOR);
					Graphics2D g2 = (Graphics2D)g;
					Stroke saveStroke = g2.getStroke();
					g2.setStroke(new BasicStroke(2));
					g.drawLine(0, 0, w, h);
					g.drawLine(w, 0, 0, h);		
					
					g2.setStroke(saveStroke);
				}
				
			};
	
			button = new JCheckBox("AI HB ", false);
			break;

		case 5:
			component =  new JComponent() {
				public void paintComponent(Graphics g) {
					Rectangle b = getBounds();
					g.setColor(CedColors.AITB_COLOR);
					Graphics2D g2 = (Graphics2D)g;
					Stroke saveStroke = g2.getStroke();
					g2.setStroke(new BasicStroke(2));
					g.drawLine(w/2, 0, w/2, h);
					g.drawLine(0, h/2, w, h/2);		
					g2.setStroke(saveStroke);
				}
				
			};
	
			button = new JCheckBox("AI TB ", false);
			break;

		}

		Dimension dim = new Dimension(w, h);
		component.setPreferredSize(dim);
		component.setSize(dim);

		GraphicsUtilities.setSizeSmall(button);
		panel.add(component);
		panel.add(button);
		button.addActionListener(this);
		return button;
	}

	/**
	 * Display raw DC hits?
	 * 
	 * @return <code> if we should display raw hits
	 */
	public boolean showRawHits() {
		return _rawHitsButton.isSelected();
	}

	/**
	 * Display regular hit based hits?
	 * 
	 * @return <code> if we should display hit based hits
	 */
	public boolean showHBHits() {
		return _hbHitsButton.isSelected();
	}

	/**
	 * Display regular time based hits?
	 * 
	 * @return <code> if we should display hits
	 */
	public boolean showTBHits() {
		return _tbHitsButton.isSelected();
	}
	
	/**
	 * Display AI hit based hits?
	 * 
	 * @return <code> if we should display hit based hits
	 */
	public boolean showAIHBHits() {
		return _aihbHitsButton.isSelected();
	}

	/**
	 * Display AI time based hits?
	 * 
	 * @return <code> if we should display hits
	 */
	public boolean showAITBHits() {
		return _aitbHitsButton.isSelected();
	}


	/**
	 * Display neural net marked hits?
	 * 
	 * @return <code> if we should display neural net marked hits
	 */
	public boolean showNNHits() {
		return _nnHitsButton.isSelected();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		_view.refresh();
	}
}
