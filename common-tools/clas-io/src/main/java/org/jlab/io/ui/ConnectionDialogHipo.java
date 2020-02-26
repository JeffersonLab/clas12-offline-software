/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.ui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import org.jlab.utils.FileUtils;

/**
 *
 * @author gavalian
 */
public class ConnectionDialogHipo extends BasicDialog {
    
    public static final int CONNECTSPECIFIC = 1;
    public static final int CONNECTDAQ = 2;
    
    public static final int   RING_TYPE_ET = 5;
    public static final int RING_TYPE_HIPO = 6;
    
    private int  connectionType = ConnectionDialog.RING_TYPE_ET;
    
    private JRadioButton _directConnect;
    private JRadioButton _connectToDAQ;
    
    private static String[] hostNames = new String[]{"clondaq2","clondaq3","clondaq4","clondaq5","clondaq6"};
    private static String[]    hostIP = new String[]{"129.57.167.109","129.57.167.226","129.57.167.227","129.57.167.41","129.57.167.60"};
    
    Map<String,String>  connectionHosts = new LinkedHashMap<String,String>();
    
    private static String[] closeoutButtons = {"Connect", "Cancel"};
    
    private JTextField _ipField;
    private JTextField _fileName;
    private JComboBox  _comboHosts;
    private JComboBox  _comboEtFiles;
    private JComboBox  _files;
    
    private int _reason = DialogUtilities.CANCEL_RESPONSE;
    
    /**
     * Create the panel for selected
     */
    public ConnectionDialogHipo() {
        super("Connection.....", true, closeoutButtons);
        int nhosts = hostNames.length;
        for(int i = 0; i < hostNames.length; i++){
            this.connectionHosts.put(hostNames[i],hostIP[i]);
        }
    }
        
    
    @Override
    protected Component createCenterComponent() {
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,
                BoxLayout.Y_AXIS));
		
        _ipField = new JTextField();
        _ipField.setText("129.57.167.227");
        
        panel.add(_ipField);
        
        ButtonGroup bg = new ButtonGroup();
        _directConnect = new JRadioButton("Connect to Specified Address", true);
        _connectToDAQ = new JRadioButton("Connect to DAQ Ring (Counting House Only)", false);
        bg.add(_directConnect);
        bg.add(_connectToDAQ);
        panel.add(_directConnect);
        panel.add(Box.createVerticalStrut(6));
        panel.add(_connectToDAQ);
        
        
 
                
      
        
        Border emptyBorder = BorderFactory
                .createEtchedBorder();//4, 4, 4, 4);
        
        //CommonBorder cborder = new CommonBorder("Connect to ET");
        
        //panel.setBorder(BorderFactory.createCompoundBorder(emptyBorder));
        panel.setBorder(BorderFactory.createTitledBorder(emptyBorder, "Connect to Host"));
        return panel;
    }
    
    public int reason() {
        return _reason;
    }
    
    @Override
    public void handleCommand(String command) {
        if ("Connect".equals(command)) {
            _reason = DialogUtilities.OK_RESPONSE;
            this.setVisible(false);
        }
        setVisible(false);
    }
    
    public String getFileName() {
        return _fileName.getText();
    }
    
    
    public String getIpAddress() {
        return _ipField.getText();
    }
    
    public String getAddressString(){
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < hostIP.length; i++){
            if(i!=0) str.append(":");
            str.append(hostIP[i]);
        }
        return str.toString();
    }
    
    public int getConnectionType() {
        if (_directConnect.isSelected()) {
            return CONNECTSPECIFIC;
        }
        else if (_connectToDAQ.isSelected()) {
            return CONNECTDAQ;
        }
        
        return -1;
    }
    
    public static void main(String arg[]) {
        ConnectionDialogHipo dialog = new ConnectionDialogHipo();
        dialog.setVisible(true);
        System.out.println(" REASON = " + dialog.reason());
    }
}
