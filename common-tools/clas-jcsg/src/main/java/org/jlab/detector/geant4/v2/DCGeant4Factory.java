/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2;

import eu.mihosoft.vrl.v3d.Vector3d;
import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.geant4.v2.DC.DCdatabase;
import org.jlab.detector.geant4.v2.DC.DCstructure.DCdetector;
import org.jlab.detector.geant4.v2.DC.DCstructure.DCsuperlayer;
import org.jlab.detector.geant4.v2.DC.DCstructure.DClayer;
import org.jlab.detector.geant4.v2.DC.DCstructure.DCsector;
import org.jlab.detector.geant4.v2.DC.DCstructure.DCwire;
import org.jlab.detector.geant4.v2.DC.Wire;
import org.jlab.detector.volume.G4Trap;
import org.jlab.detector.volume.G4World;
import org.jlab.detector.volume.Geant4Basic;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.component.DriftChamberWire;
import org.jlab.geom.prim.Shape3D;

/**
 *
 * @author kenjo
 */

///////////////////////////////////////////////////
public final class DCGeant4Factory extends Geant4Factory {

    DCdatabase dbref = DCdatabase.getInstance();

    private final int nsgwires;

    private final double y_enlargement = 3.65;
    private final double z_enlargement = -2.96;
    private final double microgap = 0.01;

    private final Wire[][][][] wires;
    private final Vector3d[][][] layerMids;
    private final Vector3d[][] regionMids;

    public static boolean MINISTAGGERON=true;
    public static boolean MINISTAGGEROFF=false;

    ///////////////////////////////////////////////////
    public DCGeant4Factory(ConstantProvider provider) {
        this(provider, MINISTAGGEROFF);
    }

    ///////////////////////////////////////////////////
    public DCGeant4Factory(ConstantProvider provider, boolean ministaggerStatus) {
        dbref.setMinistaggerStatus(ministaggerStatus);

        motherVolume = new G4World("fc");

        dbref.connect(provider);
        nsgwires = dbref.nsensewires() + dbref.nguardwires();

        for (int iregion = 0; iregion < 3; iregion++) {
            for (int isector = 0; isector < 6; isector++) {
                Geant4Basic regionVolume = createRegion(isector, iregion);
                regionVolume.setMother(motherVolume);
            }
        }

        properties.put("email", "mestayer@jlab.org");
        properties.put("author", "mestayer");
        properties.put("date", "05/08/16");

        // define wire and layer points in tilted coordinate frame (z axis is perpendicular to the chamber, y is along the wire)
        wires = new Wire[dbref.nsectors()][dbref.nsuperlayers()][][];
        layerMids = new Vector3d[dbref.nsectors()][dbref.nsuperlayers()][];
        regionMids = new Vector3d[dbref.nsectors()][dbref.nregions()];


        for(int isec = 0; isec < dbref.nsectors(); isec++) {
            for(int iregion=0; iregion<dbref.nregions(); iregion++) {
                regionMids[isec][iregion] = getRegion(isec, iregion).getGlobalPosition()
				.add(dbref.getAlignmentShift(isec, iregion))
				.rotateZ(Math.toRadians(-isec * 60))
				.rotateY(-dbref.thtilt(iregion));
/*
                //define layerMid using wires (produce slight shift compared to GEMC volumes)
                regionMids[isec][iregion] = new Vector3d(layerMids[isec][iregion*2][dbref.nsenselayers(iregion*2)-1]);
                regionMids[isec][iregion] = regionMids[isec][iregion].plus(layerMids[isec][iregion*2+1][0]).dividedBy(2.0);
*/
            }

            for(int isuper=0; isuper<dbref.nsuperlayers(); isuper++) {
                layerMids[isec][isuper]  = new Vector3d[dbref.nsenselayers(isuper)];

                for(int ilayer=0; ilayer<dbref.nsenselayers(isuper); ilayer++) {
/*
                    //define layerMid using wires (produce slight shift compared to GEMC volumes)
                    Wire firstWire = new Wire(isuper, ilayer+1, 1);
                    Wire lastWire = new Wire(isuper, ilayer+1, dbref.nsensewires());
                    Vector3d firstMid = firstWire.mid().rotateZ(Math.toRadians(-90.0)).rotateY(-dbref.thtilt(isuper/2));
                    Vector3d lastMid = lastWire.mid().rotateZ(Math.toRadians(-90.0)).rotateY(-dbref.thtilt(isuper/2));
                    layerMids[isec][isuper][ilayer] = firstMid.plus(lastMid).dividedBy(2.0);
*/
                    layerMids[isec][isuper][ilayer] = getLayer(isec, isuper, ilayer).getGlobalPosition()
					.add(dbref.getAlignmentShift(isec, isuper/2))
					.rotateZ(Math.toRadians(- isec * 60))
					.rotateY(-dbref.thtilt(isuper/2));
                }
            }

            for(int isuper=0; isuper<dbref.nsuperlayers(); isuper++) {
                wires[isec][isuper]   = new Wire[dbref.nsenselayers(isuper)][dbref.nsensewires()];

                /*
                DCsuperlayer dcSuperlayer = (int ilayer) -> {
                        return new DClayer() {
                            @Override
                            public DriftChamberWire getComponent(int iwire) {
                                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                            }
                            
                        };
                    };
                };
                */
                        
                for(int ilayer=0; ilayer<dbref.nsenselayers(isuper); ilayer++) {
                    layerMids[isec][isuper][ilayer].add(regionMids[isec][isuper/2].times(-1.0));
                    layerMids[isec][isuper][ilayer].rotateZ(Math.toRadians(dbref.getAlignmentThetaZ(isec, isuper/2)));
                    layerMids[isec][isuper][ilayer].rotateX(Math.toRadians(dbref.getAlignmentThetaX(isec, isuper/2)));
                    layerMids[isec][isuper][ilayer].rotateY(Math.toRadians(dbref.getAlignmentThetaY(isec, isuper/2)));
                    layerMids[isec][isuper][ilayer].add(regionMids[isec][isuper/2]);


                    for(int iwire=0; iwire<dbref.nsensewires(); iwire++) {
                        wires[isec][isuper][ilayer][iwire] = new Wire(isuper, ilayer+1, iwire+1);

                        wires[isec][isuper][ilayer][iwire].rotateZ(Math.toRadians(-90.0 + isec * 60))
						.translate(dbref.getAlignmentShift(isec, isuper/2))
						.rotateZ(Math.toRadians(-isec * 60))
						.rotateY(-dbref.thtilt(isuper/2));

                        //dc alignment implementation
                        wires[isec][isuper][ilayer][iwire].translate(regionMids[isec][isuper/2].times(-1.0));
                        wires[isec][isuper][ilayer][iwire].rotateZ(Math.toRadians(dbref.getAlignmentThetaZ(isec, isuper/2)));
                        wires[isec][isuper][ilayer][iwire].rotateX(Math.toRadians(dbref.getAlignmentThetaX(isec, isuper/2)));
                        wires[isec][isuper][ilayer][iwire].rotateY(Math.toRadians(dbref.getAlignmentThetaY(isec, isuper/2)));
                        wires[isec][isuper][ilayer][iwire].translate(regionMids[isec][isuper/2]);
    //                    System.out.println((isuper+1) + " " + (ilayer+1) + " " + (iwire+1) + " " + wireLefts[isuper][ilayer][iwire] + " " + wireMids[isuper][ilayer][iwire] + " " + wireRights[isuper][ilayer][iwire]);
                   }
                }

            }
        }


        List<DCsector> dcSectors = new ArrayList<>();
        for(int isec = 0; isec < dbref.nsectors(); isec++) {
            List<DCsuperlayer> dcSupers = new ArrayList<>();
            for(int isuper=0; isuper<dbref.nsuperlayers(); isuper++) {
                List<DClayer> dcLayers = new ArrayList<>();
                for(int ilayer=0; ilayer<dbref.nsenselayers(isuper); ilayer++) {
                    List<DCwire> dcWires = new ArrayList<>();
                    for(int iwire=0; iwire<dbref.nsensewires(); iwire++) {
                        final String line = String.format("sec%d sup%d lay%d wire%d", isec, isuper, ilayer, iwire);
                        dcWires.add( () -> System.out.println(line) );
                    }
                    dcLayers.add( (int iwire) -> dcWires.get(iwire) );
                }
                dcSupers.add( (int ilay) -> dcLayers.get(ilay) );
            }
            dcSectors.add( (int isl) -> dcSupers.get(isl) );
        }
        DCdetector dcDetector = (int isec) -> dcSectors.get(isec);

        
//        return motherVolume.getChildren().get(ireg*6+isec);
//        return getRegion(isec, isuper/2).getChildren().get((isuper%2)*6 + ilayer);
    }

    public Vector3d getWireMidpoint(int isec, int isuper, int ilayer, int iwire) {
        return wires[isec][isuper][ilayer][iwire].mid();
    }

    public Vector3d getWireLeftend(int isec, int isuper, int ilayer, int iwire) {
        return wires[isec][isuper][ilayer][iwire].left();
    }

    public Vector3d getWireRightend(int isec, int isuper, int ilayer, int iwire) {
        return wires[isec][isuper][ilayer][iwire].right();
    }

    public Vector3d getRegionMidpoint(int isec, int iregion) {
        return regionMids[isec][iregion].clone();
    }

    public Vector3d getLayerMidpoint(int isec, int isuper, int ilayer) {
        return layerMids[isec][isuper][ilayer].clone();
    }

    public Vector3d getWireMidpoint(int isuper, int ilayer, int iwire) {
        return wires[0][isuper][ilayer][iwire].mid();
    }

    public Vector3d getWireLeftend(int isuper, int ilayer, int iwire) {
        return wires[0][isuper][ilayer][iwire].left();
    }

    public Vector3d getWireRightend(int isuper, int ilayer, int iwire) {
        return wires[0][isuper][ilayer][iwire].right();
    }

    public Vector3d getRegionMidpoint(int iregion) {
        return regionMids[0][iregion].clone();
    }

    public Vector3d getLayerMidpoint(int isuper, int ilayer) {
        return layerMids[0][isuper][ilayer].clone();
    }

    public Vector3d getWireDirection(int isuper, int ilayer, int iwire) {
        return wires[0][isuper][ilayer][iwire].dir();
    }

    private Geant4Basic getRegion(int isec, int ireg) {
        return motherVolume.getChildren().get(ireg*6+isec);
    }

    private Geant4Basic getLayer(int isec, int isuper, int ilayer) {
        return getRegion(isec, isuper/2).getChildren().get((isuper%2)*6 + ilayer);
    }
    
    ///////////////////////////////////////////////////
    public Geant4Basic createRegion(int isector, int iregion) {
        Wire regw0 = new Wire(iregion * 2, 0, 0);
        Wire regw1 = new Wire(iregion * 2 + 1, 7, nsgwires - 1);

        double dx_shift = y_enlargement * Math.tan(Math.toRadians(29.5));

        double reg_dz = (dbref.frontgap(iregion) + dbref.backgap(iregion) + dbref.midgap(iregion) + dbref.superwidth(iregion * 2) + dbref.superwidth(iregion * 2 + 1)) / 2.0 + z_enlargement;
        double reg_dx0 = Math.abs(regw0.bottom().x) - dx_shift + 1.0;
        double reg_dx1 = Math.abs(regw1.top().x) + dx_shift + 1.0;
        double reg_dy = regw1.top().minus(regw0.bottom()).y / Math.cos(dbref.thtilt(iregion)) / 2.0 + y_enlargement + 1.0;
        double reg_thtilt = dbref.thtilt(iregion);

        Vector3d vcenter = regw1.top().plus(regw0.bottom()).dividedBy(2.0);
        vcenter.x = 0;
        Vector3d reg_position0 = new Vector3d(vcenter.x, vcenter.y, vcenter.z);
        vcenter.rotateZ(-Math.toRadians(90 - isector * 60));

        Geant4Basic regionVolume = new G4Trap("region" + (iregion + 1) + "_s" + (isector + 1),
                reg_dz, -reg_thtilt, Math.toRadians(90.0),
                reg_dy, reg_dx0, reg_dx1, 0.0,
                reg_dy, reg_dx0, reg_dx1, 0.0);
        regionVolume.rotate("yxz", 0.0, reg_thtilt, Math.toRadians(90.0 - isector * 60.0));
        regionVolume.translate(vcenter.x, vcenter.y, vcenter.z);
        regionVolume.setId(isector + 1, iregion + 1, 0, 0);

        for (int isup = 0; isup < 2; isup++) {
            int isuper = iregion * 2 + isup;
            int nsglayers = dbref.nsenselayers(isuper) + dbref.nguardlayers(isuper);
            for (int ilayer = 1; ilayer < nsglayers - 1; ilayer++) {
                Geant4Basic layerVolume = this.createLayer(isuper, ilayer);
                layerVolume.setName("sl" + (isuper + 1) + "_layer" + ilayer + "_s" + (isector + 1));

                Vector3d lcenter = layerVolume.getLocalPosition();
                Vector3d lshift = lcenter.minus(reg_position0);
                lshift.rotateX(reg_thtilt);

                layerVolume.rotate("zxy", -dbref.thster(isuper), 0.0, 0.0);

                layerVolume.setPosition(lshift.x, lshift.y, lshift.z);
                layerVolume.setMother(regionVolume);
                layerVolume.setId(isector + 1, iregion + 1, isuper + 1, ilayer);
            }
        }

        return regionVolume;
    }

    ///////////////////////////////////////////////////
    public Geant4Basic createLayer(int isuper, int ilayer) {
        Wire lw0 = new Wire(isuper, ilayer, 0);
        Wire lw1 = new Wire(isuper, ilayer, nsgwires - 1);

        Vector3d midline = lw1.mid().minus(lw0.mid());
        double lay_dy = Math.sqrt(Math.pow(midline.magnitude(), 2.0) - Math.pow(midline.dot(lw0.dir()), 2.0)) / 2.0;
        double lay_dx0 = lw0.length() / 2.0;
        double lay_dx1 = lw1.length() / 2.0;
        double lay_dz = dbref.cellthickness(isuper) * dbref.wpdist(isuper) / 2.0 - microgap;
        double lay_skew = lw0.center().minus(lw1.center()).angle(lw1.dir()) - Math.toRadians(90.0);

        Vector3d lcent = lw0.center().plus(lw1.center()).dividedBy(2.0);
        G4Trap layerVolume = new G4Trap("sl" + (isuper + 1) + "_layer" + ilayer,
                lay_dz, -dbref.thtilt(isuper / 2), Math.toRadians(90.0),
                lay_dy, lay_dx0, lay_dx1, lay_skew,
                lay_dy, lay_dx0, lay_dx1, lay_skew);

        layerVolume.setPosition(lcent.x, lcent.y, lcent.z);

        return layerVolume;
    }

    /*
    public void printWires(){
        System.out.println("hello");
        for(int isup=0;isup<2;isup++)
        for(int il=0;il<8;il+=7)
            for(int iwire=0;iwire<nsgwires+1;iwire+=nsgwires/30){
        Wire regw = new Wire(isup,il,iwire);
        System.out.println("line("+regw.left()+", "+regw.right()+");");
            }
    }
    */
}