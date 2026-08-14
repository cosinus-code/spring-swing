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

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be specified on a test class to get an AssertJ-Swing
 * {@link org.assertj.swing.core.Robot} managed automatically.
 * <p>
 * Before each test, waits for the application frame to be shown with its menu and creates a
 * {@link org.assertj.swing.core.Robot}, then cleans it up after each test. Any lifecycle or test
 * method on the annotated class can declare a {@link org.assertj.swing.core.Robot} parameter to
 * receive it, with no field declaration needed.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ExtendWith(SwingRobotExtension.class)
public @interface SwingRobotTest {
}
