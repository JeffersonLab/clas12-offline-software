package org.jlab.geom.component;

import java.util.List;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;

/**
 * A drift chamber wire.
 * <p>
 * A {@code Line3D} representing the sense wire itself can be obtained via 
 * {@link #getLine()}.  The {@code Point3D} at the intersection of the wire and
 * the {@link org.jlab.geom.detector.dc.DCLayer#getMidplane() midplane} of the
 * drift chamber can be obtained via {@link #getMidpoint()}.
 * <p>
 * The {@link #getVolumeShape() volume} of a {@code DriftChamberWire} is the
 * region enclosed by the six field wires surrounding the sense wire.  
 * Consequently, this volume is a hexagonal volume bounded by two planes. 
 * {@code Line3D} objects representing the field wires can be obtained via
 * {@link #getVolumeEdge(int) getVolumeEdge(int index)} where {@code index} 
 * is in the range [12, 17].
 * <p>
 * For convenience, all {@code DriftChamberWire}s should be constructed such
 * that the {@link org.jlab.geom.prim.Line3D#toVector() direction} of the wire's
 * {@link #getLine() line} and the direction from the first hexagon to the 
 * second hexagon given in the constructor point clockwise aground the z-axis 
 * in any of the standard coordinate systems (for information about coordinate
 * systems see {@link org.jlab.geom.base.Factory}).
 * <p>
 * For more information see {@link org.jlab.geom.component.PrismaticComponent}.
 * 
 * @author jnhankins
 */
public class DriftChamberWire extends PrismaticComponent {
    
    /**
     * Constructs a new {@code DriftChamberWire} from the given parameters.
     * <p>
     * Though not strictly required, for convenience, all
     * {@code DriftChamberWire}s should be constructed such that the
     * {@link org.jlab.geom.prim.Line3D#toVector() direction} of the wire's
     * {@link #getLine() line} and the direction from the first hexagon to the
     * second hexagon given in the constructor point clockwise aground the
     * z-axis in any of the standard coordinate systems (for information about
     * coordinate systems see {@link org.jlab.geom.base.Factory}).
     *
     * @param componentId the component id
     * @param midpoint the {@link #getMidpoint() midpoint} of the wire
     * @param line the {@link #getLine() line} representing the wire
     * @param flipReadoutDirection if true the direction will point from
     * {@code topHex} to {@code botHex} instead of from {@code botHex} to
     * {@code topHex}
     * @param botHex the first convex hexagon whose points represent the origin
     * points of the field wires
     * @param topHex the second convex hexagon whose points represent the end
     * points of the field wires
     */
    public DriftChamberWire(int componentId, Point3D midpoint, Line3D line, 
            boolean flipReadoutDirection, List<Point3D> botHex, List<Point3D> topHex) {
        super(componentId, botHex, topHex);
        
        if (botHex.size() != 6)
            throw new IllegalArgumentException("boxHex.size() != 6");
        
        getMidpoint().copy(midpoint);
        getLine().copy(line);
        if (flipReadoutDirection) {
            getDirection().negative();
        }
    }
    
    /**
     * Returns "Drift Chamber Wire".
     * @return "Drift Chamber Wire"
     */
    @Override
    public String getType() {
        return "Drift Chamber Wire";
    }
}
