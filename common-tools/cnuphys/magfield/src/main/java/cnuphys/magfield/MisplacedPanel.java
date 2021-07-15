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
public class MisplacedPanel extends JPanel {

	public static final int SHIFTX = 0;
	public static final int SHIFTY = 1;
	public static final int SHIFTZ = 2;

	private static final String shiftStr[] = { "X", "Y", "Z" };

	private JLabel _label;
	private final MagneticFields.FieldType _fieldType;
	protected JTextField _textField;

	private int _shiftDir;

	public MisplacedPanel(final MagneticFields.FieldType type, final String name, double defaultVal, int shiftDir) {
		setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
		setOpaque(false);
		setBackground(null);
		_fieldType = type;
		_shiftDir = shiftDir;

		// label
		_label = new JLabel("Shift " + name + " " + shiftStr[_shiftDir] + " (cm)");

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

	// maybe change the shift
	private void maybeChangeShift() {
		try {
			double newShift = Double.parseDouble(_textField.getText());

			if (getField() == null) {
				_textField.setText("0.0");
				return;
			}

			double currentShift = getShift();

			if (Math.abs(newShift - currentShift) > MagneticField.MISALIGNTOL) {

				switch (_shiftDir) {
				case SHIFTX:
					getField()._shiftX = newShift;
					MagneticFields.getInstance().notifyListeners();
					break;

				case SHIFTY:
					getField()._shiftY = newShift;
					MagneticFields.getInstance().notifyListeners();
					break;

				case SHIFTZ:
					getField()._shiftZ = newShift;
					MagneticFields.getInstance().notifyListeners();
					break;
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		_textField.setText(String.format("%7.3f", getShift()));
	}

	private MagneticField getField() {
		return (MagneticField) MagneticFields.getInstance().getIField(_fieldType);
	}

	public void fixText() {
		_textField.setText(String.format("%7.3f", getField()));
	}

	private double getShift() {
		switch (_shiftDir) {
		case SHIFTX:
			return getField()._shiftX;

		case SHIFTY:
			return getField()._shiftY;

		case SHIFTZ:
			return getField()._shiftZ;
		}
		return 0;
	}

	@Override
	public void setEnabled(boolean enabled) {
		_label.setEnabled(enabled);
		_textField.setEnabled(enabled);
	}
}
