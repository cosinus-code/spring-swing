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
import org.apache.commons.exec.CommandLine;
import org.cosinus.swing.form.control.ControlValue;
import org.cosinus.swing.image.icon.IconProvider;
import org.cosinus.swing.image.icon.IconSize;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.io.File;
import java.util.Objects;
import java.util.Set;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

@Getter
@Setter
public class Application implements ControlValue {

    private static final Set<String> FILE_OPTIONS = Set.of("%f", "%F", "%u", "%U");

    @Autowired
    private IconProvider iconProvider;

    private final String id;

    private final String name;

    private final String executable;

    private final String translatedName;

    private final String comment;

    private final String translatedComment;

    private final String iconName;

    private final IconSize iconSize;

    private final boolean runInTerminal;

    public Application(String id,
                       String name,
                       String executable,
                       String translatedName,
                       String comment,
                       String translatedComment,
                       String iconName,
                       IconSize iconSize,
                       boolean runInTerminal) {
        injectContext(this);
        this.id = id;
        this.name = name;
        this.executable = executable;
        this.translatedName = translatedName;
        this.comment = comment;
        this.translatedComment = translatedComment;
        this.iconName = iconName;
        this.iconSize = iconSize;
        this.runInTerminal = runInTerminal;
    }

    public String[] getCommandToExecuteFile(File file) {
        return stream(CommandLine.parse(executable).toStrings())
            .map(option -> FILE_OPTIONS.contains(option) ? file.getAbsolutePath() : option)
            .toArray(String[]::new);
    }
    @Override
    public Icon getIcon() {
        return ofNullable(iconName)
            .flatMap(name -> iconProvider.findIconByName(name, iconSize))
            .orElse(null);
    }

    @Override
    public String getTooltip() {
        return ofNullable(translatedComment)
            .orElse(comment);
    }

    @Override
    public String toString() {
        return ofNullable(translatedName)
            .orElse(name);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Application that)) {
            return false;
        }
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
