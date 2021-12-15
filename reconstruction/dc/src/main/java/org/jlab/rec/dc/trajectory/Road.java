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
