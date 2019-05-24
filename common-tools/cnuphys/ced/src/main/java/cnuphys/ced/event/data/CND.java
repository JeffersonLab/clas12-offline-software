package cnuphys.ced.event.data;

import java.awt.Color;

import org.jlab.io.base.DataEvent;

import cnuphys.ced.alldata.ColumnData;

public class CND extends DetectorData {

	/** all the data arrays */
	public byte adc_sect[];
	public byte adc_layer[];
	public byte adc_order[];
	public int adc_ADC[];
	public float adc_time[];
	public short adc_ped[];

	public byte tdc_sect[];
	public byte tdc_layer[];
	public byte tdc_order[];
	public int tdc_TDC[];

	private int _maxAdc = 500;

//	TdcAdcHitList _tdcAdcHits = new TdcAdcHitList("CND::tdc", "CND::adc");

	private static CND _instance;

	/**
	 * Public access to the singleton
	 * 
	 * @return the CND singleton
	 */
	public static CND getInstance() {
		if (_instance == null) {
			_instance = new CND();
		}
		return _instance;
	}

	@Override
	public void newClasIoEvent(DataEvent event) {

//		_tdcAdcHits =  new TdcAdcHitList("CND::tdc", "CND::adc");

		adc_sect = ColumnData.getByteArray("CND::adc.sector");
		adc_layer = ColumnData.getByteArray("CND::adc.layer");
		adc_order = ColumnData.getByteArray("CND::adc.order");
		adc_ADC = ColumnData.getIntArray("CND::adc.ADC");
		adc_time = ColumnData.getFloatArray("CND::adc.time");
		adc_ped = ColumnData.getShortArray("CND::adc.ped");

		tdc_sect = ColumnData.getByteArray("CND::tdc.sector");
		tdc_layer = ColumnData.getByteArray("CND::tdc.layer");
		tdc_order = ColumnData.getByteArray("CND::tdc.order");
		tdc_TDC = ColumnData.getIntArray("CND::tdc.TDC");

		_maxAdc = 500;

		if (adc_ADC != null) {
			for (int adc : adc_ADC) {
				_maxAdc = Math.max(_maxAdc, adc);
			}
		}
	}

	/**
	 * Get the number of adc hits
	 * 
	 * @return the number of adc hits
	 */
	public int getCountAdc() {
		return (adc_sect != null) ? adc_sect.length : 0;
	}

	/**
	 * Get the number of tdc hits
	 * 
	 * @return the number of tdc hits
	 */
	public int getCountTdc() {
		return (tdc_sect != null) ? tdc_sect.length : 0;
	}

	/**
	 * Get a color with apha based of relative adc
	 * 
	 * @param adc the adc value
	 * @return a fill color for adc hits
	 */
	public Color adcColor(int adc) {
		if (adc < 1) {
			return Color.white;
		}

		double fract = ((double) adc) / _maxAdc;
		fract = Math.max(0, Math.min(1.0, fract));

//		int alpha = 128 + (int)(127*fract);
//		alpha = Math.min(255,  alpha);

//		System.err.println("adc " +adc + "  max " + _maxAdc + "  fract " + fract);
		return AdcColorScale.getInstance().getAlphaColor(fract, 255);
	}

	/**
	 * Update data used only during accumulation
	 */
	public void updateData() {
		adc_sect = ColumnData.getByteArray("CND::adc.sector");
		adc_layer = ColumnData.getByteArray("CND::adc.layer");
		adc_order = ColumnData.getByteArray("CND::adc.order");
		adc_ADC = ColumnData.getIntArray("CND::adc.ADC");
		adc_time = ColumnData.getFloatArray("CND::adc.time");
		adc_ped = ColumnData.getShortArray("CND::adc.ped");

		tdc_sect = ColumnData.getByteArray("CND::tdc.sector");
		tdc_layer = ColumnData.getByteArray("CND::tdc.layer");
		tdc_order = ColumnData.getByteArray("CND::tdc.order");
		tdc_TDC = ColumnData.getIntArray("CND::tdc.TDC");

	}

	/**
	 * Update the list. This is probably needed only during accumulation
	 * 
	 * @return the updated list
	 */
//	public TdcAdcHitList updateTdcAdcList() {
//		_tdcAdcHits =  new TdcAdcHitList("CND::tdc", "CND::adc");
//		return _tdcAdcHits;
//	}

	/**
	 * Get the tdc and adc hit list
	 * 
	 * @return the tdc adc hit list
	 */
//	public TdcAdcHitList getHits() {
//		return _tdcAdcHits;
//	}
}