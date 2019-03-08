package cnuphys.splot.plot;

public class UnicodeSupport {

	public static final String SUPERPLUS = "\u207A";
	public static final String SUPERMINUS = "\u207B";
	public static final String DEGREE = "\u00B0";
	public static final String TIMES = "\u2715";
	public static final String SUPER2 = "\u00B2";
	public static final String SUBZERO = "\u2080";
	public static final String SUPERZERO = "\u2070";

	public static final String SUBN = "\u2099";
	public static final String SUPERN = "\u207F";

	public static final String PLUSMINUS = "\u00B1";
	public static final String APPROX = "\u2248";
	public static final String BULLET = "\u2219";
	public static final String DAGGER = "\u2020";
	public static final String SQRT = "\u221a";

	public static final String LEQ = "\u2264";
	public static final String GEQ = "\u2265";
	public static final String LL = "\u226A"; // much less than
	public static final String GG = "\u226B"; // much greater than
	public static final String PROPTO = "\u221D";
	public static final String EQUIV = "\u2261";
	public static final String SIM = "\u2236";
	public static final String SIMEQ = "\u2243";
	public static final String NEQ = "\u2260";
	public static final String PERP = "\u22A5";
	public static final String PARALLEL = "\u2225";
	public static final String INFINITY = "\u221E";
	public static final String LARROW = "\u2190";
	public static final String UARROW = "\u2191";
	public static final String RARROW = "\u2192";
	public static final String DARROW = "\u2193";
	public static final String LRARROW = "\u2194";
	public static final String UDARROW = "\u2195";

	// greek characters
	public static final String CAPITAL_ALPHA = "\u0391";
	public static final String CAPITAL_BETA = "\u0392";
	public static final String CAPITAL_GAMMA = "\u0393";
	public static final String CAPITAL_DELTA = "\u0394";
	public static final String CAPITAL_EPSILON = "\u0395";
	public static final String CAPITAL_ZETA = "\u0396";
	public static final String CAPITAL_ETA = "\u0397";
	public static final String CAPITAL_THETA = "\u0398";
	public static final String CAPITAL_IOTA = "\u0399";
	public static final String CAPITAL_KAPPA = "\u039A";
	public static final String CAPITAL_LAMBDA = "\u039B";
	public static final String CAPITAL_MU = "\u039C";
	public static final String CAPITAL_NU = "\u039D";
	public static final String CAPITAL_XI = "\u039E";
	public static final String CAPITAL_OMICRON = "\u039F";
	public static final String CAPITAL_PI = "\u03A0";
	public static final String CAPITAL_RHO = "\u03A1";
	public static final String CAPITAL_SIGMA = "\u03A3";
	public static final String CAPITAL_TAU = "\u03A4";
	public static final String CAPITAL_UPSILON = "\u03A5";
	public static final String CAPITAL_PHI = "\u03A6";
	public static final String CAPITAL_CHI = "\u03A7";
	public static final String CAPITAL_PSI = "\u03A8";
	public static final String CAPITAL_OMEGA = "\u03A9";
	public static final String CAPITAL_IOTA_WITH_DIALYTIKA = "\u03AA";
	public static final String CAPITAL_UPSILON_WITH_DIALYTIKA = "\u03AB";
	public static final String SMALL_ALPHA_WITH_TONOS = "\u03AC";
	public static final String SMALL_EPSILON_WITH_TONOS = "\u03AD";
	public static final String SMALL_ETA_WITH_TONOS = "\u03AE";
	public static final String SMALL_IOTA_WITH_TONOS = "\u03AF";
	public static final String SMALL_ALPHA = "\u03B1";
	public static final String SMALL_BETA = "\u03B2";
	public static final String SMALL_GAMMA = "\u03B3";
	public static final String SMALL_DELTA = "\u03B4";
	public static final String SMALL_EPSILON = "\u03B5";
	public static final String SMALL_ZETA = "\u03B6";
	public static final String SMALL_ETA = "\u03B7";
	public static final String SMALL_THETA = "\u03B8";
	public static final String SMALL_IOTA = "\u03B9";
	public static final String SMALL_KAPPA = "\u03BA";
	public static final String SMALL_LAMBDA = "\u03BB";
	public static final String SMALL_MU = "\u03BC";
	public static final String SMALL_NU = "\u03BD";
	public static final String SMALL_XI = "\u03BE";
	public static final String SMALL_OMICRON = "\u03BF";
	public static final String SMALL_PI = "\u03C0";
	public static final String SMALL_RHO = "\u03C1";
	public static final String SMALL_FINAL_SIGMA = "\u03C2";
	public static final String SMALL_SIGMA = "\u03C3";
	public static final String SMALL_TAU = "\u03C4";
	public static final String SMALL_UPSILON = "\u03C5";
	public static final String SMALL_PHI = "\u03C6";
	public static final String SMALL_CHI = "\u03C7";
	public static final String SMALL_PSI = "\u03C8";
	public static final String SMALL_OMEGA = "\u03C9";
	public static final String SMALL_IOTA_WITH_DIALYTIKA = "\u03CA";
	public static final String SMALL_UPSILON_WITH_DIALYTIKA = "\u03CB";
	public static final String SMALL_OMICRON_WITH_TONOS = "\u03CC";
	public static final String SMALL_UPSILON_WITH_TONOS = "\u03CD";
	public static final String SMALL_OMEGA_WITH_TONOS = "\u03CE";

	/**
	 * Replace the Latex-like special characters with their unicode equivalents.
	 * 
	 * @param s the input string
	 * @return the output, where special character sequences are replaced by unicode
	 *         characters.
	 */
	public static String specialCharReplace(String s) {
		if (s == null) {
			return null;
		}

		if (s.indexOf("\\") < 0) {
			return s;
		}

		s = s.replace("\\Alpha", CAPITAL_ALPHA);
		s = s.replace("\\alpha", SMALL_ALPHA);

		s = s.replace("\\Beta", CAPITAL_BETA);
		s = s.replace("\\beta", SMALL_BETA);

		s = s.replace("\\Gamma", CAPITAL_GAMMA);
		s = s.replace("\\gamma", SMALL_GAMMA);

		s = s.replace("\\Delta", CAPITAL_DELTA);
		s = s.replace("\\delta", SMALL_DELTA);

		s = s.replace("\\Epsilon", CAPITAL_EPSILON);
		s = s.replace("\\epsilon", SMALL_EPSILON);

		s = s.replace("\\Zeta", CAPITAL_ZETA);
		s = s.replace("\\zeta", SMALL_ZETA);

		s = s.replace("\\Eta", CAPITAL_ETA);
		s = s.replace("\\eta", SMALL_ETA);

		s = s.replace("\\Theta", CAPITAL_THETA);
		s = s.replace("\\theta", SMALL_THETA);

		s = s.replace("\\Iota", CAPITAL_IOTA);
		s = s.replace("\\iota", SMALL_IOTA);

		s = s.replace("\\Kappa", CAPITAL_KAPPA);
		s = s.replace("\\kappa", SMALL_KAPPA);

		s = s.replace("\\Lambda", CAPITAL_LAMBDA);
		s = s.replace("\\lambda", SMALL_LAMBDA);

		s = s.replace("\\Mu", CAPITAL_MU);
		s = s.replace("\\mu", SMALL_MU);

		s = s.replace("\\Nu", CAPITAL_NU);
		s = s.replace("\\nu", SMALL_NU);

		s = s.replace("\\Xi", CAPITAL_XI);
		s = s.replace("\\xi", SMALL_XI);

		s = s.replace("\\Omicron", CAPITAL_OMICRON);
		s = s.replace("\\omicron", SMALL_OMICRON);

		s = s.replace("\\Pi", CAPITAL_PI);
		s = s.replace("\\pi", SMALL_PI);

		s = s.replace("\\Rho", CAPITAL_RHO);
		s = s.replace("\\rho", SMALL_RHO);

		s = s.replace("\\Sigma", CAPITAL_SIGMA);
		s = s.replace("\\sigma", SMALL_SIGMA);

		s = s.replace("\\Tau", CAPITAL_TAU);
		s = s.replace("\\tau", SMALL_TAU);

		s = s.replace("\\Upsilon", CAPITAL_UPSILON);
		s = s.replace("\\upsilon", SMALL_UPSILON);

		s = s.replace("\\Phi", CAPITAL_PHI);
		s = s.replace("\\phi", SMALL_PHI);

		s = s.replace("\\Chi", CAPITAL_CHI);
		s = s.replace("\\chi", SMALL_CHI);

		s = s.replace("\\Psi", CAPITAL_PSI);
		s = s.replace("\\psi", SMALL_PSI);

		s = s.replace("\\Omega", CAPITAL_OMEGA);
		s = s.replace("\\omega", SMALL_OMEGA);

		// some math symbols
		s = s.replace("\\times", TIMES);
		s = s.replace("\\degree", DEGREE);
		s = s.replace("\\pm", PLUSMINUS);
		s = s.replace("\\approx", APPROX);
		s = s.replace("\\bullet", BULLET);

		s = s.replace("\\leq", LEQ);
		s = s.replace("\\geq", GEQ);
		s = s.replace("\\ll", LL);
		s = s.replace("\\gg", GG);
		s = s.replace("\\propto", PROPTO);
		s = s.replace("\\equiv", EQUIV);
		s = s.replace("\\sim", SIM);
		s = s.replace("\\simeq", SIMEQ);
		s = s.replace("\\neq", NEQ);
		s = s.replace("\\perp", PERP);
		s = s.replace("\\parallel", PARALLEL);
		s = s.replace("\\infinity", INFINITY);

		s = s.replace("\\larrow", LARROW);
		s = s.replace("\\uarrow", UARROW);
		s = s.replace("\\rarrow", RARROW);
		s = s.replace("\\darrow", DARROW);
		s = s.replace("\\lrarrow", LRARROW);
		s = s.replace("\\udarrow", UDARROW);

		s = s.replace("\\dagger", DAGGER);
		return s;
	}

}