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

package org.cosinus.swing.boot.config;

import org.cosinus.swing.boot.SpringSwingApplication;
import org.cosinus.swing.boot.event.ApplicationContextStartupConfiguration;
import org.cosinus.swing.context.ApplicationProperties;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Spring Swing.
 */
@Configuration
@ConditionalOnClass(SpringSwingApplication.class)
@EnableConfigurationProperties(ApplicationProperties.class)
@Import({
    ApplicationUIConfiguration.class,
    ApplicationConfiguration.class,
    ApplicationContextStartupConfiguration.class,
})
public @interface SpringSwingAutoConfiguration {
}
