package cnuphys.ced.event.data;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Environment;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.IClasIoEventListener;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.IAccumulationListener;
import cnuphys.splot.plot.PlotPanel;

public abstract class PlotDialog extends JDialog implements ActionListener, IAccumulationListener, IClasIoEventListener {
	
	//the name
	protected String _name;
	
	private static final int width = 650;
	private static final int height = 500;
	
	//save dir
	private static String _saveDir = Environment.getInstance().getHomeDirectory();
	
	//menus
	protected JMenu _fileMenu;	
	protected JMenuItem _saveItem;
	protected JMenuItem _closeItem;
	protected JMenuItem _deleteItem;
	protected JMenuItem _clearItem;
	
	//don't print a gazillion error messages
	protected int _errorCount = 0;
	
	// the plot panel
	protected PlotPanel _plotPanel;
	
	//cut table
	protected CutTablePanel _cutPanel;

	public PlotDialog(String name) {
		_name = name;
		setTitle(name);
		setModal(false);
		setSize(width, height);
		
		addMenus();
		
		AccumulationManager.getInstance().addAccumulationListener(this);
		ClasIoEventManager.getInstance().addClasIoEventListener(this, 2);

		_cutPanel = new CutTablePanel(this);
		add(_cutPanel, BorderLayout.WEST);
	}
	
	//add the menu
	private void addMenus() {
		JMenuBar mbar = new JMenuBar();
		setJMenuBar(mbar);
		
		_fileMenu = new JMenu("File");
		_saveItem = addItem(_fileMenu, "Save Definition...");
		_closeItem = addItem(_fileMenu, "Close");
		_clearItem = addItem(_fileMenu, "Clear Data");
		
		_fileMenu.addSeparator();
		_deleteItem = addItem(_fileMenu, "Delete Plot");
		
		mbar.add(_fileMenu);
	}
	
	//add a menu item
	private JMenuItem addItem(JMenu menu, String label) {
		JMenuItem item = new JMenuItem(label);
		menu.add(item);
		item.addActionListener(this);
		return item;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o == _closeItem) {
			setVisible(false);
		}
		else if (o == _deleteItem) {
			AccumulationManager.getInstance().removeAccumulationListener(this);
			ClasIoEventManager.getInstance().removeClasIoEventListener(this);

			DefinitionManager.getInstance().remove(_name);
		}
		else if (o == _clearItem) {
			clear();
		}
		else if (o == _saveItem) {
			File file = getSaveDefinitionFile();
			if (file != null) {
				saveDefinition(file);
			}
		}

	}
	
	/** Clear all the data */
	protected abstract void clear();

	/** Save the definition */
	protected abstract void saveDefinition(File file);
	
	//select file for saving
	protected File getSaveDefinitionFile() {
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Plot Definition File",
				"pdef", "PDEF");

		File selectedFile = null;
		JFileChooser chooser = new JFileChooser(_saveDir);
		chooser.setSelectedFile(null);
		chooser.setFileFilter(filter);
		int returnVal = chooser.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			selectedFile = chooser.getSelectedFile();
			if (selectedFile != null) {
				_saveDir =selectedFile.getParent();

				if (selectedFile.exists()) {
					int answer = JOptionPane.showConfirmDialog(null,
							selectedFile.getAbsolutePath()
									+ "  already exists. Do you want to overwrite it?",
							"Overwite Existing File?",
							JOptionPane.YES_NO_OPTION);

					if (answer != JFileChooser.APPROVE_OPTION) {
						selectedFile = null;
					}
				} // end file exists check
			}
		}

		return selectedFile;
	}
	
	
	@Override
	public void accumulationEvent(int reason) {
		switch (reason) {
		case AccumulationManager.ACCUMULATION_STARTED:
			_errorCount = 0;
			break;

		case AccumulationManager.ACCUMULATION_CLEAR:
			clear();
			break;


		case AccumulationManager.ACCUMULATION_CANCELLED:
		case AccumulationManager.ACCUMULATION_FINISHED:
			_plotPanel.getCanvas().needsRedraw(true);
			break;
		}

	}

	@Override
	public void openedNewEventFile(String path) {
		_errorCount = 0;
	}

	protected void warning(String s) {
		_errorCount++;
		
		if (_errorCount < 10) {
			Log.getInstance().warning(s);
		}
	}
	
	/**
	 * Get all the defined cuts, active or not
	 * @return all the cuts
	 */
	protected Vector<ICut> getCuts() {
		return _cutPanel.getModel()._data;
	}

}
