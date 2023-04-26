package org.jlab.clas.detector;

import java.util.Comparator;

/**
 *
 * @author baltzell
 */
public class DetectorResponseComparators {
    
    public static final LayerEnergyCompare layerEnergy = new LayerEnergyCompare();
    public static final EnergyCompare energy = new EnergyCompare();
    public static final TimeCompare time = new TimeCompare();
    public static final PathCompare path = new PathCompare();
    
    /*
    Order by energy, largest to smallest
    */
    public static final class EnergyCompare implements Comparator<DetectorResponse> {
        @Override
        public int compare(DetectorResponse a, DetectorResponse b) {
            if (a.getEnergy() > b.getEnergy()) return -1;
            if (a.getEnergy() < b.getEnergy()) return  1;
            return 0;
        }
    }
    /*
    Order by time, smallest to largest
    */
    public static final class TimeCompare implements Comparator<DetectorResponse> {
        @Override
        public int compare(DetectorResponse a, DetectorResponse b) {
            if (a.getTime() < b.getTime()) return -1;
            if (a.getTime() > b.getTime()) return  1;
            return 0;
        }
    }
    /*
    Order by path, smallest to largest
    */
    public static final class PathCompare implements Comparator<DetectorResponse> {
        @Override
        public int compare(DetectorResponse a, DetectorResponse b) {
            if (a.getPath() < b.getPath()) return -1;
            if (a.getPath() > b.getPath()) return  1;
            return 0;
        }
    }

    /*
    Ordering by layer, smallest to largest, and within each layer by energy,
    largest to smallest.
    */
    public static final class LayerEnergyCompare implements Comparator<DetectorResponse> {
        @Override
        public int compare(DetectorResponse a, DetectorResponse b) {
            if (a.getDescriptor().getLayer() < b.getDescriptor().getLayer()) return -1;
            if (a.getDescriptor().getLayer() > b.getDescriptor().getLayer()) return  1;
            if (a.getEnergy() > b.getEnergy()) return -1;
            if (a.getEnergy() < b.getEnergy()) return  1;
            return 0;
        }
    }

}
