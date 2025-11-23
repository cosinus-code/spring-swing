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

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import static java.util.Optional.ofNullable;

/**
 * Application properties
 */
@ConfigurationProperties(prefix = "swing.application")
public class ApplicationProperties {

    @Getter
    @Setter
    private String name;

    @Setter
    private String version;

    @Getter
    @Setter
    private String icon;

    @Getter
    @Setter
    private String home;

    @Getter
    @Setter
    private String menu = "menu";

    @Getter
    @Setter
    private String iconsPath;

    @Getter
    @Setter
    @NestedConfigurationProperty
    private Frame frame = new Frame();

    public String getVersion() {
        return ofNullable(version)
            .orElseGet(this::getManifestVersion);
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

    @Getter
    @Setter
    public static class Frame {

        private String name;
    }
}
