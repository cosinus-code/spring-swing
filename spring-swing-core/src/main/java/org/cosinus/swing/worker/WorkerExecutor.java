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
import org.cosinus.swing.progress.ProgressModel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Optional.ofNullable;

/**
 * Implementation of {@link ActionExecutor} for deleting streamers based on {@link Worker}
 */
public abstract class WorkerExecutor<A extends ActionModel, M extends WorkerModel<T>, T, P extends ProgressModel>
    implements ActionExecutor<A> {

    private final Map<String, Worker<M, T, P>> workersMap = new ConcurrentHashMap<>();

    @Override
    public void execute(A actionModel) {
        ofNullable(actionModel)
            .map(this::createWorker)
            .filter(this::isValid)
            .ifPresent(worker -> {
                cancel(worker.getId());
                workersMap.put(worker.getId(), worker);
                worker.start();
            });
    }

    protected boolean isValid(Worker<M, T, P> workerModel) {
        return true;
    }

    @Override
    public void cancel(String workerId) {
        ofNullable(workersMap.get(workerId))
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

    protected abstract Worker<M, T, P> createWorker(A actionModel);
}
