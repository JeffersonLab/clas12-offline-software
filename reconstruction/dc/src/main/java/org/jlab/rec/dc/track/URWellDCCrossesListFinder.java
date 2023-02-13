package org.jlab.rec.dc.track;

import java.util.List;
import org.jlab.rec.dc.cross.CrossList;
import org.jlab.rec.urwell.reader.URWellCross;
import org.jlab.rec.dc.cross.Cross;

public class URWellDCCrossesListFinder {
       
    public URWellDCCrossesListFinder() {
    }
    
    public URWellDCCrossesList candURWellDCCrossLists(List<URWellCross> urCrosses, CrossList crosslist){
        URWellDCCrossesList urDCCrossesList = new URWellDCCrossesList();
                
        for(int i = 0; i < urCrosses.size(); i++){
            for(int j = 0; j < crosslist.size(); j++){
                URWellCross urCross = urCrosses.get(i);
                List<Cross> dcCrosses = crosslist.get(j);                 
                
                if(urCross.sector() == dcCrosses.get(0).get_Sector() && urCross.sector() == dcCrosses.get(1).get_Sector() && urCross.sector() == dcCrosses.get(2).get_Sector())
                    urDCCrossesList.add_URWellDCCrosses(urCross, dcCrosses);                                
            }
        }
        
        return urDCCrossesList;        
    }
    
    
}
