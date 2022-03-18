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
    
    public static DataBank VtxBank(DataEvent event, List<Vertex> vtx) { 
        DataBank bank = event.createBank("REC::VertDoca", vtx.size());
        if(vtx!=null) {
            for (int i = 0; i < vtx.size(); i++) {
                bank.setShort("index1", i, (short) vtx.get(i).get_HelixPair().get(0).getId());
                bank.setShort("index2", i, (short) vtx.get(i).get_HelixPair().get(1).getId());
                bank.setFloat("x", i, (float) vtx.get(i).get_Vertex().x());
                bank.setFloat("y", i, (float) vtx.get(i).get_Vertex().y());
                bank.setFloat("z", i, (float) vtx.get(i).get_Vertex().z());
                bank.setFloat("x1", i, (float) vtx.get(i).getTrack1POCA().x());
                bank.setFloat("y1", i, (float) vtx.get(i).getTrack1POCA().y());
                bank.setFloat("z1", i, (float) vtx.get(i).getTrack1POCA().z());
                bank.setFloat("cx1", i, (float) vtx.get(i).getTrack1POCADir().x());
                bank.setFloat("cy1", i, (float) vtx.get(i).getTrack1POCADir().y());
                bank.setFloat("cz1", i, (float) vtx.get(i).getTrack1POCADir().z());
                bank.setFloat("x2", i, (float) vtx.get(i).getTrack2POCA().x());
                bank.setFloat("y2", i, (float) vtx.get(i).getTrack2POCA().y());
                bank.setFloat("z2", i, (float) vtx.get(i).getTrack2POCA().z());
                bank.setFloat("cx2", i, (float) vtx.get(i).getTrack2POCADir().x());
                bank.setFloat("cy2", i, (float) vtx.get(i).getTrack2POCADir().y());
                bank.setFloat("cz2", i, (float) vtx.get(i).getTrack2POCADir().z());
                bank.setFloat("r", i, (float) vtx.get(i).getDoca()); 
            }
        }
        //bank.show();
        return bank;
    }
}
