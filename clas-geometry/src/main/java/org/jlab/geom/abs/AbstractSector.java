package org.jlab.geom.abs;

import org.jlab.geom.base.Sector;
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
 * {@link org.jlab.geom.base.Sector} interface to minimize the effort
 * required to implement a {@code Sector}.
 * <p>
 * To implement a {@code Sector}, the programmer needs only to extend this class
 * and provide implementations for the
 * {@link org.jlab.geom.base.Sector#getType()} method.
 * <p>
 * Initially the {@code Sector} will contain no {@code Superlayer}s, so
 * {@code Superlayer}s must be added to the {@code Sector} after
 * {@code AbstractSector}'s constructor has been invoked via the
 * {@link #addSuperlayer(org.jlab.geom.base.Superlayer)} method.
 *
 * @author jnhankins
 * @param <SuperlayerType> the specific type of {@code Superlayer} contained by 
 * the {@code Sector}
 */
public abstract class AbstractSector<SuperlayerType extends Superlayer> implements Sector<SuperlayerType> {
    private final DetectorId detectorId;
    private final int sectorId;
    
    private final Map<Integer, SuperlayerType> superlayerMap;
    private List<SuperlayerType> superlayerList;
    
    /**
     * Initializes an empty AbstractSector with the given id.
     * @param detectorId the id of this sector's detector
     * @param sectorId the id of this sector
     */
    protected AbstractSector(DetectorId detectorId, int sectorId) {
        this.detectorId   = detectorId;
        this.sectorId     = sectorId;
        
        superlayerMap  = new HashMap();
        superlayerList = Collections.unmodifiableList(new ArrayList());
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
    public final int getNumSuperlayers() {
        return superlayerMap.size();
    }
    
    /**
     * Adds a superlayer to this sector. If a superlayer with the same id is
     * contained in this sector then the preexisting superlayer is replaced by
     * the given superlayer. Intended for use by detector factories.
     * @param superlayer the superlayer to add
     */
    public final void addSuperlayer(SuperlayerType superlayer) {
        if (superlayer == null)
            throw new IllegalArgumentException("superlayer is null");
        if (detectorId != superlayer.getDetectorId())
            throw new IllegalArgumentException("detectorIds do not match");
        if (sectorId != superlayer.getSectorId())
            throw new IllegalArgumentException("sectorIds do not match");
        
        superlayerMap.put(superlayer.getSuperlayerId(), superlayer);
        List<SuperlayerType> list = new ArrayList(superlayerMap.values());
        Collections.sort(list, new Comparator<SuperlayerType>() {
            @Override
            public int compare(SuperlayerType a, SuperlayerType b) {
                return a.getSuperlayerId() - b.getSuperlayerId();
            }
        });
        superlayerList = Collections.unmodifiableList(list);
    }

    @Override
    public final SuperlayerType getSuperlayer(int superlayerId) {
        SuperlayerType superlayer = superlayerMap.get(superlayerId);
        if (superlayer == null)
            System.err.println("AbstractSector: getSuperlayer(int superlayerId): no such superlayer: superlayerId="+superlayerId);
        return superlayer;
    }
    
    @Override
    public final List<SuperlayerType> getAllSuperlayers() {
        return superlayerList;
    }
    
    @Override
    public final List<List<Line3D>> getCrossSections(Transformation3D transform) {
        List<List<Line3D>> crosses = new ArrayList();
        for (SuperlayerType superlayer : getAllSuperlayers())
            crosses.addAll(superlayer.getCrossSections(transform));
        return crosses;
    }
    
    @Override
    public List<DetectorHit> getLayerHits(Path3D path) {
        List<DetectorHit> hits = new ArrayList();
        for (SuperlayerType superlayer : getAllSuperlayers())
            hits.addAll(superlayer.getLayerHits(path));
        return hits;
    }
    
    @Override
    public List<DetectorHit> getHits(Path3D path) {
        List<DetectorHit> hits = new ArrayList();
        for (SuperlayerType superlayer : getAllSuperlayers())
            hits.addAll(superlayer.getHits(path));
        return hits;
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
        str.deleteCharAt(str.length()-1);
        return str.toString();
    }
}
