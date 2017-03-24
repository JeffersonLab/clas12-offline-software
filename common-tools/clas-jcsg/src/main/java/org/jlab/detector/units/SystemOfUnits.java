/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.units;

/**
 *
 * @author from Geant4 System of Units
 */
public final class SystemOfUnits {

    private SystemOfUnits() {
    }

    public static final class Length {

        private Length() {
        }

        public static Measurement value(double value) {
            return new Measurement(value, "cm");
        }

        public static String unit() {
            return "cm";
        }

        public static final double centimeter = 1.;
        public static final double millimeter = 0.1;
        public static final double meter = 1000. * millimeter;
        public static final double kilometer = 1000. * meter;
        public static final double micrometer = 1.e-6 * meter;
        public static final double nanometer = 1.e-9 * meter;
        public static final double mm = millimeter;
        public static final double cm = centimeter;
        public static final double m = meter;
        public static final double km = kilometer;
        public static final double in = 2.54 * cm;
    }

    public static final class Angle {

        private Angle() {
        }

        public static Measurement value(double value) {
            return new Measurement(value, "rad");
        }

        public static String unit() {
            return "rad";
        }

        public static final double radian = 1.;
        public static final double milliradian = 1.e-3 * radian;
        public static final double degree = Math.toDegrees(radian);

        public static final double rad = radian;
        public static final double mrad = milliradian;
        public static final double deg = degree;
    }
}

/*
public final class SystemOfUnits {
public static final double millimeter2 = millimeter*millimeter;
public static final double millimeter3 = millimeter*millimeter*millimeter;

public static final double centimeter2 = centimeter*centimeter;
public static final double centimeter3 = centimeter*centimeter*centimeter;

public static final double meter2 = meter*meter;
public static final double meter3 = meter*meter*meter;

public static final double kilometer2 = kilometer*kilometer;
public static final double kilometer3 = kilometer*kilometer*kilometer;

public static final double mm2 = millimeter2;
public static final double mm3 = millimeter3;

public static final double cm2 = centimeter2;
public static final double cm3 = centimeter3;

public static final double m2 = meter2;
public static final double m3 = meter3;

public static final double km2 = kilometer2;
public static final double km3 = kilometer3;

//
// Angle
//

//
// Time [T]
//

public static final double nanosecond  = 1.;
public static final double second      = 1.e+9 *nanosecond;
public static final double millisecond = 1.e-3 *second;
public static final double microsecond = 1.e-6 *second;
public static final double picosecond = 1.e-12*second;

public static final double ns = nanosecond;			
public static final double  s = second;
public static final double ms = millisecond;


 */
