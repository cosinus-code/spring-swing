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

import static org.cosinus.swing.os.OperatingSystem.LINUX;
import static org.cosinus.swing.os.OperatingSystem.isLinux;
import static org.springframework.boot.autoconfigure.condition.ConditionMessage.forCondition;
import static org.springframework.boot.autoconfigure.condition.ConditionOutcome.match;
import static org.springframework.boot.autoconfigure.condition.ConditionOutcome.noMatch;

/**
 * {@link SpringBootCondition} that check if the current operating system is Linux.
 */
public class OnLinuxCondition extends SpringBootCondition {
    @Override
    public ConditionOutcome getMatchOutcome(final ConditionContext context,
                                            final AnnotatedTypeMetadata metadata) {
        Builder message = forCondition(ConditionalOnLinux.class);
        return isLinux() ?
            match(message.foundExactly(LINUX.getName())) :
            noMatch(message.didNotFind(LINUX.getName()).atAll());
    }
}
