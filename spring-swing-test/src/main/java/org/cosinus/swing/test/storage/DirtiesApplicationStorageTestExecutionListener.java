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

package org.cosinus.swing.test.storage;

import lombok.extern.slf4j.Slf4j;
import org.cosinus.swing.store.ApplicationStorage;
import org.springframework.beans.BeansException;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestContextAnnotationUtils;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import static java.util.Optional.ofNullable;

/**
 * Implementation of {@link TestExecutionListener}
 * that do cleanup of the application storage after the test performed,
 * depending on {@link DirtiesApplicationStorage} annotation presence and value.
 * <p>
 * The cleanup is done even if {@link DirtiesApplicationStorage} is not present.
 * Only the value <code>false</code> for {@link DirtiesApplicationStorage} don't trigger the cleanup.
 */
@Slf4j
public class DirtiesApplicationStorageTestExecutionListener extends AbstractTestExecutionListener {

    /**
     * Perform application storage cleanup.
     *
     * @param testContext the test context
     */
    protected void cleanupApplicationStorage(TestContext testContext) {
        try {
            testContext
                .getApplicationContext()
                .getBean(ApplicationStorage.class)
                .clear();
        } catch (BeansException ex) {
            log.error("Cannot find ApplicationStorage bean in context for cleaning test annotated with " +
                "@DirtiesApplicationStorage", ex);
        }
    }

    @Override
    public void afterTestMethod(TestContext testContext) {
        DirtiesApplicationStorage methodAnnotation = AnnotatedElementUtils
            .findMergedAnnotation(testContext.getTestMethod(), DirtiesApplicationStorage.class);

        DirtiesApplicationStorage classAnnotation = TestContextAnnotationUtils
            .findMergedAnnotation(testContext.getTestClass(), DirtiesApplicationStorage.class);

        boolean dirtiesApplicationStorage =
            ofNullable(methodAnnotation)
                .or(() -> ofNullable(classAnnotation))
                .map(DirtiesApplicationStorage::value)
                .orElse(true);

        if (dirtiesApplicationStorage) {
            if (log.isDebugEnabled()) {
                if (methodAnnotation != null) {
                    log.debug("After test method: context {}, method annotated with @DirtiesApplicationStorage.",
                        testContext);
                } else if (classAnnotation != null) {
                    log.debug("After test method: context {}, class annotated with @DirtiesApplicationStorage.",
                        testContext);
                } else {
                    log.debug("After test method: context {}, no @DirtiesApplicationStorage annotation.",
                        testContext);
                }
            }
            cleanupApplicationStorage(testContext);
        }
    }

    @Override
    public void afterTestClass(TestContext testContext) {
        DirtiesApplicationStorage classAnnotation = TestContextAnnotationUtils
            .findMergedAnnotation(testContext.getTestClass(), DirtiesApplicationStorage.class);

        boolean dirtiesApplicationStorage =
            ofNullable(classAnnotation)
                .map(DirtiesApplicationStorage::value)
                .orElse(true);

        if (dirtiesApplicationStorage) {
            if (log.isDebugEnabled()) {
                if (classAnnotation != null) {
                    log.debug("After test class: context {}, class annotated with @DirtiesApplicationStorage.",
                        testContext);
                } else {
                    log.debug("After test class: context {}, no @DirtiesApplicationStorage annotation.",
                        testContext);
                }
            }
            cleanupApplicationStorage(testContext);
        }
    }
}
