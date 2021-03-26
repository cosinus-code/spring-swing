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

package org.cosinus.swing.preference;

public class PreferenceException extends RuntimeException {

    private String preferenceName;

    private String preferenceValue;

    public PreferenceException(String preferenceName, String preferenceValue, String message) {
        super(message);
        this.preferenceName = preferenceName;
        this.preferenceValue = preferenceValue;
    }

    public PreferenceException(String preferenceName, String preferenceValue, String message, Throwable cause) {
        super(message, cause);
        this.preferenceName = preferenceName;
        this.preferenceValue = preferenceValue;
    }

    public String getPreferenceName() {
        return preferenceName;
    }

    public String getPreferenceValue() {
        return preferenceValue;
    }
}
