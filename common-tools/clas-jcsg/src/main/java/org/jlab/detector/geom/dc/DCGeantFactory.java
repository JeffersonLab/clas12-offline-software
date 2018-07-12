/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geom.dc;

import eu.mihosoft.vrl.v3d.Vector3d;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.geom.DetectorId;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.base.DetectorTransformation;
import org.jlab.geom.base.Factory;
import org.jlab.geom.component.DriftChamberWire;
import org.jlab.geom.detector.dc.DCDetector;
import org.jlab.geom.detector.dc.DCLayer;
import org.jlab.geom.detector.dc.DCSector;
import org.jlab.geom.detector.dc.DCSuperlayer;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;

/**
 *
 * @author gavalian
 */
public class DCGeantFactory implements Factory<DCDetector, DCSector, DCSuperlayer, DCLayer>{

    
    String geomDBVar = "";
    private  DCGeant4Factory dcDetector = null;
    
    private void initDetector(ConstantProvider provider){
        if(dcDetector==null){
            dcDetector = new DCGeant4Factory(provider, DCGeant4Factory.MINISTAGGERON);    
        }
    }
    
    @Override
    public DCDetector createDetectorCLAS(ConstantProvider cp) {
        //ConstantProvider provider = GeometryFactory.getConstants(DetectorType.DC, 11, Optional.ofNullable(geomDBVar).orElse("default"));
        initDetector(cp);
        DCDetector detector = createDetectorSector(cp);
        for (DCSector sector : detector.getAllSectors()) {
            for (DCSuperlayer superlayer : sector.getAllSuperlayers()) {
                for (DCLayer layer: superlayer.getAllLayers()) {
                    int sectorId = superlayer.getSectorId();
                    Transformation3D trans = layer.getTransformation();
                    trans.rotateZ(Math.toRadians(60*sectorId));
                    layer.setTransformation(trans);
                }
            }
        }
        return detector;
    }

    @Override
    public DCDetector createDetectorSector(ConstantProvider cp) {
        initDetector(cp);
        DCDetector detector = createDetectorTilted(cp);
        for (DCSector sector: detector.getAllSectors()) {
            for (DCSuperlayer superlayer: sector.getAllSuperlayers()) {
                for (DCLayer layer: superlayer.getAllLayers()) {
                    int regionId = superlayer.getSuperlayerId()/2;
                    double thtilt = Math.toRadians(cp.getDouble("/geometry/dc/region/thtilt", regionId));
                    Transformation3D trans = layer.getTransformation();
                    trans.rotateY(thtilt);
                    layer.setTransformation(trans);
                }
            }
        }
        return detector;       
    }

    @Override
    public DCDetector createDetectorTilted(ConstantProvider cp) {
        initDetector(cp);
        DCDetector detector = createDetectorLocal(cp);
        for (DCSector sector: detector.getAllSectors()) {
            for (DCSuperlayer superlayer: sector.getAllSuperlayers()) {
                int superlayerId = superlayer.getSuperlayerId();
                int region = superlayerId/2;
                double dist2tgt = cp.getDouble("/geometry/dc/region/dist2tgt", region);
                double midgap   = cp.getDouble("/geometry/dc/region/midgap", region);
                double d_layer  = cp.getDouble("/geometry/dc/superlayer/wpdist", superlayerId);
                double gz = dist2tgt + 3*d_layer;
                if (superlayerId%2 == 1) {
                    gz += midgap + 21*cp.getDouble("/geometry/dc/superlayer/wpdist", superlayerId-1);
                }
                Transformation3D trans = new Transformation3D();
                trans.translateXYZ(0, 0, gz);
                superlayer.setTransformation(trans);
            }
        }
        return detector;
    }

    @Override
    public DCDetector createDetectorLocal(ConstantProvider cp) {
        initDetector(cp);
        DCDetector detector = new DCDetector(DetectorId.DC);
        for (int sectorId=0; sectorId<6; sectorId++)
            detector.addSector(createSector(cp, sectorId));
        return detector;
    }

    @Override
    public DCSector createSector(ConstantProvider cp, int sectorId) {
        if(!(0<=sectorId && sectorId<6))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        DCSector sector = new DCSector(DetectorId.DC,sectorId);
        for (int superlayerId=0; superlayerId<6; superlayerId++)
            sector.addSuperlayer(createSuperlayer(cp, sectorId, superlayerId));
        return sector;
    }

    @Override
    public DCSuperlayer createSuperlayer(ConstantProvider cp, int sectorId, int superlayerId) {
        if(!(0<=sectorId && sectorId<6))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        if(!(0<=superlayerId && superlayerId<6))
            throw new IllegalArgumentException("Error: invalid superlayer="+superlayerId);
        DCSuperlayer superlayer = new DCSuperlayer(DetectorId.DC,sectorId, superlayerId);
        for (int layerId=0; layerId<6; layerId++)
            superlayer.addLayer(createLayer(cp, sectorId, superlayerId, layerId));
        return superlayer;
    }

    @Override
    public DCLayer createLayer(ConstantProvider cp, int sectorId, int superlayerId, int layerId) {
        if(!(0<=sectorId && sectorId<6))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        if(!(0<=superlayerId && superlayerId<6))
            throw new IllegalArgumentException("Error: invalid superlayer="+superlayerId);
        if(!(0<=layerId && layerId<6))
            throw new IllegalArgumentException("Error: invalid layer="+layerId);
        
        DCLayer layer = new DCLayer(DetectorId.DC,sectorId, superlayerId, layerId);
        
        for(int wireId = 0; wireId < 112; wireId++){            
            Vector3d vOrigin = dcDetector.getWireLeftend(superlayerId, layerId, wireId);
            Vector3d vEnd    = dcDetector.getWireRightend(superlayerId, layerId, wireId);            
            Vector3d vMidpoint = dcDetector.getWireMidpoint(superlayerId, layerId, wireId);
            Line3D   wireLine  = new Line3D(
                    vOrigin.x,vOrigin.y,vOrigin.z,
                    vEnd.x, vEnd.y, vEnd.z
            );
            
            Point3D wireMid = new Point3D(vMidpoint.x, vMidpoint.y,vMidpoint.z);
            List<Point3D> botHex = new ArrayList();
            List<Point3D> topHex = new ArrayList();
            for (int h=0; h<6; h++) {
                botHex.add(new Point3D(0.0,0.0,0.0));
                topHex.add(new Point3D(0.0,0.0,0.0));
            }
            DriftChamberWire wire = new DriftChamberWire(wireId, wireMid, wireLine, 
                    false, botHex, topHex);
            layer.addComponent(wire);
        }
        return layer;
    }

    @Override
    public String getType() {
        return "DC Factory";
    }

    @Override
    public Transformation3D getTransformation(ConstantProvider cp, int sector, int superlayer, int layer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DetectorTransformation getDetectorTransform(ConstantProvider cp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void show() {
        
    }
    
}
