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

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * The result of a {@link TextFinder} action
 */
public record FindResult(String text, int index, int startPosition, int endPosition) {

    /**
     * Get this result moved after a text inserted in the searched text.
     * <p>
     * A text inserted at the start of the result or before is not part of the result: the result is moved after it.
     * A text inserted at the end of the result or after is not part of the result: the result is not changed.
     * A text inserted inside the result is part of the result.
     *
     * @param offset the offset of the inserted text
     * @param length the length of the inserted text
     * @return the moved result
     */
    public FindResult textInserted(int offset, int length) {
        int start = startPosition >= offset ? startPosition + length : startPosition;
        int end = endPosition > offset ? endPosition + length : endPosition;
        return withPositions(start, max(start, end));
    }

    /**
     * Get this result moved after a text removed from the searched text.
     * <p>
     * The removed part of the result is removed from the result,
     * so a result entirely removed becomes empty, at the offset of the removed text.
     *
     * @param offset the offset of the removed text
     * @param length the length of the removed text
     * @return the moved result
     */
    public FindResult textRemoved(int offset, int length) {
        return withPositions(
            removed(startPosition, offset, length),
            removed(endPosition, offset, length));
    }

    private static int removed(int position, int offset, int length) {
        if (position >= offset + length) {
            return position - length;
        }
        return min(position, offset);
    }

    private FindResult withPositions(int start, int end) {
        return start == startPosition && end == endPosition ?
            this :
            new FindResult(text, index, start, end);
    }
}
