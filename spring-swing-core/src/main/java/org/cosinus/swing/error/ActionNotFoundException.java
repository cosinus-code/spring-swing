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

package org.cosinus.swing.error;

import java.io.Serial;

/**
 * Indicates that an action was not found.
 */
public class ActionNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -8028789626344404281L;

    public ActionNotFoundException() {
        this("Action not found");
    }

    public ActionNotFoundException(String message) {
        this(message, null);
    }

    public ActionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
