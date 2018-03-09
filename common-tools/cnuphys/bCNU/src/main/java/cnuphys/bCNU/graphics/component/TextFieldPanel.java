package cnuphys.bCNU.graphics.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class TextFieldPanel extends JPanel {

	//used to keep the labels a fixed width for alignment
	private int _targetWidth;
	
	//the text field
	private JTextField _textField;
	
	//the label
	private JLabel _label;
	
	@SuppressWarnings("serial")
	public TextFieldPanel(String prompt, String sizeToMeString, int numCol) {
		
		FontMetrics fm = getFontMetrics(getFont());
		_targetWidth = fm.stringWidth(sizeToMeString);
		
		setLayout(new BorderLayout(6, 0));
		
		_label = new JLabel(prompt) {
			@Override
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.width = _targetWidth;
				return d;
			}
		};
		
		add(_label, BorderLayout.WEST);			

		_textField = new JTextField(numCol);
		add(_textField, BorderLayout.CENTER);


	}
	
	/**
	 * Get the text from the underlying text field
	 * @return the text from the underlying text field
	 */
	public String getText() {
		return _textField.getText();
	}
	
	/**
	 * Set the text for the underlying text field
	 * @param text the text for the underlying text field
	 */
	public void setText(String text) {
		_textField.setText(text);
	}
	
	/**
	 * Set whether the underlying text field is editable
	 * @param editable the editable flag
	 */
	public void setEditable(boolean editable) {
		_textField.setEditable(editable);
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		_textField.setEnabled(enabled);
		_label.setEnabled(enabled);
	}

}
