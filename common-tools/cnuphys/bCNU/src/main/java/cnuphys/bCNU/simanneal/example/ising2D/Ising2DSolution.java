package cnuphys.bCNU.simanneal.example.ising2D;

import java.util.Random;

import javax.management.modelmbean.InvalidTargetObjectTypeException;

import cnuphys.bCNU.attributes.Attributes;
import cnuphys.bCNU.simanneal.Solution;

public class Ising2DSolution extends Solution {
	
	//min and max cities
	private static final int MIN_DIM = 4;
	private static final int MAX_DIM = 200;
	
	//the data
	private int _spins[][];
	
	//the dimensions
	private int _numRow;
	private int _numColumn;
	
	//random number generator
	private static Random _rand = new Random();

	//the simulation owner
	private Ising2DSimulation _simulation;

	/**
	 * Create a solution
	 * @param simulation the parent simulation
	 */
	public Ising2DSolution(Ising2DSimulation simulation) {
		_simulation = simulation;
		init();
	}
	
	/**
	 * Copy constructor
	 * @param solution
	 */
	public Ising2DSolution(Ising2DSolution solution) {
		_simulation = solution._simulation;
		_numRow = solution._numRow;
		_numColumn = solution._numColumn;
		_spins = new int[_numRow][_numColumn];

		for (int row = 0; row < _numRow; row++) {
			for (int col = 0; col < _numColumn; col++) {
				_spins[row][col] = solution._spins[row][col];
			}			
		}

	}
	
	/**
	 * Get the spin (-1 or +1) at the given row and column (0-based)
	 * @param row the row index
	 * @param col the column index
	 * @return the spin (-1 or +1).
	 */
	public int getSpin(int row, int col) {
		return _spins[row][col];
	}
	
	public void init() {
		_numRow = getDimFromAttributes(Ising2DSimulation.NUMROWS);
		_numColumn = getDimFromAttributes(Ising2DSimulation.NUMCOLUMNS);
		
		_spins = new int[_numRow][_numColumn];
		
		for (int row = 0; row < _numRow; row++) {
			for (int col = 0; col < _numColumn; col++) {
				_spins[row][col] = (_rand.nextDouble() < 0.5) ? 1 : -1;
			}			
		}
	}
	
	
	//get the spins of the neighbors using wrap around bc
	private void getNeighborsWrap(int row, int col, int[] neighbors) {
		int rowm1 = (row == 0) ? (_numRow-1) : row-1;
		int rowp1 = (row == (_numRow-1)) ? 0 : row+1;
		int colm1 = (col == 0) ? (_numColumn-1) : col-1;
		int colp1 = (col == (_numColumn-1)) ? 0 : col+1;
		neighbors[0] = _spins[rowm1][col];
		neighbors[1] = _spins[rowp1][col];
		neighbors[2] = _spins[row][colm1];
		neighbors[3] = _spins[row][colp1];
	}
	
	//get the spins of the neighbors using wrap around bc
	private void getNeighborsNoWrap(int row, int col, int[] neighbors) {
		
		if (row > 0) {
			neighbors[0] = _spins[row - 1][col];
		} else {
			neighbors[0] = 0;
		}
		
		if (row == (_numRow - 1)) {
			neighbors[1] = 0;
		} else {
			neighbors[1] = _spins[row + 1][col];
		}
		
		if (col > 0) {
			neighbors[2] = _spins[row][col - 1];
		} else {
			neighbors[2] = 0;
		}
		
		if (col == (_numColumn - 1)) {
			neighbors[3] = 0;
		} else {
			neighbors[3] = _spins[row][col + 1];
		}
	}


	@Override
	public double getEnergy() {
		int[] neighbors = new int[4];
		
		double energy = 0;
		
		for (int row = 0; row < _numRow; row++) {
			for (int col = 0; col < _numColumn; col++) {
				getNeighborsWrap(row, col, neighbors);
//				getNeighborsNoWrap(row, col, neighbors);
				int nsum = 0;
				for (int i = 0; i < 4; i++) {
					nsum += neighbors[i];
				}
				energy += _spins[row][col]*nsum;
			}
			
		}
		
		return -energy;
	}
	
	public double getAbsMagnetization() {
		
		double m = 0;
		for (int row = 0; row < _numRow; row++) {
			for (int col = 0; col < _numColumn; col++) {
				m += _spins[row][col];
			}
			
		}
		
		int numCell = _numRow*_numColumn;
		return Math.abs(m/numCell);
	}
	
	/**
	 * Get the y value for the plot.
	 * @return the y value for the plot
	 */
	public double getPlotY() {
		return getAbsMagnetization();
	}


//	@Override
//	public Solution getRearrangement() {
//		int neighbors[] = new int[4];
//		Ising2DSolution neighbor = (Ising2DSolution) copy();
//
//		int row = _rand.nextInt(_numRow);
//		int col = _rand.nextInt(_numColumn);
//
//		getNeighborsWrap(row, col, neighbors);
//		int nsum = 0;
//		for (int i = 0; i < 4; i++) {
//			nsum += neighbors[i];
//		}
//		
//		if (((nsum > 0) && (_spins[row][col] < 0))  || ((nsum < 0) && (_spins[row][col] > 0))) {
//			if (_rand.nextDouble() < 0.5) {
//				if (_rand.nextDouble() < 0.75) {
//					neighbor.flipSpin(row, col);
//				} else {
//					neighbor.flipNeighbors(row, col);
//				}
//			}
//		} 
//		else {
//			if (_rand.nextDouble() < 0.5) {
//				neighbor.flipSpin(row, col);
//			} else {
//				neighbor.flipNeighbors(row, col);
//			}
//
//		}
//		return neighbor;
//	}
	
	@Override
	public Solution getRearrangement() {
		Ising2DSolution neighbor = (Ising2DSolution)copy();
		
		int numTimes = 1 + _rand.nextInt(3);
		for (int i = 0; i < numTimes; i++) {
			int ranRow = _rand.nextInt(_numRow);
			int ranCol = _rand.nextInt(_numColumn);
			
			if (_rand.nextDouble() < 0.5) {
				neighbor.flipSpin(ranRow, ranCol);
			} else {
				neighbor.flipNeighbors(ranRow, ranCol);
			}

		}
		
		return neighbor;
	}

	
	private void flipSpin(int row, int col) {
		if (_spins[row][col] == 1) {
			_spins[row][col] = -1;
		}
		else {
			_spins[row][col] = 1;
		}
	}
	
	private void flipNeighbors(int row, int col) {
		int rowm1 = (row == 0) ? (_numRow-1) : row-1;
		int rowp1 = (row == (_numRow-1)) ? 0 : row+1;
		int colm1 = (col == 0) ? (_numColumn-1) : col-1;
		int colp1 = (col == (_numColumn-1)) ? 0 : col+1;
		flipSpin(rowm1, col);
		flipSpin(rowp1, col);
		flipSpin(row, colm1);
		flipSpin(row, colp1);
	}

	@Override
	public Solution copy() {
		return new Ising2DSolution(this);
	}
	
	/**
	 * Get the row count
	 * @return the number of rows
	 */
	public int getNumRows() {
		return _numRow;
	}
	
	/**
	 * Get the column count
	 * @return the number of column
	 */
	public int getNumColumns() {
		return _numColumn;
	}
	
	/**
	 * Get the number of cities from the attributes
	 * @return the number of cities
	 */
	private int getDimFromAttributes(String attName) {
		
		Attributes attributes = _simulation.getAttributes();
		try {
			int count = attributes.getAttribute(attName).getInt();
			return Math.max(MIN_DIM, Math.min(MAX_DIM, count));
		} catch (InvalidTargetObjectTypeException e) {
			e.printStackTrace();
		}	
		return -1;
	}

}
