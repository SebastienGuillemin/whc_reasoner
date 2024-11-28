package com.sebastienguillemin.whcreasoner.core.util;

import com.sebastienguillemin.whcreasoner.core.reasoner.Reasoner;

public class Logger {
    private static PropertiesReader propertiesReader = PropertiesReader.getInstance();
    public static final boolean ENABLE_LOG_INFO;
    public static final boolean ENABLE_LOG_INFERENCE;

    static  {
        ENABLE_LOG_INFO = propertiesReader.getPropertyValueBoolean("util.log.info");
        ENABLE_LOG_INFERENCE = propertiesReader.getPropertyValueBoolean("util.log.inference");

        System.out.println("[Util] LOG INFO IS " + ((ENABLE_LOG_INFO) ? "ENABLE." : "DISABLED."));
        System.out.println("[Util] LOG REASONER IS " + ((ENABLE_LOG_INFERENCE) ? "ENABLE." : "DISABLED."));
    }

    /**
     * Log message and indicates the caller.
     * 
     * @param message The message to log.
     */
    public static void log(Object message) {
        String completeClassName[] = Thread.currentThread().getStackTrace()[2].getClassName().split("\\.");
        String classCaller = completeClassName[completeClassName.length - 1];
        // String methodCaller =
        // Thread.currentThread().getStackTrace()[2].getMethodName();

        System.out.println("[" + classCaller + "] " + message);
    }

    /**
     * Log message and indicates the caller. Can be disabled using the 'util.log.info' in
     * property file.
     * 
     * @param message The message to log.
     */
    public static void logInfo(Object message) {
        if (!ENABLE_LOG_INFO)
            return;
        String completeClassName[] = Thread.currentThread().getStackTrace()[2].getClassName().split("\\.");
        String classCaller = completeClassName[completeClassName.length - 1];
        // String methodCaller =
        // Thread.currentThread().getStackTrace()[2].getMethodName();

        System.out.println("[" + classCaller + "] " + message);
    }

    /**
     * Log message and indicates the caller. Used in the reasoner inference method (prints
     * arrow depending on {@code depth} value). Can be disabled using the
     * 'util.log.reasoner' in property file.
     * 
     * @see Reasoner
     * 
     * @param message The message to log.
     * @param depth Reasiner depth where the method was called.
     */
    public static void logInference(Object message, int depth) {
        if (!ENABLE_LOG_INFERENCE)
            return;
        String completeClassName[] = Thread.currentThread().getStackTrace()[2].getClassName().split("\\.");
        String classCaller = completeClassName[completeClassName.length - 1];
        // String methodCaller =
        // Thread.currentThread().getStackTrace()[2].getMethodName();

        System.out.println("[" + classCaller + "] " + "--" + "--".repeat(2 * depth) + "> " + message);
    }
}
