package org.dreamexposure.discal.core.logger.loggers;

import org.dreamexposure.discal.core.logger.interfaces.Logger;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.utils.GlobalVal;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

@SuppressWarnings({"ConstantConditions", "resource", "IOResourceOpenedButNotSafelyClosed"})
public class FileLogger implements Logger {
    private final String exceptionFile;
    private final String debugFile;

    public FileLogger(final String exceptionFile, final String debugFile) {
        this.exceptionFile = exceptionFile;
        this.debugFile = debugFile;
    }

    @Override
    public void write(final LogObject log) {
        switch (log.getType()) {
            case EXCEPTION:
                this.writeException(log);
                break;
            case DEBUG:
                this.writeDebug(log);
                break;
            default:
                break;
        }
    }

    private void writeException(final LogObject log) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        log.getException().printStackTrace(pw);
        final String error = sw.toString(); // stack trace as a string
        pw.close();
        try {
            sw.close();
        } catch (final IOException e1) {
            //Can ignore silently...
        }

        try {
            final FileWriter exceptions = new FileWriter(this.exceptionFile, true);
            exceptions.write("ERROR --- " + log.getTimestamp() + " ---" + GlobalVal.getLineBreak());
            exceptions.write("message: " + log.getMessage() + GlobalVal.getLineBreak());

            if (log.getInfo() != null)
                exceptions.write("info: " + log.getInfo() + GlobalVal.getLineBreak());

            exceptions.write("Class:" + log.getClazz().getName() + GlobalVal.getLineBreak());

            exceptions.write(error + GlobalVal.getLineBreak());
            exceptions.close();
        } catch (final IOException io) {
            io.printStackTrace();
        }
    }

    private void writeDebug(final LogObject log) {
        try {
            final FileWriter file = new FileWriter(this.debugFile, true);
            file.write("DEBUG --- " + log.getTimestamp() + " ---" + GlobalVal.getLineBreak());
            file.write("message: " + log.getMessage() + GlobalVal.getLineBreak());

            if (log.getInfo() != null)
                file.write("info: " + log.getInfo() + GlobalVal.getLineBreak());

            file.close();
        } catch (final IOException io) {
            io.printStackTrace();
        }
    }
}
