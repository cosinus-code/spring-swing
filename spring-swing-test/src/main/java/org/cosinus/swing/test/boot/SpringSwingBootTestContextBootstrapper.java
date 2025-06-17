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

package org.cosinus.swing.test.boot;

import org.cosinus.swing.boot.SpringSwingApplication;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextLoader;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.TestContextBootstrapper;

import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static org.springframework.test.context.TestContextAnnotationUtils.findMergedAnnotation;

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
        setSpringSwingApplicationClass(testClass);
        return ofNullable(testClass)
            .map(this::getSpringSwingBootTestAnnotation)
            .map(SpringSwingBootTest::properties)
            .orElse(null);
    }

    @Override
    protected Class<?>[] getClasses(Class<?> testClass) {
        setSpringSwingApplicationClass(testClass);
        return ofNullable(testClass)
            .map(this::getSpringSwingBootTestAnnotation)
            .map(SpringSwingBootTest::classes)
            .orElse(null);
    }

    protected SpringSwingBootTest getSpringSwingBootTestAnnotation(Class<?> testClass) {
        return AnnotatedElementUtils.getMergedAnnotation(testClass, SpringSwingBootTest.class);
    }

    @Override
    protected void verifyConfiguration(Class<?> testClass) {
    }

    private void setSpringSwingApplicationClass(Class<?> testClass) {
        if (SpringSwingApplication.applicationClass == null && testClass != null) {
            SpringSwingApplication.applicationClass = testClass;
        }
    }

    @Override
    protected MergedContextConfiguration processMergedContextConfiguration(MergedContextConfiguration mergedConfig) {
        SpringSwingBootTest springSwingBootTestAnnotation =
            findMergedAnnotation(mergedConfig.getTestClass(), SpringSwingBootTest.class);

        Set<ContextCustomizer> contextCustomizers = new LinkedHashSet<>(mergedConfig.getContextCustomizers());
        contextCustomizers.add(new SpringSwingBootTestAnnotation(springSwingBootTestAnnotation));

        MergedContextConfiguration mergedContextConfiguration = super.processMergedContextConfiguration(mergedConfig);
        return new MergedContextConfiguration(
            mergedContextConfiguration.getTestClass(),
            mergedContextConfiguration.getLocations(),
            mergedContextConfiguration.getClasses(),
            mergedContextConfiguration.getContextInitializerClasses(),
            mergedContextConfiguration.getActiveProfiles(),
            mergedContextConfiguration.getPropertySourceLocations(),
            mergedContextConfiguration.getPropertySourceProperties(),
            contextCustomizers,
            mergedContextConfiguration.getContextLoader(),
            getCacheAwareContextLoaderDelegate(),
            mergedContextConfiguration.getParent());
    }
}
