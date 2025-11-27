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

/**
 * Worker listener.
 *
 * @param <M> the worker model type
 */
public interface WorkerListener<M extends WorkerModel<T>, T> {

    /**
     * Signal the worker was started
     *
     * @param workerModel the worker model
     */
    default void workerStarted(M workerModel) {

    }

    /**
     * Signal the worker model was updated
     *
     * @param workerModel the worker model
     */
    default void workerUpdated(M workerModel) {

    }

    /**
     * Signal the worker was finished
     *
     * @param workerModel the worker model
     */
    default void workerFinished(M workerModel) {

    }
}
