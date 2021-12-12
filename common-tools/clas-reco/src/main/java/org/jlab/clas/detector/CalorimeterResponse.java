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

    public void copy(CalorimeterResponse r) {
        super.copy(r);
        widthUVW.copy(r.widthUVW);
        coordUVW.copy(r.coordUVW);
        secondMomentUVW.copy(r.secondMomentUVW);
        thirdMomentUVW.copy(r.thirdMomentUVW);
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

    public static List<DetectorResponse>  readHipoEvent(DataEvent event, 
            String bankName, DetectorType type, String momentsBankName){        
        List<DetectorResponse> responseList = new ArrayList<>();
        if(event.hasBank(bankName)==true){
            DataBank bank = event.getBank(bankName);
            DataBank momentsBank=null;
            if (momentsBankName!=null && event.hasBank(momentsBankName)) {
                momentsBank=event.getBank(momentsBankName);
                if (bank.rows() != momentsBank.rows()) {
                    throw new RuntimeException("Bank length mismatch: "+bankName+" and "+momentsBankName);
                }
            }
            int nrows = bank.rows();
            for(int row = 0; row < nrows; row++){
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
                if (momentsBank!=null) {
                    u = momentsBank.getFloat("distU",row);
                    v = momentsBank.getFloat("distV",row);
                    w = momentsBank.getFloat("distW",row);
                    response.setCoordUVW(u,v,w);
                    u = momentsBank.getFloat("m2u",row);
                    v = momentsBank.getFloat("m2v",row);
                    w = momentsBank.getFloat("m2w",row);
                    response.setSecondMomentUVW(u,v,w);
                    u = momentsBank.getFloat("m3u",row);
                    v = momentsBank.getFloat("m3v",row);
                    w = momentsBank.getFloat("m3w",row);
                    response.setThirdMomentUVW(u,v,w);
                }
                response.setEnergy(bank.getFloat("energy", row));
                response.setTime(bank.getFloat("time", row));
                response.setStatus(bank.getInt("status",row));

                responseList.add((DetectorResponse)response);
            }
        }
        return responseList;
    }

}

