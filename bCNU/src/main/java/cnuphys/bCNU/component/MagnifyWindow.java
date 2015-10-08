package cnuphys.bCNU.component;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.JWindow;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.container.BaseContainer;

public class MagnifyWindow extends JWindow {

    private static MagnifyWindow _magnifyWindow;

    // size of the magnify window
    private static int _WIDTH = 200;
    private static int _HEIGHT = 200;

    private static int OFFSET = 10; // from pointer location

    // magnification factor
    private static double _MAGFACTOR = 4.;

    // mouse location relative to container
    private static Point _mouseLocation;

    // the drawing container
    private static BaseContainer _container;
    
    /**
     * Create a translucent window
     */
    private MagnifyWindow() {
	setLayout(new BorderLayout(0, 0));
	setSize(_WIDTH, _HEIGHT);
	
	_container = new BaseContainer(new Rectangle.Double(0, 0, 1, 1), false);

	add(_container, BorderLayout.CENTER);
    }


    /**
     * Magnify a view
     * 
     * @param sContainer the container to magnify
     * @param me the mouse event which contains the location
     */
    public static synchronized void magnify(BaseContainer sContainer, MouseEvent me,
	    IDrawable drawable) {
	if (_magnifyWindow == null) {
	    _magnifyWindow = new MagnifyWindow();
	}

	_mouseLocation = new Point(me.getPoint());

	// get the screen mouse location
	Point p = MouseInfo.getPointerInfo().getLocation();

	// where to place magnify window
	int screenX = p.x;
	int screenY = p.y;

	int x = screenX - _WIDTH - OFFSET;
	if (x < 0) {
	    x = screenX + OFFSET;
	}
	int y = screenY - _HEIGHT - OFFSET;
	if (y < 0) {
	    y = screenY + OFFSET;
	}

	_magnifyWindow.setLocation(x, y);

	_magnifyWindow.setVisible(true);
	_magnifyWindow.toFront();

	_container.setWorldSystem(getMagWorld(sContainer));
	_container.shareModel(sContainer);
	_container.setDirty(true);
	_container.refresh();
    }
    
    //get the world for the mag container
   
    private static Rectangle2D.Double getMagWorld(BaseContainer sContainer) { 
	
	Rectangle bounds = _container.getBounds();
	Rectangle sBounds = sContainer.getBounds();
	
	Rectangle2D.Double sWorld = sContainer.getWorldSystem();
	Rectangle2D.Double wr = new Rectangle2D.Double(sWorld.x, sWorld.y,
		sWorld.width, sWorld.height);

	Point pp = new Point(_mouseLocation.x, _mouseLocation.y);
	Point.Double wp = new Point.Double();
	sContainer.localToWorld(pp, wp);
	
	double scaleX = ((double)sBounds.width)/((double)bounds.width);
	double scaleY = ((double)sBounds.height)/((double)bounds.height);

	double ww = sWorld.width / (scaleX*_MAGFACTOR);
	double hh = sWorld.height / (scaleY*_MAGFACTOR);

	wr.setFrame(wp.x - ww / 2, wp.y - hh / 2, ww, hh);
	
	return wr;
    }

    /**
     * Hide the magnify window
     */
    public static void closeMagnifyWindow() {
	System.err.println("CLOSE MAG WINDOW");
	if ((_magnifyWindow != null) && (_magnifyWindow.isVisible())) {
	    _magnifyWindow.setVisible(false);
	}
    }

    @Override
    public Insets getInsets() {
	Insets def = super.getInsets();
	return new Insets(def.top + 2, def.left + 2, def.bottom + 2,
		def.right + 2);
    }
    
}
