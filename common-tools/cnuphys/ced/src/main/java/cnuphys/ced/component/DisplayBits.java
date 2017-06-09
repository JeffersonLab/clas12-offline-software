package cnuphys.ced.component;

public class DisplayBits {

	/** A bit for a "Monte Carlo Truth" checkbox */
	public static final int MCTRUTH = 01;

	/** A flag for accumulation */
	public static final int ACCUMULATION = 02;

	/** Distance scale */
	public static final int SCALE = 04;

	/** A bit for uvw strips */
	public static final int UVWSTRIPS = 010;

	/** A bit for inner/outer selection for ec */
	public static final int INNEROUTER = 020;

	/** dc hit based reconstructed crosses */
	public static final int DC_HB_RECONS_CROSSES = 0100;

	/** bst reconstructed crosses */
	public static final int SVTRECONS_CROSSES = 0200;

	/** dc hit based reconstructed crosses */
	public static final int DC_TB_RECONS_CROSSES = 01000;

	/** mag field */
	public static final int MAGFIELD = 02000;

	/** hits FTOF Recons */
	public static final int FTOFHITS = 010000;

	/** Cosmic tracks */
	public static final int COSMICS = 020000;
	
	/** dc hit based reconstructed doca */
	public static final int DC_TB_RECONS_DOCA = 040000;

	/** dc hit based reconstructed doca */
	public static final int DC_HB_RECONS_SEGMENTS = 0100000;
	
	/** dc hit based reconstructed doca */
	public static final int DC_TB_RECONS_SEGMENTS = 0200000;

	/** global display of hb data */
	public static final int GLOBAL_HB = 0400000;
	
	/** global display of hb data */
	public static final int GLOBAL_TB = 01000000;


	// max octal for ints 20000000000

}
