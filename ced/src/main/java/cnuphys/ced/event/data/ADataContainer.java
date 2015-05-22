package cnuphys.ced.event.data;

import java.util.List;
import java.util.Vector;

import org.jlab.evio.clas12.EvioDataEvent;
import org.jlab.geom.prim.Point3D;

import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Bits;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.bCNU.view.EventDisplayView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.IClasIoEventListener;
import cnuphys.ced.geometry.PCALGeometry;
import cnuphys.ced.geometry.Transformations;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;

public abstract class ADataContainer implements IClasIoEventListener {

    // the all important event manager
    protected ClasIoEventManager _eventManager;

    // for uniform feedback colors
    public static final String prelimColor = "$orange$";
    public static final String trueColor = "$Alice Blue$";
    public static final String dgtxColor = "$Moccasin$";
    public static final String reconColor = "$coral$";

    // Common TRUE (simulated) arrays

    public ADataContainer(ClasIoEventManager eventManager) {
	_eventManager = eventManager;
	_eventManager.addPhysicsListener(this, 0);
    }

    /**
     * Clear all the data. This is where subclasses nullify their unique arrays.
     * The common arrays are nullified in commonClear().
     */
    public abstract void clear();

    /**
     * Load all the arrays from the given event
     * 
     * @param event
     *            the new event
     */
    public abstract void load(EvioDataEvent event);

    /**
     * Get the hit count.
     * 
     * @param option
     *            might be used for "composite" data containers like EC, or
     *            might be ignored
     * @return the hit count
     */
    public abstract int getHitCount(int option);

    // {
    // int hitCount = (dgtz_sector == null) ? 0 : dgtz_sector.length;
    // return hitCount;
    // }

    @Override
    public void openedNewEventFile(String path) {
    }

    @Override
    public void newClasIoEvent(EvioDataEvent event) {
	clear();
	if (event != null) {
	    load(event);
	}
	finalEventPrep(event);
	// extractUniqueLundIds();
    }

    public abstract void finalEventPrep(EvioDataEvent event);

    protected void extractUniqueLundIds(int pid[]) {

	if (pid != null) {
	    Vector<LundId> uniqueIds = _eventManager.getUniqueLundIds();

	    synchronized (uniqueIds) {
		for (int pdgid : pid) {
		    // System.err.print("PID: " + pdgid + "    " +
		    // this.getClass().getName());
		    LundId lid = LundSupport.getInstance().get(pdgid);
		    if (lid != null) {
			// System.err.println("  " + lid.getName());
			uniqueIds.remove(lid); // avoid duplicates
			uniqueIds.add(lid);
		    } else {
			Log.getInstance()
				.warning(
					"[extractUniqueLundIds] Unrecognized PID: "
						+ pdgid + "    "
						+ getClass().getName());
			// System.err.print("Unrecognized PID: " + pdgid +
			// "    " + this.getClass().getName());
		    }
		}
	    }
	}
    }

    /**
     * Add preliminary feedback, like noise guess
     * 
     * @param hitIndex
     *            the hit index
     * @param option
     *            might be used for "composite" data containers like EC, or
     *            might be ignored
     * @param feedbackStringsthe
     *            strings to add to
     */
    public abstract void addPreliminaryFeedback(int hitIndex, int option,
	    List<String> feedbackStrings);

    /**
     * Add true feedback, beyond the defaults in the super class
     * 
     * @param hitIndex
     *            the hit index
     * @param option
     *            might be used for "composite" data containers like EC, or
     *            might be ignored
     * @param feedbackStringsthe
     *            strings to add to
     */
    public abstract void addTrueFeedback(int hitIndex, int option,
	    List<String> feedbackStrings);

    /**
     * Add dgtz feedback, beyond the defaults in the super class
     * 
     * @param hitIndex
     *            the hit index
     * @param option
     *            might be used for "composite" data containers like EC, or
     *            might be ignored
     * @param feedbackStringsthe
     *            strings to add to
     */
    public abstract void addDgtzFeedback(int hitIndex, int option,
	    List<String> feedbackStrings);

    /**
     * Add final feedback
     * 
     * @param option
     *            might be used for "composite" data containers like EC, or
     *            might be ignored
     * @param feedbackStringst
     *            the strings to add to
     */
    public abstract void addFinalFeedback(int option,
	    List<String> feedbackStrings);

    /**
     * Add reconstructed feedback
     * 
     * @param option
     *            might be used for "composite" data containers like EC, or
     *            might be ignored
     * @param feedbackStringsthe
     *            strings to add to
     */
    public abstract void addReconstructedFeedback(int option,
	    List<String> feedbackStrings);

    /**
     * Add to the feedback strings
     * 
     * @param hitIndex
     *            a hit index
     * @param option
     *            might be used for "composite" data containers like EC, or
     *            might be ignored
     * @param feedbackStrings
     *            the strings to add to
     */
    public void onHitFeedbackStrings(int hitIndex, int option, int true_pid[],
	    int true_mpid[], int true_tid[], int true_mtid[], int true_otid[],
	    List<String> feedbackStrings) {

	if ((hitIndex < 0) || (hitIndex >= getHitCount(option))) {
	    System.err.println("ohfs abort hitIndex: " + hitIndex
		    + "  option: " + option + "   count: "
		    + getHitCount(option));
	    return;
	}

	// add preliminary
	addPreliminaryFeedback(hitIndex, option, feedbackStrings);

	// add default dgtz

	// add extra dgtz
	addDgtzFeedback(hitIndex, option, feedbackStrings);

	// add default true
	if (true_pid != null) {
	    feedbackStrings.add(trueColor + "pid " + pidStr(true_pid[hitIndex])
		    + "  mpid " + pidStr(true_mpid[hitIndex]) + " tid "
		    + true_tid[hitIndex] + " mtid " + true_mtid[hitIndex]
		    + " otid " + true_otid[hitIndex]);
	}

	// add extra true
	addTrueFeedback(hitIndex, option, feedbackStrings);

    }

    /**
     * Add to the feedback strings (even if not on a hit)
     * 
     * @param option
     *            might be used for "composite" data containers like EC, or
     *            might be ignored
     * @param feedbackStrings
     *            the strings to add to
     */
    public void generalFeedbackStrings(int option, List<String> feedbackStrings) {
	// add reconstructed
	addReconstructedFeedback(option, feedbackStrings);

	// add final
	addFinalFeedback(option, feedbackStrings);
    }

    /**
     * Safe way to get an integer element from an array for printing
     * 
     * @param array
     *            the array in question
     * @param index
     *            the array index
     * @return a string for printing
     */
    protected String safeString(int[] array, int index) {
	if (array == null) {
	    return "null";
	} else if ((index < 0) || (index >= array.length)) {
	    return "BADIDX: " + index;
	} else {
	    return "" + array[index];
	}
    }

    /**
     * Safe way to get an double element from an array for printing
     * 
     * @param array
     *            the array in question
     * @param index
     *            the array index
     * @param numdec
     *            the number of decimal points
     * @return a string for printing
     */
    protected String safeString(double[] array, int index, int numdec) {
	if (array == null) {
	    return "null";
	} else if ((index < 0) || (index >= array.length)) {
	    return "BADIDX: " + index;
	} else {
	    return DoubleFormat.doubleFormat(array[index], numdec);
	}
    }

    public static final int FB_CLAS_XYZ = 01;
    public static final int FB_CLAS_RTP = 02; // r, theta, phi
    public static final int FB_TOTEDEP = 04; // total energy dep (MeV)
    public static final int FB_LOCAL_XYZ = 010;

    public List<String> gemcHitFeedback(int hitIndex, int bits,
	    Transformations trans, int true_pid[], double avgX[],
	    double avgY[], double avgZ[], double eDep[]) {

	Vector<String> fbs = new Vector<String>();

	int trueCount = (avgX == null) ? 0 : avgX.length;
	if (hitIndex >= trueCount) {
	    Log.getInstance().warning(
		    "gemcHitFeedback index out of range: " + hitIndex);
	    return fbs;
	}

	// some preliminaries
	double labXYZ[] = new double[3];
	labXYZ[0] = avgX[hitIndex] / 10; // mm to cm
	labXYZ[1] = avgY[hitIndex] / 10;
	labXYZ[2] = avgZ[hitIndex] / 10;

	int pid = 0;

	if ((true_pid != null) && (hitIndex < true_pid.length)) {
	    pid = true_pid[hitIndex];
	}

	String prefix = trueColor + "Gemc Hit [" + (hitIndex + 1) + "] "
		+ pidStr(pid);

	try {
	    if (Bits.checkBit(bits, FB_CLAS_XYZ)) {
		fbs.add(vecStr(prefix + "clas xyz", labXYZ, 2, "cm"));
	    }

	    if (Bits.checkBit(bits, FB_CLAS_RTP)) {
		fbs.add(sphericalStr(prefix, labXYZ, 3));
	    }

	    if (Bits.checkBit(bits, FB_LOCAL_XYZ)) {
		if (trans != null) {
		    Point3D clasP = new Point3D(labXYZ[0], labXYZ[1], labXYZ[2]);
		    Point3D localP = new Point3D();
		    PCALGeometry.getTransformations()
			    .clasToLocal(localP, clasP);
		    fbs.add(p3dStr(prefix + "loc xyz", localP, 2, "cm"));
		}
	    }

	    if ((eDep != null) && Bits.checkBit(bits, FB_TOTEDEP)) {
		if (hitIndex < eDep.length) {
		    fbs.add(scalarStr(prefix + "tot edep", eDep[hitIndex], 2,
			    "MeV"));
		}
	    }

	} catch (Exception e) {
	    Log.getInstance().exception(e);
	}

	return fbs;
    }

    /**
     * Returns a string representation of the form: "(x,y,z)".
     * 
     * @param xyz
     *            the Cartesian coordinates in cm
     * @param numDec
     *            the number of decimal places for each value.
     * @return a String representation of the vector in spherical coordinates
     */
    public String sphericalStr(String prefix, double xyz[], int numDec) {
	double r = Math.sqrt(xyz[0] * xyz[0] + xyz[1] * xyz[1] + xyz[2]
		* xyz[2]);
	double theta = Math.toDegrees(Math.acos(xyz[2] / r));
	double phi = Math.toDegrees(Math.atan2(xyz[1], xyz[0]));

	StringBuilder sb = new StringBuilder(128);

	if (prefix != null) {
	    sb.append(prefix + " ");
	}

	sb.append(EventDisplayView.rThetaPhi + " (");

	sb.append(DoubleFormat.doubleFormat(r, numDec) + "cm, ");
	sb.append(DoubleFormat.doubleFormat(theta, numDec));
	sb.append(UnicodeSupport.DEGREE + ", ");
	sb.append(DoubleFormat.doubleFormat(phi, numDec));
	sb.append(UnicodeSupport.DEGREE + ", ");
	sb.append(")");

	return sb.toString();

    }

    /**
     * Returns a string representation of the form: "(x,y,z)".
     * 
     * @param v
     *            the value
     * @param numDec
     *            the number of decimal places for each coordinate.
     * @return a String representation of the vector
     */
    public String scalarStr(String prefix, double v, int numDec, String postfix) {

	StringBuilder sb = new StringBuilder(128);

	if (prefix != null) {
	    sb.append(prefix + " ");
	}

	sb.append(DoubleFormat.doubleFormat(v, numDec));

	if (postfix != null) {
	    sb.append(" " + postfix);
	}

	return sb.toString();
    }

    /**
     * Returns a string representation of the form: "(x,y,z)".
     * 
     * @param v
     *            the vector (any double array)
     * @param numDec
     *            the number of decimal places for each coordinate.
     * @return a String representation of the vector
     */
    public String vecStr(String prefix, double v[], int numDec, String postfix) {

	StringBuilder sb = new StringBuilder(128);
	int lm1 = v.length - 1;

	if (prefix != null) {
	    sb.append(prefix + " ");
	}

	sb.append("(");

	for (int i = 0; i <= lm1; i++) {
	    sb.append(DoubleFormat.doubleFormat(v[i], numDec));
	    if (i != lm1) {
		sb.append(", ");
	    }
	}

	sb.append(")");

	if (postfix != null) {
	    sb.append(" " + postfix);
	}

	return sb.toString();
    }

    /**
     * Returns a string representation of the form: "(x,y,z)".
     * 
     * @param p3d
     *            the ythree dim point
     * @param numDec
     *            the number of decimal places for each coordinate.
     * @return a String representation of the vector
     */
    public String p3dStr(String prefix, Point3D p3d, int numDec, String postfix) {

	StringBuilder sb = new StringBuilder(128);

	if (prefix != null) {
	    sb.append(prefix + " ");
	}

	sb.append("(");

	sb.append(DoubleFormat.doubleFormat(p3d.x(), numDec) + ", ");
	sb.append(DoubleFormat.doubleFormat(p3d.y(), numDec) + ", ");
	sb.append(DoubleFormat.doubleFormat(p3d.z(), numDec));

	sb.append(")");

	if (postfix != null) {
	    sb.append(" " + postfix);
	}

	return sb.toString();
    }

    /**
     * Get a string for the particle Id
     * 
     * @param pid
     *            the Lund particle Id
     * @return the string representation of the particle Id
     */
    protected String pidStr(int pid) {

	if (pid == 0) {
	    return "";
	}
	LundId lid = LundSupport.getInstance().get(pid);

	if (lid == null) {
	    return "(" + pid + ") ??";
	} else {
	    return lid.getName();
	}
    }

    /**
     * Get a double array from the event, by name. This is used because the
     * getDouble in clasIO returns an array with one element and value zero if
     * the bank is not there!
     * 
     * @param event
     *            the event
     * @param name
     *            the name
     * @return the double array, or <code>null</code>
     */
    protected double[] safeDoubleLoad(EvioDataEvent event, String name) {
	if ((event == null) || (name == null) || event.hasBank(name)) {
	    return null;
	}
	return event.getDouble(name);
    }

    /**
     * Safe way to get in int
     * 
     * @param array
     *            the array
     * @param index
     *            the index
     * @return -2147483648 [0x80000000] or -2^31 on any error otherwise the
     *         value
     */
    public int get(int array[], int index) {
	if (array == null) {
	    return Integer.MIN_VALUE;
	}
	if ((index < 0) || (index >= array.length)) {
	    return Integer.MIN_VALUE;
	}
	return array[index];
    }

    /**
     * Safe way to get a double
     * 
     * @param array
     *            the array
     * @param index
     *            the index
     * @return NaN on any error, otherwise the value
     */
    public double get(double array[], int index) {
	if (array == null) {
	    return Double.NaN;
	}
	if ((index < 0) || (index >= array.length)) {
	    return Double.NaN;
	}
	return array[index];
    }

}
