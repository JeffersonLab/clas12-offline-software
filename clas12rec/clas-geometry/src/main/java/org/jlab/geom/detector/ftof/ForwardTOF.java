package org.jlab.geom.detector.ftof;

import java.util.*;
import static java.lang.Math.*;

import org.jlab.ccdb.JDBCProvider;
import org.jlab.ccdb.Assignment;

import org.jlab.geom.G4VolumeMap;
import org.jlab.geom.CoordinateSystem;
import org.jlab.geom.detector.ftof.*;

/**
 * \brief The forward time of flight geometry class for CLAS12
 *
 * The FTOF consists of six sectors, each of
 * which with three panels: 1a,1b,2.
 * Each panel consists of a number of scintillator
 * paddles: 23 for 1a, 62 for 1b, 5 for 2.
 **/
public class ForwardTOF {

    List<Sector> sectors;
    private Map<String,Integer> panel_index_map;
    private Vector<String> panel_names;

    /**
     * \brief default constructor
     **/
    public ForwardTOF() {
        this.sectors = new ArrayList<Sector>();
        this.panel_index_map = new HashMap<String,Integer>();
        this.panel_index_map.put("1a",0);
        this.panel_index_map.put("1b",1);
        this.panel_index_map.put("2" ,2);
        this.panel_names = new Vector<String>();
        this.panel_names.add("1a");
        this.panel_names.add("1b");
        this.panel_names.add("2" );
    }

    /**
     * \brief constructor which fetches the nominal geometry from the database
     *
     * This calls ForwardTOF.fetchNominalParameters(provider)
     **/
    public ForwardTOF(JDBCProvider provider) {
        this();
        this.sectors = new ArrayList<Sector>();
        this.fetchNominalParameters(provider);
    }

    /**
     * \brief fills the ForwardTOF class with the nominal geometry
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

        int nsectors;
        int npanels;

        Assignment asgmt = provider.getData("/geometry/ftof/ftof");

        nsectors = asgmt.getColumnValuesInt("nsectors").get(0);
        npanels  = asgmt.getColumnValuesInt("npanels").get(0);

        Vector<Double> paddle_width = new Vector<Double>(npanels);
        Vector<Double> paddle_thick = new Vector<Double>(npanels);
        Vector<Double> panel_thtilt = new Vector<Double>(npanels);
        Vector<Double> panel_thmin = new Vector<Double>(npanels);
        Vector<Double> panel_dist2edge = new Vector<Double>(npanels);
        Vector<Double> paddle_gap = new Vector<Double>(npanels);
        double panel1b_pairgap;
        Vector<Double> wrapper_thick = new Vector<Double>(npanels);

        Assignment asgmt1a = provider.getData("/geometry/ftof/panel1a/panel");
        Assignment asgmt1b = provider.getData("/geometry/ftof/panel1b/panel");
        Assignment asgmt2  = provider.getData("/geometry/ftof/panel2/panel");

        paddle_width   .add(asgmt1a.getColumnValuesDouble("paddlewidth"    ).get(0));
        paddle_width   .add(asgmt1b.getColumnValuesDouble("paddlewidth"    ).get(0));
        paddle_width   .add(asgmt2 .getColumnValuesDouble("paddlewidth"    ).get(0));
        paddle_thick   .add(asgmt1a.getColumnValuesDouble("paddlethickness").get(0));
        paddle_thick   .add(asgmt1b.getColumnValuesDouble("paddlethickness").get(0));
        paddle_thick   .add(asgmt2 .getColumnValuesDouble("paddlethickness").get(0));
        panel_thtilt   .add(asgmt1a.getColumnValuesDouble("thtilt"         ).get(0));
        panel_thtilt   .add(asgmt1b.getColumnValuesDouble("thtilt"         ).get(0));
        panel_thtilt   .add(asgmt2 .getColumnValuesDouble("thtilt"         ).get(0));
        panel_thmin    .add(asgmt1a.getColumnValuesDouble("thmin"          ).get(0));
        panel_thmin    .add(asgmt1b.getColumnValuesDouble("thmin"          ).get(0));
        panel_thmin    .add(asgmt2 .getColumnValuesDouble("thmin"          ).get(0));
        panel_dist2edge.add(asgmt1a.getColumnValuesDouble("dist2edge"      ).get(0));
        panel_dist2edge.add(asgmt1b.getColumnValuesDouble("dist2edge"      ).get(0));
        panel_dist2edge.add(asgmt2 .getColumnValuesDouble("dist2edge"      ).get(0));
        paddle_gap     .add(asgmt1a.getColumnValuesDouble("gap"            ).get(0));
        paddle_gap     .add(asgmt1b.getColumnValuesDouble("gap"            ).get(0));
        paddle_gap     .add(asgmt2 .getColumnValuesDouble("gap"            ).get(0));

        panel1b_pairgap = asgmt1b.getColumnValuesDouble("pairgap").get(0);

        wrapper_thick.add(asgmt1a.getColumnValuesDouble("wrapperthickness").get(0));
        wrapper_thick.add(asgmt1b.getColumnValuesDouble("wrapperthickness").get(0));
        wrapper_thick.add(asgmt2 .getColumnValuesDouble("wrapperthickness").get(0));

        asgmt1a = provider.getData("/geometry/ftof/panel1a/paddles");
        asgmt1b = provider.getData("/geometry/ftof/panel1b/paddles");
        asgmt2  = provider.getData("/geometry/ftof/panel2/paddles");

        Vector<Vector<Double>> paddle_meas_lengths = new Vector<Vector<Double>>(npanels);
        Vector<Vector<Double>> paddle_slopes       = new Vector<Vector<Double>>(npanels);
        Vector<Vector<Double>> paddle_intercepts   = new Vector<Vector<Double>>(npanels);

        paddle_meas_lengths.add(asgmt1a.getColumnValuesDouble("Length"));
        paddle_meas_lengths.add(asgmt1b.getColumnValuesDouble("Length"));
        paddle_meas_lengths.add(asgmt2 .getColumnValuesDouble("Length"));

        paddle_slopes.add(asgmt1a.getColumnValuesDouble("Slope"));
        paddle_slopes.add(asgmt1b.getColumnValuesDouble("Slope"));
        paddle_slopes.add(asgmt2 .getColumnValuesDouble("Slope"));

        paddle_intercepts.add(asgmt1a.getColumnValuesDouble("Intercept"));
        paddle_intercepts.add(asgmt1b.getColumnValuesDouble("Intercept"));
        paddle_intercepts.add(asgmt2 .getColumnValuesDouble("Intercept"));

        this.sectors.clear();

        for (int sec=0; sec<nsectors; sec++) {
            this.sectors.add(new Sector(this));
            Sector sector = this.sectors.get(sec);
            sector.index = sec;

            for (int pan=0; pan<npanels; pan++) {
                sector.panels.add(new Panel(sector));
                Panel panel = sector.panels.get(pan);
                panel.index = pan;

                panel.paddle_width      = paddle_width.get(pan);
                panel.paddle_thickness  = paddle_thick.get(pan);
                panel.thtilt            = toDegrees(panel_thtilt.get(pan));
                panel.thmin             = toDegrees(panel_thmin.get(pan));
                panel.dist2edge         = panel_dist2edge.get(pan);
                panel.paddle_gap        = paddle_gap.get(pan);
                if (panel.name() == "1b") {
                    panel.paddle_pairgap = panel1b_pairgap;
                } else {
                    panel.paddle_pairgap = 0.;
                }
                panel.wrapper_thickness = wrapper_thick.get(pan);

                int npaddles = paddle_meas_lengths.get(pan).size();
                for (int pad=0; pad<npaddles; pad++) {
                    panel.paddles.add(new Paddle(panel));
                    Paddle paddle = panel.paddles.get(pad);
                    paddle.index = pad;

                    paddle.meas_length = paddle_meas_lengths.get(pan).get(pad);
                    paddle.slope       = paddle_slopes.get(pan).get(pad);
                    paddle.intercept   = paddle_intercepts.get(pan).get(pad);
                }
            }
        }
    }

    /*
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
    */

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

    int panelIndex(String id) {
        return panel_index_map.get(id);
    }

    String panelName(int id) {
        return panel_names.get(id);
    }

    String description() {
        return new String("Forward Time of Flight");
    }
}
