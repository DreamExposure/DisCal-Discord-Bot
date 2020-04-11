package org.dreamexposure.discal.core.logger.loggers;

import org.dreamexposure.discal.core.logger.interfaces.Logger;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.utils.GlobalConst;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class FileLogger implements Logger {
	private final String exceptionFile;
	private final String debugFile;

	public FileLogger(String exceptionFile, String debugFile) {
		this.exceptionFile = exceptionFile;
		this.debugFile = debugFile;
	}

	@Override
	public void write(LogObject log) {
		switch (log.getType()) {
			case EXCEPTION:
				writeException(log);
				break;
			case DEBUG:
				writeDebug(log);
				break;
			default:
				break;
		}
	}

	private void writeException(LogObject log) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		log.getException().printStackTrace(pw);
		String error = sw.toString(); // stack trace as a string
		pw.close();
		try {
			sw.close();
		} catch (IOException e1) {
			//Can ignore silently...
		}

		try {
			FileWriter exceptions = new FileWriter(this.exceptionFile, true);
			exceptions.write("ERROR --- " + log.getTimestamp() + " ---" + GlobalConst.lineBreak);
			exceptions.write("message: " + log.getMessage() + GlobalConst.lineBreak);

			if (log.getInfo() != null)
				exceptions.write("info: " + log.getInfo() + GlobalConst.lineBreak);

			exceptions.write("Class:" + log.getClazz().getName() + GlobalConst.lineBreak);

			exceptions.write(error + GlobalConst.lineBreak);
			exceptions.close();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	private void writeDebug(LogObject log) {
		try {
			FileWriter file = new FileWriter(debugFile, true);
			file.write("DEBUG --- " + log.getTimestamp() + " ---" + GlobalConst.lineBreak);
			file.write("message: " + log.getMessage() + GlobalConst.lineBreak);

			if (log.getInfo() != null)
				file.write("info: " + log.getInfo() + GlobalConst.lineBreak);

			file.close();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}
}
