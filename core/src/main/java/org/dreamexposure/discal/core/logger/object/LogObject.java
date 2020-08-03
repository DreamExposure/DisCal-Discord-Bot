package org.dreamexposure.discal.core.logger.object;

import org.dreamexposure.discal.core.logger.enums.LogType;

import java.text.SimpleDateFormat;

import javax.annotation.Nullable;

public class LogObject {
    //static initializers
    public static LogObject forException(final String message, final Throwable exception, final Class<?> clazz) {
        return new LogObject(LogType.EXCEPTION, message, null, clazz, exception);
    }

    public static LogObject forException(final String message, final String info, final Throwable e, final Class<?> clazz) {
        return new LogObject(LogType.EXCEPTION, message, info, clazz, e);
    }

    public static LogObject forDebug(final String message) {
        return new LogObject(LogType.DEBUG, message, null, null, null);
    }

    public static LogObject forDebug(final String message, final String info) {
        return new LogObject(LogType.DEBUG, message, info, null, null);
    }

    public static LogObject forStatus(final String message) {
        return new LogObject(LogType.STATUS, message, null, null, null);
    }

    public static LogObject forStatus(final String message, final String info) {
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


    private LogObject(final LogType type, final String message, @Nullable final String info, @Nullable final Class<?> clazz,
                      @Nullable final Throwable exception) {
        this.type = type;
        this.message = message;
        this.time = System.currentTimeMillis();

        this.info = info;
        this.clazz = clazz;
        this.exception = exception;
    }

    //Getters
    public LogType getType() {
        return this.type;
    }

    public String getMessage() {
        return this.message;
    }

    public long getTime() {
        return this.time;
    }

    @Nullable
    public String getInfo() {
        return this.info;
    }

    @Nullable
    public Class<?> getClazz() {
        return this.clazz;
    }

    @Nullable
    public Throwable getException() {
        return this.exception;
    }

    public String getTimestamp() {
        return new SimpleDateFormat("dd-MM-yyyy-hh.mm.ss").format(this.time);
    }
}
