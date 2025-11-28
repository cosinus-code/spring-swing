/*
 * Copyright 2025 Cosinus Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.cosinus.swing.worker;

import lombok.Getter;
import lombok.Setter;
import org.cosinus.swing.error.AbortActionException;
import org.cosinus.swing.error.ActionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.action.execute.ActionExecutors;
import org.cosinus.swing.action.execute.ActionModel;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.error.TranslatableRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.lang.Thread.currentThread;

/**
 * Abstract {@link javax.swing.SwingWorker} with custom progress
 */
public abstract class Worker<M extends WorkerModel<T>, T> extends SwingWorker<M, T> {

    private static final Logger LOG = LogManager.getLogger(Worker.class);

    @Autowired
    protected ActionExecutors actionExecutors;

    @Autowired
    protected WorkerListenerHandler workerListenerHandler;

    @Autowired
    protected ErrorHandler errorHandler;

    @Getter
    protected final String id;

    protected final ActionModel actionModel;

    @Getter
    protected final M workerModel;

    protected Exception error;

    @Setter
    @Getter
    protected boolean paused;

    @Getter
    protected boolean aborted;

    protected Worker(ActionModel actionModel, M workerModel) {
        this.id = actionModel.getExecutionId();
        this.actionModel = actionModel;
        this.workerModel = workerModel;
    }

    public void start() {
        execute();
    }

    protected void setError(Exception error) {
        this.error = error;
    }

    protected void setAborted() {
        aborted = true;
        logUserAbort();
    }

    @Override
    protected M doInBackground() {
        LOG.trace("Work for action `{}` started: {}", actionModel.getActionName(), id);
        try {
            workerListenerHandler.workerStarted(getId(), workerModel);
            doWork();
        } catch (ActionException ex) {
            setError(ex);
        } catch (AbortActionException ex) {
            setAborted();
        }
        return workerModel;
    }

    @Override
    protected void process(List<T> items) {
        try {
            workerModel.update(items);
            workerListenerHandler.workerUpdated(getId(), workerModel);
        } catch (Exception ex) {
            setError(ex);
        }
    }

    protected void logUserAbort() {
        LOG.trace("Work for action `{}` aborted: {}", actionModel.getActionName(), id);
    }

    @Override
    protected void done() {
        try {
            if (!isCancelled()) {
                get();
            }
        } catch (InterruptedException e) {
            currentThread().interrupt();
        } catch (ExecutionException ex) {
            setError(ex.getCause() instanceof TranslatableRuntimeException translatableException ?
                translatableException :
                new TranslatableRuntimeException(ex, "worker.execution.error"));
        }

        if (error != null) {
            LOG.error("Error while worker run", error);
            errorHandler.handleError(error.getLocalizedMessage());
        }

        onWorkerDoneBeforeFinishing();
        workerListenerHandler.workerFinished(getId(), workerModel);
        actionExecutors.getActionExecutor(actionModel)
            .ifPresent(executor -> executor.remove(getId()));
        LOG.trace("Work for action `{}` finished: {}", actionModel.getActionName(), id);
    }

    protected void onWorkerDoneBeforeFinishing() {
    }

    public void checkWorkerStatus() {
        if (isCancelled()) {
            throw new AbortActionException("Worker aborted by user");
        }
        if (isPaused()) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    currentThread().interrupt();
                }
            }
        }
    }

    public boolean isSuccessful() {
        return error == null && !isCancelled() && !isAborted();
    }

    protected abstract void doWork();
}
