package org.jlab.geom.detector.dc;

import static java.lang.Math.*;
import java.util.*;

import org.jlab.geom.G4Volume;
import org.jlab.geom.CoordinateSystem;
import org.jlab.geom.prim.*;
import org.jlab.geom.detector.dc.*;

/**
 * \brief A layer that consists of sense and/or guard wires.
 *
 * There are typically two guard wires which surround many
 * sensewires, each of which can be on or off.
 **/
class Layer {

    DriftChamber dc;
    Sector sector;
    Region region;
    Superlayer superlayer;
    int index;

    int nGuardwires;
    int nSensewires;

    Layer(Superlayer superlayer) {
        this.superlayer = superlayer;
        this.region = superlayer.region;
        this.sector = region.sector;
        this.dc = sector.dc;
    }

    int nWires() {
        return nGuardwires + nSensewires;
    }

    int wireIndex(int idx) {
        if (idx<0) {
            idx = this.nWires() + idx;
        }
        return idx;
    }
    int sensewireIndex(int idx) {
        return this.wireIndex(idx) + 1;
    }

    /**
     * \brief x-position of the midpoint of a wire in this layer
     *
     * Midpoints are the intersection points of the wires with
     * the sector mid-plane.
     *
     * \param [in] w the wire index (starting from zero) in this layer
     * \return x position in sector coordinate system (cm)
     **/
    public double wireMidX(int w) {
        // r is the distance from the guard layer to this wire plane in cm.
        double r = ((double) index) * superlayer.cellthickness * superlayer.wpdist;
        double xmid = superlayer.firstWireMidX() + r * sin(region.thtilt);
        double wmidsp = superlayer.wireMidSpacing();

        // stagger wire planes
        double nwire = (double) this.wireIndex(w);
        if ((index % 2) == 1) {
            nwire += 0.5;
        }
        xmid += nwire * wmidsp * cos(region.thtilt);

        // mini-stagger
        if (region.index == 2)
        {
            if ((index % 2) == 1) {
                xmid += 0.03 * cos(region.thtilt);
            } else {
                xmid -= 0.03 * cos(region.thtilt);
            }
        }

        return xmid;
    }

    /**
     * \brief y-position of the midpoint of a wire in this layer
     *
     * The y-position of the midpoint of all wires in the
     * sector coordinate system is zero. Midpoints are the
     * intersection points of the wires with the sector
     * mid-plane.
     *
     * \param [in] w wire index from zero (ignored)
     * \return y position in sector coordinate system (cm)
     **/
    double wireMidY() {
        return 0.;
    }
    double wireMidY(int w) {
        return this.wireMidY();
    }

    /**
     * \brief z-position of the midpoint of a wire in this layer
     *
     * Midpoints are the intersection points of the wires with
     * the sector mid-plane.
     *
     * \param [in] w the wire index (starting from zero) in this layer
     * \return z position in sector coordinate system (cm)
     **/
    double wireMidZ(int w) {
        // r is the distance from the guard layer to this wire plane in cm.
        double r = ((double) index) * superlayer.cellthickness * superlayer.wpdist;
        double zmid = superlayer.firstWireMidZ() + r * cos(region.thtilt);
        double wmidsp = superlayer.wireMidSpacing();

        // stagger even wire planes
        double nwire = (double) this.wireIndex(w);
        if ((index % 2) == 1) {
            nwire += 0.5;
        }
        zmid -= nwire * wmidsp * sin(region.thtilt);

        // mini-stagger
        if (region.index == 2) {
            if ((index % 2) == 1) {
                zmid -= 0.03 * sin(region.thtilt);
            } else {
                zmid += 0.03 * sin(region.thtilt);
            }
        }

        return zmid;
    }

    /**
     * \brief position of the midpoint of a wire in this layer
     *
     * Midpoints are the intersection points of the wires with
     * the sector mid-plane.
     *
     * \param [in] w the wire index (starting from zero) in this layer
     * \return (x,y,z) position in sector coordinate system (cm)
     **/
    Vector3D wireMid(int w, CoordinateSystem coord) {
        Vector3D ret = new Vector3D(
            this.wireMidX(w),
            this.wireMidY(w),
            this.wireMidZ(w) );
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

    /**
     * \brief x-positions of the midpoints of all wires in this layer
     *
     * Midpoints are the intersection points of the wires with
     * the sector mid-plane.
     *
     * \return x positions in sector coordinate system (cm)
     **/
    Vector<Double> wiresMidX() {
        Vector<Double> ret = new Vector<Double>(this.nWires());
        for (int w=0; w<ret.size(); w++) {
            ret.set(w,this.wireMidX(w));
        }
        return ret;
    }

    /**
     * \brief y-positions of the midpoints of all wires in this layer
     *
     * Midpoints are the intersection points of the wires with
     * the sector mid-plane.
     *
     * \return y positions in sector coordinate system (cm)
     **/
    Vector<Double> wiresMidY() {
        Vector<Double> ret = new Vector<Double>(this.nWires());
        for (int w=0; w<ret.size(); w++) {
            ret.set(w,this.wireMidY());
        }
        return ret;
    }

    /**
     * \brief z-positions of the midpoints of all wires in this layer
     *
     * Midpoints are the intersection points of the wires with
     * the sector mid-plane.
     *
     * \return z positions in sector coordinate system (cm)
     **/
    Vector<Double> wiresMidZ() {
        Vector<Double> ret = new Vector<Double>(this.nWires());
        for (int w=0; w<ret.size(); w++) {
            ret.set(w,this.wireMidZ(w));
        }
        return ret;
    }

    /**
     * \brief vector of midpoints of all wires in this layer
     *
     * Midpoints are the intersection points of the wires with
     * the sector mid-plane.
     *
     * \return (x,y,z) positions in sector coordinate system (cm)
     **/
    Vector<Vector3D> wiresMid(CoordinateSystem coord) {
        Vector<Vector3D> ret = new Vector<Vector3D>(this.nWires());
        for (int w=0; w<ret.size(); w++) {
            ret.set(w,this.wireMid(w,coord));
        }
        return ret;
    }

    /**
     * \brief position of the center point of a wire in this layer
     *
     * This is the wire's center point. Not to be confused with
     * the "midpoint" which is the intersection point of this
     * wire with the sector's mid-plane.
     *
     * \param [in] w the wire index (starting from zero) in this layer
     * \return (x,y,z) position in sector coordinate system (cm)
     **/
    Vector3D wireCenter(int w, CoordinateSystem coord) {
        return this.wire(w,coord).midpoint().toVector3D();
    }

    /**
     * \brief the center-point of this sense-layer volume
     * \return (x,y,z) position in sector coordinate system (cm)
     **/
    Vector3D center(CoordinateSystem coord) {
        return this.wireCenter(0,coord).add(this.wireCenter(-1,coord)).multiply(0.5);
    }

    /**
     * \brief distance from the origin to this wire plane
     *
     * The origin is (0,0,0) in the CLAS coordinate system
     *
     * \return distance (cm)
     **/
    double dist2tgt() {
        return superlayer.dist2tgt() + index * superlayer.layerThickness();
    }

    /**
     * \brief all 3D line segments representing guard and sense wires in this layer
     * \return vector of line segments in sector coordinate system (cm)
     **/
    Vector<Line3D> wires(CoordinateSystem coord) {
        Vector<Line3D> ret = new Vector<Line3D>();

        // end plates of this region
        Plane3D lplate = region.leftEndPlate(coord);
        Plane3D rplate = region.rightEndPlate(coord);

        Vector3D wd = superlayer.wireDirection(coord);
        Point3D ileft = new Point3D();
        Point3D iright = new Point3D();
        for (int idx=0; idx<this.nWires(); idx++) {
            // wire as a line
            Line3D wireLine = new Line3D(this.wireMid(idx,coord).toPoint3D(), wd);

            // get the intersection and create line segment from one
            // point to the other.
            lplate.intersection(wireLine, ileft);
            rplate.intersection(wireLine, iright);
            ret.add(new Line3D(ileft,iright));
        }
        return ret;
    }

    /**
     * \brief 3D line segment representing a wire
     * \param [in] w the wire index (starting from zero) in this layer
     * \return line segment with end-points in sector coordinate system (cm)
     **/
    Line3D wire(int w, CoordinateSystem coord) {
        // end plates of this region
        Plane3D lplate = region.leftEndPlate(coord);
        Plane3D rplate = region.rightEndPlate(coord);

        // wire as a line
        Line3D wireLine = new Line3D(
            this.wireMid(w,coord).toPoint3D(),
            superlayer.wireDirection(coord) );

        // get the intersection and create line segment from one
        // point to the other.
        Point3D ileft = new Point3D();
        Point3D iright = new Point3D();
        lplate.intersection(wireLine, ileft);
        rplate.intersection(wireLine, iright);
        return new Line3D(ileft,iright);
    }

    /**
     * \brief length of a given sense wire
     * \param [in] w the wire index (starting from zero) in this layer
     * \return length (cm)
     **/
    double wireLength(int w) {
        return this.wire(w,CoordinateSystem.SECTOR).length();
    }

    /**
     * \brief this sense layer's wire-plane
     * \return plane(point on plane, normal) in sector coordinate system (cm)
     **/
    Plane3D wirePlane(CoordinateSystem coord) {
        return new Plane3D(
            new Point3D(this.wireMid(0,coord)),
            superlayer.wireDirection(coord) );
    }

    Line3D sensewire(int w, CoordinateSystem coord) {
        return this.wire(this.sensewireIndex(w), coord);
    }

    Vector<Line3D> sensewires(CoordinateSystem coord) {
        Vector<Line3D> ret = this.wires(coord);
        return new Vector<Line3D>(ret.subList(1,this.nWires()-1));
    }


    String g4Name() {
        return new String("L"+index+"_"+superlayer.g4Name());
    }

    String description() {
        return new String(superlayer.description()+" Layer "+index);
    }

    /**
     * generating the trapezoid parameters for this layer
     * following the G4Trap constructor:
     *     pDz     Half-length along the z-axis
     *     pTheta  Polar angle of the line joining the centres of the faces
     *             at -/+pDz
     *     pPhi    Azimuthal angle of the line joing the centre of the face at
     *             -pDz to the centre of the face at +pDz
     *     pDy1    Half-length along y of the face at -pDz
     *     pDx1    Half-length along x of the side at y=-pDy1 of the face at -pDz
     *     pDx2    Half-length along x of the side at y=+pDy1 of the face at -pDz
     *     pAlp1   Angle with respect to the y axis from the centre of the side
     *             at y=-pDy1 to the centre at y=+pDy1 of the face at -pDz
     *
     *     pDy2    Half-length along y of the face at +pDz
     *     pDx3    Half-length along x of the side at y=-pDy2 of the face at +pDz
     *     pDx4    Half-length along x of the side at y=+pDy2 of the face at +pDz
     *     pAlp2   Angle with respect to the y axis from the centre of the side
     *             at y=-pDy2 to the centre at y=+pDy2 of the face at +pDz
     *
     * \return map of strings to strings: value = ret.get(param_name)
    **/
    G4Volume g4Volume(CoordinateSystem coord) {
        // all done in sector coordinate system. ///////////////////////////////

        // 100 um gap between layers (to avoid G4 volume overlap)
        final double microgap = 0.01;

        double hflyrthk = 0.5 * superlayer.layerThickness();
        Vector3D half_lyr_thickness = new Vector3D(
            hflyrthk * sin(region.thtilt),
            0.,
            hflyrthk * cos(region.thtilt) );

        // volume edges as infinitely-extending lines
        // The first two edges are the wire-lines displaced by
        // half a layer-thickness in the direction of (0,0,0).
        // The last two are displaced away from the origin.
        Line3D edge00_line = new Line3D(
            this.wireMid( 0,coord).sub(half_lyr_thickness).toPoint3D(),
            superlayer.wireDirection(coord) );
        Line3D edge01_line = new Line3D(
            this.wireMid(-1,coord).sub(half_lyr_thickness).toPoint3D(),
            superlayer.wireDirection(coord) );
        Line3D edge10_line = new Line3D(
            this.wireMid( 0,coord).add(half_lyr_thickness).toPoint3D(),
            superlayer.wireDirection(coord) );
        Line3D edge11_line = new Line3D(
            this.wireMid(-1,coord).add(half_lyr_thickness).toPoint3D(),
            superlayer.wireDirection(coord) );

        // get the intersection and create line segment from one
        // point to the other. These are the same lines as above
        // but with endpoints at the left and right end-planes.
        Point3D edge00_lplate_int = new Point3D();
        Point3D edge00_rplate_int = new Point3D();
        Point3D edge01_lplate_int = new Point3D();
        Point3D edge01_rplate_int = new Point3D();
        Point3D edge10_lplate_int = new Point3D();
        Point3D edge10_rplate_int = new Point3D();
        Point3D edge11_lplate_int = new Point3D();
        Point3D edge11_rplate_int = new Point3D();

        // end plates of this region
        Plane3D lplate = region.leftEndPlate(coord);
        Plane3D rplate = region.rightEndPlate(coord);

        lplate.intersection(edge00_line, edge00_lplate_int);
        rplate.intersection(edge00_line, edge00_rplate_int);
        lplate.intersection(edge01_line, edge01_lplate_int);
        rplate.intersection(edge01_line, edge01_rplate_int);
        lplate.intersection(edge10_line, edge00_lplate_int);
        rplate.intersection(edge10_line, edge00_rplate_int);
        lplate.intersection(edge11_line, edge01_lplate_int);
        rplate.intersection(edge11_line, edge01_rplate_int);

        Line3D edge00 = new Line3D(edge00_lplate_int, edge00_rplate_int);
        Line3D edge01 = new Line3D(edge01_lplate_int, edge01_rplate_int);
        Line3D edge10 = new Line3D(edge10_lplate_int, edge10_rplate_int);
        Line3D edge11 = new Line3D(edge11_lplate_int, edge11_rplate_int);

        // Layer closer to the origin:
        // p0 = projection of the 1st edge's midpoint onto the
        //      line representing the longer edge on the same layer.
        // d00 = direction from the 1st edges's midpoint to p0
        // d01 = direction from the 1st to the 2nd edges's midpoint
        Vector3D p0 = edge01_line.projection(edge00.midpoint().toVector3D());
        Vector3D d00 = p0.sub(edge00.midpoint().toVector3D()).asUnit();
        Vector3D d01 = edge01.midpoint().toVector3D().sub(edge00.midpoint().toVector3D()).asUnit();

        // Layer farther away from the origin:
        // p1 = projection of the 1st edge's midpoint onto the
        //      line representing the longer edge on the same layer.
        Vector3D p1 = edge11_line.projection(edge10.midpoint().toVector3D());

        // get the sign of the alp angle for the trapezoid
        double sign_of_alp1 = 1;
        if ((edge01.midpoint().y() - p0.y()) < 0.)
        {
            sign_of_alp1 = -1;
        }

        double dz    = hflyrthk - microgap;
        double theta = - region.thtilt;
        double phi   = 0.5 * PI;
        double dy1   = 0.5 * (new Line3D(edge00.midpoint(), p0)).length();
        double dx1   = 0.5 * edge00.length();
        double dx2   = 0.5 * edge01.length();
        double alp1  = sign_of_alp1 * d00.angle(d01);
        double dy2   = 0.5 * (new Line3D(edge10.midpoint(), p1)).length();
        double dx3   = 0.5 * edge10.length();
        double dx4   = 0.5 * edge11.length();
        double alp2  = alp1;

        // d = position of layer volume relative to the region (mother volume)
        Vector3D d = this.center(coord).sub(region.center(coord));

        // rotate about the y-axis in sector coordinates by the region's tilt
        // this is done because the trapezoids are defined by the edges
        // and the whole volume is then rotated to get the final position
        d.setXYZ(cos(region.thtilt)*d.x() - sin(region.thtilt)*d.z(),
                 d.y(),
                 cos(region.thtilt)*d.z() + sin(region.thtilt)*d.x());

        // x and y are reversed for gemc's coordinate system
        String layer_pos = new String(
            d.y() + "*cm " +
            d.x() + "*cm " +
            d.z() + "*cm");

        String layer_rot = new String(
            "0*deg " +
            "0*deg " +
            toDegrees(superlayer.thster) + "*deg");

        String layer_dim = new String(
            dz + "*cm " +
            toDegrees(theta) + "*deg " +
            toDegrees(phi) + "*deg " +
            dy1 + "*cm " +
            dx1 + "*cm " +
            dx2 + "*cm " +
            toDegrees(alp1) + "*deg " +
            dy2 + "*cm " +
            dx3 + "*cm " +
            dx4 + "*cm " +
            toDegrees(alp2) + "*deg");

        String layer_ids = new String(
            "sector ncopy 0 " +
            "superlayer manual " + (superlayer.index+1) + " " +
            "layer manual " + (index+1) + " " +
            "wire manual 1");

        // The (Sense)Layer volume
        G4Volume vol = new G4Volume();
        vol.put("mother", region.g4Name());
        vol.put("description", this.description());
        vol.put("pos", layer_pos);
        vol.put("rotation", layer_rot);
        vol.put("color", "66aadd");
        vol.put("type", "G4Trap");
        vol.put("dimensions", layer_dim);
        vol.put("material", "DCgas");
        vol.put("mfield", "no");
        vol.put("ncopy", "1");
        vol.put("pMany", "1");
        vol.put("exist", "1");
        vol.put("visible", "1");
        vol.put("style", "1");
        vol.put("sensitivity", "DC");
        vol.put("hit_type", "DC");
        vol.put("identifiers", layer_ids);
        return vol;
    }
}
