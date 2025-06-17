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

package org.cosinus.swing.boot;

import org.cosinus.swing.boot.event.ApplicationContextAfterInitializeBeanEvent;
import org.cosinus.swing.boot.event.ApplicationContextBeforeInitializeBeanEvent;
import org.cosinus.swing.boot.event.ApplicationContextBeforeInitializeBeansEvent;
import org.cosinus.swing.boot.event.ApplicationFrameAfterInitializeEvent;
import org.cosinus.swing.boot.event.ApplicationFrameBeforeInitializeEvent;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handler for {@link SwingSpringApplicationStartupListener}.
 * <p>
 * This allows registering {@link SwingSpringApplicationStartupListener}
 * and propagating the events to them
 */
class ApplicationStartupListeners {

    private final List<SwingSpringApplicationStartupListener> startupListeners = new ArrayList<>();

    /**
     * Register some {@link SwingSpringApplicationStartupListener}
     * and make them eligible for startup events propagation.
     * <p>
     * This is done internally from {@link SpringSwingApplicationBuilder}
     *
     * @param startupListeners the startup listener to register
     */
    void register(SwingSpringApplicationStartupListener... startupListeners) {
        this.startupListeners.addAll(Arrays.asList(startupListeners));
    }

    /**
     * Get the startup listeners as {@link ApplicationListener}.
     * <p>
     * It is used internally by {@link SpringSwingApplicationBuilder} when register application listeners.
     *
     * @return the startup listeners translated as application listeners.
     */
    ApplicationListener<?>[] listeners() {
        return new ApplicationListener<?>[]{
            (ApplicationListener<ApplicationContextInitializedEvent>) event ->
                startupListeners.forEach(listener -> listener.contextPrepared(event.getApplicationContext())),
            (ApplicationListener<ApplicationPreparedEvent>) event ->
                startupListeners.forEach(listener -> listener.contextLoaded(event.getApplicationContext())),
            (ApplicationListener<ApplicationContextBeforeInitializeBeansEvent>) event ->
                startupListeners.forEach(listener -> listener.contextBeforeInitializeBeans(event.getApplicationContext())),
            (ApplicationListener<ApplicationContextBeforeInitializeBeanEvent>) event ->
                startupListeners.forEach(listener -> listener.contextBeforeInitializeBean(
                    event.getApplicationContext(), event.getBean(), event.getBeanName())),
            (ApplicationListener<ApplicationContextAfterInitializeBeanEvent>) event ->
                startupListeners.forEach(listener -> listener.contextAfterInitializeBean(
                    event.getApplicationContext(), event.getBean(), event.getBeanName())),
            (ApplicationListener<ApplicationStartedEvent>) event ->
                startupListeners.forEach(listener -> listener.started(event.getApplicationContext(), event.getTimeTaken())),
            (ApplicationListener<ApplicationReadyEvent>) event ->
                startupListeners.forEach(listener -> listener.ready(event.getApplicationContext(), event.getTimeTaken())),
            (ApplicationListener<ApplicationFrameBeforeInitializeEvent>) event ->
                startupListeners.forEach(listener -> listener.applicationFrameInitializing(event.getApplicationFrame())),
            (ApplicationListener<ApplicationFrameAfterInitializeEvent>) event ->
                startupListeners.forEach(listener -> listener.applicationFrameInitialized(event.getApplicationFrame()))
        };
    }
}
