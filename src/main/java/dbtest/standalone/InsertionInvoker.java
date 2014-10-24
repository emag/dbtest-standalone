package dbtest.standalone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @author Yoshimasa Tanabe
 */
public class InsertionInvoker implements Callable<Integer> {

  private static final Logger LOGGER = LoggerFactory.getLogger(InsertionInvoker.class.getName());
  private static final String STATEMENT = "INSERT INTO test_table (id, value) VALUES (?, ?)";

  private int sleep;

  public InsertionInvoker(int sleep) {
    this.sleep = sleep;
  }

  @Override
  public Integer call() throws Exception {
    Integer insertedRowNum = 0;

    try (Connection con = DataSourceFactory.getDataSource().getConnection()) {
      PreparedStatement ps = con.prepareStatement(STATEMENT);
      ps.setInt(1, App.countTotal.get());
      ps.setInt(2, App.countSuccess.get());
      insertedRowNum = ps.executeUpdate();

      App.countTotal.incrementAndGet();
      App.countSuccess.incrementAndGet();

      LOGGER.info("[Insert success] COUNT_TOTAL: {}, COUNT_SUCCESS: {}, COUNT_FAILURE: {}",
        App.countTotal.get(), App.countSuccess.get(), App.countFailure);
    } catch (SQLException e) {
      e.printStackTrace();

      App.countTotal.incrementAndGet();
      App.countFailure.incrementAndGet();

      LOGGER.error("[Insert failure] COUNT_TOTAL: {}, COUNT_SUCCESS: {}, COUNT_FAILURE: {}, CAUSE: {}",
        App.countTotal.get(), App.countSuccess.get(), App.countFailure, e.getMessage());
    }

    TimeUnit.MICROSECONDS.sleep(this.sleep);

    return insertedRowNum;
  }
}
