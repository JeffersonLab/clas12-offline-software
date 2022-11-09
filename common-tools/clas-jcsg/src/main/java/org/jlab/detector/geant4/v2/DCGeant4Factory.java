package org.jlab.detector.geant4.v2;

import eu.mihosoft.vrl.v3d.Vector3d;
import java.util.HashMap;
import org.jlab.detector.units.SystemOfUnits.Length;
import org.jlab.detector.volume.G4Trap;
import org.jlab.detector.volume.G4World;
import org.jlab.detector.volume.Geant4Basic;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Trap3D;

/**
 *
 * @author kenjo
 */
final class DCdatabase {

    private final int nSectors = 6;
    private final int nRegions = 3;
    private final int nSupers = 6;
    private final int nShifts = 6;

    private final double dist2tgt[] = new double[nRegions];
    private final double xdist[] = new double[nRegions];
    private final double frontgap[] = new double[nRegions];
    private final double midgap[] = new double[nRegions];
    private final double backgap[] = new double[nRegions];
    private final double thopen[] = new double[nRegions];
    private final double thtilt[] = new double[nRegions];

    private final double thmin[] = new double[nSupers];
    private final double thster[] = new double[nSupers];
    private final double wpdist[] = new double[nSupers];
    private final double cellthickness[] = new double[nSupers];
    private final int nsenselayers[] = new int[nSupers];
    private final int nguardlayers[] = new int[nSupers];
    private final int nfieldlayers[] = new int[nSupers];
    private final double superwidth[] = new double[nSupers];

    private final double align_dx[][] = new double[nSectors][nRegions];
    private final double align_dy[][] = new double[nSectors][nRegions];
    private final double align_dz[][] = new double[nSectors][nRegions];

    private final double align_dthetax[][] = new double[nSectors][nRegions];
    private final double align_dthetay[][] = new double[nSectors][nRegions];
    private final double align_dthetaz[][] = new double[nSectors][nRegions];
    
    private final double endplatesbow[][][] = new double[nSectors][nRegions][2];

    private int nsensewires;
    private int nguardwires;

    private boolean ministaggerStatus = false;
    private double ministagger ;

    private boolean endplatesStatus = false;
    
    private final String dcdbpath = "/geometry/dc/";
    private static DCdatabase instance = null;

    private DCdatabase() {
    }

    public static DCdatabase getInstance() {
        if (instance == null) {
            instance = new DCdatabase();
        }
        return instance;
    }

    public void connect(ConstantProvider cp, double[][] shifts) {

        if(shifts==null || shifts.length!=nRegions || shifts[0].length!=nShifts) {
            shifts = new double[nRegions][nShifts];
        }
        
        nguardwires = cp.getInteger(dcdbpath + "layer/nguardwires", 0);
        nsensewires = cp.getInteger(dcdbpath + "layer/nsensewires", 0);
        ministagger = cp.getDouble(dcdbpath + "ministagger/ministagger", 0);

        for (int ireg = 0; ireg < nRegions; ireg++) {
            dist2tgt[ireg] = cp.getDouble(dcdbpath + "region/dist2tgt", ireg)*Length.cm;
            xdist[ireg] = cp.getDouble(dcdbpath + "region/xdist", ireg)*Length.cm;
            frontgap[ireg] = cp.getDouble(dcdbpath + "region/frontgap", ireg)*Length.cm;
            midgap[ireg] = cp.getDouble(dcdbpath + "region/midgap", ireg)*Length.cm;
            backgap[ireg] = cp.getDouble(dcdbpath + "region/backgap", ireg)*Length.cm;
            thopen[ireg] = Math.toRadians(cp.getDouble(dcdbpath + "region/thopen", ireg));
            thtilt[ireg] = Math.toRadians(cp.getDouble(dcdbpath + "region/thtilt", ireg));
        }
        for (int isuper = 0; isuper < nSupers; isuper++) {
            thmin[isuper] = Math.toRadians(cp.getDouble(dcdbpath + "superlayer/thmin", isuper));
            thster[isuper] = Math.toRadians(cp.getDouble(dcdbpath + "superlayer/thster", isuper));
            wpdist[isuper] = cp.getDouble(dcdbpath + "superlayer/wpdist", isuper)*Length.cm;
            cellthickness[isuper] = cp.getDouble(dcdbpath + "superlayer/cellthickness", isuper);
            nsenselayers[isuper] = cp.getInteger(dcdbpath + "superlayer/nsenselayers", isuper);
            nguardlayers[isuper] = cp.getInteger(dcdbpath + "superlayer/nguardlayers", isuper);
            nfieldlayers[isuper] = cp.getInteger(dcdbpath + "superlayer/nfieldlayers", isuper);

            superwidth[isuper] = wpdist[isuper] * (nsenselayers[isuper] + nguardlayers[isuper] - 1) * cellthickness[isuper];
        }
        double scaleTest=1;
        int alignrows = cp.length(dcdbpath+"alignment/dx");
        for(int irow = 0; irow< alignrows; irow++) {
               int isec = cp.getInteger(dcdbpath + "alignment/sector",irow)-1;
               int ireg = cp.getInteger(dcdbpath + "alignment/region",irow)-1;

            Vector3d align_delta    = new Vector3d(shifts[ireg][0], shifts[ireg][1], shifts[ireg][2]);
            Vector3d align_position = new Vector3d(scaleTest*cp.getDouble(dcdbpath + "alignment/dx",irow),
                                                   scaleTest*cp.getDouble(dcdbpath + "alignment/dy",irow),
                                                   scaleTest*cp.getDouble(dcdbpath + "alignment/dz",irow));
            align_position = align_position.rotateZ(-isec*Math.toRadians(60));
            align_position = align_position.rotateY(-thtilt[ireg]);
            align_position = align_position.add(align_delta);
            align_position = align_position.rotateY(thtilt[ireg]);
            align_position = align_position.rotateZ(isec*Math.toRadians(60));
            align_dx[isec][ireg]=align_position.x;
            align_dy[isec][ireg]=align_position.y;
            align_dz[isec][ireg]=align_position.z;

            align_dthetax[isec][ireg]=shifts[ireg][3]+scaleTest*cp.getDouble(dcdbpath + "alignment/dtheta_x",irow);
            align_dthetay[isec][ireg]=shifts[ireg][4]+scaleTest*cp.getDouble(dcdbpath + "alignment/dtheta_y",irow);
            align_dthetaz[isec][ireg]=shifts[ireg][5]+scaleTest*cp.getDouble(dcdbpath + "alignment/dtheta_z",irow);
        }
        
        int endplatesrows = cp.length(dcdbpath+"endplatesbow/coefficient");
        for(int irow = 0; irow< endplatesrows; irow++) {
               int isec  = cp.getInteger(dcdbpath + "endplatesbow/sector",irow)-1;
               int ireg  = cp.getInteger(dcdbpath + "endplatesbow/region",irow)-1;
               int order = cp.getInteger(dcdbpath + "endplatesbow/order",irow);
               endplatesbow[isec][ireg][order] = cp.getDouble(dcdbpath+"endplatesbow/coefficient", irow)*Length.cm;
               //System.out.println("READ ENDPLATES COEFF [isec"+isec+"]["+ireg+"]="+endplatesbow[isec][ireg][order] );
        }
    }
    
    public double dist2tgt(int ireg) {
        return dist2tgt[ireg];
    }

    public double xdist(int ireg) {
        return xdist[ireg];
    }

    public double frontgap(int ireg) {
        return frontgap[ireg];
    }

    public double midgap(int ireg) {
        return midgap[ireg];
    }

    public double backgap(int ireg) {
        return backgap[ireg];
    }

    public double thopen(int ireg) {
        return thopen[ireg];
    }

    public double thtilt(int ireg) {
        return thtilt[ireg];
    }

    public double thmin(int isuper) {
        return thmin[isuper];
    }

    public double thster(int isuper) {
        return thster[isuper];
    }

    public double wpdist(int isuper) {
        return wpdist[isuper];
    }

    public double cellthickness(int isuper) {
        return cellthickness[isuper];
    }

    public int nsenselayers(int isuper) {
        return nsenselayers[isuper];
    }

    public int nguardlayers(int isuper) {
        return nguardlayers[isuper];
    }

    public int nfieldlayers(int isuper) {
        return nfieldlayers[isuper];
    }

    public double superwidth(int isuper) {
        return superwidth[isuper];
    }

    public int nsensewires() {
        return nsensewires;
    }

    public int nguardwires() {
        return nguardwires;
    }

    public int nsuperlayers() {
        return nSupers;
    }

    public int nregions() {
        return nRegions;
    }

    public int nsectors() {
        return nSectors;
    }
    
    public double ministagger() {
        return ministagger;
    }
    
    public void setMinistaggerStatus(boolean ministaggerStatus) {
        this.ministaggerStatus = ministaggerStatus;
    }

    public boolean getMinistaggerStatus(){
        return ministaggerStatus;
    }
    
    public double endplatesbow(int isec, int ireg, int order) {
        return endplatesbow[isec][ireg][order];
    }
    
    public void setEndPlatesStatus(boolean endplatesStatus) {
        this.endplatesStatus = endplatesStatus;
    }

    public boolean getEndPlatesStatus(){
        return endplatesStatus;
    }
    
    public double getAlignmentThetaX(int isec, int ireg) {
        return align_dthetax[isec][ireg];
    }

    public double getAlignmentThetaY(int isec, int ireg) {
        return align_dthetay[isec][ireg];
    }

    public double getAlignmentThetaZ(int isec, int ireg) {
        return align_dthetaz[isec][ireg];
    }

    public Vector3d getAlignmentShift(int isec, int ireg) {
        return new Vector3d(align_dx[isec][ireg], align_dy[isec][ireg], align_dz[isec][ireg]);
    }
}

final class Wire {

    private final int sector;
    private final int ireg;
    private final int isuper;
    private final int layer;
    private final int wire;
    private final DCdatabase dbref = DCdatabase.getInstance();

    private final Vector3d midpoint;
    private final Vector3d center;
    private final Vector3d direction;
    private Vector3d leftend;
    private Vector3d rightend;

    public Wire translate(Vector3d vshift) {
        leftend.add(vshift);
        rightend.add(vshift);
        setCenter();
        setMiddle();
        return this;
    }

    public Wire rotateX(double rotX) {
        direction.rotateX(rotX);
        leftend.rotateX(rotX);
        rightend.rotateX(rotX);
        setCenter();
        setMiddle();
        return this;
    }

    public Wire rotateY(double rotY) {
        direction.rotateY(rotY);
        leftend.rotateY(rotY);
        rightend.rotateY(rotY);
        setCenter();
        setMiddle();
        return this;
    }

    public Wire rotateZ(double rotZ) {
        direction.rotateZ(rotZ);
        leftend.rotateZ(rotZ);
        rightend.rotateZ(rotZ);
        setCenter();
        setMiddle();
        return this;
    }

    private void findEnds() {
        // define vector from wire midpoint to chamber tip (z is wrong!!)
        Vector3d vnum = new Vector3d(0, dbref.xdist(ireg), 0);
        vnum.sub(midpoint);

        double copen = Math.cos(dbref.thopen(ireg) / 2.0);
        double sopen = Math.sin(dbref.thopen(ireg) / 2.0);

        // define unit vector normal to the sides of the chamber and pointing inside
        Vector3d rnorm = new Vector3d(copen, sopen, 0);
        Vector3d lnorm = new Vector3d(-copen, sopen, 0);

        double wlenl = vnum.dot(lnorm) / direction.dot(lnorm);
        leftend = direction.times(wlenl).add(midpoint);

        double wlenr = vnum.dot(rnorm) / direction.dot(rnorm);
        rightend = direction.times(wlenr).add(midpoint);
    }

    /**
     * Correct for endplates bowing in tilted coordinate system. (ziegler)
     */
    public void correctEnds() {
        double iwirn = (double) wire/112.0;
        //deflection function has to be 1 at extremum (3.8465409 scales it so it is 1 at first derivative)
        double defFunc = 3.8465409*(iwirn - 3 * iwirn*iwirn*iwirn +2 * iwirn*iwirn*iwirn*iwirn);
        //max deflection for L and R sides of wire
        double deflMaxL = dbref.endplatesbow(sector-1, ireg, 0);
        double deflMaxR = dbref.endplatesbow(sector-1, ireg, 1); 
        //deflection of the L and R sides
        double deflL = 0.5 * deflMaxL * defFunc;
        double deflR = 0.5 * deflMaxR * defFunc;
        
        double xL = leftend.x + deflL;
        double xR = rightend.x + deflR;
        double yL = leftend.y;
        double yR = rightend.y;
        
        // the uncorrected wirelength.  We assume the wire length is not changing
        double wlenl = leftend.sub(midpoint).magnitude();
        double wlenr = rightend.sub(midpoint).magnitude();
        //get the modified wire direction
        double n = Math.sqrt((xR - xL)*(xR - xL)+(yR - yL)*(yR - yL));
        direction.set((xR - xL)/n, (yR - yL)/n, 0);
        // midpoint corresponds to y = 0
        midpoint.set(xR -yR*((xR-xL)/(yR-yL)), 0, midpoint.z);
        //get left and right ends assuming the wire length is not changing
        leftend = direction.times(-wlenl).add(midpoint);
        rightend = direction.times(wlenr).add(midpoint);
        
//        if(sector == 4)
//            System.out.println((this.isuper+1)+" "+layer+" "+wire+" "+(float)(xL-deflL)+" "+(float)yL+" "+(float)leftend.z+" "+ 
//                    (float)(xL)+" "+(float)yL+" "+(float)leftend.z+" "+
//                    (float)leftend.x+" "+(float)leftend.y+" "+(float)leftend.z
//            +" "+(float)(xR-deflR)+" "+(float)yR+" "+(float)rightend.z+" "+ 
//                    (float)(xR)+" "+(float)yR+" "+(float)rightend.z+" "+
//                    (float)rightend.x+" "+(float)rightend.y+" "+(float)rightend.z);
    }
    /**
     * 
     * @param sector sector 1...6
     * @param super  superlayer index 0...5
     * @param layer  layer 1...6
     * @param wire   wire 1...112
     */
    public Wire(int sector, int isuperl, int layer, int wire) {
        this.sector  = sector;
        this.isuper  = isuperl;
        this.layer   = layer;
        this.wire    = wire;
        this.ireg    = isuper / 2;

        // calculate first-wire distance from target
        double w2tgt = dbref.dist2tgt(ireg);
        if (isuper % 2 > 0) {
            w2tgt += dbref.superwidth(isuper - 1) + dbref.midgap(ireg);
        }
        w2tgt /= Math.cos(dbref.thtilt(ireg) - dbref.thmin(isuper));

        // y0 and z0 in the lab for the first wire of the layer
        double y0mid = w2tgt * Math.sin(dbref.thmin(isuper));
        double z0mid = w2tgt * Math.cos(dbref.thmin(isuper));

        double cster = Math.cos(dbref.thster(isuper));
        double ctilt = Math.cos(dbref.thtilt(ireg));
        double stilt = Math.sin(dbref.thtilt(ireg));

        double dw = 4 * Math.cos(Math.toRadians(30)) * dbref.wpdist(isuper);
        double dw2 = dw / cster;

        // hh: wire distance in the wire plane
        double hh = (wire-1 + ((double)(layer % 2)) / 2.0) * dw2;
        if(ireg==2 && isSensitiveWire(isuper, layer, wire) && dbref.getMinistaggerStatus())
                hh += ((layer%2)*2-1)*dbref.ministagger();

        // ll: layer distance
        double tt = dbref.cellthickness(isuper) * dbref.wpdist(isuper);
        double ll = layer * tt;

        // wire x=0 coordinates in the lab
        double ym = y0mid + ll * stilt + hh * ctilt;
        double zm = z0mid + ll * ctilt - hh * stilt;

        // wire midpoint in the lab
        midpoint = new Vector3d(0, ym, zm);
        direction = new Vector3d(1, 0, 0);
        direction.rotateZ(dbref.thster(isuper));
        direction.rotateX(-dbref.thtilt(ireg));
        findEnds();
        center = leftend.plus(rightend).dividedBy(2.0);
    }

    private boolean isSensitiveWire(int isuper, int ilayer, int iwire) {
        return iwire>0 && iwire<=dbref.nsensewires() &&
                ilayer>0 && ilayer<=dbref.nsenselayers(isuper);
    }

    private void setCenter() {  
        center.set(leftend.plus(rightend).dividedBy(2.0));
    }
    
    private void setMiddle() {
        double t = -leftend.y/direction.y;
        midpoint.set(leftend.plus(direction.times(t)));
    }
        
    public Vector3d mid() {
        return new Vector3d(midpoint);
    }

    public Vector3d left() {
        return new Vector3d(leftend);
    }

    public Vector3d right() {
        return new Vector3d(rightend);
    }

    public Vector3d dir() {
        return new Vector3d(direction);
    }

    public Vector3d top() {
        if (leftend.y < rightend.y) {
            return new Vector3d(rightend);
        }
        return new Vector3d(leftend);
    }

    public Vector3d bottom() {
        if (leftend.y < rightend.y) {
            return new Vector3d(leftend);
        }
        return new Vector3d(rightend);
    }

    public double length() {
        return leftend.minus(rightend).magnitude();
    }

    public Vector3d center() {
        return new Vector3d(center);
    }
}

///////////////////////////////////////////////////
public final class DCGeant4Factory extends Geant4Factory {

    DCdatabase dbref = DCdatabase.getInstance();

    private final HashMap<String, String> properties = new HashMap<>();
    private int nsgwires;

    private final double y_enlargement = 3.65;
    private final double z_enlargement = -2.96;
    private final double microgap = 0.01;

    private final Wire[][][][] wires;
    private final Vector3d[][][] layerMids;
    private final Vector3d[][] regionMids;

    public static boolean MINISTAGGERON=true;
    public static boolean MINISTAGGEROFF=false;
    
    public static boolean ENDPLATESBOWON=true;
    public static boolean ENDPLATESBOWOFF=false;

    ///////////////////////////////////////////////////
    public DCGeant4Factory(ConstantProvider provider) {
        this(provider, MINISTAGGEROFF, ENDPLATESBOWOFF, null);
    }

    ///////////////////////////////////////////////////
    public DCGeant4Factory(ConstantProvider provider, boolean ministaggerStatus,
            boolean endplatesStatus) {
        this(provider, ministaggerStatus, endplatesStatus, null);
    }
    
    ///////////////////////////////////////////////////
    public DCGeant4Factory(ConstantProvider provider, boolean ministaggerStatus,
            boolean endplatesStatus, double[][] shifts) {
        dbref.setMinistaggerStatus(ministaggerStatus);
        dbref.setEndPlatesStatus(endplatesStatus);
        
        motherVolume = new G4World("fc");

        dbref.connect(provider, shifts);
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
                    Wire firstWire = new Wire(isuper, layer+1, 1);
                    Wire lastWire = new Wire(isuper, layer+1, dbref.nsensewires());
                    Vector3d firstMid = firstWire.mid().rotateZ(Math.toRadians(-90.0)).rotateY(-dbref.thtilt(isuper/2));
                    Vector3d lastMid = lastWire.mid().rotateZ(Math.toRadians(-90.0)).rotateY(-dbref.thtilt(isuper/2));
                    layerMids[isec][isuper][layer] = firstMid.plus(lastMid).dividedBy(2.0);
*/
                    layerMids[isec][isuper][ilayer] = getLayer(isec, isuper, ilayer).getGlobalPosition()
					.add(dbref.getAlignmentShift(isec, isuper/2))
					.rotateZ(Math.toRadians(- isec * 60))
					.rotateY(-dbref.thtilt(isuper/2));
                }
            }

            for(int isuper=0; isuper<dbref.nsuperlayers(); isuper++) {
                wires[isec][isuper]   = new Wire[dbref.nsenselayers(isuper)][dbref.nsensewires()];
                for(int ilayer=0; ilayer<dbref.nsenselayers(isuper); ilayer++) {
                    layerMids[isec][isuper][ilayer].add(regionMids[isec][isuper/2].times(-1.0));
                    layerMids[isec][isuper][ilayer].rotateZ(Math.toRadians(dbref.getAlignmentThetaZ(isec, isuper/2)));
                    layerMids[isec][isuper][ilayer].rotateX(Math.toRadians(dbref.getAlignmentThetaX(isec, isuper/2)));
                    layerMids[isec][isuper][ilayer].rotateY(Math.toRadians(dbref.getAlignmentThetaY(isec, isuper/2)));
                    layerMids[isec][isuper][ilayer].add(regionMids[isec][isuper/2]);

                    for(int iwire=0; iwire<dbref.nsensewires(); iwire++) {
                        wires[isec][isuper][ilayer][iwire] = new Wire(isec+1, isuper, ilayer+1, iwire+1);
                        //rotate in tilted sector coordinate system
                        wires[isec][isuper][ilayer][iwire].rotateZ(Math.toRadians(-90.0 + isec * 60))
						.translate(dbref.getAlignmentShift(isec, isuper/2))
						.rotateZ(Math.toRadians(-isec * 60))
						.rotateY(-dbref.thtilt(isuper/2));
                        
                        //implement end-plates bow in the tilted sector coordinate system (ziegler)
                        if(dbref.getEndPlatesStatus())
                            wires[isec][isuper][ilayer][iwire].correctEnds();
                        //dc alignment implementation
                        wires[isec][isuper][ilayer][iwire].translate(regionMids[isec][isuper/2].times(-1.0));
                        wires[isec][isuper][ilayer][iwire].rotateZ(Math.toRadians(dbref.getAlignmentThetaZ(isec, isuper/2)));
                        wires[isec][isuper][ilayer][iwire].rotateX(Math.toRadians(dbref.getAlignmentThetaX(isec, isuper/2)));
                        wires[isec][isuper][ilayer][iwire].rotateY(Math.toRadians(dbref.getAlignmentThetaY(isec, isuper/2)));
                        wires[isec][isuper][ilayer][iwire].translate(regionMids[isec][isuper/2]);
                   }
                }

            }
        }
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
        Wire regw0 = new Wire(isector+1, iregion * 2, 0, 0);
        Wire regw1 = new Wire(isector+1, iregion * 2 + 1, 7, nsgwires - 1);

        double dx_shift = y_enlargement * Math.tan(Math.toRadians(29.5));

        double reg_dz = (dbref.frontgap(iregion) + dbref.backgap(iregion) + dbref.midgap(iregion) + dbref.superwidth(iregion * 2) + dbref.superwidth(iregion * 2 + 1)) / 2.0 + z_enlargement;
        double reg_dx0 = Math.abs(regw0.bottom().x) - dx_shift + 1.0;
        double reg_dx1 = Math.abs(regw1.top().x) + dx_shift + 1.0;
        double reg_dy = regw1.top().minus(regw0.bottom()).y / Math.cos(dbref.thtilt(iregion)) / 2.0 + y_enlargement + 1.0;
        double reg_skew = 0.0;
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
        Wire lw0 = new Wire(1, isuper, ilayer, 0);
        Wire lw1 = new Wire(1, isuper, ilayer, nsgwires - 1);

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

    public Trap3D getTrajectorySurface(int isector, int isuperlayer, int ilayer) {
        Wire lw0 = new Wire(isector+1, isuperlayer, ilayer+1, 0);
        Wire lw1 = new Wire(isector+1, isuperlayer, ilayer+1, nsgwires - 1);
        
        // move to CLAS12 frame
        Vector3d p0 = lw0.right().rotateZ(Math.toRadians(-90 + isector*60));
        Vector3d p1 = lw0.left().rotateZ(Math.toRadians(-90 + isector*60));
        Vector3d p2 = lw1.left().rotateZ(Math.toRadians(-90 + isector*60)); 
        Vector3d p3 = lw1.right().rotateZ(Math.toRadians(-90 + isector*60)); 
        
        // define left and right side lines and their intersection
        Line3D left  = new Line3D(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z);
        Line3D right = new Line3D(p0.x, p0.y, p0.z, p3.x, p3.y, p3.z);
        Point3D v = left.distance(right).origin();
        
        // shift the left or right origin, depending on distance from "triangle" vertex
        double dleft  = v.distance(left.origin());
        double dright = v.distance(right.origin());
        if(dleft<dright)
            left.setOrigin(left.lerpPoint((dright-dleft)/left.length()));
        else 
            right.setOrigin(right.lerpPoint((dleft-dright)/right.length()));
        
        // shift the left or right end, depending on lengths
        double lleft  = left.length();
        double lright = right.length();
        if(lleft<lright)
            right.setEnd(right.lerpPoint(lleft/lright));
        else 
            left.setEnd(left.lerpPoint(lright/lleft));
        
        Trap3D trapezoid = new Trap3D(right.origin(), left.origin(), left.end(), right.end());
        
        return trapezoid;
    } 

    
    public double getCellSize(int isuperlayer) {
        return dbref.cellthickness(isuperlayer);
    }
    
    /*
    public void printWires(){
        System.out.println("hello");
        for(int isup=0;isup<2;isup++)
        for(int il=0;il<8;il+=7)
            for(int wire=0;wire<nsgwires+1;wire+=nsgwires/30){
        Wire regw = new Wire(isup,il,wire);
        System.out.println("line("+regw.left()+", "+regw.right()+");");
            }
    }
    */
}
