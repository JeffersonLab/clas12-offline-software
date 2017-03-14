/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.io.stream;

import java.util.TreeMap;

/**
 *
 * @author gavalian
 */
public interface EvioStreamObject {
    int  getType();
    TreeMap<Integer,Object> getStreamData();
    void setStreamData(TreeMap<Integer,Object> data);
}
