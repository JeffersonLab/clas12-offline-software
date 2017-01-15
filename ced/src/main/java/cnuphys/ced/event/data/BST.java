package cnuphys.ced.event.data;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import cnuphys.ced.alldata.ColumnData;

/**
 * static methods to centralize getting data arrays related to BST
 * @author heddle
 *
 */

public class BST {

	/**
	 * Get the pid array from the true data
	 * @return the pid array
	 */
	public static int[] pid() {
		return ColumnData.getIntArray("BST::true.pid");
	}
		
	/**
	 * Get the sector array from the dgtz array
	 * @return the sector array
	 */
	public static int[] sector() {
		return ColumnData.getIntArray("BST::dgtz.sector");
	}
	
	/**
	 * Get the layer array from the dgtz array
	 * @return the layer array
	 */
	public static int[] layer() {
		return ColumnData.getIntArray("BST::dgtz.layer");
	}

	/**
	 * Get the strip array from the dgtz array
	 * @return the strip array
	 */
	public static int[] strip() {
		return ColumnData.getIntArray("BST::dgtz.strip");
	}

	/**
	 * Get the ADC array from the dgtz array
	 * @return the ADC array
	 */
	public static int[] ADC() {
		return ColumnData.getIntArray("BST::dgtz.ADC");
	}


	/**
	 * Get the avgX array from the true data
	 * @return the avgX array
	 */
	public static double[] avgX() {
		return ColumnData.getDoubleArray("BST::true.avgX");
	}
	
	/**
	 * Get the avgY array from the true data
	 * @return the avgY array
	 */
	public static double[] avgY() {
		return ColumnData.getDoubleArray("BST::true.avgY");
	}
	
	/**
	 * Get the avgZ array from the true data
	 * @return the avgZ array
	 */
	public static double[] avgZ() {
		return ColumnData.getDoubleArray("BST::true.avgZ");
	}
	
	/**
	 * Get the avgLx array from the true data
	 * @return the avgLx array
	 */
	public static double[] avgLx() {
		return ColumnData.getDoubleArray("BST::true.avgLx");
	}
	
	/**
	 * Get the avgLy array from the true data
	 * @return the avgLy array
	 */
	public static double[] avgLy() {
		return ColumnData.getDoubleArray("BST::true.avgLy");
	}
	
	/**
	 * Get the avgLz array from the true data
	 * @return the avgLz array
	 */
	public static double[] avgLz() {
		return ColumnData.getDoubleArray("BST::true.avgLz");
	}
	
	/**
	 * Get the cross X array from the reconstructed data
	 * @return the cross X array
	 */
	public static double[] crossX() {
		return ColumnData.getDoubleArray("BSTRec::Crosses.x");
	}
	
	/**
	 * Get the cross Y array from the reconstructed data
	 * @return the cross Y array
	 */
	public static double[] crossY() {
		return ColumnData.getDoubleArray("BSTRec::Crosses.y");
	}
	
	/**
	 * Get the cross Z array from the reconstructed data
	 * @return the cross Z array
	 */
	public static double[] crossZ() {
		return ColumnData.getDoubleArray("BSTRec::Crosses.z");
	}
	
	/**
	 * Get the cross Ux array from the reconstructed data
	 * @return the cross Ux array
	 */
	public static double[] crossUx() {
		return ColumnData.getDoubleArray("BSTRec::Crosses.ux");
	}
	
	/**
	 * Get the cross Uy array from the reconstructed data
	 * @return the cross Uy array
	 */
	public static double[] crossUy() {
		return ColumnData.getDoubleArray("BSTRec::Crosses.uy");
	}
	
	/**
	 * Get the cross Uz array from the reconstructed data
	 * @return the cross Uz array
	 */
	public static double[] crossUz() {
		return ColumnData.getDoubleArray("BSTRec::Crosses.uz");
	}
	
	/**
	 * Get the cross xerr array from the reconstructed data
	 * @return the cross xerr array
	 */
	public static double[] crossXerr() {
		return ColumnData.getDoubleArray("BSTRec::Crosses.err_x");
	}

	/**
	 * Get the cross yerr array from the reconstructed data
	 * @return the cross yerr array
	 */
	public static double[] crossYerr() {
		return ColumnData.getDoubleArray("BSTRec::Crosses.err_y");
	}

	/**
	 * Get the cross zerr array from the reconstructed data
	 * @return the cross zerr array
	 */
	public static double[] crossZerr() {
		return ColumnData.getDoubleArray("BSTRec::Crosses.err_z");
	}

	/**
	 * Get the IDs from the reconstructed cross data
	 * @return the ID array
	 */
	public static int[] crossID() {
		return ColumnData.getIntArray("BSTRec::Crosses.ID");
	}
		
	/**
	 * Get the cross sector from the reconstructed cross data
	 * @return the cross sector array
	 */
	public static int[] crossSector() {
		return ColumnData.getIntArray("BSTRec::Crosses.sector");
	}
	
	/**
	 * Get the cross region from the reconstructed cross data
	 * @return the cross region array
	 */
	public static int[] crossRegion() {
		return ColumnData.getIntArray("BSTRec::Crosses.region");
	}
	
	
	/**
	 * Get the yx intercept from the reconstructed cosmics data
	 * @return the yx intercept array
	 */
	public static double[] cosmicYxInterc() {
		return ColumnData.getDoubleArray("BSTRec::Cosmics.trkline_yx_interc");
	}
	
	/**
	 * Get the yx slope from the reconstructed cosmics data
	 * @return the yx slope array
	 */
	public static double[] cosmicYxSlope() {
		return ColumnData.getDoubleArray("BSTRec::Cosmics.trkline_yx_slope");
	}
	
	/**
	 * Get the yz intercept from the reconstructed cosmics data
	 * @return the yz intercept array
	 */
	public static double[] cosmicYzInterc() {
		return ColumnData.getDoubleArray("BSTRec::Cosmics.trkline_yz_interc");
	}
	
	/**
	 * Get the yz slope from the reconstructed cosmics data
	 * @return the yz slope array
	 */
	public static double[] cosmicYzSlope() {
		return ColumnData.getDoubleArray("BSTRec::Cosmics.trkline_yz_slope");
	}
	
	/**
	 * Get the IDs from the reconstructed cosmics data
	 * @return the ID array
	 */
	public static int[] cosmicID() {
		return ColumnData.getIntArray("BSTRec::Cosmics.ID");
	}
	
	/**
	 * Get a collection of all strip, adc doublets for a given sector and layer
	 * 
	 * @param sector the 1-based sector
	 * @param layer the 1-based layer
	 * @return a collection of all strip, adc doublets for a given sector and
	 *         layer. It is a collection of integer arrays. For each array, the
	 *         0 entry is the 1-based strip and the 1 entry is the adc.
	 */
	public static Vector<int[]> allStripsForSectorAndLayer(int sector,
			int layer) {
		Vector<int[]> strips = new Vector<int[]>();

		int sect[] = sector();

		if (sect != null) {
			int lay[] = layer();
			int strip[] = strip();
			int ADC[] = ADC();

			for (int hitIndex = 0; hitIndex < sect.length; hitIndex++) {
				if ((sect[hitIndex] == sector) && (lay[hitIndex] == layer)) {
					int data[] = { strip[hitIndex], ADC[hitIndex] };
					strips.add(data);
				}
			}
		}

		// sort based on strips
		if (strips.size() > 1) {
			Comparator<int[]> c = new Comparator<int[]>() {

				@Override
				public int compare(int[] o1, int[] o2) {
					return Integer.compare(o1[0], o2[0]);
				}
			};

			Collections.sort(strips, c);
		}

		return strips;
	}
	
	/**
	 * Get the hit count for bst 
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
