/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.dc.trajectory;

import java.util.ArrayList;
import org.jlab.rec.dc.segment.Segment;

/**
 *
 * @author ziegler
 */
public class Road extends ArrayList<Segment>{

    public int id;
    public double[] a = new double[3]; // fit params
}
