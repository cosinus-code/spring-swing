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

import org.cosinus.swing.boot.event.ApplicationContextCreationEvent;
import org.cosinus.swing.boot.event.ApplicationFrameEvent;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ApplicationContext;

/**
 * Listener for swing spring application startup.
 * It listen for basic spring application startup events from {@link SpringApplicationRunListener}
 * along with custom {@link ApplicationContextCreationEvent} and {@link ApplicationFrameEvent}
 */
public interface SwingSpringApplicationStartupListener extends SpringApplicationRunListener {

    /**
     * Called just before starting to initialize the application context.
     *
     * @param context the application context
     */
    void contextBeforeInitializeBeans(ApplicationContext context);

    /**
     * Called just before a bean is initialized.
     *
     * @param context  the application context
     * @param bean     the bean to be initialized
     * @param beanName the name of the bean to be initialized
     */
    @SuppressWarnings("EmptyMethod")
    void contextBeforeInitializeBean(ApplicationContext context,
                                     Object bean,
                                     String beanName);

    /**
     * Called immediately after a bean is initialized.
     *
     * @param context  the application context
     * @param bean     the initialized bean
     * @param beanName the name of the initialized bean
     */
    void contextAfterInitializeBean(ApplicationContext context,
                                    Object bean,
                                    String beanName);

    /**
     * Called just before starting to initialize the application frame.
     *
     * @param applicationFrame the application frame
     */
    void applicationFrameInitializing(ApplicationFrame applicationFrame);

    /**
     * Called immediately after the application frame is initialized.
     *
     * @param applicationFrame the application frame
     */
    void applicationFrameInitialized(ApplicationFrame applicationFrame);
}
