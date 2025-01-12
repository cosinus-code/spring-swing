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
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;
import static org.cosinus.swing.util.ReflectionUtils.setFieldsSafe;
import static org.springframework.beans.BeanUtils.instantiateClass;

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
     * Inject the application into an object.La
     *
     * @param object the object to inject the context into
     */
    public static void injectContext(Object object) {
        if (applicationContext != null) {
            applicationContext.getAutowireCapableBeanFactory().autowireBean(object);

            setFieldsSafe(object,
                Object.class,
                SwingAutowired.class,
                ApplicationContextInjector::getAvailableSwingComponents);
        }
    }

    private static Map<String, Object> getAvailableSwingComponents() {
        return applicationContext.getBeansWithAnnotation(SwingComponent.class)
            .entrySet()
            .stream()
            .filter(entry -> Optional.of(entry.getKey())
                .map(ApplicationContextInjector::getSwingComponentAnnotation)
                .map(SwingComponent::value)
                .stream()
                .flatMap(Arrays::stream)
                .map(ApplicationContextInjector::instantiateCondition)
                .allMatch(condition -> condition.matches(applicationContext, entry.getValue())))
            .collect(toMap(Entry::getKey, Entry::getValue));
    }

    public static SwingComponent getSwingComponentAnnotation(String beanName) {
        return ((AnnotationConfigApplicationContext) applicationContext).getBeanFactory()
            .findAnnotationOnBean(beanName, SwingComponent.class);
    }

    public static SwingCondition instantiateCondition(Class<? extends SwingCondition> conditionClass) {
        return instantiateClass(conditionClass);
    }
}
