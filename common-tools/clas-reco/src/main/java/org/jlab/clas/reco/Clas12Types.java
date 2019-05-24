package org.jlab.clas.reco;

import java.nio.ByteBuffer;

import org.jlab.clara.base.error.ClaraException;
import org.jlab.clara.engine.ClaraSerializer;
import org.jlab.clara.engine.EngineDataType;
import org.jlab.clas12.tools.MimeType;
import org.jlab.clas12.tools.property.JPropertyList;
import org.jlab.jnp.hipo4.data.Event;


public final class Clas12Types {

	private Clas12Types() {}

        private static class HipoSerializer implements ClaraSerializer {

        @Override
        public ByteBuffer write(Object data) throws ClaraException {
            Event event = (Event) data;
            return event.getEventBuffer();
        }

        @Override
        public Object read(ByteBuffer buffer) throws ClaraException {
            Event event = new Event(buffer.array().length);
            event.initFrom(buffer.array());
            return event;
        }
    }
        
	public static final EngineDataType EVIO = new EngineDataType(MimeType.EVIO.type(), EngineDataType.BYTES.serializer());

        public static final EngineDataType HIPO =
                    new EngineDataType("binary/data-hipo", new HipoSerializer());
        
	public static final EngineDataType PROPERTY_LIST = new EngineDataType(MimeType.PROPERTY_LIST.type(), new ClaraSerializer() {

                @Override
		public ByteBuffer write(Object data) throws ClaraException {
			JPropertyList pl = (JPropertyList) data;
			return ByteBuffer.wrap(pl.getStringRepresentation(true).getBytes());
		}

                @Override
		public Object read(ByteBuffer buffer) throws ClaraException {
			return new JPropertyList(new String(buffer.array()));
		}
	});
}
