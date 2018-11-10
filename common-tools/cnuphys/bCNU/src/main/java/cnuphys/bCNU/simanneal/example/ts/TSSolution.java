package cnuphys.bCNU.simanneal.example.ts;

import java.util.ArrayList;
import java.util.Random;

import javax.management.modelmbean.InvalidTargetObjectTypeException;

import cnuphys.bCNU.attributes.Attributes;
import cnuphys.bCNU.simanneal.Solution;

public class TSSolution extends Solution {
	
	
	//min and max cities
	private static final int MIN_CITY = 10;
	private static final int MAX_CITY = 2000;
	
	//the array of cities
	private TSCity[] _cities;
	
	//random number generator
	private static Random _rand = new Random();
	
	//the itinerary
	private int[] _itinerary;
		
	//the simulation owner
	private TSSimulation _simulation;
	
	/**
	 * A Solution with randomly located cities
	 * @param numCity the number of cities
	 */
	public TSSolution(TSSimulation simulation) {
		_simulation = simulation;
		init();
	}
	
	/**
	 * Copy constructor
	 * @param ts the solution to copy
	 */
	public TSSolution(TSSolution ts) {
		_simulation = ts.getSimulation();
		
		//cities are immutable and shared
		_cities = ts._cities;
		
		//_itinerary is mutable
		_itinerary = new int[ts.count()];
		System.arraycopy(ts._itinerary, 0, _itinerary, 0, ts.count());
	}
	
		
	/**
	 * Initialize the colution
	 */
	public void init() {
		
		int numCity = getNumCityFromAttributes();
		
		_cities = new TSCity[numCity];
		
		for (int i = 0; i < numCity; i++) {
			_cities[i] = new TSCity();
		}
		
		_itinerary = new int[numCity];
		for (int i = 0; i < numCity; i++) {
			_itinerary[i] = i;
		}
	}
		
	/**
	 * Get the itinerary
	 * @return the itinerary
	 */
	public int[] getItinerary() {
		return _itinerary;
	}
	
	/**
	 * Get the cities
	 * @return the cities
	 */
	public TSCity [] getCities() {
		return _cities;
	}
	
	/**
	 * Get the number of cities
	 * @return the number of cities
	 */
	public int count() {
		return _cities.length;
	}
		
	public int getThermalizationCount() {
		return 10*count();
	}

	@Override
	public double getEnergy() {
//		return 5*getDistance()/_cities.length;
		return getDistance() + riverPenalty();
	}
	
	/**
	 * Get the y value for the plot.
	 * @return the y value for the plot
	 */
	public double getPlotY() {
		return getEnergy();
	}

	
	//get the "distance" which includes river penalties or bonuses
	public double getDistance() {
		int len = count();
		
		double distance = 0;
		for (int i = 0; i < (len-1); i++) {
			int j = _itinerary[i];
			int k = _itinerary[i+1];			
			distance += _cities[j].distance(_cities[k]);
		}
		
		//plus return
		int i0 = _itinerary[0];
		int iN = _itinerary[len-1];
		distance += _cities[i0].distance(_cities[iN]);
		return distance;
	}
	
	//compute the penalty (or bonus) for crossing the river
	private double riverPenalty() {
		double lambda = _simulation.getRiverPenalty();
		if (Math.abs(lambda) < 0.01) {
			return 0;
		}
		
		int crossCount = 0;
		int len = count();
		
		for (int i = 0; i < (len-1); i++) {
			int j = _itinerary[i];
			int k = _itinerary[i+1];			
			if (_cities[j].acrossRiver(_cities[k])) {
				crossCount++;
			}
		}
		
		//plus return
		int i0 = _itinerary[0];
		int iN = _itinerary[len-1];
		if (_cities[i0].acrossRiver(_cities[iN])) {
			crossCount++;
		}

		return lambda*crossCount;
	}

	@Override
	public Solution getRearrangement() {
		
		TSSolution neighbor = (TSSolution)copy();

		int seg[] = getSegment(count());
		
		if ((_rand.nextInt() % 2) == 0) { //transport
			
			int nn = (seg[0] - seg[1] + count() - 1) % count();  //num not in segment
			seg[2] = seg[1] + _rand.nextInt(Math.abs(nn-1)) + 1;
			seg[2] = seg[2] % count();
			transport(neighbor._itinerary, seg);
		}
		else { //reversal
			reverse(neighbor._itinerary, seg);
		}
		
		return neighbor;
	}
	
	
	private void transport(int[] iArry, int[] seg) {
		ArrayList<Integer> alist = new ArrayList<>();
		ArrayList<Integer> blist = new ArrayList<>();
		int len = iArry.length;
		
		int insertVal = iArry[seg[2]];

		for (int i = 0; i < len; i++) {
			if (!inSeg(seg, i)) {
				alist.add(iArry[i]);
			}
		}
		
		int nn = (seg[0] - seg[1] + len - 1) % len;  //num not in segment
		int seglen = len - nn; //num in segment
		
		int idx1 = seg[0];				
		for (int i = 0; i < seglen; i++) {
			
			idx1 = idx1 % len;
			blist.add(iArry[idx1]);
			idx1++;
		}
		
		int insertIndex = alist.indexOf(insertVal);
		alist.addAll(insertIndex, blist);

//		System.err.print("");
		
		for (int i = 0; i < len; i++) {
			iArry[i] = alist.get(i);
		}
	}
	
	public boolean inSeg(int[] seg, int index) {
		int n0 = seg[0];
		int n1 = seg[1];
		
		if (n0 < n1) { //eg 5-15
			return ((index >= n0) && (index <= n1));
		}
		else {  //eg 15 -5
			return ((index >= n0) || (index <= n1));
		}
	}

	
	private void reverse(int[] iArry, int[] seg) {
		
		
		int len = iArry.length;
		
		int cArry[] = new int[len];
		System.arraycopy(iArry, 0, cArry, 0, len);
		
		int nn = (seg[0] - seg[1] + len - 1) % len;  //num not in segment
		int seglen = len - nn; //num in segment
		
		int idx1 = seg[0];
		int idx2 = seg[1];
				
		for (int i = 0; i < seglen; i++) {
			
			idx1 = idx1 % len;
			if (idx2 < 0) {
				idx2 = len-1;
			}
			
			iArry[idx1] = cArry[idx2];
			idx1++;
			idx2--;
		}
		
	}
	
	//get the segment for reconfiguration
	private int[] getSegment(int nc) {
		int seg[] = new int [3];
		
		int nn = 0;
		
		do {
			
			seg[0] = _rand.nextInt(nc);
			seg[1] = _rand.nextInt(nc-1);
			if (seg[1] >= seg[0]) {
				++seg[1];
			}
			nn = (seg[0] - seg[1] + nc - 1) % nc;
			
		} while (nn < 2);

	//	System.out.println("SEGMENT [" + seg[0] + "-" + seg[1] + "]   nn = " + nn);

		return seg;
	}
	

	@Override
	public Solution copy() {
		return new TSSolution(this);
	}
		
	/**
	 * Accessor for the simulation
	 * @return the simulation
	 */
	public TSSimulation getSimulation() {
		return _simulation;
	}
	
	/**
	 * Get the number of cities from the attributes
	 * @return the number of cities
	 */
	private int getNumCityFromAttributes() {
		
		Attributes attributes = _simulation.getAttributes();
		try {
			int numCity = attributes.getAttribute(TSSimulation.NUMCITY).getInt();
			return Math.max(MIN_CITY, Math.min(MAX_CITY, numCity));
		} catch (InvalidTargetObjectTypeException e) {
			e.printStackTrace();
		}	
		return -1;
	}


}
