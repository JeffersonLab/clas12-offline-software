package org.jlab.rec.fmt.hit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.fmt.Constants;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * A hit characterized by layer, sector, wire number, and Edep. The ADC to time
 * conversion has been already done.
 *
 * @author ziegler
 * @author benkel
 * @author devita
*/
// Class implements Comparable interface to allow for sorting a collection of hits by wire numbers
public class Hit implements Comparable<Hit> {

        private int _Layer;    	  // layer [1,...6]
        private int _Strip;    	  // strip [1...1024]

        private double _Energy;      	  // Reconstructed time, for now it is the gemc time
        private double _Time;	  // Hit time
        private double _Error;	  // Hit time
        private Line3D _LocalSegment;  // The geometry segment representing the strip position in the local frame
        private Line3D _GlobalSegment; // The geometry segment representing the strip position in the global frame
        private int _Index;		  // Hit Id
        private int _ClusterIndex = -1;

        
        /**
         * @param index
         * @param layer
         * @param strip
         * @param energy
         * @param time
         */
        public Hit(int index, int layer, int strip, double energy, double time) {
            this._Index = index;
            this._Layer = layer;
            this._Strip = strip;
            this._Energy = energy;
            this._Time  = time;
            this._Error = Constants.getPitch()/Math.sqrt(12);
            
//            double x0 = Constants.FVT_stripsX[layer - 1][strip - 1][0];
//            double x1 = Constants.FVT_stripsX[layer - 1][strip - 1][1];
//            double y0 = Constants.FVT_stripsY[layer - 1][strip - 1][0];
//            double y1 = Constants.FVT_stripsY[layer - 1][strip - 1][1];
//            double z  = Geometry.getLayerZ(layer - 1);
            this._GlobalSegment = Constants.getStrip(layer, strip);
            this._LocalSegment  = Constants.getLocalStrip(layer, strip);
        }

        /**
         *
         * @return the layer (1...6)
         */
        public int getLayer() {
            return _Layer;
        }

        /**
         * Sets the layer
         *
         * @param _Layer
         */
        public void setLayer(int _Layer) {
            this._Layer = _Layer;
        }

        /**
         *
         * @return the strip number (1...256)
         */
        public int getStrip() {
            return _Strip;
        }

        /**
         * Sets the strip number
         *
         * @param _Strip
         */
        public void setStrip(int _Strip) {
            this._Strip = _Strip;
        }

        /**
         *
         * @return the Edep in MeV
         */
        public double getEnergy() {
            return _Energy;
        }

        /**
         * Sets the Edep
         *
         * @param _Edep
         */
        public void setEnergy(double _Edep) {
            this._Energy = _Edep;
        }

        public double getTime() {
            return _Time;
        }

        public void setTime(double _Time) {
            this._Time = _Time;
        }

        public double getError() {
            return _Error;
        }

        public void setError(double _Error) {
            this._Error = _Error;
        }

        /**
         *
         * @return the ID
         */
        public int getIndex() {
            return _Index;
        }

        /**
         * Sets the hit ID. The ID corresponds to the hit index in the EvIO column.
         *
         * @param _Id
         */
        public void setIndex(int _Id) {
            this._Index = _Id;
        }

        /**
         *
         * @return region (1...4)
         */
        public int getRegion() {
            return (int) (this._Layer + 1) / 2;
        }

        /**
         *
         * @return superlayer 1 or 2 in region (1...4)
         */
        public int getRegionSlayer() {
            return (this._Layer + 1) % 2 + 1;
        }

        
        public Line3D getStripGlobalSegment() {
            return _GlobalSegment;
        }

        
        public Line3D getStripLocalSegment() {
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
            int CompLay    = this.getLayer() < arg.getLayer() ? -1 : this.getLayer() == arg.getLayer() ? 0 : 1;
            int CompStrip  = this.getStrip() < arg.getStrip() ? -1 : this.getStrip() == arg.getStrip() ? 0 : 1;

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
            if (this.getEnergy() == otherHit.getEnergy()
                    && this.getLayer() == otherHit.getLayer()
                    && this.getStrip() == otherHit.getStrip()) {
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
            if (this.getLayer() == other.getLayer()) {
                return Constants.areClose(this.getLayer(),this.getStrip(),other.getStrip());
            }
            return false;
        }

        public int getClusterIndex() {
            return _ClusterIndex;
        }

        public void setClusterIndex(int _AssociatedClusterIndex) {
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
                    
                    Hit hit = new Hit(i, layer, strip, (double) ADC, time);
                    
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
            String s = " Hit: Index " + this.getIndex()   
                     + " Layer "      + this.getLayer()   + " Strip "  + this.getStrip()
                     + " Energy "     + this.getEnergy()  + " Time "   + this.getTime()
                     + " LocalY "     + String.format("%.4f",this.getStripLocalSegment().origin().y());
            return s;
        }

}
