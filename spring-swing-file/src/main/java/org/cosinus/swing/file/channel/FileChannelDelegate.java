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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class FileChannelDelegate extends FileChannel {

    protected final FileChannel fileChannel;

    public FileChannelDelegate(final FileChannel fileChannel) {
        this.fileChannel = fileChannel;
    }

    public void force(boolean metaData) throws IOException {
        this.fileChannel.force(metaData);
    }

    public FileLock lock(long position, long size, boolean shared) throws IOException {
        return this.fileChannel.lock(position, size, shared);
    }

    public MappedByteBuffer map(FileChannel.MapMode mode, long position, long size) throws IOException {
        return this.fileChannel.map(mode, position, size);
    }

    public long position() throws IOException {
        return this.fileChannel.position();
    }

    public FileChannel position(long newPosition) throws IOException {
        return this.fileChannel.position(newPosition);
    }

    public int read(ByteBuffer dst, long position) throws IOException {
        return this.fileChannel.read(dst, position);
    }

    public int read(ByteBuffer dst) throws IOException {
        return this.fileChannel.read(dst);
    }

    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        return this.fileChannel.read(dsts, offset, length);
    }

    public long size() throws IOException {
        return this.fileChannel.size();
    }

    public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException {
        return this.fileChannel.transferFrom(src, position, count);
    }

    public long transferTo(long position, long count, WritableByteChannel target) throws IOException {
        return this.fileChannel.transferTo(position, count, target);
    }

    public FileChannel truncate(long size) throws IOException {
        return this.fileChannel.truncate(size);
    }

    public FileLock tryLock(long position, long size, boolean shared) throws IOException {
        return this.fileChannel.tryLock(position, size, shared);
    }

    public int write(ByteBuffer src, long position) throws IOException {
        return this.fileChannel.write(src, position);
    }

    public int write(ByteBuffer src) throws IOException {
        return this.fileChannel.write(src);
    }

    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        return this.fileChannel.write(srcs, offset, length);
    }

    @Override
    protected void implCloseChannel() throws IOException {
        this.fileChannel.close();
    }
}
