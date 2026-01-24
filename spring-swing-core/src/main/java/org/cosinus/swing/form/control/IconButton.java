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

package org.cosinus.swing.form.control;

import lombok.Getter;import lombok.Setter;
import lombok.extern.slf4j.Slf4j;import org.cosinus.swing.dialog.DialogHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static javax.imageio.ImageIO.read;

@Slf4j
public class IconButton extends Button {

    @Autowired
    protected DialogHandler dialogHandler;

    @Getter
    protected File iconFile;

    public IconButton() {
        initComponents();
    }

    public IconButton(Icon icon) {
        super(icon);
        initComponents();
    }

    protected void initComponents() {
        addActionListener(event ->
            ofNullable(dialogHandler.chooseFile(null, false))
                .ifPresent(this::setIconFile));
    }

    public void setIconFile(File iconFile) {
        ofNullable(iconFile)
            .flatMap(this::createIcon)
            .ifPresent(icon -> {
                setIcon(icon);
                this.iconFile = iconFile;
            });
    }

    protected Optional<Icon> createIcon(File file) {
        try {
            return ofNullable(read(file))
                .map(ImageIcon::new);
        } catch (IOException e) {
            log.warn("Failed to create icon from file: {}", file, e);
            return Optional.empty();
        }
    }
}
