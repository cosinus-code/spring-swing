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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.IntStream.range;
import static org.springframework.util.ClassUtils.isAssignableValue;

@SpringSwingComponent
public class SwingInjector {

    private static final Logger LOG = LogManager.getLogger(SwingInjector.class);

    private final SwingApplicationContext swingContext;

    public SwingInjector(SwingApplicationContext swingContext) {
        this.swingContext = swingContext;
    }

    public <T extends SwingInject> T inject(Class<T> swingComponentClass,
                                            Object... arguments) {
        try {
            T swingComponent = getConstructor(swingComponentClass, arguments)
                .newInstance(arguments);
            swingComponent.injectSwingContext(swingContext);

            return swingComponent;
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new SwingInjectException(format("Cannot inject %s: failed to call constructor with arguments %s",
                                                  swingComponentClass,
                                                  Arrays.toString(arguments)), e);
        }
    }

    private <T extends SwingInject> Constructor<T> getConstructor(Class<T> swingComponentClass,
                                                                  Object... arguments) {
        return findConstructorForArguments(swingComponentClass, arguments)
            .orElseThrow(() -> new SwingInjectException(format("Cannot inject %s: no constructor for arguments %s",
                                                               swingComponentClass,
                                                               Arrays.toString(arguments))));
    }

    private <T extends SwingInject> Optional<Constructor<T>> findConstructorForArguments(Class<T> swingComponentClass,
                                                                                         Object... arguments) {
        return stream(swingComponentClass.getDeclaredConstructors())
            .filter(constructor -> matchConstructorArguments(constructor, arguments))
            .map(constructor -> (Constructor<T>) constructor)
            .findFirst();
    }

    private <T extends SwingInject> boolean matchConstructorArguments(Constructor<?> constructor,
                                                                      Object... arguments) {
        Class<?>[] argumentClasses = constructor.getParameterTypes();
        return argumentClasses.length == arguments.length &&
            range(0, arguments.length)
                .allMatch(i -> isAssignableValue(argumentClasses[i], arguments[i]));
    }
}
