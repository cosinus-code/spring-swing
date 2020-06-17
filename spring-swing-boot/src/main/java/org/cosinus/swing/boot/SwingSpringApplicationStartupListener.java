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

package org.cosinus.swing.boot;

import org.cosinus.swing.boot.event.ApplicationContextCreationEvent;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ApplicationContext;

/**
 * Listener for swing spring application startup.
 * It listen for basic spring application startup events from {@link SpringApplicationRunListener}
 * along with custom {@link ApplicationContextCreationEvent} and
 */
public interface SwingSpringApplicationStartupListener extends SpringApplicationRunListener {

    void contextBeforeInitializeBeans(ApplicationContext context);

    void contextBeforeInitializeBean(ApplicationContext context,
                                     Object bean,
                                     String beanName);

    void contextAfterInitializeBean(ApplicationContext context,
                                    Object bean,
                                    String beanName);

    void applicationFrameInitializing(ApplicationFrame applicationFrame);

    void applicationFrameInitialized(ApplicationFrame applicationFrame);
}
