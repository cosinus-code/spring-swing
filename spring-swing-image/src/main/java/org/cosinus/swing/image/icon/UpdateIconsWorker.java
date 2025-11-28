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
import org.cosinus.stream.pipeline.PipelineStrategy;
import org.cosinus.swing.action.execute.ActionModel;
import org.cosinus.swing.icon.IconHolder;
import org.cosinus.swing.worker.DirectPipelineWorker;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

public class UpdateIconsWorker extends DirectPipelineWorker<IconInitializerModel, IconHolder> {

    @Autowired
    private IconHandler iconHandler;

    protected UpdateIconsWorker() {
        super(new ActionModel("icon-initializer", "icon-initializer", "icon-initializer"),
            new IconInitializerModel());
    }

    @Override
    protected void doWork() {
        iconHandler.resetIcons();
        super.doWork();
    }

    @Override
    public Stream<IconHolder> openPipelineInputStream(PipelineStrategy pipelineStrategy) {
        return stream(Frame.getWindows())
            .filter(Component::isVisible)
            .flatMap(Streams::flatComponentsStream)
            .filter(component -> component instanceof IconHolder)
            .map(IconHolder.class::cast);
    }
}
