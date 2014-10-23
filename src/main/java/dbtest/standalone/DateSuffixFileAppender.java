package dbtest.standalone;

import ch.qos.logback.core.FileAppender;

import java.time.LocalDateTime;

/**
 * @author Yoshimasa Tanabe
 */
public class DateSuffixFileAppender extends FileAppender {

  public DateSuffixFileAppender() {
    super();
  }

  @Override
  public String getFile() {
    return super.fileName.replaceAll("###", LocalDateTime.now().toString());
  }

}
