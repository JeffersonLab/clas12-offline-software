/**
 * Trap.java
 *
 */
package org.jlab.geometry.prim;

import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Primitive;
import eu.mihosoft.vrl.v3d.PropertyStorage;
import eu.mihosoft.vrl.v3d.STL;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * An axis-aligned solid trapezoid defined by dimensions, inspired by G4Trap
 *
 * @author Andrey Kim;
 */
public class StlPrim implements Primitive {

    private final PropertyStorage properties = new PropertyStorage();

    private List<Polygon> polygons;
    
    public StlPrim(InputStream stlstream) {
        polygons = new ArrayList<>();
        
        try {
            polygons.addAll(STL.file(stlstream).getPolygons());
        } catch (IOException ex) {
            System.err.println("STL file is invalid");
        }
    }

    @Override
    public List<Polygon> toPolygons() {
        return polygons;
    }

    @Override
    public PropertyStorage getProperties() {
        return properties;
    }
}
