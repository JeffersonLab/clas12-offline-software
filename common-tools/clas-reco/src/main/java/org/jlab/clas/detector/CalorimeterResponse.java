package org.jlab.clas.detector;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

import org.jlab.clas.physics.Vector3;

/**
 *
 * @author jnewton
 */
public class CalorimeterResponse extends DetectorResponse {

    private final Vector3 widthUVW = new Vector3(0,0,0);
    private final Vector3 coordUVW = new Vector3(0,0,0);
    private final Vector3 secondMomentUVW = new Vector3(0,0,0);
    private final Vector3 thirdMomentUVW = new Vector3(0,0,0);

    private final int[] dbStatus = {-1,-1,-1};
    private final Vector3 rawPeakEnergy = new Vector3(0,0,0);
    private final Vector3 reconPeakEnergy = new Vector3(0,0,0);
    private final Vector3 peakTDCTime = new Vector3(0,0,0);
    private final Vector3 peakFADCTime = new Vector3(0,0,0);

    public CalorimeterResponse(){
        super();
    }

    public CalorimeterResponse(CalorimeterResponse r) {
        super();
        this.copy(r);
    }

    public CalorimeterResponse(int sector, int layer, int component){
        this.getDescriptor().setSectorLayerComponent(sector, layer, component);
    }

    public final void copy(CalorimeterResponse r) {
        super.copy(r);
        widthUVW.copy(r.widthUVW);
        coordUVW.copy(r.coordUVW);
        secondMomentUVW.copy(r.secondMomentUVW);
        thirdMomentUVW.copy(r.thirdMomentUVW);
        rawPeakEnergy.copy(r.rawPeakEnergy);
        reconPeakEnergy.copy(r.reconPeakEnergy);
        peakTDCTime.copy(r.peakTDCTime);
        peakFADCTime.copy(r.peakFADCTime);
        dbStatus[0] = r.dbStatus[0];
        dbStatus[1] = r.dbStatus[1];
        dbStatus[2] = r.dbStatus[2];
    }

    /**
     * Share energy between this and another response.
     * @param other The other response to share with
     * @param ratio The fraction that goes to the other response
     */
    public void shareEnergy(DetectorResponse other, double ratio) {
        double energyOther = other.getEnergy() * ratio;
        double energyThis = this.getEnergy() * (1-ratio);
        other.setEnergy(energyOther);
        this.setEnergy(energyThis);
    }

    public void setWidthUVW(float u,float v,float w) {
        widthUVW.setXYZ(u,v,w);
    }
    public void setCoordUVW(float u,float v,float w) {
        coordUVW.setXYZ(u,v,w);
    }
    public void setSecondMomentUVW(float u,float v,float w) {
        secondMomentUVW.setXYZ(u,v,w);
    }
    public void setThirdMomentUVW(float u,float v,float w) {
        thirdMomentUVW.setXYZ(u,v,w);
    }
    public Vector3 getWidthUVW() {
        return widthUVW;
    }
    public Vector3 getCoordUVW() {
        return coordUVW;
    }
    public Vector3 getSecondMomentUVW() {
        return secondMomentUVW;
    }
    public Vector3 getThirdMomentUVW() {
        return thirdMomentUVW;
    }
    public void setPeakStatus(int u, int v, int w) {
        this.dbStatus[0] = u;
        this.dbStatus[1] = v;
        this.dbStatus[2] = w;
    }
    public int[] getPeakStatus() {
        return this.dbStatus;
    }
    public Vector3 getRawPeakEnergy() {
        return this.rawPeakEnergy;
    }
    public Vector3 getReconPeakEnergy() {
        return this.reconPeakEnergy;
    }
    public Vector3 getPeakTDCTime() {
        return this.peakTDCTime;
    }
    public Vector3 getPeakFADCTime() {
        return this.peakFADCTime;
    }

    public void setRawPeakEnergy(float u,float v,float w) {
        this.rawPeakEnergy.setXYZ(u,v,w);
    }
    public void setReconPeakEnergy(float u,float v,float w) {
        this.reconPeakEnergy.setXYZ(u,v,w);
    }
    public void setPeakTDCTime(float u,float v,float w) {
        this.peakTDCTime.setXYZ(u,v,w);
    }
    public void setPeakFADCTime(float u,float v,float w) {
        this.peakFADCTime.setXYZ(u,v,w);
    }

    /**
     * Read just the main calorimeter cluster bank.
     * @param event
     * @param bankName
     * @param type
     * @return 
     */
    public static List<DetectorResponse>  readHipoEvent(DataEvent event, 
            String bankName, DetectorType type) {
        List<DetectorResponse> responseList = new ArrayList<>();
        if(event.hasBank(bankName)==true){
            DataBank bank = event.getBank(bankName);
            for(int row = 0; row < bank.rows(); row++){
                int sector = bank.getByte("sector", row);
                int layer = bank.getByte("layer", row);
                CalorimeterResponse  response = new CalorimeterResponse(sector,layer,0);
                response.setHitIndex(row);
                response.getDescriptor().setType(type);
                float x = bank.getFloat("x", row);
                float y = bank.getFloat("y", row);
                float z = bank.getFloat("z", row);
                response.setPosition(x, y, z);
                float u = bank.getFloat("widthU",row);
                float v = bank.getFloat("widthV",row);
                float w = bank.getFloat("widthW",row);
                response.setWidthUVW(u,v,w);
                response.setEnergy(bank.getFloat("energy", row));
                response.setTime(bank.getFloat("time", row));
                response.setStatus(bank.getInt("status",row));
                responseList.add((DetectorResponse)response);
            }
        }
        return responseList;
    }   
   
    /**
     * Read the main calorimeter cluster bank and its moments partner bank.
     * @param event
     * @param bankName
     * @param type
     * @param momentsBankName
     * @return 
     */
    public static List<DetectorResponse>  readHipoEvent(DataEvent event, 
            String bankName, DetectorType type, String momentsBankName) {
        List<DetectorResponse> responseList = readHipoEvent(event, bankName, type);
        DataBank momentsBank = event.getBank(momentsBankName);
        if (responseList.size() != momentsBank.rows()) {
            throw new RuntimeException("Bank length mismatch: "+bankName+" and "+momentsBankName);
        }
        for(int row = 0; row < momentsBank.rows(); row++){
            float u,v,w;
            u = momentsBank.getFloat("distU",row);
            v = momentsBank.getFloat("distV",row);
            w = momentsBank.getFloat("distW",row);
            ((CalorimeterResponse)responseList.get(row)).setCoordUVW(u,v,w);
            u = momentsBank.getFloat("m2u",row);
            v = momentsBank.getFloat("m2v",row);
            w = momentsBank.getFloat("m2w",row);
            ((CalorimeterResponse)responseList.get(row)).setSecondMomentUVW(u,v,w);
            u = momentsBank.getFloat("m3u",row);
            v = momentsBank.getFloat("m3v",row);
            w = momentsBank.getFloat("m3w",row);
            ((CalorimeterResponse)responseList.get(row)).setThirdMomentUVW(u,v,w);
        }
        return responseList;
    }
   
    /**
     * Read the main calorimeter cluster bank and its moments and extras partner banks.
     * @param event
     * @param bankName
     * @param type
     * @param momentsBankName
     * @param extrasBankName
     * @return 
     */
    public static List<DetectorResponse>  readHipoEvent(DataEvent event, 
            String bankName, DetectorType type, String momentsBankName, String extrasBankName){
        List<DetectorResponse> responseList = readHipoEvent(event, bankName, type, momentsBankName);
        DataBank extrasBank = event.getBank(extrasBankName);
        if (responseList.size() != extrasBank.rows()) {
            throw new RuntimeException("Bank length mismatch: "+bankName+" and "+extrasBankName);
        }
        for(int row = 0; row < extrasBank.rows(); row++){
            float u,v,w;
            int i = extrasBank.getShort("dbstU",row);
            int j = extrasBank.getShort("dbstV",row);
            int k = extrasBank.getShort("dbstW",row);
            ((CalorimeterResponse)responseList.get(row)).setPeakStatus(i,j,k);
            u = extrasBank.getFloat("rawEU",row);
            v = extrasBank.getFloat("rawEV",row);
            w = extrasBank.getFloat("rawEW",row);
            ((CalorimeterResponse)responseList.get(row)).setRawPeakEnergy(u,v,w);
            u = extrasBank.getFloat("recEU",row);
            v = extrasBank.getFloat("recEV",row);
            w = extrasBank.getFloat("recEW",row);
            ((CalorimeterResponse)responseList.get(row)).setReconPeakEnergy(u,v,w);
            u = extrasBank.getFloat("recDTU",row);
            v = extrasBank.getFloat("recDTV",row);
            w = extrasBank.getFloat("recDTW",row);
            ((CalorimeterResponse)responseList.get(row)).setPeakTDCTime(u,v,w);
            u = extrasBank.getFloat("recFTU",row);
            v = extrasBank.getFloat("recFTV",row);
            w = extrasBank.getFloat("recFTW",row);
            ((CalorimeterResponse)responseList.get(row)).setPeakFADCTime(u,v,w);
        }
        return responseList;
    } 

}

