package cnuphys.ced.clasio.filter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import cnuphys.bCNU.component.LabeledTextField;
import cnuphys.bCNU.dialog.VerticalFlowLayout;
import cnuphys.bCNU.graphics.component.CommonBorder;

public class BankSizeRangePanel extends JPanel implements ActionListener {
	
	//the infinity string
	private static final String INF = "Infinity";
	
	//radio buttons
	private JRadioButton _noRestrictions;
	private JRadioButton _yesRestrictions;
	
	//text fields
	private LabeledTextField _minValue;
	private LabeledTextField _maxValue;
	
	private boolean _settingRange = false;
	
	//the none string
	private static final String NONE = "(None)";
	
	//bank name label
	private JLabel _nameLabel;
	
	//the name of the bank being edited
	private String _hotBank;
	
	//the filter
	private BankSizeFilter _filter;

	/**
	 * Pane for editing the bank filter
	 * @param filter the filter
	 */
	public BankSizeRangePanel(BankSizeFilter filter) {
		_filter = filter;
		setLayout(new BorderLayout(6, 6));
		
		_nameLabel = new JLabel(NONE);
		
		Dimension d = _nameLabel.getPreferredSize();
		d.width = 300;
		_nameLabel.setPreferredSize(d);
	
		
		add(_nameLabel, BorderLayout.NORTH);

		JPanel mp = createMainPanel();
		add(mp, BorderLayout.CENTER);
				
		fixState();
	}

	//fix the state of the components
	private void fixState() {
		_minValue.getTextField().setEnabled(_settingRange);
		_maxValue.getTextField().setEnabled(_settingRange);
		
		_noRestrictions.setEnabled(_hotBank != null);
		_yesRestrictions.setEnabled(_hotBank != null);

	}
	
	private JPanel createMainPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(6, 6));
		
		JPanel np = new JPanel();
		np.setLayout(new VerticalFlowLayout());
		
		ButtonGroup bg = new ButtonGroup();
		
		_noRestrictions = new JRadioButton("No Restrictions (inactive)", !_settingRange);
		_yesRestrictions = new JRadioButton("Size (row count) Limits:  ", _settingRange);
		
		bg.add(_noRestrictions);
		bg.add(_yesRestrictions);
		
		_noRestrictions.addActionListener(this);
		_yesRestrictions.addActionListener(this);
		
		_noRestrictions.setEnabled(false);
		_yesRestrictions.setEnabled(false);
		
		np.add(_noRestrictions);
		np.add(_yesRestrictions);
		
		
		_minValue = new LabeledTextField("More than ", 150, 10);
		_maxValue = new LabeledTextField("Less than ", 150, 10);
		
		_minValue.setText("0");
		_maxValue.setText(INF);
		
		np.add(_minValue);
		np.add(_maxValue);
		

		panel.add(np, BorderLayout.CENTER);
		
		panel.setBorder(new CommonBorder());
		return panel;
	}
	
	/**
	 * Set the bank name
	 */
	public void setName(String bname) {
		_nameLabel.setText(bname == null ? NONE : bname);

		// handle the old hot bank

		if (_hotBank != null) {

			BankSizeFilter.BankRangeRecord oldRec = _filter.getRecord(_hotBank);

			// do we have to create one?
			if (_settingRange) {
				oldRec = _filter.addRecord(_hotBank, 0, Integer.MAX_VALUE, true);
			}

			if (oldRec != null) {
				// if no restrictions, deactivate it

				if (!_settingRange) {
					oldRec.active = false;
				} else {
					oldRec.active = true;
					try {
						int minCount = Integer.parseInt(_minValue.getText());

						String maxStr = _maxValue.getText();
						int maxCount;
						if (maxStr.toLowerCase().contains("inf")) {
							maxCount = Integer.MAX_VALUE;
						} else {
							maxCount = Integer.parseInt(_maxValue.getText());
						}

						oldRec.minCount = minCount;
						oldRec.maxCount = maxCount;

					} catch (Exception e) {
						oldRec.minCount = 0;
						oldRec.maxCount = Integer.MAX_VALUE;
					}
				}
			}
		}

		//handle the new hot bank
		
		_hotBank = bname;
				
		if (_hotBank != null) {
			BankSizeFilter.BankRangeRecord newRec = _filter.getRecord(_hotBank);
			if (newRec == null) {
				_minValue.setText("0");
				_maxValue.setText(INF);
				_noRestrictions.setSelected(true);
				_yesRestrictions.setSelected(false);
				_settingRange = false;
			}	
			else {
				_minValue.setText(""+newRec.minCount);
				
				if (newRec.maxCount == Integer.MAX_VALUE) {
					_maxValue.setText(INF);
				}
				else {
					_maxValue.setText(""+newRec.maxCount);
				}
				
				_noRestrictions.setSelected(!newRec.active);
				_yesRestrictions.setSelected(newRec.active);
				_settingRange = newRec.active;
			}
		}
		fixState();
	}
	


	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if ((source == _noRestrictions) && !_settingRange) {
			return;
		}
		if ((source == _yesRestrictions) && _settingRange) {
			return;
		}
		
		_settingRange = !_settingRange;
		
		fixState();
		
	}

}
