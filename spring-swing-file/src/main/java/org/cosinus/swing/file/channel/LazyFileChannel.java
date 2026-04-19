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

package org.cosinus.swing.file.channel;

import lombok.Setter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static java.lang.Math.min;
import static org.cosinus.swing.format.FormatHandler.KILO_INT;

public class LazyFileChannel extends FileChannelDelegate {

    public static final int DEFAULT_PAGE_SIZE = KILO_INT;

    public static final int DEFAULT_PAGE_CONT = 3;

    protected ByteBuffer buffer;

    private long bufferStart = -1;

    private int bufferLimit;

    @Setter
    protected int pageSize;

    public LazyFileChannel(final FileChannel fileChannel) {
        this(fileChannel, DEFAULT_PAGE_CONT, DEFAULT_PAGE_SIZE);
    }

    public LazyFileChannel(final FileChannel fileChannel, int pageCount, int pageSize) {
        super(fileChannel);
        this.pageSize = pageSize;
        this.buffer = ByteBuffer.allocate(pageCount * pageSize);
    }

    @Override
    public int read(ByteBuffer destination, long position) throws IOException {
        if (!isOpen()) {
            throw new IOException("Channel is closed");
        }

        if (!isPageInBufferAtPosition(position)) {
            refillBuffer(position);
            if (bufferLimit <= 0) {
                return -1;
            }
        }

        int offset = (int) (position - bufferStart);
        int available = bufferLimit - offset;
        int length = min(destination.remaining(), available);
        if (length <= 0) {
            return -1;
        }

        ByteBuffer temp = buffer.duplicate();
        temp.position(offset);
        temp.limit(offset + length);

        destination.put(temp);
        return length;
    }

    private boolean isPageInBufferAtPosition(long position) {
        return bufferStart >= 0 &&
            position >= bufferStart &&
            position + pageSize < bufferStart + bufferLimit;
    }

    private void refillBuffer(long position) throws IOException {
        buffer.clear();

        bufferStart = (position / pageSize) * pageSize;
        int bytesRead = super.read(buffer, bufferStart);
        if (bytesRead > 0) {
            bufferLimit = bytesRead;
        }
    }

}
