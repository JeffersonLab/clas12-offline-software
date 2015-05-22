package cnuphys.ced.geometry;

import org.jlab.clasrec.utils.DataBaseLoader;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.detector.dc.DCDetector;
import org.jlab.geom.detector.dc.DCFactory;
import org.jlab.geom.detector.dc.DCLayer;
import org.jlab.geom.detector.dc.DCSector;
import org.jlab.geom.detector.dc.DCSuperlayer;
import org.jlab.geom.prim.Line3D;

public class DCGeometry {

    public static void loadArrays() {

	ConstantProvider dcDataProvider = DataBaseLoader
		.getDriftChamberConstants();

	DCDetector dcDetector = (new DCFactory())
		.createDetectorCLAS(dcDataProvider);

	DCSector sector0 = dcDetector.getSector(0);

	// get the senswires from the geometry provider
	for (int superlayerId = 0; superlayerId < 6; superlayerId++) {
	    DCSuperlayer superlayer = sector0.getSuperlayer(superlayerId);
	    for (int layer = 0; layer < 6; layer++) {
		DCLayer dcLayer = superlayer.getLayer(layer);
		int numWire = dcLayer.getNumComponents();
		for (int w = 0; w < numWire; w++) {
		    Line3D wire = dcLayer.getComponent(w).getLine();

		    GeometryManager.x0[superlayerId][layer + 1][w + 1] = wire
			    .origin().x();
		    GeometryManager.y0[superlayerId][layer + 1][w + 1] = wire
			    .origin().y();
		    GeometryManager.z0[superlayerId][layer + 1][w + 1] = wire
			    .origin().z();
		    GeometryManager.x1[superlayerId][layer + 1][w + 1] = wire
			    .end().x();
		    GeometryManager.y1[superlayerId][layer + 1][w + 1] = wire
			    .end().y();
		    GeometryManager.z1[superlayerId][layer + 1][w + 1] = wire
			    .end().z();
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
}
