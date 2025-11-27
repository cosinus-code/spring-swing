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

import org.cosinus.swing.action.execute.ActionModel;

import java.util.List;

public abstract class SimpleWorker<M extends WorkerModel<M>> extends Worker<M, M> {
    protected SimpleWorker(ActionModel actionModel, M workerModel) {
        super(actionModel, workerModel);
    }

    public void updateModel(Runnable runnable) {
        checkWorkerStatus();
        runnable.run();
        publish(workerModel);
    }

    @Override
    protected void process(List<M> items) {
        workerListenerHandler.workerUpdated(getId(), workerModel);
    }
}
