package org.jlab.geom.detector.ftof;

import java.util.List;
import org.jlab.detector.geant4.FTOFGeant4Factory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.base.DetectorTransformation;
import org.jlab.geom.base.Factory;
import org.jlab.geom.component.ScintillatorMesh;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.geant.Geant4Basic;
import org.jlab.geom.prim.Triangle3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;


/**
 * A Forward Time of Flight (FTOF) {@link org.jlab.geom.base.Factory Factory}.
 * <p>
 * Factory: <b>{@link org.jlab.geom.detector.ftof.FTOFFactory FTOFFactory}</b><br> 
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.detector.ftof.FTOFDetector FTOFDetector} → 
 * {@link org.jlab.geom.detector.ftof.FTOFSector FTOFSector} → 
 * {@link org.jlab.geom.detector.ftof.FTOFSuperlayer FTOFSuperlayer} → 
 * {@link org.jlab.geom.detector.ftof.FTOFLayer FTOFLayer} → 
 * {@link org.jlab.geom.component.ScintillatorPaddle ScintillatorPaddle}
 * </code>
 * 
 * @author jnhankins
 */
public class FTOFFactory implements Factory <FTOFDetector, FTOFSector, FTOFSuperlayer, FTOFLayer> {
    
    @Override
    public FTOFDetector createDetectorCLAS(ConstantProvider cp) {
        FTOFDetector detector = createDetectorSector(cp);
        for (FTOFSector sector : detector.getAllSectors()) {
            for (FTOFSuperlayer superlayer : sector.getAllSuperlayers()) {
                for (FTOFLayer layer: superlayer.getAllLayers()) {
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
    public FTOFDetector createDetectorSector(ConstantProvider cp) {
        FTOFDetector detector = createDetectorTilted(cp);
        for (FTOFSector sector: detector.getAllSectors()) {
            for (FTOFSuperlayer superlayer: sector.getAllSuperlayers()) {
                for (FTOFLayer layer: superlayer.getAllLayers()) {
                    String layerStr = getSuperlayerString(superlayer.getSuperlayerId());
                    double thtilt = Math.toRadians(cp.getDouble(layerStr+"/panel/thtilt", 0));
                    double thmin  = Math.toRadians(cp.getDouble(layerStr+"/panel/thmin", 0)); 
                    Transformation3D trans = layer.getTransformation();
                    if (superlayer.getSuperlayerId() == 2)
                        trans.rotateY(thtilt-thmin);
                    else 
                        trans.rotateY(thtilt);
                    layer.setTransformation(trans);
                }
            }
        }
        return detector;
    }
    
    @Override
    public FTOFDetector createDetectorTilted(ConstantProvider cp) {
        FTOFDetector detector = createDetectorLocal(cp);
        for (FTOFSector sector: detector.getAllSectors()) {
            for (FTOFSuperlayer superlayer: sector.getAllSuperlayers()) {
                String layerStr = getSuperlayerString(superlayer.getSuperlayerId());
                double thtilt    = Math.toRadians(cp.getDouble(layerStr+"/panel/thtilt", 0));
                double thmin     = Math.toRadians(cp.getDouble(layerStr+"/panel/thmin", 0)); 
                double dist2edge = cp.getDouble(layerStr+"/panel/dist2edge", 0);
                double dz = dist2edge*Math.cos(thtilt-thmin);
                Transformation3D trans = new Transformation3D();
                trans.translateXYZ(0, 0, dz);
                if (superlayer.getSuperlayerId() == 2) {
                    trans.rotateY(thmin);
                }
                superlayer.setTransformation(trans);
            }
        }
        return detector;
    }
            
    @Override
    public FTOFDetector createDetectorLocal(ConstantProvider cp) {
        FTOFDetector detector = new FTOFDetector();
        for (int sectorId=0; sectorId<6; sectorId++)
            detector.addSector(createSector(cp, sectorId));
        return detector;
    }

    @Override
    public FTOFSector createSector(ConstantProvider cp, int sectorId) {
        if(!(0<=sectorId && sectorId<6))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        FTOFSector sector = new FTOFSector(sectorId);
        for (int superlayerId=0; superlayerId<3; superlayerId++)
            sector.addSuperlayer(createSuperlayer(cp, sectorId, superlayerId));
        return sector;
    }

    @Override
    public FTOFSuperlayer createSuperlayer(ConstantProvider cp, int sectorId, int superlayerId) {
        if(!(0<=sectorId && sectorId<6))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        if(!(0<=superlayerId && superlayerId<3))
            throw new IllegalArgumentException("Error: invalid superlayer="+superlayerId);
        FTOFSuperlayer superlayer = new FTOFSuperlayer(sectorId, superlayerId);
        for (int layerId=0; layerId<1; layerId++)
            superlayer.addLayer(createLayer(cp, sectorId, superlayerId, layerId));
        return superlayer;
    }
    
    @Override
    public FTOFLayer createLayer(ConstantProvider cp, int sectorId, int superlayerId, int layerId) {
        if(!(0<=sectorId && sectorId<6))
            throw new IllegalArgumentException("Error: invalid sector="+sectorId);
        if(!(0<=superlayerId && superlayerId<3))
            throw new IllegalArgumentException("Error: invalid superlayer="+superlayerId);
        if(!(0<=layerId && layerId<1))
            throw new IllegalArgumentException("Error: invalid layer="+layerId);
        
        String layerStr = getSuperlayerString(superlayerId);
        int    numPaddles       =    cp.length(layerStr+"/paddles/paddle");
        double paddlewidth      = cp.getDouble(layerStr+"/panel/paddlewidth", 0); 
        double paddlethickness  = cp.getDouble(layerStr+"/panel/paddlethickness", 0); 
        double gap              = cp.getDouble(layerStr+"/panel/gap", 0);
        double wrapperthickness = cp.getDouble(layerStr+"/panel/wrapperthickness", 0);
        double thtilt    = Math.toRadians(cp.getDouble(layerStr+"/panel/thtilt", 0)); 
        double thmin     = Math.toRadians(cp.getDouble(layerStr+"/panel/thmin", 0)); 
        double dist2edge = cp.getDouble(layerStr+"/panel/dist2edge", 0);
        String lengthstr = layerStr+"/paddles/Length";
        
        double dx = dist2edge*Math.sin(thtilt-thmin);
        
        FTOFLayer layer = new FTOFLayer(sectorId, superlayerId, 0);
        
        for (int paddleId=0; paddleId<numPaddles; paddleId++) {
            
            double paddlelength = cp.getDouble(lengthstr, paddleId);
            ScintillatorPaddle paddle = new ScintillatorPaddle(paddleId, paddlewidth, paddlelength, paddlethickness);
            
            double xoffset = paddleId * (paddlewidth + gap + 2*wrapperthickness);
            paddle.translateXYZ(paddlewidth*0.5 + xoffset - dx, 0, paddlethickness*0.5);
            
//            if (superlayerId == 2) {
//                double dz = dist2edge*Math.cos(thtilt-thmin);
//                paddle.translateXYZ(0, 0, dz);
//                paddle.rotateY(thtilt);
//                
//                layerStr = getSuperlayerString(0);
//                double thtilta    = Math.toRadians(cp.getDouble(layerStr+"/panel/thtilt", 0)); 
//                double thmina     = Math.toRadians(cp.getDouble(layerStr+"/panel/thmin", 0)); 
//                double dist2edgea = cp.getDouble(layerStr+"/panel/dist2edge", 0);
//                double dza = dist2edgea*Math.cos(thtilta-thmina);
//                
//                paddle.translateXYZ(0, 0, -dza);
//                paddle.rotateY(-thmin);
//            }
            
            layer.addComponent(paddle);
        }
        
        List<ScintillatorPaddle> paddles = layer.getAllComponents();
        Point3D pLL = paddles.get(0).getVolumePoint(0);
        Point3D pLR = paddles.get(0).getVolumePoint(4);
        Point3D pUL = paddles.get(numPaddles-1).getVolumePoint(1);
        Point3D pUR = paddles.get(numPaddles-1).getVolumePoint(5);
        Point3D pBL = paddles.get(numPaddles-1).getVolumePoint(0);
        Point3D pBR = paddles.get(numPaddles-1).getVolumePoint(4);
        
        pUL = new Point3D(pUL.x(), pUL.y() + paddlewidth*(pBL.y()-pLL.y())/(pBL.x()-pLL.x()) , pUL.z());
        pUR = new Point3D(pUR.x(), pUR.y() + paddlewidth*(pBR.y()-pLR.y())/(pBR.x()-pLR.x()) , pUR.z());
        layer.getBoundary().addFace(new Triangle3D(pLL, pLR, pUL));
        layer.getBoundary().addFace(new Triangle3D(pUR, pUL, pLR));
        
        layer.getPlane().set(0, 0, 0, 0, 0, 1);
        
        return layer;
    }
    
    private static String getSuperlayerString(int superlayerId) {
        switch (superlayerId) {
            case 0: return "/geometry/ftof/panel1a";
            case 1: return "/geometry/ftof/panel1b";
            case 2: return "/geometry/ftof/panel2";
        }
        throw new IllegalArgumentException("invalid superlayerId="+superlayerId);
    }

    /**
     * Returns "FTOF Factory".
     * @return "FTOF Factory"
     */
    @Override
    public String getType() {
        return "FTOF Factory";
    }

    @Override
    public void show() {
        System.out.println(this);
    }
    
    @Override
    public String toString() {
        return getType();
    }

    @Override
    public Transformation3D getTransformation(ConstantProvider cp, int sector, int superlayer, int layer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DetectorTransformation getDetectorTransform(ConstantProvider cp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    public FTOFDetectorMesh getDetectorGeant4(ConstantProvider cp){
        FTOFDetectorMesh  detector = new FTOFDetectorMesh();
        FTOFGeant4Factory  factory = new FTOFGeant4Factory();
        
        for(int sector = 1; sector <=6; sector++){
            
            FTOFSectorMesh ftofSector = new FTOFSectorMesh(sector);
            
            for(int layer = 1; layer <= 3; layer++){
                
                FTOFSuperlayerMesh  ftofSuperlayer = new FTOFSuperlayerMesh(sector,layer);
                FTOFLayerMesh       ftofLayer      = new FTOFLayerMesh(sector,layer,1);
                
                Geant4Basic  sLayer = factory.createPanel(cp, sector, layer);
                Transformation3D  rotationMother    = sLayer.rotation().inverse();
                //Transformation3D  rotationMother    = new Transformation3D();
                //rotationMother.copy(sLayer.rotation());
                //rotationMother.inverse();
                Transformation3D  translationMother = sLayer.translation();
                //System.out.println(" SECTOR = " + sector + "  LAYER = " + layer);
                //System.out.println(sLayer.toString());
                //rotationMother.show();
                //translationMother.show();
                
                int counter = 1;
                for(Geant4Basic paddle : sLayer.getChildren()){

                    double[] params = paddle.getParameters();
                    int[]    ids    = paddle.getId();
                    Transformation3D  rotChild = paddle.rotation();
                    Transformation3D  trChild  = paddle.translation();
                    //System.out.println("PADDLE " + counter);
                    //trChild.show();
                    //rotChild.show();
                    //rotationMother.show();
                    //translationMother.show();
                    counter++;
                    ScintillatorMesh  sciPaddle = new  ScintillatorMesh(ids[2],params[0],params[1],params[2]);
                    rotChild.apply(sciPaddle);
                    trChild.apply(sciPaddle);
                    
                    rotationMother.apply(sciPaddle);
                    translationMother.apply(sciPaddle);
                    
                    ftofLayer.addComponent(sciPaddle);        
                    /*
                    System.out.print("paddle = " 
                            + params[0] + " " + params[1] + " " + params[2]  + "  ID = ");
                    for(int id : ids){
                        System.out.print(" " + id);
                    }
                    System.out.println();*/
                }
                ftofSuperlayer.addLayer(ftofLayer);
                ftofSector.addSuperlayer(ftofSuperlayer);
            }
            detector.addSector(ftofSector);
        }
        return detector;
    }
}
