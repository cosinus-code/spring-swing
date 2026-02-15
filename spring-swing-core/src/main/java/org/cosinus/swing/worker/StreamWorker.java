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

import org.cosinus.stream.StreamSupplier;
import org.cosinus.stream.consumer.StreamConsumer;
import org.cosinus.stream.pipeline.PipelineStrategy;
import org.cosinus.swing.action.execute.ActionModel;
import org.cosinus.swing.action.execute.SimpleActionModel;
import org.cosinus.swing.progress.ProgressModel;

import java.util.stream.Stream;

public abstract class StreamWorker<M extends WorkerModel<T>, T, P extends ProgressModel>
    extends PipelineWorker<M, T, P> {

    private final StreamSupplier<T> streamSupplier;

    protected StreamWorker(final String actionId,
                           final M workerModel,
                           final StreamSupplier<T> streamSupplier,
                           final P progressModel) {
        this(new SimpleActionModel(actionId), workerModel, streamSupplier, progressModel);
    }

    protected StreamWorker(final ActionModel actionModel,
                           final M workerModel,
                           final StreamSupplier<T> streamSupplier,
                           final P progressModel) {
        super(actionModel, workerModel, progressModel);
        this.streamSupplier = streamSupplier;
    }

    @Override
    public Stream<T> openPipelineInputStream(PipelineStrategy pipelineStrategy) {
        return streamSupplier.stream();
    }

    @Override
    protected StreamConsumer<T> streamConsumer() {
        return null;
    }
}
