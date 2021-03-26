package org.jlab.rec.dc.cross;

import java.util.ArrayList;
import java.util.List;

/**
 * List of DC crosses used to fit a track candidate
 */
public class CrossList extends ArrayList<List<Cross>> {

    /**
     * the serial version ID (automatically generated in Eclipse)
     */
    private static final long serialVersionUID = 8509791607282273163L;

    public void removeDuplicates(CrossList crosslist) {

        int size = this.size();
        for(int i = 0; i< size; i++) {
            for(int j = 0; j< crosslist.size(); j++) {
                List<Cross> thisList = this.get(i);
                List<Cross> otherList = crosslist.get(j);
                if(thisList.get(0).get_Id()==otherList.get(0).get_Id() ||
                        thisList.get(1).get_Id()==otherList.get(1).get_Id() ||
                            thisList.get(2).get_Id()==otherList.get(2).get_Id()) {
                    this.remove(thisList);
                    size--;
                }
            }
        }
    }

}
