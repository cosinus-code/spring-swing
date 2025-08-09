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

package org.cosinus.swing.action.browser;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

import static java.awt.Desktop.Action.BROWSE;
import static java.awt.Desktop.getDesktop;
import static java.awt.Desktop.isDesktopSupported;

public class DefaultBrowser implements Browser {

    @Override
    public void browse(String url) throws IOException {
        if (isDesktopSupported()) {
            Desktop desktop = getDesktop();
            if (desktop.isSupported(BROWSE)) {
                desktop.browse(URI.create(url));
            }
        } else {
            //TODO
        }
    }
}
