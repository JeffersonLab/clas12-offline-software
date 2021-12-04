package org.jlab.rec.fmt.cluster;

import java.util.ArrayList;
import java.util.List;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.rec.fmt.hit.Hit;

/**
 * A cluster in the fmt consists of an array of hits that are grouped together
 * according to the algorithm of the ClusterFinder class
 *
 * @author ziegler
 * @author benkel
 * @author devita
*/
public class Cluster extends ArrayList<Hit> implements Comparable<Cluster> {

        private static final long serialVersionUID = 9153980362683755204L;

        private int _Layer;
        private int _Index;
        private double _Doca;
        private Line3D _GlobalSegment;
        private Line3D _LocalSegment;

        private double _TotalEnergy;
        private double _Centroid;
        private double _CentroidError;
        private double _CentroidResidual;
        private double _Time;

        private int _MinStrip;
        private int _MaxStrip;
        private int _SeedStrip;
        private int _SeedIndex;
        private double _SeedEnergy;
        private double _SeedTime;

        private int _AssociatedCrossIndex = -1;
        private int _AssociatedTrackIndex = -1;

        /**
         *
         * @param layer the layer
         * @param index
         */
        public Cluster(int layer, int index) {
            this._Layer = layer;
            this._Index = index;
            this._Doca = Double.POSITIVE_INFINITY;
        }

        /**
         *
         * @return the layer of the cluster (1...6)
         */
        public int getLayer() {
            return _Layer;
        }

        /**
         *
         * @param _Layer
         */
        public void setLayer(int _Layer) {
            this._Layer = _Layer;
        }

        /**
         *
         * @return region (1...4)
         */
        public int getRegion() {
            return (int) (this._Layer + 1) / 2;
        }

        public int getIndex() {
            return _Index;
        }

        public void setIndex(int _index) {
            this._Index = _index;
        }

        public double getDoca() {
            return this._Doca;
        }

        public void setDoca(double Doca) {
            this._Doca = Doca;
        }


        public double getCentroid() {
            return _Centroid;
        }

        public void setCentroid(double _Centroid) {
            this._Centroid = _Centroid;
        }

        public double getCentroidError() {
            return _CentroidError;
        }

        public void setCentroidError(double _CentroidError) {
            this._CentroidError = _CentroidError;
        }

        public double getCentroidResidual() {
            return _CentroidResidual;
        }

        public void setCentroidResidual(double trackLocalY) {
            this._CentroidResidual = this._Centroid-trackLocalY;
            for(Hit hit : this) hit.setResidual(trackLocalY);
        }

        public double getTotalEnergy() {
            return _TotalEnergy;
        }

        public void setTotalEnergy(double _TotalEnergy) {
            this._TotalEnergy = _TotalEnergy;
        }

        public double getTime() {
            return _Time;
        }

        public void setTime(double _Time) {
            this._Time = _Time;
        }

        public int getMinStrip() {
            return _MinStrip;
        }

        public void setMinStrip(int _MinStrip) {
            this._MinStrip = _MinStrip;
        }

        public int getMaxStrip() {
            return _MaxStrip;
        }

        public void setMaxStrip(int _MaxStrip) {
            this._MaxStrip = _MaxStrip;
        }

        public int getSeedStrip() {
            return _SeedStrip;
        }

        public void setSeedStrip(int _SeedStrip) {
            this._SeedStrip = _SeedStrip;
        }

        public double getSeedEnergy() {
            return _SeedEnergy;
        }

        public void setSeedEnergy(double _SeedEnergy) {
            this._SeedEnergy = _SeedEnergy;
        }

        public double getSeedTime() {
            return _SeedTime;
        }

        public void setSeedTime(double _SeedTime) {
            this._SeedTime = _SeedTime;
        }

        public int getSeedIndex() {
            return _SeedIndex;
        }

        public void setSeedIndex(int _SeedIndex) {
            this._SeedIndex = _SeedIndex;
        }

        public Line3D getGlobalSegment() {
            return _GlobalSegment;
        }

        public void setGlobalSegment(Line3D segment) {
            this._GlobalSegment = segment;
        }

        public Line3D getLocalSegment() {
            return _LocalSegment;
        }

        public void setLocalSegment(Line3D segment) {
            this._LocalSegment = segment;
        }

        public int getCrossIndex() {
            return _AssociatedCrossIndex;
        }

        public void setCrossIndex(int _AssociatedCrossIndex) {
            this._AssociatedCrossIndex = _AssociatedCrossIndex;
        }

        public int getTrackIndex() {
            return _AssociatedTrackIndex;
        }

        public void setTrackIndex(int _AssociatedTrackIndex) {
            this._AssociatedTrackIndex = _AssociatedTrackIndex;
            for(Hit hit: this) hit.setTrackIndex(_AssociatedTrackIndex);
        }

        private boolean containsHit(Hit hit) {
            boolean addFlag = false;
            if(hit.getLayer()==this.getLayer()) {
                for (Hit aThi : this) {
                    if (aThi.isClose(hit)) {
                        addFlag = true;
                        break;
                    }
                }                
            }
            return addFlag;
        }


        public static ArrayList<Cluster> findClusters(List<Hit> hits) {
            ArrayList<Cluster> clusters = new ArrayList<>();
            
            for(Hit hit: hits) {
                if(hit.getStatus()!=0) continue;
                if(hit.getClusterIndex()==-1)  {                       // this hit is not yet associated with a cluster
                    for(int jclus=0; jclus<clusters.size(); jclus++) {
                        Cluster cluster = clusters.get(jclus);
                        if(cluster.containsHit(hit)) {
                            hit.setClusterIndex(cluster.getIndex());     // attaching hit to previous cluster 
                            cluster.add(hit);
                            break;
                        }
                    }
                }
                if(hit.getClusterIndex()==-1)  {                       // new cluster found
                    Cluster cluster = new Cluster(hit.getLayer(),clusters.size());
                    hit.setClusterIndex(cluster.getIndex());
                    cluster.add(hit);
                    clusters.add(cluster);
                }
            }
            
            for(int i=0; i<clusters.size(); i++) clusters.get(i).calc_CentroidParams(true);
            
            return clusters;
        }

        /**
         * Sets energy-weighted parameters; these are the strip centroid
         * (energy-weighted) value
         * @param eweight set to true for energy weighting
         */
        public void calc_CentroidParams(boolean eweight) {
            // instantiation of variables
            double stripNumCent = 0;			// cluster Lorentz-angle-corrected energy-weighted strip = centroid


            double totEn = 0.;			// cluster total energy
            double totWeight = 0;
            double weightedStrp = 0;			// energy-weighted strip

            double weightedStripEndPoint1X = 0;	// Energy-weighted x of the strip first end point
            double weightedStripEndPoint1Y = 0;	// Energy-weighted y of the strip first end point
            double weightedStripEndPoint1Z = 0;	// Energy-weighted z of the strip first end point
            double weightedStripEndPoint2X = 0;	// Energy-weighted x of the strip second end point
            double weightedStripEndPoint2Y = 0;	// Energy-weighted y of the strip second end point
            double weightedStripEndPoint2Z = 0;	// Energy-weighted z of the strip second end point

            double weightedLocStripEndPoint1X = 0;	// Energy-weighted x of the strip first end point
            double weightedLocStripEndPoint1Y = 0;	// Energy-weighted y of the strip first end point
            double weightedLocStripEndPoint1Z = 0;	// Energy-weighted z of the strip first end point
            double weightedLocStripEndPoint2X = 0;	// Energy-weighted x of the strip second end point
            double weightedLocStripEndPoint2Y = 0;	// Energy-weighted y of the strip second end point
            double weightedLocStripEndPoint2Z = 0;	// Energy-weighted z of the strip second end point

            double averageTime = 0;

            int nbhits = this.size();

            if (nbhits != 0) {
                int min = Integer.MAX_VALUE;
                int max = Integer.MIN_VALUE;
                Hit seed = null;

                // looping over the number of hits in the cluster
                for (int i = 0; i < nbhits; i++) {
                    Hit thehit = this.get(i);

                    // get the energy value of the strip
                    double strpEn = thehit.getEnergy();
                    double strpTm = thehit.getTime();
                    
                    double weight = 1;
                    if(eweight) weight = strpEn;
//                    // set the cluster's Tmin
//                    if (this._Tmin > thehit.getTime()) {
//                            this._Tmin = thehit.getTime();
//                    }

                    // get strip informations
                    int strpNb = thehit.getStrip();
                    double x1 = thehit.getStripGlobalSegment().origin().x();
                    double y1 = thehit.getStripGlobalSegment().origin().y();
                    double z1 = thehit.getStripGlobalSegment().origin().z();
                    double x2 = thehit.getStripGlobalSegment().end().x();
                    double y2 = thehit.getStripGlobalSegment().end().y();
                    double z2 = thehit.getStripGlobalSegment().end().z();

                    double lx1 = thehit.getStripLocalSegment().origin().x();
                    double ly1 = thehit.getStripLocalSegment().origin().y();
                    double lz1 = thehit.getStripLocalSegment().origin().z();
                    double lx2 = thehit.getStripLocalSegment().end().x();
                    double ly2 = thehit.getStripLocalSegment().end().y();
                    double lz2 = thehit.getStripLocalSegment().end().z();

                    totEn += strpEn;
                    totWeight += weight;
                    weightedStrp += weight * thehit.getStripLocalSegment().origin().y();
                    weightedStripEndPoint1X += weight * x1;
                    weightedStripEndPoint1Y += weight * y1;
                    weightedStripEndPoint1Z += weight * z1;
                    weightedStripEndPoint2X += weight * x2;
                    weightedStripEndPoint2Y += weight * y2;
                    weightedStripEndPoint2Z += weight * z2;

                    weightedLocStripEndPoint1X += weight * lx1;
                    weightedLocStripEndPoint1Y += weight * ly1;
                    weightedLocStripEndPoint1Z += weight * lz1;
                    weightedLocStripEndPoint2X += weight * lx2;
                    weightedLocStripEndPoint2Y += weight * ly2;
                    weightedLocStripEndPoint2Z += weight * lz2;
                    
                    averageTime += strpTm;

                    // getting the max and min strip number in the cluster
                    if (strpNb <= min) {
                        min = strpNb;
                    }
                    if (strpNb >= max) {
                        max = strpNb;
                    }

//                if (totEn == 0) {
//                    System.err.println(" Cluster energy is zero .... exit");
//                    return;
//                }
                }
                
                // calculates the centroid values and associated positions
                stripNumCent = weightedStrp / totWeight;
                weightedStripEndPoint1X = weightedStripEndPoint1X / totWeight;
                weightedStripEndPoint1Y = weightedStripEndPoint1Y / totWeight;
                weightedStripEndPoint1Z = weightedStripEndPoint1Z / totWeight;
                weightedStripEndPoint2X = weightedStripEndPoint2X / totWeight;
                weightedStripEndPoint2Y = weightedStripEndPoint2Y / totWeight;
                weightedStripEndPoint2Z = weightedStripEndPoint2Z / totWeight;
                weightedLocStripEndPoint1X = weightedLocStripEndPoint1X / totWeight;
                weightedLocStripEndPoint1Y = weightedLocStripEndPoint1Y / totWeight;
                weightedLocStripEndPoint1Z = weightedLocStripEndPoint1Z / totWeight;
                weightedLocStripEndPoint2X = weightedLocStripEndPoint2X / totWeight;
                weightedLocStripEndPoint2Y = weightedLocStripEndPoint2Y / totWeight;
                weightedLocStripEndPoint2Z = weightedLocStripEndPoint2Z / totWeight;                
                averageTime /= this.size();

                double delta = Double.POSITIVE_INFINITY;
                for (int i = 0; i < nbhits; i++) {
                    Hit thehit = this.get(i);
                    if(Math.abs(thehit.getStrip()-stripNumCent)<delta) {
                        delta = Math.abs(thehit.getStrip()-stripNumCent);
                        seed  = thehit;
                    }
                }

                _TotalEnergy   = totEn;
                _Centroid      = stripNumCent;
                _CentroidError = seed.getError();// / Math.sqrt(this.size());
                _GlobalSegment = new Line3D(weightedStripEndPoint1X,weightedStripEndPoint1Y,weightedStripEndPoint1Z,
                                           weightedStripEndPoint2X,weightedStripEndPoint2Y,weightedStripEndPoint2Z);
                _LocalSegment  = new Line3D(weightedLocStripEndPoint1X,weightedLocStripEndPoint1Y,weightedLocStripEndPoint1Z,
                                           weightedLocStripEndPoint2X,weightedLocStripEndPoint2Y,weightedLocStripEndPoint2Z);
                _Time          = averageTime;
                _SeedIndex     = seed.getIndex();
                _SeedStrip     = seed.getStrip();
                _SeedEnergy    = seed.getEnergy();
                _SeedTime      = seed.getTime();
                _MinStrip      = min;
                _MaxStrip      = max;
            }
            
        }


        public double distance(double x, double y, double z) {
            Point3D trkPoint = new Point3D(x, y, z);
            return _GlobalSegment.distanceSegment(trkPoint).length();
        }

        public double distance(Point3D point) {
            return _GlobalSegment.distanceSegment(point).length();
        }

        public Point3D calcCross(double x, double y, double z) {
            Point3D trkPoint = new Point3D(x, y, z);
            return _GlobalSegment.distanceSegment(trkPoint).origin();
        }

        public Point3D calcCross(Point3D point) {
            return _GlobalSegment.distanceSegment(point).origin();
        }

        @Override
        public int compareTo(Cluster arg) {
            // Sort by layer and seed strip
            int return_val = 0;
            int CompLay  = this.getLayer()    < arg.getLayer()     ? -1 : this.getLayer()     == arg.getLayer()     ? 0 : 1;
            int CompSeed = this.getSeedStrip()< arg.getSeedStrip() ? -1 : this.getSeedStrip() == arg.getSeedStrip() ? 0 : 1;

            return_val = ((CompLay == 0) ? CompSeed : CompLay);

            return return_val;
        }

        /**
         *
         * @return cluster info. about location and number of hits contained in it
         */
        public String toStringBrief() {
            String str = "FMT cluster: Index " + this.getIndex() 
                                  + ", Layer " + this.getLayer()
                                  + ", Seed "  + this.getSeedStrip()
                                  + ", Size "  + this.size()
                                  + ", LocX "  + String.format("%.4f", this.getLocalSegment().midpoint().x())
                                  + ", LocY "  + String.format("%.4f", this.getLocalSegment().midpoint().y());
            return str;
        }

        @Override
        public String toString() {
            String str = this.toStringBrief();
            for(Hit aThi : this) {
                str = str.concat("\n" + aThi.toString());
            }
            return str;
        }
}
