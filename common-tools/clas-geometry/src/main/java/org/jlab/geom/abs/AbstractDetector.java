package org.jlab.geom.abs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.geom.DetectorHit;
import org.jlab.geom.DetectorId;
import org.jlab.geom.base.Detector;
import org.jlab.geom.base.Sector;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Transformation3D;

/**
 * This class provides a skeletal implementation of the 
 * {@link org.jlab.geom.base.Detector} interface to minimize the effort 
 * required to implement a {@code Detector}.
 * <p>
 * To implement a {@code Detector}, the programmer needs only to extend this
 * class and provide implementations for the
 * {@link org.jlab.geom.base.Detector#getType()} method.
 * <p>
 * Initially the {@code Detector} will contain no {@code Sector}s, so
 * {@code Sector}s must be added to the {@code Detector} after
 * {@code AbstractDetector}'s constructor has been invoked via the
 * {@link #addSector(org.jlab.geom.base.Sector)} method.
 *
 * @author jnhankins
 * @param <SectorType> the specific type of {@code Sector} contained by the
 * {@code Detector}
 */
public abstract class AbstractDetector<SectorType extends Sector> implements Detector<SectorType> {
    private final DetectorId detectorId;

    private final Map<Integer, SectorType> sectorMap;
    private List<SectorType> sectorList;

    /**
     * Initializes an empty AbstractDetector with the given id.
     *
     * @param detectorId the id of this detector
     */
    protected AbstractDetector(DetectorId detectorId) {
        this.detectorId = detectorId;

        sectorMap = new HashMap();
        sectorList = Collections.unmodifiableList(new ArrayList());
    }

    @Override
    public final DetectorId getDetectorId() {
        return detectorId;
    }

    @Override
    public final int getNumSectors() {
        return sectorMap.size();
    }

    /**
     * Adds a sector to this detector. If a sector with the same id is contained
     * in this detector then the preexisting sector is replaced by the given
     * sector. Intended for use by detector factories.
     *
     * @param sector the sector to add
     */
    public final void addSector(SectorType sector) {
        if (sector == null) {
            throw new IllegalArgumentException("sector is null");
        }
        if (detectorId != sector.getDetectorId()) {
            throw new IllegalArgumentException("detectorIds do not match");
        }

        sectorMap.put(sector.getSectorId(), sector);
        List<SectorType> list = new ArrayList(sectorMap.values());
        Collections.sort(list, new Comparator<SectorType>() {
            @Override
            public int compare(SectorType a, SectorType b) {
                return a.getSectorId() - b.getSectorId();
            }
        });
        sectorList = Collections.unmodifiableList(list);
    }

    @Override
    public final SectorType getSector(int sectorId) {
        SectorType sector = sectorMap.get(sectorId);
        if (sector == null) {
            System.err.println("AbstractDetector: getSector(int sectorId): no such sector: sectorId=" + sectorId);
        }
        return sector;
    }

    @Override
    public final List<SectorType> getAllSectors() {
        return sectorList;
    }

    @Override
    public final List<List<Line3D>> getCrossSections(Transformation3D transform) {
        List<List<Line3D>> crosses = new ArrayList();
        for (SectorType sector : getAllSectors()) {
            crosses.addAll(sector.getCrossSections(transform));
        }
        return crosses;
    }

    @Override
    public List<DetectorHit> getLayerHits(Path3D path) {
        List<DetectorHit> hits = new ArrayList();
        for (SectorType sector : getAllSectors()) {
            hits.addAll(sector.getLayerHits(path));
        }
        return hits;
    }

    @Override
    public List<DetectorHit> getHits(Path3D path) {
        List<DetectorHit> hits = new ArrayList();
        for (SectorType sector : getAllSectors()) {
            hits.addAll(sector.getHits(path));
        }
        return hits;
    }

    @Override
    public void show() {
        System.out.println(this);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(String.format("%-15s\n", getType()));
        str.append(String.format("%-15s : %s\n", "Detector", detectorId));
        str.deleteCharAt(str.length() - 1);
        return str.toString();
    }
}
