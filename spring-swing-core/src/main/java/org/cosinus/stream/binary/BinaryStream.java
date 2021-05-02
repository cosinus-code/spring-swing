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
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

import static java.util.stream.StreamSupport.stream;

/**
 * A {@link Stream} of chunks of byte arrays read from an input stream
 */
public class BinaryStream extends StreamDelegate<byte[]> {

    private final InputStream inputStream;

    public BinaryStream(Stream<byte[]> delegate,
                        InputStream inputStream) {
        super(delegate);
        this.inputStream = inputStream;
    }

    /**
     * Provide a {@link BinaryStream} based on a {@link BinarySpliterator}.
     *
     * @param inputStream the input stream source to stream
     * @param bufferSize  the size of the chunks of data
     * @return the binary stream
     */
    public static BinaryStream of(InputStream inputStream, int bufferSize) {
        Objects.requireNonNull(inputStream);
        BinarySpliterator spliterator = new BinarySpliterator(inputStream, bufferSize);
        return new BinaryStream(stream(spliterator, false), inputStream);
    }

    /**
     * Allows to skip a number of bytes from the input stream source.
     *
     * @param n the number of bytes to be skipped
     * @return the actual number of bytes skipped.
     * @throws IOException if an I/O error occurs during skip.
     */
    public long skipBytes(long n) throws IOException {
        return inputStream.skip(n);
    }

    /**
     * Retrieve the checksum of the input stream content
     * if the source input stream is of {@link CheckedInputStream} type.
     *
     * @return the checksum of the input stream content
     */
    public Optional<String> checksum() {
        return Optional.of(inputStream)
            .filter(input -> CheckedInputStream.class.isAssignableFrom(input.getClass()))
            .map(CheckedInputStream.class::cast)
            .map(CheckedInputStream::getChecksum)
            .map(Checksum::getValue)
            .map(Objects::toString);

    }

    /**
     * Closes this input stream and releases any system resources associated
     * with the stream.
     */
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
