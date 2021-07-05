package org.jlab.rec.fmt.cluster;

import java.util.ArrayList;
import java.util.List;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.rec.fmt.Constants;
import org.jlab.rec.fmt.hit.FittedHit;
import org.jlab.rec.fmt.hit.Hit;

/**
 * A cluster in the fmt consists of an array of hits that are grouped together
 * according to the algorithm of the ClusterFinder class
 *
 * @author ziegler
 */
public class Cluster extends ArrayList<FittedHit> implements Comparable<Cluster> {

        private static final long serialVersionUID = 9153980362683755204L;

        private int _Sector;
        private int _Layer;
        private int _Index;
        private double _Doca;
        private Line3D _GlobalSegment;
        private Line3D _LocalSegment;

        private double _TotalEnergy;
        private double _Centroid;
        private double _CentroidError;
        private double _CentroidResidual;

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
         * @param sector the sector
         * @param layer the layer
         * @param cid the cluster ID, an incremental integer corresponding to the
         * cluster formed in the series of clusters
         */
        public Cluster(int sector, int layer, int index) {
            this._Sector = sector;
            this._Layer = layer;
            this._Index = index;
            this._Doca = Double.POSITIVE_INFINITY;
        }

        /**
         *
         * @return the sector of the cluster (1...24)
         */
        public int get_Sector() {
            return _Sector;
        }

        /**
         *
         * @param _Sector sector of the cluster (1...24)
         */
        public void set_Sector(int _Sector) {
            this._Sector = _Sector;
        }

        /**
         *
         * @return the layer of the cluster (1...6)
         */
        public int get_Layer() {
            return _Layer;
        }

        /**
         *
         * @param _Superlayer the layer of the cluster (1...6)
         */
        public void set_Layer(int _Layer) {
            this._Layer = _Layer;
        }

        /**
         *
         * @return region (1...4)
         */
        public int get_Region() {
            return (int) (this._Layer + 1) / 2;
        }

        public int get_Index() {
            return _Index;
        }

        public void set_Index(int _index) {
            this._Index = _index;
        }

        public double get_Doca() {
            return this._Doca;
        }

        public void set_Doca(double Doca) {
            this._Doca = Doca;
        }


        public double get_Centroid() {
            return _Centroid;
        }

        public void set_Centroid(double _Centroid) {
            this._Centroid = _Centroid;
        }

        public double get_CentroidError() {
            return _CentroidError;
        }

        public void set_CentroidError(double _CentroidError) {
            this._CentroidError = _CentroidError;
        }

        public double get_CentroidResidual() {
            return _CentroidResidual;
        }

        public void set_CentroidResidual(double trackLocalY) {
            this._CentroidResidual = this._Centroid-trackLocalY;
            for(FittedHit hit : this) hit.set_Residual(trackLocalY);
        }

        public double get_TotalEnergy() {
            return _TotalEnergy;
        }

        public void set_TotalEnergy(double _TotalEnergy) {
            this._TotalEnergy = _TotalEnergy;
        }

        public int get_MinStrip() {
            return _MinStrip;
        }

        public void set_MinStrip(int _MinStrip) {
            this._MinStrip = _MinStrip;
        }

        public int get_MaxStrip() {
            return _MaxStrip;
        }

        public void set_MaxStrip(int _MaxStrip) {
            this._MaxStrip = _MaxStrip;
        }

        public int get_SeedStrip() {
            return _SeedStrip;
        }

        public void set_SeedStrip(int _SeedStrip) {
            this._SeedStrip = _SeedStrip;
        }

        public double get_SeedEnergy() {
            return _SeedEnergy;
        }

        public void set_SeedEnergy(double _SeedEnergy) {
            this._SeedEnergy = _SeedEnergy;
        }

        public double get_SeedTime() {
            return _SeedTime;
        }

        public void set_SeedTime(double _SeedTime) {
            this._SeedTime = _SeedTime;
        }

        public int getSeedIndex() {
            return _SeedIndex;
        }

        public void setSeedIndex(int _SeedIndex) {
            this._SeedIndex = _SeedIndex;
        }

        public Line3D get_GlobalSegment() {
            return _GlobalSegment;
        }

        public void set_GlobalSegment(Line3D segment) {
            this._GlobalSegment = segment;
        }

        public Line3D get_LocalSegment() {
            return _LocalSegment;
        }

        public void set_LocalSegment(Line3D segment) {
            this._LocalSegment = segment;
        }

        public int get_CrossIndex() {
            return _AssociatedCrossIndex;
        }

        public void set_CrossIndex(int _AssociatedCrossIndex) {
            this._AssociatedCrossIndex = _AssociatedCrossIndex;
        }

        public int get_TrackIndex() {
            return _AssociatedTrackIndex;
        }

        public void set_TrackIndex(int _AssociatedTrackIndex) {
            this._AssociatedTrackIndex = _AssociatedTrackIndex;
            for(FittedHit hit: this) hit.set_TrackIndex(_AssociatedTrackIndex);
        }

        private boolean containsHit(FittedHit hit) {
            boolean addFlag = false;
            if(hit.get_Layer()==this.get_Layer()) {
                for(int j = 0; j< this.size(); j++) {
                    if(this.get(j).isClose(hit)) {
                        addFlag = true;
                        break;
                    }
                }                
            }
            return addFlag;
        }


        public static ArrayList<Cluster> findClusters(List<Hit> hits) {
            ArrayList<Cluster> clusters = new ArrayList<Cluster>();
            
            for(int ihit=0; ihit<hits.size(); ihit++) {
                FittedHit hit = new FittedHit(hits.get(ihit));
                if(hit.get_ClusterIndex()==-1)  {                       // this hit is not yet associated with a cluster
                    for(int jclus=0; jclus<clusters.size(); jclus++) {
                        Cluster cluster = clusters.get(jclus);
                        if(cluster.containsHit(hit)) {
                            hit.set_ClusterIndex(cluster.get_Index());     // attaching hit to previous cluster 
                            cluster.add(hit);
                            break;
                        }
                    }
                }
                if(hit.get_ClusterIndex()==-1)  {                       // new cluster found
                    Cluster cluster = new Cluster(hit.get_Sector(),hit.get_Layer(),clusters.size());
                    hit.set_ClusterIndex(cluster.get_Index());
                    cluster.add(hit);
                    clusters.add(cluster);
                }
            }
            
            for(int i=0; i<clusters.size(); i++) clusters.get(i).calc_CentroidParams();
            
            return clusters;
        }

        /**
         * Sets energy-weighted parameters; these are the strip centroid
         * (energy-weighted) value, the energy-weighted phi for Z detectors and the
         * energy-weighted z for C detectors
         */
        public void calc_CentroidParams() {
            // instantiation of variables
            double stripNumCent = 0;			// cluster Lorentz-angle-corrected energy-weighted strip = centroid


            double totEn = 0.;					// cluster total energy
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

            int nbhits = this.size();

            if (nbhits != 0) {
                int min = Integer.MAX_VALUE;
                int max = -1;
                int seed = -1;
                double Emax = -1;
                double Time = -1;

                // looping over the number of hits in the cluster
                for (int i = 0; i < nbhits; i++) {
                    FittedHit thehit = this.get(i);

                    // get the energy value of the strip
                    double strpEn = thehit.get_Energy();
                    double strpTm = thehit.get_Time();

//                    // set the cluster's Tmin
//                    if (this._Tmin > thehit.get_Time()) {
//                            this._Tmin = thehit.get_Time();
//                    }

                    // get strip informations
                    int strpNb = thehit.get_Strip();
                    double x1 = thehit.get_StripGlobalSegment().origin().x();
                    double y1 = thehit.get_StripGlobalSegment().origin().y();
                    double z1 = thehit.get_StripGlobalSegment().origin().z();
                    double x2 = thehit.get_StripGlobalSegment().end().x();
                    double y2 = thehit.get_StripGlobalSegment().end().y();
                    double z2 = thehit.get_StripGlobalSegment().end().z();

                    double lx1 = thehit.get_StripLocalSegment().origin().x();
                    double ly1 = thehit.get_StripLocalSegment().origin().y();
                    double lz1 = thehit.get_StripLocalSegment().origin().z();
                    double lx2 = thehit.get_StripLocalSegment().end().x();
                    double ly2 = thehit.get_StripLocalSegment().end().y();
                    double lz2 = thehit.get_StripLocalSegment().end().z();

                    totEn += strpEn;
                    weightedStrp += strpEn * thehit.get_StripLocalSegment().origin().y();
                    weightedStripEndPoint1X += strpEn * x1;
                    weightedStripEndPoint1Y += strpEn * y1;
                    weightedStripEndPoint1Z += strpEn * z1;
                    weightedStripEndPoint2X += strpEn * x2;
                    weightedStripEndPoint2Y += strpEn * y2;
                    weightedStripEndPoint2Z += strpEn * z2;

                    weightedLocStripEndPoint1X += strpEn * lx1;
                    weightedLocStripEndPoint1Y += strpEn * ly1;
                    weightedLocStripEndPoint1Z += strpEn * lz1;
                    weightedLocStripEndPoint2X += strpEn * lx2;
                    weightedLocStripEndPoint2Y += strpEn * ly2;
                    weightedLocStripEndPoint2Z += strpEn * lz2;

                    // getting the max and min strip number in the cluster
                    if (strpNb <= min) {
                        min = strpNb;
                    }
                    if (strpNb >= max) {
                        max = strpNb;
                    }

                    // getting the seed strip which is defined as the strip with the largest deposited energy
                    if (strpEn >= Emax) {
                        Emax = strpEn;
                        Time = strpTm;
                        seed = strpNb;
                    }
                }

                if (totEn == 0) {
                    System.err.println(" Cluster energy is zero .... exit");
                    return;
                }

                this.set_MinStrip(min);
                this.set_MaxStrip(max);
                this.set_SeedStrip(seed);
                this.set_SeedEnergy(Emax);

                // calculates the centroid values and associated positions
                stripNumCent = weightedStrp / totEn;
                weightedStripEndPoint1X = weightedStripEndPoint1X / totEn;
                weightedStripEndPoint1Y = weightedStripEndPoint1Y / totEn;
                weightedStripEndPoint1Z = weightedStripEndPoint1Z / totEn;
                weightedStripEndPoint2X = weightedStripEndPoint2X / totEn;
                weightedStripEndPoint2Y = weightedStripEndPoint2Y / totEn;
                weightedStripEndPoint2Z = weightedStripEndPoint2Z / totEn;
                weightedLocStripEndPoint1X = weightedLocStripEndPoint1X / totEn;
                weightedLocStripEndPoint1Y = weightedLocStripEndPoint1Y / totEn;
                weightedLocStripEndPoint1Z = weightedLocStripEndPoint1Z / totEn;
                weightedLocStripEndPoint2X = weightedLocStripEndPoint2X / totEn;
                weightedLocStripEndPoint2Y = weightedLocStripEndPoint2Y / totEn;
                weightedLocStripEndPoint2Z = weightedLocStripEndPoint2Z / totEn;
            }

            _TotalEnergy = totEn;
            _Centroid = stripNumCent;
            _CentroidError = Math.sqrt(this.size()) * Constants.FVT_SigmaS;
            _GlobalSegment = new Line3D(weightedStripEndPoint1X,weightedStripEndPoint1Y,weightedStripEndPoint1Z,
                                       weightedStripEndPoint2X,weightedStripEndPoint2Y,weightedStripEndPoint2Z);
            _LocalSegment = new Line3D(weightedLocStripEndPoint1X,weightedLocStripEndPoint1Y,weightedLocStripEndPoint1Z,
                                       weightedLocStripEndPoint2X,weightedLocStripEndPoint2Y,weightedLocStripEndPoint2Z);
            
        }


        public double distance(double x, double y, double z) {
            Point3D trkPoint = new Point3D(x, y, z);
            return _GlobalSegment.distance(trkPoint).length();
        }

        public double distance(Point3D point) {
            return _GlobalSegment.distance(point).length();
        }

        public Point3D calcCross(double x, double y, double z) {
            Point3D trkPoint = new Point3D(x, y, z);
            return _GlobalSegment.distance(trkPoint).origin();
        }

        public Point3D calcCross(Point3D point) {
            return _GlobalSegment.distance(point).origin();
        }

        @Override
        public int compareTo(Cluster arg) {
            // Sort by layer and seed strip
            int return_val = 0;
            int CompLay  = this.get_Layer()    < arg.get_Layer()     ? -1 : this.get_Layer()     == arg.get_Layer()     ? 0 : 1;
            int CompSeed = this.get_SeedStrip()< arg.get_SeedStrip() ? -1 : this.get_SeedStrip() == arg.get_SeedStrip() ? 0 : 1;

            return_val = ((CompLay == 0) ? CompSeed : CompLay);

            return return_val;
        }

        /**
         *
         * @return cluster info. about location and number of hits contained in it
         */
        public String toStringBrief() {
            String str = "FMT cluster: Index " + this.get_Index() 
                                  + ", Layer " + this.get_Layer()
                                  + ", Seed "  + this.get_SeedStrip()
                                  + ", Size "  + this.size()
                                  + ", LocY "  + String.format("%.4f", this.get_LocalSegment().origin().y());
            return str;
        }

        @Override
        public String toString() {
            String str = this.toStringBrief();
            for (FittedHit aThi : this) {
                str = str.concat("\n" + aThi.toString());
            }
            return str;
        }
}
