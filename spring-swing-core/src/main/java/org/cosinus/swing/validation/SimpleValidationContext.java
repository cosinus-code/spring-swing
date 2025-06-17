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

package org.cosinus.swing.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple implementation of the {@link ValidationContext}
 */
public class SimpleValidationContext implements ValidationContext {

    private final List<ValidationError> errors;

    public SimpleValidationContext() {
        this.errors = new ArrayList<>();
    }

    /**
     * Add a validation error to the context.
     *
     * @param validationError the validation error to add
     */
    @Override
    public void addValidationError(ValidationError validationError) {
        errors.add(validationError);
    }

    /**
     * Get the current validation errors.
     *
     * @return the validation errors
     */
    @Override
    public List<ValidationError> getValidationErrors() {
        return errors;
    }

    /**
     * Check if there are validation errors in context.
     *
     * @return true if there are validation errors in context
     */
    @Override
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
