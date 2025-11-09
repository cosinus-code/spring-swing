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

import org.springframework.boot.test.context.SpringBootTest.UseMainMethod;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;

import java.util.Arrays;
import java.util.Objects;

import static java.util.Optional.ofNullable;
import static org.springframework.boot.test.context.SpringBootTest.UseMainMethod.NEVER;

public class SpringSwingBootTestAnnotation implements ContextCustomizer {

    private static final String[] NO_ARGS = new String[0];

    private final String[] args;

    private final UseMainMethod useMainMethod;

    public SpringSwingBootTestAnnotation(SpringSwingBootTest springSwingBootTestAnnotation) {
        this.args = ofNullable(springSwingBootTestAnnotation)
            .map(SpringSwingBootTest::args)
            .orElse(NO_ARGS);
        this.useMainMethod = ofNullable(springSwingBootTestAnnotation)
            .map(SpringSwingBootTest::useMainMethod)
            .orElse(NEVER);
    }

    @Override
    public void customizeContext(final ConfigurableApplicationContext context,
                                 final MergedContextConfiguration mergedConfig) {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpringSwingBootTestAnnotation that = (SpringSwingBootTestAnnotation) o;
        return Arrays.equals(args, that.args) && useMainMethod == that.useMainMethod;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(useMainMethod);
        result = 31 * result + Arrays.hashCode(args);
        return result;
    }
}
