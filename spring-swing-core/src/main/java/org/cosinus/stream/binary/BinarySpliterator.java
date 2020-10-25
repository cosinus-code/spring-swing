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

package org.cosinus.stream.binary;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Spliterators;
import java.util.function.Consumer;

import static java.lang.Long.MAX_VALUE;

public class BinarySpliterator extends Spliterators.AbstractSpliterator<byte[]> {

    private final InputStream inputStream;

    private final byte[] buffer;

    public BinarySpliterator(InputStream inputStream, int bufferSize) {
        super(MAX_VALUE, ORDERED | NONNULL);
        this.inputStream = inputStream;
        this.buffer = new byte[bufferSize];
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public boolean tryAdvance(Consumer<? super byte[]> action) {
        int readSize = tryRead();
        if (readSize <= 0) {
            return false;
        }

        action.accept(readSize == buffer.length ?
                buffer :
                Arrays.copyOf(buffer, readSize));
        return true;
    }

    protected int tryRead() {
        try {
            return inputStream.read(buffer);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
