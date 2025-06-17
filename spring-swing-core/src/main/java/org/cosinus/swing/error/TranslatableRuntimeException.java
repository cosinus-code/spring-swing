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

import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class TranslatableRuntimeException extends RuntimeException {

    @Autowired
    private Translator translator;

    private final String messageKey;

    private Object[] messageArguments;

    public TranslatableRuntimeException(String messageKey, Object[] messageArguments) {
        super(messageKey);
        injectContext(this);
        this.messageKey = messageKey;
        this.messageArguments = messageArguments;
    }

    public TranslatableRuntimeException(String messageKey) {
        super(messageKey);
        injectContext(this);
        this.messageKey = messageKey;
    }

    public TranslatableRuntimeException(Throwable cause, String messageKey) {
        super(messageKey, cause);
        injectContext(this);
        this.messageKey = messageKey;
    }

    public TranslatableRuntimeException(Throwable cause, String messageKey, Object[] messageArguments) {
        super(messageKey, cause);
        injectContext(this);
        this.messageKey = messageKey;
        this.messageArguments = messageArguments;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getMessageArguments() {
        return messageArguments;
    }

    @Override
    public String getLocalizedMessage() {
        return translator != null && messageKey != null ?
            translator.translate(messageKey, messageArguments) :
            super.getLocalizedMessage();
    }
}
