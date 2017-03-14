package org.jlab.geom.detector.ftof;

import static java.lang.Math.*;
import java.util.*;

import org.jlab.geom.G4Volume;
import org.jlab.geom.CoordinateSystem;
import org.jlab.geom.prim.*;
import org.jlab.geom.detector.ftof.*;

/** \class Paddle
 * \brief A single Forward TOF paddle
 **/
class Paddle {

    ForwardTOF ftof;
    Sector sector;
    Panel panel;
    int index;

    double meas_length;
    double slope;
    double intercept;

    Paddle(Panel panel) {
        this.panel = panel;
        this.sector = panel.sector;
        this.ftof = sector.ftof;
    }

    double centerX() {
        double p2pdist;
        double gaptot;

        double costhtilt = cos(panel.thtilt);
        double dist2edge = panel.dist2edge;
        double thmin = panel.thmin;
        double wrapper_thickness = panel.wrapper_thickness;
        double width = panel.paddle_width;
        double gap = panel.paddle_gap;
        double pairgap = panel.paddle_pairgap;

        double x = dist2edge * sin(thmin) + (0.5*width+wrapper_thickness) * costhtilt;

        if (panel.name() == "1b") {
            p2pdist = width + 2.*wrapper_thickness;
            gaptot = ((index+1)/2) * gap + (index/2) * pairgap;
            x += (index * p2pdist + gaptot) * costhtilt;
        } else {
            p2pdist = width + gap + 2.*wrapper_thickness;
            x += index * p2pdist * costhtilt;
        }

        // at this point, we have the face edge (outside of the wrapper)
        // we now move to the center of the volume
        //x += (0.5*_paddle_thickness + _wrapper_thickness) * sin(_thtilt);

        return x;
    }
    double centerY() {
        return 0;
    }
    double centerZ() {
        double p2pdist;
        double gaptot;

        double dist2edge = panel.dist2edge;
        double thmin = panel.thmin;
        double thtilt = panel.thtilt;
        double wrapper_thickness = panel.wrapper_thickness;
        double width = panel.paddle_width;
        double gap = panel.paddle_gap;
        double pairgap = panel.paddle_pairgap;

        double z = dist2edge * cos(thmin) - (0.5*width+wrapper_thickness) * sin(thtilt);

        if (panel.name() == "1b") {
            p2pdist = width + 2.*wrapper_thickness;
            gaptot = ((index+1)/2) * gap + (index/2) * pairgap;
            z -= (index * p2pdist + gaptot) * sin(thtilt);
        } else {
            p2pdist = width + gap + 2.*wrapper_thickness;
            z -= index * p2pdist * sin(thtilt);
        }

        // at this point, we have the face edge (outside of the wrapper)
        // we now move to the center of the volume
        //z -= (0.5*_paddle_thickness + _wrapper_thickness) * cos(_thtilt);

        return z;
    }

    Vector3D center(CoordinateSystem coord) {
        Vector3D ret = new Vector3D(this.centerX(), this.centerY(), this.centerZ());
        switch (coord) {
            case SECTOR:
                // do nothing
                break;
            case CLAS:
                ret = sector.sectorToCLAS(ret);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return ret;
    }

    double length() {
        return slope*(index+1) + intercept;
    }
}
