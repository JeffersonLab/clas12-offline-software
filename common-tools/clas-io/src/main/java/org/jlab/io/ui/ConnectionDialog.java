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
public class ConnectionDialog extends BasicDialog {
    
    public static final int CONNECTSPECIFIC = 1;
    public static final int CONNECTDAQ = 2;
    
    public static final int   RING_TYPE_ET = 5;
    public static final int RING_TYPE_HIPO = 6;
    
    private int  connectionType = ConnectionDialog.RING_TYPE_ET;
    
    private JRadioButton _directConnect;
    private JRadioButton _connectToDAQ;
    
    private static String[] hostNames = new String[]{"clondaq2","clondaq3","clondaq4","clondaq5","clondaq6","clondaq7"};
    private static String[]    hostIP = new String[]{"129.57.167.109","129.57.167.226","129.57.167.227","129.57.167.41","129.57.167.60","129.57.167.20"};
    private static String defaultHost = "clondaq6";
    private static String defaultIP = "129.57.167.60";
    
    Map<String,String>  connectionHosts = new LinkedHashMap<String,String>();
    
    private static String[] closeoutButtons = {"Connect", "Cancel"};
    
    private JTextField _ipField;
    private JTextField _fileName;
    private JTextField _portNumber;
    
    private JComboBox  _comboHosts;
    private JComboBox  _comboEtFiles;
    private JComboBox  _files;
    
    private int _reason = DialogUtilities.CANCEL_RESPONSE;
    
    /**
     * Create the panel for selected
     */
    public ConnectionDialog() {
        super("Connection.....", true, closeoutButtons);
        int nhosts = hostNames.length;
        for(int i = 0; i < hostNames.length; i++){
            this.connectionHosts.put(hostNames[i],hostIP[i]);
        }
    }
    
    public ConnectionDialog(int type) {
        super("Connection.....", true, closeoutButtons);
        int nhosts = hostNames.length;
        for(int i = 0; i < hostNames.length; i++){
            this.connectionHosts.put(hostNames[i],hostIP[i]);
        }        
    }
    
    public ConnectionDialog(String defaultHost, String defaultIP) {
        super("Connection.....", true, closeoutButtons);
        int nhosts = hostNames.length;
        for(int i = 0; i < hostNames.length; i++){
            this.connectionHosts.put(hostNames[i],hostIP[i]);
        }
        if(defaultHost!=null && defaultIP!=null) {
            if(!this.connectionHosts.containsKey(defaultHost)) {
                this.connectionHosts.put(defaultHost, defaultIP);
                this._comboHosts.addItem(defaultHost);
            }
            else
                this.connectionHosts.replace(defaultHost, defaultIP);
            _ipField.setText(defaultIP);
            _comboHosts.setSelectedItem(defaultHost);
            _comboHosts.updateUI();
        }
    }
    
    @Override
    protected Component createCenterComponent() {
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,
                BoxLayout.Y_AXIS));
        
        _ipField = new JTextField(25);
        _ipField.setText(this.defaultIP);
        
        JPanel subpanel = new JPanel();
        subpanel.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 2));
        
        JLabel labelip = new JLabel(" Address: ");
        _comboHosts = new JComboBox(this.hostNames);
        ItemListener itemListener = new ItemListener() {
            
            @Override
            public void itemStateChanged(ItemEvent e) {
                String selected = e.getItem().toString();
                if(connectionHosts.containsKey(selected)){
                    _ipField.setText(connectionHosts.get(selected));
                }
            }
        };
        _comboHosts.setSelectedItem(this.defaultHost);
        
        _comboHosts.addItemListener(itemListener);
        subpanel.add(labelip);
        subpanel.add(_ipField);
        subpanel.add(_comboHosts);
        
        panel.add(subpanel);
        panel.add(Box.createVerticalStrut(6));
        
        JPanel subpanel2 = new JPanel();
        subpanel2.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 2));
        
        JLabel label = new JLabel(" File: ");
        _fileName = new JTextField(30);
        _fileName.setText("/et/clasprod");
        subpanel2.add(label);
        subpanel2.add(_fileName);
        //List<String>  etFiles = FileUtils.filesInFolder(null, reason);
        try {
            List<String> etFiles = FileUtils.dirListStartsWith("/et", "");
            //List<String> etFiles = FileUtils.dirListStartsWith("/tmp", "et_sys");
            //List<String> etFiles = FileUtils.dirListStartsWith("/Users/gavalian/Work", "d");
            for(String f : etFiles){
                System.out.println(" -----> " + f);
            }
            String[] etList = new String[etFiles.size()];       
            for(int i = 0; i < etFiles.size(); i++) etList[i] = etFiles.get(i);            
            _comboEtFiles = new JComboBox(etList);
            ItemListener filesListener = new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    String selected = e.getItem().toString();
                    _fileName.setText(selected);
                }
            };            
            _comboEtFiles.addItemListener(filesListener);
            subpanel2.add(_comboEtFiles);
        } catch (Exception e) {
            
        }                        

        
        
        panel.add(subpanel2);
        
        
        JPanel subpanel3 = new JPanel();
        JLabel labelport = new JLabel(" Port : ");
        _portNumber = new JTextField(8);
        _portNumber.setText("11111");
        subpanel3.add(labelport);
        subpanel3.add(_portNumber);
        
        panel.add(subpanel3);
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
    
    public Integer getPort(){
        String port_number = _portNumber.getText();
        Integer port = 11111;
        try {
            port = Integer.parseInt(port_number);
        } catch (Exception e) {
            System.out.println("ERROR : the string provided is not a number : " + port_number);
        }
        return port;
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
        ConnectionDialog dialog = new ConnectionDialog();
        dialog.setVisible(true);
        System.out.println(" REASON = " + dialog.reason());
    }
        
}
