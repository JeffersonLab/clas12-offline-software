package org.jlab.geom.detector.ec;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractLayer;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Triangle3D;
import org.jlab.geom.prim.Vector3D;

/**
 * An Electromagnetic Calorimeter (EC) {@link org.jlab.geom.base.Layer Layer}.
 * <p>
 * Factory: {@link org.jlab.geom.detector.ec.ECFactory ECFactory}<br> 
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.detector.ec.ECDetector ECDetector} → 
 * {@link org.jlab.geom.detector.ec.ECSector ECSector} → 
 * {@link org.jlab.geom.detector.ec.ECSuperlayer ECSuperlayer} → 
 * <b>{@link org.jlab.geom.detector.ec.ECLayer ECLayer}</b> → 
 * {@link org.jlab.geom.component.ScintillatorPaddle ScintillatorPaddle}
 * </code>
 * 
 * @author jnhankins
 */
public class ECLayer extends AbstractLayer<ScintillatorPaddle> {
    
    protected ECLayer(int sectorId, int superlayerId, int layerId) {
        super(DetectorId.EC, sectorId, superlayerId, layerId, true);
    }
    
    /**
     * Returns "EC Layer".
     * @return "EC Layer"
     */
    @Override
    public String getType() {
        return "EC Layer";
    }
    
    /**
     * Builds the contour of the layer surface as a Triangle3D
     * @return the layer contour
     */
    public Triangle3D getTrajectorySurface() {
        
        int nPaddle = this.getAllComponents().size();
        
        ScintillatorPaddle pad0 = this.getComponent(0);
        ScintillatorPaddle padn = this.getComponent(nPaddle-1);
        
        Line3D lineleft  = new Line3D(pad0.getLine().origin(), padn.getLine().origin());
        Line3D lineright = new Line3D(pad0.getLine().end(),    padn.getLine().end());
                
        Point3D p0 = lineleft.lerpPoint( (       -0.5)/(nPaddle-1));
        Point3D p1 = lineleft.lerpPoint( (nPaddle-0.5)/(nPaddle-1));
        Point3D p2 = lineright.lerpPoint((nPaddle-0.5)/(nPaddle-1));

        Triangle3D contour = new Triangle3D(p0, p1, p2);

        double offset = p0.vectorTo(this.getPlane().point()).dot(this.getPlane().normal());
        Vector3D dXYZ = this.getPlane().normal().clone().multiply(offset);
        contour.translateXYZ(dXYZ.x(), dXYZ.y(), dXYZ.z());
        
        return contour;
    } 
    
    /**
     * Returns the layer thickness
     * @return thickness
     */
    public double getThickness() {
        return this.getComponent(0).getVolumeEdge(1).length();
    }
}
