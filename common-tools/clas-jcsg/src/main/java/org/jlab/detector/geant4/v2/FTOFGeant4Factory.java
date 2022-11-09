package org.jlab.detector.geant4.v2;

import eu.mihosoft.vrl.v3d.Vector3d;
import org.jlab.detector.volume.Geant4Basic;
import org.jlab.detector.volume.G4Trd;
import org.jlab.detector.volume.G4Box;
import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import static org.jlab.detector.hits.DetId.FTOFID;
import static org.jlab.detector.units.SystemOfUnits.Length;
import org.jlab.detector.volume.G4World;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Trap3D;

/**
 *
 * @author gavalian, kenjo
 */
public final class FTOFGeant4Factory extends Geant4Factory {

    private final double motherGap = 4.0 * Length.cm;
    private final double pbthickness = 0.005 * Length.in;
    private final double microgap = 0.001;
    private final double[][] align_deltaX = new double[6][3];
    private final double[][] align_deltaY = new double[6][3];
    private final double[][] align_deltaZ = new double[6][3];
    private final double[][] align_rotX = new double[6][3];
    private final double[][] align_rotY = new double[6][3];
    private final double[][] align_rotZ = new double[6][3];

    private final String[] stringLayers = new String[]{
        "/geometry/ftof/panel1a",
        "/geometry/ftof/panel1b",
        "/geometry/ftof/panel2"};

    private final String[] gemcLayerNames = new String[]{
        "1a", "1b", "2"
    };

    private final int[] nPaddles = new int[stringLayers.length];
    
    public FTOFGeant4Factory(ConstantProvider provider) {
        motherVolume = new G4World("fc");

        int alignrows = provider.length("/geometry/ftof/alignment/sector");
        for(int irow = 0; irow< alignrows; irow++) {
            int isector = provider.getInteger("/geometry/ftof/alignment/sector",irow)-1;
            int ilayer = provider.getInteger("/geometry/ftof/alignment/layer",irow)-1;

            align_deltaX[isector][ilayer] = provider.getDouble("/geometry/ftof/alignment/deltaX",irow);
            align_deltaY[isector][ilayer] = provider.getDouble("/geometry/ftof/alignment/deltaY",irow);
            align_deltaZ[isector][ilayer] = provider.getDouble("/geometry/ftof/alignment/deltaZ",irow);
            align_rotX[isector][ilayer] = provider.getDouble("/geometry/ftof/alignment/rotX",irow);
            align_rotY[isector][ilayer] = provider.getDouble("/geometry/ftof/alignment/rotY",irow);
            align_rotZ[isector][ilayer] = provider.getDouble("/geometry/ftof/alignment/rotZ",irow);
        }

        for (int sector = 1; sector <= 6; sector++) {
            for (int layer = 1; layer <= 3; layer++) {
                Geant4Basic layerVolume = createPanel(provider, sector, layer);
                layerVolume.setMother(motherVolume);
            }
        }
        properties.put("email", "carman@jlab.org, jguerra@jlab.org");
        properties.put("author", "carman, guerra");
        properties.put("date", "06/03/13");
    }

    private Geant4Basic createPanel(ConstantProvider cp, int sector, int layer) {
        double thtilt = Math.toRadians(cp.getDouble(stringLayers[layer - 1] + "/panel/thtilt", 0));
        double thmin = Math.toRadians(cp.getDouble(stringLayers[layer - 1] + "/panel/thmin", 0));
        double dist2edge = cp.getDouble(stringLayers[layer - 1] + "/panel/dist2edge", 0) * Length.cm;

        List<G4Box> paddles = this.createLayer(cp, layer);

        // x is along the paddle, y perpendicular to the panel and z in the panel plane pointing outward
        double panel_mother_dx1 = paddles.get(0).getXHalfLength();
        double panel_mother_dx2 = paddles.get(paddles.size() - 1).getXHalfLength()
                + (paddles.get(paddles.size() - 1).getXHalfLength() - paddles.get(paddles.size() - 2).getXHalfLength());

        double panel_mother_dy = paddles.get(0).getYHalfLength();
        double panel_width = (paddles.get(paddles.size() - 1).getLocalPosition().z - paddles.get(0).getLocalPosition().z)
                + 2 * paddles.get(0).getZHalfLength();
        double panel_mother_dz = panel_width / 2.0;

        G4Trd panelVolume = new G4Trd("ftof_p" + gemcLayerNames[layer - 1] + "_s" + sector,
                panel_mother_dx1 + motherGap, panel_mother_dx2 +motherGap,
                panel_mother_dy + motherGap, panel_mother_dy + motherGap,
                panel_mother_dz + motherGap);
        panelVolume.setId(FTOFID, sector, layer, 0);

        panelVolume.rotate("xyz", Math.toRadians(-90) - thtilt, 0.0, Math.toRadians(-30.0 - sector * 60.0));

        double panel_pos_x = dist2edge * Math.sin(thmin) + (panel_width/2 + align_deltaX[sector-1][layer-1]) * Math.cos(thtilt) + (panel_mother_dy+align_deltaZ[sector-1][layer-1]) * Math.sin(thtilt);
        double panel_pos_y = align_deltaY[sector-1][layer-1];
        double panel_pos_z = dist2edge * Math.cos(thmin) - (panel_width/2 + align_deltaX[sector-1][layer-1]) * Math.sin(thtilt) + (panel_mother_dy+align_deltaZ[sector-1][layer-1]) * Math.cos(thtilt);
        Vector3d pos_vec = new Vector3d(panel_pos_x, panel_pos_y, panel_pos_z);
        pos_vec.rotateZ(Math.toRadians((sector-1)*60));
        panelVolume.translate(pos_vec);

        for (int ipaddle = 0; ipaddle < paddles.size(); ipaddle++) {
            paddles.get(ipaddle).setName("panel" + gemcLayerNames[layer - 1] + "_sector" + sector + "_paddle_" + (ipaddle + 1));
            paddles.get(ipaddle).setId(FTOFID, sector, layer, ipaddle + 1);
            paddles.get(ipaddle).setMother(panelVolume);
        }

        if (layer != 2) {
            G4Trd pbShield = new G4Trd("ftof_shield_p" + gemcLayerNames[layer-1] + "_sector" + sector,
                    panel_mother_dx1, panel_mother_dx2, pbthickness / 2.0, pbthickness / 2.0, panel_mother_dz);
            pbShield.translate(0.0, - panel_mother_dy - microgap - pbthickness / 2.0, 0.0);
            pbShield.setMother(panelVolume);
        }

        return panelVolume;
    }

    private List<G4Box> createLayer(ConstantProvider cp, int layer) {

        nPaddles[layer-1]       = cp.length(stringLayers[layer - 1] + "/paddles/paddle");
        double paddlewidth      = cp.getDouble(stringLayers[layer - 1] + "/panel/paddlewidth", 0);
        double paddlethickness  = cp.getDouble(stringLayers[layer - 1] + "/panel/paddlethickness", 0);
        double gap              = cp.getDouble(stringLayers[layer - 1] + "/panel/gap", 0);
        double wrapperthickness = cp.getDouble(stringLayers[layer - 1] + "/panel/wrapperthickness", 0);
        double thtilt           = Math.toRadians(cp.getDouble(stringLayers[layer - 1] + "/panel/thtilt", 0));
        double thmin            = Math.toRadians(cp.getDouble(stringLayers[layer - 1] + "/panel/thmin", 0));
        double pairgap          = 0;
        if(layer==2) pairgap    = cp.getDouble(stringLayers[layer - 1] + "/panel/pairgap", 0);
            
        String paddleLengthStr = stringLayers[layer - 1] + "/paddles/Length";

        List<G4Box> paddleVolumes = new ArrayList<>();

        for (int ipaddle = 0; ipaddle < nPaddles[layer-1]; ipaddle++) {
            double paddlelength = cp.getDouble(paddleLengthStr, ipaddle);
            String vname = String.format("sci_S%d_L%d_C%d", 0, layer, ipaddle + 1);
            G4Box volume = new G4Box(vname, paddlelength / 2. * Length.cm, paddlethickness / 2. * Length.cm, paddlewidth / 2.0 * Length.cm);
            volume.makeSensitive();

            int ipair = (int) ipaddle/2;
            double zoffset = (ipaddle - nPaddles[layer-1] / 2. + 0.5) * (paddlewidth + gap);
            if(layer==2) zoffset = (ipair - nPaddles[layer-1]/4-0.5) * (2*paddlewidth + gap + pairgap) + ((ipaddle%2)+0.5) * (paddlewidth + gap);

            volume.translate(0.0, 0.0, zoffset * Length.cm);

            paddleVolumes.add(volume);
        }
        return paddleVolumes;
    }

    public G4Box getComponent(int sector, int layer, int paddle) {
        int ivolume = (sector - 1) * 3 + layer - 1;

        if (sector >= 1 && sector <= 6
                && layer >= 1 && layer <= 3) {

            List<Geant4Basic> panel = motherVolume.getChildren().get(ivolume).getChildren();
            int npaddles = panel.size();

            if (paddle >= 1 && paddle <= npaddles) {
                return (G4Box) panel.get(paddle - 1);
            }
        }

        System.err.println("ERROR!!!");
        System.err.println("Component: sector: " + sector + ", layer: " + layer + ", paddle: " + paddle + " doesn't exist");
        throw new IndexOutOfBoundsException();
    }

    public double getThickness(int sector, int layer, int paddle) {
        int ivolume = (sector - 1) * 3 + layer - 1;

        Geant4Basic panel = motherVolume.getChildren().get(ivolume);
        G4Box pad = (G4Box) panel.getChildren().get(paddle-1);
        return pad.getYHalfLength()*2.;      
    }
    
    public Plane3D getFrontalFace(int sector, int layer) {
        if (sector < 1 || sector > 6
                || layer < 1 || layer > 3) {
            System.err.println("ERROR!!!");
            System.err.println("Component: sector: " + sector + ", layer: " + layer + " doesn't exist");
            throw new IndexOutOfBoundsException();
        }

        int ivolume = (sector - 1) * 3 + layer - 1;

        Geant4Basic panel = motherVolume.getChildren().get(ivolume);
        G4Box padl = (G4Box) panel.getChildren().get(1);
        Vector3d point = new Vector3d(padl.getVertex(0)); //first corner of the paddle on the upstream face
        Vector3d normal = new Vector3d(panel.getLineY().diff().normalized());

        return new Plane3D(point.x, point.y, point.z, normal.x, normal.y, normal.z);
    }

    public Plane3D getMidPlane(int sector, int layer) {
        if (sector < 1 || sector > 6
                || layer < 1 || layer > 3) {
            System.err.println("ERROR!!!");
            System.err.println("Component: sector: " + sector + ", layer: " + layer + " doesn't exist");
            throw new IndexOutOfBoundsException();
        }

        int ivolume = (sector - 1) * 3 + layer - 1;

        Geant4Basic panel = motherVolume.getChildren().get(ivolume);
        G4Box padl = (G4Box) panel.getChildren().get(1);
        double x=(padl.getLineY().origin().x+padl.getLineY().end().x)/2;
        double y=(padl.getLineY().origin().y+padl.getLineY().end().y)/2;
        double z=(padl.getLineY().origin().z+padl.getLineY().end().z)/2;
        Vector3d normal = new Vector3d(panel.getLineY().diff().normalized());

        return new Plane3D(x, y, z, normal.x, normal.y, normal.z);
    }

    public Trap3D getTrajectorySurface(int sector, int layer) {
        if (sector < 1 || sector > 6
                || layer < 1 || layer > 3) {
            System.err.println("ERROR!!!");
            System.err.println("Component: sector: " + sector + ", layer: " + layer + " doesn't exist");
            throw new IndexOutOfBoundsException();
        }

        int ivolume = (sector - 1) * 3 + layer - 1;

        List<Geant4Basic> paddles = motherVolume.getChildren().get(ivolume).getChildren();
        G4Box padl0 = (G4Box) paddles.get(0);
        G4Box padln = (G4Box) paddles.get(nPaddles[layer-1]-1);

        Vector3d dy = padl0.getLineZ().diff().dividedBy(2);

        Vector3d p0 = padl0.getLineX().end().clone().sub(dy);
        Vector3d p1 = padl0.getLineX().origin().clone().sub(dy);
        Vector3d p2 = padln.getLineX().origin().clone().add(dy);
        Vector3d p3 = padln.getLineX().end().clone().add(dy);
        
        Trap3D trapezoid = new Trap3D(p0.x, p0.y, p0.z, p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, p3.x, p3.y, p3.z);
        
        return trapezoid; 
    }
    
    
    public G4World getMother() {
        return motherVolume;
    }
    
    
    public static void main(String[] args) {
        ConstantProvider cp = GeometryFactory.getConstants(DetectorType.FTOF, 11, "rga_spring2018");
        FTOFGeant4Factory factory = new FTOFGeant4Factory(cp);
            
        for (int sector = 1; sector <= 1; sector++) {
            for (int layer = 1; layer <= 3; layer++) {
                int npaddles = cp.length(factory.stringLayers[layer - 1] + "/paddles/paddle");
                for (int comp = 1; comp <= npaddles; comp++) {
                    G4Box pad = factory.getComponent(sector, layer, comp);
                    double x=(pad.getLineY().origin().x+pad.getLineY().end().x)/2;
                    double y=(pad.getLineY().origin().y+pad.getLineY().end().y)/2;
                    double z=(pad.getLineY().origin().z+pad.getLineY().end().z)/2;
//                    System.out.println(pad.getLineY().origin().toString());
//                    System.out.println(pad.getLineY().end().toString());
                    double dx=pad.getLineY().end().x-pad.getLineY().origin().x;
                    double dz=pad.getLineY().end().z-pad.getLineY().origin().z;
                    double tilt=Math.toDegrees(Math.atan(dx/dz));
//                    System.out.println(tilt);
                    System.out.println(comp + "\t" + pad.getLineY().origin().x + "\t" + pad.getLineY().origin().y +  "\t" + pad.getLineY().origin().z + "\t" + pad.getXHalfLength() + "\t" + pad.getYHalfLength() + "\t" + pad.getZHalfLength());
                }
            }
        }
    }

}
