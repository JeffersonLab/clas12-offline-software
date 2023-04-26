package org.jlab.rec.vtx;



import java.util.ArrayList;
import java.util.List;

import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

public class Vertex {
	
	public Vertex() {
		// TODO Auto-generated constructor stub
	}
	
	private Point3D _Vertex;                //position of the common vertex (cm)
        private Point3D _Track1POCA;            //position of the first track at the DOCA point 
        private Point3D _Track2POCA;            //position of the second track at the DOCA point 
        private Vector3D _Track1POCADir;        //direction of the first track at the DOCA point 
        private Vector3D _Track2POCADir;        //direction of the second track at the DOCA point 
        private double _Doca;                   //distance between two tracks at the DOCA point
        
        public Point3D get_Vertex() {
		return _Vertex;
	}
	public void set_Vertex(Point3D _Vertex) {
		this._Vertex = _Vertex;
	}

    /**
     * @return the _Track1POCA
     */
    public Point3D getTrack1POCA() {
        return _Track1POCA;
    }

    /**
     * @param _Track1POCA the _Track1POCA to set
     */
    public void setTrack1POCA(Point3D _Track1POCA) {
        this._Track1POCA = _Track1POCA;
    }

    /**
     * @return the _Track2POCA
     */
    public Point3D getTrack2POCA() {
        return _Track2POCA;
    }

    /**
     * @param _Track2POCA the _Track2POCA to set
     */
    public void setTrack2POCA(Point3D _Track2POCA) {
        this._Track2POCA = _Track2POCA;
    }

    /**
     * @return the _Track1POCADir
     */
    public Vector3D getTrack1POCADir() {
        return _Track1POCADir;
    }

    /**
     * @param _Track1POCADir the _Track1POCADir to set
     */
    public void setTrack1POCADir(Vector3D _Track1POCADir) {
        this._Track1POCADir = _Track1POCADir;
    }

    /**
     * @return the _Track2POCADir
     */
    public Vector3D getTrack2POCADir() {
        return _Track2POCADir;
    }

    /**
     * @param _Track2POCADir the _Track2POCADir to set
     */
    public void setTrack2POCADir(Vector3D _Track2POCADir) {
        this._Track2POCADir = _Track2POCADir;
    }

    /**
     * @return the _Doca
     */
    public double getDoca() {
        return _Doca;
    }

    /**
     * @param _Doca the _Doca to set
     */
    public void setDoca(double _Doca) {
        this._Doca = _Doca;
    }
        
      
        private List<TrackParsHelix> _HelixPair = new ArrayList<TrackParsHelix>();
	public List<TrackParsHelix> get_HelixPair() {
		return _HelixPair;
	}
	public void set_HelixPair(List<TrackParsHelix> _HelixPair) {
		this._HelixPair= _HelixPair;
	}
	
	
} // end class
