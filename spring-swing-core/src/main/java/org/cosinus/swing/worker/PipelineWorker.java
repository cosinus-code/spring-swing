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
import org.cosinus.stream.error.AbortPipelineConsumeException;
import org.cosinus.stream.pipeline.PipelineListener;
import org.cosinus.stream.pipeline.PipelineStrategy;
import org.cosinus.stream.pipeline.StreamPipeline;
import org.cosinus.swing.error.AbortActionException;
import org.cosinus.swing.error.ActionException;
import org.cosinus.swing.action.execute.ActionModel;
import org.cosinus.swing.progress.ProgressModel;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

import static java.util.Optional.ofNullable;

public abstract class PipelineWorker<M extends WorkerModel<T>, T, P extends ProgressModel>
    extends Worker<M, T, P> implements StreamPipeline<T>, PipelineListener<T> {

    protected WorkerConsumer workerConsumer;

    public PipelineWorker(ActionModel actionModel, M workerModel, P progressModel) {
        super(actionModel, workerModel, progressModel);
    }

    @Override
    protected void doWork() {
        try {
            openPipeline();
        } catch (AbortPipelineConsumeException ex) {
            throw new AbortActionException("Pipeline worker aborted", ex);
        } catch (IOException | UncheckedIOException ex) {
            throw new ActionException(ex, actionModel.getActionId());
        }
    }

    @Override
    public final StreamConsumer<T> openPipelineOutputStream(PipelineStrategy pipelineStrategy) {
        if (workerConsumer == null) {
            workerConsumer = new WorkerConsumer(streamConsumer());
        }
        return workerConsumer;
    }

    @Override
    public final PipelineListener<T> getPipelineListener() {
        return new WorkerPipelineListener(pipelineListener());
    }

    protected PipelineListener<T> pipelineListener() {
        return this;
    }

    protected abstract StreamConsumer<T> streamConsumer();

    protected class WorkerConsumer implements StreamConsumer<T> {

        protected final StreamConsumer<T> streamConsumer;

        private WorkerConsumer(final StreamConsumer<T> streamConsumer) {
            this.streamConsumer = streamConsumer;
        }

        @Override
        public void accept(T item) {
            checkWorkerStatus();
            ofNullable(streamConsumer)
                .ifPresent(consumer -> consumer.accept(item));
            publish(item);
        }

        @Override
        public void afterClose(boolean failed) {
            if (streamConsumer != null) {
                streamConsumer.afterClose(failed);
            }
        }

        @Override
        public void close() throws IOException {
            if (streamConsumer != null) {
                streamConsumer.close();
            }
        }
    }

    protected class WorkerPipelineListener implements PipelineListener<T> {

        protected final PipelineListener<T> pipelineListener;

        private WorkerPipelineListener(final PipelineListener<T> pipelineListener) {
            this.pipelineListener = pipelineListener;
        }

        @Override
        public void beforePipelineOpen() {
            triggerPipelineListener(PipelineListener::beforePipelineOpen);
            updateProgress(ProgressModel::startProgress);
        }

        @Override
        public void afterPipelineOpen() {
            triggerPipelineListener(PipelineListener::afterPipelineOpen);
        }

        @Override
        public void beforePipelineDataConsume(T data) {
            triggerPipelineListener(listener -> listener.beforePipelineDataConsume(data));
        }

        @Override
        public void afterPipelineDataConsume(T data) {
            triggerPipelineListener(listener -> listener.afterPipelineDataConsume(data));
        }

        @Override
        public void afterPipelineDataSkip(long skippedDataSize) {
            triggerPipelineListener(listener -> listener.afterPipelineDataSkip(skippedDataSize));
        }

        @Override
        public void beforePipelineClose() {
            triggerPipelineListener(PipelineListener::beforePipelineClose);
        }

        @Override
        public void afterPipelineClose(boolean pipelineFailed) {
            triggerPipelineListener(listener -> listener.afterPipelineClose(pipelineFailed));
            workerConsumer.afterClose(pipelineFailed);
            updateProgress(ProgressModel::finishProgress);
        }

        @Override
        public void onPipelineFail() {
            triggerPipelineListener(PipelineListener::onPipelineFail);
        }

        private void triggerPipelineListener(final Consumer<PipelineListener<T>> listenerCall) {
            ofNullable(pipelineListener).ifPresent(listenerCall);
            publish();
        }
    }
}
