package org.jlab.detector.geant4.v2.URWELL;

import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.geometry.prim.Line3d;

import eu.mihosoft.vrl.v3d.Vector3d;
import org.jlab.detector.volume.Geant4Basic;
import java.util.List;
import org.jlab.detector.hits.DetHit;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geometry.prim.Straight;
import org.jlab.utils.groups.IndexedList;



public final class URWellStripFactory {

    URWellGeant4Factory factory;
    IndexedList<Line3D> globalStrips = new IndexedList(3);
    IndexedList<Line3D> localStrips  = new IndexedList(3);
    
    public URWellStripFactory() {
    }
    
    public URWellStripFactory(DatabaseConstantProvider cp) {
        this.init(cp);
    }
    
    public void init(DatabaseConstantProvider cp) {
        factory = new URWellGeant4Factory(cp);
        this.fillStripLists();
    }

    public int getNStripSector() {
        int nStrips = 0;
        for (int i = 0; i < URWellConstants.NCHAMBERS; i++) {
            nStrips += getNStripChamber(i);
        }
        return nStrips;
    }

    public int getNStripChamber(int ichamber) {

        double[] dim = factory.getChamberDimensions(ichamber);

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
        int nStripTotal = 0;
        for(int i=0; i<URWellConstants.NCHAMBERS; i++) {
            nStripTotal += this.getNStripChamber(i);
            if(strip <= nStripTotal)
                return i;
        }
        return -1;
    }

    private int getLocalStripId(int strip) {
        
        int chamberIndex = getChamberIndex(strip);

        //Strip ID wrt sector -> strip ID chamber (from 1 to getNStripChamber)
        int nStripTotal = 0;
        if (chamberIndex > 0) {
            for (int i = 0; i < chamberIndex; i++) {
                nStripTotal += this.getNStripChamber(i);
            }
        }

        //Strip ID: from 1 to  getNStripChamber       
        int cStrip = strip - nStripTotal;
        
        return cStrip;
    }
        
    private Line3d createStrip(int sector, int layer, int strip) {

        int chamberIndex = getChamberIndex(strip);
                
        int cStrip = this.getLocalStripId(strip);

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
        Geant4Basic chamberVolume = factory.getChamberVolume(sector, chamberIndex+1);
        // 2 point defined before wrt the GLOBAL frame     
        Vector3d globalOrigin = chamberVolume.getGlobalTransform().transform(origin);
        Vector3d globalEnd    = chamberVolume.getGlobalTransform().transform(end);

        Straight line = new Line3d(globalOrigin, globalEnd);

        // CHECK intersections between line and volume
        chamberVolume.makeSensitive();
        List<DetHit> Hits = chamberVolume.getIntersections(line);
        if (Hits.size() >= 1) {
            return new Line3d(Hits.get(0).origin(), Hits.get(0).end());

        } else {
            return null;
        }
    }

    public Line3d getGLobalStrip(int sector, int layer, int strip) {

        Line3d stripLine = createStrip(sector, layer, strip);

        return stripLine;
    }

    private Line3d getLocalStrip(int sector, int layer, int strip) {

        Line3d globalStrip = createStrip(sector, layer, strip);
        Geant4Basic sVolume = factory.getSectorVolume(sector);

        Vector3d origin = sVolume.getGlobalTransform().invert().transform(globalStrip.origin());
        Vector3d end    = sVolume.getGlobalTransform().invert().transform(globalStrip.end());

        Line3d localStrip = new Line3d(origin, end);

        return localStrip;
    }
    
    public Line3D toLocal(int sector, Line3D global) {
        Line3D local = new Line3D();
        local.copy(global);
        local.rotateZ(Math.toRadians(-60*(sector-1)));
        local.rotateY(Math.toRadians(-URWellConstants.THTILT));
        return local;
    }
    
    private void fillStripLists() {

        for(int is=0; is<URWellConstants.NSECTORS; is++) {
            int sector = is+1;
            for(int il=0; il<URWellConstants.NLAYERS; il++) {
                int layer = il+1;
                for(int ic=0; ic<this.getNStripSector(); ic++) {
                    int strip = ic+1;
                    Line3d line = this.createStrip(sector, layer, strip);
                    Point3D origin = new Point3D(line.origin().x, line.origin().y, line.origin().z);
                    Point3D end    = new Point3D(line.end().x,    line.end().y,    line.end().z);
                    Line3D global = new Line3D(origin, end);
                    Line3D local = this.toLocal(sector, global);
                    this.globalStrips.add(global, is+1, il+1, ic+1);
                    this.localStrips.add(local, is+1, il+1, ic+1);
                }
            }
        }
    }
    
    public Line3D getStrip(int sector, int layer, int strip) {
        return globalStrips.getItem(sector, layer, strip);
    }
    
    public Line3D getStripLocal(int sector, int layer, int strip) {
        return localStrips.getItem(sector, layer, strip);
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
