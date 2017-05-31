package cnuphys.bCNU.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;

import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.X11Colors;

@SuppressWarnings("serial")
public class ProgressWindow extends JWindow {

	public static final int COMP_WIDTH = 250;

	// the label for footer messages
	private JLabel _footer;

	// the label for footer messages
	private JLabel _header;

	// the progress bar
	private JProgressBar _progressBar;

	public ProgressWindow(String defaultHeader, String defaultFooter, int min,
			int max) {
		setBackground(X11Colors.getX11Color("Alice Blue"));
		setup(defaultHeader, defaultFooter, min, max);
		pack();
	}

	@Override
	public Insets getInsets() {
		Insets def = super.getInsets();
		return new Insets(def.top + 2, def.left + 2, def.bottom + 2,
				def.right + 2);
	}

	// set up the components
	private void setup(String defaultHeader, String defaultFooter, int min,
			int max) {
		setLayout(new BorderLayout(2, 4));

		JPanel panel = new JPanel();
		// panel.setBackground(X11Colors.getX11Color("tan"));
		// panel.setOpaque(true);

		panel.setLayout(new BorderLayout(2, 4));

		// make the header
		_header = makeLabel(defaultFooter);
		add(_header, BorderLayout.NORTH);

		// make the footer
		_footer = makeLabel(defaultFooter);
		add(_footer, BorderLayout.SOUTH);

		_progressBar = new JProgressBar();

		setMinMax(min, max);

		setComponentWidth(_progressBar, COMP_WIDTH);
		panel.add(_progressBar, BorderLayout.CENTER);

		// panel.setBorder(new CommonBorder("Progress..."));

		add(panel);
		setAlwaysOnTop(true);
	}

	// convenience method to create a label
	private JLabel makeLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(Fonts.smallFont);
		setComponentWidth(label, COMP_WIDTH);
		return label;
	}

	/**
	 * Set the progress indicator to show/cancel indeterminate task mode
	 * 
	 * @param modeOn
	 *            rurns indeterminant mode on or off.
	 */
	public void setIndeterminant(boolean modeOn) {
		_progressBar.setIndeterminate(modeOn);
	}

	/**
	 * Set the min and max on the underlying progress bar. If max < min the
	 * progress bar is set to indeterminant.
	 * 
	 * @param min
	 *            the min of the progress bar
	 * @param max
	 *            the max of the progress bar
	 */
	public void setMinMax(int min, int max) {
		if (max < min) {
			_progressBar.setIndeterminate(true);
		} else {
			_progressBar.setMinimum(min);
			_progressBar.setMaximum(max);
			_progressBar.setValue(min);
			_progressBar.setIndeterminate(false);
		}
	}

	/**
	 * Set the value of the underlying progress bar
	 * 
	 * @param value
	 *            the value of the underlying progress bar
	 */
	public void setValue(int value) {
		_progressBar.setValue(value);
	}

	// set a components preferred width
	private void setComponentWidth(JComponent jc, int w) {
		Dimension d = jc.getPreferredSize();
		d.width = w;
		jc.setPreferredSize(d);
	}

	/**
	 * Set the message displayed abover the progress bar
	 * 
	 * @param text
	 *            the header message to display
	 */
	public void setHeader(String text) {
		_header.setText(text == null ? "" : text);
	}

	/**
	 * Set the message displayed below the progress bar
	 * 
	 * @param text
	 *            the footer message to display
	 */
	public void setFooter(String text) {
		_footer.setText(text == null ? "" : text);
	}

	/**
	 * Get the underlying progress bar
	 * 
	 * @return the underlying progressBar
	 */
	public JProgressBar getProgressBar() {
		return _progressBar;
	}

}
