package org.jlab.rec.ft.trk;

import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;

/**
 *
 * @author devita
 * @author filippi
 */
public class FTTRKHit implements Comparable<FTTRKHit>{
	// class implements Comparable interface to allow for sorting a collection of hits by wire number values
	
	private int _Sector;      					        //	   sector[1]
	private int _Layer;    	 					        //	   layer [1,..4]
	private int _Strip;    	 				                //	   strip [1...768]

	private double _Edep;      					        //	   Reconstructed time, for now it is the gemc time
	private Line3D _StripSegment;						//         The geometry segment representing the strip position
	private int    _Id;                                        		//	   Hit Id
	private double _Time;                                                   //	   Reconstructed time, for now it is the gemc time
	private int    _DGTZIndex;                                              //	   Pointer to raw hit
	private int    _ClusterIndex;                                           //	   Pointer to cluster
	private int    _CrossIndex;                                             //	   Pointer to cross
    
	// constructors 
	
	/**
	 * @param sector 
	 * @param layer 
	 * @param strip 
	 * @param Edep (for gemc output without digitization)
	 */
	public FTTRKHit(int sector, int layer, int strip, double Edep, double time, int id) {
                int debug = FTTRKReconstruction.debugMode;        // gets the debug flag from FTTRKReconstruction
		this._Sector = sector;
		this._Layer = layer;  
		this._Strip = strip;
		this._Edep = Edep;	
                this._Id = id;
                this._Time = time;
                // update associations after crosses are found
                this._DGTZIndex = -1;
                this._ClusterIndex = -1;
                this._CrossIndex = -1;
                
		double x0 = FTTRKConstantsLoader.stripsX[layer-1][strip-1][0];
		double x1 = FTTRKConstantsLoader.stripsX[layer-1][strip-1][1];		
		double y0 = FTTRKConstantsLoader.stripsY[layer-1][strip-1][0];
		double y1 = FTTRKConstantsLoader.stripsY[layer-1][strip-1][1];
		double Z  = FTTRKConstantsLoader.Zlayer[layer-1]; // z
        
		Line3D seg = new Line3D();
		seg.setOrigin(new Point3D(x0,y0,Z));
		seg.setEnd(new Point3D(x1,y1,Z));
		set_StripSegment(seg);
                if(debug>=1){
                    System.out.println("++++++++++++ strip origin, layer " + layer + " strip " + strip + " x " + x0 + " y0 " + y0 + " z0 " + Z);
                    System.out.println("++++++++++++ strip end, layer " + layer + " strip " + strip + " x1 " + x1 + " y1 " + y1);
                }
                /// for debugging purposes 
                int Slayer = this.get_HalfLayer(layer);   // half layer: top(1) or bottom(0)
                double x0loc = FTTRKConstantsLoader.stripsXloc[Slayer][strip-1][0];
                double x1loc = FTTRKConstantsLoader.stripsXloc[Slayer][strip-1][1];
                double y0loc = FTTRKConstantsLoader.stripsYloc[Slayer][strip-1][0];
                double y1loc = FTTRKConstantsLoader.stripsYloc[Slayer][strip-1][1];
                if(debug>=1){
                    System.out.println("~~~~~~~~~~~ strip origin local frame " + " x " + x0loc + " y " + y0loc + " layer " + layer + " Slayer " + Slayer);
                    System.out.println("~~~~~~~~~~~ strip endpoint local frame " + " x " + x1loc + " y " + y1loc + " layer " + layer + " Slayer " + Slayer);
                    System.out.println("");
                }
	}

  /**
	 * 
	 * @return the sector (1)
	 */
	public int get_Sector() {
		return _Sector;
	}

	/**
	 * Sets the sector 
	 * @param _Sector
	 */
	public void set_Sector(int _Sector) {
		this._Sector = _Sector;
	}
	

	/**
	 * 
	 * @return the layer (1-4)
	 */
	public int get_Layer() {
		return _Layer;
	}

	/**
	 * Sets the layer
	 * @param _Layer
	 */
	public void set_Layer(int _Layer) {
		this._Layer = _Layer;
	}
  
  /**
	 * 
	 * @return the superlayer: 0 for bottom modules 1+3, 1 for top modules 2+4
	 */
        public int get_HalfLayer(int _Layer){
		return (_Layer+1)%2;
	}
        
	/**
	 * 
	 * @return the wire number (1...768)
	 */
	public int get_Strip() {
		return _Strip;
	}

	/**
	 * Sets the wire number
	 * @param _Wire
	 */
	public void set_Strip(int _Strip) {
		this._Strip = _Strip;
	}

	/**
	 * 
	 * @return the Edep in MeV
	 */
	public double get_Edep() {
		return _Edep;
	}

	/**
	 * Sets the Edep
	 * @param _Edep
	 */
	public void set_Edep(double _Edep) {
		this._Edep = _Edep;
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
	public int get_Id() {
		return _Id;
	}

	/**
	 * Sets the hit ID.  The ID corresponds to the hit index in the EvIO column.
	 * @param _Id
	 */
	public void set_Id(int _Id) {
		this._Id = _Id;
	}

	/**
	 * 
	 * @return region (1-2) 
	 */
        
        public int get_Region() {
            return (int) (this._Layer+1)/2;
         }
        
        public Line3D get_StripSegment() {
		return _StripSegment;
	}

	public void set_StripSegment(Line3D _StripSegment) {
		this._StripSegment = _StripSegment;
	}

        public int get_DGTZIndex() {
            return _DGTZIndex;
        }

        public void set_DGTZIndex(int _DGTZIndex) {
//            this._DGTZIndex = _DGTZIndex;
           this._DGTZIndex = _Id;
        }

        public int get_ClusterIndex() {
            return _ClusterIndex;
        }

        public void set_ClusterIndex(int _ClusterIndex) {
            this._ClusterIndex = _ClusterIndex;
        }

        public int get_CrossIndex() {
            return _CrossIndex;
        }

        public void set_CrossIndex(int _CrossIndex) {
            this._CrossIndex = _CrossIndex;
        }

        public int get_StripNumberFromLocalY(double xGlobal, double yLocal, int layer){

        // input: xGlobal, yLocal --> must retrieve xLocal to determine strip number    
        double alpha = FTTRKConstantsLoader.Alpha[layer-1];
        
        double y = yLocal;
        double yg = (y+xGlobal*Math.sin(alpha))/Math.cos(alpha);
        double x = xGlobal*Math.cos(alpha)+yg*Math.sin(alpha);
        
        double r=Math.sqrt(x*x+y*y);
        if(r<=FTTRKConstantsLoader.Rmax && r>=FTTRKConstantsLoader.InnerHole && Math.abs(y)<=FTTRKConstantsLoader.Pitch*FTTRKConstantsLoader.Nstrips/3) {
            int strip = (int) Math.floor(y/FTTRKConstantsLoader.Pitch) + 1 + FTTRKConstantsLoader.Nstrips/3;
            if(strip>FTTRKConstantsLoader.Nstrips/2) { // strip in the top sector
                strip += FTTRKConstantsLoader.Nstrips/3;
            }else if(strip>FTTRKConstantsLoader.Nstrips/6 && x>0){
                strip += FTTRKConstantsLoader.Nstrips/3;
            }   
                return strip;
            }else{
                return -1;
            }
        }
        
        public int get_StripNumberFromGlobal(double xGlobal, double yGlobal, int layer){

        double alpha = FTTRKConstantsLoader.Alpha[layer-1];
        double x = xGlobal*Math.cos(alpha)+yGlobal*Math.sin(alpha);
        double y = yGlobal*Math.cos(alpha)-xGlobal*Math.sin(alpha);
        
        double r=Math.sqrt(x*x+y*y);
        if(r<=FTTRKConstantsLoader.Rmax && r>=FTTRKConstantsLoader.InnerHole && Math.abs(y)<=FTTRKConstantsLoader.Pitch*FTTRKConstantsLoader.Nstrips/3) {
            int strip = (int) Math.floor(y/FTTRKConstantsLoader.Pitch) + 1 + FTTRKConstantsLoader.Nstrips/3;
            if(strip>FTTRKConstantsLoader.Nstrips/2) { // strip in the top sector
                strip += FTTRKConstantsLoader.Nstrips/3;
            }else if(strip>FTTRKConstantsLoader.Nstrips/6 && x>0){
                strip += FTTRKConstantsLoader.Nstrips/3;
            }   
                return strip;
            }else{
                return -1;
            }
        }
        
        
        public int compareLayerTo(FTTRKHit arg0) {
            if(this._Layer > arg0._Layer) {
		return 1;
            } else {
		return 0;
            }
	}
        
        /**
	 * 
	 * @param otherHit
	 * @return a boolean comparing 2 hits based on basic descriptors; 
	 * returns true if the hits are the same
	 */
	public boolean isSameAs(FTTRKHit otherHit){
		boolean cmp = false;
		if(this._Edep == otherHit._Edep && 
                   this._Sector == otherHit._Sector &&
                   this._Layer == otherHit._Layer && 
                   this._Strip == otherHit._Strip )
                    cmp = true;
		return cmp;
	}
        
        /**
	 * 
	 * @param arg0 the other hit
	 * @return an int used to sort a collection of hits by wire number. Sorting by wire is used in clustering.
	 */
	@Override
	public int compareTo(FTTRKHit arg0) {
		if(this._Strip>arg0._Strip) {
			return 1;
		} else {
			return 0;
		}
	}
	/**
	 * 
	 * @return print statement with hit information
	 */
	public String printInfo() {
		String s = " Hit: ID "+this.get_Id()+" Sector "+this.get_Sector()+" Layer "+this.get_Layer()+" Strip "+this.get_Strip()+" Edep "+this.get_Edep();
		return s;
	}
 	
}
