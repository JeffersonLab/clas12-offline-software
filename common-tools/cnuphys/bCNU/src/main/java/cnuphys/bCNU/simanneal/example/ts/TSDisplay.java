package cnuphys.bCNU.simanneal.example.ts;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import cnuphys.bCNU.graphics.SymbolDraw;
import cnuphys.bCNU.simanneal.SimulationDisplay;
import cnuphys.splot.plot.X11Colors;

/**
 * This is the map
 * @author heddle
 *
 */
public class TSDisplay extends SimulationDisplay {
	
	//for converting to screen coordinates
	private final double vmin = -0.01;
	private final double vmax = 1.02;

		
	public TSDisplay(TSSimulation simulation) {
		super(simulation);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		Rectangle b = getBounds();
		g.setColor(X11Colors.getX11Color("Alice Blue"));
		g.fillRect(b.x, b.y, b.width, b.height);
		g.setColor(X11Colors.getX11Color("Dark Blue"));
		g.drawRect(b.x, b.y, b.width-1, b.height-1);
		
		TSSolution ts = (TSSolution)(_simulation.currentSolution());
		TSCity cities[] = ts.getCities();
		int itinerary[] = ts.getItinerary();
		int len = itinerary.length;
		Point p0 = new Point();
		Point p1 = new Point();
		
		//draw the river
		g.setColor(Color.blue);
		worldToLocal(p0, 0.5, 2);
		worldToLocal(p1, 0.5, -2);
		
		int top = Math.max(b.y,  p0.y);
		g.drawLine(p0.x-1, top, p1.x-1, p1.y);
		g.drawLine(p0.x, top, p1.x, p1.y);
		g.drawLine(p0.x+1, top, p1.x+1, p1.y);
		
		//draw the connections
		g.setColor(X11Colors.getX11Color("Dark Red"));
		for (int i = 0; i < len; i++) {
			int j = (i+1)%len;
			TSCity c0 = cities[itinerary[i]];
			TSCity c1 = cities[itinerary[j]];
			cityToLocal(p0, c0);
			cityToLocal(p1, c1);
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
		}
		
//		System.out.println("NUM CITY: " + cities.length);
				
		//now draw the cities
		for (TSCity city : cities) {
			cityToLocal(p0, city);
			SymbolDraw.drawOval(g, p0.x, p0.y, 3, 3, Color.black, Color.yellow);
		}
		
	}
	
	
	private void cityToLocal(Point pp, TSCity city) {
		worldToLocal(pp, city.x, city.y);
	}
	
	private void worldToLocal(Point pp, double x, double y) {
		Rectangle b = getBounds();
		double del = vmax-vmin;
		pp.x = (int) (b.x + (x-vmin)*b.width/del);
		pp.y = (int) (b.y + (vmax-y)*b.height/del);		
	}

}
