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

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static java.util.Optional.ofNullable;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Wrapper over a {@link javax.swing.SwingWorker}
 * which will automatically inject the application context.
 *
 * @param <T> the type of the worker execution result
 * @param <V> the type used for carrying out intermediate results by this
 *            {@code SwingWorker's} {@code publish} and {@code process} methods
 */
public abstract class SwingWorker<T, V> extends javax.swing.SwingWorker<T, V> {

    public SwingWorker() {
        injectContext(this);
    }

    /**
     * Get the result of the worker execution.
     *
     * @return the execution result, or {@link Optional#empty()}
     * @throws ExecutionException   {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     */
    public Optional<T> getResult() throws ExecutionException, InterruptedException {
        return ofNullable(get());
    }

    /**
     * Use the result of the worker execution.
     *
     * @param consumer the result consumer
     * @throws ExecutionException   {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     */
    public void useResult(Consumer<T> consumer) throws ExecutionException, InterruptedException {
        if (!isCancelled()) {
            getResult().ifPresent(consumer);
        }
    }

    /**
     * Cancel the current worker execution.
     *
     * @return {@inheritDoc}
     */
    public boolean cancel() {
        return cancel(false);
    }
}
