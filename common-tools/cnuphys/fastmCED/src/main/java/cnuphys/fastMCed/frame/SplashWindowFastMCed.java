package cnuphys.fastMCed.frame;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.X11Colors;


@SuppressWarnings("serial")
public class SplashWindowFastMCed extends JWindow {

    private ImageIcon _cnu2;
    private Dimension _tileSize;

    //close (and kill the startup)
    private JButton _closeButton;

    //swing timer to bring in spme pics
    private Timer _timer;
    
    //for random pics
    private String _cedPics[] = {
			"images/cedSS1.png",
			"images/cedSS2.png",
			"images/cedSS3.png",
			"images/cedSS4.png",
			"images/cedSS5.png",
			"images/cedSS6.png",
			"images/cedSS7.png",
			"images/cedSS8.png",
			"images/cedSS9.png",
			"images/cedSS10.png",
			"images/cedSS11.png",
			"images/cedSS12.png",
			"images/cedSS13.png",
			"images/cedSS14.png",
			"images/cedSS15.png",
			"images/cedSS16.png",
  		
    };
    private int _picIndex = (int)(Integer.MAX_VALUE*Math.random()) %  _cedPics.length;
    private ImageIcon _cedImage;
    private JLabel _cedLabel;
    
    /**
     * Create the ced splash window
     * @param title
     * @param bg
     * @param width
     * @param backgroundImage
     * @param version
     */
    public SplashWindowFastMCed(String title, Color bg, int width, String version) {

		setLayout(new BorderLayout(2, 2));

		for (int i = 0; i < 30; i++) {
			int r1 = ThreadLocalRandom.current().nextInt(0, _cedPics.length);
			int r2 = ThreadLocalRandom.current().nextInt(0, _cedPics.length);

			if (r1 != r2) {
				String temp = _cedPics[r1];
				_cedPics[r1] = _cedPics[r2];
				_cedPics[r2] = temp;
			}
		}
        
        addTimer(2500);

        addSouth();
        addCenter(bg, (_cnu2 != null) ? _cnu2.getIconWidth() : width);
        addNorth(title, version);
        pack();
        GraphicsUtilities.centerComponent(this);
    }
    
    @Override
    public void setVisible(boolean vis) {
    	if ((_timer != null) && !vis) {
			_timer.stop();
    	}

		super.setVisible(vis);
	}

	// add a timer to swap pics
	private void addTimer(int millis) {
		ActionListener taskPerformer = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				setImage();
			}
		};
		_timer = new Timer(0, taskPerformer);
		_timer.setDelay(millis);

		_timer.start();
	}

	private void setImage() {
    	_cedImage = ImageManager.getInstance().loadImageIcon(_cedPics[_picIndex]);
    	_picIndex = (_picIndex + 1) % _cedPics.length;
    	
    	if (_cedLabel != null) {
    		_cedLabel.setIcon(_cedImage);
    	}
    }
    
    private void addSouth() {
    	_cnu2 = ImageManager.cnu2;
    	if (_cnu2 == null) {
    		return;
    	}
    	
    	final Dimension size = new Dimension(_cnu2.getIconWidth()+20, _cnu2.getIconHeight()+20);
     	
    	JLabel label = new JLabel("") {
    		
//        	@Override
//        	public Insets getInsets() {
//        		Insets def = super.getInsets();
//        		return new Insets(def.top + 4, def.left + 4, def.bottom + 4,
//        				def.right + 4);
//        	}

           	@Override
        		public Dimension getPreferredSize() {
        			return size;
        		}
    	};
    	label.setIcon(_cnu2);
    	Border outerBorder = BorderFactory.createEtchedBorder();
    	Border emptyBorder = new EmptyBorder(10, 10, 10, 10); // top, left, bot, right
		label.setBorder(BorderFactory.createCompoundBorder(outerBorder, emptyBorder));
		
		label.setOpaque(true);
		label.setBackground(X11Colors.getX11Color("alice blue"));
    	
    	add(label, BorderLayout.SOUTH);
     }

    //add the north with the little animations
    private void addNorth(String title, String version) {
        JPanel sp = new JPanel() {
        	
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
        label.setFont(Fonts.defaultLargeFont);        
        sp.add(label);

        //close button
        _closeButton = new JButton("Close") {
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
        
        _closeButton.addActionListener(al);
        sp.add(_closeButton);

        sp.setBorder(BorderFactory.createEtchedBorder());
        
        add(sp, BorderLayout.NORTH);
    }

    private void addCenter(final Color bg, final int width) {
    	
      	
    	_cedLabel = new JLabel("") {
 
           	@Override
        		public Dimension getPreferredSize() {
        			return new Dimension(670, 420);
        		}
    	};
    	
    	setImage();
    	_cedLabel.setIcon(_cedImage);
    	Border outerBorder = BorderFactory.createEtchedBorder();
    	Border emptyBorder = new EmptyBorder(10, 10, 10, 10); // top, left, bot, right
    	_cedLabel.setBorder(BorderFactory.createCompoundBorder(outerBorder, emptyBorder));
		
    	_cedLabel.setOpaque(true);
    	_cedLabel.setBackground(X11Colors.getX11Color("dark gray"));
    	
    	add(_cedLabel, BorderLayout.CENTER);
    }

}