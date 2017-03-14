package org.jlab.geom.detector.dc;

import static java.lang.Math.*;
import java.util.*;

import org.jlab.geom.CoordinateSystem;
import org.jlab.geom.prim.*;
import org.jlab.geom.detector.dc.*;


/**
 * \brief Superlayers of wire-planes contained within a single region.
 *
 * This consists of several senselayers surrounded by guard layers.
 *
 * the angles Region::thtilt() and thmin() are used in conjunction
 * with Region::dist2tgt() to get the wire position midpoints:
 * \image html dc.png "Superlayer position determination"
 * \image latex dc.eps "Superlayer position determination"
 *
 * The wires in a given superlayer include guard, field and
 * sense wires as indicated here:
 * \image html superlayer_wires.png "Wires, wire planes and volumes"
 * \image latex superlayer_wires.eps "Wires, wire planes and volumes"
 **/
class Superlayer {

    DriftChamber dc;
    Sector sector;
    Region region;
    List<Layer> layers;
    int index;

    int nfieldlayers;
    double thster;
    double thmin;
    double wpdist;
    double cellthickness;

    Superlayer(Region region) {
        this.region = region;
        this.sector = region.sector;
        this.dc = sector.dc;
        this.layers = new ArrayList<Layer>();
    }

    /**
     * \brief Get the thickness of a hexagonal cell-layer
     *
     * This is the wire-plane thickness times Superlayer::cellthickness()
     * which is the number of wire-planes that make up a hexagonal cell.
     *
     * \return thickness
     **/
    double layerThickness() {
        return cellthickness * wpdist;
    }

    /**
     * \brief Get the total thickness of this superlayer
     *
     * This is the distance between the first and last wire plane.
     *
     * \return thickness
     **/
    double thickness() {
        return (this.nLayers() - 1) * this.layerThickness();
    }

    int nLayers() {
        return layers.size();
    }
    int nGuardlayers() {
        return 2;
    }
    int nSenselayers() {
        return layers.size() - this.nGuardlayers();
    }

    int layerIndex(int idx) {
        if (idx<0) {
            idx = this.nLayers() + idx;
        }
        return idx;
    }
    int guardlayerIndex(int idx) {
        return this.layerIndex(idx);
    }
    int senselayerIndex(int idx) {
        if (idx<0) {
            idx = this.nSenselayers() + idx;
        }
        return idx + 1;
    }

    Layer layer(int idx) {
        return layers.get(this.layerIndex(idx));
    }
    Layer senselayer(int idx) {
        return layers.get(this.senselayerIndex(idx));
    }
    Layer guardlayer(int idx) {
        return layers.get(this.guardlayerIndex(idx));
    }

    List<Layer> senselayers() {
        return layers.subList(this.senselayerIndex(0), this.senselayerIndex(-1)+1);
    }

    /**
     * \brief Get the total number of wire-planes
     *
     * The total number of wire-planes in this superlayer consists of
     * sense and guard layers, and between each of these, there
     * are nfieldlayers number of field wire planes.
     *
     * \return total number of wire-planes
     **/
    int nWirePlanes() {
        int nwplanes = this.nLayers();
        nwplanes += (nwplanes-1) * nfieldlayers;
        return nwplanes;
    }

    /**
     * \brief Get distance from target center to the first guard layer
     *
     * This is the distance from the nominal CLAS12 center to
     * the first (guard) wire-plane of this superlayer.
     *
     * \return distance (cm)
     **/
    double dist2tgt() {
        // start with distance from target to the
        // first guard wire plane in the region
        // This value is returned if we are on the first superlayer
        double dist2tgt = region.dist2tgt;

        // now add the thickness of each superlayer between
        // the one requested and the first one
        for (int slyr=0; slyr<index; slyr++)
        {
            // add the total thickness of each intervening superlayer
            // and the midgap (distance between superlayers)
            dist2tgt += region.superlayer(slyr).thickness()
                     +  region.midgap;
        }

        return dist2tgt;
    }

    /**
     * \brief distance from target to first wire midpoint
     *
     * This is the distance from the nominal CLAS12 center to
     * the first (guard) wire in the first (guard) wire-plane
     * of this superlayer. Midpoints are the intersection points
     * of the wires with the sector mid-plane.
     *
     * \return distance (cm)
     **/
    double firstWireMidDist2tgt() {
        return this.dist2tgt() / cos(region.thtilt - thmin);
    }

    /**
     * \brief radial distance to first wire midpoint
     *
     * This is the radial distance from the beam-line in cm to the
     * first wire in the first (guard) wire-plane in this superlayer.
     * Midpoints are the intersection points of the wires with the
     * sector mid-plane.
     *
     * \return distance (cm)
     **/
    double firstWireMidX() {
        return this.firstWireMidDist2tgt() * sin(thmin);
    }

    /**
     * \brief longitudinal distance to first wire midpoint
     *
     * This is the longitudinal distance (along the beam-line from
     * the nominal CLAS12 center in cm to the first wire in the first
     * (guard) wire-plane in this superlayer. Midpoints are
     * the intersection points of the wires with the
     * sector mid-plane.
     *
     * \return distance (cm)
     **/
    double firstWireMidZ() {
        return this.firstWireMidDist2tgt() * cos(thmin);
    }

    /**
     * \brief sense wire midpoint spacing distance
     *
     * This is the distance between midpoints of the
     * sense wires in this superlayer. Midpoints are
     * the intersection points of the wires with the
     * sector mid-plane.
     *
     * \return distance (cm)
     **/
    double wireMidSpacing() {
        // dw is the characteristic distance between sense wires
        double dw = wpdist * 4. * cos(PI/6.);
        return dw / cos(thster);
    }

    /**
     * \brief wire direction cosine x
     *
     * This is \f$\cos(\alpha)\f$ where \f$\alpha\f$ is the first of the
     * usual (z-x-z) Euler angles \f$(\alpha,\beta,\gamma)\f$
     *
     * \return \f$\cos(\alpha)\f$
     **/
    double wireDircosX() {
        return - sin(thster) * cos(region.thtilt);
    }

    /**
     * \brief wire direction cosine y
     *
     * This is \f$\cos(\beta)\f$ where \f$\beta\f$ is the second of the
     * usual (z-x-z) Euler angles \f$(\alpha,\beta,\gamma)\f$
     *
     * \return \f$\cos(\beta)\f$
     **/
    double wireDircosY() {
        return cos(thster);
    }

    /**
     * \brief wire direction cosine z
     *
     * This is \f$\cos(\gamma)\f$ where \f$\gamma\f$ is the third of the
     * usual (z-x-z) Euler angles \f$(\alpha,\beta,\gamma)\f$
     *
     * \return \f$\cos(\gamma)\f$
     **/
    double wireDircosZ() {
        return sin(thster) * sin(region.thtilt);
    }

    /**
     * \brief 3D direction of a wire in this superlayer
     *
     * Note that the direction of all wires in this superlayer is the same.
     *
     * \return direction (unit) vector in the direction of the wires
     **/
    Vector3D wireDirection(CoordinateSystem coord) {
        Vector3D ret = new Vector3D(
            this.wireDircosX(),
            this.wireDircosY(),
            this.wireDircosZ() ).asUnit();
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

    String g4Name() {
        return new String("SL"+(index+1)+"_"+region.g4Name());
    }

    String description() {
        return new String(region.description()+" Superlayer "+(index+1));
    }
}
