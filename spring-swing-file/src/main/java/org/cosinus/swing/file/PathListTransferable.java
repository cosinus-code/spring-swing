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

package org.cosinus.swing.file;

import lombok.Getter;
import lombok.Setter;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.awt.datatransfer.DataFlavor.javaFileListFlavor;
import static java.awt.datatransfer.DataFlavor.stringFlavor;
import static java.lang.System.lineSeparator;
import static java.util.Arrays.stream;

public class PathListTransferable implements Transferable {

    public static final DataFlavor PATH_FLAVOR = new DataFlavor(Path.class, "Path");

    public static final DataFlavor[] PATH_FLAVORS = new DataFlavor[]{
        javaFileListFlavor,
        stringFlavor,
        PATH_FLAVOR
    };

    @Getter
    private final PathListTransferData paths;

    public PathListTransferable(final List<Path> paths) {
        this.paths = new PathListTransferData(paths);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (flavor.equals(PATH_FLAVOR)) {
            return paths;
        }

        if (flavor.equals(javaFileListFlavor)) {
            return paths.stream()
                .map(Path::toFile)
                .toList();
        }

        if (flavor.equals(stringFlavor)) {
            return paths
                .stream()
                .map(Object::toString)
                .collect(Collectors.joining(lineSeparator()));
        }

        throw new UnsupportedFlavorException(flavor);
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return PATH_FLAVORS;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return stream(PATH_FLAVORS).anyMatch(flavor::equals);
    }

    public static class PathListTransferData extends ArrayList<Path> {

        @Getter
        @Setter
        private boolean moveTransfer;

        @Getter
        @Setter
        private String viewId;

        PathListTransferData(final List<Path> paths) {
            super(paths);
        }
    }
}