package cnuphys.bCNU.graphics.splashscreen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;

import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.graphics.component.StreamCapturePane;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.TextUtilities;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.splot.plot.GraphicsUtilities;

public class SplashWindow extends JWindow {
	
	private JComponent _center;
	
	private StreamCapturePane _scp;
	
	// If <code>true</code>, tile the background with an image.
	boolean tile;

	// the image for tiling
	private ImageIcon _icon;

	// The size of a tile.
	private Dimension _tileSize;
	
	private String _version;
	
	private Font _font = Fonts.commonFont(Font.BOLD, 18);

	public SplashWindow(String title, Color bg, int width, String backgroundImage, String version) {
		
		_version = version;
		setLayout(new BorderLayout(2, 2));
		
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

		
		addCenter(bg, width);
		addSouth(width);
		addNorth(title);
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
	
	private void addNorth(String title) {
		JPanel sp = new JPanel();
		sp.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 2));
		JLabel label = new JLabel(title);
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
		add(sp, BorderLayout.NORTH);
	}

	private void addCenter(final Color bg, final int width) {
		_center = new JComponent() {
			
			private int prefH = (_icon == null) ? 300 : _icon.getIconHeight() ;
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
				}
				else {
					tile(g);
				}
				
				if (_version != null) {
					g.setFont(_font);
					FontMetrics fm = getFontMetrics(_font);
					int w = fm.stringWidth("" + _version + " ");
					int h = fm.getHeight()+ 8;
					
					GraphicsUtilities.drawSimple3DRect(g, 4, 4, w, h, X11Colors.getX11Color("dark red"), false);
					g.setColor(Color.white);
					g.drawString(_version, 6, h-4);
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
		add(_center, BorderLayout.CENTER);

	}
	
	private void addSouth(final int width) {
		_scp = new StreamCapturePane() {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(width, 300);
			}
		};
		
		add(_scp, BorderLayout.SOUTH);
	}
}
