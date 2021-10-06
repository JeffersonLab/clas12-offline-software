/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.evio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.coda.jevio.ByteDataTransformer;
import org.jlab.coda.jevio.DataType;
import org.jlab.coda.jevio.EvioCompactStructureHandler;
import org.jlab.coda.jevio.EvioException;
import org.jlab.coda.jevio.EvioNode;

/**
 *
 * @author gavalian
 */
public class EvioDataEventHandler {
    
    private EvioCompactStructureHandler structure = null;
    private ByteBuffer evioBuffer;     
    private List<EvioNode> eventNodes  = null;
   
    public EvioDataEventHandler(byte[] buffer, ByteOrder b_order){
        evioBuffer = ByteBuffer.wrap(buffer);
        evioBuffer.order(b_order);

        // This had been inserted to accommodate large EVIO events that
        // were unreadable in JEVIO versions prior to 6.2:
        //if(buffer.length>500*1024){
        //    System.out.println("error >>> evio event hadler : buffer size ecceeds 500 kB");
        //    structure = null;
        //    eventNodes = null;
        //} else {
            try {
                structure = new EvioCompactStructureHandler(evioBuffer,DataType.BANK);
                //eventNodes = structure.getChildNodes();
                eventNodes = structure.getNodes();
            } catch (EvioException ex) {
                Logger.getLogger(EvioDataEvent.class.getName()).log(Level.SEVERE, null, ex);
            }
        //}
         //this.list();
    }
    
    public EvioDataEventHandler(ByteBuffer buff){
        evioBuffer = buff;
        try {
            structure = new EvioCompactStructureHandler(evioBuffer,DataType.BANK);
            eventNodes = structure.getChildNodes();
        } catch (EvioException ex) {
            Logger.getLogger(EvioDataEvent.class.getName()).log(Level.SEVERE, null, ex);
        }
        //this.list();
    }
    
    public EvioNode getRootNode(int tag, int num, DataType type){
        for(EvioNode node : eventNodes){
            //System.out.println(" LOOKING for tag = " + tag + " num =  "   + num +  " type = " + type);
            if(node.getTag()==tag&&node.getNum()==num&&node.getDataTypeObj()==type){
                return node;
            }
        }
        return null;
    }
    
    public EvioCompactStructureHandler getStructure(){return this.structure;}
    
    public void setStructure(EvioCompactStructureHandler struct){ 
        this.structure = struct;
        try {
            this.eventNodes = this.structure.getNodes();
        } catch (EvioException ex) {
            Logger.getLogger(EvioDataEventHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public EvioNode getChildNode(EvioNode rootNode, int tag, int num, DataType type){
        List<EvioNode>  leafs = rootNode.getChildNodes();
        for(EvioNode node : leafs){
            if(node.getTag()==tag&&node.getNum()==num&&node.getDataTypeObj()==type){
                return node;
            }
        }
        return null;
    }
    
    public TreeMap<Integer,Object>  getNodeData(EvioNode nodeBank){
        TreeMap<Integer,Object>  nodeData = new TreeMap<Integer,Object>();
        List<EvioNode>  nodeList = nodeBank.getChildNodes();
        for(EvioNode  node : nodeList){            
            /*
            * Adding integer entries to the tree
            */
            if(node.getDataTypeObj()==DataType.INT32||node.getDataTypeObj()==DataType.UINT32){
                try {
                    ByteBuffer buffer = structure.getData(node);
                    int[] nodedata = ByteDataTransformer.toIntArray(buffer);
                    nodeData.put(node.getNum(), nodedata);
                } catch (EvioException ex) {
                    Logger.getLogger(EvioDataEventHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            /*
            * Adding short entries to the tree
            */
            if(node.getDataTypeObj()==DataType.SHORT16){
                try {
                    ByteBuffer buffer = structure.getData(node);
                    short[] nodedata = ByteDataTransformer.toShortArray(buffer);
                    nodeData.put(node.getNum(), nodedata);
                } catch (EvioException ex) {
                    Logger.getLogger(EvioDataEventHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
             /*
            * Adding byte entries to the tree
            */
            if(node.getDataTypeObj()==DataType.UCHAR8||node.getDataTypeObj()==DataType.CHAR8){
                try {
                    ByteBuffer buffer = structure.getData(node);
                    byte[] nodedata = ByteDataTransformer.toByteArray(buffer);
                    nodeData.put(node.getNum(), nodedata);
                } catch (EvioException ex) {
                    Logger.getLogger(EvioDataEventHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
             /*
            * Adding float32 entries to the tree
            */
            if(node.getDataTypeObj()==DataType.FLOAT32){
                try {
                    ByteBuffer buffer = structure.getData(node);
                    float[] nodedata = ByteDataTransformer.toFloatArray(buffer);
                    nodeData.put(node.getNum(), nodedata);
                } catch (EvioException ex) {
                    Logger.getLogger(EvioDataEventHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
             /*
            * Adding float32 entries to the tree
            */
            if(node.getDataTypeObj()==DataType.DOUBLE64){
                try {
                    ByteBuffer buffer = structure.getData(node);
                    double[] nodedata = ByteDataTransformer.toDoubleArray(buffer);
                    nodeData.put(node.getNum(), nodedata);
                } catch (EvioException ex) {
                    Logger.getLogger(EvioDataEventHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return nodeData;
    }
    
    public void list(){
        System.out.println("  LIST ========>  EVIO NODES ");
        List<EvioNode>  childnodes;
        try {
            childnodes = this.structure.getNodes();
            for(EvioNode node : childnodes){
                //for(EvioNode node : this.eventNodes){
                System.out.println(String.format(" tag = %8d  num = %8d  type = %s", 
                        node.getTag(),node.getNum(),node.getDataTypeObj()));
            }
        } catch (EvioException ex) {
            Logger.getLogger(EvioDataEventHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
