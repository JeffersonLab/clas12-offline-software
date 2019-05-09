package cnuphys.splot.plot;

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.Hashtable;

import javax.swing.ImageIcon;

public class ImageManager {

	public static ImageIcon cnuIcon = ImageManager.getInstance().loadImageIcon("images/cnuicon.png");

	/**
	 * A memory only cache for Images (not ImageIcons).
	 */
	private Hashtable<URL, Image> imageHashtable = new Hashtable<URL, Image>(137);

	/**
	 * A memory only cache for ImageIcons (not Images).
	 */
	private Hashtable<String, ImageIcon> imageIconHashtable = new Hashtable<String, ImageIcon>(193);

	/**
	 * singleton
	 */
	private static ImageManager imageManager = getInstance();

	/**
	 * This constructor takes no arguments. It is the private constructor used to
	 * make the singleton.
	 */
	private ImageManager() {
	}

	/**
	 * Access to the image manager singleton
	 * 
	 * @return the image manager singleton.
	 */
	public static ImageManager getInstance() {
		if (imageManager == null) {
			try {
				imageManager = new ImageManager();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		return imageManager;
	}

	/**
	 * Load an image from file or the jar file.
	 * 
	 * @param imageFileName the image files name.
	 * @param component     a component to serve as an ImageObserver(any component
	 *                      should do).
	 * @return the image
	 */
	public Image loadImage(String imageFileName, Component component) {

		URL imageURL = getClass().getClassLoader().getResource(imageFileName);
		Image image = null;

		if (imageURL != null) {

			// try the hashtable first
			image = (imageHashtable.get(imageURL));

			if (image == null) {
				image = loadImageFromURL(imageURL, component);
				if (image != null) {
					imageHashtable.put(imageURL, image);
				}
			}
		}

		return image;
	}

	/**
	 * Load an image icon from a buffered image.
	 * 
	 * @param bufferedImage the buffered image.
	 * @param hashKey       the hashtable key.
	 * @return the image icon.
	 */
	public ImageIcon loadImageIcon(BufferedImage bufferedImage, String hashKey) {
		if (bufferedImage == null) {
			return null;
		}

		ImageIcon icon = imageIconHashtable.get(hashKey);
		if (icon != null) {
			return icon;
		}

		if (icon == null) {
			icon = new ImageIcon(bufferedImage);

			if (icon != null) {
				imageIconHashtable.put(hashKey, icon);
			}
		}

		return icon;
	}

	/**
	 * Load an image icon from file or the jar file.
	 * 
	 * @param imageFileName the image file name, relative to class path.
	 * @return the loaded ImageIcon, or <code>null</code>.
	 */
	public ImageIcon loadImageIcon(String imageFileName) {

		// try the image icon cache
		ImageIcon icon = imageIconHashtable.get(imageFileName);
		if (icon != null) {
			return icon;
		}

		// try from local file
		File file = new File(imageFileName);
		if (file.exists() && file.canRead()) {
			icon = new ImageIcon(imageFileName);
			if (icon != null) {
				imageIconHashtable.put(imageFileName, icon);
				return icon;
			}
		}

		URL imageURL = getClass().getClassLoader().getResource(imageFileName);

		if (imageURL != null) {

			icon = new ImageIcon(imageURL);
			if (icon != null) {
				imageIconHashtable.put(imageFileName, icon);
			}
		}

		return icon;
	}

	/**
	 * Load an image from a local file.
	 * 
	 * @param fileName  the name of the file holding the image. It will first treat
	 *                  this as an absolute path. If that fails, it will try to uses
	 *                  it relative to the current working directory. If that fails,
	 *                  relative to the home directory. If that fails, we cave.
	 * @param component a component to use as an observer. Any component should do.
	 * @return the loaded image, or <code>null</code>.
	 */
	public Image loadImageFromFile(String fileName, Component component) {

		File file = new File(fileName);
		if (!file.exists() || !file.canRead()) {
			file = new File(Environment.getInstance().getCurrentWorkingDirectory(), fileName);
			if (!file.exists() || !file.canRead()) {
				file = new File(Environment.getInstance().getHomeDirectory(), fileName);
				if (!file.exists() || !file.canRead()) {
					Throwable t = new Throwable("Could not find image file: " + fileName);
					t.printStackTrace();
					return null;
				}
			}
		}

		Image image = Toolkit.getDefaultToolkit().getImage(file.getPath());

		try {
			int id = 0;
			MediaTracker tracker = new MediaTracker(component);
			tracker.addImage(image, id);
			tracker.waitForAll();

		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return image;
	}

	/**
	 * Load an image from a URL
	 * 
	 * @param url The url of the image
	 * @param c   A component to use as an observer
	 * @return the image found at the url
	 */
	public Image loadImageFromURL(URL url, Component c) {
		Image image = Toolkit.getDefaultToolkit().getImage(url);

		try {

			int id = 0;
			MediaTracker tracker = new MediaTracker(c);
			tracker.addImage(image, id);
			tracker.waitForAll();

		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return image;
	}

	/**
	 * Try to obtain an ImageIcon from the cache.
	 * 
	 * @param key the has key.
	 * @return an ImageIcom from the key, or null.
	 */
	public ImageIcon get(String key) {
		return imageIconHashtable.get(key);
	}

	/**
	 * Place an ImageIcon into the cache.
	 * 
	 * @param key       the hask key to use.
	 * @param imageIcon the ImageIcon to cache.
	 */
	public void put(String key, ImageIcon imageIcon) {
		if (imageIcon != null) {
			imageIconHashtable.put(key, imageIcon);
		}
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
