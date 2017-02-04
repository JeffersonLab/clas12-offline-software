package cnuphys.ced.cedview.projecteddc;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import cnuphys.lund.X11Colors;
import cnuphys.splot.plot.GraphicsUtilities;

public class SectorSelectorPanel extends JPanel implements ActionListener {
	
	//parent view
	private ISector _view;
	
	//the sector buttons
	private JRadioButton sectorButtons[] = new JRadioButton[6];

	public SectorSelectorPanel(ISector view) {
		_view = view;
		
		setLayout(new GridLayout(2, 3));
		ButtonGroup bg = new ButtonGroup();
		for (int sector = 1; sector <= 6; sector++) {
			sectorButtons[sector-1] = makeButton(sector, bg);
			add(sectorButtons[sector-1]);
		}
	}
	
	private JRadioButton makeButton(int sector, ButtonGroup bg) {
		String label = "Sector " + sector;
		JRadioButton b = new JRadioButton(label, sector == 1);
		GraphicsUtilities.setSizeMini(b);
		b.addActionListener(this);
		b.setForeground(X11Colors.getX11Color("Dark Red"));
		bg.add(b);
		return b;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		for (int i = 0; i < 6; i++) {
			if (sectorButtons[i] == source) {
				_view.setSector(i+1);
			}
		}
		
	}
}
