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

@SuppressWarnings("serial")
public class ScaleFieldPanel extends JPanel {

	private JLabel _label;
	private final MagneticFields.FieldType _fieldType;
	protected JTextField _textField;

	public ScaleFieldPanel(final MagneticFields.FieldType type, final String name, double defaultVal) {
		setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
		setOpaque(false);
		setBackground(null);
		_fieldType = type;

		// label
		_label = new JLabel("Scale " + name);

		// textField
		_textField = new JTextField(String.format("%7.3f", defaultVal), 5);

		KeyAdapter ka = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent kev) {
				if (kev.getKeyCode() == KeyEvent.VK_ENTER) {
					MenuSelectionManager.defaultManager().clearSelectedPath();
					maybeChangeScaleFactor();
				}
			}
		};
		_textField.addKeyListener(ka);

		FocusAdapter fl = new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				maybeChangeScaleFactor();
			}

		};
		_textField.addFocusListener(fl);

		add(_label);
		add(_textField);
	}

	// maybe change the scale factor
	private void maybeChangeScaleFactor() {
		try {
			double newSF = Double.parseDouble(_textField.getText());

			if (getField() == null) {
				_textField.setText("1.0");
				return;
			}

			double currentSF = getField()._scaleFactor;
			if (Math.abs(newSF - currentSF) > 1.0e-6) {
				getField()._scaleFactor = newSF;
				MagneticFields.getInstance().notifyListeners();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		_textField.setText(String.format("%7.3f", getField().getScaleFactor()));
	}

	private MagneticField getField() {
		return (MagneticField) MagneticFields.getInstance().getIField(_fieldType);
	}

	public void fixText() {
		_textField.setText(String.format("%7.3f", getField().getScaleFactor()));
	}

	@Override
	public void setEnabled(boolean enabled) {
		_label.setEnabled(enabled);
		_textField.setEnabled(enabled);
	}

}
