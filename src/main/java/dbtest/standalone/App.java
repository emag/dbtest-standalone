package dbtest.standalone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.Executors.*;

/**
 * @author Yoshimasa Tanabe
 */
public class App {

  private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
  private static final String TRUNCATE = "TRUNCATE test_table;";
  private static final String ACTUAL_NUMBER_OF_RECORDS = "SELECT count(*) FROM test_table;";

  static final AtomicInteger countTotal = new AtomicInteger(0);
  static final AtomicInteger countSuccess = new AtomicInteger(0);
  static final AtomicInteger countFailure = new AtomicInteger(0);
  static final AtomicInteger countViaJdbc = new AtomicInteger(0);

  public static void main(String[] args) {
    DBTestOption option = DBTestOption.getOption(args);
    Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook(), "post-processor"));

    LOGGER.info("[DBTest begin]");
    LOGGER.info("sleep time: {} μs", option.getSleep());
    if (option.isClear()) {
      clearData();
    }

    if (option.isLoop()) {
      LOGGER.info("loop mode: Please press Ctrl+C for stopping");
      invokeLoop(option);
    } else {
      LOGGER.info("request times: {}", option.getRequests());
      invoke(option);
    }

  }

  private static void invokeLoop(DBTestOption option) {
    ExecutorService pool = newFixedThreadPool(option.getConcurrency());

    while (true) {
      Future<Integer> insertedRow = pool.submit(new InsertionInvoker(option.getSleep()));
      try {
        countViaJdbc.addAndGet(insertedRow.get());
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    }
  }

  public static void invoke(DBTestOption option) {
    ExecutorService pool = newFixedThreadPool(option.getConcurrency());
    List<Callable<Integer>> tasks = new ArrayList<>();

    for (int i = 0; i < option.getRequests(); i++) {
      InsertionInvoker task = new InsertionInvoker(option.getSleep());
      tasks.add(task);
    }

    List<Future<Integer>> result = null;
    try {
      result = pool.invokeAll(tasks);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    pool.shutdown();

    countViaJdbc.addAndGet(result.stream().map(f -> {
      int i = 0;
      try {
        i = f.get();
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
      return i;
    }).reduce(0, (x, y) -> x + y));
  }

  private static void clearData() {
    try (Connection con = DataSourceFactory.getDataSource().getConnection()){
      PreparedStatement ps = con.prepareStatement(TRUNCATE);
      ps.executeUpdate();
      LOGGER.info("Data cleared");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private static int getActualNumberOfRecords() {
    int actualNumberOfRecords = -1;

    try (Connection con = DataSourceFactory.getDataSource().getConnection()){
      PreparedStatement ps = con.prepareStatement(ACTUAL_NUMBER_OF_RECORDS);
      ResultSet resultSet = ps.executeQuery();
      while (resultSet.next()) {
        actualNumberOfRecords = resultSet.getInt(1);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return actualNumberOfRecords;
  }

  static class ShutdownHook implements Runnable {

    @Override
    public void run() {
      LOGGER.info("Insert success: {}", countSuccess.get());
      LOGGER.info("Insert failure: {}", countFailure.get());
      LOGGER.info("Insert total: {}", countTotal.get());
      LOGGER.info("Count Via JDBC: {}", countViaJdbc);
      LOGGER.info("Actual number of records: {}", getActualNumberOfRecords());
      LOGGER.info("[DBTest end]");
    }
  }
}
