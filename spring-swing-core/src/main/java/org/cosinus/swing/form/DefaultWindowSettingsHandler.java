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

package org.cosinus.swing.form;

import org.cosinus.swing.context.SpringSwingComponent;
import org.cosinus.swing.store.ApplicationStorage;

/**
 * Implementation of {@link WindowSettingsHandler} based on local storage
 */
@SpringSwingComponent
public class DefaultWindowSettingsHandler implements WindowSettingsHandler {

    private static final String FRAME = "frame";

    private static final String KEY_X = "x";

    private static final String KEY_Y = "y";

    private static final String KEY_WIDTH = "width";

    private static final String KEY_HEIGHT = "height";

    private static final String KEY_CENTERED = "centered";

    private static final String KEY_MAXIMIZED = "maximized";

    private final ApplicationStorage applicationStorage;

    public DefaultWindowSettingsHandler(ApplicationStorage applicationStorage) {
        this.applicationStorage = applicationStorage;
    }

    @Override
    public WindowSettings loadWindowSettings(WindowSettings defaultSettings) {
        String name = defaultSettings.getName();
        return defaultSettings
                .setPosition(applicationStorage.getInt(key(name, KEY_X),
                                                       defaultSettings.getX()),
                             applicationStorage.getInt(key(name, KEY_Y),
                                                       defaultSettings.getY()))
                .setSize(applicationStorage.getInt(key(name, KEY_WIDTH),
                                                   defaultSettings.getWidth()),
                         applicationStorage.getInt(key(name, KEY_HEIGHT),
                                                   defaultSettings.getHeight()))
                .setCentered(applicationStorage.getBoolean(key(name, KEY_CENTERED),
                                                           defaultSettings.isCentered()))
                .setMaximized(applicationStorage.getBoolean(key(name, KEY_MAXIMIZED),
                                                            defaultSettings.isMaximized()));
    }

    @Override
    public void saveWindowSettings(WindowSettings settings) {
        String name = settings.getName();

        applicationStorage.saveInt(key(name, KEY_X), settings.getX());
        applicationStorage.saveInt(key(name, KEY_Y), settings.getY());
        applicationStorage.saveInt(key(name, KEY_WIDTH), settings.getWidth());
        applicationStorage.saveInt(key(name, KEY_HEIGHT), settings.getHeight());
        applicationStorage.saveBoolean(key(name, KEY_CENTERED), settings.isCentered());
        applicationStorage.saveBoolean(key(name, KEY_MAXIMIZED), settings.isMaximized());
    }

    private String key(String name, String key) {
        return applicationStorage.key(FRAME, name, key);
    }

}
