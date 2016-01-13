package cnuphys.ced.event.data;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
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
	private JMenuItem _open;
	
	//save dir
	private String _saveDir = Environment.getInstance().getHomeDirectory();

	
	//all the define plots, which are dialogs
	private Hashtable<String, Holder> _plots = new Hashtable<String, Holder>();
	
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
			_histo = new JMenuItem("Define Histogram...");
			_scatter = new JMenuItem("Define Scatter Plot...");
			_open = new JMenuItem("Read a Plot Definition...");
			
			_histo.addActionListener(this);
			_scatter.addActionListener(this);
			_open.addActionListener(this);
			
			_menu.add(_histo);
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
					if (pdialog != null) {
						
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} // file != null
	}
	
	//define a histogram
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
	
}
