package org.jlab.rec.cvt.fit;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple algorithm to calculate the center and radius of a circle that passes
 * through three points
 */
public class CircleCalculator {

    private CircleCalcPars _circleresult;

    // the constructor
    public CircleCalculator() {
    }
    // Find a circle that passes through 3 points
    // Has a boolean method to return the status
    // Status set to false if no circle can be found

    public boolean status(List<Double> P_0, List<Double> P_1, List<Double> P_2) {
        if (P_0 == null || P_1 == null || P_2 == null) {
            return false;
        }
        // sort points
        List<List<Double>> sortedPoints = SortPoints(P_0, P_1, P_2);
        List<Double> P0 = sortedPoints.get(0);
        List<Double> P1 = sortedPoints.get(1);
        List<Double> P2 = sortedPoints.get(2);
        if (P0 == null || P1 == null || P2 == null) {
            return false;
        }

        if (Math.abs(P1.get(0) - P0.get(0)) < 1.0e-18 || Math.abs(P2.get(0) - P1.get(0)) < 1.0e-18) {
            return false;
        }

        // Find the intersection of the lines joining the innermost to middle and middle to outermost point
        double ma = (P1.get(1) - P0.get(1)) / (P1.get(0) - P0.get(0));
        double mb = (P2.get(1) - P1.get(1)) / (P2.get(0) - P1.get(0));

        if (Math.abs(mb - ma) < 1.0e-18) {
            return false;
        }

        double xcen = 0.5 * (ma * mb * (P0.get(1) - P2.get(1)) + mb * (P0.get(0) + P1.get(0)) - ma * (P1.get(0) + P2.get(0))) / (mb - ma);
        double ycen = (-1. / mb) * (xcen - 0.5 * (P1.get(0) + P2.get(0))) + 0.5 * (P1.get(1) + P2.get(1));

        double CircRad = Math.sqrt((P0.get(0) - xcen) * (P0.get(0) - xcen) + (P0.get(1) - ycen) * (P0.get(1) - ycen));

        _circleresult = new CircleCalcPars(xcen, ycen, CircRad);

        return true;
    }

    public CircleCalcPars getCalc() {
        return _circleresult;
    }

    private List<List<Double>> SortPoints(List<Double> p0, List<Double> p1, List<Double> p2) {

        List<List<Double>> newArray = new ArrayList<>();

        List<Double> midpoint = null;
        List<Double> nearestToTarget = null;
        List<Double> farthestToTarget = null;

        double difsq01 = (p0.get(0) - p1.get(0)) * (p0.get(0) - p1.get(0)) + (p0.get(1) - p1.get(1)) * (p0.get(1) - p1.get(1));
        double difsq02 = (p0.get(0) - p2.get(0)) * (p0.get(0) - p2.get(0)) + (p0.get(1) - p2.get(1)) * (p0.get(1) - p2.get(1));
        double difsq12 = (p1.get(0) - p2.get(0)) * (p1.get(0) - p2.get(0)) + (p1.get(1) - p2.get(1)) * (p1.get(1) - p2.get(1));

        if (difsq01 > difsq02 && difsq01 > difsq12) {
            midpoint = p2;
        }
        if (difsq02 > difsq01 && difsq02 > difsq12) {
            midpoint = p1;
        }
        if (difsq12 > difsq01 && difsq12 > difsq02) {
            midpoint = p0;
        }

        double D0 = Math.pow(p0.get(0), 2) + Math.pow(p0.get(1), 2);
        double D1 = Math.pow(p1.get(0), 2) + Math.pow(p1.get(1), 2);
        double D2 = Math.pow(p2.get(0), 2) + Math.pow(p2.get(1), 2);

        if (D0 < D1 && D0 < D2) {
            nearestToTarget = p0;
            if (p1 == midpoint) {
                farthestToTarget = p2;
            } else {
                farthestToTarget = p1;
            }
        }
        if (D1 < D0 && D1 < D2) {
            nearestToTarget = p1;
            if (p0 == midpoint) {
                farthestToTarget = p2;
            } else {
                farthestToTarget = p0;
            }
        }
        if (D2 < D1 && D2 < D0) {
            nearestToTarget = p2;
            if (p1 == midpoint) {
                farthestToTarget = p0;
            } else {
                farthestToTarget = p1;
            }
        }

        // rearrange points
        newArray.add(nearestToTarget);
        newArray.add(midpoint);
        newArray.add(farthestToTarget);

        return newArray;
    }

}
