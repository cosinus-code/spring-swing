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

package org.cosinus.swing.form;

import javax.swing.*;

import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class Slider extends JSlider {

    public Slider() {
        injectContext(this);
    }

    public Slider(int orientation) {
        super(orientation);
        injectContext(this);
    }

    public Slider(int min, int max) {
        super(min, max);
        injectContext(this);
    }

    public Slider(int min, int max, int value) {
        super(min, max, value);
        injectContext(this);
    }

    public Slider(int orientation, int min, int max, int value) {
        super(orientation, min, max, value);
        injectContext(this);
    }

    public Slider(BoundedRangeModel brm) {
        super(brm);
        injectContext(this);
    }
}
