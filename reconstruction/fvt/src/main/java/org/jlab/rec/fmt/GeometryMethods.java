package org.jlab.rec.fmt;

import org.jlab.geom.prim.Point3D;

// NOTE: Layers are counter from 0 onward, so first layer would be 0, second would be 1, etc...

public class GeometryMethods{
    public GeometryMethods(){}

    /**
     * Transform a Point3D from global to a FMT layer's local coordinates.
     */
    public static Point3D globalToLocal(Point3D glPos, int layer) {
        double x = glPos.x() * Math.cos(Constants.FVT_Alpha[layer])
                 + glPos.y() * Math.sin(Constants.FVT_Alpha[layer]);
        double y = glPos.y() * Math.cos(Constants.FVT_Alpha[layer])
                 - glPos.x() * Math.sin(Constants.FVT_Alpha[layer]);
        double z = glPos.z();

        return new Point3D(x,y,z);
    }
}
