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

package org.cosinus.swing.action.execute;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Handler for action executors
 */
public class ActionExecutors {

    private final Map<String, ActionExecutor<? extends ActionModel>> executorsMap;

    public ActionExecutors(Set<ActionExecutor<?>> executors) {
        this.executorsMap = executors
            .stream()
            .collect(Collectors.toMap(ActionExecutor::getHandledAction,
                                      Function.identity()));
    }

    public <A extends ActionModel>
    Optional<ActionExecutor<? extends ActionModel>> getActionExecutor(Class<A> actionClass) {
        return Optional.ofNullable(executorsMap.get(actionClass.getName()));
    }

    public <A extends ActionModel> void execute(A actionModel) {
        getActionExecutor(actionModel.getClass())
            .ifPresent(executor -> ((ActionExecutor<A>) executor).execute(actionModel));
    }
}
