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

package org.cosinus.swing.image.video;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Optional;

/**
 * Reader of the cover image embedded in a video container, like the cover art of a movie.
 * <p>
 * Only the container structure is read, without decoding the video.
 */
public interface VideoCoverReader {

    /**
     * The maximum size of a cover image to read, to avoid loading huge data from a corrupted file.
     */
    int MAX_COVER_SIZE = 16 * 1024 * 1024;

    /**
     * Check if the video container is supported, based on the file header.
     *
     * @param channel the video file channel
     * @return true if the cover can be searched by this reader
     * @throws IOException if the file cannot be read
     */
    boolean supports(FileChannel channel) throws IOException;

    /**
     * Read the embedded cover image.
     *
     * @param channel the video file channel
     * @return the encoded cover image bytes (usually JPEG or PNG), or empty if there is no cover
     * @throws IOException if the file cannot be read
     */
    Optional<byte[]> readCover(FileChannel channel) throws IOException;

    /**
     * Read bytes from a position in the file.
     *
     * @param channel  the file channel
     * @param position the position to read from
     * @param length   the number of bytes to read
     * @return the buffer with the bytes, ready to be read
     * @throws IOException if the file cannot be read or has less bytes than requested
     */
    default ByteBuffer read(final FileChannel channel, final long position, final int length) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(length);
        while (buffer.hasRemaining()) {
            if (channel.read(buffer, position + buffer.position()) < 0) {
                throw new EOFException("Unexpected end of file at position " + (position + buffer.position()));
            }
        }
        return buffer.flip();
    }

    /**
     * Read the bytes of a cover image, checking its size.
     *
     * @param channel  the file channel
     * @param position the position of the image
     * @param length   the image length
     * @return the image bytes, or empty if the length is not valid
     * @throws IOException if the file cannot be read
     */
    default Optional<byte[]> readCoverBytes(final FileChannel channel,
                                            final long position,
                                            final long length) throws IOException {
        return length <= 0 || length > MAX_COVER_SIZE ?
            Optional.empty() :
            Optional.of(read(channel, position, (int) length).array());
    }
}
