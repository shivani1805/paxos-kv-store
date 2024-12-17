package paxos;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

  private String LOG_FILE ;
  public Utils(String LOG_FILE){
    this.LOG_FILE=LOG_FILE;
  }
  public static void log(String LOG_FILE,String message) {
    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
    String logMessage = "[" + timestamp + "] " + message;
    System.out.println(logMessage);

    try (FileWriter logFile = new FileWriter(LOG_FILE, true)) {
      logFile.write(logMessage + "\n");
    } catch (IOException e) {
      System.err.println("Failed to log to file: " + e.getMessage());
    }
  }

  public void log(String message) {
    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
    String logMessage = "[" + timestamp + "] " + message;
    System.out.println(logMessage);

    try (FileWriter logFile = new FileWriter(LOG_FILE, true)) {
      logFile.write(logMessage + "\n");
    } catch (IOException e) {
      System.err.println("Failed to log to file: " + e.getMessage());
    }
  }
}

