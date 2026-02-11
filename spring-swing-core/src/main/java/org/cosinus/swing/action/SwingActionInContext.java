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

package org.cosinus.swing.action;

import static java.util.Optional.ofNullable;

/**
 * Interface for a swing action in context
 */
public interface SwingActionInContext<C extends ActionContext> extends SwingAction {

    /**
     * Run the action in the given context.
     *
     * @param context the context to run the action
     */
    void run(C context);

    /**
     * Create the default context for this action.
     * Used when running the action triggered ny the keystroke.
     *
     * @return the default context
     */
    default C getDefaultContext() {
        return null;
    }

    /**
     * Run the action in the default context.
     */
    @Override
    default void run() {
        ofNullable(getDefaultContext())
            .ifPresent(this::run);
    }
}
