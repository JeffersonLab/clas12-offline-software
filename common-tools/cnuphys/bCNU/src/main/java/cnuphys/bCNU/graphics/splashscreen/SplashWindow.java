package cnuphys.bCNU.graphics.splashscreen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;

import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.graphics.component.StreamCapturePane;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.splot.plot.GraphicsUtilities;

public class SplashWindow extends JWindow {

	private JComponent _center;

	private StreamCapturePane _scp;
	
	public static final String[] animatedGifs = { "images/anicat.gif", "images/anir2d2.gif", "images/coffee.gif", "images/sun.gif", "images/rubik80.gif",
			"images/bee.gif", "images/loading.gif", "images/progress33.gif", "images/saucer.gif",
			"images/spinglobe.gif", "images/runner.gif", "images/sun2.gif", "images/push.gif", "images/walker.gif",
			"images/rooftop.gif", "images/bee2.gif", "images/stickdancer.gif", "images/cricket.gif",
			"images/wiggly.gif" };
	


	// If <code>true</code>, tile the background with an image.
	boolean tile;

	// the image for tiling
	private ImageIcon _icon;

	// The size of a tile.
	private Dimension _tileSize;

	private Font _font = Fonts.commonFont(Font.BOLD, 18);

	public SplashWindow(String title, Color bg, int width, String backgroundImage, String version) {

		setLayout(new BorderLayout(2, 2));

		if (backgroundImage != null) {
			_icon = ImageManager.getInstance().loadImageIcon(backgroundImage);
			if (_icon != null) {
				tile = true;
				_tileSize = new Dimension(_icon.getIconWidth(), _icon.getIconHeight());
				if ((_tileSize.width < 2) || (_tileSize.height < 2)) {
					tile = false;
				}
			}
		}

		addCenter(bg, width);
		addSouth(width);
		addNorth(title, version);
		pack();
		GraphicsUtilities.centerComponent(this);
	}

	/**
	 * Write out the cached text
	 */
	public void writeCachedText() {
		if (_scp != null) {
			_scp.writeCachedText();
		}
	}

	@Override
	public void setVisible(boolean vis) {
		super.setVisible(vis);

		if (!vis) {
			if (_scp != null) {
				_scp.unCapture();
			}
		}
	}

	private void addNorth(String title, String version) {
		JPanel sp = new JPanel();
		sp.setLayout(new FlowLayout(FlowLayout.CENTER, 80, 2));
		sp.setBackground(Color.white);

		int index = (new Random()).nextInt(animatedGifs.length);
		if (index < 0) {
			System.err.println("Bad index in splashWindow: " + index);
			index = 0;
		} else if (index >= animatedGifs.length) {
			System.err.println("Bad index in splashWindow: " + index);
			index = animatedGifs.length - 1;
		}
		ImageIcon icon = ImageManager.getInstance().loadImageIcon(animatedGifs[index]);
		if ((icon != null) && (icon.getImage() != null)) {
			JLabel rlab = new JLabel(icon);
			sp.add(rlab);
		}

		String s = title;
		if (version != null) {
			s = s + " " + version;
		}
		JLabel label = new JLabel(s);
		label.setFont(_font);
		sp.add(label);

		final JButton cb = new JButton("Close");

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == cb) {
					setVisible(false);
					System.exit(1);
				}

			}

		};

		cb.addActionListener(al);
		sp.add(cb);

		sp.setBorder(BorderFactory.createEtchedBorder());
		add(sp, BorderLayout.NORTH);
	}

	private void addCenter(final Color bg, final int width) {
		_center = new JComponent() {

			private int prefH = (_icon == null) ? 300 : _icon.getIconHeight();

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(width, prefH);
			}

			@Override
			public void paintComponent(Graphics g) {

				if (_icon == null) {
					Rectangle b = getBounds();
					g.setColor(getBackground());
					g.fillRect(b.x, b.y, b.width, b.height);
				} else {
					tile(g);
				}

			}

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

		};

		_center.setOpaque(true);
		if (bg != null) {
			_center.setBackground(bg);
		} else {
			_center.setBackground(X11Colors.getX11Color("royal blue"));
		}

		_center.setSize(new Dimension(width, 300));

		_center.setBorder(BorderFactory.createEtchedBorder());
		add(_center, BorderLayout.CENTER);

	}

	private void addSouth(final int width) {
		_scp = new StreamCapturePane() {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(width, 300);
			}
		};

		_scp.setBorder(BorderFactory.createEtchedBorder());
		add(_scp, BorderLayout.SOUTH);
	}
}
