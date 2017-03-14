/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.utils.groups;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;

/**
 *
 * @author gavalian
 */
public class IndexedTableViewer extends JPanel {
    
    JTable            uiTable  = new JTable();
    IndexedTable  indexedTable = null;
    
    public IndexedTableViewer(IndexedTable table){
        super();
        this.indexedTable = table;
        initUI();
    }
    
    private void initUI(){
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createSoftBevelBorder(SoftBevelBorder.RAISED));
        JPanel innerPane = new JPanel();
        innerPane.setLayout(new BorderLayout());
        innerPane.setBorder(BorderFactory.createSoftBevelBorder(SoftBevelBorder.LOWERED));
        uiTable.setModel(this.indexedTable);
        JScrollPane  scroll = new JScrollPane(uiTable);
        innerPane.add(scroll,BorderLayout.CENTER);
        this.add(innerPane,BorderLayout.CENTER);
    }
}
