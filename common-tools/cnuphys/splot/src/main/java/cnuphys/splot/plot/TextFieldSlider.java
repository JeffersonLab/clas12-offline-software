package cnuphys.splot.plot;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public abstract class TextFieldSlider extends JPanel implements ChangeListener {

	// the underlying slider
	private JSlider _slider;

	// underlying text field
	private JTextField _textField;

	private int _defaultValue;

	private static Font _smallFont = Environment.getInstance().getCommonFont(9);
	private static Font _tinyFont = Environment.getInstance().getCommonFont(8);

	public TextFieldSlider(int min, int max, int value, Font font, int minorTickSpacing, String labels[],
			int sliderWidth, int textFieldWidth, String borderTitle) {
		_defaultValue = value;
		Environment.getInstance().commonize(this, null);

		_slider = new JSlider(SwingConstants.HORIZONTAL, min, max, value);
		_slider.addChangeListener(this);
		_slider.setFont(_smallFont);
		Environment.getInstance().commonize(_slider, null);

		if (minorTickSpacing > 0) {
			_slider.setMinorTickSpacing(minorTickSpacing);
			_slider.setPaintTicks(true);
			_slider.setPaintLabels(true);
		}

		if ((labels != null) && (labels.length > 1)) {
			// assume the labels are equally spaced
			float del = ((float) (max - min)) / (labels.length - 1);
			Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();

			for (int i = 0; i < labels.length; i++) {
				int sv = (int) (min + i * del);
				labelTable.put(sv, makeLabel(labels[i]));
			}

			_slider.setLabelTable(labelTable);
		}

		if (sliderWidth > 0) {
			Dimension d = _slider.getPreferredSize();
			d.width = sliderWidth;
			_slider.setPreferredSize(d);
		}

		// now the text field
		_textField = new JTextField(valueString(sliderValueToRealValue()));
		_textField.setFont(font);

		KeyAdapter keyAdapter = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent kev) {
				if (kev.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						double val = Double.parseDouble(_textField.getText());
						_slider.setValue(realValueToSliderValue(val));
					}
					catch (Exception e) {

					}
				}
			}
		};
		_textField.addKeyListener(keyAdapter);

		if (textFieldWidth > 0) {
			Dimension d = _textField.getPreferredSize();
			d.width = textFieldWidth;
			_textField.setPreferredSize(d);
		}

		setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
		add(_slider);
		add(_textField);

		// border?
		if (borderTitle != null) {
			setBorder(new CommonBorder(borderTitle));
		}

	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		_slider.setEnabled(enabled);
		_textField.setEnabled(enabled);
	}

	// make a quick label
	private JLabel makeLabel(String s) {
		JLabel j = new JLabel(s);
		j.setFont(_tinyFont);
		return j;
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		if (_textField != null) {
			_textField.setText(valueString(sliderValueToRealValue()));
		}
		if (!_slider.getValueIsAdjusting()) {
			valueChanged();
		}
	}

	/**
	 * Take the current slider value and convert in to the real value. This allows,
	 * for example, to have a slider range of 0 to 100 mapped to a real-valued range
	 * that is not even necessarily linear.
	 * 
	 * @return the real value corresponding to the current slider value.
	 */
	public abstract double sliderValueToRealValue();

	/**
	 * Convert a real value to the corresponding slider value. This allows, for
	 * example, to have a slider range of 0 to 100 mapped to a real-valued range
	 * that is not even necessarily linear.
	 * 
	 * @param val the real value to convert.
	 * @return the corresponding slider value. (The slider is not automatically set
	 *         to this value)
	 */
	public abstract int realValueToSliderValue(double val);

	/**
	 * Convert format real value into a display string
	 * 
	 * @param val the value to format
	 * @return the formatted value
	 */
	public abstract String valueString(double val);

	/**
	 * This is called when the value changes, with by the slider or by entering text
	 * into the text field and hitting return.
	 */
	public abstract void valueChanged();

	/**
	 * Get the slider component
	 * 
	 * @return the slider component
	 */
	public JSlider getSlider() {
		return _slider;
	}

	/**
	 * Get the text field component
	 * 
	 * @return the text field component
	 */
	public JTextField getTextField() {
		return _textField;
	}

	/**
	 * Convenience method to set the slider value
	 * 
	 * @param val the value to set
	 */
	public void setValue(int val) {
		_slider.setValue(val);
	}

	/**
	 * Convenience method to get the slider's current value.
	 * 
	 * @return the slider's current value.
	 */
	public int getValue() {
		return _slider.getValue();
	}

	/**
	 * Reset the slider back to its default value.
	 */
	public void reset() {
		_slider.setValue(_defaultValue);
	}
}