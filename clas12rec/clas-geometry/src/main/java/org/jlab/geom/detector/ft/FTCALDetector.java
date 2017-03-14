package org.jlab.geom.detector.ft;

import java.util.ArrayList;
import java.util.List;
import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractDetector;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.gui.DetectorComponentUI;


/**
 * A Forward Tagger Calorimeter (FTCAL) {@link org.jlab.geom.base.Detector Detector}.
 * <p>
 * Factory: {@link org.jlab.geom.detector.ft.FTCALFactory FTCALFactory}<br> 
 * Hierarchy: 
 * <code>
 * <b>{@link org.jlab.geom.detector.ft.FTCALDetector FTCALDetector}</b> → 
 * {@link org.jlab.geom.detector.ft.FTCALSector FTCALSector} → 
 * {@link org.jlab.geom.detector.ft.FTCALSuperlayer FTCALSuperlayer} → 
 * {@link org.jlab.geom.detector.ft.FTCALLayer FTCALLayer} → 
 * {@link org.jlab.geom.component.ScintillatorPaddle ScintillatorPaddle}
 * </code>
 * 
 * @author jnhankins
 */
public class FTCALDetector extends AbstractDetector<FTCALSector> {
    
    protected FTCALDetector() {
        super(DetectorId.FTCAL);
    }
    
    /**
     * Returns "FTCAL Detector".
     * @return "FTCAL Detector"
     */
    @Override
    public String getType() {
        return "FTCAL Detector";
    }
    
    public List<DetectorComponentUI> getLayerUI(int sector, int superlayer, int layer){
        ArrayList<DetectorComponentUI> components = new ArrayList<DetectorComponentUI>();
        List<ScintillatorPaddle> paddles = this.getSector(sector).getSuperlayer(superlayer).getLayer(layer).getAllComponents();
        System.out.println("FTCAL paddles # = " + paddles.size());
        int counter = 0;
        for(ScintillatorPaddle paddle : paddles){
            counter++;
            DetectorComponentUI entry = new DetectorComponentUI();
            entry.SECTOR = sector;
            entry.LAYER  = superlayer;
            entry.COMPONENT = paddle.getComponentId();
            if(counter<10){
            System.out.println("PADDLE ID = " + entry.COMPONENT);
            paddle.show();
            //paddle.getVolumeShape().show();
           
                for(int loop = 0; loop < 8; loop ++){
                    System.out.print("POINT # " + loop);
                    paddle.getVolumePoint(loop).show();
                }
            }
            entry.shapePolygon.addPoint(
                    (int) -(paddle.getVolumePoint(0).x()*100.0),
                    (int) -(paddle.getVolumePoint(0).y()*100.0)
                    );
            entry.shapePolygon.addPoint(
                    (int) -(paddle.getVolumePoint(1).x()*100.0),
                    (int) -(paddle.getVolumePoint(1).y()*100.0)
                    );
            entry.shapePolygon.addPoint(
                    (int) -(paddle.getVolumePoint(2).x()*100.0),
                    (int) -(paddle.getVolumePoint(2).y()*100.0)
                    );
            entry.shapePolygon.addPoint(
                    (int) -(paddle.getVolumePoint(3).x()*100.0),
                    (int) -(paddle.getVolumePoint(3).y()*100)
                    );
            //if(counter<10){
            components.add(entry);
            //}
        }
        return components;
    }
}
