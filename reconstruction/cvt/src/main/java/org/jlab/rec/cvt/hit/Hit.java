package org.jlab.rec.cvt.hit;

/**
 * A hit characterized by layer, sector, wire number, and Edep. The ADC to time
 * conversion has been done.
 *
 * @author ziegler
 *
 */
public class Hit implements Comparable<Hit> {
    // class implements Comparable interface to allow for sorting a collection of hits by wire number values

    // constructor
    public Hit(int detector, int detectortype, int sector, int layer, Strip strip) {
        this._Detector = detector;                                              // 0 = SVT, 1 = BMT
        this._DetectorType = detectortype;                                      // 0 = C, 1 = Z
        this._Sector = sector;
        this._Layer = layer;
        this._Strip = strip;

    }

    public int get_Detector() {
        return _Detector;
    }

    public void set_Detector(int _detector) {
        this._Detector = _detector;
    }

    public int get_DetectorType() {
        return _DetectorType;
    }

    public void set_DetectorType(int _DetectorType) {
        this._DetectorType = _DetectorType;
    }

    private int _Detector;							//       the detector SVT or BMT
    private int _DetectorType;                                                  //       for the BMT, either C or Z

    private int _Sector;      							//	   sector[1...24] for SVT, [1..3] for BMT
    private int _Layer;    	 						//	   layer [1,...]
    private Strip _Strip;    	 						//	   Strip object

    private int _Id;								//		Hit Id
    private int _Status; 							//      Status -1 dead, 0 noisy, 1 good

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
     * @return the layer (1...8)
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

    public Strip get_Strip() {
        return _Strip;
    }

    public void set_Strip(Strip _Strip) {
        this._Strip = _Strip;
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

    /**
     *
     * @param arg0 the other hit
     * @return an int used to sort a collection of hits by wire number. Sorting
     * by wire is used in clustering.
     */
    @Override
    public int compareTo(Hit arg0) {
        if (this._Strip.get_Strip() > arg0._Strip.get_Strip()) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     *
     * @return print statement with hit information
     */
    public void printInfo() {
        String s = " Hit: Detector " + this.get_Detector() + "ID " + this.get_Id() + " Sector " + this.get_Sector() + " Layer " + this.get_Layer() + " Strip " + this.get_Strip().get_Strip() + " Edep " + this.get_Strip().get_Edep();
        System.out.println(s);
    }

    /**
     *
     * @param otherHit
     * @return a boolean comparing 2 hits based on basic descriptors; returns
     * true if the hits are the same
     */
    public boolean isSameAs(FittedHit otherHit) {
        FittedHit thisHit = (FittedHit) this;
        boolean cmp = false;
        if ((thisHit.get_Detector()==otherHit.get_Detector())
                && thisHit.get_Sector() == otherHit.get_Sector()
                && thisHit.get_Layer() == otherHit.get_Layer()
                && thisHit.get_Strip().get_Strip() == otherHit.get_Strip().get_Strip()
                && thisHit.get_Strip().get_Edep() == otherHit.get_Strip().get_Edep()) {
            cmp = true;
        }
        return cmp;
    }

    public int get_Status() {
        return _Status;
    }

    public void set_Status(int _Status) {
        this._Status = _Status;
    }

}
