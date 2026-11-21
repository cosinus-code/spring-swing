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

package org.cosinus.swing.image.worker;

import lombok.extern.slf4j.Slf4j;
import org.cosinus.stream.StreamSupplier;
import org.cosinus.stream.consumer.StreamConsumer;
import org.cosinus.swing.icon.IconSize;
import org.cosinus.swing.image.icon.IconHandler;
import org.cosinus.swing.progress.ProgressModel;
import org.cosinus.swing.worker.StreamWorker;
import org.cosinus.swing.worker.WorkerModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

@Slf4j
public class LoadIconsWorker extends StreamWorker<WorkerModel<File>, File, ProgressModel> {

    public static final String UPDATE_ICONS_ACTION_ID = "update-icons";

    @Autowired
    private IconHandler iconHandler;

    private final IconSize iconSize;

    public LoadIconsWorker(final IconSize iconSize,
                           final WorkerModel<File> model,
                           final StreamSupplier<File> streamSupplier) {
        super(UPDATE_ICONS_ACTION_ID, model, streamSupplier, new ProgressModel());
        this.iconSize = iconSize;
    }

    @Override
    protected StreamConsumer<File> streamConsumer() {
        return file -> {
            if (iconHandler.getIconByFileFromCache(file, iconSize).isEmpty()) {
                iconHandler.cacheIconByFile(file, iconSize);
            }
        };
    }
}
