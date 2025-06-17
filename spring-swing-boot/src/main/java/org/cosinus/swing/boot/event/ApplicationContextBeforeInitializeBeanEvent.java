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

package org.cosinus.swing.boot.event;

import org.springframework.context.ApplicationContext;

import java.io.Serial;

/**
 * {@link ApplicationContextCreationEvent} fired before a bean is initialized
 */
public class ApplicationContextBeforeInitializeBeanEvent extends ApplicationContextCreationEvent {

    @Serial
    private static final long serialVersionUID = -8241566660318313969L;

    private final Object bean;

    private final String beanName;

    public ApplicationContextBeforeInitializeBeanEvent(ApplicationContext applicationContext,
                                                       Object bean,
                                                       String beanName) {
        super(applicationContext);
        this.bean = bean;
        this.beanName = beanName;
    }

    public Object getBean() {
        return bean;
    }

    public String getBeanName() {
        return beanName;
    }
}
