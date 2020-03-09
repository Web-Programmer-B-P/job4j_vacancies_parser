package ru.job4j.parser.property;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Class Property
 *
 * @author Petr B.
 * @since 26.11.2019, 10:06
 */
public class Property {
    private static final Logger LOG = LogManager.getLogger(Property.class.getName());
    private Properties config;

    public void init() {
        try (InputStream in = Property.class.getClassLoader().getResourceAsStream("app.properties")) {
            config = new Properties();
            if (in != null) {
                config.load(in);
            }
        } catch (IOException ioe) {
            LOG.trace(ioe.getMessage());
        }
    }

    public String getPropertyDriver() {
        return config.getProperty("jdbc.driver");
    }

    public String getPropertyUrl() {
        return config.getProperty("jdbc.url");
    }

    public String getPropertyUserName() {
        return config.getProperty("jdbc.username");
    }

    public String getPropertyUserPassword() {
        return config.getProperty("jdbc.password");
    }

    public String getPropertyCronTime() {
        return config.getProperty("cron.time");
    }
}
