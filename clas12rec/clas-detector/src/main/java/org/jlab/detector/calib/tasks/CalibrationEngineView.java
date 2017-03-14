/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.calib.tasks;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.CalibrationConstantsListener;
import org.jlab.detector.calib.utils.CalibrationConstantsView;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.group.DataGroup;
import org.jlab.utils.groups.IndexedList;

/**
 *
 * @author gavalian
 */
public class CalibrationEngineView extends JPanel implements CalibrationConstantsListener {
    
    CalibrationEngine   engine = null;
    JSplitPane          splitPane = null;
    CalibrationConstantsView ccview = null;
    EmbeddedCanvas           canvas = null;
    
    
    public CalibrationEngineView(CalibrationEngine ce){        
        super();
        this.setLayout(new BorderLayout());
        engine = ce;
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        canvas = new EmbeddedCanvas();        
        ccview = new CalibrationConstantsView();
        ccview.addConstants(engine.getCalibrationConstants().get(0),this);
        splitPane.setTopComponent(canvas);
        splitPane.setBottomComponent(ccview);
        this.add(splitPane,BorderLayout.CENTER);
        splitPane.setDividerLocation(0.5);
        
    }

    @Override
    public void constantsEvent(CalibrationConstants cc, int col, int row) {
        System.out.println("Well. it's working " + col + "  " + row);
        String str_sector    = (String) cc.getValueAt(row, 0);
        String str_layer     = (String) cc.getValueAt(row, 1);
        String str_component = (String) cc.getValueAt(row, 2);
        System.out.println(str_sector + " " + str_layer + " " + str_component);
        IndexedList<DataGroup> group = engine.getDataGroup();
        
        int sector    = Integer.parseInt(str_sector);
        int layer     = Integer.parseInt(str_layer);
        int component = Integer.parseInt(str_component);
        
        if(group.hasItem(sector,layer,component)==true){
            DataGroup dataGroup = group.getItem(sector,layer,component);
            this.canvas.draw(dataGroup);
            this.canvas.update();
        } else {
            System.out.println(" ERROR: can not find the data group");
        }
    }
    
    
}
