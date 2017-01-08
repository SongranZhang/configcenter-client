package com.linkedkeeper.configcenter.zookeeper.listener;

import com.linkedkeeper.configcenter.zookeeper.exception.ZkInterruptedException;
import org.apache.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * All listeners registered at the {@ZkClient} will be notified from this event thread. This is to prevent
 * dead-lock situations. The {@ZkClient} pulls some information out of the {@ZooKeeper} events to signal
 * {@ZkLock} conditions. Re-using the {@ZooKeeper} event thread to also notify {@ZkClient} listeners,
 * would stop the ZkClient from receiving events from {@ZooKeeper} as soon as one of the listeners blocks (because
 * it is waiting for something). {@ZkClient} would then for instance not be able to maintain it's connection state
 * anymore.
 * <p/>
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public class ZkEventThread extends Thread {

    private static final Logger log = Logger.getLogger(ZkEventThread.class);

    private BlockingQueue<ZkEvent> events = new LinkedBlockingQueue<ZkEvent>();
    private static AtomicInteger eventId = new AtomicInteger(0);

    static abstract class ZkEvent {

        private String description;

        public ZkEvent(String description) {
            this.description = description;
        }

        public abstract void run() throws Exception;

        @Override
        public String toString() {
            return "ZkEvent[" + description + "]";
        }
    }

    ZkEventThread(String name) {
        setDaemon(true);
        setName("ZkClient-EventThread-" + getId() + "-" + name);
    }

    @Override
    public void run() {
        log.info("Starting ZkClient event thread.");
        try {
            while (!isInterrupted()) {
                ZkEvent zkEvent = events.take();
                int _eventId = eventId.incrementAndGet();
                log.debug("Delivering event #" + _eventId + " " + zkEvent);
                try {
                    zkEvent.run();
                } catch (InterruptedException e) {
                    interrupt();
                } catch (ZkInterruptedException e) {
                    interrupt();
                } catch (Throwable e) {
                    log.error("Error handling event " + zkEvent, e);
                }
                log.debug("Delivering event #" + eventId + " done");
            }
        } catch (Exception e) {
            log.info("Terminate ZkClient event thread.");
        }
    }

    public void send(ZkEvent event) {
        if (!isInterrupted()) {
            log.debug("New event : " + event);
            events.add(event);
        }
    }
}
