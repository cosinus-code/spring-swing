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

package org.cosinus.swing.test.model;

import org.cosinus.swing.action.ActionController;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.form.Frame;
import org.cosinus.swing.form.WindowSettingsHandler;
import org.cosinus.swing.menu.MenuProvider;
import org.cosinus.swing.resource.ResourceResolver;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.ui.ApplicationUIHandler;

public class TestFrame extends Frame {

    public ActionController getActionController() {
        return actionController;
    }

    public Translator getTranslator() {
        return translator;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public ResourceResolver getResourceResolver() {
        return resourceResolver;
    }

    public WindowSettingsHandler getWindowSettingsHandler() {
        return frameSettingsHandler;
    }

    public MenuProvider getMenuProvider() {
        return menuProvider;
    }

    public ApplicationUIHandler getUIHandler() {
        return uiHandler;
    }

}
