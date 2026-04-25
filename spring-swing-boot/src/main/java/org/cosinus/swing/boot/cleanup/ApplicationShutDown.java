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

package org.cosinus.swing.boot.cleanup;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static java.util.Optional.ofNullable;

public class ApplicationShutDown {

    private static final Logger LOG = LogManager.getLogger(ApplicationShutDown.class);

    private final Map<String, AutoCloseable> resources;

    public ApplicationShutDown() {
        this.resources = new HashMap<>();
    }

    public void register(AutoCloseable resource) {
        register(resource.getClass().getName(), resource);
    }

    public void register(String id, AutoCloseable resource) {
        resources.put(id, resource);
    }

    public void shutDown(String id) {
        ofNullable(resources.remove(id))
            .ifPresent(resource -> shutDownResource(id, resource));
    }

    public void shutDown() {
        resources.forEach(this::shutDownResource);
    }

    protected void shutDownResource(String id, AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
                if (resource instanceof ShutDownResource shutDownResource) {
                    shutDownResource.shutDown();
                }
                resources.remove(id);
            } catch (Exception e) {
                LOG.error("Error while closing resource", e);
            }
        }
    }
}
