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

import org.springframework.context.ApplicationContext;

import static java.util.Optional.ofNullable;

/**
 * Application context injector.
 * <p>
 * A static instance of {@link ApplicationContext} is used
 * to explicitly inject the context in objects after they are instantiated.
 * <p>
 * This is a helper class and cannot be instantiated.
 */
public class ApplicationContextInjector {

    public static ApplicationContext applicationContext;

    public ApplicationContextInjector(ApplicationContext applicationContext) {
        ApplicationContextInjector.applicationContext = applicationContext;
    }

    /**
     * Inject the application into an object.
     *
     * @param object the object to inject the context into
     */
    public static void injectContext(Object object) {
        ofNullable(applicationContext)
            .map(ApplicationContext::getAutowireCapableBeanFactory)
            .ifPresent(factory -> factory.autowireBean(object));
    }
}
