package org.jlab.io.clara;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Path;

import org.jlab.clara.engine.EngineDataType;
import org.jlab.clara.std.services.AbstractEventReaderService;
import org.jlab.clara.std.services.EventReaderException;
import org.jlab.coda.jevio.EvioCompactReader;
import org.jlab.coda.jevio.EvioException;
import org.json.JSONObject;

/**
 * Converter service that converts EvIO persistent data to EvIO transient data
 * (i.e. Reads EvIO events from an input file)
 */
public class EvioToEvioReader extends AbstractEventReaderService<EvioCompactReader> {

    @Override
    protected EvioCompactReader createReader(Path file, JSONObject opts)
            throws EventReaderException {
        try {
            return new EvioCompactReader(file.toFile());
        } catch (EvioException | IOException e) {
            throw new EventReaderException(e);
        }
    }

    @Override
    protected void closeReader() {
        reader.close();
    }

    @Override
    public int readEventCount() throws EventReaderException {
        return reader.getEventCount();
    }

    @Override
    public ByteOrder readByteOrder() throws EventReaderException {
        return reader.getFileByteOrder();
    }

    @Override
    public Object readEvent(int eventNumber) throws EventReaderException {
        try {
            return reader.getEventBuffer(++eventNumber, true);
        } catch (EvioException e) {
            throw new EventReaderException(e);
        }
    }

    @Override
    protected EngineDataType getDataType() {
        return Clas12Types.EVIO;
    }
}
