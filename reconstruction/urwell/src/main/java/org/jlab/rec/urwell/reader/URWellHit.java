package org.jlab.rec.urwell.reader;

import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.base.DetectorType;

/**
 *
 * @author Tongtong Cao
 */

public class URWellHit {

    private DetectorDescriptor desc = new DetectorDescriptor(DetectorType.URWELL);
    private double energy = 0;
    private double time = 0;

    public URWellHit(int sector, int layer, int component, double energy, double time) {
        this.desc.setSectorLayerComponent(sector, layer, component);
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

    public double energy() {
        return energy;
    }

    public double time() {
        return time;
    }

}
