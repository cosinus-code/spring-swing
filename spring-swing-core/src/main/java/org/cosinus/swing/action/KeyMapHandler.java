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

package org.cosinus.swing.action;

import javax.swing.*;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * Key maps handler.
 * <p>
 * It takes all action defined in context and build a map from key strokes to actions.
 */
public class KeyMapHandler {

    private final Map<KeyStroke, ActionInContext> keyMap;

    public KeyMapHandler(Set<ActionInContext> actions) {
        this.keyMap = actions
            .stream()
            .filter(action -> action.getKeyStroke().isPresent())
            .collect(Collectors.toMap(action -> action.getKeyStroke().get(),
                Function.identity()));
    }

    /**
     * Search for an action based on a keystroke.
     *
     * @param keyStroke the keystroke to search for
     * @return the found action, or {@link Optional#empty()}
     */
    public Optional<ActionInContext> findActionByKeyStroke(KeyStroke keyStroke) {
        return ofNullable(keyMap.get(keyStroke));
    }
}
