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

package org.cosinus.swing.menu;

import org.cosinus.stream.swing.ExtendedContainer;
import org.cosinus.swing.form.FormComponent;

import javax.swing.*;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Popup menu model
 */
public class PopupMenu extends JPopupMenu implements FormComponent, ExtendedContainer {

    private final Map<String, JMenuItem> menuItemMap;

    public PopupMenu() {
        injectContext(this);
        menuItemMap = new HashMap<>();
    }

    public PopupMenu(String title) {
        super(title);
        injectContext(this);
        menuItemMap = new HashMap<>();
    }

    @Override
    public JMenuItem add(JMenuItem menuItemToAdd) {
        JMenuItem jMenuItem = super.add(menuItemToAdd);
        if (menuItemToAdd instanceof MenuItem menuItem) {
            menuItemMap.put(menuItem.getActionKey(), menuItem);
        } else if (menuItemToAdd instanceof CheckBoxMenuItem menuItem) {
            menuItemMap.put(menuItem.getActionKey(), menuItem);
        } else if (menuItemToAdd instanceof RadioButtonMenuItem menuItem) {
            menuItemMap.put(menuItem.getActionKey(), menuItem);
        }
        return jMenuItem;
    }

    @Override
    public void translate() {
        stream(getSubElements())
            .filter(FormComponent.class::isInstance)
            .map(FormComponent.class::cast)
            .forEach(FormComponent::translate);
    }

    public void setEnabled(String key, boolean enabled) {
        ofNullable(menuItemMap.get(key))
            .ifPresent(menuItem -> menuItem.setEnabled(enabled));
    }

    @Override
    public Stream<Component> streamAdditionalContainers() {
        return stream(getSubElements())
            .map(Component.class::cast);
    }
}
