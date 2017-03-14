/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import org.jlab.io.base.DataBank;

/**
 *
 * @author gavalian
 */
public class DataBankPanel extends JPanel {
    
    private JScrollPane  scrollPane = null;
    private JTable        table     = null;

    public DataBankPanel(){
        super();
        this.setPreferredSize(new Dimension(600,400));   
        this.initComponents();
    }
    
    public DataBankPanel(DataBank bank){
        super();
        this.setPreferredSize(new Dimension(600,400));   
        this.initComponents();
        this.setBank(bank);
    }
    
    public DataBankPanel(DataBank bank, BankEntryMasks masks){
        super();
        this.setPreferredSize(new Dimension(600,400));   
        this.initComponents();
        this.setBank(bank,masks);
    }

    private void initComponents(){
        this.setLayout(new BorderLayout());

        String[]   cnames = {"id","p"};
        String[][] cdata = {{"11","0.45"},{"2212","0.65"}};
        this.table = new JTable(cdata,cnames);
        
        //scrollPane.add(table);
        scrollPane = new JScrollPane(table);
        //scrollPane.setAutoscrolls(true);
        //this.table.setFillsViewportHeight(true);
        this.add(scrollPane,BorderLayout.CENTER);        
    }
    
    public void setBank(DataBank bank){
        this.table.setModel(bank.getTableModel(""));
    }
    
    public void setBank(DataBank bank, BankEntryMasks masks){
        if(masks.getMask(bank.getDescriptor().getName())==null){
            this.table.setModel(bank.getTableModel(""));
        } else {
            this.table.setModel(bank.getTableModel(masks.getMask(bank.getDescriptor().getName())));
        }
    }
    
    public static void main(String[] args){
        JFrame frame = new JFrame();
        DataBankPanel panel = new DataBankPanel();
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }
}
