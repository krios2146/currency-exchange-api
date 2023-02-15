package pet.project;

import org.postgresql.ds.PGSimpleDataSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Utils {
    public static PGSimpleDataSource getConfiguredDataSource() {
        try (InputStream input = new FileInputStream("src/main/resources/db.properties")) {
            Properties prop = new Properties();

            prop.load(input);

            PGSimpleDataSource dataSource = new PGSimpleDataSource();
            dataSource.setURL(prop.getProperty("db.url"));
            dataSource.setUser(prop.getProperty("db.username"));
            dataSource.setPassword(prop.getProperty("db.password"));

            return dataSource;

        } catch (IOException e) {
            return null;
        }
    }
}
