package pet.project.utils;

import org.postgresql.ds.PGSimpleDataSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class ConfiguredDataSource {
    private static final PGSimpleDataSource INSTANCE = new PGSimpleDataSource();

    static {
        try (InputStream input = new FileInputStream("src/main/resources/db.properties")) {
            Properties prop = new Properties();
            prop.load(input);

            INSTANCE.setURL(prop.getProperty("db.url"));
            INSTANCE.setUser(prop.getProperty("db.username"));
            INSTANCE.setPassword(prop.getProperty("db.password"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ConfiguredDataSource() {
    }

    public static PGSimpleDataSource getInstance() {
        return INSTANCE;
    }
}
