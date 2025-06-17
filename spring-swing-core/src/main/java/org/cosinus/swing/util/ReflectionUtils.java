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

package org.cosinus.swing.util;

import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.concat;
import static org.springframework.beans.BeanUtils.instantiateClass;
import static org.springframework.util.ReflectionUtils.makeAccessible;
import static org.springframework.util.ReflectionUtils.setField;

/**
 * Reflection utils
 */
public final class ReflectionUtils {

    public static void setFieldsSafe(
        final Object target,
        final Class<?> baseTargetClass,
        final Class<? extends Annotation> annotation,
        final Supplier<Map<String, Object>> beansMapSupplier) {

        if (!baseTargetClass.isAssignableFrom(target.getClass())) {
            return;
        }

        List<Field> annotatedFields = getFields(target.getClass(), baseTargetClass)
            .filter(field -> Object.class != field.getType())
            .filter(field -> field.getAnnotation(annotation) != null)
            .toList();

        if (!annotatedFields.isEmpty()) {
            Map<String, Object> beansMap = beansMapSupplier.get();
            annotatedFields.forEach(field -> findBeanForField(field, beansMap)
                .ifPresent(bean -> {
                    makeAccessible(field);
                    setField(field, target, bean);
                }));
        }
    }

    private static Optional<Object> findBeanForField(
        final Field field,
        final Map<String, Object> beansMap) {

        return ofNullable(beansMap.get(field.getName()))
            .or(() -> Optional.of(findBeanForField(field, beansMap.values())));
    }

    private static Object findBeanForField(
        final Field field,
        final Collection<Object> availableBeans) {
        List<Object> beans = availableBeans
            .stream()
            .filter(bean -> field.getType().isAssignableFrom(bean.getClass()))
            .toList();

        if (beans.size() != 1) {
            throw new BeanDefinitionValidationException(format("Expected one bean of type %s but found %d: %s",
                field.getType(),
                beans.size(),
                beans
                    .stream()
                    .map(Object::getClass)
                    .map(Class::getName)
                    .collect(joining(", "))));
        }

        return beans.get(0);
    }

    public static Stream<Field> getFields(Class<?> targetClass,
                                          Class<?> baseTargetClass) {
        return concat(
            stream(targetClass.getDeclaredFields()),
            ofNullable(targetClass.getSuperclass())
                .filter(c -> baseTargetClass != c)
                .map(c -> getFields(c, baseTargetClass))
                .orElseGet(Stream::empty)
        );
    }

    public static void setFieldSafe(Object target, List<Field> fields, String beanName, Object bean) {
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
            return (T) instantiateClass(constructor, args);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Cannot instantiate class " + instanceClass, e);
        }
    }

    private ReflectionUtils() {

    }
}
