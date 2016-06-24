package org.jlab.geom.detector.ec;

import java.util.ArrayList;
import java.util.List;
import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractDetector;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.gui.DetectorComponentUI;

/**
 * An Electromagnetic Calorimeter (EC) {@link org.jlab.geom.base.Detector Detector}.
 * <p>
 * Factory: {@link org.jlab.geom.detector.ec.ECFactory ECFactory}<br> 
 * Hierarchy: 
 * <code>
 * <b>{@link org.jlab.geom.detector.ec.ECDetector ECDetector}</b> → 
 * {@link org.jlab.geom.detector.ec.ECSector ECSector} → 
 * {@link org.jlab.geom.detector.ec.ECSuperlayer ECSuperlayer} → 
 * {@link org.jlab.geom.detector.ec.ECLayer ECLayer} → 
 * {@link org.jlab.geom.component.ScintillatorPaddle ScintillatorPaddle}
 * </code>
 * 
 * @author jnhankins
 */
public class ECDetector extends AbstractDetector<ECSector> {
    
    protected ECDetector() {
        super(DetectorId.EC);
    }
    
    /**
     * Returns "EC Detector".
     * @return "EC Detector"
     */
    @Override
    public String getType() {
        return "EC Detector";
    }
    
    public List<DetectorComponentUI> getLayerUI(int sector, int superlayer, int layer){
        ArrayList<DetectorComponentUI> components = new ArrayList<DetectorComponentUI>();
        List<ScintillatorPaddle> paddles = this.getSector(sector).getSuperlayer(superlayer).getLayer(layer).getAllComponents();
        for(ScintillatorPaddle paddle : paddles){
            DetectorComponentUI entry = new DetectorComponentUI();
            entry.SECTOR = sector;
            entry.LAYER  = layer;
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
