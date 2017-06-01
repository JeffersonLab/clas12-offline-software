package cnuphys.ced.clasio.table;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cnuphys.bCNU.util.Fonts;

public class NamedLabel extends JPanel {

	/**
	 * The label, on the right, that can be changed.
	 */
	private JTextField variableLabel;

	/**
	 * Create a NamedLabel--which has a fixed label (a prompt) and a variable
	 * label. It is not editable.
	 *
	 * @param name
	 *            this is a constant label, like "event file:"
	 * @param preferredWidth
	 *            this is the preferred width of the variable label.
	 */
	public NamedLabel(String name, int preferredWidth) {
		this(name, null, preferredWidth);
	}

	/**
	 * Create a NamedLabel--which has a fixed label (a prompt) and a variable
	 * label. It is not editable.
	 *
	 * @param name
	 *            this is a constant label, like "event file:"
	 * @param sizingString
	 *            this is used to size the fixed label, to help getting things
	 *            to align.
	 * @param preferredWidth
	 *            this is the preferred width of the variable label.
	 */
	public NamedLabel(String name, String sizingString, int preferredWidth) {
		JLabel fixedLabel = new JLabel(name);
		fixedLabel.setFont(Fonts.tweenFont);
		fixedLabel.setForeground(Color.blue);

		// try to size the fixed label?
		if (sizingString != null) {
			FontMetrics fm = getFontMetrics(fixedLabel.getFont());
			int sw = 4 + fm.stringWidth(sizingString);
			Dimension fd = fixedLabel.getPreferredSize();
			fd.width = sw;
			fixedLabel.setPreferredSize(fd);
		}

		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(fixedLabel);
		variableLabel = new JTextField("     ");
		variableLabel.setBackground(Color.white);
		variableLabel.setFont(Fonts.tweenFont);
		variableLabel.setOpaque(true);
		Dimension d = variableLabel.getPreferredSize();
		d.width = preferredWidth;
		variableLabel.setPreferredSize(d);
		variableLabel.setEditable(false);
		add(variableLabel);
		setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
//		setBorder(BorderFactory.createCompoundBorder(
//				BorderFactory.createEtchedBorder(),
//				BorderFactory.createEmptyBorder(3, 5, 3, 5)));
	}

	/**
	 * Set the text in the variable label.
	 *
	 * @param text
	 *            the text to display in the variable label.
	 */
	public void setText(String text) {
		variableLabel.setText(text);
	}

	/**
	 * Get the text in the variable label.
	 *
	 * @return the text in the variable label.
	 */
	public String getText() {
		return variableLabel.getText();
	}

}