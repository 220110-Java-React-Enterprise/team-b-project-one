package utils;

import java.io.FileWriter;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * FileLogger - Singleton used to log exceptions and other info to a file or maybe
 * another data source.
 * Interface includes: 2 methods to get the singleton, and 2 methods to input info to be logged
 */
public class FileLogger {
    /*
    generic exception logger, .txt file written when exceptions are thrown
    logs only log(Exception e)
    log(String message) also implemented but never used here
     */
    private static FileLogger fileLogger;
    private static String filePath;
    private static boolean consoleOutput;
    private static int stackTraceSize;



    private FileLogger() {
        filePath = "C:\\Users\\majaw\\IdeaProjects\\repos\\team-b-web-app\\logs";
        consoleOutput = false;
        stackTraceSize = 10;
    }

    public static FileLogger getFileLogger() {
        if(fileLogger == null) {
            fileLogger = new FileLogger();
        }
        return fileLogger;
    }

    public static FileLogger getFileLogger(String path) {
        if(fileLogger == null) {
            fileLogger = new FileLogger();
        }
        filePath = path;
        return fileLogger;
    }


    public void log(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(fileLogger.getTimestamp())
                .append(" - ")
                .append(e.getMessage())
                .append("\n")
                .append(fileLogger.formatStackTrace(e))
                .append("\n");
        fileLogger.writeToLog(sb.toString());
    }

    public void log(String str) {
        StringBuilder sb = new StringBuilder();
        sb.append(fileLogger.getTimestamp())
                .append(" - ")
                .append(str)
                .append("\n");
        fileLogger.writeToLog(sb.toString());
    }

    private String getTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("[yyyy-MM-dd HH:mm:ss]");
        return formatter.format(LocalDateTime.now());
    }

    private String getFileName() {
        return filePath + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".log";
    }

    private String formatStackTrace(Exception e) {
        StackTraceElement[] stackTrace = e.getStackTrace();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < stackTraceSize; i++) {
            if(i >= stackTrace.length) {
                break;
            }
            sb.append("\t");
            sb.append(stackTrace[i]);
            sb.append("\n");
        }
        return sb.toString();
    }

    private void writeToLog(String text){
        String fileName = getFileName();
        try(Writer fileWriter = new FileWriter(fileName, true)) {
            fileWriter.write(text);
        } catch (Exception e) {
            e.printStackTrace();
            //later
        }

    }

}
