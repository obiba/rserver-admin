package org.obiba.rserver.domain;

import com.google.common.base.Strings;
import org.obiba.rserver.model.RSession;
import org.obiba.rserver.r.NoSuchRCommandException;
import org.obiba.rserver.r.ROperation;
import org.obiba.rserver.r.RRuntimeException;
import org.obiba.rserver.r.RScriptROperation;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Rserve R session adapter.
 */
public class RServeSession implements RSession {

    private static final Logger log = LoggerFactory.getLogger(RServeSession.class);

    private final String id;

    private final Date created;

    private Date lastAccess;

    private String originalWorkDir;

    private String originalTempDir;

    private boolean busy;

    private final Lock lock = new ReentrantLock();

    private org.rosuda.REngine.Rserve.RSession rSession;

    /**
     * R commands to be processed.
     */
    private final BlockingQueue<RCommand> rCommandQueue = new LinkedBlockingQueue<>();

    /**
     * All R commands.
     */
    private final List<RCommand> rCommandList = Collections.synchronizedList(new LinkedList<RCommand>());

    private RCommandsConsumer rCommandsConsumer;

    private Thread consumer;

    /**
     * R command identifier increment.
     */
    private int commandId = 1;

    public RServeSession(RConnection connection) {
        try {
            rSession = connection.detach();
        } catch (RserveException e) {
            log.error("Error while detaching R session.", e);
            throw new RRuntimeException(e);
        }
        this.id = UUID.randomUUID().toString();
        this.created = new Date();
        this.lastAccess = created;

        try {
            originalWorkDir = getRWorkDir();
            originalTempDir = updateRTempDir();
        } catch (Exception e) {
            // ignore
        }
    }

    //
    // Model methods
    //

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @Override
    public Date getLastAccess() {
        return lastAccess;
    }

    @Override
    public boolean isBusy() {
        return busy;
    }

    //
    // Management methods
    //

    public String getWorkDir() {
        return originalWorkDir;
    }

    public String getTempDir() {
        return originalTempDir;
    }

    /**
     * Check if the R session is not busy and has expired.
     *
     * @param timeout in minutes
     * @return
     */
    public boolean hasExpired(long timeout) {
        Date now = new Date();
        return !busy && now.getTime() - lastAccess.getTime() > timeout * 60 * 1000;
    }

    /**
     * Update last access date.
     */
    public void touch() {
        lastAccess = new Date();
    }

    @Override
    public String toString() {
        return id;
    }

    //
    // ROperationTemplate methods
    //

    /**
     * Executes the R operation on the current R session of the invoking Opal user.
     */
    public synchronized void execute(ROperation rop) {
        RConnection connection = null;
        lock.lock();
        busy = true;
        touch();
        try {
            connection = newConnection();
            rop.doWithConnection(connection);
        } finally {
            busy = false;
            touch();
            lock.unlock();
            if (connection != null) close(connection);
        }
    }

    public synchronized String executeAsync(ROperation rop) {
        touch();
        ensureRCommandsConsumer();
        String rCommandId = id + "-" + commandId++;
        RCommand cmd = new RCommand(rCommandId, rop);
        rCommandList.add(cmd);
        rCommandQueue.offer(cmd);
        return rCommandId;
    }

    public Iterable<RCommand> getRCommands() {
        touch();
        return rCommandList;
    }

    public boolean hasRCommand(String cmdId) {
        touch();
        for (RCommand rCommand : rCommandList) {
            if (rCommand.getId().equals(cmdId)) return true;
        }
        return false;
    }

    public RCommand getRCommand(String cmdId) {
        touch();
        for (RCommand rCommand : rCommandList) {
            if (rCommand.getId().equals(cmdId)) return rCommand;
        }
        throw new NoSuchRCommandException(cmdId);
    }

    public RCommand removeRCommand(String cmdId) {
        touch();
        RCommand rCommand = getRCommand(cmdId);
        synchronized (rCommand) {
            rCommand.notifyAll();
        }
        rCommandList.remove(rCommand);
        return rCommand;
    }

    /**
     * Close the R session.
     */
    public void close() {
        if (isClosed()) return;

        try {
            cleanRWorkDir();
            cleanRTempDir();
        } catch (Exception e) {
            // ignore
        }

        try {
            newConnection().close();
        } catch (Exception e) {
            // ignore
        } finally {
            rSession = null;
        }

        if (consumer == null) return;
        try {
            consumer.interrupt();
        } catch (Exception e) {
            // ignore
        } finally {
            consumer = null;
            rCommandList.clear();
            rCommandQueue.clear();
        }
    }

    public boolean isClosed() {
        return rSession == null;
    }

    //
    // private methods
    //


    private String getRWorkDir() throws REXPMismatchException {
        RScriptROperation rop = new RScriptROperation("base::getwd()", false);
        execute(rop);
        return rop.getResult().asString();
    }

    private void cleanRWorkDir() {
        if (Strings.isNullOrEmpty(originalWorkDir)) return;
        RScriptROperation rop = new RScriptROperation(String.format("base::unlink('%s', recursive=TRUE)", originalWorkDir), false);
        execute(rop);
    }

    private String updateRTempDir() throws REXPMismatchException {
        RScriptROperation rop = new RScriptROperation("if (!require(unixtools)) { install.packages('unixtools', repos = 'http://www.rforge.net/') }", false);
        execute(rop);
        rop = new RScriptROperation("unixtools::set.tempdir(base::file.path(base::tempdir(), base::basename(base::getwd())))", false);
        execute(rop);
        rop = new RScriptROperation("base::dir.create(base::tempdir(), recursive = TRUE)", false);
        execute(rop);
        rop = new RScriptROperation("base::tempdir()", false);
        execute(rop);
        return rop.getResult().asString();
    }

    private void cleanRTempDir() {
        if (Strings.isNullOrEmpty(originalTempDir)) return;
        RScriptROperation rop = new RScriptROperation(String.format("base::unlink('%s', recursive=TRUE)", originalTempDir), false);
        execute(rop);
    }

    /**
     * Creates a new R connection from the last R session state.
     *
     * @return
     */
    private RConnection newConnection() {
        if (rSession == null) throw new RRuntimeException("No Rserve session");
        try {
            return rSession.attach();
        } catch (RserveException e) {
            log.error("Error while attaching R session.", e);
            throw new RRuntimeException(e);
        }
    }

    /**
     * Detach the R connection and updates the R session.
     *
     * @param connection
     */
    private void close(RConnection connection) {
        if (connection == null) return;
        if (!Strings.isNullOrEmpty(connection.getLastError()) && !connection.getLastError().toLowerCase().equals("ok")) {
            throw new RRuntimeException("Unexpected R server error: " + connection.getLastError());
        }
        try {
            rSession = connection.detach();
        } catch (RserveException e) {
            log.warn("Error while detaching R session.", e);
        }
    }

    private void ensureRCommandsConsumer() {
        if (rCommandsConsumer == null) {
            rCommandsConsumer = new RCommandsConsumer();
            startRCommandsConsumer();
        } else if (consumer == null || !consumer.isAlive()) {
            startRCommandsConsumer();
        }
    }

    private void startRCommandsConsumer() {
        consumer = new Thread() {
            @Override
            public void run() {
                try {
                    rCommandsConsumer.run();
                } catch (Exception e) {
                    if (log.isDebugEnabled()) {
                        log.error("Error in thread execution", e);
                    } else {
                        log.error("Error in thread execution: {}", e.getMessage());
                    }
                }
            }
        };
        consumer.setName("R Operations Consumer " + rCommandsConsumer);
        consumer.setPriority(Thread.NORM_PRIORITY);
        consumer.start();
    }

    private class RCommandsConsumer implements Runnable {

        @Override
        public void run() {
            log.debug("Starting R operations consumer");
            try {
                //noinspection InfiniteLoopStatement
                while (true) {
                    consume(rCommandQueue.take());
                }
            } catch (InterruptedException ignored) {
                log.debug("Stopping R operations consumer");
            } catch (Exception e) {
                log.error("Error in R command consumer", e);
            }
        }

        private void consume(RCommand rCommand) {
            try {
                rCommand.inProgress();
                execute(rCommand.getROperation());
                rCommand.completed();
            } catch (Exception e) {
                log.error("Error when consuming R command: {}", e.getMessage(), e);
                rCommand.failed(e.getMessage());
            }
            synchronized (rCommand) {
                rCommand.notifyAll();
            }
        }
    }
}
