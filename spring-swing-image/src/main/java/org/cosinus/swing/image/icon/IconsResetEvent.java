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

package org.cosinus.swing.image.icon;

import org.springframework.context.ApplicationEvent;

/**
 * Event published on the event dispatch thread after the icons were reset and the icon holders updated,
 * like after the icon theme changed.
 * <p>
 * The icons cache is empty at this point, so the listeners can load again the icons they show,
 * like the file icons of a list, with the new icon theme.
 */
public class IconsResetEvent extends ApplicationEvent {

    public IconsResetEvent(final Object source) {
        super(source);
    }
}
