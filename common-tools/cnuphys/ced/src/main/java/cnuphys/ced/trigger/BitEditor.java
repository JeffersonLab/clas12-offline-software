package cnuphys.ced.trigger;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import cnuphys.bCNU.component.EnumComboBox;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.util.Fonts;

public class BitEditor extends JPanel implements ActionListener {

	private TriggerPanel _trigPanel;
	private JLabel _descriptionLabel;
	private EnumComboBox _matchCombo;
	
	public BitEditor() {
		setLayout(new BorderLayout(0, 6));
		
		_trigPanel = new TriggerPanel(false, true);
		
		add(new JLabel("Click to create a filter pattern"), BorderLayout.NORTH);
		add(_trigPanel, BorderLayout.CENTER);
		setBits(TriggerManager.getInstance().getTriggerFilter().getBits());
		
		add(createMatchSelector(), BorderLayout.SOUTH);
		setBorder(new CommonBorder("Filter applies to the first trigger word"));
	}
	
	private JPanel createMatchSelector() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
		panel.setLayout(new GridLayout(1, 3, 8, 0));
		
		JLabel label = new JLabel("Matching algorithm:");
		
		TriggerMatch match = TriggerManager.getInstance().getTriggerFilter().getType();
		_matchCombo = TriggerMatch.getComboBox(match);
		_matchCombo.addActionListener(this);
		
		_descriptionLabel = new JLabel(match.getDescription());
		_descriptionLabel.setFont(Fonts.defaultMono);
		_descriptionLabel.setForeground(Color.red);
		
		panel.add(label);
		panel.add(_matchCombo);
		panel.add(_descriptionLabel);
		return panel;
	}
	
	@Override
	public Insets getInsets() {
		Insets def = super.getInsets();
		return new Insets(def.top + 1, def.left+4, def.bottom+4,
				def.right+4);
	}
	
	/**
	 * Set the bits. Have to fix the GUI and the actual trigger filter.
	 * @param word the bits to use.
	 */
	public void setBits(int word) {
		_trigPanel.setBits(-1, word);
		TriggerManager.getInstance().getTriggerFilter().setBits(word);
	}
	
	/**
	 * Set the word being displayed
	 * @return the word being displayed
	 */
	public int getBits() {
		return (_trigPanel.getBits());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == _matchCombo) {
			TriggerMatch pattern = (TriggerMatch) _matchCombo.getSelectedEnum();
			TriggerManager.getInstance().getTriggerFilter().setType(pattern);
			_descriptionLabel.setText(pattern.getDescription());
		}
		
	}

}
