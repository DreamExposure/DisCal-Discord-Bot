package org.dreamexposure.discal.core.logger.object;

import org.dreamexposure.discal.core.logger.enums.LogType;

import java.text.SimpleDateFormat;

import javax.annotation.Nullable;

public class LogObject {
    //static initializers
    public static LogObject forException(String message, Throwable exception, Class<?> clazz) {
        return new LogObject(LogType.EXCEPTION, message, null, clazz, exception);
    }

    public static LogObject forException(String message, String info, Throwable e, Class<?> clazz) {
        return new LogObject(LogType.EXCEPTION, message, info, clazz, e);
    }

    public static LogObject forDebug(String message) {
        return new LogObject(LogType.DEBUG, message, null, null, null);
    }

    public static LogObject forDebug(String message, String info) {
        return new LogObject(LogType.DEBUG, message, info, null, null);
    }

    public static LogObject forStatus(String message) {
        return new LogObject(LogType.STATUS, message, null, null, null);
    }

    public static LogObject forStatus(String message, String info) {
        return new LogObject(LogType.STATUS, message, info, null, null);
    }

    //nonnull variables
    private final LogType type;
    private final String message;
    private final long time;

    //nullable variables
    @Nullable
    private final String info;
    @Nullable
    private final Class<?> clazz;
    @Nullable
    private final Throwable exception;


    private LogObject(LogType type, String message, @Nullable String info, @Nullable Class<?> clazz,
                      @Nullable Throwable exception) {
        this.type = type;
        this.message = message;
        this.time = System.currentTimeMillis();

        this.info = info;
        this.clazz = clazz;
        this.exception = exception;
    }

    //Getters
    public LogType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public long getTime() {
        return time;
    }

    @Nullable
    public String getInfo() {
        return info;
    }

    @Nullable
    public Class<?> getClazz() {
        return clazz;
    }

    @Nullable
    public Throwable getException() {
        return exception;
    }

    public String getTimestamp() {
        return new SimpleDateFormat("dd-MM-yyyy-hh.mm.ss").format(time);
    }
}
