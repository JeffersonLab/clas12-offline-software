package org.jlab.detector.decode;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.jnp.hipo.packing.DataPacking;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author gavalian
 */
public class AbsDetectorDataDecoder implements DetectorDataDecoder {
    
    private String            detectorName = "UNKNOWN";
    private String           detectorTable = "UNKNOWN";
    private IndexedTable  translationTable = null;
    private DataPacking         dataPacker = new DataPacking();
    private ByteBuffer         pulseBuffer = null;
    
    public AbsDetectorDataDecoder(String name, String table){
        detectorName  = name;
        detectorTable = table;
        byte[] array  = new byte[500*1024];
        pulseBuffer   = ByteBuffer.wrap(array);
        pulseBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }
    
    public String getName(){
        return detectorName;
    }
    
    public String getTable(){
        return detectorTable;
    }

    public void setConstantsTable(IndexedTable table){
        this.translationTable = table;
    }
    
    @Override
    public List<DetectorDataDgtz> decode(List<DetectorDataDgtz> dgtzData) {
        if(this.translationTable==null){
            System.out.println("[AbsDetectorDataDecoder] **** error *** the table for ["
                    + detectorName + "] is not present.");
            return new ArrayList<>();
        }
        
        DetectorType  type = DetectorType.getType(detectorName);
        List<DetectorDataDgtz> data = new ArrayList<>();
        for(DetectorDataDgtz dgtz : dgtzData){
            int crate    = dgtz.getDescriptor().getCrate();
            int slot     = dgtz.getDescriptor().getSlot();
            int channel  = dgtz.getDescriptor().getChannel();
            if(translationTable.hasEntry(crate,slot,channel)==true){
                    int sector    = translationTable.getIntValue("sector", crate,slot,channel);
                    int layer     = translationTable.getIntValue("layer", crate,slot,channel);
                    int component = translationTable.getIntValue("component", crate,slot,channel);
                    int order     = translationTable.getIntValue("order", crate,slot,channel);
                    dgtz.getDescriptor().setSectorLayerComponent(sector, layer, component);
                    dgtz.getDescriptor().setOrder(order);
                    dgtz.getDescriptor().setType(type);
                    data.add(dgtz);
            }
        }
        return data;
    }
    
    @Override
    public List<DataBank> createBanks(List<DetectorDataDgtz> dgtzData, DataEvent event) {
        List<DetectorDataDgtz> translatedData = this.decode(dgtzData);      
        List<DetectorDataDgtz> tdc = AbsDetectorDataDecoder.getTDCData(translatedData);
        List<DetectorDataDgtz> adc = AbsDetectorDataDecoder.getADCData(translatedData);

        List<DataBank>  dataBanks = new ArrayList<>();
        if(!tdc.isEmpty()){
            DataBank  bankTDC = event.createBank(detectorName+"::tdc", tdc.size());
            if(bankTDC==null){
                System.out.println(" ERROR Trying to create bank " + detectorName+"::tdc");
                System.out.println(" TDC ARRAY LENGTH = " + tdc.size());
                for(DetectorDataDgtz d : tdc){
                    System.out.println(d);
                }
            }
            if(bankTDC!=null){
                for(int i = 0; i < tdc.size(); i++){
                    bankTDC.setByte("sector", i, (byte) tdc.get(i).getDescriptor().getSector());
                    bankTDC.setByte("layer", i, (byte) tdc.get(i).getDescriptor().getLayer());
                    bankTDC.setShort("component", i, (short) tdc.get(i).getDescriptor().getComponent());
                    bankTDC.setByte("order", i, (byte) tdc.get(i).getDescriptor().getOrder());
                    bankTDC.setInt("TDC", i, tdc.get(i).getTDCData(0).getTime());
                }
                dataBanks.add(bankTDC);
            }
        }
        
        /**
         * Write ADC pulses
         */
        if(!adc.isEmpty()){
            byte[] bufferArray = new byte[256];
            ByteBuffer buffer = ByteBuffer.wrap(bufferArray);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            pulseBuffer.rewind();
            int position = 0;
            for(int i = 0; i < adc.size(); i++){
                DetectorDataDgtz data = adc.get(i);
                short[] pulse = data.getADCData(0).getPulseArray();
                this.dataPacker.pack(buffer, pulse, 0);
                
                pulseBuffer.putInt(data.getDescriptor().getHashCode());
                position +=4;
                short packedLength = (short) buffer.limit();
                pulseBuffer.putShort(position, (short) buffer.limit());
                position +=2;
                
                System.arraycopy(buffer.array(), 0, pulseBuffer.array(), position, packedLength);
                position += packedLength;
            }
            DataBank bankPULSES = event.createBank(getName() + "::pulse", position);
            for(int i = 0; i < position; i++) bankPULSES.setByte("data", i, pulseBuffer.get(i));
            dataBanks.add(bankPULSES);
        }
        return dataBanks;
    }

    /**
     * returns a list of values from the list that are TDC values.
     * used to filter out TDC values for creating the banks
     * @param dgtzData the list containing all digitized data
     * @return list that contains only TDC digitized data
     */
    public static List<DetectorDataDgtz>  getTDCData(List<DetectorDataDgtz> dgtzData){
        List<DetectorDataDgtz> tdc = new ArrayList<>();
        for(DetectorDataDgtz dgtz : dgtzData){
            if(dgtz.getTDCSize()>0){
                tdc.add(dgtz);
            }
        }
        return tdc;
    }

    public static List<DetectorDataDgtz>  getADCData(List<DetectorDataDgtz> dgtzData){
        List<DetectorDataDgtz> adc = new ArrayList<>();
        for(DetectorDataDgtz dgtz : dgtzData){
            if(dgtz.getADCSize()>0){
                adc.add(dgtz);
            }
        }
        return adc;
    }

}
