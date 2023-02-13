package org.jlab.rec.urwell.reader;

import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.base.DetectorType;

/**
 *
 * @author Tongtong Cao
 */

public class URWellCluster {

    private DetectorDescriptor desc = new DetectorDescriptor(DetectorType.URWELL);
    private int size = 0;
    private double energy = 0;
    private double time = 0;
    private int crossIndex = -1;

    public URWellCluster(int sector, int layer, int component, int size, double energy, double time) {
        this.desc.setSectorLayerComponent(sector, layer, component);
        this.size = size;
        this.energy = energy;
        this.time = time;
    }

    public int sector() {
        return this.desc.getSector();
    }

    public int layer() {
        return this.desc.getLayer();
    }

    public int strip() {
        return this.desc.getComponent();
    }

    public int size() {
        return size;
    }

    public double energy() {
        return energy;
    }

    public double time() {
        return time;
    }

    public int getCrossIndex() {
        return crossIndex;
    }

    public void setCrossIndex(int crossIndex) {
        this.crossIndex = crossIndex;
    }

}
