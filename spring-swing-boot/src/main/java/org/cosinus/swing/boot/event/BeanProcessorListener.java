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

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Listen the application processing beans
 * and publish corresponding {@link ApplicationContextCreationEvent}
 */
public class BeanProcessorListener implements BeanPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private final ApplicationEventPublisher applicationEventPublisher;

    BeanProcessorListener(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Used for publishing {@link ApplicationContextBeforeInitializeBeanEvent} event for every bean.
     *
     * @param bean     the current bean
     * @param beanName the bean name
     * @return the bean
     */
    @Override
    public Object postProcessBeforeInitialization(@NotNull Object bean, @NotNull String beanName) {
        applicationEventPublisher.publishEvent(
            new ApplicationContextBeforeInitializeBeanEvent(applicationContext, bean, beanName));
        return bean;
    }

    /**
     * Used for publishing {@link ApplicationContextAfterInitializeBeanEvent} event for every bean.
     *
     * @param bean     the current bean
     * @param beanName the bean name
     * @return the bean
     */
    @Override
    public Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName) {
        applicationEventPublisher.publishEvent(
            new ApplicationContextAfterInitializeBeanEvent(applicationContext, bean, beanName));
        return bean;
    }
}