/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.decode;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.base.DetectorCollection;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.decode.DetectorDataDgtz.ADCData;
import org.jlab.detector.decode.DetectorDataDgtz.TDCData;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataDescriptor;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioSource;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.utils.groups.IndexedList;

/**
 *
 * @author gavalian
 */
public class DetectorBankDgtz {
    
    public static final int         BANKTYPE_ADC = 1;
    public static final int         BANKTYPE_TDC = 2;
    public static final int     BANKTYPE_ADC_TDC = 3;
    public static final int BANKTYPE_ADCLR_TDCLR = 4;
    public static final int BANKTYPE_ADCUD_TDCUD = 5;

    
    private List<DetectorDataDgtz>  dgtzData = new ArrayList<DetectorDataDgtz>();
    private String                  bankName = "FTOF::dgtz";
    private int                     bankType = DetectorBankDgtz.BANKTYPE_ADCLR_TDCLR;
    
    public DetectorBankDgtz(){
        
    }
    
    public String getName(){
        return this.bankName;
    }
    
    private int getBankType(DataDescriptor desc){
        if(desc.hasEntries("ADCL","ADCR","TDCL","TDCR")==true) 
            return DetectorBankDgtz.BANKTYPE_ADCLR_TDCLR;
        if(desc.hasEntries("ADC","TDC")==true)
            return DetectorBankDgtz.BANKTYPE_ADC_TDC;
        if(desc.hasEntries("TDC")==true)
            return DetectorBankDgtz.BANKTYPE_TDC;
        if(desc.hasEntries("ADCU","ADCD","TDCU","TDCD")==true){
            return DetectorBankDgtz.BANKTYPE_ADCUD_TDCUD;
        }
        return 0;
    }
    
    
    public static DetectorCollection<List<DetectorDataDgtz>> getDetectorData(DetectorType type, List<DetectorDataDgtz> dataDgtz){
        DetectorCollection<List<DetectorDataDgtz>>  dataList = new DetectorCollection<List<DetectorDataDgtz>>();
        
        return dataList;
    }
    
    public final void readFromEvent(DataEvent event, String bank){
        
        //DataDescriptor desc = event.getDictionary().getDescriptor(bank);        
        this.dgtzData.clear();
        this.bankType = DetectorBankDgtz.BANKTYPE_ADCLR_TDCLR;
        /*if(desc==null){        
            return;
        }*/
        
        int type = bankType;//getBankType(desc);
        if(event.hasBank(bank)==true){
            DataBank dataBank = event.getBank(bank);
            int nrows = dataBank.rows();
            for(int i = 0; i < nrows; i++){
                
                DetectorDataDgtz data = new DetectorDataDgtz();
                int sector    = dataBank.getInt("sector",i);
                int layer     = dataBank.getInt("layer",i);
                int component = dataBank.getInt("component",i);                
                data.getDescriptor().setSectorLayerComponent(sector, layer, component);
                
                switch (type) {
                    case DetectorBankDgtz.BANKTYPE_ADCLR_TDCLR: {
                        ADCData adcL = new ADCData();
                        adcL.setIntegral(dataBank.getInt("ADCL", i));
                        adcL.setPedestal((short) 0);
                        adcL.setADC(0, 0);
                        adcL.setOrder(0);
                        data.addADC(adcL);
                        ADCData adcR = new ADCData();
                        adcR.setIntegral(dataBank.getInt("ADCR", i));
                        adcR.setPedestal((short) 0);
                        adcR.setADC(0, 0);
                        adcR.setOrder(1);
                        data.addADC(adcR);
                        TDCData tdcL = new TDCData();
                        tdcL.setTime((short) dataBank.getInt("TDCL", i));
                        tdcL.setOrder(2);
                        data.addTDC(tdcL);
                        TDCData tdcR = new TDCData();
                        tdcR.setTime((short) dataBank.getInt("TDCR", i));
                        tdcR.setOrder(2);
                        data.addTDC(tdcR);
                    }     
                    default: break;
                }
                
                this.dgtzData.add(data);
            }
        }
        
        //DataDescriptor desc = event.getDictionary().getDescriptor(bank);
    }
    
    public final void readFromEvent(DataEvent event){
        this.readFromEvent(event, bankName);
    }
    
    
    public void show(){
        System.out.println("bank show #### " + bankName);
        for(DetectorDataDgtz data : this.dgtzData){
            System.out.println("-----------");
            System.out.println(data.toString());
        }
    }
    public static void main(String[] args){
        
        /*DetectorBankDgtz bank = new DetectorBankDgtz();
        HipoDataSource reader = new HipoDataSource();
        reader.open("/Users/gavalian/Work/Software/Release-9.0/COATJAVA/challenge/rec.short.hipo");
        int counter = 0;
        while(reader.hasEvent()==true){
            DataEvent event = reader.getNextEvent();
            bank.readFromEvent(event, "FTOF::dgtz");
            bank.show();
            counter++;
            
            if(counter>10) break;
        }*/
        
        EvioSource reader = new EvioSource();
        reader.open("/Users/gavalian/Work/Software/Release-9.0/COATJAVA/gagik/sector2_000233_mode7.evio.0");
        CodaEventDecoder               decoder = new CodaEventDecoder();
        DetectorEventDecoder   detectorDecoder = new DetectorEventDecoder();
        
        while(reader.hasEvent()==true){
            EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
            List<DetectorDataDgtz>  dataSet = decoder.getDataEntries((EvioDataEvent) event);
            detectorDecoder.translate(dataSet);
            //detectorDecoder.fitPulses(dataSet);
            System.out.println("------------------ ######### ");
            for(DetectorDataDgtz data : dataSet){
                System.out.println(data);
            }
        }
        
        
    }
}
