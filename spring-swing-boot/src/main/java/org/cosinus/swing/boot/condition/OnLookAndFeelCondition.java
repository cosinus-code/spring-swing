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

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import javax.swing.*;
import java.util.Arrays;
import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static org.springframework.boot.autoconfigure.condition.ConditionOutcome.match;
import static org.springframework.boot.autoconfigure.condition.ConditionOutcome.noMatch;

/**
 * {@link SpringBootCondition} for controlling what implementation of beans to be instantiated
 * based on current look and feel.
 * <p>
 * The current value of system property "os.name" should start with the value
 * specified in the {@link ConditionalOnOperatingSystem} annotation
 */
public class OnLookAndFeelCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnLookAndFeel.class.getName());
        String[] lookAndFeels = ofNullable(attributes)
            .map(attrs -> attrs.get("value"))
            .map(value -> (String[]) value)
            .orElseGet(() -> new String[]{});

        ConditionMessage.Builder message = ConditionMessage.forCondition(ConditionalOnCloudPlatform.class);
        return isCurrentLookAndFeelOneOf() ?
            match(message.foundExactly(Arrays.toString(lookAndFeels))) :
            noMatch(message.didNotFind(Arrays.toString(lookAndFeels)).atAll());
    }

    public boolean isCurrentLookAndFeelOneOf(String... lookAndFeels) {
        return ofNullable(UIManager.getLookAndFeel())
            .map(LookAndFeel::getName)
            .map(lookAndFeel -> stream(lookAndFeels)
                .anyMatch(lookAndFeel::startsWith))
            .orElse(false);
    }
}
