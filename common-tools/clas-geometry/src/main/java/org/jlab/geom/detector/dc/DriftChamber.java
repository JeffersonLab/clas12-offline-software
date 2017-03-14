package org.jlab.geom.detector.dc;

import java.util.*;
import static java.lang.Math.*;

import org.jlab.ccdb.JDBCProvider;
import org.jlab.ccdb.Assignment;

import org.jlab.geom.G4VolumeMap;
import org.jlab.geom.CoordinateSystem;
import org.jlab.geom.detector.dc.*;

/**
 * \brief The drift chamber geometry class for CLAS12
 *
 * The DC consists of six sectors, each with three regions.
 * Each region consists of two superlayers which hold the 22
 * wire-planes: There are six sense-wire planes in a superlayer
 * surrounded by two guard-wire planes, and between each of these
 * there are two "field-wire" planes.
 **/
public class DriftChamber {

    List<Sector> sectors;

    /**
     * \brief default constructor
     **/
    public DriftChamber() {
        this.sectors = new ArrayList<Sector>();
    }

    /**
     * \brief constructor which fetches the nominal geometry from the database
     *
     * This calls DriftChamber.fetchNominalParameters(provider)
     **/
    public DriftChamber(JDBCProvider provider) {
        this.sectors = new ArrayList<Sector>();
        this.fetchNominalParameters(provider);
    }

    /**
     * \brief fills the DriftChamber class with the nominal geometry
     *
     * The nominal geometry is identical for each sector and therefore
     * after this call, there is much redundancy in the geometry. The
     * parameters obtained are the so-called "core" paramters and
     * it is expected that additional alignment paramters will be
     * obtained from the database in a later method-call.
     *
     * \param [in] dataprovider the ccdb::DataProvider object
     **/
    public void fetchNominalParameters(JDBCProvider provider) {
        final double deg = 3.14159265358979 / 180.;

        // here we connect to the CCDB (MySQL) databse and request
        // the nominal geometry parameters for the Drift Chamber.
        // These numbers come from four tables: dc, region, superlayer,
        // and layer.
        Assignment asgmt = provider.getData("/geometry/dc/dc");
        int nsectors = asgmt.getColumnValuesInt("nsectors").get(0);
        int nregions = asgmt.getColumnValuesInt("nregions").get(0);

        asgmt = provider.getData("/geometry/dc/region");
        Vector<Integer> nsuperlayers = asgmt.getColumnValuesInt   ("nsuperlayers");
        Vector<Double > dist2tgt     = asgmt.getColumnValuesDouble("dist2tgt"    );
        Vector<Double > frontgap     = asgmt.getColumnValuesDouble("frontgap"    );
        Vector<Double > midgap       = asgmt.getColumnValuesDouble("midgap"      );
        Vector<Double > backgap      = asgmt.getColumnValuesDouble("backgap"     );
        Vector<Double > thopen       = asgmt.getColumnValuesDouble("thopen"      );
        Vector<Double > thtilt       = asgmt.getColumnValuesDouble("thtilt"      );
        Vector<Double > xdist        = asgmt.getColumnValuesDouble("xdist"       );

        asgmt = provider.getData("/geometry/dc/superlayer");
        Vector<Integer> nsenselayers  = asgmt.getColumnValuesInt   ("nsenselayers" );
        Vector<Integer> nguardlayers  = asgmt.getColumnValuesInt   ("nguardlayers" );
        Vector<Integer> nfieldlayers  = asgmt.getColumnValuesInt   ("nfieldlayers" );
        Vector<Double > thster        = asgmt.getColumnValuesDouble("thster"       );
        Vector<Double > thmin         = asgmt.getColumnValuesDouble("thmin"        );
        Vector<Double > wpdist        = asgmt.getColumnValuesDouble("wpdist"       );
        Vector<Double > cellthickness = asgmt.getColumnValuesDouble("cellthickness");

        asgmt = provider.getData("/geometry/dc/layer");
        int nsensewires = asgmt.getColumnValuesInt("nsensewires").get(0);
        int nguardwires = asgmt.getColumnValuesInt("nguardwires").get(0);

        // Now we fill the sectors object which holds all these
        // core parameters. Here, many numbers will be redundant.
        // It is expected that this will change once efficiency
        // alignment and other calibrations are taken into effect.
        this.sectors.clear();

        for (int sec=0; sec<nsectors; sec++) {
            this.sectors.add(new Sector(this));
            Sector sector = this.sectors.get(sec);
            sector.index = sec;

            for (int reg=0; reg<nregions; reg++) {
                sector.regions.add(new Region(sector));
                Region region = sector.regions.get(reg);
                region.index = reg;

                region.dist2tgt = dist2tgt.get(reg);
                region.frontgap = frontgap.get(reg);
                region.midgap   = midgap.get(reg)  ;
                region.backgap  = backgap.get(reg) ;
                region.thopen   = toRadians(thopen.get(reg));
                region.thtilt   = toRadians(thtilt.get(reg));
                region.xdist    = xdist.get(reg)   ;

                for (int slyr=0; slyr<nsuperlayers.get(reg); slyr++) {

                    // nslyr is the "global" superlayer number starting
                    // from 0 and going to 5
                    int nslyr = slyr;
                    for (int n=0; n<reg; n++) {
                        nslyr += nsuperlayers.get(n);
                    }

                    region.superlayers.add(new Superlayer(region));
                    Superlayer superlayer = region.superlayers.get(slyr);
                    superlayer.index = slyr;

                    superlayer.nfieldlayers  = nfieldlayers.get(nslyr);
                    superlayer.thster        = toRadians(thster.get(nslyr));
                    superlayer.thmin         = toRadians(thmin.get(nslyr));
                    superlayer.wpdist        = wpdist.get(nslyr)       ;
                    superlayer.cellthickness = cellthickness.get(nslyr);

                    int nlayers = nguardlayers.get(slyr) + nsenselayers.get(slyr);
                    for (int lyr=0; lyr<nlayers; lyr++) {
                        superlayer.layers.add(new Layer(superlayer));
                        Layer layer = superlayer.layers.get(lyr);
                        layer.index = lyr;
                    }
                }
            }
        }
    }

    public G4VolumeMap g4Volumes(CoordinateSystem coord) {
        G4VolumeMap vols = new G4VolumeMap();
        for (Sector sector : sectors) {
            vols.putAll(sector.g4Volumes(coord));
        }
        return vols;
    }

    public G4VolumeMap g4Volumes() {
        return this.g4Volumes(CoordinateSystem.CLAS);
    }

    int nSectors() {
        return sectors.size();
    }

    int sectorIndex(int idx) {
        if (idx<0) {
            idx = this.nSectors() + idx;
        }
        return idx;
    }

    Sector sector(int idx) {
        return sectors.get(this.sectorIndex(idx));
    }

    String description() {
        return new String("Drift Chamber");
    }
}
