package cnuphys.bCNU.component;

import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class LabeledTextField extends JPanel {

	private JTextField _textField;

	/**
	 * Create a labeled text field.
	 * 
	 * @param label
	 *            the label to serve as a prompt.
	 * @param numcol
	 *            the default number of columns in the text field.
	 */
	public LabeledTextField(String label, int numcol) {

		setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
		JLabel jlabel = new JLabel(label);
		add(jlabel);
		_textField = new JTextField(numcol);
		add(_textField);
	}

	/**
	 * Set the text in the text field.
	 * 
	 * @param s
	 *            the text to place in the text field.
	 */
	public void setText(String s) {
		_textField.setText(s);
	}

	/**
	 * Get the text from the text field.
	 * 
	 * @return the text from the text field.
	 */
	public String getText() {
		return _textField.getText();
	}

	/**
	 * Get the underlying text field.
	 * 
	 * @return the underlying text field.
	 */
	public JTextField getTextField() {
		return _textField;
	}
}
