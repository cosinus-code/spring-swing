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

package org.cosinus.swing.ui.dark;

import com.bulenkov.darcula.DarculaLaf;

import javax.swing.UIManager.LookAndFeelInfo;

/**
 * Dark look-and-feel bean
 */
public class DarkLookAndFeel extends LookAndFeelInfo {

    public DarkLookAndFeel() {
        super(DarculaLaf.NAME, DarculaLaf.class.getName());
    }

    public DarkLookAndFeel(String name, String className) {
        super(name, className);
    }
}
