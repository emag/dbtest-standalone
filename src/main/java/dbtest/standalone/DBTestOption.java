package dbtest.standalone;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * @author Yoshimasa Tanabe
 */
public class DBTestOption {

  @Option(name = "-c", aliases = "--concurrency", metaVar = "<concurrency-number>", usage = "実行スレッド数(デフォルト 1)")
  private int concurrency = 1;

  @Option(name = "-n", aliases = "--requests", metaVar = "<request-times>", usage = "クエリ実行回数(デフォルト 1)")
  private int requests = 1;

  @Option(name = "-d", aliases = "--disable-clear", usage = "DB のデータを初期化しない(デフォルト false)")
  private boolean disableClear = false;

  @Option(name = "-s", aliases = "--sleep", metaVar = "<time>", usage = "次回クエリ実行前のスリープ時間。単位: μs (デフォルト 10000 μs)")
  private int sleep = 10_000;

  @Option(name = "-l", aliases = "--loop", usage = "ループモード(デフォルト false)")
  private boolean loop = false;

  public int getConcurrency() {
    return concurrency;
  }

  public int getRequests() {
    return requests;
  }

  public boolean isClear() {
    return ! disableClear;
  }

  public int getSleep() {
    return sleep;
  }

  public boolean isLoop() {
    return loop;
  }

  public static DBTestOption getOption(String... args) {
    DBTestOption option = new DBTestOption();
    CmdLineParser parser = new CmdLineParser(option);

    try {
      parser.parseArgument(args);
    } catch (CmdLineException e) {
      System.err.println("引数エラー: " + e.getMessage());
      parser.printUsage(System.err);
      System.exit(1);
    }

    return option;
  }

}
