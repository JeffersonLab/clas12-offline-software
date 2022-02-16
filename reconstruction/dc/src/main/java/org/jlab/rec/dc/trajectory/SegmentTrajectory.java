package org.jlab.rec.dc.trajectory;

import org.jlab.detector.geant4.v2.DCGeant4Factory;


public class SegmentTrajectory {

    public SegmentTrajectory() {
            // TODO Auto-generated constructor stub
    }

    private int _Sector;      							//	    sector[1...6]
    private int _Superlayer;    	 					//	    superlayer [1,...6]
    private int _SegmentId;								//		Segment Id
    private double[] trkDoca = new double[6];			// list of trkDocas of trajectory hits for all layers in a superlayer
    private int[] matchedHitId = new int[6];			// list of of matched hits of trajectory hits for all layers in a superlayer

    public int get_Sector() {
            return _Sector;
    }

    public void set_Sector(int _Sector) {
            this._Sector = _Sector;
    }

    public int get_Superlayer() {
            return _Superlayer;
    }

    public void set_Superlayer(int _Superlayer) {
            this._Superlayer = _Superlayer;
    }

    public int get_SegmentId() {
            return _SegmentId;
    }

    public void set_SegmentId(int _SegmentId) {
            this._SegmentId = _SegmentId;
    }

    public double[] getTrkDoca() {
            return trkDoca;
    }

    public void setTrkDoca(double[] trkDoca) {
            this.trkDoca = trkDoca;
    }

    public int[] getMatchedHitId() {
            return matchedHitId;
    }

    public void setMatchedHitId(int[] matchedHitId) {
            this.matchedHitId = matchedHitId;
    }

    /**
     * @param sector
     * @param superlayer
     * @param layer
     * @param trkX trk x in local tilted coordinate system
     * @param DcDetector
     * @return the wire that should fire for a trajectory with x value trkX at the given superlayer and layer
     */
    public int getWireOnTrajectory(int sector, int superlayer, int layer, double trkX, DCGeant4Factory DcDetector) {

            //double x1 = GeometryLoader.dcDetector.getSector(0).getSuperlayer(superlayer-1).getLayer(layer-1).getComponent(1).getMidpoint().x();
            //double x0 = GeometryLoader.dcDetector.getSector(0).getSuperlayer(superlayer-1).getLayer(layer-1).getComponent(0).getMidpoint().x();
            double x1 = DcDetector.getWireMidpoint(sector-1, superlayer-1, layer-1, 1).x;
            double x0 = DcDetector.getWireMidpoint(sector-1, superlayer-1, layer-1, 0).x;

            double deltax = Math.abs(x1-x0);

            //double xFirstCell = GeometryLoader.dcDetector.getSector(0).getSuperlayer(superlayer-1).getLayer(layer-1).getComponent(0).getMidpoint().x();
            double xFirstCell = DcDetector.getWireMidpoint(sector-1, superlayer-1, layer-1, 0).x;

            int nearestWire = (int)Math.ceil((trkX-xFirstCell+deltax/2.)/deltax) ;

            if(nearestWire>112)
                    nearestWire = 112;
            if(nearestWire<1)
                    nearestWire = 1;
            return nearestWire;
    }
	
}
