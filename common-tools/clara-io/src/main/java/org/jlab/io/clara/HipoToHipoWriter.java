package org.jlab.io.clara;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.text.StringSubstitutor;

import org.jlab.clara.engine.EngineDataType;
import org.jlab.clara.std.services.AbstractEventWriterService;
import org.jlab.clara.std.services.EventWriterException;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.io.HipoWriter;
import org.jlab.jnp.hipo4.io.HipoWriterSorted;
import org.jlab.jnp.utils.file.FileUtils;
import org.json.JSONObject;

/**
 * Service that converts HIPO transient data to HIPO persistent data
 * (i.e. writes HIPO events to an output file).
 */
public class HipoToHipoWriter extends AbstractEventWriterService<HipoWriterSorted> {

    private static final String CONF_COMPRESSION = "compression";
    private static final String CONF_SCHEMA_DIR = "schema_dir";
    private static final String CONF_SCHEMA_FILTER = "schema_filter";
    private List<Bank>       schemaBankList = new ArrayList<Bank>();
    private final StringSubstitutor stringSub = new StringSubstitutor(System.getenv());


    @Override
    protected HipoWriterSorted createWriter(Path file, JSONObject opts) throws EventWriterException {
        try {
            HipoWriterSorted writer = new HipoWriterSorted();
            configure(writer, opts);
            writer.open(file.toString());
            return writer;
        } catch (Exception e) {
            throw new EventWriterException(e);
        }
    }

    private void configure(HipoWriterSorted writer, JSONObject opts) {
        schemaBankList.clear();
        if (opts.has(CONF_COMPRESSION)) {
            int compression = opts.getInt(CONF_COMPRESSION);
            System.out.printf("%s service: compression level = %d%n", getName(), compression);
            writer.setCompressionType(compression);
        }

        String schemaDir = FileUtils.getEnvironmentPath("CLAS12DIR", "etc/bankdefs/hipo4");
        if (opts.has(CONF_SCHEMA_DIR)) {
            schemaDir = opts.getString(CONF_SCHEMA_DIR);
            schemaDir = stringSub.replace(schemaDir);
            System.out.printf("%s service: schema directory = %s%n", getName(), schemaDir);
        }
        writer.getSchemaFactory().initFromDirectory(schemaDir);

        if (opts.has(CONF_SCHEMA_DIR)) {
            //try {
                // previous releases of COATJAVA may not have the setter
                //Method filterSetter = getSchemaFilterSetter();
                boolean useFilter = opts.optBoolean(CONF_SCHEMA_FILTER, true);
                System.out.printf("%s service: schema filter = %b%n", getName(), useFilter);
                //filterSetter.invoke(writer, useFilter);
                
                if(useFilter==true){
                    int schemaSize = writer.getSchemaFactory().getSchemaList().size();
                    for(int i = 0; i < schemaSize; i++){
                        Bank dataBank = new Bank(writer.getSchemaFactory().getSchemaList().get(i));
                        schemaBankList.add(dataBank);
                    }
                }
           /* } catch (NoSuchMethodException | IllegalAccessException
                        | IllegalArgumentException | InvocationTargetException e) {
                System.out.printf("%s service: schema filter not supported%n", getName());
            }*/
        }
    }

    private Method getSchemaFilterSetter() throws NoSuchMethodException, SecurityException {
        return HipoWriter.class.getMethod("setSchemaFilter", boolean.class);
    }

    @Override
    protected void closeWriter() {
        writer.close();
        schemaBankList.clear();
    }

    @Override
    protected void writeEvent(Object event) throws EventWriterException {
        try {
            Event hipoEvent = (Event) event;
            int   eventTag  = hipoEvent.getEventTag();
            
            if(eventTag == 1){
                writer.addEvent( hipoEvent,eventTag);
            } else {
                if(schemaBankList.size()>0){
                    Event reduced = hipoEvent.reduceEvent(schemaBankList);
                    writer.addEvent( reduced,eventTag);
                } else {
                    writer.addEvent( hipoEvent,eventTag);
                }
            }
        } catch (Exception e) {
            throw new EventWriterException(e);
        }
    }

    @Override
    protected EngineDataType getDataType() {
        return Clas12Types.HIPO;
    }
}
