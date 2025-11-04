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

import javax.swing.*;
import java.util.Optional;

import static org.cosinus.swing.action.ActionContext.EMPTY_CONTEXT;

/**
 * Interface for a swing action in context
 */
public interface ActionInContext {

    /**
     * Run the action in the given context.
     *
     * @param context the context to run the action
     */
    void run(ActionContext context);

    /**
     * Run the action in the given context.
     */
    default void run() {
        run(EMPTY_CONTEXT);
    }

    /**
     * Get the icon name associated with this action.
     *
     * @return the icon name
     */
    default String getIconName() {
        return null;
    }

    /**
     * Get the id of the action.
     *
     * @return the id of the action
     */
    String getId();

    /**
     * Get the keystroke corresponding to the action.
     *
     * @return {@link Optional#empty()}
     */
    default Optional<KeyStroke> getKeyStroke() {
        return Optional.empty();
    }
}
