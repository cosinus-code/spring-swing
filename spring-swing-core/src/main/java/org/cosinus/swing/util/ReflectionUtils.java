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

package org.cosinus.swing.util;

import org.springframework.beans.BeanUtils;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.util.ReflectionUtils.makeAccessible;
import static org.springframework.util.ReflectionUtils.setField;

/**
 * Reflection utils
 */
public final class ReflectionUtils {

    public static void setFieldsSafe(Object target,
                                     Class<?> baseTargetClass,
                                     Class<? extends Annotation> annotation,
                                     Map<String, Object> beansMap) {
        if (!baseTargetClass.isAssignableFrom(target.getClass())) {
            return;
        }

        List<Field> fields = getFields(target.getClass(), baseTargetClass)
                .filter(field -> Object.class != field.getType())
                .filter(field -> field.getAnnotation(annotation) != null)
                .collect(Collectors.toList());
        beansMap.forEach((beanName, bean) -> setFieldSafe(target,
                                                          fields,
                                                          beanName,
                                                          bean));

    }

    public static Stream<Field> getFields(Class<?> targetClass,
                                          Class<?> baseTargetClass) {
        return Stream.concat(
                Arrays.stream(targetClass.getDeclaredFields()),
                Optional.ofNullable(targetClass.getSuperclass())
                        .filter(c -> baseTargetClass != c)
                        .map(c -> getFields(c, baseTargetClass))
                        .orElseGet(Stream::empty)
        );
    }

    public static void setFieldSafe(Object target,
                                    List<Field> fields,
                                    String beanName,
                                    Object bean) {
        fields.stream()
                .filter(field -> field.getType().isAssignableFrom(bean.getClass()) ||
                        field.getName().equals(beanName))
                .findFirst()
                .ifPresent(field -> {
                    makeAccessible(field);
                    setField(field, target, bean);
                });
    }

    public static Class<?> getClassForName(String name, ClassLoader classLoader) {
        try {
            return ClassUtils.forName(name, classLoader);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unknown class " + name, e);
        }
    }

    public static <T> T createBean(Class<T> instanceClass, Class<?>[] parameterTypes, Object[] args) {
        try {
            Constructor<?> constructor = instanceClass.getDeclaredConstructor(parameterTypes);
            return (T) BeanUtils.instantiateClass(constructor, args);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Cannot instantiate class " + instanceClass, e);
        }
    }

    private ReflectionUtils() {

    }
}
