package cnuphys.ced.magfield;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import bCNU3D.DoubleFormat;
import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.MagneticFieldInitializationException;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.MagneticFields.FieldType;
import cnuphys.splot.example.APlotDialog;
import cnuphys.splot.fit.FitType;
import cnuphys.splot.pdata.DataColumn;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.DataSetType;
import cnuphys.splot.plot.PlotParameters;
import cnuphys.splot.plot.X11Colors;
import cnuphys.splot.style.SymbolType;

@SuppressWarnings("serial")
public class PlotFieldDialog extends APlotDialog implements ActionListener {

	private static int _numPlotPoints = 50000;

	private static Color[] _curveColors = { Color.black, X11Colors.getX11Color("dark red"),
			X11Colors.getX11Color("dark blue"), Color.red, Color.green, Color.blue };

	private static final int Z = 0;
	private static final int RHO = 1;
	private static final int PHI = 2;

	// which is the variable (other two are fixed)
	private static int _whichVaries = Z;

	// the default fixed values and ranges

	private static String sPHI = UnicodeSupport.SMALL_PHI;
	private static String sRHO = UnicodeSupport.SMALL_RHO;
	private static String sDEG = UnicodeSupport.DEGREE;

	// the x axis labels
	private static String _xLabels[] = { "z (cm) ", sRHO + " (cm) ", sPHI + " (deg)" };

	// the toggle button labels
	private static String tbLabels[] = { " z ", " " + sRHO + " ", " " + sPHI + " " };

	// generate a new plot
	private JButton _plotButton;

	// clear all plots
	private JButton _clearButton;

	// hold the variable changing fields
	private VariablePanel _varPanels[];

	// the variable toggle buttons
	private JRadioButton _vButtons[];

	// plot parameters
	private PlotParameters _parameters;

	/**
	 * Create the dialog for ploting the field
	 * 
	 * @param parent
	 *            the parent dialog
	 * @param modal
	 *            the usual meaning
	 */
	public PlotFieldDialog(JFrame parent, boolean modal) {
		super(parent, "Magnetic Field Plotter", modal, null);
		setIconImage(ImageManager.cnuIcon.getImage());
		_canvas.setPreferredSize(new Dimension(600, 600));
		pack();
	}

	@Override
	protected DataSet createDataSet() throws DataSetException {

		DataSet ds = new DataSet(DataSetType.XYXY, getColumnNames());

		DataColumn curve = ds.getCurve(0);
		if (curve != null) {
			curve.getFit().setFitType(FitType.CONNECT);
			curve.getStyle().setSymbolType(SymbolType.NOSYMBOL);
			curve.getStyle().setLineColor(_curveColors[0]);
			curve.getStyle().setLineWidth(2f);
		}

		return ds;
	}

	@Override
	protected String[] getColumnNames() {
		String labels[] = { "Component", "|B| (1) " + 
				MagneticFields.getInstance().getCurrentConfiguration() };
		return labels;
	}

	@Override
	protected String getXAxisLabel() {
		return "z (cm)";
	}

	@Override
	protected String getYAxisLabel() {
		return "|B| (T)";
	}

	@Override
	protected String getPlotTitle() {
		return "Magnetic Field";
	}

	@Override
	public void fillData() {
	}

	@Override
	public void setPreferences() {
		_parameters = _canvas.getParameters();
		_parameters.setExtraDrawing(true);
		_parameters.mustIncludeYZero(true);
		_parameters.setMinExponentX(3);
	}

	/**
	 * Add a north component
	 */
	@Override
	protected void addNorth() {
		JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout(2, 2));
		panel.add(makeVariablePanel(), BorderLayout.NORTH);
		panel.add(makeButtonPanel(), BorderLayout.SOUTH);

		panel.setBorder(new CommonBorder("Variable Selection"));

		JPanel cPanel = new JPanel() {
			@Override
			public Insets getInsets() {
				Insets def = super.getInsets();
				return new Insets(def.top + 2, def.left + 2, def.bottom + 2, def.right + 2);
			}

		};

		cPanel.setLayout(new GridLayout(3, 1, 0, 6));

		_varPanels = new VariablePanel[3];

		for (int i = 0; i < 3; i++) {
			_varPanels[i] = new VariablePanel(i);
			cPanel.add(_varPanels[i]);
		}

		panel.add(cPanel, BorderLayout.CENTER);

		add(panel, BorderLayout.NORTH);

	}

	// make a button panel
	private JPanel makeButtonPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 0));

		_clearButton = new JButton(" Clear ");
		_clearButton.addActionListener(this);

		_plotButton = new JButton(" Plot ");
		_plotButton.addActionListener(this);

		panel.add(_clearButton);
		panel.add(_plotButton);
		return panel;
	}

	private JPanel makeVariablePanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 0));
		ButtonGroup bg = new ButtonGroup();

		panel.add(new JLabel("Select what varies: "));

		_vButtons = new JRadioButton[3];
		for (int var = 0; var < 3; var++) {
			_vButtons[var] = makeRadioButton(tbLabels[var], var == _whichVaries);
			bg.add(_vButtons[var]);
			panel.add(_vButtons[var]);
		}

		return panel;
	}

	private JRadioButton makeRadioButton(String label, boolean selected) {
		JRadioButton tb = new JRadioButton(label, selected);
		tb.addActionListener(this);
		return tb;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if (source == _plotButton) {
			doPlot();
		} else if (source == _clearButton) {
			doClear();
		} else {
			for (int var = 0; var < 3; var++) {
				if (source == _vButtons[var]) {

					if (var == _whichVaries) {
						return;
					}

					int oldV = _whichVaries;
					_whichVaries = var;
					_varPanels[oldV].setEnabled();
					_varPanels[_whichVaries].setEnabled();

					_parameters.setXLabel(_xLabels[var]);
					fixExtraStrings();
					// System.err.println("CHANGED VARIABLE");
					// _canvas.getDataSet().clear();
					break;
				}

			}
		}
	}

	private void fixExtraStrings() {

		String s0 = MagneticFields.getInstance().getActiveFieldDescription();
		String s4 = MagneticFields.getInstance().fileBaseNames();
		String s1 = "";
		String s2 = "";
		switch (_whichVaries) {
		case Z:
			String pV = DoubleFormat.doubleFormat(_varPanels[PHI].getFixedValue(), 1);
			String rV = DoubleFormat.doubleFormat(_varPanels[RHO].getFixedValue(), 1);
			s1 = sPHI + " = " + pV + sDEG;
			s2 = sRHO + " = " + rV + "cm";
			break;

		case RHO:
			pV = DoubleFormat.doubleFormat(_varPanels[PHI].getFixedValue(), 1);
			String zV = DoubleFormat.doubleFormat(_varPanels[Z].getFixedValue(), 1);
			s1 = sPHI + " = " + pV + sDEG;
			s2 = "z  = " + zV + "cm";
			break;

		case PHI:
			rV = DoubleFormat.doubleFormat(_varPanels[RHO].getFixedValue(), 1);
			zV = DoubleFormat.doubleFormat(_varPanels[Z].getFixedValue(), 1);
			s1 = sRHO + " = " + rV + "cm";
			s2 = "z  = " + zV + "cm";
			break;
		}

		_parameters.setExtraStrings(s0, s4, s1, s2);

	}

	// clear all the plots
	private void doClear() {

		try {
			_canvas.setDataSet(createDataSet());
			_canvas.setWorldSystem();
		} catch (DataSetException e) {
			e.printStackTrace();
		}
		_canvas.repaint();
	}

	// create the plot
	private void doPlot() {
		// _canvas.getDataSet().clear();

		// see if I have any curve slots avaiable
		int curveCount = _canvas.getDataSet().getCurveCount();

		int hotIndex = -1;
		for (int i = 0; i < curveCount; i++) {
			DataColumn curve = _canvas.getDataSet().getCurve(i);
			if (curve.size() == 0) {
				hotIndex = i;
				break;
			}
		}
		if (hotIndex < 0) {
			hotIndex = curveCount;
			DataColumn newCurve = _canvas.getDataSet().addCurve("Component", "|B| (" + (hotIndex + 1) + ") " + 
					MagneticFields.getInstance().getCurrentConfiguration());
			newCurve.getFit().setFitType(FitType.CONNECT);
			newCurve.getStyle().setSymbolType(SymbolType.NOSYMBOL);
			newCurve.getStyle().setLineColor(_curveColors[hotIndex % _curveColors.length]);
			newCurve.getStyle().setLineWidth(2f);
		}

		FieldProbe probe = FieldProbe.factory();

		double min = _varPanels[_whichVaries].getMinValue();
		double max = _varPanels[_whichVaries].getMaxValue();
		double del = (max - min) / (_numPlotPoints - 1);
		
		float x;
		float y;
		float z;
		double phiRad;

		for (int i = 0; i < _numPlotPoints; i++) {
			double val = min + i * del;
			double mag = 0;
			switch (_whichVaries) {
			case Z:
				phiRad = Math.toRadians(_varPanels[PHI].getFixedValue());
				x = (float)(_varPanels[RHO].getFixedValue() * Math.cos(phiRad));
				y = (float)(_varPanels[RHO].getFixedValue() * Math.sin(phiRad));
				z = (float)val;
				mag = probe.fieldMagnitude(x, y, z);
				break;

			case RHO:
				phiRad = Math.toRadians(_varPanels[PHI].getFixedValue());
				x = (float)(val * Math.cos(phiRad));
				y = (float)(val * Math.sin(phiRad));
				z = (float)_varPanels[Z].getFixedValue();
				mag = probe.fieldMagnitude(x, y, z);
				break;

			case PHI:
				phiRad = Math.toRadians(val);
				x = (float)(_varPanels[RHO].getFixedValue() * Math.cos(phiRad));
				y = (float)(_varPanels[RHO].getFixedValue() * Math.sin(phiRad));
				z = (float)_varPanels[Z].getFixedValue();
				mag = probe.fieldMagnitude(x, y, z);
				break;
			}
			
			mag = mag / 10; // to tesla
			try {
				_canvas.getDataSet().addToCurve(hotIndex, val, mag);
			} catch (DataSetException e) {
				e.printStackTrace();
				break;
			}
		}

		fixExtraStrings();
		_canvas.setWorldSystem();
		_canvas.repaint();
	}

	public static void main(String arg[]) {

		// String torusPath =
		// "/Users/heddle/magfield/Jan_clas12TorusFull_2.00.dat";
		String torusPath = "/Users/heddle/magfield/clas12TorusFull_2.00.dat";
		String solenoidPath = "/Users/heddle/magfield/clas12-fieldmap-solenoid.dat";
		try {
			MagneticFields.getInstance().initializeMagneticFieldsFromPath(torusPath, solenoidPath);
			MagneticFields.getInstance().setActiveField(FieldType.TORUS);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("Could not initialize Magnetic Fields");
			System.exit(1);
		} catch (MagneticFieldInitializationException e) {
			e.printStackTrace();
			System.err.println("Could not initialize Magnetic Fields");
			System.exit(1);
		}

		PlotFieldDialog pfd = new PlotFieldDialog(null, true);

		JMenuBar mb = pfd.getJMenuBar();
		mb.add(MagneticFields.getInstance().getMagneticFieldMenu());

		WindowAdapter wa = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				System.out.println("Exiting.");
				System.exit(0);
			}
		};
		pfd.addWindowListener(wa);

		pfd.setVisible(true);

	}

	class VariablePanel extends JPanel {

		private JTextField _fixedTF;
		private JTextField _minTF;
		private JTextField _maxTF;

		private double fixedValues[] = { 375, 50, 0 };
		private double minValues[] = { 200, 0, -20 };
		private double maxValues[] = { 500, 250, 20 };

		private int _varIndex;

		public VariablePanel(int var) {

			_varIndex = var;
			setLayout(new FlowLayout(FlowLayout.LEFT, 20, 0));
			_fixedTF = new JTextField(DoubleFormat.doubleFormat(fixedValues[var], 2), 8);

			add(new JLabel(fwString(_xLabels[var], "  X (XXX) ")));
			add(_fixedTF);

			add(new JLabel(" min "));
			_minTF = new JTextField(DoubleFormat.doubleFormat(minValues[var], 2), 8);
			add(_minTF);

			add(new JLabel(" max "));
			_maxTF = new JTextField(DoubleFormat.doubleFormat(maxValues[var], 2), 8);
			add(_maxTF);

			setEnabled();
		}

		public void setEnabled() {
			// System.err.println("INDEX: " + _varIndex + " VARIES: "+
			// _whichVaries);
			_fixedTF.setEnabled(_varIndex != _whichVaries);
			_minTF.setEnabled(_varIndex == _whichVaries);
			_maxTF.setEnabled(_varIndex == _whichVaries);
		}

		public double getMinValue() {
			try {
				return Double.parseDouble(_minTF.getText());
			} catch (Exception e) {
				return minValues[_varIndex];
			}
		}

		public double getMaxValue() {
			try {
				return Double.parseDouble(_maxTF.getText());
			} catch (Exception e) {
				return maxValues[_varIndex];
			}
		}

		public double getFixedValue() {
			try {
				return Double.parseDouble(_fixedTF.getText());
			} catch (Exception e) {
				return fixedValues[_varIndex];
			}
		}

		private String fwString(String s, String targ) {
			FontMetrics fm = getFontMetrics(getFont());
			int targSW = fm.stringWidth(targ);

			String ss = new String(s);

			while (fm.stringWidth(ss) < targSW) {
				ss = " " + ss;
			}
			return ss;
		}

		@Override
		public Insets getInsets() {
			Insets def = super.getInsets();
			return new Insets(def.top + 2, def.left + 2, def.bottom + 2, def.right + 2);
		}

	}

}
