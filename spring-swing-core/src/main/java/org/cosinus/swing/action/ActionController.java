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

package org.cosinus.swing.action;

import org.cosinus.swing.action.execute.ActionModel;
import org.cosinus.swing.error.ActionNotFoundException;
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

import static java.util.Optional.ofNullable;

/**
 * Controller for the swing actions.
 * <p>
 * It is used to control the action executions.
 */
public class ActionController implements ActionListener {

    public static final int KEY_CODE_PAGE_UP = 33;
    public static final int KEY_CODE_PAGE_DOWN = 34;
    public static final int KEY_CODE_END = 35;
    public static final int KEY_CODE_HOME = 36;
    public static final int KEY_CODE_LEFT_ARROW = 37;
    public static final int KEY_CODE_UP_ARROW = 38;
    public static final int KEY_CODE_RIGHT_ARROW = 39;
    public static final int KEY_CODE_DOWN_ARROW = 40;
    public static final int KEY_CODE_F1 = 112;
    public static final int KEY_CODE_F12 = 123;

    private final ErrorHandler errorHandler;

    private final KeyMapHandler keyMapHandler;

    private final Map<String, SwingAction> actionMap;

    public ActionController(final ErrorHandler errorHandler,
                            final KeyMapHandler keyMapHandler,
                            final Set<SwingAction> actions) {
        this.errorHandler = errorHandler;
        this.keyMapHandler = keyMapHandler;
        this.actionMap = actions
            .stream()
            .collect(Collectors.toMap(SwingAction::getId,
                Function.identity()));

    }

    /**
     * Run an action by id.
     *
     * @param actionId the id of the action to execute
     */
    public void runAction(String actionId) {
        try {
            findAction(actionId)
                .orElseThrow(() -> new ActionNotFoundException("Action not implemented (" + actionId + ")"))
                .run();
        } catch (Throwable throwable) {
            errorHandler.handleError(throwable);
        }
    }

    /**
     * Run an action model based by id.
     *
     * @param actionId the id of the action to execute
     * @param actionModel the action model to execute
     */
    public <M extends ActionModel> void runAction(String actionId, M actionModel) {
        try {
            SwingActionWithModel<M> actionWithModel = findAction(actionId)
                .filter(action -> action instanceof SwingActionWithModel)
                .map(action -> (SwingActionWithModel<M>) action)
                .orElseThrow(() -> new ActionNotFoundException("Action not implemented (" + actionId + ")"));

            actionWithModel.run(actionModel);
        } catch (Throwable throwable) {
            errorHandler.handleError(throwable);
        }
    }

    /**
     * Run an action executor based on a key troke defined in a key event.
     * <p>
     * This will search for an action corresponding to the given keystroke
     * and will execute it.
     *
     * @param keyEvent the key event to match the action
     */
    public void runActionByKeyStroke(KeyEvent keyEvent) {
        try {
            KeyStroke keyStroke = KeyStroke.getKeyStroke(keyEvent.getKeyCode(),
                keyEvent.getModifiersEx());
            keyMapHandler.findActionByKeyStroke(keyStroke)
                .ifPresent(action -> {
                    action.run();
                    keyEvent.consume();
                });
        } catch (Throwable throwable) {
            errorHandler.handleError(throwable);
        }
    }

    /**
     * Run an action executor based on an action event.
     *
     * @param event the action event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() instanceof ActionProducer) {
            runAction(((ActionProducer) event.getSource()).getActionKey());
        }
    }

    public boolean isLetterKey(KeyEvent keyEvent) {
        return keyEvent.getKeyCode() >= ' ' &&
            keyEvent.getKeyCode() <= '~' &&
            !keyEvent.isAltDown() &&
            !keyEvent.isControlDown() &&
            !keyEvent.isShiftDown() &&
            !isGoKey(keyEvent) &&
            !isFunctionalKey(keyEvent);
    }

    public boolean isGoKey(KeyEvent keyEvent) {
        return keyEvent.getKeyCode() >= KEY_CODE_PAGE_UP &&
            keyEvent.getKeyCode() <= KEY_CODE_DOWN_ARROW;
    }

    public boolean isFunctionalKey(KeyEvent keyEvent) {
        return keyEvent.getKeyCode() >= KEY_CODE_F1 &&
            keyEvent.getKeyCode() <= KEY_CODE_F12;
    }

    public Optional<SwingAction> findAction(String actionId) {
        return ofNullable(actionMap.get(actionId));
    }
}
