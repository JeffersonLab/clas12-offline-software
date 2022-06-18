package org.jlab.clas.tracking.kalmanfilter;

/**
 *
 * @author ziegler
 */
    public enum Units {
        MM (10.0),
        CM  (1.0);

        private final double unit;  
        
        Units(double unit) {
            this.unit = unit;
        }
        
        public double value() { 
            return unit; 
        }
        
        public static Units getUnit(double value) {
            for (Units unit : Units.values()) {
                if (unit.unit == value) {
                    return unit;
                }
            }
            return Units.CM;
        }
    }

