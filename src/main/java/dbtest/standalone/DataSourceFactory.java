package dbtest.standalone;

import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @author Yoshimasa Tanabe
 */
public class DataSourceFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceFactory.class);
  private static DataSource ds = null;
  private static final String ERR_MESSAGE = "DataSource initialize failed. Please check dbcp.properties.";

  static {
    try(InputStream in = DataSourceFactory.class.getResourceAsStream("/dbcp.properties");
        Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
      Properties props = new Properties();
      props.load(reader);
      ds = BasicDataSourceFactory.createDataSource(props);
      LOGGER.info("DataSource initialized.");
    } catch (Exception e) {
      LOGGER.error(ERR_MESSAGE);
    }
  }

  public static DataSource getDataSource() {
    if (ds == null) {
      throw new IllegalStateException(ERR_MESSAGE);
    }
    return ds;
  }

}
