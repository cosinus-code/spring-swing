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
package org.cosinus.swing.boot.condition;

import org.apache.commons.lang3.SystemUtils;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;
import java.util.Optional;

public class OnOperatingSystemCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnOperatingSystem.class.getName());
        String operatingSystem = (String) attributes.get("value");

        ConditionMessage.Builder message = ConditionMessage.forCondition(ConditionalOnCloudPlatform.class);
        return isOs(operatingSystem) ?
                ConditionOutcome.match(message.foundExactly(operatingSystem)) :
                ConditionOutcome.noMatch(message.didNotFind(operatingSystem).atAll());
    }

    public boolean isOs(String name) {
        return Optional.ofNullable(name)
                .filter(os -> SystemUtils.OS_NAME.startsWith(os))
                .isPresent();
    }
}