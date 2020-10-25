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

import org.cosinus.stream.StreamDelegate;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

public class BinaryStream extends StreamDelegate<byte[]> {

    private final InputStream inputStream;

    public BinaryStream(Stream<byte[]> delegate,
                        InputStream inputStream) {
        super(delegate);
        this.inputStream = inputStream;
    }

    public static BinaryStream of(InputStream inputStream, int bufferSize) {
        Objects.requireNonNull(inputStream);
        BinarySpliterator spliterator = new BinarySpliterator(inputStream, bufferSize);
        return new BinaryStream(StreamSupport.stream(spliterator, false), inputStream);
    }

    public long skipBytes(long n) throws IOException {
        return inputStream.skip(n);
    }

    public Optional<String> checksum() {
        return Optional.of(inputStream)
                .filter(input -> CheckedInputStream.class.isAssignableFrom(input.getClass()))
                .map(CheckedInputStream.class::cast)
                .map(CheckedInputStream::getChecksum)
                .map(Checksum::getValue)
                .map(Objects::toString);

    }

    @Override
    public void close() {
        super.close();

        try {
            inputStream.close();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
