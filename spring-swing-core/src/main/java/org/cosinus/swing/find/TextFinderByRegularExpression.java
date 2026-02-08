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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterators.AbstractSpliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.Long.MAX_VALUE;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.regex.Pattern.quote;
import static java.util.stream.StreamSupport.stream;

/**
 * Implementation of {@link TextFinder} based on regular expression matcher
 */
public class TextFinderByRegularExpression extends TextFinder {

    private final Matcher matcher;

    private final Map<Integer, FindResult> findResultsMap;

    private final AtomicInteger index;

    private final AtomicInteger count;

    public TextFinderByRegularExpression(String inputText, FindText textToFind) {
        super(inputText, textToFind);
        matcher = initPattern();
        findResultsMap = new LinkedHashMap<>();
        index = new AtomicInteger(0);
        count = new AtomicInteger(0);
    }

    private Matcher initPattern() {
        return Optional.of(textToFind.getText())
            .map(text -> textToFind.isCaseSensitive() ? text : text.toLowerCase())
            .map(text -> textToFind.isRegularExpression() ? text : quote(text))
            .map(text -> textToFind.isWholeWord() ? "\\b(" + text + ")\\b" : text)
            .map(Pattern::compile)
            .map(pattern -> pattern.matcher(textToFind.isCaseSensitive() ? inputText : inputText.toLowerCase()))
            .orElseThrow(() -> new IllegalArgumentException("Invalid regular expression: " + textToFind.getText()));
    }

    @Override
    public Optional<FindResult> findNext(int startPosition) {
        if (index.get() >= count.get()) {
            return empty();
        }
        return ofNullable(findResultsMap.get(index.incrementAndGet()))
            .or(() -> matcher.find(startPosition) ? Optional.of(createFindResult()) : empty());
    }

    @Override
    public Optional<FindResult> findNext() {
        if (index.get() >= count.get()) {
            return empty();
        }
        return ofNullable(findResultsMap.get(index.incrementAndGet()))
            .or(() -> matcher.find() ? Optional.of(createFindResult()) : empty());
    }

    @Override
    public Optional<FindResult> findPrevious() {
        if (index.get() <= 1) {
            return empty();
        }
        return ofNullable(findResultsMap.get(index.decrementAndGet()));
    }

    @Override
    public Stream<FindResult> findAll() {
        return stream(new FindSpliterator(), false)
            .onClose(() -> index.set(0));
    }

    @Override
    public int count() {
        return count.get();
    }

    @Override
    public int currentIndex() {
        return index.get();
    }

    @Override
    public String replaceNext(String replacementText) {
        return matcher.replaceFirst(replacementText);
    }

    @Override
    public String replaceAll(String replacementText) {
        return matcher.replaceAll(replacementText);
    }

    @Override
    public FindResult getCurrentFind() {
        return findResultsMap.get(index.get());
    }

    private FindResult createFindResult() {
        FindResult findResult = new FindResult(matcher.group(), index.get(), matcher.start(), matcher.end());
        findResultsMap.put(index.get(), findResult);
        return findResult;
    }

    /**
     * Spliterator used to create the stream of found results
     */
    private class FindSpliterator extends AbstractSpliterator<FindResult> {
        protected FindSpliterator() {
            super(MAX_VALUE, ORDERED | NONNULL);
        }

        @Override
        public boolean tryAdvance(Consumer<? super FindResult> action) {
            boolean found = matcher.find();
            if (found) {
                count.incrementAndGet();
                index.incrementAndGet();
                action.accept(createFindResult());
            }
            return found;
        }
    }

    @Override
    public boolean containsText() {
        if (textToFind.isWholeWord() || textToFind.isRegularExpression()) {
            return super.containsText();
        }
        return textToFind.isCaseSensitive() ?
            inputText.contains(textToFind.getText()) :
            inputText.toLowerCase().contains(textToFind.getText().toLowerCase());
    }
}
