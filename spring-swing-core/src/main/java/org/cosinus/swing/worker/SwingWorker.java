/*
 * Copyright 2020 Cosinus Software
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

import org.cosinus.swing.context.SwingApplicationContext;
import org.cosinus.swing.context.SwingInject;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static java.util.Optional.ofNullable;

public abstract class SwingWorker<T, V> extends javax.swing.SwingWorker<T, V> implements SwingInject {

    public SwingWorker(SwingApplicationContext swingContext) {
        injectSwingContext(swingContext);
    }

    public Optional<T> getResult() throws ExecutionException, InterruptedException {
        return ofNullable(get());
    }

    public void useResult(Consumer<T> consumer) throws ExecutionException, InterruptedException {
        if (!isCancelled()) {
            getResult().ifPresent(consumer);
        }
    }

    public boolean cancel() {
        return cancel(true);
    }
}
