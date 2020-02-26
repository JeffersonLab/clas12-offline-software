package cnuphys.bCNU.application;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.metal.MetalIconFactory;

import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.bCNU.view.BaseView;

/**
 * This class is used for the desktop.
 * 
 * @author heddle
 */
@SuppressWarnings("serial")
public final class Desktop extends JDesktopPane {

	/**
	 * Used to operate (e.g., refresh) all views
	 */
	public static final int ALL_VIEWS = 0;

	/**
	 * Used to operate (e.g., refresh) top view only
	 */
	public static final int TOP_VIEW = 1;

	/**
	 * View configuration properties
	 */
	private Properties _properties;

	// If <code>true</code>, tile the background with an image.
	boolean tile;

	// the image for tiling
	private ImageIcon _icon;

	// The size of a tile.
	private Dimension _tileSize;

	// the singleton
	private static Desktop instance;
	
	//optional after drawer
	private IDrawable _afterDraw;

	/**
	 * Create a desktop pane.
	 * 
	 * @param background
	 *            optional background color.
	 * @param backgroundImage
	 *            optional background image. Will be tiled. Probably reference
	 *            into a jar file, such as "images/background.png".
	 */
	private Desktop(Color background, String backgroundImage) {
		initializeLookAndFeel();

		setDragMode(JDesktopPane.OUTLINE_DRAG_MODE); // faster
		setDoubleBuffered(true);

		if (background != null) {
			setBackground(background);
		} else {
			setBackground(X11Colors.getX11Color("royal blue"));
		}

		// tile?
		tile = false;
		if (backgroundImage != null) {
			_icon = ImageManager.getInstance().loadImageIcon(backgroundImage);
			if (_icon != null) {
				tile = true;
				_tileSize = new Dimension(_icon.getIconWidth(),
						_icon.getIconHeight());
				if ((_tileSize.width < 2) || (_tileSize.height < 2)) {
					tile = false;
				}
			}
		}

	}

	/**
	 * Create a desktop pane.
	 * 
	 * @param background
	 *            optional background color.
	 * @param backgroundImage
	 *            optional background image. Will be tiled. Probably reference
	 *            into a jar file, such as "images/background.png".
	 */
	public static Desktop createDesktop(Color background, String backgroundImage) {
		if (instance == null) {
			instance = new Desktop(background, backgroundImage);
		}
		return instance;
	}

	/**
	 * Access to the singleton
	 * 
	 * @return the singleton desktop
	 */
	public static Desktop getInstance() {
		return instance;
	}

	/**
	 * The paint method for the desktop. This is where the background
	 * image gets tiled
	 * 
	 * @param g
	 *            the graphics context.
	 */
	@Override
	public void paintComponent(Graphics g) {

		if (tile) {
			tile(g);
		} else {
			super.paintComponent(g);
		}
		
		if (_afterDraw != null) {
			_afterDraw.draw(g, null);
		}
	}

	/**
	 * Set an "after" draw
	 * @param afterDraw the drawable
	 */
	public void setAfterDraw(IDrawable afterDraw) {
		_afterDraw = afterDraw;
	}
	
	/**
	 * Tile the background.
	 * 
	 * @param g
	 *            the graphics context
	 */
	private void tile(Graphics g) {

		Rectangle bounds = getBounds();
		int ncol = bounds.width / _tileSize.width + 1;
		int nrow = bounds.height / _tileSize.height + 1;

		for (int i = 0; i < ncol; i++) {
			int x = i * _tileSize.width;
			for (int j = 0; j < nrow; j++) {
				int y = j * _tileSize.height;
				g.drawImage(_icon.getImage(), x, y, this);
			}
		}

	}

	/**
	 * Gets the top internal frame. Surprising that we had to write this.
	 * 
	 * @return the top internal frame.
	 */
	public JInternalFrame getTopFrame() {

		int minIndex = -1;
		int minZorder = 99999;

		JInternalFrame frames[] = getAllFrames();
		if (frames != null) {
			for (int index = 0; index < frames.length; index++) {
				if (getComponentZOrder(frames[index]) < minZorder) {
					minZorder = getComponentZOrder(frames[index]);
					minIndex = index;
				}
			}
		}

		if (minIndex < 0) {
			return null;
		}
		return frames[minIndex];
	}

	/**
	 * Refresh all the views.
	 */
	public void refresh() {
		refresh(ALL_VIEWS);
	}

	/**
	 * Refresh all or just the top view.
	 * 
	 * @param opt
	 *            one of the class constants such as ALL_VIEWS.
	 */
	public void refresh(int opt) {
		if (opt == ALL_VIEWS) {
			JInternalFrame frames[] = getAllFrames();
			if (frames != null) {
				for (JInternalFrame frame : frames) {
					frame.repaint();
				}
			}
		} else if (opt == TOP_VIEW) {
			JInternalFrame frame = getTopFrame();
			if (frame != null) {
				frame.repaint();
			}
		}
	}

	/**
	 * Load the configuration file that preserves the an arrangement of views.
	 */
	public void loadConfigurationFile() {

		File file = Environment.getInstance().getConfigurationFile();

		if ((file != null) && file.exists() && file.canRead()) {
			try {
				FileInputStream fis = new FileInputStream(file);
				_properties = new Properties();
				try {
					_properties.loadFromXML(fis);
				} catch (InvalidPropertiesFormatException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				Log.getInstance().info("Loaded a configuration file from [" + file.getPath() + "]");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		else {
			Log.getInstance().info("Did not load a configuration file from [" + file.getPath() + "]");
		}
	}

	/**
	 * configure the views based on the properties (which were read-in by
	 * loadConfigurationFile)
	 */
	public void configureViews() {
		JInternalFrame[] frames = getAllFrames();
		if (frames != null) {
			for (JInternalFrame frame : frames) {
				if (frame instanceof BaseView) {
					BaseView view = (BaseView) frame;
					view.setFromProperties(_properties);
				}

			}
		}
	}

	/**
	 * Write the configuration file that preserves the current arrangement of
	 * views.
	 */
	public void writeConfigurationFile() {

		File file = Environment.getInstance().getConfigurationFile();

		if (file.exists()) {
			int answer = JOptionPane.showConfirmDialog(null,
					file.getAbsolutePath()
							+ "  already exists.\nDo you want to overwrite it?",
					"Overwite Existing File?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
					ImageManager.cnuIcon);

			if (answer != JFileChooser.APPROVE_OPTION) {
				return;
			}
		} // end file exists check

		Properties properties = new Properties();
		JInternalFrame[] frames = getAllFrames();

		if (frames != null) {
			for (JInternalFrame frame : frames) {
				if (frame instanceof BaseView) {
					BaseView view = (BaseView) frame;
					Properties vprops = view.getConfigurationProperties();
					properties.putAll(vprops);
				}

			}
		}

		// write config file
		try {
			FileOutputStream fos = new FileOutputStream(file);
			try {
				properties.storeToXML(fos, null);
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Delete the configuration file that preserves the current arrangement of
	 * views.
	 */
	public void deleteConfigurationFile() {

		File file = Environment.getInstance().getConfigurationFile();

		if (file.exists()) {
			ImageIcon icon = ImageManager.getInstance().loadImageIcon("images/cnuicon.png");
			int answer = JOptionPane.showConfirmDialog(null,
					"Confim delete operation (this can not be undone).",
					"Delete Configuration?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, 
					ImageManager.cnuIcon);

			if (answer != JFileChooser.APPROVE_OPTION) {
				return;
			}
			file.delete();
		}
	}

	/**
	 * Initialize the look and feel.
	 */

	public void initializeLookAndFeel() {

		LookAndFeelInfo[] lnfinfo = UIManager.getInstalledLookAndFeels();

		String preferredLnF[];
		
		if (Environment.getInstance().isWindows()) {
			String arry[] = { UIManager.getSystemLookAndFeelClassName(), "Metal", "CDE/Motif", "Nimbus", 
					UIManager.getCrossPlatformLookAndFeelClassName() };
			preferredLnF = arry;
		}
		else {
			String arry[] = { UIManager.getSystemLookAndFeelClassName(),
					"Windows", UIManager.getCrossPlatformLookAndFeelClassName() };
			preferredLnF = arry;
		}
		
		if ((lnfinfo == null) || (lnfinfo.length < 1)) {
			System.err.println("No installed look and feels");
			return;
		}
//		else {
//			for (LookAndFeelInfo linfo : lnfinfo) {
//				System.err.println(" ****** [" + linfo.getName() + "]");
//			}
//		}

		for (String targetLnF : preferredLnF) {
			for (int i = 0; i < lnfinfo.length; i++) {
				String linfoName = lnfinfo[i].getClassName();
				if (linfoName.indexOf(targetLnF) >= 0) {
					try {
						UIManager.setLookAndFeel(lnfinfo[i].getClassName());
						UIDefaults defaults = UIManager.getDefaults();

						defaults.put("RadioButtonMenuItem.checkIcon",
								MetalIconFactory.getRadioButtonMenuItemIcon());
						return;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} //end for
	}

	/**
	 * Checks whether all views are ready for display.
	 * 
	 * @return <code>true</code> if all views are ready for display.
	 */
	public boolean isReady() {
		JInternalFrame[] frames = getAllFrames();

		if (frames != null) {
			for (JInternalFrame frame : frames) {
				if (frame instanceof BaseView) {
					BaseView view = (BaseView) frame;
					if (!view.isReady()) {
						return false;
					}
				}
			}
		}

		return true;
	}

	/**
	 * Singleton objects cannot be cloned, so we override clone to throw a
	 * CloneNotSupportedException.
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

}
