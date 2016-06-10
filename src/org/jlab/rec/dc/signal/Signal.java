package org.jlab.rec.dc.signal;

/**
 * A DC signal charaterized by crate, slot, channel and TDC value. This is not used for simulated events
 * @author ziegler
 *
 */
public class Signal {
	
	public Signal(int crate, int slot, int channel, int tdc) {
		this._Channel = channel;
		this._Crate = crate;
		this._Slot = slot;
		this._TDC = tdc;
	}
	
	private int _Crate;
	private int _Slot;
	private int _Channel;
	private int _TDC;

	public int get_Crate() {
		return _Crate;
	}
	public void set_Crate(int _Crate) {
		this._Crate = _Crate;
	}
	public int get_Slot() {
		return _Slot;
	}
	public void set_Slot(int _Slot) {
		this._Slot = _Slot;
	}
	public int get_Channel() {
		return _Channel;
	}
	public void set_Channel(int _Channel) {
		this._Channel = _Channel;
	}
	public int get_TDC() {
		return _TDC;
	}
	public void set_TDC(int _TDC) {
		this._TDC = _TDC;
	}


	public double get_Sector(int crate, int slot, int channel) {
		return -1;
	}
	
	public int get_Superlayer(int crate, int slot, int channel) {
		return -1;
	}
	
	public int get_Layer(int crate, int slot, int channel) {
		return -1;
	}
	
	public int get_Wire(int crate, int slot, int channel) {
		return -1;
	}
	
	public double get_Time(int tdc) {
		return -1;
	}
	

}
