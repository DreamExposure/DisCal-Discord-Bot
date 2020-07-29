package org.dreamexposure.discal.core.logger.threads;

import org.dreamexposure.discal.core.logger.interfaces.Logger;
import org.dreamexposure.discal.core.logger.object.LogObject;

import java.util.concurrent.BlockingQueue;

public class LoggerThread implements Runnable {
    private final BlockingQueue<LogObject> queue;
    private final Logger logger;

    public LoggerThread(BlockingQueue<LogObject> queue, Logger logger) {
        this.queue = queue;
        this.logger = logger;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        try {
            while (true) {
                LogObject log = queue.take();

                logger.write(log);
            }
        } catch (InterruptedException e) {
            //We really don't need to worry about errors here..
            // but we will spit it out for the console
            e.printStackTrace();
        }
    }
}
