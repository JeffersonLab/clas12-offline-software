package cnuphys.ced.event.data;

import cnuphys.ced.alldata.ColumnData;

/**
 * static methods to centralize getting data arrays related to BMT
 * @author heddle
 *
 */

public class BMT {
	
	/**
	 * Get the pid array from the true data
	 * @return the pid array
	 */
	public static int[] pid() {
		return ColumnData.getIntArray("BMT::true.pid");
	}
		
	/**
	 * Get the sector array from the dgtz array
	 * @return the sector array
	 */
	public static int[] sector() {
		return ColumnData.getIntArray("BMT::dgtz.sector");
	}
	
	/**
	 * Get the layer array from the dgtz array
	 * @return the layer array
	 */
	public static int[] layer() {
		return ColumnData.getIntArray("BMT::dgtz.layer");
	}
	
	/**
	 * Get the strip array from the dgtz array
	 * @return the strip array
	 */
	public static int[] strip() {
		return ColumnData.getIntArray("BMT::dgtz.strip");
	}
	
	/**
	 * Get the Edep array from the dgtz array
	 * @return the Edep array
	 */
	public static double[] Edep() {
		return ColumnData.getDoubleArray("BMT::dgtz.Edep");
	}
	
	/**
	 * Get the cross X array from the reconstructed data
	 * @return the cross X array
	 */
	public static double[] crossX() {
		return ColumnData.getDoubleArray("BMTRec::Crosses.x");
	}
	
	/**
	 * Get the cross Y array from the reconstructed data
	 * @return the cross Y array
	 */
	public static double[] crossY() {
		return ColumnData.getDoubleArray("BMTRec::Crosses.y");
	}
	
	/**
	 * Get the cross Z array from the reconstructed data
	 * @return the cross Z array
	 */
	public static double[] crossZ() {
		return ColumnData.getDoubleArray("BMTRec::Crosses.z");
	}
	
	/**
	 * Get the cross Ux array from the reconstructed data
	 * @return the cross Ux array
	 */
	public static double[] crossUx() {
		return ColumnData.getDoubleArray("BMTRec::Crosses.ux");
	}
	
	/**
	 * Get the cross Uy array from the reconstructed data
	 * @return the cross Uy array
	 */
	public static double[] crossUy() {
		return ColumnData.getDoubleArray("BMTRec::Crosses.uy");
	}
	
	/**
	 * Get the cross Uz array from the reconstructed data
	 * @return the cross Uz array
	 */
	public static double[] crossUz() {
		return ColumnData.getDoubleArray("BMTRec::Crosses.uz");
	}
	
	/**
	 * Get the cross xerr array from the reconstructed data
	 * @return the cross xerr array
	 */
	public static double[] crossXerr() {
		return ColumnData.getDoubleArray("BMTRec::Crosses.err_x");
	}

	/**
	 * Get the cross yerr array from the reconstructed data
	 * @return the cross yerr array
	 */
	public static double[] crossYerr() {
		return ColumnData.getDoubleArray("BMTRec::Crosses.err_y");
	}

	/**
	 * Get the cross zerr array from the reconstructed data
	 * @return the cross zerr array
	 */
	public static double[] crossZerr() {
		return ColumnData.getDoubleArray("BMTRec::Crosses.err_z");
	}


	/**
	 * Get the IDs from the reconstructed cross data
	 * @return the ID array
	 */
	public static int[] crossID() {
		return ColumnData.getIntArray("BMTRec::Crosses.ID");
	}
		
	/**
	 * Get the cross sector from the reconstructed cross data
	 * @return the cross sector array
	 */
	public static int[] crossSector() {
		return ColumnData.getIntArray("BMTRec::Crosses.sector");
	}
	
	/**
	 * Get the cross region from the reconstructed cross data
	 * @return the cross region array
	 */
	public static int[] crossRegion() {
		return ColumnData.getIntArray("BMTRec::Crosses.region");
	}
	
	/**
	 * Get the hit count for bmt 
	 * @return the hit count
	 */
	public static int hitCount() {
		int sector[] = sector();
		return (sector == null) ? 0 : sector.length;
	}

	/**
	 * Get the number of reconstructed crosses
	 * 
	 * @return the number of reconstructed crosses
	 */
	public static int crossCount() {
		int sector[] = crossSector();
		return (sector == null) ? 0 : sector.length;
	}

}
