package cnuphys.splot.pdata;

/**
 * Types of data sets
 * 
 * @author heddle These are the different types of data sets.
 *         <ul>
 *         <li><code>XYY</code> An arbitrary number of y sets sharing the same x
 *         <li><code>XYXY</code> An arbitrary number of (x, y) columns
 *         <li><code>XYEXYE</code> An arbitrary number of (x, y, yerr) columns
 *         <li><code>XYEEXYEE</code> An arbitrary number of (x, y, xerr, yerr)
 *         columns
 *         <li><code>H1D</code> Data values for a 1D histogram
 *         <li><code>H2D</code> Data values for a 2D histogram
 *         <li><code>STRIP</code> xy strip chart
 */
public enum DataSetType {
	XYY, XYXY, XYEXYE, XYEEXYEE, H1D, H2D, STRIP, UNKNOWN;

}
