package cnuphys.fastMCed.eventgen.sweep;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.NumberFormat;
import java.util.Random;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class VariableSweepPanel  extends JPanel implements FocusListener, KeyListener {

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
	private JFormattedTextField _stepTF;
	String oldMinText = "";
	String oldMaxText = "";
	String oldStepText = "";
	
	private int _numStep;
	
	private JLabel _numStepLabel;

	// the current values
	private double _minValue;
	private double _maxValue;
	private double _stepValue;
	private double _del; 
	
	//owner
	private SweepEvGenDialog _dialog;
	
	private static int MINTFWIDTH = 80;

	public VariableSweepPanel(final SweepEvGenDialog dialog, final String name, double minVal, double maxVal, double stepVal, final String units) {
 
		_dialog = dialog;
		setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));

		_minValTF = new JFormattedTextField(numberFormat);
		_maxValTF = new JFormattedTextField(numberFormat);
		_stepTF = new JFormattedTextField(numberFormat);
		
		Dimension minDim =_minValTF.getPreferredSize();
		minDim.width = MINTFWIDTH;

		_minValue = minVal;
		_maxValue = maxVal;
		_stepValue = stepVal;

		_name = name;
		_units = units;

		_del = _maxValue - _minValue;
		_stepValue = Math.min(_stepValue, _del);

		initTF(_minValTF, minDim, _minValue);
		initTF(_maxValTF, minDim, _maxValue);
		initTF(_stepTF, minDim, _stepValue);

		_numStepLabel = new JLabel("      ");

		add(new JLabel(name));
		add(_minValTF);
		add(new JLabel("to"));
		add(_maxValTF);
		add(new JLabel("step"));
		add(_stepTF);
		add(new JLabel(units));
		add(_numStepLabel);
		
		
		fix();
	}
	
	@Override
	public Insets getInsets() {
		Insets def = super.getInsets();
		return new Insets(def.top + 4, def.left + 4, def.bottom + 4,
				def.right + 4);
	}

	
	private void fix() {
		
		double maxVal = _maxValue;
		double step = _stepValue;
		
		_maxValue = Math.max(_maxValue, _minValue);
		_del = _maxValue - _minValue;
		_stepValue = Math.min(_stepValue, _del);
		
		if (_stepValue > 0.55*_del) {
			_stepValue = _del;
		}
		
		if (Math.abs(maxVal - _maxValue) > 1.0e-6) {
			oldMaxText = "" + _maxValue;
			_maxValTF.setText(oldMaxText);
		}
		if (Math.abs(step - _stepValue) > 1.0e-6) {
			oldStepText = "" + _stepValue;
			_stepTF.setText(oldStepText);
		}
		
		if (_stepValue < 1.0e-10) {
			_numStep = 1;
		}
		else if (_stepValue > 0.55*_del) {
			_stepValue = _del;
			_numStep = 2;
		}
		else {
			_numStep =  1 + (int)(_del/_stepValue);
		}

		
		_numStepLabel.setText("  #steps: " + _numStep);
		_dialog.fix();
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
		_stepTF.setEnabled(enabled);
	}

	// See if the min value has changed.
	protected void checkMinTextChange() {

		try {
			String newText = _minValTF.getText();
			if (newText == null) {
				return;
			}
			if (newText.length() < 1) {
				return;
			}

			if (oldMinText.compareTo(newText) != 0) {
				try {
					_minValue = Double.parseDouble(newText);
					_del = _maxValue - _minValue;
					_stepValue = Math.min(_stepValue, _del);
					oldMinText = newText;
					fix();
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
				return;
			}
			if (newText.length() < 1) {
				return;
			}
			

			if (oldMaxText.compareTo(newText) != 0) {

				try {
					_maxValue = Double.parseDouble(newText);
					_del = _maxValue - _minValue;
					_stepValue = Math.min(_stepValue, _del);
					oldMaxText = newText;
					fix();
				} catch (NumberFormatException e) {
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	// See if the step value has changed.
	protected void checkStepTextChange() {

		try {
			String newText = _stepTF.getText();
			if (newText == null) {
				return;
			}
			if (newText.length() < 1) {
				return;
			}

			if (oldStepText.compareTo(newText) != 0) {

				try {
					_stepValue = Double.parseDouble(newText);
					_del = _maxValue - _minValue;
					_stepValue = Math.min(_stepValue, _del);

					oldStepText = newText;
					fix();
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
		checkStepTextChange();
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
			checkStepTextChange();
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
	 * Get the vlaue for a given step
	 * @param step should go from [0..(_numStep-1)]
	 */
	public double getValue(int step) {
		
		double val;
		
		if (step >= _numStep) {
			System.err.println("Index problem in VariableSweepPanel.getValue()");
			val =  _maxValue;
		}
		
		val = Math.min(_maxValue, _minValue + _stepValue*step);
		
		return val;
	}
	
	/**
	 * Get the number of steps to cover the range
	 * @return the number of steps to cover the range
	 */
	public int numSteps() {
		return _numStep;
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

	@Override
	public String toString() {
		return _name + " " + _minValue + " to " + _maxValue + " " + _units;
	}
}