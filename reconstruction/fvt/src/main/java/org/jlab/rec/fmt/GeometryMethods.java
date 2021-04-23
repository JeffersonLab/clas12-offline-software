package org.jlab.rec.fmt;

import org.jlab.geom.prim.Point3D;

// NOTE: Layers are counter from 0 onward, so first layer would be 0, second would be 1, etc...

public class GeometryMethods{
    public GeometryMethods(){}

    /**
     * Transform a Point3D from global to a FMT layer's local coordinates, applying the x-y alignment
     * shifts in the process.
     * @param glPos: Point3D describing the position to be transformed in lab coordinates.
     * @param layer: Target FMT layer.
     */
    public static Point3D globalToLocal(Point3D glPos, int layer) {
        // TODO: Apply x and y rot.
        double x = (glPos.x() + Constants.FVT_xShift[layer]) * Math.cos(Constants.FVT_Alpha[layer])
                 + (glPos.y() + Constants.FVT_yShift[layer]) * Math.sin(Constants.FVT_Alpha[layer]);
        double y = (glPos.y() + Constants.FVT_yShift[layer]) * Math.cos(Constants.FVT_Alpha[layer])
                 - (glPos.x() + Constants.FVT_xShift[layer]) * Math.sin(Constants.FVT_Alpha[layer]);
        double z = glPos.z();

        return new Point3D(x,y,z);
    }

    /**
     * Transform a Point3D from global to a FMT layer's local coordinates without applying the x-y
     * alignment shifts.
     * @param glPos: Point3D describing the position to be transformed in lab coordinates.
     * @param layer: Target FMT layer.
     */
    public static Point3D globalToLocalNoShift(Point3D glPos, int layer) {
        // TODO: Apply x and y rot.
        double x = (glPos.x()) * Math.cos(Constants.FVT_Alpha[layer])
                 + (glPos.y()) * Math.sin(Constants.FVT_Alpha[layer]);
        double y = (glPos.y()) * Math.cos(Constants.FVT_Alpha[layer])
                 - (glPos.x()) * Math.sin(Constants.FVT_Alpha[layer]);
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
