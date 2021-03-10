package cnuphys.cnf.alldata.graphics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Environment;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.Histo2DData;
import cnuphys.splot.pdata.HistoData;

/**
 * For managing defined histograms and scatter plots
 * 
 * @author heddle
 *
 */
public class DefinitionManager implements ActionListener {

	// singleton
	private static DefinitionManager _instance;

	// the menu
	private JMenu _menu;
	private JMenuItem _histo;
	private JMenuItem _scatter;
	private JMenuItem _expression;
	private JMenuItem _histo2D;
	private JMenuItem _saveDefinitions;
	private JMenuItem _readDefinitions;

	// save dir
	private String _saveDir = Environment.getInstance().getHomeDirectory();

	// all the define plots, which are dialogs
	private Hashtable<String, Holder> _plots = new Hashtable<String, Holder>();

	// name bundings
	protected Vector<NameBinding> _bindings = new Vector<NameBinding>();

	// expressions
	protected Vector<NamedExpression> _expressions = new Vector<NamedExpression>();

	// for creating expressions
	protected DefineExpressionDialog _defineExpressionDialog;

	// private constructor for single ton
	private DefinitionManager() {
	}

	/**
	 * public access for the singleton
	 * 
	 * @return the singleton
	 */
	public static DefinitionManager getInstance() {
		if (_instance == null) {
			_instance = new DefinitionManager();
		}
		return _instance;
	}

	/**
	 * Get the plot save directory
	 * 
	 * @return the plot save directory
	 */
	public String getSaveDir() {
		return _saveDir;
	}

	/**
	 * Set the plot save directory
	 * 
	 * @param saveDir the new plot save directory
	 */
	public void setSaveDir(String saveDir) {
		_saveDir = saveDir;
	}

	/**
	 * Het the definition menu
	 * 
	 * @return the menu
	 */
	public JMenu getMenu() {
		if (_menu == null) {
			_menu = new JMenu("Define");
			_histo = new JMenuItem("Define a 1D Histogram...");
			_histo2D = new JMenuItem("Define a 2D (colorized) Histogram...");
			_scatter = new JMenuItem("Define a Scatter Plot...");
			_expression = new JMenuItem("Define Expressions...");
			_saveDefinitions = new JMenuItem("Save Definitions...");
			_readDefinitions = new JMenuItem("Read Definitions...");

			_histo.addActionListener(this);
			_histo2D.addActionListener(this);
			_scatter.addActionListener(this);
			_expression.addActionListener(this);
			_saveDefinitions.addActionListener(this);
			_readDefinitions.addActionListener(this);

			_menu.add(_histo);
			_menu.add(_histo2D);
			_menu.add(_scatter);
			_menu.add(_expression);
			_menu.addSeparator();
			_menu.add(_saveDefinitions);
			_menu.add(_readDefinitions);
			_menu.addSeparator();
		}
		return _menu;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o == _histo) {
			defineHistogram();
		} else if (o == _histo2D) {
			defineHistogram2D();
		} else if (o == _scatter) {
			defineScatterPlot();
		} else if (o == _expression) {
			defineExpressions();
		} else if (o == _saveDefinitions) {
			saveAllDefinitions();
		} else if (o == _readDefinitions) {
			//TODO Implement
		}
	}

	// save all definitions
	private void saveAllDefinitions() {
		//TODO implement
	}



	// Expressions
	private void defineExpressions() {
		if (_defineExpressionDialog == null) {
			_defineExpressionDialog = new DefineExpressionDialog();
		}

		_defineExpressionDialog.setVisible(true);
	}

	// define a 1D histogram
	private void defineHistogram() {
		DefineHistoDialog dialog = new DefineHistoDialog();
		dialog.setVisible(true);
		int reason = dialog.getReason();
		if (reason == DialogUtilities.OK_RESPONSE) {
			HistoData histoData = dialog.getHistoData();
			if (histoData != null) {
				// see if already have
				String name = histoData.getName();
				if (_plots.containsKey(name)) {
					JOptionPane.showMessageDialog(null, "Already have a histogram named " + name, "Already Exists",
							JOptionPane.INFORMATION_MESSAGE, ImageManager.cnuIcon);
				} else {
					JMenuItem item = new JMenuItem(name);
					final Histogram histo = new Histogram(histoData);
					int count = _plots.size();
					int x = 10 + 30 * (count % 20);
					int y = 10 + 30 * (count / 20);
					histo.setLocation(x, y);
					_plots.put(name, new Holder(name, histo, item));

					item.addActionListener(event->histo.setVisible(true));
					_menu.add(item);
					// histo.setVisible(true);
				}
			}
		}
	}

	// define a 2D colorized histogram
	private void defineHistogram2D() {
		DefineHisto2DDialog dialog = new DefineHisto2DDialog();
		dialog.setVisible(true);
		int reason = dialog.getReason();
		if (reason == DialogUtilities.OK_RESPONSE) {
			Histo2DData histoData = dialog.getHistoData();
			if (histoData != null) {
				String name = histoData.getName();
				if (_plots.containsKey(name)) {
					JOptionPane.showMessageDialog(null, "Already have a 2D histogram named " + name, "Already Exists",
							JOptionPane.INFORMATION_MESSAGE, ImageManager.cnuIcon);
				} else {
					JMenuItem item = new JMenuItem(name);
					final Histogram2D histo = new Histogram2D(histoData);
					int count = _plots.size();
					int x = 10 + 30 * (count % 20);
					int y = 10 + 30 * (count / 20);
					histo.setLocation(x, y);
					_plots.put(name, new Holder(name, histo, item));

					item.addActionListener(event -> histo.setVisible(true));
					_menu.add(item);
				} // unique name
			} // histodata != null
		} // OK response

	}

	/**
	 * Add a histogram
	 * 
	 * @param histoData the HistoData object
	 * @return the histogram
	 */
	public Histogram addHistogram(HistoData histoData) {

		if (histoData != null) {
			String name = histoData.getName();
			if (_plots.containsKey(name)) {
				JOptionPane.showMessageDialog(null, "Already have a histogram named " + name, "Already Exists",
						JOptionPane.INFORMATION_MESSAGE, ImageManager.cnuIcon);
				return null;
			}

			final Histogram histo = new Histogram(histoData);
			JMenuItem item = new JMenuItem(name);
			_plots.put(name, new Holder(name, histo, item));

			item.addActionListener(event->histo.setVisible(true));
			_menu.add(item);

			return histo;
		}
		return null;
	}

	/**
	 * Add a 2D histogram
	 * 
	 * @param histoData the Histo2DData object
	 * @return the 2D histogram
	 */
	public Histogram2D addHistogram2D(Histo2DData histoData) {
		if (histoData != null) {
			String name = histoData.getName();
			if (_plots.containsKey(name)) {
				JOptionPane.showMessageDialog(null, "Already have a 2D histogram named " + name, "Already Exists",
						JOptionPane.INFORMATION_MESSAGE, ImageManager.cnuIcon);
				return null;
			}

			final Histogram2D histo = new Histogram2D(histoData);
			JMenuItem item = new JMenuItem(name);
			_plots.put(name, new Holder(name, histo, item));

			item.addActionListener(event->histo.setVisible(true));
			_menu.add(item);

			return histo;
		}
		return null;
	}


	// define a scatterplot
	private void defineScatterPlot() {
		DefineScatterDialog dialog = new DefineScatterDialog();
		dialog.setVisible(true);
		int reason = dialog.getReason();
		if (reason == DialogUtilities.OK_RESPONSE) {
			DataSet dataSet = dialog.getDataSeta();
			if (dataSet != null) {
				String name = ScatterPanel.getTitle(dataSet);
				if (_plots.containsKey(name)) {
					JOptionPane.showMessageDialog(null, "Already have a plot named " + name, "Already Exists",
							JOptionPane.INFORMATION_MESSAGE, ImageManager.cnuIcon);
				} else {

					JMenuItem item = new JMenuItem(name);
					
					final ScatterPlot plot = new ScatterPlot(dataSet);
					int count = _plots.size();
					int x = 10 + 30 * (count % 20);
					int y = 10 + 30 * (count / 20);
					plot.setLocation(x, y);
					_plots.put(name, new Holder(name, plot, item));

					item.addActionListener(event->plot.setVisible(true));
					_menu.add(item);
				}

			}
		}
	}

	/**
	 * Remove a plot
	 * 
	 * @param name the name of the plot to remove
	 */
	public void remove(String name) {
		Holder holder = _plots.remove(name);
		if (holder != null) {
			_menu.remove(holder.item);
			holder.dialog.setVisible(false);
		}
	}

	// container for the plots
	class Holder {
		public String name;
		public PlotDialog dialog;
		public JMenuItem item;

		public Holder(String n, PlotDialog d, JMenuItem mi) {
			name = n;
			dialog = d;
			item = mi;
		}
	}

	/**
	 * Get all the current plots
	 * 
	 * @return all the plots
	 */
	public Vector<PlotDialog> getAllPlots() {
		if ((_instance._plots == null) || _instance._plots.isEmpty()) {
			return null;
		}

		Vector<PlotDialog> v = new Vector<PlotDialog>();
		Enumeration<Holder> e = _instance._plots.elements();
		while (e.hasMoreElements()) {
			Holder holder = e.nextElement();
			v.add(holder.dialog);
		}

		return v;
	}

	/**
	 * Add a binding
	 * 
	 * @param vname  the variable name, like "x" or "theta". Case sensitive.
	 * @param bcname the bank column name, like "DC::dgtz.sector"
	 */
	public boolean addBinding(String vname, String bcname) {
		if (isNameBound(vname)) {
			JOptionPane.showMessageDialog(null, "Already have a variable named " + vname, "Already Exists",
					JOptionPane.INFORMATION_MESSAGE, ImageManager.cnuIcon);
			return false;
		}

		NameBinding nb = new NameBinding(vname, bcname);
		_bindings.add(nb);
		Collections.sort(_bindings);
		return true;
	}

	/**
	 * Check to see if a name is already bound
	 * 
	 * @param name the variable name, like "x" or "theta". Case sensitive.
	 * @return <code>true</code> if the name is already bound
	 */
	public boolean isNameBound(String name) {
		// there are not a lot of these so no need for a clever search
		if ((_bindings != null) && !_bindings.isEmpty()) {
			for (NameBinding nb : _bindings) {
				if (nb.varName.equals(name)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get the name bindings
	 * 
	 * @return the name bindings
	 */
	public Vector<NameBinding> getBindings() {
		return _bindings;
	}

	/**
	 * Add an expression
	 * 
	 * @param ename   the expression name. Case sensitive.
	 * @param estring the expression
	 */
	public boolean addExpression(String ename, String estring) {
		if (isNamedExpression(ename)) {
			JOptionPane.showMessageDialog(null, "Already have an expression named " + ename, "Already Exists",
					JOptionPane.INFORMATION_MESSAGE, ImageManager.cnuIcon);
			return false;
		}

		NamedExpression nb = new NamedExpression(ename, estring);
		_expressions.add(nb);
		Collections.sort(_expressions);
		return true;
	}

	/**
	 * Check to see if a name is already defined for an expression
	 * 
	 * @param name the expression name.
	 * @return <code>true</code> if the name is already bound to an expression
	 */
	public boolean isNamedExpression(String name) {
		// there are not a lot of these so no need for a clever search
		if ((_expressions != null) && !_expressions.isEmpty()) {
			for (NamedExpression nb : _expressions) {
				if (nb._expName.equals(name)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get the named expressions
	 * 
	 * @return the expressions
	 */
	public Vector<NamedExpression> getExpressions() {
		return _expressions;
	}

	/**
	 * Find a NamedExpression with the matching name or return null.
	 * 
	 * @param name the name to match
	 * @return the matching NamedExpression
	 */
	public NamedExpression getNamedExpression(String name) {

		if ((name == null) || name.isEmpty()) {
			return null;
		}

		if (haveExpressions()) {
			for (NamedExpression ne : _expressions) {
				if (name.endsWith(ne._expName)) {
					return ne;
				}
			}
		}
		return null;
	}

	/**
	 * Check whether we have any expressions
	 * 
	 * @return <code>true</code> if we have at least one expression
	 */
	public boolean haveExpressions() {
		return (_expressions == null) ? false : !_expressions.isEmpty();
	}

	/**
	 * Find a NameBinding with the matching name or return null.
	 * 
	 * @param name the name to match
	 * @return the matching NameBinding
	 */
	public NameBinding getNameBinding(String name) {

		if ((name == null) || (name.length() < 2)) {
			return null;
		}

		if (name.startsWith("_")) {
			name = name.substring(1);
		} else {
			Log.getInstance().warning("In getNameBindings variable did not start with an underscore [" + name + "]");
		}

		if (haveBindings()) {
			for (NameBinding ne : _bindings) {
				if (name.equals(ne.varName)) {
					return ne;
				}
			}
		}
		return null;
	}

	/**
	 * Check whether we have any bindings
	 * 
	 * @return <code>true</code> if we have at least one expression
	 */
	public boolean haveBindings() {
		return (_bindings == null) ? false : !_bindings.isEmpty();
	}

}
