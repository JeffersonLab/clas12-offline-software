package org.jlab.service;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;

 public class DCWire implements Comparable {
    
    private DetectorDescriptor  desc = new DetectorDescriptor(DetectorType.DC);

    private int _Sector;      							//	   sector[1...6]
	private int _Superlayer;    	 					//	   superlayer [1,...6]
	private int _Layer;    	 							//	   layer [1,...6]
	private int _Wire;    	 							//	   wire [1...112]
	private int _Id; 									//     hit index
	
    private int                 iTDC  = 0;
    private double              iGEMCTime =0;
    private double[][][]		iResolutionsPars = new double[6][6][5];
    private Point3D 			iMidPoint = new Point3D();
    private double 				iCellSize = 0;             
    private Line3D              iWireLine = new Line3D();
    
    public DCWire(int sector, int superlayer, int layer, int component){
        this.desc.setSectorLayerComponent(sector, (superlayer-1)*6+layer, component);
        this._Sector = sector;
		this._Superlayer = superlayer;
		this._Layer = layer;
		this._Wire = component;
    }
  
    public int get_Id() {
		return _Id;
	}

	public void set_Id(int _Id) {
		this._Id = _Id;
	}

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

	public int get_Layer() {
		return _Layer;
	}

	public void set_Layer(int _Layer) {
		this._Layer = _Layer;
	}

	public int get_Wire() {
		return _Wire;
	}

	public void set_Wire(int _Wire) {
		this._Wire = _Wire;
	}

	public DCWire setTDC(int tdc){
        this.iTDC = tdc;
        return this;
    }
    
    
    
    public double getGEMCTime() {
		return iGEMCTime;
	}

	public void setGEMCTime(double iGEMCTime) {
		this.iGEMCTime = iGEMCTime;
	}

	public double getTime(){
		if(this.getGEMCTime()!=0)
			return this.getGEMCTime();
        return (double)this.iTDC;
    }
   
    public int getTDC(){
        return this.iTDC;
    }
     
    public Point3D getMidPoint() {
		return iMidPoint;
	}

	public void setMidPoint(Point3D iMidPoint) {
		this.iMidPoint = iMidPoint;
	}

	public Line3D getWireLine() {
		return iWireLine;
	}

	public void setWireLine(Line3D iWireLine) {
		this.iWireLine = iWireLine;
	}

	public double[][][] getResolutionsPars() {
		return iResolutionsPars;
	}

	public void setResolutionsPars(double[][][] iResolutionsPars) {
		this.iResolutionsPars = iResolutionsPars;
	}
	
	public double get_CellSize() {
		return iCellSize;
	}

	public void set_CellSize(double iCellSize) {
		this.iCellSize = iCellSize;
	}

	public boolean isNeighbour(DCWire wire){
        if(wire.getDescriptor().getSector()==this.desc.getSector()&&
                wire.getDescriptor().getLayer()==this.desc.getLayer()){
            if(Math.abs(wire.getDescriptor().getComponent()-this.desc.getComponent())<=1) return true;
        }
        return false;
    }
    
    public DetectorDescriptor  getDescriptor(){return this.desc;}
    
    public int compareTo(Object o) {
        DCWire ob = (DCWire) o;
        if(ob.getDescriptor().getSector() < this.desc.getSector()) return  1;
        if(ob.getDescriptor().getSector() > this.desc.getSector()) return -1;
        if(ob.getDescriptor().getLayer()  < this.desc.getLayer()) return   1;
        if(ob.getDescriptor().getLayer()  > this.desc.getLayer()) return  -1;
        if(ob.getDescriptor().getComponent() <  this.desc.getComponent()) return  1;
        if(ob.getDescriptor().getComponent() == this.desc.getComponent()) return  0;
        return -1;
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        //str.append(String.format("----> wire (%3d %3d %3d) TDC  %5d  ENERGY = %12.5f", 
        //        this.desc.getSector(),this.desc.getLayer(),this.desc.getComponent(),
               // this.iTDC,this.getEnergy()));
       // str.append(String.format("  GAIN (%5.3f) ATT (%12.5f %12.5f %12.5f)", 
               // this.iGain,this.iAttenLengthA,this.iAttenLengthB,this.iAttenLengthC));
        return str.toString();
    }

	

	



	
}
