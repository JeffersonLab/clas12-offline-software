package cnuphys.magfield;

import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.MenuSelectionManager;

public class ScaleFieldPanel extends JPanel {

    private JLabel _label;
    private final MagneticFields.FieldType _fieldType;
    protected JTextField _textField;

    public ScaleFieldPanel(final MagneticFields.FieldType type,
	    final String name, double defaultVal) {
	setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
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
		    try {
			double sf = Double.parseDouble(_textField.getText());
			getField()._scaleFactor = sf;
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		    _textField.setText(String.format("%7.3f", getField()
			    .getScaleFactor()));
		    MagneticFields.notifyListeners();
		}
	    }
	};
	_textField.addKeyListener(ka);

//	FocusListener fl = new FocusListener() {
//
//	    @Override
//	    public void focusGained(FocusEvent arg0) {
//	    }
//
//	    @Override
//	    public void focusLost(FocusEvent arg0) {
//		MenuSelectionManager.defaultManager().clearSelectedPath();
//		try {
//		    double sf = Double.parseDouble(_textField.getText());
//		    getField().setScaleFactor(sf);
//		} catch (Exception e) {
//		    e.printStackTrace();
//		}
//		_textField.setText(String.format("%5.2f", getField()
//			.getScaleFactor()));
//		MagneticFields.notifyListeners();
//	    }
//	};
//	_textField.addFocusListener(fl);

	add(_label);
	add(_textField);
    }

    private MagneticField getField() {
	return (MagneticField) MagneticFields.getIField(_fieldType);
    }

    @Override
    public void setEnabled(boolean enabled) {
	_label.setEnabled(enabled);
	_textField.setEnabled(enabled);
    }

}
