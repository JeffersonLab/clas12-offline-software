package cnuphys.lund;

import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;

public class LundSupport {

	// singleton
	private static LundSupport instance;
	// holds the ids
	private ArrayList<LundId> _lundIds;

	// some unicodes for particle symbols
	private static final String SUPERPLUS = "\u207A";
	private static final String SUPERMINUS = "\u207B";
	private static final String SUPERZERO = "\u2070";
	private static final String SUBZERO = "\u2080";
	
	private static final String BIG_DELTA = "\u0394";
	private static final String BIG_SIGMA = "\u03A3";

	private static final String BIG_LAMBDA = "\u039B";
	private static final String SMALL_GAMMA = "\u03B3";
	private static final String SMALL_MU = "\u03BC";
	private static final String SMALL_PI = "\u03C0";
	private static final String SMALL_RHO = "\u03C1";
	
	private static final String SMALL_ETA = "\u03B7";
	private static final String SMALL_OMEGA = "\u03C9";
	
	private static final String OVERLINE = "\u0305";
	

	private static Color brown = X11Colors.getX11Color("Brown");
	private static Color goldenrod = X11Colors.getX11Color("Dark Goldenrod");
	
	private static Color darkGreen = X11Colors.getX11Color("Dark Green");
	private static Color darkOrange = X11Colors.getX11Color("Dark Orange");
	private static Color wheat = X11Colors.getX11Color("Wheat");
	private static Color purple = X11Colors.getX11Color("Purple");
	private static Color cadetBlue = X11Colors.getX11Color("Cadet Blue");
	private static Color lawnGreen = X11Colors.getX11Color("Lawn Green");
	private static Color orangeRed = X11Colors.getX11Color("Orange Red");
	private static Color olive = X11Colors.getX11Color("Olive");
	private static Color deepsky = X11Colors.getX11Color("Deep Sky Blue");

	/** Unknown positive lepton */
	public static LundId unknownPlus = new LundId("Lepton", "?" + SUPERPLUS, 0, 0, 3, 0);

	/** Unknown negative lepton */
	public static LundId unknownMinus = new LundId("Lepton", "?" + SUPERMINUS, -1, 0, -3, 0);

	/** Unknown neutral "lepton" */
	public static LundId unknownNeutral = new LundId("Lepton", "?" + SUPERZERO, -2, 0, 0, 0);

	/**
	 * private constructor for the singleton.
	 */
	private LundSupport() {
		initialize();
		// sort based on Id so we can use binary search
		Collections.sort(_lundIds);
	}

	/**
	 * public access to the singleton
	 * 
	 * @return the singleton object.
	 */
	public static LundSupport getInstance() {
		if (instance == null) {
			instance = new LundSupport();
			instance.initStyles();
		}
		return instance;
	}

	/**
	 * Get a string representing a table of Ids.
	 * 
	 * @return a string representing a table of Ids.
	 */
	@Override
	public String toString() {
		if ((_lundIds == null) || (_lundIds.size() < 1)) {
			return "Empty collection of Lund Ids";
		}
		StringBuffer sb = new StringBuffer(_lundIds.size() * 80);
		for (LundId lid : _lundIds) {
			sb.append(lid);
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * Obtain the LundID object for a track based particle
	 * 
	 * @return the LundID object for an geantino
	 */
	public static LundId getTrackbased(int q) {
		if (q < 0) {
			return LundSupport.getInstance().get(-101);
		}
		if (q > 0) {
			return LundSupport.getInstance().get(-99);
		} else {
			return LundSupport.getInstance().get(-100);
		}
	}

	/**
	 * Obtain the LundID object for a hit based particle
	 * 
	 * @return the LundID object for a hit based particle
	 */
	public static LundId getHitbased(int q) {
		if (q < 0) {
			return LundSupport.getInstance().get(-201);
		}
		if (q > 0) {
			return LundSupport.getInstance().get(-199);
		} else {
			return LundSupport.getInstance().get(-200);
		}
	}

	/**
	 * Obtain the LundID object for a cvt based particle
	 * 
	 * @return the LundID object for a hit based particle
	 */
	public static LundId getCVTbased(int q) {
		if (q < 0) {
			return LundSupport.getInstance().get(-301);
		}
		if (q > 0) {
			return LundSupport.getInstance().get(-299);
		} else {
			return LundSupport.getInstance().get(-300);
		}
	}

	/**
	 * Obtain the LundID object for an electron
	 * 
	 * @return the LundID object for an electron
	 */
	public static LundId getElectron() {
		return LundSupport.getInstance().get(11);
	}

	/**
	 * Obtain the LundID object for a positron
	 * 
	 * @return the LundID object for a positron
	 */
	public static LundId getPositron() {
		return LundSupport.getInstance().get(-11);
	}

	/**
	 * Obtain the LundID object for a proton
	 * 
	 * @return the LundID object for a proton
	 */
	public static LundId getProton() {
		return LundSupport.getInstance().get(2212);
	}

	/**
	 * Initialize the Ids. This has been pruned a bit for JLab use.
	 */
	private void initialize() {
		_lundIds = new ArrayList<LundId>(400);

		// Geantinos or generic
		_lundIds.add(unknownPlus);
		_lundIds.add(unknownMinus);
		_lundIds.add(unknownNeutral);

		// unknowns (orange--track based)
		_lundIds.add(new LundId("Lepton", "?TB" + SUPERPLUS, -99, 0, 3, 0));
		_lundIds.add(new LundId("Lepton", "?TB" + SUPERMINUS, -101, 0, -3, 0));
		_lundIds.add(new LundId("Lepton", "?TB" + SUPERZERO, -100, 0, 0, 0));

		// unknowns (yellow--hit based)
		_lundIds.add(new LundId("Lepton", "?HB" + SUPERPLUS, -199, 0, 3, 0));
		_lundIds.add(new LundId("Lepton", "?HB" + SUPERMINUS, -201, 0, -3, 0));
		_lundIds.add(new LundId("Lepton", "?HB" + SUPERZERO, -200, 0, 0, 0));

		// unknowns (green--cvt based)
		_lundIds.add(new LundId("Lepton", "?CVT" + SUPERPLUS, -299, 0, 3, 0));
		_lundIds.add(new LundId("Lepton", "?CVT" + SUPERMINUS, -301, 0, -3, 0));
		_lundIds.add(new LundId("Lepton", "?CVT" + SUPERZERO, -300, 0, 0, 0));

		_lundIds.add(new LundId("InterBoson", "g", 21, 0, 0, 2));
		// 510998910
		_lundIds.add(new LundId("Lepton", "e" + SUPERMINUS, 11, 0.00051099891, -3, 1)); // e-
		_lundIds.add(new LundId("Lepton", "e" + SUPERPLUS, -11, 0.00051099891, 3, 1)); // e+

		_lundIds.add(new LundId("Baryon", "p", 2212, 0.93827203, 3, 1));
		_lundIds.add(new LundId("Baryon", "n", 2112, 0.93956536, 0, 1));

		_lundIds.add(new LundId("Meson", SMALL_PI + SUPERZERO, 111, 0.1349766, 0, 0));
		_lundIds.add(new LundId("Meson", SMALL_PI + SUPERPLUS, 211, 0.13957018, 3, 0)); // pi +
		_lundIds.add(new LundId("Meson", SMALL_PI + SUPERMINUS, -211, 0.13957018, -3, 0)); // pi -

		_lundIds.add(new LundId("Lepton", "nu_e", 12, 0, 0, 1));
		_lundIds.add(new LundId("Lepton", "anti-nu_e", -12, 0, 0, 1));
		_lundIds.add(new LundId("Lepton", SMALL_MU + SUPERMINUS, 13, 0.1056584, -3, 1)); // mu-
		_lundIds.add(new LundId("Lepton", SMALL_MU + SUPERPLUS, -13, 0.1056584, 3, 1)); // mu+
		_lundIds.add(new LundId("Lepton", "nu_mu", 14, 0, 0, 1));
		_lundIds.add(new LundId("Lepton", "anti-nu_mu", -14, 0, 0, 1));
		// _lundIds.add(new LundId("Lepton", "tau-", 15, 1.7770, -3, 1));
		// _lundIds.add(new LundId("Lepton", "tau+", -15, 1.7770, 3, 1));
		// _lundIds.add(new LundId("Lepton", "nu_tau", 16, 0, 0, 1));
		// _lundIds.add(new LundId("Lepton", "anti-nu_tau", -16, 0, 0, 1));
		// _lundIds.add(new LundId("Lepton", "L-", 17, 400., -3, 1));
		// _lundIds.add(new LundId("Lepton", "L+", -17, 400., 3, 1));
		// _lundIds.add(new LundId("Lepton", "nu_L", 18, 0, 0, 1));
		// _lundIds.add(new LundId("Lepton", "anti-nu_L", -18, 0, 0, 1));
		_lundIds.add(new LundId("InterBoson", SMALL_GAMMA, 22, 0, 0, 2)); // photon
		// _lundIds.add(new LundId("InterBoson", "Z0", 23, 91.187, 0, 2));
		// _lundIds.add(new LundId("InterBoson", "W+", 24, 80.419, 3, 2));
		// _lundIds.add(new LundId("InterBoson", "W-", -24, 80.419, -3, 2));
		// _lundIds.add(new LundId("InterBoson", "Z'0", 32, 500., 0, 2));
		// _lundIds.add(new LundId("InterBoson", "Z''0", 33, 900., 0, 2));
		// _lundIds.add(new LundId("InterBoson", "W'+", 34, 500., 3, 2));
		// _lundIds.add(new LundId("InterBoson", "W'-", -34, 500., -3, 2));
		// _lundIds.add(new LundId("InterBoson", "A0", 36, 300., 0, 0));
		// _lundIds.add(new LundId("InterBoson", "R0", 40, 5000., 0, 2));
		// _lundIds.add(new LundId("InterBoson", "anti-R0", -40, 5000., 0, 2));
		_lundIds.add(new LundId("Meson", "pi(2S)0", 20111, 1.30, 0, 0));
		_lundIds.add(new LundId("Meson", "pi(2S)+", 20211, 1.30, 3, 0));
		_lundIds.add(new LundId("Meson", "pi(2S)-", -20211, 1.30, -3, 0));
		_lundIds.add(new LundId("Meson", SMALL_ETA, 221, 0.547853, 0, 0));
		_lundIds.add(new LundId("Meson", "eta(2S)", 20221, 1.297, 0, 0));
		_lundIds.add(new LundId("Meson", "eta'", 331, 0.95766, 0, 0));
		_lundIds.add(new LundId("Meson", SMALL_RHO + SUPERZERO, 113, 0.7685, 0, 2));
		_lundIds.add(new LundId("Meson", SMALL_RHO + SUPERPLUS, 213, 0.7685, 3, 2));
		_lundIds.add(new LundId("Meson", SMALL_RHO + SUPERMINUS, -213, 0.7685, -3, 2));
		_lundIds.add(new LundId("Meson", "rho(2S)0", 30113, 1.46, 0, 2));
		_lundIds.add(new LundId("Meson", "rho(2S)+", 30213, 1.46, 3, 2));
		_lundIds.add(new LundId("Meson", "rho(2S)-", -30213, 1.46, -3, 2));
		_lundIds.add(new LundId("Meson", "rho(3S)0", 40113, 1.70, 0, 2));
		_lundIds.add(new LundId("Meson", "rho(3S)+", 40213, 1.70, 3, 2));
		_lundIds.add(new LundId("Meson", "rho(3S)-", -40213, 1.70, -3, 2));
		_lundIds.add(new LundId("Meson", SMALL_OMEGA, 223, 0.78257, 0, 2));
		_lundIds.add(new LundId("Meson", "omega(2S)", 30223, 1.42, 0, 2));
		_lundIds.add(new LundId("Meson", "phi", 333, 1.019455, 0, 2));
		// _lundIds.add(new LundId("Meson", "a_00", 10111, 0.9847, 0, 0));
		// _lundIds.add(new LundId("Meson", "a_0+", 10211, 0.9847, 3, 0));
		// _lundIds.add(new LundId("Meson", "a_0-", -10211, 0.9847, -3, 0));
		// _lundIds.add(new LundId("Meson", "f_0", 10221, 1.000, 0, 0));
		// _lundIds.add(new LundId("Meson", "f'_0", 10331, 1.4, 0, 0));
		// _lundIds.add(new LundId("Meson", "b_10", 10113, 1.231, 0, 2));
		// _lundIds.add(new LundId("Meson", "b_1+", 10213, 1.231, 3, 2));
		// _lundIds.add(new LundId("Meson", "b_1-", -10213, 1.231, -3, 2));
		// _lundIds.add(new LundId("Meson", "h_1", 10223, 1.17, 0, 2));
		// _lundIds.add(new LundId("Meson", "h'_1", 10333, 1.40, 0, 2));
		// _lundIds.add(new LundId("Meson", "a_10", 20113, 1.23, 0, 2));
		// _lundIds.add(new LundId("Meson", "a_1+", 20213, 1.23, 3, 2));
		// _lundIds.add(new LundId("Meson", "a_1-", -20213, 1.23, -3, 2));
		// _lundIds.add(new LundId("Meson", "f_1", 20223, 1.2822, 0, 2));
		// _lundIds.add(new LundId("Meson", "f'_1", 20333, 1.4268, 0, 2));
		// _lundIds.add(new LundId("Meson", "a_20", 115, 1.318, 0, 4));
		// _lundIds.add(new LundId("Meson", "a_2+", 215, 1.318, 3, 4));
		// _lundIds.add(new LundId("Meson", "a_2-", -215, 1.318, -3, 4));
		// _lundIds.add(new LundId("Meson", "f_2", 225, 1.275, 0, 4));
		// _lundIds.add(new LundId("Meson", "f_0(1500)", 50221, 1.500, 0, 0));
		// _lundIds.add(new LundId("Meson", "f'_2", 335, 1.525, 0, 4));
		// _lundIds.add(new LundId("Meson", "K0", 311, 0.497614, 0, 0));
		// _lundIds.add(new LundId("Meson", "anti-K0", -311, 0.497614, 0, 0));
		// _lundIds.add(new LundId("Meson", "K_S0", 310, 0.497614, 0, 0));
		// _lundIds.add(new LundId("Meson", "K_L0", 130, 0.497614, 0, 0));
		_lundIds.add(new LundId("Meson", "K" + SUPERPLUS, 321, 0.493677, 3, 0));
		_lundIds.add(new LundId("Meson", "K" + SUPERMINUS, -321, 0.493677, -3, 0));
		// _lundIds.add(new LundId("Meson", "K*0", 313, 0.89600, 0, 2));
		// _lundIds.add(new LundId("Meson", "anti-K*0", -313, 0.89600, 0, 2));
		// _lundIds.add(new LundId("Meson", "K*" + SUPERPLUS, 323,
		// 0.8916, 3, 2));
		// _lundIds.add(new LundId("Meson", "K*" + SUPERMINUS,
		// -323, 0.8916, -3, 2));
		// _lundIds.add(new LundId("Meson", "K_0*0", 10311, 1.412, 0, 0));
		// _lundIds.add(new LundId("Meson", "anti-K_0*0", -10311, 1.412, 0, 0));
		// _lundIds.add(new LundId("Meson", "K_0*+", 10321, 1.412, 3, 0));
		// _lundIds.add(new LundId("Meson", "K_0*-", -10321, 1.412, -3, 0));
		// _lundIds.add(new LundId("Meson", "K_10", 10313, 1.273, 0, 2));
		// _lundIds.add(new LundId("Meson", "anti-K_10", -10313, 1.273, 0, 2));
		// _lundIds.add(new LundId("Meson", "K_1+", 10323, 1.273, 3, 2));
		// _lundIds.add(new LundId("Meson", "K_1-", -10323, 1.273, -3, 2));
		// _lundIds.add(new LundId("Meson", "K_2*0", 315, 1.432, 0, 4));
		// _lundIds.add(new LundId("Meson", "anti-K_2*0", -315, 1.432, 0, 4));
		// _lundIds.add(new LundId("Meson", "K_2*+", 325, 1.425, 3, 4));
		// _lundIds.add(new LundId("Meson", "K_2*-", -325, 1.425, -3, 4));
		// _lundIds.add(new LundId("Meson", "K'_10", 20313, 1.402, 0, 2));
		// _lundIds.add(new LundId("Meson", "anti-K'_10", -20313, 1.402, 0, 2));
		// _lundIds.add(new LundId("Meson", "K'_1+", 20323, 1.402, 3, 2));
		// _lundIds.add(new LundId("Meson", "K'_1-", -20323, 1.402, -3, 2));
		// _lundIds.add(new LundId("Meson", "K'*0", 100313, 1.414, 0, 2));
		// _lundIds.add(new LundId("Meson", "anti-K'*0", -100313, 1.414, 0, 2));
		// _lundIds.add(new LundId("Meson", "K'*+", 100323, 1.414, 3, 2));
		// _lundIds.add(new LundId("Meson", "K'*-", -100323, 1.414, -3, 2));
		// _lundIds.add(new LundId("Meson", "K''*0", 30313, 1.717, 0, 2));
		// _lundIds.add(new LundId("Meson", "anti-K''*0", -30313, 1.717, 0, 2));
		// _lundIds.add(new LundId("Meson", "K''*+", 30323, 1.717, 3, 2));
		// _lundIds.add(new LundId("Meson", "K''*-", -30323, 1.717, -3, 2));
		// _lundIds.add(new LundId("Meson", "K_3*0", 317, 1.776, 0, 6));
		// _lundIds.add(new LundId("Meson", "anti-K_3*0", -317, 1.776, 0, 6));
		// _lundIds.add(new LundId("Meson", "K_3*+", 327, 1.776, 3, 6));
		// _lundIds.add(new LundId("Meson", "K_3*-", -327, 1.776, -3, 6));
		// _lundIds.add(new LundId("Meson", "K_4*0", 319, 2.045, 0, 8));
		// _lundIds.add(new LundId("Meson", "anti-K_4*0", -319, 2.045, 0, 8));
		// _lundIds.add(new LundId("Meson", "K_4*+", 329, 2.045, 3, 8));
		// _lundIds.add(new LundId("Meson", "K_4*-", -329, 2.045, -3, 8));
		// _lundIds.add(new LundId("Meson", "D+", 411, 1.86962, 3, 0));
		// _lundIds.add(new LundId("Meson", "D-", -411, 1.86962, -3, 0));
		// _lundIds.add(new LundId("Meson", "D0", 421, 1.86484, 0, 0));
		// _lundIds.add(new LundId("Meson", "anti-D0", -421, 1.86484, 0, 0));
		// _lundIds.add(new LundId("Meson", "D*+", 413, 2.01027, 3, 2));
		// _lundIds.add(new LundId("Meson", "D*-", -413, 2.01027, -3, 2));
		// _lundIds.add(new LundId("Meson", "D*0", 423, 2.00697, 0, 2));
		// _lundIds.add(new LundId("Meson", "anti-D*0", -423, 2.00697, 0, 2));
		// _lundIds.add(new LundId("Meson", "D_0*+", 10411, 2.308, 3, 0));
		// _lundIds.add(new LundId("Meson", "D_0*-", -10411, 2.308, -3, 0));
		// _lundIds.add(new LundId("Meson", "D_0*0", 10421, 2.308, 0, 0));
		// _lundIds.add(new LundId("Meson", "anti-D_0*0", -10421, 2.308, 0, 0));
		// _lundIds.add(new LundId("Meson", "D_1+", 10413, 2.427, 3, 2));
		// _lundIds.add(new LundId("Meson", "D_1-", -10413, 2.427, -3, 2));
		// _lundIds.add(new LundId("Meson", "D_10", 10423, 2.4223, 0, 2));
		// _lundIds.add(new LundId("Meson", "anti-D_10", -10423, 2.4223, 0, 2));
		// _lundIds.add(new LundId("Meson", "D_2*+", 415, 2.4601, 3, 4));
		// _lundIds.add(new LundId("Meson", "D_2*-", -415, 2.4601, -3, 4));
		// _lundIds.add(new LundId("Meson", "D_2*0", 425, 2.4611, 0, 4));
		// _lundIds.add(new LundId("Meson", "anti-D_2*0", -425, 2.4611, 0, 4));
		// _lundIds.add(new LundId("Meson", "D'_1+", 20413, 2.461, 3, 2));
		// _lundIds.add(new LundId("Meson", "D'_1-", -20413, 2.461, -3, 2));
		// _lundIds.add(new LundId("Meson", "D'_10", 20423, 2.461, 0, 2));
		// _lundIds.add(new LundId("Meson", "anti-D'_10", -20423, 2.461, 0, 2));
		// _lundIds.add(new LundId("Meson", "D_s+", 431, 1.96849, 3, 0));
		// _lundIds.add(new LundId("Meson", "D_s-", -431, 1.96849, -3, 0));
		// _lundIds.add(new LundId("Meson", "D_s*+", 433, 2.1123, 3, 2));
		// _lundIds.add(new LundId("Meson", "D_s*-", -433, 2.1123, -3, 2));
		// _lundIds.add(new LundId("Meson", "D_s0*+", 10431, 2.3178, 3, 0));
		// _lundIds.add(new LundId("Meson", "D_s0*-", -10431, 2.3178, -3, 0));
		// _lundIds.add(new LundId("Meson", "D_s1+", 10433, 2.4596, 3, 2));
		// _lundIds.add(new LundId("Meson", "D_s1-", -10433, 2.4596, -3, 2));
		// _lundIds.add(new LundId("Meson", "D_s2*+", 435, 2.5735, 3, 4));
		// _lundIds.add(new LundId("Meson", "D_s2*-", -435, 2.5735, -3, 4));
		// _lundIds.add(new LundId("Meson", "D'_s1+", 20433, 2.53535, 3, 2));
		// _lundIds.add(new LundId("Meson", "D'_s1-", -20433, 2.53535, -3, 2));
		// _lundIds.add(new LundId("Meson", "D(2S)+", 30411, 2.58, 3, 0));
		// _lundIds.add(new LundId("Meson", "D(2S)-", -30411, 2.58, -3, 0));
		// _lundIds.add(new LundId("Meson", "D(2S)0", 30421, 2.58, 0, 0));
		// _lundIds.add(new LundId("Meson", "anti-D(2S)0", -30421, 2.58, 0, 0));
		// _lundIds.add(new LundId("Meson", "D*(2S)+", 30413, 2.64, 3, 2));
		// _lundIds.add(new LundId("Meson", "D*(2S)-", -30413, 2.64, -3, 2));
		// _lundIds.add(new LundId("Meson", "D*(2S)0", 30423, 2.64, 0, 2));
		// _lundIds.add(new LundId("Meson", "anti-D*(2S)0", -30423, 2.64, 0,
		// 2));
		// _lundIds.add(new LundId("Meson", "B0", 511, 5.27953, 0, 0));
		// _lundIds.add(new LundId("Meson", "anti-B0", -511, 5.27953, 0, 0));
		// _lundIds.add(new LundId("Meson", "B0L", 150, 5.27953, 0, 0));
		// _lundIds.add(new LundId("Meson", "B0H", 510, 5.27953, 0, 0));
		// _lundIds.add(new LundId("Meson", "B+", 521, 5.27915, 3, 0));
		// _lundIds.add(new LundId("Meson", "B-", -521, 5.27915, -3, 0));
		// _lundIds.add(new LundId("Meson", "B*0", 513, 5.3248, 0, 2));
		// _lundIds.add(new LundId("Meson", "anti-B*0", -513, 5.3248, 0, 2));
		// _lundIds.add(new LundId("Meson", "B*+", 523, 5.3246, 3, 2));
		// _lundIds.add(new LundId("Meson", "B*-", -523, 5.3246, -3, 2));
		// _lundIds.add(new LundId("Meson", "B_0*0", 10511, 5.697, 0, 0));
		// _lundIds.add(new LundId("Meson", "anti-B_0*0", -10511, 5.697, 0, 0));
		// _lundIds.add(new LundId("Meson", "B_0*+", 10521, 5.697, 3, 0));
		// _lundIds.add(new LundId("Meson", "B_0*-", -10521, 5.697, -3, 0));
		// _lundIds.add(new LundId("Meson", "B_10", 10513, 5.679, 0, 2));
		// _lundIds.add(new LundId("Meson", "anti-B_10", -10513, 5.679, 0, 2));
		// _lundIds.add(new LundId("Meson", "B_1+", 10523, 5.679, 3, 2));
		// _lundIds.add(new LundId("Meson", "B_1-", -10523, 5.679, -3, 2));
		// _lundIds.add(new LundId("Meson", "B_2*0", 515, 5.692, 0, 4));
		// _lundIds.add(new LundId("Meson", "anti-B_2*0", -515, 5.692, 0, 4));
		// _lundIds.add(new LundId("Meson", "B_2*+", 525, 5.692, 3, 4));
		// _lundIds.add(new LundId("Meson", "B_2*-", -525, 5.692, -3, 4));
		// _lundIds.add(new LundId("Meson", "B'_10", 20513, 5.740, 0, 2));
		// _lundIds.add(new LundId("Meson", "anti-B'_10", -20513, 5.740, 0, 2));
		// _lundIds.add(new LundId("Meson", "B'_1+", 20523, 5.740, 3, 2));
		// _lundIds.add(new LundId("Meson", "B'_1-", -20523, 5.740, -3, 2));
		// _lundIds.add(new LundId("Meson", "B_s0", 531, 5.3663, 0, 0));
		// _lundIds.add(new LundId("Meson", "anti-B_s0", -531, 5.3663, 0, 0));
		// _lundIds.add(new LundId("Meson", "B_s0L", 350, 5.3663, 0, 0));
		// _lundIds.add(new LundId("Meson", "B_s0H", 530, 5.3663, 0, 0));
		// _lundIds.add(new LundId("Meson", "B_s*0", 533, 5.4128, 0, 2));
		// _lundIds.add(new LundId("Meson", "anti-B_s*0", -533, 5.4128, 0, 2));
		// _lundIds.add(new LundId("Meson", "B_s0*0", 10531, 5.766, 0, 0));
		// _lundIds.add(new LundId("Meson", "anti-B_s0*0", -10531, 5.766, 0,
		// 0));
		// _lundIds.add(new LundId("Meson", "B_s0*0", 10531, 5.8, 0, 0));
		// _lundIds.add(new LundId("Meson", "anti-B_s0*0", -10531, 5.8, 0, 0));
		// _lundIds.add(new LundId("Meson", "B_s10", 10533, 5.8294, 0, 2));
		// _lundIds.add(new LundId("Meson", "anti-B_s10", -10533, 5.8294, 0,
		// 2));
		// _lundIds.add(new LundId("Meson", "B_s2*0", 535, 5.853, 0, 4));
		// _lundIds.add(new LundId("Meson", "anti-B_s2*0", -535, 5.853, 0, 4));
		// _lundIds.add(new LundId("Meson", "B'_s10", 20533, 5.840, 0, 2));
		// _lundIds.add(new LundId("Meson", "anti-B'_s10", -20533, 5.840, 0,
		// 2));
		// _lundIds.add(new LundId("Meson", "B_c+", 541, 6.276, 3, 0));
		// _lundIds.add(new LundId("Meson", "B_c-", -541, 6.276, -3, 0));
		// _lundIds.add(new LundId("Meson", "B_c*+", 543, 6.602, 3, 2));
		// _lundIds.add(new LundId("Meson", "B_c*-", -543, 6.602, -3, 2));
		// _lundIds.add(new LundId("Meson", "B_c0*+", 10541, 7.25, 3, 0));
		// _lundIds.add(new LundId("Meson", "B_c0*-", -10541, 7.25, -3, 0));
		// _lundIds.add(new LundId("Meson", "B_c1+", 10543, 7.30, 3, 2));
		// _lundIds.add(new LundId("Meson", "B_c1-", -10543, 7.30, -3, 2));
		// _lundIds.add(new LundId("Meson", "B_c2*+", 545, 7.350, 3, 4));
		// _lundIds.add(new LundId("Meson", "B_c2*-", -545, 7.350, -3, 4));
		// _lundIds.add(new LundId("Meson", "B'_c1+", 20543, 7.40, 3, 2));
		// _lundIds.add(new LundId("Meson", "B'_c1-", -20543, 7.40, -3, 2));
		// _lundIds.add(new LundId("Meson", "eta_c", 441, 2.9803, 0, 0));
		// _lundIds.add(new LundId("Meson", "eta_c(2S)", 20441, 3.637, 0, 0));
		// _lundIds.add(new LundId("Meson", "J/psi", 443, 3.096916, 0, 2));
		// _lundIds.add(new LundId("Meson", "psi(2S)", 30443, 3.68609, 0, 2));
		// _lundIds.add(new LundId("Meson", "psi(3770)", 40443, 3.77292, 0, 2));
		// _lundIds.add(new LundId("Meson", "psi(4040)", 50443, 4.040, 0, 2));
		// _lundIds.add(new LundId("Meson", "psi(4160)", 60443, 4.153, 0, 2));
		// _lundIds.add(new LundId("Meson", "psi(4415)", 70443, 4.421, 0, 2));
		// _lundIds.add(new LundId("Meson", "h_c", 10443, 3.52614, 0, 2));
		// _lundIds.add(new LundId("Meson", "chi_c0", 10441, 3.41475, 0, 0));
		// _lundIds.add(new LundId("Meson", "chi_c1", 20443, 3.51066, 0, 2));
		// _lundIds.add(new LundId("Meson", "chi_c2", 445, 3.55620, 0, 4));
		// _lundIds.add(new LundId("Meson", "eta_b", 551, 9.403, 0, 0));
		// _lundIds.add(new LundId("Meson", "eta_b(2S)", 20551, 9.997, 0, 0));
		// _lundIds.add(new LundId("Meson", "eta_b(3S)", 40551, 10.335, 0, 0));
		// _lundIds.add(new LundId("Meson", "Upsilon", 553, 9.46030, 0, 2));
		// _lundIds.add(new LundId("Meson", "Upsilon(2S)", 100553, 10.02326, 0,
		// 2));
		// _lundIds.add(new LundId("Meson", "Upsilon(3S)", 60553, 10.3552, 0,
		// 2));
		// _lundIds.add(new LundId("Meson", "Upsilon(4S)", 70553, 10.5794, 0,
		// 2));
		// _lundIds.add(new LundId("Meson", "Upsilon(5S)", 80553, 10.865, 0,
		// 2));
		// _lundIds.add(new LundId("Meson", "h_b", 10553, 9.875, 0, 2));
		// _lundIds.add(new LundId("Meson", "h_b(2P)", 110553, 10.25500, 0, 2));
		// _lundIds.add(new LundId("Meson", "h_b(3P)", 210553, 10.51600, 0, 2));
		// _lundIds.add(new LundId("Meson", "chi_b0", 10551, 9.85944, 0, 0));
		// _lundIds.add(new LundId("Meson", "chi_b1", 20553, 9.89278, 0, 2));
		// _lundIds.add(new LundId("Meson", "chi_b2", 555, 9.91221, 0, 4));
		// _lundIds.add(new LundId("Meson", "chi_b0(2P)", 30551, 10.2325, 0,
		// 0));
		// _lundIds.add(new LundId("Meson", "chi_b1(2P)", 50553, 10.25546, 0,
		// 2));
		// _lundIds.add(new LundId("Meson", "chi_b2(2P)", 10555, 10.26865, 0,
		// 4));
		// _lundIds.add(new LundId("Meson", "chi_b0(3P)", 50551, 10.50070, 0,
		// 0));
		// _lundIds.add(new LundId("Meson", "chi_b1(3P)", 220553, 10.51600, 0,
		// 2));
		// _lundIds.add(new LundId("Meson", "chi_b2(3P)", 200555, 10.52640, 0,
		// 4));
		// _lundIds.add(new LundId("Meson", "eta_b2(1D)", 40555, 10.157, 0, 2));
		// _lundIds.add(new LundId("Meson", "eta_b2(2D)", 60555, 10.441, 0, 2));
		// _lundIds.add(new LundId("Meson", "Upsilon_1(1D)", 30553, 10.15010, 0,
		// 2));
		// _lundIds.add(new LundId("Meson", "Upsilon_2(1D)", 20555, 10.15620, 0,
		// 4));
		// _lundIds.add(new LundId("Meson", "Upsilon_3(1D)", 557, 10.15990, 0,
		// 6));
		// _lundIds.add(new LundId("Meson", "Upsilon_1(2D)", 130553, 10.43490,
		// 0, 2));
		// _lundIds.add(new LundId("Meson", "Upsilon_2(2D)", 120555, 10.44060,
		// 0, 4));
		// _lundIds.add(new LundId("Meson", "Upsilon_3(2D)", 100557, 10.44430,
		// 0, 6));
		// _lundIds.add(new LundId("Meson", "sigma_0", 10222, 0.478, 0, 0));
		_lundIds.add(new LundId("Baryon", BIG_DELTA + SUPERMINUS, 1114, 1.234, -3, 3));
		// _lundIds.add(new LundId("Baryon", "anti-Delta+", -1114, 1.234, 3,
		// 3));
		// _lundIds.add(new LundId("Baryon", "anti-n0", -2112, 0.93956536, 0,
		// 1));
		 _lundIds.add(new LundId("Baryon", BIG_DELTA + SUPERZERO, 2114, 1.233, 0, 3));
		// _lundIds.add(new LundId("Baryon", "anti-Delta0", -2114, 1.233, 0,
		// 3));
		_lundIds.add(new LundId("Baryon", "p" + OVERLINE, -2212, 0.93827203, -3, 1));
		_lundIds.add(new LundId("Baryon", BIG_DELTA + SUPERPLUS, 2214, 1.232, 3, 3));
		// _lundIds.add(new LundId("Baryon", "anti-Delta-", -2214, 1.232, -3,
		// 3));
		_lundIds.add(new LundId("Baryon", BIG_DELTA + SUPERPLUS + SUPERPLUS, 2224, 1.231, 6, 3));
		// _lundIds.add(new LundId("Baryon", "anti-Delta--", -2224, 1.231, -6,
		// 3));
		_lundIds.add(new LundId("Baryon", BIG_LAMBDA + SUBZERO, 3122, 1.115683, 0, 1));
		// _lundIds
		// .add(new LundId("Baryon", "anti-Lambda0", -3122, 1.115683, 0, 1));
		// _lundIds.add(new LundId("Baryon", "Lambda(1405)0", 13122, 1.406, 0,
		// 1));
		// _lundIds.add(new LundId("Baryon", "anti-Lambda(1405)0", -13122,
		// 1.406,
		// 0, 1));
		// _lundIds.add(new LundId("Baryon", "Lambda(1520)0", 3124, 1.5195, 0,
		// 3));
		// _lundIds.add(new LundId("Baryon", "anti-Lambda(1520)0", -3124,
		// 1.5195,
		// 0, 3));
		// _lundIds.add(new LundId("Baryon", "Lambda(1600)0", 23122, 1.6, 0,
		// 1));
		// _lundIds.add(new LundId("Baryon", "anti-Lambda(1600)0", -23122, 1.6,
		// 0,
		// 1));
		// _lundIds.add(new LundId("Baryon", "Lambda(1670)0", 33122, 1.67, 0,
		// 1));
		// _lundIds.add(new LundId("Baryon", "anti-Lambda(1670)0", -33122, 1.67,
		// 0, 1));
		// _lundIds.add(new LundId("Baryon", "Lambda(1690)0", 13124, 1.69, 0,
		// 3));
		// _lundIds.add(new LundId("Baryon", "anti-Lambda(1690)0", -13124, 1.69,
		// 0, 3));
		// _lundIds.add(new LundId("Baryon", "Lambda(1800)0", 43122, 1.8, 0,
		// 1));
		// _lundIds.add(new LundId("Baryon", "anti-Lambda(1800)0", -43122, 1.8,
		// 0,
		// 1));
		// _lundIds.add(new LundId("Baryon", "Lambda(1810)0", 53122, 1.81, 0,
		// 1));
		// _lundIds.add(new LundId("Baryon", "anti-Lambda(1810)0", -53122, 1.81,
		// 0, 1));
		// _lundIds.add(new LundId("Baryon", "Lambda(1820)0", 3126, 1.82, 0,
		// 5));
		// _lundIds.add(new LundId("Baryon", "anti-Lambda(1820)0", -3126, 1.82,
		// 0,
		// 5));
		// _lundIds.add(new LundId("Baryon", "Lambda(1830)0", 13126, 1.83, 0,
		// 5));
		// _lundIds.add(new LundId("Baryon", "anti-Lambda(1830)0", -13126, 1.83,
		// 0, 5));
		// _lundIds.add(new LundId("Baryon", "Sigma(1660)0", 13212, 1.66, 0,
		// 1));
		// _lundIds.add(new LundId("Baryon", "anti-Sigma(1660)0", -13212, 1.66,
		// 0,
		// 1));
		// _lundIds.add(new LundId("Baryon", "Sigma(1670)0", 13214, 1.67, 0,
		// 3));
		// _lundIds.add(new LundId("Baryon", "anti-Sigma(1670)0", -13214, 1.67,
		// 0,
		// 3));
		// _lundIds.add(new LundId("Baryon", "Sigma(1750)0", 23212, 1.75, 0,
		// 1));
		// _lundIds.add(new LundId("Baryon", "anti-Sigma(1750)0", -23212, 1.75,
		// 0,
		// 1));
		// _lundIds.add(new LundId("Baryon", "Sigma(1775)0", 3216, 1.775, 0,
		// 5));
		// _lundIds.add(new LundId("Baryon", "anti-Sigma(1775)0", -3216, 1.775,
		// 0,
		// 5));
		 _lundIds.add(new LundId("Baryon", BIG_SIGMA + SUPERMINUS, 3112, 1.197449, -3, 1));
		// _lundIds
		// .add(new LundId("Baryon", "anti-Sigma+", -3112, 1.197449, 3, 1));
		 _lundIds.add(new LundId("Baryon", BIG_SIGMA + "*" + SUPERMINUS, 3114, 1.3872, -3, 3));
		// _lundIds.add(new LundId("Baryon", "anti-Sigma*+", -3114, 1.3872, 3,
		// 3));
		 _lundIds.add(new LundId("Baryon", BIG_SIGMA + SUPERZERO, 3212, 1.192642, 0, 1));
		// _lundIds
		// .add(new LundId("Baryon", "anti-Sigma0", -3212, 1.192642, 0, 1));
		 _lundIds.add(new LundId("Baryon",  BIG_SIGMA + "*" + SUPERZERO, 3214, 1.3837, 0, 3));
		// _lundIds.add(new LundId("Baryon", "anti-Sigma*0", -3214, 1.3837, 0,
		// 3));
		 _lundIds.add(new LundId("Baryon", BIG_SIGMA + SUPERPLUS, 3222, 1.18937, 3, 1));
		// _lundIds
		// .add(new LundId("Baryon", "anti-Sigma-", -3222, 1.18937, -3, 1));
		 _lundIds.add(new LundId("Baryon",  BIG_SIGMA + "*" + SUPERPLUS, 3224, 1.3828, 3, 3));
		// _lundIds
		// .add(new LundId("Baryon", "anti-Sigma*-", -3224, 1.3828, -3, 3));
		// _lundIds.add(new LundId("Baryon", "Xi-", 3312, 1.32171, -3, 1));
		// _lundIds.add(new LundId("Baryon", "anti-Xi+", -3312, 1.32171, 3, 1));
		// _lundIds.add(new LundId("Baryon", "Xi*-", 3314, 1.5350, -3, 3));
		// _lundIds.add(new LundId("Baryon", "anti-Xi*+", -3314, 1.5350, 3, 3));
		// _lundIds.add(new LundId("Baryon", "Xi0", 3322, 1.31483, 0, 1));
		// _lundIds.add(new LundId("Baryon", "anti-Xi0", -3322, 1.31483, 0, 1));
		// _lundIds.add(new LundId("Baryon", "Xi*0", 3324, 1.5318, 0, 3));
		// _lundIds.add(new LundId("Baryon", "anti-Xi*0", -3324, 1.5318, 0, 3));
		// _lundIds.add(new LundId("Baryon", "Omega-", 3334, 1.67245, -3, 3));
		// _lundIds.add(new LundId("Baryon", "anti-Omega+", -3334, 1.67245, 3,
		// 3));
		// _lundIds.add(new LundId("Baryon", "Lambda_c+", 4122, 2.28646, 3, 1));
		// _lundIds.add(new LundId("Baryon", "anti-Lambda_c-", -4122, 2.28646,
		// -3,
		// 1));
		// _lundIds.add(new LundId("Baryon", "Lambda_c(2593)+", 14122, 2.5954,
		// 3,
		// 1));
		// _lundIds.add(new LundId("Baryon", "anti-Lambda_c(2593)-", -14122,
		// 2.5954, -3, 1));
		// _lundIds.add(new LundId("Baryon", "Lambda_c(2625)+", 14124, 2.6281,
		// 3,
		// 3));
		// _lundIds.add(new LundId("Baryon", "anti-Lambda_c(2625)-", -14124,
		// 2.6281, -3, 3));
		// _lundIds.add(new LundId("Baryon", "Sigma_c0", 4112, 2.45376, 0, 1));
		// _lundIds
		// .add(new LundId("Baryon", "anti-Sigma_c0", -4112, 2.45376, 0, 1));
		// _lundIds.add(new LundId("Baryon", "Sigma_c+", 4212, 2.4529, 3, 1));
		// _lundIds
		// .add(new LundId("Baryon", "anti-Sigma_c-", -4212, 2.4529, -3, 1));
		// _lundIds.add(new LundId("Baryon", "Sigma_c++", 4222, 2.45402, 6, 1));
		// _lundIds.add(new LundId("Baryon", "anti-Sigma_c--", -4222, 2.45402,
		// -6,
		// 1));
		// _lundIds.add(new LundId("Baryon", "Sigma_c*0", 4114, 2.5180, 0, 3));
		// _lundIds
		// .add(new LundId("Baryon", "anti-Sigma_c*0", -4114, 2.5180, 0, 3));
		// _lundIds.add(new LundId("Baryon", "Sigma_c*+", 4214, 2.5175, 3, 3));
		// _lundIds.add(new LundId("Baryon", "anti-Sigma_c*-", -4214, 2.5175,
		// -3,
		// 3));
		// _lundIds.add(new LundId("Baryon", "Sigma_c*++", 4224, 2.5184, 6, 3));
		// _lundIds.add(new LundId("Baryon", "anti-Sigma_c*--", -4224, 2.5184,
		// -6,
		// 3));
		// _lundIds.add(new LundId("Baryon", "Xi_c0", 4132, 2.4710, 0, 1));
		// _lundIds.add(new LundId("Baryon", "anti-Xi_c0", -4132, 2.4710, 0,
		// 1));
		// _lundIds.add(new LundId("Baryon", "Xi_c+", 4232, 2.4679, 3, 1));
		// _lundIds.add(new LundId("Baryon", "anti-Xi_c-", -4232, 2.4679, -3,
		// 1));
		// _lundIds.add(new LundId("Baryon", "Xi'_c0", 4312, 2.5780, 0, 1));
		// _lundIds.add(new LundId("Baryon", "anti-Xi'_c0", -4312, 2.5780, 0,
		// 1));
		// _lundIds.add(new LundId("Baryon", "Xi'_c+", 4322, 2.5757, 3, 1));
		// _lundIds.add(new LundId("Baryon", "anti-Xi'_c-", -4322, 2.5757, -3,
		// 1));
		// _lundIds.add(new LundId("Baryon", "Xi_c*0", 4314, 2.6461, 0, 3));
		// _lundIds.add(new LundId("Baryon", "anti-Xi_c*0", -4314, 2.6461, 0,
		// 3));
		// _lundIds.add(new LundId("Baryon", "Xi_c*+", 4324, 2.6466, 3, 3));
		// _lundIds.add(new LundId("Baryon", "anti-Xi_c*-", -4324, 2.6466, -3,
		// 3));
		// _lundIds.add(new LundId("Baryon", "Omega_c0", 4332, 2.6975, 0, 1));
		// _lundIds
		// .add(new LundId("Baryon", "anti-Omega_c0", -4332, 2.6975, 0, 1));
		// _lundIds.add(new LundId("Baryon", "Omega_c*0", 4334, 2.8, 0, 3));
		// _lundIds.add(new LundId("Baryon", "anti-Omega_c*0", -4334, 2.8, 0,
		// 3));
		// _lundIds.add(new LundId("Baryon", "Xi_cc+", 4412, 3.59798, 3, 1));
		// _lundIds
		// .add(new LundId("Baryon", "anti-Xi_cc-", -4412, 3.59798, -3, 1));
		// _lundIds.add(new LundId("Baryon", "Xi_cc*+", 4414, 3.65648, 3, 3));
		// _lundIds
		// .add(new LundId("Baryon", "anti-Xi_cc*-", -4414, 3.65648, -3, 3));
		// _lundIds.add(new LundId("Baryon", "Xi_cc++", 4422, 3.59798, 6, 1));
		// _lundIds
		// .add(new LundId("Baryon", "anti-Xi_cc--", -4422, 3.59798, -6, 1));
		// _lundIds.add(new LundId("Baryon", "Xi_cc*++", 4424, 3.65648, 6, 3));
		// _lundIds.add(new LundId("Baryon", "anti-Xi_cc*--", -4424, 3.65648,
		// -6,
		// 3));
		// _lundIds.add(new LundId("Baryon", "Omega_cc+", 4432, 3.78663, 3, 1));
		// _lundIds.add(new LundId("Baryon", "anti-Omega_cc-", -4432, 3.78663,
		// -3,
		// 1));
		// _lundIds.add(new LundId("Baryon", "Omega_cc*+", 4434, 3.82466, 3,
		// 3));
		// _lundIds.add(new LundId("Baryon", "anti-Omega_cc*-", -4434, 3.82466,
		// -3, 3));
		// _lundIds.add(new LundId("Baryon", "Omega_ccc*++", 4444, 4.91594, 6,
		// 3));
		// _lundIds.add(new LundId("Baryon", "anti-Omega_ccc*--", -4444,
		// 4.91594,
		// -6, 3));
		// _lundIds.add(new LundId("Baryon", "Sigma_b-", 5112, 5.8152, -3, 1));
		// _lundIds
		// .add(new LundId("Baryon", "anti-Sigma_b+", -5112, 5.8152, 3, 1));
		// _lundIds.add(new LundId("Baryon", "Sigma_b*-", 5114, 5.8364, -3, 3));
		// _lundIds
		// .add(new LundId("Baryon", "anti-Sigma_b*+", -5114, 5.8364, 3, 3));
		// _lundIds.add(new LundId("Baryon", "Lambda_b0", 5122, 5.6197, 0, 1));
		// _lundIds
		// .add(new LundId("Baryon", "anti-Lambda_b0", -5122, 5.6197, 0, 1));
		// _lundIds.add(new LundId("Baryon", "Xi_b-", 5132, 5.7924, -3, 1));
		// _lundIds.add(new LundId("Baryon", "anti-Xi_b+", -5132, 5.7924, 3,
		// 1));
		// _lundIds.add(new LundId("Baryon", "Sigma_b0", 5212, 5.81, 0, 1));
		// _lundIds.add(new LundId("Baryon", "anti-Sigma_b0", -5212, 5.81, 0,
		// 1));
		// _lundIds.add(new LundId("Baryon", "Sigma_b*0", 5214, 5.83, 0, 3));
		// _lundIds.add(new LundId("Baryon", "anti-Sigma_b*0", -5214, 5.83, 0,
		// 3));
		// _lundIds.add(new LundId("Baryon", "Sigma_b+", 5222, 5.8078, 3, 1));
		// _lundIds
		// .add(new LundId("Baryon", "anti-Sigma_b-", -5222, 5.8078, -3, 1));
		// _lundIds.add(new LundId("Baryon", "Sigma_b*+", 5224, 5.8290, 3, 3));
		// _lundIds.add(new LundId("Baryon", "anti-Sigma_b*-", -5224, 5.8290,
		// -3,
		// 3));
		// _lundIds.add(new LundId("Baryon", "Xi_b0", 5232, 5.84, 0, 1));
		// _lundIds.add(new LundId("Baryon", "anti-Xi_b0", -5232, 5.84, 0, 1));
		// _lundIds.add(new LundId("Baryon", "Xi'_b-", 5312, 5.96, -3, 1));
		// _lundIds.add(new LundId("Baryon", "anti-Xi'_b+", -5312, 5.96, 3, 1));
		// _lundIds.add(new LundId("Baryon", "Xi_b*-", 5314, 5.97, -3, 3));
		// _lundIds.add(new LundId("Baryon", "anti-Xi_b*+", -5314, 5.97, 3, 3));
		// _lundIds.add(new LundId("Baryon", "Xi'_b0", 5322, 5.96, 0, 1));
		// _lundIds.add(new LundId("Baryon", "anti-Xi'_b0", -5322, 5.96, 0, 1));
		// _lundIds.add(new LundId("Baryon", "Xi_b*0", 5324, 5.97, 0, 3));
		// _lundIds.add(new LundId("Baryon", "anti-Xi_b*0", -5324, 5.97, 0, 3));
		// _lundIds.add(new LundId("Baryon", "Omega_b-", 5332, 6.12, -3, 1));
		// _lundIds.add(new LundId("Baryon", "anti-Omega_b+", -5332, 6.12, 3,
		// 1));
		// _lundIds.add(new LundId("Baryon", "Omega_b*-", 5334, 6.13, -3, 3));
		// _lundIds.add(new LundId("Baryon", "anti-Omega_b*+", -5334, 6.13, 3,
		// 3));
		_lundIds.add(new LundId("Nucleus", "deuteron", 1011, 1.8756134, 3, 0));
		// _lundIds.add(new LundId("Nucleus", "anti-deuteron", -1011, 1.8756134,
		// -3, 0));
		_lundIds.add(new LundId("Nucleus", "t", 1021, 2.80925, 3, 1));
		// _lundIds.add(new LundId("Nucleus", "anti-tritium", -1021, 2.80925,
		// -3, 1));
		// _lundIds.add(new LundId("Nucleus", "He3", 1012, 2.80923, 6, 1));
		// _lundIds.add(new LundId("Nucleus", "anti-He3", -1012, 2.80923, -6,
		// 1));
		// _lundIds.add(new LundId("Nucleus", "alpha", 1022, 3.727417, 6, 0));
		// _lundIds.add(new LundId("Nucleus", "anti-alpha", -1022, 3.727417, -6,
		// 0));
	}

	/**
	 * Finds an LundId object based on the given particle id. Uses a binary search.
	 * 
	 * @param id the id to look for
	 * @return the object if found, or <code>null</code>
	 */
	public LundId get(int id, int charge) {
		if (_lundIds == null) {
			return null;
		}
		LundId testId = new LundId(null, null, id, 0, 0, 0);
		int index = Collections.binarySearch(_lundIds, testId);

		if (index >= 0) {
			return _lundIds.get(index);
		} else {
			if (charge == -1) {
				return unknownMinus;
			}
			if (charge == 1) {
				return unknownPlus;
			}
			return unknownNeutral;
		}
	}
	
	/**
	 * Finds an LundId object based on the given particle id. Uses a binary search.
	 * 
	 * @param id the id to look for
	 * @return the object if found, or <code>null</code>
	 */
	public LundId get(int id) {
		if (_lundIds == null) {
			return null;
		}
		LundId testId = new LundId(null, null, id, 0, 0, 0);
		int index = Collections.binarySearch(_lundIds, testId);

		if (index >= 0) {
			return _lundIds.get(index);
		} else {
			return null;
		}
	}


	/**
	 * Finds an LundId object based on the given particle id. Uses a binary search.
	 * 
	 * @param id the id to look for. This is rounded. This method is to support
	 *           GEMC.
	 * @return the object if found, or <code>null</code>
	 */
	public LundId get(double id) {
		int intId = (int) Math.round(id);
		return get(intId);
	}

	/**
	 * Get the list of lundIds
	 * 
	 * @return the list of lundIds
	 */
	public ArrayList<LundId> getLundIds() {
		return _lundIds;
	}

	/**
	 * Convert a geant ID onto a Lund (PDG) id.
	 * 
	 * @param geantId the geant Id
	 * @return the lund (pdg) Id.
	 */
	public static int geantToLund(int geantId) {
		switch (geantId) {
		case 1:
			return 22;
		case 2:
			return -11;
		case 3:
			return 11;
		case 4:
			return 12;
		case 5:
			return -13;
		case 6:
			return 13;
		case 7:
			return 111;
		case 8:
			return 211;
		case 9:
			return -211;
		case 10:
			return 130;
		case 11:
			return 321;
		case 12:
			return -321;
		case 13:
			return 2112; // neutron
		case 14:
			return 2212;
		case 15:
			return -2212;
		case 16:
			return 310;
		case 17:
			return 221;
		case 18:
			return 3122;
		case 19:
			return 3222;
		case 20:
			return 3212;
		case 21:
			return 3112;
		case 22:
			return 3322;
		case 23:
			return 3312;
		case 24:
			return 3332;
		case 25:
			return -2112;
		case 26:
			return -3122;
		case 27:
			return -3112;
		case 28:
			return -3212;
		case 29:
			return -3112;
		case 30:
			return -3322;
		case 31:
			return -3312;
		case 32:
			return -3332;
		case 33:
			return -15;
		case 34:
			return 15;
		case 35:
			return 411;
		case 36:
			return -411;
		case 37:
			return 421;
		case 38:
			return -421;
		case 39:
			return 431;
		case 40:
			return -431;
		case 41:
			return 4122;
		case 42:
			return 24;
		case 43:
			return -24;
		case 44:
			return 23;
		case 45:
			return 1011; // deuteron
		case 46:
			return 1021; // triton
		default:
			System.err.println("Unrecognized Geant particle ID: " + geantId);
			return -1;
		}
	}

	/**
	 * Returns the Lund ID of a particle given the mass and charge.
	 * 
	 * @param mass   the mass of the particle in GeV
	 * @param charge the charge of the particle in units of electron charge
	 * @return the Geant ID
	 */
	public static int massAndChargeToLundId(double mass, int charge) {
		int geantID = 0;

		if (mass < 1.0e-9 && charge == 0) {
			geantID = 1; // photon
		} else if (0.13 <= mass && mass < 0.145) { // pi+/- mass .139 GeV
			if (charge < 0) {
				geantID = 9;
			} else if (charge > 0) {
				geantID = 8;
			} else {
				geantID = 7;
			}
		} else if (0.103 < mass && mass < 0.107) { // muon +/- mass .105 GeV
			if (charge < 0) {
				geantID = 6;
			} else if (charge > 0) {
				geantID = 5;
			}
		} else if (0.397 < mass && mass < 0.597) { // K+/- mass .497 GeV
			if (charge < 0) {
				geantID = 12;
			} else {
				geantID = 11;
			}
		} else if (0.000411 < mass && mass < 0.000611) { // e+/- mass .000511
			// GeV
			if (charge < 0) {
				geantID = 3;
			} else if (charge > 0) {
				geantID = 2;
			}
		} else if (mass > .93 && mass < 0.95) { // p mass .935 GeV
			if (charge < 0) {
				geantID = 15; // anti proton
			} else if (charge > 0) {
				geantID = 14; // proton
			} else {
				geantID = 13; // neutron
			}
		}

		return geantToLund(geantID);
	}

	/**
	 * Used to initialize the styles for some jlab particles of interest.
	 */
	private void initStyles() {
		// Geatininos
		setStyle(0, Color.black);
		setStyle(-1, Color.white);
		setStyle(-2, Color.gray);

		// recon tracks
		setStyle(-99, darkOrange);
		setStyle(-100, darkOrange);
		setStyle(-101, darkOrange);
		setStyle(-199, Color.yellow);
		setStyle(-200, Color.yellow);
		setStyle(-201, Color.yellow);
		setStyle(-299, darkGreen);
		setStyle(-500, darkGreen);
		setStyle(-301, darkGreen);

		setStyle(11, Color.red); // e-
		setStyle(-11, Color.magenta); // e+
		setStyle(111, purple); // pi0
		setStyle(211, X11Colors.getX11Color("purple")); // pi+
		setStyle(-211, X11Colors.getX11Color("medium purple")); // pi-
		setStyle(321, goldenrod); // K+
		setStyle(-321, olive); // K-
		setStyle(213, X11Colors.getX11Color("Slate Gray")); // rho+
		setStyle(113, X11Colors.getX11Color("Slate Gray")); // rho0
		setStyle(-213, Color.orange); // rho-
		setStyle(13, Color.cyan); // mu-
		setStyle(-13, brown); // mu+
		setStyle(2212, Color.blue); // proton
		setStyle(2112, lawnGreen); // neutron
		setStyle(22, deepsky); // photon
		setStyle(1011, Color.darkGray); // deuteron
		setStyle(1021, Color.red); // triton
		setStyle(3122, Color.pink); // Lambda0
		setStyle(2224, X11Colors.getX11Color("brown")); // Delta++
		setStyle(1114, X11Colors.getX11Color("dark red")); // Delta-
		setStyle(2114, X11Colors.getX11Color("coral")); // Delta0
		setStyle(2214, X11Colors.getX11Color("coral")); // Delta+
		
		setStyle(3222, X11Colors.getX11Color("dark blue")); // Sigma+
		setStyle(3212, X11Colors.getX11Color("dark blue")); // Sigma0
		setStyle(3112, X11Colors.getX11Color("indigo")); // Sigma-
		setStyle(3224, X11Colors.getX11Color("dark magenta")); // Sigma*+
		setStyle(3214, X11Colors.getX11Color("dark magenta")); // Sigma*0
		setStyle(3114, X11Colors.getX11Color("lawn green")); // Sigma*-

		
		setStyle(221, Color.magenta); // eta
		setStyle(223, orangeRed); // omega


	}

	/**
	 * Set the style for a given lund Id
	 * 
	 * @param lundId    the id to set the style for.
	 * @param lineColor the lineColor to use.
	 * @param darker    if <code>true</code> make line color datker, else make it
	 *                  lighter.
	 */
	public static void setStyle(int lundId, Color lineColor) {
		LundSupport ls = LundSupport.getInstance();
		LundId lid = ls.get(lundId);
		if (lid != null) {
			LundStyle.addLundStyle(lid, lineColor);
		}
	}

	/**
	 * Main program for testing
	 * 
	 * @param arg command arguments ignored.
	 */
	public static void main(String arg[]) {
		System.out.println("\u00a5123");
		try {
			System.out.println(new String("\u207A".getBytes(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LundSupport lundSupport = getInstance();
		System.err.println(lundSupport.toString());
		System.err.println("-------------");

		testGet(lundSupport, 22);

		testGet(lundSupport, -11);
		testGet(lundSupport, 11);

		testGet(lundSupport, -11.01);
		testGet(lundSupport, -10.98);

		testGet(lundSupport, 11.01);
		testGet(lundSupport, 10.98);

		// test mass and charge to Lund Id
		System.err.println("\nTest mass, charge to Lund");
		testGet(lundSupport, 0, 0);
		testGet(lundSupport, 0.935, 1);

	}

	// used by main program for testing
	private static void testGet(LundSupport ls, int id) {
		LundId lid = ls.get(id);
		System.err.println("[" + id + "] " + lid);
	}

	// used by main program for testing
	private static void testGet(LundSupport ls, double id) {
		LundId lid = ls.get(id);
		System.err.println("(" + id + ") " + lid);
	}

	private static void testGet(LundSupport ls, double mass, int charge) {
		int id = massAndChargeToLundId(mass, charge);
		LundId lid = ls.get(id);
		System.err.println("(" + mass + ", " + charge + ") " + lid);
	}

}
