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

package org.cosinus.swing.action.execute;

/**
 * Generic action executor interface
 */
public interface ActionExecutor<A extends ActionModel> {

    /**
     * Execute action.
     *
     * @param actionModel the action to execute
     */
    void execute(A actionModel);

    /**
     * Cancel action execution.
     *
     * @param executionId the id of the action to cancel
     */
    void cancel(String executionId);

    /**
     * Remove action execution.
     *
     * @param executionId the execution id
     */
    void remove(String executionId);

    /**
     * Get the action handled by this executor
     *
     * @return the action
     */
    String getHandledAction();
}
