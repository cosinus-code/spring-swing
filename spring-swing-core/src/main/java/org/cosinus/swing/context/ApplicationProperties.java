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

package org.cosinus.swing.context;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import static java.util.Optional.ofNullable;

/**
 * Application properties
 */
@ConfigurationProperties(prefix = "swing.application")
public class ApplicationProperties {

    private String name;

    private String version;

    private String icon;

    private String home;

    private String menu = "menu";

    private String iconsPath;

    @NestedConfigurationProperty
    private Frame frame = new Frame();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return ofNullable(version)
            .orElseGet(this::getManifestVersion);
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public String getMenu() {
        return menu;
    }

    public void setMenu(String menu) {
        this.menu = menu;
    }

    public Frame getFrame() {
        return frame;
    }

    public void setFrame(Frame frame) {
        this.frame = frame;
    }

    public String getIconsPath() {
        return iconsPath;
    }

    public void setIconsPath(String iconsPath) {
        this.iconsPath = iconsPath;
    }

    public String getManifestVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    public String userAgent() {
        return ofNullable(getVersion())
            .map("/"::concat)
            .map(getName()::concat)
            .orElseGet(this::getName);
    }

    public static class Frame {

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
