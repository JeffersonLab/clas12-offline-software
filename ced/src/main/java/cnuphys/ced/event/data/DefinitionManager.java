package cnuphys.ced.event.data;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import cnuphys.bCNU.dialog.DialogUtilities;
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
	private boolean addedSeparator;
	
	//all the define plots, which are dialogs
	private Hashtable<String, Holder> _plots = new Hashtable<String, Holder>();
	
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
	 * Het the definition menu
	 * @return the menu
	 */
	public JMenu getMenu() {
		if (_menu == null) {
			_menu = new JMenu("Define");
			_histo = new JMenuItem("Define Histogram...");
			_scatter = new JMenuItem("Define Scatter Plot...");
			
			_histo.addActionListener(this);
			_scatter.addActionListener(this);
			
			_menu.add(_histo);
			_menu.add(_scatter);
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
					if (!addedSeparator) {
						_menu.addSeparator();
						addedSeparator = true;
					}

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
	
	public void remove(String name) {
		Holder holder = _plots.remove(name);
		if (holder != null) {
			_menu.remove(holder.item);
			holder.dialog.setVisible(false);
		}
	}
	
	
	//define a scatterplot
	private void defineScatterPlot() {
		
	}
	
	class Holder {
		public String name;
		public JDialog dialog;
		public JMenuItem item;
		public Holder(String n, JDialog d, JMenuItem mi) {
			name = n;
			dialog = d;
			item = mi;
		}
	}

	
}
