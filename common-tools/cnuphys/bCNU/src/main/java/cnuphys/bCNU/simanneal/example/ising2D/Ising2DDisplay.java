package cnuphys.bCNU.simanneal.example.ising2D;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import cnuphys.bCNU.simanneal.SimulationDisplay;
import cnuphys.lund.X11Colors;

public class Ising2DDisplay extends SimulationDisplay {

	private Rectangle _cell = new Rectangle();
	
	private final Color _spinUpColor = X11Colors.getX11Color("wheat");
	private final Color _spinDownColor = X11Colors.getX11Color("dark red");
	
	private final Color[] _spinColors = {_spinDownColor, null, _spinUpColor};

	public Ising2DDisplay(Ising2DSimulation simulation) {
		super(simulation);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		Rectangle b = getBounds();
		b.grow(-1, -1);
		
		Ising2DSolution i2dSolution = currentSolution();
		
		for (int row = 0; row < getSimulation().getNumRows(); row++) {
			for (int col = 0; col < getSimulation().getNumColumns(); col++) {
				setCell(b, row, col, _cell);
				
				int spin = currentSolution().getSpin(row, col);
				g.setColor(_spinColors[spin+1]);
				g.fillRect(_cell.x, _cell.y, _cell.width, _cell.height);
			
				g.setColor(Color.gray);
				g.drawRect(_cell.x, _cell.y, _cell.width, _cell.height);
			}
			
		}
		
	}
	
	//get the current solution
	private Ising2DSolution currentSolution() {
		return (Ising2DSolution)(_simulation.currentSolution());
	}
	
	/**
	 * Get the underlying simulation as an Ising2DSimulation
	 * @return the underlying simulation as an Ising2DSimulation
	 */
	private Ising2DSimulation getSimulation() {
		return (Ising2DSimulation)_simulation;
	}
	
	private void setCell(Rectangle bounds, int row, int col, Rectangle cell) {
		int numrow = getSimulation().getNumRows();
		int numcol = getSimulation().getNumColumns();
		int delX = bounds.width/numcol;
		int delY = bounds.height/numrow;
		
		int x = bounds.x + col*delX;
		int y = bounds.y + row*delY;
		
		cell.setBounds(x, y, delX, delY);
		
	}

}
