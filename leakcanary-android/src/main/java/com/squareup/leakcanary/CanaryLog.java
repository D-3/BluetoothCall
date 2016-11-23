package com.squareup.leakcanary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class CanaryLog {

    private static final String TAG = "Logger";

    private static CanaryLog instance;

    public static boolean DEBUG_ENABLE = true;
    public static boolean FILE_ENABLE = true;

    private static String FILE_PATH = StorageUtil.getSDCardPath();
    private static final int QUEUE_SIZE = 9999;
    private File nLogFile;

    private BlockingQueue<String> nWaitLogs;

    private static String LOGDIR = "leakcanary";

    private WriterThread nLogThread;

    private static volatile Logger logger = new DefaultLogger();

    private CanaryLog() {
        CharSequence time = DateFormat.format("yyyy-MM-dd", System.currentTimeMillis());
        File logDir = new File(FILE_PATH);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        nLogFile = new File(logDir, time + ".txt");

        nWaitLogs = new ArrayBlockingQueue<String>(QUEUE_SIZE, true);

        nLogThread = new WriterThread(nWaitLogs);

        nLogThread.start();

    }

    public static void initFile(Context context) {
        if (!FILE_ENABLE || !DEBUG_ENABLE) {
            return;
        }

        if (StorageUtil.hasSDCard()) {
            FILE_PATH = StorageUtil.getSDCardPath();
        } else {
            FILE_PATH = StorageUtil.getApplicationPath(context);
        }

        FILE_PATH = FILE_PATH.endsWith("/") ? FILE_PATH + LOGDIR + File.separator : FILE_PATH + File.separator + LOGDIR + File.separator;
        String[] splitPackName = context.getPackageName().split("\\.");
        if (splitPackName != null && splitPackName.length > 0) {
            FILE_PATH = FILE_PATH + splitPackName[splitPackName.length - 1] + File.separator;
        }
        close();
        Log.w("Log", "FILE_PATH = " + FILE_PATH);
        instance = new CanaryLog();
    }

    private static void close() {
        if (instance != null) {
            instance.releaseRes();
        }
    }

    private void releaseRes() {
        if (nLogThread != null) {
            nLogThread.shutdown();
        }
        if (nWaitLogs != null) {
            nWaitLogs.clear();
        }
    }


    private void logToFile(String str) {
        if (nWaitLogs != null) {
            try {
                nWaitLogs.put(str);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private final static void logToFile(String level, String tag, String msg) {
        if (!FILE_ENABLE)
            return;

        if(instance != null){
            Date date = new Date();
            String d = formatDate(date);

            StringBuffer sb = new StringBuffer();
            sb.append(d).append(" ").append(level).append("  ").append(tag)
                    .append("  ").append(msg).append("  ").append("\n");

            instance.logToFile(sb.toString());
        }
    }

    @SuppressLint("SimpleDateFormat")
    private final static String formatDate(Date d) {
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss.SSS");
        return format.format(d);
    }

    public interface Logger {
        void d(String message, Object... args);

        void d(Throwable throwable, String message, Object... args);
    }

    private static class DefaultLogger implements Logger {
        DefaultLogger() {
        }

        @Override
        public void d(String message, Object... args) {
            String formatted = String.format(message, args);
            if (formatted.length() < 4000) {
                Log.d("LeakCanary",formatted);
                logToFile("DEBUG","LeakCanary",formatted);
            } else {
                String[] lines = formatted.split("\n");
                StringBuffer stringBuffer=new StringBuffer();
                for (String line : lines) {
                    Log.d("LeakCanary", line);
                    stringBuffer.append(line+"\n");
                }
                logToFile("DEBUG","LeakCanary",stringBuffer.toString());
            }
        }

        @Override
        public void d(Throwable throwable, String message, Object... args) {
            d(String.format(message, args) + '\n' + Log.getStackTraceString(throwable));
        }
    }

    public static void setLogger(Logger logger) {
        CanaryLog.logger = logger;
    }

    public static void d(String message, Object... args) {
        // Local variable to prevent the ref from becoming null after the null check.
        Logger logger = CanaryLog.logger;
        if (logger == null) {
            return;
        }
        logger.d(message, args);
    }

    public static void d(Throwable throwable, String message, Object... args) {
        // Local variable to prevent the ref from becoming null after the null check.
        Logger logger = CanaryLog.logger;
        if (logger == null) {
            return;
        }
        logger.d(throwable, message, args);
    }

    //  private CanaryLog() {
//    throw new AssertionError();
//  }
    private static String getLocation() {
        final String className = Logger.class.getName();
        final StackTraceElement[] traces = Thread.currentThread().getStackTrace();

        boolean found = false;

        for (StackTraceElement trace : traces) {
            try {
                if (found) {
                    if (!trace.getClassName().startsWith(className)) {
                        Class<?> clazz = Class.forName(trace.getClassName());
                        return "[" + getClassName(clazz) + "; " + trace.getMethodName() + "; " + trace.getLineNumber() + "; ThreadName:" + Thread.currentThread().getName() + "; ThreadId:" + Thread.currentThread().getId() + "]: ";
                    }
                } else if (trace.getClassName().startsWith(className)) {
                    found = true;
                }
            } catch (ClassNotFoundException ignored) {
            }
        }

        return "[]: ";
    }
    private static String getClassName(Class<?> clazz) {
        if (clazz != null) {
            if (!TextUtils.isEmpty(clazz.getSimpleName())) {
                return clazz.getSimpleName();
            }

            return getClassName(clazz.getEnclosingClass());
        }

        return "";
    }

    private class WriterThread extends Thread {
        BlockingQueue<String> nnContentQueue;
        boolean nnIsShutdown;

        public WriterThread(final BlockingQueue<String> bq) {
            nnContentQueue = bq;
            nnIsShutdown = true;
        }

        @Override
        public void run() {
            nnIsShutdown = false;
            String logContent = null;

            while (!nnIsShutdown) {
                // 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
                FileWriter fileWriter = null;
                BufferedWriter bufferedWriter = null;
                try {
                    fileWriter = new FileWriter(nLogFile, true);
                    bufferedWriter = new BufferedWriter(fileWriter);
                    logContent = nnContentQueue.take() + "\n";
                    if (logContent != null && bufferedWriter != null) {
                        bufferedWriter.write(logContent);
                        bufferedWriter.flush();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    StorageUtil.closeSilently(bufferedWriter);
                }
            }
        }

        public void shutdown() {
            nnIsShutdown = true;
        }
    }
}
