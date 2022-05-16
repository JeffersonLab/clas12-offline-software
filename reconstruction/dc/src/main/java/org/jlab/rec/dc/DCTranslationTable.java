package org.jlab.rec.dc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.jlab.detector.base.DetectorCollection;
import org.jlab.detector.base.DetectorDescriptor;

/**
 * Converts DC readout board (crate, slot, channel) to DC wire hit (sector,
 * layer, wire) where crate runs from 67 to 67+17, active readout slots are 4 to
 * 10 and 13 to 19, and DCRB channels run from 0 to 95. Sector runs from 1 to 6,
 * layer from 1 to 36, and wire from 1 to 112
 *
 */
public class DCTranslationTable {

    /**
     * IP names and ROC IDs for drift chamber crates are following: dc11 - 41
     * dc12 - 42 dc13 - 43 dc21 - 44 dc22 - 45 dc23 - 46 dc31 - 47 dc32 - 48
     * dc33 - 49 dc41 - 50 dc42 - 51 dc43 - 52 dc51 - 53 dc52 - 54 dc53 - 55
     * dc61 - 56 dc62 - 57 dc63 - 58
     *
     * where first digit in IP name 'dcXX' is sector number (1-6) and second is
     * region numbers (1-3).
     */
    // the sector served by the 1st to the 18th crate
    // likewise for the region
    //int[] crate_sector = {1,2,3,4,5,6,1,2,3,4,5,6,1,2,3,4,5,6};
    int[] crate_sector = {1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6};
    int[] crate_region = {1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3};
    // the local layer corresponding to channel number from 0 to 95
    int[] chan_loclayer = {2, 4, 6, 1, 3, 5, 2, 4, 6, 1, 3, 5, 2, 4, 6, 1, 3, 5,
        2, 4, 6, 1, 3, 5, 2, 4, 6, 1, 3, 5, 2, 4, 6, 1, 3, 5, 2, 4, 6, 1, 3, 5,
        2, 4, 6, 1, 3, 5, 2, 4, 6, 1, 3, 5, 2, 4, 6, 1, 3, 5, 2, 4, 6, 1, 3, 5,
        2, 4, 6, 1, 3, 5, 2, 4, 6, 1, 3, 5, 2, 4, 6, 1, 3, 5, 2, 4, 6, 1, 3, 5,
        2, 4, 6, 1, 3, 5};
    // the local wire number on one STB board corresponding to channel number from 0 to 95
    int[] chan_locwire = {1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3,
        4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7,
        8, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 10,
        11, 11, 11, 11, 11, 11, 12, 12, 12, 12, 12, 12, 13, 13, 13, 13, 13, 13,
        14, 14, 14, 14, 14, 14, 15, 15, 15, 15, 15, 15, 16, 16, 16, 16, 16, 16};
    // the STB corresponding to DCRB slots 
    //int[] slot_stb = {0,0,0,1,2,3,4,5,6,7,0,0,1,2,3,4,5,6,7,0};
    int[] slot_stb = {0, 0, 1, 2, 3, 4, 5, 6, 7, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7};
    //the local superlayer corresponding to STB slots 
    //int[] slot_locsuplayer = {0,0,0,1,1,1,1,1,1,1,0,0,2,2,2,2,2,2,2,0};
    int[] slot_locsuplayer = {0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2};

    public static final int[] cableid
            = {0, 0, 1, 7, 13, 19, 25, 31, 37, 0, 0, 0, 0, 43, 49, 55, 61, 67, 73, 79,
                0, 0, 2, 8, 14, 20, 26, 32, 38, 0, 0, 0, 0, 44, 50, 56, 62, 68, 74, 80,
                0, 0, 3, 9, 15, 21, 27, 33, 39, 0, 0, 0, 0, 45, 51, 57, 63, 69, 75, 81,
                0, 0, 4, 10, 16, 22, 28, 34, 40, 0, 0, 0, 0, 46, 52, 58, 64, 70, 76, 82,
                0, 0, 5, 11, 17, 23, 29, 35, 41, 0, 0, 0, 0, 47, 53, 59, 65, 71, 77, 83,
                0, 0, 6, 12, 18, 24, 30, 36, 42, 0, 0, 0, 0, 48, 54, 60, 66, 72, 78, 84};

    public static final double[] cableT0
            = {0, 0, 180, 180, 180, 180, 180, 180, 180, 0, 0, 0, 0, 180, 180, 180, 180, 180, 180, 180,
                0, 0, 180, 180, 180, 180, 180, 180, 180, 0, 0, 0, 0, 180, 180, 180, 180, 180, 180, 180,
                0, 0, 180, 180, 180, 180, 180, 180, 180, 0, 0, 0, 0, 180, 180, 180, 180, 180, 180, 180,
                0, 0, 180, 180, 180, 180, 180, 180, 180, 0, 0, 0, 0, 180, 180, 180, 180, 180, 180, 180,
                0, 0, 180, 180, 180, 180, 180, 180, 180, 0, 0, 0, 0, 180, 180, 180, 180, 180, 180, 180,
                0, 0, 180, 180, 180, 180, 180, 180, 180, 0, 0, 0, 0, 180, 180, 180, 180, 180, 180, 180};

    public DCTranslationTable() {

    }

    /**
     * crate : the crate number (41...58) slot : the slot number (4...10,
     * 13...19) channel : the channel on one DCRB (0...95)
     *
     * returns : the sector (1...6)
     * @param crate
     * @param slot
     * @param channel
     * @return 
     */
    public int getSector(int crate, int slot, int channel) {
        int crateIdx = crate - 41;
        return crate_sector[crateIdx];
    }

    /**
     * crate : the crate number (41...58) slot : the slot number (4...10,
     * 13...19) channel : the channel on one DCRB (0...95)
     *
     * returns : the layer (1...36)
     * @param crate
     * @param slot
     * @param channel
     * @return 
     */
    public int getLayer(int crate, int slot, int channel) {

//	System.out.println( " crate " + crate + " slot " + slot + " channel " + channel);
        int slotIdx = slot - 1;
        int crateIdx = crate - 41;
        int channelIdx = channel; // channel runs from 0 to 95

        int region = crate_region[crateIdx];
        int loclayer = chan_loclayer[channelIdx];
        int locsuplayer = slot_locsuplayer[slotIdx];
        int suplayer = (region - 1) * 2 + locsuplayer;
        int layer = (suplayer - 1) * 6 + loclayer;

        // System.out.println("   -->    region "+region + " loclayer " + loclayer + " locsuplayer " + locsuplayer + " suplayer " + suplayer+" layer "+layer);
        return layer;
        //return 1;
    }

    /**
     * crate : the crate number (41...58) slot : the slot number (4...10,
     * 13...19); channel : the channel on one DCRB (0...95)
     *
     * returns : the wire (1...112)
     */
    public Integer getComponent(int crate, int slot, int channel) {
        int channelIdx = channel; // channel runs from 0 to 95
        int slotIdx = slot - 1;
        int locwire = chan_locwire[channelIdx];
        int nstb = slot_stb[slotIdx];
        int wire = (nstb - 1) * 16 + locwire;
        return wire;
        //return 0;
    }
    public static DetectorCollection<DetectorDescriptor> reverseTable;
    static DetectorDescriptor desc;

    public void createInvertedTable() {
        reverseTable = new DetectorCollection<DetectorDescriptor>();

        DCTranslationTable tran = new DCTranslationTable();
        for (int crate = 41; crate <= 58; crate++) {
            for (int slot = 1; slot <= 20; slot++) {
                for (int channel = 0; channel < 96; channel++) {
                    int crateIdx = crate - 41;
                    int sector = tran.crate_sector[crateIdx];
                    int slotIdx = slot - 1;

                    int channelIdx = channel; // channel runs from 0 to 95

                    int region = tran.crate_region[crateIdx];
                    int loclayer = tran.chan_loclayer[channelIdx];
                    int locsuplayer = tran.slot_locsuplayer[slotIdx];
                    int suplayer = (region - 1) * 2 + locsuplayer;
                    int layer = (suplayer - 1) * 6 + loclayer;
                    // DetectorDescriptor desc = new DetectorDescriptor();
                    //	desc.setCrateSlotChannel(crateIdx, slotIdx, channelIdx);

                    int locwire = tran.chan_locwire[channelIdx];
                    int nstb = tran.slot_stb[slotIdx];
                    int wire = (nstb - 1) * 16 + locwire;

                    DetectorDescriptor desc = new DetectorDescriptor();
                    desc.setCrateSlotChannel(crate, slot, channel);

                    reverseTable.add(sector, layer, wire, desc);

                    int connector = (int) (channel / 16) + 1;
                    int icableID = (connector - 1) * 20 + slot - 1; //20 slots
                    //	 int cable_id = tran.cableid[icableID];
                    //	 double t0 = tran.cableT0[icableID];

                    if (sector <= 0 || layer <= 0 || wire <= 0) {
                        continue;
                    }

                    // System.out.println((crate)+"  "+(slot)+"  "+(channel)+" sector "+sector
                    //		  +" region "+region+" layer "+layer+" wire "+wire+" connector "+connector);
                }
            }
        }

        //reverseTable.get(sector,layer,component).getCrate();
    }

    public static void main(String arg[]) throws FileNotFoundException {

        DCTranslationTable tran = new DCTranslationTable();
        tran.createInvertedTable();
        /*
		 *  Crate number can be only 1-18 (1 crate per chamber)
		 *  Slot number can be only the following: (4-10 or 13-19)
		 *       Slots 1,2,3, 11, 12 and 20 are empty/unused
		 *       Slots 4-10 are for one superlayer, 13-19 is for the other
		 *  Channel # goes from 0 to 95, and we get the connector # as 
		 *    follows (see below too):  connector = (int)(Channel/16)+1; 
		 *       Channels 0-15 in a module/slot are connected through connector 1
		 *       Channels 16-31 in the same module/slot are connected through connector 2
		 *         and so on.
         */

        //PrintWriter pw = new PrintWriter(new File("/Users/ziegler/Workdir/Files/TranslationTables/DC/DC.table"));
        PrintWriter pw = new PrintWriter(new File("/Users/ziegler/Workdir/Files/TranslationTables/DC/DCtable2.txt.log"));

        int order = 0;
        for (int crate = 41; crate <= 58; crate++) {
            for (int slot = 1; slot <= 20; slot++) {
                for (int channel = 0; channel < 96; channel++) {
                    int crateIdx = crate - 41;
                    int sector = tran.crate_sector[crateIdx];
                    int slotIdx = slot - 1;

                    int channelIdx = channel; // channel runs from 0 to 95

                    int region = tran.crate_region[crateIdx];
                    int loclayer = tran.chan_loclayer[channelIdx];
                    int locsuplayer = tran.slot_locsuplayer[slotIdx];
                    int suplayer = (region - 1) * 2 + locsuplayer;
                    int layer = (suplayer - 1) * 6 + loclayer;
                    //  DetectorDescriptor desc = new DetectorDescriptor();
                    //	desc.setCrateSlotChannel(crateIdx, slotIdx, channelIdx);

                    int locwire = tran.chan_locwire[channelIdx];
                    int nstb = tran.slot_stb[slotIdx];
                    int wire = (nstb - 1) * 16 + locwire;
                    //  reverseTable.add(sector,layer,wire,desc);
                    int connector = (int) (channel / 16) + 1;
                    int icableID = (connector - 1) * 20 + slot - 1; //20 slots
                    int cable_id = tran.cableid[icableID];

                    if (sector <= 0 || layer <= 0 || wire <= 0) {
                        continue;
                    }

                    //pw.printf("DC\t\t %d\t\t %d\t\t %d\t\t %d\t\t %d\t\t %d\t\t %d\t\t\n", crate, slot, channel, sector, layer, wire, order);
                    pw.printf("%d\t %d\t %d\t %d\t %d\t %d\t %d\t\n", crate, slot, channel, sector, layer, wire, order);
                    System.out.println((crate) + "  " + (slot) + "  " + (channel) + " sector " + sector
                            + " region " + region + " layer " + layer + " wire " + wire + " connector " + connector + " " + " CH " + reverseTable.get(sector, layer, wire).getChannel());
                }
            }
        }
        pw.close();
        //reverseTable.get(sector,layer,component).getCrate();
    }

}
