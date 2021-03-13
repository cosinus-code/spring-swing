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

package org.cosinus.swing.action;

import org.cosinus.swing.context.SpringSwingComponent;
import org.cosinus.swing.error.ActionNotFound;
import org.cosinus.swing.error.ErrorHandler;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Controller for the swing actions
 */
@SpringSwingComponent
public class ActionController<C extends ActionContext> implements ActionListener {

    private final ErrorHandler errorHandler;

    private final KeyMapHandler<C> keyMapHandler;

    private final ActionContextProvider<C> actionContextProvider;

    private final Map<String, ActionInContext<C>> actionMap;

    public ActionController(ErrorHandler errorHandler,
                            KeyMapHandler<C> keyMapHandler,
                            ActionContextProvider<C> actionContextProvider,
                            Set<ActionInContext<C>> actions) {
        this.errorHandler = errorHandler;
        this.keyMapHandler = keyMapHandler;
        this.actionContextProvider = actionContextProvider;
        this.actionMap = actions
                .stream()
                .collect(Collectors.toMap(ActionInContext::getId,
                                          Function.identity()));

    }

    public void runAction(String actionId) {
        runAction(actionId, actionContextProvider.provideActionContext());
    }

    public void runAction(String actionId, C context) {
        try {
            Optional.ofNullable(actionMap.get(actionId))
                    .orElseThrow(() -> new ActionNotFound("Action not implemented (" + actionId + ")"))
                    .run(context);
        } catch (Throwable throwable) {
            errorHandler.handleError(throwable);
        }
    }


    public void runActionByKeyStroke(KeyEvent keyEvent) {
        try {
            KeyStroke keyStroke = KeyStroke.getKeyStroke(keyEvent.getKeyCode(),
                                                         keyEvent.getModifiersEx());
            keyMapHandler.findActionByKeyStroke(keyStroke)
                    .ifPresent(action -> action.run(actionContextProvider.provideActionContext()));
        } catch (Throwable throwable) {
            errorHandler.handleError(throwable);
        }
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof ActionProducer) {
            runAction(((ActionProducer) e.getSource()).getActionKey());
        }
    }
}
