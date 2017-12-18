package cnuphys.ced.frame;

import java.awt.Color;
import java.util.Collection;
import java.util.Random;
import java.util.Vector;

import cnuphys.bCNU.component.TextAreaWriter;
import cnuphys.bCNU.dialog.TextDisplayDialog;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.GridCoordinate;
import cnuphys.magfield.MagneticField;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.Torus;
import cnuphys.magfield.TorusMap;
import cnuphys.magfield.TorusProbe;
import cnuphys.rk4.RungeKuttaException;
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
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.Swimmer;

public class CedTests {
	
	final static double RHO = 72.0;   //cm
	final static double Z = 360.0;    //cm


	protected static void edgeTest(boolean probeCache) {
		System.out.println("Edge test. Using probes: " + probeCache);
		FieldProbe.cache(probeCache);
		
		TextDisplayDialog dialog = new TextDisplayDialog("Some Magfield Test Results");
		dialog.setVisible(true);
		TextAreaWriter writer = dialog.getWriter();

		//two maps used in tests
		TorusMap tmap2 = TorusMap.FULL_025;
		TorusMap tmap1 = TorusMap.FULL_200;
		
		TorusMap currentTmap = MagneticFields.getInstance().getTorusMap();

		makeBPlot(tmap1).setVisible(true);
		makeBPlot(tmap2).setVisible(true);
		
		makeDiffPlot(tmap1, tmap2).setVisible(true);
		
		crossBoundary(writer, tmap1, tmap2);
		
		MagneticFields.getInstance().setTorus(currentTmap);
		FieldProbe.cache(true);
	}
	
	private static void crossBoundary(TextAreaWriter writer, TorusMap tmap1, TorusMap tmap2) {
		FieldProbe.cache(false);
		
		double phi0 = 29;
		double phi1 = phi0+2;
		int n = 101;
		double dphi = (phi1 - phi0)/(n-1);

		float[] field = new float[3];
		double phi[] = new double[n];
		final double b1x[] = new double[n];
		final double b1y[] = new double[n];
		final double b1z[] = new double[n];
		final double b2x[] = new double[n];
		final double b2y[] = new double[n];
		final double b2z[] = new double[n];

		final double xdiff[] = new double[n];
		final double ydiff[] = new double[n];
		final double zdiff[] = new double[n];

		
		for (int i = 0; i < n; i++) {
			phi[i] = phi0 + (i*dphi);
		}
		
		MagneticFields.getInstance().setTorus(tmap1);
		double magicVal = 29.9;
		
		Torus torus = MagneticFields.getInstance().getTorus();

		FieldProbe probe = FieldProbe.factory(torus);
		
		writer.writeln("-----------------");
		probe.fieldCylindrical(29.75, 70., 358., field);
		writer.writeln(String.format("BG0 (%5e, %5e, %5e)", field[0], field[1], field[2]));
		probe.fieldCylindrical(29.75, 70., 360., field);
		writer.writeln(String.format("BG1 (%5e, %5e, %5e)", field[0], field[1], field[2]));
		
		probe.fieldCylindrical(29.75, 72., 358., field);
		writer.writeln(String.format("BG2 (%5e, %5e, %5e)", field[0], field[1], field[2]));
		probe.fieldCylindrical(29.75, 72., 360., field);
		writer.writeln(String.format("BG3 (%5e, %5e, %5e)", field[0], field[1], field[2]));

		probe.fieldCylindrical(30.0, 70., 358., field);
		writer.writeln(String.format("BG4 (%5e, %5e, %5e)", field[0], field[1], field[2]));
		probe.fieldCylindrical(30.0, 70., 360., field);
		writer.writeln(String.format("BG5 (%5e, %5e, %5e)", field[0], field[1], field[2]));

		probe.fieldCylindrical(30.0, 72., 358., field);
		writer.writeln(String.format("BG6 (%5e, %5e, %5e)", field[0], field[1], field[2]));
		probe.fieldCylindrical(30.0, 72., 360., field);
		writer.writeln(String.format("BG7 (%5e, %5e, %5e)", field[0], field[1], field[2]));
		probe.fieldCylindrical(29.9, 72., 360., field);
		writer.writeln(String.format("BT (%5e, %5e, %5e)", field[0], field[1], field[2]));
		
		int index1 = 7506285;
		writer.writeln("B1 at index " + index1 + " = " + torus.getB1(index1));
		int index2 = 7569286;
		writer.writeln("B1 at index " + index2 + " = " + torus.getB1(index2));
		
		writer.writeln("-----------------");
		
		
		
		for (int i = 0; i < n; i++) {
			probe.fieldCylindrical(phi[i], RHO, Z, field);
			b1x[i] = field[0];
			b1y[i] = field[1];
			b1z[i] = field[2];
			
			if (Math.abs(phi[i]-magicVal) < 1.0e-8) {
				writer.writeln("Map 1 probe at " + magicVal);
				writer.writeln(String.format("B (%5e, %5e, %5e)", field[0], field[1], field[2]));
				
			
				writeIndexInfo(writer, phi[i]);
				
				
				writer.writeln(probe.toString());
			
			}
		}
		
		MagneticFields.getInstance().setTorus(tmap2);
		
		torus = MagneticFields.getInstance().getTorus();

		
		probe = FieldProbe.factory(torus);
		
		writer.writeln("-----------------");
		probe.fieldCylindrical(29.75, 70., 358., field);
		writer.writeln(String.format("BG0 (%5e, %5e, %5e)", field[0], field[1], field[2]));
		probe.fieldCylindrical(29.75, 70., 360., field);
		writer.writeln(String.format("BG1 (%5e, %5e, %5e)", field[0], field[1], field[2]));
		
		probe.fieldCylindrical(29.75, 72., 358., field);
		writer.writeln(String.format("BG2 (%5e, %5e, %5e)", field[0], field[1], field[2]));
		probe.fieldCylindrical(29.75, 72., 360., field);
		writer.writeln(String.format("BG3 (%5e, %5e, %5e)", field[0], field[1], field[2]));

		probe.fieldCylindrical(30.0, 70., 358., field);
		writer.writeln(String.format("BG4 (%5e, %5e, %5e)", field[0], field[1], field[2]));
		probe.fieldCylindrical(30.0, 70., 360., field);
		writer.writeln(String.format("BG5 (%5e, %5e, %5e)", field[0], field[1], field[2]));

		probe.fieldCylindrical(30.0, 72., 358., field);
		writer.writeln(String.format("BG6 (%5e, %5e, %5e)", field[0], field[1], field[2]));
		probe.fieldCylindrical(30.0, 72., 360., field);
		writer.writeln(String.format("BG7 (%5e, %5e, %5e)", field[0], field[1], field[2]));
		probe.fieldCylindrical(29.9, 72., 360., field);
		writer.writeln(String.format("BT (%5e, %5e, %5e)", field[0], field[1], field[2]));

		writer.writeln("B1 at index " + index1 + " = " + torus.getB1(index1));
		writer.writeln("B1 at index " + index2 + " = " + torus.getB1(index2));

		writer.writeln("-----------------");


		
		for (int i = 0; i < n; i++) {
			probe.fieldCylindrical(phi[i], RHO, Z, field);
			b2x[i] = field[0];
			b2y[i] = field[1];
			b2z[i] = field[2];

			if (Math.abs(phi[i]-magicVal) < 1.0e-8) {
				writer.writeln("Map 2 probe at " + magicVal);
				writer.writeln(String.format("B (%5e, %5e, %5e)", field[0], field[1], field[2]));
				
				writeIndexInfo(writer, phi[i]);
				
				
				writer.writeln(probe.toString());
			}
			
			xdiff[i] = safeDiff(b1x[i], b2x[i]);
			ydiff[i] = safeDiff(b1y[i], b2y[i]);
			zdiff[i] = safeDiff(b1z[i], b2z[i]);
		}

		
		for (int i = 0; i < n; i++) {
			writer.writeln(String.format("%-10.3f  %5e  %5e  %5e ", phi[i], b1x[i], b2x[i], xdiff[i]));
		}


	}
	
	private static void writeIndexInfo(TextAreaWriter writer, double phi) {
		Torus torus = MagneticFields.getInstance().getTorus();
		GridCoordinate phiCoordinate = torus.getPhiCoordinate();
		GridCoordinate rhoCoordinate = torus.getRCoordinate();
		GridCoordinate zCoordinate = torus.getZCoordinate();
		int n0 = phiCoordinate.getIndex(phi);
		int n1 = rhoCoordinate.getIndex(RHO);
		int n2 = zCoordinate.getIndex(Z);
		writer.writeln("N = [" + n0 + ", " + n1 + ", " + n2 + "]");
		double f0 = phiCoordinate.getFraction(phi, n0);
		double f1 = rhoCoordinate.getFraction(RHO, n1);
		double f2 = zCoordinate.getFraction(Z, n2);

		writer.writeln(String.format("F: (%-10.5f, %-10.5f, %-10.5f)", f0, f1, f2));
		
		//composite indices
		
		int i000 = torus.getCompositeIndex(n0, n1, n2);
		int i001 = i000 + 1;

		int i010 = torus.getCompositeIndex(n0, n1 + 1, n2);
		int i011 = i010 + 1;

		int i100 = torus.getCompositeIndex(n0 + 1, n1, n2);
		int i101 = i100 + 1;

		int i110 = torus.getCompositeIndex(n0 + 1, n1 + 1, n2);
		int i111 = i110 + 1;
		writer.writeln("COMPIDX = [" + i000 + ", " + i010 + ", " + i100 + ", " + i110 + "]");
		
		double b000 = torus.getB1(i000);
		double b010 = torus.getB1(i010);
		double b100 = torus.getB1(i100);
		double b110 = torus.getB1(i110);
		
		double b001 = torus.getB1(i001);
		double b011 = torus.getB1(i011);
		double b101 = torus.getB1(i101);
		double b111 = torus.getB1(i111);
		
		
		writer.writeln(String.format("Bindx  (%5e, %5e, %5e, %5e)", b000/10, b010/10, b100/10, b110/10));
		writer.writeln(String.format("Bindx+ (%5e, %5e, %5e, %5e)", b001/10, b011/10, b101/10, b111/10));

//		double b001 = torus.getB1(i001);
//		double b011 = torus.getB1(i011);
//		double b101 = torus.getB1(i101);
//		double b111 = torus.getB1(i111);

		
	}
	
	private static final double epsilon = 1.0e-8;
	private static double safeDiff(double a, double b) {
		double diff = b-a;
		if (Math.abs(diff) < epsilon) {
			return 0;
		}
		double big = Math.max(Math.abs(a), Math.abs(b));
		return 100.*diff/big;
		
	}
	
	//plot the differences of the components
	private static APlotDialog makeDiffPlot(final TorusMap tmap1, final TorusMap tmap2) {
		
			
		int n = 50000;
		double dphi = 360.0/n;
		
		n = n + 3;
		final double phi[] = new double[n];
		
		double eps = 0.00001;
		phi[0] = -1;
		phi[1] = eps;
		phi[n-2] = 360-eps;
		phi[n-1] = 361;
		for (int i = 1; i < (n-2); i++) {
			phi[i] = (i-1)*dphi;
		}
		
//		for (int i = 0; i < n; i++) {
//			System.out.println("[" + i + "] phi = " + phi[i]);
//		}
		
		float[] field = new float[3];
		final double b1x[] = new double[n];
		final double b1y[] = new double[n];
		final double b1z[] = new double[n];
		final double b1mag[] = new double[n];
		final double b2x[] = new double[n];
		final double b2y[] = new double[n];
		final double b2z[] = new double[n];
		final double b2mag[] = new double[n];
		
		MagneticFields.getInstance().setTorus(tmap1);

		for (int i = 0; i < n; i++) {
			MagneticFields.getInstance().getActiveField().fieldCylindrical(phi[i], RHO, Z, field);
			b1x[i] = field[0];
			b1y[i] = field[1];
			b1z[i] = field[2];
			
			b1mag[i] = MagneticFields.getInstance().getActiveField().fieldMagnitudeCylindrical(phi[i], RHO, Z);
			
		}
		
		MagneticFields.getInstance().setTorus(tmap2);
		for (int i = 0; i < n; i++) {
			MagneticFields.getInstance().getActiveField().fieldCylindrical(phi[i], RHO, Z, field);
			b2x[i] = field[0];
			b2y[i] = field[1];
			b2z[i] = field[2];
			
			b2mag[i] = MagneticFields.getInstance().getActiveField().fieldMagnitudeCylindrical(phi[i], RHO, Z);
			
		}

		
		APlotDialog pdialog;
		pdialog = new APlotDialog(Ced.getFrame(), "", false) {

			@Override
			protected DataSet createDataSet() throws DataSetException {
				return new DataSet(DataSetType.XYY, getColumnNames());
			}

			@Override
			protected String[] getColumnNames() {
				String cn[] = {"Phi", "Bx Diff", "By Diff", "100x(Bz Diff)", "BMag Diff"};
				return cn;
			}

			@Override
			protected String getXAxisLabel() {
				return "Phi (degrees)";
			}

			@Override
			protected String getYAxisLabel() {
				return "Component Diff (kG)";
			}

			@Override
			protected String getPlotTitle() {
				return "B Diff " + tmap1.getName() + " v " + tmap2.getName();
			}

			@Override
			public void fillData() {
				DataSet ds = _canvas.getDataSet();

				for (int i = 0; i < phi.length; i++) {

					try {
						ds.add(phi[i], b1x[i]-b2x[i], b1y[i]-b2y[i], 100*(b1z[i]-b2z[i]), b1mag[i]-b2mag[i]);
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
				    dc.getFit().setFitType(FitType.CONNECT);
				    dc.getStyle().setSymbolType(SymbolType.NOSYMBOL);
				    dc.getStyle().setLineWidth(1.5f);
				}
				ycols.get(0).getStyle().setLineColor(Color.red);
				ycols.get(1).getStyle().setLineColor(Color.blue);
				ycols.get(2).getStyle().setLineColor(Color.gray);
				ycols.get(2).getStyle().setLineColor(Color.green);
				
				PlotTicks ticks = _canvas.getPlotTicks();
				ticks.setNumMajorTickY(6);
				ticks.setNumMajorTickX(5);
				
				PlotParameters params = _canvas.getParameters();
//				params.mustIncludeXZero(true);
//				params.mustIncludeYZero(true);
				params.setYRange(-.070, .070);
				params.setXRange(0, 180);
				
				params.setNumDecimalY(3);
				params.setMinExponentX(4);
				params.setMinExponentY(4);

				params.addPlotLine(new HorizontalLine(_canvas, 0));
//				params.addPlotLine(new HorizontalLine(_canvas, 1));
				params.addPlotLine(new VerticalLine(_canvas, 30));
				params.addPlotLine(new VerticalLine(_canvas, 90));
				params.addPlotLine(new VerticalLine(_canvas, 150));
				params.addPlotLine(new VerticalLine(_canvas, 210));
				params.addPlotLine(new VerticalLine(_canvas, 270));
				params.addPlotLine(new VerticalLine(_canvas, 330));
								
				
				params.setExtraStrings(String.format("rho = %-4.0f cm   z = %-5.0f cm", RHO, Z));
				params.setExtraDrawing(true);
				
				
			}
			
		};
		
		pdialog.setSize(800, 800);
		return pdialog;

	}
	
	//plot the components
	private static APlotDialog makeBPlot(TorusMap tmap) {
		//make a plot
		MagneticFields.getInstance().setTorus(tmap);

		int n = 10000;
		double dphi = 360.0/n;
		
		n = n + 3;
		final double phi[] = new double[n];
		
		double eps = 0.00001;
		phi[0] = -1;
		phi[1] = eps;
		phi[n-2] = 360-eps;
		phi[n-1] = 361;
		for (int i = 1; i < (n-2); i++) {
			phi[i] = (i-1)*dphi;
		}
		
//		for (int i = 0; i < n; i++) {
//			System.out.println("[" + i + "] phi = " + phi[i]);
//		}
		
		float[] field = new float[3];
		final double bx[] = new double[n];
		final double by[] = new double[n];
		final double bz[] = new double[n];
		final double bmag[] = new double[n];
		
		for (int i = 0; i < n; i++) {
			MagneticFields.getInstance().getActiveField().fieldCylindrical(phi[i], RHO, Z, field);
			bx[i] = field[0];
			by[i] = field[1];
			bz[i] = field[2];
			
			bmag[i] = MagneticFields.getInstance().getActiveField().fieldMagnitudeCylindrical(phi[i], RHO, Z);
			
		}
		
		
		APlotDialog pdialog;
		pdialog = new APlotDialog(Ced.getFrame(), "", false) {

			@Override
			protected DataSet createDataSet() throws DataSetException {
				return new DataSet(DataSetType.XYY, getColumnNames());
			}

			@Override
			protected String[] getColumnNames() {
				String cn[] = {"Phi", "Bx", "By", "100xBz", "BMag"};
				return cn;
			}

			@Override
			protected String getXAxisLabel() {
				return "Phi (degrees)";
			}

			@Override
			protected String getYAxisLabel() {
				return "B (kG)";
			}

			@Override
			protected String getPlotTitle() {
				return "B Components (kG) Torus: " + MagneticFields.getInstance().getTorusMap().getName();
			}

			@Override
			public void fillData() {
				DataSet ds = _canvas.getDataSet();

				for (int i = 0; i < phi.length; i++) {

					try {
						ds.add(phi[i], bx[i], by[i], 100*bz[i], bmag[i]);
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
				    dc.getFit().setFitType(FitType.CONNECT);
				    dc.getStyle().setSymbolType(SymbolType.NOSYMBOL);
				    dc.getStyle().setLineWidth(1.5f);
				}
				ycols.get(0).getStyle().setLineColor(Color.red);
				ycols.get(1).getStyle().setLineColor(Color.blue);
				ycols.get(2).getStyle().setLineColor(Color.gray);
				ycols.get(2).getStyle().setLineColor(Color.green);
				
				PlotTicks ticks = _canvas.getPlotTicks();
				ticks.setNumMajorTickX(5);
				
				PlotParameters params = _canvas.getParameters();
//				params.mustIncludeXZero(true);
//				params.mustIncludeYZero(true);
				params.setYRange(-20.0, 20.0);
				params.setXRange(0, 360);

				params.addPlotLine(new HorizontalLine(_canvas, 0));
//				params.addPlotLine(new HorizontalLine(_canvas, 1));
				params.addPlotLine(new VerticalLine(_canvas, 30));
				params.addPlotLine(new VerticalLine(_canvas, 90));
				params.addPlotLine(new VerticalLine(_canvas, 150));
				params.addPlotLine(new VerticalLine(_canvas, 210));
				params.addPlotLine(new VerticalLine(_canvas, 270));
				params.addPlotLine(new VerticalLine(_canvas, 330));
				
				
				params.setExtraStrings(String.format("rho = %-4.0f cm   z = %-5.0f cm", RHO, Z));
				params.setExtraDrawing(true);
				
				
			}
			
		};
		
		pdialog.setSize(800, 800);
		return pdialog;
	}
	
	
	protected static void swimTest(boolean probeCache) {
		
		FieldProbe.cache(probeCache);
		
		TextDisplayDialog dialog = new TextDisplayDialog("Test Results");
		dialog.setVisible(true);
		TextAreaWriter writer = dialog.getWriter();
		
		int n = 1000;
		int micronAccuracy = 50;
        double accuracy = micronAccuracy/ 1.0e6;  //micons converted to meters

        writer.writeln("Some test results for the swimmer.");
		writer.writeln("Test: Fixed Z swimming, 1 GeV electron, Random " +
		UnicodeSupport.SMALL_PHI + " (0-360), " + UnicodeSupport.SMALL_THETA + " (20-40)");
		writer.writeln("Compare final |" + UnicodeSupport.CAPITAL_DELTA + UnicodeSupport.SMALL_RHO + "| for different full maps compared to symmetric map.");
		writer.writeln("Final Z accuraccy: " + micronAccuracy + " microns");
		writer.writeln("Number of swims: " + n);
		writer.writeln("-------------------------------------------------");

		
		double xo = 0;  //meters
		double yo = 0;
		double zo = 0;
		double rMax = 7;  //max radial coordinate meters
		double momentum = 1;  //GeV/c
		double stepSize = 5e-3; // m
		double maxPathLen = 8.0; // m
		double hdata[] = new double[3];
		double ztarget = 5.0; //meters
		int charge = -1;  //electron
		SwimTrajectory traj = null;

		

		//get the random values
		double phi[] = new double[n];
		double theta[] = new double[n];
		
		//test results
		double xf[] = new double[n];
		double yf[] = new double[n];
		double zf[] = new double[n];
		
		double Q[][] = new double[n][6];
		double Qf[] = new double[7];
		
		
		//stay away from coils
		long seed = 2343453249L;
		Random rand = new Random(seed);
		for (int i = 0; i < n; i++) {
			
			double base = 60*rand.nextInt(6);
			phi[i] = -30 + base + (5 + 50*rand.nextDouble());
			while(phi[i] < 360) {
				phi[i] += 360;
			}
			while(phi[i] > 360) {
				phi[i] -= 360;
			}

			theta[i] = 20.0 + 20.0*rand.nextDouble();
		}
		
		//set to the symmetric map
		MagneticFields.getInstance().setTorus(TorusMap.SYMMETRIC);
		Swimmer swimmer = new Swimmer(MagneticFields.getInstance().getActiveField());
		
		long time = System.nanoTime();

		for (int i = 0; i < n; i++) {
			try {
				traj = swimmer.swim(charge, xo, yo, zo,
						momentum, theta[i], phi[i], ztarget, accuracy,
						rMax, maxPathLen, stepSize,
						Swimmer.CLAS_Tolerance, hdata);
			} catch (RungeKuttaException e) {
				e.printStackTrace();
				return;
			}
			
			System.arraycopy(traj.lastElement(), 0, Q[i], 0, 6);
		}
		timeReport(writer, "Priming pump time", time, System.nanoTime());
		
//		for (int i = 0; i < 5; i++){
//			cleanTrajPoint(momentum, Q[i], Qf);
//			printVect(writer, momentum, Qf);
//		}
		
		//should produce perfect results
		compareField(writer, Q, TorusMap.SYMMETRIC, charge, xo, yo, zo,
				momentum, theta, phi, ztarget, accuracy,
				rMax, maxPathLen, stepSize,
				hdata);
		
		compareField(writer, Q, TorusMap.FULL_200, charge, xo, yo, zo,
				momentum, theta, phi, ztarget, accuracy,
				rMax, maxPathLen, stepSize,
				hdata);

		compareField(writer, Q, TorusMap.FULL_150, charge, xo, yo, zo,
				momentum, theta, phi, ztarget, accuracy,
				rMax, maxPathLen, stepSize,
				hdata);
	
		compareField(writer, Q, TorusMap.FULL_125, charge, xo, yo, zo,
				momentum, theta, phi, ztarget, accuracy,
				rMax, maxPathLen, stepSize,
				hdata);
		
		compareField(writer, Q, TorusMap.FULL_100, charge, xo, yo, zo,
				momentum, theta, phi, ztarget, accuracy,
				rMax, maxPathLen, stepSize,
				hdata);
		
		compareField(writer, Q, TorusMap.FULL_075, charge, xo, yo, zo,
				momentum, theta, phi, ztarget, accuracy,
				rMax, maxPathLen, stepSize,
				hdata);

		compareField(writer, Q, TorusMap.FULL_050, charge, xo, yo, zo,
				momentum, theta, phi, ztarget, accuracy,
				rMax, maxPathLen, stepSize,
				hdata);
		
		compareField(writer, Q, TorusMap.FULL_025, charge, xo, yo, zo,
				momentum, theta, phi, ztarget, accuracy,
				rMax, maxPathLen, stepSize,
				hdata);
		
		FieldProbe.cache(true);

	}
	
	private static void compareField(TextAreaWriter writer, double[][] results, TorusMap tmap, int charge, double xo, double yo, double zo, 
			double momentum, double theta[], double phi[], double ztarget, double accuracy, double rMax,
			double maxPathLen, double stepSize, double hdata[]) {
		MagneticFields.getInstance().setTorus(tmap);
		Swimmer swimmer = new Swimmer(MagneticFields.getInstance().getActiveField());
		SwimTrajectory traj = null;
		
		int n = theta.length;
		double Q[][] = new double[n][6];
		
		double resultQf[] = new double[7];
		double Qf[] = new double[7];


		writer.writeln("\nResults for torus map: " + tmap.getName());
		
		
		//once to prime the pump
		for (int i = 0; i < n; i++) {
			try {
				traj = swimmer.swim(charge, xo, yo, zo,
						momentum, theta[i], phi[i], ztarget, accuracy,
						rMax, maxPathLen, stepSize,
						Swimmer.CLAS_Tolerance, hdata);
			} catch (RungeKuttaException e) {
				e.printStackTrace();
				return;
			}
		}
		
		
		long time = System.nanoTime();

		for (int i = 0; i < n; i++) {
			try {
				traj = swimmer.swim(charge, xo, yo, zo,
						momentum, theta[i], phi[i], ztarget, accuracy,
						rMax, maxPathLen, stepSize,
						Swimmer.CLAS_Tolerance, hdata);
			} catch (RungeKuttaException e) {
				e.printStackTrace();
				return;
			}
			
			System.arraycopy(traj.lastElement(), 0, Q[i], 0, 6);
		}
		timeReport(writer, "Swim time", time, System.nanoTime());

//		for (int i = 0; i < 5; i++){
//			cleanTrajPoint(momentum, Q[i], Qf);
//			printVect(writer, momentum, Qf);
//		}
		
		double rhosum = 0;
		double zsum = 0;
		double rhomax = -1;
		double zmax = -1;
		int worstIndex = 0;
		
		for (int i = 0;i < n; i++) {
			cleanTrajPoint(momentum, results[i], resultQf);
			cleanTrajPoint(momentum, Q[i], Qf);
			double rhodiff = Math.abs(resultQf[3]-Qf[3]);
			double zdiff= Math.abs(resultQf[2]-Qf[2]);
			
			if (rhodiff > rhomax) {
				worstIndex = i;
				rhomax = rhodiff;
			}
			
			zmax = Math.max(zmax,  zdiff);
			
			rhosum += rhodiff;
			zsum += zdiff;
		}
		
		double rhoavg = rhosum/n;
		double zavg = zsum/n;
		
		writer.writeln(String.format("Avg |z diff| = %-10.5f   Max |z diff| =  %-10.5f cm", zavg, zmax));
		writer.writeln(String.format("Avg |" + UnicodeSupport.SMALL_RHO + " diff| = %-10.5f   Max |" + UnicodeSupport.SMALL_RHO + " diff| =  %-10.5f cm", rhoavg, rhomax));
		writer.writeln(String.format("Worst case: INDEX = %d   theta = %-8.3f  phi = %-8.3f", worstIndex, theta[worstIndex], phi[worstIndex]));

		
//		cleanTrajPoint(momentum, results[worstIndex], resultQf);
//		cleanTrajPoint(momentum, Q[worstIndex], Qf);
//		
//		writer.writeln("WORST CASE RHO DIFFERENCE"); 
//		writer.writeln("SYMMETRIC RESULT");
//		printVect(writer, momentum, resultQf);
//		writer.writeln(tmap.getName() + " RESULT");
//		printVect(writer, momentum, Qf);

	}
	
	private static void timeReport(TextAreaWriter writer, String message, long startNano, long stopNano) {
		long del = stopNano - startNano;
		
		if (del > 1000000000L) {
			double sec = del/1.0e9;
			writer.writeln(String.format("[%s] %-8.3f sec", message, sec));
		}
		else if (del > 1000000L) {
			double millisec = del/1.0e6;
			writer.writeln(String.format("[%s] %-8.3f millis", message, millisec));
		}
		else {
			double microsec = del/1.0e3;
			writer.writeln(String.format("[%s] %-8.3f " + UnicodeSupport.SMALL_MU+"s", message, microsec));
		}

	}
	
	private static void cleanTrajPoint(double momentum, double Q[], double Qf[]) {
		
		//Qf[0] = x in cm
		//Qf[1] = y in cm
		//Qf[2] = z in cm
		//Qf[3] = rho in cm
		//Qf[4] = norm (should be one)
		//Qf[5] = theta in deg
		//Qf[6] = phi in deg
		
		Qf[0] = Q[0]*100;
		Qf[1] = Q[1]*100;
		Qf[2] = Q[2]*100;
		Qf[3] = Math.hypot(Qf[0], Qf[1]);
		
		
		double norm = Math.sqrt(Q[3] * Q[3] + Q[4] * Q[4] + Q[5] * Q[5]);
		double px = Q[3];
		double py = Q[4];
		double pz = Q[5];
		
		Qf[4] = norm;
		
		Qf[5] = MagneticField.acos2Deg(pz);
		Qf[6] = MagneticField.atan2Deg(py, px);
		
	}
	
	
	
	private static void printVect(TextAreaWriter writer, double P, double Qf[]) {
		
		//Qf[0] = x in cm
		//Qf[1] = y in cm
		//Qf[2] = z in cm
		//Qf[3] = rho in cm
		//Qf[4] = norm should be 1
		//Qf[5] = theta in deg
		//Qf[6] = phi in deg
	
		
		writer.write(String.format("R = [%-9.4f, %-9.4f, %-9.4f]  ", Qf[0], Qf[1], Qf[2]));
		writer.write(String.format(UnicodeSupport.SMALL_RHO + " = %9.4f  ", Qf[3]));
		writer.write(String.format("norm = %-8.4f  ", Qf[4]));
		writer.write(String.format(UnicodeSupport.SMALL_THETA + " = %-8.3f  ", Qf[5]));
		writer.writeln(String.format(UnicodeSupport.SMALL_PHI + " = %-8.3f  ", Qf[6]));
	}
}
