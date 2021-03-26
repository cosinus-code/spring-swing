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

package org.cosinus.swing.form.control;

import org.apache.commons.lang3.StringUtils;
import org.cosinus.swing.error.ValidationException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.text.Format;
import java.text.ParseException;

import static java.awt.Color.red;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class FormattedTextField<T> extends JFormattedTextField implements Control<T> {

    private final Class<T> valueClass;

    private boolean ignoreValidationErrors;

    private Color associatedLabelColor;

    public FormattedTextField(Format format, Class<T> valueClass) {
        super(format);
        injectContext(this);
        this.valueClass = valueClass;
    }

    public FormattedTextField(AbstractFormatter formatter, Class<T> valueClass) {
        super(formatter);
        injectContext(this);
        this.valueClass = valueClass;
    }

    public FormattedTextField(AbstractFormatterFactory factory, Class<T> valueClass) {
        super(factory);
        injectContext(this);
        this.valueClass = valueClass;
    }

    public FormattedTextField(AbstractFormatterFactory factory, Object currentValue, Class<T> valueClass) {
        super(factory, currentValue);
        injectContext(this);
        this.valueClass = valueClass;
    }

    @Override
    public T getValue() {
        if (StringUtils.isEmpty(getText())) {
            return null;
        }

        commitEdit();
        Object value = super.getValue();
        if (value == null) {
            return null;
        }

        if (valueClass.isAssignableFrom(value.getClass())) {
            return valueClass.cast(value);
        }

        if (value instanceof Number) {
            //TODO: to find a more elegant solution
            if (valueClass == Double.class) {
                return (T) Double.valueOf(((Number) value).doubleValue());
            }
            if (valueClass == Float.class) {
                return (T) Float.valueOf(((Number) value).floatValue());
            }
            if (valueClass == Long.class) {
                return (T) Long.valueOf(((Number) value).longValue());
            }
            if (valueClass == Integer.class) {
                return (T) Integer.valueOf(((Number) value).intValue());
            }
        }

        throw new ValidationException(getText(),
                                      format("Cannot convert '%s' to %s",
                                             value,
                                             valueClass));
    }

    @Override
    public void commitEdit() {
        try {
            super.commitEdit();
        } catch (ParseException e) {
            throw new ValidationException(getText(),
                                          format("Cannot parse '%s' to %s",
                                                 getText(),
                                                 valueClass),
                                          e);
        }
    }

    @Override
    protected void processFocusEvent(FocusEvent e) {
        try {
            super.processFocusEvent(e);
            if (!ignoreValidationErrors) {
                ofNullable(associatedLabelColor)
                    .ifPresent(color -> getAssociatedLabel()
                        .ifPresent(label -> label.setForeground(color)));
                associatedLabelColor = null;
            }
        } catch (ValidationException ex) {
            if (!ignoreValidationErrors) {
                //TODO: to do it more generic
                getAssociatedLabel().ifPresent(label -> {
                    if (associatedLabelColor == null) {
                        associatedLabelColor = label.getForeground();
                    }
                    label.setForeground(red);
                });
            }

            throw ex;
        }
    }
}
