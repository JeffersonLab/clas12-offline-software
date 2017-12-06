/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.fvt.track.trajectory;

/**
 *
 * @author ziegler
 */
public class Surface {
    
        private int _DetectorIndex;
        public int getDetectorIndex() {
            return _DetectorIndex;
        }
        public void setDetectorIndex(int _DetectorIndex) {
            this._DetectorIndex = _DetectorIndex;
        }
        private int _DetectorLayer;
        public int getDetectorLayer() {
            return _DetectorLayer;
        }
        public void setDetectorLayer(int _DetectorLayer) {
            this._DetectorLayer = _DetectorLayer;
        }
        
        private String _DetectorName;

        public String getDetectorName() {
            return _DetectorName;
        }

        public void setDetectorName(String _DetectorName) {
            this._DetectorName = _DetectorName;
        }
        
        public Surface (String name, int id, int layer, double d, double nx, double ny, double nz) {
            _DetectorName = name;
            _DetectorIndex = id;
            _DetectorLayer = layer;
            _d = d;
            _nx = nx;
            _ny = ny;
            _nz = nz;
        }
        private double _d;
        private double _nx;
        private double _ny;
        private double _nz;
        
        public double get_d() {
            return _d;
        }

        public void set_d(double _d) {
            this._d = _d;
        }

        public double get_nx() {
            return _nx;
        }

        public void set_nx(double _nx) {
            this._nx = _nx;
        }

        public double get_ny() {
            return _ny;
        }

        public void set_ny(double _ny) {
            this._ny = _ny;
        }

        public double get_nz() {
            return _nz;
        }

        public void set_nz(double _nz) {
            this._nz = _nz;
        }
        
        
}
