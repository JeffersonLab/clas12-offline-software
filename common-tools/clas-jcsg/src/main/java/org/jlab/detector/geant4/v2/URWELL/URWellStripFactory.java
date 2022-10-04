package org.jlab.detector.geant4.v2.URWELL;

import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.geometry.prim.Line3d;

import eu.mihosoft.vrl.v3d.Vector3d;
import org.jlab.detector.volume.Geant4Basic;
import java.util.List;
import org.jlab.detector.hits.DetHit;
import org.jlab.geometry.prim.Straight;

public class URWellStripFactory {

    DatabaseConstantProvider cp = new DatabaseConstantProvider(11, "default");
    URWellGeant4Factory factory = new URWellGeant4Factory(cp);

    public URWellStripFactory(DatabaseConstantProvider cp) {
        URWellConstants.connect(cp);

    }

    public int[] getNumberStripSector() {
        int[] nStrips = new int[3];
        for (int i = 0; i < URWellConstants.NCHAMBERS; i++) {
            nStrips[i] = getNumberStripChamber(i);
        }
        return nStrips;
    }

    public int getNumberStripChamber(int aChamber) {

        double[] dim = factory.getChamberDimensions(aChamber);

        double xHalfSmallBase = dim[0];
        double xHalfLargeBase = dim[1];
        double yHalf          = dim[2];

        // C-------------D //
        //  -------------  //
        //   -----------   //
        //    A-------B   //
        /**
         * * number of strip in AB**
         */
        int nAB = (int) (2 * xHalfSmallBase / (URWellConstants.PITCH
                  / Math.sin(Math.toRadians(URWellConstants.STEREOANGLE))));

        double AC = Math.sqrt((Math.pow((xHalfSmallBase - xHalfLargeBase), 2) + Math.pow((2 * yHalf), 2)));
        double theta = Math.acos(2 * yHalf / AC);
        int nAC = (int) (AC / (URWellConstants.PITCH
                / Math.cos(theta - Math.toRadians(URWellConstants.STEREOANGLE))));

        int nStrips = nAB + nAC + 1;

        return nStrips;
    }

    public int getChamberIndex(int strip) {
        int chamberIndex = 0;
        if (strip <= getNumberStripChamber(0)) {
            chamberIndex = 0;
        }
        else if (strip > getNumberStripChamber(0)
                && strip <= (getNumberStripChamber(0) + getNumberStripChamber(1))) {
            chamberIndex = 1;
        }
        else if (strip > (getNumberStripChamber(0) + getNumberStripChamber(1))) {
            chamberIndex = 2;
        }
        return chamberIndex;
    }

    public Line3d createStrip(int sector, int layer, int strip) {

        Line3d stripLine;

        int chamberIndex = getChamberIndex(strip);

        //Strip ID wrt sector -> strip ID chamber (from 1 to getNumberStripChamber)
        int[] nStripChamber = getNumberStripSector();
        int nStripTotal = 0;
        if (chamberIndex > 0) {
            for (int i = 0; i < chamberIndex; i++) {
                nStripTotal += nStripChamber[i];
            }
        }
        //Strip ID: from 1 to  getNumberStripChamber       
        int cStrip = strip - nStripTotal;

        // CHAMBER reference frame
        // new numeration with stri ID_strip=0 crossing (0,0,0) of chamber
        double[] dim = factory.getChamberDimensions(chamberIndex);

        // Y coordinate of the intersection point between the x=0 and the strip line crossing for B
        double DY = -dim[2] - Math.tan(Math.toRadians(URWellConstants.STEREOANGLE)) * dim[0];

        // ID of the strip 
        int nS = (int) (DY * Math.cos(Math.toRadians(URWellConstants.STEREOANGLE)) / URWellConstants.PITCH);
        int nCStrip = nS + (cStrip - 1);

        //strip straight line chamber reference frame -> y = mx +c; 
        double stereoAngle = URWellConstants.STEREOANGLE;
        if (layer % 2 != 0) {
            stereoAngle = -URWellConstants.STEREOANGLE;
        }
        double m = Math.tan(Math.toRadians(stereoAngle));
        double c = nCStrip * URWellConstants.PITCH / Math.cos(Math.toRadians(stereoAngle));

        // Take 2 points in the strip straight line. They needs to define Line object 
        double oX = -dim[1];
        double oY = -dim[1] * m + c;
        double oZ = 0;
        Vector3d origin = new Vector3d(oX, oY, oZ);

        double eX = dim[1];
        double eY = dim[1] * m + c;
        double eZ = 0;
        Vector3d end = new Vector3d(eX, eY, eZ);

        // Get Chamber Volume
        Geant4Basic chamberVolume = factory.getChamberVolume(chamberIndex+1, sector, strip);
        // 2 point defined before wrt the GLOBAL frame     
        Vector3d globalOrigin = chamberVolume.getGlobalTransform().transform(origin);
        Vector3d globalEnd    = chamberVolume.getGlobalTransform().transform(end);

        Straight line = new Line3d(globalOrigin, globalEnd);

        // CHECK intersections between line and volume
        chamberVolume.makeSensitive();
        List<DetHit> Hits = chamberVolume.getIntersections(line);
        if (Hits.size() >= 1) {
            stripLine = new Line3d(Hits.get(0).origin(), Hits.get(0).end());

        } else {
            stripLine = null;
        }

        return stripLine;
    }

    public Line3d getGLobalStrip(int sector, int layer, int strip) {

        Line3d stripLine = createStrip(sector, layer, strip);

        return stripLine;
    }

    public Line3d getLocalStrip(int sector, int layer, int strip) {

        Line3d globalStrip = createStrip(sector, layer, strip);
        Geant4Basic sVolume = factory.getSectorVolume(sector);

        Vector3d origin = sVolume.getGlobalTransform().invert().transform(globalStrip.origin());
        Vector3d end    = sVolume.getGlobalTransform().invert().transform(globalStrip.end());

        Line3d localStrip = new Line3d(origin, end);

        return localStrip;
    }

    public static void main(String[] args) {
        DatabaseConstantProvider cp = new DatabaseConstantProvider(11, "default");

        URWellConstants.connect(cp);

        URWellGeant4Factory factory = new URWellGeant4Factory(cp);
        URWellStripFactory factory2 = new URWellStripFactory(cp);

        Line3d strip_line = factory2.getLocalStrip(1, 0, 1600);
        System.out.println(strip_line.toString());

    }

}
