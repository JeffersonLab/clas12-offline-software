package org.jlab.io.clara;

import org.jlab.clara.base.error.ClaraException;
import org.jlab.clara.engine.ClaraSerializer;
import org.jlab.clara.engine.EngineDataType;
import org.jlab.jnp.hipo4.data.Event;

import java.nio.ByteBuffer;

// TODO: put this in a common CLAS package
// TODO: should bytes be copied?
public final class Clas12Types {

    private Clas12Types() { }

    private static class HipoSerializer implements ClaraSerializer {

        @Override
        public ByteBuffer write(Object data) throws ClaraException {
            Event event = (Event) data;
            return ByteBuffer.wrap(event.getEventBuffer().array());
        }

        @Override
        public Object read(ByteBuffer buffer) throws ClaraException {
            Event event = new Event(buffer.array().length);
            event.initFrom(buffer.array());
            return event;
        }
    }

    public static final EngineDataType EVIO =
            new EngineDataType("binary/data-evio", EngineDataType.BYTES.serializer());

    public static final EngineDataType HIPO =
            new EngineDataType("binary/data-hipo", new HipoSerializer());
}
