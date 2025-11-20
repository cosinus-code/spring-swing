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

package org.cosinus.swing.icon;

import javax.swing.*;

import static org.cosinus.swing.icon.IconSize.X16;

public interface IconHolder {

    default IconSize getIconSize() {
        return X16;
    }

    String getIconName();

    Icon getIcon();

    void setIcon(Icon icon);

    default String getTooltip() {
        return null;
    }
}
