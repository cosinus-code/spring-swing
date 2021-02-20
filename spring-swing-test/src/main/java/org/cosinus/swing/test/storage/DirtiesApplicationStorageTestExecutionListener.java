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

package org.cosinus.swing.test.storage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cosinus.swing.store.ApplicationStorage;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestContextAnnotationUtils;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import static java.lang.String.format;

public class DirtiesApplicationStorageTestExecutionListener extends AbstractTestExecutionListener {

    private static final Log logger = LogFactory.getLog(DirtiesApplicationStorageTestExecutionListener.class);

    protected void cleanupApplicationStorage(TestContext testContext) {
        try {
            ApplicationStorage applicationStorage = testContext.getApplicationContext().getBean(ApplicationStorage.class);
            applicationStorage.clean();
        } catch (BeansException ex) {
            logger.error("Cannot find ApplicationStorage bean in context for cleaning test annotated with @DirtiesApplicationStorage", ex);
        }
    }

    @Override
    public void afterTestMethod(@NotNull TestContext testContext) {
        boolean methodAnnotated = null != AnnotatedElementUtils
                .findMergedAnnotation(testContext.getTestMethod(), DirtiesApplicationStorage.class);
        boolean classAnnotated = null != TestContextAnnotationUtils
                .findMergedAnnotation(testContext.getTestClass(), DirtiesApplicationStorage.class);

        if (methodAnnotated || classAnnotated) {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("After test method: context %s, %s annotated with @DirtiesApplicationStorage.",
                                           methodAnnotated ? "method" : "class",
                                           testContext));
            }
            cleanupApplicationStorage(testContext);
        }
    }

    @Override
    public void afterTestClass(@NotNull TestContext testContext) {
        boolean classAnnotated = null != TestContextAnnotationUtils
                .findMergedAnnotation(testContext.getTestClass(), DirtiesApplicationStorage.class);

        if (classAnnotated) {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("After test class: context %s, class annotated with @DirtiesApplicationStorage.", testContext));
            }
            cleanupApplicationStorage(testContext);
        }
    }
}
