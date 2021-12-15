package org.jlab.rec.fmt.cross;

import java.util.ArrayList;
import java.util.List;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.fmt.cluster.Cluster;
import org.jlab.rec.fmt.hit.Hit;

/**
 * The crosses are objects used to find tracks and are characterized by a 3-D
 * point and a direction unit vector.
 *
 * @author ziegler
 *
 */
public class Cross extends ArrayList<Cluster> implements Comparable<Cross> {

    private static final long serialVersionUID = 5317526429163382618L;

    private int _Region; // region [1,...3]
    private int _Index;     // cross Id

    // point parameters:
    private Point3D _Point;
    private Point3D _PointErr;

    private Vector3D _Dir;
    private Vector3D _DirErr;

    private Cluster _clus1;
    private Cluster _clus2;

    private int _TrackIndex = -1;

    /**
     *
     * @param region the region (1...3)
     * @param index
     */
    public Cross(int region, int index) {
        this._Region = region;
        this._Index = index;
    }

    /**
     *
     * @return the region of the cross
     */
    public int getRegion() {
        return _Region;
    }

    /**
     * Sets the region
     *
     * @param _Region the region of the cross
     */
    public void setRegion(int _Region) {
        this._Region = _Region;
    }

    /**
     *
     * @return the id of the cross
     */
    public int getIndex() {
        return _Index;
    }

    /**
     * Sets the cross ID
     *
     * @param index
     */
    public void setIndex(int index) {
        this._Index = index;
    }

    /**
     *
     * @return a 3-D point characterizing the position of the cross in the
     * tilted coordinate system.
     */
    public Point3D getPoint() {
        return _Point;
    }

    /**
     * Sets the cross 3-D point
     *
     * @param _Point a 3-D point characterizing the position of the cross in the
     * tilted coordinate system.
     */
    public void setPoint(Point3D _Point) {
        this._Point = _Point;
    }

    /**
     *
     * @return a 3-dimensional error on the 3-D point characterizing the
     * position of the cross in the tilted coordinate system.
     */
    public Point3D getPointErr() {
        return _PointErr;
    }

    /**
     * Sets a 3-dimensional error on the 3-D point
     *
     * @param _PointErr a 3-dimensional error on the 3-D point characterizing
     * the position of the cross in the tilted coordinate system.
     */
    public void setPointErr(Point3D _PointErr) {
        this._PointErr = _PointErr;
    }

    /**
     *
     * @return the cross unit direction vector
     */
    public Vector3D getDir() {
        return _Dir;
    }

    /**
     * Sets the cross unit direction vector
     *
     * @param _Dir the cross unit direction vector
     */
    public void setDir(Vector3D _Dir) {
        this._Dir = _Dir;
    }

    /**
     *
     * @return the cross unit direction vector
     */
    public Vector3D getDirErr() {
        return _DirErr;
    }

    /**
     * Sets the cross unit direction vector
     *
     * @param _DirErr the cross unit direction vector
     */
    public void setDirErr(Vector3D _DirErr) {
        this._DirErr = _DirErr;
    }

    /**
     *
     * @return serialVersionUID
     */
    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    /**
     * Sorts crosses by azimuth angle values
     */
    @Override
    public int compareTo(Cross arg) {
        if (this.getPoint().toVector3D().phi() < arg.getPoint().toVector3D().phi()) {
            return 1;
        } else {
            return -1;
        }
    }

    /**
     * Set the first cluster (corresponding to the first superlayer in a region)
     *
     * @param seg1 the Cluster (in the first superlayer) which is used to make a
     * cross
     */
    public void setCluster1(Cluster seg1) {
        this._clus1 = seg1;
    }

    /**
     * Set the second Cluster (corresponding to the second superlayer in a
     * region)
     *
     * @param seg2 the Cluster (in the second superlayer) which is used to make
     * a cross
     */
    public void setCluster2(Cluster seg2) {
        this._clus2 = seg2;
    }

    /**
     *
     * @return he Cluster (in the first superlayer) which is used to make a
     * cross
     */
    public Cluster getCluster1() {
        return _clus1;
    }

    /**
     *
     * @return the Cluster (in the second superlayer) which is used to make a
     * cross
     */
    public Cluster getCluster2() {
        return _clus2;
    }

    /**
     * Sets the cross parameters: the position and direction unit vector
     */
    public void setCrossParams() {

        Cluster inlayerclus = this.getCluster1();
        Cluster outlayerclus = this.getCluster2();

        // Getting the cross position from the calculated centroid segment line positions
        //----------------------------------------------------
//		double x0_inner = inlayerclus.getGlobalSegment().origin().x();
//		double x1_inner = inlayerclus.getGlobalSegment().end().x();
//		double x0_outer = outlayerclus.getGlobalSegment().origin().x();
//		double x1_outer = outlayerclus.getGlobalSegment().end().x();
//		double y0_inner = inlayerclus.getGlobalSegment().origin().y();
//		double y1_inner = inlayerclus.getGlobalSegment().end().y();
//		double y0_outer = outlayerclus.getGlobalSegment().origin().y();
//		double y1_outer = outlayerclus.getGlobalSegment().end().y();
//		double z0_inner = inlayerclus.getGlobalSegment().origin().z();
//		double z0_outer = outlayerclus.getGlobalSegment().origin().z();
        Point3D interPoint = inlayerclus.getGlobalSegment().distanceSegments(outlayerclus.getGlobalSegment()).midpoint();

        this.setPoint(interPoint);
        this.setPointErr(new Point3D(inlayerclus.getCentroidError() / Math.sqrt(2.), outlayerclus.getCentroidError() / Math.sqrt(2.), 0));
    }

    public int getTrackIndex() {
        return _TrackIndex;
    }

    public void setTrackIndex(int _AssociatedTrackID) {
        this._TrackIndex = _AssociatedTrackID;
    }

    public void setAssociatedElementsIDs() {
        for (Cluster cluster : this) {
            cluster.setCrossIndex(this._Index);
            cluster.setTrackIndex(this._TrackIndex);

            for (Hit hit : cluster) {
                hit.setClusterIndex(cluster.getIndex());
                hit.setCrossIndex(this._Index);
                hit.setTrackIndex(this._TrackIndex);
            }
        }
    }

    /**
     *
     * @return the track info.
     */
    public String printInfo() {
        String s = "fmt cross: Index " + this.getIndex() + " trackIndex " + this.getTrackIndex()
                + " Region " + this.getRegion()
                + " Point "  + this.getPoint().toString();
        return s;
    }
    
    @Deprecated
    public static ArrayList<Cross> findCrosses(List<Cluster> clusters) {

        // first separate the segments according to layers
        ArrayList<Cluster> allinnerlayrclus = new ArrayList<Cluster>();
        ArrayList<Cluster> allouterlayrclus = new ArrayList<Cluster>();

        // Sorting by layer first:
        for (Cluster theclus : clusters) {
            if (theclus.getLayer() % 2 == 0) {
                allouterlayrclus.add(theclus);
            }
            if (theclus.getLayer() % 2 == 1) {
                allinnerlayrclus.add(theclus);
            }
        }

        ArrayList<Cross> crosses = new ArrayList<Cross>();

        int rid = 0;
        for (Cluster inlayerclus : allinnerlayrclus) {
            for (Cluster outlayerclus : allouterlayrclus) {
                if (outlayerclus.getLayer() - inlayerclus.getLayer() != 1) {
                    continue;
                }
//                // put correct numbers to make sure the intersection is valid
//				if ((inlayerclus.get_MinStrip() + outlayerclus.get_MinStrip() > 1)
//						&& (inlayerclus.get_MaxStrip() + outlayerclus.get_MaxStrip() < Geometry.nStrips*2) ) {

                // define new cross
                Cross this_cross = new Cross(inlayerclus.getRegion(), rid++);
                this_cross.setCluster1(inlayerclus);
                this_cross.setCluster2(outlayerclus);

                this_cross.setCrossParams();
                // make arraylist
                crosses.add(this_cross);
//				}
            }
        }
        return crosses;
    }

}
