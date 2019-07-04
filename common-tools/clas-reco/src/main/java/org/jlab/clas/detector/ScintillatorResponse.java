package org.jlab.clas.detector;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;


/**
 *
 * @author jnewton
 */
public class ScintillatorResponse extends DetectorResponse {
    
    public ScintillatorResponse(){
        super();
    }
    
   public ScintillatorResponse(int sector, int layer, int component){
        this.getDescriptor().setSectorLayerComponent(sector, layer, component);
    }
    
    public static List<DetectorResponse>  readHipoEvent(DataEvent event, 
            String bankName, DetectorType type){        
        List<DetectorResponse> responseList = new ArrayList<>();
        if(event.hasBank(bankName)==true){
            DataBank bank = event.getBank(bankName);
            int nrows = bank.rows();
            for(int row = 0; row < nrows; row++){
                int sector = bank.getByte("sector", row);
                int layer  = bank.getByte("layer", row);
                int paddle = bank.getShort("component", row);
                DetectorResponse  response = new DetectorResponse(sector,layer,paddle);
                response.setHitIndex(row);
                response.getDescriptor().setType(type);
                float x = bank.getFloat("x", row);
                float y = bank.getFloat("y", row);
                float z = bank.getFloat("z", row);
                response.setPosition(x, y, z);
                response.setEnergy(bank.getFloat("energy", row));
                response.setTime(bank.getFloat("time", row));
                response.setStatus(bank.getInt("status",row));

                responseList.add(response);
            }
        }
        return responseList;
    }
    
}
