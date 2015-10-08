package cnuphys.ced.cedview.allec;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Vector;

import org.jlab.geom.prim.Point3D;

import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.FeedbackRect;
import cnuphys.ced.event.data.ADataContainer;
import cnuphys.ced.event.data.DataDrawSupport;
import cnuphys.ced.event.data.ECDataContainer;
import cnuphys.ced.geometry.ECGeometry;
import cnuphys.ced.geometry.GeometryManager;

public class McHitDrawer extends ECViewDrawer {

    // cached rectangles for feedback
    private Vector<FeedbackRect> _fbRects = new Vector<FeedbackRect>();

    public McHitDrawer(ECView view) {
	super(view);
    }

    @Override
    public void draw(Graphics g, IContainer container) {

	if (ClasIoEventManager.getInstance().isAccumulating()) {
	    return;
	}

	if (!_view.showMcTruth()) {
	    return;
	}

	ECDataContainer ecData = _eventManager.getECData();

	_fbRects.clear();

	showGemcXYZHits(g, container, ecData);
    }

    // the actual hit drawing
    private void showGemcXYZHits(Graphics g, IContainer container,
	    ECDataContainer data) {

	double x[] = data.ec_true_avgX;
	double y[] = data.ec_true_avgY;
	double z[] = data.ec_true_avgZ;
	int stack[] = data.ec_dgtz_stack;

	if ((x == null) || (y == null) || (z == null) || (x.length < 1)
		|| (stack == null)) {
	    return;
	}

	double labXYZ[] = new double[3];
	Point pp = new Point();

	// should not be necessary but be safe
	int len = x.length;
	len = Math.min(len, y.length);
	len = Math.min(len, z.length);

	for (int hitIndex = 0; hitIndex < len; hitIndex++) {

	    boolean show = false;
	    if (_view.displayInner()) {
		show = stack[hitIndex] == 1;
	    } else {
		show = stack[hitIndex] == 2;
	    }

	    if (show) {
		labXYZ[0] = x[hitIndex] / 10; // mm to cm
		labXYZ[1] = y[hitIndex] / 10;
		labXYZ[2] = z[hitIndex] / 10;

		int plane = stack[hitIndex] - 1;

		Point3D clasP = new Point3D(labXYZ[0], labXYZ[1], labXYZ[2]);
		Point3D localP = new Point3D();
		ECGeometry.getTransformations(plane).clasToLocal(localP, clasP);
		int sector = GeometryManager.getSector(labXYZ[0], labXYZ[1]);
		localP.setZ(0);

		List<String> fbs = data.gemcHitFeedback(hitIndex,
			ADataContainer.FB_CLAS_XYZ + ADataContainer.FB_CLAS_RTP
				+ ADataContainer.FB_LOCAL_XYZ
				+ ADataContainer.FB_TOTEDEP,
			ECGeometry.getTransformations(plane), data.ec_true_pid,
			data.ec_true_avgX, data.ec_true_avgY,
			data.ec_true_avgZ, data.ec_true_totEdep);
		
	    //get the right item
	    _view.getHexSectorItem(sector).ijkToScreen(container, localP, pp);

		FeedbackRect rr = new FeedbackRect(pp.x - 4, pp.y - 4, 8, 8,
			hitIndex, data, 0, fbs);
		_fbRects.addElement(rr);

		DataDrawSupport.drawGemcHit(g, pp);
	    }
	}
    }

    /**
     * Use what was drawn to generate feedback strings
     * 
     * @param container
     *            the drawing container
     * @param screenPoint
     *            the mouse location
     * @param worldPoint
     *            the corresponding world location
     * @param feedbackStrings
     *            add strings to this collection
     */
    @Override
    public void feedback(IContainer container, Point screenPoint,
	    Point2D.Double worldPoint, List<String> feedbackStrings) {

	if (_fbRects.isEmpty()) {
	    return;
	}

	for (FeedbackRect rr : _fbRects) {
	    boolean contains = rr.contains(screenPoint, feedbackStrings);
	    if (contains && (rr.hitIndex >= 0)) {

		ADataContainer data = rr.dataContainer;
		int hitIndex = rr.hitIndex;

		// additional feedback
		if (rr.dataContainer instanceof ECDataContainer) {
		    ECDataContainer ecdata = (ECDataContainer) (rr.dataContainer);
		    int strip = rr.dataContainer.get(ecdata.ec_dgtz_strip,
			    hitIndex);
		    int stack = rr.dataContainer.get(ecdata.ec_dgtz_stack,
			    hitIndex);
		    int view = rr.dataContainer.get(ecdata.ec_dgtz_view,
			    hitIndex);

		    if ((strip > 0) && (stack > 0) && (view > 0)) {
			String s = ADataContainer.trueColor + "Gemc Hit "
				+ " plane "
				+ DataDrawSupport.EC_PLANE_NAMES[stack]
				+ " type "
				+ DataDrawSupport.EC_VIEW_NAMES[view]
				+ " strip " + strip;
			feedbackStrings.add(s);
		    }
		}
		return;
	    }
	}

    }

    // for writing out a vector
    private String vecStr(String prompt, double vx, double vy, double vz) {
	return ADataContainer.trueColor + prompt + " ("
		+ DoubleFormat.doubleFormat(vx, 3) + ", "
		+ DoubleFormat.doubleFormat(vy, 3) + ", "
		+ DoubleFormat.doubleFormat(vz, 3) + ")";
    }

}