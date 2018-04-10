package cnuphys.bCNU.simanneal.example;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JComponent;

import cnuphys.bCNU.graphics.SymbolDraw;
import cnuphys.bCNU.simanneal.IUpdateListener;
import cnuphys.bCNU.simanneal.Simulation;
import cnuphys.bCNU.simanneal.Solution;
import cnuphys.splot.plot.X11Colors;

public class TSDisplay extends JComponent implements IUpdateListener {
	
	//for converting to screen coordinates
	private final double vmin = -0.05;
	private final double vmax = 1.1;

	
	//the simulation
	private TravelingSalesperson _travPerson;
	
	public TSDisplay(TravelingSalesperson travPerson) {
		_travPerson = travPerson;
		setOpaque(false);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		Rectangle b = getBounds();
		g.setColor(X11Colors.getX11Color("Alice Blue"));
		g.fillRect(b.x, b.y, b.width, b.height);
		g.setColor(X11Colors.getX11Color("Dark Blue"));
		g.drawRect(b.x, b.y, b.width-1, b.height-1);
		
		TravelingSalesperson ts = _travPerson.getCurrentSolution();
		TSCity cities[] = ts.getCities();
		int itinerary[] = ts.getItinerary();
		int len = itinerary.length;
		Point p0 = new Point();
		Point p1 = new Point();
		
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
		
				
		//now draw the cities
		for (TSCity city : cities) {
			cityToLocal(p0, city);
			SymbolDraw.drawOval(g, p0.x, p0.y, 3, 3, Color.black, Color.yellow);
		}
		
	}
	
	
	private void cityToLocal(Point pp, TSCity city) {
		Rectangle b = getBounds();
		double x = city.x;
		double y = city.y;
		double del = vmax-vmin;
		pp.x = (int) (b.x + (x-vmin)*b.width/del);
		pp.y = (int) (b.y + (vmax-y)*b.height/del);
	}

	@Override
	public void updateSolution(Simulation simulation, Solution newSolution, Solution oldSolution) {
		repaint();
	}

}
