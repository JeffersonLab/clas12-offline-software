package org.jlab.geom;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

/**
 * Representation of an intersection between a particle's path and a sensing 
 * component.  When this intersection corresponds to corresponds to an event
 * which would cause the sensor the produce a signal that triggers the sensors
 * discriminator mechanism, this intersection is referred to as a "hit". Though
 * {@code DetectorHit} can be used to store a hits, {@code DetectorHit} is can
 * be used more generally to associate any intersection point with detector,
 * sector, superlayer, layer, and, optionally, component id. For example,
 * {@code DetectorHit} may be used to store outgoing intersections which would
 * not trigger a discriminator.
 * <p>
 * Additionally, {@code DetectorHit} can, optionally, associate a hit with an
 * error measurement ({@link #getError()}), energy ({@link #getEnergy() }, 
 * {@link #setEnergy(double)}), and time ({@link #getTime()}, 
 * {@link #setTime(double)}).
 * <p>
 * The detector, superlayer, layer, and component id's are immutable, however
 * the hit position and error can be retrieved and modified via 
 * {@link #getPosition()} and {@link #getError()}.
 *
 * @see org.jlab.geom.base.Layer#getHits(org.jlab.geom.prim.Path3D) 
 * @see org.jlab.geom.base.Layer#getLayerHits(org.jlab.geom.prim.Path3D) 
 * @author jnhankins
 */
public class DetectorHit implements Showable {
    /**
     * Used for debugging, if PRINTMODE_ONELINE is true then DetectorHit's 
     * toString() method is shortened to fit on one line.
     */
    public static boolean PRINTMODE_ONELINE = true;
    
    private final DetectorId detectorId; // id of the detector containing this hit
    private final int sectorId;          // id of the sector containing this hit
    private final int superlayerId;      // id of the superlayer containing this hit
    private final int layerId;           // id of the layer containing this hit
    private final int componentId;       // id of the component closest to this hit
    
    private final Point3D hitPosition;   // position of the hit
    private final Vector3D hitDirection;     // uncertainty in the hit position
    private final Vector3D hitError;     // uncertainty in the hit position
    private double hitEnergy = 0.0;      // energy deposited by the hit
    private double hitTime   = 0.0;      // the time of the hit;
    
    /**
     * Constructs a new {@code DetectorHit} at the given position with the
     * detector, sector, superlayer, layer, and component ids all set to 0.
     * @param hitPosition  the position of the hit
     * @param detectorId   the id of the detector containing this hit
     * @param sectorId     the id of the sector containing this hit
     * @param superlayerId the id of the superlayer containing this hit
     * @param layerId      the id of the layer containing this hit
     * @param componentId  the id of the component closest to this hit
     */
    public DetectorHit(
            DetectorId detectorId,
            int sectorId,
            int superlayerId,
            int layerId,
            int componentId,
            Point3D hitPosition) {
        this.detectorId   = detectorId;
        this.sectorId     = sectorId;
        this.superlayerId = superlayerId;
        this.layerId      = layerId;
        this.componentId     = componentId;
        this.hitPosition  = new Point3D(hitPosition);
        this.hitError     = new Vector3D();
        this.hitDirection = new Vector3D();
        hitPosition.copy(hitPosition);
    }
    
    public DetectorHit(
            DetectorId detectorId,
            int sectorId,
            int superlayerId,
            int layerId,
            int componentId,
            Point3D hitPosition, Vector3D direction) {
        this.detectorId   = detectorId;
        this.sectorId     = sectorId;
        this.superlayerId = superlayerId;
        this.layerId      = layerId;
        this.componentId     = componentId;
        this.hitPosition  = new Point3D(hitPosition);
        this.hitError     = new Vector3D();
        this.hitDirection = new Vector3D();
        hitPosition.copy(hitPosition);
        hitDirection.copy(direction);
    }
    
    /**
     * Returns the detector id of this hit.
     * @return the detector id
     */
    public DetectorId getDetectorId() {
        return detectorId;
    }
    
    /**
     * Returns the sector id of this hit.
     * @return the sector id
     */
    public int getSectorId() {
        return sectorId;
    }
    
    /**
     * Returns the superlayer id of this hit.
     * @return the superlayer id
     */
    public int getSuperlayerId() {
        return superlayerId;
    }
    
    /**
     * Returns the layer id of this hit.
     * @return the layer id
     */
    public int getLayerId() {
        return layerId;
    }
    
    /**
     * Returns the component id of this hit or -1 if the specific component
     * is not known.
     * @return the component id or -1
     */
    public int getComponentId() {
        return componentId;
    }
    
    /**
     * Returns the position of this hit.
     * @return the position of this hit
     */
    public Point3D getPosition() {
        return hitPosition;        
    }
    
    /**
     * Returns the position of this hit.
     * @return the position of this hit
     */
    public Vector3D getError() {
        return hitError;        
    }
    
    /**
     * Returns the amount of energy in this hit or 0 if the energy has not yet
     * been set.
     * @return the energy or 0
     */
    public double getEnergy() {
        return hitEnergy;
    }
    
    /**
     * Returns the time that this hit occurred or 0 if the time has not yet been
     * set.
     * @return the time or 0
     */
    public double getTime() {
        return hitTime;
    }
    
    /**
     * Sets the amount of energy deposited by this hit.
     * @param e the energy
     */
    public void setEnergy(double e){
        hitEnergy = e;
    }
    
    /**
     * Sets the time that this hit occurred.
     * @param t the time
     */
    public void setTime(double t){
        hitTime = t;
    }
    
    /**
     * Invokes {@code System.out.println(this)}.
     */
    public void show() {
        System.out.println(this);
    }
    
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        if(PRINTMODE_ONELINE){
         str.append(String.format("HIT : %8s %4d %4d %4d %4d ", 
                 detectorId.getName(),sectorId,superlayerId,layerId,componentId));
         str.append(String.format(" (%12.5f,%12.5f)  XYZ = %12.5f %12.5f %12.5f", 
                 hitTime,hitEnergy,hitPosition.x(),hitPosition.y(),hitPosition.z()));
        } else {
            str.append(String.format("%-15s : %s\n", "Detector",    detectorId.getName()));
            str.append(String.format("%-15s : %d\n", "Detector ID", detectorId.getIdNumber()));
            str.append(String.format("%-15s : %d\n", "Sector",      sectorId));
            str.append(String.format("%-15s : %d\n", "Superlayer",  superlayerId));
            str.append(String.format("%-15s : %d\n", "Layer",       layerId));
            str.append(String.format("%-15s : %d\n", "Component",      componentId));
            str.append(String.format("%-15s : %12.5f\n", "Time (ns)",      hitTime));
            str.append(String.format("%-15s : %12.5f\n", "Energy (GeV)",      hitEnergy));
            str.append(String.format("%-15s : %12.5f %12.5f %12.5f\n", "Position",
                    hitPosition.x(), hitPosition.y(), hitPosition.z()));
            str.append(String.format("%-15s : %12.5f %12.5f %12.5f\n", "Error",
                    hitError.x(), hitError.y(), hitError.z()));
            str.deleteCharAt(str.length()-1);
        }
        return str.toString();
    }
}
