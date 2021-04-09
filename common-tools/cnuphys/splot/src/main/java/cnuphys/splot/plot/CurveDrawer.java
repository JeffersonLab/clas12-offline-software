package cnuphys.splot.plot;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import cnuphys.splot.fit.AltPolynomialFit;
import cnuphys.splot.fit.CubicSpline;
import cnuphys.splot.fit.ErfFit;
import cnuphys.splot.fit.ErfcFit;
import cnuphys.splot.fit.Fit;
import cnuphys.splot.fit.FitType;
import cnuphys.splot.fit.FitUtilities;
import cnuphys.splot.fit.GaussianFit;
import cnuphys.splot.fit.IValueGetter;
import cnuphys.splot.fit.LineFit;
import cnuphys.splot.fit.LinearExyFit;
import cnuphys.splot.fit.PolyAndGaussianFit;
import cnuphys.splot.fit.PolyFit;
import cnuphys.splot.pdata.DataColumn;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.Histo2DData;
import cnuphys.splot.pdata.HistoData;
import cnuphys.splot.style.IStyled;
import cnuphys.splot.style.Styled;
import cnuphys.splot.style.SymbolDraw;
import cnuphys.splot.style.SymbolType;

public class CurveDrawer {

	private static final Color _transGray = new Color(80, 80, 80, 16);

	public static void drawHisto2D(Graphics g, PlotCanvas canvas, DataColumn histoColumn) {
		Histo2DData hd2 = histoColumn.getHistoData2D();

		long maxCount = hd2.getMaxCount();
		if (maxCount < 1) {
			return;
		}

		long counts[][] = hd2.getCounts();

		Gradient gradient = canvas.getGradient();

		Rectangle r = new Rectangle();
		for (int xbin = 0; xbin < hd2.getNumberBinsX(); xbin++) {
			for (int ybin = 0; ybin < hd2.getNumberBinsY(); ybin++) {
				long count = counts[xbin][ybin];
				double fract = ((double) count) / maxCount;

				Color color = gradient.getColor(fract);

				Rectangle2D.Double wrect = hd2.getRectangle(xbin, ybin);
//				System.err.println("WR: " + wrect);
				canvas.worldToLocal(r, wrect);

				g.setColor(color);
				;
				g.fillRect(r.x, r.y, r.width, r.height);

			}
		}
	}

	/**
	 * Draw a 1D histogram
	 * 
	 * @param g           the graphics context
	 * @param plotCanvas  the plot canvas
	 * @param histoColumn the column (should contain a HistData object)
	 */
	public static void drawHisto1D(Graphics g, PlotCanvas canvas, DataColumn histoColumn) {

		if (!histoColumn.isVisible()) {
			return;
		}

		HistoData hd = histoColumn.getHistoData();

		Polygon poly = HistoData.GetPolygon(canvas, hd);
		IStyled style = histoColumn.getStyle();

		g.setColor(style.getFillColor());
		g.fillPolygon(poly);

		Color borderColor = style.getBorderColor();
		if (borderColor == null) {
			borderColor = _transGray;
		}
		else {
			// borderColor = new Color(borderColor.getRed(), borderColor.getGreen(),
			// borderColor.getBlue(), 16);
		}
		g.setColor(borderColor);
		g.drawPolygon(poly);

		long counts[] = hd.getCounts();
		int numBin = hd.getNumberBins();
		double x[] = new double[numBin];
		double y[] = new double[numBin];
		double err[] = new double[numBin];
		for (int bin = 0; bin < numBin; bin++) {
			x[bin] = hd.getBinMidValue(bin);
			y[bin] = counts[bin];
			err[bin] = Math.sqrt(y[bin]);
		}

		// draw a fit
		Fit yfit = histoColumn.getFit();
		if (yfit != null) {
			yfit.setX(x);
			yfit.setY(y);
			yfit.setSigmaY(null);
			if (fitDrawable(histoColumn)) {
				drawFit(g, canvas, yfit, histoColumn.getStyle());
			}
		}

		// draw statistical errors
		if (hd.drawStatisticalErrors()) {
			Point p0 = new Point();
			Point p1 = new Point();
			Point.Double wp = new Point.Double();

			for (int bin = 0; bin < numBin; bin++) {
				if (hd.getCount(bin) > 0) {
					double ymin = y[bin] - err[bin];
					double ymax = y[bin] + err[bin];
					wp.setLocation(x[bin], ymin);
					canvas.worldToLocal(p0, wp);
					wp.setLocation(x[bin], ymax);
					canvas.worldToLocal(p1, wp);
					g.drawLine(p0.x, p0.y, p1.x, p1.y);
					g.drawLine(p0.x - 2, p0.y, p0.x + 2, p0.y);
					g.drawLine(p1.x - 2, p1.y, p1.x + 2, p1.y);
				}
			}

		}

		// draw the bin borders
		// TODO make optional
		Rectangle b = canvas.getActiveBounds();
		int n = poly.npoints;
		g.setColor(borderColor);
		for (int i = 0; i < n; i++) {
			g.drawLine(poly.xpoints[i], poly.ypoints[i], poly.xpoints[i], b.y + b.height);
		}

	}

	/**
	 * Draw a curve with no error bars
	 * 
	 * @param g          the graphics context
	 * @param plotCanvas the plot canvas
	 * @param xcol       the x data column
	 * @param ycol       the y data column
	 */
	public static void drawCurve(Graphics g, PlotCanvas plotCanvas, DataColumn xcol, DataColumn ycol) {
		drawCurve(g, plotCanvas, xcol, ycol, null, null);
	}

	/**
	 * Draw a curve with x and y error bars
	 * 
	 * @param g          the graphics context
	 * @param plotCanvas the plot canvas
	 * @param xcol       the x data column
	 * @param ycol       the y data column
	 * @param xerrCol    the x error bar column (often <code>null</code>)
	 * @param yerrCol    the y error bar column
	 */
	public static void drawCurve(Graphics g, PlotCanvas plotCanvas, DataColumn xcol, DataColumn ycol,
			DataColumn xerrCol, DataColumn yerrCol) {

		if (!ycol.isVisible()) {
			return;
		}

		Point2D.Double wp = new Point2D.Double();
		Point p0 = new Point();
		Point p1 = new Point();

		double x[] = xcol.getMinimalCopy();
		double y[] = ycol.getMinimalCopy();

		if ((x == null) || (x.length < 1)) {
			return;
		}
		if ((y == null) || (y.length < 1)) {
			return;
		}

		double ysig[] = null;
		double xsig[] = null;
		if (xerrCol != null) {
			xsig = xerrCol.getMinimalCopy();
		}
		if (yerrCol != null) {
			ysig = yerrCol.getMinimalCopy();
		}

		// is there a fit?
		Fit yfit = ycol.getFit();
		if (yfit != null) {
			if (yfit.isDirty()) {
				yfit.setX(x);
				yfit.setY(y);
				yfit.setSigmaX(xsig);
				yfit.setSigmaY(ysig);
			}
			if (fitDrawable(ycol)) {
				drawFit(g, plotCanvas, yfit, ycol.getStyle());
			}
		}

		// symbols?
		Styled style = ycol.getStyle();

		if (style == null) {
			return;
		}

		if (style.getSymbolType() != SymbolType.NOSYMBOL) {
			for (int i = 0; i < x.length; i++) {

				if (xsig != null) {
					double x0 = x[i] - xsig[i];
					double x1 = x[i] + xsig[i];
					wp.setLocation(x0, y[i]);
					plotCanvas.worldToLocal(p0, wp);
					wp.setLocation(x1, y[i]);
					plotCanvas.worldToLocal(p1, wp);
					g.drawLine(p0.x, p0.y, p1.x, p1.y);
				}

				if (ysig != null) {
					double y0 = y[i] - ysig[i];
					double y1 = y[i] + ysig[i];
					wp.setLocation(x[i], y0);
					plotCanvas.worldToLocal(p0, wp);
					wp.setLocation(x[i], y1);
					plotCanvas.worldToLocal(p1, wp);
					g.drawLine(p0.x, p0.y, p1.x, p1.y);
				}

				wp.setLocation(x[i], y[i]);
				plotCanvas.worldToLocal(p0, wp);
				SymbolDraw.drawSymbol(g, p0.x, p0.y, style);

			}
		}

	}

	// check whether a fit is drawable
	private static boolean fitDrawable(DataColumn ycol) {
		if (ycol == null) {
			return false;
		}

		Fit fit = ycol.getFit();

		if (fit == null) {
			return false;
		}

		if (ycol.isHistogram1D()) {
			HistoData hd = ycol.getHistoData();
			if ((hd == null) || (hd.getGoodCount() < 3)) {
				return false;
			}
		}

		int size = fit.size();
		if (fit.getFitType() == FitType.LINE) {
			return (size > 1);
		}
		else {
			return (size > 2);
		}
	}

	// draw the fit
	private static void drawFit(Graphics g, PlotCanvas plotCanvas, Fit yfit, IStyled style) {

		if (yfit.size() < 2) {
			return;
		}

		Point2D.Double wp = new Point2D.Double();
		Point p0 = new Point();
		Point p1 = new Point();

		Graphics2D g2 = (Graphics2D) g;

		Stroke oldStroke = g2.getStroke();
		g2.setStroke(GraphicsUtilities.getStroke(style.getFitLineWidth(), style.getFitLineStyle()));

		Color fitColor = style.getFitLineColor();
		if (fitColor == null) {
			return;
		}
		g2.setColor(fitColor);

		double x[] = yfit.getX();
		double y[] = yfit.getY();

		FitType fitType = yfit.getFitType();

		switch (fitType) {
		case NOLINE:
			break;

		case CONNECT:
			FitUtilities.parallelSort(x, y);
			wp.setLocation(x[0], y[0]);
			plotCanvas.worldToLocal(p0, wp);

			for (int i = 1; i < x.length; i++) {
				wp.setLocation(x[i], y[i]);
				plotCanvas.worldToLocal(p1, wp);
				g2.drawLine(p0.x, p0.y, p1.x, p1.y);
				p0.setLocation(p1);
			}
			break;

		case STAIRS:
			FitUtilities.parallelSort(x, y);
			wp.setLocation(x[0], y[0]);
			plotCanvas.worldToLocal(p0, wp);

			Rectangle rr = plotCanvas.getActiveBounds();
			int bottom = rr.y + rr.height;

			for (int i = 1; i < x.length; i++) {
				wp.setLocation(x[i], y[i]);
				plotCanvas.worldToLocal(p1, wp);

				g2.setColor(style.getFillColor());
				g.fillRect(p0.x, p0.y, p1.x - p0.x, bottom - p0.y);

				g2.setColor(style.getFitLineColor());
				g2.drawLine(p0.x, p0.y, p1.x, p0.y);
				g2.drawLine(p1.x, p0.y, p1.x, p1.y);
				p0.setLocation(p1);
			}
			break;

		case LINE:
			if (yfit.isDirty()) {
				try {
					FitUtilities.fitStraightLine(yfit);
				}
				catch (Exception e) {
					System.err.println("Line Fit FAILED");
					yfit.setDirty();
					return;
				}
			}
			if (plotCanvas.getDataSet().hasXErrors()) {
				LinearExyFit lexyfit = (LinearExyFit) yfit.getFit();
				drawValueGetter(g2, plotCanvas, lexyfit);
			}
			else {
				LineFit lfit = (LineFit) yfit.getFit();
				drawValueGetter(g2, plotCanvas, lfit);
			}
			break;

		case ERF:
			if (yfit.isDirty()) {
				try {
					// FitUtilities.doFit(yfit, "cnuphys.splot.fit.ErfFit");
					FitUtilities.fitErf(yfit);
				}
				catch (Exception e) {
					System.err.println("Erf Fit FAILED");
					yfit.setDirty();
					return;
				}
			}

			ErfFit efit = (ErfFit) yfit.getFit();
			drawValueGetter(g2, plotCanvas, efit);

			break;

		case ERFC:
			if (yfit.isDirty()) {
				try {
					yfit.hold(0, 0);
					yfit.hold(1, 0.5);
					// FitUtilities.doFit(yfit, "cnuphys.splot.fit.ErfcFit");
					FitUtilities.fitErfc(yfit);
				}
				catch (Exception e) {
					System.err.println("Erfc Fit FAILED");
					yfit.setDirty();
					return;
				}
			}

			ErfcFit ecfit = (ErfcFit) yfit.getFit();
			drawValueGetter(g2, plotCanvas, ecfit);

			break;

		case GAUSSIANS:
			if (yfit.isDirty()) {
				System.err.println("Gaussian fit with errors num: " + yfit.getNumGaussian());
				try {
					// FitUtilities.doFit(yfit,
					// "cnuphys.splot.fit.GaussianFit");
					FitUtilities.fitGaussians(yfit);
				}
				catch (Exception e) {
					System.err.println("Gaussian Fit FAILED: " + e.getMessage());
					yfit.setDirty();
					return;
				}
			}

			GaussianFit ngfit = (GaussianFit) yfit.getFit();
			drawValueGetter(g2, plotCanvas, ngfit);
			break;

		case CUBICSPLINE:
			FitUtilities.parallelSort(x, y);
			if (yfit.isDirty()) {
				yfit.setFit(new CubicSpline(x, y));
			}
			CubicSpline nrcubic = (CubicSpline) yfit.getFit();
			drawValueGetter(g2, plotCanvas, nrcubic);
			break;

		case POLYNOMIAL:
			if (yfit.isDirty()) {
				FitUtilities.fitPoly(yfit);
			}

			PolyFit nrpfit = (PolyFit) yfit.getFit();
			drawValueGetter(g2, plotCanvas, nrpfit);
			break;

		case ALTPOLYNOMIAL:
			if (yfit.isDirty()) {
				FitUtilities.fitAltPoly(yfit);
				// FitUtilities.doFit(yfit,
				// "cnuphys.splot.fit.AltPolynomialFit");
			}

			AltPolynomialFit altfit = (AltPolynomialFit) yfit.getFit();
			drawValueGetter(g2, plotCanvas, altfit);
			break;

		case POLYPLUSGAUSS:
			if (yfit.isDirty()) {
				FitUtilities.fitGaussPlusPoly(yfit);
				// FitUtilities.doFit(yfit,
				// "cnuphys.splot.fit.PolyAndGaussianFit");
			}
			PolyAndGaussianFit npgfit = (PolyAndGaussianFit) yfit.getFit();
			drawValueGetter(g2, plotCanvas, npgfit);
			break;
		}

		g2.setStroke(oldStroke);
	}

	// draw a value getter
	private static void drawValueGetter(Graphics2D g, PlotCanvas plotCanvas, IValueGetter ivg) {

		if (ivg == null) {
			return;
		}

		// the plot screen rectangle
		Rectangle rect = plotCanvas.getActiveBounds();
		int iy = rect.y;
		Path2D poly = null;

		Point pp = new Point();
		Point.Double wp = new Point.Double();

		DataSet ds = plotCanvas.getDataSet();
		double ymid = 0.5 * (ds.getYmin() + ds.getYmax());

		wp.setLocation(ds.getXmin(), ymid);
		plotCanvas.worldToLocal(pp, wp);
		int xsmin = pp.x;
		wp.setLocation(ds.getXmax(), ymid);
		plotCanvas.worldToLocal(pp, wp);
		int xsmax = pp.x;

		// xsmin = Math.max(rect.x, xsmin);
		// xsmax = Math.min(rect.x + rect.width, xsmax);
		xsmin = rect.x;
		xsmax = rect.x + rect.width;

		for (int ix = xsmin; ix <= xsmax + rect.width; ix++) {
			pp.setLocation(ix, iy);
			plotCanvas.localToWorld(pp, wp);
			wp.y = ivg.value(wp.x);
			plotCanvas.worldToLocal(pp, wp);

			if (poly == null) {
				poly = new Path2D.Float();
				poly.moveTo(pp.x, pp.y);

			}
			else {
				poly.lineTo(pp.x, pp.y);
			}
		}
		g.draw(poly);
	}

}
