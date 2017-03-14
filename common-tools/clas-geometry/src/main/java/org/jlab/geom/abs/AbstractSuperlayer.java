package org.jlab.geom.abs;

import org.jlab.geom.base.Layer;
import org.jlab.geom.base.Superlayer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.geom.DetectorHit;
import org.jlab.geom.DetectorId;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Transformation3D;

/**
 * This class provides a skeletal implementation of the
 * {@link org.jlab.geom.base.Superlayer} interface to minimize the effort
 * required to implement a {@code Superlayer}.
 * <p>
 * To implement a {@code Superlayer}, the programmer needs only to extend this
 * class and provide implementations for the
 * {@link org.jlab.geom.base.Superlayer#getType()} method.
 * <p>
 * Initially the {@code Superlayer} will contain no {@code Layer}s, so
 * {@code Layer}s must be added to the {@code Superlayer} after
 * {@code AbstractSuperlayer}'s constructor has been invoked via the
 * {@link #addLayer(org.jlab.geom.base.Layer)} method.
 *
 * @author jnhankins
 * @param <LayerType> the specific type of {@code Layer} contained by the 
 * {@code Superlayer}
 */
public abstract class AbstractSuperlayer<LayerType extends Layer> implements Superlayer<LayerType> {
    private final DetectorId detectorId;
    private final int sectorId;
    private final int superlayerId;
    
    private final Map<Integer, LayerType> layerMap;
    private List<LayerType> layerList;
    
    /**
     * Initializes an empty AbstractSuperlayer with the given id.
     * @param detectorId the id of this superlayer's detector
     * @param sectorId the id of this superlayer's sector
     * @param superlayerId the id of this superlayer
     */
    protected AbstractSuperlayer(DetectorId detectorId, int sectorId, int superlayerId) {
        this.detectorId   = detectorId;
        this.sectorId     = sectorId;
        this.superlayerId = superlayerId;
        
        layerMap  = new HashMap();
        layerList = Collections.unmodifiableList(new ArrayList());
    }

    @Override
    public DetectorId getDetectorId() {
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
    public final int getNumLayers() {
        return layerMap.size();
    }
    
    /**
     * Adds a layer to this superlayer. If a layer with the same id is contained
     * in this superlayer then the preexisting layer is replaced by the given
     * layer. Intended for use by detector factories.
     * @param layer the layer to add
     */
    public final void addLayer(LayerType layer) {
        if (layer == null)
            throw new IllegalArgumentException("component is null");
        if (detectorId != layer.getDetectorId())
            throw new IllegalArgumentException("detectorIds do not match");
        if (sectorId != layer.getSectorId())
            throw new IllegalArgumentException("sectorIds do not match");
        if (superlayerId != layer.getSuperlayerId())
            throw new IllegalArgumentException("superlayerIds do not match");
        
        layerMap.put(layer.getLayerId(), layer);
        List<LayerType> list = new ArrayList(layerMap.values());
        Collections.sort(list, new Comparator<LayerType>() {
            @Override
            public int compare(LayerType a, LayerType b) {
                return a.getLayerId() - b.getLayerId();
            }
        });
        layerList = Collections.unmodifiableList(list);
    }
    
    @Override
    public final LayerType getLayer(int layerId) {
        LayerType layer = layerMap.get(layerId);
        if (layer == null)
            System.err.println("AbstractSuperlayer: getLayer(int layerId): no such layer: layerId="+layerId);
        return layer;
    }
    
    @Override
    public final List<LayerType> getAllLayers() {
        return layerList;
    }
    
    @Override
    public final List<List<Line3D>> getCrossSections(Transformation3D transform) {
        List<List<Line3D>> crosses = new ArrayList();
        for (LayerType layer : getAllLayers())
            crosses.addAll(layer.getCrossSections(transform));
        return crosses;
    }
    
    @Override
    public List<DetectorHit> getLayerHits(Path3D path) {
        List<DetectorHit> hits = new ArrayList();
        for (LayerType layer : getAllLayers())
            hits.addAll(layer.getLayerHits(path));
        return hits;
    }
    
    @Override
    public List<DetectorHit> getHits(Path3D path) {
        List<DetectorHit> hits = new ArrayList();
        for (LayerType layer : getAllLayers())
            hits.addAll(layer.getHits(path));
        return hits;
        
    }
    
    @Override
    public final void setTransformation(Transformation3D transform) {
        if (transform == null)
           throw new IllegalArgumentException("transform is null");
        
        for (LayerType layer : layerList) {
            layer.setTransformation(transform);
        }
    }
    
    @Override
    public void show() {
        System.out.println(this);
    }
    
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(String.format("%-15s\n", getType()));
        str.append(String.format("%-15s : %s\n", "Detector",    detectorId));
        str.append(String.format("%-15s : %d\n", "Sector",      sectorId));
        str.append(String.format("%-15s : %d\n", "Superlayer",  superlayerId));
        str.deleteCharAt(str.length()-1);
        return str.toString();
    }
}
