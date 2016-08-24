package org.jlab.service;

import java.util.HashMap;

public class DetectorConstants {

	public DetectorConstants() {
		// TODO Auto-generated constructor stub
	}

	private HashMap<String, double[][][]> _CalibConstants = new HashMap<String, double[][][]>();
    
    public HashMap<String, double[][][]> get_CalibConstants() {
		return _CalibConstants;
	}

	public void set_CalibConstants(HashMap<String, double[][][]> _CalibConstants) {
		this._CalibConstants = _CalibConstants;
	}

	private HashMap<String, double[][]> _GeomConstants = new HashMap<String, double[][]>();
	
	public HashMap<String, double[][]> get_GeomConstants() {
		return _GeomConstants;
	}

	public void set_GeomConstants(HashMap<String, double[][]> _GeomConstants) {
		this._GeomConstants = _GeomConstants;
	}

	
}
