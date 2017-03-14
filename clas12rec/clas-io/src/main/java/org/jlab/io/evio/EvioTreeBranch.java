/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.io.evio;

import java.util.ArrayList;
import org.jlab.coda.jevio.EvioNode;

/**
 *
 * @author gavalian
 */
public class EvioTreeBranch {
    
    private Integer  branchTag    = 0;
    private Integer  branchNumber = 0;
    
    private final ArrayList<EvioNode> leafNodes = new ArrayList<EvioNode>();
    
    
    public EvioTreeBranch(int tag, int num){
        branchTag  = tag;
        branchNumber = num;
    }
    
    public void addNode(EvioNode node){
        leafNodes.add(node);
    }
    
    public ArrayList<EvioNode> getNodes(){ return leafNodes;}
    public Integer getTag(){ return branchTag;}
    public Integer getNum(){ return branchNumber;}

    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("EVIO Branch tag = %x (%d) num = %x (%d)\n", 
                this.getTag(),this.getTag(),this.getNum(),this.getNum()));
        for(EvioNode node : leafNodes){
            str.append(String.format("\tevio leaf : tag = %x (%d) num = %x (%d) length = %d\n",
                    node.getTag(),node.getTag(),
                    node.getNum(),node.getNum(),
                    node.getDataLength()
                    ));
        }
        return str.toString();
    }
}
