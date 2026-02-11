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

package org.cosinus.swing.image;

import lombok.extern.slf4j.Slf4j;
import org.cosinus.stream.StreamSupplier;
import org.cosinus.stream.consumer.StreamConsumer;
import org.cosinus.swing.progress.ProgressModel;
import org.cosinus.swing.worker.StreamWorker;
import org.cosinus.swing.worker.WorkerModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;

import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

@Slf4j
public class LoadThumbnailsWorker extends StreamWorker<WorkerModel<File>, File, ProgressModel> {

    public static final String UPDATE_THUMBNAILS_ACTION_ID = "update-thumbnails";

    @Autowired
    private ImageHandler imageHandler;

    private final int thumbnailSize;

    private final boolean quickThumbnail;

    public LoadThumbnailsWorker(final int thumbnailSize,
                                final WorkerModel<File> model,
                                final StreamSupplier<File> streamSupplier,
                                final boolean quickThumbnail) {
        super(UPDATE_THUMBNAILS_ACTION_ID,
            model,
            streamSupplier,
            new ProgressModel());
        injectContext(this);
        this.thumbnailSize = thumbnailSize;
        this.quickThumbnail = quickThumbnail;
    }

    @Override
    protected StreamConsumer<File> streamConsumer() {
        return file -> {
            try {
                if (quickThumbnail) {
                    imageHandler.createQuickThumbnail(file, thumbnailSize);
                } else {
                    imageHandler.createThumbnail(file, thumbnailSize);
                }
            } catch (IOException e) {
                log.error("Cannot create preview icon for file: {}", file);
            }
        };
    }
}
