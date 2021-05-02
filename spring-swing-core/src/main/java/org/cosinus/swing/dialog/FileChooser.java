/*
 * Copyright 2020 Cosinus Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cosinus.swing.dialog;

import org.cosinus.swing.store.ApplicationStorage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.io.File;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Extension of the {@link JFileChooser}
 * which will automatically inject the application context.
 */
public class FileChooser extends JFileChooser {

    private static final String FILE_CHOOSER = "file-chooser";

    @Autowired
    private ApplicationStorage applicationStorage;

    public FileChooser() {
        this(null);
    }

    public FileChooser(File file) {
        injectContext(this);
        File currentFile = ofNullable(file)
            .orElseGet(() -> getLatestFilePath()
                .map(File::new)
                .orElseGet(this::getDefaultFile));
        setCurrentDirectory(currentFile);
    }

    @Override
    public void setSelectedFile(File file) {
        super.setSelectedFile(file);
        saveLatestFilePath(file);
    }

    protected File getDefaultFile() {
        return new File(System.getProperty("user.home"));
    }

    protected Optional<String> getLatestFilePath() {
        return ofNullable(applicationStorage.getString(FILE_CHOOSER));
    }

    protected void saveLatestFilePath(File currentFile) {
        ofNullable(currentFile)
            .ifPresent(file -> applicationStorage.saveString(FILE_CHOOSER, file.getAbsolutePath()));
    }
}
