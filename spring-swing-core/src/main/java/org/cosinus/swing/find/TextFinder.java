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

import lombok.Getter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Abstract class for a text finder
 */
@Getter
public abstract class TextFinder {

    /**
     * The searched text, kept up to date with the changes reported by
     * {@link #textInserted(int, String)} and {@link #textRemoved(int, int)}.
     */
    protected String inputText;

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

    /**
     * Insert a text in the searched text,
     * and move the found results after it (see {@link FindResult#textInserted(int, int)}).
     * <p>
     * The new occurrences of the text to find are not searched: see {@link #refresh()}.
     *
     * @param offset the offset of the inserted text
     * @param text   the inserted text
     * @return the found results which can be changed by the insertion, with their new positions and texts
     */
    public abstract List<FindResult> textInserted(int offset, String text);

    /**
     * Remove a text from the searched text,
     * and move the found results after it (see {@link FindResult#textRemoved(int, int)}).
     * <p>
     * The new occurrences of the text to find are not searched: see {@link #refresh()}.
     *
     * @param offset the offset of the removed text
     * @param length the length of the removed text
     * @return the found results which can be changed by the removal, with their new positions and texts
     */
    public abstract List<FindResult> textRemoved(int offset, int length);

    /**
     * Search again all occurrences of the text to find in the searched text, after it was changed.
     * <p>
     * The current result becomes the first result found at the position of the previous current result or after
     * (or the last result, if none), so that finding the next result continues from the same place.
     * Without a previous current result, there is no current result.
     *
     * @return all found results
     */
    public abstract List<FindResult> refresh();

    public boolean containsText() {
        return findAll()
            .findFirst()
            .isPresent();
    }
}
