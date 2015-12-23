package cnuphys.splot.plot;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import cnuphys.splot.edit.CurveEditorDialog;
import cnuphys.splot.edit.DialogUtilities;
import cnuphys.splot.xml.XmlSupport;

public class SplotMenus implements ActionListener {

	// the owner canvas
	private PlotCanvas _plotCanvas;

	// the menus
	protected JMenu _fileMenu;
	protected JMenu _editMenu;

	// the menu items
	protected JMenuItem _quitItem;
	protected JMenuItem _prefItem;
	protected JMenuItem _dataItem;
	protected JMenuItem _clearItem;
	protected JMenuItem _curveItem;
	protected JMenuItem _openItem;
	protected JMenuItem _saveItem;

	/**
	 * Create a set of menus and items for sPlot
	 * 
	 * @param canvas the plot canvas being controlled
	 * @param menuBar the menu bar
	 * @param addQuit if <code>true</code> include a quit item
	 */
	public SplotMenus(PlotCanvas canvas, JMenuBar menuBar, boolean addQuit) {
		_plotCanvas = canvas;
		makeMenus(menuBar, addQuit);
	}

	/**
	 * Create a set of menus and items for sPlot
	 * 
	 * @param canvas the plot canvas being controlled
	 * @param popup a popup to hold the menus
	 * @param addQuit if <code>true</code> include a quit item
	 */
	public SplotMenus(PlotCanvas canvas, JPopupMenu popup, boolean addQuit) {
		_plotCanvas = canvas;
		makeMenus(popup, addQuit);
	}

	// make the menus
	private void makeMenus(Container container, boolean addQuit) {
//		makeFileMenu(container, addQuit);
		makeEditMenu(container);
	}

	// make the file menu
	protected void makeFileMenu(Container container, boolean addQuit) {
		_fileMenu = new JMenu("File");
		_openItem = addMenuItem("Open...", 'O', _fileMenu);
		_saveItem = addMenuItem("Save...", 'S', _fileMenu);

		if (addQuit) {
			_fileMenu.addSeparator();
			_quitItem = addMenuItem("Quit", 'Q', _fileMenu);
		}
		container.add(_fileMenu);
	}

	// make the edit menu
	protected void makeEditMenu(Container container) {
		_editMenu = new JMenu("Edit");
		_prefItem = addMenuItem("Preferences...", 'P', _editMenu);
//		_dataItem = addMenuItem("Data...", 'D', _editMenu);
		_curveItem = addMenuItem("Curves...", 'C', _editMenu);
//		_editMenu.addSeparator();
//		_clearItem = addMenuItem("Clear Data", '\0', _editMenu);
		container.add(_editMenu);
	}

	/**
	 * Convenience routine for adding a menu item.
	 * 
	 * @param label the menu label.
	 * @param accelChar the accelerator character.
	 * @param menu the menu to add the item to.
	 */

	protected JMenuItem addMenuItem(String label, char accelChar, JMenu menu) {
		JMenuItem mitem = null;

		if ((label != null) && (menu != null)) {
			try {
				mitem = new JMenuItem(label);
				menu.add(mitem);
				if (accelChar != '\0') {
					KeyStroke keyStroke = KeyStroke
							.getKeyStroke("control " + accelChar);
					mitem.setAccelerator(keyStroke);
				}

				mitem.addActionListener(this);
			} catch (Exception e) {
			}
		}

		return mitem;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if (source == _openItem) {
			XmlSupport.open(_plotCanvas);
		}
		else if (source == _saveItem) {
			XmlSupport.save(_plotCanvas);
		}
		else if (source == _quitItem) {
			System.exit(0);
		}
		else if (source == _prefItem) {
			_plotCanvas.showPreferencesEditor();
		}
		else if (source == _dataItem) {
			_plotCanvas.showDataEditor();
		}
		else if (source == _curveItem) {
			CurveEditorDialog cd = new CurveEditorDialog(_plotCanvas);
			DialogUtilities.centerDialog(cd);
			cd.selectFirstCurve();
			cd.setVisible(true);
		}
		else if (source == _clearItem) {
			_plotCanvas.clearPlot();
		}
	}

	/**
	 * Get the underlying plot canvas
	 * 
	 * @return the plot canvas
	 */
	public PlotCanvas getPlotCanvas() {
		return _plotCanvas;
	}

	/**
	 * Get the file menu
	 * 
	 * @return the file menu
	 */
	public JMenu getFileMenu() {
		return _fileMenu;
	}

	/**
	 * Get the edit menu
	 * 
	 * @return the edit menu
	 */
	public JMenu getEditMenu() {
		return _editMenu;
	}

	/**
	 * Get the quit item (might be null)
	 * 
	 * @return the quit item
	 */
	public JMenuItem getQuitItem() {
		return _quitItem;
	}

	/**
	 * Get the preferences item
	 * 
	 * @return the preferences item
	 */
	public JMenuItem getPreferencesItem() {
		return _prefItem;
	}

	/**
	 * Get the clear (canvas) item
	 * 
	 * @return the clear item
	 */
	public JMenuItem getClearItem() {
		return _clearItem;
	}

	/**
	 * Get the curve (editor) item
	 * 
	 * @return the curve editor item
	 */
	public JMenuItem getCurveItem() {
		return _curveItem;
	}

	/**
	 * Get the open item
	 * 
	 * @return the open item
	 */
	public JMenuItem getOpenItem() {
		return _openItem;
	}

	/**
	 * Get the save item
	 * 
	 * @return the save item
	 */
	public JMenuItem getSaveItem() {
		return _saveItem;
	}

}
