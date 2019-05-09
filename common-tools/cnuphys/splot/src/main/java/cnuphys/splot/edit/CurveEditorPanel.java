package cnuphys.splot.edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;

import javax.swing.DefaultListModel;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import cnuphys.splot.fit.Fit;
import cnuphys.splot.fit.FitEditorPanel;
import cnuphys.splot.fit.FitType;
import cnuphys.splot.pdata.DataColumn;
import cnuphys.splot.pdata.DataColumnType;
import cnuphys.splot.pdata.HistoData;
import cnuphys.splot.plot.CommonBorder;
import cnuphys.splot.plot.Environment;
import cnuphys.splot.plot.PlotCanvas;
import cnuphys.splot.style.EnumComboBox;
import cnuphys.splot.style.IStyled;
import cnuphys.splot.style.LineStyle;
import cnuphys.splot.style.StyleEditorPanel;
import cnuphys.splot.style.SymbolType;

/**
 * Used to edit parameters for curves on a plot
 * 
 * @author heddle
 *
 */
public class CurveEditorPanel extends JPanel implements ActionListener, PropertyChangeListener {

	// the underlying plot canvas
	protected PlotCanvas _plotCanvas;

	// list font
	protected static Font _listFont = Environment.getInstance().getCommonFont(12);
	protected static Font _textFont = Environment.getInstance().getCommonFont(10);

	// curve table
	private CurveTable _curveTable;

	// style panel
	protected StyleEditorPanel _stylePanel;

	// fit editor
	protected FitEditorPanel _fitPanel;

	// text area for fit info
	protected JEditorPane _textArea;

	/**
	 * A panel for editing data sets
	 * 
	 * @param plotCanvas the plot being edited
	 */
	public CurveEditorPanel(PlotCanvas plotCanvas) {
		// note components already created by super constructor
		_plotCanvas = plotCanvas;
		_plotCanvas.addPropertyChangeListener(this);
		Environment.getInstance().commonize(this, null);
		setBorder(new CommonBorder());
		addContent();
	}

	@Override
	public void setEnabled(boolean enabled) {
		_fitPanel.setEnabled(enabled);
		_stylePanel.setEnabled(enabled);
		// _showCurve.setEnabled(enabled);
	}

	// new curve has been selected
	private void curveChanged(DataColumn curve) {
		// a new curve was selected, which might be null
		// set all editors accordingly

		setEnabled(curve != null);
		if (curve != null) {
			// _showCurve.setSelected(curve.isVisible());
			_stylePanel.setStyle(curve.getStyle());
			_fitPanel.setFit(curve);
			_fitPanel.fitSpecific(curve.getFit().getFitType());
		}

		_fitPanel.reconfigure(curve);
		validate();
		_fitPanel.repaint();
		setTextArea();
	}

	/**
	 * Add the content to the panel
	 */
	protected void addContent() {
		setLayout(new BorderLayout());

		JPanel sp = getOpaquePanel();
		sp.setLayout(new VerticalFlowLayout());
		addList(sp);
		addStyle(sp);
		addFit(sp);

		add(sp, BorderLayout.NORTH);
		addTextArea();
	}

	// add the curve list
	protected void addList(JPanel addPanel) {

		JPanel nPanel = getOpaquePanel();
		nPanel.setLayout(new BorderLayout(0, 4));

		Collection<DataColumn> ycols = _plotCanvas.getDataSet().getAllColumnsByType(DataColumnType.Y);
		final DefaultListModel<DataColumn> model = new DefaultListModel<DataColumn>();
		for (DataColumn dc : ycols) {
			model.addElement(dc);
		}

		_curveTable = new CurveTable(_plotCanvas);
		ListSelectionListener lsl = new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					DataColumn curve = _curveTable.getSelectedCurve();
					// might be null!

					if (curve != null) {
						System.err.println("selected curve " + curve.getName());
					}
					curveChanged(curve);
				}
			}

		};

		_curveTable.getSelectionModel().addListSelectionListener(lsl);
		JScrollPane scrollPane = _curveTable.getScrollPane();
		scrollPane.setBorder(new CommonBorder("Curves"));

		nPanel.add(scrollPane, BorderLayout.CENTER);
		addPanel.add(nPanel);
	}

	/**
	 * Select the first curve
	 */
	public void selectFirstCurve() {
		if (_curveTable != null) {
			try {
				_curveTable.getSelectionModel().setSelectionInterval(0, 0);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// add the style editor
	private void addStyle(JPanel addPanel) {
		_stylePanel = new StyleEditorPanel(_plotCanvas.getDataSet().getType());

		if (_stylePanel.getSymbolSelector() != null) {
			_stylePanel.getSymbolSelector().addActionListener(this);
		}

		if (_stylePanel.getBorderSelector() != null) {
			_stylePanel.getBorderSelector().addActionListener(this);
		}

		if (_stylePanel.getSymbolSizeSelector() != null) {
			_stylePanel.getSymbolSizeSelector().addPropertyChangeListener(this);
		}
		if (_stylePanel.getLineWidthSelector() != null) {
			_stylePanel.getLineWidthSelector().addPropertyChangeListener(this);
		}

		IColorChangeListener iccl = new IColorChangeListener() {

			@Override
			public void colorChanged(Component component, Color color) {
				DataColumn curve = _curveTable.getSelectedCurve();
				if (curve != null) {

					if (component == _stylePanel.getSymbolColor()) {
						curve.getStyle().setFillColor(_stylePanel.getSymbolColor().getColor());
					}
					else if (component == _stylePanel.getBorderColor()) {
						curve.getStyle().setBorderColor(_stylePanel.getBorderColor().getColor());
					}
					else if (component == _stylePanel.getFitLineColor()) {
						curve.getStyle().setFitLineColor(_stylePanel.getFitLineColor().getColor());
					}
					_plotCanvas.repaint();
				}

			}

		};

		if (_stylePanel.getSymbolColor() != null) {
			_stylePanel.getSymbolColor().setColorListener(iccl);
		}
		if (_stylePanel.getBorderColor() != null) {
			_stylePanel.getBorderColor().setColorListener(iccl);
		}
		if (_stylePanel.getFitLineColor() != null) {
			_stylePanel.getFitLineColor().setColorListener(iccl);
		}

		_stylePanel.setEnabled(false);
		addPanel.add(_stylePanel);
	}

	// add the fit editor
	private void addFit(JPanel addPanel) {
		_fitPanel = new FitEditorPanel();
		_fitPanel.setEnabled(false);
		_fitPanel.getPolynomialOrderSelector().addPropertyChangeListener(this);
		_fitPanel.getNumGaussianSelector().addPropertyChangeListener(this);
		_fitPanel.getNumRMSCheckBox().addPropertyChangeListener(this);
		_fitPanel.getStatErrorCheckBox().addPropertyChangeListener(this);

		_fitPanel.getFitSelector().addActionListener(this);

		addPanel.add(_fitPanel);
	}

	// add th text area
	private void addTextArea() {

		_textArea = new JEditorPane();
		_textArea.setEditable(false);
		_textArea.setContentType("text/html");

		if (Environment.getInstance().isLinux()) {
			_textArea.setText("<body style=\"font-size:10px;color:blue\">CNU sPlot</body>");
		}
		else {
			_textArea.setText("<body style=\"font-size:11px;color:blue\">CNU sPlot</body>");
		}
		JScrollPane scrollPane = new JScrollPane(_textArea);
		scrollPane.setBorder(new CommonBorder("Fit Parameters"));

		Dimension d = scrollPane.getPreferredSize();
		d.height = 350;
		scrollPane.setPreferredSize(d);
		add(scrollPane, BorderLayout.CENTER);
	}

	// put the text in the text area
	private void setTextArea() {
		_textArea.setText("");
		DataColumn curve = _curveTable.getSelectedCurve();
		if (curve != null) {
			_textArea.setText(curve.getFit().getFitString(curve));
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		DataColumn curve = _curveTable.getSelectedCurve();
		if (curve == null) {
			return;
		}

		Object source = e.getSource();
		if (source == _stylePanel.getSymbolSelector()) {
			EnumComboBox ecb = (EnumComboBox) source;
			SymbolType stype = SymbolType.getValue((String) ecb.getSelectedItem());

			if (curve.getStyle().getSymbolType() != stype) {
				curve.getStyle().setSymbolType(stype);
				_plotCanvas.repaint();
			}

		}
		else if (source == _stylePanel.getBorderSelector()) {
			EnumComboBox ecb = (EnumComboBox) source;
			LineStyle lineStyle = LineStyle.getValue((String) ecb.getSelectedItem());

			if (curve.getStyle().getFitLineStyle() != lineStyle) {
				curve.getStyle().setFitLineStyle(lineStyle);
				_plotCanvas.repaint();
			}

		}
		else if (source == _fitPanel.getFitSelector()) {
			EnumComboBox ecb = (EnumComboBox) source;
			FitType fitType = FitType.getValue((String) ecb.getSelectedItem());

			if (curve.getFit().getFitType() != fitType) {
				curve.getFit().setFitType(fitType);
				_fitPanel.fitSpecific(curve.getFit().getFitType());

				_fitPanel.reconfigure(curve);
				validate();
				_fitPanel.repaint();
				_plotCanvas.repaint();
			}

		}

	}

	/**
	 * The canvas or a widget has fired a property change. This is used as a simple
	 * notification mechanism.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		if (PlotCanvas.DONEDRAWINGPROP.equals(evt.getPropertyName())) {
			setTextArea();
			return;
		}
		else if (PlotCanvas.DATACLEAREDPROP.equals(evt.getPropertyName())) {
			// all plot data cleared
			_curveTable.clear();
			return;
		}

		// all other props rely on having a non null curve
		DataColumn curve = _curveTable.getSelectedCurve();
		if (curve == null) {
			return;
		}

		if (StyleEditorPanel.SYMBOLSIZEPROP.equals(evt.getPropertyName())) {
			int ssize = (Integer) evt.getNewValue();
			IStyled style = curve.getStyle();
			if (style.getSymbolSize() != ssize) {
				style.setSymbolSize(ssize);
				_plotCanvas.repaint();
			}
		}

		if (StyleEditorPanel.LINEWIDTHPROP.equals(evt.getPropertyName())) {
			int lwidth = (Integer) evt.getNewValue();
			float fwidth = (lwidth / 2.f);
			IStyled style = curve.getStyle();
			System.err.println("Setting line width to: " + fwidth);
			if (style.getFitLineWidth() != fwidth) {
				style.setFitLineWidth(fwidth);
				_plotCanvas.repaint();
			}
		}

		else if (FitEditorPanel.POLYNOMIALORDERPROP.equals(evt.getPropertyName())) {
			int porder = (Integer) evt.getNewValue();
			Fit fit = curve.getFit();
			if (fit.getPolynomialOrder() != porder) {
				fit.setPolynomialOrder(porder);
				_plotCanvas.repaint();
			}
		}

		else if (FitEditorPanel.GAUSSIANNUMPROP.equals(evt.getPropertyName())) {
			int ngauss = (Integer) evt.getNewValue();
			Fit fit = curve.getFit();
			if (fit.getNumGaussian() != ngauss) {
				fit.setNumGaussian(ngauss);
				_plotCanvas.repaint();
			}
		}

		else if (FitEditorPanel.USERMSPROP.equals(evt.getPropertyName())) {
			boolean useRMS = (Boolean) evt.getNewValue();
			HistoData hd = curve.getHistoData();
			hd.setRmsInHistoLegend(useRMS);
			_plotCanvas.repaint();
		}

		else if (FitEditorPanel.STATERRPROP.equals(evt.getPropertyName())) {
			boolean statErr = (Boolean) evt.getNewValue();
			HistoData hd = curve.getHistoData();
			hd.setDrawStatisticalErrors(statErr);
			_plotCanvas.repaint();
		}

	}

	private JPanel getOpaquePanel() {
		JPanel panel = new JPanel();
		Environment.getInstance().commonize(panel, null);
		return panel;
	}

}
