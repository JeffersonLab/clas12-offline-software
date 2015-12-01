package cnuphys.bCNU.application;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.geom.Rectangle2D.Double;

import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.graphics.style.SymbolType;
import cnuphys.bCNU.plugin.Plugin;
import cnuphys.bCNU.plugin.PluginProperties;
import cnuphys.bCNU.plugin.shapes.PluginShape;

public class DemoPlugin extends Plugin {

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
	
	addText("Some Text\\^2\\d\\_3", 0.5979, 0.5951,
			PluginProperties.TEXTCOLOR, "orange",
			PluginProperties.FONT, new Font(Font.MONOSPACED, Font.BOLD, 16),
			PluginProperties.LOCKED, false);
	
	addArc("An arc", -0.885, -0.7045, 0.2, 30, -120,
			PluginProperties.LINECOLOR, "dark green",
			PluginProperties.LINEWIDTH, 3,
			PluginProperties.LINESTYLE, LineStyle.DOT,
			PluginProperties.LOCKED, false);
	
	addDonut("A donut", -0.1392, 0.4939, 0.2, 0.4, 30, 120,
			PluginProperties.FILLCOLOR, "orange red",
			PluginProperties.LINECOLOR, "dark green",
			PluginProperties.LINEWIDTH, 3,
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



}
