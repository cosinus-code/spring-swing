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

package org.cosinus.swing.test.boot;

import org.cosinus.swing.test.storage.TestApplicationStorageConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.BootstrapWith;

import java.lang.annotation.*;

/**
 * Annotation that can be specified on a test class that runs Spring Swing Boot based tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@BootstrapWith(SpringSwingBootTestContextBootstrapper.class)
@Import(TestApplicationStorageConfiguration.class)
public @interface SpringSwingBootTest {

    String[] properties() default {};

    Class<?>[] classes() default {};
}
