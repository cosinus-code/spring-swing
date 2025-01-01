/*
 *
 *  * Copyright 2024 Cosinus Software
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */
package org.cosinus.swing.context;

import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.ApplicationContext;

import javax.swing.*;

import static java.util.Optional.ofNullable;

/**
 * {@link SpringBootCondition} that check if the current LookAndFeel is Gtk.
 */
public class OnGtkLookAndFeelCondition implements SwingCondition {

    public static final String GTK_LOOK_AND_FEEL_NAME = "GTK";

    @Override
    public boolean matches(ApplicationContext applicationContext, Object beanToCheck) {
        return ofNullable(UIManager.getLookAndFeel())
            .map(LookAndFeel::getID)
            .filter(GTK_LOOK_AND_FEEL_NAME::equals)
            .isPresent();
    }
}
