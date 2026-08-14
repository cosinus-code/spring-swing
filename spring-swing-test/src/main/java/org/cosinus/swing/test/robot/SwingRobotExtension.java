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

package org.cosinus.swing.test.robot;

import org.assertj.swing.core.Robot;
import org.assertj.swing.timing.Condition;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.swing.core.BasicRobot.robotWithCurrentAwtHierarchy;
import static org.assertj.swing.timing.Pause.pause;
import static org.assertj.swing.timing.Timeout.timeout;
import static org.cosinus.swing.boot.SwingApplicationFrame.applicationFrame;

/**
 * JUnit 5 {@link org.junit.jupiter.api.extension.Extension} backing the {@link SwingRobotTest}
 * annotation.
 * <p>
 * Waits for the application frame to be shown with its menu and creates a {@link Robot} before
 * each test, resolves it as a parameter of type {@link Robot} on any lifecycle or test method of
 * the annotated class, and cleans it up after each test.
 */
public class SwingRobotExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private static final Namespace NAMESPACE = Namespace.create(SwingRobotExtension.class);
    private static final String ROBOT_KEY = "robot";

    @Override
    public void beforeEach(ExtensionContext context) {
        waitForApplicationFrame();
        Robot robot = robotWithCurrentAwtHierarchy();
        SwingRobot swingRobot = new SwingRobot(robot);
        context.getStore(NAMESPACE).put(ROBOT_KEY, swingRobot);
    }

    private void waitForApplicationFrame() {
        pause(new Condition("application frame is shown with its menu") {
            @Override
            public boolean test() {
                return applicationFrame != null
                    && applicationFrame.isShowing()
                    && applicationFrame.getMenu() != null;
            }
        }, timeout(10, SECONDS));
    }

    @Override
    public void afterEach(ExtensionContext context) {
        SwingRobot swingRobot = context.getStore(NAMESPACE).remove(ROBOT_KEY, SwingRobot.class);
        if (swingRobot != null) {
            swingRobot.cleanUp();
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return SwingRobot.class.isAssignableFrom(parameterContext.getParameter().getType());
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return extensionContext.getStore(NAMESPACE).get(ROBOT_KEY, SwingRobot.class);
    }
}
