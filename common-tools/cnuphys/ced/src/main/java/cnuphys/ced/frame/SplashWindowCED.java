package cnuphys.ced.frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.util.Fonts;


@SuppressWarnings("serial")
public class SplashWindowCED extends JWindow {

    private JComponent _center;

    boolean tile;
    private ImageIcon _icon;
    private Dimension _tileSize;

    private JButton closeButton;

    public SplashWindowCED(String title, Color bg, int width, String backgroundImage, String version) {

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
        addNorth(title, version);
        pack();
        GraphicsUtilities.centerComponent(this);
    }

    private void addNorth(String title, String version) {
        JPanel sp = new JPanel() {
//        	@Override
//        	public Dimension getPreferredSize() {
//        		Dimension d = super.getPreferredSize();
//        		d.height += 50;
//        		return d;
//        	}
        	
        	@Override
        	public Insets getInsets() {
        		Insets def = super.getInsets();
        		return new Insets(def.top + 4, def.left + 4, def.bottom + 4,
        				def.right + 4);
        	}

        };
        
        sp.setLayout(new FlowLayout(FlowLayout.CENTER, 100, 0));
		sp.setBackground(Color.white);

		String imageNames[] = {
				"images/anicat.gif",
				"images/anir2d2.gif",
				"images/sun.gif",
				"images/rubik80.gif",
				"images/bee.gif",
				"images/loading.gif",
				"images/progress33.gif",
				"images/saucer.gif",
				"images/spinglobe.gif",
				"images/runner.gif",
				"images/sun2.gif",
				"images/push.gif",
				"images/walker.gif",
				"images/rooftop.gif",
				"images/bee2.gif",
		        "images/stickdancer.gif",
		        "images/cricket.gif",
				"images/wiggly.gif"};
		

		
		
		int index = (new Random()).nextInt(imageNames.length);
		if (index < 0) {
			System.err.println("Bad index in splashWindow: " + index);
			index = 0;
		}
		else if (index >= imageNames.length) {
			System.err.println("Bad index in splashWindow: " + index);
			index = imageNames.length-1;
		}
		ImageIcon icon = ImageManager.getInstance().loadImageIcon(imageNames[index]);
		if ((icon != null) && (icon.getImage() != null)) {
			JLabel rlab = new JLabel(icon);
			sp.add(rlab);
		}


        String labelText = title;
        if (version != null) {
            labelText += " " + version;
        }
        final JLabel label = new JLabel(labelText);
        label.setFont(Fonts.defaultBoldFont);        
        sp.add(label);

        //close button
        closeButton = new JButton("Close") {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                if (d.height > label.getHeight()) {
                    d.height = label.getHeight();
                }
                return d;
            }
        };
        
        ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
        };
        
        closeButton.addActionListener(al);
        sp.add(closeButton);

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

        _center.setSize(new Dimension(width, 300));
        add(_center, BorderLayout.CENTER);

    }

}