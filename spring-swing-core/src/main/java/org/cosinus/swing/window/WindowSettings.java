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

package org.cosinus.swing.window;

/**
 * Encapsulates the info about window
 */
public class WindowSettings {

    public static final int DEFAULT_WIDTH = 800;

    public static final int DEFAULT_HEIGHT = 600;

    private final String name;

    private final String title;

    private String icon;

    private String menu;

    private int x;

    private int y;

    private int width = DEFAULT_WIDTH;

    private int height = DEFAULT_HEIGHT;

    private Integer oldX;

    private Integer oldY;

    private Integer oldWidth;

    private Integer oldHeight;

    private boolean maximized;

    private boolean centered = true;

    private boolean exitOnEscape;

    public WindowSettings(String name,
                          String title) {
        this.name = name;
        this.title = title;
    }

    public WindowSettings(String name,
                          String title,
                          String icon,
                          String menu) {
        this.name = name;
        this.title = title;
        this.icon = icon;
        this.menu = menu;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getIcon() {
        return icon;
    }

    public String getMenu() {
        return menu;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public WindowSettings setPosition(int x, int y) {
        this.oldX = this.x;
        this.oldY = this.y;
        this.x = x;
        this.y = y;
        return this;
    }

    public WindowSettings setSize(int width, int height) {
        this.oldWidth = this.width;
        this.oldHeight = this.height;
        this.width = width;
        this.height = height;
        return this;
    }

    public boolean isMaximized() {
        return maximized;
    }

    public WindowSettings setMaximized(boolean maximized) {
        this.maximized = maximized;
        return this;
    }

    public boolean isCentered() {
        return centered;
    }

    public WindowSettings setCentered(boolean centered) {
        this.centered = centered;
        return this;
    }

    public boolean isExitOnEscape() {
        return exitOnEscape;
    }

    public WindowSettings setExitOnEscape(boolean exitOnEscape) {
        this.exitOnEscape = exitOnEscape;
        return this;
    }

    public WindowSettings resetOldPositionSize() {
        if (this.oldX != null && this.oldY != null) {
            this.x = this.oldX;
            this.y = this.oldY;
            this.oldX = null;
            this.oldY = null;
        }
        if (this.oldWidth != null && this.oldHeight != null) {
            this.width = this.oldWidth;
            this.height = this.oldHeight;
            this.oldWidth = null;
            this.oldHeight = null;
        }
        return this;
    }
}
