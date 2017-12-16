package cnuphys.bCNU.simanneal.example;

import java.awt.Color;
import java.util.List;
import java.util.Random;
import java.util.Vector;

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
import cnuphys.splot.plot.HorizontalLine;
import cnuphys.splot.plot.PlotParameters;
import cnuphys.splot.plot.PlotTicks;
import cnuphys.splot.plot.VerticalLine;
import cnuphys.splot.style.SymbolType;

public class TravelingSalesman extends Solution {
	
	private City[] cities;
	
	private static Random _rand = new Random();
	
	public TravelingSalesman(int numCity) {
		cities = new City[numCity];
		
		for (int i = 0; i < numCity; i++) {
			cities[i] = new City();
		}
	}
	
	public int count() {
		return cities.length;
	}
	
	public TravelingSalesman(TravelingSalesman ts) {
		cities = new City[ts.count()];
		
		System.arraycopy(ts.cities, 0, cities, 0, ts.count());
	}

	@Override
	public double getEnergy() {
		return 5*getDistance()/cities.length;
	}
	
	public double getDistance() {
		int len = cities.length;
		double distance = 0;
		for (int i = 0; i < (len-1); i++) {
			distance += cities[i].distance(cities[i+1]);
		}
		
		//plus return
		distance += cities[0].distance(cities[len-1]);
		return distance;
	}

	@Override
	public Solution getNeighbor() {
		TravelingSalesman neighbor = (TravelingSalesman)copy();
	
		int idx1 = _rand.nextInt(count());
		int idx2 = _rand.nextInt(count());
		
		while (idx2 == idx1) {
			idx2 = _rand.nextInt(count());
		}
		
		
		City temp = neighbor.cities[idx1];
		neighbor.cities[idx1] = neighbor.cities[idx2];
		neighbor.cities[idx2] = temp;
		return neighbor;
	}

	@Override
	public Solution copy() {
		return new TravelingSalesman(this);
	}
	
	
	class City {
		
		public double x;
		public double y;
		
		public City() {
			x = Math.random();
			y = Math.random();
		}
		
		public double distance(City c) {
			return Math.hypot(c.x-x, c.y-y);
		}
	}
	
	
	private static APlotDialog makePlot(List<Double> temps, List<Double> dists) {
		
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
				params.setXRange(1.05, -0.05);
				
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
		TravelingSalesman initSol = new TravelingSalesman(100);
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
		
		temps.add(1.0);
		dists.add(initSol.getDistance());
		
		Simulation simulation = new Simulation(initSol, 0.003, 100);
		
		IUpdateListener updater = new IUpdateListener() {
			

			@Override
			public void updateSolution(Solution newSolution, Solution oldSolution, double temperature) {
				TravelingSalesman ts = (TravelingSalesman)newSolution;
				System.out.println(String.format("T: %-10.5f   D: %-10.5f", temperature, ts.getDistance()));
				temps.add(temperature);
				dists.add(ts.getDistance());

			}
			
		};
		
		simulation.addUpdateListener(updater);
		simulation.run();
		
		makePlot(temps, dists).setVisible(true);
	}

}
