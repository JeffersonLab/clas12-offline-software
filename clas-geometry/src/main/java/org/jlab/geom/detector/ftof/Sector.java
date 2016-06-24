package org.jlab.geom.detector.ftof;

import java.util.*;

import org.jlab.geom.G4VolumeMap;
import org.jlab.geom.CoordinateSystem;
import org.jlab.geom.prim.*;
import org.jlab.geom.detector.ftof.*;

/**
 * \brief a sector of the forward TOF which consists of several panels
 *
 * There are six sectors of forward TOF in CLAS12 which hold
 * three panels
 **/
class Sector {

    ForwardTOF ftof;
    ArrayList<Panel> panels;
    int index;

    Sector(ForwardTOF ftof) {
        this.ftof = ftof;
        this.panels = new ArrayList<Panel>();
    }

    int nPanels() {
        return panels.size();
    }

    /**
     * \brief convert negative indexes to positive counting from end
     * \param [in] idx index either from zero or from -1 (counting from end)
     * \return unsigned int index of the region in this sector
     **/
    int panelIndex(int idx) {
        if (idx<0) {
            idx = this.nPanels() + idx;
        }
        return idx;
    }

    Panel panel(int idx) {
        return panels.get(this.panelIndex(idx));
    }

    Panel panel(String id) {
        return panels.get(ftof.panelIndex(id));
    }

    Vector3D sectorToCLAS(Vector3D v) {
        final double sector_phi = 3.1415926535898 / 3.;
        Vector3D ret = v.clone();
        if (index != 0) {
            ret.rotateZ(index * sector_phi);
        }
        return ret;
    }
    Point3D sectorToCLAS(Point3D p) {
        return this.sectorToCLAS(p.toVector3D()).toPoint3D();
    }
    Line3D sectorToCLAS(Line3D l) {
        return new Line3D(
            this.sectorToCLAS(l.origin()),
            this.sectorToCLAS(l.end()) );
    }
    Plane3D sectorToCLAS(Plane3D p) {
        return new Plane3D(
            this.sectorToCLAS(p.point()),
            this.sectorToCLAS(p.normal()) );
    }

    String g4Name() {
        return new String("S"+(index+1));
    }

    String description() {
        return new String(ftof.description()+" Sector "+(index+1));
    }

    /*
    G4VolumeMap g4Volumes(CoordinateSystem coord) {
        G4VolumeMap vols = new G4VolumeMap();
        for (Panel panel : panels) {
            vols.put(panel.g4Name(),panel.g4Volume(coord));
        }
        return vols;
    }
    */
}
