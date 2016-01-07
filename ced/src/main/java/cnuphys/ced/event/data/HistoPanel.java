package cnuphys.ced.event.data;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.util.Environment;
import cnuphys.splot.pdata.HistoData;

public class HistoPanel extends JPanel implements PropertyChangeListener {

	protected static NumberFormat _intFormat = NumberFormat.getNumberInstance();

	static {
		_intFormat.setMinimumFractionDigits(0);
	}

	private JFormattedTextField _numBinsTF;

	private JFormattedTextField _minValTF;

	private JFormattedTextField _maxValTF;
	
	private JFormattedTextField _componentCountTF;

	private SelectPanel _sp;

	private int _numBins = 100;
	private double _minVal = 0;
	private double _maxVal = 1;
	private int _numComponents = 0;

	public HistoPanel() {
		setLayout(new BorderLayout(2, 2));

		_sp = new SelectPanel();
		add(_sp, BorderLayout.CENTER);
		addEast();
	}

	/**
	 * Get the select panel
	 * 
	 * @return the select panel
	 */
	public SelectPanel getSelectPanel() {
		return _sp;
	}

	private void addEast() {
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(5, 1, 4, 4));
		_numBinsTF = integerField(p, "Number of Bins", _numBins);
		_minValTF = decimalField(p, "Min Value", _minVal);
		_maxValTF = decimalField(p, "Max Value", _maxVal);
		_componentCountTF = integerField(p, "Set for Component Count", _numComponents);
		
		KeyAdapter kl = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent kev) {
				if (kev.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						_numComponents = ((Number) _componentCountTF.getValue()).intValue();
						_numComponents = Math.max(0, Math.min(_numComponents, 10000));
						_componentCountTF.setValue(_numComponents);
						
						if (_numComponents > 0) {
							_numBins = _numComponents;
							_minVal = -0.5;
							_maxVal = _numComponents;
							_numBinsTF.setValue(_numBins);
							_minValTF.setValue(_minVal);
							_maxValTF.setValue(_maxVal);
						}
					} catch (Exception e) {
					}
				}
			}
		};
		_componentCountTF.addKeyListener(kl);

		_numBinsTF.addPropertyChangeListener("value", this);
		_minValTF.addPropertyChangeListener("value", this);
		_maxValTF.addPropertyChangeListener("value", this);
		add(p, BorderLayout.EAST);
	}

	private JTextField textField(JPanel p, String title, String defText) {
		JPanel panel = titledPanel(title);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		JTextField tf = new JTextField(defText, 20);
		panel.add(tf);
		p.add(panel);
		return tf;
	}

	private JFormattedTextField integerField(JPanel p, String title,
			int defValue) {
		NumberFormat numberFormat = NumberFormat.getNumberInstance();
		numberFormat.setMaximumFractionDigits(0);

		JPanel panel = titledPanel(title);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));

		JFormattedTextField tf = new JFormattedTextField(numberFormat);
		tf.setValue(defValue);
		tf.setColumns(8);
		panel.add(tf);
		p.add(panel);
		return tf;
	}

	private JFormattedTextField decimalField(JPanel p, String title,
			double defValue) {
		NumberFormat numberFormat = NumberFormat.getNumberInstance();

		JPanel panel = titledPanel(title);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		JFormattedTextField tf = new JFormattedTextField(numberFormat);
		tf.setValue(defValue);
		tf.setColumns(10);

		panel.add(tf);
		p.add(panel);
		return tf;
	}

	// get a titled panel
	protected JPanel titledPanel(String title) {
		JPanel pan = new JPanel();
		pan.setBorder(new CommonBorder(title));
		Environment.getInstance().commonize(pan, null);
		return pan;
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		Object source = e.getSource();
		if (source == _numBinsTF) {
			_numBins = ((Number) _numBinsTF.getValue()).intValue();
		}
		else if (source == _minValTF) {
			_minVal = ((Number) _minValTF.getValue()).doubleValue();
		}
		else if (source == _maxValTF) {
			_maxVal = ((Number) _maxValTF.getValue()).doubleValue();
		}
	}

	public HistoData getHistoData() {
		// public HistoData(String name, double valMin, double valMax, int
		// numBins) {
		String name = _sp.getFullName();
		if (name == null) {
			name = "???";
		}
		_numBins = ((Number) _numBinsTF.getValue()).intValue();
		_minVal = ((Number) _minValTF.getValue()).doubleValue();
		_maxVal = ((Number) _maxValTF.getValue()).doubleValue();

		return new HistoData(name, _minVal, _maxVal, _numBins);
	}
}
