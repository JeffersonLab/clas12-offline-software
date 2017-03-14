package org.jlab.clas.reco;

import java.nio.ByteBuffer;

import org.jlab.clara.base.error.ClaraException;
import org.jlab.clara.engine.ClaraSerializer;
import org.jlab.clara.engine.EngineDataType;
import org.jlab.clas12.tools.MimeType;
import org.jlab.clas12.tools.property.JPropertyList;
import org.jlab.hipo.data.HipoEvent;

public final class Clas12Types {

	private Clas12Types() {}

        private static class HipoSerializer implements ClaraSerializer {

        @Override
        public ByteBuffer write(Object data) throws ClaraException {
            HipoEvent event = (HipoEvent) data;
            return ByteBuffer.wrap(event.getDataBuffer());
        }

        @Override
        public Object read(ByteBuffer buffer) throws ClaraException {
            return new HipoEvent(buffer.array());
        }
    }
        
	public static final EngineDataType EVIO = new EngineDataType(MimeType.EVIO.type(), EngineDataType.BYTES.serializer());

        public static final EngineDataType HIPO =
                    new EngineDataType("binary/data-hipo", new HipoSerializer());
        
	public static final EngineDataType PROPERTY_LIST = new EngineDataType(MimeType.PROPERTY_LIST.type(), new ClaraSerializer() {

		public ByteBuffer write(Object data) throws ClaraException {
			JPropertyList pl = (JPropertyList) data;
			return ByteBuffer.wrap(pl.getStringRepresentation(true).getBytes());
		}

		public Object read(ByteBuffer buffer) throws ClaraException {
			return new JPropertyList(new String(buffer.array()));
		}
	});
}
