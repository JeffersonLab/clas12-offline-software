package cnuphys.ced.event.data;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.FileUtilities;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.Histo2DData;
import cnuphys.splot.pdata.HistoData;

/**
 * For managing defined histograms and scatter plots
 * @author heddle
 *
 */
public class DefinitionManager implements ActionListener {

	//singleton
	private static DefinitionManager _instance;
	
	//the menu
	private JMenu _menu;
	private JMenuItem _histo;
	private JMenuItem _scatter;
	private JMenuItem _histo2D;
	private JMenuItem _open;
	
	//save dir
	private String _saveDir = Environment.getInstance().getHomeDirectory();

	
	//all the define plots, which are dialogs
	private Hashtable<String, Holder> _plots = new Hashtable<String, Holder>();
	
	//name bundings
    protected Vector<NameBinding> _bindings = new Vector<NameBinding>();
    
    //expressions
    protected Vector<NamedExpression> _expressions = new Vector<NamedExpression>();
	
	//private constructor for single ton
	private DefinitionManager() {
	}
	
	/**
	 * public access for the singleton
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
	 * @return the plot save directory
	 */
	public String getSaveDir() {
		return _saveDir;
	}
	
	/**
	 * Set the plot save directory
	 * @param saveDir the new plot save directory
	 */
	public void setSaveDir(String saveDir) {
		_saveDir = saveDir;
	}
	
	/**
	 * Het the definition menu
	 * @return the menu
	 */
	public JMenu getMenu() {
		if (_menu == null) {
			_menu = new JMenu("Define");
			_histo = new JMenuItem("Define 1D Histogram...");
			_histo2D = new JMenuItem("Define 2D (colorized) Histogram...");
			_scatter = new JMenuItem("Define Scatter Plot...");
			_open = new JMenuItem("Read a Plot Definition...");
			
			_histo.addActionListener(this);
			_histo2D.addActionListener(this);
			_scatter.addActionListener(this);
			_open.addActionListener(this);
			
			_menu.add(_histo);
			_menu.add(_histo2D);
			_menu.add(_scatter);
			_menu.addSeparator();
			_menu.add(_open);
			_menu.addSeparator();
		}
		return _menu;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o == _histo) {
			defineHistogram();
		}
		else if (o == _histo2D) {
			defineHistogram2D();
		}

		else if (o == _scatter) {
			defineScatterPlot();
		}
		else if (o == _open) {
			readPlotDefinition();
		}
	}
	
	//read a saved plot definition
	private void readPlotDefinition() {
		File file = FileUtilities.openFile(_saveDir, "Plot Definition File", "pltd", "PLTD");
		if (file != null) {
			try {
				PlotReader reader = new PlotReader(file);
				if (reader != null) {
					PlotDialog pdialog = reader.getPlotDialog();
//					if (pdialog != null) {
//						
//					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} // file != null
	}
	
	//define a 1D histogram
	private void defineHistogram() {
		DefineHistoDialog dialog = new DefineHistoDialog();
		dialog.setVisible(true);
		int reason = dialog.getReason();
		if (reason == DialogUtilities.OK_RESPONSE) {
			HistoData histoData = dialog.getHistoData();
			if (histoData != null) {
				//see if already have
				String name = histoData.getName();
				if (_plots.containsKey(name)) {
					JOptionPane.showMessageDialog(null, "Already have a histogram named " + name);
				}
				else {
					JMenuItem item = new JMenuItem(name);
					final Histogram histo = new Histogram(histoData);
					int count = _plots.size();
					int x = 10 + 30*(count % 20);
					int y = 10 + 30*(count / 20);
					histo.setLocation(x, y);
					_plots.put(name, new Holder(name, histo, item));
										
					ActionListener al = new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							histo.setVisible(true);
						}					
					};
					item.addActionListener(al);
					_menu.add(item);
					//histo.setVisible(true);
				}
			}
		}
	}
	
	//define a 2D colorized histogram
	private void defineHistogram2D() {
		DefineHisto2DDialog dialog = new DefineHisto2DDialog();
		dialog.setVisible(true);
		int reason = dialog.getReason();
		if (reason == DialogUtilities.OK_RESPONSE) {
			Histo2DData histoData = dialog.getHistoData();
			if (histoData != null) {
				String name = histoData.getName();
				if (_plots.containsKey(name)) {
					JOptionPane.showMessageDialog(null, "Already have a 2D histogram named " + name);
				}
				else {
					JMenuItem item = new JMenuItem(name);
					final Histogram2D histo = new Histogram2D(histoData);
					int count = _plots.size();
					int x = 10 + 30*(count % 20);
					int y = 10 + 30*(count / 20);
					histo.setLocation(x, y);
					_plots.put(name, new Holder(name, histo, item));
										
					ActionListener al = new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							histo.setVisible(true);
						}					
					};
					item.addActionListener(al);
					_menu.add(item);
				} //unique name
			} //histodata != null
		} //OK response

	}
	
	/**
	 * Add a histogram
	 * @param histoData the HistoData object
	 * @return the histogram
	 */
	public Histogram addHistogram(HistoData histoData) {

		if (histoData != null) {
			String name = histoData.getName();
			if (_plots.containsKey(name)) {
				JOptionPane.showMessageDialog(null, "Already have a histogram named " + name);
				return null;
			}
			
			final Histogram histo = new Histogram(histoData);
			JMenuItem item = new JMenuItem(name);
			_plots.put(name, new Holder(name, histo, item));
			
			ActionListener al = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					histo.setVisible(true);
				}					
			};
			item.addActionListener(al);
			_menu.add(item);
			
			return histo;
		}
		return null;
	}
	
	/**
	 * Add a 2D histogram
	 * @param histoData the Histo2DData object
	 * @return the 2D histogram
	 */
	public Histogram2D addHistogram2D(Histo2DData histoData) {
		if (histoData != null) {
			String name = histoData.getName();
			if (_plots.containsKey(name)) {
				JOptionPane.showMessageDialog(null, "Already have a 2D histogram named " + name);
				return null;
			}
			
			final Histogram2D histo = new Histogram2D(histoData);
			JMenuItem item = new JMenuItem(name);
			_plots.put(name, new Holder(name, histo, item));
			
			ActionListener al = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					histo.setVisible(true);
				}					
			};
			item.addActionListener(al);
			_menu.add(item);
			
			return histo;
		}
		return null;
	}
	
	/**
	 * Add a scatter plot
	 * @param DataSet the DataSet object
	 * @return the scatter plot
	 */
	public ScatterPlot addScatterPlot(DataSet dataSet) {
		if (dataSet != null) {
			String name = ScatterPanel.getTitle(dataSet);
			if (_plots.containsKey(name)) {
				JOptionPane.showMessageDialog(null, "Already have a scatter plot named " + name);
				return null;
			}
			
			final ScatterPlot splot = new ScatterPlot(dataSet);
			JMenuItem item = new JMenuItem(name);
			_plots.put(name, new Holder(name, splot, item));
			
			ActionListener al = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					splot.setVisible(true);
				}					
			};
			item.addActionListener(al);
			_menu.add(item);
			
			return splot;
		}
		return null;
	}
	
	//define a scatterplot
	private void defineScatterPlot() {
		DefineScatterDialog dialog = new DefineScatterDialog();
		dialog.setVisible(true);
		int reason = dialog.getReason();
		if (reason == DialogUtilities.OK_RESPONSE) {
			DataSet dataSet = dialog.getDataSeta();
			if (dataSet != null) {
				String name = ScatterPanel.getTitle(dataSet);
				if (_plots.containsKey(name)) {
					JOptionPane.showMessageDialog(null, "Already have a plot named " + name);
				}
				else {

					JMenuItem item = new JMenuItem(name);
					final ScatterPlot plot = new ScatterPlot(dataSet);
					int count = _plots.size();
					int x = 10 + 30*(count % 20);
					int y = 10 + 30*(count / 20);
					plot.setLocation(x, y);
					_plots.put(name, new Holder(name, plot, item));
										
					ActionListener al = new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							plot.setVisible(true);
						}					
					};
					item.addActionListener(al);
					_menu.add(item);
				}

			}
		}
	}
	
	/**
	 * Remove a plot
	 * @param name the name of the plot to remove
	 */
	public void remove(String name) {
		Holder holder = _plots.remove(name);
		if (holder != null) {
			_menu.remove(holder.item);
			holder.dialog.setVisible(false);
		}
	}
	
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
	 * @return all the plots
	 */
	public static Vector<PlotDialog> getAllPlots() {
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
	 * @param vname the variable name, like "x" or "theta". Case sensitive.
	 * @param bcname the bank column name, like "DC::dgtz.sector"
	 */
	public boolean addBinding(String vname, String bcname) {
		if (isNameBound(vname)) {
			JOptionPane.showMessageDialog(null, "Already have a variable named " + vname);
			return false;
		}
		
		NameBinding nb = new NameBinding(vname, bcname);
		_bindings.add(nb);
		Collections.sort(_bindings);
		return true;
	}
	
	/**
	 * Check to see if a name is already bound
	 * @param name the variable name, like "x" or "theta". Case sensitive.
	 * @return <code>true</code> if the name is already bound
	 */
	public boolean isNameBound(String name) {
		//there are not a lot of these so no need for a clever search
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
	 * @return the name bindings
	 */
	public Vector<NameBinding> getBindings() {
		return _bindings;
	}
	
	
	/**
	 * Add an expression
	 * @param ename the expression name. Case sensitive.
	 * @param estring the expression
	 */
	public boolean addExpression(String ename, String estring) {
		if (isNamedExpression(ename)) {
			JOptionPane.showMessageDialog(null, "Already have an expression named " + ename);
			return false;
		}
		
		NamedExpression nb = new NamedExpression(ename, estring);
		_expressions.add(nb);
		Collections.sort(_expressions);
		return true;
	}
	
	/**
	 * Check to see if a name is already defined for an expression
	 * @param name the expression name.
	 * @return <code>true</code> if the name is already bound to an expression
	 */
	public boolean isNamedExpression(String name) {
		//there are not a lot of these so no need for a clever search
		if ((_expressions != null) && !_expressions.isEmpty()) {
			for (NamedExpression nb : _expressions) {
				if (nb.expName.equals(name)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Get the named expressions
	 * @return the expressions
	 */
	public Vector<NamedExpression> getExpressions() {
		return _expressions;
	}	
}
