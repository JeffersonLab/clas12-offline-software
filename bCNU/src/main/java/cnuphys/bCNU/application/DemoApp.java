package cnuphys.bCNU.application;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.geom.Rectangle2D.Double;

import cnuphys.bCNU.attributes.AttributeType;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.graphics.style.SymbolType;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.plugin.Plugin;
import cnuphys.bCNU.plugin.PluginProperties;
import cnuphys.bCNU.plugin.shapes.PluginLine;
import cnuphys.bCNU.plugin.shapes.PluginShape;
import cnuphys.bCNU.plugin.shapes.PluginSquare;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.FileUtilities;
import cnuphys.bCNU.view.DrawingView;
import cnuphys.bCNU.view.LogView;
import cnuphys.bCNU.view.ViewManager;
import cnuphys.bCNU.view.XMLView;

/**
 * Demonstrates and tests the generic views
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class DemoApp extends BaseMDIApplication {

    // the singleton
    private static DemoApp instance;

    /**
     * Constructor (private--used to create singleton)
     * 
     * @param keyVals an optional variable length list of attributes in
     *            type-value pairs. For example, AttributeType.NAME,
     *            "my application", AttributeType.CENTER, true, etc.
     */
    private DemoApp(Object... keyVals) {
	super(keyVals);
    }

    /**
     * Public access to the singleton.
     * 
     * @return the singleton (the main application frame.)(
     */
    public static DemoApp getInstance() {
	if (instance == null) {
	    instance = new DemoApp(AttributeType.TITLE,
		    "Demo Application of Generic bCNU Views",
		    AttributeType.BACKGROUNDIMAGE, "images/cnu.png",
		    AttributeType.FRACTION, 0.8);

	    instance.addInitialViews();
	}
	return instance;
    }

    /**
     * Add the initial views to the desktop.
     */
    private void addInitialViews() {

	// add logview
	LogView logView = new LogView();
	logView.setVisible(false);
	ViewManager.getInstance().getViewMenu().addSeparator();

	// log some environment info
	Log.getInstance().config(Environment.getInstance().toString());

	// drawing view
	DrawingView drawingView = DrawingView.createDrawingView();
	drawingView.setVisible(true);

	// xml view
	XMLView xmlView = new XMLView();
	xmlView.setVisible(false);

	//Sample plugin
	
	//Sample plugin
	new Plugin() {

	    @Override
	    public void initializePluginWorld(Double world) {
		world.x = -1;   //xmin
		world.y = -1;   //ymin
		world.width = 2;
		world.height = 2;
	    }

	    @Override
	    public void customizePlugin() {
		addProperty(PluginProperties.WIDTH, 800);
		addProperty(PluginProperties.HEIGHT, 600);
		addProperty(PluginProperties.BACKGROUND, "cadet blue");
	    }

	    @Override
	    public String getPluginTitle() {
		return "Fancy Plugin";
	    }

	    @Override
	    public void addInitialShapes() {
		
		addSymbol("A symbol", -0.8, -0.25, 
			PluginProperties.FILLCOLOR, "Aquamarine",
			PluginProperties.LINECOLOR, Color.yellow,
			PluginProperties.LINEWIDTH, 2,
			PluginProperties.SYMBOL, SymbolType.UPTRIANGLE,
			PluginProperties.SYMBOLSIZE, 20,
			PluginProperties.LOCKED, false);

		addSquare("A draggable square", 0.2, -0.3, 0.5, 
			PluginProperties.FILLCOLOR, "wheat",
			PluginProperties.LINECOLOR, Color.red,
			PluginProperties.LINEWIDTH, 3,
			PluginProperties.LINESTYLE, LineStyle.DASH,
			PluginProperties.LOCKED, false);
		
		addLine("A draggable line", 0.75, 0, -0.25, 0.3,
			PluginProperties.LINECOLOR, Color.yellow,
			PluginProperties.LINEWIDTH, 3,
			PluginProperties.LOCKED, false);
		
		addRectangle("Intially rotated rectangle", -0.8, 0.1, .4, .8,
			PluginProperties.LOCKED, false,
			PluginProperties.ROTATED, 20);
		
		addEllipse("Intially rotated ellipse", -0.07, 0.6, .5, .3,
			PluginProperties.LOCKED, false,
			PluginProperties.ROTATED, -20,
			PluginProperties.FILLCOLOR, new Color(60, 90, 120, 120));
		
		addCircle("A circle", 0.731, -0.684, 0.269, 
			PluginProperties.FILLCOLOR, "orange red",
			PluginProperties.LINECOLOR, Color.green,
			PluginProperties.LINEWIDTH, 2,
			PluginProperties.LINESTYLE, LineStyle.DOT,
			PluginProperties.LOCKED, false);
		
		
		double x[] = {.1, .2, .6, .5, .3};
		double y[] = {.1, .5, .6, .4, 0};
		addPolygon("A closed polygon", x, y, PluginProperties.LOCKED, false);
		
		for (int i = 0; i < x.length; i++) {
		    x[i] = -x[i];
		    y[i] = -y[i];
		}
		addPolyline("A polyline", x, y, PluginProperties.LOCKED, false);
	    }
	    
	    @Override
	    public void mouseOverShape(PluginShape shape) {
		updateStatus((shape == null) ? null : shape.getInfoString());
	    }

	    @Override
	    public void shapeClick(PluginShape shape, int clickCount, Point pixelPoint) {
		updateStatus((shape == null) ? null : "click count "+ clickCount + " on " + shape.getInfoString());
	    }

	    @Override
	    public void shapePopupTrigger(PluginShape shape, Point pixelPoint) {
		updateStatus((shape == null) ? null : "popup trigger on " + shape.getInfoString());
	    }

	    
	};

    }

    /**
     * Main program used for testing only.
     * <p>
     * Command line arguments:</br>
     * -p [dir] dir is the optional default directory for the file manager
     * 
     * @param arg the command line arguments.
     */
    public static void main(String[] arg) {

	if ((arg != null) && (arg.length > 0)) {
	    int len = arg.length;
	    int lm1 = len - 1;
	    boolean done = false;
	    int i = 0;
	    while (!done) {
		if (arg[i].equalsIgnoreCase("-p")) {
		    if (i < lm1) {
			i++;
			FileUtilities.setDefaultDir(arg[i]);
		    }
		}
		i++;
		done = (i >= lm1);
	    }
	}

	final DemoApp frame = getInstance();

	// now make the frame visible, in the AWT thread
	EventQueue.invokeLater(new Runnable() {

	    @Override
	    public void run() {
		frame.setVisible(true);
	    }

	});
	Log.getInstance().error("DemoApp is ready.");
    }
}
