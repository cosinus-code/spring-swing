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
import lombok.extern.slf4j.Slf4j;
import org.cosinus.swing.action.execute.ActionExecutors;
import org.cosinus.swing.action.execute.ActionModel;
import org.cosinus.swing.error.AbortActionException;
import org.cosinus.swing.error.ActionException;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.error.TranslatableRuntimeException;
import org.cosinus.swing.progress.ProgressListener;
import org.cosinus.swing.progress.ProgressModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static java.lang.Thread.currentThread;
import static java.util.Optional.ofNullable;

/**
 * Abstract {@link javax.swing.SwingWorker} with custom progress and worker model
 */
@Slf4j
public abstract class Worker<M extends WorkerModel<T>, T, P extends ProgressModel> extends SwingWorker<M, T> {

    @Autowired
    protected ActionExecutors actionExecutors;

    @Autowired
    protected ErrorHandler errorHandler;

    @Autowired
    protected WorkerProperties workerProperties;

    @Getter
    protected final String id;

    protected final ActionModel actionModel;

    protected final Queue<WorkerListener<M, T>> workerListeners;

    protected final Queue<ProgressListener<P>> progressListeners;

    protected final M workerModel;

    protected final P progressModel;

    protected Exception error;

    @Setter
    @Getter
    protected boolean paused;

    @Getter
    protected boolean aborted;

    protected long startTime = System.currentTimeMillis();

    protected Worker(ActionModel actionModel, M workerModel) {
        this(actionModel, workerModel, null);
    }

    protected Worker(ActionModel actionModel, P progressModel) {
        this(actionModel, null, progressModel);
    }

    protected Worker(ActionModel actionModel, M workerModel, P progressModel) {
        this.id = actionModel.getExecutionId();
        this.actionModel = actionModel;
        this.workerModel = workerModel;
        this.progressModel = progressModel;
        this.workerListeners = new ConcurrentLinkedQueue<>();
        this.progressListeners = new ConcurrentLinkedQueue<>();
    }

    public void registerListener(final WorkerListener<M, T> workerListener) {
        workerListeners.add(workerListener);
    }

    public void registerListener(final ProgressListener<P> progressListener) {
        progressListeners.add(progressListener);
    }

    public void start() {
        startTime = System.currentTimeMillis();
        fireWorkerListeners(workerListener -> workerListener.workerStarted(workerModel));
        fireProgressListeners(progressListener -> progressListener.progressStarted(progressModel));
        execute();
    }

    protected void setError(Exception error) {
        this.error = error;
    }

    protected void setAborted() {
        aborted = true;
        logWorkerStatus("aborted");
    }

    @Override
    protected final M doInBackground() {
        logWorkerStatus("started");
        try {
            doWork();
        } catch (ActionException ex) {
            setError(ex);
        } catch (AbortActionException ex) {
            setAborted();
        }
        return workerModel;
    }

    public void updateProgress(final ProgressModelUpdater<P> progressUpdater) {
        progressUpdater.update(progressModel);
        checkStatusAndPublish();
    }

    @Override
    protected void process(final List<T> items) {
        try {
            if (workerModel != null) {
                workerModel.update(items);
            }
            fireWorkerListeners(workerListener -> workerListener.workerUpdated(workerModel));
            fireProgressListeners(progressListener -> progressListener.progressUpdated(progressModel));
        } catch (Exception ex) {
            setError(ex);
        }
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
            log.error("Error while worker run", error);
            errorHandler.handleError(error.getLocalizedMessage());
        }

        onWorkerDoneBeforeFinishing();
        fireWorkerListeners(workerListener -> workerListener.workerFinished(workerModel));
        fireProgressListeners(progressListener -> progressListener.progressFinished(progressModel));
        actionExecutors.getActionExecutor(actionModel)
            .ifPresent(executor -> executor.remove(getId()));
        logWorkerStatus("finished");

        long duration = System.currentTimeMillis() - startTime;
        log.info("Finished worker {} in {} ms", actionModel.getActionName(), duration);
    }

    protected void fireWorkerListeners(Consumer<WorkerListener<M, T>> workerListenerCall) {
        if (workerModel != null) {
            workerListeners.forEach(workerListenerCall);
        }
    }

    protected void fireProgressListeners(Consumer<ProgressListener<P>> progressListenerCall) {
        if (progressModel != null) {
            progressListeners.forEach(progressListenerCall);
        }
    }

    protected void onWorkerDoneBeforeFinishing() {
        //TODO: to move to worker listener
    }

    public void checkStatusAndPublish(T... item) {
        checkWorkerStatus();
        publish(item);
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
        delayWorker();
    }

    protected void delayWorker() {
        ofNullable(workerProperties.getDelay())
            .filter(delay -> delay > 0)
            .ifPresent(delay -> {
                try {
                    java.lang.Thread.sleep(delay);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    public boolean isSuccessful() {
        return error == null && !isCancelled() && !isAborted();
    }

    @Override
    public boolean cancel() {
        logWorkerStatus("cancelled");
        return super.cancel();
    }

    protected void logWorkerStatus(String status) {
        log.debug("Work for action `{}` {}: {}", actionModel.getActionName(), status, id);
    }

    protected abstract void doWork();
}
