package cnuphys.magfield;

import java.awt.FlowLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.MenuSelectionManager;

public class MisplacedPanel extends JPanel {

	private JLabel _label;
	private final MagneticFields.FieldType _fieldType;
	protected JTextField _textField;

	public MisplacedPanel(final MagneticFields.FieldType type,
			final String name, double defaultVal) {
		setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
//		setBackground(Color.white);
		_fieldType = type;

		// label
		_label = new JLabel("Shift " + name + " Z (cm)");

		// textField
		_textField = new JTextField(String.format("%7.3f", defaultVal), 5);

		KeyAdapter ka = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent kev) {
				if (kev.getKeyCode() == KeyEvent.VK_ENTER) {
					MenuSelectionManager.defaultManager().clearSelectedPath();
					maybeChangeShift();
				}
			}
		};
		_textField.addKeyListener(ka);
		
		FocusAdapter fl = new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				maybeChangeShift();
			}
			
		};
		_textField.addFocusListener(fl);


		add(_label);
		add(_textField);
	}
	
	//maybe change the Z shift
	private void maybeChangeShift() {
		try {
			double newShift= Double.parseDouble(_textField.getText());
			
			if (getField() == null) {
				_textField.setText("0.0");
				return;
			}
			
			double currentShift = getField()._shiftZ;
			if (Math.abs(newShift - currentShift) > 1.0e-6) {
				getField()._shiftZ = newShift;
				MagneticFields.getInstance().notifyListeners();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		_textField.setText(String.format("%7.3f", getField().getShiftZ()));
	}

	private MagneticField getField() {
		return (MagneticField) MagneticFields.getInstance().getIField(_fieldType);
	}
	
	public void fixText() {
		_textField.setText(String.format("%7.3f", getField().getShiftZ()));
	}

	@Override
	public void setEnabled(boolean enabled) {
		_label.setEnabled(enabled);
		_textField.setEnabled(enabled);
	}
}
