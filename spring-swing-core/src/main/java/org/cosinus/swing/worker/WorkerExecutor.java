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

import org.cosinus.swing.action.execute.ActionExecutor;
import org.cosinus.swing.action.execute.ActionModel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Optional.ofNullable;

/**
 * Implementation of {@link ActionExecutor} for deleting streamers based on {@link Worker}
 */
public abstract class WorkerExecutor<A extends ActionModel, M extends WorkerModel<T>, T> implements ActionExecutor<A> {

    protected final WorkerListenerHandler workerListenerHandler;

    private final Map<String, Worker<M, T>> workersMap = new ConcurrentHashMap<>();

    protected WorkerExecutor(final WorkerListenerHandler workerListenerHandler) {
        this.workerListenerHandler = workerListenerHandler;
    }

    @Override
    public void execute(A actionModel) {
        ofNullable(actionModel)
            .map(this::createWorker)
            .ifPresent(worker -> {
                cancel(actionModel.getExecutionId());
                workersMap.put(actionModel.getExecutionId(), worker);
                registerWorkerListeners(actionModel, worker.getWorkerModel());
                worker.start();
            });
    }

    protected void registerWorkerListeners(A actionModel, M workerModel) {
        Class<M> modelClass = (Class<M>) workerModel.getClass();
        ofNullable(createWorkerListener(actionModel))
            .ifPresent(workerListener -> workerListenerHandler
                .register(modelClass, actionModel.getExecutionId(), workerListener));
    }

    @Override
    public void cancel(String executionId) {
        ofNullable(workersMap.get(executionId))
            .ifPresent(SwingWorker::cancel);
    }

    @Override
    public void remove(String workerId) {
        workersMap.remove(workerId);
    }

    public boolean isWorkerRunning(String workerId) {
        return ofNullable(workersMap.get(workerId))
            .isPresent();
    }

//    @Override
//    public void runInBackground(String copyId) {
//        Optional.ofNullable(workersMap.get(copyId))
//                .ifPresent(worker -> {
//                    //TODO
//                });
//    }

    protected abstract WorkerListener<M, T> createWorkerListener(A actionModel);

    protected abstract Worker<M, T> createWorker(A actionModel);
}
