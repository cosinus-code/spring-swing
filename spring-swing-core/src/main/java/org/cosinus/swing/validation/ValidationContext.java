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

package org.cosinus.swing.validation;

import java.util.Arrays;
import java.util.List;

public interface ValidationContext {

    void addValidationError(ValidationError validationError);

    List<ValidationError> getValidationErrors();

    boolean hasErrors();

    default void addValidationError(String code, Object... arguments) {
        addValidationError(new ValidationError(code, arguments));
    }

    default void addValidationErrors(List<ValidationError> errors) {
        errors.forEach(this::addValidationError);
    }

    default void addValidationErrors(String... errorCodes) {
        Arrays.stream(errorCodes)
            .map(ValidationError::new)
            .forEach(this::addValidationError);
    }

    default <T> ValidationContext validate(Validator<T> validator, T value) {
        validator.validate(value, this);
        return this;
    }
}
