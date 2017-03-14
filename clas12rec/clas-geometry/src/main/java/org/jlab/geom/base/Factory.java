package org.jlab.geom.base;

import org.jlab.geom.Showable;
import org.jlab.geom.prim.Transformation3D;

/**
 * A set of functions to create individual
 * {@link org.jlab.geom.base.Detector Detector},
 * {@link org.jlab.geom.base.Sector Sector},
 * {@link org.jlab.geom.base.Superlayer Superlayer}, or
 * {@link org.jlab.geom.base.Layer Layer} objects for a specific type of 
 * detector using the factory method pattern.
 * <p>
 * Detectors are constructed entirely from scratch using using as little hard
 * coded information (primarily equations). Instead, detectors and their 
 * various components are constructed using a few important constants provided
 * by a {@link org.jlab.geom.base.ConstantProvider}.
 * <p>
 * Detectors can be produced in four different coordinate systems via four
 * different methods:
 * <ol>
 * <li>
 * CLAS Coordinates: {@link #createDetectorCLAS(ConstantProvider)}
 * <ul>
 * <li>Detectors created in CLAS coordinates are in their final nominal
 * positions</li>
 * <li>z-axis: parallel to the beam, positive direction is down beam</li>
 * <li>y-axis: anti-parallel to gravity, positive direction is "up"</li>
 * <li>x-axis: forms a right-handed coordinate system, positive is left</li>
 * <li>To Sector: Rotate each sector CCW around the z-axis by sectorId·60° to φ
 * = 0.</li>
 * </ul>
 * </li>
 * <li>
 * Sector Coordinates: {@link #createDetectorSector(ConstantProvider)}
 * <ul>
 * <li>All sectors from the same detector coincide at φ = 0.</li>
 * <li>To CLAS:	Rotate each sector CW around z-axis by φ = sectorId·60°.</li>
 * <li>To Tilted: Rotate each sector CCW around the y-axis by 25° to θ = 0.</li>
 * <li>Exceptions: BST, CND, FTCAL</li>
 * </ul>
 * </li>
 * <li>
 * Tilted Coordinates: {@link #createDetectorTilted(ConstantProvider)}
 * <ul>
 * <li>The normal of the up-beam surface of each detector is parallel to the
 * z-axis.</li>
 * <li>To Sector: Rotate each sector CW around the y-axis by 25°.</li>
 * <li>To Local: Translate each superlayer’s up-beam surface to z = 0.</li>
 * <li>Exceptions: FTOF Panel2, BST, CND</li>
 * </ul>
 * </li>
 * <li>
 * Local Coordinates: {@link #createDetectorTilted(ConstantProvider)}
 * <ul>
 * <li>All sectors belonging to the same detector coincide.</li>
 * <li>No rotations around the x-axis are required to get to CLAS.</li>
 * <li>No translations along the x- or y-axis are required to get to CLAS.</li>
 * <li>No rotations around the z-axis are required to get the first sector to
 * CLAS.</li>
 * <li>The upstream surface of each superlayer is at z=0 and in the
 * xy-plane.</li>
 * <li>Layers are in place relative to their respective superlayers and do not
 * require further translations or rotations independently of their
 * superlayer.</li>
 * <li>To Tilted: Translate each superlayer’s up-beam surface along the
 * z-axis.</li>
 * <li>Exceptions: BST, CND</li>
 * </ul>
 * </li>
 * </ol>
 * <p>
 * The {@link org.jlab.geom.base.Detector Detector},
 * {@link org.jlab.geom.base.Sector Sector},
 * {@link org.jlab.geom.base.Superlayer Superlayer}, and
 * {@link org.jlab.geom.base.Layer Layer} construction methods return objects in
 * Local coordinates.
 * <p>
 * Factory: <b>{@link org.jlab.geom.base.Factory Factory}</b><br>
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.base.Detector Detector} → 
 * {@link org.jlab.geom.base.Sector Sector} → 
 * {@link org.jlab.geom.base.Superlayer Superlayer} → 
 * {@link org.jlab.geom.base.Layer Layer} → 
 * {@link org.jlab.geom.base.Component Component}
 * </code>
 * <p>
 * All objects are returned in their nominal positions.  For information about 
 * calibration see 
 * {@link org.jlab.geom.base.Layer#setTransformation(org.jlab.geom.prim.Transformation3D)}.
 * <p>
 * Factories have no member variables or initialization procedure. It is
 * acceptable to instantiate a new factory every time one is needed.
 * <p>
 * However some detectors DO have non-negligible initialization procedures. For
 * example, the BST has 33792 wires, the end points of which are calculated
 * using 67584 line-plane intersections. This can take ~0.5 seconds. 
 * <p>
 * Example usage:
 * <p>
 * <code>
 * ConstantProvider constants = DataBaseLoader.getConstantsEC();<br>
 * ECDetector detector;<br>
 * detector = new ECFactory().createDetectorCLAS(constants);<br>
 * detector = new ECFactory().createDetectorSector(constants);<br>
 * detector = new ECFactory().createDetectorTilted(constants);<br>
 * detector = new ECFactory().createDetectorLocal(constants);<br>
 * <br>
 * // Constructed in “local” coordinates (varies by detector)<br>
 * ECSector     firstSector = factory.createSector(constants, 0);<br>
 * ECSuperlayer outerEC     = factory.createSuperlayer(constants, 0, 2);<br>
 * ECLayer      wLayer      = factory.createLayer(constants, 0, 2, 2);<br>
 * <br>
 * ConstantProvider dcConstants = DataBaseLoader.getConstantsDC();<br>
 * DCDetector dc = new DCFactory().createDetectorCLAS(dcConstants);<br>
 * <br>
 * DCSector         sector     =         dc.getSector(0);<br>
 * DCSuperlayer     superlayer =     sector.getSuperlayer(5);<br>
 * DCLayer          layer      = superlayer.getLayer(5);<br>
 * DriftChamberWire wire       =      layer.getComponent(12);<br>
 * Point3D          wireStart  =       wire.getLine().origin();<br>
 * <br>
 * double length = dc.getSector(0).getSuperlayer(0).getLayer(0).getComponent(0).getLength();<br>
 * <br>
 * int numSectors = dc.getNumSectors();<br>
 * <br>
 * List midPoints = new ArrayList();
 * for (DCSector sector : dc.getAllSectors())<br>
 * &nbsp;&nbsp;for (DCSuperlayer superlayer : sector.getAllSuperlayers())<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;for (DCLayer layer : sup.getAllLayers())<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;for (DriftChamberWire wire : layer.getAllComponents())<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;midPoints.add(wire.getMidpoint());
 * </code>
 * 
 * @author jnhankins
 * @param <DetectorType> the specific type of {@link org.jlab.geom.base.Detector}
 * produced by the {@code Factory}
 * @param <SectorType> the specific type of {@link org.jlab.geom.base.Sector}
 * produced by the {@code Factory}
 * @param <SuperlayerType> the specific type of {@link org.jlab.geom.base.Superlayer}
 * produced by the {@code Factory}
 * @param <LayerType> the specific type of {@link org.jlab.geom.base.Layer}
 * produced by the {@code Factory}
 */
public interface Factory<
        DetectorType extends Detector,
        SectorType extends Sector,
        SuperlayerType extends Superlayer,
        LayerType extends Layer> extends Showable {
    
    /**
     * Constructs a new {@code Detector} in CLAS coordinates using the given
     * constants.
     * @param cp the constant provider
     * @return a detector in CLAS coordinates
     */
    DetectorType createDetectorCLAS(ConstantProvider cp);
    
    /**
     * Constructs a new {@code Detector} in Sector coordinates using the given
     * constants.
     * @param cp the constant provider
     * @return a detector in Sector coordinates
     */
    DetectorType createDetectorSector(ConstantProvider cp);
    
    /**
     * Constructs a new {@code Detector} in Tilted coordinates using the given
     * constants.
     * @param cp the constant provider
     * @return a detector in Tilted coordinates
     */
    DetectorType createDetectorTilted(ConstantProvider cp);
    
    /**
     * Constructs a new {@code Detector} in Local coordinates using the given
     * constants.
     * @param cp the constant provider
     * @return a detector in Local coordinates
     */
    DetectorType createDetectorLocal(ConstantProvider cp);

    /**
     * Constructs the specified {@code Sector} in Local coordinates using the
     * given constants.
     * @param cp the constant provider
     * @param sectorId the sector id of the desired sector
     * @return a sector in Local coordinates
     */
    SectorType createSector(ConstantProvider cp, int sectorId);
    
    /**
     * Constructs the specified {@code Superlayer} in Local coordinates using
     * the given constants.
     * @param cp the constant provider
     * @param sectorId the sector id of the desired superlayer
     * @param superlayerId the superlayer id of the desired superlayer
     * @return a superlayer in Local coordinates
     */
    SuperlayerType createSuperlayer(ConstantProvider cp, int sectorId, int superlayerId);
    
    
    /**
     * Constructs the specified {@code Layer} in Local coordinates using the
     * given constants.
     * @param cp the constant provider
     * @param sectorId the sector id of the desired layer
     * @param superlayerId the superlayer id of the desired layer
     * @param layerId the layer id of the desired layer
     * @return a layer in Local coordinates
     */
    LayerType createLayer(ConstantProvider cp, int sectorId, int superlayerId, int layerId);
    
    /**
     * Returns a string that identifies the specific subtype of this factory.
     * @return a string naming this factory's type
     */
    String getType();
    /**
     * Returns a transformation object for given sector, superlayer and layer
     * @param cp
     * @param sector
     * @param superlayer
     * @param layer
     * @return 
     */
    Transformation3D getTransformation(ConstantProvider cp,int sector, int superlayer, int layer);
    
    DetectorTransformation getDetectorTransform(ConstantProvider cp);
    /**
     * Invokes {@code System.out.println(this)}.
     */
    void show();
}
