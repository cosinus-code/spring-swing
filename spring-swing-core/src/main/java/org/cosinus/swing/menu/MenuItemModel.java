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

public class MenuItemModel {

    private String shortcut;

    private String icon;

    public String getShortcut() {
        return shortcut;
    }

    public String getIcon() {
        return icon;
    }

    public void setShortcut(String shortcut) {
        this.shortcut = shortcut;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
