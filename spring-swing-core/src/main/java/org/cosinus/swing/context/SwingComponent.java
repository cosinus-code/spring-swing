/*
 *
 *  * Copyright 2024 Cosinus Software
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package org.cosinus.swing.context;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Conditional for lazy inject dependency on {@link org.cosinus.swing.context.ApplicationContextInjector}.
 * Used when the condition cannot be evaluated on application startup, before initializing it,
 * like for look-and-feel related conditions.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface SwingComponent {

    /**
     * All {@link SwingCondition} classes that must matches the current application context.
     */
    Class<? extends SwingCondition>[] value() default {};
}