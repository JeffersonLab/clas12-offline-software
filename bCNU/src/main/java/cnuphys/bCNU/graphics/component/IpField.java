package cnuphys.bCNU.graphics.component;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.util.regex.Matcher;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import cnuphys.bCNU.util.Fonts;

public class IpField extends JFormattedTextField implements DocumentListener {

	private static final Font _ipFont = Fonts.mono;

	private boolean goodDoc = true;

	public static final String EVERYADDRESS = "*.*.*.*";

	public IpField(String initAddress) {
		this();
		setText(initAddress);
	}

	public IpField() {
		setFont(_ipFont);

		FontMetrics fm = getFontMetrics(_ipFont);
		int sw = fm.stringWidth("255.255.255.255");
		Dimension d = getPreferredSize();
		d.width = sw + 6;
		setPreferredSize(d);

		setText("*.*.*.*");

		getDocument().addDocumentListener(this);

		setInputVerifier(new InputVerifier() {
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				boolean inputOK = verify(input);
				if (inputOK) {
					return true;
				} else {
					Toolkit.getDefaultToolkit().beep();
					return false;
				}
			}

			@Override
			public boolean verify(JComponent input) {
				JTextField field = (JTextField) input;
				return checkString(field.getText());
			}
		});

	}

	/**
	 * Reset to default state
	 */
	public void reset() {
		setText(EVERYADDRESS);
	}

	/**
	 * Is this in its reset state?
	 * 
	 * @return <code>true</code> if it is in its reset state.
	 */
	public boolean inResetState() {
		return EVERYADDRESS.equals(getText());
	}

	@Override
	public void setText(String s) {
		if (checkString(s)) {
			super.setText(s);
		} else {
			super.setText("127.0.0.1");
		}
	}

	// checks whether we have a legal string
	private boolean checkString(String s) {
		Matcher m = IpAddressSupport.SIMPLE_STAR_PATTERN.matcher(s);
		return m.matches();
	}

	// check that the format is valid
	private void checkDocument(DocumentEvent e) {
		try {
			String text = e.getDocument().getText(0,
					e.getDocument().getLength());
			goodDoc = checkString(text);
		} catch (BadLocationException ex) {
			// Do something, OK?
		}

	}

	/**
	 * Is the text valid?
	 * 
	 * @return <code>true</code> if the text is valid
	 */
	public boolean validText() {
		return goodDoc;
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		checkDocument(e);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		checkDocument(e);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		checkDocument(e);
	}
}