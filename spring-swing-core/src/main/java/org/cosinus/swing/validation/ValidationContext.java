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

import java.util.Arrays;
import java.util.List;

/**
 * Validation context interface to be used in link with a {@link Validator}
 */
public interface ValidationContext {

    /**
     * Add a validation error to the context.
     *
     * @param validationError the validation error to add
     */
    void addValidationError(ValidationError validationError);

    /**
     * Get the current validation errors.
     *
     * @return the validation errors
     */
    List<ValidationError> getValidationErrors();

    /**
     * Check if there are validation errors in context.
     *
     * @return true if there are validation errors in context
     */
    boolean hasErrors();

    /**
     * Add validation error to the context.
     *
     * @param code      the code of error
     * @param arguments the arguments of the error
     */
    default void addValidationError(String code, Object... arguments) {
        addValidationError(new ValidationError(code, arguments));
    }

    /**
     * Add validation errors to the context.
     *
     * @param errors the validation errors to add
     */
    default void addValidationErrors(List<ValidationError> errors) {
        errors.forEach(this::addValidationError);
    }

    /**
     * Add validation errors to the context.
     *
     * @param errorCodes the codes of the validation errors
     */
    default void addValidationErrors(String... errorCodes) {
        Arrays.stream(errorCodes)
            .map(ValidationError::new)
            .forEach(this::addValidationError);
    }

    /**
     * Intermediate a validator validation in this validation context.
     *
     * @param validator the validator to intermediate
     * @param value     the value to validate
     * @param <T>       the type of the value to validate
     * @return this
     */
    default <T> ValidationContext validate(Validator<T> validator, T value) {
        validator.validate(value, this);
        return this;
    }
}
