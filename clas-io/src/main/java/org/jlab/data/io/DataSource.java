package org.jlab.data.io;

import java.io.File;
import java.nio.ByteBuffer;

public interface DataSource {
    
    boolean hasEvent();
    void open(File file);
    void open(String filename);
    void open(ByteBuffer buff);
    void close();
    int getSize();
    DataEventList getEventList(int start, int stop);
    DataEventList getEventList(int nrecords);
    DataEvent     getNextEvent();
    DataEvent     getPreviousEvent();
    DataEvent     gotoEvent(int index);
    void reset();
    int getCurrentIndex();
}
