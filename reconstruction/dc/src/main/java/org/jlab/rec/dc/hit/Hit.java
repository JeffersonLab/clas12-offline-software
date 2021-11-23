package org.jlab.rec.dc.hit;

import org.jlab.detector.geant4.v2.DCGeant4Factory;


/**
 * A DC hit characterized by superlayer, layer, sector, wire number, and time.
 * The TDC to time conversion has been done.
 *
 * @author ziegler
 *
 */
public class Hit implements Comparable<Hit> {

    private int _Sector;      							//	   sector[1...6]
    private int _Superlayer;    	 					//	   superlayer [1,...6]
    private int _Layer;    	 						//	   layer [1,...6]
    private int _Wire;    	 						//	   wire [1...112]
    private int _TDC;
    private int _Id;
    private double _cellSize;
    private double _DocaErr;
    // class implements Comparable interface to allow for sorting a collection of hits by wire number values
    public int NNTrkId;
    public int NNClusId;
    public double NNTrkP;
    public double NNTrkTheta;
    public double NNTrkPhi;
    
    // constructors
    /**
     *
     * @param sector (1...6)
     * @param superlayer (1...6)
     * @param layer (1...6)
     * @param wire (1...112)
     * @param TDC TDC
     * @param Id
     */
    public Hit(int sector, int superlayer, int layer, int wire, int TDC, int Id) {
        this._Sector = sector;
        this._Superlayer = superlayer;
        this._Layer = layer;
        this._Wire = wire;
        this._TDC = TDC;
        this._Id = Id;

    }
    

    /**
     *
     * @return the sector (1...6)
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
     * @return the superlayer (1...6)
     */
    public int get_Superlayer() {
        return _Superlayer;
    }

    /**
     * Sets the superlayer
     *
     * @param _Superlayer
     */
    public void set_Superlayer(int _Superlayer) {
        this._Superlayer = _Superlayer;
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
     * @return the wire number (1...112)
     */
    public int get_Wire() {
        return _Wire;
    }

    /**
     * Sets the wire number
     *
     * @param _Wire
     */
    public void set_Wire(int _Wire) {
        this._Wire = _Wire;
    }

    /**
     *
     * @return the ID
     */
    public int get_TDC() {
        return _TDC;
    }

    /**
     * Sets the hit ID. The ID corresponds to the hit index in the EvIO column.
     *
     * @param TDC
     */
    public void set_TDC(int TDC) {
        this._TDC = TDC;
    }
    
    /**
     *
     * @return the ID
     */
    public int get_Id() {
        return _Id;
    }

    /**
     * Sets the hit ID. The ID corresponds to the hit index in the EvIO column.
     *
     * @param _Id
     */
    public void set_Id(int _Id) {
        this._Id = _Id;
    }

    /**
     *
     * @return region (1...3)
     */
    public int get_Region() {
        return (this._Superlayer + 1) / 2;
    }

    /**
     *
     * @return superlayer 1 or 2 in region (1...3)
     */
    public int get_RegionSlayer() {
        return (this._Superlayer + 1) % 2 + 1;
    }

    /**
     *
     * @param arg
     * @return an int used to sort a collection of hits by wire number. Sorting
     * by wire is used in clustering.
     */
    @Override
    public int compareTo(Hit arg) {
        int return_val = 0;
        int CompSec = this.get_Sector() < arg.get_Sector() ? -1 : this.get_Sector() == arg.get_Sector() ? 0 : 1;
        int CompSly = this.get_Superlayer() < arg.get_Superlayer() ? -1 : this.get_Superlayer() == arg.get_Superlayer() ? 0 : 1;
        int CompLay = this.get_Layer() < arg.get_Layer() ? -1 : this.get_Layer() == arg.get_Layer() ? 0 : 1;
        int CompWir = this.get_Wire() < arg.get_Wire() ? -1 : this.get_Wire() == arg.get_Wire() ? 0 : 1;

        int return_val1 = ((CompLay == 0) ? CompWir : CompLay);
        int return_val2 = ((CompSly == 0) ? return_val1 : CompSly);
        return_val = ((CompSec == 0) ? return_val2 : CompSec);

        return return_val;

    }

    /**
     *
     * @param layer layer number from 1 to 6
     * @param wire wire number from 1 to 112 calculates the center of the cell
     * as a function of wire number in the local superlayer coordinate system.
     * @return y
     */
    public double calcLocY(int layer, int wire) {

        // in old mc, layer 1 is closer to the beam than layer 2, in hardware it is the opposite
        //double  brickwallPattern = GeometryLoader.dcDetector.getSector(0).getSuperlayer(0).getLayer(1).getComponent(1).getMidpoint().x()
        //		- GeometryLoader.dcDetector.getSector(0).getSuperlayer(0).getLayer(0).getComponent(1).getMidpoint().x();
        //double brickwallPattern = GeometryLoader.getDcDetector().getWireMidpoint(0, 1, 1).x
        //        - GeometryLoader.getDcDetector().getWireMidpoint(0, 0, 1).x;

        //double brickwallSign = Math.signum(brickwallPattern);
        double brickwallSign = -1;

        //center of the cell asfcn wire num
        //double y= (double)wire*(1.+0.25*Math.sin(Math.PI/3.)/(1.+Math.sin(Math.PI/6.)));
        double y = (double) wire * 2 * Math.tan(Math.PI / 6.);
        if (layer % 2 == 1) {
            //y = y-brickwallSign*Math.sin(Math.PI/3.)/(1.+Math.sin(Math.PI/6.));
            y -= brickwallSign * Math.tan(Math.PI / 6.);
        }
        return y;

    }

    
    
    /**
     *
     * @return the cell size in a given superlayer
     */
    public double get_CellSize() {       
        return _cellSize;
    }
    
    public void set_CellSize(double cellSize) {
        _cellSize = cellSize;
    }
    
    public void calc_CellSize(DCGeant4Factory DcDetector) {
        // fix cell size = w_{i+1} -w_{i}
        //double layerDiffAtMPln  = GeometryLoader.dcDetector.getSector(0).getSuperlayer(this.get_Superlayer()-1).getLayer(0).getComponent(0).getMidpoint().x()
        //             - GeometryLoader.dcDetector.getSector(0).getSuperlayer(this.get_Superlayer()-1).getLayer(0).getComponent(1).getMidpoint().x();
        double layerDiffAtMPln = DcDetector.getWireMidpoint(this.get_Sector() - 1, this.get_Superlayer() - 1, 0, 0).x
                - DcDetector.getWireMidpoint(this.get_Sector() - 1, this.get_Superlayer() - 1, 0, 1).x;

        //double cellSize = 0.5*Math.cos(Math.toRadians(6.)*Math.abs(layerDiffAtMPln*Math.cos(Math.toRadians(6.)));
        _cellSize = 0.5 * Math.abs(layerDiffAtMPln);

    }
    
    /**
     *
     * @return error on the time in ns (4ns time window used by default in
     * reconstructing simulated data)
     */
    public double get_DocaErr() {
        return _DocaErr;
    }

    /**
     * Sets the doca uncertainty
     *
     * @param _docaErr
     */
    public void set_DocaErr(double _docaErr) {
        this._DocaErr = _docaErr;
    }

    /**
     *
     * @return print statement with hit information
     */
    public String printInfo() {
        String s = "DC Hit: ID " + this.get_Id() + " Sector " + this.get_Sector() + " Superlayer " + this.get_Superlayer() + " Layer " + this.get_Layer() + " Wire " + this.get_Wire() + " TDC " + this.get_TDC();
        return s;
    }

}
