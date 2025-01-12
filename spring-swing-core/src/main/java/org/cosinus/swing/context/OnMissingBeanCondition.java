/*
 *
 *  * Copyright 2024 Cosinus Software
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */
package org.cosinus.swing.context;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.cosinus.swing.context.ApplicationContextInjector.getSwingComponentAnnotation;

/**
 * {@link SwingCondition} that check if there beans of the given classes are missing from context.
 */
public class OnMissingBeanCondition implements SwingCondition {

    @Override
    public boolean matches(ApplicationContext applicationContext, Object beanToCheck) {
        return ofNullable(beanToCheck.getClass().getAnnotation(OnMissingBeanSwingComponent.class).value())
            .filter(ArrayUtils::isNotEmpty)
            .map(Arrays::stream)
            .orElseGet(() -> Stream.of(beanToCheck.getClass()))
            .flatMap(missingClassToCheck -> applicationContext
                .getBeansOfType(missingClassToCheck)
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() != beanToCheck)
                .filter(entry -> ofNullable(getSwingComponentAnnotation(entry.getKey()))
                        .map(SwingComponent::value)
                        .stream()
                        .flatMap(Arrays::stream)
                        .map(ApplicationContextInjector::instantiateCondition)
                        .allMatch(condition -> condition.matches(applicationContext, entry.getValue()))))
            .findFirst()
            .isEmpty();
    }
}
