package cnuphys.bCNU.menu;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import cnuphys.bCNU.application.Desktop;

/**
 * The global file menu common to all bCNU applications
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class FileMenu extends JMenu {

	public static final String menuLabel = "File";

	// create the file menu
	public FileMenu() {
		super(menuLabel);
		MenuManager.setFileMenu(this);
		addSaveConfigurationItem();
		addClearConfigurationItem();
		addSeparator();
		addQuitItem();
	}

	// add the save configuration menu item
	private void addSaveConfigurationItem() {
		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				Desktop.getInstance().writeConfigurationFile();
			}
		};

		JMenuItem item = new JMenuItem("Save View Configuration...");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit
				.getDefaultToolkit().getMenuShortcutKeyMask()));
		item.addActionListener(al);
		add(item);
	}

	// add the clear configuration menu item
	private void addClearConfigurationItem() {
		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				Desktop.getInstance().deleteConfigurationFile();
				// Delete file here
			}
		};

		JMenuItem item = new JMenuItem("Delete View Configuration");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, Toolkit
				.getDefaultToolkit().getMenuShortcutKeyMask()));
		item.addActionListener(al);
		add(item);
	}

	// add the quit menu item
	private void addQuitItem() {
		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		};

		JMenuItem item = new JMenuItem("Quit");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit
				.getDefaultToolkit().getMenuShortcutKeyMask()));
		item.addActionListener(al);
		add(item);
	}
}
