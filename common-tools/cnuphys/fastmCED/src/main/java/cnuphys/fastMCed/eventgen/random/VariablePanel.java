package cnuphys.fastMCed.eventgen.random;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.NumberFormat;
import java.util.Random;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class VariablePanel extends JPanel implements FocusListener, KeyListener {

	protected static NumberFormat numberFormat = NumberFormat.getNumberInstance();
	static {
		numberFormat.setMaximumFractionDigits(6);
		numberFormat.setMinimumFractionDigits(0);
	}

	private final String _name;
	private final String _units;

	// the text fields
	private JFormattedTextField _minValTF;
	private JFormattedTextField _maxValTF;
	String oldMinText = "";
	String oldMaxText = "";

	// the current values
	private double _minValue;
	private double _maxValue;
	private double _del;
	
	private static int MINTFWIDTH = 80;

	public VariablePanel(final String name, double minVal, double maxVal, final String units) {
		setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));

		_minValTF = new JFormattedTextField(numberFormat);
		_maxValTF = new JFormattedTextField(numberFormat);
		
		Dimension minDim =_minValTF.getPreferredSize();
		minDim.width = MINTFWIDTH;
	
		_minValue = minVal;
		_maxValue = maxVal;

		_name = name;
		_units = units;

		_del = _maxValue - _minValue;

		initTF(_minValTF, minDim, _minValue);
		initTF(_maxValTF, minDim, _maxValue);

		add(new JLabel(name));
		add(_minValTF);
		add(new JLabel("to"));
		add(_maxValTF);
		add(new JLabel(units));
	}
	
	//set up the text field
	private void initTF(JFormattedTextField tf, Dimension dim, double val) {
		tf.setMinimumSize(dim);
		tf.setPreferredSize(dim);
		tf.addFocusListener(this);
		tf.addKeyListener(this);
		tf.setText("" + val);
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		_minValTF.setEnabled(enabled);
		_maxValTF.setEnabled(enabled);
	}

	// See if the min value has changed.
	protected void checkMinTextChange() {

		try {
			String newText = _minValTF.getText();
			if (newText == null) {
				newText = "";
			}

			if (oldMinText.compareTo(newText) != 0) {
				try {
					_minValue = Double.parseDouble(newText);
					_del = _maxValue - _minValue;
					oldMinText = newText;
					System.err.println(toString());
				} catch (NumberFormatException e) {
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// See if the max value has changed.
	protected void checkMaxTextChange() {

		try {
			String newText = _maxValTF.getText();
			if (newText == null) {
				newText = "";
			}

			if (oldMaxText.compareTo(newText) != 0) {

				try {
					_maxValue = Double.parseDouble(newText);
					_del = _maxValue - _minValue;
					oldMaxText = newText;
					System.err.println(toString());
				} catch (NumberFormatException e) {
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	@Override
	public void focusGained(FocusEvent e) {
	}

	@Override
	public void focusLost(FocusEvent e) {
		checkMinTextChange();
		checkMaxTextChange();
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent kev) {
		if (kev.getKeyCode() == KeyEvent.VK_ENTER) {

			checkMinTextChange();
			checkMaxTextChange();
		}
	}

	/**
	 * Get the minimum value
	 * @return the minimum value
	 */
	public double getMinimumValue() {
		return _minValue;
	}

	/**
	 * Get the maximum value
	 * @return the minimum value
	 */
	public double getMaximumValue() {
		return _maxValue;
	}

	/**
	 * Generate a random value in range
	 * 
	 * @param rand
	 *            the generator
	 * @return a random value in range
	 */
	public double randomValue(Random rand) {
		return _minValue + _del * rand.nextDouble();
	}
	
	/**
	 * Generate a random value in range
	 * @param altMax and alternative max
	 * @param rand
	 *            the generator
	 * @return a random value in range
	 */
	public double randomValue(double altMax, Random rand) {
		double del = altMax - _minValue;
		return _minValue + del * rand.nextDouble();
	}


	@Override
	public String toString() {
		return _name + " " + _minValue + " to " + _maxValue + " " + _units;
	}
}
