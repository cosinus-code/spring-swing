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

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;

/**
 * Handler for worker listeners
 */
public class WorkerListenerHandler {

    private final Map<
        Class<? extends WorkerModel<?>>,
        Map<String, Queue<WorkerListener<?, ?>>>> workerListenersMap = new HashMap<>();

    /**
     * Register a worker listener.
     *
     * @param modelClass the worker model class
     * @param workerId   the worker id
     * @param listeners  the worker listener
     */
    public <M extends WorkerModel<T>, T> void register(Class<M> modelClass,
                                                       String workerId,
                                                       final WorkerListener<M, T>... listeners) {
        workerListenersMap
            .computeIfAbsent(modelClass, k -> new HashMap<>())
            .computeIfAbsent(workerId, k -> new ConcurrentLinkedQueue<>())
            .addAll(asList(listeners));
    }

    /**
     * Signal a worker was tarted.
     *
     * @param workerId the worker id
     */
    public <M extends WorkerModel<T>, T> void workerStarted(String workerId, M workerModel) {
        getListeners(workerId, workerModel)
            .forEach(listener -> listener.workerStarted(workerModel));
    }

    /**
     * Signal a worker model was updated.
     *
     * @param workerModel the worker model
     */
    public <M extends WorkerModel<T>, T> void workerUpdated(String workerId, M workerModel) {
        getListeners(workerId, workerModel)
            .forEach(listener -> listener.workerUpdated(workerModel));
    }

    /**
     * Signal a worker was finished.
     *
     * @param workerModel the worker model
     */
    public <M extends WorkerModel<T>, T> void workerFinished(String workerId, M workerModel) {
        getListeners(workerId, workerModel)
            .forEach(listener -> listener.workerFinished(workerModel));
        removeListeners(workerId, workerModel);
    }

    /**
     * Get the listeners registered to listen for a worker.
     *
     * @param workerId    the worker id
     * @param workerModel the worker model
     * @return the stream of listeners
     */
    private <M extends WorkerModel<T>, T> Stream<WorkerListener<M, T>> getListeners(String workerId, M workerModel) {
        return ofNullable(workerListenersMap.get(workerModel.getClass()))
            .flatMap(listenersByIdmap -> ofNullable(listenersByIdmap.get(workerId)))
            .stream()
            .flatMap(Collection::stream)
            .map(listener -> (WorkerListener<M, T>) listener);
    }

    private <M extends WorkerModel<T>, T> void removeListeners(String workerId, M workerModel) {
        ofNullable(workerListenersMap.get(workerModel.getClass()))
            .flatMap(listenersByIdmap -> ofNullable(listenersByIdmap.get(workerId)))
            .ifPresent(Queue::clear);
    }
}
