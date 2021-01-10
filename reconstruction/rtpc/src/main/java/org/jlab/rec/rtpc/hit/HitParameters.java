package org.jlab.rec.rtpc.hit;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.utils.groups.IndexedTable;

public class HitParameters {

    final private int _SignalStepSize = 10;
    final private int _BinSize = 40; 
    final private int _NBinKept = 3; 
    private int _TrigWindSize = 9600;
    private int _eventnum = 0; 
    private ADCMap _ADCMap = new ADCMap();
    private HashMap<Integer, List<Double>> _TimeMap = new HashMap<>();
    private HashMap<Integer, List<HitVector>> _FinalTIDMap = new HashMap<>();
    private List<Integer> _PadN = new ArrayList<>();  // used to read only cell with signal, one entry for each hit         
    private List<Integer> _PadList = new ArrayList<>();// used to read only cell with signal, one entry for each cell
    private List<Integer> _Pad = new ArrayList<>();
    private List<Double> _ADC = new ArrayList<>();
    private List<Double> _Time_o = new ArrayList<>();
    private List<Double> _weightave = new ArrayList<>();
    private List<Double> _maxinte = new ArrayList<>();
    private List<Double> _time = new ArrayList<>();
    private List<Double> _XVec = new ArrayList<>();
    private List<Double> _YVec = new ArrayList<>();
    private List<Double> _ZVec = new ArrayList<>();
    private HashMap<Integer, HashMap<Integer,List<Integer>>> _TIDMap = new HashMap<>();
    private HashMap<Integer, HashMap<Integer,List<Integer>>> _strkTIDMap = new HashMap<>();
    private HashMap<Integer, List<HitVector>> _alltracks = new HashMap<>();
    private HashMap<Integer, Double> _largetmap = new HashMap<>();
    private HashMap<Integer, List<RecoHitVector>> _recotrackmap = new HashMap<>();
    private HashMap<Integer, PadVector> _padmap = new HashMap<>();
    private TrackMap _trackmap = new TrackMap();
    private ReducedTrackMap _rtrackmap = new ReducedTrackMap();
    private HashMap<Integer, FinalTrackInfo> _finaltrackinfomap;
    private int _timeadjlimit = 4;
    private double _zthreshTF = 16;
    private double _phithreshTF = 0.16;
    private double _zthreshTFgap = 20;
    private double _phithreshTFgap = 0.20;
    private double _zthreshTD = 8;
    private double _phithreshTD = 0.1;
    private double _zthreshTDgap = 10;
    private double _phithreshTDgap = 0.12;
    private double _TFtotaltracktimeflag = 5000;
    private double _TFtotalpadtimeflag = 1000;
    private int _tthreshTD = 300;
    private int _tthreshTDgap = 300;
    private double _adcthresh = 320;
    private int _minhitspertrack = 5;
    private int _minhitspertrackreco = 10;
    private double[] _atparms = new double[5];
    private double[] _btparms = new double[5];
    private double[] _ctparms = new double[5];
    private double[] _aphiparms = new double[5];
    private double[] _bphiparms = new double[5];
    private double[] _cphiparms = new double[5];
    private double _tl = 0;
    private double _tp = 0;
    private double _tr = 0;
    private double _tcathode = 0;
    private double _tshiftfactorshort = 0;
    private double _tshiftfactorlong = 0;
    private double _chi2termthreshold = 20;
    private double _chi2percthreshold = 50;

    public void init(ConstantsManager manager, int runNo){
        IndexedTable time_offsets = manager.getConstants(runNo, "/calibration/rtpc/time_offsets");
        IndexedTable time_parms = manager.getConstants(runNo, "/calibration/rtpc/time_parms");
        IndexedTable recon_parms = manager.getConstants(runNo, "/calibration/rtpc/recon_parms");
        
        _TrigWindSize = (int) recon_parms.getDoubleValue("Dtm", 1,1,3);
        _chi2termthreshold = recon_parms.getDoubleValue("Dzm", 1,1,3);
        _chi2percthreshold = recon_parms.getDoubleValue("Dphim",1,1,3);
        _timeadjlimit = (int) recon_parms.getDoubleValue("Dtm", 1,1,1);
        _zthreshTF = recon_parms.getDoubleValue("Dzm", 1,1,1);
        _phithreshTF = recon_parms.getDoubleValue("Dphim", 1,1,1);
        _zthreshTFgap = recon_parms.getDoubleValue("Dzm", 1,1,4);
        _phithreshTFgap = recon_parms.getDoubleValue("Dphim", 1,1,4);
        _adcthresh = recon_parms.getDoubleValue("ADCmin", 1,1,1);
        _minhitspertrack = (int) recon_parms.getDoubleValue("Hitmin",1,1,1);
        _minhitspertrackreco = (int) recon_parms.getDoubleValue("Hitmin",1,1,2);
        _zthreshTD = recon_parms.getDoubleValue("Dzm", 1,1,2);
        _phithreshTD = recon_parms.getDoubleValue("Dphim", 1,1,2);
        _zthreshTDgap = recon_parms.getDoubleValue("Dzm", 1,1,5);
        _phithreshTDgap = recon_parms.getDoubleValue("Dphim", 1,1,5);
        _TFtotaltracktimeflag = recon_parms.getDoubleValue("Dtm", 1,1,6);
        _TFtotalpadtimeflag = recon_parms.getDoubleValue("Dtm", 1,1,7);
        _tthreshTD = (int) recon_parms.getDoubleValue("Dtm",1,1,2);
        _tthreshTDgap = (int) recon_parms.getDoubleValue("Dtm",1,1,5);
        for(int i = 0; i < 5; i++){
            _atparms[i] = time_parms.getDoubleValue("z"+i, 1,1,1);
            _btparms[i] = time_parms.getDoubleValue("z"+i, 1,1,2);
            _ctparms[i] = time_parms.getDoubleValue("z"+i, 1,1,3);
            _aphiparms[i] = time_parms.getDoubleValue("z"+i, 1,1,4);
            _bphiparms[i] = time_parms.getDoubleValue("z"+i, 1,1,5);
            _cphiparms[i] = time_parms.getDoubleValue("z"+i, 1,1,6);
        }
        _tl = time_offsets.getDoubleValue("tl", 1,1,3);
        _tp = time_offsets.getDoubleValue("tp", 1,1,3);
        _tr = time_offsets.getDoubleValue("tr", 1,1,3);
        _tcathode = time_parms.getDoubleValue("z0", 1,1,7);
        _tshiftfactorshort = time_parms.getDoubleValue("z1",1,1,7);
        _tshiftfactorlong = time_parms.getDoubleValue("z2",1,1,7);
    }
    
    public int get_SignalStepSize(){return _SignalStepSize;} // step size of the signal before integration (arbitrary value)
    public int get_BinSize(){return _BinSize;} // electronics integrates the signal over 40 ns
    public int get_NBinKept(){return _NBinKept;} // only 1 bin over 3 is kept by the daq
    public int get_TrigWindSize(){return _TrigWindSize;} // Trigger window should be 10 micro
    public ADCMap get_ADCMap(){return _ADCMap;}
    public HashMap<Integer, List<Double>> get_TimeMap(){return _TimeMap;}
    public List<Integer> get_PadN(){return _PadN;}
    public List<Integer> get_PadList(){return _PadList;}
    public List<Integer> get_Pad(){return _Pad;}
    public List<Double> get_ADC(){return _ADC;}
    public List<Double> get_Time_o(){return _Time_o;}
    public int get_eventnum(){return _eventnum;}
    public List<Double> get_weightave() {return _weightave;}
    public List<Double> get_maxinte() {return _maxinte;}
    public List<Double> get_time() {return _time;}
    public List<Double> get_XVec() {return _XVec;}
    public List<Double> get_YVec() {return _YVec;}
    public List<Double> get_ZVec() {return _ZVec;}
    public HashMap<Integer, HashMap<Integer,List<Integer>>> get_TIDMap() {return _TIDMap;}
    public HashMap<Integer, HashMap<Integer,List<Integer>>> get_strkTIDMap() {return _strkTIDMap;}
    public HashMap<Integer, List<HitVector>> get_FinalTIDMap() {return _FinalTIDMap;}
    public HashMap<Integer, List<HitVector>> get_alltracks() {return _alltracks;}
    public HashMap<Integer, Double> get_largetmap() {return _largetmap;}
    public HashMap<Integer, List<RecoHitVector>> get_recotrackmap() {return _recotrackmap;}
    public TrackMap get_trackmap() {return _trackmap;}
    public ReducedTrackMap get_rtrackmap() {return _rtrackmap;}
    public HashMap<Integer, FinalTrackInfo> get_finaltrackinfomap() {return _finaltrackinfomap;}
    public PadVector get_padvector(int pad) {
        if(!_padmap.containsKey(pad)) {
            _padmap.put(pad, new PadVector(pad));
        }
        return _padmap.get(pad);
    }
    public int get_timeadjlimit(){return _timeadjlimit;}
    public double get_zthreshTF(){return _zthreshTF;}
    public double get_phithreshTF(){return _phithreshTF;}
    public double get_zthreshTD(){return _zthreshTD;}
    public double get_phithreshTD(){return _phithreshTD;}
    public double get_zthreshTFgap(){return _zthreshTFgap;}
    public double get_phithreshTFgap(){return _phithreshTFgap;}
    public double get_zthreshTDgap(){return _zthreshTDgap;}
    public double get_phithreshTDgap(){return _phithreshTDgap;}
    public int get_tthreshTD(){return _tthreshTD;}
    public int get_tthreshTDgap(){return _tthreshTDgap;}
    public double get_adcthresh(){return _adcthresh;}
    public int get_minhitspertrack(){return _minhitspertrack;}
    public int get_minhitspertrackreco(){return _minhitspertrackreco;}
    public double[] get_atparms(){return _atparms;}
    public double[] get_btparms(){return _btparms;}
    public double[] get_aphiparms(){return _aphiparms;}
    public double[] get_bphiparms(){return _bphiparms;}
    public double[] get_ctparms(){return _ctparms;}
    public double[] get_cphiparms(){return _cphiparms;}
    public double get_tl(){return _tl;}
    public double get_tp(){return _tp;}
    public double get_tr(){return _tr;}
    public double get_tcathode(){return _tcathode;}
    public double get_tshiftfactorshort(){return _tshiftfactorshort;}
    public double get_tshiftfactorlong(){return _tshiftfactorlong;}
    public double get_TFtotaltracktimeflag(){return _TFtotaltracktimeflag;}
    public double get_TFtotalpadtimeflag(){return _TFtotalpadtimeflag;}
    public double get_chi2termthreshold(){return _chi2termthreshold;}
    public double get_chi2percthreshold(){return _chi2percthreshold;}

    public void set_ADCMap(ADCMap _ADCMap){this._ADCMap = _ADCMap;}
    public void set_TimeMap(HashMap<Integer, List<Double>> _TimeMap){this._TimeMap = _TimeMap;}
    public void set_PadN(List<Integer> _PadN){this._PadN = _PadN;}
    public void set_PadList(List<Integer> _PadList){this._PadList = _PadList;}
    public void set_Pad(List<Integer> _Pad){this._Pad = _Pad;}
    public void set_ADC(List<Double> _ADC){this._ADC = _ADC;}
    public void set_Time_o(List<Double> _Time_o){this._Time_o = _Time_o;}
    public void set_eventnum(int _eventnum){this._eventnum = _eventnum;}
    public void set_weightave(List<Double> _weightave) {this._weightave = _weightave;}
    public void set_maxinte(List<Double> _maxinte) {this._maxinte = _maxinte;}
    public void set_time(List<Double> _time) {this._time = _time;}
    public void set_XVec(List<Double> _XVec) {this._XVec = _XVec;}
    public void set_YVec(List<Double> _YVec) {this._YVec = _YVec;}
    public void set_ZVec(List<Double> _ZVec) {this._ZVec = _ZVec;}
    public void set_TIDMap(HashMap<Integer, HashMap<Integer,List<Integer>>> _TIDMap) {this._TIDMap = _TIDMap;}
    public void set_strkTIDMap(HashMap<Integer, HashMap<Integer,List<Integer>>> _strkTIDMap) {this._strkTIDMap = _strkTIDMap;}
    public void set_FinalTimeMap(HashMap<Integer, List<HitVector>> _finalTIDMap){this._FinalTIDMap = _finalTIDMap;}
    public void set_alltracks(HashMap<Integer, List<HitVector>> _alltracks) {this._alltracks = _alltracks;}
    public void set_largetmap(HashMap<Integer, Double> _largetmap) {this._largetmap = _largetmap;}
    public void set_recotrackmap(HashMap<Integer,List<RecoHitVector>> _recotrackmap) {this._recotrackmap = _recotrackmap;}
    public void set_trackmap(TrackMap _trackmap) {this._trackmap = _trackmap;}
    public void set_rtrackmap(ReducedTrackMap _rtrackmap) {this._rtrackmap = _rtrackmap;}
    public void set_finaltrackinfomap(HashMap<Integer, FinalTrackInfo> _finaltrackinfomap) {this._finaltrackinfomap = _finaltrackinfomap;}

    
    public HitParameters() {}


}