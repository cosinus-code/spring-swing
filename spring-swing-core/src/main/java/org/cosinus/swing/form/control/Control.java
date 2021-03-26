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

package org.cosinus.swing.form.control;

import javax.swing.*;
import java.util.Optional;

import static javax.accessibility.AccessibleRelation.LABELED_BY;

public interface Control<T> {

    T getValue();

    void setValue(T realValue);

    default JLabel createAssociatedLabel(String label) {
        JLabel controlLabel = new JLabel(label);
        Optional.of(this)
            .filter(control -> JComponent.class.isAssignableFrom(control.getClass()))
            .map(JComponent.class::cast)
            .ifPresent(controlLabel::setLabelFor);
        return controlLabel;
    }

    default Optional<JLabel> getAssociatedLabel() {
        return Optional.of(this)
            .filter(control -> JComponent.class.isAssignableFrom(control.getClass()))
            .map(JComponent.class::cast)
            .map(component -> component.getClientProperty(LABELED_BY))
            .filter(label -> JLabel.class.isAssignableFrom(label.getClass()))
            .map(JLabel.class::cast);
    }
}
