package org.jlab.rec.dc.track;

import java.util.List;
import java.util.ArrayList;
import org.jlab.rec.dc.cross.CrossList;
import org.jlab.rec.urwell.reader.URWellCross;
import org.jlab.rec.dc.cross.Cross;

public class URWellDCCrossesList{
    private List<URWellDCCrosses> urDCCrossesList = new ArrayList<URWellDCCrosses>();
    
    public URWellDCCrossesList(){
        
    }
    
    public List<URWellDCCrosses> get_URWellDCCrossesList(){
        return urDCCrossesList;
    }
    
    public void set_URWellDCCrossesList(List<URWellDCCrosses> urDCCrossesList){
        this.urDCCrossesList = urDCCrossesList;
    }
    
    public void add_URWellDCCrosses(URWellCross urCross, List<Cross> dcCrosses){
        urDCCrossesList.add(new URWellDCCrosses(urCross, dcCrosses));
    }

    public class URWellDCCrosses {

        private URWellCross urCross;
        private List<Cross> dcCrosses = new ArrayList<Cross>();

        public URWellDCCrosses(URWellCross urCross, List<Cross> dcCrosses) {
            this.urCross = urCross;
            this.dcCrosses = dcCrosses;
        }
        
        URWellCross get_URWellCross(){
            return urCross;
        }
        
        List<Cross> get_DCCrosses(){
            return dcCrosses;
        }

    }
}
