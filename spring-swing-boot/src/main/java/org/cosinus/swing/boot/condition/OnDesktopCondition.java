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
package org.cosinus.swing.boot.condition;

import org.springframework.boot.autoconfigure.condition.ConditionMessage.Builder;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Arrays;
import java.util.Map;

import static java.lang.System.getenv;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static org.springframework.boot.autoconfigure.condition.ConditionMessage.forCondition;
import static org.springframework.boot.autoconfigure.condition.ConditionOutcome.match;
import static org.springframework.boot.autoconfigure.condition.ConditionOutcome.noMatch;

/**
 * {@link SpringBootCondition} for controlling what implementation of beans to be instantiated
 * based on current desktop.
 * <p>
 * The environment variable XDG_CURRENT_DESKTOP should be one of the values
 * specified in the {@link ConditionalOnDesktop} annotation
 */
public class OnDesktopCondition extends SpringBootCondition {

    public static final String XDG_CURRENT_DESKTOP = "XDG_CURRENT_DESKTOP";

    @Override
    public ConditionOutcome getMatchOutcome(final ConditionContext context,
                                            final AnnotatedTypeMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnDesktop.class.getName());
        String[] desktops = ofNullable(attributes)
            .map(attrs -> attrs.get("value"))
            .map(value -> (String[]) value)
            .orElseGet(() -> new String[]{});

        Builder message = forCondition(ConditionalOnDesktop.class);
        return isCurrentDesktopOneOf(desktops) ?
            match(message.foundExactly(Arrays.toString(desktops))) :
            noMatch(message.didNotFind(Arrays.toString(desktops)).atAll());
    }

    public boolean isCurrentDesktopOneOf(String... desktops) {
        String desktop = getenv(XDG_CURRENT_DESKTOP);
        return stream(desktops)
            .anyMatch(desktop::contains);
    }
}
