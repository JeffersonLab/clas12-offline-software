package org.jlab.geom.detector.dc;

import static java.lang.Math.*;
import java.util.*;

import org.jlab.geom.G4Volume;
import org.jlab.geom.CoordinateSystem;
import org.jlab.geom.prim.*;
import org.jlab.geom.detector.dc.*;

/** \class Region
 * \brief A region of superlayers in a sector of the drift chambers
 *
 * There are three regions in each sector of CLAS12
 **/
class Region {

    DriftChamber dc;
    Sector sector;
    List<Superlayer> superlayers;
    int index;

    double dist2tgt;
    double frontgap;
    double midgap;
    double backgap;
    double thopen;
    double thtilt;
    double xdist;

    Region(Sector sector) {
        this.sector = sector;
        this.dc = sector.dc;
        this.superlayers = new ArrayList<Superlayer>();
    }

    int nSuperlayers() {
        return superlayers.size();
    }

    int superlayerIndex(int idx) {
        if (idx<0) {
            idx = this.nSuperlayers() + idx;
        }
        return idx;
    }

    Superlayer superlayer(int idx) {
        return superlayers.get(this.superlayerIndex(idx));
    }

    /**
     * \brief Get the thickness of this region
     *
     * This is sum of the frontgap, midgap, backgap and the thicknesses
     * of each superlayer in this region.
     *
     * \return thickness of this region
     **/
    double thickness() {
        // sum up the gaps
        double thickness = frontgap + midgap + backgap;
        // add each superlayer's thickness
        for (Superlayer slyr : superlayers) {
            thickness += slyr.thickness();
        }
        return thickness;
    }

    /**
     * \brief The left end-plane plane
     * \return plane(point on plane, normal) in sector coordinate system (cm)
     **/
    Plane3D leftEndPlate(CoordinateSystem coord) {
        // first, calculate the plane in sector coords
        Point3D point = new Point3D(xdist,0,0);
        Vector3D norm = new Vector3D(sin(0.5*thopen),cos(0.5*thopen),0).asUnit();
        Plane3D ret = new Plane3D(point,norm);
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
     * \brief The right end-plane plane
     * \return plane(point on plane, normal) in sector coordinate system (cm)
     **/
    Plane3D rightEndPlate(CoordinateSystem coord) {
        // first, calculate the plane in sector coords
        Point3D point = new Point3D(xdist,0,0);
        Vector3D norm = new Vector3D(sin(0.5*thopen),-cos(0.5*thopen),0).asUnit();
        Plane3D ret = new Plane3D(point,norm);
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
     * \brief The center-point of this region
     *
     * The x and z coordinates are taken as the midpoint between
     * these two point:
     * p0 = endpoint of the first guard wire in the first (gaurd)
     *      wire-plane in the first superlayer of this region
     * p1 = endpoint of the last guard wire in the last (guard)
     *      wire-plane in the last superlayer of this region
     * The y-coordinate is always zero since this is in the sector
     * coordinate system.
     *
     * \return (x,y,z) position in sector-coordinates of this region (cm)
     **/
    Vector3D center(CoordinateSystem coord) {
        Vector3D p0 = this.superlayer( 0).guardlayer( 0).wire( 0,coord).end().toVector3D();
        Vector3D p1 = this.superlayer(-1).guardlayer(-1).wire(-1,coord).end().toVector3D();
        Vector3D regionCenter = p0.add(p1).multiply(0.5);
        regionCenter.setY(0);
        return regionCenter;
    }


    String g4Name() {
        return new String("R"+(index+1)+"_S"+(sector.index+1));
    }

    String description() {
        return new String(sector.description()+" Region "+(index+1));
    }

    /**
     * \brief generate the mother volume of a DC Region for input into gemc/geant4
     *
     * The numbers calculated are following Geant4's G4Trap constructor:
     *     pDz     Half-length along the z-axis
     *     pTheta  Polar angle of the line joining the centres of the faces
     *             at -/+pDz
     *     pPhi    Azimuthal angle of the line joing the centre of the face
     *             at -pDz to the centre of the face at +pDz
     *     pDy1    Half-length along y of the face at -pDz
     *     pDx1    Half-length along x of the side at y=-pDy1 of the face at -pDz
     *     pDx2    Half-length along x of the side at y=+pDy1 of the face at -pDz
     *     pAlp1   Angle with respect to the y axis from the centre of the
     *             side at y=-pDy1 to the centre at y=+pDy1 of the face at -pDz
     *
     *     pDy2    Half-length along y of the face at +pDz
     *     pDx3    Half-length along x of the side at y=-pDy2 of the face at +pDz
     *     pDx4    Half-length along x of the side at y=+pDy2 of the face at +pDz
     *     pAlp2   Angle with respect to the y axis from the centre of the
     *             side at y=-pDy2 to the centre at y=+pDy2 of the face at +pDz
     *
     * \return map of strings to strings: value = ret.get(param_name)
     **/
    G4Volume g4Volume(CoordinateSystem coord) {

        // first and last guard wire endpoints
        Vector3D guardwire0_endpoint = this.superlayer( 0).guardlayer( 0).wire( 0,coord).end().toVector3D();
        Vector3D guardwire1_endpoint = this.superlayer(-1).guardlayer(-1).wire(-1,coord).end().toVector3D();

        // region center-point in sector coordinates
        Vector3D region_center = this.center(coord);

        // x and y are reversed for gemc's coordinate system
        double dz    = 0.5 * this.thickness();
        double theta = - thtilt;
        double phi   = 0.5 * PI;
        double dy1   = 0.5 * (guardwire1_endpoint.x() - guardwire0_endpoint.x())
                / cos(thtilt);
        double dx1   = guardwire0_endpoint.y();
        double dx2   = guardwire1_endpoint.y();
        double alp1  = 0.;
        double dy2   = dy1;
        double dx3   = dx1;
        double dx4   = dx2;
        double alp2  = alp1;

        // x and y are reversed for gemc's coordinate system
        String region_pos = new String(
            region_center.y()+"*cm " +
            region_center.x()+"*cm " +
            region_center.z()+"*cm");

        String region_rot = new String(
            "ordered: zxy " +
            ( 90. + toDegrees(this.thtilt)) + "*deg " +
            (-90. - 60.*index) + "*deg " +
            0 + "*deg");

        String region_dim = new String(
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

        // The Region mother volume
        G4Volume vol = new G4Volume();
        vol.put("mother", "root");
        vol.put("description", this.description());
        vol.put("pos", region_pos);
        vol.put("rotation", region_rot);
        vol.put("color", "aa0000");
        vol.put("type", "G4Trap");
        vol.put("dimensions", region_dim);
        vol.put("material", "DCgas");
        vol.put("mfield", "no");
        vol.put("ncopy", "1");
        vol.put("pMany", "1");
        vol.put("exist", "1");
        vol.put("visible", "1");
        vol.put("style", "0");
        vol.put("sensitivity", "no");
        vol.put("hit_type", "");
        vol.put("identifiers", "");
        return vol;
    }

}
