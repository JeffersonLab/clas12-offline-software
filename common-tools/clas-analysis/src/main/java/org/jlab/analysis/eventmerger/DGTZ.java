package org.jlab.analysis.eventmerger;

import java.util.List;
import org.jlab.detector.banks.RawBank;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author devita
 */
public class DGTZ extends DetectorDescriptor {
    
    private boolean background = false;
    private boolean outoftime = false;
    private boolean removed = false;

    public DGTZ(DetectorType detector) {
        super(detector);
    }

    public DGTZ(DetectorType detector, byte sector, byte layer, short component, byte order) {
        super(detector);
        this.setSectorLayerComponent(sector, layer, component);
        this.setOrder(order);
        this.removed    = false;
        this.background = false;
    }

    public boolean isGood() {
        return true;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void remove() {
        this.removed = true;
    }

    public boolean isBackground() {
        return background;
    }

    public void markAsBackground() {
        this.background = true;
    }

    public boolean isOutofTime() {
        return outoftime;
    }

    public void markAsOutOfTime() {
        this.outoftime = true;
    }

    public boolean write() {
        return !(this.isBackground() && this.isRemoved()) && !this.isOutofTime();
//        return !(this.isRemoved());
    }
    
    public RawBank.OrderType getOrderType() {
        if(this.isBackground())
            return RawBank.OrderType.BGADDED_NOMINAL;
        else if(this.isRemoved())
            return RawBank.OrderType.BGREMOVED;
        else
            return RawBank.OrderType.NOMINAL;

    }

    public int getLabeledOrder() {
        return this.getOrder()+this.getOrderType().getTypeId();
    }

    public void addToBank(DataBank bank, int row) {
        bank.setByte("sector",     row, (byte)  this.getSector());
        bank.setByte("layer",      row, (byte)  this.getLayer());
        bank.setShort("component", row, (short) this.getComponent());
        bank.setByte("order",      row, (byte)  this.getLabeledOrder()); 
    }

    public void readFromBank(DataBank bank, int row) {
        byte sector     = bank.getByte("sector",     row);
        byte layer      = bank.getByte("layer",      row);
        short component = bank.getShort("component", row);
        byte order      = bank.getByte("order",      row);
        this.setSectorLayerComponent(sector, layer, component);
        this.setOrder(order);
    }

    public static final DataBank writeBank(DataEvent event, String name, List<DGTZ> dgtzs) {
        int size = 0;
        for(DGTZ dgtz : dgtzs) {
            if(dgtz.write()) size++;
        }
        DataBank bank = event.createBank(name, size);
        int row = 0;
        for (int i = 0; i < dgtzs.size(); i++) {
            if(dgtzs.get(i).write()) 
                dgtzs.get(i).addToBank(bank, row++);
        }
        return bank;
    }
    
    public boolean pilesUp(DGTZ o){
        return  this.getSector()    == o.getSector()    &&
                this.getLayer()     == o.getLayer()     &&
                this.getComponent() == o.getComponent() &&
                this.getOrder()     == o.getOrder();
    }

    public boolean inWindow() {
        return true;
    }
    
    @Override
    public String toString() {
        String s = "Sector/Layer/Component/Order: " + this.getSector() + 
                                                "/" + this.getLayer() + 
                                                "/" + this.getComponent() + 
                                                "/" + this.getOrder();
        s += " Good/Bg/Rm:" +this.isGood() + "/" + this.isBackground() + "/" + this.isRemoved();
        return s;
    }
}
