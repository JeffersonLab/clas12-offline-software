package cnuphys.ced.event.data;

import java.util.List;

import cnuphys.bCNU.log.Log;

/**
 * static methods to centralize getting data arrays
 * 
 * @author heddle
 *
 */
public class FTOF {

	// ftof constants
	public static final int PANEL_1A = 0;
	public static final int PANEL_1B = 1;
	public static final int PANEL_2 = 2;
	public static final String panelNames[] = { "Panel 1A", "Panel 1B",
			"Panel 2" };

	/**
	 * Get the pid array from the true data
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the pid array
	 */
	public static int[] pid(int panelType) {

		int pid[] = null;
		switch (panelType) {
		case PANEL_1A:
			pid = ColumnData.getIntArray("FTOF1A::true.pid");
			break;

		case PANEL_1B:
			pid = ColumnData.getIntArray("FTOF1B::true.pid");
			break;

		case PANEL_2:
			pid = ColumnData.getIntArray("FTOF2B::true.pid");
			break;
		}
		return pid;
	}

	/**
	 * Get the sector array from the dgtz data
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the sector array
	 */
	public static int[] sector(int panelType) {

		int sector[] = null;
		switch (panelType) {
		case PANEL_1A:
			sector = ColumnData.getIntArray("FTOF1A::dgtz.sector");
			break;

		case PANEL_1B:
			sector = ColumnData.getIntArray("FTOF1B::dgtz.sector");
			break;

		case PANEL_2:
			sector = ColumnData.getIntArray("FTOF2B::dgtz.sector");
			break;
		}
		return sector;
	}

	/**
	 * Get the paddle array from the dgtz data
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the paddle array
	 */
	public static int[] paddle(int panelType) {

		int paddle[] = null;
		switch (panelType) {
		case PANEL_1A:
			paddle = ColumnData.getIntArray("FTOF1A::dgtz.paddle");
			break;

		case PANEL_1B:
			paddle = ColumnData.getIntArray("FTOF1B::dgtz.paddle");
			break;

		case PANEL_2:
			paddle = ColumnData.getIntArray("FTOF2B::dgtz.paddle");
			break;
		}
		return paddle;
	}

	/**
	 * Get the ADCL array from the dgtz data
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the ADCL array
	 */
	public static int[] ADCL(int panelType) {

		int ADCL[] = null;
		switch (panelType) {
		case PANEL_1A:
			ADCL = ColumnData.getIntArray("FTOF1A::dgtz.ADCL");
			break;

		case PANEL_1B:
			ADCL = ColumnData.getIntArray("FTOF1B::dgtz.ADCL");
			break;

		case PANEL_2:
			ADCL = ColumnData.getIntArray("FTOF2B::dgtz.ADCL");
			break;
		}
		return ADCL;
	}

	/**
	 * Get the ADCR array from the dgtz data
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the ADCR array
	 */
	public static int[] ADCR(int panelType) {

		int ADCR[] = null;
		switch (panelType) {
		case PANEL_1A:
			ADCR = ColumnData.getIntArray("FTOF1A::dgtz.ADCR");
			break;

		case PANEL_1B:
			ADCR = ColumnData.getIntArray("FTOF1B::dgtz.ADCR");
			break;

		case PANEL_2:
			ADCR = ColumnData.getIntArray("FTOF2B::dgtz.ADCR");
			break;
		}
		return ADCR;
	}

	/**
	 * Get the TDCL array from the dgtz data
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the TDCL array
	 */
	public static int[] TDCL(int panelType) {

		int TDCL[] = null;
		switch (panelType) {
		case PANEL_1A:
			TDCL = ColumnData.getIntArray("FTOF1A::dgtz.TDCL");
			break;

		case PANEL_1B:
			TDCL = ColumnData.getIntArray("FTOF1B::dgtz.TDCL");
			break;

		case PANEL_2:
			TDCL = ColumnData.getIntArray("FTOF2B::dgtz.TDCL");
			break;
		}
		return TDCL;
	}

	/**
	 * Get the TDCR array from the dgtz data
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the TDCR array
	 */
	public static int[] TDCR(int panelType) {

		int TDCR[] = null;
		switch (panelType) {
		case PANEL_1A:
			TDCR = ColumnData.getIntArray("FTOF1A::dgtz.TDCR");
			break;

		case PANEL_1B:
			TDCR = ColumnData.getIntArray("FTOF1B::dgtz.TDCR");
			break;

		case PANEL_2:
			TDCR = ColumnData.getIntArray("FTOF2B::dgtz.TDCR");
			break;
		}
		return TDCR;
	}
	
	/**
	 * Get the avgX array from the true data
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the avgX array
	 */
	public static double[] avgX(int panelType) {
		double avgX[] = null;
		switch (panelType) {

		case PANEL_1A:
			avgX = ColumnData.getDoubleArray("FTOF1A::true.avgX");
			break;

		case PANEL_1B:
			avgX = ColumnData.getDoubleArray("FTOF1B::true.avgX");
			break;

		case PANEL_2:
			avgX = ColumnData.getDoubleArray("FTOF2B::true.avgX");
			break;
		}
		return avgX;
	}
	
	
	/**
	 * Get the avgY array from the true data
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the avgY array
	 */
	public static double[] avgY(int panelType) {
		double avgY[] = null;
		switch (panelType) {

		case PANEL_1A:
			avgY = ColumnData.getDoubleArray("FTOF1A::true.avgY");
			break;

		case PANEL_1B:
			avgY = ColumnData.getDoubleArray("FTOF1B::true.avgY");
			break;

		case PANEL_2:
			avgY = ColumnData.getDoubleArray("FTOF2B::true.avgY");
			break;
		}
		return avgY;
	}
	
	
	/**
	 * Get the avgZ array from the true data
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the avgZ array
	 */
	public static double[] avgZ(int panelType) {
		double avgZ[] = null;
		switch (panelType) {

		case PANEL_1A:
			avgZ = ColumnData.getDoubleArray("FTOF1A::true.avgZ");
			break;

		case PANEL_1B:
			avgZ = ColumnData.getDoubleArray("FTOF1B::true.avgZ");
			break;

		case PANEL_2:
			avgZ = ColumnData.getDoubleArray("FTOF2B::true.avgZ");
			break;
		}
		return avgZ;
	}
	
	/**
	 * Get the reconstructed sector array from the reconstructed data
	 * 
	 * @return the reconstructed sector array
	 */
	public static int[] reconSector() {
		return ColumnData.getIntArray("FTOFRec::ftofhits.sector");
	}

	/**
	 * Get the reconstructed panel array from the reconstructed data
	 * 
	 * @return the reconstructed panel array
	 */
	public static int[] reconPanel() {
		return ColumnData.getIntArray("FTOFRec::ftofhits.panel_id");
	}

	/**
	 * Get the reconstructed paddle array from the reconstructed data
	 * 
	 * @return the reconstructed sector array
	 */
	public static int[] reconPaddle() {
		return ColumnData.getIntArray("FTOFRec::ftofhits.paddle_id");
	}

	/**
	 * Get the reconstructed x array from the reconstructed data
	 * 
	 * @return the reconstructed x array
	 */
	public static float[] reconX() {
		return ColumnData.getFloatArray("FTOFRec::ftofhits.x");
	}

	/**
	 * Get the reconstructed y array from the reconstructed data
	 * 
	 * @return the reconstructed y array
	 */
	public static float[] reconY() {
		return ColumnData.getFloatArray("FTOFRec::ftofhits.y");
	}

	/**
	 * Get the reconstructed z array from the reconstructed data
	 * 
	 * @return the reconstructed z array
	 */
	public static float[] reconZ() {
		return ColumnData.getFloatArray("FTOFRec::ftofhits.z");
	}
	
	/**
	 * Add some dgtz hit feedback for ftof
	 * 
	 * @param hitIndex
	 *            the hit index
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @param feedbackStrings
	 *            the collection of feedback strings
	 */
	public static void dgtzFeedback(int hitIndex, int panelType,
			List<String> feedbackStrings) {

		if (hitIndex < 0) {
			return;
		}

		int sector[] = sector(panelType);
		int paddle[] = paddle(panelType);
		int ADCL[] = ADCL(panelType);
		int ADCR[] = ADCR(panelType);
		int TDCL[] = TDCL(panelType);
		int TDCR[] = TDCR(panelType);
		if ((sector == null) || (paddle == null) || (ADCL == null)
				|| (ADCR == null) || (TDCL == null) || (TDCR == null)) {
			return;
		}

		feedbackStrings.add(DataSupport.dgtzColor + "panel_1B  sector "
				+ sector[hitIndex] + "  paddle " + paddle[hitIndex]);

		feedbackStrings.add(DataSupport.dgtzColor + "adc_left "
				+ ADCL[hitIndex] + "  adc_right " + ADCR[hitIndex]);
		feedbackStrings.add(DataSupport.dgtzColor + "tdc_left "
				+ TDCL[hitIndex] + "  tdc_right " + TDCR[hitIndex]);

	}

	/**
	 * Get the name from the panel type
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the name of the panel type
	 */
	public static String name(int panelType) {
		if ((panelType < 0) || (panelType > 2)) {
			return "???";
		} else {
			return panelNames[panelType];
		}
	}

	/**
	 * Get the index of the ftof hit
	 * 
	 * @param sect
	 *            the 1-based sector
	 * @param paddle
	 *            the 1-based paddle
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the index of a hit in a panel with these parameters, or -1 if not
	 *         found
	 */
	public static int hitIndex(int sect, int paddle, int panelType) {

		int sector[] = sector(panelType);
		int paddles[] = paddle(panelType);

		if (sector == null) {
			return -1;
		}

		if (paddles == null) {
			Log.getInstance().warning(
					"null paddles array in hitIndex for panelType: "
							+ panelType + " sector: " + sector + " paddle: "
							+ paddle);
			return -1;
		}

		for (int i = 0; i < hitCount(panelType); i++) {
			if ((sect == sector[i]) && (paddle == paddles[i])) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Get the hit count for panel 1A panel
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the hit count for panel 1A
	 */
	public static int hitCount(int panelType) {
		int sector[] = sector(panelType);
		return (sector == null) ? 0 : sector.length;
	}

}
