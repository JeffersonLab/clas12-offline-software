package org.jlab.logging;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Read the default logging configuration and load it into the global log manager.
 *
 * @author Nick Tyler, UofSC
 *
 * Modified from hps-java logging
 * @author Jeremy McCormick, SLAC
 */
public class DefaultLogger {

    private static String LOG_PROP = "logging.properties";

    /**
     * Class constructor which reads in a logging properties file from a classpath resource.
     */
    public DefaultLogger() {
        InputStream inputStream = DefaultLogger.class.getResourceAsStream(LOG_PROP);
        try {
            LogManager.getLogManager().readConfiguration(inputStream);
            Logger.getLogger(DefaultLogger.class.getName()).log(Level.INFO,"Reading default logging config from " + LOG_PROP);
        } catch (SecurityException | IOException e) {
            throw new RuntimeException("Initialization of default logging configuration failed.", e);
        }
    }

    /**
     * Initialize default logging if java system properties are not set.
     */
    public static void initialize() {
        String clas12_logger = System.getenv("CLAS12_LOGGER");
        if(clas12_logger != null)
            System.setProperty("java.util.logging.config.file", clas12_logger);

        if (System.getProperty("java.util.logging.config.file") == null) {
            // Config is only read in if there is not an externally set class or file already.
            new DefaultLogger();
        }
    }

    public static void debug() {
        LOG_PROP = "debug.properties";
        if (System.getProperty("java.util.logging.config.file") == null) {
            // Config is only read in if there is not an externally set class or file already.
            new DefaultLogger();
        }
    }

    public static void main(String[] args) {
        DefaultLogger.initialize();
        Logger.getLogger(DefaultLogger.class.getName()).log(Level.INFO,"Info");
        Logger.getLogger(DefaultLogger.class.getName()).log(Level.WARNING,"Warning");
        Logger.getLogger(DefaultLogger.class.getName()).log(Level.SEVERE,"Severe");
    }
}