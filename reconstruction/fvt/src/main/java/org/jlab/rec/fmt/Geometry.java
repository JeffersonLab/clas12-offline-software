package org.jlab.rec.fmt;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Line3D;

// NOTE: Layers are counter from 0 onward, so first layer would be 0, second would be 1, etc...

public class Geometry {
	public Geometry() {}

	public static Point3D getStripsIntersection(
			double x0_inner, double x1_inner, double x0_outer, double x1_outer,
			double y0_inner, double y1_inner, double y0_outer, double y1_outer,
			double z0_inner, double z0_outer) {

		Line3D l_in  = new Line3D(x0_inner, y0_inner, z0_inner, x1_inner, y1_inner, z0_inner);
        Line3D l_out = new Line3D(x0_outer, y0_outer, z0_outer, x1_outer, y1_outer, z0_outer);

        return l_in.distance(l_out).midpoint();
	}

	/**
	 * Transform a Point3D from global to a FMT layer's local coordinates, applying the x-y alignment
	 * shifts in the process.
	 * @param glPos: Point3D describing the position to be transformed in lab coordinates.
	 * @param layer: Target FMT layer.
	 */
	public static Point3D globalToLocal(Point3D glPos, int layer) {
		// TODO: Apply x and y rot.
		double x = (glPos.x() - Constants.FVT_xShift[layer-1]) * Math.cos(Constants.FVT_Alpha[layer-1])
				 + (glPos.y() - Constants.FVT_yShift[layer-1]) * Math.sin(Constants.FVT_Alpha[layer-1]);
		double y = (glPos.y() - Constants.FVT_yShift[layer-1]) * Math.cos(Constants.FVT_Alpha[layer-1])
				 - (glPos.x() - Constants.FVT_xShift[layer-1]) * Math.sin(Constants.FVT_Alpha[layer-1]);
		double z = glPos.z();

		return new Point3D(x,y,z);
	}

	/**
     * Return the z coordinate of a layer + half the thickness of the drift region.
     * @param layer: Target FMT layer.
     */
    public static double getLayerZ(int layer) {
        return Constants.FVT_Zlayer[layer] + Constants.hDrift/2.;
    }
}
