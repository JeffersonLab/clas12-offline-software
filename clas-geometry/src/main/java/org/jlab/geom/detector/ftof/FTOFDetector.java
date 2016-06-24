package org.jlab.geom.detector.ftof;

import java.util.ArrayList;
import java.util.List;
import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractDetector;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.gui.DetectorComponentUI;

/**
 * A Forward Time of Flight (FTOF) {@link org.jlab.geom.base.Detector Detector}.
 * <p>
 * Factory: {@link org.jlab.geom.detector.ftof.FTOFFactory FTOFFactory}<br> 
 * Hierarchy: 
 * <code>
 * <b>{@link org.jlab.geom.detector.ftof.FTOFDetector FTOFDetector}</b> → 
 * {@link org.jlab.geom.detector.ftof.FTOFSector FTOFSector} → 
 * {@link org.jlab.geom.detector.ftof.FTOFSuperlayer FTOFSuperlayer} → 
 * {@link org.jlab.geom.detector.ftof.FTOFLayer FTOFLayer} → 
 * {@link org.jlab.geom.component.ScintillatorPaddle ScintillatorPaddle}
 * </code>
 * 
 * @author jnhankins
 */
public class FTOFDetector extends AbstractDetector<FTOFSector> {
    
    protected FTOFDetector() {
        super(DetectorId.FTOF);
    }
    
    /**
     * Returns "FTOF Detector".
     * @return "FTOF Detector"
     */
    @Override
    public String getType() {
        return "FTOF Detector";
    }
    
    public List<DetectorComponentUI> getLayerUI(int superlayer, int layer){
        ArrayList<DetectorComponentUI> components = new ArrayList<DetectorComponentUI>();
        for(int sector = 0; sector < 6 ; sector++){
            
        }
        return components;
    }
        
    public List<DetectorComponentUI> getLayerUI(int sector, int superlayer, int layer){
        ArrayList<DetectorComponentUI> components = new ArrayList<DetectorComponentUI>();
        List<ScintillatorPaddle> paddles = this.getSector(sector).getSuperlayer(superlayer).getLayer(layer).getAllComponents();
        for(ScintillatorPaddle paddle : paddles){
            DetectorComponentUI entry = new DetectorComponentUI();
            entry.SECTOR = sector;
            entry.LAYER  = superlayer;
            entry.COMPONENT = paddle.getComponentId();
            
            entry.shapePolygon.addPoint(
                    (int) paddle.getVolumePoint(0).x(),
                    (int) paddle.getVolumePoint(0).y()
                    );
            entry.shapePolygon.addPoint(
                    (int) paddle.getVolumePoint(1).x(),
                    (int) paddle.getVolumePoint(1).y()
                    );
            entry.shapePolygon.addPoint(
                    (int) paddle.getVolumePoint(5).x(),
                    (int) paddle.getVolumePoint(5).y()
                    );
            entry.shapePolygon.addPoint(
                    (int) paddle.getVolumePoint(4).x(),
                    (int) paddle.getVolumePoint(4).y()
                    );
            
            /*
            entry.shapePolygon.addPoint(
                    (int) paddle.getVolumePoint(0).y(),
                    (int) -paddle.getVolumePoint(0).x()
                    );
            entry.shapePolygon.addPoint(
                    (int) paddle.getVolumePoint(1).y(),
                    (int) -paddle.getVolumePoint(1).x()
                    );
            entry.shapePolygon.addPoint(
                    (int) paddle.getVolumePoint(5).y(),
                    (int) -paddle.getVolumePoint(5).x()
                    );
            entry.shapePolygon.addPoint(
                    (int) paddle.getVolumePoint(4).y(),
                    (int) -paddle.getVolumePoint(4).x()
                    );
            */
            /*
            entry.shapePolygon.addPoint(
                    (int) paddle.getVolumePoint(0).x(),
                    (int) paddle.getVolumePoint(0).y()
                    );*/
            /*
            entry.shapePolygon.addPoint(paddle.getVolumePoint(1));
            entry.shapePolygon.addPoint(paddle.getVolumePoint(3));
            entry.shapePolygon.addPoint(paddle.getVolumePoint(4));            
            */
            //System.out.println(entry.shapePolygon);
            components.add(entry);
        }
        return components;
    }
    
}
