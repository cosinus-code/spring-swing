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

package org.cosinus.swing.image.icon;

import org.cosinus.stream.Streams;
import org.cosinus.swing.icon.IconHolder;
import org.cosinus.swing.progress.ProgressModel;
import org.cosinus.swing.worker.StreamWorker;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;

import static java.util.Arrays.stream;

public class UpdateIconsWorker extends StreamWorker<IconInitializerModel, IconHolder, ProgressModel> {

    public static final String ACTION_ID = "update-icons";

    @Autowired
    private IconHandler iconHandler;

    protected UpdateIconsWorker() {
        super(ACTION_ID,
            new IconInitializerModel(),
            () -> stream(Frame.getWindows())
                .filter(Component::isVisible)
                .flatMap(Streams::flatComponentsStream)
                .filter(component -> component instanceof IconHolder)
                .map(IconHolder.class::cast),
            new ProgressModel());
    }

    @Override
    public void beforePipelineOpen() {
        iconHandler.resetIcons();
    }
}
