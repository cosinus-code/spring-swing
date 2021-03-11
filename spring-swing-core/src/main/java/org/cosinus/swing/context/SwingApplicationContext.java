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

package org.cosinus.swing.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Swing application context
 */
public class SwingApplicationContext {

    public static SwingApplicationContext instance;

    private Map<String, Object> swingComponents = new HashMap<>();

    public void addSwingComponents(String name,
                                   Object component) {
        swingComponents.put(name, component);
    }

    public Map<String, Object> getSwingComponents() {
        return swingComponents;
    }

    public SwingApplicationContext setSwingComponents(Map<String, Object> swingComponents) {
        this.swingComponents = swingComponents;
        return this;
    }

    public Optional<Object> findByName(String swingComponentName) {
        return Optional.ofNullable(swingComponents.get(swingComponentName));
    }
}
