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

package org.cosinus.swing.boot.test;

import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.test.context.ContextLoader;
import org.springframework.test.context.TestContextBootstrapper;

import java.util.Optional;

import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedAnnotation;

/**
 * {@link TestContextBootstrapper} for Spring Swing Boot.
 */
public class SpringSwingBootTestContextBootstrapper extends SpringBootTestContextBootstrapper {

    @Override
    protected Class<? extends ContextLoader> getDefaultContextLoaderClass(Class<?> testClass) {
        return SpringSwingBootContextLoader.class;
    }

    @Override
    protected WebEnvironment getWebEnvironment(Class<?> testClass) {
        return null;
    }

    @Override
    protected String[] getProperties(Class<?> testClass) {
        return Optional.ofNullable(testClass)
                .map(this::getSpringSwingBootTestAnnotation)
                .map(SpringSwingBootTest::properties)
                .orElse(null);
    }

    @Override
    protected Class<?>[] getClasses(Class<?> testClass) {
        return Optional.ofNullable(testClass)
                .map(this::getSpringSwingBootTestAnnotation)
                .map(SpringSwingBootTest::classes)
                .orElse(null);
    }

    protected SpringSwingBootTest getSpringSwingBootTestAnnotation(Class<?> testClass) {
        return getMergedAnnotation(testClass, SpringSwingBootTest.class);
    }

    @Override
    protected void verifyConfiguration(Class<?> testClass) {
    }
}
