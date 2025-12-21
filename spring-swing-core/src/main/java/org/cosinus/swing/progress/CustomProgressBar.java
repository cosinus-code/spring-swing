/*
 *
 *  * Copyright 2024 Cosinus Software
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */
package org.cosinus.swing.progress;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cosinus.swing.form.CustomProgressBarUI;
import org.cosinus.swing.form.ProgressBar;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;

import static javax.swing.SwingUtilities.invokeLater;
import static org.cosinus.swing.border.Borders.emptyBorder;

@Slf4j
public class CustomProgressBar extends ProgressBar implements ProgressListener<ProgressModel> {

    @Autowired
    private ApplicationUIHandler uiHandler;

    @Getter
    private final ProgressModel progressModel;

    public CustomProgressBar() {
        this.progressModel = new ProgressModel();
        if (uiHandler.isLookAndFeelMac()) {
            setUI(new CustomProgressBarUI());
            setBorder(emptyBorder(0));
        }
    }

    @Override
    public void paint(Graphics g) {
        try {
            super.paint(g);
        } catch (Exception ex) {
            log.debug(ex.getMessage(), ex);
        }
    }

    public void startLoading() {
        startLoading(-1);
    }

    public void startLoading(long totalSizeToLoad) {
        progressModel.startProgress(totalSizeToLoad);
    }

    public void updateLoading(long loadedSize, long totalSizeToLoad) {
        if (totalSizeToLoad > 0 && loadedSize > 0) {
            progressModel.updateProgress(loadedSize, totalSizeToLoad);
        }
    }

    public void finishLoading() {
        progressModel.finishProgress();
    }

    @Override
    public void progressStarted(ProgressModel progressModel) {
        startLoading();
        progressUpdated(progressModel);
    }

    @Override
    public void progressUpdated(ProgressModel progressModel) {
        int progressPercent = progressModel.getProgressPercent();
        long totalSizeToLoad = progressModel.getProgressTotalSize();
        if (totalSizeToLoad > 0) {
            setMaximum(100);
        }

        setValue(progressPercent);
        setIndeterminate(progressPercent <= 0);

        if (progressPercent == 100) {
            invokeLater(() -> setValue(0));
        }
    }

    @Override
    public void progressFinished(ProgressModel progressModel) {
        finishLoading();
        progressUpdated(progressModel);
    }
}
