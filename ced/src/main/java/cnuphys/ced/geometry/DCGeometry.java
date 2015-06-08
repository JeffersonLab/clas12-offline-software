package cnuphys.ced.geometry;

import java.awt.geom.Point2D;
import java.util.List;

import org.jlab.clasrec.utils.DataBaseLoader;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.component.DriftChamberWire;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.detector.dc.DCDetector;
import org.jlab.geom.detector.dc.DCFactory;
import org.jlab.geom.detector.dc.DCLayer;
import org.jlab.geom.detector.dc.DCSector;
import org.jlab.geom.detector.dc.DCSuperlayer;
import org.jlab.geom.detector.ftof.FTOFLayer;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;

public class DCGeometry {
    
    private static ConstantProvider dcDataProvider;
    private static DCDetector dcDetector;
    private static DCSector sector0;
    
    static  {
	dcDataProvider = DataBaseLoader
		.getDriftChamberConstants();

	dcDetector = (new DCFactory())
		.createDetectorCLAS(dcDataProvider);

	sector0 = dcDetector.getSector(0);
	
    }

    public static void loadArrays() {


	// get the sense wires from the geometry provider
	for (int superlayerId = 0; superlayerId < 6; superlayerId++) {
	    DCSuperlayer superlayer = sector0.getSuperlayer(superlayerId);
	    for (int layer = 0; layer < 6; layer++) {
		DCLayer dcLayer = superlayer.getLayer(layer);
		int numWire = dcLayer.getNumComponents();
		for (int w = 0; w < numWire; w++) {
		    Line3D wire = dcLayer.getComponent(w).getLine();
		    
		    GeometryManager.x0[superlayerId][layer + 1][w + 1] = wire
			    .end().x();
		    GeometryManager.y0[superlayerId][layer + 1][w + 1] = wire
			    .end().y();
		    GeometryManager.z0[superlayerId][layer + 1][w + 1] = wire
			    .end().z();
		    GeometryManager.x1[superlayerId][layer + 1][w + 1] = wire
			    .origin().x();
		    GeometryManager.y1[superlayerId][layer + 1][w + 1] = wire
			    .origin().y();
		    GeometryManager.z1[superlayerId][layer + 1][w + 1] = wire
			    .origin().z();
		    
		    double len2 = wire.length();
		    Point3D mid = wire.midpoint();
		    
		    
		    if ((superlayerId < 2) && (layer == 3) && (w == 40)) {
			System.err.println("---------");
			System.err.println("Superlayer: " + (superlayerId+1) + "  layer: " + (layer+1) + "  wire: " + (w+1));
			System.err.println("Mid: " + mid);
			System.err.println("X: " + wire.end().x() + ", "
				+ wire.origin().x());
			System.err.println("Y: " + wire.end().y() + ", "
				+ wire.origin().y());
			System.err.println("Z: " + wire.end().z() + ", "
				+ wire.origin().z());
			System.err.println("dely/delx = " + ((wire.origin().y()-wire.end().y())/(wire.origin().x()-wire.end().x())));
		    }
		}
	    }
	}

	// get the guard wires (approximately)
	// first, wire 1 in all the sense layers
	for (int superLayer = 0; superLayer < 6; superLayer++) {
	    for (int layer = 1; layer <= 7; layer++) {
		double delx0 = GeometryManager.x0[superLayer][layer][2]
			- GeometryManager.x0[superLayer][layer][1];
		double dely0 = GeometryManager.y0[superLayer][layer][2]
			- GeometryManager.y0[superLayer][layer][1];
		double delz0 = GeometryManager.z0[superLayer][layer][2]
			- GeometryManager.z0[superLayer][layer][1];
		double delx1 = GeometryManager.x1[superLayer][layer][2]
			- GeometryManager.x1[superLayer][layer][1];
		double dely1 = GeometryManager.y1[superLayer][layer][2]
			- GeometryManager.y1[superLayer][layer][1];
		double delz1 = GeometryManager.z1[superLayer][layer][2]
			- GeometryManager.z1[superLayer][layer][1];
		GeometryManager.x0[superLayer][layer][0] = GeometryManager.x0[superLayer][layer][1]
			- delx0;
		GeometryManager.y0[superLayer][layer][0] = GeometryManager.y0[superLayer][layer][1]
			- dely0;
		GeometryManager.z0[superLayer][layer][0] = GeometryManager.z0[superLayer][layer][1]
			- delz0;
		GeometryManager.x1[superLayer][layer][0] = GeometryManager.x1[superLayer][layer][1]
			- delx1;
		GeometryManager.y1[superLayer][layer][0] = GeometryManager.y1[superLayer][layer][1]
			- dely1;
		GeometryManager.z1[superLayer][layer][0] = GeometryManager.z1[superLayer][layer][1]
			- delz1;
	    }
	}

	// now wire 113 in all the sense layers
	for (int superLayer = 0; superLayer < 6; superLayer++) {
	    for (int layer = 1; layer <= 7; layer++) {
		double delx0 = GeometryManager.x0[superLayer][layer][112]
			- GeometryManager.x0[superLayer][layer][111];
		double dely0 = GeometryManager.y0[superLayer][layer][112]
			- GeometryManager.y0[superLayer][layer][111];
		double delz0 = GeometryManager.z0[superLayer][layer][112]
			- GeometryManager.z0[superLayer][layer][111];
		double delx1 = GeometryManager.x1[superLayer][layer][112]
			- GeometryManager.x1[superLayer][layer][111];
		double dely1 = GeometryManager.y1[superLayer][layer][112]
			- GeometryManager.y1[superLayer][layer][111];
		double delz1 = GeometryManager.z1[superLayer][layer][112]
			- GeometryManager.z1[superLayer][layer][111];
		GeometryManager.x0[superLayer][layer][113] = GeometryManager.x0[superLayer][layer][112]
			+ delx0;
		GeometryManager.y0[superLayer][layer][113] = GeometryManager.y0[superLayer][layer][112]
			+ dely0;
		GeometryManager.z0[superLayer][layer][113] = GeometryManager.z0[superLayer][layer][112]
			+ delz0;
		GeometryManager.x1[superLayer][layer][113] = GeometryManager.x1[superLayer][layer][112]
			+ delx1;
		GeometryManager.y1[superLayer][layer][113] = GeometryManager.y1[superLayer][layer][112]
			+ dely1;
		GeometryManager.z1[superLayer][layer][113] = GeometryManager.z1[superLayer][layer][112]
			+ delz1;
	    }
	}

	// now layer 1 (guard layer)
	for (int superLayer = 0; superLayer < 6; superLayer++) {
	    for (int w = 0; w < 114; w++) {
		double delx0 = GeometryManager.x0[superLayer][4][w]
			- GeometryManager.x0[superLayer][2][w];
		double dely0 = GeometryManager.y0[superLayer][4][w]
			- GeometryManager.y0[superLayer][2][w];
		double delz0 = GeometryManager.z0[superLayer][4][w]
			- GeometryManager.z0[superLayer][2][w];
		double delx1 = GeometryManager.x1[superLayer][4][w]
			- GeometryManager.x1[superLayer][2][w];
		double dely1 = GeometryManager.y1[superLayer][4][w]
			- GeometryManager.y1[superLayer][2][w];
		double delz1 = GeometryManager.z1[superLayer][4][w]
			- GeometryManager.z1[superLayer][2][w];
		GeometryManager.x0[superLayer][0][w] = GeometryManager.x0[superLayer][2][w]
			- delx0;
		GeometryManager.y0[superLayer][0][w] = GeometryManager.y0[superLayer][2][w]
			- dely0;
		GeometryManager.z0[superLayer][0][w] = GeometryManager.z0[superLayer][2][w]
			- delz0;
		GeometryManager.x1[superLayer][0][w] = GeometryManager.x1[superLayer][2][w]
			- delx1;
		GeometryManager.y1[superLayer][0][w] = GeometryManager.y1[superLayer][2][w]
			- dely1;
		GeometryManager.z1[superLayer][0][w] = GeometryManager.z1[superLayer][2][w]
			- delz1;
	    }
	}

	// layer 8 (outer guard layer)
	for (int superLayer = 0; superLayer < 6; superLayer++) {
	    for (int w = 0; w < 114; w++) {
		double delx0 = GeometryManager.x0[superLayer][5][w]
			- GeometryManager.x0[superLayer][3][w];
		double dely0 = GeometryManager.y0[superLayer][5][w]
			- GeometryManager.y0[superLayer][3][w];
		double delz0 = GeometryManager.z0[superLayer][5][w]
			- GeometryManager.z0[superLayer][3][w];
		double delx1 = GeometryManager.x1[superLayer][5][w]
			- GeometryManager.x1[superLayer][3][w];
		double dely1 = GeometryManager.y1[superLayer][5][w]
			- GeometryManager.y1[superLayer][3][w];
		double delz1 = GeometryManager.z1[superLayer][5][w]
			- GeometryManager.z1[superLayer][3][w];
		GeometryManager.x0[superLayer][7][w] = GeometryManager.x0[superLayer][5][w]
			+ delx0;
		GeometryManager.y0[superLayer][7][w] = GeometryManager.y0[superLayer][5][w]
			+ dely0;
		GeometryManager.z0[superLayer][7][w] = GeometryManager.z0[superLayer][5][w]
			+ delz0;
		GeometryManager.x1[superLayer][7][w] = GeometryManager.x1[superLayer][5][w]
			+ delx1;
		GeometryManager.y1[superLayer][7][w] = GeometryManager.y1[superLayer][5][w]
			+ dely1;
		GeometryManager.z1[superLayer][7][w] = GeometryManager.z1[superLayer][5][w]
			+ delz1;
	    }
	}

    } // loadArrays
    
    
    /**
     * Get the intersections of a wire with a constant phi plane. If the wire does not
     * intersect (happens as phi grows) return null;
     * 
     * @param superlayer
     *            0..5
     * @param layer
     *            0..5
     * @param w 0..111
     * @param transform3D
     *            the transformation to the constant phi
     * @return the intersection points (z component will be 0).
     */
    public static Point3D getIntersection(int superlayer,
	    int layer, int w, Transformation3D transform3D) {
	
	DCSuperlayer sl = sector0.getSuperlayer(superlayer);
	DCLayer dcLayer = sl.getLayer(layer);
	DriftChamberWire dcw = dcLayer.getComponent(w);
	List<Line3D> lines = dcw.getVolumeCrossSection(transform3D);
	// perhaps no intersection

	if ((lines == null)  || (lines.size() < 1)) {
	    return null;
	}
	
//	System.err.println("number of lines: " + lines.size());
	
	Line3D line1 = lines.get(0);
	return line1.end();
    }

}
