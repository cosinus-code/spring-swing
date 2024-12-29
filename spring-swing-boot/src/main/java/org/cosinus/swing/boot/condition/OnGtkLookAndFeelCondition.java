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
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import javax.swing.*;

import static java.util.Optional.ofNullable;
import static org.springframework.boot.autoconfigure.condition.ConditionOutcome.match;
import static org.springframework.boot.autoconfigure.condition.ConditionOutcome.noMatch;

/**
 * {@link SpringBootCondition} that check if the current LookAndFeel is Gtk.
 */
public class OnGtkLookAndFeelCondition extends SpringBootCondition {

    public static final String GTK_LOOK_AND_FEEL_NAME = "GTK+";

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ConditionMessage.Builder message = ConditionMessage.forCondition(ConditionalOnGtkLookAndFeel.class);
        return ofNullable(UIManager.getLookAndFeel())
            .map(LookAndFeel::getName)
            .filter(GTK_LOOK_AND_FEEL_NAME::equals)
            .isPresent() ?
            match(message.foundExactly(GTK_LOOK_AND_FEEL_NAME)) :
            noMatch(message.didNotFind(GTK_LOOK_AND_FEEL_NAME).atAll());
    }
}
