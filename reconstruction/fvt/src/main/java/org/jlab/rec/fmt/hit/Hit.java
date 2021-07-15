package org.jlab.rec.fmt.hit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.fmt.Constants;
import org.jlab.rec.fmt.Geometry;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * A hit characterized by layer, sector, wire number, and Edep. The ADC to time
 * conversion has been already done.
 *
 * @author ziegler
 */
// Class implements Comparable interface to allow for sorting a collection of hits by wire numbers
public class Hit implements Comparable<Hit> {

        private int _Sector;      	  // unused
        private int _Layer;    	  // layer [1,...6]
        private int _Strip;    	  // strip [1...1024]

        private double _Energy;      	  // Reconstructed time, for now it is the gemc time
        private double _Time;	  // Hit time
        private Line3D _LocalSegment;  // The geometry point representing the strip position in the local frame
        private Line3D _GlobalSegment; // The geometry segment representing the strip position in the global frame
        private int _Index;		  // Hit Id
        private int _ClusterIndex = -1;

        
        /**
         * @param sector
         * @param layer
         * @param strip
         * @param Energy (for gemc output without digitization)
         */
        public Hit(int index, int sector, int layer, int strip, double energy, double time) {
            this._Index = index;
            this._Sector = sector;
            this._Layer = layer;
            this._Strip = strip;
            this._Energy = energy;
            this._Time = time;

            double x0 = Constants.FVT_stripsX[layer - 1][strip - 1][0];
            double x1 = Constants.FVT_stripsX[layer - 1][strip - 1][1];
            double y0 = Constants.FVT_stripsY[layer - 1][strip - 1][0];
            double y1 = Constants.FVT_stripsY[layer - 1][strip - 1][1];
            double z  = Geometry.getLayerZ(layer - 1);
            this._GlobalSegment = new Line3D(x0, y0, z, x1, y1, z);
            this._LocalSegment = new Line3D(Constants.FVT_stripsXloc[strip-1][0], Constants.FVT_stripsYloc[strip-1][0], 0,
                                            Constants.FVT_stripsXloc[strip-1][1], Constants.FVT_stripsYloc[strip-1][1], 0);
        }

        /**
         *
         * @return the sector (1...24)
         */
        public int get_Sector() {
            return _Sector;
        }

        /**
         * Sets the sector
         *
         * @param _Sector
         */
        public void set_Sector(int _Sector) {
            this._Sector = _Sector;
        }

        /**
         *
         * @return the layer (1...6)
         */
        public int get_Layer() {
            return _Layer;
        }

        /**
         * Sets the layer
         *
         * @param _Layer
         */
        public void set_Layer(int _Layer) {
            this._Layer = _Layer;
        }

        /**
         *
         * @return the strip number (1...256)
         */
        public int get_Strip() {
            return _Strip;
        }

        /**
         * Sets the strip number
         *
         * @param _Strip
         */
        public void set_Strip(int _Strip) {
            this._Strip = _Strip;
        }

        /**
         *
         * @return the Edep in MeV
         */
        public double get_Energy() {
            return _Energy;
        }

        /**
         * Sets the Edep
         *
         * @param _Edep
         */
        public void set_Energy(double _Edep) {
            this._Energy = _Edep;
        }

        public double get_Time() {
            return _Time;
        }

        public void set_Time(double _Time) {
            this._Time = _Time;
        }

        /**
         *
         * @return the ID
         */
        public int get_Index() {
            return _Index;
        }

        /**
         * Sets the hit ID. The ID corresponds to the hit index in the EvIO column.
         *
         * @param _Id
         */
        public void set_Index(int _Id) {
            this._Index = _Id;
        }

        /**
         *
         * @return region (1...4)
         */
        public int get_Region() {
            return (int) (this._Layer + 1) / 2;
        }

        /**
         *
         * @return superlayer 1 or 2 in region (1...4)
         */
        public int get_RegionSlayer() {
            return (this._Layer + 1) % 2 + 1;
        }

        
        public Line3D get_StripGlobalSegment() {
            return _GlobalSegment;
        }

        
        public Line3D get_StripLocalSegment() {
            return _LocalSegment;
        }

        
        public double getDoca(double x, double y, double z) {
            Point3D trkPoint = new Point3D(x, y, z);
            return _GlobalSegment.distance(trkPoint).length();
        }

        /**
         *
         * @param arg the other hit
         * @return an int used to sort a collection of hits by wire number. Sorting
         * by wire is used in clustering.
         */
        @Override
        public int compareTo(Hit arg) {
            // Sort by layer and seed strip
            int return_val = 0;
            int CompLay    = this.get_Layer() < arg.get_Layer() ? -1 : this.get_Layer() == arg.get_Layer() ? 0 : 1;
            int CompStrip  = this.get_Strip() < arg.get_Strip() ? -1 : this.get_Strip() == arg.get_Strip() ? 0 : 1;

            return_val = ((CompLay == 0) ? CompStrip : CompLay);

            return return_val;
        }

        /**
         *
         * @param otherHit
         * @return a boolean comparing 2 hits based on basic descriptors; returns
         * true if the hits are the same
         */
        public boolean equal(Hit otherHit) {
            if (this.get_Energy() == otherHit.get_Energy()
                    && this.get_Sector() == otherHit.get_Sector()
                    && this.get_Layer() == otherHit.get_Layer()
                    && this.get_Strip() == otherHit.get_Strip()) {
                return true;
            }
            return false;
        }

        /**
         *
         * @param other
         * @return a boolean comparing 2 hits based on basic descriptors; returns
         * true if the hits are the same
         */
        public boolean isClose(Hit other) {
            if (this.get_Sector() == other.get_Sector() && this.get_Layer() == other.get_Layer()) {
                Point3D p0 = this.get_StripLocalSegment().midpoint();
                Point3D p1 = other.get_StripLocalSegment().midpoint();
                if(Math.round(Math.abs(p0.y()-p1.y())/Constants.FVT_Pitch)==1 && 
                   Math.abs(Math.signum(p0.x())-Math.signum(p1.x()))<=1)
                    return true;
            }
            return false;
        }

        public int get_ClusterIndex() {
            return _ClusterIndex;
        }

        public void set_ClusterIndex(int _AssociatedClusterIndex) {
            this._ClusterIndex = _AssociatedClusterIndex;
        }

        public static List<Hit> fetchHits(DataEvent event, IndexedTable statuses) {

            List<Hit> hits = new ArrayList<Hit>();

            if (event.hasBank("FMT::adc")) {
                DataBank bankDGTZ = event.getBank("FMT::adc");
                int rows = bankDGTZ.rows();;
                for (int i = 0; i < rows; i++) {
                    int sector  = bankDGTZ.getByte("sector", i);
                    int layer   = bankDGTZ.getByte("layer", i);
                    int strip   = bankDGTZ.getShort("component", i);
                    int ADC     = bankDGTZ.getInt("ADC", i);
                    double time = bankDGTZ.getFloat("time", i);

                    if (strip == -1 || ADC == 0) continue;

                    Hit hit = new Hit(i, sector, layer, strip, (double) ADC, time);
                    
                    int status = statuses.getIntValue("status", sector, layer, strip);
                    if(status==0) hits.add(hit);
                }
            }
            Collections.sort(hits);

            return hits;
        }
        /**
         *
         * @return print statement with hit information
         */
        @Override
        public String toString() {
            String s = " Hit: Index " + this.get_Index()   + " Sector " + this.get_Sector()
                     + " Layer "      + this.get_Layer()   + " Strip "  + this.get_Strip()
                     + " Energy "     + this.get_Energy()  + " Time "   + this.get_Time()
                     + " LocalY "     + String.format("%.4f",this.get_StripLocalSegment().origin().y());
            return s;
        }

}
