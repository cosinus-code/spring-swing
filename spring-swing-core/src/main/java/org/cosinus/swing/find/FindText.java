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

package org.cosinus.swing.find;

import java.util.Objects;

public class FindText {

    private String text;

    private boolean caseSensitive;

    private boolean wholeWord;

    private boolean regularExpression;

    public FindText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public FindText caseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        return this;
    }

    public boolean isWholeWord() {
        return wholeWord;
    }

    public FindText wholeWord(boolean wholeWord) {
        this.wholeWord = wholeWord;
        return this;
    }

    public boolean isRegularExpression() {
        return regularExpression;
    }

    public FindText regularExpression(boolean regularExpression) {
        this.regularExpression = regularExpression;
        return this;
    }

    public static FindText buildFindText(String text) {
        return new FindText(text);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FindText findText = (FindText) o;
        return caseSensitive == findText.caseSensitive &&
            wholeWord == findText.wholeWord &&
            regularExpression == findText.regularExpression &&
            Objects.equals(text, findText.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, caseSensitive, wholeWord, regularExpression);
    }
}
