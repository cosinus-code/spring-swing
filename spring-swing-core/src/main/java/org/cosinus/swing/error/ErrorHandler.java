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

package org.cosinus.swing.error;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.form.error.DefaultErrorFormProvider;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.validation.ValidationError;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static java.lang.Math.min;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.cosinus.swing.dialog.OptionsDialog.showMessageDialog;

/**
 * Generic error handler
 */
public class ErrorHandler {

    private static final Logger LOG = LogManager.getLogger(ErrorHandler.class);

    private final Translator translator;

    private final DefaultErrorFormProvider errorFormProvider;

    public ErrorHandler(Translator translator,
                        DefaultErrorFormProvider errorFormProvider) {
        this.translator = translator;
        this.errorFormProvider = errorFormProvider;
    }

    public void handleError(Window parent, Throwable throwable) {
        LOG.error(throwable.getMessage(), throwable);
        errorFormProvider
            .getErrorForm(parent)
            .showError(throwable);
    }

    public void handleError(Component component,
                            Throwable throwable) {
        LOG.error(throwable.getMessage(), throwable);
        handleError(component,
                    throwable.getLocalizedMessage());
    }

    public void handleError(Component component,
                            String msg) {
        LOG.error(msg);
        showMessageDialog(component,
                          msg,
                          translator.translate("error"),
                          JOptionPane.ERROR_MESSAGE);
    }

    public void handleError(Throwable throwable) {
        handleError(null, throwable);
    }

    public void handleValidationErrors(Component component,
                                       List<ValidationError> validationErrors) {
        String errorMessage = format("<html><body style='width: 300px'>%s</html>",
                                     validationErrors.subList(0, min(10, validationErrors.size()))
                                         .stream()
                                         .map(error -> translator.translate(error.getCode(), error.getArguments()))
                                         .collect(joining("<br/>")));
        handleError(component, errorMessage);
    }
}
