package cnuphys.ced.event.data;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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

	private JFormattedTextField _numBinsTF;

	private JFormattedTextField _minValTF;

	private JFormattedTextField _maxValTF;
	
//	private JFormattedTextField _componentCountTF;
	private JTextField _componentCountTF;

	private SelectPanel _sp;

	private int _numBins = 100;
	private double _minVal = 0;
	private double _maxVal = 1;
	private int _numComponents = 0;

	public HistoPanel() {
		setLayout(new BorderLayout(2, 2));

		_sp = new SelectPanel("Select Variable");
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
		p.setLayout(new BorderLayout(0, 0));

		JPanel sp = new JPanel();
		sp.setLayout(new GridLayout(4, 1, 4, 4));
		_numBinsTF = integerField(sp, "Number of Bins", _numBins);
		_minValTF = decimalField(sp, "Min Value", _minVal);
		_maxValTF = decimalField(sp, "Max Value", _maxVal);
//		_componentCountTF = integerField(sp, "Set for Component Count", _numComponents);
		_componentCountTF = numField(sp, "Set for Component Count", _numComponents);
		
//		KeyAdapter kl = new KeyAdapter() {
//			@Override
//			public void keyReleased(KeyEvent kev) {
//				if (kev.getKeyCode() == KeyEvent.VK_ENTER) {
////				if (Character.isDigit(kev.getKeyChar())) {
//					try {
//						_numComponents = ((Number) _componentCountTF.getValue()).intValue();
//						_numComponents = Math.max(0, Math.min(_numComponents, 10000));
//						_componentCountTF.setValue(_numComponents);
//						
//						if (_numComponents > 0) {
//							_numBins = _numComponents;
//							_minVal = 0.5;
//							_maxVal = _numComponents + 0.5;
//							_numBinsTF.setValue(_numBins);
//							_minValTF.setValue(_minVal);
//							_maxValTF.setValue(_maxVal);
//						}
//					} catch (Exception e) {
//					}
//				}
//			}
//		};
//		_componentCountTF.addKeyListener(kl);

		_numBinsTF.addPropertyChangeListener("value", this);
		_minValTF.addPropertyChangeListener("value", this);
		_maxValTF.addPropertyChangeListener("value", this);
		p.add(sp, BorderLayout.NORTH);
		add(p, BorderLayout.EAST);
	}
	
	private JTextField numField(JPanel p, String title, int defValue) {
		final JTextField tf = new JTextField();

		KeyAdapter kl = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent kev) {

				int oldCount = _numComponents;
				try {
					String s = tf.getText();
					if (s != null) {
						s = s.trim();
						if (s.length() > 0) {
							_numComponents = Integer.parseInt(tf.getText());

							if (_numComponents > 0) {
								_numBins = _numComponents;
								_minVal = 0.5;
								_maxVal = _numComponents + 0.5;
								_numBinsTF.setValue(_numBins);
								_minValTF.setValue(_minVal);
								_maxValTF.setValue(_maxVal);
							}
						}
					}
				} catch (Exception e) {
					_numComponents = oldCount;
					tf.setText("" + _numComponents);
				}
			}
		};
		tf.addKeyListener(kl);
		
		tf.setText("" + defValue);
		tf.setColumns(8);
		JPanel panel = titledPanel(title);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.add(tf);
		p.add(panel);
		return tf;
	}

	private JFormattedTextField integerField(JPanel p, String title,
			int defValue) {
		NumberFormat numberFormat = NumberFormat.getNumberInstance();
		numberFormat.setMaximumFractionDigits(0);
		numberFormat.setGroupingUsed(false);

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
		numberFormat.setMaximumFractionDigits(4);
		numberFormat.setGroupingUsed(false);

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

	/**
	 * Create the histogram data
	 * @return the histogram data
	 */
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
