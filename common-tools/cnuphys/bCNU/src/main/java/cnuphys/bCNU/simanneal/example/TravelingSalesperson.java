package cnuphys.bCNU.simanneal.example;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import cnuphys.bCNU.simanneal.IUpdateListener;
import cnuphys.bCNU.simanneal.Simulation;
import cnuphys.bCNU.simanneal.Solution;
import cnuphys.splot.example.APlotDialog;
import cnuphys.splot.fit.FitType;
import cnuphys.splot.pdata.DataColumn;
import cnuphys.splot.pdata.DataColumnType;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.DataSetType;
import cnuphys.splot.plot.PlotParameters;
import cnuphys.splot.plot.PlotTicks;
import cnuphys.splot.style.SymbolType;

public class TravelingSalesperson extends Solution implements IUpdateListener {
	
	//singleton
	private static TravelingSalesperson _instance;
	
	//min and max cities
	private static final int MIN_CITY = 10;
	private static final int MAX_CITY = 2000;
	
	//the array of cities
	private TSCity[] _cities;
	
	//random number generator
	private static Random _rand = new Random();
	
	//the itinerary
	private int[] _itinerary;
	
	//the river "penalty"
	private River _river;
	
	//number of cities
	private int _numCity;
	
	//record intermediate results for making a plot
	private final Vector<Double> temps = new Vector<>(1000);
	private Vector<Double> dists = new Vector<>(1000);

	//Simulation panel for display
	private TSDisplay tsd;

	private Simulation _simulation;
	
	/**
	 * A Solution with randomly located cities
	 * @param numCity the number of cities
	 */
	private TravelingSalesperson(int numCity, River river) {
		reset(numCity, river);
		tsd = new TSDisplay(this);
	}
	
	/**
	 * Public access to the singleton
	 * @return
	 */
	public static TravelingSalesperson getInstance() {
		if (_instance == null) {
			_instance = new TravelingSalesperson(200, River.NORIVER);
		}
		return _instance;
	}
	
	/**
	 * Convenience method to get the current solution
	 * @return the current solution
	 */
	public TravelingSalesperson getCurrentSolution() {
		if (_simulation == null) {
			return this;
		}
		else {
			return (TravelingSalesperson)(_simulation.currentSolution());
		}
	}
	
	/**
	 * Reset the simulation
	 * @param numCity the number of cities
	 * @param the river "penalty"
	 */
	public void reset(int numCity, River river) {
		
		temps.clear();
		dists.clear();
		
		_numCity = Math.max(MIN_CITY, Math.min(MAX_CITY, numCity));
		
		_cities = new TSCity[_numCity];
		_river = river;
		
		for (int i = 0; i < _numCity; i++) {
			_cities[i] = new TSCity();
		}
		
		_itinerary = new int[_numCity];
		for (int i = 0; i < _numCity; i++) {
			_itinerary[i] = i;
		}
	}
	
	/**
	 * Get the number of cities
	 * @return the number of cities
	 */
	public int getCityCount() {
		return _numCity;
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
	
	/**
	 * Copy constructor
	 * @param ts the solution to copy
	 */
	public TravelingSalesperson(TravelingSalesperson ts) {
		//cities are immutable and shared
		_cities = ts._cities;
		
		//_itinerary is mutable
		_itinerary = new int[ts.count()];
		System.arraycopy(ts._itinerary, 0, _itinerary, 0, ts.count());
	}
	
	public int getThermalizationCount() {
		return 10*count();
	}

	@Override
	public double getEnergy() {
//		return 5*getDistance()/_cities.length;
		return getDistance();
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

	@Override
	public Solution getNeighbor() {
		
		TravelingSalesperson neighbor = (TravelingSalesperson)copy();

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
		return new TravelingSalesperson(this);
	}
	
	
	//create a temperature plot
	private static APlotDialog XmakePlot(List<Double> temps, List<Double> dists) {
		
		int len = temps.size();
		double tArry[] = new double[len];
		double dArry[] = new double[len];
		for (int i = 0; i < len; i++) {
			tArry[i] = temps.get(i);
			dArry[i] = dists.get(i);
		}
		
	

		
		APlotDialog pdialog;
		pdialog = new APlotDialog(null, "", false) {

			@Override
			protected DataSet createDataSet() throws DataSetException {
				return new DataSet(DataSetType.XYY, getColumnNames());
			}

			@Override
			protected String[] getColumnNames() {
				String cn[] = {"Temp", "Distance"};
				return cn;
			}

			@Override
			protected String getXAxisLabel() {
				return "Temperature";
			}

			@Override
			protected String getYAxisLabel() {
				return "Distance";
			}

			@Override
			protected String getPlotTitle() {
				return "Distance vs Temperature";
			}

			@Override
			public void fillData() {
				DataSet ds = _canvas.getDataSet();

				for (int i = 0; i < len; i++) {

					try {
						ds.add(tArry[i], dArry[i]);
					}
					catch (DataSetException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			}

			@Override
			public void setPreferences() {
				DataSet ds = _canvas.getDataSet();
				Vector<DataColumn> ycols = (Vector<DataColumn>)(ds.getAllColumnsByType(DataColumnType.Y));
				for (DataColumn dc : ycols) {
				    dc.getFit().setFitType(FitType.NOLINE);
				    dc.getStyle().setSymbolType(SymbolType.CIRCLE);
				    dc.getStyle().setLineWidth(1.5f);
				}
				ycols.get(0).getStyle().setLineColor(Color.red);
				
				PlotTicks ticks = _canvas.getPlotTicks();
				ticks.setNumMajorTickY(5);
				ticks.setNumMajorTickX(5);
				
				PlotParameters params = _canvas.getParameters();
//				params.mustIncludeXZero(true);
//				params.mustIncludeYZero(true);
				params.setYRange(0, 1.2*dArry[0]);
//				params.setXRange(0, 1.1*tArry[0]);
				
				double maxTemp = tArry[0];
				double minTemp = tArry[(tArry.length-1)];
				params.setXRange(1.05*maxTemp, 0.0);
				
				params.setNumDecimalY(3);
				params.setMinExponentX(4);
				params.setMinExponentY(4);

				
			}
			
		};
		
		pdialog.setSize(800, 800);
		return pdialog;

	}
	

	@Override
	public void updateSolution(Simulation simulation, Solution newSolution, Solution oldSolution) {
		TravelingSalesperson ts = (TravelingSalesperson)newSolution;
		double temperature = simulation.getTemperature();
		System.out.println(String.format("T: %-12.8f   D: %-10.5f", temperature, ts.getDistance()));
		temps.add(temperature);
		dists.add(ts.getDistance());
	}
	
	
	/**
	 * Set the simulation
	 * @param simulation the simulation
	 */
	public void setSimulation(Simulation simulation) {
		_simulation = simulation;
		_simulation.addUpdateListener(this);
		_simulation.addUpdateListener(tsd);
	}
	
	/**
	 * Accessor for the simulation
	 * @return the simulation
	 */
	public Simulation getSimulation() {
		return _simulation;
	}



	//main program for testing
	public static void main(String arg[]) {
		//initial solution
		
		int numCity = 400;
		River river = River.NORIVER;
		
		TravelingSalesperson travPerson = getInstance();
		travPerson.reset(numCity, river);
		
		System.out.println("City count: " + travPerson.count());
		System.out.println("Initial distance: " + travPerson.getDistance());
		System.out.println("Initial energy: " + travPerson.getEnergy());
		TravelingSalesperson neighbor = (TravelingSalesperson) travPerson.getNeighbor();
		System.out.println("Initial distance: " + travPerson.getDistance());
		System.out.println("Initial energy: " + travPerson.getEnergy());
		System.out.println("Neighbor distance: " + neighbor.getDistance());
		System.out.println("Neighbor energy: " + neighbor.getEnergy());
		
//		public Simulation(Solution initialSolution, double coolRate, int thermalizationCount) {
//			this(initialSolution, coolRate, thermalizationCount, -1L);
//		}
		
		
		
		Properties props = new Properties();
		props.setProperty(Simulation.RANDSEED, "-1");
		props.setProperty(Simulation.COOLRATE, "0.03");
		props.setProperty(Simulation.THERMALCOUNT, "200");
		props.setProperty(Simulation.MAXSTEPS, "1000");
		
		Simulation simulation = new Simulation(travPerson, props);
		
		travPerson.setSimulation(simulation);
		
		//add initial values
		travPerson.temps.add(simulation.getTemperature());
		travPerson.dists.add(travPerson.getDistance());
		
		final JFrame frame = new JFrame();

		// set up what to do if the window is closed
		WindowAdapter windowAdapter = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				System.exit(1);
			}
		};

		frame.addWindowListener(windowAdapter);

		frame.setLayout(new BorderLayout());
		
		
		travPerson.tsd.setPreferredSize(new Dimension(600, 600));
		
		frame.add(travPerson.tsd, BorderLayout.CENTER);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				frame.pack();
				frame.setVisible(true);
				frame.setLocationRelativeTo(null);
			}
		});

		
		
		simulation.run();
		
//		makePlot(temps, dists).setVisible(true);
	}

}
