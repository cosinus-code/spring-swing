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
package org.cosinus.swing.find;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Abstract class for a text finder
 */
public abstract class TextFinder {

    protected final String inputText;

    protected final FindText textToFind;

    /**
     *
     * @param inputText the input text
     * @param textToFind the text to find
     */
    protected TextFinder(String inputText, FindText textToFind) {
        this.inputText = inputText;
        this.textToFind = textToFind;
    }

    public String getInputText() {
        return inputText;
    }

    public FindText getTextToFind() {
        return textToFind;
    }

    /**
     * Try to find the next occurrence of the text to find starting with a given position
     *
     * @param startPosition the start position
     * @return the found result, or {@link Optional#empty()}
     */
    public abstract Optional<FindResult> findNext(int startPosition);

    /**
     * Try to find the next occurrence of the text to find starting with the current position.
     *
     * @return the found result, or {@link Optional#empty()}
     */
    public abstract Optional<FindResult> findNext();

    /**
     * Try to find the previous occurrence of the text to find starting with the current position.
     *
     * @return the found result, or {@link Optional#empty()}
     */
    public abstract Optional<FindResult> findPrevious();

    /**
     * Find all occurrences of the text to find.
     *
     * @return the stream of found results
     */
    public abstract Stream<FindResult> findAll();

    /**
     * Get the current result count.
     *
     * @return the result count
     */
    public abstract int count();

    /**
     * Get the current result index.
     *
     * @return the result index
     */
    public abstract int currentIndex();

    /**
     * Replace the next occurrence of the text to find with the given replacement text.
     *
     * @param replacementText the replacement text
     * @return the resulted overall text after replacement
     */
    public abstract String replaceNext(String replacementText);

    /**
     * Replace all occurrences of the text to find with the given replacement text.
     *
     * @param replacementText the replacement text
     * @return the resulted overall text after replacement
     */
    public abstract String replaceAll(String replacementText);

    /**
     * Get the current find.
     *
     * @return the current find
     */
    public abstract FindResult getCurrentFind();
}
