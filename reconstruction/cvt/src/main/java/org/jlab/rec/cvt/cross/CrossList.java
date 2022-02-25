package org.jlab.rec.cvt.cross;

import java.util.ArrayList;

/**
 * List of crosses used to fit a track candidate
 */
public class CrossList extends ArrayList<ArrayList<Cross>> {

    /**
     * the serial version ID (automatically generated in Eclipse)
     */
    private static final long serialVersionUID = 8509791607282273163L;

    public boolean ContainsNot(ArrayList<ArrayList<Cross>> trkCnds,
            ArrayList<Cross> trkCand) {

        boolean DoesNotContain = true;

        for (int i = 0; i < trkCnds.size(); i++) {
            if (Contains(trkCnds.get(i), trkCand)) {
                DoesNotContain = false;
            }
        }
        return DoesNotContain;
    }

    public boolean Contains(ArrayList<Cross> arrayList,
            ArrayList<Cross> arrayList2) {

        if (arrayList.size() < arrayList2.size()) {
            return false;
        }

        int array2size = arrayList2.size();
        for (int i = 0; i < arrayList.size(); i++) {
            Cross c1 = arrayList.get(i);

            for (int j = 0; j < array2size; j++) {
                Cross c2 = arrayList2.get(j);

                if ((c1.getDetector()).equals(c2.getDetector()) && c1.getId() == c2.getId()) {
                    arrayList2.remove(j);

                    if (array2size > 0) {
                        array2size--;
                    }
                }
            }

        }
        return array2size == 0;
    }

}
