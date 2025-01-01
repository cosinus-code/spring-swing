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

import java.lang.annotation.*;

/**
 * {@link @Conditional} that only matches when a beans of the given classes are missing from context.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SwingComponent(value = OnMissingBeanCondition.class)
public @interface OnMissingBeanSwingComponent {

    /**
     * The class types of beans that should be checked.
     * @return the class types of beans to check
     */
    Class<?>[] value() default {};
}
