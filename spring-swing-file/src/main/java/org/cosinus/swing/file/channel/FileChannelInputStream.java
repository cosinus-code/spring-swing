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

import lombok.Getter;
import org.cosinus.swing.boot.cleanup.ShutDownResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileChannelInputStream extends InputStream implements ShutDownResource {

    @Getter
    private final FileChannel fileChannel;

    private boolean read;

    public FileChannelInputStream(final FileChannel fileChannel) {
        this.fileChannel = fileChannel;
    }

    @Override
    public int read(byte[] bytes) throws IOException {
        return super.read(bytes);
    }

    @Override
    public int read() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(1);
        int count = readFromChannel(buffer, -1);
        return count > 0 ? buffer.getInt() : count;
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(length);
        int count = readFromChannel(buffer, offset);
        if (count >= 0) {
            buffer.flip();
            buffer.get(bytes);
        }
        return count;
    }

    protected int readFromChannel(ByteBuffer buffer, long position) throws IOException {
        if (read) {
            return -1;
        }

        int count = position >= 0 ?
            fileChannel.read(buffer, position) :
            fileChannel.read(buffer);

        read = true;
        return count;
    }

    @Override
    public void shutDown() throws IOException {
        close();
    }
}
