/**
 * 
 */
package cnuphys.bCNU.util;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

@SuppressWarnings("serial")
public class NumericDocument extends PlainDocument {

	private double _minValue = Double.NEGATIVE_INFINITY;
	private double _maxValue = Double.POSITIVE_INFINITY;

	// Constructor
	public NumericDocument() {
		super();
	}

	// Constructor
	public NumericDocument(double minValue, double maxValue) {
		super();
		if (_minValue < _maxValue) {
			_minValue = minValue;
			_maxValue = maxValue;
		}
	}

	// Insert string method
	@Override
	public void insertString(int offs, String str, AttributeSet a)
			throws BadLocationException {
		if (str == null) {
			return;
		}
		String oldString = getText(0, getLength());
		String newString = oldString.substring(0, offs) + str
				+ oldString.substring(offs);
		try {
			newString = newString.trim();
			double val = 0.0;
			if (newString.length() < 1) {
			} else if ((newString.length() == 1)
					&& ((newString.startsWith("-")) || (newString
							.startsWith("+")))) {
			} else {
				val = Double.parseDouble(newString);
			}

			boolean outOfRange = ((val < _minValue) || (val > _maxValue));
			if (!outOfRange) {
				super.insertString(offs, str, a);
			}
		} catch (NumberFormatException e) {
		}
	}
}
