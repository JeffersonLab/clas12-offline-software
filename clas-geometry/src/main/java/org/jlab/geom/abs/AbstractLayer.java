package org.jlab.geom.abs;

import org.jlab.geom.base.Component;
import org.jlab.geom.base.Layer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.geom.DetectorHit;
import org.jlab.geom.DetectorId;
import org.jlab.geom.prim.Face3D;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Shape3D;
import org.jlab.geom.prim.Transformation3D;

/**
 * This class provides a skeletal implementation of the
 * {@link org.jlab.geom.base.Layer} interface to minimize the effort required to
 * implement a {@code Layer}.
 * <p>
 * To implement a {@code Layer}, the programmer needs only to extend this class
 * and provide implementations for the
 * {@link org.jlab.geom.base.Layer#getType()} method.
 * <p>
 * Initially the {@code Layer} will contain no {@code Component}s, so
 * {@code Component}s must be added to the {@code Layer} after
 * {@code AbstractLayer}'s constructor has been invoked via the
 * {@link #addComponent(org.jlab.geom.base.Component)} method.
 * <p>
 * To set the boundary, use {@link #getBoundary()} to retrieve the point and
 * modify it accordingly. Similarly, to set the plane, use {@link #getPlane()}.
 * <p>
 * If the subtyped {@code Layer} contains additional geometry data, then, to
 * ensure that the additional geometry is properly rotated and translated with
 * the {@code Layer}, the programmer must also override
 * {@link #onSetTransformation(org.jlab.geom.prim.Transformation3D)} which is
 * invoked after {@link #setTransformation(org.jlab.geom.prim.Transformation3D)}
 * is invoked.
 * <p>
 * {@code AbstractLayer}'s constructor takes a boolean argument
 * {@code useBoundaryAsHitFilter}, which, if true, causes the
 * {@link #getLayerHits(org.jlab.geom.prim.Path3D)} and
 * {@link #getHits(org.jlab.geom.prim.Path3D)} methods to return empty lists if
 * the {@code Path3D} does not intersects any faces in this {@code Layer}'s
 * {@link #getBoundary() boundary}. This can be useful for performance
 * optimization or used to simulate dead zones between the layer's sensing
 * component's active regions.
 * <p>
 * {@code AbstractLayer} provides implementations for
 * {@code getLayerHits(Path3D)} and {@code getHits(Path3D)} which test for
 * intersections with the layer boundary and component volume shapes
 * respectively. However, programmers may in many times need to override these
 * methods based on the requirements of the type of detector this type of layer
 * belongs to. Further, the these implementations are designed to be general
 * enough to handle several types of detectors and components. Consequently,
 * they do not contain optimizations that may be made available by the specific
 * geometry of a specific detector or component type.
 *
 * @author jhankins
 * @param <ComponentType> the specific type of {@code Component} contained by 
 * the {@code Layer}
 */
public abstract class AbstractLayer<ComponentType extends Component> implements Layer<ComponentType> {
    private final DetectorId detectorId;
    private final int sectorId;
    private final int superlayerId;
    private final int layerId;
    
    private final Map<Integer, ComponentType> componentMap;
    private List<ComponentType> componentList;
    
    private final Transformation3D transform;
    private final Shape3D boundary;
    private final Plane3D plane;
    private final boolean useBoundaryAsHitFilter;
    
    /**
     * Initializes an empty AbstractLayer with the given id.
     * @param detectorId the id of this layer's detector
     * @param sectorId the id of this layer's sector
     * @param superlayerId the id of this layer's superlayer
     * @param layerId the id of this layer
     * @param useBoundaryAsHitFilter if true, the 
     * {@link #getLayerHits(org.jlab.geom.prim.Path3D)} and 
     * {@link #getHits(org.jlab.geom.prim.Path3D)} methods will return empty 
     * lists if the {@code Path3D} does not intersects any faces in this 
     * {@code Layer}'s {@link #getBoundary() boundary}.  This can be useful for
     * performance optimization or used to simulate dead zones between the
     * layer's sensing component's active regions.
     */
    protected AbstractLayer(DetectorId detectorId, int sectorId, int superlayerId, int layerId, boolean useBoundaryAsHitFilter) {
        this.detectorId   = detectorId;
        this.sectorId     = sectorId;
        this.superlayerId = superlayerId;
        this.layerId      = layerId;
        
        componentMap  = new HashMap();
        componentList = Collections.unmodifiableList(new ArrayList());
        
        transform  = new Transformation3D();
        boundary   = new Shape3D();
        plane      = new Plane3D(0, 0, 0, 0, 0, 0);
        this.useBoundaryAsHitFilter = useBoundaryAsHitFilter;
    }
    
    @Override
    public final DetectorId getDetectorId() {
        return detectorId;
    }
    
    @Override
    public final int getSectorId() {
        return sectorId;
    }
    
    @Override
    public final int getSuperlayerId() {
        return superlayerId;
    }
    
    @Override
    public final int getLayerId() {
        return layerId;
    }

    @Override
    public final int getNumComponents() {
        return componentMap.size();
    }
    
    /**
     * Adds a component to this layer. If a component with the same id is
     * contained in this layer then the preexisting component is replaced by the
     * given component. Intended for use by detector factories.
     * @param component the component to add
     */
    public final void addComponent(ComponentType component) {
        if (component == null)
            throw new IllegalArgumentException("component is null");
        
        componentMap.put(component.getComponentId(), component);
        List<ComponentType> list = new ArrayList(componentMap.values());
        Collections.sort(list, new Comparator<ComponentType>() {
            @Override
            public int compare(ComponentType a, ComponentType b) {
                return a.getComponentId() - b.getComponentId();
            }
        });
        componentList = Collections.unmodifiableList(list);
    }
    
    @Override
    public final ComponentType getComponent(int componentId) {
        ComponentType component = componentMap.get(componentId);
        if (component == null)
            System.err.println("AbstractLayer: getComponent(int componentId): no such component: componentId="+componentId);
        return component;
    }
    
    @Override
    public final List<ComponentType> getAllComponents() {
        return componentList;
    }
    
    @Override
    public final Shape3D getBoundary() {
        return boundary;
    }
    
    @Override
    public final Plane3D getPlane() {
        return plane;
    }
    
    @Override
    public final Transformation3D getTransformation() {
        return new Transformation3D(transform);
    }
    
    @Override
    public final void setTransformation(Transformation3D transform) {
        if (transform == null) {
            System.out.println("AbstractLayer: setTransformation(Transformation3D transform): transform is null");
            return;
        }
        Transformation3D xform = this.transform.inverse();
        xform.append(transform);
        for (Component component : componentList)
            xform.apply(component);
        xform.apply(boundary);
        xform.apply(plane);
        this.transform.copy(transform);
        onSetTransformation(xform);
    }
    
    /**
     * Classes extending AbstractLayer should implement onSetTransformation so
     * that any additional geometric data they contain is transformed
     * appropriately when AbstractLayer's onSetTransformation method is invoked.
     * To implement this method simply apply the given transformation on all of
     * the transformable contained in the layer that are not managed by
     * AbstractLayer. This method is called at the end of AbstractLayer's
     * setTransformation method.
     * @param transform the full transformation from the old coordinate system 
     * to the new coordinate system
     * @see org.jlab.geom.base.Layer#setTransformation(org.jlab.geom.prim.Transformation3D) 
     */
    protected void onSetTransformation(Transformation3D transform) {}
    
    @Override
    public final List<List<Line3D>> getCrossSections(Transformation3D transform) {
        List<List<Line3D>> crosses = new ArrayList();
        for (ComponentType component : getAllComponents()) {
            List<Line3D> poly = component.getVolumeCrossSection(transform);
            if (!poly.isEmpty()) {
                crosses.add(poly);
            }
        }
        return crosses;
    }
    
    @Override
    public List<DetectorHit> getLayerHits(Path3D path) {
        List<DetectorHit> hitList = new ArrayList();
        
        // For each line in the path
        List<Point3D> intersections = new ArrayList();
        lineLoop: for(int i=0; i<path.size()-1; i++) {
            Line3D line = path.getLine(i);
            if (boundary.intersectionSegment(line, intersections) > 0) {
                hitList.add(new DetectorHit(
                    detectorId,
                    sectorId,
                    superlayerId,
                    layerId,
                    -1,
                    intersections.get(0)));
                return hitList;
            }
        }
        return hitList;
    }
    
    @Override
    public List<DetectorHit> getHits(Path3D path) {
        List<DetectorHit> hitList = new ArrayList();
        Point3D hitPosition0 = new Point3D();
        Point3D hitPosition1 = new Point3D();
        
        // For each line in the path
        for(int i=0; i<path.size()-1; i++) {
            Line3D line = path.getLine(i);
            
            // Check to see if the boundary was hit
            if (!useBoundaryAsHitFilter || boundary.hasIntersectionSegment(line)) {
                // For each paddle
                for (Component component : getAllComponents()) {
                    // Find the paddle that the line hits
                    if (component.getVolumeIntersection(line, hitPosition0, hitPosition1)) {
                        hitList.add(new DetectorHit(
                            detectorId,
                            sectorId,
                            superlayerId,
                            layerId,
                            component.getComponentId(),
                            hitPosition0));
                        hitList.add(new DetectorHit(
                            detectorId,
                            sectorId,
                            superlayerId,
                            layerId,
                            component.getComponentId(),
                            hitPosition1));
                        return hitList;
                    }
                }
                return hitList;
            }
        }
        return hitList;
    }
    
    @Override
    public void show() {
        System.out.print(this);
    }
    
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(String.format("%-15s\n", getType()));
        str.append(String.format("%-15s : %s\n", "Detector",    detectorId));
        str.append(String.format("%-15s : %d\n", "Sector",      sectorId));
        str.append(String.format("%-15s : %d\n", "Superlayer",  superlayerId));
        str.append(String.format("%-15s : %d\n", "Layer",       layerId));
        for (int f=0; f<boundary.size(); f++) {
            Face3D face = boundary.face(f);
            str.append(String.format("%-15s :", "Face3D #"+f));
            str.append(String.format(" (%7.1f %7.1f %7.1f)", 
                    face.point(0).x(),
                    face.point(0).y(),
                    face.point(0).z()));
            str.append(String.format(" (%7.1f %7.1f %7.1f)", 
                    face.point(1).x(),
                    face.point(1).y(),
                    face.point(1).z()));
            str.append(String.format(" (%7.1f %7.1f %7.1f)", 
                    face.point(2).x(),
                    face.point(2).y(),
                    face.point(2).z()));
        }
        return str.toString();
    }
}
