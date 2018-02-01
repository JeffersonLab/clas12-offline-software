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
import cnuphys.bCNU.simanneal.SimulationPanel;
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

public class TravelingSalesman extends Solution {
	
	//the array of cities
	private TSCity[] _cities;
	
	//random number generator
	private static Random _rand = new Random();
	
	//the itinerary
	private int[] _itinerary;
	
	/**
	 * A Solution with randomly located cities
	 * @param numCity the number of cities
	 */
	public TravelingSalesman(int numCity) {
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
	
	/**
	 * Copy constructor
	 * @param ts the solution to copy
	 */
	public TravelingSalesman(TravelingSalesman ts) {
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
		
		TravelingSalesman neighbor = (TravelingSalesman)copy();

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
		return new TravelingSalesman(this);
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

	
	public static void main(String arg[]) {
		//initial solution
		
		int numCity = 200;
		
		TravelingSalesman initSol = new TravelingSalesman(numCity);
		System.out.println("City count: " + initSol.count());
		System.out.println("Initial distance: " + initSol.getDistance());
		System.out.println("Initial energy: " + initSol.getEnergy());
		TravelingSalesman neighbor = (TravelingSalesman) initSol.getNeighbor();
		System.out.println("Initial distance: " + initSol.getDistance());
		System.out.println("Initial energy: " + initSol.getEnergy());
		System.out.println("Neighbor distance: " + neighbor.getDistance());
		System.out.println("Neighbor energy: " + neighbor.getEnergy());
		
//		public Simulation(Solution initialSolution, double coolRate, int thermalizationCount) {
//			this(initialSolution, coolRate, thermalizationCount, -1L);
//		}
		
		final Vector<Double> temps = new Vector<>(1000);
		final Vector<Double> dists = new Vector<>(1000);
		
		
		Properties props = new Properties();
		props.setProperty(Simulation.RANDSEED, "-1");
		props.setProperty(Simulation.COOLRATE, "0.03");
		props.setProperty(Simulation.THERMALCOUNT, "200");
		props.setProperty(Simulation.MAXSTEPS, "1000");
		
		Simulation simulation = new Simulation(initSol, props);
		
		temps.add(simulation.getTemperature());
		dists.add(initSol.getDistance());

		
		IUpdateListener updater = new IUpdateListener() {
			

			@Override
			public void updateSolution(Simulation simulation, Solution newSolution, Solution oldSolution) {
				TravelingSalesman ts = (TravelingSalesman)newSolution;
				double temperature = simulation.getTemperature();
				System.out.println(String.format("T: %-12.8f   D: %-10.5f", temperature, ts.getDistance()));
				temps.add(temperature);
				dists.add(ts.getDistance());

			}
			
		};
		
		simulation.addUpdateListener(updater);
		
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
		
		TSDisplay tsd = new TSDisplay(simulation);
		tsd.setPreferredSize(new Dimension(600, 600));
		
		SimulationPanel simPanel = new SimulationPanel(simulation, null, tsd, null);
		frame.add(simPanel, BorderLayout.CENTER);

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
