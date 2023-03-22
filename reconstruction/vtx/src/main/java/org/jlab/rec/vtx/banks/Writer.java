/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.vtx.banks;

import java.util.List;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.vtx.Vertex;

/**
 *
 * @author ziegler
 */
public class Writer {
    
    public  DataBank VtxBank(DataEvent event, List<Vertex> vtx) { 
        DataBank bank = event.createBank("REC::VertDoca", vtx.size());
       
        for (int i = 0; i < vtx.size(); i++) {
            bank.setShort("index1", i, (short) vtx.get(i).getP1().getIndex());
            bank.setShort("index2", i, (short) vtx.get(i).getP2().getIndex());
            bank.setFloat("x", i, (float) vtx.get(i).getP0().getVx());
            bank.setFloat("y", i, (float) vtx.get(i).getP0().getVy());
            bank.setFloat("z", i, (float) vtx.get(i).getP0().getVz());
            bank.setFloat("x1", i, (float) vtx.get(i).getP1().getVx());
            bank.setFloat("y1", i, (float) vtx.get(i).getP1().getVy());
            bank.setFloat("z1", i, (float) vtx.get(i).getP1().getVz());
            bank.setFloat("cx1", i, (float) vtx.get(i).getP1().getPx());
            bank.setFloat("cy1", i, (float) vtx.get(i).getP1().getPy());
            bank.setFloat("cz1", i, (float) vtx.get(i).getP1().getPz());
            bank.setFloat("x2", i, (float) vtx.get(i).getP2().getVx());
            bank.setFloat("y2", i, (float) vtx.get(i).getP2().getVy());
            bank.setFloat("z2", i, (float) vtx.get(i).getP2().getVz());
            bank.setFloat("cx2", i, (float) vtx.get(i).getP2().getPx());
            bank.setFloat("cy2", i, (float) vtx.get(i).getP2().getPy());
            bank.setFloat("cz2", i, (float) vtx.get(i).getP2().getPz());
            bank.setFloat("r", i, (float) vtx.get(i).getR()); 
        }
        
        //bank.show();
        return bank;
    }
}
