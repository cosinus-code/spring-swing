/*
 * Copyright 2025 Cosinus Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cosinus.swing.worker;

import org.cosinus.stream.consumer.StreamConsumer;
import org.cosinus.stream.pipeline.PipelineStrategy;
import org.cosinus.stream.pipeline.StreamPipeline;
import org.cosinus.swing.error.ActionException;
import org.cosinus.swing.action.execute.ActionModel;

import java.io.IOException;
import java.io.UncheckedIOException;

import static java.util.Optional.ofNullable;

public abstract class PipelineWorker<M extends WorkerModel<V>, T, V>
    extends Worker<M, V> implements StreamPipeline<T> {

    protected PipelineWorker(ActionModel actionModel, M workerModel) {
        super(actionModel, workerModel);
    }

    @Override
    protected void doWork() {
        try {
            openPipeline();
        } catch (IOException | UncheckedIOException ex) {
            throw new ActionException(ex, actionModel.getActionId());
        }
    }

    @Override
    public final StreamConsumer<T> openPipelineOutputStream(PipelineStrategy pipelineStrategy) {
        return new WorkerConsumer(streamConsumer());
    }

    protected StreamConsumer<T> streamConsumer() {
        return null;
    }

    protected abstract V transform(T item);

    private class WorkerConsumer implements StreamConsumer<T> {

        protected final StreamConsumer<T> streamConsumer;

        private WorkerConsumer(final StreamConsumer<T> streamConsumer) {
            this.streamConsumer = streamConsumer;
        }

        @Override
        public void accept(T item) {
            checkWorkerStatus();
            ofNullable(streamConsumer)
                .ifPresent(consumer -> consumer.accept(item));
            publish(transform(item));
        }

        @Override
        public void close() throws IOException {
            if (streamConsumer != null) {
                streamConsumer.close();
            }
        }
    }
}
