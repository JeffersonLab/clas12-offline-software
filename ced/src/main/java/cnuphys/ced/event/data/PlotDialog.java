package cnuphys.ced.event.data;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.FileUtilities;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.IClasIoEventListener;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.IAccumulationListener;
import cnuphys.splot.plot.PlotPanel;

public abstract class PlotDialog extends JDialog implements ActionListener, IAccumulationListener, IClasIoEventListener {
	
	//String delimitter for tokenizing
	public static final String DELIMIT = "$ $";

	//properties for saving/reading definitions
	protected static final String NAME = "NAME";
	
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

	/**
	 * Create a Plot Dialog
	 * @param name the name of the plot
	 */
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
			    FileWriter fstream;
				try {
					fstream = new FileWriter(file.getPath(), false);
				    BufferedWriter out = new BufferedWriter(fstream);
					saveDefinition(out);
					out.flush();
					out.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				} //true tells to append data.
			}
		}

	}
	
	/** Clear all the data */
	protected abstract void clear();

	/** Save the definition */
	protected void saveDefinition(BufferedWriter out) {
		try {
			comment(out, "ced Plot Definition File");
			writeDelimitted(out, "TYPE", getPlotType()); //type
			
			Vector<ICut> cuts = getCuts();
			if ((cuts != null) && !cuts.isEmpty()) {
				comment(out, "cuts-- count = " + cuts.size());
				for (ICut cut:cuts) {
					out.write(cut.getDefinition() + "\n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Write a delimitted string to the save file
	 * @param out theoutput stream
	 * @param sa the array of strings
	 * @throws IOException
	 */
	protected void writeDelimitted(BufferedWriter out, String... sa) throws IOException {
		String s = makeDelimittedString(sa);
		if (s != null) {
			out.write(s + "\n");
		}
	}
	protected void comment(BufferedWriter out, String s) throws IOException {
		out.write("\n" + "!" + s + "\n");
	}
	
	/** custom definitions */
	protected abstract void customWrite(BufferedWriter out);
	
	//select file for saving
	protected File getSaveDefinitionFile() {
		File selectedFile = null;
		JFileChooser chooser = new JFileChooser(_saveDir);
		
		File defFile = new File(_saveDir, "plotdef.pltd");
		chooser.setSelectedFile(defFile);
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
	
	/**
	 * Get a string representing the type
	 * @return a string representing the type
	 */
	public abstract String getPlotType();
	    
    /**
     * Make a single delimited string out of collection 
     * @param sa the array of strings
     * @return the delimitted string
     */
    public static String makeDelimittedString(String... sa) {
    	if (sa == null) {
    		return null;
    	}
    	
    	String s = sa[0];
    	
    	if (sa.length > 1) {
    		for (int i = 1; i < sa.length; i++) {
    			s = s + DELIMIT + sa[i];
    		}
    	}
    	
    	return s;
    }
    
    /**
     * Get the tokens from a string
     * @param s the string 
     * @return the tokens
     */
    public static String[] getTokens(String s) {
    	if (s == null) {
    		return null;
    	}
    	return FileUtilities.tokens(s, DELIMIT);
    }
	
}
