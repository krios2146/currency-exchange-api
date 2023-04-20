package pet.project.utils;

import org.postgresql.ds.PGSimpleDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class ConfiguredDataSource {
    private static final PGSimpleDataSource INSTANCE = new PGSimpleDataSource();

    static {
        String url = System.getenv("DB_URL");
        String username = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");

        if (url == null || username == null || password == null) {
            try (InputStream input = ConfiguredDataSource.class.getResourceAsStream("/db.properties")) {
                Properties prop = new Properties();
                prop.load(input);

                url = prop.getProperty("db.url");
                username = prop.getProperty("db.username");
                password = prop.getProperty("db.password");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        INSTANCE.setURL(url);
        INSTANCE.setUser(username);
        INSTANCE.setPassword(password);
    }

    private ConfiguredDataSource() {
    }

    public static PGSimpleDataSource getInstance() {
        return INSTANCE;
    }
}
