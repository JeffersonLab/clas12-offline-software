package cnuphys.ced.fastmc;

import java.awt.FlowLayout;

import javax.swing.JPanel;

import cnuphys.lund.LundComboBox;

public class ConditionPanel extends JPanel {
	
	//the particle selector
	private LundComboBox _lundComboBox;
	
	public ConditionPanel() {
		setLayout(new FlowLayout(FlowLayout.LEFT, 6, 0));
		
		_lundComboBox = new LundComboBox(true, 950.0, 11);
		add(_lundComboBox);
	}

}
