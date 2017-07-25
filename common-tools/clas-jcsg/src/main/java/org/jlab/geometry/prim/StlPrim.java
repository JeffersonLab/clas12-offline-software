/**
 * Trap.java
 *
 */
package org.jlab.geometry.prim;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Primitive;
import eu.mihosoft.vrl.v3d.PropertyStorage;
import eu.mihosoft.vrl.v3d.STL;
import eu.mihosoft.vrl.v3d.Transform;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * primitive volume loaded from STL file
 *
 * @author Andrey Kim;
 */
public class StlPrim implements Primitive {

    private final PropertyStorage properties = new PropertyStorage();

    private List<Polygon> polygons;
    ByteArrayOutputStream stlBuffer;

    public StlPrim(InputStream stlstream, double scaleFactor) {
        polygons = new ArrayList<>();

        stlBuffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        try {
            while ((nRead = stlstream.read(data, 0, data.length)) != -1) {
                stlBuffer.write(data, 0, nRead);
            }

            stlBuffer.flush();

            CSG stlCSG = STL.file(new ByteArrayInputStream(stlBuffer.toByteArray()));
            stlCSG.transform(Transform.unity().scale(scaleFactor));
            polygons.addAll(stlCSG.getPolygons());
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

    public void copyToStlFile(String fname) {
        try (FileOutputStream outputStream = new FileOutputStream(fname)) {
            stlBuffer.writeTo(outputStream);
        } catch (IOException ex) {
            Logger.getLogger(StlPrim.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
