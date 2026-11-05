/*
 * Copyright 2026 Cosinus Software
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

package org.cosinus.swing.test;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.swing.SwingUtilities;

/**
 * Runs a test, with its set up, on the event dispatch thread, as swing requires for its components.
 */
public class EventDispatchThreadRule implements TestRule {

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Throwable[] failure = new Throwable[1];
                SwingUtilities.invokeAndWait(() -> {
                    try {
                        base.evaluate();
                    } catch (Throwable throwable) {
                        failure[0] = throwable;
                    }
                });
                if (failure[0] != null) {
                    throw failure[0];
                }
            }
        };
    }
}
