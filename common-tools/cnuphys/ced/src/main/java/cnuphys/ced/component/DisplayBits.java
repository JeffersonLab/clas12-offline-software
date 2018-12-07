package cnuphys.ced.component;

public class DisplayBits {

	/** A bit for a "Monte Carlo Truth" checkbox */
	public static final int MCTRUTH = 01;

	/** A flag for accumulation */
	public static final int ACCUMULATION = 02;
	
	/** reconstructed segments */
	public static final int SEGMENTS = 04;

	/** A bit for uvw strips */
	public static final int UVWSTRIPS = 010;

	/** A bit for inner/outer selection for ec */
	public static final int INNEROUTER = 020;
	
	/** dc  reconstructed hits */
	public static final int DC_HITS = 040;

	/** reconstructed crosses */
	public static final int CROSSES = 0100;
	
	/** reconstructed clusters */
	public static final int CLUSTERS = 0200;

	/** mag field */
	public static final int MAGFIELD = 0400;

	/** hits Recons */
	public static final int RECONHITS = 01000;

	/** Cosmic tracks */
	public static final int COSMICS = 02000;
	
	/** global display of hb data */
	public static final int GLOBAL_HB = 04000;
	
	/** global display of hb data */
	public static final int GLOBAL_TB = 010000;
	
	/** adc hits */
	public static final int ADC_HITS = 020000;
	
	/** cvt recon tracks */
	public static final int CVTTRACKS = 040000;
	
	/** reconstructed clusters */
	public static final int FMTCROSSES = 0100000;

	
	
	/** Distance scale */
	//public static final int SCALE = 04;



	// max octal for ints 20000000000

}
