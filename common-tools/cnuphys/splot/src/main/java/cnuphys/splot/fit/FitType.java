package cnuphys.splot.fit;

import java.util.EnumMap;

import com.nr.model.Fitab;
import com.nr.model.Fitexy;
import com.nr.model.Fitsvd;

import cnuphys.splot.plot.DoubleFormat;
import cnuphys.splot.plot.UnicodeSupport;
import cnuphys.splot.plot.X11Colors;
import cnuphys.splot.style.EnumComboBox;

public enum FitType {

	NOLINE, CONNECT, STAIRS, LINE, POLYNOMIAL, GAUSSIANS, POLYPLUSGAUSS, ERF, ERFC, ALTPOLYNOMIAL, CUBICSPLINE;

	// some hex colors
	private static final String WHEAT = X11Colors.getX11ColorAsHex("WHEAT");
	private static final String DARKRED = X11Colors.getX11ColorAsHex("Dark RED");
	private static final String DARKGREEN = X11Colors.getX11ColorAsHex("Dark GREEN");

	/**
	 * A map for the names of the fit types
	 */
	public static EnumMap<FitType, String> names = new EnumMap<FitType, String>(FitType.class);

	static {
		names.put(CONNECT, "Simple Connect");
		names.put(STAIRS, "Stairs");
		names.put(LINE, "Line Fit");
		names.put(CUBICSPLINE, "Cubic Spline");
		names.put(POLYNOMIAL, "Polynomial");
		names.put(GAUSSIANS, "Gaussians");
		names.put(POLYPLUSGAUSS, "Poly & Gaussians");
		names.put(ERF, "Erf Function");
		names.put(ERFC, "Erfc Function");
		names.put(ALTPOLYNOMIAL, "Alt Polynomial");
		names.put(NOLINE, "No Line");
	}

	private static final String _MU = UnicodeSupport.SMALL_MU;
	private static final String _SUM = UnicodeSupport.CAPITAL_SIGMA;
	private static final String _EOL = "<BR>";
	private static final String _SP = "&nbsp;";

	/**
	 * Get the nice name of the enum.
	 * 
	 * @return the nice name, for combo boxes, menus, etc.
	 */
	public String getName() {
		return names.get(this);
	}

	/**
	 * Returns the enum value from the name.
	 * 
	 * @param name the name to match.
	 * @return the <code>FitType</code> that corresponds to the name. Returns
	 *         <code>null</code> if no match is found.
	 */
	public static FitType getValue(String name) {
		if (name == null) {
			return null;
		}

		for (FitType val : values()) {
			// check the nice name
			if (name.equalsIgnoreCase(val.getName())) {
				return val;
			}
			// check the base name
			if (name.equalsIgnoreCase(val.name())) {
				return val;
			}
		}
		return null;
	}

	/**
	 * Get an html string the describes the fit
	 * 
	 * @param object the fit object that will have to be cast appropriately
	 * @return an html string the describes the fit
	 */
	public static String getFitString(Fit curveFit) {
		StringBuffer sb = new StringBuffer(1024);

		FitType type = curveFit.getFitType();
		Object fit = curveFit.getFit(); // must be cast

		switch (type) {

		case NOLINE:
			sb.append("No line.");
			break;

		case CONNECT:
			sb.append("Connect the points.");
			break;

		case STAIRS:
			sb.append("Staircase connection.");
			break;

		case CUBICSPLINE:
			sb.append("Natural cubic spline interpolation");
			break;

		case LINE:
			sb.append(header("Linear Fit:") + descript("y = a + b&thinsp;x"));

			if (fit != null) {

				if (fit instanceof Fitab) {
					Fitab fab = (Fitab) fit;
					sb.append(pmString("a", fab.a, fab.siga) + "<BR>");
					sb.append(pmString("b", fab.b, fab.sigb) + "<BR>");
					sb.append(errorType(curveFit));
					sb.append(chiSqString(fab.chi2));
				}
				else if (fit instanceof Fitexy) {
					Fitexy fitexy = (Fitexy) fit;
					sb.append(pmString("a", fitexy.a, fitexy.siga) + "<BR>");
					sb.append(pmString("b", fitexy.b, fitexy.sigb) + "<BR>");
					sb.append(errorType(curveFit));
					sb.append(chiSqString(fitexy.chi2));
				}
			}
			else {
				sb.append(warning("LINEAR FIT PROBLEM"));
			}
			break; // LINE

		case POLYNOMIAL:
			sb.append(header("Polynomial Fit") + descript("y = " + _SUM + sub("A", "n") + "&thinsp;x<SUP>n</SUP>"));
			sb.append(info("Polynomial Order: " + curveFit.getPolynomialOrder()));

			if (fit != null) {
				Fitsvd fsvd = (Fitsvd) fit;

				for (int i = 0; i < fsvd.a.length; i++) {
					sb.append(pmString("A<SUB>" + i + "</SUB>", fsvd.a[i], Math.sqrt(fsvd.covar[i][i])) + "<BR>");
				}
				sb.append(errorType(curveFit));
				sb.append(chiSqString(fsvd.chisq));
			}
			else {
				sb.append(warning("POLYNOMIAL FIT PROBLEM"));
			}
			break; // POLYNOMIAL

		case ERF:
			sb.append(header("Erf Fit:") + descript("y = A + B&thinsp;Erf[(x-" + _MU + ")/S" + "]"));

			if (fit != null) {
				ErfFit efit = (ErfFit) fit;

				double a[] = efit.getFitParameters();
				double covar[][] = efit.getCovarianceMatrix();
				double chisq = efit.getChiSquare();

				sb.append(pmString("A", a[0], Math.sqrt(covar[0][0])) + _EOL);
				sb.append(pmString("B", a[1], Math.sqrt(covar[1][1])) + _EOL);
				sb.append(pmString(_MU, a[2], Math.sqrt(covar[2][2])) + _EOL);
				sb.append(pmString("S", a[3], Math.sqrt(covar[3][3])) + _EOL);
				sb.append(colorStr("S/" + UnicodeSupport.SQRT + "2 = " + valStr(a[3] / Math.sqrt(2)), "gray") + _EOL);
				sb.append(errorType(curveFit));
				sb.append(chiSqString(chisq));
			}
			else {
				sb.append(warning("ERF FIT PROBLEM"));
			}
			break; // ERF

		case ERFC:
			sb.append(header("Erfc Fit:") + descript("y = A + B&thinsp;Erfc[(x-" + _MU + ")/S" + "]"));

			if (fit != null) {
				ErfcFit ecfit = (ErfcFit) fit;

				double a[] = ecfit.getFitParameters();
				double covar[][] = ecfit.getCovarianceMatrix();
				double chisq = ecfit.getChiSquare();

				sb.append(paramStr("A", a[0], covar[0][0], curveFit.isHeld(0)));
				sb.append(paramStr("B", a[1], covar[1][1], curveFit.isHeld(1)));
				sb.append(paramStr(_MU, a[2], covar[2][2], curveFit.isHeld(2)));
				sb.append(paramStr("S", a[3], covar[3][3], curveFit.isHeld(3)));
				sb.append(colorStr("S/" + UnicodeSupport.SQRT + "2 = " + valStr(a[3] / Math.sqrt(2)), "gray") + _EOL);
				sb.append(errorType(curveFit));
				sb.append(chiSqString(chisq));
			}
			else {
				sb.append(warning("ERFC FIT PROBLEM"));
			}
			break; // ERFC

		case ALTPOLYNOMIAL:
			sb.append(
					header("Alt Polynomial Fit:") + descript("y = " + _SUM + sub("A", "n") + "&thinsp;x<SUP>n</SUP>"));
			sb.append(info("Polynomial Order: " + curveFit.getPolynomialOrder()));

			if (fit != null) {
				AltPolynomialFit faltp = (AltPolynomialFit) fit;

				for (int i = 0; i < faltp.a.length; i++) {
					sb.append(pmString(sub("A", i), faltp.a[i], Math.sqrt(faltp.covar[i][i])) + "<BR>");
				}
				sb.append(errorType(curveFit));
				sb.append(chiSqString(faltp.chisq));
			}
			else {
				sb.append(warning("ALTERNATE POLY FIT PROBLEM"));
			}
			break; // ALTPOLYNOMIAL

		case GAUSSIANS:
			sb.append(header("Gaussian Fit:") + descript(" y = " + _SUM + sub("A", "n") + "&thinsp;exp{-[(x-"
					+ sub(_MU, "n") + ")/" + sub("S", "n") + "]<SUP>2</SUP>}"));
			sb.append(info("Number of Gaussians: " + curveFit.getNumGaussian()));

			if (fit != null) {
				GaussianFit fg = (GaussianFit) fit;

				for (int i = 0; i < fg.a.length / 3; i++) {
					int j = 3 * i;
					sb.append(pmString(sub("A", i), fg.a[j], Math.sqrt(fg.covar[j][j])) + _EOL);
					j++;
					sb.append(pmString(sub(_MU, i), fg.a[j], Math.sqrt(fg.covar[j][j])) + _EOL);
					j++;
					sb.append(pmString(sub("S", i), fg.a[j], Math.sqrt(fg.covar[j][j])) + _EOL);
				}
				sb.append(errorType(curveFit));
				sb.append(chiSqString(fg.chisq));
			}
			else {
				sb.append(warning("GAUSSIAN FIT PROBLEM"));
			}
			break; // GAUSSIANS

		case POLYPLUSGAUSS:
			sb.append(header("Gaussian & Polynomial Fit:")
					+ descript("y = " + _SUM + sub("A", "n") + "&thinsp;exp{-[(x-" + sub(_MU, "n") + ")/"
							+ sub("S", "n") + "]<SUP>2</SUP>} + " + _SUM + sub("D", "m") + "&thinsp;x<SUP>m</SUP>"));
			sb.append(info("Number of Gaussians: " + curveFit.getNumGaussian()));
			sb.append(info("Polynomial Order: " + curveFit.getPolynomialOrder()));
			sb.append(colorStr("<b>Gaussian Parameters</b>", "blue") + "<BR>");

			if (fit != null) {
				PolyAndGaussianFit fpg = (PolyAndGaussianFit) fit;

				for (int i = 0; i < fpg._numGauss; i++) {
					int j = 3 * i;
					sb.append(pmString(sub("A", i), fpg.a[j], Math.sqrt(fpg.covar[j][j])) + "<BR>");
					j++;
					sb.append(pmString(sub(_MU, i), fpg.a[j], Math.sqrt(fpg.covar[j][j])) + "<BR>");
					j++;
					sb.append(pmString(sub("S", i), fpg.a[j], Math.sqrt(fpg.covar[j][j])) + "<BR>");
				}
				// now the poly
				sb.append(colorStr("<b>Polynomial Parameters</b>", "blue") + _EOL);
				for (int i = 3 * fpg._numGauss; i < fpg.a.length; i++) {
					int j = i - 3 * fpg._numGauss;
					sb.append(pmString(sub("D", j), fpg.a[i], Math.sqrt(fpg.covar[i][i])) + _EOL);

				}

				sb.append(errorType(curveFit));
				sb.append(chiSqString(fpg.chisq));
			}
			else {
				sb.append(warning("GAUSSIAN PLUS POLY FIT PROBLEM"));
			}
			break;

		}

		return sb.toString();
	}

	// <FONT style="BACKGROUND-COLOR: yellow">next </FONT>
	private static String colorStr(String s, String fg, String bg) {
		String htmlstr = "<font style=\"COLOR: " + fg + "; " + "BACKGROUND-COLOR: " + bg + "\">" + s + "</font>";
		return htmlstr;
	}

	private static String paramStr(String name, double val, double var, boolean held) {
		String s = "";
		if (held) {
			s = colorStr(_SP + "<b>[HELD]</b>" + _SP, "yellow", "black") + _SP;
		}
		return s + pmString(name, val, Math.sqrt(var)) + _EOL;
	}

	// header string
	private static String header(String s) {
		return colorStr("<b>" + s + "</b>" + _EOL, "black", WHEAT);
	}

	private static String descript(String s) {
		return colorStr(s + _EOL, DARKRED, "white");
	}

	private static String info(String s) {
		return colorStr(s + _EOL, DARKGREEN, "white");
	}

	private static String warning(String s) {
		return colorStr("<b>" + s + "</b>" + _EOL, "red", "yellow");
	}

	private static String colorStr(String s, String color) {
		return "<font color=" + color + ">" + s + "</font>";
	}

	private static String errorType(Fit fit) {
		return colorStr("Errors: " + fit.getErrorType().name() + _EOL, "black");
	}

	// convert a value and an error into a a +- b string
	private static String pmString(String vname, double val, double sig) {

		String vstr = valStr(val);
		String vsigstr = valStr(sig);

		return vname + " = " + vstr + colorStr("&thinsp;&plusmn;&thinsp;" + vsigstr, "black");
	}

	// generate a chi-sq string
	private static String chiSqString(double val) {
		return UnicodeSupport.SMALL_CHI + "<SUP>2</SUP> = " + valStr(val);
	}

	// generate a value string
	private static String valStr(double val) {
		if (Double.isNaN(val)) {
			return "Nan";
		}
		else if (Double.isInfinite(val)) {
			return UnicodeSupport.INFINITY;
		}
		return DoubleFormat.doubleFormat(val, 4, 3);
	}

	// subscript
	private static String sub(String s, int i) {
		return s + "<SUB>" + i + "</SUB>";
	}

	// subscript
	private static String sub(String s, String ss) {
		return s + "<SUB>" + ss + "</SUB>";
	}

	/**
	 * Obtain a combo box of choices.
	 * 
	 * @param defaultChoice
	 * @return the combo box of fit types
	 */
	public static EnumComboBox getComboBox(FitType defaultChoice) {
		return new EnumComboBox(names, defaultChoice);
	}
}
