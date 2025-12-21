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

package org.cosinus.swing.progress;

import lombok.Getter;
import lombok.Setter;

/**
 * Progress model
 */
public class ProgressModel {

    @Setter
    @Getter
    private long progressTotalSize;

    @Getter
    private long progressDone;

    @Getter
    private int progressPercent;

    private long startTime;

    @Getter
    private long speed;

    @Getter
    private long remainingTime;

    public void startProgress() {
        startTime = System.currentTimeMillis();
        this.progressDone = 0;
        this.progressPercent = 0;
    }

    public void addTotalProgress(long value) {
        progressTotalSize += value;
    }

    public void startProgress(long totalProgressSize) {
        setProgressTotalSize(totalProgressSize);
        startProgress();
    }

    public void addProgress(long value) {
        updateProgress(progressDone + value);
    }

    public void updateProgress(long progressDone) {
        updateProgress(progressDone, progressTotalSize);
    }

    public void updateProgress(long progressDone, long progressTotalSize) {
        this.progressDone = progressDone;
        this.progressTotalSize = progressTotalSize;
        if (progressTotalSize != 0) {
            progressPercent = (int) ((progressDone * 100) / progressTotalSize);
        }

        long spentTime = System.currentTimeMillis() - startTime;
        if (spentTime > 0) {
            speed = 1000 * progressDone / spentTime;
            remainingTime = progressDone != 0 ?
                (progressTotalSize - progressDone) * spentTime / (1000 * progressDone) :
                0;
        }
    }

    public void finishProgress() {
        progressPercent = 100;
    }
}
